package com.example.tvremote.network.remote

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import java.io.ByteArrayOutputStream

/**
 * Type-safe encoder and parser for Android TV Remote v2 RemoteMessage (remotemessage.proto).
 */
object RemoteMessageManager {
    const val KEY_DIRECTION_SHORT = 1
    const val KEY_DIRECTION_START = 2
    const val KEY_DIRECTION_END = 3

    data class ParsedRemoteMessage(
        val hasRemoteConfigure: Boolean = false,
        val hasRemoteSetActive: Boolean = false,
        val hasRemotePingRequest: Boolean = false,
        val pingVal: Int = 1,
        val hasRemoteKeyInject: Boolean = false,
        val hasRemoteAppLink: Boolean = false
    )

    fun parseRemoteMessage(data: ByteArray): ParsedRemoteMessage {
        val cis = CodedInputStream.newInstance(data)
        var hasRemoteConfigure = false
        var hasRemoteSetActive = false
        var hasRemotePingRequest = false
        var pingVal = 1
        var hasRemoteKeyInject = false
        var hasRemoteAppLink = false

        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            val fieldNumber = WireFormat.getTagFieldNumber(tag)
            when (fieldNumber) {
                1 -> {
                    cis.readBytes()
                    hasRemoteConfigure = true
                }
                2 -> {
                    cis.readBytes()
                    hasRemoteSetActive = true
                }
                3 -> {
                    val bytes = cis.readBytes().toByteArray()
                    hasRemotePingRequest = true
                    pingVal = parsePingRequestVal(bytes)
                }
                4 -> cis.readBytes() // RemotePingResponse
                5 -> {
                    cis.readBytes()
                    hasRemoteKeyInject = true
                }
                6 -> {
                    cis.readBytes()
                    hasRemoteAppLink = true
                }
                else -> cis.skipField(tag)
            }
        }
        return ParsedRemoteMessage(
            hasRemoteConfigure = hasRemoteConfigure,
            hasRemoteSetActive = hasRemoteSetActive,
            hasRemotePingRequest = hasRemotePingRequest,
            pingVal = pingVal,
            hasRemoteKeyInject = hasRemoteKeyInject,
            hasRemoteAppLink = hasRemoteAppLink
        )
    }

    private fun parsePingRequestVal(payload: ByteArray): Int {
        val cis = CodedInputStream.newInstance(payload)
        var val1 = 1
        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            when (WireFormat.getTagFieldNumber(tag)) {
                1 -> val1 = cis.readInt32()
                else -> cis.skipField(tag)
            }
        }
        return val1
    }

    fun buildRemoteConfigure(): ByteArray {
        val devInfoBaos = ByteArrayOutputStream()
        val devCos = CodedOutputStream.newInstance(devInfoBaos)
        devCos.writeString(1, "NovaRemote")
        devCos.writeString(2, "Android")
        devCos.writeInt32(3, 1)
        devCos.writeString(4, "1.0.0")
        devCos.flush()

        val configBaos = ByteArrayOutputStream()
        val configCos = CodedOutputStream.newInstance(configBaos)
        configCos.writeInt32(1, 622)
        configCos.writeBytes(2, ByteString.copyFrom(devInfoBaos.toByteArray()))
        configCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(1, ByteString.copyFrom(configBaos.toByteArray()))
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    fun buildRemoteSetActive(active: Int = 1): ByteArray {
        val activeBaos = ByteArrayOutputStream()
        val activeCos = CodedOutputStream.newInstance(activeBaos)
        activeCos.writeInt32(1, active)
        activeCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(2, ByteString.copyFrom(activeBaos.toByteArray()))
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    fun buildRemotePingResponse(val1: Int = 1): ByteArray {
        val pingBaos = ByteArrayOutputStream()
        val pingCos = CodedOutputStream.newInstance(pingBaos)
        pingCos.writeInt32(1, val1)
        pingCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(4, ByteString.copyFrom(pingBaos.toByteArray()))
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    fun buildRemoteKeyInject(keyCode: Int, direction: Int = KEY_DIRECTION_SHORT): ByteArray {
        val injectBaos = ByteArrayOutputStream()
        val injectCos = CodedOutputStream.newInstance(injectBaos)
        injectCos.writeInt32(1, keyCode)
        injectCos.writeEnum(2, direction)
        injectCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(5, ByteString.copyFrom(injectBaos.toByteArray()))
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    fun buildRemoteAppLink(url: String): ByteArray {
        val linkBaos = ByteArrayOutputStream()
        val linkCos = CodedOutputStream.newInstance(linkBaos)
        linkCos.writeString(1, url)
        linkCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(6, ByteString.copyFrom(linkBaos.toByteArray()))
        msgCos.flush()

        return msgBaos.toByteArray()
    }
}
