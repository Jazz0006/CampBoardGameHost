package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerCausalJournalPersistence
import com.codex.campboardgamehost.clocktower.domain.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DayVirginSpyRegistrationProducerV1Test {
    private val script = ScriptId("trouble_brewing")
    private val original = listOf(
        PlayerState(1, "Virgin", RoleId("Virgin"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(2, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION),
        PlayerState(3, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
        PlayerState(4, "Chef", RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(5, "Recluse", RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER),
    )
    private val roles = listOf(
        RoleDefinition(RoleId("Virgin"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, setOf(script)),
        RoleDefinition(RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, setOf(script)),
        RoleDefinition(RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER, setOf(script)),
    )
    private fun session(players: List<PlayerState> = original) =
        ClocktowerGameSession.createProduction(
            gameId = "virgin-public-first-nomination",
            gameSeed = 2026L,
            initialState = GameState(script, players, 2026L),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    private fun snapshot(session: ClocktowerGameSession, phase: StorytellerPhase = StorytellerPhase.DAY) =
        TroubleBrewingGameSnapshotV1(
            gameId = session.state.gameId,
            gameSeed = session.state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.RUNTIME,
                phase = SnapshotField.Known(phase),
                round = SnapshotField.Known(1),
                gameStateRevision = SnapshotField.Known(session.state.gameStateRevision),
                playerInputRevision = SnapshotField.Known(session.state.playerInputRevision),
            ),
            grimoireSeats = session.state.gameState.players.map { player ->
                TroubleBrewingSnapshotSeat(
                    seat = player.seat,
                    actualRoleId = SnapshotField.Known(player.actualRole.value),
                    shownRoleId = SnapshotField.Known((player.shownRole ?: player.actualRole).value),
                    alive = SnapshotField.Known(player.alive),
                    poisoned = SnapshotField.Known(player.poisoned),
                )
            },
            setupState = TroubleBrewingSnapshotSetupState(SnapshotField.Unknown, SnapshotField.Unknown),
        )
    private fun input(special: Boolean) = ConfirmedDayVirginSpyRegistrationV1(
        "day:1:virgin:1:2", 1, 2, special,
    )
    private fun recover(session: ClocktowerGameSession, journal: StorytellerCausalDecisionJournalV1) =
        StorytellerCausalDecisionJournalV1.restore(
            ClocktowerCausalJournalPersistence.decode(
                ClocktowerCausalJournalPersistence.encode(journal.archive()),
                expectedGameId = session.state.gameId,
                actions = session.state.actionTimeline,
                observations = session.state.epistemicObservationLog.records,
                recoveredCursor = session.state.nextTimelineGlobalSequence,
            ),
            session.state,
        )

    @Test fun `Virgin confirms only Townsfolk type and does not invent named role or private observation`() {
        val s = session()
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        val event = DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
            s, journal, snapshot(s), roles, input(true), confirmedExecution = true,
        )
        assertEquals("day-ability-registration", event.selectedOutcome.decisionType)
        assertEquals("special:type:TOWNSFOLK", event.selectedCandidateId)
        val fact = event.registrations.single()
        assertNull(fact.registeredRole)
        assertEquals(CharacterType.TOWNSFOLK, fact.registeredType)
        assertEquals(Alignment.GOOD, fact.registeredAlignment)
        assertEquals(RegistrationQuestion.CHARACTER_TYPE, fact.registrationQuestion)
        assertEquals(RegistrationReason.SPY_ABILITY, fact.reason)
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        val id = journal.archive().records.filterIsInstance<
            StorytellerCausalJournalRecordV1.Captured>().single().frozen.identity.decisionId
        assertTrue(journal.effectiveAt(id).isEmpty())
        assertEquals(listOf(event), recover(s, journal).effectiveNow())
        assertEquals(journal.frozenAt(id), recover(s, journal).frozenAt(id))
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
                s, journal, snapshot(s), roles, input(true), confirmedExecution = true,
            )
        }
    }

    @Test fun `deliberate actual Spy registration is a distinct event with no special RegistrationFact`() {
        val s = session()
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        val event = DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
            s, journal, snapshot(s), roles, input(false), confirmedExecution = false,
        )
        assertEquals("actual", event.selectedCandidateId)
        assertEquals("EXPLICIT_ACTUAL", event.selectedOutcome.canonicalFields["status"])
        assertTrue(event.registrations.isEmpty())
        assertEquals(TruthRelation.NOT_APPLICABLE, event.truthRelation)
        assertEquals(listOf(event), recover(s, journal).effectiveNow())
    }

    @Test fun `contradictory execution forged actor or role and poisoned subjects fail before capture`() {
        val bad = listOf(
            original.map { if (it.seat == 1) it.copy(actualRole = RoleId("Chef")) else it },
            original.map { if (it.seat == 2) it.copy(actualRole = RoleId("Chef")) else it },
            original.map { if (it.seat == 1) it.copy(poisoned = true) else it },
            original.map { if (it.seat == 2) it.copy(poisoned = true) else it },
            original.map { if (it.seat == 2) it.copy(alive = false) else it },
        )
        bad.forEach { roster ->
            val s = session(roster)
            val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
            assertThrows(IllegalArgumentException::class.java) {
                DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
                    s, journal, snapshot(s), roles, input(true), confirmedExecution = true,
                )
            }
            assertTrue(journal.archive().records.isEmpty())
        }
        val s = session()
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
                s, journal, snapshot(s), roles, input(true), confirmedExecution = false,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
                s, journal, snapshot(s, StorytellerPhase.NIGHT), roles, input(false),
                confirmedExecution = false,
            )
        }
        assertTrue(journal.archive().records.isEmpty())
    }

    @Test fun `malformed Recovery cannot turn a type-only ruling into Washerwoman`() {
        val s = session()
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        DayAbilityRegistrationRulingProducerV1.confirmVirginSpy(
            s, journal, snapshot(s), roles, input(true), confirmedExecution = true,
        )
        val json = ClocktowerCausalJournalPersistence.encode(journal.archive())
        json.getJSONArray("records").getJSONObject(1).getJSONArray("registrations")
            .getJSONObject(0).put("registeredRole", "Washerwoman")
        val decoded = ClocktowerCausalJournalPersistence.decode(
            json, s.state.gameId, s.state.actionTimeline, s.state.epistemicObservationLog.records,
            s.state.nextTimelineGlobalSequence,
        )
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerCausalDecisionJournalV1.restore(decoded, s.state)
        }
    }
}
