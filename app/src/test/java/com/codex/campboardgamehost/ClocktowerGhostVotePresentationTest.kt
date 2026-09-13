package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerGhostVotePresentationTest {
    @Test
    fun `ghost vote marker derives from durable seat authority only for dead unspent seat`() {
        val seatId = ClocktowerSeatId(2)
        val unspent = ClocktowerGhostVoteAuthority()
        val spent = ClocktowerGhostVoteAuthority(spentSeatIds = setOf(seatId))

        assertTrue(hostSeatHasUnspentGhostVote(seatId, isAlive = false, ghostVoteAuthority = unspent))
        assertFalse(hostSeatHasUnspentGhostVote(seatId, isAlive = false, ghostVoteAuthority = spent))
        assertFalse(hostSeatHasUnspentGhostVote(seatId, isAlive = true, ghostVoteAuthority = unspent))
        assertFalse(hostSeatHasUnspentGhostVote(seatId, isAlive = false, ghostVoteAuthority = null))
    }

    @Test
    fun `night storyteller seat carries dead and unspent ghost vote presentation together`() {
        val deadCard = PlayerCard(
            name = "Dana",
            role = Role.Civilian,
            word = "",
            eliminatedRound = 2,
        )
        val seat = deadCard.toStorytellerHostSeatPresentation(
            seatNumber = 3,
            language = "en",
            ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
        )

        assertFalse(seat.isAlive)
        assertTrue(seat.hasUnspentGhostVote)
    }
}
