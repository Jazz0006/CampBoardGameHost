package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredFortuneTellerInformationAdapterTest {
    private val coordinator = ClocktowerRecommendationCoordinator()
    private val revision = InformationDecisionRevision(gameStateRevision = 14, playerInputRevision = 21)

    @Test
    fun `healthy Fortune Teller exposes only the truthful legal result`() {
        val model = prepareBooleanInformationUiModel(
            coordinator = coordinator,
            gameId = "fortune-healthy",
            phase = ClocktowerPhase.Night,
            round = 2,
            sequence = 8,
            actorSeat = 4,
            abilityRole = RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = listOf(2, 7),
            trueValue = true,
            reliability = InformationReliability.RELIABLE,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = true,
        )

        assertEquals(listOf(true), model.choices.map { it.value })
        assertTrue(model.choices.single().recommended)
    }

    @Test
    fun `poisoned Fortune Teller exposes both legal results with the recommendation marked`() {
        val model = poisonedModel()

        assertEquals(listOf(false, true), model.choices.map { it.value })
        assertEquals(false, model.choices.single { it.recommended }.value)
    }

    @Test
    fun `manual Fortune Teller result keeps the exact selected pair in the typed proposition`() {
        val model = poisonedModel()
        val manualYes = model.choices.single { it.value }

        val confirmation = model.chooseManually(manualYes.candidateId, revision)
        val confirmed = requireNotNull(confirmation.confirmed)
        val proposition = confirmed.draft.proposition as InformationProposition.BooleanResult

        assertEquals(InformationDecisionSource.MANUAL, confirmed.source)
        assertEquals(RoleId("Fortune Teller"), confirmed.draft.sourceAbility)
        assertEquals(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, proposition.metric)
        assertEquals(4, proposition.sourceSeat)
        assertEquals(listOf(2, 7), proposition.subjectSeats)
        assertEquals(true, proposition.value)
    }

    private fun poisonedModel() = prepareBooleanInformationUiModel(
        coordinator = coordinator,
        gameId = "fortune-poisoned",
        phase = ClocktowerPhase.Night,
        round = 2,
        sequence = 8,
        actorSeat = 4,
        abilityRole = RoleId("Fortune Teller"),
        metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
        subjectSeats = listOf(2, 7),
        trueValue = true,
        reliability = InformationReliability.POISONED,
        recommendationStyle = RecommendationStyle.BALANCED,
        revision = revision,
        recommendedValue = false,
        falseMisinformationPressure = 3,
    )
}
