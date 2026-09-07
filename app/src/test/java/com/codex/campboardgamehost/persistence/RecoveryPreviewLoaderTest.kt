package com.codex.campboardgamehost

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryPreviewLoaderTest {
    @Test
    fun stableTypedRecoveryWithoutRawScreenProducesPreviewWithoutClear() {
        val raw = RecoverySnapshotJsonCodec.encode(undercoverSnapshot(savedAtMillis = NOW - 60_000L))
        assertFalse(raw.has("screen"))
        var clearCount = 0

        val preview = RecoveryPreviewLoader.load(
            raw = raw,
            prepare = ::prepare,
            clearRejected = { clearCount += 1 },
        )

        assertNotNull(preview)
        assertEquals(0, clearCount)
        assertEquals(GameKind.Undercover, preview?.gameKind)
        assertEquals(2, preview?.round)
        assertEquals(2, preview?.playerCount)
        assertEquals(RecoverySafeReentry.UndercoverGame, preview?.safeReentry)
        assertFalse(preview?.presentResults ?: true)
    }

    @Test
    fun rejectedRecoveryIsClearedExactlyOnce() {
        val raw = RecoverySnapshotJsonCodec.encode(
            undercoverSnapshot(savedAtMillis = NOW - RecoveryValidityPolicy.MAX_AGE_MILLIS - 1L),
        )
        var clearCount = 0

        val preview = RecoveryPreviewLoader.load(
            raw = raw,
            prepare = ::prepare,
            clearRejected = { clearCount += 1 },
        )

        assertEquals(null, preview)
        assertEquals(1, clearCount)
    }

    @Test
    fun absentRecoveryDoesNotClearAnything() {
        var clearCount = 0

        val preview = RecoveryPreviewLoader.load(
            raw = null,
            prepare = ::prepare,
            clearRejected = { clearCount += 1 },
        )

        assertEquals(null, preview)
        assertEquals(0, clearCount)
    }

    private fun prepare(raw: JSONObject): RecoveryPlanPreparation = RecoveryRestorePlanner.prepare(
        raw = raw,
        expectedCompatibilityToken = TOKEN,
        nowMillis = NOW,
        roleByName = { null },
        clocktowerRulesetResolver = { _, _ -> null },
    )

    private fun undercoverSnapshot(savedAtMillis: Long): RecoverySnapshot = RecoverySnapshot(
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
            outcome = null,
            undercoverCount = 1,
            includeBlank = false,
            lastWordsMode = LastWordsMode.FirstDay,
        ),
    )

    private companion object {
        const val TOKEN = "test-current-build"
        const val NOW = 20_000_000L
    }
}
