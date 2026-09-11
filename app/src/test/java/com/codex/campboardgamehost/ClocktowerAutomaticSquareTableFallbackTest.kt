package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerAutomaticSquareTableFallbackTest {
    @Test
    fun `automatic Empath keeps square table when automatic preview is unavailable`() {
        val proposition = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 1,
            subjectSeats = listOf(2, 3),
            value = 1,
        )
        val choices = clocktowerEmpathResultChoices(
            step = step("Empath", proposition, "1"),
            players = listOf(
                player(1, "Empath", Alignment.GOOD, CharacterType.TOWNSFOLK),
                player(2, "Imp", Alignment.EVIL, CharacterType.DEMON),
                player(3, "Washerwoman", Alignment.GOOD, CharacterType.TOWNSFOLK),
            ),
            automaticStorytellerInfo = true,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
            structuredNumberUiModel = null,
        )

        assertEquals(1, choices.size)
        assertEquals(ClocktowerEmpathResultSourceKind.Direct, choices.single().sourceKind)
        assertEquals(1, choices.single().value)
    }

    @Test
    fun `automatic Chef keeps square table when automatic preview is unavailable`() {
        val proposition = InformationProposition.NumericResult(
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = 1,
            subjectSeats = listOf(1, 2, 3),
            value = 1,
        )
        val choices = clocktowerChefResultChoices(
            step = step("Chef", proposition, "1"),
            players = listOf(
                player(1, "Chef", Alignment.GOOD, CharacterType.TOWNSFOLK),
                player(2, "Imp", Alignment.EVIL, CharacterType.DEMON),
                player(3, "Poisoner", Alignment.EVIL, CharacterType.MINION),
            ),
            automaticStorytellerInfo = true,
            automaticDisplayOption = null,
            resultFirstRegistrationCandidates = emptyList(),
            structuredNumberUiModel = null,
        )

        assertEquals(1, choices.size)
        assertEquals(ClocktowerChefResultSourceKind.Direct, choices.single().sourceKind)
        assertEquals(1, choices.single().value)
    }

    private fun step(
        role: String,
        proposition: InformationProposition.NumericResult,
        shown: String,
    ) = ClocktowerNightStepUi(
        title = role,
        actor = null,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = shown,
        explanation = "",
        displayKind = ClocktowerDisplayKind.Number,
        displayPrimary = shown,
        displayProposition = proposition,
        roleEnName = role,
    )

    private fun player(
        seat: Int,
        role: String,
        alignment: Alignment,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
    )
}
