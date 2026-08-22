package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableNumberContext
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import com.codex.campboardgamehost.clocktower.session.InformationDecisionValidationResult
import com.codex.campboardgamehost.clocktower.session.InformationResolutionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredEmpathInformationAdapterTest {
    private val coordinator = ClocktowerRecommendationCoordinator()
    private val revision = InformationDecisionRevision(gameStateRevision = 5, playerInputRevision = 6)

    @Test
    fun `typed numeric resolution stays behind the coordinator boundary`() {
        val request = InformationResolutionRequest.Number(
            context = UnreliableNumberContext(
                trueValue = 1,
                minimumValue = 0,
                maximumValue = 2,
            ),
            generation = DynamicGenerationContext(
                abilityRole = RoleId("Empath"),
                recipientSeat = 2,
                reliability = InformationReliability.RELIABLE,
                style = RecommendationStyle.BALANCED,
                targetSeats = setOf(1, 3),
            ),
        )

        val typed = coordinator.resolveNumberInformation(request)
        val generic = coordinator.resolveInformation(request)

        assertEquals(generic.map { it.candidate.candidateId }, typed.map { it.candidate.candidateId })
        assertEquals(listOf(0, 1, 2), typed.map { it.candidate.outcome.value }.sorted())
    }

    @Test
    fun `healthy Empath adapter exposes only the Foundation-legal truthful number`() {
        val model = prepareEmpathNumberInformationUiModel(
            coordinator = coordinator,
            gameId = "game-healthy",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 4,
            actorSeat = 2,
            subjectSeats = listOf(1, 3),
            trueValue = 1,
            reliability = InformationReliability.RELIABLE,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = 1,
        )

        assertEquals(listOf(1), model.choices.map { it.value })
        assertTrue(model.choices.single().recommended)
    }

    @Test
    fun `healthy Empath without explicit recommendation falls back to the sole legal truth`() {
        val model = prepareEmpathNumberInformationUiModel(
            coordinator = coordinator,
            gameId = "game-healthy-fallback",
            phase = ClocktowerPhase.Night,
            round = 3,
            sequence = 4,
            actorSeat = 2,
            subjectSeats = listOf(1, 3),
            trueValue = 1,
            reliability = InformationReliability.RELIABLE,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = null,
        )

        val onlyChoice = model.choices.single()
        assertEquals(1, onlyChoice.value)
        assertTrue(onlyChoice.recommended)
        assertTrue(model.acceptRecommendation(onlyChoice.candidateId, revision).confirmed != null)
    }

    @Test
    fun `poisoned Empath accepted recommendation yields the exact unbound player-visible draft`() {
        val model = poisonedModel()

        assertEquals(listOf(0, 1, 2), model.choices.map { it.value })
        val recommended = model.choices.single { it.recommended }
        assertEquals(2, recommended.value)

        val confirmation = model.acceptRecommendation(recommended.candidateId, revision)
        val confirmed = requireNotNull(confirmation.confirmed)
        val draft = confirmed.draft
        val proposition = draft.proposition as InformationProposition.NumericResult

        assertEquals(InformationDecisionSource.RECOMMENDATION_ACCEPTED, confirmed.source)
        assertEquals(StorytellerPhase.NIGHT, draft.phase)
        assertEquals(2, draft.round)
        assertEquals(4, draft.sequence)
        assertEquals(2, draft.sourceSeat)
        assertEquals(RoleId("Empath"), draft.sourceAbility)
        assertEquals(setOf(2), draft.recipientSeats)
        assertEquals(ObservationReliability.RECEIVED_AS_FUNCTIONING, draft.reliability)
        assertEquals(NumericMetric.LIVING_EVIL_NEIGHBOURS, proposition.metric)
        assertEquals(2, proposition.sourceSeat)
        assertEquals(listOf(1, 3), proposition.subjectSeats)
        assertEquals(2, proposition.value)
    }

    @Test
    fun `poisoned Empath truthful manual choice remains legal but requires the Foundation warning`() {
        val model = poisonedModel()
        val truthfulManualChoice = model.choices.single { it.value == 1 }

        val confirmation = model.chooseManually(truthfulManualChoice.candidateId, revision)
        val validation = confirmation.validation as InformationDecisionValidationResult.Allowed

        assertEquals(InformationDecisionSource.MANUAL, confirmation.confirmed!!.source)
        assertTrue(validation.warnings.any { it.code == "information.manual.differs-from-recommendation" })
        assertTrue(validation.warnings.any { it.code == "information.impaired.truthful-with-false-alternative" })
    }

    private fun poisonedModel() = prepareEmpathNumberInformationUiModel(
        coordinator = coordinator,
        gameId = "game-poisoned",
        phase = ClocktowerPhase.Night,
        round = 2,
        sequence = 4,
        actorSeat = 2,
        subjectSeats = listOf(1, 3),
        trueValue = 1,
        reliability = InformationReliability.POISONED,
        recommendationStyle = RecommendationStyle.BALANCED,
        revision = revision,
        recommendedValue = 2,
    )
}
