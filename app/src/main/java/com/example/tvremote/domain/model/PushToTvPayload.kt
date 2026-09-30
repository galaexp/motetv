package com.example.tvremote.domain.model

enum class PushMediaType(
    val displayName: String,
    val badgeColorHex: Long,
    val defaultAppPackage: String?,
    val description: String
) {
    YOUTUBE("YouTube", 0xFFFF0000, "com.google.android.youtube.tv", "Launches YouTube on TV"),
    NETFLIX("Netflix", 0xFFE50914, "com.netflix.ninja", "Opens Netflix title"),
    TWITCH("Twitch", 0xFF9146FF, "tv.twitch.android.app", "Streams Twitch channel"),
    SPOTIFY("Spotify", 0xFF1DB954, "com.spotify.tv.android", "Plays track on Spotify"),
    PRIME_VIDEO("Prime Video", 0xFF00A8E1, "com.amazon.amazonvideo.livingroom", "Opens Prime Video"),
    DIRECT_VIDEO("Stream Video", 0xFF10B981, "org.videolan.vlc", "Plays direct video link"),
    WEB_BROWSER("Web Page", 0xFF00E5FF, "com.android.chrome", "Opens page in TV Browser"),
    TEXT_INPUT("Text / Keyboard", 0xFF38BDF8, null, "Sends text to TV input field")
}

data class PushToTvPayload(
    val rawContent: String,
    val mediaType: PushMediaType,
    val targetUri: String,
    val title: String,
    val subtitle: String,
    val previewThumbnail: String? = null
) {
    companion object {
        private val urlRegex = Regex("""(https?://[^\s]+)""", RegexOption.IGNORE_CASE)

        fun parse(input: String): PushToTvPayload {
            val trimmed = input.trim()
            val match = urlRegex.find(trimmed)
            val detectedUrl = match?.value

            if (detectedUrl != null) {
                val cleanUrl = cleanUrlString(detectedUrl)
                val lower = cleanUrl.lowercase()

                return when {
                    lower.contains("youtube.com") || lower.contains("youtu.be") -> {
                        val videoId = extractYouTubeVideoId(cleanUrl)
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.YOUTUBE,
                            targetUri = cleanUrl,
                            title = if (videoId != null) "YouTube Video ($videoId)" else "YouTube Link",
                            subtitle = cleanUrl
                        )
                    }
                    lower.contains("netflix.com") -> {
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.NETFLIX,
                            targetUri = cleanUrl,
                            title = "Netflix Stream",
                            subtitle = cleanUrl
                        )
                    }
                    lower.contains("twitch.tv") -> {
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.TWITCH,
                            targetUri = cleanUrl,
                            title = "Twitch Live Stream",
                            subtitle = cleanUrl
                        )
                    }
                    lower.contains("spotify.com") -> {
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.SPOTIFY,
                            targetUri = cleanUrl,
                            title = "Spotify Track / Playlist",
                            subtitle = cleanUrl
                        )
                    }
                    lower.contains("primevideo.com") || lower.contains("amazon.com/video") -> {
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.PRIME_VIDEO,
                            targetUri = cleanUrl,
                            title = "Prime Video Stream",
                            subtitle = cleanUrl
                        )
                    }
                    lower.endsWith(".mp4") || lower.endsWith(".m3u8") || lower.endsWith(".mkv") || lower.endsWith(".webm") -> {
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.DIRECT_VIDEO,
                            targetUri = cleanUrl,
                            title = "Direct Video Stream",
                            subtitle = cleanUrl
                        )
                    }
                    else -> {
                        val domain = extractDomain(cleanUrl)
                        PushToTvPayload(
                            rawContent = trimmed,
                            mediaType = PushMediaType.WEB_BROWSER,
                            targetUri = cleanUrl,
                            title = "Web Page ($domain)",
                            subtitle = cleanUrl
                        )
                    }
                }
            } else {
                // Plain text or Wi-Fi password or search query
                val preview = if (trimmed.length > 40) trimmed.take(40) + "…" else trimmed
                return PushToTvPayload(
                    rawContent = trimmed,
                    mediaType = PushMediaType.TEXT_INPUT,
                    targetUri = trimmed,
                    title = "Text / Keyboard Input",
                    subtitle = "\"$preview\""
                )
            }
        }

        private fun cleanUrlString(url: String): String {
            return url.trimEnd('.', ',', ')', '>', ']', '"', '\'')
        }

        private fun extractYouTubeVideoId(url: String): String? {
            return try {
                when {
                    url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?").substringBefore("&")
                    url.contains("v=") -> url.substringAfter("v=").substringBefore("&")
                    url.contains("/shorts/") -> url.substringAfter("/shorts/").substringBefore("?").substringBefore("&")
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun extractDomain(url: String): String {
            return try {
                val noProto = url.substringAfter("://")
                noProto.substringBefore("/").substringBefore(":")
            } catch (_: Exception) {
                "Web Link"
            }
        }
    }
}
