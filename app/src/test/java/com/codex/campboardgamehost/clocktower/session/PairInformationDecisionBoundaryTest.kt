package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protects AI pair provider inputs, NOT a Host game-rule confirmation gate. */
class PairInformationDecisionBoundaryTest {
    @Test
    fun `AI receives pair candidates and stable request identity without publishing a Host observation`() {
        val identity = StorytellerDecisionRequestIdentity(
            gameId = "game-res-1", requestId = "first-night:investigator:seat-1")
        val revision = StorytellerDecisionRevision(7, 3)
        val decision = PairInformationDecisionBoundary.create(
            requestIdentity = identity,
            revision = revision,
            game = GameState(
                script = ScriptId("trouble_brewing"),
                players = listOf(
                    player(1, "Investigator", CharacterType.TOWNSFOLK),
                    player(2, "Chef", CharacterType.TOWNSFOLK),
                    player(3, "Recluse", CharacterType.OUTSIDER),
                    player(4, "Poisoner", CharacterType.MINION),
                    player(5, "Imp", CharacterType.DEMON),
                ),
                seed = 20261006L,
            ),
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        assertEquals(identity, decision.requestIdentity)
        assertEquals(revision, decision.revision)
        assertEquals(RoleId("Investigator"), decision.abilityRole)
        assertTrue(decision.legalCandidates.isNotEmpty())
        assertEquals(
            decision.legalCandidates.size,
            decision.legalCandidates.map { it.candidateId }.distinct().size,
        )
        assertTrue(decision.legalCandidates.all { candidate ->
            candidate.outcome.candidateSeats.all { it in 1..5 }
        })
    }

    private fun player(seat: Int, role: String, type: CharacterType) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )
}

