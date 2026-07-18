package com.codex.campboardgamehost.clocktower.rules

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Test

class FixedInformationEvaluatorTest {
    @Test
    fun `chef counts one adjacent evil pair`() {
        val players = players(evilSeats = setOf(7, 8))

        assertEquals(1, FixedInformationEvaluator.chefEvilPairs(players))
    }

    @Test
    fun `chef treats last and first seats as adjacent`() {
        val players = players(evilSeats = setOf(1, 8))

        assertEquals(1, FixedInformationEvaluator.chefEvilPairs(players))
    }

    @Test
    fun `chef supports per ability registration decisions`() {
        val players = players(evilSeats = setOf(8))
        val registeredEvilSeats = setOf(7, 8)

        val result = FixedInformationEvaluator.chefEvilPairs(players) {
            it.seat in registeredEvilSeats
        }

        assertEquals(1, result)
    }

    @Test
    fun `living neighbors skip dead players and preserve circular seating`() {
        val players = players(evilSeats = emptySet(), deadSeats = setOf(2))

        val neighbors = FixedInformationEvaluator.livingNeighbors(players, sourceSeat = 3)

        assertEquals(listOf(1, 4), neighbors.map { it.seat })
    }

    @Test
    fun `empath counts registered evil among living neighbors`() {
        val players = players(evilSeats = setOf(2), deadSeats = setOf(4))

        val result = FixedInformationEvaluator.empathEvilNeighborCount(
            players = players,
            sourceSeat = 3,
        )

        assertEquals(1, result)
    }

    private fun players(
        evilSeats: Set<Int>,
        deadSeats: Set<Int> = emptySet(),
    ): List<PlayerState> = (1..8).map { seat ->
        val evil = seat in evilSeats
        PlayerState(
            seat = seat,
            name = "Player $seat",
            actualRole = RoleId(if (evil) "Imp-$seat" else "Townsfolk-$seat"),
            actualAlignment = if (evil) Alignment.EVIL else Alignment.GOOD,
            actualType = if (evil) CharacterType.DEMON else CharacterType.TOWNSFOLK,
            alive = seat !in deadSeats,
        )
    }
}
