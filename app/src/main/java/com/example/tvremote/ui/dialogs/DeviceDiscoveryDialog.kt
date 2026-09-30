package com.example.tvremote.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.ConnectionMedium
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.TvDevice
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassBorderSubtle
import com.example.ui.theme.IosGlassCard
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemGray5
import com.example.ui.theme.IosSystemGray6
import com.example.ui.theme.IosSystemGreen
import com.example.ui.theme.IosSystemOrange
import com.example.ui.theme.IosSystemRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDiscoveryDialog(
    connectionStatus: ConnectionStatus,
    discoveredDevices: List<TvDevice>,
    savedDevices: List<TvDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (TvDevice) -> Unit,
    onConnectManual: (ip: String, port: String, isMiStick: Boolean, protocol: ProtocolType) -> Unit,
    onSubmitPin: (String) -> Unit,
    onDisconnect: () -> Unit,
    onDeleteSavedDevice: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    // PIN Pairing Dialog
    if (connectionStatus is ConnectionStatus.PairingRequired) {
        PinPairingModal(
            device = connectionStatus.device,
            prompt = connectionStatus.prompt,
            onSubmit = onSubmitPin,
            onCancel = onDisconnect
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IosGlassElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device Discovery & Pairing",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary
                    )
                    Text(
                        text = "Connect to Android TV, Mi Stick, or Google TV",
                        fontSize = 12.sp,
                        color = IosTextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = IosTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = IosSystemGray6,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = IosSystemBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Discovered (${discoveredDevices.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) IosSystemBlue else IosTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Manual IP",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) IosSystemBlue else IosTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Saved (${savedDevices.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) IosSystemBlue else IosTextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> DiscoveredTabContent(
                    devices = discoveredDevices,
                    activeStatus = connectionStatus,
                    isScanning = isScanning,
                    onRefresh = {
                        if (isScanning) onStopScan() else onStartScan()
                    },
                    onConnect = onConnectDevice,
                    onDisconnect = onDisconnect
                )
                1 -> ManualConnectTabContent(
                    onConnect = onConnectManual
                )
                2 -> SavedDevicesTabContent(
                    saved = savedDevices,
                    activeStatus = connectionStatus,
                    onConnect = onConnectDevice,
                    onDelete = onDeleteSavedDevice
                )
            }
        }
    }
}

