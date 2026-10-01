package com.example.tvremote.network.protocol

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

/**
 * Single, unified length-delimited Protobuf wire framer for Android TV Remote Protocol v2.
 * Uses Protobuf varint message length header for BOTH Port 6467 (Pairing) and Port 6466 (Remote Control).
 */
object PoloProtocolHelper {
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
                throw EOFException("Unexpected EOF while reading varint length")
            }
            result = result or ((b and 0x7F) shl shift)
            if ((b and 0x80) == 0) {
                return result
            }
            shift += 7
        }
        throw IllegalArgumentException("Malformed varint length header")
    }

    /**
     * Writes varint-framed protobuf payload.
     * Format: [Varint Message Length][Protobuf Payload]
     */
    fun writeVarintFramed(out: OutputStream, payload: ByteArray) {
        writeVarint(out, payload.size)
        out.write(payload)
        out.flush()
    }

    /**
     * Reads varint-framed protobuf payload from stream.
     */
    fun readVarintFramed(input: InputStream): ByteArray? {
        val length = readVarint(input)
        if (length <= 0 || length > 1048576) return null
        val buf = ByteArray(length)
        var total = 0
        while (total < length) {
            val count = input.read(buf, total, length - total)
            if (count == -1) break
            total += count
        }
        return if (total == length) buf else null
    }
}
