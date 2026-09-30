package com.example.tvremote.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.ConnectionMedium
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.RemoteSettings
import com.example.tvremote.domain.model.TvDevice
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassDivider
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
fun SettingsScreen(
    connectionStatus: ConnectionStatus,
    discoveredDevices: List<TvDevice>,
    savedDevices: List<TvDevice>,
    settings: RemoteSettings,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (TvDevice) -> Unit,
    onConnectManual: (ip: String, port: String, isMiStick: Boolean, protocol: ProtocolType) -> Unit,
    onDisconnect: () -> Unit,
    onDeleteSavedDevice: (String) -> Unit,
    onUpdateSettings: (RemoteSettings) -> Unit,
    onOpenPush: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showManualIp by remember { mutableStateOf(false) }
    var manualIp by remember { mutableStateOf("192.168.1.") }
    var manualPort by remember { mutableStateOf("6467") }
    var manualIsMiStick by remember { mutableStateOf(true) }
    var manualProtocol by remember { mutableStateOf(ProtocolType.ANDROID_TV_V2) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. Current Active Device Card (Frosted Glass)
        item {
            ActiveDeviceCard(
                status = connectionStatus,
                onDisconnect = onDisconnect
            )
        }

        // 2. Discovered Devices & Pairing Section
        item {
            SettingsSectionHeader(
                title = "DISCOVERED DEVICES",
                subtitle = "Local network (mDNS) & Bluetooth targets",
                action = {
                    Button(
                        onClick = { if (isScanning) onStopScan() else onStartScan() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScanning) IosSystemBlue.copy(alpha = 0.15f) else Color.White,
                            contentColor = if (isScanning) IosSystemBlue else IosTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0A000000))
                            .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(12.dp))
                            .testTag("btn_settings_scan")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = IosSystemBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scanning", fontSize = 11.sp, color = IosSystemBlue, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = IosSystemBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan", fontSize = 11.sp, color = IosTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }

        if (discoveredDevices.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0C000000))
                        .clip(RoundedCornerShape(20.dp))
                        .background(IosGlassCard)
                        .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(IosSystemBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Router,
                                contentDescription = null,
                                tint = IosSystemBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isScanning) "Searching for Android TV devices..." else "No devices discovered yet",
                            color = IosTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ensure your TV or Mi Stick is powered on and on the same Wi-Fi",
                            color = IosTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(discoveredDevices, key = { it.id }) { device ->
                val isConnected = (connectionStatus as? ConnectionStatus.Connected)?.device?.id == device.id
                DiscoveredDeviceCard(
                    device = device,
                    isConnected = isConnected,
                    onConnect = { onConnectDevice(device) },
                    onDisconnect = onDisconnect
                )
            }
        }

        // Manual IP Connect Expander
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0C000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosGlassCard)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showManualIp = !showManualIp },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(IosSystemBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tv,
                                    contentDescription = null,
                                    tint = IosSystemBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Manual IP Connection",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IosTextPrimary
                            )
                        }
                        Icon(
                            imageVector = if (showManualIp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = IosTextTertiary
                        )
                    }

                    AnimatedVisibility(visible = showManualIp) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            OutlinedTextField(
                                value = manualIp,
                                onValueChange = { manualIp = it },
                                label = { Text("IP Address", fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IosSystemBlue,
                                    unfocusedBorderColor = IosGlassBorderSubtle,
                                    focusedTextColor = IosTextPrimary,
                                    unfocusedTextColor = IosTextPrimary,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_manual_ip")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualPort,
                                    onValueChange = { manualPort = it },
                                    label = { Text("Port", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = IosSystemBlue,
                                        unfocusedBorderColor = IosGlassBorderSubtle,
                                        focusedTextColor = IosTextPrimary,
                                        unfocusedTextColor = IosTextPrimary,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_manual_port")
                                )

                                Button(
                                    onClick = {
                                        onConnectManual(manualIp, manualPort, manualIsMiStick, manualProtocol)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("settings_btn_connect_manual")
                                ) {
                                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Saved & Paired Devices
        if (savedDevices.isNotEmpty()) {
            item {
                SettingsSectionHeader(
                    title = "SAVED DEVICES",
                    subtitle = "Previously paired TV remotes"
                )
            }

            items(savedDevices, key = { "saved_${it.id}" }) { device ->
                SavedDeviceRow(
                    device = device,
                    onConnect = { onConnectDevice(device) },
                    onDelete = { onDeleteSavedDevice(device.id) }
                )
            }
        }

        // 4. Connection & Network Preferences
        item {
            SettingsSectionHeader(
                title = "CONNECTION PREFERENCES",
                subtitle = "Discovery mediums and reconnect behavior"
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0C000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosGlassCard)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Wi-Fi Discovery toggle
                    SettingToggleRow(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi Discovery (mDNS Bonjour)",
                        description = "Automatically finds Android TV 2 and Google Cast targets",
                        checked = settings.wifiDiscoveryEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(wifiDiscoveryEnabled = it)) }
                    )

                    HorizontalDivider(color = IosGlassDivider)

                    // Bluetooth Discovery toggle
                    SettingToggleRow(
                        icon = Icons.Default.Bluetooth,
                        title = "Bluetooth Discovery",
                        description = "Searches for Bluetooth remotes and bonded TV devices",
                        checked = settings.bluetoothDiscoveryEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(bluetoothDiscoveryEnabled = it)) }
                    )

                    HorizontalDivider(color = IosGlassDivider)

                    // Auto-Reconnect
                    SettingToggleRow(
                        icon = Icons.Default.Refresh,
                        title = "Auto-Reconnect",
                        description = "Re-establishes connection automatically if Wi-Fi socket drops",
                        checked = settings.autoReconnect,
                        onCheckedChange = { onUpdateSettings(settings.copy(autoReconnect = it)) }
                    )

                    HorizontalDivider(color = IosGlassDivider)

                    // Wake-on-LAN broadcast
                    SettingToggleRow(
                        icon = Icons.Default.Tv,
                        title = "Wake-on-LAN Standby Wake",
                        description = "Sends network wake packets before issuing power commands",
                        checked = settings.wakeOnLanEnabled,
                        onCheckedChange = { onUpdateSettings(settings.copy(wakeOnLanEnabled = it)) }
                    )
                }
            }
        }

        // 5. Xiaomi Mi TV Stick Optimizations (Spacious & Fixed Spacing)
        item {
            SettingsSectionHeader(
                title = "XIAOMI MI STICK SUITE",
                subtitle = "Specialized hardware workarounds and shortcuts"
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0C000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosGlassCard)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Deep Sleep Wakeup Protocol Toggle
                    SettingToggleRow(
                        icon = Icons.Default.Tv,
                        title = "Deep Sleep Wakeup Protocol",
                        description = "Wakes dormant Mi Stick Wi-Fi with burst WOL packets & home pulse",
                        checked = settings.miStickOptimization,
                        badge = "ACTIVE",
                        onCheckedChange = { onUpdateSettings(settings.copy(miStickOptimization = it)) }
                    )

                    HorizontalDivider(color = IosGlassDivider)

                    // Keep-Alive interval slider
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Socket Keep-Alive Heartbeat",
                                fontSize = 13.sp,
                                color = IosTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IosSystemBlue.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${settings.keepAliveIntervalSec}s",
                                    fontSize = 11.5.sp,
                                    color = IosSystemBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prevents Mi Stick thermal power management from disconnecting idle sockets",
                            fontSize = 11.sp,
                            color = IosTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = settings.keepAliveIntervalSec.toFloat(),
                            onValueChange = { onUpdateSettings(settings.copy(keepAliveIntervalSec = it.toInt())) },
                            valueRange = 5f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = IosSystemBlue,
                                activeTrackColor = IosSystemBlue,
                                inactiveTrackColor = IosSystemGray5
                            )
                        )
                    }
                }
            }
        }

        // 6. Smart Shortcuts & Customization
        item {
            SettingsSectionHeader(
                title = "SMART SHORTCUTS & CUSTOMIZATION",
                subtitle = "Ad-skipping, haptics, and touchpad speed"
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0C000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosGlassCard)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // YouTube Ad Skip Feature
                    SettingToggleRow(
                        icon = Icons.Default.ElectricBolt,
                        title = "YouTube Ad Skip Turbo",
                        description = "Automates Up+Right+OK pulses to instantly skip skippable ads on TV",
                        checked = settings.youtubeAdSkipTurbo,
                        badge = "POPULAR",
                        onCheckedChange = { onUpdateSettings(settings.copy(youtubeAdSkipTurbo = it)) }
                    )

                    HorizontalDivider(color = IosGlassDivider)

                    // Haptic feedback
                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = "Haptic Tactile Feedback",
                        description = "Subtle vibration pulse on every button actuation",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { onUpdateSettings(settings.copy(hapticFeedback = it)) }
                    )

                    HorizontalDivider(color = IosGlassDivider)

                    // Touchpad Sensitivity
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Touchpad Sensitivity",
                                fontSize = 13.sp,
                                color = IosTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IosSystemBlue.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format("%.1fx", settings.touchpadSensitivity),
                                    fontSize = 11.5.sp,
                                    color = IosSystemBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = settings.touchpadSensitivity,
                            onValueChange = { onUpdateSettings(settings.copy(touchpadSensitivity = it)) },
                            valueRange = 0.5f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = IosSystemBlue,
                                activeTrackColor = IosSystemBlue,
                                inactiveTrackColor = IosSystemGray5
                            )
                        )
                    }
                }
            }
        }

        // 7. Universal Push to TV Feature Card (Frosted Glass Hub)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = IosSystemBlue.copy(alpha = 0.15f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosGlassElevated)
                    .border(1.dp, IosSystemBlue.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(IosSystemBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = IosSystemBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Universal Push to TV",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                            Text(
                                text = "System Share-Sheet & Direct Link Caster",
                                fontSize = 11.5.sp,
                                color = IosSystemBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Share any video or web link from YouTube, Chrome, TikTok, Netflix, or Reddit to Nova Remote to play it instantly on your TV. You can also paste text to type directly into TV input fields.",
                        fontSize = 12.sp,
                        color = IosTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenPush,
                        colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_settings_open_push")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Push to TV Hub", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // 8. About & Diagnostics
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0A000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(IosGlassCard)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nova Remote Diagnostics", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary)
                    Text("Protocol: Android TV Remote v2 (TLS 6467) & ADB TCP (5555)", fontSize = 11.5.sp, color = IosTextSecondary)
                    Text("Mi Stick Profile: Enhanced (WOL + PatchWall mapping active)", fontSize = 11.sp, color = IosTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { onUpdateSettings(RemoteSettings()) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = IosSystemRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset to Defaults", fontSize = 11.5.sp, color = IosSystemRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun ActiveDeviceCard(
    status: ConnectionStatus,
    onDisconnect: () -> Unit
) {
    val isConnected = status is ConnectionStatus.Connected
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0x10000000))
            .clip(RoundedCornerShape(22.dp))
            .background(IosGlassElevated)
            .border(
                1.dp,
                if (isConnected) IosSystemGreen.copy(alpha = 0.4f) else IosGlassBorderHighlight,
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) IosSystemGreen.copy(alpha = 0.15f) else IosSystemGray5),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = if (isConnected) IosSystemGreen else IosTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = when (status) {
                            is ConnectionStatus.Connected -> status.device.name
                            is ConnectionStatus.Connecting -> "Connecting..."
                            is ConnectionStatus.PairingRequired -> "Pairing Required"
                            is ConnectionStatus.Scanning -> "Scanning Network..."
                            else -> "Not Connected"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary
                    )
                    Text(
                        text = when (status) {
                            is ConnectionStatus.Connected -> "${status.device.ipAddress} • ${status.latencyMs}ms • ${if (status.device.isMiStick) "Mi Stick" else "Android TV"}"
                            is ConnectionStatus.Connecting -> "Establishing socket to TV..."
                            is ConnectionStatus.PairingRequired -> "Enter PIN code displayed on screen"
                            else -> "Select a device below to pair"
                        },
                        fontSize = 11.sp,
                        color = IosTextSecondary
                    )
                }
            }

            if (isConnected) {
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Disconnect", fontSize = 11.5.sp, color = IosSystemRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = IosSystemBlue,
                letterSpacing = 0.8.sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = IosTextSecondary
            )
        }
        action?.invoke()
    }
}

