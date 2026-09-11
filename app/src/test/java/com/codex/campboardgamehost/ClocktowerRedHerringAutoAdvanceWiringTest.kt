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
    fun `night step wires automatic red herring policy to next navigation`() {
        val source = nightStepSource()

        assertTrue(source.contains("shouldAutoAdvanceRedHerring("))
        assertTrue(source.contains("isRedHerringStep = step.action == ClocktowerNightAction.RedHerring"))
        assertTrue(source.contains("isRealAction = step.isRealAction"))
        assertTrue(source.contains("hasSelectedRedHerring = selectedName != null"))
        assertTrue(source.contains("onNext()"))
    }

    private fun nightStepSource(): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("ClocktowerNightStepUi.kt source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
