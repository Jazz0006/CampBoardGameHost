package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MayorRedirectBoundaryTest {
    private val playerStates = listOf(
        PlayerState(1, "Mayor", RoleId("Mayor"), Alignment.GOOD, CharacterType.TOWNSFOLK, RoleId("Mayor")),
        PlayerState(2, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, RoleId("Spy")),
        PlayerState(3, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, RoleId("Imp")),
        PlayerState(4, "Recluse", RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER, RoleId("Recluse")),
        PlayerState(5, "Empath", RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK, RoleId("Empath")),
    )

    private fun session(
        mode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    ) = ClocktowerGameSession.createProduction(
        gameId = "causal-game",
        gameSeed = 42L,
        initialState = GameState(ScriptId("trouble_brewing"), playerStates, 42L),
        semanticHistoryMode = mode,
    )

    private fun snapshot(session: ClocktowerGameSession) = TroubleBrewingGameSnapshotV1(
        gameId = session.state.gameId,
        gameSeed = session.state.gameSeed,
        position = TroubleBrewingSnapshotPosition(
            stage = TroubleBrewingSnapshotStage.RUNTIME,
            phase = SnapshotField.Known(StorytellerPhase.NIGHT),
            round = SnapshotField.Known(2),
            gameStateRevision = SnapshotField.Known(session.state.gameStateRevision),
            playerInputRevision = SnapshotField.Known(session.state.playerInputRevision),
        ),
        grimoireSeats = session.state.gameState.players.map {
            TroubleBrewingSnapshotSeat(
                seat = it.seat,
                actualRoleId = SnapshotField.Known(it.actualRole.value),
                shownRoleId = SnapshotField.Known((it.shownRole ?: it.actualRole).value),
                alive = SnapshotField.Known(it.alive),
                poisoned = SnapshotField.Known(it.poisoned),
            )
        },
        setupState = TroubleBrewingSnapshotSetupState(SnapshotField.Unknown, SnapshotField.Unknown),
    )

    private fun request(id: String, session: ClocktowerGameSession):
        StorytellerProviderRequestV1 {
        val snapshot = snapshot(session)
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                snapshot.gameId, snapshot.script.value,
                StorytellerProviderDecisionContextV1.MAYOR_REDIRECT, id,
            ),
            sourceRevision = StorytellerProviderRevisionV1(
                session.state.gameStateRevision, session.state.playerInputRevision,
            ),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = StorytellerProviderDecisionContextV1.MayorRedirect(1),
            legalCandidates = listOf(
                StorytellerProviderCandidateV1("seat-2", StorytellerProviderCandidatePayloadV1.SeatTarget(2)),
                StorytellerProviderCandidateV1("seat-4", StorytellerProviderCandidatePayloadV1.SeatTarget(4)),
            ),
            gameContext = StorytellerProviderGameContextBuilderV1.build(
                snapshot, StorytellerProviderRevisionV1(
                    session.state.gameStateRevision, session.state.playerInputRevision,
                ), session.state,
            ),
        )
    }

    @Test
    fun `confirmed Mayor commits one actual fact without candidate gating`() {
        val game = session()
        fun mayorRequest(id: String): StorytellerProviderRequestV1 =
            request(id, game).copy(legalCandidates = emptyList())
        val firstRequest = mayorRequest("night:2:mayor-redirect:first")
        val choice = ActionFactDraft.MayorRedirectChoice(
            actionId = "mayor-night-two-first",
            phase = StorytellerPhase.NIGHT,
            round = 2,
            sequence = 1,
            mayorSeat = 1,
            targetSeat = 2,
        )
        val accepted = game.commitConfirmedMayorRedirect(choice, firstRequest, 0L)
        assertEquals(0L, accepted.point.globalSequence)
        assertEquals(1L, game.state.gameStateRevision)
        assertEquals(1L, game.state.nextTimelineGlobalSequence)
        assertEquals(choice.targetSeat, (game.state.actionTimeline.entries.single().fact as
            com.codex.campboardgamehost.clocktower.domain.ActionFact.MayorRedirectChoice).targetSeat)

        // Re-tapping the same stable confirmation is a no-op.
        val afterFirst = game.state
        assertEquals(accepted, game.commitConfirmedMayorRedirect(choice, firstRequest, 0L))
        assertEquals(afterFirst, game.state)

        val bad = choice.copy(actionId = "mayor-invalid", targetSeat = 99, sequence = 2)
        assertThrows(IllegalArgumentException::class.java) {
            game.commitConfirmedMayorRedirect(
                bad, mayorRequest("night:2:mayor-redirect:invalid"),
                1L,
            )
        }
        assertEquals(afterFirst, game.state)

        assertThrows(IllegalStateException::class.java) {
            game.commitConfirmedMayorRedirect(choice.copy(actionId = "stale", sequence = 2), firstRequest, 0L)
        }
        val wrongSource = mayorRequest("wrong-source").copy(
            decisionContext = StorytellerProviderDecisionContextV1.MayorRedirect(2))
        assertThrows(IllegalArgumentException::class.java) {
            game.commitConfirmedMayorRedirect(choice.copy(actionId = "wrong-source", sequence = 2), wrongSource, 1L)
        }
        assertEquals(afterFirst, game.state)

        val secondRequest = mayorRequest("night:2:mayor-redirect:revised")
        game.commitConfirmedMayorRedirect(
            choice.copy(actionId = "mayor-night-two-revised", targetSeat = 3, sequence = 2),
            secondRequest, 1L,
        )
        assertEquals(2L, game.state.gameStateRevision)
        assertEquals(2L, game.state.nextTimelineGlobalSequence)
        assertEquals(listOf(0L, 1L), game.state.actionTimeline.entries.map { it.point.globalSequence })
        assertEquals(3, game.state.nightTargets.mayorRedirectSeat)

        // The legal domain also permits letting the Mayor die: target == Mayor seat.
        game.commitConfirmedMayorRedirect(
            choice.copy(actionId = "mayor-night-two-no-redirect", targetSeat = 1, sequence = 3),
            mayorRequest("night:2:mayor-redirect:no-redirect"),
            2L,
        )
        assertEquals(3L, game.state.gameStateRevision)
        assertEquals(3L, game.state.nextTimelineGlobalSequence)
        assertEquals(1, game.state.nightTargets.mayorRedirectSeat)

    }

}