@Composable
private fun DiscoveredDeviceCard(
    device: TvDevice,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                1.dp,
                if (isConnected) IosSystemGreen.copy(alpha = 0.5f) else IosGlassBorderHighlight,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
            .testTag("device_card_${device.id}")
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IosSystemBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (device.connectionMedium == ConnectionMedium.BLUETOOTH) Icons.Default.Bluetooth else Icons.Default.Tv,
                        contentDescription = null,
                        tint = IosSystemBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosTextPrimary
                        )
                        if (device.isMiStick) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IosSystemOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("MI STICK", fontSize = 8.5.sp, color = IosSystemOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = device.displaySubtitle,
                        fontSize = 11.sp,
                        color = IosTextSecondary
                    )
                }
            }

            if (isConnected) {
                Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Disconnect", fontSize = 11.sp, color = IosSystemRed, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("btn_connect_${device.id}")
                ) {
                    Text("Connect", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SavedDeviceRow(
    device: TvDevice,
    onConnect: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(device.name, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = IosTextPrimary)
                Text(device.ipAddress, fontSize = 11.sp, color = IosTextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = IosSystemRed, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Pair", fontSize = 11.5.sp, color = IosSystemBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    badge: String? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IosSystemBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IosSystemBlue,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IosTextPrimary
                    )
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(IosSystemGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 8.5.sp,
                                color = IosSystemGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = IosTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = IosSystemGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = IosSystemGray5,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
