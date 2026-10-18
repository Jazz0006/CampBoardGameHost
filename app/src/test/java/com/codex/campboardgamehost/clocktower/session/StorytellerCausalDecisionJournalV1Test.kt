package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
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
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellerCausalDecisionJournalV1Test {
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

    private fun request(id: String, session: ClocktowerGameSession, journal: StorytellerCausalDecisionJournalV1):
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
            gameContext = journal.contextForRequest(snapshot, session.state),
        )
    }

    private fun event(name: String, selected: String = "seat-2", revision: StorytellerProviderRevisionV1) =
        StorytellerProviderPriorDecisionV1(
            eventId = "event-$name",
            gameStateRevision = revision.gameStateRevision,
            playerInputRevision = revision.playerInputRevision,
            selectedCandidateId = selected,
            selectedOutcome = DecisionOutcomeSnapshot("mayor-redirect", sortedMapOf("seat" to selected)),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            registrations = emptyList(),
        )

    @Test
    fun `confirmed Mayor commits one actual fact without candidate or duplicate journal writes`() {
        val game = session()
        fun mayorRequest(id: String): StorytellerProviderRequestV1 =
            request(id, game, game.causalDecisionJournal).copy(legalCandidates = emptyList())
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
        assertTrue(game.causalDecisionJournal.archive().records.isEmpty())

        // Re-tapping the same stable confirmation is truly a no-op, including the journal.
        val afterFirst = game.state
        val journalAfterFirst = game.causalDecisionJournal.archive()
        assertEquals(accepted, game.commitConfirmedMayorRedirect(choice, firstRequest, 0L))
        assertEquals(afterFirst, game.state)
        assertEquals(journalAfterFirst, game.causalDecisionJournal.archive())

        val bad = choice.copy(actionId = "mayor-invalid", targetSeat = 99, sequence = 2)
        assertThrows(IllegalArgumentException::class.java) {
            game.commitConfirmedMayorRedirect(
                bad, mayorRequest("night:2:mayor-redirect:invalid"),
                1L,
            )
        }
        assertEquals(afterFirst, game.state)
        assertEquals(journalAfterFirst, game.causalDecisionJournal.archive())

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
        assertTrue(game.causalDecisionJournal.archive().records.isEmpty())

        // The legal domain also permits letting the Mayor die: target == Mayor seat.
        game.commitConfirmedMayorRedirect(
            choice.copy(actionId = "mayor-night-two-no-redirect", targetSeat = 1, sequence = 3),
            mayorRequest("night:2:mayor-redirect:no-redirect"),
            2L,
        )
        assertEquals(3L, game.state.gameStateRevision)
        assertEquals(3L, game.state.nextTimelineGlobalSequence)
        assertEquals(1, game.state.nightTargets.mayorRedirectSeat)
        assertTrue(game.causalDecisionJournal.archive().records.isEmpty())

    }

    @Test
    fun `accepted corrections determine the next request without copying earlier history`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val aReq = request("A", session, journal)
        val a = journal.captureBeforeDecision(aReq, session.state)
        assertEquals(0L, a.exclusiveGlobalSequence)
        journal.commit("A", event("A", revision = a.revision))

        session.commitAcceptedTestAction(ActionFactDraft.Poison(
            "poison", StorytellerPhase.FIRST_NIGHT, 1, 1, 5,
        ))
        val bReq = request("B", session, journal)
        val b = journal.captureBeforeDecision(bReq, session.state)
        assertTrue(a.revision != b.revision)
        assertEquals(1L, b.exclusiveGlobalSequence)
        assertEquals(listOf("event-A"), bReq.gameContext.priorDecisions.map { it.eventId })
        journal.commit("B", event("B", "seat-4", b.revision))

        journal.correct("correct-A-to-B", "event-A", "event-B")
        val cReq = request("C", session, journal)
        assertEquals(listOf("event-B"), cReq.gameContext.priorDecisions.map { it.eventId })
        val c = journal.captureBeforeDecision(cReq, session.state)
        assertEquals(b.revision, c.revision)
        assertEquals(1L, c.exclusiveGlobalSequence)

    }

    @Test
    fun `capturing stale request at same game revision but changed timeline fails closed`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val stale = request("stale", session, journal)
        session.commitAcceptedTestAction(ActionFactDraft.Death(
            "new-action-same-revision", StorytellerPhase.DAWN, 1, 0, 2,
        ))
        assertThrows(IllegalArgumentException::class.java) {
            journal.captureBeforeDecision(stale, session.state)
        }
    }

    @Test
    fun `no legacy chronology or invented registration events enter the journal`() {
        val old = session(ClocktowerSemanticHistoryMode.LEGACY_LOCAL)
        val journal = StorytellerCausalDecisionJournalV1(old.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            journal.captureBeforeDecision(request("old", old, journal), old.state)
        }

        val session = session()
        val fresh = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val req = request("A", session, fresh)
        val captured = fresh.captureBeforeDecision(req, session.state)
        val withMadeUpRegistration = event("A", revision = captured.revision).copy(
            registrations = listOf(
                com.codex.campboardgamehost.clocktower.domain.RegistrationFact(
                    interactionId = "hypothetical",
                    subjectSeat = 4,
                    registeredAlignment = Alignment.EVIL,
                    registrationQuestion = com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion.ALIGNMENT,
                    reason = com.codex.campboardgamehost.clocktower.domain.RegistrationReason.RECLUSE_ABILITY,
                ),
            ),
        )
        assertThrows(IllegalArgumentException::class.java) {
            fresh.commit("A", withMadeUpRegistration)
        }
        assertThrows(IllegalStateException::class.java) {
            fresh.correct("before-commit", "event-A", "event-B")
        }
    }

    @Test
    fun `duplicate decision and correction identities fail closed`() {
        val session = session()
        val journal = StorytellerCausalDecisionJournalV1(session.state.gameId)
        val req = request("A", session, journal)
        val a = journal.captureBeforeDecision(req, session.state)
        assertThrows(IllegalArgumentException::class.java) {
            journal.captureBeforeDecision(req, session.state)
        }
        journal.commit("A", event("A", revision = a.revision))
        assertThrows(IllegalArgumentException::class.java) {
            journal.commit("A", event("A", revision = a.revision))
        }
        val b = journal.captureBeforeDecision(request("B", session, journal), session.state)
        journal.commit("B", event("B", "seat-4", b.revision))
        journal.correct("c", "event-A", "event-B")
        assertThrows(IllegalArgumentException::class.java) {
            journal.correct("c", "event-A", "event-B")
        }
        assertThrows(IllegalArgumentException::class.java) {
            journal.correct("backwards", "event-B", "event-A")
        }
    }
}
