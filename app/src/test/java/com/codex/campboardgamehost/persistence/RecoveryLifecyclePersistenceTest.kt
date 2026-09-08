package com.codex.campboardgamehost

import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertEquals
import org.junit.Test

class RecoveryLifecyclePersistenceTest {
    @Test
    fun pauseThenStopAvoidsSecondPhysicalWriteAfterSuccessfulPause() {
        val gate = RecoveryWriteGate()
        var writes = 0
        var savedAtMillis = 1_000L
        var round = 1

        val persist: (Boolean) -> Unit = { force ->
            gate.persist(snapshot(savedAtMillis++, round), force = force) {
                writes += 1
                true
            }
        }

        persistRecoveryForLifecycleEvent(Lifecycle.Event.ON_PAUSE, persist)
        persistRecoveryForLifecycleEvent(Lifecycle.Event.ON_STOP, persist)

        assertEquals(1, writes)
    }

    @Test
    fun stopRetriesWhenPausePhysicalWriteFails() {
        val gate = RecoveryWriteGate()
        var writes = 0
        var savedAtMillis = 1_000L

        val persist: (Boolean) -> Unit = { force ->
            gate.persist(snapshot(savedAtMillis++), force = force) {
                writes += 1
                writes > 1
            }
        }

        persistRecoveryForLifecycleEvent(Lifecycle.Event.ON_PAUSE, persist)
        persistRecoveryForLifecycleEvent(Lifecycle.Event.ON_STOP, persist)

        assertEquals(2, writes)
    }

    @Test
    fun stopPersistsDurableContentChangedAfterPause() {
        val gate = RecoveryWriteGate()
        var writes = 0
        var savedAtMillis = 1_000L
        var round = 1

        val persist: (Boolean) -> Unit = { force ->
            gate.persist(snapshot(savedAtMillis++, round), force = force) {
                writes += 1
                true
            }
        }

        persistRecoveryForLifecycleEvent(Lifecycle.Event.ON_PAUSE, persist)
        round = 2
        persistRecoveryForLifecycleEvent(Lifecycle.Event.ON_STOP, persist)

        assertEquals(2, writes)
    }

    private fun snapshot(
        savedAtMillis: Long,
        round: Int = 1,
    ): RecoverySnapshot = RecoverySnapshot(
        compatibilityToken = "recovery-v2:Undercover",
        savedAtMillis = savedAtMillis,
        game = UndercoverRecovery(
            entryPoint = RecoveryEntryPoint.Stable,
            currentDealIndex = 0,
            round = round,
            cards = listOf(PlayerCard("Alice", Role.Civilian, "cat")),
            records = emptyList(),
            outcome = null,
            undercoverCount = 1,
            includeBlank = false,
            lastWordsMode = LastWordsMode.FirstDay,
        ),
    )
}
