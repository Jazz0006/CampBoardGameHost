package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ClocktowerDayOverviewTableStateTest {
    @Test
    fun `day overview projects canonical typed seats into read only shared table state`() {
        val state = gameState(
            players = listOf(
                player(seat = 3, name = "Casey", actualRole = "Imp"),
                player(seat = 1, name = "Alice", actualRole = "Drunk", shownRole = "Empath"),
                player(seat = 2, name = "Bob", actualRole = "Chef", alive = false),
            ),
        )

        val tableState = clocktowerDayOverviewTableState(state)

        assertEquals(HostTableInteractionMode.ReadOnly, tableState.interaction.mode)
        assertEquals(emptySet<ClocktowerSeatId>(), tableState.interaction.selectableSeatIds)
        assertEquals(
            listOf(ClocktowerSeatId(1), ClocktowerSeatId(2), ClocktowerSeatId(3)),
            tableState.seats.map(HostSeatPresentation::seatId),
        )
        assertEquals(listOf("Alice", "Bob", "Casey"), tableState.seats.map(HostSeatPresentation::playerName))
        assertFalse(tableState.seats.single { it.seatId == ClocktowerSeatId(2) }.isAlive)
        assertEquals("Drunk", tableState.seats.first().actualRole?.roleId)
        assertEquals("Empath", tableState.seats.first().shownRole?.roleId)
    }

    private fun gameState(players: List<PlayerState>): GameState = GameState(
        script = ScriptId("trouble_brewing"),
        players = players,
        seed = 42L,
    )

    private fun player(
        seat: Int,
        name: String,
        actualRole: String,
        shownRole: String? = actualRole,
        alive: Boolean = true,
    ): PlayerState = PlayerState(
        seat = seat,
        name = name,
        actualRole = RoleId(actualRole),
        actualAlignment = if (actualRole == "Imp") Alignment.EVIL else Alignment.GOOD,
        actualType = if (actualRole == "Imp") CharacterType.DEMON else CharacterType.TOWNSFOLK,
        shownRole = shownRole?.let(::RoleId),
        alive = alive,
        poisoned = false,
    )
}
