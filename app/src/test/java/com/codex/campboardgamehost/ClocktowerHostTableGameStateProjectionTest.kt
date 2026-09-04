package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ClocktowerHostTableGameStateProjectionTest {
    @Test
    fun `domain player list order cannot change typed host seat identity`() {
        val players = listOf(
            player(seat = 1, name = "Alice", actualRole = "Washerwoman"),
            player(seat = 2, name = "Bob", actualRole = "Imp"),
            player(seat = 3, name = "Casey", actualRole = "Monk"),
        )
        val reorderedState = gameState(players = listOf(players[2], players[0], players[1]))

        val presentations = reorderedState.toHostSeatPresentations()

        assertEquals(
            listOf(ClocktowerSeatId(1), ClocktowerSeatId(2), ClocktowerSeatId(3)),
            presentations.map(HostSeatPresentation::seatId),
        )
        assertEquals(listOf("Alice", "Bob", "Casey"), presentations.map(HostSeatPresentation::playerName))
    }

    @Test
    fun `actual shown and life state remain separate through host projection`() {
        val state = gameState(
            players = listOf(
                player(
                    seat = 1,
                    name = "Alice",
                    actualRole = "Drunk",
                    shownRole = "Empath",
                    alive = false,
                ),
            ),
        )

        val presentation = state.toHostSeatPresentations().single()

        assertEquals("Drunk", presentation.actualRole?.roleId)
        assertEquals("Empath", presentation.shownRole?.roleId)
        assertFalse(presentation.isAlive)
    }

    @Test
    fun `role display resolver changes labels without changing semantic role ids`() {
        val state = gameState(
            players = listOf(player(seat = 1, name = "Alice", actualRole = "Monk", shownRole = "Monk")),
        )

        val presentation = state.toHostSeatPresentations { roleId -> "显示:${roleId.value}" }.single()

        assertEquals("Monk", presentation.actualRole?.roleId)
        assertEquals("显示:Monk", presentation.actualRole?.displayName)
        assertEquals("Monk", presentation.shownRole?.roleId)
        assertEquals("显示:Monk", presentation.shownRole?.displayName)
    }

    @Test
    fun `non contiguous domain seats fail closed before reaching the table shell`() {
        val malformed = gameState(
            players = listOf(
                player(seat = 1, name = "Alice", actualRole = "Monk"),
                player(seat = 3, name = "Casey", actualRole = "Imp"),
            ),
        )

        try {
            malformed.toHostSeatPresentations()
            fail("Expected non-contiguous host seat topology to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected: a phase adapter must never renumber malformed domain seats.
        }
    }

    @Test
    fun `projected host seats remain usable by shared spatial contract`() {
        val state = gameState(
            players = listOf(
                player(seat = 3, name = "Casey", actualRole = "Imp"),
                player(seat = 1, name = "Alice", actualRole = "Monk"),
                player(seat = 2, name = "Bob", actualRole = "Chef"),
            ),
        )
        val presentations = state.toHostSeatPresentations()

        val frames = hostTableSeatFrames(
            seats = presentations,
            interaction = HostTableInteractionState(
                mode = HostTableInteractionMode.Selection,
                selectableSeatIds = setOf(ClocktowerSeatId(2)),
                selectedSeatIds = listOf(ClocktowerSeatId(2)),
            ),
            layout = layoutFor(presentations.size),
        )

        assertEquals(3, frames.size)
        assertTrue(frames.single { it.seat.seatId == ClocktowerSeatId(2) }.isSelected)
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

    private fun layoutFor(playerCount: Int): HostTableLayout = hostTableLayout(
        playerCount = playerCount,
        constraints = HostTableLayoutConstraints(
            availableWidth = 360f,
            availableHeight = 600f,
            seatCardWidth = 64f,
            seatCardHeight = 50f,
            minimumSafeSeparation = 4f,
            centerWorkspaceWidth = 200f,
            centerWorkspaceHeight = 312f,
        ),
    )
}
