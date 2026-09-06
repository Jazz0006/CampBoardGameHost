package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.*
import org.junit.Test

class ClocktowerStructuredInformationPreparationTest {
    private val revision = InformationDecisionRevision(5, 6)
    private val identity = ClocktowerInformationDecisionIdentity("preparation", ClocktowerPhase.Night, 2, 4, revision)

    @Test
    fun `assisted defaults prefer displayed then unreliable options then automatic fallback`() {
        val displayed = option(2).copy(isDefaultRecommendation = true)
        val unreliable = option(1).copy(isDefaultRecommendation = true)
        val automatic = option(0)
        assertSame(displayed, clocktowerStructuredRecommendedOption(false, automatic, listOf(displayed), listOf(unreliable)))
        assertSame(unreliable, clocktowerStructuredRecommendedOption(false, automatic, emptyList(), listOf(unreliable)))
        assertSame(automatic, clocktowerStructuredRecommendedOption(false, automatic, emptyList(), emptyList()))
        assertSame(automatic, clocktowerStructuredRecommendedOption(true, automatic, listOf(displayed), listOf(unreliable)))
        assertNull(clocktowerStructuredRecommendedOption(true, null, listOf(displayed), listOf(unreliable)))
    }

    @Test
    fun `Empath uses legacy subjects and truthful candidate before display fallbacks`() {
        val legacy = option(1).copy(isTruthful = true, displayPrimary = "99")
        val input = requireNotNull(clocktowerNumericInformationPreparation(
            step().copy(legacyInformationCandidates = listOf(legacy)), 2, option(2),
        ))
        assertEquals(listOf(1, 3), input.subjectSeats)
        assertEquals(1, input.trueValue)
        assertEquals(2, input.recommendedValue)
        assertEquals(0, input.minimumValue)
        assertEquals(2, input.maximumValue)
        assertFalse(input.isTruthful(1, listOf(legacy.copy(isTruthful = false))))
        assertTrue(input.isTruthful(1, emptyList()))
    }

    @Test
    fun `numeric legacy text fallback is retained when typed values are absent`() {
        val textOption = option(1).copy(proposition = null, displayPrimary = "2", isTruthful = true)
        val prepared = requireNotNull(clocktowerNumericInformationPreparation(
            step().copy(legacyInformationCandidates = listOf(textOption)), 2, textOption,
        ))
        assertEquals(2, prepared.trueValue)
        assertEquals(2, prepared.recommendedValue)
        val subjectsOnly = option(0).copy(isTruthful = false)
        val textStep = step().copy(displayProposition = null, tellPlayer = "1", legacyInformationCandidates = listOf(subjectsOnly))
        assertEquals(1, requireNotNull(clocktowerNumericInformationPreparation(textStep, 2, null)).trueValue)
        assertNull(clocktowerNumericInformationPreparation(textStep.copy(tellPlayer = "not a number"), 2, null))
    }

    @Test
    fun `numeric registration routes and missing actors do not enter structured adapter`() {
        assertNull(clocktowerNumericInformationPreparation(step(), null, null))
        assertNull(clocktowerNumericInformationPreparation(step().copy(spyRegistrationKey = "spy"), 2, null))
        assertNull(clocktowerNumericInformationPreparation(step().copy(recluseRegistrationKey = "recluse"), 2, null))
        assertNull(clocktowerNumericInformationPreparation(step().copy(roleEnName = "Washerwoman"), 2, null))
        assertNull(clocktowerNumericInformationPreparation(step().copy(displayProposition = null), 2, null))
    }

