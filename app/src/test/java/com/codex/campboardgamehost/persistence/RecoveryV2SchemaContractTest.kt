package com.codex.campboardgamehost

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryV2SchemaContractTest {
    @Test
    fun currentRecoveryContractIsV2AndOwnsItsCompatibilityToken() {
        assertEquals(2, RecoverySnapshot.CURRENT_FORMAT_VERSION)
        assertEquals("recovery-v2:Undercover", RecoveryCompatibilityToken.currentFor(GameKind.Undercover))
        assertEquals("recovery-v2:Clocktower", RecoveryCompatibilityToken.currentFor(GameKind.Clocktower))
    }

    @Test
    fun previousV1RecoveryIsRejectedWithoutMigration() {
        val raw = currentUndercoverRaw().apply {
            put(RecoverySnapshotJsonCodec.FORMAT_VERSION_KEY, 1)
            put(RecoverySnapshotJsonCodec.COMPATIBILITY_TOKEN_KEY, "recovery-v1:Undercover")
        }

        val result = prepare(raw)

        assertTrue(result is RecoveryPlanPreparation.Rejected)
        assertEquals(
            RecoveryRejectionReason.UnsupportedFormat,
            (result as RecoveryPlanPreparation.Rejected).reason,
        )
    }

    @Test
    fun currentRecoveryWireOmitsObsoleteActiveGameMetadata() {
        val raw = currentUndercoverRaw()

        assertFalse(raw.has("version"))
        assertFalse(raw.has(PersistedActiveGameIdentityJsonCodec.ROOT_KEY))
        assertFalse(raw.has(CommittedClocktowerSetupPersistence.ROOT_KEY))
        assertFalse(raw.has("clocktowerRulesetRoleIds"))
        assertFalse(raw.has("clocktowerRulesetRef"))
    }

    private fun currentUndercoverRaw(): JSONObject = RecoverySnapshotJsonCodec.encode(
        RecoverySnapshot(
            compatibilityToken = RecoveryCompatibilityToken.currentFor(GameKind.Undercover),
            savedAtMillis = NOW - 1_000L,
            legacyRestoreCompatibility = LegacyRestoreCompatibility(
                activeGameStateVersion = ActiveGamePersistenceCoordinator.CURRENT_VERSION,
                identity = PersistedActiveGameIdentityEnvelope.undercover(),
            ),
            game = UndercoverRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 1,
                cards = listOf(
                    PlayerCard("Alice", Role.Civilian, "cat"),
                    PlayerCard("Bob", Role.Undercover, "dog"),
                ),
                records = emptyList(),
                outcome = null,
                undercoverCount = 1,
                includeBlank = false,
                lastWordsMode = LastWordsMode.FirstDay,
            ),
        ),
    )

    private fun prepare(raw: JSONObject): RecoveryPlanPreparation = RecoveryRestorePlanner.prepare(
        raw = raw,
        expectedCompatibilityToken = RecoveryCompatibilityToken.currentFor(GameKind.Undercover),
        nowMillis = NOW,
        roleByName = { null },
        clocktowerRulesetResolver = { _, _ -> null },
    )

    private companion object {
        const val NOW = 20_000_000L
    }
}
