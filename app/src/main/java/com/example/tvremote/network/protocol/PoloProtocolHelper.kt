package com.example.tvremote.network.protocol

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Protocol Buffer wire encoder, decoder, and framer for Android TV Remote Protocol v2 & Google Polo Pairing.
 * Fully compliant with the Google TV Polo Pairing Protocol (polo.proto).
 */
object PoloProtocolHelper {
    private const val TAG = "PoloProtocol"

    // Polo OuterMessage MessageType Constants (polo.proto)
    const val MESSAGE_TYPE_UNKNOWN = 0
    const val MESSAGE_TYPE_PAIRING_REQUEST = 10
    const val MESSAGE_TYPE_PAIRING_REQUEST_ACK = 11
    const val MESSAGE_TYPE_OPTIONS = 20
    const val MESSAGE_TYPE_CONFIGURATION = 30
    const val MESSAGE_TYPE_CONFIGURATION_ACK = 31
    const val MESSAGE_TYPE_SECRET = 40
    const val MESSAGE_TYPE_SECRET_ACK = 41

    // Polo OuterMessage Status Constants (polo.proto)
    const val STATUS_UNKNOWN = 0
    const val STATUS_OK = 1
    const val STATUS_ERROR = 2
    const val STATUS_BAD_CONFIGURATION = 3
    const val STATUS_BAD_SECRET = 4

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

    fun writeVarint(out: OutputStream, value: Int) {
        var v = value
        while (v and 0x7F.inv() != 0) {
            out.write((v and 0x7F) or 0x80)
            v = v ushr 7
        }
        out.write(v and 0x7F)
    }

    fun readVarint(input: InputStream): Int {
        var result = 0
        var shift = 0
        while (shift < 32) {
            val b = input.read()
            if (b == -1) {
                if (shift == 0) return -1
                throw java.io.EOFException("Unexpected EOF while reading varint")
            }
            result = result or ((b and 0x7F) shl shift)
            if ((b and 0x80) == 0) {
                return result
            }
            shift += 7
        }
        throw IllegalArgumentException("Malformed varint")
    }

    fun writeFramed(out: OutputStream, payload: ByteArray) {
        writeVarint(out, payload.size)
        out.write(payload)
        out.flush()
    }

    fun readFramed(input: InputStream): ByteArray? {
        val length = readVarint(input)
        if (length <= 0) return null
        val buf = ByteArray(length)
        var total = 0
        while (total < length) {
            val count = input.read(buf, total, length - total)
            if (count == -1) break
            total += count
        }
        return if (total == length) buf else null
    }

    /**
     * Data structure for parsed Polo OuterMessage.
     */
    data class ParsedPoloMessage(
        val protocolVersion: Int = 1,
        val status: Int = STATUS_OK,
        val type: Int = 0,
        val payload: ByteArray = byteArrayOf()
    )

    fun parseOuterMessage(raw: ByteArray): ParsedPoloMessage {
        var protocolVersion = 1
        var status = STATUS_OK
        var type = 0
        var payload = byteArrayOf()

        val stream = ByteArrayInputStream(raw)
        while (stream.available() > 0) {
            val tag = readVarint(stream)
            if (tag == -1) break
            val fieldNumber = tag ushr 3
            val wireType = tag and 0x07

            when (fieldNumber) {
                1 -> protocolVersion = readVarint(stream)
                2 -> status = readVarint(stream)
                3 -> type = readVarint(stream)
                4 -> {
                    val len = readVarint(stream)
                    val pBytes = ByteArray(len)
                    var read = 0
                    while (read < len) {
                        val count = stream.read(pBytes, read, len - read)
                        if (count == -1) break
                        read += count
                    }
                    payload = pBytes
                }
                else -> skipField(stream, wireType)
            }
        }
        return ParsedPoloMessage(protocolVersion, status, type, payload)
    }

    private fun skipField(stream: InputStream, wireType: Int) {
        when (wireType) {
            0 -> readVarint(stream)
            1 -> stream.skip(8)
            2 -> {
                val len = readVarint(stream)
                if (len > 0) stream.skip(len.toLong())
            }
            5 -> stream.skip(4)
        }
    }

    /**
     * Builds Polo PairingRequest OuterMessage (type = 10).
     */
    fun buildPairingRequestMessage(
        clientName: String = "Nova Remote",
        serviceName: String = "androidtvremote",
        protocolVersion: Int = 1
    ): ByteArray {
        val payloadStream = ByteArrayOutputStream()
        // Tag 1 (field 1, string): service_name
        writeTag(payloadStream, 1, 2)
        writeString(payloadStream, serviceName)
        // Tag 2 (field 2, string): client_name
        writeTag(payloadStream, 2, 2)
        writeString(payloadStream, clientName)
        val payload = payloadStream.toByteArray()

        return buildOuterMessage(type = MESSAGE_TYPE_PAIRING_REQUEST, payload = payload, protocolVersion = protocolVersion)
    }

