package com.codex.campboardgamehost

import androidx.lifecycle.Lifecycle

/**
 * Owns the mapping from Android lifecycle checkpoints to Recovery persistence attempts.
 *
 * This is intentionally separate from Compose wiring so lifecycle durability policy can be
 * exercised as a typed behavior contract.
 */
internal fun persistRecoveryForLifecycleEvent(
    event: Lifecycle.Event,
    persist: (force: Boolean) -> Unit,
) {
    when (event) {
        Lifecycle.Event.ON_PAUSE,
        Lifecycle.Event.ON_STOP,
        -> persist(true)
        else -> Unit
    }
}
