package com.codex.campboardgamehost

import androidx.lifecycle.Lifecycle

/**
 * Owns the mapping from Android lifecycle checkpoints to Recovery persistence attempts.
 *
 * ON_PAUSE is the freshness checkpoint. ON_STOP is an ordinary follow-up attempt: unchanged
 * content is deduplicated after a successful pause write, while RecoveryWriteGate retryRequired
 * still forces a physical retry after failure and changed durable content still writes.
 */
internal fun persistRecoveryForLifecycleEvent(
    event: Lifecycle.Event,
    persist: (force: Boolean) -> Unit,
) {
    when (event) {
        Lifecycle.Event.ON_PAUSE -> persist(true)
        Lifecycle.Event.ON_STOP -> persist(false)
        else -> Unit
    }
}
