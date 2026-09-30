package com.example.tvremote.network.protocol

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import java.io.ByteArrayOutputStream

/**
 * Type-safe Protobuf models and builders for Android TV Remote Protocol v2 (remotemessage.proto).
 * Port 6466 Control session.
 */
object RemoteMessageModels {
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

    fun buildRemoteKeyInject(keyCode: Int, direction: Int = 1): ByteArray {
        val injectBaos = ByteArrayOutputStream()
        val injectCos = CodedOutputStream.newInstance(injectBaos)
        injectCos.writeInt32(1, keyCode)
        injectCos.writeEnum(2, direction)
        injectCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(2, ByteString.copyFrom(injectBaos.toByteArray()))
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

    fun buildRemotePingRequest(): ByteArray {
        val pingBaos = ByteArrayOutputStream()
        val pingCos = CodedOutputStream.newInstance(pingBaos)
        pingCos.writeInt32(1, 1)
        pingCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(3, ByteString.copyFrom(pingBaos.toByteArray()))
        msgCos.flush()

        return msgBaos.toByteArray()
    }
}
