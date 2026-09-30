package com.example.tvremote.domain.model

/**
 * Standard Android TV Keycodes according to android.view.KeyEvent:
 * - KEYCODE_DPAD_UP = 19
 * - KEYCODE_DPAD_DOWN = 20
 * - KEYCODE_DPAD_LEFT = 21
 * - KEYCODE_DPAD_RIGHT = 22
 * - KEYCODE_DPAD_CENTER = 23 (OK / Select)
 * - KEYCODE_HOME = 3
 * - KEYCODE_BACK = 4
 * - KEYCODE_VOLUME_UP = 24
 * - KEYCODE_VOLUME_DOWN = 25
 * - KEYCODE_POWER = 26
 * - KEYCODE_WAKEUP = 224
 * - KEYCODE_SLEEP = 223
 * - KEYCODE_MENU = 82
 * - KEYCODE_MEDIA_PLAY_PAUSE = 85
 * - KEYCODE_MEDIA_STOP = 86
 * - KEYCODE_MEDIA_NEXT = 87
 * - KEYCODE_MEDIA_PREVIOUS = 88
 * - KEYCODE_MEDIA_REWIND = 89
 * - KEYCODE_MEDIA_FAST_FORWARD = 90
 * - KEYCODE_VOLUME_MUTE = 164
 * - KEYCODE_CHANNEL_UP = 166
 * - KEYCODE_CHANNEL_DOWN = 167
 * - KEYCODE_TV_INPUT = 178
 * - KEYCODE_APP_SWITCH = 187 (Recent Apps)
 * - KEYCODE_VOICE_ASSIST = 231
 */
sealed class RemoteCommand {
    // D-Pad Navigation
    object DpadUp : RemoteCommand()
    object DpadDown : RemoteCommand()
    object DpadLeft : RemoteCommand()
    object DpadRight : RemoteCommand()
    object DpadCenter : RemoteCommand() // OK / Select

    // System Navigation
    object Back : RemoteCommand()
    object Home : RemoteCommand()
    object Menu : RemoteCommand()
    object RecentApps : RemoteCommand()

    // Power & Audio
    object PowerToggle : RemoteCommand()
    object WakeUp : RemoteCommand()
    object Sleep : RemoteCommand()
    object VolumeUp : RemoteCommand()
    object VolumeDown : RemoteCommand()
    object VolumeMute : RemoteCommand()

    // Channels & Source
    object ChannelUp : RemoteCommand()
    object ChannelDown : RemoteCommand()
    object TvInput : RemoteCommand()

    // Playback controls
    object PlayPause : RemoteCommand()
    object Stop : RemoteCommand()
    object Rewind : RemoteCommand()
    object FastForward : RemoteCommand()
    object PreviousTrack : RemoteCommand()
    object NextTrack : RemoteCommand()

    // Smart Features
    object SkipYouTubeAd : RemoteCommand()

    // Numeric keypad
    data class Number(val digit: Int) : RemoteCommand()
    object KeyEnter : RemoteCommand()
    object KeyClear : RemoteCommand()

    // Xiaomi Mi Stick specific
    object LaunchPatchWall : RemoteCommand()

    // Advanced Input
    data class SendText(val text: String) : RemoteCommand()
    data class OpenUrl(val url: String) : RemoteCommand()
    data class LaunchApp(val packageName: String, val appName: String) : RemoteCommand()
    data class TouchpadMove(val deltaX: Float, val deltaY: Float) : RemoteCommand()
    object TouchpadClick : RemoteCommand()
    object VoiceSearch : RemoteCommand()

    val androidKeyCode: Int?
        get() = when (this) {
            DpadUp -> 19
            DpadDown -> 20
            DpadLeft -> 21
            DpadRight -> 22
            DpadCenter -> 23
            Home -> 3
            Back -> 4
            Menu -> 82
            RecentApps -> 187
            PowerToggle -> 26
            WakeUp -> 224
            Sleep -> 223
            VolumeUp -> 24
            VolumeDown -> 25
            VolumeMute -> 164
            ChannelUp -> 166
            ChannelDown -> 167
            TvInput -> 178
            PlayPause -> 85
            Stop -> 86
            Rewind -> 89
            FastForward -> 90
            PreviousTrack -> 88
            NextTrack -> 87
            is Number -> 7 + digit // KEYCODE_0 = 7, KEYCODE_9 = 16
            KeyEnter -> 66
            KeyClear -> 28
            VoiceSearch -> 231
            LaunchPatchWall -> 3 // Fallback if direct component intent not used
            else -> null
        }
}
