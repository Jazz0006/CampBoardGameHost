package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryWriteGateTest {
    @Test
    fun unchangedRecoveryContentSkipsPhysicalRewriteButRemainsDurable() {
        val gate = RecoveryWriteGate()
        var writes = 0

        assertTrue(gate.persist(snapshot(savedAtMillis = 1_000L)) {
            writes += 1
            true
        })
        assertTrue(gate.persist(snapshot(savedAtMillis = 2_000L)) {
            writes += 1
            true
        })

        assertEquals(1, writes)
    }

    @Test
    fun changedRecoveryContentWritesAgain() {
        val gate = RecoveryWriteGate()
        var writes = 0

        assertTrue(gate.persist(snapshot(savedAtMillis = 1_000L, round = 1)) {
            writes += 1
            true
        })
        assertTrue(gate.persist(snapshot(savedAtMillis = 2_000L, round = 2)) {
            writes += 1
            true
        })

        assertEquals(2, writes)
    }

    @Test
    fun failedWriteIsNotRememberedAsDurableAndIsRetried() {
        val gate = RecoveryWriteGate()
        var writes = 0

        assertFalse(gate.persist(snapshot(savedAtMillis = 1_000L)) {
            writes += 1
            false
        })
        assertTrue(gate.persist(snapshot(savedAtMillis = 2_000L)) {
            writes += 1
            true
        })

        assertEquals(2, writes)
    }

    @Test
    fun clearForgetsDurableIdentitySoSameContentMustBeWrittenAgain() {
        val gate = RecoveryWriteGate()
        var writes = 0
        val snapshot = snapshot(savedAtMillis = 1_000L)

        assertTrue(gate.persist(snapshot) {
            writes += 1
            true
        })
        gate.clear()
        assertTrue(gate.persist(snapshot.copy(savedAtMillis = 2_000L)) {
            writes += 1
            true
        })

        assertEquals(2, writes)
    }

    @Test
    fun forceWriteRefreshesPersistenceEvenWhenSemanticContentIsUnchanged() {
        val gate = RecoveryWriteGate()
        var writes = 0

        assertTrue(gate.persist(snapshot(savedAtMillis = 1_000L)) {
            writes += 1
            true
        })
        assertTrue(gate.persist(snapshot(savedAtMillis = 2_000L), force = true) {
            writes += 1
            true
        })

        assertEquals(2, writes)
    }

    @Test
    fun failedForceWriteRequiresOrdinaryRetryEvenWhenContentMatchesLastDurableWrite() {
        val gate = RecoveryWriteGate()
        var writes = 0

        assertTrue(gate.persist(snapshot(savedAtMillis = 1_000L)) {
            writes += 1
            true
        })
        assertFalse(gate.persist(snapshot(savedAtMillis = 2_000L), force = true) {
            writes += 1
            false
        })
        assertTrue(gate.persist(snapshot(savedAtMillis = 3_000L)) {
            writes += 1
            true
        })

        assertEquals(3, writes)
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
