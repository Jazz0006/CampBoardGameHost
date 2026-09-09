package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerEmpathSquareTablePresentationTest {
    @Test
    fun `scope comes from typed subject seats and is not reconstructed from adjacency`() {
        val proposition = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 1,
            subjectSeats = listOf(2, 5),
            value = 1,
        )

        assertEquals(setOf(2, 5), clocktowerEmpathScopeSeats(proposition, seatCount = 6))
    }

    @Test
    fun `wrong metric or invalid subject seat fails closed`() {
        val wrongMetric = InformationProposition.NumericResult(
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = 1,
            subjectSeats = listOf(2, 5),
            value = 1,
        )
        val invalidSeat = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 1,
            subjectSeats = listOf(2, 9),
            value = 1,
        )

        assertEquals(emptySet<Int>(), clocktowerEmpathScopeSeats(wrongMetric, seatCount = 6))
        assertEquals(emptySet<Int>(), clocktowerEmpathScopeSeats(invalidSeat, seatCount = 6))
    }

    @Test
    fun `scope evil recluse and actor remain independent visual dimensions`() {
        val evil = clocktowerEmpathSeatVisual(
            seatNumber = 2,
            actorSeat = 1,
            scopeSeats = setOf(2, 5),
            actualEvilSeats = setOf(2, 3),
            recluseSeat = 5,
            contributingSeats = emptySet(),
            language = "zh",
        )
        val recluse = clocktowerEmpathSeatVisual(
            seatNumber = 5,
            actorSeat = 1,
            scopeSeats = setOf(2, 5),
            actualEvilSeats = setOf(2, 3),
            recluseSeat = 5,
            contributingSeats = emptySet(),
            language = "zh",
        )
        val actor = clocktowerEmpathSeatVisual(
            seatNumber = 1,
            actorSeat = 1,
            scopeSeats = setOf(2, 5),
            actualEvilSeats = setOf(2, 3),
            recluseSeat = 5,
            contributingSeats = emptySet(),
            language = "zh",
        )

        assertEquals(ClocktowerSquareTableSeatState.HighlightedInformation, evil.state)
        assertEquals("邻", evil.badge)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, recluse.state)
        assertEquals("邻·隐", recluse.badge)
        assertTrue(actor.isCurrentActor)
        assertFalse(evil.isCurrentActor)
    }

    @Test
    fun `recluse registering evil contributes only when it is in authoritative scope`() {
        val players = players()
        val option = option(
            value = 2,
            subjectSeats = listOf(2, 5),
            recluseRegistersEvil = true,
        )

        assertEquals(
            setOf(2, 5),
            clocktowerEmpathContributingSeats(players, option, value = 2),
        )
    }

    @Test
    fun `spy registering good is removed from contribution but remains actual evil hint`() {
        val players = listOf(
            player(1, "Empath", Alignment.GOOD, CharacterType.TOWNSFOLK),
            player(2, "Spy", Alignment.EVIL, CharacterType.MINION),
            player(3, "Washerwoman", Alignment.GOOD, CharacterType.TOWNSFOLK),
        )
        val option = option(
            value = 0,
            subjectSeats = listOf(2, 3),
            spyRegistersGood = true,
        )

        assertEquals(emptySet<Int>(), clocktowerEmpathContributingSeats(players, option, value = 0))
        assertEquals(setOf(2), clocktowerEmpathActualEvilSeats(players))
    }

    @Test
    fun `mismatched witness value fails closed instead of inventing contributing neighbour`() {
        val players = players()
        val option = option(
            value = 1,
            subjectSeats = listOf(2, 5),
            recluseRegistersEvil = true,
        )

        assertEquals(emptySet<Int>(), clocktowerEmpathContributingSeats(players, option, value = 1))
    }

    @Test
    fun `single result never exposes contribution as a selection`() {
        val only = ClocktowerEmpathResultChoice(
            key = "one",
            value = 1,
            sourceKind = ClocktowerEmpathResultSourceKind.Direct,
            scopeSeats = setOf(2, 5),
            contributingSeats = setOf(2),
        )

        assertEquals(emptySet<Int>(), clocktowerEmpathDisplayedContributionSeats(listOf(only), "one"))
    }

    @Test
    fun `multiple results expose contribution only for currently selected result`() {
        val zero = ClocktowerEmpathResultChoice(
            key = "zero",
            value = 0,
            sourceKind = ClocktowerEmpathResultSourceKind.DisplayOption,
            scopeSeats = setOf(2, 5),
            contributingSeats = emptySet(),
        )
        val two = ClocktowerEmpathResultChoice(
            key = "two",
            value = 2,
            sourceKind = ClocktowerEmpathResultSourceKind.DisplayOption,
            scopeSeats = setOf(2, 5),
            contributingSeats = setOf(2, 5),
        )

        assertEquals(setOf(2, 5), clocktowerEmpathDisplayedContributionSeats(listOf(zero, two), "two"))
        assertEquals(emptySet<Int>(), clocktowerEmpathDisplayedContributionSeats(listOf(zero, two), "zero"))
    }

    private fun players() = listOf(
        player(1, "Empath", Alignment.GOOD, CharacterType.TOWNSFOLK),
        player(2, "Imp", Alignment.EVIL, CharacterType.DEMON),
        player(3, "Washerwoman", Alignment.GOOD, CharacterType.TOWNSFOLK),
        player(4, "Chef", Alignment.GOOD, CharacterType.TOWNSFOLK),
        player(5, "Recluse", Alignment.GOOD, CharacterType.OUTSIDER),
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

    private fun option(
        value: Int,
        subjectSeats: List<Int>,
        spyRegistersGood: Boolean? = null,
        recluseRegistersEvil: Boolean? = null,
    ) = ClocktowerDisplayOption(
        label = value.toString(),
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Empath information",
        displayPrimary = value.toString(),
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = 1,
            subjectSeats = subjectSeats,
            value = value,
        ),
        spyRegistersGood = spyRegistersGood,
        recluseRegistersEvil = recluseRegistersEvil,
    )
}
