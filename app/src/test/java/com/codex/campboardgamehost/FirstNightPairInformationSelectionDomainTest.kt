package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstNightPairInformationSelectionDomainTest {
    private val actor = PlayerCard(
        name = "Alice",
        role = Role.Civilian,
        word = "",
    )

    @Test
    fun `impaired pair semantic domain is independent from automatic preference`() {
        val curated = displayOption("curated", truthful = false, pressure = 2)
        val semanticAlternative = displayOption("semantic-alternative", truthful = false, pressure = 2)

        fun buildStep(automaticStorytellerInfo: Boolean) = builder(automaticStorytellerInfo).build(
            roleName = "图书管理员",
            enName = "Librarian",
            tellPlayer = null,
            explanation = "pair information",
            displayOptions = { listOf(curated) },
            automaticSelectionOptions = { listOf(curated, semanticAlternative) },
        )

        val automaticStep = buildStep(automaticStorytellerInfo = true)
        val manualStep = buildStep(automaticStorytellerInfo = false)
        val expectedSemanticDomain = listOf("curated", "semantic-alternative")

        assertEquals(
            expectedSemanticDomain,
            automaticStep.automaticInformationCandidates.map(ClocktowerDisplayOption::label),
        )
        assertEquals(
            expectedSemanticDomain,
            manualStep.automaticInformationCandidates.map(ClocktowerDisplayOption::label),
        )
        assertEquals(
            listOf("curated"),
            manualStep.legacyInformationCandidates.map(ClocktowerDisplayOption::label),
        )
    }

    @Test
    fun `automatic domain falls back to curated candidates when no expanded domain is supplied`() {
        val curated = displayOption("curated", truthful = false, pressure = 2)

        val step = builder().build(
            roleName = "图书管理员",
            enName = "Librarian",
            tellPlayer = null,
            explanation = "pair information",
            displayOptions = { listOf(curated) },
        )

        assertEquals(
            step.legacyInformationCandidates,
            step.automaticInformationCandidates,
        )
    }

    private fun builder(automaticStorytellerInfo: Boolean = true) = ClocktowerInformationStepBuilder(
        cards = listOf(actor),
        language = "en",
        automaticStorytellerInfo = automaticStorytellerInfo,
        text = { _, en -> en },
        roleActor = { actor },
        roleMissingReason = { "missing" },
        abilityStateFor = { _, _ -> AbilityFunctioningState.DRUNK },
        actorIsUnreliable = { _, _ -> true },
        recentMisinformationStreak = { 0 },
    )

    private fun displayOption(
        label: String,
        truthful: Boolean,
        pressure: Int,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = "Librarian information",
        displayPrimary = label,
        displaySecondary = null,
        displayFooter = null,
        isTruthful = truthful,
        misinformationPressure = pressure,
    )
}
