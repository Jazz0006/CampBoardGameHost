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
        val source = String(
            Files.readAllBytes(
                sourcePath("src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt"),
            ),
            Charsets.UTF_8,
        )

        assertFalse(source.contains("import androidx.compose.ui.window.Dialog"))
        assertFalse(source.contains("DialogProperties("))
        assertFalse(source.contains("Dialog("))
        assertTrue(source.contains("modifier = Modifier.fillMaxSize()"))
    }

    @Test
    fun `night bottom navigation uses shared visibility aware inset policy`() {
        val source = String(
            Files.readAllBytes(
                sourcePath("src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt"),
            ),
            Charsets.UTF_8,
        )
        val bottomBar = source
            .substringAfter("internal fun ClocktowerNightBottomActionBar(")
            .substringBefore("@Composable\ninternal fun ClocktowerSingleTargetSquareTableDialog(")

        assertTrue(bottomBar.contains("clocktowerHostBottomNavigationBarPadding()"))
        assertFalse(bottomBar.contains("windowInsetsPadding"))
        assertFalse(bottomBar.contains("navigationBarsIgnoringVisibility"))
    }

    private fun sourcePath(relativeText: String): Path {
        val relative = Path.of(relativeText)
        val fromRoot = Path.of("app").resolve(relative)
        return when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("Source not found from ${Path.of("").toAbsolutePath()}: $relativeText")
        }
    }
}
