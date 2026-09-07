package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoverySnapshotJsonCodecTest {
    @Test
    fun stableUndercoverRecoveryOmitsArbitraryUiState() {
        val snapshot = RecoverySnapshot(
            compatibilityToken = "test-current-build",
            savedAtMillis = 1234L,
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = PersistedActiveGameIdentityEnvelope.undercover(),
            ),
            game = UndercoverRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 2,
                round = 3,
                cards = listOf(PlayerCard("Alice", Role.Civilian, "cat")),
                records = listOf(EliminationRecord(2, "Bob", "vote")),
                outcome = null,
                undercoverCount = 1,
                includeBlank = false,
                lastWordsMode = LastWordsMode.FirstDay,
            ),
        )

        val json = RecoverySnapshotJsonCodec.encode(snapshot)

        assertEquals(RecoverySnapshot.CURRENT_FORMAT_VERSION, json.getInt("recoveryFormatVersion"))
        assertEquals("test-current-build", json.getString("recoveryCompatibilityToken"))
        assertEquals(GameKind.Undercover.name, json.getString("currentGameKind"))
        assertEquals(3, json.getInt("round"))
        assertEquals("Alice", json.getJSONArray("cards").getJSONObject(0).getString("name"))
        assertFalse(json.has("screen"))
        assertFalse(json.has("selectedElimination"))
        assertFalse(json.has("lastWordsPromptNames"))
        assertFalse(json.has("showResults"))
        assertFalse(json.has("playerNames"))
    }

    @Test
    fun dealRevealRecoveryKeepsOnlyNarrowNavigationContinuation() {
        val snapshot = RecoverySnapshot(
            compatibilityToken = "test-current-build",
            savedAtMillis = 1234L,
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = PersistedActiveGameIdentityEnvelope.undercover(),
            ),
            game = UndercoverRecovery(
                entryPoint = RecoveryEntryPoint.RevealCard,
                currentDealIndex = 4,
                round = 1,
                cards = listOf(PlayerCard("Alice", Role.Civilian, "cat")),
                records = emptyList(),
                outcome = null,
                undercoverCount = 1,
                includeBlank = false,
                lastWordsMode = LastWordsMode.FirstDay,
            ),
        )

        val json = RecoverySnapshotJsonCodec.encode(snapshot)

        assertEquals("RevealCard", json.getString("screen"))
        assertEquals(4, json.getInt("currentDealIndex"))
    }

    @Test
    fun werewolfRecoveryRetainsAlreadyPerformedNightInteractions() {
        val identity = PersistedActiveGameIdentityEnvelope.werewolf(
            PersistedWerewolfGameIdentity(
                board = PersistedGameContentIdentity(
                    kind = PersistedVariantKind.WEREWOLF_BOARD,
                    variantId = "test-board",
                    contentHash = "0123456789abcdef0123456789abcdef",
                    semanticVersion = "1",
                ),
                ruleOptions = WerewolfRuleOptions(LastWordsMode.FirstDay),
            ),
        )
        val snapshot = RecoverySnapshot(
            compatibilityToken = "test-current-build",
            savedAtMillis = 1234L,
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = identity,
            ),
            game = WerewolfRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = listOf(PlayerCard("Wolf", Role.Werewolf, "")),
                records = emptyList(),
                outcome = null,
                werewolfCount = 1,
                includeSeer = true,
                includeWitch = true,
                includeHunter = false,
                lastWordsMode = LastWordsMode.FirstDay,
                judgeStepIndex = 3,
                pendingNightDeath = "Alice",
                seerCheckTarget = "Bob",
                witchSaveUsed = true,
                witchPoisonUsed = true,
                witchSavedTonight = false,
                witchPoisonTarget = "Carol",
                hunterShotTarget = null,
            ),
        )

        val json = RecoverySnapshotJsonCodec.encode(snapshot)

        assertEquals(3, json.getInt("werewolfJudgeStepIndex"))
        assertEquals("Alice", json.getString("pendingNightDeath"))
        assertEquals("Bob", json.getString("seerCheckTarget"))
        assertTrue(json.getBoolean("witchSaveUsed"))
        assertTrue(json.getBoolean("witchPoisonUsed"))
        assertEquals("Carol", json.getString("witchPoisonTarget"))
        assertFalse(json.has("selectedDayExile"))
    }

    @Test
    fun clocktowerRecoveryKeepsConfirmedFactsButDiscardsDraftTargetsAndDayUi() {
        val identity = PersistedActiveGameIdentityEnvelope.clocktower(
            PersistedGameContentIdentity(
                kind = PersistedVariantKind.CLOCKTOWER_SCRIPT,
                variantId = "trouble-brewing-test",
                contentHash = "0123456789abcdef0123456789abcdef",
                semanticVersion = "1",
            ),
        )
        val snapshot = RecoverySnapshot(
            compatibilityToken = "test-current-build",
            savedAtMillis = 1234L,
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = identity,
            ),
            game = ClocktowerRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 2,
                cards = listOf(PlayerCard("Alice", Role.Civilian, "")),
                records = emptyList(),
                outcome = null,
                identity = ClocktowerRecoveryIdentity(
                    script = ClocktowerScript.TroubleBrewing,
                    gameId = "game-1",
                    gameSeed = 99L,
                ),
                position = ClocktowerRecoveryPosition(
                    phase = ClocktowerPhase.Night,
                    nightStarted = true,
                    nightStepIndex = 5,
                ),
                mechanics = ClocktowerRecoveryMechanics(
                    confirmedAttackTarget = "Alice",
                    confirmedPoisonTarget = "Bob",
                    confirmedMonkProtectedTarget = "Carol",
                    confirmedMayorRedirectTarget = null,
                    pendingNewDemonName = "Demon 2",
                    pendingNightNewDemonIdentityName = "Imp",
                    confirmedDemonSuccessorTarget = "Demon 2",
                    redHerring = "Bob",
                    demonBluffRoleNames = listOf("Chef", "Monk", "Mayor"),
                    butlerMaster = "Carol",
                    virginUsed = true,
                    slayerUsed = true,
                    slayerClaimedNames = listOf("Alice"),
                    artistUsed = true,
                    artistClaimedNames = listOf("Bob"),
                    lastExecutedName = "Carol",
                    pendingKlutzName = "Dave",
                    klutzChoiceName = null,
                    klutzReturnToDawn = true,
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

        val json = RecoverySnapshotJsonCodec.encode(snapshot)

        assertEquals("Alice", json.getString("clocktowerPendingNightDeath"))
        assertEquals("Bob", json.getString("clocktowerConfirmedPoisonTarget"))
        assertEquals("Carol", json.getString("clocktowerConfirmedMonkProtectedTarget"))
        assertEquals("Demon 2", json.getString("clocktowerConfirmedDemonSuccessorTarget"))
        assertTrue(json.isNull("clocktowerDemonAttackDraftTarget"))
        assertTrue(json.isNull("clocktowerPoisonTarget"))
        assertTrue(json.isNull("clocktowerMonkProtectedTarget"))
        assertTrue(json.isNull("clocktowerMayorRedirectTarget"))
        assertTrue(json.isNull("clocktowerDemonSuccessorTarget"))
        assertFalse(json.has("clocktowerDayMode"))
        assertFalse(json.has("clocktowerNominatorName"))
        assertFalse(json.has("clocktowerNomineeName"))
        assertFalse(json.has("clocktowerCurrentVoteCount"))
        assertFalse(json.has("clocktowerSelectedExecution"))
        assertFalse(json.has("clocktowerArtistClaimantName"))
        assertFalse(json.has("clocktowerSlayerTargetName"))
    }
}
