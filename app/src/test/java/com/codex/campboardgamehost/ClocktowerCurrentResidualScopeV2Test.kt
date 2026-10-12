package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Boundary safety tests against a real production GLOBAL_V1 Host session. */
class ClocktowerCurrentResidualScopeV2Test {
    private fun host(): ClocktowerGameSession {
        val roles = listOf("Chef", "Empath", "Monk", "Poisoner", "Imp")
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

    private fun snapshot(session: ClocktowerGameSession): TroubleBrewingGameSnapshotV1 {
        val state = session.state
        return TroubleBrewingGameSnapshotV1(
            gameId = state.gameId, gameSeed = state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.RUNTIME,
                SnapshotField.Known(StorytellerPhase.FIRST_NIGHT),
                SnapshotField.Known(1)),
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
            snapshot = snapshot(session), gameContext = StorytellerProviderGameContextV1.EMPTY,
            pairContext = null)
        assertTrue(scope.requests.isEmpty())
        assertEquals(0, scope.barrierStep)
        assertEquals("PLAYER_OR_HOST_ACTION_NOT_YET_CONFIRMED", scope.barrierReason)
        val past = ClocktowerCurrentResidualScopeV2.collect(
            state = session.state, phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, steps = steps, startIndex = 1, cards = emptyList(),
            snapshot = snapshot(session), gameContext = StorytellerProviderGameContextV1.EMPTY,
            pairContext = null)
        assertEquals(null, past.barrierStep)
        assertTrue(past.requests.isEmpty())
    }

    @Test fun actionKindsAreFlowBarriersNotIndependentAiRoleRecommenders() {
        for (action in listOf(
            ClocktowerNightAction.Poison, ClocktowerNightAction.FortuneTeller,
            ClocktowerNightAction.DemonKill, ClocktowerNightAction.MonkProtect,
            ClocktowerNightAction.Ravenkeeper, ClocktowerNightAction.MayorRedirect,
            ClocktowerNightAction.ButlerMaster, ClocktowerNightAction.RedHerring
        )) assertTrue(ClocktowerCurrentResidualScopeV2.needsLiveInteraction(action))
        assertFalse(ClocktowerCurrentResidualScopeV2.needsLiveInteraction(
            ClocktowerNightAction.None))
        val session = host()
        val scope = ClocktowerCurrentResidualScopeV2.collect(
            state = session.state, phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, steps = listOf(step(ClocktowerNightAction.RedHerring),
                step(ClocktowerNightAction.Poison)), startIndex = 0,
            cards = emptyList(), snapshot = snapshot(session),
            gameContext = StorytellerProviderGameContextV1.EMPTY,
            pairContext = null, redHerringAlreadyChosen = true)
        assertEquals(1, scope.barrierStep)
    }
}
