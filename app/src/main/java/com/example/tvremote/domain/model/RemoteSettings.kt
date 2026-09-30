package com.example.tvremote.domain.model

data class RemoteSettings(
    val hapticFeedback: Boolean = true,
    val touchpadSensitivity: Float = 1.0f,
    val miStickOptimization: Boolean = true,
    val keepAliveIntervalSec: Int = 15,
    val preferredProtocol: ProtocolType = ProtocolType.ANDROID_TV_V2,
    val autoReconnect: Boolean = true,
    val wakeOnLanEnabled: Boolean = true,
    val wifiDiscoveryEnabled: Boolean = true,
    val bluetoothDiscoveryEnabled: Boolean = true,
    val youtubeAdSkipTurbo: Boolean = true,
    val themePreset: String = "OLED_BLACK" // "OLED_BLACK", "TITANIUM_DARK"
)
