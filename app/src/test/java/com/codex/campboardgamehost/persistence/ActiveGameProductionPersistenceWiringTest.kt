package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production boundary guard for active-game persistence.
 *
 * Schema, identity and ruleset validation semantics are proved by typed persistence tests. These
 * checks retain only the non-callable App contract that save/restore routes through those canonical
 * owners and validates restore input before mutating live state.
 */
class ActiveGameProductionPersistenceWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `App save routes active identity ruleset and committed setup through canonical persistence owners`() {
        val snapshot = source
            .substringAfter("fun activeGameSnapshotJson(): JSONObject")
            .substringBefore("fun persistActiveGameStateIfNeeded()")

        assertTrue(snapshot.contains("activeGamePersistenceCoordinator.identityForSave("))
        assertTrue(snapshot.contains("PersistedActiveGameIdentityJsonCodec.encode("))
        assertTrue(snapshot.contains("ClocktowerRulesetPersistenceBasisJsonCodec.encode("))
        assertTrue(snapshot.contains("CommittedClocktowerSetupPersistence.encode("))
    }

    @Test
    fun `App restore validates schema identity ruleset and committed setup before live-state mutation`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        val versionGateIndex = restore.indexOf("ActiveGamePersistenceCoordinator.isSupportedVersion")
        val identityIndex = restore.indexOf("activeGamePersistenceCoordinator.resolveForRestore(")
        val rulesetIndex = restore.indexOf("TroubleBrewingRulesetPersistence.resolveForRestore(")
        val committedSetupIndex = restore.indexOf("CommittedClocktowerSetupPersistence.decodeOrNull(json)")
        val mutationIndex = restore.indexOf("playerNames.clear()")

        assertTrue(versionGateIndex >= 0)
        assertTrue(identityIndex >= 0)
        assertTrue(rulesetIndex >= 0)
        assertTrue(committedSetupIndex >= 0)
        assertTrue(mutationIndex >= 0)
        assertTrue(versionGateIndex < mutationIndex)
        assertTrue(identityIndex < mutationIndex)
        assertTrue(rulesetIndex < mutationIndex)
        assertTrue(committedSetupIndex < mutationIndex)
    }

    @Test
    fun `Trouble Brewing start commits exact generic setup before active-state persistence`() {
        val start = source
            .substringAfter("fun startTroubleBrewingGame()")
            .substringBefore("fun persistCompletedTroubleBrewingSetupIfNeeded()")

        val genericCommitIndex = start.indexOf(
            "committedClocktowerSetup = TroubleBrewingCommittedSetupAdapter.fromDealPlan(",
        )
        val persistIndex = start.indexOf("persistActiveGameStateIfNeeded()")

        assertTrue(genericCommitIndex >= 0)
        assertTrue(persistIndex >= 0)
        assertTrue(genericCommitIndex < persistIndex)
    }
}
