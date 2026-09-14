package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression guard for immersive full-screen night surfaces on devices with gesture navigation. */
class ClocktowerNightDialogInsetsContractTest {
    private val nightSquareTableSources = listOf(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt",
    )

    @Test
    fun `night square tables stay on the Activity window instead of opening full screen dialogs`() {
        nightSquareTableSources.forEach { relativeText ->
            val source = source(relativeText)
            assertFalse(
                "$relativeText must not import platform Dialog",
                source.contains("import androidx.compose.ui.window.Dialog"),
            )
            assertFalse(
                "$relativeText must not configure a platform Dialog window",
                source.contains("DialogProperties("),
            )
            assertFalse(
                "$relativeText must not open a platform Dialog window",
                source.contains("\n    Dialog("),
            )
            assertFalse(
                "$relativeText must not own the retired Night bottom bar",
                source.contains("ClocktowerNightBottomActionBar("),
            )
        }
        nightSquareTableSources.forEach { relativeText ->
            assertTrue(
                "$relativeText must delegate to the shared square-table scaffold",
                source(relativeText).contains("ClocktowerHostSquareTableScaffold("),
            )
        }
        assertTrue(
            source(
                "src/main/java/com/codex/campboardgamehost/ClocktowerHostSquareTableScaffold.kt",
            ).contains("ClocktowerHostFullScreenScaffold("),
        )
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

    @Test
    fun `night step cannot retain a dormant second bottom navigation owner`() {
        val nightStep = source(
            "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
        )
        val hostScreen = source(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        )

        assertFalse(nightStep.contains("showNavigationActions"))
        assertFalse(nightStep.contains("HostBottomActionBar("))
        assertFalse(hostScreen.contains("showNavigationActions = false"))
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
