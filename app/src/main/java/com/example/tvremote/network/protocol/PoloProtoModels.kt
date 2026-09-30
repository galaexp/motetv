package com.example.tvremote.network.protocol

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import java.io.ByteArrayOutputStream

/**
 * Type-safe Protobuf models and builders for Google Polo Pairing Protocol (polo.proto).
 * Port 6467 Pairing session.
 */
object PoloProtoModels {
    // Polo OuterMessage Status Constants (polo.proto)
    const val STATUS_UNKNOWN = 0
    const val STATUS_OK = 200
    const val STATUS_ERROR = 400
    const val STATUS_BAD_CONFIGURATION = 401
    const val STATUS_BAD_SECRET = 402

    // Polo OuterMessage MessageType Constants (polo.proto)
    const val MESSAGE_TYPE_UNKNOWN = 0
    const val MESSAGE_TYPE_PAIRING_REQUEST = 10
    const val MESSAGE_TYPE_PAIRING_REQUEST_ACK = 11
    const val MESSAGE_TYPE_OPTIONS = 20
    const val MESSAGE_TYPE_CONFIGURATION = 30
    const val MESSAGE_TYPE_CONFIGURATION_ACK = 31
    const val MESSAGE_TYPE_SECRET = 40
    const val MESSAGE_TYPE_SECRET_ACK = 41

    // Encoding Types
    const val ENCODING_TYPE_UNKNOWN = 0
    const val ENCODING_TYPE_ALPHANUMERIC = 1
    const val ENCODING_TYPE_NUMERIC = 2
    const val ENCODING_TYPE_HEXADECIMAL = 3
    const val ENCODING_TYPE_QRCODE = 4

    // Role Types
    const val ROLE_TYPE_UNKNOWN = 0
    const val ROLE_TYPE_INPUT = 1
    const val ROLE_TYPE_OUTPUT = 2

    data class ParsedOuterMessage(
        val protocolVersion: Int = 2,
        val status: Int = STATUS_OK,
        val type: Int = 0,
        val payload: ByteArray = byteArrayOf()
    )

    fun parseOuterMessage(data: ByteArray): ParsedOuterMessage {
        val cis = CodedInputStream.newInstance(data)
        var protocolVersion = 2
        var status = STATUS_OK
        var type = 0
        var payload = byteArrayOf()

        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            when (WireFormat.getTagFieldNumber(tag)) {
                1 -> protocolVersion = cis.readUInt32()
                2 -> status = cis.readEnum()
                3 -> type = cis.readEnum()
                4 -> payload = cis.readBytes().toByteArray()
                else -> cis.skipField(tag)
            }
        }
        return ParsedOuterMessage(protocolVersion, status, type, payload)
    }

    fun buildOuterMessage(
        type: Int,
        payload: ByteArray = byteArrayOf(),
        status: Int = STATUS_OK,
        protocolVersion: Int = 2
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)
        cos.writeUInt32(1, protocolVersion)
        cos.writeEnum(2, status)
        cos.writeEnum(3, type)
        if (payload.isNotEmpty()) {
            cos.writeBytes(4, ByteString.copyFrom(payload))
        }
        cos.flush()
        return baos.toByteArray()
    }

    fun buildPairingRequest(
        serviceName: String = "androidtv-remote",
        clientName: String = "Nova Remote"
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)
        cos.writeString(1, serviceName)
        cos.writeString(2, clientName)
        cos.flush()
        return baos.toByteArray()
    }

    data class ParsedPairingRequestAck(val serverName: String = "")

    fun parsePairingRequestAck(data: ByteArray): ParsedPairingRequestAck {
        val cis = CodedInputStream.newInstance(data)
        var serverName = ""
        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            when (WireFormat.getTagFieldNumber(tag)) {
                1 -> serverName = cis.readString()
                else -> cis.skipField(tag)
            }
        }
        return ParsedPairingRequestAck(serverName)
    }

    fun buildEncoding(type: Int, symbolLength: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)
        cos.writeEnum(1, type)
        cos.writeUInt32(2, symbolLength)
        cos.flush()
        return baos.toByteArray()
    }

    fun buildOptions(): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)

        val encHex = buildEncoding(ENCODING_TYPE_HEXADECIMAL, 6)
        val encAlpha = buildEncoding(ENCODING_TYPE_ALPHANUMERIC, 6)
        val encNum6 = buildEncoding(ENCODING_TYPE_NUMERIC, 6)
        val encNum4 = buildEncoding(ENCODING_TYPE_NUMERIC, 4)

        cos.writeBytes(1, ByteString.copyFrom(encHex))
        cos.writeBytes(1, ByteString.copyFrom(encAlpha))
        cos.writeBytes(1, ByteString.copyFrom(encNum6))
        cos.writeBytes(1, ByteString.copyFrom(encNum4))

        cos.writeBytes(2, ByteString.copyFrom(encHex))

        cos.writeEnum(3, ROLE_TYPE_INPUT)

        cos.flush()
        return baos.toByteArray()
    }

    fun buildConfiguration(
        symbolLength: Int = 6,
        encodingType: Int = ENCODING_TYPE_HEXADECIMAL
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)

        val enc = buildEncoding(encodingType, symbolLength)
        cos.writeBytes(1, ByteString.copyFrom(enc))
        cos.writeEnum(2, ROLE_TYPE_INPUT)

        cos.flush()
        return baos.toByteArray()
    }

    fun buildSecret(secretHash: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)
        cos.writeBytes(1, ByteString.copyFrom(secretHash))
        cos.flush()
        return baos.toByteArray()
    }

    data class ParsedSecretAck(val secret: ByteArray = byteArrayOf())

    fun parseSecretAck(data: ByteArray): ParsedSecretAck {
        val cis = CodedInputStream.newInstance(data)
        var secret = byteArrayOf()
        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            when (WireFormat.getTagFieldNumber(tag)) {
                1 -> secret = cis.readBytes().toByteArray()
                else -> cis.skipField(tag)
            }
        }
        return ParsedSecretAck(secret)
    }
}
