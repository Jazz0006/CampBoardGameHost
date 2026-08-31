package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production boundary guard for active-game persistence.
 *
 * Schema and payload semantics are proved by typed persistence tests. These checks retain only the
 * non-callable App ownership contract: setup facts route through canonical persistence owners and
 * Trouble Brewing restore no longer reloads template data to reconstruct a completed selection.
 */
class ActiveGameProductionPersistenceWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `App save routes active identity ruleset exact setup and TB completion through canonical owners`() {
        val snapshot = source
            .substringAfter("fun activeGameSnapshotJson(): JSONObject")
            .substringBefore("fun persistActiveGameStateIfNeeded()")

        assertTrue(snapshot.contains("activeGamePersistenceCoordinator.identityForSave("))
        assertTrue(snapshot.contains("PersistedActiveGameIdentityJsonCodec.encode("))
        assertTrue(snapshot.contains("ClocktowerRulesetPersistenceBasisJsonCodec.encode("))
        assertTrue(snapshot.contains("CommittedClocktowerSetupPersistence.encode("))
        assertTrue(snapshot.contains("TroubleBrewingSetupCompletionPersistence.encode("))
        assertFalse(snapshot.contains("TroubleBrewingSetupProvenancePersistence.encode("))
    }

    @Test
    fun `TB restore validates exact setup and completion before mutation without template reload`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        val versionGateIndex = restore.indexOf("ActiveGamePersistenceCoordinator.isSupportedVersion")
        val identityIndex = restore.indexOf("activeGamePersistenceCoordinator.resolveForRestore(")
        val rulesetIndex = restore.indexOf("TroubleBrewingRulesetPersistence.resolveForRestore(")
        val committedSetupIndex = restore.indexOf("CommittedClocktowerSetupPersistence.decodeOrNull(json)")
        val completionIndex = restore.indexOf("TroubleBrewingSetupCompletionPersistence.decodeOrNull(json)")
        val mutationIndex = restore.indexOf("playerNames.clear()")

        assertTrue(versionGateIndex >= 0)
        assertTrue(identityIndex >= 0)
        assertTrue(rulesetIndex >= 0)
        assertTrue(committedSetupIndex >= 0)
        assertTrue(completionIndex >= 0)
        assertTrue(mutationIndex >= 0)
        assertTrue(versionGateIndex < mutationIndex)
        assertTrue(identityIndex < mutationIndex)
        assertTrue(rulesetIndex < mutationIndex)
        assertTrue(committedSetupIndex < mutationIndex)
        assertTrue(completionIndex < mutationIndex)
        assertFalse(restore.contains("TroubleBrewingSetupProvenancePersistence.decodeOrNull"))
        assertFalse(restore.contains("trouble_brewing_setup_presets_v2_final.json"))
    }

    @Test
    fun `Trouble Brewing start commits exact setup and completion before active-state persistence`() {
        val start = source
            .substringAfter("fun startTroubleBrewingGame()")
            .substringBefore("fun persistCompletedTroubleBrewingSetupIfNeeded()")

        val genericCommitIndex = start.indexOf(
            "committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(",
        )
        val completionCommitIndex = start.indexOf(
            "committedTroubleBrewingSetupRotationRecord = TroubleBrewingSetupRotationRecordFactory.fromSelection(",
        )
        val persistIndex = start.indexOf("persistActiveGameStateIfNeeded()")

        assertTrue(genericCommitIndex >= 0)
        assertTrue(completionCommitIndex >= 0)
        assertTrue(persistIndex >= 0)
        assertTrue(genericCommitIndex < persistIndex)
        assertTrue(completionCommitIndex < persistIndex)
    }

    @Test
    fun `TB completion history consumes compact record rather than reconstructed selection`() {
        val completion = source
            .substringAfter("fun persistCompletedTroubleBrewingSetupIfNeeded(): Boolean")
            .substringBefore("fun archiveAndReturnToPlayerManagement()")

        assertTrue(completion.contains("committedTroubleBrewingSetupRotationRecord"))
        assertTrue(completion.contains("record = record"))
        assertFalse(completion.contains("committedTroubleBrewingSetupSelection"))
    }
}
