package com.codex.campboardgamehost

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCompatibilityTokenTest {
    @Test
    fun currentTokenIsOwnedByRecoveryFormatAndAcceptedByPlanner() {
        val expectedToken = "recovery-v2:Undercover"
        assertEquals(expectedToken, RecoveryCompatibilityToken.currentFor(GameKind.Undercover))

        val raw = RecoverySnapshotJsonCodec.encode(
            RecoverySnapshot(
                compatibilityToken = expectedToken,
                savedAtMillis = NOW - 1_000L,
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

        val result = RecoveryRestorePlanner.prepare(
            raw = raw,
            expectedCompatibilityToken = RecoveryCompatibilityToken.currentFor(GameKind.Undercover),
            nowMillis = NOW,
            roleByName = { null },
            clocktowerRulesetResolver = { _, _ -> null },
        )

        assertTrue(result is RecoveryPlanPreparation.Ready)
    }

    @Test
    fun clocktowerTokenUsesTheSameRecoveryOwnedFormatAuthority() {
        assertEquals(
            "recovery-v2:Clocktower",
            RecoveryCompatibilityToken.currentFor(GameKind.Clocktower),
        )
    }

    private companion object {
        const val NOW = 20_000_000L
    }
}
