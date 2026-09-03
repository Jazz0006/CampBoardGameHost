package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerTableVoteStateTest {
    @Test
    fun `clockwise vote order starts after nominee and ends with nominee`() {
        listOf(5, 8, 12, 15).forEach { playerCount ->
            val seats = testSeats(playerCount)
            val nominee = ClocktowerSeatId((playerCount / 2).coerceAtLeast(1))

            val state = clocktowerTableVoteState(
                seats = seats,
                nomineeSeatId = nominee,
            )

            assertEquals(playerCount, state.orderedSeatIds.size)
            assertEquals(nominee, state.orderedSeatIds.last())
            assertEquals(
                ClocktowerSeatId((nominee.number % playerCount) + 1),
                state.orderedSeatIds.first(),
            )
            assertEquals((1..playerCount).map(::ClocktowerSeatId).toSet(), state.orderedSeatIds.toSet())
        }
    }

    @Test
    fun `dead seat is selectable until its ghost vote authority is spent`() {
        val seats = testSeats(8).map { seat ->
            if (seat.seatId == ClocktowerSeatId(4)) seat.copy(isAlive = false) else seat
        }
        val available = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(6),
        )

        assertTrue(ClocktowerSeatId(3) in available.selectableSeatIds)
        assertTrue(ClocktowerSeatId(4) in available.selectableSeatIds)
        assertTrue(ClocktowerSeatId(6) in available.selectableSeatIds)

        val pending = available.togglePendingVoter(ClocktowerSeatId(4))
        assertEquals(setOf(ClocktowerSeatId(4)), pending.selectedVoterSeatIds)
        assertTrue(pending.ghostVoteAuthority.spentSeatIds.isEmpty())

        val spentAuthority = available.ghostVoteAuthority.confirmVote(
            selectedVoterSeatIds = setOf(ClocktowerSeatId(4)),
            seats = seats,
        )
        val spent = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(6),
            ghostVoteAuthority = spentAuthority,
        )
        assertFalse(ClocktowerSeatId(4) in spent.selectableSeatIds)
        assertEquals(spent, spent.togglePendingVoter(ClocktowerSeatId(4)))
    }

    @Test
    fun `tapping an eligible seat toggles one pending vote without duplicates`() {
        val state = clocktowerTableVoteState(
            seats = testSeats(8),
            nomineeSeatId = ClocktowerSeatId(6),
        )

        val selected = state
            .togglePendingVoter(ClocktowerSeatId(7))
            .togglePendingVoter(ClocktowerSeatId(2))

        assertEquals(setOf(ClocktowerSeatId(7), ClocktowerSeatId(2)), selected.selectedVoterSeatIds)
        assertEquals(2, selected.voteCount)
        assertEquals(
            listOf(ClocktowerSeatId(7), ClocktowerSeatId(2)),
            selected.interaction.selectedSeatIds,
        )
        assertEquals(HostTableInteractionMode.MultiSelection, selected.interaction.mode)
        assertEquals(setOf(ClocktowerSeatId(6)), selected.interaction.highlightedSeatIds)

        val deselected = selected.togglePendingVoter(ClocktowerSeatId(7))
        assertEquals(setOf(ClocktowerSeatId(2)), deselected.selectedVoterSeatIds)
        assertEquals(1, deselected.voteCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unknown nominee is rejected instead of silently reindexing`() {
        clocktowerTableVoteState(
            seats = testSeats(8),
            nomineeSeatId = ClocktowerSeatId(9),
        )
    }

    private fun testSeats(count: Int): List<HostSeatPresentation> =
        (1..count).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = true,
            )
        }
}
