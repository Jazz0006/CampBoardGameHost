package com.codex.campboardgamehost

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Host-device configuration only. Never part of a game archive or APK.
 *
 * Endpoint is an ordinary preference. The bearer token is encrypted with an
 * AndroidKeyStore non-exportable AES/GCM key, written atomically to noBackupFilesDir.
 * Backup/restore, a different debug application ID, reinstall, and changed signing
 * identity are not a way to migrate the secret: the user can enter it again.
 */
internal class StorytellerGatewayConnectionStore(
    context: Context,
) {
    private val app = context.applicationContext
    private val tokenFile = AtomicFile(File(app.noBackupFilesDir, TOKEN_FILE))

    fun loadEndpoint(): String =
        app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(ENDPOINT_KEY, "").orEmpty()

    fun saveEndpoint(endpoint: String) {
        app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(ENDPOINT_KEY, endpoint.trim()).apply()
    }

    fun loadToken(): String = runCatching {
        // AtomicFile can recover a previous complete write even after an interrupted update.
        val bytes = tokenFile.readFully()
        val (iv, ciphertext) = GatewayTokenEnvelopeV1.decode(bytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, loadOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(AAD)
        String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
    }.getOrDefault("") // Missing/revoked device key fails closed; never log token/ciphertext.

    fun saveToken(token: String): Boolean = runCatching {
        if (token.isEmpty()) {
            tokenFile.delete()
            return@runCatching true
        }
        require(token.toByteArray(StandardCharsets.UTF_8).size <= MAX_PLAINTEXT_BYTES)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, loadOrCreateKey())
        cipher.updateAAD(AAD)
        val encrypted = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))
        val envelope = GatewayTokenEnvelopeV1.encode(cipher.iv, encrypted)
        val stream = tokenFile.startWrite()
        try {
            stream.write(envelope)
            tokenFile.finishWrite(stream)
        } catch (error: Exception) {
            tokenFile.failWrite(stream)
            throw error
        }
        true
    }.getOrDefault(false)

    private fun loadOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null)
        if (existing is SecretKey) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val PREFS_NAME = "storyteller_gateway_connection"
        const val ENDPOINT_KEY = "endpoint"
        const val TOKEN_FILE = "storyteller_gateway_token_v1.enc"
        const val KEY_ALIAS = "campboardgamehost.storyteller.gateway.token.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val MAX_PLAINTEXT_BYTES = 4096
        val AAD: ByteArray = "com.codex.campboardgamehost.storyteller.gateway.v1"
            .toByteArray(StandardCharsets.UTF_8)
    }
}
