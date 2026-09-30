package com.example.tvremote.domain.model

data class MacroStep(
    val commandKey: String, // String identifier matching RemoteCommand
    val delayMs: Long = 400L,
    val extraArg: String? = null
)

data class RemoteMacro(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String = "play",
    val steps: List<MacroStep>,
    val isPreset: Boolean = false
) {
    companion object {
        fun defaultPresets(): List<RemoteMacro> = listOf(
            RemoteMacro(
                id = "preset_skip_youtube_ad",
                name = "Skip YouTube Ads",
                description = "Focuses and clicks the Skip Ad button on Android TV YouTube",
                iconName = "ad_skip",
                steps = listOf(
                    MacroStep("DPAD_UP", 100L),
                    MacroStep("DPAD_RIGHT", 80L),
                    MacroStep("DPAD_CENTER", 100L),
                    MacroStep("DPAD_RIGHT", 80L),
                    MacroStep("DPAD_CENTER", 50L)
                ),
                isPreset = true
            ),
            RemoteMacro(
                id = "preset_cinema_mode",
                name = "Cinema Mode",
                description = "Wakes TV, sets comfortable volume, and launches Netflix",
                iconName = "movie",
                steps = listOf(
                    MacroStep("WAKE_UP", 800L),
                    MacroStep("VOLUME_UP", 200L),
                    MacroStep("LAUNCH_APP", 500L, "com.netflix.ninja")
                ),
                isPreset = true
            ),
            RemoteMacro(
                id = "preset_youtube_kids",
                name = "Launch YouTube",
                description = "Navigates to Leanback Home and opens YouTube",
                iconName = "video",
                steps = listOf(
                    MacroStep("HOME", 600L),
                    MacroStep("LAUNCH_APP", 500L, "com.google.android.youtube.tv")
                ),
                isPreset = true
            ),
            RemoteMacro(
                id = "preset_mi_patchwall",
                name = "Xiaomi PatchWall",
                description = "Directly switches Mi TV Stick to PatchWall entertainment hub",
                iconName = "tv",
                steps = listOf(
                    MacroStep("LAUNCH_PATCHWALL", 300L)
                ),
                isPreset = true
            ),
            RemoteMacro(
                id = "preset_tv_reboot",
                name = "Quick Sleep/Wake",
                description = "Cycles standby power on unresponsive stick",
                iconName = "power",
                steps = listOf(
                    MacroStep("SLEEP", 1200L),
                    MacroStep("WAKE_UP", 500L),
                    MacroStep("HOME", 400L)
                ),
                isPreset = true
            )
        )
    }
}
