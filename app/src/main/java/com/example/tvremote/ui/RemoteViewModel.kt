package com.example.tvremote.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvremote.data.local.SettingsPreferences
import com.example.tvremote.data.local.TvDatabase
import com.example.tvremote.data.repository.DeviceRepository
import com.example.tvremote.data.repository.MacroRepository
import com.example.tvremote.domain.model.ConnectionMedium
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.domain.model.CustomControlButton
import com.example.tvremote.domain.model.MacroStep
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.PushMediaType
import com.example.tvremote.domain.model.PushToTvPayload
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.RemoteMacro
import com.example.tvremote.domain.model.RemoteSettings
import com.example.tvremote.domain.model.TvDevice
import com.example.tvremote.network.client.TvRemoteManager
import com.example.tvremote.network.discovery.BluetoothDiscoveryManager
import com.example.tvremote.network.discovery.NsdDiscoveryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class RemoteTab(val label: String) {
    REMOTE("Remote"),
    TOUCHPAD("Touchpad"),
    KEYPAD("Keypad"),
    APPS_MACROS("Apps"),
    SETTINGS("Settings")
}

class RemoteViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "RemoteViewModel"
    private val context = application.applicationContext

    // Local DB & Prefs
    private val database = TvDatabase.getDatabase(context)
    private val deviceRepo = DeviceRepository(database.deviceDao())
    private val macroRepo = MacroRepository(database.macroDao())
    private val settingsPrefs = SettingsPreferences(context)

    // Network & Protocol Manager
    private val remoteManager = TvRemoteManager(context, viewModelScope)
    private val nsdDiscovery = NsdDiscoveryManager(context)
    private val btDiscovery = BluetoothDiscoveryManager(context)

    // Haptics
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // State Flows
    val connectionStatus: StateFlow<ConnectionStatus> = remoteManager.connectionStatus
    val settings: StateFlow<RemoteSettings> = settingsPrefs.settingsFlow
    val savedDevices: StateFlow<List<TvDevice>> = deviceRepo.savedDevices.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val macros: StateFlow<List<RemoteMacro>> = macroRepo.macros.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), RemoteMacro.defaultPresets()
    )
    val isExecutingMacro: StateFlow<Boolean> = remoteManager.isExecutingMacro

    // Combined Discovered Devices (mDNS Wi-Fi + Bluetooth)
    val discoveredDevices: StateFlow<List<TvDevice>> = combine(
        nsdDiscovery.discoveredDevices,
        btDiscovery.discoveredBtDevices
    ) { nsdMap, btMap ->
        val combined = (nsdMap.values + btMap.values).distinctBy { it.id }
        combined.sortedWith(compareByDescending<TvDevice> { it.isMiStick }.thenBy { it.name })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isScanning: StateFlow<Boolean> = combine(
        nsdDiscovery.isScanning,
        btDiscovery.isBtScanning
    ) { nsd, bt -> nsd || bt }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI Dialog & Tab state
    private val _selectedTab = MutableStateFlow(RemoteTab.REMOTE)
    val selectedTab: StateFlow<RemoteTab> = _selectedTab.asStateFlow()

    private val _showDeviceSheet = MutableStateFlow(false)
    val showDeviceSheet: StateFlow<Boolean> = _showDeviceSheet.asStateFlow()

    private val _showSettingsSheet = MutableStateFlow(false)
    val showSettingsSheet: StateFlow<Boolean> = _showSettingsSheet.asStateFlow()

    private val _showVoiceDialog = MutableStateFlow(false)
    val showVoiceDialog: StateFlow<Boolean> = _showVoiceDialog.asStateFlow()

    private val _showAddMacroDialog = MutableStateFlow(false)
    val showAddMacroDialog: StateFlow<Boolean> = _showAddMacroDialog.asStateFlow()

    // Push to TV State (Share Sheet & In-App Caster)
    private val _showPushDialog = MutableStateFlow(false)
    val showPushDialog: StateFlow<Boolean> = _showPushDialog.asStateFlow()

    private val _currentPushPayload = MutableStateFlow<PushToTvPayload?>(null)
    val currentPushPayload: StateFlow<PushToTvPayload?> = _currentPushPayload.asStateFlow()

    private val _recentPushes = MutableStateFlow<List<PushToTvPayload>>(emptyList())
    val recentPushes: StateFlow<List<PushToTvPayload>> = _recentPushes.asStateFlow()

    // Customizable Control Buttons State
    private val _customButtons = MutableStateFlow<List<CustomControlButton>>(settingsPrefs.getCustomButtons())
    val customButtons: StateFlow<List<CustomControlButton>> = _customButtons.asStateFlow()

    private val _isEditingLayout = MutableStateFlow(false)
    val isEditingLayout: StateFlow<Boolean> = _isEditingLayout.asStateFlow()

    private val _editingButton = MutableStateFlow<CustomControlButton?>(null)
    val editingButton: StateFlow<CustomControlButton?> = _editingButton.asStateFlow()

    private val _showAddButtonDialog = MutableStateFlow(false)
    val showAddButtonDialog: StateFlow<Boolean> = _showAddButtonDialog.asStateFlow()

    // Voice recognition state
    private val _voiceText = MutableStateFlow("")
    val voiceText: StateFlow<String> = _voiceText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        // Load recent pushes
        loadRecentPushes()

        // Sync settings to manager
        viewModelScope.launch {
            settings.collect { current ->
                remoteManager.updateSettings(current)
            }
        }

        // Mark device paired when successfully connected
        viewModelScope.launch {
            remoteManager.connectionStatus.collect { status ->
                if (status is ConnectionStatus.Connected) {
                    deviceRepo.markPaired(status.device.id, true)
                }
            }
        }

        // Start initial Wi-Fi discovery automatically
        startDiscovery()
    }

    fun selectTab(tab: RemoteTab) {
        _selectedTab.value = tab
        triggerHaptic()
    }

    fun openDeviceSheet() {
        _showDeviceSheet.value = true
        startDiscovery()
    }

    fun closeDeviceSheet() {
        _showDeviceSheet.value = false
    }

    fun openSettingsSheet() {
        _showSettingsSheet.value = true
    }

    fun closeSettingsSheet() {
        _showSettingsSheet.value = false
    }

    fun openVoiceDialog() {
        _voiceText.value = ""
        _showVoiceDialog.value = true
    }

    fun closeVoiceDialog() {
        stopVoiceListening()
        _showVoiceDialog.value = false
    }

    fun openAddMacroDialog() {
        _showAddMacroDialog.value = true
    }

    fun closeAddMacroDialog() {
        _showAddMacroDialog.value = false
    }

    fun startDiscovery() {
        nsdDiscovery.startDiscovery()
        btDiscovery.startDiscovery()
    }

    fun stopDiscovery() {
        nsdDiscovery.stopDiscovery()
        btDiscovery.stopDiscovery()
    }

    fun connectDevice(device: TvDevice) {
        viewModelScope.launch {
            deviceRepo.saveDevice(device)
            remoteManager.connect(device)
        }
    }

    fun connectManual(ip: String, portStr: String, isMiStick: Boolean, protocol: ProtocolType) {
        val port = portStr.toIntOrNull() ?: if (protocol == ProtocolType.ADB_TCP) 5555 else 6467
        val device = TvDevice(
            id = "manual_$ip",
            name = if (isMiStick) "Mi Stick ($ip)" else "TV ($ip)",
            ipAddress = ip.trim(),
            port = port,
            protocolType = protocol,
            isMiStick = isMiStick,
            connectionMedium = ConnectionMedium.WIFI_MANUAL,
            modelInfo = if (isMiStick) "Xiaomi Mi Stick (Manual)" else "Android TV (Manual)"
        )
        connectDevice(device)
        closeDeviceSheet()
    }

    fun submitPin(pin: String) {
        remoteManager.submitPin(pin)
    }

    fun disconnect() {
        remoteManager.disconnect()
        triggerHaptic()
    }

    fun sendCommand(command: RemoteCommand) {
        triggerHaptic()
        remoteManager.sendCommand(command)
    }

    fun sendTextToTv(text: String) {
        triggerHaptic()
        remoteManager.sendText(text)
    }

    fun openUrl(url: String) {
        triggerHaptic()
        remoteManager.openUrl(url)
    }

    // Customizable Control Buttons Operations
    fun toggleEditLayout() {
        triggerHaptic()
        _isEditingLayout.value = !_isEditingLayout.value
    }

    fun setEditLayout(enabled: Boolean) {
        triggerHaptic()
        _isEditingLayout.value = enabled
    }

    fun moveButtonLeft(buttonId: String) {
        val list = _customButtons.value.toMutableList()
        val index = list.indexOfFirst { it.id == buttonId }
        if (index > 0) {
            triggerHaptic()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _customButtons.value = list
            settingsPrefs.saveCustomButtons(list)
        }
    }

    fun moveButtonRight(buttonId: String) {
        val list = _customButtons.value.toMutableList()
        val index = list.indexOfFirst { it.id == buttonId }
        if (index >= 0 && index < list.size - 1) {
            triggerHaptic()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _customButtons.value = list
            settingsPrefs.saveCustomButtons(list)
        }
    }

    fun moveButton(fromIndex: Int, toIndex: Int) {
        val list = _customButtons.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices && fromIndex != toIndex) {
            triggerHaptic()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _customButtons.value = list
            settingsPrefs.saveCustomButtons(list)
        }
    }

    fun deleteCustomButton(buttonId: String) {
        triggerHaptic()
        val list = _customButtons.value.filter { it.id != buttonId }
        _customButtons.value = list
        settingsPrefs.saveCustomButtons(list)
    }

    fun addCustomButton(button: CustomControlButton) {
        triggerHaptic()
        val list = _customButtons.value.toMutableList()
        list.add(button)
        _customButtons.value = list
        settingsPrefs.saveCustomButtons(list)
        _showAddButtonDialog.value = false
    }

    fun updateCustomButton(button: CustomControlButton) {
        triggerHaptic()
        val list = _customButtons.value.map {
            if (it.id == button.id) button else it
        }
        _customButtons.value = list
        settingsPrefs.saveCustomButtons(list)
        _editingButton.value = null
    }

    fun resetCustomButtons() {
        triggerHaptic()
        val defaults = settingsPrefs.resetCustomButtons()
        _customButtons.value = defaults
    }

    fun openCustomizeButton(button: CustomControlButton) {
        triggerHaptic()
        _editingButton.value = button
    }

    fun closeCustomizeButton() {
        _editingButton.value = null
    }

    fun openAddButtonDialog() {
        triggerHaptic()
        _showAddButtonDialog.value = true
    }

    fun closeAddButtonDialog() {
        _showAddButtonDialog.value = false
    }

    fun handleCustomButtonClick(button: CustomControlButton) {
        if (_isEditingLayout.value) {
            openCustomizeButton(button)
            return
        }
        triggerHaptic()
        if (button.commandType == "PUSH_TO_TV") {
            openPushDialog()
            return
        }
        val cmd = button.toRemoteCommand()
        if (cmd != null) {
            remoteManager.sendCommand(cmd)
        }
    }

    // Push To TV Handling
    fun handleIncomingSharedContent(content: String) {
        if (content.isBlank()) return
        val payload = PushToTvPayload.parse(content)
        _currentPushPayload.value = payload
        _showPushDialog.value = true
        triggerHaptic()
    }

    fun openPushDialog(initialInput: String? = null) {
        if (!initialInput.isNullOrBlank()) {
            _currentPushPayload.value = PushToTvPayload.parse(initialInput)
        } else if (_currentPushPayload.value == null) {
            // Default sample so user can test immediately
            _currentPushPayload.value = PushToTvPayload.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        }
        _showPushDialog.value = true
        triggerHaptic()
    }

    fun closePushDialog() {
        _showPushDialog.value = false
    }

    fun pushPayloadToTv(payload: PushToTvPayload) {
        triggerHaptic()
        if (payload.mediaType == PushMediaType.TEXT_INPUT) {
            remoteManager.sendText(payload.targetUri)
        } else {
            remoteManager.openUrl(payload.targetUri)
        }

        // Save to recent pushes
        settingsPrefs.addRecentPush(payload.rawContent)
        loadRecentPushes()
        closePushDialog()
    }

    fun selectRecentPush(payload: PushToTvPayload) {
        _currentPushPayload.value = payload
    }

    fun clearPushHistory() {
        settingsPrefs.clearRecentPushes()
        _recentPushes.value = emptyList()
    }

    private fun loadRecentPushes() {
        val rawList = settingsPrefs.getRecentPushes()
        _recentPushes.value = rawList.map { PushToTvPayload.parse(it) }
    }

    fun sendTouchpadMove(dx: Float, dy: Float) {
        remoteManager.sendTouchMove(dx, dy)
    }

    fun sendTouchpadClick() {
        triggerHaptic()
        remoteManager.sendCommand(RemoteCommand.DpadCenter)
    }

    fun launchApp(packageName: String) {
        triggerHaptic()
        remoteManager.launchApp(packageName)
    }

    fun runMacro(macro: RemoteMacro) {
        triggerHaptic()
        remoteManager.runMacro(macro)
    }

    fun addCustomMacro(name: String, desc: String, icon: String, steps: List<MacroStep>) {
        viewModelScope.launch {
            val macro = RemoteMacro(
                id = UUID.randomUUID().toString(),
                name = name,
                description = desc,
                iconName = icon,
                steps = steps,
                isPreset = false
            )
            macroRepo.saveMacro(macro)
            closeAddMacroDialog()
        }
    }

    fun deleteMacro(macroId: String) {
        viewModelScope.launch {
            macroRepo.deleteMacro(macroId)
        }
    }

    fun deleteSavedDevice(deviceId: String) {
        viewModelScope.launch {
            deviceRepo.deleteDevice(deviceId)
        }
    }

    fun updateSettings(newSettings: RemoteSettings) {
        settingsPrefs.updateSettings(newSettings)
    }

    fun triggerHaptic() {
        if (!settings.value.hapticFeedback) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Exception) {
            Log.w(tag, "Vibration failed", e)
        }
    }

    // Voice Search
    fun startVoiceListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceText.value = "Speech recognition is not available on this device"
            return
        }

        try {
            stopVoiceListening()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        Log.w(tag, "Speech recognition error: $error")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _voiceText.value = matches[0]
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _voiceText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(tag, "Failed to start speech recognition", e)
            _isListening.value = false
        }
    }

    fun stopVoiceListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
    }

    fun sendVoiceTextToTv() {
        val query = _voiceText.value.trim()
        if (query.isNotEmpty()) {
            // First trigger voice search on TV, then type text
            sendCommand(RemoteCommand.VoiceSearch)
            sendTextToTv(query)
            closeVoiceDialog()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopVoiceListening()
        stopDiscovery()
        remoteManager.disconnect()
    }
}
