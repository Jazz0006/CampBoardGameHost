package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import org.json.JSONArray
import org.json.JSONObject

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

    fun loadStorytellerOperationMode(): StorytellerOperationMode =
        preferences.getString(STORYTELLER_OPERATION_MODE_KEY, null)
            ?.let { stored -> StorytellerOperationMode.entries.firstOrNull { it.name == stored } }
            ?: StorytellerOperationMode.MANUAL

    fun saveStorytellerOperationMode(mode: StorytellerOperationMode) {
        preferences.edit().putString(STORYTELLER_OPERATION_MODE_KEY, mode.name).apply()
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

    /** Persistent player profile, independent of table seat, game and night. */
    fun loadPlayerExperienceByName(): Map<String, PlayerExperienceLevelV1> {
        val raw = preferences.getString(PLAYER_EXPERIENCE_KEY, null) ?: return emptyMap()
        return runCatching {
            val stored = JSONObject(raw)
            buildMap {
                stored.keys().forEach { name ->
                    if (name.isNotBlank()) {
                        val level = PlayerExperienceLevelV1.entries.firstOrNull {
                            it.name == stored.optString(name)
                        } ?: PlayerExperienceLevelV1.BEGINNER
                        if (level != PlayerExperienceLevelV1.BEGINNER) put(name, level)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun savePlayerExperienceByName(values: Map<String, PlayerExperienceLevelV1>) {
        val stored = JSONObject()
        values.forEach { (name, level) ->
            if (name.isNotBlank() && level != PlayerExperienceLevelV1.BEGINNER) {
                stored.put(name, level.name)
            }
        }
        preferences.edit().putString(PLAYER_EXPERIENCE_KEY, stored.toString()).apply()
    }

    private val preferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFS_NAME = "camp_board_game_host"
        const val COMMON_PLAYERS_KEY = "common_players"
        const val PLAYER_EXPERIENCE_KEY = "player_experience_by_name"
        const val LANGUAGE_MODE_KEY = "language_mode"
        const val STORYTELLER_OPERATION_MODE_KEY = "storyteller_operation_mode"
    }
}
