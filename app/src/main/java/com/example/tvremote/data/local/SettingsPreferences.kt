package com.example.tvremote.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.RemoteSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("tv_remote_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<RemoteSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): RemoteSettings {
        return RemoteSettings(
            hapticFeedback = prefs.getBoolean("haptic_feedback", true),
            touchpadSensitivity = prefs.getFloat("touchpad_sensitivity", 1.0f),
            miStickOptimization = prefs.getBoolean("mi_stick_optimization", true),
            keepAliveIntervalSec = prefs.getInt("keep_alive_interval", 15),
            preferredProtocol = try {
                ProtocolType.valueOf(prefs.getString("preferred_protocol", ProtocolType.ANDROID_TV_V2.name) ?: ProtocolType.ANDROID_TV_V2.name)
            } catch (e: Exception) {
                ProtocolType.ANDROID_TV_V2
            },
            autoReconnect = prefs.getBoolean("auto_reconnect", true),
            wakeOnLanEnabled = prefs.getBoolean("wake_on_lan", true),
            wifiDiscoveryEnabled = prefs.getBoolean("wifi_discovery", true),
            bluetoothDiscoveryEnabled = prefs.getBoolean("bt_discovery", true),
            youtubeAdSkipTurbo = prefs.getBoolean("yt_ad_skip_turbo", true),
            themePreset = prefs.getString("theme_preset", "OLED_BLACK") ?: "OLED_BLACK"
        )
    }

    fun updateSettings(settings: RemoteSettings) {
        prefs.edit()
            .putBoolean("haptic_feedback", settings.hapticFeedback)
            .putFloat("touchpad_sensitivity", settings.touchpadSensitivity)
            .putBoolean("mi_stick_optimization", settings.miStickOptimization)
            .putInt("keep_alive_interval", settings.keepAliveIntervalSec)
            .putString("preferred_protocol", settings.preferredProtocol.name)
            .putBoolean("auto_reconnect", settings.autoReconnect)
            .putBoolean("wake_on_lan", settings.wakeOnLanEnabled)
            .putBoolean("wifi_discovery", settings.wifiDiscoveryEnabled)
            .putBoolean("bt_discovery", settings.bluetoothDiscoveryEnabled)
            .putBoolean("yt_ad_skip_turbo", settings.youtubeAdSkipTurbo)
            .putString("theme_preset", settings.themePreset)
            .apply()

        _settingsFlow.value = settings
    }

    fun getRecentPushes(): List<String> {
        val raw = prefs.getString("recent_pushes_v1", null) ?: return emptyList()
        return raw.split("|||").filter { it.isNotBlank() }.take(10)
    }

    fun addRecentPush(content: String) {
        if (content.isBlank()) return
        val current = getRecentPushes().toMutableList()
        current.remove(content)
        current.add(0, content)
        val trimmed = current.take(10).joinToString("|||")
        prefs.edit().putString("recent_pushes_v1", trimmed).apply()
    }

    fun clearRecentPushes() {
        prefs.edit().remove("recent_pushes_v1").apply()
    }

    fun getCustomButtons(): List<com.example.tvremote.domain.model.CustomControlButton> {
        val raw = prefs.getString("custom_control_buttons_v1", null)
        return if (raw.isNullOrBlank()) {
            com.example.tvremote.domain.model.CustomControlButton.defaultButtons()
        } else {
            com.example.tvremote.domain.model.CustomControlButton.deserializeList(raw)
        }
    }

    fun saveCustomButtons(buttons: List<com.example.tvremote.domain.model.CustomControlButton>) {
        val serialized = com.example.tvremote.domain.model.CustomControlButton.serializeList(buttons)
        prefs.edit().putString("custom_control_buttons_v1", serialized).apply()
    }

    fun resetCustomButtons(): List<com.example.tvremote.domain.model.CustomControlButton> {
        val defaults = com.example.tvremote.domain.model.CustomControlButton.defaultButtons()
        saveCustomButtons(defaults)
        return defaults
    }
}
