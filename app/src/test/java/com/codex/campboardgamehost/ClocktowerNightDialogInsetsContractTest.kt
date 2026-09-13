package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression guard for immersive full-screen night surfaces on devices with gesture navigation. */
class ClocktowerNightDialogInsetsContractTest {
    @Test
    fun `night square table stays on the Activity window instead of opening a full screen dialog`() {
        val source = source(
            "src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt",
        )

        assertFalse(source.contains("import androidx.compose.ui.window.Dialog"))
        assertFalse(source.contains("DialogProperties("))
        assertFalse(source.contains("Dialog("))
        assertTrue(source.contains("ClocktowerHostFullScreenScaffold("))
    }

    @Test
    fun `night active shell delegates bottom navigation to the shared host scaffold`() {
        val source = source(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNightScreen.kt",
        )

        assertTrue(source.contains("contentOwnsFullScreen"))
        assertTrue(source.contains("ClocktowerHostFullScreenScaffold("))
        assertFalse(source.contains("ClocktowerNightBottomActionBar("))
    }

    private fun source(relativeText: String): String {
        val relative = Path.of(relativeText)
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("Source not found from ${Path.of("").toAbsolutePath()}: $relativeText")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }
}
