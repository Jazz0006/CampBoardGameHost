package com.codex.campboardgamehost.clocktower.session

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
        PlayerState(6, "Virgin", RoleId("Virgin"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(7, "Fortune Teller", RoleId("Fortune Teller"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(8, "Investigator", RoleId("Investigator"), Alignment.GOOD, CharacterType.TOWNSFOLK),
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
        val archive = journal.archive()
        return StorytellerCausalDecisionJournalV1.restore(archive, session.state)
    }

    @Test fun `confirmed public Slayer shot anchors one special Demon registration with no fake observation`() {
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val committed = DayAbilityRegistrationRulingProducerV1.confirmSlayer(
            session, journal, snapshot(session), input(),
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
        assertEquals(listOf(committed), journal.effectiveNow())
        val restored = recover(session, journal)
        assertEquals(listOf(committed), restored.effectiveNow())
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session), input(),
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
            actual, journal, snapshot(actual), input().copy(registeredDemonRole = null),
        )
        assertEquals(RegistrationResolutionStatusV1.EXPLICIT_ACTUAL.name,
            committed.selectedOutcome.canonicalFields.getValue("status"))
        assertEquals("actual", committed.selectedCandidateId)
        assertTrue(committed.registrations.isEmpty())
        assertEquals(TruthRelation.NOT_APPLICABLE, committed.truthRelation)
        assertEquals(listOf(committed), recover(actual, journal).effectiveNow())
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                actual, journal, snapshot(actual), input().copy(registeredDemonRole = null),
            )
        }
    }

    @Test fun `explicit ruling records what was confirmed without Host ability-functioning veto`() {
        val roster = basePlayers.map {
            if (it.seat == 1 || it.seat == 4) it.copy(poisoned = true) else it
        }
        val session = makeSession(roster)
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val committed = DayAbilityRegistrationRulingProducerV1.confirmSlayer(
            session, journal, snapshot(session), input(),
        )
        assertEquals(RoleId("Imp"), committed.registrations.single().registeredRole)
        assertEquals(listOf(committed), recover(session, journal).effectiveNow())
    }

    @Test fun `subject identity and snapshot phase still protect the fact record`() {
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session), input().copy(recluseSeat = 2),
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DayAbilityRegistrationRulingProducerV1.confirmSlayer(
                session, journal, snapshot(session, StorytellerPhase.NIGHT), input(),
            )
        }
        assertTrue(journal.archive().records.isEmpty())
    }

    @Test fun `tampered recovered day registration cannot forge a different subject`() {
        val session = makeSession()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        DayAbilityRegistrationRulingProducerV1.confirmSlayer(
            session, journal, snapshot(session), input(),
        )
        val archive = journal.archive()
        val recovered = archive.copy(records = archive.records.map { entry ->
            if (entry is StorytellerCausalJournalRecordV1.Committed) {
                entry.copy(value = entry.value.copy(selectedOutcome = entry.value.selectedOutcome.copy(
                    canonicalFields = (entry.value.selectedOutcome.canonicalFields + ("subjectSeat" to "2")).toSortedMap(),
                )))
            } else entry
        })
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerCausalDecisionJournalV1.restore(recovered, session.state)
        }
    }
}
