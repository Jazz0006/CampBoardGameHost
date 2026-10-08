package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerSemanticHistoryPersistence
import com.codex.campboardgamehost.ClocktowerCausalJournalPersistence
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Archival regression ONLY: the synthetic TB/Spy/Klutz combination never occurs
 * in either of the production script role catalogs. Tests below validate old
 * Recovery decode/tamper semantics and shared death evidence, NOT live coverage.
 */
class LegacyKlutzSpyArchiveRecoveryCompatibilityTest {
    private val script = ScriptId("trouble_brewing")
    private val players = listOf(
        PlayerState(1, "Klutz", RoleId("Klutz"), Alignment.GOOD, CharacterType.OUTSIDER,
            alive = false, poisoned = false),
        PlayerState(2, "Spy", RoleId("Spy"), Alignment.EVIL, CharacterType.MINION),
        PlayerState(3, "Imp", RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
        PlayerState(4, "Chef", RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK),
        PlayerState(5, "Recluse", RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER),
    )
    private val roles = listOf(
        RoleDefinition(RoleId("Klutz"), Alignment.GOOD, CharacterType.OUTSIDER, setOf(script)),
        RoleDefinition(RoleId("Spy"), Alignment.EVIL, CharacterType.MINION, setOf(script)),
        RoleDefinition(RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON, setOf(script)),
        RoleDefinition(RoleId("Chef"), Alignment.GOOD, CharacterType.TOWNSFOLK, setOf(script)),
        RoleDefinition(RoleId("Recluse"), Alignment.GOOD, CharacterType.OUTSIDER, setOf(script)),
    )
    private fun session(roster: List<PlayerState> = players) =
        ClocktowerGameSession.createProduction(
            gameId = "day-klutz-spy-verified", gameSeed = 97L,
            initialState = GameState(script, roster, 97L),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    private fun snapshot(s: ClocktowerGameSession) = TroubleBrewingGameSnapshotV1(
        gameId = s.state.gameId,
        gameSeed = s.state.gameSeed,
        position = TroubleBrewingSnapshotPosition(
            stage = TroubleBrewingSnapshotStage.RUNTIME,
            phase = SnapshotField.Known(StorytellerPhase.DAY),
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
    private fun input(special: Boolean) = ConfirmedDayKlutzSpyRegistrationV1(
        interactionId = "day:2:klutz:1:2:death-klutz", klutzSeat = 1,
        chosenSpySeat = 2, spyRegistersGood = special,
    )
    private fun death(
        s: ClocktowerGameSession,
        poisonedAtDeath: Boolean = false,
        withKnownPoison: Boolean = true,
    ) {
        if (withKnownPoison) {
            s.commitGlobalActionFact(ActionFactDraft.Poison(
                "known-poison", StorytellerPhase.NIGHT, 1, 1, if (poisonedAtDeath) 1 else 4,
            ))
        }
        s.commitGlobalActionFact(ActionFactDraft.Death(
            "death-klutz", StorytellerPhase.DAWN, 2, 2, 1,
        ))
    }
    private fun roundtrip(
        s: ClocktowerGameSession,
        journal: StorytellerCausalDecisionJournalV1,
    ): StorytellerCausalDecisionJournalV1 {
        val archive = ClocktowerCausalJournalPersistence.decode(
            ClocktowerCausalJournalPersistence.encode(journal.archive()),
            expectedGameId = s.state.gameId,
            actions = s.state.actionTimeline,
            observations = s.state.epistemicObservationLog.records,
            recoveredCursor = s.state.nextTimelineGlobalSequence,
        )
        return StorytellerCausalDecisionJournalV1.restore(archive, s.state)
    }

    @Test fun `registered GOOD Klutz choice anchors real death and known sober provenance`() {
        val s = session()
        death(s)
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        val event = KlutzSpyDayRegistrationProducerV1.confirm(
            s, journal, snapshot(s), roles, input(true),
        )
        assertEquals("day-ability-registration", event.selectedOutcome.decisionType)
        assertEquals("special:alignment:GOOD", event.selectedCandidateId)
        val fact = event.registrations.single()
        assertNull(fact.registeredRole)
        assertNull(fact.registeredType)
        assertEquals(Alignment.GOOD, fact.registeredAlignment)
        assertEquals(RegistrationQuestion.ALIGNMENT, fact.registrationQuestion)
        assertEquals(RegistrationReason.SPY_ABILITY, fact.reason)
        assertEquals("death-klutz", event.selectedOutcome.canonicalFields["deathActionId"])
        assertEquals("known-poison", event.selectedOutcome.canonicalFields["lastPoisonActionId"])
        assertTrue(s.state.epistemicObservationLog.records.isEmpty())
        val decisionId = journal.archive().records.filterIsInstance<
            StorytellerCausalJournalRecordV1.Captured>().single().frozen.identity.decisionId
        assertTrue(journal.effectiveAt(decisionId).isEmpty())
        assertEquals(listOf(event), roundtrip(s, journal).effectiveNow())
        assertEquals(journal.frozenAt(decisionId), roundtrip(s, journal).frozenAt(decisionId))
        assertThrows(IllegalArgumentException::class.java) {
            KlutzSpyDayRegistrationProducerV1.confirm(
                s, journal, snapshot(s), roles, input(true),
            )
        }
    }

    @Test fun `explicit actual evil Spy is a distinct decision without special witness`() {
        val s = session()
        death(s)
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        val event = KlutzSpyDayRegistrationProducerV1.confirm(
            s, journal, snapshot(s), roles, input(false),
        )
        assertEquals("actual", event.selectedCandidateId)
        assertEquals("EXPLICIT_ACTUAL", event.selectedOutcome.canonicalFields["status"])
        assertEquals("true", event.selectedOutcome.canonicalFields["evilWins"])
        assertTrue(event.registrations.isEmpty())
        assertEquals(listOf(event), roundtrip(s, journal).effectiveNow())
    }

    @Test fun `missing death or poison chronology and poisoned-at-death fail closed`() {
        val noDeath = session()
        val noDeathJournal = StorytellerCausalDecisionJournalV1(noDeath.state.gameId)
        assertNull(KlutzDeathTriggerProvenanceResolverV1.verified(
            noDeath.state.actionTimeline.reducerFacts(), 1,
        ))
        assertThrows(IllegalArgumentException::class.java) {
            KlutzSpyDayRegistrationProducerV1.confirm(
                noDeath, noDeathJournal, snapshot(noDeath), roles, input(true),
            )
        }
        val noPoison = session()
        death(noPoison, withKnownPoison = false)
        assertNull(KlutzDeathTriggerProvenanceResolverV1.verified(
            noPoison.state.actionTimeline.reducerFacts(), 1,
        ))
        val poisonous = session()
        death(poisonous, poisonedAtDeath = true)
        assertNull(KlutzDeathTriggerProvenanceResolverV1.verified(
            poisonous.state.actionTimeline.reducerFacts(), 1,
        ))
        val journal = StorytellerCausalDecisionJournalV1(poisonous.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            KlutzSpyDayRegistrationProducerV1.confirm(
                poisonous, journal, snapshot(poisonous), roles, input(true),
            )
        }
        assertTrue(journal.archive().records.isEmpty())
    }

    @Test fun `non Klutz dead actor poisoned Spy or false chosen identity cannot create registration`() {
        val rosters = listOf(
            players.map { if (it.seat == 1) it.copy(actualRole = RoleId("Chef")) else it },
            players.map { if (it.seat == 1) it.copy(alive = true) else it },
            players.map { if (it.seat == 2) it.copy(poisoned = true) else it },
            players.map { if (it.seat == 2) it.copy(actualRole = RoleId("Chef")) else it },
            players.map { if (it.seat == 2) it.copy(alive = false) else it },
        )
        rosters.forEach { roster ->
            val s = session(roster)
            death(s)
            val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
            assertThrows(IllegalArgumentException::class.java) {
                KlutzSpyDayRegistrationProducerV1.confirm(
                    s, journal, snapshot(s), roles, input(true),
                )
            }
            assertTrue(journal.archive().records.isEmpty())
        }
    }

    /** New games can prove death-time ability state even with no Poisoner and no Poison action. */
    private fun captureCanonicalDeath(
        poisonedAtDeath: Boolean = false,
        byExecution: Boolean = false,
    ): ClocktowerGameSession {
        val aliveRoster = players.map { player ->
            if (player.seat == 1) player.copy(alive = true, poisoned = poisonedAtDeath)
            else player
        }
        val s = session(aliveRoster)
        val draft = if (byExecution) ActionFactDraft.Execution(
            "true-execution-klutz", StorytellerPhase.DAY, 2, 0, 1,
        ) else ActionFactDraft.Death(
            "true-death-klutz", StorytellerPhase.DAWN, 2, 0, 1,
        )
        val committed = s.commitGlobalActionFact(draft)
        val evidence = when (val fact = committed.fact) {
            is ActionFact.Death -> fact.klutzDeathTrigger
            is ActionFact.Execution -> fact.klutzDeathTrigger
            else -> null
        }
        assertEquals(RoleId("Klutz"), evidence?.actualRole)
        assertEquals(true, evidence?.wasAlive)
        assertEquals(poisonedAtDeath, evidence?.wasPoisoned)
        s.synchronizePlayerDeathWithinCurrentRevision(1)
        assertEquals(false, s.state.gameState.playerAt(1)?.alive)
        assertEquals(false, s.state.gameState.playerAt(1)?.poisoned)
        return s
    }

    @Test fun `unpoisoned Klutz with no Poisoner has session-owned trigger evidence and recovers`() {
        val s = captureCanonicalDeath()
        val proof = requireNotNull(KlutzDeathTriggerProvenanceResolverV1.verified(
            s.state.actionTimeline.reducerFacts(), 1,
        ))
        assertEquals("true-death-klutz", proof.deathActionId)
        assertTrue(proof.isVerifiedDeathSnapshot)
        assertEquals(null, proof.lastPoisonActionId)
        val archive = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(s.state.actionTimeline)
        val timeline = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(org.json.JSONObject().put(
            ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, archive,
        ))
        assertEquals(s.state.actionTimeline, timeline)
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        val event = KlutzSpyDayRegistrationProducerV1.confirm(
            s, journal, snapshot(s), roles,
            input(true).copy(interactionId = "day:2:klutz:1:2:true-death-klutz"),
        )
        assertEquals("SESSION_TRIGGER", event.selectedOutcome.canonicalFields["deathEvidenceKind"])
        assertEquals("", event.selectedOutcome.canonicalFields["lastPoisonActionId"])
        assertEquals(listOf(event), roundtrip(s, journal).effectiveNow())
        val id = journal.archive().records.filterIsInstance<
            StorytellerCausalJournalRecordV1.Captured>().single().frozen.identity.decisionId
        assertEquals(journal.frozenAt(id), roundtrip(s, journal).frozenAt(id))
    }

    @Test fun `canonical Klutz execution before death captures ability state without Poisoner`() {
        val s = captureCanonicalDeath(byExecution = true)
        val proof = requireNotNull(KlutzDeathTriggerProvenanceResolverV1.verified(
            s.state.actionTimeline.reducerFacts(), 1,
        ))
        assertEquals("true-execution-klutz", proof.deathActionId)
        assertTrue(proof.isVerifiedDeathSnapshot)
    }

    @Test fun `poisoned at death cannot be reclassified after death clears poisoned flag`() {
        val s = captureCanonicalDeath(poisonedAtDeath = true)
        assertEquals(null, KlutzDeathTriggerProvenanceResolverV1.verified(
            s.state.actionTimeline.reducerFacts(), 1,
        ))
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        assertThrows(IllegalArgumentException::class.java) {
            KlutzSpyDayRegistrationProducerV1.confirm(
                s, journal, snapshot(s), roles,
                input(true).copy(interactionId = "day:2:klutz:1:2:true-death-klutz"),
            )
        }
        assertTrue(journal.archive().records.isEmpty())
    }

    @Test fun `serialized death evidence rejects missing boolean or made up role`() {
        val s = captureCanonicalDeath()
        val array = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(s.state.actionTimeline)
        val factJson = array.getJSONObject(0).getJSONObject("fact")
        factJson.getJSONObject("klutzDeathTrigger").put("role", "Chef")
        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(org.json.JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, array,
            ))
        }
        factJson.getJSONObject("klutzDeathTrigger").put("role", "Klutz")
        factJson.getJSONObject("klutzDeathTrigger").remove("poisonedBeforeDeath")
        assertThrows(IllegalArgumentException::class.java) {
            ClocktowerSemanticHistoryPersistence.decodeActionTimeline(org.json.JSONObject().put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, array,
            ))
        }
    }

    @Test fun `recovered Klutz death action and registered alignment cannot be forged`() {
        val s = session()
        death(s)
        val journal = StorytellerCausalDecisionJournalV1(s.state.gameId)
        KlutzSpyDayRegistrationProducerV1.confirm(
            s, journal, snapshot(s), roles, input(true),
        )
        val json = ClocktowerCausalJournalPersistence.encode(journal.archive())
        json.getJSONArray("records").getJSONObject(1)
            .getJSONObject("outcomeFields").put("deathActionId", "forged-death")
        val decoded = ClocktowerCausalJournalPersistence.decode(
            json, s.state.gameId, s.state.actionTimeline,
            s.state.epistemicObservationLog.records, s.state.nextTimelineGlobalSequence,
        )
        assertThrows(IllegalArgumentException::class.java) {
            StorytellerCausalDecisionJournalV1.restore(decoded, s.state)
        }
    }
}
