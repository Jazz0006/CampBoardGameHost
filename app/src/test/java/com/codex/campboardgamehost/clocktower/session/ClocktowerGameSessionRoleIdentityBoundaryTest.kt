package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerGameSessionRoleIdentityBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `confirmed actual role change commits fact, mechanics and revision together`() {
        val seeded = initialState.copy(
            players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(poisoned = true) else player
            },
        )
        val session = ClocktowerGameSession.createProduction(
            gameId = "a1-confirmed-role-change",
            gameSeed = seeded.seed,
            initialState = seeded,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val beforeTarget = requireNotNull(session.state.gameState.playerAt(1))
        val beforeOther = requireNotNull(session.state.gameState.playerAt(2))
        val draft = roleChange("promote-scarlet", targetSeat = 1)

        val committed = session.commitConfirmedActualRoleChange(
            draft = draft,
            expectedGameStateRevision = 0L,
        )

        val after = requireNotNull(session.state.gameState.playerAt(1))
        assertEquals(RoleId("Imp"), after.actualRole)
        assertEquals(Alignment.EVIL, after.actualAlignment)
        assertEquals(CharacterType.DEMON, after.actualType)
        assertEquals(beforeTarget.shownRole, after.shownRole)
        assertEquals(beforeTarget.alive, after.alive)
        assertEquals(beforeTarget.poisoned, after.poisoned)
        assertEquals(beforeOther, session.state.gameState.playerAt(2))
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1L, session.state.nextTimelineGlobalSequence)
        assertEquals(committed, session.state.actionTimeline.entries.single())
        assertEquals("promote-scarlet", committed.fact.actionId)

        val accepted = session.state
        assertEquals(committed, session.commitConfirmedActualRoleChange(draft, 0L))
        assertEquals(accepted, session.state)
        assertThrows(IllegalStateException::class.java) {
            session.commitConfirmedActualRoleChange(roleChange("other-transition", 1), 1L)
        }
        assertThrows(IllegalStateException::class.java) {
            session.commitConfirmedActualRoleChange(roleChange("stale-transition", 3), 0L)
        }
        assertEquals(accepted, session.state)
    }

    private fun roleChange(actionId: String, targetSeat: Int) = ActionFactDraft.RoleChange(
        actionId = actionId,
        phase = StorytellerPhase.NIGHT,
        round = 2,
        sequence = 1,
        targetSeat = targetSeat,
        role = RoleId("Imp"),
        alignment = Alignment.EVIL,
        type = CharacterType.DEMON,
    )

}
