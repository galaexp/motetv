package com.example.tvremote.network.protocol

import android.content.Context
import android.util.Log
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket

/**
 * Android TV Remote Protocol v2 implementation (Google TV, Android TV 9/10/11/12/13/14, Xiaomi Mi Stick, Shield TV).
 * Supports mutual TLS (mTLS) Polo pairing on port 6467 and live protobuf control channel on port 6466.
 */
class AndroidTvV2Protocol(private val context: Context) : TvProtocolHandler {
    private val tag = "AndroidTvV2Protocol"
    private val certManager = TvCertificateManager(context)

    // Control session (Port 6466)
    private var rawControlSocket: Socket? = null
    private var controlSocket: SSLSocket? = null
    private var controlOutput: OutputStream? = null
    private var controlInput: InputStream? = null

    // Pairing session (Port 6467)
    private var rawPairingSocket: Socket? = null
    private var pairingSocket: SSLSocket? = null
    private var pairingOutput: OutputStream? = null
    private var pairingInput: InputStream? = null
    private var serverCertificate: X509Certificate? = null
    private var activeProtocolVersion: Int = 1

    private var targetDevice: TvDevice? = null
    private var connected = false

    override val isConnected: Boolean
        get() = connected && controlSocket?.isConnected == true && !(controlSocket?.isClosed ?: true)

    override suspend fun connect(
        device: TvDevice,
        onPairingRequired: (prompt: String) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        targetDevice = device
        disconnect()

        val sslFactory = certManager.getSslSocketFactory()

        // 1. If device was previously paired, attempt direct control on port 6466
        if (device.isPaired) {
            try {
                Log.d(tag, "Device marked as paired. Trying direct control on ${device.ipAddress}:6466...")
                val rawControl = Socket()
                rawControl.connect(InetSocketAddress(device.ipAddress, 6466), 4000)
                rawControl.tcpNoDelay = true
                rawControl.soTimeout = 10000

                val ctrlSock = sslFactory.createSocket(rawControl, device.ipAddress, 6466, true) as SSLSocket
                ctrlSock.startHandshake()

                rawControlSocket = rawControl
                controlSocket = ctrlSock
                controlOutput = ctrlSock.outputStream
                controlInput = ctrlSock.inputStream

                val configureBytes = PoloProtocolHelper.buildRemoteConfigure()
                PoloProtocolHelper.writeFramed(controlOutput!!, configureBytes)

                connected = true
                Log.i(tag, "Successfully established direct control session on port 6466")
                return@withContext Result.success(true)
            } catch (e: Exception) {
                Log.w(tag, "Direct control port 6466 failed (${e.message}), proceeding to pairing handshake on port 6467")
                disconnect()
            }
        }

        // 2. Perform Polo pairing handshake on port 6467 with standard "atvremote" v2
        val pairingResult = executePairingHandshake(
            device = device,
            sslFactory = sslFactory,
            serviceName = "atvremote",
            protocolVersion = 2,
            onPairingRequired = onPairingRequired
        )
        if (pairingResult.isSuccess) {
            return@withContext pairingResult
        }

        // 3. Fallback: retry with "atvremote" v1 and "androidtvremote"
        Log.w(tag, "First pairing attempt with atvremote v2 failed. Retrying with atvremote v1...")
        disconnect()
        try { Thread.sleep(300) } catch (_: Exception) {}

        val fallback1 = executePairingHandshake(
            device = device,
            sslFactory = sslFactory,
            serviceName = "atvremote",
            protocolVersion = 1,
            onPairingRequired = onPairingRequired
        )
        if (fallback1.isSuccess) {
            return@withContext fallback1
        }

        Log.w(tag, "Second pairing attempt failed. Retrying with service name 'androidtvremote'...")
        disconnect()
        try { Thread.sleep(300) } catch (_: Exception) {}

        return@withContext executePairingHandshake(
            device = device,
            sslFactory = sslFactory,
            serviceName = "androidtvremote",
            protocolVersion = 1,
            onPairingRequired = onPairingRequired
        )
    }

