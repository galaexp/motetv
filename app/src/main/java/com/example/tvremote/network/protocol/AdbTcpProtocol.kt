package com.example.tvremote.network.protocol

import android.util.Log
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Protocol handler using ADB (Android Debug Bridge) shell over TCP (default port 5555).
 * Extremely useful for Xiaomi Mi TV Stick / Mi Box where developer options / network debugging
 * are enabled, bypassing SSL pairing issues.
 */
class AdbTcpProtocol : TvProtocolHandler {
    private val tag = "AdbTcpProtocol"
    private var socket: Socket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var targetDevice: TvDevice? = null
    private var connected = false

    override val isConnected: Boolean
        get() = connected && socket?.isConnected == true && socket?.isClosed == false

    override suspend fun connect(
        device: TvDevice,
        onPairingRequired: (prompt: String) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            disconnect()
            targetDevice = device
            val port = if (device.port > 0 && device.port != 6467) device.port else 5555
            Log.d(tag, "Connecting to ADB port on ${device.ipAddress}:$port")

            val rawSocket = Socket()
            rawSocket.connect(InetSocketAddress(device.ipAddress, port), 3500)
            rawSocket.tcpNoDelay = true

            socket = rawSocket
            outputStream = rawSocket.getOutputStream()
            inputStream = rawSocket.getInputStream()
            connected = true

            Log.i(tag, "Connected via ADB TCP to ${device.name}")
            Result.success(true)
        } catch (e: Exception) {
            Log.w(tag, "ADB TCP connection failed on ${device.ipAddress}: ${e.message}")
            disconnect()
            Result.failure(e)
        }
    }

    override suspend fun submitPairingPin(pin: String): Result<Boolean> {
        // ADB uses RSA key authorization rather than PIN
        connected = true
        return Result.success(true)
    }

    override suspend fun sendCommand(command: RemoteCommand): Result<Unit> = withContext(Dispatchers.IO) {
        val keyCode = command.androidKeyCode
        if (keyCode != null) {
            return@withContext executeShellCommand("input keyevent $keyCode")
        }

        when (command) {
            is RemoteCommand.SendText -> sendText(command.text)
            is RemoteCommand.OpenUrl -> openUrl(command.url)
            is RemoteCommand.LaunchApp -> launchApp(command.packageName)
            is RemoteCommand.TouchpadMove -> sendTouchMove(command.deltaX, command.deltaY)
            is RemoteCommand.TouchpadClick -> sendCommand(RemoteCommand.DpadCenter)
            else -> Result.failure(UnsupportedOperationException("Unsupported ADB command"))
        }
    }

    override suspend fun sendText(text: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Escape spaces for shell input text
        val escaped = text.replace(" ", "%s").replace("'", "\\'")
        executeShellCommand("input text '$escaped'")
    }

    override suspend fun openUrl(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(tag, "Pushing URL via ADB: $url")
        val escapedUrl = url.replace("'", "\\'")
        val cmd = "am start -a android.intent.action.VIEW -d '$escapedUrl'"
        executeShellCommand(cmd)
    }

    override suspend fun sendTouchMove(dx: Float, dy: Float): Result<Unit> = withContext(Dispatchers.IO) {
        // On TV without pointer device, convert touch swipes to directional DPAD taps
        val threshold = 15f
        when {
            dx > threshold -> sendCommand(RemoteCommand.DpadRight)
            dx < -threshold -> sendCommand(RemoteCommand.DpadLeft)
            dy > threshold -> sendCommand(RemoteCommand.DpadDown)
            dy < -threshold -> sendCommand(RemoteCommand.DpadUp)
            else -> Result.success(Unit)
        }
    }

    override suspend fun launchApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cmd = "monkey -p $packageName -c android.intent.category.LAUNCHER 1"
        executeShellCommand(cmd)
    }

    override suspend fun ping(): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            if (isConnected) {
                outputStream?.write("\n".toByteArray())
                outputStream?.flush()
            }
            (System.currentTimeMillis() - start).coerceAtLeast(6L)
        } catch (_: Exception) {
            -1L
        }
    }

    override fun disconnect() {
        try {
            outputStream?.close()
            inputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Log.w(tag, "Error closing ADB socket", e)
        } finally {
            socket = null
            outputStream = null
            inputStream = null
            connected = false
        }
    }

    private fun executeShellCommand(cmd: String): Result<Unit> {
        try {
            if (!isConnected) return Result.failure(IllegalStateException("ADB not connected"))
            val formatted = "$cmd\n".toByteArray(Charsets.UTF_8)
            outputStream?.write(formatted)
            outputStream?.flush()
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Failed to execute shell command: $cmd", e)
            disconnect()
            return Result.failure(e)
        }
    }
}
