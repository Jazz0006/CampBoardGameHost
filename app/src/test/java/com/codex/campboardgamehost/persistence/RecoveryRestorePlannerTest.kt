package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInput
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInputJsonCodec
import com.codex.campboardgamehost.clocktower.recommendation.sde.SdeHistoricalReplayInputOrigin
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryRestorePlannerTest {
    @Test
    fun supportedRecentStableSnapshotParsesWithoutRawScreen() {
        val json = RecoverySnapshotJsonCodec.encode(undercoverSnapshot(savedAtMillis = NOW - 60_000L))
        assertFalse(json.has("screen"))

        val result = prepare(json)

        assertTrue(result is RecoveryPlanPreparation.Ready)
        val plan = (result as RecoveryPlanPreparation.Ready).plan
        assertEquals(RecoverySafeReentry.UndercoverGame, plan.safeReentry)
        assertFalse(plan.presentResults)
    }

    @Test
    fun exactFourHourBoundaryIsEligibleButOlderSnapshotIsExpired() {
        val boundary = prepare(
            RecoverySnapshotJsonCodec.encode(undercoverSnapshot(savedAtMillis = NOW - FOUR_HOURS_MILLIS)),
        )
        val expired = prepare(
            RecoverySnapshotJsonCodec.encode(undercoverSnapshot(savedAtMillis = NOW - FOUR_HOURS_MILLIS - 1L)),
        )

        assertTrue(boundary is RecoveryPlanPreparation.Ready)
        assertRejected(expired, RecoveryRejectionReason.Expired)
    }

    @Test
    fun futureTimestampIsRejected() {
        val result = prepare(
            RecoverySnapshotJsonCodec.encode(undercoverSnapshot(savedAtMillis = NOW + 1L)),
        )

        assertRejected(result, RecoveryRejectionReason.InvalidTimestamp)
    }

    @Test
    fun wrongFormatAndCompatibilityTokenAreRejected() {
        val wrongFormat = RecoverySnapshotJsonCodec.encode(undercoverSnapshot()).apply {
            put(RecoverySnapshotJsonCodec.FORMAT_VERSION_KEY, RecoverySnapshot.CURRENT_FORMAT_VERSION + 1)
        }
        val wrongToken = RecoverySnapshotJsonCodec.encode(undercoverSnapshot()).apply {
            put(RecoverySnapshotJsonCodec.COMPATIBILITY_TOKEN_KEY, "other-build")
        }

        assertRejected(prepare(wrongFormat), RecoveryRejectionReason.UnsupportedFormat)
        assertRejected(prepare(wrongToken), RecoveryRejectionReason.CompatibilityMismatch)
    }

    @Test
    fun stableRecoveryRejectsNonzeroDealContinuationIndex() {
        val json = RecoverySnapshotJsonCodec.encode(undercoverSnapshot()).apply {
            put("currentDealIndex", 1)
        }

        assertRejected(prepare(json), RecoveryRejectionReason.InvalidGameState)
    }

    @Test
    fun removedWerewolfRuntimeRecoveryFailsClosed() {
        val json = RecoverySnapshotJsonCodec.encode(undercoverSnapshot()).apply {
            put("currentGameKind", "Werewolf")
        }

        assertRejected(prepare(json), RecoveryRejectionReason.MalformedPayload)
    }

    @Test
    fun malformedCardDoesNotSilentlyProducePartialRecovery() {
        val json = RecoverySnapshotJsonCodec.encode(undercoverSnapshot()).apply {
            getJSONArray("cards").put(JSONObject().put("name", "Broken"))
        }

        assertRejected(prepare(json), RecoveryRejectionReason.MalformedPayload)
    }

    @Test
    fun malformedRecordDoesNotSilentlyDisappear() {
        val json = RecoverySnapshotJsonCodec.encode(undercoverSnapshot()).apply {
            put("records", JSONArray().put(JSONObject().put("round", 1)))
        }

        assertRejected(prepare(json), RecoveryRejectionReason.MalformedPayload)
    }

    @Test
    fun malformedClocktowerEventDoesNotSilentlyDisappear() {
        val json = RecoverySnapshotJsonCodec.encode(clocktowerSnapshot()).apply {
            put("clocktowerEvents", JSONArray().put(JSONObject().put("title", "missing required event fields")))
        }

        assertRejected(prepare(json), RecoveryRejectionReason.MalformedPayload)
    }

    @Test
    fun outOfRangeTimelineSeatFailsValidationBeforeApplication() {
        val fact = JSONObject().apply {
            put("actionId", "attack-1")
            put("sequence", 1L)
            put("kind", "attack")
            put("targetSeat", 99)
        }
        val point = JSONObject().apply {
            put("phase", "NIGHT")
            put("round", 1)
            put("sequence", 1)
            put("globalSequence", 1L)
        }
        val json = RecoverySnapshotJsonCodec.encode(clocktowerSnapshot()).apply {
            put(
                ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY,
                JSONArray().put(JSONObject().put("fact", fact).put("point", point)),
            )
        }

        assertRejected(prepare(json), RecoveryRejectionReason.InvalidGameState)
    }

    @Test
    fun malformedEpistemicObservationFailsClosed() {
        val json = RecoverySnapshotJsonCodec.encode(clocktowerSnapshot()).apply {
            put("clocktowerEpistemicObservations", JSONArray().put(17))
        }

        assertRejected(prepare(json), RecoveryRejectionReason.MalformedPayload)
    }

    @Test
    fun clocktowerConfirmedFactsRoundTripWhileDraftTargetsRemainAbsent() {
        val json = RecoverySnapshotJsonCodec.encode(clocktowerSnapshot())
        val decoded = RecoverySnapshotJsonCodec.decodeStrict(json, roleByName = ::testRoleByName)
        val game = decoded.game as ClocktowerRecovery

        assertEquals("Alice", game.mechanics.confirmedAttackTarget)
        assertEquals("Bob", game.mechanics.confirmedPoisonTarget)
        assertEquals("Carol", game.mechanics.confirmedMonkProtectedTarget)
        assertEquals("Demon 2", game.mechanics.confirmedDemonSuccessorTarget)
        assertEquals(listOf("Mayor", "Butler", "Soldier"), game.mechanics.demonBluffRoleNames)
        assertFalse(json.has("clocktowerDemonAttackDraftTarget"))
        assertFalse(json.has("clocktowerPoisonTarget"))
        assertFalse(json.has("clocktowerMonkProtectedTarget"))
        assertFalse(json.has("clocktowerMayorRedirectTarget"))
        assertFalse(json.has("clocktowerDemonSuccessorTarget"))
    }

    @Test
    fun clocktowerPlanCarriesCurrentResolvedRuleset() {
        val result = prepare(RecoverySnapshotJsonCodec.encode(clocktowerSnapshot()))

        assertTrue(result is RecoveryPlanPreparation.Ready)
        val runtime = (result as RecoveryPlanPreparation.Ready).plan.clocktowerRuntime
        assertNotNull(runtime)
        assertEquals(TEST_RULESET_REF, runtime?.rulesetRef)
        assertEquals(TEST_ROLE_IDS, runtime?.rulesetBasis?.roleIds)
        assertEquals(null, runtime?.sdeReplayMaterialization)
    }

    @Test
    fun clocktowerPlanRestoresExactDurableSdeReplayExport() {
        val base = clocktowerSnapshot()
        val game = base.game as ClocktowerRecovery
        val replayRaw = sdeReplayRaw(game)
        val snapshot = base.copy(
            game = game.copy(sdeHistoricalReplayInputJson = replayRaw),
        )

        val result = prepare(RecoverySnapshotJsonCodec.encode(snapshot))

        assertTrue(result is RecoveryPlanPreparation.Ready)
        val runtime = requireNotNull((result as RecoveryPlanPreparation.Ready).plan.clocktowerRuntime)
        val restored = requireNotNull(runtime.sdeReplayMaterialization)
        assertEquals(SdeHistoricalReplayInputOrigin.DURABLE_EXPORT, restored.origin)
        assertEquals(SdeHistoricalReplayInputJsonCodec.decodeStrict(replayRaw).input, restored.input)
        assertEquals(TEST_RULESET_REF, restored.input.rulesetRef)
        assertEquals("if-d-test", restored.input.committedSetup.provenance.providerId)
    }

    @Test
    fun mismatchedDurableSdeReplayExportFailsBeforeApplication() {
        val base = clocktowerSnapshot()
        val game = base.game as ClocktowerRecovery
        val mismatched = JSONObject(sdeReplayRaw(game))
            .put("gameId", "different-game")
            .toString()
        val snapshot = base.copy(
            game = game.copy(sdeHistoricalReplayInputJson = mismatched),
        )

        assertRejected(
            prepare(RecoverySnapshotJsonCodec.encode(snapshot)),
            RecoveryRejectionReason.InvalidGameState,
        )
    }

    @Test
    fun malformedDurableSdeReplayExportFailsBeforeApplication() {
        val base = clocktowerSnapshot()
        val game = base.game as ClocktowerRecovery
        val snapshot = base.copy(
            game = game.copy(sdeHistoricalReplayInputJson = "{broken"),
        )

        assertRejected(
            prepare(RecoverySnapshotJsonCodec.encode(snapshot)),
            RecoveryRejectionReason.InvalidGameState,
        )
    }

    @Test
    fun pendingKlutzDerivesDayKlutzSafeReentry() {
        val snapshot = clocktowerSnapshot(
            phase = ClocktowerPhase.Day,
            pendingKlutzName = "Dave",
            confirmedDemonSuccessorTarget = null,
            pendingNewDemonName = null,
            pendingNightNewDemonIdentityName = null,
        )

        val result = prepare(RecoverySnapshotJsonCodec.encode(snapshot))

        assertTrue(result is RecoveryPlanPreparation.Ready)
        val safe = (result as RecoveryPlanPreparation.Ready).plan.safeReentry
        assertEquals(
            RecoverySafeReentry.ClocktowerJudge(
                phase = ClocktowerPhase.Day,
                nightStepIndex = 5,
                continuation = ClocktowerRecoveryContinuation.Klutz,
            ),
            safe,
        )
    }

    @Test
    fun gameOutcomeDerivesResultsPresentation() {
        val outcome = GameOutcome("Good wins", "summary", "reason")
        val result = prepare(
            RecoverySnapshotJsonCodec.encode(undercoverSnapshot(outcome = outcome)),
        )

        assertTrue(result is RecoveryPlanPreparation.Ready)
        assertTrue((result as RecoveryPlanPreparation.Ready).plan.presentResults)
    }

    @Test
    fun globalProviderPrefixIsIdenticalAcrossStrictRecoveryCodecPlannerAndRestoredSession() {
        val base = clocktowerSnapshot()
        val originalRecovery = base.game as ClocktowerRecovery
        val gameState = com.codex.campboardgamehost.clocktower.domain.GameState(
            script = ScriptId("trouble_brewing"),
            players = originalRecovery.cards.mapIndexed { index, card ->
                val role = requireNotNull(card.clocktowerRole)
                com.codex.campboardgamehost.clocktower.domain.PlayerState(
                    seat = index + 1,
                    name = card.name,
                    actualRole = RoleId(role.enName),
                    actualAlignment =
                        if (role.team in setOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider))
                            com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD
                        else com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL,
                    actualType = when (role.team) {
                        ClocktowerTeam.Townsfolk -> com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK
                        ClocktowerTeam.Outsider -> com.codex.campboardgamehost.clocktower.domain.CharacterType.OUTSIDER
                        ClocktowerTeam.Minion -> com.codex.campboardgamehost.clocktower.domain.CharacterType.MINION
                        ClocktowerTeam.Demon -> com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON
                    },
                    shownRole = RoleId(role.enName),
                    alive = true,
                )
            },
            seed = originalRecovery.identity.gameSeed,
        )
        val session = com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession.createProduction(
            gameId = originalRecovery.identity.gameId,
            gameSeed = gameState.seed,
            initialState = gameState,
            semanticHistoryMode =
                com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalActionFact(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Poison(
                "first-poison", com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.FIRST_NIGHT,
                1, 0, 2,
            ),
        )
        session.commitGlobalEpistemicObservation(
            com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft(
                recordId = "first-chef-zero",
                phase = com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
                sourceSeat = 1,
                sourceAbility = RoleId("Chef"),
                visibility = com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility.PRIVATE,
                recipientSeats = setOf(1),
                reliability = com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.NumericResult(
                    com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.ADJACENT_EVIL_PAIRS,
                    sourceSeat = 1, subjectSeats = listOf(1, 2, 3, 4, 5), value = 0,
                ),
            ),
        )
        session.commitGlobalActionFact(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Execution(
                "day-execution", com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY,
                1, 0, 3,
            ),
        )
        session.commitGlobalEpistemicObservation(
            com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft(
                recordId = "public-execution",
                phase = com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.DAY,
                round = 1,
                sequence = 1,
                sourceSeat = null,
                sourceAbility = null,
                visibility = com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.AliveAt(3, false),
            ),
        )
        session.commitGlobalActionFact(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Protect(
                "night-protect", com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.NIGHT,
                2, 2, 4,
            ),
        )
        val revision = com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1(
            session.state.gameStateRevision, session.state.playerInputRevision,
        )
        val materializer = com.codex.campboardgamehost.clocktower.session.StorytellerProviderHistoryPrefixMaterializerV1
        val before = materializer.captureLive(session.state, revision)
        val source = base.copy(
            game = originalRecovery.copy(
                history = originalRecovery.history.copy(
                    gameStateRevision = session.state.gameStateRevision,
                    playerInputRevision = session.state.playerInputRevision,
                    actionTimeline = session.state.actionTimeline,
                    nextTimelineGlobalSequence = session.state.nextTimelineGlobalSequence,
                    epistemicObservations = session.state.epistemicObservationLog.records,
                ),
            ),
        )
        val serialized = RecoverySnapshotJsonCodec.encode(source)
        assertTrue("A complete Recovery must be accepted by the real restore planner.", prepare(serialized) is RecoveryPlanPreparation.Ready)
        val decoded = RecoverySnapshotJsonCodec.decodeStrict(serialized, ::testRoleByName)
        val restoredHistory = (decoded.game as ClocktowerRecovery).history
        val restored = com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession.restoreProduction(
            com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState(
                gameId = (decoded.game as ClocktowerRecovery).identity.gameId,
                gameStateRevision = restoredHistory.gameStateRevision,
                playerInputRevision = restoredHistory.playerInputRevision,
                gameSeed = gameState.seed,
                gameState = gameState,
                actionTimeline = restoredHistory.actionTimeline,
                epistemicObservationLog = EpistemicObservationLog(restoredHistory.epistemicObservations),
                semanticHistoryMode =
                    com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
                nextTimelineGlobalSequence = restoredHistory.nextTimelineGlobalSequence,
            ),
        )
        val after = materializer.captureLive(restored.state, revision)
        assertEquals(before, after)
        assertEquals(5L, after.exclusiveGlobalSequence)
        assertEquals(listOf("first-poison", "first-chef-zero", "day-execution", "public-execution", "night-protect"),
            after.entries.map { it.entryId })
        assertEquals(listOf(0L, 1L, 2L, 3L, 4L), after.entries.map { it.point.globalSequence })
        assertEquals(
            com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCoverageStateV1.UNKNOWN,
            after.coverage.getValue(
                com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryDimensionV1.REGISTRATION_RULINGS
            ).state,
        )
        restored.commitGlobalActionFact(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Attack(
                "night-attack-after-restore",
                com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.NIGHT,
                2, 3, 2,
            ),
        )
        assertEquals(5L, restored.state.actionTimeline.entries.last().point.globalSequence)
        assertEquals(before, materializer.captureLive(session.state, revision))
    }

    @Test
    fun causalDecisionRecoveryPreservesFrozenCutoffsAndCorrectionAsOf() {
        val original = clocktowerSnapshot()
        val game = original.game as ClocktowerRecovery
        val initialState = game.cards.toClocktowerGameState(
            game.identity.script, game.identity.gameSeed, game.mechanics.confirmedPoisonTarget,
        )
        val session = com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession.createProduction(
            gameId = game.identity.gameId,
            gameSeed = game.identity.gameSeed,
            initialState = initialState,
            semanticHistoryMode =
                com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val journal = com.codex.campboardgamehost.clocktower.session.StorytellerCausalDecisionJournalV1(
            game.identity.gameId,
        )
        fun currentSnapshot(): com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1 =
            com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1(
                gameId = game.identity.gameId,
                gameSeed = game.identity.gameSeed,
                position = com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition(
                    stage = com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotStage.RUNTIME,
                    phase = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(
                        com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.NIGHT,
                    ),
                    round = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(2),
                    gameStateRevision = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(
                        session.state.gameStateRevision,
                    ),
                    playerInputRevision = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(
                        session.state.playerInputRevision,
                    ),
                ),
                grimoireSeats = session.state.gameState.players.map { player ->
                    com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat(
                        seat = player.seat,
                        actualRoleId = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(
                            player.actualRole.value,
                        ),
                        shownRoleId = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(
                            (player.shownRole ?: player.actualRole).value,
                        ),
                        alive = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(player.alive),
                        poisoned = com.codex.campboardgamehost.clocktower.domain.SnapshotField.Known(player.poisoned),
                    )
                },
                setupState = com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState(
                    com.codex.campboardgamehost.clocktower.domain.SnapshotField.Unknown,
                    com.codex.campboardgamehost.clocktower.domain.SnapshotField.Unknown,
                ),
            )
        fun request(id: String): com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1 {
            val snapshot = currentSnapshot()
            return com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1(
                identity = com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1(
                    snapshot.gameId, snapshot.script.value, "mayor-redirect", id,
                ),
                sourceRevision = com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1(
                    session.state.gameStateRevision, session.state.playerInputRevision,
                ),
                state = com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1.TroubleBrewing(snapshot),
                decisionContext = com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1.MayorRedirect(1),
                legalCandidates = listOf(
                    com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1(
                        "seat-2",
                        com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1.SeatTarget(2),
                    ),
                    com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidateV1(
                        "seat-3",
                        com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1.SeatTarget(3),
                    ),
                ),
                gameContext = journal.contextForRequest(snapshot, session.state),
            )
        }
        fun commit(id: String, eventId: String, selected: String) {
            val frozen = journal.captureBeforeDecision(request(id), session.state)
            journal.commit(id, com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1(
                eventId = eventId,
                gameStateRevision = frozen.revision.gameStateRevision,
                playerInputRevision = frozen.revision.playerInputRevision,
                selectedCandidateId = selected,
                selectedOutcome = com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot(
                    "mayor-redirect", sortedMapOf("target" to selected),
                ),
                abilityState = com.codex.campboardgamehost.clocktower.domain.AbilityState.FUNCTIONING,
                truthRelation = com.codex.campboardgamehost.clocktower.domain.TruthRelation.NOT_APPLICABLE,
                registrations = emptyList(),
            ))
        }
        commit("first", "event-first", "seat-2")
        session.commitGlobalActionFact(
            com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft.Poison(
                "poison-between", com.codex.campboardgamehost.clocktower.domain.StorytellerPhase.NIGHT,
                2, 0, 2,
            ),
        )
        commit("second", "event-second", "seat-3")
        journal.correct("correct-after-second", "event-first", "event-second")
        val third = journal.captureBeforeDecision(request("third"), session.state)
        assertEquals(0L, journal.frozenAt("first").exclusiveGlobalSequence)
        assertEquals(1L, journal.frozenAt("second").exclusiveGlobalSequence)
        assertEquals(listOf("event-first"), journal.effectiveAt("second").map { it.eventId })
        assertEquals(listOf("event-second"), journal.effectiveAt("third").map { it.eventId })

        val savedGame = game.copy(
            history = game.history.copy(
                gameStateRevision = session.state.gameStateRevision,
                playerInputRevision = session.state.playerInputRevision,
                actionTimeline = session.state.actionTimeline,
                nextTimelineGlobalSequence = session.state.nextTimelineGlobalSequence,
                epistemicObservations = session.state.epistemicObservationLog.records,
                causalDecisionJournal = journal.archive(),
            ),
        )
        val raw = RecoverySnapshotJsonCodec.encode(original.copy(game = savedGame))
        assertTrue(prepare(raw) is RecoveryPlanPreparation.Ready)
        val decoded = RecoverySnapshotJsonCodec.decodeStrict(raw, ::testRoleByName)
        val history = (decoded.game as ClocktowerRecovery).history
        val restored = com.codex.campboardgamehost.clocktower.session.StorytellerCausalDecisionJournalV1.restore(
            requireNotNull(history.causalDecisionJournal),
            com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState(
                gameId = game.identity.gameId,
                gameStateRevision = history.gameStateRevision,
                playerInputRevision = history.playerInputRevision,
                gameSeed = game.identity.gameSeed,
                gameState = initialState,
                actionTimeline = history.actionTimeline,
                epistemicObservationLog = EpistemicObservationLog(history.epistemicObservations),
                semanticHistoryMode =
                    com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
                nextTimelineGlobalSequence = history.nextTimelineGlobalSequence,
            ),
        )
        assertEquals(journal.frozenAt("first"), restored.frozenAt("first"))
        assertEquals(journal.frozenAt("second"), restored.frozenAt("second"))
        assertEquals(third, restored.frozenAt("third"))
        assertEquals(listOf("event-first"), restored.effectiveAt("second").map { it.eventId })
        assertEquals(listOf("event-second"), restored.effectiveAt("third").map { it.eventId })

        val tampered = JSONObject(raw.toString())
        val journalJson = tampered.getJSONObject(ClocktowerCausalJournalPersistence.ROOT_KEY)
        journalJson.getJSONArray("records").getJSONObject(1).put("selectedCandidateId", "seat-99")
        assertRejected(prepare(tampered), RecoveryRejectionReason.InvalidGameState)
        val wrongPrefix = JSONObject(raw.toString())
        wrongPrefix.getJSONObject(ClocktowerCausalJournalPersistence.ROOT_KEY)
            .getJSONArray("records").getJSONObject(2)
            .put("prefixDigest", "f".repeat(64))
        assertRejected(prepare(wrongPrefix), RecoveryRejectionReason.MalformedPayload)

        val legacy = JSONObject(raw.toString()).apply {
            remove(ClocktowerCausalJournalPersistence.ROOT_KEY)
        }
        val older = RecoverySnapshotJsonCodec.decodeStrict(legacy, ::testRoleByName)
        assertEquals(null, (older.game as ClocktowerRecovery).history.causalDecisionJournal)
    }

    private fun prepare(json: JSONObject): RecoveryPlanPreparation = RecoveryRestorePlanner.prepare(
        raw = json,
        expectedCompatibilityToken = TOKEN,
        nowMillis = NOW,
        roleByName = ::testRoleByName,
        clocktowerRulesetResolver = { script, basis ->
            TEST_RULESET_REF.takeIf {
                script == ClocktowerScript.TroubleBrewing && basis.roleIds == TEST_ROLE_IDS
            }
        },
    )

    private fun testRoleByName(name: String): ClocktowerRole? = TEST_ROLES_BY_NAME[name]

    private fun assertRejected(
        result: RecoveryPlanPreparation,
        reason: RecoveryRejectionReason,
    ) {
        assertTrue(result is RecoveryPlanPreparation.Rejected)
        assertEquals(reason, (result as RecoveryPlanPreparation.Rejected).reason)
    }

    private fun undercoverSnapshot(
        savedAtMillis: Long = NOW - 1_000L,
        outcome: GameOutcome? = null,
    ): RecoverySnapshot = RecoverySnapshot(
        compatibilityToken = TOKEN,
        savedAtMillis = savedAtMillis,
        game = UndercoverRecovery(
            entryPoint = RecoveryEntryPoint.Stable,
            currentDealIndex = 0,
            round = 2,
            cards = listOf(
                PlayerCard("Alice", Role.Civilian, "cat"),
                PlayerCard("Bob", Role.Undercover, "dog"),
            ),
            records = emptyList(),
            outcome = outcome,
        ),
    )

    private fun clocktowerSnapshot(
        phase: ClocktowerPhase = ClocktowerPhase.Night,
        pendingKlutzName: String? = null,
        confirmedDemonSuccessorTarget: String? = "Demon 2",
        pendingNewDemonName: String? = "Demon 2",
        pendingNightNewDemonIdentityName: String? = "Demon 2",
    ): RecoverySnapshot {
        return RecoverySnapshot(
            compatibilityToken = TOKEN,
            savedAtMillis = NOW - 1_000L,
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = listOf(
                    clocktowerCard("Alice", "Chef"),
                    clocktowerCard("Bob", "Washerwoman"),
                    clocktowerCard("Carol", "Monk"),
                    clocktowerCard("Dave", "Poisoner"),
                    clocktowerCard("Demon 2", "Imp"),
                ),
                records = emptyList(),
                outcome = null,
                identity = ClocktowerRecoveryIdentity(
                    script = ClocktowerScript.TroubleBrewing,
                    gameId = "game-1",
                    gameSeed = 99L,
                ),
                position = ClocktowerRecoveryPosition(
                    phase = phase,
                    nightStarted = phase != ClocktowerPhase.Day,
                    nightStepIndex = 5,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = "Alice",
                    confirmedPoisonTarget = "Bob",
                    confirmedMonkProtectedTarget = "Carol",
                    confirmedMayorRedirectTarget = null,
                    pendingNewDemonName = pendingNewDemonName,
                    pendingNightNewDemonIdentityName = pendingNightNewDemonIdentityName,
                    confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
                    redHerring = "Bob",
                    demonBluffRoleNames = listOf("Mayor", "Butler", "Soldier"),
                    butlerMaster = "Carol",
                    virginUsed = true,
                    slayerUsed = true,
                    slayerClaimedNames = listOf("Alice"),
                    artistUsed = true,
                    artistClaimedNames = listOf("Bob"),
                    lastExecutedName = "Carol",
                    pendingKlutzName = pendingKlutzName,
                    klutzReturnToDawn = pendingKlutzName != null,
                    ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
                    highestVoteName = "Alice",
                    highestVoteCount = 4,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = 10L,
                    playerInputRevision = 11L,
                    actionTimeline = ActionFactTimeline(),
                    nextTimelineGlobalSequence = 0L,
                    events = emptyList(),
                    epistemicObservations = emptyList(),
                ),
            ),
        )
    }

    private fun sdeReplayRaw(game: ClocktowerRecovery): String {
        val setup = CommittedClocktowerSetup(
            script = ScriptId("trouble_brewing"),
            setupSeed = game.identity.gameSeed,
            assignments = game.cards.mapIndexed { index, card ->
                CommittedSetupSeat(
                    seat = index + 1,
                    actualRole = RoleId(requireNotNull(card.clocktowerRole).enName),
                    shownRole = RoleId(requireNotNull(card.clocktowerShownRole).enName),
                )
            },
            provenance = SetupProvenance(
                sourceKind = SetupSourceKind.GENERATED,
                providerId = "if-d-test",
                candidateId = "candidate-1",
            ),
        )
        return SdeHistoricalReplayInputJsonCodec.encode(
            SdeHistoricalReplayInput(
                gameId = game.identity.gameId,
                gameStateRevision = game.history.gameStateRevision,
                playerInputRevision = game.history.playerInputRevision,
                rulesetRef = TEST_RULESET_REF,
                committedSetup = setup,
                playerNamesBySeat = game.cards.map(PlayerCard::name),
                actionTimeline = game.history.actionTimeline,
                observationLog = EpistemicObservationLog(game.history.epistemicObservations),
                nextTimelineGlobalSequence = game.history.nextTimelineGlobalSequence,
            ),
        )
    }

    private fun clocktowerCard(name: String, roleName: String): PlayerCard {
        val role = requireNotNull(TEST_ROLES_BY_NAME[roleName])
        return PlayerCard(
            name = name,
            role = Role.Civilian,
            word = "",
            roleLabel = role.enName,
            actualRoleLabel = role.enName,
            clocktowerTeam = role.team,
            clocktowerRole = role,
            clocktowerShownRole = role,
        )
    }

    private companion object {
        const val TOKEN = "test-current-build"
        const val NOW = 20_000_000L
        const val FOUR_HOURS_MILLIS = 4L * 60L * 60L * 1000L

        val TEST_ROLES = listOf(
            ClocktowerRole(ClocktowerTeam.Townsfolk, "厨师", "Chef", "", ""),
            ClocktowerRole(ClocktowerTeam.Townsfolk, "洗衣妇", "Washerwoman", "", ""),
            ClocktowerRole(ClocktowerTeam.Townsfolk, "僧侣", "Monk", "", ""),
            ClocktowerRole(ClocktowerTeam.Minion, "投毒者", "Poisoner", "", ""),
            ClocktowerRole(ClocktowerTeam.Demon, "小恶魔", "Imp", "", ""),
        )
        val TEST_ROLES_BY_NAME = TEST_ROLES.associateBy(ClocktowerRole::enName)
        val TEST_ROLE_IDS = TEST_ROLES.mapTo(linkedSetOf()) { RoleId(it.enName) }
        val TEST_RULESET_REF = RulesetRef(
            scriptId = ScriptId("trouble_brewing"),
            scriptContentHash = "0123456789abcdef0123456789abcdef",
            rulesetVersion = "test-rules-v1",
            sourceRevision = "test",
            coverage = RuleCoverage.PARTIAL,
        )
    }
}
