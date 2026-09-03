package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerVoteHistoryRecordTest {
    @Test
    fun `confirmed vote records voter identities in clockwise count order and marks ghost votes`() {
        val seats = (1..8).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = number !in setOf(2, 7),
            )
        }
        val state = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(6),
            selectedVoterSeatIds = setOf(
                ClocktowerSeatId(5),
                ClocktowerSeatId(2),
                ClocktowerSeatId(7),
            ),
        )

        val record = state.confirmedVoteRecord()

        assertEquals(3, record.voteCount)
        assertEquals(
            listOf(ClocktowerSeatId(7), ClocktowerSeatId(2), ClocktowerSeatId(5)),
            record.voters.map { voter -> voter.seatId },
        )
        assertEquals(
            listOf("Player 7", "Player 2", "Player 5"),
            record.voters.map { voter -> voter.playerName },
        )
        assertTrue(record.voters[0].isGhostVote)
        assertTrue(record.voters[1].isGhostVote)
        assertFalse(record.voters[2].isGhostVote)
        assertEquals(
            "#7 Player 7（幽灵票）、#2 Player 2（幽灵票）、#5 Player 5",
            record.voterDetail(
                playerLabel = { playerName -> "#${playerName.substringAfter("Player ")} $playerName" },
                ghostVoteSuffix = "（幽灵票）",
                noVotesLabel = "无人投票",
            ),
        )
    }

    @Test
    fun `confirmed vote keeps zero voter detail rather than inventing participants`() {
        val seats = (1..5).map { number ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(number),
                playerName = "Player $number",
                isAlive = true,
            )
        }
        val state = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(3),
        )

        val record = state.confirmedVoteRecord()

        assertEquals(0, record.voteCount)
        assertTrue(record.voters.isEmpty())
        assertEquals(
            "No votes",
            record.voterDetail(
                playerLabel = { it },
                ghostVoteSuffix = " (ghost vote)",
                noVotesLabel = "No votes",
            ),
        )
    }
}
