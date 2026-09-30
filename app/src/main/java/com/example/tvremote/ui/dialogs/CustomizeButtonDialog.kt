package com.example.tvremote.ui.dialogs

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tvremote.domain.model.CustomControlButton
import com.example.tvremote.ui.components.ButtonIconHelper
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun CustomizeButtonDialog(
    button: CustomControlButton,
    onSave: (CustomControlButton) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(button.label) }
    var selectedIcon by remember { mutableStateOf(button.iconKey) }
    var selectedColor by remember { mutableLongStateOf(button.colorHex) }
    var selectedCommand by remember { mutableStateOf(button.commandType) }

    val accentColors = listOf(
        0xFF007AFF, // iOS Blue
        0xFFFF3B30, // iOS Red
        0xFF34C759, // iOS Green
        0xFFFF9500, // iOS Orange
        0xFF5856D6, // iOS Indigo
        0xFFAF52DE, // iOS Purple
        0xFF30B0C7, // iOS Teal
        0xFF1C1C1E  // iOS Black
    )

    val availableCommands = listOf(
        "TV_INPUT" to "TV Input / Source",
        "MUTE" to "Volume Mute",
        "MENU" to "TV Menu",
        "SKIP_AD" to "Skip YouTube Ad",
        "PUSH_TO_TV" to "Push to TV",
        "HOME" to "Home Screen",
        "BACK" to "Back",
        "RECENT_APPS" to "Recent Apps",
        "SETTINGS" to "Settings",
        "INFO" to "Info / Details",
        "GUIDE" to "TV Guide",
        "SUBTITLES" to "Subtitles",
        "PLAY_PAUSE" to "Play / Pause",
        "REWIND" to "Rewind 10s",
        "FAST_FORWARD" to "Fast Forward 10s",
        "STOP" to "Stop Playback",
        "VOL_UP" to "Volume Up",
        "VOL_DOWN" to "Volume Down",
        "CH_UP" to "Channel Up",
        "CH_DOWN" to "Channel Down",
        "APP_YOUTUBE" to "Launch YouTube",
        "APP_NETFLIX" to "Launch Netflix",
        "APP_PRIME" to "Launch Prime Video",
        "APP_DISNEY" to "Launch Disney+",
        "APP_SPOTIFY" to "Launch Spotify",
        "SLEEP" to "Sleep Timer",
        "POWER" to "Power Toggle"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0x20000000))
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(26.dp)),
            color = IosGlassElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customize Button",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosTextPrimary
                        )
                        Text(
                            text = "Tailor layout, action & aesthetic",
                            fontSize = 12.sp,
                            color = IosTextSecondary
                        )
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

                Spacer(modifier = Modifier.height(18.dp))

                // Live Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(IosGlassCard)
                        .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(selectedColor).copy(alpha = 0.14f))
                                .border(1.dp, Color(selectedColor).copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ButtonIconHelper.getIcon(selectedIcon),
                                contentDescription = label,
                                tint = Color(selectedColor),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label.ifBlank { "Button" },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Label Input
                Text(
                    text = "BUTTON LABEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_button_label"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = IosSystemBlue,
                        unfocusedBorderColor = IosGlassBorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color Accent Selector
                Text(
                    text = "ACCENT COLOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    accentColors.forEach { colorVal ->
                        val isSelected = selectedColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    if (isSelected) 3.dp else 1.dp,
                                    if (isSelected) Color.White else Color(0x33000000),
                                    CircleShape
                                )
                                .clickable { selectedColor = colorVal },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon Picker
                Text(
                    text = "CHOOSE ICON",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ButtonIconHelper.availableIconKeys.forEach { iconKey ->
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(selectedColor).copy(alpha = 0.16f) else IosSystemGray5)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(selectedColor) else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ButtonIconHelper.getIcon(iconKey),
                                contentDescription = iconKey,
                                tint = if (isSelected) Color(selectedColor) else IosTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Command Action Selector
                Text(
                    text = "ASSIGN TV COMMAND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextTertiary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(14.dp))
                        .verticalScroll(rememberScrollState())
                ) {
                    availableCommands.forEach { (cmdKey, cmdName) ->
                        val isSelected = selectedCommand == cmdKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCommand = cmdKey }
                                .background(if (isSelected) IosSystemBlue.copy(alpha = 0.12f) else Color.Transparent)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cmdName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) IosSystemBlue else IosTextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = IosSystemBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Actions: Delete & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (button.isDeletable) {
                        Button(
                            onClick = {
                                onDelete(button.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IosSystemRed.copy(alpha = 0.12f),
                                contentColor = IosSystemRed
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_delete_custom_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Delete", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val updated = button.copy(
                                label = label.ifBlank { "Button" },
                                iconKey = selectedIcon,
                                colorHex = selectedColor,
                                commandType = selectedCommand
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IosSystemBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp)
                            .testTag("btn_save_custom_button")
                    ) {
                        Text(text = "Save Changes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
