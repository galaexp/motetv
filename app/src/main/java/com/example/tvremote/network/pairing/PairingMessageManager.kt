package com.example.tvremote.network.pairing

import com.google.protobuf.ByteString
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import java.io.ByteArrayOutputStream

/**
 * Type-safe encoder and parser for Android TV Remote v2 PairingMessage (pairingmessage.proto).
 */
object PairingMessageManager {
    const val STATUS_OK = 200
    const val STATUS_ERROR = 400
    const val STATUS_BAD_CONFIGURATION = 401
    const val STATUS_BAD_SECRET = 402

    const val ROLE_INPUT = 1
    const val ROLE_OUTPUT = 2

    const val ENCODING_HEXADECIMAL = 3
    const val ENCODING_ALPHANUMERIC = 1
    const val ENCODING_NUMERIC = 2

    data class ParsedPairingMessage(
        val status: Int = STATUS_OK,
        val protocolVersion: Int = 2,
        val hasPairingRequest: Boolean = false,
        val hasPairingOption: Boolean = false,
        val hasPairingConfiguration: Boolean = false,
        val hasPairingSecret: Boolean = false,
        val hasPairingSecretAck: Boolean = false,
        val secretBytes: ByteArray = byteArrayOf()
    )

    fun parsePairingMessage(data: ByteArray): ParsedPairingMessage {
        val cis = CodedInputStream.newInstance(data)
        var status = STATUS_OK
        var protocolVersion = 2
        var hasPairingRequest = false
        var hasPairingOption = false
        var hasPairingConfiguration = false
        var hasPairingSecret = false
        var hasPairingSecretAck = false
        var secretBytes = byteArrayOf()

        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            val fieldNumber = WireFormat.getTagFieldNumber(tag)
            when (fieldNumber) {
                1 -> {
                    cis.readBytes()
                    hasPairingRequest = true
                }
                2 -> {
                    cis.readBytes()
                    hasPairingOption = true
                }
                3 -> {
                    cis.readBytes()
                    hasPairingConfiguration = true
                }
                4 -> {
                    val bytes = cis.readBytes().toByteArray()
                    hasPairingSecret = true
                    secretBytes = parseSecretPayload(bytes)
                }
                5 -> {
                    cis.readBytes()
                    hasPairingSecretAck = true
                }
                200 -> status = cis.readEnum()
                201 -> protocolVersion = cis.readEnum()
                else -> cis.skipField(tag)
            }
        }
        return ParsedPairingMessage(
            status = status,
            protocolVersion = protocolVersion,
            hasPairingRequest = hasPairingRequest,
            hasPairingOption = hasPairingOption,
            hasPairingConfiguration = hasPairingConfiguration,
            hasPairingSecret = hasPairingSecret,
            hasPairingSecretAck = hasPairingSecretAck,
            secretBytes = secretBytes
        )
    }

    private fun parseSecretPayload(payload: ByteArray): ByteArray {
        val cis = CodedInputStream.newInstance(payload)
        var secret = byteArrayOf()
        while (!cis.isAtEnd) {
            val tag = cis.readTag()
            if (tag == 0) break
            when (WireFormat.getTagFieldNumber(tag)) {
                1 -> secret = cis.readBytes().toByteArray()
                else -> cis.skipField(tag)
            }
        }
        return secret
    }

    /**
     * Builds a PairingMessage containing PairingRequest (field 1).
     */
    fun buildPairingRequest(
        serviceName: String = "androidtv-remote",
        clientName: String = "Nova Remote",
        protocolVersion: Int = 2
    ): ByteArray {
        val reqBaos = ByteArrayOutputStream()
        val reqCos = CodedOutputStream.newInstance(reqBaos)
        reqCos.writeString(1, serviceName)
        reqCos.writeString(2, clientName)
        reqCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(1, ByteString.copyFrom(reqBaos.toByteArray()))
        msgCos.writeEnum(200, STATUS_OK)
        msgCos.writeEnum(201, protocolVersion)
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    /**
     * Builds a PairingMessage containing PairingOption (field 2).
     */
    fun buildPairingOption(protocolVersion: Int = 2): ByteArray {
        val hexEncoding = buildEncoding(ENCODING_HEXADECIMAL, 6)
        val alphaEncoding = buildEncoding(ENCODING_ALPHANUMERIC, 6)
        val num6Encoding = buildEncoding(ENCODING_NUMERIC, 6)
        val num4Encoding = buildEncoding(ENCODING_NUMERIC, 4)

        val optBaos = ByteArrayOutputStream()
        val optCos = CodedOutputStream.newInstance(optBaos)
        optCos.writeBytes(1, ByteString.copyFrom(hexEncoding))
        optCos.writeBytes(1, ByteString.copyFrom(alphaEncoding))
        optCos.writeBytes(1, ByteString.copyFrom(num6Encoding))
        optCos.writeBytes(1, ByteString.copyFrom(num4Encoding))

        optCos.writeBytes(2, ByteString.copyFrom(hexEncoding))
        optCos.writeEnum(3, ROLE_INPUT)
        optCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(2, ByteString.copyFrom(optBaos.toByteArray()))
        msgCos.writeEnum(200, STATUS_OK)
        msgCos.writeEnum(201, protocolVersion)
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    /**
     * Builds a PairingMessage containing PairingConfiguration (field 3).
     */
    fun buildPairingConfiguration(
        symbolLength: Int = 6,
        encodingType: Int = ENCODING_HEXADECIMAL,
        protocolVersion: Int = 2
    ): ByteArray {
        val enc = buildEncoding(encodingType, symbolLength)

        val configBaos = ByteArrayOutputStream()
        val configCos = CodedOutputStream.newInstance(configBaos)
        configCos.writeBytes(1, ByteString.copyFrom(enc))
        configCos.writeEnum(2, ROLE_INPUT)
        configCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(3, ByteString.copyFrom(configBaos.toByteArray()))
        msgCos.writeEnum(200, STATUS_OK)
        msgCos.writeEnum(201, protocolVersion)
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    /**
     * Builds a PairingMessage containing PairingSecret (field 4).
     */
    fun buildPairingSecret(secretHash: ByteArray, protocolVersion: Int = 2): ByteArray {
        val secBaos = ByteArrayOutputStream()
        val secCos = CodedOutputStream.newInstance(secBaos)
        secCos.writeBytes(1, ByteString.copyFrom(secretHash))
        secCos.flush()

        val msgBaos = ByteArrayOutputStream()
        val msgCos = CodedOutputStream.newInstance(msgBaos)
        msgCos.writeBytes(4, ByteString.copyFrom(secBaos.toByteArray()))
        msgCos.writeEnum(200, STATUS_OK)
        msgCos.writeEnum(201, protocolVersion)
        msgCos.flush()

        return msgBaos.toByteArray()
    }

    private fun buildEncoding(type: Int, symbolLength: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        val cos = CodedOutputStream.newInstance(baos)
        cos.writeEnum(1, type)
        cos.writeUInt32(2, symbolLength)
        cos.flush()
        return baos.toByteArray()
    }
}
