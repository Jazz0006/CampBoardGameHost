package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerGameSessionRoleIdentityBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `actual role boundary changes only actual identity and advances revision once`() {
        val seeded = initialState.copy(
            players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(poisoned = true) else player
            },
        )
        val session = ClocktowerGameSession.createProduction(
            gameId = "d6-1d-actual-role",
            gameSeed = seeded.seed,
            initialState = seeded,
        )
        val beforeTarget = requireNotNull(session.state.gameState.playerAt(1))
        val beforeOther = requireNotNull(session.state.gameState.playerAt(2))

        session.commitActualRoleBoundary(
            seat = 1,
            actualRole = RoleId("Imp"),
            actualAlignment = Alignment.EVIL,
            actualType = CharacterType.DEMON,
        )

        val afterTarget = requireNotNull(session.state.gameState.playerAt(1))
        assertEquals(RoleId("Imp"), afterTarget.actualRole)
        assertEquals(Alignment.EVIL, afterTarget.actualAlignment)
        assertEquals(CharacterType.DEMON, afterTarget.actualType)
        assertEquals(beforeTarget.shownRole, afterTarget.shownRole)
        assertEquals(beforeTarget.alive, afterTarget.alive)
        assertEquals(beforeTarget.poisoned, afterTarget.poisoned)
        assertEquals(beforeOther, session.state.gameState.playerAt(2))
        assertEquals(1L, session.state.gameStateRevision)
    }

    @Test
    fun `shown role boundary changes only shown identity and advances revision once`() {
        val seeded = initialState.copy(
            players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(poisoned = true) else player
            },
        )
        val session = ClocktowerGameSession.createProduction(
            gameId = "d6-1d-shown-role",
            gameSeed = seeded.seed,
            initialState = seeded,
        )
        val beforeTarget = requireNotNull(session.state.gameState.playerAt(1))
        val beforeOther = requireNotNull(session.state.gameState.playerAt(2))

        session.commitShownRoleBoundary(
            seat = 1,
            shownRole = RoleId("Monk"),
        )

        val afterTarget = requireNotNull(session.state.gameState.playerAt(1))
        assertEquals(RoleId("Monk"), afterTarget.shownRole)
        assertEquals(beforeTarget.actualRole, afterTarget.actualRole)
        assertEquals(beforeTarget.actualAlignment, afterTarget.actualAlignment)
        assertEquals(beforeTarget.actualType, afterTarget.actualType)
        assertEquals(beforeTarget.alive, afterTarget.alive)
        assertEquals(beforeTarget.poisoned, afterTarget.poisoned)
        assertEquals(beforeOther, session.state.gameState.playerAt(2))
        assertEquals(1L, session.state.gameStateRevision)
    }
}
