package com.codex.campboardgamehost.clocktower.history

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CandidateAuditSummary
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import org.junit.Assert.assertEquals
import org.junit.Test

class DecisionHistoryRepositoryTest {
    @Test
    fun `only applied effective events contribute to every projection`() {
        val falseEvent = event(
            id = "event-1",
            truth = TruthRelation.FALSE_TO_ACTUAL_STATE,
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            pressureDelta = mapOf(2 to 4),
            registrations = listOf(registration("registration-1", Alignment.EVIL, CharacterType.MINION)),
        )
        val truthfulEvent = event(
            id = "event-2",
            truth = TruthRelation.TRUE_TO_ACTUAL_STATE,
            abilityState = AbilityState.MALFUNCTIONING_DRUNK,
            pressureDelta = mapOf(2 to -2),
        )
        val ignoredProposal = event(
            id = "event-3",
            status = DecisionEventStatus.PROPOSED,
            truth = TruthRelation.FALSE_TO_ACTUAL_STATE,
            pressureDelta = mapOf(2 to 20),
        )

        val projection = DecisionHistoryRepository(
            DecisionHistoryArchive(events = listOf(falseEvent, truthfulEvent, ignoredProposal)),
        ).project()

        assertEquals(listOf("event-1", "event-2"), projection.effectiveEvents.map { it.eventId })
        assertEquals(4, projection.pressureBySeat.getValue(2).directSuspicion)
        assertEquals(2, projection.pressureBySeat.getValue(2).confirmation)
        assertEquals(2, projection.pressureBySeat.getValue(2).recentTargetCount)
        assertEquals(2, projection.misinformationLedger.totalOpportunities)
        assertEquals(1, projection.misinformationLedger.falseInformationCount)
        assertEquals(1, projection.misinformationLedger.highImpactFalseCount)
        assertEquals(0, projection.misinformationLedger.consecutiveFalseCount)
        assertEquals(1, projection.misinformationLedger.truthfulWhileImpairedCount)
        assertEquals(1, projection.registrationLedgerBySeat.getValue(2).evilRegistrationCount)
        assertEquals(1, projection.registrationLedgerBySeat.getValue(2).minionRegistrationCount)
    }

    @Test
    fun `correction removes the replaced event and retains the replacement`() {
        val replaced = event("event-1", pressureDelta = mapOf(2 to 4))
        val replacement = event("event-2", pressureDelta = mapOf(3 to 1))
        val archive = DecisionHistoryArchive(
            events = listOf(replaced, replacement),
            corrections = listOf(DecisionCorrectionEvent("correction-1", "event-1", "event-2", "operator-fix")),
        )

        val projection = DecisionHistoryRepository(archive).project()

        assertEquals(listOf("event-2"), projection.effectiveEvents.map { it.eventId })
        assertEquals(setOf(3), projection.pressureBySeat.keys)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `correction chains cannot merge into one replacement`() {
        DecisionHistoryRepository(
            DecisionHistoryArchive(
                events = listOf(event("event-1"), event("event-2"), event("event-3")),
                corrections = listOf(
                    DecisionCorrectionEvent("correction-1", "event-1", "event-3", "operator-fix"),
                    DecisionCorrectionEvent("correction-2", "event-2", "event-3", "operator-fix"),
                ),
            ),
        ).project()
    }

    private fun registration(id: String, alignment: Alignment, type: CharacterType) = RegistrationFact(
        interactionId = id,
        subjectSeat = 2,
        registeredAlignment = alignment,
        registeredType = type,
        registrationQuestion = RegistrationQuestion.ABILITY_EFFECT,
        reason = RegistrationReason.SPY_ABILITY,
    )

    private fun event(
        id: String,
        status: DecisionEventStatus = DecisionEventStatus.APPLIED,
        truth: TruthRelation = TruthRelation.NOT_APPLICABLE,
        abilityState: AbilityState = AbilityState.FUNCTIONING,
        pressureDelta: Map<Int, Int> = emptyMap(),
        registrations: List<RegistrationFact> = emptyList(),
    ) = StorytellerDecisionEvent(
        eventId = id,
        requestId = "request-$id",
        idempotencyKey = "game-1:$id",
        gameStateRevision = 4,
        playerInputRevision = 2,
        rulesetRef = RulesetRef(
            scriptId = ScriptId("Trouble Brewing"),
            scriptContentHash = "e12f6425ece137da02477a642235c797",
            rulesetVersion = "trouble-brewing-v1",
            sourceRevision = "official-wiki-2026-08-06",
            coverage = RuleCoverage.PARTIAL,
        ),
        algorithmConfigVersion = "v4-pr8",
        selectorVersion = "weighted-stable-v1",
        decisionSeed = 42,
        stateDigest = "state-digest",
        historyDigest = "history-digest",
        selectedCandidateId = "candidate-$id",
        selectedOutcomeSnapshot = DecisionOutcomeSnapshot("test", emptyMap()),
        abilityState = abilityState,
        truthRelation = truth,
        registrations = registrations,
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = 100,
        finalProbabilityFixedPoint = 1_000_000,
        pressureDelta = pressureDelta,
        candidatePoolFingerprint = "pool-fingerprint",
        candidateAudit = listOf(
            CandidateAuditSummary(
                candidateId = "candidate-$id",
                candidateFamilyId = "test-family",
                qualityTier = QualityTier.RECOMMENDED,
                totalScore = 100,
                finalProbabilityFixedPoint = 1_000_000,
                explanationCodes = emptyList(),
            ),
        ),
        explanationCodes = emptyList(),
        status = status,
    )
}
