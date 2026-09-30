package com.example.tvremote.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.CustomControlButton
import com.example.tvremote.domain.model.RemoteCommand
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosGlassFill
import com.example.ui.theme.IosGlassUltra
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemGray6
import com.example.ui.theme.IosSystemIndigo
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun DpadController(
    onCommand: (RemoteCommand) -> Unit,
    onOpenVoice: () -> Unit = {},
    onOpenPush: () -> Unit = {},
    customButtons: List<CustomControlButton> = CustomControlButton.defaultButtons(),
    isEditingLayout: Boolean = false,
    onCustomButtonClick: (CustomControlButton) -> Unit = {},
    onCustomButtonLongClick: (CustomControlButton) -> Unit = {},
    onMoveButtonLeft: (String) -> Unit = {},
    onMoveButtonRight: (String) -> Unit = {},
    onDeleteButton: (String) -> Unit = {},
    onAddButtonClick: () -> Unit = {},
    onResetButtonsClick: () -> Unit = {},
    onToggleEditMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Gentle wiggle animation during Edit Mode (iOS style)
    val infiniteTransition = rememberInfiniteTransition(label = "jiggle")
    val jiggleRotation by infiniteTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(120),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Edit Mode Header Toolbar
        AnimatedVisibility(
            visible = isEditingLayout,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(IosGlassElevated)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(IosSystemIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Edit Mode",
                                tint = IosSystemIndigo,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Layout Editor Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                            Text(
                                text = "Move, tap to edit, (-) to delete",
                                fontSize = 10.sp,
                                color = IosTextSecondary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onResetButtonsClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IosSystemGray5,
                                contentColor = IosTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Reset", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onToggleEditMode,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IosSystemBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // Customizable Control Buttons Deck (4-Column)
        // ==========================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(IosGlassCard)
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(22.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK CONTROL DECK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )

                if (!isEditingLayout) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleEditMode() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Customize",
                            tint = IosSystemIndigo,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit Layout",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosSystemIndigo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Button Grid
            val rows = customButtons.chunked(4)
            rows.forEach { rowButtons ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowButtons.forEach { button ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .rotate(if (isEditingLayout) jiggleRotation else 0f)
                        ) {
                            CustomButtonCell(
                                button = button,
                                isEditing = isEditingLayout,
                                onClick = { onCustomButtonClick(button) },
                                onLongClick = { onCustomButtonLongClick(button) },
                                onDelete = { onDeleteButton(button.id) },
                                onMoveLeft = { onMoveButtonLeft(button.id) },
                                onMoveRight = { onMoveButtonRight(button.id) }
                            )
                        }
                    }

                    // Pad empty slots in last row if not full
                    repeat(4 - rowButtons.size) {
                        if (isEditingLayout && it == 0) {
                            Box(modifier = Modifier.weight(1f)) {
                                AddButtonPlaceholder(onClick = onAddButtonClick)
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (isEditingLayout && rows.isNotEmpty() && rows.last().size == 4) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(modifier = Modifier.width(76.dp)) {
                        AddButtonPlaceholder(onClick = onAddButtonClick)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // Apple TV Style Frosted Glass D-PAD Wheel
        // ==========================================
        Box(
            modifier = Modifier
                .size(240.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0x18000000))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF3F6FA),
                            Color(0xFFE5ECF4)
                        )
                    )
                )
                .border(1.5.dp, IosGlassBorderHighlight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Directional Arrow Track Ring
            Box(
                modifier = Modifier
                    .size(236.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0x12000000), CircleShape)
            )

            // UP Touch Sector
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .size(76.dp, 60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onCommand(RemoteCommand.DpadUp) }
                    .testTag("btn_dpad_up"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "DPad Up",
                    tint = IosTextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // DOWN Touch Sector
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .size(76.dp, 60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onCommand(RemoteCommand.DpadDown) }
                    .testTag("btn_dpad_down"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "DPad Down",
                    tint = IosTextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // LEFT Touch Sector
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(60.dp, 76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onCommand(RemoteCommand.DpadLeft) }
                    .testTag("btn_dpad_left"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "DPad Left",
                    tint = IosTextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // RIGHT Touch Sector
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(60.dp, 76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onCommand(RemoteCommand.DpadRight) }
                    .testTag("btn_dpad_right"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "DPad Right",
                    tint = IosTextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // CENTER / OK Frosted Button
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .shadow(10.dp, CircleShape, spotColor = Color(0x18000000))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFF0F4FA)
                            )
                        )
                    )
                    .border(1.2.dp, IosGlassBorderHighlight, CircleShape)
                    .clickable { onCommand(RemoteCommand.DpadCenter) }
                    .testTag("btn_dpad_ok"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = IosTextPrimary,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // Navigation Triad: Back, Home, PatchWall
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosGlassNavPill(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Back",
                testTag = "btn_back",
                onClick = { onCommand(RemoteCommand.Back) }
            )

            IosGlassNavPill(
                icon = Icons.Default.Home,
                label = "Home",
                accent = true,
                testTag = "btn_home",
                onClick = { onCommand(RemoteCommand.Home) }
            )

            IosGlassNavPill(
                icon = Icons.Default.Tv,
                label = "PatchWall",
                testTag = "btn_patchwall",
                onClick = { onCommand(RemoteCommand.LaunchPatchWall) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // Apple TV Style Dual Rockers (VOL & CH) with Voice Search in Center
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volume Rocker
            IosGlassRockerPillar(
                label = "VOL",
                onPlus = { onCommand(RemoteCommand.VolumeUp) },
                onMinus = { onCommand(RemoteCommand.VolumeDown) },
                plusTag = "btn_vol_up",
                minusTag = "btn_vol_down"
            )

            // Center Column: Apple TV Siri / Voice Search and Quick Mute
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Search Button (Glowing Frosted Sphere)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(10.dp, CircleShape, spotColor = IosSystemBlue.copy(alpha = 0.35f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    IosSystemBlue,
                                    Color(0xFF5AC8FA)
                                )
                            )
                        )
                        .clickable { onOpenVoice() }
                        .testTag("action_voice_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Search",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Voice",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosSystemBlue
                )

                // Quick Mute Pill
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(4.dp, CircleShape, spotColor = Color(0x10000000))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, IosGlassBorderHighlight, CircleShape)
                        .clickable { onCommand(RemoteCommand.VolumeMute) }
                        .testTag("btn_quick_mute"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = "Mute Volume",
                        tint = IosTextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Channel Rocker
            IosGlassRockerPillar(
                label = "CH",
                onPlus = { onCommand(RemoteCommand.ChannelUp) },
                onMinus = { onCommand(RemoteCommand.ChannelDown) },
                plusTag = "btn_ch_up",
                minusTag = "btn_ch_down"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // iOS Frosted Glass Media Playback Strip
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(18.dp))
                .background(IosGlassCard)
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(18.dp))
                .padding(vertical = 5.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosPlaybackButton(
                icon = Icons.Default.FastRewind,
                desc = "Rewind",
                testTag = "btn_media_rewind",
                onClick = { onCommand(RemoteCommand.Rewind) }
            )

            IosPlaybackButton(
                icon = Icons.Default.PlayArrow,
                desc = "Play / Pause",
                testTag = "btn_media_play_pause",
                accent = true,
                onClick = { onCommand(RemoteCommand.PlayPause) }
            )

            IosPlaybackButton(
                icon = Icons.Default.FastForward,
                desc = "Fast Forward",
                testTag = "btn_media_fast_forward",
                onClick = { onCommand(RemoteCommand.FastForward) }
            )

            IosPlaybackButton(
                icon = Icons.Default.Stop,
                desc = "Stop",
                testTag = "btn_media_stop",
                onClick = { onCommand(RemoteCommand.Stop) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomButtonCell(
    button: CustomControlButton,
    isEditing: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(
                    1.dp,
                    if (isEditing) Color(button.colorHex).copy(alpha = 0.6f) else IosGlassBorderSubtle,
                    RoundedCornerShape(16.dp)
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(button.colorHex).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ButtonIconHelper.getIcon(button.iconKey),
                    contentDescription = button.label,
                    tint = Color(button.colorHex),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = button.label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Reorder controls in Edit Mode
            if (isEditing) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(IosSystemGray5)
                            .clickable { onMoveLeft() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Move Left",
                            tint = IosTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(IosSystemGray5)
                            .clickable { onMoveRight() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Move Right",
                            tint = IosTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Delete (-) Badge in Edit Mode
        if (isEditing && button.isDeletable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(IosSystemRed)
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Button",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun AddButtonPlaceholder(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(IosGlassFill)
            .border(1.dp, IosSystemBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Button",
                tint = IosSystemBlue,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Add",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosSystemBlue
            )
        }
    }
}

@Composable
private fun IosGlassNavPill(
    icon: ImageVector,
    label: String,
    accent: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp, 46.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0x10000000))
                .clip(RoundedCornerShape(16.dp))
                .background(if (accent) IosSystemBlue.copy(alpha = 0.12f) else Color.White)
                .border(
                    1.dp,
                    if (accent) IosSystemBlue.copy(alpha = 0.4f) else IosGlassBorderHighlight,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onClick() }
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (accent) IosSystemBlue else IosTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = IosTextSecondary
        )
    }
}

@Composable
private fun IosGlassRockerPillar(
    label: String,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    plusTag: String,
    minusTag: String
) {
    Box(
        modifier = Modifier
            .width(68.dp)
            .height(132.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Plus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onPlus() }
                    .testTag(plusTag),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary
                )
            }

            // Central Label Capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(IosSystemGray6),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextSecondary
                )
            }

            // Minus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable { onMinus() }
                    .testTag(minusTag),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary
                )
            }
        }
    }
}

@Composable
private fun IosPlaybackButton(
    icon: ImageVector,
    desc: String,
    testTag: String,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (accent) IosSystemBlue else Color.Transparent)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = if (accent) Color.White else IosTextPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}
