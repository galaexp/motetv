package com.example.tvremote.network.pairing

import android.util.Log
import com.example.tvremote.domain.model.TvDevice
import com.example.tvremote.network.protocol.PoloProtocolHelper
import com.example.tvremote.network.protocol.TvCertificateManager
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Manages port 6467 Pairing session for Android TV Remote v2.
 */
class PairingManager(private val certManager: TvCertificateManager) {
    private val tag = "PairingManager"

    private var rawSocket: Socket? = null
    private var sslSocket: SSLSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    var serverCertificate: X509Certificate? = null
        private set

    suspend fun initiatePairing(
        device: TvDevice,
        sslFactory: SSLSocketFactory,
        serviceName: String = "androidtv-remote"
    ): Result<Boolean> {
        disconnect()
        try {
            Log.i(tag, "[ATV2] TCP connect ${device.ipAddress}:6467")
            val raw = Socket()
            raw.connect(InetSocketAddress(device.ipAddress, 6467), 7000)
            raw.tcpNoDelay = true
            raw.soTimeout = 15000

            Log.i(tag, "[ATV2] TLS handshake started on 6467...")
            val ssl = sslFactory.createSocket(raw, device.ipAddress, 6467, true) as SSLSocket
            ssl.startHandshake()
            Log.i(tag, "[ATV2] TLS handshake successful")

            rawSocket = raw
            sslSocket = ssl
            output = ssl.outputStream
            input = ssl.inputStream

            val session = ssl.session
            serverCertificate = session.peerCertificates.firstOrNull() as? X509Certificate

            // 1. TX PairingRequest
            val reqBytes = PairingMessageManager.buildPairingRequest(serviceName = serviceName, clientName = "Nova Remote", protocolVersion = 2)
            PoloProtocolHelper.writeVarintFramed(output!!, reqBytes)
            Log.i(tag, "[ATV2] TX PairingRequest (len=${reqBytes.size})")

            // 2. TX PairingOption
            val optBytes = PairingMessageManager.buildPairingOption(protocolVersion = 2)
            PoloProtocolHelper.writeVarintFramed(output!!, optBytes)
            Log.i(tag, "[ATV2] TX PairingOption (len=${optBytes.size})")

            // 3. TX PairingConfiguration
            val configBytes = PairingMessageManager.buildPairingConfiguration(symbolLength = 6, encodingType = PairingMessageManager.ENCODING_HEXADECIMAL, protocolVersion = 2)
            PoloProtocolHelper.writeVarintFramed(output!!, configBytes)
            Log.i(tag, "[ATV2] TX PairingConfiguration (len=${configBytes.size})")

            // 4. RX PairingMessage response from TV
            val rxBytes = PoloProtocolHelper.readVarintFramed(input!!)
                ?: throw IllegalStateException("No response received from TV after Configuration")
            val parsed = PairingMessageManager.parsePairingMessage(rxBytes)
            Log.i(tag, "[ATV2] RX PairingMessage (len=${rxBytes.size}, status=${parsed.status}, version=${parsed.protocolVersion})")

            if (parsed.status != PairingMessageManager.STATUS_OK && parsed.status != 0 && parsed.status != 200) {
                throw IllegalStateException("TV returned non-OK status ${parsed.status} for PairingConfiguration")
            }

            Log.i(tag, "[ATV2] ConfigurationAck confirmed! TV IS NOW DISPLAYING PIN OVERLAY.")
            return Result.success(true)
        } catch (e: Exception) {
            Log.e(tag, "[ATV2] Pairing initiation failed on ${device.ipAddress}: ${e.message}")
            disconnect()
            return Result.failure(e)
        }
    }

    suspend fun submitPin(pin: String): Result<Boolean> {
        val out = output ?: return Result.failure(IllegalStateException("Pairing stream output closed"))
        val inp = input ?: return Result.failure(IllegalStateException("Pairing stream input closed"))
        val cCert = certManager.getClientCertificate() ?: return Result.failure(IllegalStateException("No client cert"))
        val sCert = serverCertificate ?: return Result.failure(IllegalStateException("No server cert"))

        try {
            val secretHash = certManager.computePairingSecret(cCert, sCert, pin)
            val secretBytes = PairingMessageManager.buildPairingSecret(secretHash, protocolVersion = 2)
            PoloProtocolHelper.writeVarintFramed(out, secretBytes)
            Log.i(tag, "[ATV2] TX Secret (len=${secretBytes.size})")

            val secretAckBytes = PoloProtocolHelper.readVarintFramed(inp)
                ?: throw IllegalStateException("No response from TV for PairingSecret")
            val parsedAck = PairingMessageManager.parsePairingMessage(secretAckBytes)
            Log.i(tag, "[ATV2] RX SecretAck (len=${secretAckBytes.size}, status=${parsedAck.status})")

            if (parsedAck.status != PairingMessageManager.STATUS_OK && parsedAck.status != 0 && parsedAck.status != 200) {
                throw IllegalArgumentException("Invalid PIN code entered (status ${parsedAck.status}).")
            }

            Log.i(tag, "[ATV2] Pairing successful! Closing port 6467 socket.")
            disconnect()
            return Result.success(true)
        } catch (e: Exception) {
            Log.e(tag, "[ATV2] Failed to verify PIN with TV: ${e.message}")
            disconnect()
            return Result.failure(e)
        }
    }

    fun disconnect() {
        try { sslSocket?.close() } catch (_: Exception) {}
        try { rawSocket?.close() } catch (_: Exception) {}
        sslSocket = null
        rawSocket = null
        input = null
        output = null
    }
}