    @Test
    fun `Chef requires unreliable information and explicit numeric bounds`() {
        val chef = step().copy(roleEnName = "Chef", displayProposition = InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 2, listOf(1, 2, 3), 1,
        ), numericMinimumValue = 0, numericMaximumValue = 3)
        val prepared = requireNotNull(clocktowerNumericInformationPreparation(chef, 2, option(2)))
        assertEquals(RoleId("Chef"), prepared.abilityRole)
        assertEquals(NumericMetric.ADJACENT_EVIL_PAIRS, prepared.metric)
        assertEquals(3, prepared.maximumValue)
        assertNull(clocktowerNumericInformationPreparation(chef.copy(informationReliability = InformationReliability.RELIABLE), 2, null))
        assertNull(clocktowerNumericInformationPreparation(chef.copy(numericMaximumValue = null), 2, null))
        assertNull(clocktowerNumericInformationPreparation(chef.copy(numericMinimumValue = null), 2, null))
    }

    @Test
    fun `prepared Empath model matches existing adapter including history and confirmation identity`() {
        val prepared = requireNotNull(clocktowerNumericInformationPreparation(step().copy(previousShownNumber = 0), 2, option(2)))
        val coordinator = ClocktowerRecommendationCoordinator()
        val actual = prepared.prepareUiModel(coordinator, identity, RecommendationStyle.BALANCED)
        val expected = prepareEmpathNumberInformationUiModel(
            coordinator, identity.gameId, identity.phase, identity.round, identity.sequence,
            2, listOf(1, 3), 1, InformationReliability.POISONED, RecommendationStyle.BALANCED, revision, 2,
            previousShownValue = 0, pressureCostPerPoint = 1,
        )
        assertEquals(expected.choices, actual.choices)
        assertEquals(expected.contextSnapshot, actual.contextSnapshot)
        val candidate = actual.choices.single { it.recommended }.candidateId
        assertEquals(expected.acceptRecommendation(candidate, revision), actual.acceptRecommendation(candidate, revision))
    }

    @Test
    fun `Boolean preparation requires exact action actor and ordered selected pair`() {
        val step = fortuneStep()
        assertNotNull(clocktowerBooleanInformationPreparation(step, 4, listOf(2, 7), null))
        assertNull(clocktowerBooleanInformationPreparation(step, null, listOf(2, 7), null))
        assertNull(clocktowerBooleanInformationPreparation(step, 3, listOf(2, 7), null))
        assertNull(clocktowerBooleanInformationPreparation(step, 4, listOf(7, 2), null))
        assertNull(clocktowerBooleanInformationPreparation(step, 4, listOf(2, 2), null))
        assertNull(clocktowerBooleanInformationPreparation(step, 4, listOf(2), null))
        assertNull(clocktowerBooleanInformationPreparation(step.copy(action = ClocktowerNightAction.None), 4, listOf(2, 7), null))
        assertNull(clocktowerBooleanInformationPreparation(step.copy(roleEnName = "Chef"), 4, listOf(2, 7), null))
    }

    @Test
    fun `Boolean recommendation is accepted only for matching typed target identity`() {
        val recommended = option(0).copy(proposition = booleanProposition(false))
        assertEquals(false, requireNotNull(clocktowerBooleanInformationPreparation(fortuneStep(), 4, listOf(2, 7), recommended)).recommendedValue)
        val wrongPair = recommended.copy(proposition = booleanProposition(false).copy(subjectSeats = listOf(1, 7)))
        assertNull(requireNotNull(clocktowerBooleanInformationPreparation(fortuneStep(), 4, listOf(2, 7), wrongPair)).recommendedValue)
        val textOnly = recommended.copy(proposition = null, displayPrimary = "No")
        assertNull(requireNotNull(clocktowerBooleanInformationPreparation(fortuneStep(), 4, listOf(2, 7), textOnly)).recommendedValue)
    }

    @Test
    fun `prepared Boolean model preserves adapter candidate and observation identity`() {
        val prepared = requireNotNull(clocktowerBooleanInformationPreparation(
            fortuneStep(), 4, listOf(2, 7), option(0).copy(proposition = booleanProposition(false)),
        ))
        val coordinator = ClocktowerRecommendationCoordinator()
        val actual = prepared.prepareUiModel(coordinator, identity, RecommendationStyle.BALANCED)
        val expected = prepareBooleanInformationUiModel(
            coordinator, identity.gameId, identity.phase, identity.round, identity.sequence,
            4, RoleId("Fortune Teller"), BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, listOf(2, 7),
            true, InformationReliability.POISONED, RecommendationStyle.BALANCED, revision, false,
        )
        assertEquals(expected.choices, actual.choices)
        assertEquals(expected.contextSnapshot, actual.contextSnapshot)
        val candidate = actual.choices.single { it.recommended }.candidateId
        assertEquals(expected.acceptRecommendation(candidate, revision), actual.acceptRecommendation(candidate, revision))
    }

    private fun step() = ClocktowerNightStepUi(
        title = "Empath", actor = null, isRealAction = true, reason = "", storytellerAction = "",
        tellPlayer = "0", explanation = "", roleEnName = "Empath",
        informationReliability = InformationReliability.POISONED,
        displayProposition = InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 1),
    )
    private fun option(value: Int) = ClocktowerDisplayOption(
        label = "number", displayKind = ClocktowerDisplayKind.Number, displayTitle = "number",
        displayPrimary = value.toString(), displaySecondary = null, displayFooter = null,
        proposition = InformationProposition.NumericResult(NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), value),
    )
    private fun booleanProposition(value: Boolean) = InformationProposition.BooleanResult(
        BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 4, listOf(2, 7), value,
    )
    private fun fortuneStep() = step().copy(roleEnName = "Fortune Teller", action = ClocktowerNightAction.FortuneTeller,
        displayProposition = booleanProposition(true))
}
