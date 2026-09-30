package com.example.tvremote.network.protocol

import com.example.tvremote.domain.model.RemoteCommand
import com.example.tvremote.domain.model.TvDevice

interface TvProtocolHandler {
    val isConnected: Boolean
    suspend fun connect(device: TvDevice, onPairingRequired: (prompt: String) -> Unit): Result<Boolean>
    suspend fun submitPairingPin(pin: String): Result<Boolean>
    suspend fun sendCommand(command: RemoteCommand): Result<Unit>
    suspend fun sendText(text: String): Result<Unit>
    suspend fun sendTouchMove(dx: Float, dy: Float): Result<Unit>
    suspend fun launchApp(packageName: String): Result<Unit>
    suspend fun openUrl(url: String): Result<Unit>
    suspend fun ping(): Long
    fun disconnect()
}
