package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPlayerDisplayResolutionTest {
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
    }

    @Test
    fun `selected unreliable role metadata overrides the truthful step anchor`() {
        val selected = ClocktowerDisplayOption(
            label = "alternate appearance",
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = "Ravenkeeper",
            displayPrimary = "小恶魔",
            displaySecondary = null,
            displayFooter = null,
            presentationSubjectSeats = listOf(5),
            presentationRoleId = "Imp",
            proposition = null,
        )
        val truthful = InformationProposition.RoleAt(5, RoleId("Chef"))
        val resolved = resolveClocktowerPlayerDisplay(
            unresolvedStep(selected).copy(
                roleEnName = "Ravenkeeper",
                displayProposition = truthful,
            ),
            selected,
        )
        assertEquals("小恶魔", resolved.displayPrimary)
        assertEquals(InformationProposition.RoleAt(5, RoleId("Imp")), resolved.displayProposition)
        // A conflicting explicitly selected target never silently inherits the truthful anchor.
        assertNull(resolveClocktowerPlayerDisplay(
            unresolvedStep(selected).copy(displayProposition = truthful),
            selected.copy(presentationSubjectSeats = listOf(4)),
        ).displayProposition)
    }

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
