package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDynamicNightAdvanceWiringTest {
    @Test
    fun `host defers completion for checkpoint actions that can expand other-night flow`() {
        val source = hostScreenSource()
        val activeNight = source.substringAfter(
            "if ((phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) && nightStarted) {",
        )
        val advanceBlock = activeNight.substringAfter("val advanceNightStep = {")
            .substringBefore("\n\n        LaunchedEffect(")

        assertTrue(advanceBlock.contains("clocktowerNightAdvanceDirective("))
        assertTrue(advanceBlock.contains("ClocktowerNightAction.DemonKill"))
        assertTrue(advanceBlock.contains("ClocktowerNightAction.MayorRedirect"))
        assertTrue(advanceBlock.contains("deferredNightAdvanceIndex = directive.stepIndex"))
        assertTrue(source.contains("clocktowerDeferredNightAdvanceShouldComplete("))
    }

    private fun hostScreenSource(): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("ClocktowerHostScreen.kt source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
