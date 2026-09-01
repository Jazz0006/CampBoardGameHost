package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerInformationStepBuilderManualAuthorityTest {
    @Test
    fun `manual legal selection domain is available when automatic mode is off and recommendations are empty`() {
        val investigator = ClocktowerRole(
            team = ClocktowerTeam.Townsfolk,
            zhName = "调查员",
            enName = "Investigator",
            zhDescription = "",
            enDescription = "",
        )
        val actor = PlayerCard(
            name = "Player 1",
            role = Role.Civilian,
            word = "",
            clocktowerTeam = ClocktowerTeam.Townsfolk,
            clocktowerRole = investigator,
            clocktowerShownRole = investigator,
        )
        val manualCandidate = ClocktowerDisplayOption(
            label = "manual legal candidate",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Investigator information",
            displayPrimary = "Poisoner",
            displaySecondary = "2   3",
            displayFooter = "One of these two players",
        )
        val builder = ClocktowerInformationStepBuilder(
            cards = listOf(actor),
            language = "en",
            automaticStorytellerInfo = false,
            text = { _, en -> en },
            roleActor = { actor },
            roleMissingReason = { "" },
            abilityStateFor = { _, _ -> AbilityFunctioningState.FUNCTIONING },
            actorIsUnreliable = { _, _ -> false },
            recentMisinformationStreak = { 0 },
        )

        val step = builder.build(
            roleName = "调查员",
            enName = "Investigator",
            tellPlayer = null,
            explanation = "",
            reliableDisplayOptions = { emptyList() },
            legalSelectionOptions = { listOf(manualCandidate) },
        )

        assertEquals(listOf(manualCandidate), step.manualInformationCandidates)
        assertEquals(listOf(manualCandidate), step.automaticInformationCandidates)
    }
}