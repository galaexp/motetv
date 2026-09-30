package com.example.tvremote.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.ui.components.AppsAndMacrosView
import com.example.tvremote.ui.components.DpadController
import com.example.tvremote.ui.components.KeypadView
import com.example.tvremote.ui.components.RemoteTopBar
import com.example.tvremote.ui.components.TouchpadView
import com.example.tvremote.ui.dialogs.AddButtonDialog
import com.example.tvremote.ui.dialogs.AddMacroDialog
import com.example.tvremote.ui.dialogs.CustomizeButtonDialog
import com.example.tvremote.ui.dialogs.DeviceDiscoveryDialog
import com.example.tvremote.ui.dialogs.PinPairingModal
import com.example.tvremote.ui.dialogs.PushToTvDialog
import com.example.tvremote.ui.dialogs.VoiceSearchDialog
import com.example.tvremote.ui.screens.SettingsScreen
import com.example.ui.theme.IosCanvasAmbientLilac
import com.example.ui.theme.IosCanvasAmbientSky
import com.example.ui.theme.IosCanvasBackground
import com.example.ui.theme.IosGlassBorderHighlight
import com.example.ui.theme.IosGlassElevated
import com.example.ui.theme.IosSystemBlue
import com.example.ui.theme.IosSystemBlueSubtle
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.IosTextTertiary

