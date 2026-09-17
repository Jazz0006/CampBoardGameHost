package com.codex.campboardgamehost.clocktower.domain

import org.junit.Test

class GameStateTest {
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
