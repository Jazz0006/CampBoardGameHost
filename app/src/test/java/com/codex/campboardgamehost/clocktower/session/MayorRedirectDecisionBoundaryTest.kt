package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MayorRedirectDecisionBoundaryTest {
    @Test
    fun `Mayor redirect boundary exposes the complete rules domain and confirms stable candidate IDs`() {
        val revision = StorytellerDecisionRevision(gameStateRevision = 8, playerInputRevision = 3)
        val decision = MayorRedirectDecisionBoundary.create(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = "game-mayor",
                requestId = "night:2:mayor-redirect",
            ),
            revision = revision,
            game = game(
                player(1, "Mayor", CharacterType.TOWNSFOLK),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Poisoner", CharacterType.MINION),
                player(4, "Imp", CharacterType.DEMON),
            ),
        )

        assertEquals(setOf(1, 2, 3), decision.legalTargetSeats)
        assertFalse(4 in decision.legalTargetSeats)

        val candidateId = requireNotNull(decision.candidateIdForSeat(3))
        assertEquals("mayor-redirect-v1|seat:3", candidateId)
        assertEquals(
            MayorRedirectDecisionConfirmation.Confirmed(candidateId = candidateId, targetSeat = 3),
            decision.confirm(candidateId, revision),
        )
    }

    @Test
    fun `Mayor redirect confirmation fails closed for stale or illegal candidates`() {
        val revision = StorytellerDecisionRevision(gameStateRevision = 8, playerInputRevision = 3)
        val decision = MayorRedirectDecisionBoundary.create(
            requestIdentity = StorytellerDecisionRequestIdentity(
                gameId = "game-mayor",
                requestId = "night:2:mayor-redirect",
            ),
            revision = revision,
            game = game(
                player(1, "Mayor", CharacterType.TOWNSFOLK),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Imp", CharacterType.DEMON),
            ),
        )

        assertEquals(
            MayorRedirectDecisionConfirmation.Blocked(MayorRedirectDecisionBlockReason.ILLEGAL_CANDIDATE),
            decision.confirm(MayorRedirectDecisionBoundary.candidateIdForSeat(3), revision),
        )
        assertEquals(
            MayorRedirectDecisionConfirmation.Blocked(MayorRedirectDecisionBlockReason.STALE_CONTEXT),
            decision.confirm(
                requireNotNull(decision.candidateIdForSeat(2)),
                revision.copy(playerInputRevision = 4),
            ),
        )
        assertTrue(decision.candidateIdForSeat(1) != null)
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20261006L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
        alive = true,
    )
}
