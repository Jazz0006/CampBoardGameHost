package com.codex.campboardgamehost.clocktower.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameStateTest {
    @Test
    fun `playerAt resolves players by stable seat`() {
        val first = player(seat = 1, role = "Chef")
        val second = player(seat = 2, role = "Empath")
        val state = GameState(
            script = ScriptId("trouble_brewing"),
            players = listOf(first, second),
            seed = 42L,
        )

        assertEquals(second, state.playerAt(2))
        assertNull(state.playerAt(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duplicate seats are rejected`() {
        GameState(
            script = ScriptId("trouble_brewing"),
            players = listOf(player(1, "Chef"), player(1, "Empath")),
            seed = 0L,
        )
    }

    private fun player(seat: Int, role: String): PlayerState = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = Alignment.GOOD,
        actualType = CharacterType.TOWNSFOLK,
    )
}
