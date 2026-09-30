package com.example.tvremote.ui.dialogs

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.domain.model.PushMediaType
import com.example.tvremote.domain.model.PushToTvPayload
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemGray6
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemIndigo
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun PushToTvDialog(
    initialPayload: PushToTvPayload?,
    connectionStatus: ConnectionStatus,
    recentPushes: List<PushToTvPayload>,
    onPush: (PushToTvPayload) -> Unit,
    onSelectRecent: (PushToTvPayload) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember(initialPayload) {
        mutableStateOf(initialPayload?.rawContent ?: "")
    }

    val currentPayload = remember(inputText) {
        if (inputText.isNotBlank()) PushToTvPayload.parse(inputText) else null
    }

    val isConnected = connectionStatus is ConnectionStatus.Connected
    val deviceName = (connectionStatus as? ConnectionStatus.Connected)?.device?.name ?: "No TV Connected"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0x18000000))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, IosGlassElevated)
                    )
                )
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(IosSystemBlue.copy(alpha = 0.15f))
                                .border(1.dp, IosSystemBlue.copy(alpha = 0.35f), RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Push",
                                tint = IosSystemBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "UNIVERSAL PUSH TO TV",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isConnected) IosSystemGreen else IosSystemOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isConnected) "Target: $deviceName" else "Offline • Pair TV in Settings",
                                    fontSize = 11.sp,
                                    color = if (isConnected) IosSystemGreen else IosSystemOrange
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IosSystemGray5)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = IosTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input Box with Paste Button
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Paste YouTube link, stream URL, or text…",
                            fontSize = 13.sp,
                            color = IosTextTertiary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_url_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IosTextPrimary,
                        unfocusedTextColor = IosTextPrimary,
                        focusedBorderColor = IosSystemBlue,
                        unfocusedBorderColor = IosGlassBorderSubtle,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 3,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    inputText = clip
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = IosSystemBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickPresetChip(
                        label = "🎬 YouTube Demo",
                        onClick = { inputText = "https://www.youtube.com/watch?v=dQw4w9WgXcQ" }
                    )
                    QuickPresetChip(
                        label = "🚀 NASA 4K Live",
                        onClick = { inputText = "https://www.youtube.com/watch?v=21X5lGlDOfg" }
                    )
                    QuickPresetChip(
                        label = "☕ Lofi Beats",
                        onClick = { inputText = "https://www.youtube.com/watch?v=jfKfPfyJRdk" }
                    )
                    QuickPresetChip(
                        label = "🌐 Open Google",
                        onClick = { inputText = "https://www.google.com" }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Detection Preview Card
                if (currentPayload != null) {
                    DetectionPreviewCard(payload = currentPayload)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Launch CTA Button
                Button(
                    onClick = {
                        if (currentPayload != null) {
                            onPush(currentPayload)
                        }
                    },
                    enabled = currentPayload != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_trigger_push_to_tv"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosSystemBlue,
                        contentColor = Color.White,
                        disabledContainerColor = IosSystemGray5,
                        disabledContentColor = IosTextTertiary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentPayload?.mediaType == PushMediaType.TEXT_INPUT) "Type into TV Screen" else "Push & Play on TV",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Recent Pushes History
                if (recentPushes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = IosTextTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECENT CASTS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextTertiary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = IosSystemRed,
                            modifier = Modifier
                                .clickable { onClearHistory() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                    ) {
                        recentPushes.take(3).forEach { item ->
                            RecentPushRow(
                                payload = item,
                                onClick = {
                                    inputText = item.rawContent
                                    onSelectRecent(item)
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectionPreviewCard(payload: PushToTvPayload) {
    val badgeColor = Color(payload.mediaType.badgeColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (payload.mediaType) {
                        PushMediaType.TEXT_INPUT -> Icons.Default.TextFields
                        PushMediaType.WEB_BROWSER -> Icons.Default.Link
                        else -> Icons.Default.OndemandVideo
                    },
                    contentDescription = payload.mediaType.displayName,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = payload.mediaType.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = payload.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IosTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = payload.mediaType.description,
                    fontSize = 11.sp,
                    color = IosTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickPresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = IosTextPrimary
        )
    }
}

@Composable
private fun RecentPushRow(
    payload: PushToTvPayload,
    onClick: () -> Unit
) {
    val badgeColor = Color(payload.mediaType.badgeColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
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
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = payload.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Push again",
                tint = IosSystemBlue,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
