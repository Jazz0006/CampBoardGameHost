package com.codex.campboardgamehost

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerGhostVoteAuthorityTest {
    @Test
    fun `dead player is eligible until confirmed ghost vote spends their stable seat`() {
        val seats = testSeats(
            aliveSeatIds = setOf(1, 3),
            count = 4,
        )
        val deadSeat = ClocktowerSeatId(2)
        val otherDeadSeat = ClocktowerSeatId(4)
        val authority = ClocktowerGhostVoteAuthority()

        val pending = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(3),
            ghostVoteAuthority = authority,
        ).togglePendingVoter(deadSeat)

        assertTrue(deadSeat in pending.selectedVoterSeatIds)
        assertTrue(authority.spentSeatIds.isEmpty())

        val confirmed = authority.confirmVote(
            selectedVoterSeatIds = pending.selectedVoterSeatIds,
            seats = seats,
        )

        assertEquals(setOf(deadSeat), confirmed.spentSeatIds)
        assertFalse(confirmed.canVote(deadSeat, seats))
        assertTrue(confirmed.canVote(otherDeadSeat, seats))
        assertTrue(confirmed.canVote(ClocktowerSeatId(1), seats))

        val nextVote = clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = ClocktowerSeatId(3),
            ghostVoteAuthority = confirmed,
        )
        assertFalse(deadSeat in nextVote.selectableSeatIds)
        assertTrue(otherDeadSeat in nextVote.selectableSeatIds)
    }

    @Test
    fun `alive votes never consume ghost vote authority and confirm is idempotent`() {
        val seats = testSeats(
            aliveSeatIds = setOf(1, 3),
            count = 4,
        )
        val authority = ClocktowerGhostVoteAuthority()

        val afterAliveVote = authority.confirmVote(
            selectedVoterSeatIds = setOf(ClocktowerSeatId(1), ClocktowerSeatId(3)),
            seats = seats,
        )
        assertTrue(afterAliveVote.spentSeatIds.isEmpty())

        val afterDeadVote = afterAliveVote.confirmVote(
            selectedVoterSeatIds = setOf(ClocktowerSeatId(2)),
            seats = seats,
        )
        val afterDuplicateConfirm = afterDeadVote.confirmVote(
            selectedVoterSeatIds = setOf(ClocktowerSeatId(2)),
            seats = seats,
        )
        assertEquals(setOf(ClocktowerSeatId(2)), afterDuplicateConfirm.spentSeatIds)
    }

    @Test
    fun `persistence round trip preserves spent seats and missing legacy payload defaults empty`() {
        val authority = ClocktowerGhostVoteAuthority(
            spentSeatIds = setOf(ClocktowerSeatId(2), ClocktowerSeatId(5)),
        )
        val json = JSONObject().apply {
            put(
                ClocktowerGhostVoteAuthorityPersistence.ROOT_KEY,
                ClocktowerGhostVoteAuthorityPersistence.encode(authority),
            )
        }

        assertEquals(authority, ClocktowerGhostVoteAuthorityPersistence.decode(json))
        assertEquals(
            ClocktowerGhostVoteAuthority(),
            ClocktowerGhostVoteAuthorityPersistence.decode(JSONObject()),
        )
    }

    private fun testSeats(
        aliveSeatIds: Set<Int>,
        count: Int,
    ): List<HostSeatPresentation> = (1..count).map { number ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(number),
            playerName = "Player $number",
            isAlive = number in aliveSeatIds,
        )
    }
}
