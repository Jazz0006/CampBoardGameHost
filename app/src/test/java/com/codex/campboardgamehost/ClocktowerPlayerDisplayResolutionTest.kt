package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPlayerDisplayResolutionTest {
    @Test
    fun `same resolved pair option produces identical player display for manual and recommendation paths`() {
        val option = pairOption("Chef", 2, 5)
        val manualResolved = ClocktowerPairManualSelectionModel.from(ClocktowerPairManualAuthority.selectionPresentation(listOf(option)))
            .selectRole("Chef")
            .selectSeat(2)
            .selectSeat(5)
            .resolvedOption
        val recommendationResolved = clocktowerRecommendationPresentation(listOf(option)).primary

        val baseStep = unresolvedStep(option)

        assertEquals(option, manualResolved)
        assertEquals(option, recommendationResolved)
        assertEquals(
            resolveClocktowerPlayerDisplay(baseStep, requireNotNull(manualResolved)),
            resolveClocktowerPlayerDisplay(baseStep, requireNotNull(recommendationResolved)),
        )
    }

    @Test
    fun `resolved player display copies exact visible payload and strips storyteller choices`() {
        val proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.RoleAt(1, RoleId("Empath")),
                InformationProposition.RoleAt(7, RoleId("Empath")),
            ),
        )
        val resolvedOption = ClocktowerDisplayOption(
            label = "storyteller-only label",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Player title",
            displayPrimary = "Empath",
            displaySecondary = "1 / 7",
            displayFooter = "Player footer",
            proposition = proposition,
            isTruthful = false,
        )
        val alternate = pairOption("Chef", 2, 5)
        val baseStep = unresolvedStep(resolvedOption).copy(
            displayOptions = listOf(resolvedOption, alternate),
            recommendedDisplayOptions = listOf(alternate),
        )

        val resolved = resolveClocktowerPlayerDisplay(baseStep, resolvedOption)

        assertEquals(baseStep.title, resolved.title)
        assertEquals(baseStep.actor, resolved.actor)
        assertEquals(baseStep.action, resolved.action)
        assertEquals(baseStep.roleEnName, resolved.roleEnName)
        assertEquals(resolvedOption.displayPrimary, resolved.tellPlayer)
        assertEquals(resolvedOption.displayKind, resolved.displayKind)
        assertEquals(resolvedOption.displayTitle, resolved.displayTitle)
        assertEquals(resolvedOption.displayPrimary, resolved.displayPrimary)
        assertEquals(resolvedOption.displaySecondary, resolved.displaySecondary)
        assertEquals(resolvedOption.displayFooter, resolved.displayFooter)
        assertEquals(proposition, resolved.displayProposition)
        assertEquals(false, resolved.selectedInformationTruthful)
        assertTrue(resolved.displayOptions.isEmpty())
        assertTrue(resolved.recommendedDisplayOptions.isEmpty())
        assertNull(resolved.informationDecisionConfirmation)
    }

    @Test
    fun `numeric result uses confirmed value and authority instead of template semantics`() {
        val model = numericModel()
        val choice = model.choices.single { it.value == 2 }
        val confirmed = requireNotNull(model.acceptRecommendation(choice.candidateId, revision).confirmed)
        val template = pairOption("Chef", 2, 5).copy(
            displayPrimary = "unrelated template value",
            displayTitle = "Number title",
            displaySecondary = "Template seats",
            displayFooter = "Template footer",
        )
        val step = unresolvedStep(template)
        val resolved = resolveClocktowerNumericPlayerDisplay(
            step, template, 2, false, confirmed, model.contextSnapshot,
        )

        assertEquals("2", resolved.tellPlayer)
        assertEquals("2", resolved.displayPrimary)
        assertEquals(ClocktowerDisplayKind.Number, resolved.displayKind)
        assertEquals("Number title", resolved.displayTitle)
        assertEquals("Template seats", resolved.displaySecondary)
        assertEquals("Template footer", resolved.displayFooter)
        assertEquals(false, resolved.selectedInformationTruthful)
        assertSame(confirmed.draft.proposition, resolved.displayProposition)
        assertSame(confirmed, resolved.informationDecisionConfirmation)
        assertSame(model.contextSnapshot, resolved.informationDecisionExpectedSnapshot)
        assertTrue(resolved.displayOptions.isEmpty())
        assertTrue(resolved.recommendedDisplayOptions.isEmpty())
        assertEquals(step.reason, resolved.reason)
        assertEquals(step.roleEnName, resolved.roleEnName)
    }

    @Test
    fun `numeric text falls back per field and preserves intentional empty strings`() {
        val model = numericModel()
        val confirmed = requireNotNull(model.acceptRecommendation(
            model.choices.single { it.recommended }.candidateId, revision,
        ).confirmed)
        val template = pairOption("Chef", 2, 5).copy(displaySecondary = null, displayFooter = null)
        val step = unresolvedStep(template).copy(displaySecondary = "Base seats", displayFooter = "Base footer")
        fun resolve(base: ClocktowerNightStepUi, option: ClocktowerDisplayOption?) =
            resolveClocktowerNumericPlayerDisplay(base, option, 2, false, confirmed, model.contextSnapshot)

        val missing = resolve(step, null)
        assertEquals(step.displayTitle, missing.displayTitle)
        assertEquals("Base seats", missing.displaySecondary)
        assertEquals("Base footer", missing.displayFooter)
        val partial = resolve(step, template)
        assertEquals(template.displayTitle, partial.displayTitle)
        assertEquals("Base seats", partial.displaySecondary)
        assertEquals("Base footer", partial.displayFooter)
        assertEquals(step.explanation, resolve(step.copy(displayFooter = null), null).displayFooter)
        val empty = resolve(step, template.copy(displayTitle = "", displaySecondary = "", displayFooter = ""))
        assertEquals("", empty.displayTitle)
        assertEquals("", empty.displaySecondary)
        assertEquals("", empty.displayFooter)
    }

    @Test
    fun `boolean option supplies text but confirmation supplies the exact proposition`() {
        val model = booleanModel()
        val confirmed = requireNotNull(model.acceptRecommendation(
            model.choices.single { it.recommended }.candidateId, revision,
        ).confirmed)
        val option = pairOption("Chef", 2, 5).copy(
            displayKind = ClocktowerDisplayKind.YesNo,
            displayPrimary = "No", displaySecondary = "2 / 7", displayFooter = null, isTruthful = false,
        )
        val step = unresolvedStep(option).copy(displayFooter = "must not leak through null option footer")
        val resolved = resolveClocktowerBooleanPlayerDisplay(step, option, confirmed, model.contextSnapshot)

        assertEquals("No", resolved.tellPlayer)
        assertEquals("No", resolved.displayPrimary)
        assertEquals(ClocktowerDisplayKind.YesNo, resolved.displayKind)
        assertEquals("2 / 7", resolved.displaySecondary)
        assertNull(resolved.displayFooter)
        assertEquals(false, resolved.selectedInformationTruthful)
        assertSame(confirmed.draft.proposition, resolved.displayProposition)
        assertSame(confirmed, resolved.informationDecisionConfirmation)
        assertSame(model.contextSnapshot, resolved.informationDecisionExpectedSnapshot)
        assertTrue(resolved.displayOptions.isEmpty())
        assertTrue(resolved.recommendedDisplayOptions.isEmpty())
    }

    @Test
    fun `boolean missing option retains base presentation while binding confirmation`() {
        val model = booleanModel()
        val confirmed = requireNotNull(model.acceptRecommendation(
            model.choices.single { it.recommended }.candidateId, revision,
        ).confirmed)
        val step = unresolvedStep(pairOption("Chef", 2, 5)).copy(
            tellPlayer = "Existing instruction", displayKind = ClocktowerDisplayKind.YesNo,
            displayPrimary = "Existing result", displaySecondary = "Existing seats",
            displayFooter = "Existing footer", selectedInformationTruthful = true,
        )
        val resolved = resolveClocktowerBooleanPlayerDisplay(step, null, confirmed, model.contextSnapshot)

        assertEquals(step.tellPlayer, resolved.tellPlayer)
        assertEquals(step.displayKind, resolved.displayKind)
        assertEquals(step.displayTitle, resolved.displayTitle)
        assertEquals(step.displayPrimary, resolved.displayPrimary)
        assertEquals(step.displaySecondary, resolved.displaySecondary)
        assertEquals(step.displayFooter, resolved.displayFooter)
        assertEquals(step.selectedInformationTruthful, resolved.selectedInformationTruthful)
        assertSame(confirmed.draft.proposition, resolved.displayProposition)
        assertSame(confirmed, resolved.informationDecisionConfirmation)
        assertSame(model.contextSnapshot, resolved.informationDecisionExpectedSnapshot)
        assertTrue(resolved.displayOptions.isEmpty())
        assertTrue(resolved.recommendedDisplayOptions.isEmpty())
    }

    @Test
    fun `projection retains a mismatched expected snapshot rather than granting publication`() {
        val model = booleanModel()
        val confirmed = requireNotNull(model.acceptRecommendation(
            model.choices.single { it.recommended }.candidateId, revision,
        ).confirmed)
        val expected = model.contextSnapshot.copy(semanticIdentity = "different interaction")
        val resolved = resolveClocktowerBooleanPlayerDisplay(
            unresolvedStep(pairOption("Chef", 2, 5)), null, confirmed, expected,
        )
        assertSame(expected, resolved.informationDecisionExpectedSnapshot)
        assertFalse(requireNotNull(resolved.informationDecisionConfirmation).authorizes(
            requireNotNull(resolved.informationDecisionExpectedSnapshot), revision,
        ))
        assertFalse(confirmed.authorizes(model.contextSnapshot, revision.copy(playerInputRevision = 22)))
    }

    @Test
    fun `legacy unreliable selection retains recommendation list and existing confirmation`() {
        val model = booleanModel()
        val confirmed = requireNotNull(model.acceptRecommendation(
            model.choices.single { it.recommended }.candidateId, revision,
        ).confirmed)
        val option = pairOption("Chef", 2, 5)
        val step = unresolvedStep(option).copy(
            informationDecisionConfirmation = confirmed,
            informationDecisionExpectedSnapshot = model.contextSnapshot,
        )
        val legacy = resolveClocktowerLegacyUnreliablePlayerDisplay(step, option)
        val ordinary = resolveClocktowerPlayerDisplay(step, option)
        assertEquals(step.recommendedDisplayOptions, legacy.recommendedDisplayOptions)
        assertTrue(legacy.displayOptions.isEmpty())
        assertTrue(ordinary.recommendedDisplayOptions.isEmpty())
        assertEquals(ordinary, legacy.copy(recommendedDisplayOptions = emptyList()))
        assertSame(confirmed, ordinary.informationDecisionConfirmation)
        assertSame(model.contextSnapshot, ordinary.informationDecisionExpectedSnapshot)
    }

    private val revision = InformationDecisionRevision(14, 21)

    private fun numericModel() = prepareEmpathNumberInformationUiModel(
        coordinator = ClocktowerRecommendationCoordinator(), gameId = "display-number",
        phase = ClocktowerPhase.Night, round = 2, sequence = 4, actorSeat = 2,
        subjectSeats = listOf(1, 3), trueValue = 0, reliability = InformationReliability.POISONED,
        recommendationStyle = RecommendationStyle.BALANCED, revision = revision, recommendedValue = 2,
    )

    private fun booleanModel() = prepareBooleanInformationUiModel(
        coordinator = ClocktowerRecommendationCoordinator(), gameId = "display-boolean",
        phase = ClocktowerPhase.Night, round = 2, sequence = 8, actorSeat = 4,
        abilityRole = RoleId("Fortune Teller"), metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
        subjectSeats = listOf(2, 7), trueValue = true, reliability = InformationReliability.POISONED,
        recommendationStyle = RecommendationStyle.BALANCED, revision = revision, recommendedValue = false,
    )

    private fun unresolvedStep(option: ClocktowerDisplayOption) = ClocktowerNightStepUi(
        title = "Washerwoman information",
        actor = null,
        isRealAction = true,
        reason = "storyteller-only reason",
        storytellerAction = "choose information",
        tellPlayer = null,
        explanation = "storyteller-only explanation",
        action = ClocktowerNightAction.None,
        displayKind = ClocktowerDisplayKind.None,
        displayTitle = "unresolved",
        displayPrimary = null,
        displaySecondary = null,
        displayFooter = null,
        displayProposition = null,
        displayOptions = listOf(option),
        recommendedDisplayOptions = listOf(option),
        roleEnName = "Washerwoman",
    )

    private fun pairOption(role: String, first: Int, second: Int) = ClocktowerDisplayOption(
        label = "$role $first/$second",
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = "information",
        displayPrimary = role,
        displaySecondary = "$first / $second",
        displayFooter = null,
        proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.RoleAt(first, RoleId(role)),
                InformationProposition.RoleAt(second, RoleId(role)),
            ),
        ),
    )
}
