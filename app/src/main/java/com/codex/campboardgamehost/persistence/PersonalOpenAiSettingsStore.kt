package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.session.PersonalDirectOpenAiV1

/** Personal test mode only. Separate Keystore alias, file, AAD and preference scope from gateway. */
internal class PersonalOpenAiSettingsStore(context: Context) {
    private val credentials = StorytellerGatewayConnectionStore(context, "direct")
    private val prefs = context.applicationContext
        .getSharedPreferences("personal_openai_test_mode_v1", Context.MODE_PRIVATE)

    fun loadEnabled(): Boolean = prefs.getBoolean("direct_enabled", false)
    fun saveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("direct_enabled", enabled).apply()
    }
    fun loadModel(): String {
        val saved = credentials.loadEndpoint().trim()
        // Migrate the former shipped default; a saved gpt-5.6-luna would otherwise
        // silently override the new default after every app upgrade.
        return if (saved.isBlank() || saved == "gpt-5.6-luna")
            PersonalDirectOpenAiV1.DEFAULT_MODEL else saved
    }
    fun saveModel(model: String) = credentials.saveEndpoint(model)
    fun loadKey(): String = credentials.loadToken()
    fun saveKey(key: String): Boolean = credentials.saveToken(key)
}
