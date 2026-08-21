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
    fun `production active game schema is owned by the v3 persistence coordinator`() {
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
    fun `snapshot writes strict content identity and immutable Clocktower ruleset basis`() {
        val snapshot = source
            .substringAfter("fun activeGameSnapshotJson(): JSONObject")
            .substringBefore("fun persistActiveGameStateIfNeeded()")

        assertTrue(snapshot.contains("activeGamePersistenceCoordinator.identityForSave("))
        assertTrue(snapshot.contains("PersistedActiveGameIdentityJsonCodec.ROOT_KEY"))
        assertTrue(snapshot.contains("PersistedActiveGameIdentityJsonCodec.encode("))
        assertTrue(snapshot.contains("assignedClocktowerRoleIds"))
        assertTrue(snapshot.contains("assignedWerewolfRoles"))
        assertTrue(snapshot.contains("lastWordsMode = lastWordsMode"))
        assertTrue(snapshot.contains("\"clocktowerRulesetRoleIds\""))
        assertTrue(snapshot.contains("ClocktowerRulesetPersistenceBasisJsonCodec.encode("))
        assertTrue(snapshot.contains("ClocktowerRulesetPersistenceBasis(clocktowerRulesetRoleIds)"))
    }

    @Test
    fun `Clocktower setup captures immutable ruleset basis before later role changes`() {
        assertTrue(
            source.contains(
                "var clocktowerRulesetRoleIds by remember { mutableStateOf<Set<RoleId>>(emptySet()) }",
            ),
        )
        val reset = source
            .substringAfter("fun resetDealState(")
            .substringBefore("fun startUndercoverGame()")
        assertTrue(reset.contains("val rulesetBasis = ClocktowerRulesetPersistenceBasis("))
        assertTrue(reset.contains("clocktowerRulesetRoleIds = rulesetBasis.roleIds"))
        assertTrue(reset.contains("troubleBrewingRulesetRefFor(rulesetBasis)"))

        val roleMutation = source
            .substringAfter("fun setClocktowerActualRole(")
            .substringBefore("fun setClocktowerShownRole(")
        assertFalse(roleMutation.contains("clocktowerRulesetRoleIds"))
    }

    @Test
    fun `restore validates version identity ruleset basis and ref before mutating live game state`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertTrue(restore.contains("ActiveGamePersistenceCoordinator.isSupportedVersion"))
        assertTrue(
            restore.contains(
                "val restoredPersistence = activeGamePersistenceCoordinator.resolveForRestore(",
            ),
        )
        assertTrue(restore.contains("val restoredRulesetBasis ="))
        assertTrue(restore.contains("ClocktowerRulesetPersistenceBasisJsonCodec.decode("))
        assertTrue(restore.contains("val restoredClocktowerRulesetRef ="))
        assertTrue(restore.contains("val resolvedClocktowerRulesetRef ="))
        assertTrue(restore.contains("TroubleBrewingRulesetPersistence.resolveForRestore("))

        val identityIndex = restore.indexOf("val restoredPersistence = activeGamePersistenceCoordinator.resolveForRestore(")
        val rulesetIndex = restore.indexOf("val resolvedClocktowerRulesetRef =")
        val mutationIndex = restore.indexOf("playerNames.clear()")
        assertTrue(identityIndex >= 0)
        assertTrue(rulesetIndex >= 0)
        assertTrue(mutationIndex >= 0)
        assertTrue("Persistence identity must be validated before live state mutation.", identityIndex < mutationIndex)
        assertTrue("Ruleset basis/ref must be validated before live state mutation.", rulesetIndex < mutationIndex)
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
    fun `Trouble Brewing v3 restore requires persisted immutable basis`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertTrue(restore.contains("TroubleBrewingRulesetPersistence.resolveForRestore("))
        assertTrue(restore.contains("Version 3 Clocktower save is missing ruleset role basis."))
        assertFalse(restore.contains("troubleBrewingRulesetRefFor(localizedRestoredCards)"))
        assertTrue(restore.contains("clocktowerRulesetRoleIds = restoredRulesetBasis?.roleIds.orEmpty()"))
        assertTrue(restore.contains("clocktowerRulesetRef = resolvedClocktowerRulesetRef"))
    }

    @Test
    fun `production restore contains no legacy active-game migration path`() {
        val restore = source
            .substringAfter("fun restoreSavedGame()")
            .substringBefore("val latestPersistActiveGameState")

        assertFalse(restore.contains("allowLegacyClocktowerRulesetFallback"))
        assertFalse(restore.contains("resolveLegacyBasisForRestore("))
    }
}
