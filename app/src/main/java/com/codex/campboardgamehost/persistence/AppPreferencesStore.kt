package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.domain.StorytellerExperienceMode
import org.json.JSONArray

/**
 * Preferences owned by the application shell rather than an active game session.
 *
 * Recovery and archived-game persistence deliberately live behind separate owners.
 */
internal class AppPreferencesStore(
    private val context: Context,
) {
    fun loadLanguageMode(): LanguageMode {
        val value = preferences.getString(LANGUAGE_MODE_KEY, LanguageMode.System.prefsValue)
        return LanguageMode.entries.firstOrNull { it.prefsValue == value } ?: LanguageMode.System
    }

    fun saveLanguageMode(languageMode: LanguageMode) {
        preferences.edit()
            .putString(LANGUAGE_MODE_KEY, languageMode.prefsValue)
            .apply()
    }

    fun loadStorytellerExperienceMode(): StorytellerExperienceMode =
        StorytellerExperienceMode.fromPrefsValue(
            preferences.getString(STORYTELLER_EXPERIENCE_MODE_KEY, null),
        )

    fun saveStorytellerExperienceMode(mode: StorytellerExperienceMode) {
        preferences.edit()
            .putString(STORYTELLER_EXPERIENCE_MODE_KEY, mode.prefsValue)
            .apply()
    }

    fun loadCommonPlayers(): List<String> {
        val hasStoredPlayers = preferences.contains(COMMON_PLAYERS_KEY)
        val raw = preferences.getString(COMMON_PLAYERS_KEY, null)
        val storedPlayers = raw?.let { encoded ->
            runCatching {
                val json = JSONArray(encoded)
                List(json.length()) { index -> json.getString(index) }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
            }.getOrElse { emptyList() }
        } ?: emptyList()
        return resolveInitialCommonPlayers(
            hasStoredPlayers = hasStoredPlayers,
            storedPlayers = storedPlayers,
        )
    }

    fun saveCommonPlayers(players: List<String>) {
        val json = JSONArray()
        players.map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .forEach { json.put(it) }
        preferences.edit()
            .putString(COMMON_PLAYERS_KEY, json.toString())
            .apply()
    }

    private val preferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFS_NAME = "camp_board_game_host"
        const val COMMON_PLAYERS_KEY = "common_players"
        const val LANGUAGE_MODE_KEY = "language_mode"
        const val STORYTELLER_EXPERIENCE_MODE_KEY = "storyteller_experience_mode"
    }
}
