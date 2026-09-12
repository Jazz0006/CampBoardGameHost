package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightDialogInsetsContractTest {
    @Test
    fun `full screen compose dialogs opt into edge to edge window ownership`() {
        val sourceRoot = sourcePath("src/main/java/com/codex/campboardgamehost")
        val unsafe = mutableListOf<String>()
        val dialogProperties = Regex("""DialogProperties\([\s\S]*?\)""")

        Files.walk(sourceRoot).use { paths ->
            paths
                .filter { path -> path.isRegularFile() && path.toString().endsWith(".kt") }
                .forEach { path ->
                    val source = Files.readString(path)
                    dialogProperties.findAll(source).forEach { match ->
                        val call = match.value
                        if (
                            call.contains("usePlatformDefaultWidth = false") &&
                            !call.contains("decorFitsSystemWindows = false")
                        ) {
                            unsafe += sourceRoot.relativize(path).toString()
                        }
                    }
                }
        }

        assertTrue(
            "Full-screen DialogProperties must set decorFitsSystemWindows=false: ${unsafe.distinct()}",
            unsafe.isEmpty(),
        )
    }

    @Test
    fun `night bottom navigation reserves hidden navigation bar inset`() {
        val source = Files.readString(
            sourcePath("src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt"),
        )
        val bottomBar = source
            .substringAfter("internal fun ClocktowerNightBottomActionBar(")
            .substringBefore("@Composable\ninternal fun ClocktowerSingleTargetSquareTableDialog(")

        assertTrue(bottomBar.contains("windowInsetsPadding"))
        assertTrue(bottomBar.contains("WindowInsets.navigationBarsIgnoringVisibility"))
        assertTrue(bottomBar.contains("WindowInsetsSides.Bottom"))
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
