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

class PairInformationDecisionBoundaryTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `manual confirmation accepts only a current rules-legal candidate and produces the observation without a recommender`() {
        val decision = PairInformationDecisionBoundary.create(
            requestIdentity = InformationDecisionRequestIdentity(
                gameId = "game-res-1",
                requestId = "first-night:investigator:seat-1",
            ),
            revision = InformationDecisionRevision(gameStateRevision = 7, playerInputRevision = 3),
            game = game(
                player(1, "Investigator", CharacterType.TOWNSFOLK),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Recluse", CharacterType.OUTSIDER),
                player(4, "Poisoner", CharacterType.MINION),
                player(5, "Imp", CharacterType.DEMON),
            ),
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Investigator"),
            reliability = ReliabilityState.RELIABLE,
        )
        val selected = decision.legalCandidates.first()

        val confirmation = decision.confirm(selected.candidateId, decision.revision)
        assertTrue(confirmation is PairInformationDecisionConfirmation.Confirmed)
        confirmation as PairInformationDecisionConfirmation.Confirmed
        assertEquals(selected.candidateId, confirmation.candidateId)
        assertEquals(selected.outcome.shownRole, confirmation.observation.shownRole)
        assertEquals(selected.outcome.candidateSeats, confirmation.observation.candidateSeats)
        assertEquals(selected.semanticTruth, confirmation.observation.semanticTruth)
        assertEquals(selected.registrations.map { it.subjectSeat }, confirmation.observation.registrations.map { it.playerSeat })
    }

    @Test
    fun `unknown and stale selections fail closed before observation publication`() {
        val revision = InformationDecisionRevision(gameStateRevision = 7, playerInputRevision = 3)
        val decision = PairInformationDecisionBoundary.create(
            requestIdentity = InformationDecisionRequestIdentity(
                gameId = "game-res-1",
                requestId = "first-night:librarian:seat-1",
            ),
            revision = revision,
            game = game(
                player(1, "Librarian", CharacterType.TOWNSFOLK),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Butler", CharacterType.OUTSIDER),
                player(4, "Poisoner", CharacterType.MINION),
                player(5, "Imp", CharacterType.DEMON),
            ),
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
        )

        assertEquals(
            PairInformationDecisionConfirmation.Blocked(PairInformationDecisionBlockReason.ILLEGAL_CANDIDATE),
            decision.confirm("not-legal", revision),
        )
        assertEquals(
            PairInformationDecisionConfirmation.Blocked(PairInformationDecisionBlockReason.STALE_CONTEXT),
            decision.confirm(
                decision.legalCandidates.first().candidateId,
                revision.copy(playerInputRevision = 4),
            ),
        )
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
    )
}
