package com.example.tvremote.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

object ButtonIconHelper {
    fun getIcon(key: String): ImageVector {
        return when (key.lowercase()) {
            "input" -> Icons.Default.Input
            "mute" -> Icons.Default.VolumeMute
            "menu" -> Icons.Default.Menu
            "skip_ad" -> Icons.Default.ElectricBolt
            "push" -> Icons.Default.RocketLaunch
            "home" -> Icons.Default.Home
            "back" -> Icons.AutoMirrored.Filled.ArrowBack
            "recent_apps" -> Icons.Default.GridView
            "power" -> Icons.Default.PowerSettingsNew
            "sleep" -> Icons.Default.Bedtime
            "settings" -> Icons.Default.Settings
            "guide" -> Icons.Default.List
            "info" -> Icons.Default.Info
            "subtitles" -> Icons.Default.Subtitles
            "youtube" -> Icons.Default.SmartDisplay
            "netflix" -> Icons.Default.Movie
            "prime" -> Icons.Default.LiveTv
            "disney" -> Icons.Default.AutoAwesome
            "spotify" -> Icons.Default.MusicNote
            "patchwall" -> Icons.Default.Tv
            "play_pause" -> Icons.Default.PlayArrow
            "rewind" -> Icons.Default.FastRewind
            "fast_forward" -> Icons.Default.FastForward
            "stop" -> Icons.Default.Stop
            "vol_up" -> Icons.Default.VolumeUp
            "vol_down" -> Icons.Default.VolumeDown
            "ch_up" -> Icons.Default.KeyboardArrowUp
            "ch_down" -> Icons.Default.KeyboardArrowDown
            else -> Icons.Default.Widgets
        }
    }

    val availableIconKeys = listOf(
        "input", "mute", "menu", "skip_ad", "push",
        "home", "back", "recent_apps", "power", "sleep",
        "settings", "guide", "info", "subtitles",
        "youtube", "netflix", "prime", "disney", "spotify", "patchwall",
        "play_pause", "rewind", "fast_forward", "stop",
        "vol_up", "vol_down", "ch_up", "ch_down"
    )
}
