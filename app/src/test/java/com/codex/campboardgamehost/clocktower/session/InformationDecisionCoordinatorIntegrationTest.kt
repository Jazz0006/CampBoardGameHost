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
import org.junit.Assert.assertTrue
import org.junit.Test

class InformationDecisionCoordinatorIntegrationTest {
    @Test
    fun `coordinator exposes recommendation confirmation only through shared information decision context`() {
        val evaluation = evaluation("recommended")
        val revision = InformationDecisionRevision(gameStateRevision = 4, playerInputRevision = 9)
        val coordinator = ClocktowerRecommendationCoordinator()

        val context = coordinator.informationDecisionContext(
            evaluations = listOf(evaluation),
            recommendedCandidateIds = setOf("recommended"),
            revision = revision,
            semanticIdentity = "night-2-empath-seat-2",
            draftOf = { draft(it) },
        )
        val accepted = context.confirm(
            candidateId = "recommended",
            source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
            currentRevision = revision,
        )
        val manual = context.confirm(
            candidateId = "recommended",
            source = InformationDecisionSource.MANUAL,
            currentRevision = revision,
        )

        assertEquals(accepted.confirmed!!.draft, manual.confirmed!!.draft)
        assertEquals("night-2-empath-seat-2", context.semanticIdentity)
        assertTrue(
            ClocktowerRecommendationCoordinator::class.java.declaredMethods.none {
                it.returnType == EpistemicObservationDraft::class.java
            },
        )
    }

    private fun evaluation(candidateId: String): DecisionEvaluation<DynamicInformationOutcome.Category> = DecisionEvaluation(
        candidate = DecisionCandidate(
            candidateId = candidateId,
            candidateFamilyId = "truth",
            outcome = DynamicInformationOutcome.Category(candidateId),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.TRUE_TO_ACTUAL_STATE,
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
        warnings = emptyList(),
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
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = InformationProposition.RoleInPlay(RoleId("Empath"), inPlay = true),
    )
}
