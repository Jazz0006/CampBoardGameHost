package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerCausalJournalPersistence
import com.codex.campboardgamehost.clocktower.domain.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DayAbilityRegistrationRulingProducerV1Test {
    private val script = ScriptId("trouble_brewing")
    private val basePlayers = listOf(
        PlayerState(1, "Slayer", RoleId("Slayer"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(2, "Chef", RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(3, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
        PlayerState(4, "Recluse", RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER),
        PlayerState(5, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION),
    )
    private val roleDefinitions = listOf(
        RoleDefinition(RoleId("Slayer"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, setOf(script)),
        RoleDefinition(RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER, setOf(script)),
        RoleDefinition(RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, setOf(script)),
    )
    private fun makeSession(players: List<PlayerState> = basePlayers) =
        ClocktowerGameSession.createProduction(
            gameId = "day-slayer-registration",
            gameSeed = 51L,
            initialState = GameState(script, players, 51L),
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
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Unknown, SnapshotField.Unknown,
            ),
        )

    private fun input() = ConfirmedDaySlayerRegistrationV1(
        "day:1:slayer:1:4", slayerSeat = 1, recluseSeat = 4,
        registeredDemonRole = RoleId("Imp"),
    )
    private fun recover(
        session: ClocktowerGameSession, journal: StorytellerCausalDecisionJournalV1,
    ): StorytellerCausalDecisionJournalV1 {
        val archive = ClocktowerCausalJournalPersistence.decode(
            ClocktowerCausalJournalPersistence.encode(journal.archive()),
            expectedGameId = session.state.gameId,
            actions = session.state.actionTimeline,
            observations = session.state.epistemicObservationLog.records,
            recoveredCursor = session.state.nextTimelineGlobalSequence,
        )
        return StorytellerCausalDecisionJournalV1.restore(archive, session.state)
    }

    @Test fun `confirmed public Slayer shot anchors one special Demon registration with no fake observation`() {
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val committed = DayAbilityRegistrationRulingProducerV1.confirmSlayer(
            session, journal, snapshot(session), roleDefinitions, input(),
        )
        assertEquals("day-ability-registration", committed.selectedOutcome.decisionType)
        assertEquals(1, committed.registrations.size)
        val registration = committed.registrations.single()
        assertEquals(4, registration.subjectSeat)
        assertEquals(RoleId("Imp"), registration.registeredRole)
        assertEquals(CharacterType.DEMON, registration.registeredType)
        assertEquals(Alignment.EVIL, registration.registeredAlignment)
        assertEquals(RegistrationReason.RECLUSE_ABILITY, registration.reason)
        assertEquals(RegistrationQuestion.DEMON, registration.registrationQuestion)
        assertTrue(session.state.epistemicObservationLog.records.isEmpty())
        val decisionId = journal.archive().records.filterIsInstance<
            StorytellerCausalJournalRecordV1.Captured>().single().frozen.identity.decisionId
        assertTrue(journal.effectiveAt(decisionId).isEmpty())
        assertEquals(listOf(committed), journal.effectiveNow())
        val restored = recover(session, journal)
        assertEquals(listOf(committed), restored.effectiveNow())
        assertEquals(journal.frozenAt(decisionId), restored.frozenAt(decisionId))
        assertEquals(journal.effectiveAt(decisionId), restored.effectiveAt(decisionId))
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session), roleDefinitions, input(),
            )
        }
    }

    @Test fun `explicit actual Slayer Recluse ruling is durable but untouched produces no decision`() {
        val untouched = makeSession()
        val untouchedJournal = StorytellerCausalDecisionJournalV1(untouched.state.gameId)
        assertTrue(untouchedJournal.archive().records.isEmpty())

        val actual = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(actual.state.gameId)
        val committed = DayAbilityRegistrationRulingProducerV1.confirmSlayer(
            actual, journal, snapshot(actual), roleDefinitions,
            input().copy(registeredDemonRole = null),
        )
        assertEquals(RegistrationResolutionStatusV1.EXPLICIT_ACTUAL.name,
            committed.selectedOutcome.canonicalFields.getValue("status"))
        assertEquals("actual", committed.selectedCandidateId)
        assertTrue(committed.registrations.isEmpty())
        assertEquals(TruthRelation.NOT_APPLICABLE, committed.truthRelation)
        assertEquals(listOf(committed), recover(actual, journal).effectiveNow())
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                actual, journal, snapshot(actual), roleDefinitions,
                input().copy(registeredDemonRole = null),
            )
        }
    }

    @Test fun `non-Slayer claimant poisoned participant and non-day phase never create a ruling`() {
        val cases = listOf(
            basePlayers.map { if (it.seat == 1) it.copy(actualRole = RoleId("Chef")) else it },
            basePlayers.map { if (it.seat == 1) it.copy(poisoned = true) else it },
            basePlayers.map { if (it.seat == 4) it.copy(poisoned = true) else it },
            basePlayers.map { if (it.seat == 4) it.copy(alive = false) else it },
        )
        cases.forEach { roster ->
            val session = makeSession(roster)
            val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
            assertThrows(IllegalArgumentException::class.java) {
                DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                    session, journal, snapshot(session), roleDefinitions, input(),
                )
            }
            assertTrue(journal.archive().records.isEmpty())
        }
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session, StorytellerPhase.NIGHT), roleDefinitions, input(),
            )
        }
        assertTrue(journal.archive().records.isEmpty())
    }

    @Test fun `unverified Demon role and mismatched subject fail before capture`() {
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session), roleDefinitions,
                input().copy(registeredDemonRole = RoleId("Poisoner")),
            )
        }
        assertTrue(journal.archive().records.isEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session), roleDefinitions,
                input().copy(recluseSeat = 2),
            )
        }
        assertTrue(journal.archive().records.isEmpty())
    }

    @Test fun `tampered recovered day registration cannot forge a different subject`() {
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        DayAbilityRegistrationRulingProducerV1.confirmSlayer(
            session, journal, snapshot(session), roleDefinitions, input(),
        )
        val encoded = ClocktowerCausalJournalPersistence.encode(journal.archive())
        encoded.getJSONArray("records").getJSONObject(1)
            .getJSONObject("outcomeFields").put("subjectSeat", "2")
        val recovered = ClocktowerCausalJournalPersistence.decode(
            encoded, session.state.gameId, session.state.actionTimeline,
            session.state.epistemicObservationLog.records,
            session.state.nextTimelineGlobalSequence,
        )
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerCausalDecisionJournalV1.restore(recovered, session.state)
        }
    }
}
