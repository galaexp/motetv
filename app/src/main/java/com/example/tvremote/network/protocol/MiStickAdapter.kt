package com.example.tvremote.network.protocol

import android.util.Log
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Xiaomi Mi TV Stick / Mi Box Compatibility Adapter.
 * Bridges Android TV v2 protocol with Xiaomi-specific hardware sleep, WOL wake bursts,
 * and PatchWall launcher mappings.
 */
class MiStickAdapter(
    private val v2Protocol: AndroidTvV2Protocol,
    private val adbProtocol: AdbTcpProtocol
) : TvProtocolHandler {
    private val tag = "MiStickAdapter"
    private var activeProtocol: TvProtocolHandler = v2Protocol
    private var currentDevice: TvDevice? = null

    override val isConnected: Boolean
        get() = activeProtocol.isConnected

    override suspend fun connect(
        device: TvDevice,
        onPairingRequired: (prompt: String) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        currentDevice = device

        // Pre-connection: Send wake burst in case Wi-Fi was in low-power sleep
        if (device.isMiStick) {
            Log.d(tag, "Mi Stick detected. Sending pre-connect wake burst to ${device.ipAddress}")
            sendWakeOnLan(device.ipAddress, device.macAddress)
        }

        // Connect via standard Android TV v2 protocol (Google TV / UniMote protocol)
        activeProtocol = v2Protocol
        val v2Result = v2Protocol.connect(device, onPairingRequired)

        if (v2Result.isSuccess) {
            return@withContext v2Result
        }

        // Only try ADB fallback if explicitly set or if port 5555 was manually specified
        if (device.protocolType == ProtocolType.ADB_TCP || device.port == 5555) {
            Log.w(tag, "Android TV V2 failed on Mi Stick; attempting ADB TCP fallback (port 5555)")
            activeProtocol = adbProtocol
            val adbResult = adbProtocol.connect(device.copy(port = 5555), onPairingRequired)
            if (adbResult.isSuccess) {
                return@withContext adbResult
            }
        }

        activeProtocol = v2Protocol
        v2Result
    }

    override suspend fun submitPairingPin(pin: String): Result<Boolean> {
        return activeProtocol.submitPairingPin(pin)
    }

    override suspend fun sendCommand(command: RemoteCommand): Result<Unit> = withContext(Dispatchers.IO) {
        when (command) {
            is RemoteCommand.LaunchPatchWall -> {
                Log.d(tag, "Handling Mi Stick PatchWall launch")
                return@withContext activeProtocol.launchApp("com.xiaomi.mitv.tvhome")
            }

            is RemoteCommand.WakeUp, RemoteCommand.PowerToggle -> {
                currentDevice?.let { dev ->
                    sendWakeOnLan(dev.ipAddress, dev.macAddress)
                }
                val wakeResult = activeProtocol.sendCommand(RemoteCommand.WakeUp)
                try {
                    Thread.sleep(150)
                } catch (_: Exception) {}
                activeProtocol.sendCommand(RemoteCommand.Home)
                return@withContext wakeResult
            }

            else -> activeProtocol.sendCommand(command)
        }
    }

    override suspend fun sendText(text: String): Result<Unit> {
        return activeProtocol.sendText(text)
    }

    override suspend fun sendTouchMove(dx: Float, dy: Float): Result<Unit> {
        return activeProtocol.sendTouchMove(dx, dy)
    }

    override suspend fun launchApp(packageName: String): Result<Unit> {
        return activeProtocol.launchApp(packageName)
    }

    override suspend fun openUrl(url: String): Result<Unit> {
        return activeProtocol.openUrl(url)
    }

    override suspend fun ping(): Long {
        return activeProtocol.ping()
    }

    override fun disconnect() {
        v2Protocol.disconnect()
        adbProtocol.disconnect()
        currentDevice = null
    }

    /**
     * Sends a Wake-on-LAN magic packet or UDP broadcast pulse to wake Mi Stick Wi-Fi chipset.
     */
    private fun sendWakeOnLan(ipAddress: String, macAddress: String?) {
        try {
            val broadcastIp = InetAddress.getByName("255.255.255.255")
            val socket = DatagramSocket()
            socket.broadcast = true

            val packetData = if (!macAddress.isNullOrBlank()) {
                val macBytes = parseMac(macAddress)
                val bytes = ByteArray(6 + 16 * macBytes.size)
                for (i in 0 until 6) bytes[i] = 0xFF.toByte()
                for (i in 0 until 16) {
                    System.arraycopy(macBytes, 0, bytes, 6 + i * macBytes.size, macBytes.size)
                }
                bytes
            } else {
                ByteArray(64) { 0xFF.toByte() }
            }

            val packet = DatagramPacket(packetData, packetData.size, broadcastIp, 9)
            socket.send(packet)
            socket.close()
            Log.d(tag, "Wake-on-LAN packet dispatched to $ipAddress")
        } catch (e: Exception) {
            Log.w(tag, "Failed to send WOL packet: ${e.message}")
        }
    }

    private fun parseMac(mac: String): ByteArray {
        val clean = mac.replace(":", "").replace("-", "")
        val bytes = ByteArray(6)
        for (i in 0 until 6) {
            bytes[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return bytes
    }
}
