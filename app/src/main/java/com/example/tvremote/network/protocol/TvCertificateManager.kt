package com.example.tvremote.network.protocol

import android.content.Context
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Principal
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509TrustManager

/**
 * Manages persistent RSA-2048 keypair, self-signed X.509 client certificate,
 * and standard SSLContext for Android TV Remote v2 / Polo pairing (Port 6467 & 6466).
 *
 * Uses a custom X509ExtendedKeyManager to ensure Conscrypt/BoringSSL ALWAYS presents
 * the client certificate during mutual TLS (mTLS) handshake, fixing the issue where
 * default JSSE KeyManager returns null alias when TV server sends non-matching CA issuers.
 */
class TvCertificateManager(private val context: Context) {
    private val tag = "TvCertManager"
    private val keyAlias = "novaremote"
    private val certFileName = "novaremote_client_cert.der"
    private val keyFileName = "novaremote_client_key.pk8"

    private var sslContext: SSLContext? = null
    private var clientCertificate: X509Certificate? = null
    private var clientPrivateKey: PrivateKey? = null

    init {
        ensureCredentialsExist()
    }

    private fun ensureCredentialsExist() {
        try {
            val certFile = File(context.filesDir, certFileName)
            val keyFile = File(context.filesDir, keyFileName)

            if (certFile.exists() && keyFile.exists()) {
                val certBytes = certFile.readBytes()
                val keyBytes = keyFile.readBytes()

                val certFactory = CertificateFactory.getInstance("X.509")
                clientCertificate = certFactory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate

                val keySpec = PKCS8EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance("RSA")
                clientPrivateKey = keyFactory.generatePrivate(keySpec)

                Log.i(tag, "Loaded persistent client certificate: ${clientCertificate?.subjectDN}")
            } else {
                generateAndPersistCredentials(certFile, keyFile)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to load stored certificates, generating fresh keypair", e)
            try {
                val certFile = File(context.filesDir, certFileName)
                val keyFile = File(context.filesDir, keyFileName)
                generateAndPersistCredentials(certFile, keyFile)
            } catch (ex: Exception) {
                Log.e(tag, "Fatal error generating fallback client credentials", ex)
            }
        }
    }

    private fun generateAndPersistCredentials(certFile: File, keyFile: File) {
        Log.i(tag, "Generating new RSA-2048 keypair and self-signed X.509 certificate for Android TV...")
        val kpg = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048, SecureRandom())
        }
        val keyPair = kpg.generateKeyPair()
        val cert = generateSelfSignedCertificate(keyPair, commonName = "atvremote")

        // Persist DER certificate and PKCS#8 private key
        FileOutputStream(certFile).use { it.write(cert.encoded) }
        FileOutputStream(keyFile).use { it.write(keyPair.private.encoded) }

