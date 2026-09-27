package com.codex.campboardgamehost

import android.content.Context
import com.codex.campboardgamehost.clocktower.recommendation.sde.DecisionTraceArchivePersistenceLane
import com.codex.campboardgamehost.clocktower.recommendation.sde.DecisionTraceArchiveStore

/**
 * Android transport for the SDE DecisionTrace diagnostic archive.
 *
 * SharedPreferences is physical storage only. DecisionTraceArchive remains the diagnostic owner and
 * canonical game/history authority remains elsewhere.
 *
 * The persistence lane is process-scoped rather than Compose-scoped. An admitted NonCancellable
 * write may outlive an Activity/Composition instance, so every UI reconstruction must recover the
 * same serialized mutation owner instead of creating a second Mutex over the same archive.
 */
internal object DecisionTraceArchivePreferencesStorage {
    private const val PREFS_NAME = "camp_board_game_host"
    private const val STORAGE_KEY = "decision_trace_archive_v1"

    private var sharedPersistenceLane: DecisionTraceArchivePersistenceLane? = null

    @Synchronized
    fun persistenceLaneFromContext(context: Context): DecisionTraceArchivePersistenceLane =
        sharedPersistenceLane
            ?: DecisionTraceArchivePersistenceLane(fromContext(context.applicationContext)).also {
                sharedPersistenceLane = it
            }

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
