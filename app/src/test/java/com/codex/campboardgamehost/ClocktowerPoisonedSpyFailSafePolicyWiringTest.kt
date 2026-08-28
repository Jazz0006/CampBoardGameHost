package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Temporary GCR-2 policy-characterization guard.
 *
 * Product policy intentionally fails safe for a poisoned Spy: wake normally, but suppress all
 * Grimoire information instead of fabricating misinformation. The Spy materializers still live
 * inside the protected Compose Host, so this coarse source guard locks that wiring until GCR-3
 * can replace it with a callable production seam if one is introduced for independent reasons.
 */
class ClocktowerPoisonedSpyFailSafePolicyWiringTest {
    @Test
    fun `first night poisoned Spy suppresses true Grimoire while healthy Spy retains it`() {
        val source = hostSource()

        assertTrue(
            source.contains(
                "tellPlayer = if (poisonTarget == spyCard?.name) null else {",
            ),
        )
        assertTrue(
            source.contains(
                "displayProposition = if (poisonTarget == spyCard?.name) null else InformationProposition.GrimoireState(",
            ),
        )
        assertTrue(
            source.contains(
                "hostInstruction = if (poisonTarget == spyCard?.name) text(\"照常轻拍间谍示意睁眼，但不要展示真实魔典；停顿后示意闭眼。\", \"Wake the Spy normally, but do not show the real grimoire. Pause, then signal them to close their eyes.\")",
            ),
        )
    }

    @Test
    fun `other night poisoned Spy suppresses true Grimoire while healthy Spy retains it`() {
        val source = hostSource()

        assertTrue(
            source.contains(
                "tellPlayer = if (effectivePoisonForRole(\"Spy\") == spyCard?.name) null else {",
            ),
        )
        assertTrue(
            source.contains(
                "displayProposition = if (effectivePoisonForRole(\"Spy\") == spyCard?.name) null else InformationProposition.GrimoireState(",
            ),
        )
        assertTrue(
            source.contains(
                "hostInstruction = if (effectivePoisonForRole(\"Spy\") == spyCard?.name) text(\"照常唤醒间谍，但不要展示真实魔典。\", \"Wake the Spy normally, but do not show the real grimoire.\")",
            ),
        )
    }

    private fun hostSource(): String = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText()
}