        clientCertificate = cert
        clientPrivateKey = keyPair.private
        Log.i(tag, "Successfully generated and persisted credentials: ${cert.subjectDN}")
    }

    fun getClientCertificate(): X509Certificate? = clientCertificate

    fun getClientPrivateKey(): PrivateKey? = clientPrivateKey

    fun getSslSocketFactory(): SSLSocketFactory {
        return getSslContext().socketFactory
    }

    @Synchronized
    fun getSslContext(): SSLContext {
        sslContext?.let { return it }

        val cert = clientCertificate ?: throw IllegalStateException("Client certificate not initialized")
        val key = clientPrivateKey ?: throw IllegalStateException("Client private key not initialized")

        // Custom KeyManager that unconditionally selects and serves our client certificate
        val customKeyManager = object : X509ExtendedKeyManager() {
            override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String> {
                return arrayOf(keyAlias)
            }

            override fun chooseClientAlias(
                keyType: Array<out String>?,
                issuers: Array<out Principal>?,
                socket: Socket?
            ): String {
                Log.d(tag, "chooseClientAlias called by SSL engine, serving $keyAlias")
                return keyAlias
            }

            override fun chooseEngineClientAlias(
                keyType: Array<out String>?,
                issuers: Array<out Principal>?,
                engine: SSLEngine?
            ): String {
                Log.d(tag, "chooseEngineClientAlias called by SSL engine, serving $keyAlias")
                return keyAlias
            }

            override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null
            override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String? = null
            override fun chooseEngineServerAlias(keyType: String?, issuers: Array<out Principal>?, engine: SSLEngine?): String? = null

            override fun getCertificateChain(alias: String?): Array<X509Certificate> {
                return arrayOf(cert)
            }

            override fun getPrivateKey(alias: String?): PrivateKey {
                return key
            }
        }

        // TrustManager that accepts the Android TV's self-signed server certificate
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val ctx = SSLContext.getInstance("TLS").apply {
            init(arrayOf(customKeyManager), trustAll, SecureRandom())
        }
        sslContext = ctx
        return ctx
    }

    fun resetCredentials() {
        Log.i(tag, "Resetting stored credentials and certificate...")
        try {
            val certFile = File(context.filesDir, certFileName)
            val keyFile = File(context.filesDir, keyFileName)
            if (certFile.exists()) certFile.delete()
            if (keyFile.exists()) keyFile.delete()
            sslContext = null
            generateAndPersistCredentials(certFile, keyFile)
        } catch (e: Exception) {
            Log.e(tag, "Error resetting credentials", e)
        }
    }

    /**
     * Computes the pairing secret hash according to the Google Polo pairing protocol:
     * SHA-256(client_modulus + client_exponent + server_modulus + server_exponent + pin_bytes)
     */
    fun computePairingSecret(clientCert: X509Certificate, serverCert: X509Certificate, pin: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")

        val clientRsa = clientCert.publicKey as? java.security.interfaces.RSAPublicKey
        val serverRsa = serverCert.publicKey as? java.security.interfaces.RSAPublicKey

        if (clientRsa != null && serverRsa != null) {
            val clientMod = stripLeadingZero(clientRsa.modulus.toByteArray())
            val clientExp = stripLeadingZero(clientRsa.publicExponent.toByteArray())
            val serverMod = stripLeadingZero(serverRsa.modulus.toByteArray())
            val serverExp = stripLeadingZero(serverRsa.publicExponent.toByteArray())

            md.update(clientMod)
            md.update(clientExp)
            md.update(serverMod)
            md.update(serverExp)
        } else {
            md.update(clientCert.publicKey.encoded)
            md.update(serverCert.publicKey.encoded)
        }

        val cleanPin = pin.replace("-", "").replace(" ", "").trim()
        val pinBytes = try {
            if (cleanPin.length % 2 == 0 && cleanPin.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
                cleanPin.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            } else {
                cleanPin.uppercase().toByteArray(Charsets.UTF_8)
            }
        } catch (_: Exception) {
            cleanPin.uppercase().toByteArray(Charsets.UTF_8)
        }

        md.update(pinBytes)
        return md.digest()
    }

    private fun stripLeadingZero(bytes: ByteArray): ByteArray {
        return if (bytes.size > 1 && bytes[0] == 0.toByte()) {
            bytes.copyOfRange(1, bytes.size)
        } else {
            bytes
        }
    }

    companion object {
        /**
         * Pure Kotlin/Java self-signed X.509 v3 Certificate generator.
         */
        fun generateSelfSignedCertificate(keyPair: KeyPair, commonName: String): X509Certificate {
            val pubKeyBytes = keyPair.public.encoded // SubjectPublicKeyInfo DER
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val notBefore = dateFormat.format(Date(now - 86400000L))
            val notAfter = dateFormat.format(Date(now + 1000L * 60 * 60 * 24 * 365 * 25)) // 25 years

            val serial = BigInteger(64, SecureRandom())

            // 1. Version: [0] { INTEGER 2 } -> version v3
            val version = encodeExplicit(0, encodeInteger(BigInteger.valueOf(2)))

            // 2. Serial Number
            val serialNumber = encodeInteger(serial)

            // 3. Signature Algorithm Identifier: SEQUENCE { OID sha256WithRSAEncryption, NULL }
            val sha256WithRSA_OID = byteArrayOf(0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01, 0x0B)
            val sigAlg = encodeSequence(encodeOid(sha256WithRSA_OID) + encodeNull())

            // 4. Issuer & Subject: SEQUENCE { SET { SEQUENCE { OID commonName (2.5.4.3), UTF8String commonName } } }
            val cn_OID = byteArrayOf(0x55, 0x04, 0x03)
            val rdn = encodeSequence(encodeOid(cn_OID) + encodeUtf8String(commonName))
            val name = encodeSequence(encodeSet(rdn))

            // 5. Validity: SEQUENCE { UTCTime notBefore, UTCTime notAfter }
            val validity = encodeSequence(encodeUtcTime(notBefore) + encodeUtcTime(notAfter))

            // Combine into TBSCertificate
            val tbsCertificate = encodeSequence(
                version + serialNumber + sigAlg + name + validity + name + pubKeyBytes
            )

            // Sign TBSCertificate with private key
            val signer = Signature.getInstance("SHA256withRSA")
            signer.initSign(keyPair.private)
            signer.update(tbsCertificate)
            val signatureBytes = signer.sign()

            // Full Certificate: SEQUENCE { tbsCertificate, sigAlg, BIT STRING signature }
            val certDer = encodeSequence(
                tbsCertificate + sigAlg + encodeBitString(signatureBytes)
            )

            val certFactory = CertificateFactory.getInstance("X.509")
            return certFactory.generateCertificate(ByteArrayInputStream(certDer)) as X509Certificate
        }

        private fun encodeLength(length: Int): ByteArray {
            return when {
                length < 128 -> byteArrayOf(length.toByte())
                length < 256 -> byteArrayOf(0x81.toByte(), length.toByte())
                length < 65536 -> byteArrayOf(0x82.toByte(), (length ushr 8).toByte(), (length and 0xFF).toByte())
                else -> byteArrayOf(
                    0x83.toByte(),
                    (length ushr 16).toByte(),
                    ((length ushr 8) and 0xFF).toByte(),
                    (length and 0xFF).toByte()
                )
            }
        }

        private fun encodeSequence(bytes: ByteArray): ByteArray = byteArrayOf(0x30) + encodeLength(bytes.size) + bytes
        private fun encodeSet(bytes: ByteArray): ByteArray = byteArrayOf(0x31) + encodeLength(bytes.size) + bytes
        private fun encodeExplicit(tag: Int, bytes: ByteArray): ByteArray = byteArrayOf((0xA0 or tag).toByte()) + encodeLength(bytes.size) + bytes
        private fun encodeInteger(bigInt: BigInteger): ByteArray {
            val b = bigInt.toByteArray()
            return byteArrayOf(0x02) + encodeLength(b.size) + b
        }
        private fun encodeBitString(bytes: ByteArray): ByteArray {
            return byteArrayOf(0x03) + encodeLength(bytes.size + 1) + byteArrayOf(0x00) + bytes
        }
        private fun encodeOid(rawOidBytes: ByteArray): ByteArray = byteArrayOf(0x06) + encodeLength(rawOidBytes.size) + rawOidBytes
        private fun encodeNull(): ByteArray = byteArrayOf(0x05, 0x00)
        private fun encodeUtf8String(str: String): ByteArray {
            val b = str.toByteArray(Charsets.UTF_8)
            return byteArrayOf(0x0C) + encodeLength(b.size) + b
        }
        private fun encodeUtcTime(utcStr: String): ByteArray {
            val b = utcStr.toByteArray(Charsets.US_ASCII)
            return byteArrayOf(0x17) + encodeLength(b.size) + b
        }
    }
}
