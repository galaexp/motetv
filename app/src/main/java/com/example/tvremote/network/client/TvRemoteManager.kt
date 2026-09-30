package com.example.tvremote.network.client

import android.content.Context
import android.util.Log
import com.example.tvremote.domain.model.ConnectionStatus
import com.example.tvremote.domain.model.MacroStep
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.RemoteMacro
import com.example.tvremote.domain.model.RemoteSettings
import com.example.tvremote.domain.model.TvDevice
import com.example.tvremote.network.protocol.AdbTcpProtocol
import com.example.tvremote.network.protocol.AndroidTvV2Protocol
import com.example.tvremote.network.protocol.MiStickAdapter
import com.example.tvremote.network.protocol.TvProtocolHandler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TvRemoteManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val tag = "TvRemoteManager"

    private val v2Protocol = AndroidTvV2Protocol(context)
    private val adbProtocol = AdbTcpProtocol()
    private val miStickAdapter = MiStickAdapter(v2Protocol, adbProtocol)

    private var activeHandler: TvProtocolHandler = v2Protocol
    private var keepAliveJob: Job? = null
    private var activeDevice: TvDevice? = null
    private var currentSettings: RemoteSettings = RemoteSettings()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _isExecutingMacro = MutableStateFlow(false)
    val isExecutingMacro: StateFlow<Boolean> = _isExecutingMacro.asStateFlow()

    fun updateSettings(settings: RemoteSettings) {
        this.currentSettings = settings
    }

    fun connect(device: TvDevice) {
        coroutineScope.launch {
            _connectionStatus.value = ConnectionStatus.Connecting(device)
            activeDevice = device

            // Choose appropriate protocol
            activeHandler = when {
                device.isMiStick || currentSettings.miStickOptimization -> miStickAdapter
                device.protocolType == ProtocolType.ADB_TCP -> adbProtocol
                else -> v2Protocol
            }

            val result = activeHandler.connect(device) { pinPrompt ->
                _connectionStatus.value = ConnectionStatus.PairingRequired(device, pinPrompt)
            }

            result.onSuccess { isFullyConnected ->
                if (isFullyConnected) {
                    onConnectedSuccess(device)
                }
            }.onFailure { error ->
                Log.e(tag, "Connection failed: ${error.message}")
                _connectionStatus.value = ConnectionStatus.Error(
                    message = error.message ?: "Could not connect to ${device.name}",
                    device = device
                )
            }
        }
    }

    fun submitPin(pin: String) {
        val dev = activeDevice ?: return
        coroutineScope.launch {
            _connectionStatus.value = ConnectionStatus.Connecting(dev)
            val result = activeHandler.submitPairingPin(pin)
            result.onSuccess {
                val paired = dev.copy(isPaired = true)
                activeDevice = paired
                onConnectedSuccess(paired)
            }.onFailure { error ->
                _connectionStatus.value = ConnectionStatus.Error(
                    message = "Pairing failed: ${error.message}",
                    device = dev
                )
            }
        }
    }

    private fun onConnectedSuccess(device: TvDevice) {
        _connectionStatus.value = ConnectionStatus.Connected(
            device = device,
            latencyMs = 15L,
            protocol = device.protocolType
        )
        startKeepAlive(device)
    }

    fun sendCommand(command: RemoteCommand) {
        coroutineScope.launch {
            if (command is RemoteCommand.SkipYouTubeAd) {
                skipYouTubeAd()
                return@launch
            }
            val result = activeHandler.sendCommand(command)
            result.onFailure { error ->
                Log.w(tag, "Failed to send command $command: ${error.message}")
                if (!activeHandler.isConnected && currentSettings.autoReconnect && activeDevice != null) {
                    handleConnectionDrop()
                }
            }
        }
    }

    private suspend fun skipYouTubeAd() {
        Log.d(tag, "Executing YouTube Ad Skip Sequence")
        // Step 1: Up arrow to activate HUD / focus skip button
        activeHandler.sendCommand(RemoteCommand.DpadUp)
        delay(120L)
        // Step 2: Right to move cursor to Skip Ad
        activeHandler.sendCommand(RemoteCommand.DpadRight)
        delay(90L)
        // Step 3: Center OK to trigger Skip
        activeHandler.sendCommand(RemoteCommand.DpadCenter)
        // Secondary pulse for older YouTube for Android TV builds
        delay(140L)
        activeHandler.sendCommand(RemoteCommand.DpadRight)
        delay(80L)
        activeHandler.sendCommand(RemoteCommand.DpadCenter)
    }

    fun sendText(text: String) {
        coroutineScope.launch {
            activeHandler.sendText(text)
        }
    }

    fun sendTouchMove(dx: Float, dy: Float) {
        coroutineScope.launch {
            activeHandler.sendTouchMove(dx * currentSettings.touchpadSensitivity, dy * currentSettings.touchpadSensitivity)
        }
    }

    fun launchApp(packageName: String) {
        coroutineScope.launch {
            activeHandler.launchApp(packageName)
        }
    }

    fun openUrl(url: String) {
        coroutineScope.launch {
            val result = activeHandler.openUrl(url)
            result.onFailure { error ->
                Log.w(tag, "Failed to open URL $url: ${error.message}")
            }
        }
    }

    fun runMacro(macro: RemoteMacro) {
        if (_isExecutingMacro.value) return
        coroutineScope.launch {
            _isExecutingMacro.value = true
            try {
                for (step in macro.steps) {
                    executeStep(step)
                    if (step.delayMs > 0) {
                        delay(step.delayMs)
                    }
                }
            } catch (e: CancellationException) {
                Log.d(tag, "Macro cancelled")
            } catch (e: Exception) {
                Log.e(tag, "Error running macro ${macro.name}", e)
            } finally {
                _isExecutingMacro.value = false
            }
        }
    }

    private suspend fun executeStep(step: MacroStep) {
        val cmd = when (step.commandKey) {
            "DPAD_UP" -> RemoteCommand.DpadUp
            "DPAD_DOWN" -> RemoteCommand.DpadDown
            "DPAD_LEFT" -> RemoteCommand.DpadLeft
            "DPAD_RIGHT" -> RemoteCommand.DpadRight
            "DPAD_CENTER" -> RemoteCommand.DpadCenter
            "HOME" -> RemoteCommand.Home
            "BACK" -> RemoteCommand.Back
            "MENU" -> RemoteCommand.Menu
            "POWER" -> RemoteCommand.PowerToggle
            "WAKE_UP" -> RemoteCommand.WakeUp
            "SLEEP" -> RemoteCommand.Sleep
            "VOLUME_UP" -> RemoteCommand.VolumeUp
            "VOLUME_DOWN" -> RemoteCommand.VolumeDown
            "VOLUME_MUTE" -> RemoteCommand.VolumeMute
            "CHANNEL_UP" -> RemoteCommand.ChannelUp
            "CHANNEL_DOWN" -> RemoteCommand.ChannelDown
            "PLAY_PAUSE" -> RemoteCommand.PlayPause
            "LAUNCH_PATCHWALL" -> RemoteCommand.LaunchPatchWall
            "SKIP_YOUTUBE_AD" -> {
                skipYouTubeAd()
                return
            }
            "LAUNCH_APP" -> {
                if (step.extraArg != null) {
                    activeHandler.launchApp(step.extraArg)
                    return
                } else null
            }
            "SEND_TEXT" -> {
                if (step.extraArg != null) {
                    activeHandler.sendText(step.extraArg)
                    return
                } else null
            }
            else -> null
        }

        if (cmd != null) {
            activeHandler.sendCommand(cmd)
        }
    }

    private fun startKeepAlive(device: TvDevice) {
        keepAliveJob?.cancel()
        val intervalMs = (currentSettings.keepAliveIntervalSec.coerceIn(5, 60)) * 1000L

        keepAliveJob = coroutineScope.launch {
            while (isActive && activeHandler.isConnected) {
                delay(intervalMs)
                val latency = activeHandler.ping()
                if (latency > 0) {
                    if (_connectionStatus.value is ConnectionStatus.Connected) {
                        _connectionStatus.value = ConnectionStatus.Connected(
                            device = device,
                            latencyMs = latency,
                            protocol = device.protocolType
                        )
                    }
                } else if (!activeHandler.isConnected) {
                    Log.w(tag, "Keep-alive ping failed; connection lost")
                    handleConnectionDrop()
                    break
                }
            }
        }
    }

    private fun handleConnectionDrop() {
        val dev = activeDevice
        _connectionStatus.value = ConnectionStatus.Error("Connection lost to ${dev?.name ?: "TV"}", dev)
        if (currentSettings.autoReconnect && dev != null) {
            coroutineScope.launch {
                delay(2000L)
                Log.i(tag, "Attempting auto-reconnect to ${dev.name}")
                connect(dev)
            }
        }
    }

    fun disconnect() {
        keepAliveJob?.cancel()
        keepAliveJob = null
        activeHandler.disconnect()
        activeDevice = null
        _connectionStatus.value = ConnectionStatus.Disconnected
    }
}
