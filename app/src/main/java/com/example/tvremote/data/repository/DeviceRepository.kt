package com.example.tvremote.data.repository

import com.example.tvremote.data.local.dao.DeviceDao
import com.example.tvremote.data.local.entity.DeviceEntity
import com.example.tvremote.domain.model.TvDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepository(private val deviceDao: DeviceDao) {
    val savedDevices: Flow<List<TvDevice>> = deviceDao.getAllDevices().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveDevice(device: TvDevice) {
        deviceDao.insertDevice(DeviceEntity.fromDomain(device))
    }

    suspend fun deleteDevice(deviceId: String) {
        deviceDao.deleteDeviceById(deviceId)
    }

    suspend fun markPaired(deviceId: String, paired: Boolean) {
        val existing = deviceDao.getDeviceById(deviceId)
        if (existing != null) {
            deviceDao.updateDevice(existing.copy(isPaired = paired, lastSeen = System.currentTimeMillis()))
        }
    }
}
