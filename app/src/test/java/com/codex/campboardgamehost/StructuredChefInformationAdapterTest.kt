package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StructuredChefInformationAdapterTest {
    private val coordinator = ClocktowerRecommendationCoordinator()
    private val revision = InformationDecisionRevision(gameStateRevision = 10, playerInputRevision = 12)

    @Test
    fun `healthy Chef exposes only the truthful legal number`() {
        val model = prepareNumericInformationUiModel(
            coordinator = coordinator,
            gameId = "chef-healthy",
            phase = ClocktowerPhase.FirstNight,
            round = 1,
            sequence = 5,
            actorSeat = 3,
            abilityRole = RoleId("Chef"),
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            subjectSeats = listOf(1, 2, 3, 4, 5, 6, 7),
            trueValue = 1,
            minimumValue = 0,
            maximumValue = 3,
            reliability = InformationReliability.RELIABLE,
            recommendationStyle = RecommendationStyle.BALANCED,
            revision = revision,
            recommendedValue = 1,
        )

        assertEquals(listOf(1), model.choices.map { it.value })
        assertTrue(model.choices.single().recommended)
    }

    @Test
    fun `poisoned Chef exposes every legal number and preserves recommended identity`() {
        val model = poisonedChefModel()

        assertEquals(listOf(0, 1, 2, 3), model.choices.map { it.value })
        assertEquals(2, model.choices.single { it.recommended }.value)
    }

    @Test
    fun `manual Chef choice commits exact typed adjacent evil pairs proposition`() {
        val model = poisonedChefModel()
        val manualThree = model.choices.single { it.value == 3 }

        val confirmation = model.chooseManually(manualThree.candidateId, revision)
        val confirmed = requireNotNull(confirmation.confirmed)
        val proposition = confirmed.draft.proposition as InformationProposition.NumericResult

        assertEquals(InformationDecisionSource.MANUAL, confirmed.source)
        assertEquals(RoleId("Chef"), confirmed.draft.sourceAbility)
        assertEquals(NumericMetric.ADJACENT_EVIL_PAIRS, proposition.metric)
        assertEquals(3, proposition.sourceSeat)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), proposition.subjectSeats)
        assertEquals(3, proposition.value)
    }

    private fun poisonedChefModel() = prepareNumericInformationUiModel(
        coordinator = coordinator,
        gameId = "chef-poisoned",
        phase = ClocktowerPhase.FirstNight,
        round = 1,
        sequence = 5,
        actorSeat = 3,
        abilityRole = RoleId("Chef"),
        metric = NumericMetric.ADJACENT_EVIL_PAIRS,
        subjectSeats = listOf(1, 2, 3, 4, 5, 6, 7),
        trueValue = 1,
        minimumValue = 0,
        maximumValue = 3,
        reliability = InformationReliability.POISONED,
        recommendationStyle = RecommendationStyle.BALANCED,
        revision = revision,
        recommendedValue = 2,
    )
}
