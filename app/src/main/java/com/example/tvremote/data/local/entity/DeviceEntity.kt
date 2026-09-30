package com.example.tvremote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tvremote.domain.model.ConnectionMedium
import com.example.tvremote.domain.model.ProtocolType
import com.example.tvremote.domain.model.TvDevice

@Entity(tableName = "saved_devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val macAddress: String?,
    val protocolType: String,
    val isMiStick: Boolean,
    val isPaired: Boolean,
    val lastSeen: Long,
    val connectionMedium: String,
    val modelInfo: String?
) {
    fun toDomain(): TvDevice = TvDevice(
        id = id,
        name = name,
        ipAddress = ipAddress,
        port = port,
        macAddress = macAddress,
        protocolType = try { ProtocolType.valueOf(protocolType) } catch (e: Exception) { ProtocolType.ANDROID_TV_V2 },
        isMiStick = isMiStick,
        isPaired = isPaired,
        lastSeen = lastSeen,
        connectionMedium = try { ConnectionMedium.valueOf(connectionMedium) } catch (e: Exception) { ConnectionMedium.WIFI_MDNS },
        modelInfo = modelInfo
    )

    companion object {
        fun fromDomain(device: TvDevice): DeviceEntity = DeviceEntity(
            id = device.id,
            name = device.name,
            ipAddress = device.ipAddress,
            port = device.port,
            macAddress = device.macAddress,
            protocolType = device.protocolType.name,
            isMiStick = device.isMiStick,
            isPaired = device.isPaired,
            lastSeen = device.lastSeen,
            connectionMedium = device.connectionMedium.name,
            modelInfo = device.modelInfo
        )
    }
}
