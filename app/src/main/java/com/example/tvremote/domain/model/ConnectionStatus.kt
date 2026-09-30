package com.example.tvremote.domain.model

sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    data class Scanning(val devicesFound: Int = 0) : ConnectionStatus()
    data class Connecting(val device: TvDevice) : ConnectionStatus()
    data class PairingRequired(val device: TvDevice, val prompt: String) : ConnectionStatus()
    data class Connected(
        val device: TvDevice,
        val latencyMs: Long = 12L,
        val protocol: ProtocolType = ProtocolType.ANDROID_TV_V2
    ) : ConnectionStatus()
    data class Error(val message: String, val device: TvDevice? = null) : ConnectionStatus()

    val isConnected: Boolean
        get() = this is Connected

    val currentDevice: TvDevice?
        get() = when (this) {
            is Connecting -> device
            is PairingRequired -> device
            is Connected -> device
            is Error -> device
            else -> null
        }
}
