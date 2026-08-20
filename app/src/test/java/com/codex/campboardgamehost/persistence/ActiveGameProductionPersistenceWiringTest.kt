package com.codex.campboardgamehost

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ActiveGameProductionPersistenceWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production active game schema is owned by the v2 persistence coordinator`() {
        assertTrue(
            source.contains(
                "private const val ACTIVE_GAME_STATE_VERSION = ActiveGamePersistenceCoordinator.CURRENT_VERSION",
            ),
        )
        assertTrue(
            source.contains(
                "val activeGamePersistenceCoordinator = remember(baseContext) {\n" +
                    "        ActiveGamePersistenceCoordinator.fromContext(baseContext)\n" +
                    "    }",
            ),
        )
        assertTrue(source.contains("ActiveGamePersistenceCoordinator.isSupportedVersion"))
    }

    @Test
    fun `snapshot writes strict content identity for the active game`() {
        val snapshot = source
            .substringAfter("fun activeGameSnapshotJson(): JSONObject")
            .substringBefore("fun persistActiveGameState()")

        assertTrue(snapshot.contains("activeGamePersistenceCoordinator.identityForSave("))
        assertTrue(snapshot.contains("PersistedActiveGameIdentityJsonCodec.ROOT_KEY"))
        assertTrue(snapshot.contains("PersistedActiveGameIdentityJsonCodec.encode("))
        assertTrue(snapshot.contains("assignedClocktowerRoleIds"))
        assertTrue(snapshot.contains("assignedWerewolfRoles"))
        assertTrue(snapshot.contains("lastWordsMode = lastWordsMode"))
    }

    @Test
    fun `restore validates version and identity before mutating live game state`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertTrue(restore.contains("ActiveGamePersistenceCoordinator.isSupportedVersion"))
        assertTrue(
            restore.contains(
                "val restoredPersistence = activeGamePersistenceCoordinator.resolveForRestore(",
            ),
        )
        val validationIndex = restore.indexOf("val restoredPersistence = activeGamePersistenceCoordinator.resolveForRestore(")
        val mutationIndex = restore.indexOf("playerNames.clear()")
        assertTrue(validationIndex >= 0)
        assertTrue(mutationIndex >= 0)
        assertTrue("Persistence identity must be validated before live state mutation.", validationIndex < mutationIndex)
    }

    @Test
    fun `Clocktower restore never infers selected script from player count or assigned roles`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertFalse(restore.contains("restoredHasNoGreaterJoyOnlyRole"))
        assertFalse(restore.contains("defaultClocktowerScriptFor(localizedRestoredCards.size)"))
        assertTrue(
            restore.contains(
                "currentClocktowerScript = requireNotNull(restoredPersistence.clocktowerScript)",
            ),
        )
    }

    @Test
    fun `Trouble Brewing ruleset fallback is legacy-only and v2 validates exact current ruleset`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertTrue(restore.contains("restoredPersistence.allowLegacyClocktowerRulesetFallback"))
        assertTrue(restore.contains("val currentTroubleBrewingRulesetRef = troubleBrewingRulesetRefFor(localizedRestoredCards)"))
        assertTrue(restore.contains("clocktowerRulesetRef == currentTroubleBrewingRulesetRef"))
        assertTrue(restore.contains("Version 2 Trouble Brewing save has an incompatible ruleset reference."))
    }
}