@Composable
fun RemoteScreen(
    viewModel: RemoteViewModel,
    modifier: Modifier = Modifier
) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val savedDevices by viewModel.savedDevices.collectAsState()
    val macros by viewModel.macros.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isExecutingMacro by viewModel.isExecutingMacro.collectAsState()

    val showDeviceSheet by viewModel.showDeviceSheet.collectAsState()
    val showVoiceDialog by viewModel.showVoiceDialog.collectAsState()
    val showAddMacroDialog by viewModel.showAddMacroDialog.collectAsState()
    val showPushDialog by viewModel.showPushDialog.collectAsState()
    val currentPushPayload by viewModel.currentPushPayload.collectAsState()
    val recentPushes by viewModel.recentPushes.collectAsState()
    val voiceText by viewModel.voiceText.collectAsState()
    val isListeningVoice by viewModel.isListening.collectAsState()

    // Customizable Control Buttons State
    val customButtons by viewModel.customButtons.collectAsState()
    val isEditingLayout by viewModel.isEditingLayout.collectAsState()
    val editingButton by viewModel.editingButton.collectAsState()
    val showAddButtonDialog by viewModel.showAddButtonDialog.collectAsState()

    // Ambient light iOS backdrop brush
    val ambientBackdrop = Brush.verticalGradient(
        colors = listOf(
            IosCanvasBackground,
            IosCanvasAmbientSky,
            IosCanvasAmbientLilac
        )
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ambientBackdrop),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            RemoteTopBar(
                currentTab = selectedTab,
                connectionStatus = connectionStatus,
                onOpenDevices = { viewModel.selectTab(RemoteTab.SETTINGS) },
                onPowerToggle = { viewModel.sendCommand(RemoteCommand.PowerToggle) }
            )
        },
        bottomBar = {
            // Frosted Glass iOS Tab Bar with specular border
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), spotColor = Color(0x14000000))
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(
                        1.dp,
                        IosGlassBorderHighlight,
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ),
                color = IosGlassElevated
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    RemoteTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val icon = when (tab) {
                            RemoteTab.REMOTE -> Icons.Default.Tv
                            RemoteTab.TOUCHPAD -> Icons.Default.PanTool
                            RemoteTab.KEYPAD -> Icons.Default.Dialpad
                            RemoteTab.APPS_MACROS -> Icons.Default.GridView
                            RemoteTab.SETTINGS -> Icons.Default.Settings
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = IosSystemBlue,
                                indicatorColor = IosSystemBlue,
                                unselectedIconColor = IosTextTertiary,
                                unselectedTextColor = IosTextTertiary
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_content_transition"
            ) { currentTab ->
                when (currentTab) {
                    RemoteTab.REMOTE -> {
                        DpadController(
                            onCommand = { viewModel.sendCommand(it) },
                            onOpenVoice = { viewModel.openVoiceDialog() },
                            onOpenPush = { viewModel.openPushDialog() },
                            customButtons = customButtons,
                            isEditingLayout = isEditingLayout,
                            onCustomButtonClick = { viewModel.handleCustomButtonClick(it) },
                            onCustomButtonLongClick = { viewModel.toggleEditLayout() },
                            onMoveButtonLeft = { viewModel.moveButtonLeft(it) },
                            onMoveButtonRight = { viewModel.moveButtonRight(it) },
                            onDeleteButton = { viewModel.deleteCustomButton(it) },
                            onAddButtonClick = { viewModel.openAddButtonDialog() },
                            onResetButtonsClick = { viewModel.resetCustomButtons() },
                            onToggleEditMode = { viewModel.toggleEditLayout() }
                        )
                    }
                    RemoteTab.TOUCHPAD -> {
                        TouchpadView(
                            onCommand = { viewModel.sendCommand(it) },
                            onTouchMove = { dx, dy -> viewModel.sendTouchpadMove(dx, dy) },
                            onTouchClick = { viewModel.sendTouchpadClick() }
                        )
                    }
                    RemoteTab.KEYPAD -> {
                        KeypadView(
                            onCommand = { viewModel.sendCommand(it) }
                        )
                    }
                    RemoteTab.APPS_MACROS -> {
                        AppsAndMacrosView(
                            macros = macros,
                            isExecutingMacro = isExecutingMacro,
                            onLaunchApp = { viewModel.launchApp(it) },
                            onCommand = { viewModel.sendCommand(it) },
                            onRunMacro = { viewModel.runMacro(it) },
                            onDeleteMacro = { viewModel.deleteMacro(it) },
                            onAddMacroClick = { viewModel.openAddMacroDialog() }
                        )
                    }
                    RemoteTab.SETTINGS -> {
                        SettingsScreen(
                            connectionStatus = connectionStatus,
                            discoveredDevices = discoveredDevices,
                            savedDevices = savedDevices,
                            settings = settings,
                            isScanning = isScanning,
                            onStartScan = { viewModel.startDiscovery() },
                            onStopScan = { viewModel.stopDiscovery() },
                            onConnectDevice = { viewModel.connectDevice(it) },
                            onConnectManual = { ip, port, isMi, proto -> viewModel.connectManual(ip, port, isMi, proto) },
                            onDisconnect = { viewModel.disconnect() },
                            onDeleteSavedDevice = { viewModel.deleteSavedDevice(it) },
                            onUpdateSettings = { viewModel.updateSettings(it) },
                            onOpenPush = { viewModel.openPushDialog() }
                        )
                    }
                }
            }
        }
    }

    // Modal: Customize Single Button
    if (editingButton != null) {
        CustomizeButtonDialog(
            button = editingButton!!,
            onSave = { viewModel.updateCustomButton(it) },
            onDelete = { viewModel.deleteCustomButton(it) },
            onDismiss = { viewModel.closeCustomizeButton() }
        )
    }

    // Modal: Add Button from Catalog
    if (showAddButtonDialog) {
        AddButtonDialog(
            onAdd = { viewModel.addCustomButton(it) },
            onDismiss = { viewModel.closeAddButtonDialog() }
        )
    }

    // ROOT LEVEL PIN PAIRING MODAL - Pops up unconditionally whenever code entry is needed
    if (connectionStatus is ConnectionStatus.PairingRequired) {
        val pairing = connectionStatus as ConnectionStatus.PairingRequired
        PinPairingModal(
            device = pairing.device,
            prompt = pairing.prompt,
            onSubmit = { pin -> viewModel.submitPin(pin) },
            onCancel = { viewModel.disconnect() }
        )
    }

    // Device Discovery & Pairing Sheet
    if (showDeviceSheet) {
        DeviceDiscoveryDialog(
            connectionStatus = connectionStatus,
            discoveredDevices = discoveredDevices,
            savedDevices = savedDevices,
            isScanning = isScanning,
            onStartScan = { viewModel.startDiscovery() },
            onStopScan = { viewModel.stopDiscovery() },
            onConnectDevice = { viewModel.connectDevice(it) },
            onConnectManual = { ip, port, isMi, proto -> viewModel.connectManual(ip, port, isMi, proto) },
            onSubmitPin = { viewModel.submitPin(it) },
            onDisconnect = { viewModel.disconnect() },
            onDeleteSavedDevice = { viewModel.deleteSavedDevice(it) },
            onDismiss = { viewModel.closeDeviceSheet() }
        )
    }

    // Voice Search Dialog
    if (showVoiceDialog) {
        VoiceSearchDialog(
            voiceText = voiceText,
            isListening = isListeningVoice,
            onStartListening = { viewModel.startVoiceListening() },
            onStopListening = { viewModel.stopVoiceListening() },
            onSendQuery = { viewModel.sendVoiceTextToTv() },
            onDismiss = { viewModel.closeVoiceDialog() }
        )
    }

    // Add Custom Macro Dialog
    if (showAddMacroDialog) {
        AddMacroDialog(
            onSave = { name, desc, icon, steps ->
                viewModel.addCustomMacro(name, desc, icon, steps)
            },
            onDismiss = { viewModel.closeAddMacroDialog() }
        )
    }

    // Universal Push to TV Dialog (Share Sheet receiver & Manual URL Caster)
    if (showPushDialog) {
        PushToTvDialog(
            initialPayload = currentPushPayload,
            connectionStatus = connectionStatus,
            recentPushes = recentPushes,
            onPush = { viewModel.pushPayloadToTv(it) },
            onSelectRecent = { viewModel.selectRecentPush(it) },
            onClearHistory = { viewModel.clearPushHistory() },
            onDismiss = { viewModel.closePushDialog() }
        )
    }
}
