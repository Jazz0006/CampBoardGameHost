package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderScalarKindV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import com.codex.campboardgamehost.clocktower.session.StorytellerStrategicContinuationV2.SemanticResult
import org.junit.Test

/** All actions use the SAME generic transition gate. No role-specific planner. */
class StorytellerStrategicChangeCheckpointV2Test {
    @Test fun currentInformationAlwaysUsesModelAdviceWithoutHostCandidateSelection() {
        assertFalse(StorytellerStrategicChangeCheckpointV2.pendingNeedsJudgment(emptyList()))
        assertTrue(StorytellerStrategicChangeCheckpointV2.pendingNeedsJudgment(listOf(
            StorytellerStrategicContinuationV2.OpenDecision("current", 1, "NIGHT_INFORMATION"))))
    }

    private val position = StorytellerStrategicChangeCheckpointV2.Position(
        StorytellerPhase.FIRST_NIGHT, 1)
    private val memory = HostOpeningStrategyMemoryV2(
        tensions = listOf("Cross-role information relationship"),
        intentions = listOf(HostOpeningStrategicIntentionV2(
            "Any meaningful confirmed Host change",
            "Maintain a plausible multi-night world",
            "Do not precommit an unknown future outcome")),
        uncertainties = emptyList(),
    )

    private fun session(secondRole: String = "Empath"): ClocktowerGameSession {
        val identities = listOf("Chef", secondRole, "Monk", "Poisoner", "Imp")
        val players = identities.mapIndexed { index, name ->
            val category = when (name) {
                "Poisoner" -> CharacterType.MINION
                "Imp" -> CharacterType.DEMON
                else -> CharacterType.TOWNSFOLK
            }
            PlayerState(
                seat = index + 1, name = "Player " + (index + 1),
                actualRole = RoleId(name), shownRole = RoleId(name),
                actualType = category,
                actualAlignment = if (category == CharacterType.TOWNSFOLK)
                    Alignment.GOOD else Alignment.EVIL,
            )
        }
        return ClocktowerGameSession.createProduction(
            "checkpoint-game", 17,
            GameState(ScriptId("trouble_brewing"), players, 17),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
    }

    private fun baseline(session: ClocktowerGameSession) =
        StorytellerStrategicChangeCheckpointV2.capture(session, position)

    private fun cursor() = StorytellerStrategicContinuationV2.MemoryCursor(
        "checkpoint-game", 0, 0, 0, 0, memory)

    private fun open(seat: Int = 2) = listOf(
        StorytellerStrategicContinuationV2.OpenDecision(
            "host-current-open-" + seat, seat, "NIGHT_INFORMATION", "Empath",
            StorytellerProviderDecisionContextV1.ScalarInformation(seat, RoleId("Empath"),
                ReliabilityState.RELIABLE, StorytellerProviderScalarKindV1.NUMBER,
                NumericMetric.LIVING_EVIL_NEIGHBOURS.name, emptyList())))

    @Test fun confirmedSetupContradictingAdvisoryOpeningCanRequestWithoutInventingAHostEvent() {
        val host = session()
        val current = baseline(host)
        val review = StorytellerStrategicChangeCheckpointV2.review(
            current, current, cursor(), open(),
            advisoryPlanContradictsConfirmedState = true,
        )
        assertEquals(
            StorytellerStrategicChangeCheckpointV2.Readiness.READY_FOR_WHOLE_GAME_REVIEW,
            review.readiness)
        assertTrue(review.changeKinds.isEmpty())
        val reply = StorytellerStrategicContinuationV2.Result(
            gameId = review.gameId,
            gameStateRevision = review.gameStateRevision,
            playerInputRevision = review.playerInputRevision,
            exclusiveGlobalSequence = review.exclusiveGlobalSequence,
            memoryVersion = 0,
            revisions = emptyList(),
            affectedOpenDecisionIds = emptySet(),
            remainingChoices = mapOf("host-current-open-2" to SemanticResult("SCALAR_RESULT", scalarValue = "99")),
            updatedMemory = memory,
            strategicDirection = "Reevaluate against actual confirmed setup.",
        )
        StorytellerStrategicChangeCheckpointV2.validateProposal(
            review, current, reply, cursor())
        assertTrue(runCatching {
            StorytellerStrategicChangeCheckpointV2.accept(
                review, current, reply, cursor())
        }.isFailure) // Unconfirmed advice cannot advance the model memory cursor.
        assertEquals(0L, host.state.gameStateRevision)
    }

    @Test fun chronologyWithoutNewFactsDoesNotStartAnotherPaidModelCall() {
        val host = session()
        val before = baseline(host)
        host.recordPlayerInput()
        val result = StorytellerStrategicChangeCheckpointV2.review(
            before, baseline(host), cursor(), open())
        assertEquals(
            StorytellerStrategicChangeCheckpointV2.Readiness.NO_MATERIAL_CHANGE,
            result.readiness)
        assertFalse(result.needsReview)

        val unconfirmedScreenOnly = before.copy(
            position = StorytellerStrategicChangeCheckpointV2.Position(
                StorytellerPhase.DAY, 1))
        val noAuthority = StorytellerStrategicChangeCheckpointV2.review(
            before, unconfirmedScreenOnly, cursor(), open())
        assertEquals(
            StorytellerStrategicChangeCheckpointV2.Readiness.HOST_REVISION_REQUIRED,
            noAuthority.readiness)
    }

    @Test fun actualCurrentPlayerQueryCanRequestBeforeItsObservationWithoutInventingFacts() {
        val host = session("Fortune Teller")
        val before = baseline(host)
        host.recordPlayerInput()
        val query = listOf(StorytellerStrategicContinuationV2.OpenDecision(
            "ft-2-query-3-5", 2, "NIGHT_INFORMATION", "Fortune Teller",
            StorytellerProviderDecisionContextV1.ScalarInformation(2, RoleId("Fortune Teller"),
                ReliabilityState.RELIABLE, StorytellerProviderScalarKindV1.BOOLEAN,
                "DEMON_OR_RED_HERRING_PRESENT", listOf(3, 5)),
        ))
        val review = StorytellerStrategicChangeCheckpointV2.review(
            before, baseline(host), cursor(), query, currentPlayerQuerySupplied = true,
        )
        assertTrue(review.needsReview)
        assertTrue(StorytellerStrategicChangeCheckpointV2.ChangeKind.PLAYER_CONTEXT_OR_CLAIMS in review.changeKinds)
        assertTrue(host.state.actionTimeline.entries.isEmpty())
        assertTrue(host.state.epistemicObservationLog.records.isEmpty())
        assertEquals(0L, host.state.gameStateRevision)
        val unchanged = baseline(host)
        assertFalse(StorytellerStrategicChangeCheckpointV2.review(
            unchanged, unchanged, cursor(), query, currentPlayerQuerySupplied = true,
        ).needsReview)
    }

    @Test fun confirmedActionsPlayerClaimsDeathAndObservationsUseTheSameGate() {
        val host = session()
        var last = baseline(host)
        val evidence = mutableSetOf<StorytellerStrategicChangeCheckpointV2.ChangeKind>()
        host.commitAcceptedTestAction(ActionFactDraft.Protect(
            "protect-1", StorytellerPhase.FIRST_NIGHT, 1, 0, 2))
        var verdict = StorytellerStrategicChangeCheckpointV2.review(
            last, baseline(host), cursor(), open())
        assertTrue(verdict.needsReview)
        evidence += verdict.changeKinds
        assertTrue(
            StorytellerStrategicChangeCheckpointV2.ChangeKind.CONFIRMED_ACTION in
                verdict.changeKinds)
        last = baseline(host)

        host.updateStorytellerPlayerContext(3,
            StorytellerPlayerContextInputV1(claimedRoleIds = listOf(RoleId("Mayor"))))
        verdict = StorytellerStrategicChangeCheckpointV2.review(
            last, baseline(host), cursor(), open())
        assertTrue(verdict.needsReview)
        evidence += verdict.changeKinds
        assertTrue(
            StorytellerStrategicChangeCheckpointV2.ChangeKind.PLAYER_CONTEXT_OR_CLAIMS in
                verdict.changeKinds)
        last = baseline(host)

        val nextPlayers = host.state.gameState.players.map {
            if (it.seat == 3) it.copy(alive = false) else it
        }
        host.updateGameState(host.state.gameState.copy(players = nextPlayers))
        verdict = StorytellerStrategicChangeCheckpointV2.review(
            last, baseline(host), cursor(), open())
        assertTrue(verdict.needsReview)
        evidence += verdict.changeKinds
        last = baseline(host)

        host.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-private-result", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 1, sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 0),
        ))
        verdict = StorytellerStrategicChangeCheckpointV2.review(
            last, baseline(host), cursor(), open(2))
        assertTrue(verdict.needsReview)
        assertTrue(
            StorytellerStrategicChangeCheckpointV2.ChangeKind.PLAYER_RECEIVED_OBSERVATION
                in verdict.changeKinds)
        assertTrue(StorytellerStrategicChangeCheckpointV2.ChangeKind.MECHANICAL_STATE in evidence)
        assertTrue(runCatching {
            StorytellerStrategicChangeCheckpointV2.review(
                last, baseline(host), cursor(), open(1))
        }.isFailure) // Chef observation is immutable and cannot be re-opened.
        assertEquals(
            StorytellerStrategicChangeCheckpointV2.Readiness.NO_CURRENT_HOST_OPEN_DECISION,
            StorytellerStrategicChangeCheckpointV2.review(
                last, baseline(host), cursor(), emptyList()).readiness)
    }

    @Test fun newestChronologyOnlyCanAdvanceStrategyCursorOnce() {
        val host = session()
        val before = baseline(host)
        host.commitAcceptedTestAction(ActionFactDraft.Protect(
            "protect-2", StorytellerPhase.FIRST_NIGHT, 1, 0, 2))
        val current = baseline(host)
        val gate = StorytellerStrategicChangeCheckpointV2.review(
            before, current, cursor(), open())
        val reply = StorytellerStrategicContinuationV2.Result(
            gameId = gate.gameId,
            gameStateRevision = gate.gameStateRevision,
            playerInputRevision = gate.playerInputRevision,
            exclusiveGlobalSequence = gate.exclusiveGlobalSequence,
            memoryVersion = 0,
            revisions = listOf(StorytellerStrategicContinuationV2.Revision(
                0, "REVISE", "New confirmed actual game event")),
            affectedOpenDecisionIds = setOf("host-current-open-2"),
            remainingChoices = mapOf("host-current-open-2" to SemanticResult("SCALAR_RESULT", scalarValue = "99")),
            updatedMemory = memory,
            strategicDirection = "Reevaluate coupled unexecuted information.",
        )
        val advanced = StorytellerStrategicChangeCheckpointV2.accept(
            gate, current, reply, cursor())
        assertEquals(1, advanced.version)
        assertTrue(runCatching {
            StorytellerStrategicChangeCheckpointV2.accept(
                gate, current, reply, advanced)
        }.isFailure)

        host.recordPlayerInput()
        assertTrue(runCatching {
            StorytellerStrategicChangeCheckpointV2.accept(
                gate, baseline(host), reply, cursor())
        }.isFailure)
    }

    private fun scalarRequest(host: ClocktowerGameSession): StorytellerProviderRequestV1 {
        val state = host.state
        val snapshot = TroubleBrewingGameSnapshotV1(
            gameId = state.gameId, gameSeed = state.gameSeed,
            position = TroubleBrewingSnapshotPosition(
                TroubleBrewingSnapshotStage.RUNTIME,
                SnapshotField.Known(StorytellerPhase.FIRST_NIGHT),
                SnapshotField.Known(1)),
            grimoireSeats = state.gameState.players.map { player ->
                TroubleBrewingSnapshotSeat(
                    player.seat,
                    SnapshotField.Known((player.shownRole ?: player.actualRole).value.lowercase()),
                    SnapshotField.Known(player.actualRole.value.lowercase()),
                    SnapshotField.Known(player.alive),
                    SnapshotField.Known(player.poisoned))
            },
            setupState = TroubleBrewingSnapshotSetupState(
                SnapshotField.Known(false), SnapshotField.NotApplicable),
        )
        val context = StorytellerProviderDecisionContextV1.ScalarInformation(
            sourceSeat = 1, abilityRole = RoleId("Chef"),
            reliability = ReliabilityState.RELIABLE,
            kind = StorytellerProviderScalarKindV1.NUMBER,
            metric = "ADJACENT_EVIL_PAIRS", subjectSeats = emptyList(),
        )
        return StorytellerProviderRequestV1(
            identity = StorytellerProviderDecisionIdentityV1(
                state.gameId, "trouble_brewing", context.decisionTypeId,
                "current-chef-info"),
            sourceRevision = StorytellerProviderRevisionV1(
                state.gameStateRevision, state.playerInputRevision),
            state = StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
            decisionContext = context,
            legalCandidates = listOf(StorytellerProviderCandidateV1(
                "number-0", StorytellerProviderCandidatePayloadV1.ScalarResult("0"))),
        )
    }

    @Test fun onlyExactlyPublishedCurrentHostSemanticsCanConfirmModelMemory() {
        val request = scalarRequest(session())
        fun publication(value: Int, seat: Int = 1, role: String = "Chef") =
            EpistemicObservationDraft(
                recordId = "actually-published", phase = StorytellerPhase.FIRST_NIGHT,
                round = 1, sequence = 1, sourceSeat = seat,
                sourceAbility = RoleId(role), visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(seat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = InformationProposition.NumericResult(
                    NumericMetric.ADJACENT_EVIL_PAIRS, seat, value = value),
            )
        val matcher = com.codex.campboardgamehost.ClocktowerResidualDisplayMatcherV2
        assertTrue(matcher.matchesConfirmedPublication(request, SemanticResult("SCALAR_RESULT", scalarValue = "0"), publication(0)))
        assertFalse(matcher.matchesConfirmedPublication(request, SemanticResult("SCALAR_RESULT", scalarValue = "0"), publication(1)))
        assertFalse(matcher.matchesConfirmedPublication(request, SemanticResult("SCALAR_RESULT", scalarValue = "0"), publication(0, 2)))
        assertFalse(matcher.matchesConfirmedPublication(request, SemanticResult("SCALAR_RESULT", scalarValue = "0"),
            publication(0, role = "Empath")))
        assertFalse(matcher.matchesConfirmedPublication(request, SemanticResult("SCALAR_RESULT", scalarValue = "99"), publication(0)))
        assertFalse(matcher.matchesConfirmedPublication(request, SemanticResult("SCALAR_RESULT", scalarValue = "0"),
            publication(0).copy(visibility = ObservationVisibility.PUBLIC, recipientSeats = emptySet())))
    }

    @Test fun residualPackageGateRequiresCurrentWholeHostDomainBeforeAnyAdoption() {
        val host = session()
        val first = scalarRequest(host)
        val second = first.copy(
            identity = first.identity.copy(decisionId = "second-current-choice"),
            decisionContext = (first.decisionContext as
                StorytellerProviderDecisionContextV1.ScalarInformation).copy(sourceSeat = 2),
            legalCandidates = listOf(StorytellerProviderCandidateV1(
                "number-1", StorytellerProviderCandidatePayloadV1.ScalarResult("1"))),
        )
        val requests = listOf(first, second)
        val original = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            host.state, StorytellerPhase.FIRST_NIGHT, 1, requests)
        val reply = StorytellerStrategicContinuationV2.Result(
            gameId = host.state.gameId,
            gameStateRevision = host.state.gameStateRevision,
            playerInputRevision = host.state.playerInputRevision,
            exclusiveGlobalSequence = host.state.nextTimelineGlobalSequence,
            memoryVersion = 0,
            revisions = emptyList(), affectedOpenDecisionIds = original.map { it.decisionId }.toSet(),
            remainingChoices = mapOf("current-chef-info" to SemanticResult("SCALAR_RESULT", scalarValue = "0"), "second-current-choice" to SemanticResult("SCALAR_RESULT", scalarValue = "1")),
            updatedMemory = memory, strategicDirection = "Linked current decisions",
        )
        val accepted = StorytellerResidualPackageGateV2.verify(
            host.state, StorytellerPhase.FIRST_NIGHT, 1, original, requests, reply, cursor())
        assertEquals(2, accepted.choices.size)
        assertTrue(runCatching {
            val sameSource = second.copy(decisionContext = first.decisionContext)
            HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
                host.state, StorytellerPhase.FIRST_NIGHT, 1, listOf(first, sameSource))
        }.isFailure) // distinct IDs cannot give one seat two concurrent observations
        assertTrue(runCatching {
            StorytellerResidualPackageGateV2.verify(host.state, StorytellerPhase.FIRST_NIGHT, 1,
                original, requests.dropLast(1), reply, cursor())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerResidualPackageGateV2.verify(host.state, StorytellerPhase.FIRST_NIGHT, 1,
                original, requests, reply.copy(remainingChoices = mapOf("current-chef-info" to SemanticResult("SCALAR_RESULT", scalarValue = "0"))), cursor())
        }.isFailure)
        assertTrue(runCatching {
            StorytellerResidualPackageGateV2.verify(host.state, StorytellerPhase.FIRST_NIGHT, 1,
                original, listOf(first, second.copy(decisionContext =
                    (second.decisionContext as StorytellerProviderDecisionContextV1.ScalarInformation)
                        .copy(subjectSeats = listOf(3)))), reply, cursor())
        }.isFailure) // same opaque ID but changed public meaning
        host.recordPlayerInput()
        assertTrue(runCatching {
            StorytellerResidualPackageGateV2.verify(host.state, StorytellerPhase.FIRST_NIGHT, 1,
                original, requests, reply, cursor())
        }.isFailure)
    }

    @Test fun onlyCurrentRulesOwnedPendingDecisionBecomesAnOpenId() {
        val host = session()
        val request = scalarRequest(host)
        val correct = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            host.state, StorytellerPhase.FIRST_NIGHT, 1, listOf(request))
        assertEquals(listOf("current-chef-info"), correct.map { it.decisionId })
        assertEquals("NIGHT_INFORMATION", correct.single().family)
        assertEquals(1, correct.single().sourceSeat)
        assertEquals("Chef", correct.single().abilityRoleId)
        // Multiple independent Host-owned requests form ONE coherent residual scope.
        val secondContext = StorytellerProviderDecisionContextV1.ScalarInformation(
            sourceSeat = 2, abilityRole = RoleId("Empath"),
            reliability = ReliabilityState.RELIABLE,
            kind = StorytellerProviderScalarKindV1.NUMBER,
            metric = "ADJACENT_EVIL_PLAYERS", subjectSeats = emptyList())
        val second = request.copy(
            identity = request.identity.copy(decisionId = "current-empath-info"),
            decisionContext = secondContext,
            legalCandidates = listOf(StorytellerProviderCandidateV1(
                "number-1", StorytellerProviderCandidatePayloadV1.ScalarResult("1"))),
        )
        val whole = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            host.state, StorytellerPhase.FIRST_NIGHT, 1, listOf(request, second))
        assertEquals(listOf("current-chef-info", "current-empath-info"),
            whole.map { it.decisionId })
        assertTrue(runCatching {
            HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
                host.state, StorytellerPhase.FIRST_NIGHT, 1,
                listOf(request, request))
        }.isFailure)
        host.updateStorytellerPlayerContext(3,
            StorytellerPlayerContextInputV1(claimedRoleIds = listOf(RoleId("Mayor"))))
        assertTrue(runCatching {
            HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
                host.state, StorytellerPhase.FIRST_NIGHT, 1, listOf(request))
        }.isFailure) // stale input revision must regenerate legal Host scope.

        host.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "published-chef", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = 0, sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                NumericMetric.ADJACENT_EVIL_PAIRS, 1, value = 0),
        ))
        // An otherwise current Host provider request still cannot re-open
        // an observation that the recipient has already received.
        val freshAfterPublished = scalarRequest(host)
        assertTrue(runCatching {
            HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
                host.state, StorytellerPhase.FIRST_NIGHT, 1, listOf(freshAfterPublished))
        }.isFailure)
    }
}
