package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateAuditSummary
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionEventStoreTest {
    @Test
    fun `same event append is idempotent even after current state advances`() {
        val store = InMemoryDecisionEventStore()
        val event = event()

        assertTrue(store.appendAtomically(event, DecisionRevision(4, 2)) is DecisionAppendResult.Appended)
        assertTrue(store.appendAtomically(event, DecisionRevision(5, 3)) is DecisionAppendResult.Existing)
        assertEquals(1, store.allEvents().size)
    }

    @Test
    fun `stale request cannot append an event`() {
        val store = InMemoryDecisionEventStore()

        val result = store.appendAtomically(event(), DecisionRevision(5, 2))

        assertTrue(result is DecisionAppendResult.StaleRequest)
        assertTrue(store.allEvents().isEmpty())
    }

    @Test
    fun `reusing idempotency key for different content fails closed`() {
        val store = InMemoryDecisionEventStore()
        val first = event()
        store.appendAtomically(first, DecisionRevision(4, 2))

        val result = store.appendAtomically(
            first.copy(selectedOutcomeSnapshot = DecisionOutcomeSnapshot("pair", mapOf("value" to "different"))),
            DecisionRevision(4, 2),
        )

        assertTrue(result is DecisionAppendResult.IdempotencyConflict)
        assertEquals(1, store.allEvents().size)
    }

    @Test
    fun `concurrent identical appends create exactly one event`() {
        val store = InMemoryDecisionEventStore()
        val event = event()
        val executor = Executors.newFixedThreadPool(8)
        try {
            val results = executor.invokeAll(
                List(32) { Callable { store.appendAtomically(event, DecisionRevision(4, 2)) } },
            ).map { it.get() }

            assertEquals(1, results.count { it is DecisionAppendResult.Appended })
            assertEquals(31, results.count { it is DecisionAppendResult.Existing })
            assertEquals(1, store.allEvents().size)
        } finally {
            executor.shutdownNow()
        }
    }

    private fun event() = StorytellerDecisionEvent(
        eventId = "event-1",
        requestId = "request-1",
        idempotencyKey = "game-1:first-night:investigator:0",
        gameStateRevision = 4,
        playerInputRevision = 2,
        rulesetRef = RulesetRef(
            scriptId = ScriptId("Trouble Brewing"),
            scriptContentHash = "e12f6425ece137da02477a642235c797",
            rulesetVersion = "trouble-brewing-v1",
            sourceRevision = "official-wiki-2026-08-06",
            coverage = RuleCoverage.PARTIAL,
        ),
        algorithmConfigVersion = "v4-pr4",
        selectorVersion = "weighted-stable-v1",
        decisionSeed = 42,
        stateDigest = "state-digest",
        historyDigest = "history-digest",
        selectedCandidateId = "candidate-1",
        selectedOutcomeSnapshot = DecisionOutcomeSnapshot("pair", mapOf("seats" to "2,5")),
        abilityState = AbilityState.FUNCTIONING,
        truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
        registrations = emptyList(),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = 100,
        finalProbabilityFixedPoint = 1_000_000,
        pressureDelta = mapOf(2 to 1, 5 to 1),
        candidatePoolFingerprint = "pool-fingerprint",
        candidateAudit = listOf(
            CandidateAuditSummary(
                candidateId = "candidate-1",
                candidateFamilyId = "natural-truth",
                qualityTier = QualityTier.RECOMMENDED,
                totalScore = 100,
                finalProbabilityFixedPoint = 1_000_000,
                explanationCodes = listOf("selection.high-quality-pool"),
            ),
        ),
        explanationCodes = listOf("selection.weighted-stable-random"),
        status = DecisionEventStatus.PROPOSED,
    )
}
