package com.example.tvremote.domain.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents a customizable, movable, editable, and deletable button on the Nova Remote.
 */
data class CustomControlButton(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val iconKey: String,
    val commandType: String,
    val commandArg: String? = null,
    val colorHex: Long = 0xFF007AFF, // iOS System Blue default
    val isDeletable: Boolean = true
) {
    fun toRemoteCommand(): RemoteCommand? {
        return when (commandType) {
            "TV_INPUT" -> RemoteCommand.TvInput
            "MUTE" -> RemoteCommand.VolumeMute
            "MENU" -> RemoteCommand.Menu
            "SKIP_AD" -> RemoteCommand.SkipYouTubeAd
            "HOME" -> RemoteCommand.Home
            "BACK" -> RemoteCommand.Back
            "RECENT_APPS" -> RemoteCommand.RecentApps
            "POWER" -> RemoteCommand.PowerToggle
            "SLEEP" -> RemoteCommand.Sleep
            "WAKE_UP" -> RemoteCommand.WakeUp
            "VOL_UP" -> RemoteCommand.VolumeUp
            "VOL_DOWN" -> RemoteCommand.VolumeDown
            "CH_UP" -> RemoteCommand.ChannelUp
            "CH_DOWN" -> RemoteCommand.ChannelDown
            "PLAY_PAUSE" -> RemoteCommand.PlayPause
            "REWIND" -> RemoteCommand.Rewind
            "FAST_FORWARD" -> RemoteCommand.FastForward
            "STOP" -> RemoteCommand.Stop
            "PATCHWALL" -> RemoteCommand.LaunchPatchWall
            "SETTINGS" -> RemoteCommand.Menu
            "INFO" -> RemoteCommand.Menu
            "GUIDE" -> RemoteCommand.Menu
            "SUBTITLES" -> RemoteCommand.Menu
            "APP_YOUTUBE" -> RemoteCommand.LaunchApp("com.google.android.youtube.tv", "YouTube")
            "APP_NETFLIX" -> RemoteCommand.LaunchApp("com.netflix.ninja", "Netflix")
            "APP_PRIME" -> RemoteCommand.LaunchApp("com.amazon.amazonvideo.livingroom", "Prime Video")
            "APP_DISNEY" -> RemoteCommand.LaunchApp("com.disney.disneyplus", "Disney+")
            "APP_SPOTIFY" -> RemoteCommand.LaunchApp("com.spotify.tv.android", "Spotify")
            "LAUNCH_APP" -> if (commandArg != null) RemoteCommand.LaunchApp(commandArg, label) else null
            "OPEN_URL" -> if (commandArg != null) RemoteCommand.OpenUrl(commandArg) else null
            "SEND_TEXT" -> if (commandArg != null) RemoteCommand.SendText(commandArg) else null
            else -> null
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("label", label)
            put("iconKey", iconKey)
            put("commandType", commandType)
            putOpt("commandArg", commandArg)
            put("colorHex", colorHex)
            put("isDeletable", isDeletable)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CustomControlButton {
            return CustomControlButton(
                id = json.optString("id", UUID.randomUUID().toString()),
                label = json.optString("label", "Button"),
                iconKey = json.optString("iconKey", "default"),
                commandType = json.optString("commandType", "MENU"),
                commandArg = if (json.has("commandArg")) json.optString("commandArg") else null,
                colorHex = json.optLong("colorHex", 0xFF007AFF),
                isDeletable = json.optBoolean("isDeletable", true)
            )
        }

        fun defaultButtons(): List<CustomControlButton> {
            return listOf(
                CustomControlButton(
                    id = "btn_input",
                    label = "Input",
                    iconKey = "input",
                    commandType = "TV_INPUT",
                    colorHex = 0xFF5856D6, // iOS Indigo
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_mute",
                    label = "Mute",
                    iconKey = "mute",
                    commandType = "MUTE",
                    colorHex = 0xFFFF9500, // iOS Orange
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_menu",
                    label = "Menu",
                    iconKey = "menu",
                    commandType = "MENU",
                    colorHex = 0xFF007AFF, // iOS Blue
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_skip_ad",
                    label = "Skip Ad",
                    iconKey = "skip_ad",
                    commandType = "SKIP_AD",
                    colorHex = 0xFFFF3B30, // iOS Red
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_push",
                    label = "Push to TV",
                    iconKey = "push",
                    commandType = "PUSH_TO_TV",
                    colorHex = 0xFF30B0C7, // iOS Teal
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_yt",
                    label = "YouTube",
                    iconKey = "youtube",
                    commandType = "APP_YOUTUBE",
                    colorHex = 0xFFFF3B30, // Red
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_netflix",
                    label = "Netflix",
                    iconKey = "netflix",
                    commandType = "APP_NETFLIX",
                    colorHex = 0xFFE50914, // Netflix Red
                    isDeletable = true
                ),
                CustomControlButton(
                    id = "btn_subtitles",
                    label = "Subtitles",
                    iconKey = "subtitles",
                    commandType = "SUBTITLES",
                    colorHex = 0xFF34C759, // iOS Green
                    isDeletable = true
                )
            )
        }

        fun availablePresets(): List<CustomControlButton> {
            return listOf(
                CustomControlButton(label = "TV Input", iconKey = "input", commandType = "TV_INPUT", colorHex = 0xFF5856D6),
                CustomControlButton(label = "Volume Mute", iconKey = "mute", commandType = "MUTE", colorHex = 0xFFFF9500),
                CustomControlButton(label = "TV Menu", iconKey = "menu", commandType = "MENU", colorHex = 0xFF007AFF),
                CustomControlButton(label = "Skip YouTube Ad", iconKey = "skip_ad", commandType = "SKIP_AD", colorHex = 0xFFFF3B30),
                CustomControlButton(label = "Push to TV", iconKey = "push", commandType = "PUSH_TO_TV", colorHex = 0xFF30B0C7),
                CustomControlButton(label = "Home", iconKey = "home", commandType = "HOME", colorHex = 0xFF007AFF),
                CustomControlButton(label = "Back", iconKey = "back", commandType = "BACK", colorHex = 0xFF8E8E93),
                CustomControlButton(label = "Recent Apps", iconKey = "recent_apps", commandType = "RECENT_APPS", colorHex = 0xFFAF52DE),
                CustomControlButton(label = "Sleep Timer", iconKey = "sleep", commandType = "SLEEP", colorHex = 0xFF5856D6),
                CustomControlButton(label = "Power Off", iconKey = "power", commandType = "POWER", colorHex = 0xFFFF3B30),
                CustomControlButton(label = "Settings", iconKey = "settings", commandType = "SETTINGS", colorHex = 0xFF8E8E93),
                CustomControlButton(label = "TV Guide", iconKey = "guide", commandType = "GUIDE", colorHex = 0xFF007AFF),
                CustomControlButton(label = "Info", iconKey = "info", commandType = "INFO", colorHex = 0xFF30B0C7),
                CustomControlButton(label = "Subtitles", iconKey = "subtitles", commandType = "SUBTITLES", colorHex = 0xFF34C759),
                CustomControlButton(label = "YouTube", iconKey = "youtube", commandType = "APP_YOUTUBE", colorHex = 0xFFFF3B30),
                CustomControlButton(label = "Netflix", iconKey = "netflix", commandType = "APP_NETFLIX", colorHex = 0xFFE50914),
                CustomControlButton(label = "Prime Video", iconKey = "prime", commandType = "APP_PRIME", colorHex = 0xFF00A8E1),
                CustomControlButton(label = "Disney+", iconKey = "disney", commandType = "APP_DISNEY", colorHex = 0xFF113CCF),
                CustomControlButton(label = "Spotify", iconKey = "spotify", commandType = "APP_SPOTIFY", colorHex = 0xFF1DB954),
                CustomControlButton(label = "PatchWall", iconKey = "patchwall", commandType = "PATCHWALL", colorHex = 0xFFFF6900),
                CustomControlButton(label = "Play / Pause", iconKey = "play_pause", commandType = "PLAY_PAUSE", colorHex = 0xFF007AFF),
                CustomControlButton(label = "Rewind", iconKey = "rewind", commandType = "REWIND", colorHex = 0xFF8E8E93),
                CustomControlButton(label = "Fast Forward", iconKey = "fast_forward", commandType = "FAST_FORWARD", colorHex = 0xFF8E8E93),
                CustomControlButton(label = "Stop", iconKey = "stop", commandType = "STOP", colorHex = 0xFFFF3B30),
                CustomControlButton(label = "Volume Up", iconKey = "vol_up", commandType = "VOL_UP", colorHex = 0xFF007AFF),
                CustomControlButton(label = "Volume Down", iconKey = "vol_down", commandType = "VOL_DOWN", colorHex = 0xFF007AFF),
                CustomControlButton(label = "Channel Up", iconKey = "ch_up", commandType = "CH_UP", colorHex = 0xFF34C759),
                CustomControlButton(label = "Channel Down", iconKey = "ch_down", commandType = "CH_DOWN", colorHex = 0xFF34C759)
            )
        }

        fun serializeList(list: List<CustomControlButton>): String {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun deserializeList(jsonString: String): List<CustomControlButton> {
            return try {
                val array = JSONArray(jsonString)
                val result = mutableListOf<CustomControlButton>()
                for (i in 0 until array.length()) {
                    result.add(fromJson(array.getJSONObject(i)))
                }
                result
            } catch (e: Exception) {
                defaultButtons()
            }
        }
    }
}
