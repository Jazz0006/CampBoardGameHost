package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.HostCurrentOpenDecisionInventoryV2
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
        val roles = listOf("Ravenkeeper", "Empath", "Monk", "Poisoner", "Imp")
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

    @Test fun secondNightRealNumericInformationIsOpenAndHostLegal() {
        val session = host()
        val cards = (0..4).map { PlayerCard("Player $it", Role.Civilian, "") }
        val empath = step(ClocktowerNightAction.None).copy(
            actor = cards[1], roleEnName = "Empath",
            displayProposition = InformationProposition.NumericResult(
                NumericMetric.LIVING_EVIL_NEIGHBOURS, 2, listOf(1, 3), 0),
            numericMinimumValue = 0, numericMaximumValue = 2)
        val scope = ClocktowerCurrentResidualScopeV2.collect(
            state = session.state, phase = StorytellerPhase.NIGHT, round = 2,
            steps = listOf(empath, step(ClocktowerNightAction.DemonKill)),
            startIndex = 0, cards = cards, snapshot = snapshot(session, StorytellerPhase.NIGHT, 2),
            gameContext = StorytellerProviderGameContextV1.EMPTY,
            pairContext = null)
        assertEquals(1, scope.requests.size)
        assertEquals(1, scope.barrierStep)
        val open = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            session.state, StorytellerPhase.NIGHT, 2, scope.requests)
        assertEquals(1, open.size)
        assertEquals("Empath", open.single().abilityRoleId)
    }

    @Test fun aCurrentNightRoleResultOpensOnlyAfterPlayerTargetIsKnown() {
        val session = host()
        val cards = (0..4).map { PlayerCard("Player $it", Role.Civilian, "") }
        val roleOption = ClocktowerDisplayOption(
            label = "Monk", displayKind = ClocktowerDisplayKind.RoleReveal,
            displayTitle = "revealed", displayPrimary = "Monk",
            displaySecondary = null, displayFooter = null,
            proposition = InformationProposition.RoleAt(3, RoleId("Monk")))
        val current = step(ClocktowerNightAction.Ravenkeeper).copy(
            actor = cards[0], roleEnName = "Ravenkeeper",
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayProposition = InformationProposition.RoleAt(3, RoleId("Monk")),
            automaticInformationCandidates = listOf(roleOption))
        fun scope(value: ClocktowerNightStepUi) = ClocktowerCurrentResidualScopeV2.collect(
            session.state, StorytellerPhase.NIGHT, 2,
            listOf(value, step(ClocktowerNightAction.DemonKill)), 0, cards,
            snapshot(session, StorytellerPhase.NIGHT, 2),
            StorytellerProviderGameContextV1.EMPTY, null)
        val pending = scope(current)
        assertEquals(1, pending.requests.size)
        val first = pending.requests.single()
        val chosen = first.legalCandidates.single().candidateId
        val selection = mapOf(first.identity.decisionId to chosen)
        assertTrue(ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
            pending.requests, selection, listOf(current), cards))
        assertFalse(ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
            pending.requests, emptyMap(), listOf(current), cards))
        assertFalse(ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
            listOf(first, first), selection, listOf(current), cards))
        assertFalse(ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
            pending.requests, selection, listOf(current.copy(
                automaticInformationCandidates = listOf(roleOption.copy(
                    recluseRegistersEvil = true)))), cards))
        assertFalse(ClocktowerResidualDisplayMatcherV2.allChoicesCanBePresented(
            pending.requests, selection, listOf(current.copy(
                automaticInformationCandidates = listOf(roleOption, roleOption))), cards))
        assertEquals(1, pending.barrierStep)
        val legal = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            session.state, StorytellerPhase.NIGHT, 2, pending.requests)
        assertEquals("Ravenkeeper", legal.single().abilityRoleId)
        assertEquals("ROLE", (legal.single().currentDecisionContext as
            StorytellerProviderDecisionContextV1.ScalarInformation).kind.name)
        assertTrue(scope(current.copy(displayProposition = null,
            automaticInformationCandidates = emptyList())).requests.isEmpty())
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
