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

class ClocktowerChefSquareTablePresentationTest {
    @Test
    fun `actual evil is information hint while recluse uses a distinct badge`() {
        val visualEvil = clocktowerChefSeatVisual(
            seatNumber = 2,
            actorSeat = 1,
            actualEvilSeats = setOf(2, 3),
            recluseSeat = 4,
            effectivePairSeats = emptySet(),
            language = "zh",
        )
        val visualRecluse = clocktowerChefSeatVisual(
            seatNumber = 4,
            actorSeat = 1,
            actualEvilSeats = setOf(2, 3),
            recluseSeat = 4,
            effectivePairSeats = emptySet(),
            language = "zh",
        )
        val visualActor = clocktowerChefSeatVisual(
            seatNumber = 1,
            actorSeat = 1,
            actualEvilSeats = setOf(2, 3),
            recluseSeat = 4,
            effectivePairSeats = emptySet(),
            language = "zh",
        )

        assertEquals(ClocktowerSquareTableSeatState.HighlightedInformation, visualEvil.state)
        assertEquals(null, visualEvil.badge)
        assertEquals(ClocktowerSquareTableSeatState.Neutral, visualRecluse.state)
        assertEquals("隐", visualRecluse.badge)
        assertTrue(visualActor.isCurrentActor)
        assertFalse(visualEvil.isCurrentActor)
    }

    @Test
    fun `recluse registration witness highlights only seats participating in counted evil pairs`() {
        val players = players()
        val option = option(value = 2, recluseRegistersEvil = true)

        assertEquals(setOf(2, 3, 4), clocktowerChefEffectivePairSeats(players, option, value = 2))
        val recluseVisual = clocktowerChefSeatVisual(
            seatNumber = 4,
            actorSeat = 1,
            actualEvilSeats = clocktowerChefActualEvilSeats(players),
            recluseSeat = clocktowerChefRecluseSeat(players),
            effectivePairSeats = clocktowerChefEffectivePairSeats(players, option, 2),
            language = "zh",
        )
        assertEquals(ClocktowerSquareTableSeatState.SelectedHighlighted, recluseVisual.state)
        assertEquals("隐", recluseVisual.badge)
    }

    @Test
    fun `spy registering good removes the spy from the selected result but keeps static evil identity available`() {
        val players = listOf(
            player(1, "Chef", Alignment.GOOD, CharacterType.TOWNSFOLK),
            player(2, "Imp", Alignment.EVIL, CharacterType.DEMON),
            player(3, "Spy", Alignment.EVIL, CharacterType.MINION),
            player(4, "Washerwoman", Alignment.GOOD, CharacterType.TOWNSFOLK),
        )
        val option = option(value = 0, spyRegistersGood = true)

        assertEquals(setOf(2), clocktowerChefEffectiveEvilSeats(players, option))
        assertEquals(emptySet<Int>(), clocktowerChefEffectivePairSeats(players, option, value = 0))
        assertEquals(setOf(2, 3), clocktowerChefActualEvilSeats(players))
    }

    @Test
    fun `mismatched witness fails closed instead of inventing selected players`() {
        val players = players()
        val option = option(value = 1, recluseRegistersEvil = true)

        assertEquals(emptySet<Int>(), clocktowerChefEffectivePairSeats(players, option, value = 1))
    }

    private fun players() = listOf(
        player(1, "Chef", Alignment.GOOD, CharacterType.TOWNSFOLK),
        player(2, "Imp", Alignment.EVIL, CharacterType.DEMON),
        player(3, "Poisoner", Alignment.EVIL, CharacterType.MINION),
        player(4, "Recluse", Alignment.GOOD, CharacterType.OUTSIDER),
        player(5, "Washerwoman", Alignment.GOOD, CharacterType.TOWNSFOLK),
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
        spyRegistersGood: Boolean? = null,
        recluseRegistersEvil: Boolean? = null,
    ) = ClocktowerDisplayOption(
        label = value.toString(),
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chef information",
        displayPrimary = value.toString(),
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.NumericResult(
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = 1,
            subjectSeats = listOf(1, 2, 3, 4, 5),
            value = value,
        ),
        spyRegistersGood = spyRegistersGood,
        recluseRegistersEvil = recluseRegistersEvil,
    )
}
