package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
            put(ClocktowerSemanticHistoryPersistence.CURSOR_KEY, 2L)
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
        val decoded = RecoverySnapshotJsonCodec.decodeStrict(json, roleByName = { null })
        val game = decoded.game as ClocktowerRecovery

        assertEquals("Alice", game.mechanics.confirmedAttackTarget)
        assertEquals("Bob", game.mechanics.confirmedPoisonTarget)
        assertEquals("Carol", game.mechanics.confirmedMonkProtectedTarget)
        assertEquals("Demon 2", game.mechanics.confirmedDemonSuccessorTarget)
        assertTrue(json.isNull("clocktowerDemonAttackDraftTarget"))
        assertTrue(json.isNull("clocktowerPoisonTarget"))
        assertTrue(json.isNull("clocktowerMonkProtectedTarget"))
        assertTrue(json.isNull("clocktowerDemonSuccessorTarget"))
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

    private fun prepare(json: JSONObject): RecoveryPlanPreparation = RecoveryRestorePlanner.prepare(
        raw = json,
        expectedCompatibilityToken = TOKEN,
        nowMillis = NOW,
        roleByName = { null },
    )

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
        legacyRestoreCompatibility = LegacyRestoreCompatibility(
            activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
            identity = PersistedActiveGameIdentityEnvelope.undercover(),
        ),
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
            undercoverCount = 1,
            includeBlank = false,
            lastWordsMode = LastWordsMode.FirstDay,
        ),
    )

    private fun clocktowerSnapshot(
        phase: ClocktowerPhase = ClocktowerPhase.Night,
        pendingKlutzName: String? = null,
        confirmedDemonSuccessorTarget: String? = "Demon 2",
        pendingNewDemonName: String? = "Demon 2",
        pendingNightNewDemonIdentityName: String? = "Imp",
    ): RecoverySnapshot {
        val identity = PersistedActiveGameIdentityEnvelope.clocktower(
            PersistedGameContentIdentity(
                kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
                variantId = "trouble-brewing-test",
                contentHash = "0123456789abcdef0123456789abcdef",
                semanticVersion = "1",
            ),
        )
        return RecoverySnapshot(
            compatibilityToken = TOKEN,
            savedAtMillis = NOW - 1_000L,
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = identity,
            ),
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = listOf(
                    PlayerCard("Alice", Role.Civilian, ""),
                    PlayerCard("Bob", Role.Civilian, ""),
                    PlayerCard("Carol", Role.Civilian, ""),
                    PlayerCard("Dave", Role.Civilian, ""),
                    PlayerCard("Demon 2", Role.Civilian, ""),
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
                    demonBluffRoleNames = listOf("Chef", "Monk", "Mayor"),
                    butlerMaster = "Carol",
                    virginUsed = true,
                    slayerUsed = true,
                    slayerClaimedNames = listOf("Alice"),
                    artistUsed = true,
                    artistClaimedNames = listOf("Bob"),
                    lastExecutedName = "Carol",
                    pendingKlutzName = pendingKlutzName,
                    klutzChoiceName = null,
                    klutzReturnToDawn = pendingKlutzName != null,
                    ghostVoteAuthority = ClocktowerGhostVoteAuthority(),
                    highestVoteName = "Alice",
                    highestVoteCount = 4,
                ),
                history = ClocktowerRecoveryHistory(
                    gameStateRevision = 10L,
                    playerInputRevision = 11L,
                    semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                    actionTimeline = ActionFactTimeline(),
                    nextTimelineGlobalSequence = 12L,
                    events = emptyList(),
                    epistemicObservations = emptyList(),
                ),
            ),
        )
    }

    private companion object {
        const val TOKEN = "test-current-build"
        const val NOW = 20_000_000L
        const val FOUR_HOURS_MILLIS = 4L * 60L * 60L * 1000L
    }
}
