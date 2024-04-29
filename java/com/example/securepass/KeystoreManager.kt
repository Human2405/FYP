package com.example.securepass

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class KeystoreManager(private val context: Context) {

    init {
        generateEncryptionKey()
    }

    private fun generateEncryptionKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            context.toString(),
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(false) // Adjust as needed
            .build()
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    fun encryptData(data: String): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        return cipher.doFinal(data.toByteArray())
    }

    /*fun decryptData(encryptedData: ByteArray): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData)
    }*/

    /*fun decryptData(encryptedData: ByteArray): String {

        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            // Extract IV from the encrypted data (first 16 bytes)
            val iv = encryptedData.copyOfRange(0, 16)
            val cipherTextBase64 = encryptedData.copyOfRange(16, encryptedData.size)

            // Decode Base64-encoded cipherText
            val cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val spec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedData = cipher.doFinal(cipherText)

            Log.d("DecryptionTest", "Encrypted Data Length: ${encryptedData.size}")
            Log.d("DecryptionTest", "Decrypted Data Length: ${decryptedData.size}")

            Log.d("DecryptionTest", "Decrypted String: $decryptedData")


            return String(decryptedData)

        } catch (e: Exception) {
            Log.d("DecryptionTest", "Encrypted Data Length: ${encryptedData.size}")
            //Log.d("DecryptionTest", "Decrypted Data Length: ${decryptedData.size}")
            Log.e("DecryptionTest", "Error during decryption: $e")
            throw e  // Rethrow the exception to handle it at the higher level
        }

    }*/

   /* fun decryptData(encryptedData: ByteArray): String {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            // Extract IV from the encrypted data (first 16 bytes)
            val iv = encryptedData.copyOfRange(0, 16)
            val cipherTextBase64 = encryptedData.copyOfRange(16, encryptedData.size)

            // Decode Base64-encoded cipherText
            val cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val spec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedData = cipher.doFinal(cipherText)
            return String(decryptedData)
        } catch (e: Exception) {
            Log.e("DecryptionError", "Error during decryption: ${e.message}")
            return ""
        }
    }*/

    /*fun decryptData(encryptedDataBase64: String?): String {

        Log.e("DecryptionError", "value is: $encryptedDataBase64")
        if (encryptedDataBase64.isNullOrBlank()) {
            Log.e("DecryptionError", "Encrypted data is null or blank")
            return ""
        }

        try {
            val cipherText = Base64.decode(encryptedDataBase64, Base64.DEFAULT)

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            // Extract IV from the encrypted data (first 16 bytes)
            val iv = cipherText.copyOfRange(0, 16)

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            val spec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            // Decrypt the entire cipherText
            val decryptedData = cipher.doFinal(cipherText)
            //return String(decryptedData, Charsets.UTF_8) // Convert the decrypted data to String
            return String(decryptedData, Charset.defaultCharset())
        } catch (e: Exception) {
            Log.e("DecryptionError", "Error during decryption: ${e.message}")
            return ""
        }
    }*/

    /*fun decryptData(encryptedDataBase64: String?): String {
        Log.e("DecryptionError", "value is: $encryptedDataBase64")

        if (encryptedDataBase64.isNullOrBlank()) {
            Log.e("DecryptionError", "Encrypted data is null or blank")
            return ""
        }

        try {
            val cipherText = Base64.decode(encryptedDataBase64, Base64.DEFAULT)

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Fetch the original secret key entry from the keystore
            val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
            val originalSecretKey = secretKeyEntry.secretKey

            // Log information about the original secret key
            Log.d("DecryptionTest", "Original Key Algorithm: ${originalSecretKey.algorithm}")
            Log.d("DecryptionTest", "Original Key Format: ${originalSecretKey.format}")

            // Extract the encoded key bytes
            val encodedKeyBytes = originalSecretKey.encoded

            // Create a new SecretKey using the encoded key bytes
            val newSecretKey = SecretKeySpec(encodedKeyBytes, originalSecretKey.algorithm)

            // Log information about the new secret key
            Log.d("DecryptionTest", "New Key Algorithm: ${newSecretKey.algorithm}")
            Log.d("DecryptionTest", "New Key Format: ${newSecretKey.format}")

            // Extract IV from the encrypted data (first 16 bytes)
            val iv = cipherText.copyOfRange(0, 16)

            Log.d("DecryptionTest", "IV: ${Base64.encodeToString(iv, Base64.DEFAULT)}")
            Log.d("DecryptionTest", "IV (raw): ${Arrays.toString(iv)}")

            // Initialize the cipher with the new secret key and IV
            val spec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, newSecretKey, spec)

            // Decrypt the entire cipherText
            val decryptedData = cipher.doFinal(cipherText)

            return String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("DecryptionError", "Error during decryption: ${e.message}")
            return ""
        }
    }*/

    /*fun decryptData(encryptedDataBase64: String?): String {
        Log.e("DecryptionError", "value is: $encryptedDataBase64")

        if (encryptedDataBase64.isNullOrBlank()) {
            Log.e("DecryptionError", "Encrypted data is null or blank")
            return ""
        }

        try {
            val cipherText = Base64.decode(encryptedDataBase64, Base64.DEFAULT)

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Fetch the original secret key entry from the keystore
            val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
            val originalSecretKey = secretKeyEntry.secretKey


            // Log information about the original secret key
            Log.d("DecryptionTest", "Original Key Algorithm: ${originalSecretKey.algorithm}")
            Log.d("DecryptionTest", "Original Key Format: ${originalSecretKey.format}")

            // Extract the encoded key bytes
            val encodedKeyBytes = originalSecretKey.encoded

            // Create a new SecretKey using the encoded key bytes and the original key format
            //val newSecretKey = SecretKeySpec(encodedKeyBytes, 0, encodedKeyBytes.size, originalSecretKey.algorithm)

            // Create a new SecretKey using the original key's algorithm
            val newSecretKey = SecretKeySpec(encodedKeyBytes, 0, encodedKeyBytes.size, "AES")

            // Log information about the new secret key
            Log.d("DecryptionTest", "New Key Algorithm: ${newSecretKey.algorithm}")
            Log.d("DecryptionTest", "New Key Format: ${newSecretKey.format}")

            // Extract IV from the encrypted data (first 16 bytes)
            val iv = cipherText.copyOfRange(0, 16)

            Log.d("DecryptionTest", "IV: ${Base64.encodeToString(iv, Base64.DEFAULT)}")
            Log.d("DecryptionTest", "IV (raw): ${Arrays.toString(iv)}")

            // Initialize the cipher with the new secret key and IV
            val spec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, newSecretKey, spec)

            // Decrypt the entire cipherText
            val decryptedData = cipher.doFinal(cipherText)

            return String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("DecryptionError", "Error during decryption: ${e.message}")
            return ""
        }
    }*/

    fun decryptData(encryptedDataBase64: String?): String {
        Log.e("DecryptionError", "value is: $encryptedDataBase64")

        if (encryptedDataBase64.isNullOrBlank()) {
            Log.e("DecryptionError", "Encrypted data is null or blank")
            return ""
        }

        try {
            val cipherText = Base64.decode(encryptedDataBase64, Base64.DEFAULT)

            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Fetch the original secret key entry from the keystore
            val secretKeyEntry = keyStore.getEntry(context.toString(), null) as KeyStore.SecretKeyEntry
            val originalSecretKey = secretKeyEntry.secretKey

            // Log information about the original secret key
            Log.d("DecryptionTest", "Original Key Algorithm: ${originalSecretKey.algorithm}")
            Log.d("DecryptionTest", "Original Key Format: ${originalSecretKey.format}")

            // Extract IV from the encrypted data (first 16 bytes)
            val iv = cipherText.copyOfRange(0, 16)

            Log.d("DecryptionTest", "IV: ${Base64.encodeToString(iv, Base64.DEFAULT)}")
            Log.d("DecryptionTest", "IV (raw): ${Arrays.toString(iv)}")

            // Initialize the cipher with the original secret key and IV
            val spec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, originalSecretKey, spec)

            // Decrypt the entire cipherText
            val decryptedData = cipher.doFinal(cipherText)

            return String(decryptedData, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("DecryptionError", "Error during decryption: ${e.message}")
            return ""
        }
    }


}