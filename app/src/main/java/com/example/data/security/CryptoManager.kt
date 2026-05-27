package com.example.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * CryptoManager handles hardware-backed Keystore cryptographic operations
 * on Android. It utilizes AES/GCM/NoPadding for high security.
 */
object CryptoManager {
    private const val ALIAS = "rtchat_secure_secret_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false) // Set to false so the key is always accessible
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypt a byte array using AES GCM block mode.
     * Returns a Pair of (Initialization Vector, Ciphertext)
     */
    fun encrypt(bytes: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val ciphertext = cipher.doFinal(bytes)
        return Pair(cipher.iv, ciphertext)
    }

    /**
     * Decrypt a byte array using AES GCM block mode and specified validation vector (IV).
     */
    fun decrypt(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), spec)
        return cipher.doFinal(ciphertext)
    }

    /**
     * Encrypts plain text string and returns a colon-separated String of "Base64(IV):Base64(Ciphertext)"
     */
    fun encryptText(text: String): String {
        if (text.isEmpty()) return ""
        return try {
            val (iv, ciphertext) = encrypt(text.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ciphertextBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
            "$ivBase64:$ciphertextBase64"
        } catch (e: Throwable) {
            e.printStackTrace()
            // Graceful fallback to legacy base64 if keystore is un-integrated or buggy on custom devices
            "FALLBACK:" + Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts a colon-separated String of "Base64(IV):Base64(Ciphertext)" back to plain text
     */
    fun decryptText(encryptedTextWithIv: String): String? {
        if (encryptedTextWithIv.isEmpty()) return ""
        if (encryptedTextWithIv.startsWith("FALLBACK:")) {
            return try {
                val encodedBytes = encryptedTextWithIv.removePrefix("FALLBACK:")
                String(Base64.decode(encodedBytes, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (e: Throwable) {
                null
            }
        }
        return try {
            val parts = encryptedTextWithIv.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
            val decryptedBytes = decrypt(iv, ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}
