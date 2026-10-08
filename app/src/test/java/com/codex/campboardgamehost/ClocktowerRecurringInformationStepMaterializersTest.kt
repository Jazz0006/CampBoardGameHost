package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerRecurringInformationStepMaterializersTest {
    private val actor = PlayerCard("Alice", Role.Civilian, "")
    private val cards = listOf(actor, PlayerCard("Bob", Role.Civilian, ""), PlayerCard("Carol", Role.Civilian, ""))

    @Test
    fun `Empath keeps prepared result and defers candidate calculation for a reliable actor`() {
        var optionCalls = 0
        val step = clocktowerEmpathStepMaterializer(
            builder = builder(AbilityFunctioningState.FUNCTIONING),
            content = ClocktowerEmpathStepContent("1", null, 0, null, null),
            text = { _, en -> en },
            displayOptions = {
                optionCalls++
                emptyList()
            },
            legalSelectionOptions = { emptyList() },
        ).build()

        assertEquals("1", step.tellPlayer)
        assertEquals(ClocktowerDisplayKind.Number, step.displayKind)
        assertEquals(0, step.previousShownNumber)
        assertEquals(0, optionCalls)
    }

    @Test
    fun `Fortune Teller retains the selected seats in unreliable options`() {
        val proposition = InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, 1, listOf(2, 3), true,
        )
        val step = clocktowerFortuneTellerStepMaterializer(
            builder = builder(AbilityFunctioningState.POISONED),
            content = ClocktowerFortuneTellerStepContent(
                result = "Yes",
                matched = true,
                selectedNames = listOf("Bob", "Carol"),
                displaySecondary = "2   3",
                proposition = proposition,
                recluseRegistrationKey = "registration",
            ),
            cards = cards,
            text = { _, en -> en },
            legalSelectionOptions = { emptyList() },
        ).build()

        assertNull(step.tellPlayer)
        assertNull(step.recluseRegistrationKey)
        assertEquals(listOf(true, false), step.recommendedDisplayOptions.map { it.isTruthful })
        assertEquals(listOf(2, 3),
            (step.recommendedDisplayOptions.first().proposition as InformationProposition.BooleanResult).subjectSeats)
    }

    private fun builder(state: AbilityFunctioningState) = ClocktowerInformationStepBuilder(
        cards = cards,
        language = "en",
        automaticStorytellerInfo = true,
        text = { _, en -> en },
        roleActor = { actor },
        roleMissingReason = { "missing $it" },
        abilityStateFor = { _, _ -> state },
        actorIsUnreliable = { _, _ -> state != AbilityFunctioningState.FUNCTIONING },
        recentMisinformationStreak = { 0 },
    )
}
