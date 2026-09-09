package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerChambermaidStepMaterializerTest {
    private val actor = PlayerCard("Alice", Role.Civilian, "")
    private val presentation = ClocktowerChambermaidSelectionPresentation(
        selectedTargetNames = listOf("Bob", "Carol"),
        subjectSeats = listOf(2, 3),
    )
    private val proposition = InformationProposition.NumericResult(
        metric = NumericMetric.PLAYERS_WAKING_FOR_ABILITY,
        sourceSeat = 1,
        subjectSeats = listOf(2, 3),
        value = 1,
    )
    private val content = ClocktowerChambermaidStepContent(
        explanation = "Chambermaid explanation",
        displayFooter = "Checking these two players",
        hostInstruction = "Wake the Chambermaid",
    )

    @Test
    fun `reliable Chambermaid step preserves prepared presentation without invoking options`() {
        var optionCalls = 0

        val step = builder(actor = actor, unreliable = false).buildChambermaidStep(
            content = content,
            result = "1",
            presentation = presentation,
            displayProposition = proposition,
            displayOptions = {
                optionCalls += 1
                listOf(option("unused"))
            },
        )

        assertEquals("Chambermaid", step.title)
        assertEquals(actor, step.actor)
        assertTrue(step.isRealAction)
        assertEquals(ClocktowerNightAction.Chambermaid, step.action)
        assertEquals(ClocktowerDisplayKind.Number, step.displayKind)
        assertEquals("1", step.tellPlayer)
        assertEquals("1", step.displayPrimary)
        assertEquals("2   3", step.displaySecondary)
        assertEquals(content.displayFooter, step.displayFooter)
        assertEquals(content.hostInstruction, step.storytellerAction)
        assertEquals(content.explanation, step.explanation)
        assertEquals(proposition, step.displayProposition)
        assertEquals(0, optionCalls)
    }

    @Test
    fun `unreliable Chambermaid step lazily forwards options`() {
        var optionCalls = 0
        val recommended = option("recommended")

        val step = builder(actor = actor, unreliable = true).buildChambermaidStep(
            content = content,
            result = "1",
            presentation = presentation,
            displayProposition = proposition,
            displayOptions = {
                optionCalls += 1
                listOf(recommended)
            },
        )

        assertEquals(1, optionCalls)
        assertNull(step.tellPlayer)
        assertEquals(ClocktowerDisplayKind.None, step.displayKind)
        assertEquals(listOf(recommended), step.recommendedDisplayOptions)
        assertEquals(listOf(recommended), step.legacyInformationCandidates)
    }

    @Test
    fun `missing Chambermaid remains a placeholder without invoking options`() {
        var optionCalls = 0

        val step = builder(actor = null, unreliable = false).buildChambermaidStep(
            content = content,
            result = null,
            presentation = presentation.copy(selectedTargetNames = emptyList(), subjectSeats = emptyList()),
            displayProposition = null,
            displayOptions = {
                optionCalls += 1
                emptyList()
            },
        )

        assertNull(step.actor)
        assertFalse(step.isRealAction)
        assertEquals("missing Chambermaid", step.reason)
        assertNull(step.tellPlayer)
        assertEquals(0, optionCalls)
    }

    private fun builder(
        actor: PlayerCard?,
        unreliable: Boolean,
    ) = ClocktowerInformationStepBuilder(
        cards = listOfNotNull(actor),
        language = "en",
        automaticStorytellerInfo = true,
        text = { _, en -> en },
        roleActor = { actor },
        roleMissingReason = { "missing $it" },
        abilityStateFor = { _, _ ->
            if (unreliable) AbilityFunctioningState.DRUNK else AbilityFunctioningState.FUNCTIONING
        },
        actorIsUnreliable = { _, _ -> unreliable },
        recentMisinformationStreak = { 0 },
    )

    private fun option(label: String) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chambermaid information",
        displayPrimary = "0",
        displaySecondary = "2   3",
        displayFooter = content.displayFooter,
    )
}