    /**
     * Builds Polo Options OuterMessage (type = 20).
     */
    fun buildOptionsMessage(protocolVersion: Int = 1): ByteArray {
        val payloadStream = ByteArrayOutputStream()

        // 1. input_encodings: HEXADECIMAL length 6
        val encHex = encodeSubEncoding(ENCODING_TYPE_HEXADECIMAL, 6)
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, encHex.size)
        payloadStream.write(encHex)

        // 2. input_encodings: ALPHANUMERIC length 6
        val encAlpha = encodeSubEncoding(ENCODING_TYPE_ALPHANUMERIC, 6)
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, encAlpha.size)
        payloadStream.write(encAlpha)

        // 3. input_encodings: NUMERIC length 6
        val encNum6 = encodeSubEncoding(ENCODING_TYPE_NUMERIC, 6)
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, encNum6.size)
        payloadStream.write(encNum6)

        // 4. input_encodings: NUMERIC length 4
        val encNum4 = encodeSubEncoding(ENCODING_TYPE_NUMERIC, 4)
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, encNum4.size)
        payloadStream.write(encNum4)

        // 5. output_encodings: HEXADECIMAL length 6
        writeTag(payloadStream, 2, 2)
        writeVarint(payloadStream, encHex.size)
        payloadStream.write(encHex)

        // Tag 3 (field 3, enum preferred_role): ROLE_TYPE_INPUT (1)
        writeTag(payloadStream, 3, 0)
        writeVarint(payloadStream, ROLE_TYPE_INPUT)

        val payload = payloadStream.toByteArray()
        return buildOuterMessage(type = MESSAGE_TYPE_OPTIONS, payload = payload, protocolVersion = protocolVersion)
    }

    private fun encodeSubEncoding(type: Int, length: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        writeTag(stream, 1, 0)
        writeVarint(stream, type)
        writeTag(stream, 2, 0)
        writeVarint(stream, length)
        return stream.toByteArray()
    }

    /**
     * Builds Polo Configuration OuterMessage (type = 30).
     */
    fun buildConfigurationMessage(
        symbolLength: Int = 6,
        encodingType: Int = ENCODING_TYPE_HEXADECIMAL,
        protocolVersion: Int = 1
    ): ByteArray {
        val encBytes = encodeSubEncoding(encodingType, symbolLength)
        val payloadStream = ByteArrayOutputStream()
        // Tag 1 (field 1, submessage encoding)
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, encBytes.size)
        payloadStream.write(encBytes)
        // Tag 2 (field 2, enum client_role): ROLE_TYPE_INPUT (1)
        writeTag(payloadStream, 2, 0)
        writeVarint(payloadStream, ROLE_TYPE_INPUT)
        val payload = payloadStream.toByteArray()

        return buildOuterMessage(type = MESSAGE_TYPE_CONFIGURATION, payload = payload, protocolVersion = protocolVersion)
    }

    /**
     * Builds Polo ConfigurationAck OuterMessage (type = 31).
     */
    fun buildConfigurationAckMessage(protocolVersion: Int = 1): ByteArray {
        return buildOuterMessage(type = MESSAGE_TYPE_CONFIGURATION_ACK, payload = byteArrayOf(), protocolVersion = protocolVersion)
    }

    /**
     * Builds Polo Secret OuterMessage (type = 40).
     */
    fun buildSecretMessage(secretHash: ByteArray, protocolVersion: Int = 1): ByteArray {
        val payloadStream = ByteArrayOutputStream()
        // Tag 1 (field 1, bytes secret)
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, secretHash.size)
        payloadStream.write(secretHash)
        val payload = payloadStream.toByteArray()

        return buildOuterMessage(type = MESSAGE_TYPE_SECRET, payload = payload, protocolVersion = protocolVersion)
    }

    /**
     * Builds Polo SecretAck OuterMessage (type = 41).
     */
    fun buildSecretAckMessage(secretHash: ByteArray, protocolVersion: Int = 1): ByteArray {
        val payloadStream = ByteArrayOutputStream()
        writeTag(payloadStream, 1, 2)
        writeVarint(payloadStream, secretHash.size)
        payloadStream.write(secretHash)
        val payload = payloadStream.toByteArray()

        return buildOuterMessage(type = MESSAGE_TYPE_SECRET_ACK, payload = payload, protocolVersion = protocolVersion)
    }

    private fun buildOuterMessage(type: Int, payload: ByteArray, protocolVersion: Int = 1): ByteArray {
        val out = ByteArrayOutputStream()
        // Tag 1: protocol_version (uint32)
        writeTag(out, 1, 0)
        writeVarint(out, protocolVersion)
        // Tag 2: status (enum) = 1 (STATUS_OK in polo.proto)
        writeTag(out, 2, 0)
        writeVarint(out, STATUS_OK)
        // Tag 3: type (enum)
        writeTag(out, 3, 0)
        writeVarint(out, type)
        // Tag 4: payload (bytes) - only write if non-empty
        if (payload.isNotEmpty()) {
            writeTag(out, 4, 2)
            writeVarint(out, payload.size)
            out.write(payload)
        }

        return out.toByteArray()
    }

    /**
     * Builds Android TV Remote Protocol v2 RemoteConfigure message (Port 6466).
     */
    fun buildRemoteConfigure(): ByteArray {
        val deviceInfoStream = ByteArrayOutputStream()
        writeTag(deviceInfoStream, 1, 2)
        writeString(deviceInfoStream, "NovaRemote")
        writeTag(deviceInfoStream, 2, 2)
        writeString(deviceInfoStream, "Android")
        writeTag(deviceInfoStream, 3, 0)
        writeVarint(deviceInfoStream, 1)
        writeTag(deviceInfoStream, 4, 2)
        writeString(deviceInfoStream, "1.0.0")
        val devInfo = deviceInfoStream.toByteArray()

        val configStream = ByteArrayOutputStream()
        writeTag(configStream, 1, 0)
        writeVarint(configStream, 622) // Android TV Remote API version
        writeTag(configStream, 2, 2)
        writeVarint(configStream, devInfo.size)
        configStream.write(devInfo)
        val configBytes = configStream.toByteArray()

        // RemoteMessage tag 1: remote_configure
        val message = ByteArrayOutputStream()
        writeTag(message, 1, 2)
        writeVarint(message, configBytes.size)
        message.write(configBytes)
        return message.toByteArray()
    }

    /**
     * Builds Android TV Remote Protocol v2 RemoteKeyInject message (Port 6466).
     * direction: 1 = SHORT click, 2 = START (press down), 3 = END (release)
     */
    fun buildKeyInject(keyCode: Int, direction: Int = 1): ByteArray {
        val injectStream = ByteArrayOutputStream()
        // Tag 1: key_code (int32)
        writeTag(injectStream, 1, 0)
        writeVarint(injectStream, keyCode)
        // Tag 2: direction (enum)
        writeTag(injectStream, 2, 0)
        writeVarint(injectStream, direction)
        val injectBytes = injectStream.toByteArray()

        // RemoteMessage tag 2: remote_key_inject
        val message = ByteArrayOutputStream()
        writeTag(message, 2, 2)
        writeVarint(message, injectBytes.size)
        message.write(injectBytes)
        return message.toByteArray()
    }

    /**
     * Builds Android TV Remote Protocol v2 AppLink message for launching applications.
     */
    fun buildAppLink(uri: String): ByteArray {
        val linkStream = ByteArrayOutputStream()
        writeTag(linkStream, 1, 2)
        writeString(linkStream, uri)
        val linkBytes = linkStream.toByteArray()

        // RemoteMessage tag 6: app_link
        val message = ByteArrayOutputStream()
        writeTag(message, 6, 2)
        writeVarint(message, linkBytes.size)
        message.write(linkBytes)
        return message.toByteArray()
    }

    /**
     * Builds Android TV Remote Protocol v2 Ping message.
     */
    fun buildPing(): ByteArray {
        val pingStream = ByteArrayOutputStream()
        writeTag(pingStream, 1, 0)
        writeVarint(pingStream, 1)
        val pingBytes = pingStream.toByteArray()

        // RemoteMessage tag 3: ping_request
        val message = ByteArrayOutputStream()
        writeTag(message, 3, 2)
        writeVarint(message, pingBytes.size)
        message.write(pingBytes)
        return message.toByteArray()
    }

    private fun writeTag(out: OutputStream, fieldNumber: Int, wireType: Int) {
        val tag = (fieldNumber shl 3) or wireType
        writeVarint(out, tag)
    }

    private fun writeString(out: OutputStream, str: String) {
        val bytes = str.toByteArray(Charsets.UTF_8)
        writeVarint(out, bytes.size)
        out.write(bytes)
    }
}
