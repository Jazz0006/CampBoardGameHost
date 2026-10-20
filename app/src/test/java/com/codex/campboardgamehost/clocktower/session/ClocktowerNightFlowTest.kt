package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.ClocktowerNightHostProjection
import com.codex.campboardgamehost.PlayerCard
import com.codex.campboardgamehost.clocktowerRoleByName
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ClocktowerNightFlowTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { File("src/main/assets/$it").readText(Charsets.UTF_8) }
    private val tb = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val ngj = catalog.ruleset(ClocktowerScript.NoGreaterJoy)

    private fun session(vararg roles: String, script: String = "trouble_brewing", night: Boolean = false): ClocktowerGameSession {
        val minions = setOf("Poisoner", "Spy", "Baron", "Scarlet Woman")
        val outsiders = setOf("Butler", "Saint", "Recluse", "Drunk", "Klutz")
        val game = GameState(ScriptId(script), roles.mapIndexed { index, role ->
            val type = when (role) {
                "Imp" -> CharacterType.DEMON
                in minions -> CharacterType.MINION
                in outsiders -> CharacterType.OUTSIDER
                else -> CharacterType.TOWNSFOLK
            }
            PlayerState(index + 1, "P${index + 1}", RoleId(role),
                if (type in setOf(CharacterType.DEMON, CharacterType.MINION)) Alignment.EVIL else Alignment.GOOD, type)
        }, 73L)
        return ClocktowerGameSession.createProduction("flow-test", game.seed, game, ClocktowerSemanticHistoryMode.GLOBAL_V1).also { s ->
            if (night) {
                s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT, 1,
                    s.nextInputSequence, StorytellerPhase.DAY, 1), s.state.gameStateRevision)
                s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("night", StorytellerPhase.DAY, 1,
                    s.nextInputSequence, StorytellerPhase.NIGHT, 2), s.state.gameStateRevision)
            }
        }
    }

    private fun next(s: ClocktowerGameSession, script: ClocktowerScript = ClocktowerScript.TroubleBrewing): String? =
        s.state.nextNightInteraction(catalog.ruleset(script))?.id?.value

    private fun information(s: ClocktowerGameSession, role: String, seat: Int) {
        s.commitGlobalEpistemicObservation(EpistemicObservationDraft("info-${s.state.round}-$role", s.state.phase,
            s.state.round, s.nextInputSequence, seat, RoleId(role), ObservationVisibility.PRIVATE, setOf(seat),
            ObservationReliability.RECEIVED_AS_FUNCTIONING, InformationProposition.RoleAt(seat, RoleId("Chef"))))
    }

    @Test fun `first night progresses from actual private information without any UI cursor`() {
        val s = session("Chef", "Empath", "Soldier", "Saint", "Imp")
        assertEquals("first_night:role:Chef", next(s))
        val unchanged = s.state
        repeat(3) { assertEquals("first_night:role:Chef", next(s)) }
        assertSame(unchanged, s.state)
        information(s, "Chef", 1)
        assertEquals("first_night:role:Empath", next(s))
        information(s, "Empath", 2)
        assertNull(next(s))
        // A fresh reader of the exact committed state reaches the same next safe step.
        assertNull(s.state.copy().nextNightInteraction(tb))
        assertEquals(2, s.state.epistemicObservationLog.records.size)
    }

    @Test fun `confirmed attack skips a slain Fortune Teller and reaches Butler`() {
        val s = session("Imp", "Fortune Teller", "Butler", "Soldier", "Saint", night = true)
        assertEquals("other_night:role:Imp", next(s))
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)))
        assertFalse(s.state.gameState.playerAt(2)!!.alive)
        assertEquals("other_night:role:Butler", next(s))
        s.confirmNightButlerMaster(3, 4, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)))
        assertNull(next(s))
    }

    @Test fun `Ravenkeeper death inserts a real information step and reading cannot complete it`() {
        val s = session("Imp", "Ravenkeeper", "Butler", "Soldier", "Saint", night = true)
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)))
        repeat(2) { assertEquals("other_night:role:Ravenkeeper", next(s)) }
        information(s, "Ravenkeeper", 2)
        assertEquals("other_night:role:Butler", next(s))
    }

    @Test fun `poisoned death-trigger information preserves impairment from before death`() {
        val s = session("Poisoner", "Imp", "Ravenkeeper", "Soldier", "Saint", night = true)
        s.confirmNightPoison(3, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)))
        s.confirmNightAttack(3, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)))
        val cards = s.state.gameState.players.map { player ->
            PlayerCard(player.name, clocktowerRole = clocktowerRoleByName(player.actualRole.value))
        }
        val projection = ClocktowerNightHostProjection(cards, s.state)
        assertEquals(AbilityFunctioningState.POISONED, projection.ravenkeeperDeathTriggerAbilityState)
        val step = com.codex.campboardgamehost.ClocktowerInformationStepBuilder(
            cards, "en", { _, en -> en }, { null }, { "" }, { _, _ -> null },
        ).build("Ravenkeeper", "Ravenkeeper", null, "",
            actorOverride = projection.ravenkeeperTrigger,
            abilityStateOverride = projection.ravenkeeperDeathTriggerAbilityState)
        assertTrue(com.codex.campboardgamehost.clocktowerDisplayedInformationIsUnreliable(step) { _, _ -> false })
        assertEquals("other_night:role:Ravenkeeper", next(s))
    }

    @Test fun `Mayor ruling is derived from accepted attack and blocks later information until confirmed`() {
        val s = session("Imp", "Mayor", "Empath", "Soldier", "Saint", night = true)
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)))
        assertTrue(s.state.gameState.playerAt(2)!!.alive)
        assertEquals("other_night:event:mayor:death_resolution", next(s))
        val revision = StorytellerDecisionRevision(s.state.gameStateRevision, s.state.playerInputRevision)
        val pending = MayorRedirectDecisionBoundary.create(StorytellerDecisionRequestIdentity(s.state.gameId, "mayor-step"), revision, s.state.gameState, 2)
        val snapshot = com.codex.campboardgamehost.clocktower.setup.TroubleBrewingGameSnapshotProjector.fromRuntime(
            s.toGameSnapshot(tb.toRulesetRef("test", "flow")), s.state.phase, s.state.round, tb.characterRegistry)
        s.commitConfirmedMayorRedirect(ActionFactDraft.MayorRedirectChoice("mayor-ruling", s.state.phase, s.state.round,
            s.nextInputSequence, 2, 4), StorytellerProviderRequestFactoryV1.fromMayorRedirect(pending, snapshot), s.state.gameStateRevision)
        assertTrue(s.state.gameState.playerAt(4)!!.alive)
        assertEquals("other_night:role:Empath", next(s))
    }

    @Test fun `Imp succession notification must be confirmed and does not grant another attack`() {
        val s = session("Imp", "Scarlet Woman", "Empath", "Soldier", "Saint", night = true)
        s.confirmNightAttack(1, StorytellerPhase.NIGHT, 2, requireNotNull(next(s)), 2)
        assertEquals("other_night:event:imp:new_demon_identity", next(s))
        assertEquals(2, s.state.pendingImpNotification()!!.targetSeat)
        s.confirmImpNotification()
        s.confirmImpNotification()
        assertEquals("other_night:role:Empath", next(s))
        assertEquals(1, s.state.epistemicObservationLog.records.size)
        assertEquals(setOf(2), s.state.epistemicObservationLog.records.single().recipientSeats)
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.Attack })
    }

    @Test fun `Spy Next records one wake without a second Grimoire or another action`() {
        val s = session("Spy", "Chef", "Soldier", "Saint", "Imp")
        val step = requireNotNull(next(s))
        assertEquals("first_night:role:Spy", step)
        s.confirmSpyWake(tb, step)
        s.confirmSpyWake(tb, step)
        assertEquals("first_night:role:Chef", next(s))
        assertEquals(1, s.state.actionTimeline.entries.count { it.fact is ActionFact.NightWake })
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
    }

    @Test fun `seven player evil introductions record real recipients only on explicit confirmation`() {
        val s = session("Spy", "Chef", "Soldier", "Saint", "Empath", "Butler", "Imp")
        s.commitDemonBluffs(listOf(RoleId("Monk"), RoleId("Mayor"), RoleId("Virgin")), StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence)
        assertEquals("first_night:system:minion_info", next(s))
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        s.confirmEvilInformation(tb, requireNotNull(next(s)))
        assertEquals(setOf(1), s.state.epistemicObservationLog.records.single().recipientSeats)
        assertEquals("first_night:system:demon_info", next(s))
        s.confirmEvilInformation(tb, requireNotNull(next(s)))
        assertEquals(setOf(7), s.state.epistemicObservationLog.records.last().recipientSeats)
        assertEquals("first_night:role:Spy", next(s))
    }

    @Test fun `Drunk shown Fortune Teller wakes without gaining the real red herring ability`() {
        val s = session("Drunk", "Soldier", "Saint", "Butler", "Imp")
        s.updateGameState(s.state.gameState.copy(players = s.state.gameState.players.map {
            if (it.seat == 1) it.copy(shownRole = RoleId("Fortune Teller")) else it
        }))
        assertEquals("first_night:role:Fortune Teller", next(s))
        assertFalse(s.state.nightInteractions(tb).any { it.id.value.endsWith("red_herring") })
    }

    @Test fun `NGJ first-night order has no evil introductions and Sage death inserts its information`() {
        val first = session("Clockmaker", "Investigator", "Empath", "Chambermaid", "Imp", script = "no_greater_joy")
        assertEquals(listOf("Investigator", "Empath", "Clockmaker", "Chambermaid"),
            first.state.nightInteractions(ngj).mapNotNull { it.roleId?.value })
        val s = session("Imp", "Sage", "Empath", "Chambermaid", "Klutz", script = "no_greater_joy", night = true)
        s.confirmNightAttack(2, StorytellerPhase.NIGHT, 2, requireNotNull(next(s, ClocktowerScript.NoGreaterJoy)))
        assertEquals("other_night:role:Sage", next(s, ClocktowerScript.NoGreaterJoy))
        information(s, "Sage", 2)
        assertEquals("other_night:role:Empath", next(s, ClocktowerScript.NoGreaterJoy))
    }
    @Test fun `duplicate information confirmation is idempotent while changed or stale new information cannot commit`() {
        val s = session("Chef", "Soldier", "Saint", "Butler", "Imp")
        val revision = StorytellerDecisionRevision(s.state.gameStateRevision, s.state.playerInputRevision)
        val draft = EpistemicObservationDraft("confirmed-chef", s.state.phase, 1, s.nextInputSequence, 1, RoleId("Chef"),
            ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, emptyList(), 1))
        s.commitGlobalEpistemicObservation(draft, revision)
        val accepted = s.state
        s.commitGlobalEpistemicObservation(draft, revision)
        assertSame(accepted, s.state)
        assertThrows(IllegalArgumentException::class.java) {
            s.commitGlobalEpistemicObservation(draft.copy(proposition = InformationProposition.RoleAt(1, RoleId("Spy"))), revision)
        }
        assertSame(accepted, s.state)
    }

    @Test fun `stale evil preview cannot publish changed setup facts`() {
        val s = session("Spy", "Chef", "Soldier", "Saint", "Empath", "Butler", "Imp")
        val revision = StorytellerDecisionRevision(s.state.gameStateRevision, s.state.playerInputRevision)
        val step = requireNotNull(next(s))
        s.recordPlayerInput()
        val before = s.state
        assertThrows(IllegalStateException::class.java) { s.confirmEvilInformation(tb, step, revision) }
        assertSame(before, s.state)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
    }

    @Test fun `current Imp identity fails closed instead of choosing an arbitrary living Demon`() {
        val s = session("Imp", "Imp", "Soldier", "Saint", "Butler", night = true)
        assertThrows(IllegalStateException::class.java) { next(s) }
    }

    @Test fun `real Fortune Teller setup is confirmed before the information step and does not publish a clue`() {
        val s = session("Fortune Teller", "Soldier", "Saint", "Butler", "Imp")
        assertEquals("first_night:fortune_teller:red_herring", next(s))
        s.commitRedHerringSeat(2, StorytellerPhase.FIRST_NIGHT, 1, s.nextInputSequence)
        assertEquals("first_night:role:Fortune Teller", next(s))
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
    }

}
