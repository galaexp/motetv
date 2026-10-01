package com.example.tvremote.network.protocol

import android.content.Context
import android.util.Log
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.TvDevice
import com.example.tvremote.network.pairing.PairingManager
import com.example.tvremote.network.remote.RemoteManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android TV Remote Protocol v2 implementation (Google TV, Android TV 9-14, Xiaomi Mi TV Stick, Shield TV).
 * Uses separate PairingManager (port 6467) and RemoteManager (port 6466) with varint length-delimited protobuf framing.
 */
class AndroidTvV2Protocol(private val context: Context) : TvProtocolHandler {
    private val tag = "AndroidTvV2Protocol"
    private val certManager = TvCertificateManager(context)
    private val pairingManager = PairingManager(certManager)
    private val remoteManager = RemoteManager()

    private var targetDevice: TvDevice? = null

    override val isConnected: Boolean
        get() = remoteManager.isConnected

    override suspend fun connect(
        device: TvDevice,
        onPairingRequired: (prompt: String) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        targetDevice = device
        disconnect()

        val sslFactory = certManager.getSslSocketFactory()

        // 1. Direct control check if device is marked as paired
        if (device.isPaired) {
            Log.i(tag, "[ATV2] Device marked as paired. Trying direct control on ${device.ipAddress}:6466...")
            val directResult = remoteManager.connect(device.ipAddress, sslFactory)
            if (directResult.isSuccess) {
                return@withContext Result.success(true)
            }
            Log.w(tag, "[ATV2] Direct control session failed, proceeding to pairing on 6467...")
            disconnect()
        }

        // 2. Initiate pairing handshake on port 6467 with "androidtv-remote"
        val pairResult = pairingManager.initiatePairing(
            device = device,
            sslFactory = sslFactory,
            serviceName = "androidtv-remote"
        )

        if (pairResult.isSuccess) {
            Log.i(tag, "[ATV2] Pairing handshake completed! TV IS NOW DISPLAYING PIN CODE.")
            withContext(Dispatchers.Main) {
                onPairingRequired("Enter the code shown on your ${device.name}")
            }
            return@withContext Result.success(false)
        }

        // 3. Fallback: retry with serviceName "atvremote"
        Log.w(tag, "[ATV2] First pairing attempt with androidtv-remote failed. Retrying with atvremote...")
        disconnect()
        try { Thread.sleep(300) } catch (_: Exception) {}

        val fallbackResult = pairingManager.initiatePairing(
            device = device,
            sslFactory = sslFactory,
            serviceName = "atvremote"
        )

        if (fallbackResult.isSuccess) {
            Log.i(tag, "[ATV2] Fallback pairing handshake completed! TV IS NOW DISPLAYING PIN CODE.")
            withContext(Dispatchers.Main) {
                onPairingRequired("Enter the code shown on your ${device.name}")
            }
            return@withContext Result.success(false)
        }

        Result.failure(fallbackResult.exceptionOrNull() ?: IllegalStateException("Pairing initiation failed"))
    }

    override suspend fun submitPairingPin(pin: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val dev = targetDevice ?: return@withContext Result.failure(IllegalStateException("No target TV device"))
        Log.i(tag, "[ATV2] Submitting pairing code '$pin' to TV...")

        val pinResult = pairingManager.submitPin(pin)
        if (pinResult.isFailure) {
            return@withContext Result.failure(pinResult.exceptionOrNull() ?: IllegalStateException("Invalid PIN"))
        }

        Log.i(tag, "[ATV2] Pairing approved! Opening live remote session on ${dev.ipAddress}:6466...")
        val sslFactory = certManager.getSslSocketFactory()
        val remoteResult = remoteManager.connect(dev.ipAddress, sslFactory)

        if (remoteResult.isSuccess) {
            Log.i(tag, "[ATV2] Connected and ready to control ${dev.name}!")
            Result.success(true)
        } else {
            Result.failure(remoteResult.exceptionOrNull() ?: IllegalStateException("Failed to establish remote control session after pairing"))
        }
    }

    override suspend fun sendCommand(command: RemoteCommand): Result<Unit> = withContext(Dispatchers.IO) {
        remoteManager.sendCommand(command)
    }

    override suspend fun sendText(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        remoteManager.sendText(text)
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
        remoteManager.launchApp(packageName)
    }

    override suspend fun openUrl(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        remoteManager.openUrl(url)
    }

    override suspend fun ping(): Long = withContext(Dispatchers.IO) {
        if (remoteManager.isConnected) 5L else -1L
    }

    override fun disconnect() {
        pairingManager.disconnect()
        remoteManager.disconnect()
    }

    fun resetPairing() {
        disconnect()
        certManager.resetCredentials()
        Log.i(tag, "[ATV2] Reset pairing identity and credentials completed.")
    }
}
