package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvanceWiringTest {
    @Test
    fun `automatic red herring advance waits for a real recommendation but skips non-actions`() {
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = false,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = false,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertFalse(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = false,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = true,
                hasSelectedRedHerring = true,
            ),
        )
        assertTrue(
            shouldAutoAdvanceRedHerring(
                automaticStorytellerInfo = true,
                isRedHerringStep = true,
                isRealAction = false,
                hasSelectedRedHerring = false,
            ),
        )
    }

    @Test
    fun `host screen is the only automatic red herring navigation owner`() {
        val hostSource = hostScreenSource()
        val nightStepSource = nightStepSource()

        assertTrue(hostSource.contains("shouldAutoAdvanceRedHerring("))
        assertTrue(hostSource.contains("isRedHerringStep = currentStep.action == ClocktowerNightAction.RedHerring"))
        assertTrue(hostSource.contains("isRealAction = currentStep.isRealAction"))
        assertTrue(hostSource.contains("hasSelectedRedHerring = redHerring != null"))
        assertTrue(hostSource.contains("advanceNightStep()"))

        assertFalse(nightStepSource.contains("shouldAutoAdvanceRedHerring("))
    }

    private fun hostScreenSource(): String = sourceAt(
        Path.of("src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"),
    )

    private fun nightStepSource(): String = sourceAt(
        Path.of("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt"),
    )

    private fun sourceAt(relative: Path): String {
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("Source not found from ${Path.of("").toAbsolutePath()}: $relative")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
