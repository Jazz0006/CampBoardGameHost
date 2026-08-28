package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary GCR-2 production policy guard.
 *
 * Typed information/history tests own publication semantics. This source check only protects the
 * non-callable Host policy boundary: a poisoned Spy must take a no-Grimoire branch on both nights.
 */
class ClocktowerPoisonedSpyFailSafePolicyWiringTest {
    private val hostSource = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `first night poisoned Spy takes no Grimoire publication branch`() {
        assertTrue(
            hostSource.contains(
                "displayProposition = if (poisonTarget == spyCard?.name) null else InformationProposition.GrimoireState(",
            ),
        )
    }

    @Test
    fun `other night poisoned Spy takes no Grimoire publication branch`() {
        assertTrue(
            hostSource.contains(
                "displayProposition = if (effectivePoisonForRole(\"Spy\") == spyCard?.name) null else InformationProposition.GrimoireState(",
            ),
        )
    }
}
