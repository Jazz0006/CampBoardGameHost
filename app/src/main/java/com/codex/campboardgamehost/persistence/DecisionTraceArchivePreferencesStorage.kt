package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.recommendation.sde.DecisionTraceArchiveStore

/**
 * Android transport for the SDE DecisionTrace diagnostic archive.
 *
 * SharedPreferences is physical storage only. DecisionTraceArchive remains the diagnostic owner and
 * canonical game/history authority remains elsewhere.
 */
internal object DecisionTraceArchivePreferencesStorage {
    private const val PREFS_NAME = "camp_board_game_host"
    private const val STORAGE_KEY = "decision_trace_archive_v1"

    fun fromContext(context: Context): DecisionTraceArchiveStore {
        val preferences =
            context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return DecisionTraceArchiveStore(
            readRaw = { preferences.getString(STORAGE_KEY, null) },
            writeRaw = { raw ->
                preferences.edit().putString(STORAGE_KEY, raw).commit()
            },
        )
    }
}
