package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstNightPairInformationPoolLegalityTest {
    @Test
    fun `investigator pool drops zero minions but retains pair clue`() {
        val pair = pairOption(
            title = "Investigator information",
            shownRole = "Poisoner",
            firstSeat = 2,
            secondSeat = 3,
        )
        val zero = zeroOption(
            title = "Investigator information",
            label = "No Minions",
            shownRole = "Poisoner",
        )

        val pool = unifiedFirstNightInformationPool(
            options = listOf(pair, zero),
            familyId = "Investigator",
            automaticStyle = RecommendationStyle.BALANCED,
        )

        assertEquals(listOf(pair), pool.rankedCandidates.map { it.payload })
    }

    @Test
    fun `librarian pool retains zero outsiders alongside pair clue`() {
        val pair = pairOption(
            title = "Librarian information",
            shownRole = "Saint",
            firstSeat = 2,
            secondSeat = 3,
        )
        val zero = zeroOption(
            title = "Librarian information",
            label = "No Outsiders",
            shownRole = "Saint",
        )

        val pool = unifiedFirstNightInformationPool(
            options = listOf(pair, zero),
            familyId = "Librarian",
            automaticStyle = RecommendationStyle.BALANCED,
        )

        assertEquals(setOf(pair, zero), pool.rankedCandidates.map { it.payload }.toSet())
    }

    private fun pairOption(
        title: String,
        shownRole: String,
        firstSeat: Int,
        secondSeat: Int,
    ) = ClocktowerDisplayOption(
        label = "$shownRole: $firstSeat / $secondSeat",
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = title,
        displayPrimary = shownRole,
        displaySecondary = "$firstSeat / $secondSeat",
        displayFooter = null,
        proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.RoleAt(firstSeat, RoleId(shownRole)),
                InformationProposition.RoleAt(secondSeat, RoleId(shownRole)),
            ),
        ),
    )

    private fun zeroOption(
        title: String,
        label: String,
        shownRole: String,
    ) = ClocktowerDisplayOption(
        label = label,
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = title,
        displayPrimary = label,
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.AllOf(
            listOf(InformationProposition.RoleInPlay(RoleId(shownRole), false)),
        ),
    )
}
