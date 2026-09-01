package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.presentation.PairInformationManualSelection
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerInformationStepBuilderManualPairSelectionTest {
    private val actor = PlayerCard(
        name = "Alice",
        role = Role.Civilian,
        word = "",
    )

    @Test
    fun `builder carries precomputed manual pair model without rebuilding or narrowing it`() {
        val legalCandidate = PairInformationLegalCandidate(
            candidateId = "librarian-saint-23",
            outcome = PairInformationOutcome(
                shownRole = RoleId("Saint"),
                targetSeat = 2,
                decoySeat = 3,
            ),
            semanticTruth = SemanticTruth.FALSE,
            registrations = emptyList(),
        )
        val manualModel = PairInformationManualSelection.fromLegalCandidates(listOf(legalCandidate))

        val step = builder().build(
            roleName = "图书管理员",
            enName = "Librarian",
            tellPlayer = null,
            explanation = "pair information",
            manualPairSelection = manualModel,
        )

        assertSame(manualModel, step.manualPairSelection)
        assertSame(legalCandidate, step.manualPairSelection?.allCandidates?.single())
    }

    private fun builder() = ClocktowerInformationStepBuilder(
        cards = listOf(actor),
        language = "en",
        automaticStorytellerInfo = true,
        text = { _, en -> en },
        roleActor = { actor },
        roleMissingReason = { "missing" },
        abilityStateFor = { _, _ -> AbilityFunctioningState.DRUNK },
        actorIsUnreliable = { _, _ -> true },
        recentMisinformationStreak = { 0 },
    )
}
