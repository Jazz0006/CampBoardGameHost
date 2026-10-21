package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Prevent an AI request from inventing decisions beyond an unconfirmed player action. */
class ClocktowerResidualUnconfirmedActionBarrierTest {
    private fun host(
        roles: List<String> = listOf("Ravenkeeper", "Empath", "Monk", "Poisoner", "Imp"),
    ): ClocktowerGameSession {
        return ClocktowerGameSession.createProduction(
            "residual-boundary", 17,
            GameState(ScriptId("trouble_brewing"), roles.mapIndexed { index, role ->
                val type = when (role) {
                    "Poisoner" -> CharacterType.MINION
                    "Imp" -> CharacterType.DEMON
                    else -> CharacterType.TOWNSFOLK
                }
                PlayerState(seat = index + 1, name = "Player $index",
                    actualRole = RoleId(role), shownRole = RoleId(role),
                    actualType = type,
                    actualAlignment = if (type == CharacterType.TOWNSFOLK)
                        Alignment.GOOD else Alignment.EVIL)
            }, 17),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1)
    }

    private fun snapshot(session: ClocktowerGameSession,
        phase: StorytellerPhase = StorytellerPhase.FIRST_NIGHT,
        round: Int = 1): TroubleBrewingGameSnapshotV1 {
        val state = session.state
        return TroubleBrewingGameSnapshotV1(
            gameId = state.gameId, gameSeed = state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.RUNTIME,
                SnapshotField.Known(phase),
                SnapshotField.Known(round)),
            grimoireSeats = state.gameState.players.map {
                TroubleBrewingSnapshotSeat(
                    it.seat, SnapshotField.Known(it.actualRole.value),
                    SnapshotField.Known(it.actualRole.value),
                    SnapshotField.Known(it.alive), SnapshotField.Known(it.poisoned))
            },
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Known(false), SnapshotField.NotApplicable))
    }

    private fun step(action: ClocktowerNightAction) = ClocktowerNightStepUi(
        title = "pending", actor = null, isRealAction = true,
        reason = "", storytellerAction = "", tellPlayer = null, explanation = "",
        action = action)

    @Test fun stopsBeforeUncommittedInteractionAndNeverInventsLaterTargets() {
        val session = host()
        val steps = listOf(step(ClocktowerNightAction.Poison), step(ClocktowerNightAction.None))
        val scope = ClocktowerCurrentResidualScopeV2.collect(
            state = session.state, phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, steps = steps, startIndex = 0, cards = emptyList(),
            snapshot = snapshot(session), gameContext = StorytellerProviderGameContextV1.EMPTY)
        assertTrue(scope.requests.isEmpty())
        assertEquals(0, scope.barrierStep)
        assertEquals("PLAYER_OR_HOST_ACTION_NOT_YET_CONFIRMED", scope.barrierReason)
        val past = ClocktowerCurrentResidualScopeV2.collect(
            state = session.state, phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, steps = steps, startIndex = 1, cards = emptyList(),
            snapshot = snapshot(session), gameContext = StorytellerProviderGameContextV1.EMPTY)
        assertEquals(null, past.barrierStep)
        assertTrue(past.requests.isEmpty())
    }

}
