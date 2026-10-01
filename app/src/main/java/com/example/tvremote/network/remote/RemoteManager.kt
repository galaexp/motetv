package com.example.tvremote.network.remote

import android.util.Log
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.network.protocol.PoloProtocolHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Manages port 6466 Control session and persistent receive loop for Android TV Remote v2.
 */
class RemoteManager {
    private val tag = "RemoteManager"

    private var rawSocket: Socket? = null
    private var sslSocket: SSLSocket? = null
    private var output: OutputStream? = null
    private var input: InputStream? = null
    private var receiveJob: Job? = null

    @Volatile
    var isConnected: Boolean = false
        private set

    suspend fun connect(
        ipAddress: String,
        sslFactory: SSLSocketFactory
    ): Result<Boolean> {
        disconnect()
        try {
            Log.i(tag, "[ATV2] Connecting TCP ${ipAddress}:6466...")
            val raw = Socket()
            raw.connect(InetSocketAddress(ipAddress, 6466), 5000)
            raw.tcpNoDelay = true
            raw.soTimeout = 10000

            Log.i(tag, "[ATV2] Starting TLS handshake on 6466...")
            val ssl = sslFactory.createSocket(raw, ipAddress, 6466, true) as SSLSocket
            ssl.startHandshake()
            Log.i(tag, "[ATV2] TLS handshake successful on 6466")

            rawSocket = raw
            sslSocket = ssl
            output = ssl.outputStream
            input = ssl.inputStream

            // 1. TX RemoteConfigure
            val configBytes = RemoteMessageManager.buildRemoteConfigure()
            PoloProtocolHelper.writeVarintFramed(output!!, configBytes)
            Log.i(tag, "[ATV2] TX RemoteConfigure (len=${configBytes.size})")

            // 2. RX RemoteConfigure response
            val rxConfig = PoloProtocolHelper.readVarintFramed(input!!)
                ?: throw IllegalStateException("No response to RemoteConfigure from TV")
            val parsedConfig = RemoteMessageManager.parseRemoteMessage(rxConfig)
            Log.i(tag, "[ATV2] RX RemoteConfigure ack (len=${rxConfig.size}, hasConfig=${parsedConfig.hasRemoteConfigure})")

            // 3. TX RemoteSetActive
            val setActiveBytes = RemoteMessageManager.buildRemoteSetActive(1)
            PoloProtocolHelper.writeVarintFramed(output!!, setActiveBytes)
            Log.i(tag, "[ATV2] TX RemoteSetActive (len=${setActiveBytes.size})")

            isConnected = true
            startReceiveLoop()
            Log.i(tag, "[ATV2] Remote control session ready on 6466!")
            return Result.success(true)
        } catch (e: Exception) {
            Log.e(tag, "[ATV2] Failed to establish remote session on $ipAddress: ${e.message}")
            disconnect()
            return Result.failure(e)
        }
    }

    private fun startReceiveLoop() {
        receiveJob?.cancel()
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            Log.i(tag, "[ATV2] Starting background receive loop on port 6466...")
            try {
                val inp = input ?: return@launch
                while (isActive && isConnected) {
                    val frame = PoloProtocolHelper.readVarintFramed(inp) ?: break
                    val parsed = RemoteMessageManager.parseRemoteMessage(frame)
                    if (parsed.hasRemotePingRequest) {
                        Log.d(tag, "[ATV2] RX PingRequest -> TX PingResponse")
                        val pingResp = RemoteMessageManager.buildRemotePingResponse(parsed.pingVal)
                        output?.let { out ->
                            synchronized(out) {
                                PoloProtocolHelper.writeVarintFramed(out, pingResp)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "[ATV2] Receive loop terminated: ${e.message}")
            } finally {
                isConnected = false
            }
        }
    }

    suspend fun sendCommand(cmd: RemoteCommand): Result<Unit> {
        val out = output ?: return Result.failure(IllegalStateException("Not connected on 6466"))
        val keyCode = cmd.androidKeyCode ?: return Result.failure(UnsupportedOperationException("Unsupported command $cmd"))
        try {
            val keyBytes = RemoteMessageManager.buildRemoteKeyInject(keyCode, RemoteMessageManager.KEY_DIRECTION_SHORT)
            synchronized(out) {
                PoloProtocolHelper.writeVarintFramed(out, keyBytes)
            }
            Log.d(tag, "[ATV2] TX RemoteKeyInject keyCode=$keyCode")
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "[ATV2] Error sending command $cmd: ${e.message}")
            isConnected = false
            return Result.failure(e)
        }
    }

    suspend fun sendText(text: String): Result<Unit> {
        val out = output ?: return Result.failure(IllegalStateException("Not connected"))
        try {
            for (ch in text) {
                val keyCode = charToKeyCode(ch)
                if (keyCode != -1) {
                    val keyBytes = RemoteMessageManager.buildRemoteKeyInject(keyCode, RemoteMessageManager.KEY_DIRECTION_SHORT)
                    synchronized(out) {
                        PoloProtocolHelper.writeVarintFramed(out, keyBytes)
                    }
                    Thread.sleep(25)
                }
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun launchApp(packageName: String): Result<Unit> {
        val out = output ?: return Result.failure(IllegalStateException("Not connected"))
        try {
            val deepLink = "https://play.google.com/store/apps/details?id=$packageName"
            val appLinkBytes = RemoteMessageManager.buildRemoteAppLink(deepLink)
            synchronized(out) {
                PoloProtocolHelper.writeVarintFramed(out, appLinkBytes)
            }
            Log.i(tag, "[ATV2] TX AppLink deepLink=$deepLink")
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun openUrl(url: String): Result<Unit> {
        val out = output ?: return Result.failure(IllegalStateException("Not connected"))
        try {
            val appLinkBytes = RemoteMessageManager.buildRemoteAppLink(url)
            synchronized(out) {
                PoloProtocolHelper.writeVarintFramed(out, appLinkBytes)
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun disconnect() {
        isConnected = false
        receiveJob?.cancel()
        receiveJob = null
        try { sslSocket?.close() } catch (_: Exception) {}
        try { rawSocket?.close() } catch (_: Exception) {}
        sslSocket = null
        rawSocket = null
        output = null
        input = null
    }

    private fun charToKeyCode(c: Char): Int = when (c) {
        in '0'..'9' -> 7 + (c - '0')
        in 'a'..'z' -> 29 + (c - 'a')
        in 'A'..'Z' -> 29 + (c - 'A')
        ' ' -> 62
        '\n' -> 66
        else -> -1
    }
}
