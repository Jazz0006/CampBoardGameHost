package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.CandidateMetadata
import com.codex.campboardgamehost.clocktower.domain.DecisionCandidate
import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InformationDecisionFoundationTest {
    private val currentRevision = InformationDecisionRevision(gameStateRevision = 7, playerInputRevision = 11)

    @Test
    fun `manual and accepted recommendation of the same legal result produce the same unbound draft`() {
        val truthful = evaluation("truth", AbilityState.FUNCTIONING, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val context = context(listOf(truthful), recommendedCandidateIds = setOf("truth"))

        val manual = context.confirm("truth", InformationDecisionSource.MANUAL, currentRevision)
        val accepted = context.confirm("truth", InformationDecisionSource.RECOMMENDATION_ACCEPTED, currentRevision)

        assertNotNull(manual.confirmed)
        assertNotNull(accepted.confirmed)
        assertEquals(manual.confirmed!!.draft, accepted.confirmed!!.draft)
        assertTrue(manual.confirmed!!.draft is EpistemicObservationDraft)
        assertEquals(InformationDecisionSource.MANUAL, manual.confirmed!!.source)
        assertEquals(InformationDecisionSource.RECOMMENDATION_ACCEPTED, accepted.confirmed!!.source)
    }

    @Test
    fun `healthy false result is hard blocked for both decision sources`() {
        val truth = evaluation("truth", AbilityState.FUNCTIONING, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val falsehood = evaluation("false", AbilityState.FUNCTIONING, TruthRelation.FALSE_TO_ACTUAL_STATE)
        val context = context(listOf(truth, falsehood), recommendedCandidateIds = setOf("truth"))

        InformationDecisionSource.entries.forEach { source ->
            val result = context.confirm("false", source, currentRevision)
            assertEquals(
                InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.ILLEGAL_CANDIDATE),
                result.validation,
            )
            assertNull(result.confirmed)
        }
    }

    @Test
    fun `impaired false result remains a legal unreliable manual choice`() {
        val truth = evaluation("truth", AbilityState.MALFUNCTIONING_POISONED, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val falsehood = evaluation("false", AbilityState.MALFUNCTIONING_POISONED, TruthRelation.FALSE_TO_ACTUAL_STATE)
        val context = context(listOf(truth, falsehood), recommendedCandidateIds = setOf("false"))

        val result = context.confirm("false", InformationDecisionSource.MANUAL, currentRevision)

        assertTrue(result.validation is InformationDecisionValidationResult.Allowed)
        assertEquals("false", result.confirmed!!.candidateId)
        assertEquals(ObservationReliability.KNOWN_MALFUNCTIONING, result.confirmed!!.draft.reliability)
    }

    @Test
    fun `manual input cannot invent a role format or target shape outside the validated candidate set`() {
        val legalPair = evaluation("investigator:minion:2,5", AbilityState.MALFUNCTIONING_DRUNK, TruthRelation.FALSE_TO_ACTUAL_STATE)
        val context = context(listOf(legalPair), recommendedCandidateIds = setOf(legalPair.candidate.candidateId))

        val wrongTargetCount = context.confirm(
            "investigator:minion:2",
            InformationDecisionSource.MANUAL,
            currentRevision,
        )

        assertEquals(
            InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.ILLEGAL_CANDIDATE),
            wrongTargetCount.validation,
        )
        assertNull(wrongTargetCount.confirmed)
    }

    @Test
    fun `legal manual result that differs from recommendation is allowed with a soft warning`() {
        val recommended = evaluation("false", AbilityState.MALFUNCTIONING_DRUNK, TruthRelation.FALSE_TO_ACTUAL_STATE)
        val legalAlternative = evaluation("truth", AbilityState.MALFUNCTIONING_DRUNK, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val context = context(listOf(recommended, legalAlternative), recommendedCandidateIds = setOf("false"))

        val result = context.confirm("truth", InformationDecisionSource.MANUAL, currentRevision)

        val allowed = result.validation as InformationDecisionValidationResult.Allowed
        assertTrue(allowed.warnings.any { it.code == "information.manual.differs-from-recommendation" })
        assertNotNull(result.confirmed)
    }

    @Test
    fun `accepted recommendation must be one of the context recommendations`() {
        val recommended = evaluation("false", AbilityState.MALFUNCTIONING_DRUNK, TruthRelation.FALSE_TO_ACTUAL_STATE)
        val legalAlternative = evaluation("truth", AbilityState.MALFUNCTIONING_DRUNK, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val context = context(listOf(recommended, legalAlternative), recommendedCandidateIds = setOf("false"))

        val result = context.confirm("truth", InformationDecisionSource.RECOMMENDATION_ACCEPTED, currentRevision)

        assertEquals(
            InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.NOT_RECOMMENDED),
            result.validation,
        )
        assertNull(result.confirmed)
    }

    @Test
    fun `stale decision context is rejected before confirmation`() {
        val truth = evaluation("truth", AbilityState.FUNCTIONING, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val context = context(listOf(truth), recommendedCandidateIds = setOf("truth"))

        val result = context.confirm(
            "truth",
            InformationDecisionSource.MANUAL,
            currentRevision.copy(playerInputRevision = currentRevision.playerInputRevision + 1),
        )

        assertEquals(
            InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.STALE_CONTEXT),
            result.validation,
        )
        assertNull(result.confirmed)
    }

    @Test
    fun `confirmation retains exact immutable context snapshot beyond candidate id and revision`() {
        val truth = evaluation("truth", AbilityState.FUNCTIONING, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val contextA = context(
            evaluations = listOf(truth),
            recommendedCandidateIds = setOf("truth"),
            semanticIdentity = "context-a",
        )
        val contextB = context(
            evaluations = listOf(truth),
            recommendedCandidateIds = setOf("truth"),
            semanticIdentity = "context-b",
        )

        val confirmationA = requireNotNull(
            contextA.confirm("truth", InformationDecisionSource.MANUAL, currentRevision).confirmed,
        )
        val confirmationB = requireNotNull(
            contextB.confirm("truth", InformationDecisionSource.MANUAL, currentRevision).confirmed,
        )

        val snapshotField = confirmationA.javaClass.getDeclaredField("contextSnapshot")
        snapshotField.isAccessible = true
        val snapshotA = snapshotField.get(confirmationA)
        val snapshotB = snapshotField.get(confirmationB)

        assertNotNull(snapshotA)
        assertNotNull(snapshotB)
        assertTrue(snapshotA!!.toString().contains("context-a"))
        assertTrue(snapshotB!!.toString().contains("context-b"))
        assertTrue("Each confirmation must retain its own immutable snapshot.", snapshotA != snapshotB)
    }

    @Test
    fun `confirmed authority is stale when durable publication revision advances`() {
        val truth = evaluation("truth", AbilityState.FUNCTIONING, TruthRelation.TRUE_TO_ACTUAL_STATE)
        val confirmation = requireNotNull(
            context(listOf(truth), recommendedCandidateIds = setOf("truth"))
                .confirm("truth", InformationDecisionSource.RECOMMENDATION_ACCEPTED, currentRevision)
                .confirmed,
        )

        assertFalse(
            confirmation.contextSnapshot.isCurrentFor(
                InformationDecisionRevision(gameStateRevision = 7, playerInputRevision = 12),
            ),
        )
    }

    @Test
    fun `decision provenance is intentionally limited to manual and accepted recommendation`() {
        assertEquals(
            setOf("MANUAL", "RECOMMENDATION_ACCEPTED"),
            InformationDecisionSource.entries.map { it.name }.toSet(),
        )
    }

    private fun context(
        evaluations: List<DecisionEvaluation<DynamicInformationOutcome.Category>>,
        recommendedCandidateIds: Set<String>,
        semanticIdentity: String = "test-information-decision",
    ): InformationDecisionContext<DynamicInformationOutcome.Category> = InformationDecisionContext.fromEvaluations(
        evaluations = evaluations,
        recommendedCandidateIds = recommendedCandidateIds,
        revision = currentRevision,
        semanticIdentity = semanticIdentity,
        draftOf = { evaluation -> draft(evaluation) },
    )

    private fun evaluation(
        candidateId: String,
        abilityState: AbilityState,
        truthRelation: TruthRelation,
        warnings: List<String> = emptyList(),
    ): DecisionEvaluation<DynamicInformationOutcome.Category> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = candidateId,
            candidateFamilyId = if (truthRelation == TruthRelation.FALSE_TO_ACTUAL_STATE) "false" else "truth",
            outcome = DynamicInformationOutcome.Category(candidateId),
            abilityState = abilityState,
            truthRelation = truthRelation,
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = 2,
                    sourceAbility = RoleId("Empath"),
                    value = InformationValue.Category(candidateId),
                ),
            ),
            metadata = CandidateMetadata("test-v1", "information-decision-test"),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = 0,
        withinFamilyWeightFixedPoint = 1,
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = warnings,
        explanationCodes = listOf("test"),
    )

    private fun draft(
        evaluation: DecisionEvaluation<DynamicInformationOutcome.Category>,
    ): EpistemicObservationDraft = EpistemicObservationDraft(
        recordId = "decision-${evaluation.candidate.candidateId}",
        phase = StorytellerPhase.NIGHT,
        round = 2,
        sequence = 3,
        sourceSeat = 2,
        sourceAbility = RoleId("Empath"),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(2),
        reliability = when (evaluation.candidate.abilityState) {
            AbilityState.FUNCTIONING -> ObservationReliability.RECEIVED_AS_FUNCTIONING
            AbilityState.MALFUNCTIONING_DRUNK,
            AbilityState.MALFUNCTIONING_POISONED,
            -> ObservationReliability.KNOWN_MALFUNCTIONING
        },
        proposition = InformationProposition.RoleInPlay(RoleId(evaluation.candidate.candidateId), inPlay = true),
    )
}
