package com.codex.campboardgamehost

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import java.io.IOException

/** Main-thread UI adapter for one Session. The snapshot contains only accepted Session state. */
internal class ClocktowerSessionUiState(
    val session: ClocktowerGameSession,
    private val onSaveError: () -> Unit,
) {
    var snapshot by mutableStateOf(session.state)
        private set
    private var active = true

    /** Invalidate captured callbacks before replacing/restoring the active Session. */
    fun deactivate() { active = false }

    /** Session owns atomicity and persistence; publish only after its operation succeeds. */
    fun <T> commit(confirm: (ClocktowerGameSession) -> T): T? {
        if (!active) return null
        return try {
            val result = confirm(session)
            if (active) snapshot = session.state
            result
        } catch (_: IOException) {
            if (active) onSaveError()
            null
        }
    }
}
