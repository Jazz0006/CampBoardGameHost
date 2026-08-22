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
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredNumberInformationUiModelTest {
    private val revision = InformationDecisionRevision(gameStateRevision = 12, playerInputRevision = 4)

    @Test
    fun `number UI projects only Foundation legal candidates and recommendation identity`() {
        val context = context(
            listOf(
                evaluation("empath-0", 0, truthful = false),
                evaluation("empath-1", 1, truthful = true),
                evaluation("empath-2", 2, truthful = false),
            ),
            recommendedCandidateIds = setOf("empath-2"),
        )

        val ui = StructuredNumberInformationUiModel.from(context)

        assertEquals(listOf(0, 1, 2), ui.choices.map { it.value })
        assertEquals(setOf("empath-0", "empath-1", "empath-2"), ui.choices.map { it.candidateId }.toSet())
        assertEquals(listOf(2), ui.choices.filter { it.recommended }.map { it.value })
        assertTrue(ui.choices.none { it.value == 3 })
    }

    @Test
    fun `semantic state key survives reconstruction but changes with decision projection`() {
        val evaluations = listOf(
            evaluation("empath-0", 0, truthful = false),
            evaluation("empath-1", 1, truthful = true),
            evaluation("empath-2", 2, truthful = false),
        )
        val first = StructuredNumberInformationUiModel.from(context(evaluations, setOf("empath-2")))
        val reconstructed = StructuredNumberInformationUiModel.from(context(evaluations, setOf("empath-2")))
        val changedRecommendation = StructuredNumberInformationUiModel.from(context(evaluations, setOf("empath-0")))

        assertEquals(first.semanticStateKey, reconstructed.semanticStateKey)
        assertTrue(first.semanticStateKey != changedRecommendation.semanticStateKey)
    }

    @Test
    fun `accept recommendation and structured manual choice share Foundation confirmation`() {
        val context = context(
            listOf(
                evaluation("empath-0", 0, truthful = false),
                evaluation("empath-1", 1, truthful = true),
                evaluation("empath-2", 2, truthful = false),
            ),
            recommendedCandidateIds = setOf("empath-2"),
        )
        val ui = StructuredNumberInformationUiModel.from(context)

        val accepted = ui.acceptRecommendation("empath-2", revision)
        val manual = ui.chooseManually("empath-0", revision)

        assertEquals(InformationDecisionSource.RECOMMENDATION_ACCEPTED, accepted.confirmed!!.source)
        assertEquals(InformationDecisionSource.MANUAL, manual.confirmed!!.source)
        assertEquals(2, (accepted.confirmed!!.draft.proposition as InformationProposition.NumericResult).value)
        assertEquals(0, (manual.confirmed!!.draft.proposition as InformationProposition.NumericResult).value)
        assertTrue(
            (manual.validation as InformationDecisionValidationResult.Allowed)
                .warnings.any { it.code == "information.manual.differs-from-recommendation" },
        )
    }

    @Test
    fun `structured number UI cannot confirm a candidate that Foundation did not expose`() {
        val context = context(
            listOf(evaluation("empath-1", 1, truthful = true)),
            recommendedCandidateIds = setOf("empath-1"),
        )
        val ui = StructuredNumberInformationUiModel.from(context)

        val result = ui.chooseManually("invented-empath-3", revision)

        assertEquals(
            InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.ILLEGAL_CANDIDATE),
            result.validation,
        )
        assertNull(result.confirmed)
    }

    @Test
    fun `structured number UI preserves Foundation stale-context rejection`() {
        val context = context(
            listOf(evaluation("empath-1", 1, truthful = true)),
            recommendedCandidateIds = setOf("empath-1"),
        )
        val ui = StructuredNumberInformationUiModel.from(context)

        val result = ui.acceptRecommendation(
            "empath-1",
            revision.copy(playerInputRevision = revision.playerInputRevision + 1),
        )

        assertEquals(
            InformationDecisionValidationResult.Blocked(InformationDecisionHardBlockReason.STALE_CONTEXT),
            result.validation,
        )
        assertNull(result.confirmed)
    }

    private fun context(
        evaluations: List<DecisionEvaluation<DynamicInformationOutcome.Number>>,
        recommendedCandidateIds: Set<String>,
    ): InformationDecisionContext<DynamicInformationOutcome.Number> = InformationDecisionContext.fromEvaluations(
        evaluations = evaluations,
        recommendedCandidateIds = recommendedCandidateIds,
        revision = revision,
        semanticIdentity = "empath-night-2",
        draftOf = { evaluation -> draft(evaluation) },
    )

    private fun evaluation(
        candidateId: String,
        value: Int,
        truthful: Boolean,
    ): DecisionEvaluation<DynamicInformationOutcome.Number> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = candidateId,
            candidateFamilyId = if (truthful) "malfunction-truth" else "malfunction-falsehood-numeric",
            outcome = DynamicInformationOutcome.Number(value),
            abilityState = AbilityState.MALFUNCTIONING_POISONED,
            truthRelation = if (truthful) TruthRelation.TRUE_TO_ACTUAL_STATE else TruthRelation.FALSE_TO_ACTUAL_STATE,
            effects = listOf(
                EffectDraft.PlayerInformation(
                    recipientSeat = 2,
                    sourceAbility = RoleId("Empath"),
                    value = InformationValue.Number(value),
                ),
            ),
            metadata = CandidateMetadata("test-v1", "empath-number-ui"),
        ),
        qualityTier = QualityTier.RECOMMENDED,
        totalScore = 0,
        withinFamilyWeightFixedPoint = 1,
        finalProbabilityFixedPoint = 0,
        pressureDelta = emptyMap(),
        warnings = emptyList(),
        explanationCodes = listOf("test"),
    )

    private fun draft(
        evaluation: DecisionEvaluation<DynamicInformationOutcome.Number>,
    ): EpistemicObservationDraft = EpistemicObservationDraft(
        recordId = "empath-${evaluation.candidate.candidateId}",
        phase = StorytellerPhase.NIGHT,
        round = 2,
        sequence = 5,
        sourceSeat = 2,
        sourceAbility = RoleId("Empath"),
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(2),
        reliability = ObservationReliability.KNOWN_MALFUNCTIONING,
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 2,
            subjectSeats = listOf(1, 3),
            value = (evaluation.candidate.outcome as DynamicInformationOutcome.Number).value,
        ),
    )
}
