package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerVoteTransactionTest {
    @Test
    fun `qualified vote above current high puts nominee on the block and commits ghost vote`() {
        val seats = testSeats(count = 6, deadSeatIds = setOf(2))
        val voteState = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(4),
            selectedVoterSeatIds = setOf(
                ClocktowerSeatId(2),
                ClocktowerSeatId(3),
                ClocktowerSeatId(5),
            ),
        )

        val result = commitClocktowerVoteTransaction(
            voteState = voteState,
            nomineeName = "Player 4",
            executionThreshold = 3,
            highestVoteName = "Player 1",
            highestVoteCount = 2,
        )

        assertEquals(3, result.voteRecord.voteCount)
        assertEquals("Player 4", result.highestVoteName)
        assertEquals(3, result.highestVoteCount)
        assertEquals("Player 4", result.executionCandidateName)
        assertEquals(setOf(ClocktowerSeatId(2)), result.ghostVoteAuthority.spentSeatIds)
        assertTrue(result.voteRecord.voters.first { it.seatId == ClocktowerSeatId(2) }.isGhostVote)
    }

    @Test
    fun `qualified vote equal to current high creates a tie with no execution candidate`() {
        val result = commitClocktowerVoteTransaction(
            voteState = voteState(count = 7, nomineeSeat = 5, selected = setOf(1, 3, 5)),
            nomineeName = "Player 5",
            executionThreshold = 3,
            highestVoteName = "Player 2",
            highestVoteCount = 3,
        )

        assertNull(result.highestVoteName)
        assertEquals(3, result.highestVoteCount)
        assertNull(result.executionCandidateName)
    }

    @Test
    fun `vote below execution threshold keeps current standing but still commits selected ghost votes`() {
        val seats = testSeats(count = 8, deadSeatIds = setOf(7))
        val state = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(6),
            selectedVoterSeatIds = setOf(ClocktowerSeatId(7), ClocktowerSeatId(8)),
        )

        val result = commitClocktowerVoteTransaction(
            voteState = state,
            nomineeName = "Player 6",
            executionThreshold = 4,
            highestVoteName = "Player 3",
            highestVoteCount = 5,
        )

        assertEquals("Player 3", result.highestVoteName)
        assertEquals(5, result.highestVoteCount)
        assertEquals("Player 3", result.executionCandidateName)
        assertEquals(setOf(ClocktowerSeatId(7)), result.ghostVoteAuthority.spentSeatIds)
    }

    @Test
    fun `lower qualifying vote preserves the existing higher execution candidate`() {
        val result = commitClocktowerVoteTransaction(
            voteState = voteState(count = 8, nomineeSeat = 6, selected = setOf(1, 2, 6, 7)),
            nomineeName = "Player 6",
            executionThreshold = 4,
            highestVoteName = "Player 3",
            highestVoteCount = 5,
        )

        assertEquals("Player 3", result.highestVoteName)
        assertEquals(5, result.highestVoteCount)
        assertEquals("Player 3", result.executionCandidateName)
    }

    private fun voteState(
        count: Int,
        nomineeSeat: Int,
        selected: Set<Int>,
    ): ClocktowerTableVoteState = clocktowerTableVoteState(
        seats = testSeats(count),
        nomineeSeatId = ClocktowerSeatId(nomineeSeat),
        selectedVoterSeatIds = selected.mapTo(mutableSetOf(), ::ClocktowerSeatId),
    )

    private fun testSeats(
        count: Int,
        deadSeatIds: Set<Int> = emptySet(),
    ): List<HostSeatPresentation> = (1..count).map { number ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(number),
            playerName = "Player $number",
            isAlive = number !in deadSeatIds,
        )
    }
}
