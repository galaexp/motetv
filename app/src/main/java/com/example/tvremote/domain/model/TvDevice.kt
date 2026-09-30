package com.example.tvremote.domain.model

enum class ProtocolType {
    ANDROID_TV_V2,    // Google TV / Android TV Remote Protocol v2 (ports 6466/6467)
    ADB_TCP,          // Direct TCP / ADB keyevent protocol (port 5555, common on Mi Stick dev mode)
    GENERIC_SOCKET    // Raw TCP command protocol
}

enum class ConnectionMedium {
    WIFI_MDNS,
    WIFI_MANUAL,
    BLUETOOTH
}

data class TvDevice(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int = 6467,
    val macAddress: String? = null,
    val protocolType: ProtocolType = ProtocolType.ANDROID_TV_V2,
    val isMiStick: Boolean = false,
    val isPaired: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val connectionMedium: ConnectionMedium = ConnectionMedium.WIFI_MDNS,
    val modelInfo: String? = null
) {
    val displaySubtitle: String
        get() = when {
            isMiStick -> "Xiaomi Mi TV Stick / Box • $ipAddress"
            connectionMedium == ConnectionMedium.BLUETOOTH -> "Bluetooth Device • ${macAddress ?: ipAddress}"
            else -> "Android TV OS • $ipAddress"
        }
}
