package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mayor ruling happens at the attack-result boundary, not at dawn. */
class MayorRedirectBoundaryTest {
    private val players = listOf(
        PlayerState(1, "Mayor", RoleId("Mayor"), Alignment.GOOD, CharacterType.TOWNSFOLK, RoleId("Mayor")),
        PlayerState(2, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, RoleId("Spy")),
        PlayerState(3, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, RoleId("Imp")),
        PlayerState(4, "Recluse", RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER, RoleId("Recluse")),
        PlayerState(5, "Empath", RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK, RoleId("Empath")),
    )
    private fun session(
        setup: List<PlayerState> = players,
    ): ClocktowerGameSession {
        val s = ClocktowerGameSession.createProduction(
            "mayor-attack", 42L, GameState(ScriptId("trouble_brewing"), setup, 42L),
            ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "night-one-to-day", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, StorytellerPhase.DAY, 1,
        ), s.state.gameStateRevision)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance(
            "day-to-night", StorytellerPhase.DAY, 1,
            s.nextInputSequence, StorytellerPhase.NIGHT, 2,
        ), s.state.gameStateRevision)
        s.confirmNightAttack(1, StorytellerPhase.NIGHT, 2, "imp-step")
        assertTrue(s.state.gameState.playerAt(1)!!.alive)
        return s
    }
    private fun request(s: ClocktowerGameSession): StorytellerProviderRequestV1 {
        val snapshot = TroubleBrewingGameSnapshotV1(
            gameId = s.state.gameId,
            gameSeed = s.state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.RUNTIME,
                phase = SnapshotField.Known(StorytellerPhase.NIGHT),
                round = SnapshotField.Known(2),
                gameStateRevision = SnapshotField.Known(s.state.gameStateRevision),
                playerInputRevision = SnapshotField.Known(s.state.playerInputRevision),
            ),
            grimoireSeats = s.state.gameState.players.map {
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
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                snapshot.gameId, snapshot.script.value,
                StorytellerProviderDecisionContextV1.MAYOR_REDIRECT,
                "night:2:mayor-redirect",
            ),
            sourceRevision = StorytellerProviderRevisionV1(
                s.state.gameStateRevision, s.state.playerInputRevision,
            ),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = StorytellerProviderDecisionContextV1.MayorRedirect(1),
            legalCandidates = emptyList(),
            gameContext = StorytellerProviderGameContextBuilderV1.build(
                snapshot, StorytellerProviderRevisionV1(
                    s.state.gameStateRevision, s.state.playerInputRevision,
                ), s.state,
            ),
        )
    }
    private fun choice(s: ClocktowerGameSession, seat: Int) =
        ActionFactDraft.MayorRedirectChoice(
            "mayor-choice-$seat", StorytellerPhase.NIGHT, 2,
            s.nextInputSequence, 1, seat,
        )

    @Test fun `Mayor redirect immediately kills the chosen living player in same transaction`() {
        val s = session()
        val before = s.state
        val selected = choice(s, 5)
        val request = request(s)
        val accepted = s.commitConfirmedMayorRedirect(selected, request, before.gameStateRevision)
        assertTrue(s.state.gameState.playerAt(1)!!.alive)
        assertFalse(s.state.gameState.playerAt(5)!!.alive)
        assertEquals(before.gameStateRevision + 2, s.state.gameStateRevision)
        assertEquals(before.nextTimelineGlobalSequence + 2, s.state.nextTimelineGlobalSequence)
        assertEquals(5, s.state.nightTargets.mayorRedirectSeat)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.MayorRedirectChoice })
        assertEquals(listOf(5), s.state.actionTimeline.entries.mapNotNull {
            (it.fact as? ActionFact.Death)?.targetSeat
        })
        val committed = s.state
        assertSame(accepted, s.commitConfirmedMayorRedirect(selected, request, before.gameStateRevision))
        assertSame(committed, s.state)
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedMayorRedirect(choice(s, 4), request(s), committed.gameStateRevision)
        }
        assertSame(committed, s.state)
    }

    @Test fun `Mayor may choose own death and its result is immediately recorded`() {
        val s = session()
        val request = request(s)
        s.commitConfirmedMayorRedirect(choice(s, 1), request, s.state.gameStateRevision)
        assertFalse(s.state.gameState.playerAt(1)!!.alive)
        assertEquals(listOf(1), s.state.actionTimeline.entries.mapNotNull {
            (it.fact as? ActionFact.Death)?.targetSeat
        })
    }

    @Test fun `choosing a dead target does not invent a second death`() {
        val s = session(players.map { if (it.seat == 4) it.copy(alive = false) else it })
        val request = request(s)
        s.commitConfirmedMayorRedirect(choice(s, 4), request, s.state.gameStateRevision)
        assertTrue(s.state.gameState.playerAt(1)!!.alive)
        assertEquals(0, s.state.actionTimeline.entries.count { it.fact is ActionFact.Death })
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.MayorRedirectChoice })
    }

    @Test fun `ruling refuses stale revision or unrelated source`() {
        val s = session()
        val request = request(s)
        val action = choice(s, 4)
        assertThrows(IllegalStateException::class.java) {
            s.commitConfirmedMayorRedirect(action, request, s.state.gameStateRevision - 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            s.commitConfirmedMayorRedirect(action.copy(mayorSeat = 2), request, s.state.gameStateRevision)
        }
    }
}
