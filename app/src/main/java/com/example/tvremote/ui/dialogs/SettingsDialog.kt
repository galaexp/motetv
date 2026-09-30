package com.example.tvremote.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.RemoteSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    settings: RemoteSettings,
    onUpdateSettings: (RemoteSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var haptic by remember(settings) { mutableStateOf(settings.hapticFeedback) }
    var miStickOpt by remember(settings) { mutableStateOf(settings.miStickOptimization) }
    var autoReconnect by remember(settings) { mutableStateOf(settings.autoReconnect) }
    var sensitivity by remember(settings) { mutableFloatStateOf(settings.touchpadSensitivity) }
    var preferredProto by remember(settings) { mutableStateOf(settings.preferredProtocol) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1523)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Remote Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Haptic Feedback
            SettingSwitchItem(
                title = "Haptic Vibration",
                subtitle = "Tactile vibration click on every remote button press",
                checked = haptic,
                onCheckedChange = {
                    haptic = it
                    onUpdateSettings(settings.copy(hapticFeedback = it))
                },
                testTag = "switch_haptic"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Xiaomi Mi Stick Optimization
            SettingSwitchItem(
                title = "Xiaomi Mi Stick Enhancements",
                subtitle = "Sends Wake-on-LAN packets on standby, enables PatchWall key, and keeps 15s connection alive",
                checked = miStickOpt,
                badge = "RECOMMENDED",
                onCheckedChange = {
                    miStickOpt = it
                    onUpdateSettings(settings.copy(miStickOptimization = it))
                },
                testTag = "switch_mistick_opt"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Auto Reconnect
            SettingSwitchItem(
                title = "Auto-Reconnect",
                subtitle = "Automatically re-establishes connection if Wi-Fi socket drops",
                checked = autoReconnect,
                onCheckedChange = {
                    autoReconnect = it
                    onUpdateSettings(settings.copy(autoReconnect = it))
                },
                testTag = "switch_auto_reconnect"
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Touchpad Sensitivity
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Touchpad Sensitivity", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Text(String.format("%.1fx", sensitivity), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00D2FF))
                }
                Slider(
                    value = sensitivity,
                    onValueChange = {
                        sensitivity = it
                        onUpdateSettings(settings.copy(touchpadSensitivity = it))
                    },
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00D2FF),
                        activeTrackColor = Color(0xFF0284C7)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Protocol Mode
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Default Protocol Mode", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProtocolChoiceChip(
                        label = "Android TV v2 (Port 6467)",
                        selected = preferredProto == ProtocolType.ANDROID_TV_V2,
                        onClick = {
                            preferredProto = ProtocolType.ANDROID_TV_V2
                            onUpdateSettings(settings.copy(preferredProtocol = ProtocolType.ANDROID_TV_V2))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ProtocolChoiceChip(
                        label = "ADB TCP (Port 5555)",
                        selected = preferredProto == ProtocolType.ADB_TCP,
                        onClick = {
                            preferredProto = ProtocolType.ADB_TCP
                            onUpdateSettings(settings.copy(preferredProtocol = ProtocolType.ADB_TCP))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    badge: String? = null,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (badge != null) {
                    Spacer(modifier = Modifier.size(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00D2FF),
                checkedTrackColor = Color(0xFF0369A1)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ProtocolChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF0369A1) else Color(0xFF1E293B))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF94A3B8)
        )
    }
}
