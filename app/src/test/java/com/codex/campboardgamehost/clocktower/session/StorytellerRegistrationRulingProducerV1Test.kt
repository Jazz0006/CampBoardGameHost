package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerConfirmedRegistrationChoiceV1
import com.codex.campboardgamehost.ClocktowerConfirmedRegistrationPublicationV1
import com.codex.campboardgamehost.ClocktowerRegistrationWitness
import com.codex.campboardgamehost.ClocktowerCausalJournalPersistence
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerRegistrationRulingProducerV1Test {
    private val script = ScriptId("trouble_brewing")
    private val players = listOf(
        PlayerState(1, "Chef", RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(2, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION),
        PlayerState(3, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
        PlayerState(4, "Recluse", RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER),
        PlayerState(5, "Empath", RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK),
    )
    private val roles = listOf(
        RoleDefinition(RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Empath"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Saint"), Alignment.GOOD, CharacterType.OUTSIDER, setOf(script)),
        RoleDefinition(RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, setOf(script)),
        RoleDefinition(RoleId("Poisoner"), Alignment.EVIL, CharacterType.MINION, setOf(script)),
        RoleDefinition(RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, setOf(script)),
    )

    private fun session(poisonedEmpath: Boolean = false): ClocktowerGameSession =
        ClocktowerGameSession.createProduction(
            gameId = "registration-case",
            gameSeed = 42L,
            initialState = GameState(
                script, players.map { p -> if (p.seat == 5 && poisonedEmpath) p.copy(poisoned = true) else p }, 42L,
            ),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )

    private fun snapshot(session: ClocktowerGameSession): TroubleBrewingGameSnapshotV1 =
        TroubleBrewingGameSnapshotV1(
            gameId = session.state.gameId,
            gameSeed = session.state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                stage = TroubleBrewingSnapshotStage.RUNTIME,
                phase = SnapshotField.Known(StorytellerPhase.FIRST_NIGHT),
                round = SnapshotField.Known(1),
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

    private fun show(
        session: ClocktowerGameSession,
        recordId: String,
        sourceSeat: Int,
        unreliable: Boolean = false,
    ) {
        session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = recordId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = if (sourceSeat == 1) 1 else 2,
            sourceSeat = sourceSeat,
            sourceAbility = if (sourceSeat == 1) RoleId("Chef") else RoleId("Empath"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = if (unreliable) ObservationReliability.KNOWN_MALFUNCTIONING
                else ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                if (sourceSeat == 1) NumericMetric.ADJACENT_EVIL_PAIRS else NumericMetric.LIVING_EVIL_NEIGHBOURS,
                sourceSeat,
                if (sourceSeat == 1) (1..5).toList() else listOf(4, 1),
                1,
            ),
        ))
    }

    private fun select(
        session: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
        id: String,
        observation: String,
        source: Int,
        subject: Int,
        status: RegistrationResolutionStatusV1,
        role: RoleId? = null,
    ): StorytellerProviderPriorDecisionV1 = StorytellerRegistrationRulingProducerV1.confirm(
        session, journal, snapshot(session),
        ConfirmedRegistrationResolutionInputV1(
            interactionId = "first_night:role:${if (source == 1) "Chef" else "Empath"}",
            observationRecordId = observation,
            sourceSeat = source,
            subjectSeat = subject,
            question = RegistrationQuestion.ROLE,
            status = status,
            selectedRoleId = role,
        ),
        allowedRoles = roles,
        decisionId = id,
    )

    private fun recover(session: ClocktowerGameSession, journal: StorytellerCausalDecisionJournalV1):
        StorytellerCausalDecisionJournalV1 {
        val encoded = ClocktowerCausalJournalPersistence.encode(journal.archive())
        val decoded = ClocktowerCausalJournalPersistence.decode(
            encoded,
            expectedGameId = session.state.gameId,
            actions = session.state.actionTimeline,
            observations = session.state.epistemicObservationLog.records,
            recoveredCursor = session.state.nextTimelineGlobalSequence,
        )
        return StorytellerCausalDecisionJournalV1.restore(decoded, session.state)
    }

    @Test
    fun `unresolved shows only result and special explicit selection remains independently typed`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        show(session, "empath-one", sourceSeat = 5)
        val unresolved = select(session, journal, "first", "empath-one", 5, 4,
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED)
        assertTrue(unresolved.registrations.isEmpty())
        assertEquals("unresolved", unresolved.selectedCandidateId)
        val previous = journal.frozenAt("first")
        assertEquals(listOf("empath-one"), previous.historyPrefix.entries.map { it.entryId })

        val explicit = select(session, journal, "second", "empath-one", 5, 4,
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, RoleId("Imp"))
        assertEquals(RegistrationReason.RECLUSE_ABILITY, explicit.registrations.single().reason)
        assertEquals(Alignment.EVIL, explicit.registrations.single().registeredAlignment)
        assertEquals(RoleId("Imp"), explicit.registrations.single().registeredRole)
        assertEquals(listOf(unresolved.eventId), journal.effectiveAt("second").map { it.eventId })
        journal.correct("correction-1", unresolved.eventId, explicit.eventId)
        assertEquals(listOf(explicit), journal.effectiveNow())
        assertTrue(journal.effectiveAt("first").isEmpty())
        assertEquals(listOf(unresolved.eventId), journal.effectiveAt("second").map { it.eventId })
        val restored = recover(session, journal)
        assertEquals(journal.effectiveNow(), restored.effectiveNow())
        assertEquals(journal.effectiveAt("second"), restored.effectiveAt("second"))
        assertEquals(previous, restored.frozenAt("first"))
    }

    @Test
    fun `Spy and Recluse rulings for separate role interactions cannot leak or pick a first role`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        show(session, "chef-one", sourceSeat = 1)
        val spy = select(session, journal, "chef-spy", "chef-one", 1, 2,
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL)
        assertEquals(Alignment.GOOD, spy.registrations.single().registeredAlignment)
        assertEquals(null, spy.registrations.single().registeredRole)
        show(session, "empath-one", sourceSeat = 5)
        val recluse = select(session, journal, "empath-recluse", "empath-one", 5, 4,
            RegistrationResolutionStatusV1.EXPLICIT_ACTUAL)
        assertEquals("actual", recluse.selectedCandidateId)
        assertTrue(recluse.registrations.isEmpty())
        assertEquals(listOf(spy, recluse), recover(session, journal).effectiveNow())
        assertThrows(IllegalArgumentException::class.java) {
            select(session, journal, "invalid", "empath-one", 5, 4,
                RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, RoleId("Chef"))
        }
    }

    @Test
    fun `known malfunction cannot fabricate a witness and unrecorded history cannot be confirmed`() {
        val session = session(poisonedEmpath = true)
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        show(session, "poisoned-empath", sourceSeat = 5, unreliable = true)
        val recorded = select(session, journal, "not-applicable", "poisoned-empath", 5, 4,
            RegistrationResolutionStatusV1.NOT_APPLICABLE)
        assertEquals("not-applicable", recorded.selectedCandidateId)
        assertTrue(recorded.registrations.isEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            select(session, journal, "false-ruling", "poisoned-empath", 5, 4,
                RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, RoleId("Imp"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ConfirmedRegistrationResolutionInputV1(
                "first_night:role:Empath", "poisoned-empath", 5, 4,
                RegistrationQuestion.ROLE, RegistrationResolutionStatusV1.UNAVAILABLE_OR_UNRECORDED,
            )
        }
        assertEquals(listOf(recorded), recover(session, journal).effectiveNow())
    }

    @Test
    fun `malformed registration subject and unobserved output fail strict Recovery`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        show(session, "empath-one", sourceSeat = 5)
        select(session, journal, "r", "empath-one", 5, 4,
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL, RoleId("Imp"))
        val encoded = ClocktowerCausalJournalPersistence.encode(journal.archive())
        encoded.getJSONArray("records").getJSONObject(1).getJSONArray("registrations")
            .getJSONObject(0).put("subjectSeat", 2)
        val broken = ClocktowerCausalJournalPersistence.decode(
            encoded,
            expectedGameId = session.state.gameId,
            actions = session.state.actionTimeline,
            observations = session.state.epistemicObservationLog.records,
            recoveredCursor = session.state.nextTimelineGlobalSequence,
        )
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerCausalDecisionJournalV1.restore(broken, session.state)
        }

        val missing = session()
        val newJournal = StorytellerCausalDecisionJournalV1(missing.state.gameId)
        assertThrows(IllegalStateException::class.java) {
            select(missing, newJournal, "unpublished", "never-recorded", 5, 4,
                RegistrationResolutionStatusV1.EXPLICIT_SPECIAL)
        }
    }

    private fun chefPublication(
        spyStatus: RegistrationResolutionStatusV1,
        recluseStatus: RegistrationResolutionStatusV1,
    ) = ClocktowerConfirmedRegistrationPublicationV1(
        interactionId = "FirstNight:1:first_night:role:Chef",
        observationRecordId = "chef-one",
        sourceSeat = 1,
        shownProposition = InformationProposition.NumericResult(
            NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..5).toList(), 1,
        ),
        choices = listOf(
            ClocktowerConfirmedRegistrationChoiceV1(2, RegistrationQuestion.ALIGNMENT, spyStatus),
            ClocktowerConfirmedRegistrationChoiceV1(4, RegistrationQuestion.ALIGNMENT, recluseStatus),
        ),
        legalResultWitnesses = listOf(
            ClocktowerRegistrationWitness(spyRegistersGood = false, recluseRegistersEvil = false),
            ClocktowerRegistrationWitness(spyRegistersGood = true, recluseRegistersEvil = true),
        ),
    )

    @Test
    fun `real Host writer idempotently confirms and corrects same observed result then recovers`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        show(session, "chef-one", sourceSeat = 1)
        val unresolved = chefPublication(
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
            RegistrationResolutionStatusV1.UNRESOLVED_NOT_REQUIRED,
        )
        val first = ClocktowerConfirmedRegistrationHostWriterV1.commit(
            unresolved, session, journal, snapshot(session), roles,
        )
        assertEquals(2, first.size)
        assertEquals(emptyList<StorytellerProviderPriorDecisionV1>(),
            ClocktowerConfirmedRegistrationHostWriterV1.commit(
                unresolved, session, journal, snapshot(session), roles,
            ))
        val corrected = unresolved.copy(choices = unresolved.choices.map { choice ->
            if (choice.subjectSeat == 2) choice.copy(
                status = RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
            ) else choice
        })
        val revised = ClocktowerConfirmedRegistrationHostWriterV1.commit(
            corrected, session, journal, snapshot(session), roles,
        )
        assertEquals(1, revised.size)
        assertEquals(Alignment.GOOD, revised.single().registrations.single().registeredAlignment)
        assertEquals(null, revised.single().registrations.single().registeredRole)
        assertEquals(2, journal.effectiveNow().size)
        assertEquals(1, journal.effectiveNow().count { it.registrations.isNotEmpty() })
        // Reopened UI or Recovery has no transient manual selection. An absent toggle does
        // not revoke the last explicitly confirmed registration.
        assertTrue(ClocktowerConfirmedRegistrationHostWriterV1.commit(
            unresolved, session, journal, snapshot(session), roles,
        ).isEmpty())
        assertEquals(1, journal.effectiveNow().count { it.registrations.isNotEmpty() })
        val rewrittenDecisionId = journal.archive().records
            .filterIsInstance<StorytellerCausalJournalRecordV1.Committed>()
            .last().decisionId
        assertEquals(first.map { it.eventId },
            journal.effectiveAt(rewrittenDecisionId).map { it.eventId })
        val restored = recover(session, journal)
        assertEquals(journal.effectiveNow(), restored.effectiveNow())
        assertEquals(journal.effectiveAt(rewrittenDecisionId), restored.effectiveAt(rewrittenDecisionId))
    }

    @Test
    fun `real Host writer rejects incompatible joint witness before mutating journal`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        show(session, "chef-one", sourceSeat = 1)
        val impossible = chefPublication(
            RegistrationResolutionStatusV1.EXPLICIT_SPECIAL,
            RegistrationResolutionStatusV1.EXPLICIT_ACTUAL,
        )
        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerConfirmedRegistrationHostWriterV1.commit(
                impossible, session, journal, snapshot(session), roles,
            )
        }
        assertTrue(journal.archive().records.isEmpty())
        val stale = impossible.copy(observationRecordId = "not-observed")
        assertTrue(ClocktowerConfirmedRegistrationHostWriterV1.commit(
            stale, session, journal, snapshot(session), roles,
        ).isEmpty())
        assertTrue(journal.archive().records.isEmpty())
    }
}
