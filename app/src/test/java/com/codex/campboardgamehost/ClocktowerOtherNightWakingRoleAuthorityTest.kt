package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerOtherNightWakingRoleAuthorityTest {
    @Test
    fun `ordinary other night keeps only living current and shown Drunk roles`() {
        val gameState = GameState(
            script = ScriptId("trouble_brewing"),
            seed = 1L,
            players = listOf(
                PlayerState(1, "DeadImp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, alive = false),
                PlayerState(2, "Monk", RoleId("Monk"), Alignment.GOOD, CharacterType.TOWNSFOLK),
                PlayerState(3, "Drunk", RoleId("Drunk"), Alignment.GOOD, CharacterType.OUTSIDER,
                    shownRole = RoleId("Fortune Teller")),
            ),
        )

        assertEquals(
            setOf(RoleId("Monk"), RoleId("Drunk"), RoleId("Fortune Teller")),
            clocktowerNightWakingRoleIds(gameState, includeDead = false),
        )
        assertEquals(
            setOf(RoleId("Imp"), RoleId("Monk"), RoleId("Drunk"), RoleId("Fortune Teller")),
            clocktowerNightWakingRoleIds(gameState, includeDead = true),
        )
    }

}
