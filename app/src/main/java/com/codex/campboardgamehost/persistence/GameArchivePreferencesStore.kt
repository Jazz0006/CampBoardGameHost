package com.codex.campboardgamehost

import android.content.Context
import org.json.JSONArray

/**
 * Archived completed-game preference I/O.
 *
 * This store does not own active-game Recovery and delegates archive wire semantics to
 * [GameArchiveJsonCodec].
 */
internal class GameArchivePreferencesStore(
    private val context: Context,
    private val roleByName: (String) -> ClocktowerRole?,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    fun loadGameHistory(): List<ArchivedGameReview> {
        val raw = preferences.getString(GAME_HISTORY_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    array.optJSONObject(index)?.let { entry ->
                        GameArchiveJsonCodec.decodeEntry(entry, roleByName)?.let(::add)
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun archiveGame(record: GameArchiveRecord): List<ArchivedGameReview> {
        if (record.cards.isEmpty()) return loadGameHistory()

        val existing = runCatching {
            JSONArray(preferences.getString(GAME_HISTORY_KEY, "[]"))
        }.getOrDefault(JSONArray())
        val archivedAt = nowMillis()
        val next = JSONArray().apply {
            put(
                GameArchiveJsonCodec.encodeEntry(
                    record = record,
                    id = archivedAt,
                    archivedAtMillis = archivedAt,
                ),
            )
            for (index in 0 until minOf(existing.length(), MAX_GAME_HISTORY - 1)) {
                existing.optJSONObject(index)?.let(::put)
            }
        }
        preferences.edit().putString(GAME_HISTORY_KEY, next.toString()).commit()
        return loadGameHistory()
    }

    private val preferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private companion object {
        const val PREFS_NAME = "camp_board_game_host"
        const val GAME_HISTORY_KEY = "game_history"
        const val MAX_GAME_HISTORY = 20
    }
}
