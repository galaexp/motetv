package com.example.tvremote.network.discovery

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.tvremote.domain.model.ConnectionMedium
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.TvDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothDiscoveryManager(private val context: Context) {
    private val tag = "BtDiscovery"
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _discoveredBtDevices = MutableStateFlow<Map<String, TvDevice>>(emptyMap())
    val discoveredBtDevices: StateFlow<Map<String, TvDevice>> = _discoveredBtDevices.asStateFlow()

    private val _isBtScanning = MutableStateFlow(false)
    val isBtScanning: StateFlow<Boolean> = _isBtScanning.asStateFlow()

    private var receiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    if (device != null) {
                        val name = try { device.name } catch (e: Exception) { null } ?: "Bluetooth Device"
                        val address = device.address ?: return

                        // Filter or mark TV/Stick candidates
                        val isTvCandidate = isLikelyTvDevice(name, device)
                        val isMi = name.lowercase().contains("mi") || name.lowercase().contains("xiaomi")

                        val tvDevice = TvDevice(
                            id = "bt_$address",
                            name = name,
                            ipAddress = "127.0.0.1",
                            port = 0,
                            macAddress = address,
                            protocolType = ProtocolType.GENERIC_SOCKET,
                            isMiStick = isMi,
                            lastSeen = System.currentTimeMillis(),
                            connectionMedium = ConnectionMedium.BLUETOOTH,
                            modelInfo = if (isMi) "Xiaomi Mi Stick (Bluetooth)" else "Bluetooth Remote Target"
                        )

                        val current = _discoveredBtDevices.value.toMutableMap()
                        current[tvDevice.id] = tvDevice
                        _discoveredBtDevices.value = current
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isBtScanning.value = false
                }
            }
        }
    }

    private fun hasBtPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(tag, "Bluetooth is disabled or not supported")
            return
        }

        if (!hasBtPermission()) {
            Log.w(tag, "Bluetooth permissions not granted")
            return
        }

        // 1. First add already paired / bonded devices
        try {
            val bonded = bluetoothAdapter.bondedDevices
            val map = _discoveredBtDevices.value.toMutableMap()
            bonded?.forEach { dev ->
                val name = dev.name ?: "Paired TV"
                val addr = dev.address
                val isMi = name.lowercase().contains("mi") || name.lowercase().contains("xiaomi")
                map["bt_$addr"] = TvDevice(
                    id = "bt_$addr",
                    name = name,
                    ipAddress = "127.0.0.1",
                    port = 0,
                    macAddress = addr,
                    protocolType = ProtocolType.GENERIC_SOCKET,
                    isMiStick = isMi,
                    isPaired = true,
                    connectionMedium = ConnectionMedium.BLUETOOTH,
                    modelInfo = "Paired Bluetooth Device"
                )
            }
            _discoveredBtDevices.value = map
        } catch (e: Exception) {
            Log.e(tag, "Error reading bonded devices", e)
        }

        // 2. Start active broadcast discovery
        try {
            if (!receiverRegistered) {
                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context.registerReceiver(receiver, filter)
                receiverRegistered = true
            }

            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
            _isBtScanning.value = true
        } catch (e: Exception) {
            Log.e(tag, "Error starting bluetooth discovery", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            if (receiverRegistered) {
                context.unregisterReceiver(receiver)
                receiverRegistered = false
            }
        } catch (e: Exception) {
            Log.w(tag, "Error unregistering BT receiver", e)
        } finally {
            _isBtScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun isLikelyTvDevice(name: String, device: BluetoothDevice): Boolean {
        val lower = name.lowercase()
        if (lower.contains("tv") || lower.contains("stick") || lower.contains("box") ||
            lower.contains("mi") || lower.contains("shield") || lower.contains("chromecast")) {
            return true
        }
        val btClass = device.bluetoothClass?.deviceClass ?: 0
        // Audio/Video or display device classes
        return btClass in 1024..1096
    }
}