    private suspend fun executePairingHandshake(
        device: TvDevice,
        sslFactory: javax.net.ssl.SSLSocketFactory,
        serviceName: String,
        protocolVersion: Int,
        onPairingRequired: (prompt: String) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.i(tag, "Connecting raw TCP to ${device.ipAddress}:6467 for pairing ($serviceName, v$protocolVersion)...")
            val rawSock = Socket()
            rawSock.connect(InetSocketAddress(device.ipAddress, 6467), 7000)
            rawSock.tcpNoDelay = true
            rawSock.soTimeout = 15000

            Log.i(tag, "Wrapping socket in SSL and performing mTLS handshake with TV...")
            val pairSock = sslFactory.createSocket(rawSock, device.ipAddress, 6467, true) as SSLSocket
            pairSock.startHandshake()

            rawPairingSocket = rawSock
            pairingSocket = pairSock
            pairingOutput = pairSock.outputStream
            pairingInput = pairSock.inputStream
            activeProtocolVersion = protocolVersion

            // Capture TV's server certificate from TLS session
            val session = pairSock.session
            val peerCerts = session.peerCertificates
            serverCertificate = peerCerts.firstOrNull() as? X509Certificate
            Log.i(tag, "mTLS handshake successful on port 6467! TV Server cert: ${serverCertificate?.subjectDN}")

            // Step 1: Send Polo PairingRequest (type 10)
            val requestPacket = PoloProtocolHelper.buildPairingRequestMessage(
                clientName = "Nova Remote",
                serviceName = serviceName,
                protocolVersion = protocolVersion
            )
            PoloProtocolHelper.writeFramed(pairingOutput!!, requestPacket)
            Log.d(tag, "Dispatched Polo PairingRequest (type 10, service=$serviceName, v=$protocolVersion)")

            // Step 2: Read TV's PairingRequestAck (type 11)
            val ackBytes = PoloProtocolHelper.readFramed(pairingInput!!)
                ?: throw IllegalStateException("No response received from TV after PairingRequest")
            val ackMsg = PoloProtocolHelper.parseOuterMessage(ackBytes)
            Log.d(tag, "Received TV response to PairingRequest: type=${ackMsg.type}, status=${ackMsg.status}, ver=${ackMsg.protocolVersion}")

            if (ackMsg.status != PoloProtocolHelper.STATUS_OK && ackMsg.status != 0 && ackMsg.status != 200) {
                throw IllegalStateException("TV returned non-OK status ${ackMsg.status} for PairingRequest")
            }

            // Step 3: Send Options (type 20)
            val optionsPacket = PoloProtocolHelper.buildOptionsMessage(protocolVersion = protocolVersion)
            PoloProtocolHelper.writeFramed(pairingOutput!!, optionsPacket)
            Log.d(tag, "Dispatched Polo Options (type 20)")

            // Step 4: Exchange Configuration with TV
            var configComplete = false
            for (step in 0 until 5) {
                val incomingBytes = PoloProtocolHelper.readFramed(pairingInput!!) ?: break
                val incomingMsg = PoloProtocolHelper.parseOuterMessage(incomingBytes)
                Log.d(tag, "Polo exchange step $step: type=${incomingMsg.type}, status=${incomingMsg.status}")

                when (incomingMsg.type) {
                    PoloProtocolHelper.MESSAGE_TYPE_PAIRING_REQUEST_ACK -> {
                        val opt = PoloProtocolHelper.buildOptionsMessage(protocolVersion = protocolVersion)
                        PoloProtocolHelper.writeFramed(pairingOutput!!, opt)
                    }
                    PoloProtocolHelper.MESSAGE_TYPE_OPTIONS -> {
                        // TV sent Options -> Send Configuration (type 30)
                        val configPacket = PoloProtocolHelper.buildConfigurationMessage(
                            symbolLength = 6,
                            encodingType = PoloProtocolHelper.ENCODING_TYPE_HEXADECIMAL,
                            protocolVersion = protocolVersion
                        )
                        PoloProtocolHelper.writeFramed(pairingOutput!!, configPacket)
                        Log.d(tag, "Dispatched Configuration (type 30) to TV")
                    }
                    PoloProtocolHelper.MESSAGE_TYPE_CONFIGURATION -> {
                        // TV sent Configuration -> Send ConfigurationAck (type 31)
                        val configAck = PoloProtocolHelper.buildConfigurationAckMessage(protocolVersion = protocolVersion)
                        PoloProtocolHelper.writeFramed(pairingOutput!!, configAck)
                        Log.d(tag, "Dispatched ConfigurationAck (type 31) to TV")
                        configComplete = true
                        break
                    }
                    PoloProtocolHelper.MESSAGE_TYPE_CONFIGURATION_ACK -> {
                        Log.d(tag, "TV acknowledged configuration (type 31)")
                        configComplete = true
                        break
                    }
                }
            }

            if (!configComplete) {
                throw IllegalStateException("Pairing handshake did not reach Configuration completion")
            }

            // TV has acknowledged configuration and is NOW actively displaying the PIN overlay!
            Log.i(tag, "Configuration confirmed! TV is now actively displaying pairing code on screen.")
            withContext(Dispatchers.Main) {
                onPairingRequired("Enter the pairing code shown on your ${device.name}")
            }

            Result.success(false)
        } catch (e: Exception) {
            Log.e(tag, "Pairing initiation failed to ${device.ipAddress} ($serviceName)", e)
            disconnect()
            Result.failure(e)
        }
    }

    override suspend fun submitPairingPin(pin: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val dev = targetDevice ?: return@withContext Result.failure(IllegalStateException("No target TV device"))
        val pOut = pairingOutput ?: return@withContext Result.failure(IllegalStateException("Pairing socket not connected"))
        val pIn = pairingInput ?: return@withContext Result.failure(IllegalStateException("Pairing socket not connected"))

        try {
            val clientCert = certManager.getClientCertificate()
                ?: return@withContext Result.failure(IllegalStateException("Client certificate not found"))
            val sCert = serverCertificate
                ?: return@withContext Result.failure(IllegalStateException("Server certificate not captured during TLS handshake"))

            val cleanPin = pin.trim().replace("-", "").replace(" ", "").uppercase()
            Log.i(tag, "Submitting pairing code '$cleanPin' to TV...")

            // Compute Polo pairing secret SHA-256 hash
            val secretHash = certManager.computePairingSecret(clientCert, sCert, cleanPin)
            val secretPacket = PoloProtocolHelper.buildSecretMessage(secretHash, protocolVersion = activeProtocolVersion) // type = 40
            PoloProtocolHelper.writeFramed(pOut, secretPacket)
            Log.d(tag, "Dispatched Polo Secret (type 40) to TV")

            // Read SecretAck (type 41)
            val secretAckBytes = PoloProtocolHelper.readFramed(pIn)
                ?: throw IllegalStateException("TV did not respond to pairing secret")

            val parsedSecretAck = PoloProtocolHelper.parseOuterMessage(secretAckBytes)
            Log.i(tag, "SecretAck response: type=${parsedSecretAck.type}, status=${parsedSecretAck.status}")

            if (parsedSecretAck.status != PoloProtocolHelper.STATUS_OK && parsedSecretAck.status != 0 && parsedSecretAck.status != 200) {
                throw IllegalArgumentException("Invalid PIN code entered (status ${parsedSecretAck.status}). Please check the code on your TV and retry.")
            }

            Log.i(tag, "Pairing approved by TV! Closing pairing port 6467...")
            try {
                pairingSocket?.close()
                rawPairingSocket?.close()
            } catch (_: Exception) {}
            pairingSocket = null
            rawPairingSocket = null
            pairingOutput = null
            pairingInput = null

            // Connect to live control session on Port 6466
            Log.d(tag, "Opening live control session on ${dev.ipAddress}:6466...")
            val sslFactory = certManager.getSslSocketFactory()
            val rawControl = Socket()
            rawControl.connect(InetSocketAddress(dev.ipAddress, 6466), 6000)
            rawControl.tcpNoDelay = true

            val ctrlSock = sslFactory.createSocket(rawControl, dev.ipAddress, 6466, true) as SSLSocket
            ctrlSock.startHandshake()

            rawControlSocket = rawControl
            controlSocket = ctrlSock
            controlOutput = ctrlSock.outputStream
            controlInput = ctrlSock.inputStream

            // Send configure message
            val configureBytes = PoloProtocolHelper.buildRemoteConfigure()
            PoloProtocolHelper.writeFramed(controlOutput!!, configureBytes)

            connected = true
            Log.i(tag, "Connected and ready to control ${dev.name}!")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(tag, "Error verifying PIN with TV", e)
            disconnect()
            Result.failure(e)
        }
    }

    override suspend fun sendCommand(command: RemoteCommand): Result<Unit> = withContext(Dispatchers.IO) {
        val out = controlOutput ?: return@withContext Result.failure(IllegalStateException("Not connected to TV"))
        val keyCode = getAndroidTvKeyCode(command)

        if (keyCode == -1) {
            Log.w(tag, "Unsupported command $command for Android TV v2")
            return@withContext Result.failure(UnsupportedOperationException("Unsupported command $command"))
        }

        try {
            val packet = PoloProtocolHelper.buildKeyInject(keyCode = keyCode, direction = 1)
            PoloProtocolHelper.writeFramed(out, packet)
            Log.d(tag, "Sent key inject keyCode=$keyCode for command=$command")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to send command $command", e)
            connected = false
            Result.failure(e)
        }
    }

    override suspend fun sendText(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        val out = controlOutput ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            for (ch in text) {
                val keyCode = charToAndroidKeyCode(ch)
                if (keyCode != -1) {
                    val packet = PoloProtocolHelper.buildKeyInject(keyCode = keyCode, direction = 1)
                    PoloProtocolHelper.writeFramed(out, packet)
                    Thread.sleep(25)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendTouchMove(dx: Float, dy: Float): Result<Unit> = withContext(Dispatchers.IO) {
        val threshold = 18f
        when {
            dx > threshold -> sendCommand(RemoteCommand.DpadRight)
            dx < -threshold -> sendCommand(RemoteCommand.DpadLeft)
            dy > threshold -> sendCommand(RemoteCommand.DpadDown)
            dy < -threshold -> sendCommand(RemoteCommand.DpadUp)
            else -> Result.success(Unit)
        }
    }

    override suspend fun launchApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val out = controlOutput ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val deepLink = "https://play.google.com/store/apps/details?id=$packageName"
            val appLinkPacket = PoloProtocolHelper.buildAppLink(deepLink)
            PoloProtocolHelper.writeFramed(out, appLinkPacket)
            Log.i(tag, "Launched app: $packageName via AppLink")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to launch app $packageName", e)
            Result.failure(e)
        }
    }

    override suspend fun openUrl(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        val out = controlOutput ?: return@withContext Result.failure(IllegalStateException("Not connected"))
        try {
            val appLinkPacket = PoloProtocolHelper.buildAppLink(url)
            PoloProtocolHelper.writeFramed(out, appLinkPacket)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ping(): Long = withContext(Dispatchers.IO) {
        val out = controlOutput ?: return@withContext -1L
        try {
            val start = System.currentTimeMillis()
            val pingBytes = PoloProtocolHelper.buildPing()
            PoloProtocolHelper.writeFramed(out, pingBytes)
            System.currentTimeMillis() - start
        } catch (e: Exception) {
            -1L
        }
    }

    override fun disconnect() {
        connected = false
        try { pairingSocket?.close() } catch (_: Exception) {}
        try { rawPairingSocket?.close() } catch (_: Exception) {}
        try { controlSocket?.close() } catch (_: Exception) {}
        try { rawControlSocket?.close() } catch (_: Exception) {}
        pairingSocket = null
        rawPairingSocket = null
        pairingOutput = null
        pairingInput = null
        controlSocket = null
        rawControlSocket = null
        controlOutput = null
        controlInput = null
        serverCertificate = null
    }

    private fun getAndroidTvKeyCode(cmd: RemoteCommand): Int = cmd.androidKeyCode ?: -1

    private fun charToAndroidKeyCode(c: Char): Int = when (c) {
        in '0'..'9' -> 7 + (c - '0')
        in 'a'..'z' -> 29 + (c - 'a')
        in 'A'..'Z' -> 29 + (c - 'A')
        ' ' -> 62 // KEYCODE_SPACE
        '\n' -> 66 // KEYCODE_ENTER
        else -> -1
    }
}