@Composable
private fun DiscoveredTabContent(
    devices: List<TvDevice>,
    activeStatus: ConnectionStatus,
    isScanning: Boolean,
    onRefresh: () -> Unit,
    onConnect: (TvDevice) -> Unit,
    onDisconnect: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = IosSystemBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning Wi-Fi & Bluetooth...", fontSize = 12.sp, color = IosSystemBlue)
                } else {
                    Text("Wi-Fi mDNS & Bluetooth Devices", fontSize = 12.sp, color = IosTextSecondary)
                }
            }

            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = IosTextPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(36.dp)
                    .border(1.dp, IosGlassBorderHighlight, RoundedCornerShape(12.dp))
                    .testTag("btn_refresh_scan")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(16.dp),
                    tint = IosTextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isScanning) "Stop" else "Scan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (devices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IosGlassCard)
                    .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = IosTextTertiary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Android TVs discovered yet", color = IosTextPrimary, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ensure your TV / Mi Stick and phone are on the same Wi-Fi",
                        color = IosTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices, key = { it.id }) { device ->
                    val isConnected = (activeStatus as? ConnectionStatus.Connected)?.device?.id == device.id
                    DeviceListItem(
                        device = device,
                        isConnected = isConnected,
                        onConnect = { onConnect(device) },
                        onDisconnect = onDisconnect
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceListItem(
    device: TvDevice,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0C000000))
            .clip(RoundedCornerShape(16.dp))
            .background(if (isConnected) IosSystemGreen.copy(alpha = 0.12f) else Color.White)
            .border(
                1.dp,
                if (isConnected) IosSystemGreen.copy(alpha = 0.4f) else IosGlassBorderHighlight,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
            .testTag("device_item_${device.id}")
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
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (device.isMiStick) IosSystemOrange.copy(alpha = 0.15f) else IosSystemBlue.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (device.connectionMedium == ConnectionMedium.BLUETOOTH) Icons.Default.Bluetooth else Icons.Default.Tv,
                        contentDescription = null,
                        tint = if (device.isMiStick) IosSystemOrange else IosSystemBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosTextPrimary
                        )
                        if (device.isMiStick) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IosSystemOrange)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("MI STICK", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosSystemRed.copy(alpha = 0.12f),
                        contentColor = IosSystemRed
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_disconnect_${device.id}")
                ) {
                    Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp).testTag("btn_connect_${device.id}")
                ) {
                    Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ManualConnectTabContent(
    onConnect: (ip: String, port: String, isMiStick: Boolean, protocol: ProtocolType) -> Unit
) {
    var ip by remember { mutableStateOf("192.168.1.") }
    var port by remember { mutableStateOf("6467") }
    var isMiStick by remember { mutableStateOf(true) }
    var selectedProtocol by remember { mutableStateOf(ProtocolType.ANDROID_TV_V2) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Manual IP Connection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IosTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            label = { Text("TV IP Address") },
            placeholder = { Text("e.g. 192.168.1.150") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IosSystemBlue,
                unfocusedBorderColor = IosGlassBorderSubtle,
                focusedTextColor = IosTextPrimary,
                unfocusedTextColor = IosTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_manual_ip")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port (6467 for Android TV v2, 5555 for ADB)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IosSystemBlue,
                unfocusedBorderColor = IosGlassBorderSubtle,
                focusedTextColor = IosTextPrimary,
                unfocusedTextColor = IosTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("input_manual_port")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Xiaomi Mi Stick Optimization", fontSize = 13.sp, color = IosTextPrimary)
                Text("Enables sleep wake-up and PatchWall integration", fontSize = 11.sp, color = IosTextSecondary)
            }
            Switch(
                checked = isMiStick,
                onCheckedChange = { isMiStick = it },
                colors = SwitchDefaults.colors(checkedThumbColor = IosSystemOrange)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onConnect(ip, port, isMiStick, selectedProtocol)
            },
            colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_manual_connect")
        ) {
            Text("Connect Directly to TV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SavedDevicesTabContent(
    saved: List<TvDevice>,
    activeStatus: ConnectionStatus,
    onConnect: (TvDevice) -> Unit,
    onDelete: (String) -> Unit
) {
    if (saved.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(IosGlassCard)
                .border(1.dp, IosGlassBorderSubtle, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No saved devices yet", color = IosTextSecondary, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(saved, key = { it.id }) { device ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x0C000000))
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
                            Text(device.name, fontWeight = FontWeight.Bold, color = IosTextPrimary)
                            Text(device.displaySubtitle, fontSize = 11.sp, color = IosTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onDelete(device.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IosSystemRed)
                            }
                            Button(
                                onClick = { onConnect(device) },
                                colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinPairingModal(
    device: TvDevice,
    prompt: String,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tv, contentDescription = null, tint = IosSystemBlue, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enter TV Pairing Code", color = IosTextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = "A pairing code is now displayed on your ${device.name} screen (${device.ipAddress}). Enter the code below:",
                    color = IosTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        pin = input.filter { it.isLetterOrDigit() }.take(8).uppercase()
                    },
                    label = { Text("6-character code (e.g. 1A2B3C)") },
                    placeholder = { Text("Code from TV screen") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IosSystemBlue,
                        unfocusedBorderColor = IosGlassBorderSubtle,
                        focusedTextColor = IosTextPrimary,
                        unfocusedTextColor = IosTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_pairing_pin")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tip: Make sure your TV and phone are on the exact same Wi-Fi network. The code is shown in a system dialog on your TV.",
                    fontSize = 11.sp,
                    color = IosTextMuted,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(pin) },
                enabled = pin.length >= 4,
                colors = ButtonDefaults.buttonColors(containerColor = IosSystemBlue, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_submit_pin")
            ) {
                Text("Confirm & Pair", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = IosTextSecondary, fontSize = 13.sp)
            }
        },
        containerColor = IosGlassElevated
    )
}
