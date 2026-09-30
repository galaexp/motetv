package com.example.tvremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.RemoteMacro
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

data class QuickApp(
    val name: String,
    val packageName: String,
    val badgeColor: Color,
    val initial: String
)

@Composable
fun AppsAndMacrosView(
    macros: List<RemoteMacro>,
    isExecutingMacro: Boolean,
    onLaunchApp: (String) -> Unit,
    onCommand: (RemoteCommand) -> Unit,
    onRunMacro: (RemoteMacro) -> Unit,
    onDeleteMacro: (String) -> Unit,
    onAddMacroClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickApps = listOf(
        QuickApp("YouTube", "com.google.android.youtube.tv", Color(0xFFFF0000), "YT"),
        QuickApp("Netflix", "com.netflix.ninja", Color(0xFFE50914), "N"),
        QuickApp("Prime", "com.amazon.amazonvideo.livingroom", Color(0xFF00A8E1), "PV"),
        QuickApp("Disney+", "com.disney.disneyplus", Color(0xFF113CCF), "D+"),
        QuickApp("PatchWall", "com.xiaomi.mitv.tvhome", Color(0xFFFF6900), "MI"),
        QuickApp("Spotify", "com.spotify.tv.android", Color(0xFF1DB954), "SP"),
        QuickApp("Twitch", "tv.twitch.android.app", Color(0xFF9146FF), "TW"),
        QuickApp("Plex", "com.plexapp.android", Color(0xFFE5A00D), "PX"),
        QuickApp("Kodi", "org.xbmc.kodi", Color(0xFF17B2E7), "KD")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // YouTube Ad Skip Featured Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x12000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, IosSystemRed.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("hero_skip_youtube_ad")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(IosSystemRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = IosSystemRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "YouTube Ad Skipper",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                            Text(
                                text = "Instantly skips sponsored ads on TV",
                                fontSize = 11.5.sp,
                                color = IosTextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { onCommand(RemoteCommand.SkipYouTubeAd) },
                        colors = ButtonDefaults.buttonColors(containerColor = IosSystemRed, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_hero_skip_ad")
                    ) {
                        Text("Skip Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // App Launcher Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TV APP LAUNCHER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )

                // Recent Apps Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(IosGlassCard)
                        .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(10.dp))
                        .clickable { onCommand(RemoteCommand.RecentApps) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("btn_recent_apps"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Recent Apps",
                        tint = IosTextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Apps",
                        fontSize = 11.sp,
                        color = IosTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Apps 3x3 Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickApps.chunked(3).forEach { rowApps ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowApps.forEach { app ->
                            AppTileItem(
                                app = app,
                                onClick = { onLaunchApp(app.packageName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Custom Macros Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROGRAMMABLE MACROS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextTertiary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Automate multi-key sequences with delays",
                        fontSize = 11.sp,
                        color = IosTextSecondary
                    )
                }

                Button(
                    onClick = onAddMacroClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = IosSystemBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(10.dp))
                        .testTag("btn_create_macro")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = IosSystemBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Macro items
        items(macros, key = { it.id }) { macro ->
            MacroCard(
                macro = macro,
                isExecuting = isExecutingMacro,
                onRun = { onRunMacro(macro) },
                onDelete = { onDeleteMacro(macro.id) }
            )
        }
    }
}

@Composable
private fun AppTileItem(
    app: QuickApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0C000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("app_launch_${app.name.lowercase().replace(" ", "_")}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(app.badgeColor.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.initial,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MacroCard(
    macro: RemoteMacro,
    isExecuting: Boolean,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0E000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("macro_card_${macro.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IosSystemBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (macro.iconName) {
                        "movie" -> Icons.Default.Movie
                        "video" -> Icons.Default.VideoLibrary
                        "tv" -> Icons.Default.Tv
                        "power" -> Icons.Default.Power
                        "ad_skip" -> Icons.Default.ElectricBolt
                        else -> Icons.Default.PlayArrow
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (macro.iconName == "ad_skip") IosSystemRed else IosSystemBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = macro.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosTextPrimary
                        )
                        if (macro.isPreset) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IosSystemGray5)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("PRESET", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = IosTextTertiary)
                            }
                        }
                    }
                    Text(
                        text = macro.description,
                        fontSize = 11.sp,
                        color = IosTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!macro.isPreset) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = IosTextTertiary, modifier = Modifier.size(16.dp))
                    }
                }

                Button(
                    onClick = onRun,
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("btn_run_macro_${macro.id}")
                ) {
                    if (isExecuting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("RUN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
