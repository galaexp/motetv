package com.example.tvremote.network.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.tvremote.domain.model.ConnectionMedium
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.TvDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.InetAddress

class NsdDiscoveryManager(context: Context) {
    private val tag = "NsdDiscovery"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    private val _discoveredDevices = MutableStateFlow<Map<String, TvDevice>>(emptyMap())
    val discoveredDevices: StateFlow<Map<String, TvDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val serviceTypes = listOf(
        "_androidtvremote2._tcp.",
        "_androidtvremote._tcp.",
        "_googlecast._tcp.",
        "_adb._tcp."
    )

    private val activeListeners = mutableListOf<NsdManager.DiscoveryListener>()

    fun startDiscovery() {
        if (nsdManager == null) {
            Log.e(tag, "NsdManager not available on this device")
            return
        }

        stopDiscovery()
        _isScanning.value = true

        serviceTypes.forEach { serviceType ->
            try {
                val listener = createDiscoveryListener(serviceType)
                activeListeners.add(listener)
                nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
                Log.d(tag, "Started discovery for: $serviceType")
            } catch (e: Exception) {
                Log.e(tag, "Failed to start discovery for $serviceType", e)
            }
        }
    }

    fun stopDiscovery() {
        if (nsdManager == null) return
        activeListeners.forEach { listener ->
            try {
                nsdManager.stopServiceDiscovery(listener)
            } catch (e: Exception) {
                Log.w(tag, "Error stopping discovery listener", e)
            }
        }
        activeListeners.clear()
        _isScanning.value = false
    }

    private fun createDiscoveryListener(serviceType: String): NsdManager.DiscoveryListener {
        return object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(tag, "Discovery failed for $serviceType: error $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(tag, "Stop discovery failed for $serviceType: error $errorCode")
            }

            override fun onDiscoveryStarted(regType: String?) {
                Log.d(tag, "Discovery started for $regType")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(tag, "Discovery stopped for $serviceType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                if (serviceInfo == null) return
                Log.d(tag, "Service found: ${serviceInfo.serviceName} (${serviceInfo.serviceType})")

                // Resolve service to extract IP and port
                try {
                    nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                            Log.w(tag, "Resolve failed for ${serviceInfo?.serviceName}: error $errorCode")
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                            if (resolvedInfo == null) return
                            val host: InetAddress = resolvedInfo.host ?: return
                            val ip = host.hostAddress ?: return
                            val port = resolvedInfo.port
                            val serviceName = resolvedInfo.serviceName ?: "Android TV"

                            val isMi = detectIfXiaomi(serviceName, resolvedInfo)
                            val isAdb = resolvedInfo.serviceType?.contains("adb") == true
                            val protocol = if (isAdb) ProtocolType.ADB_TCP else ProtocolType.ANDROID_TV_V2

                            val targetPort = when {
                                isAdb -> if (port > 0) port else 5555
                                resolvedInfo.serviceType?.contains("googlecast") == true -> 6467 // Target Android TV Remote pairing port rather than Cast port 8009
                                else -> if (port > 0) port else 6467
                            }

                            val device = TvDevice(
                                id = "tv_$ip",
                                name = cleanDeviceName(serviceName),
                                ipAddress = ip,
                                port = targetPort,
                                protocolType = protocol,
                                isMiStick = isMi,
                                lastSeen = System.currentTimeMillis(),
                                connectionMedium = ConnectionMedium.WIFI_MDNS,
                                modelInfo = if (isMi) "Xiaomi Mi TV Stick" else "Android TV Device"
                            )

                            CoroutineScope(Dispatchers.Main).launch {
                                val current = _discoveredDevices.value.toMutableMap()
                                current[device.id] = device
                                _discoveredDevices.value = current
                            }
                        }
                    })
                } catch (e: Exception) {
                    Log.e(tag, "Exception triggering resolveService", e)
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                Log.d(tag, "Service lost: ${serviceInfo?.serviceName}")
            }
        }
    }

    private fun detectIfXiaomi(name: String, info: NsdServiceInfo): Boolean {
        val lower = name.lowercase()
        if (lower.contains("mi") || lower.contains("xiaomi") || lower.contains("aquaman") ||
            lower.contains("crocus") || lower.contains("redmi") || lower.contains("patchwall")) {
            return true
        }
        val attributes = info.attributes
        attributes.forEach { (key, value) ->
            val valStr = String(value).lowercase()
            if (valStr.contains("xiaomi") || valStr.contains("mi stick") || valStr.contains("mibox")) {
                return true
            }
        }
        return false
    }

    private fun cleanDeviceName(raw: String): String {
        return raw.replace("\\032", " ")
            .replace("-[0-9a-fA-F]{4,}$".toRegex(), "")
            .trim()
            .ifEmpty { "Android TV" }
    }
}
