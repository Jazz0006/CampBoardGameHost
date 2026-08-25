package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppClocktowerPresentationThemeOwnershipTest {
    private val productionRoot = File(
        "src/main/java/com/codex/campboardgamehost",
    )

    private val appRootFile = File(
        productionRoot,
        "CampBoardGameHostApp.kt",
    )

    private val themeOwnerFile = File(
        productionRoot,
        "ClocktowerPresentationTheme.kt",
    )

    @Test
    fun `clocktower dark theme has a dedicated presentation owner`() {
        assertTrue(themeOwnerFile.exists())

        val source = themeOwnerFile.readText(Charsets.UTF_8)

        assertTrue(
            source.contains(
                "internal fun ClocktowerDarkTheme(content: @Composable () -> Unit)",
            ),
        )
        assertTrue(source.contains("androidx.compose.material3.darkColorScheme("))
        assertTrue(source.contains("primary = Color(0xFFC5A56A)"))
        assertTrue(source.contains("background = Color(0xFF0B0D10)"))
        assertTrue(source.contains("surface = Color(0xFF14171C)"))

        assertFalse(source.contains("remember("))
        assertFalse(source.contains("LaunchedEffect"))
        assertFalse(source.contains("DisposableEffect"))
        assertFalse(source.contains("SideEffect"))
        assertFalse(source.contains("ClocktowerGameSession"))
        assertFalse(source.contains("JSONObject"))
        assertFalse(source.contains("ClocktowerNewDemonConfirmationScreen("))
    }

    @Test
    fun `app root no longer declares clocktower dark theme`() {
        val rootSource = appRootFile.readText(Charsets.UTF_8)

        assertFalse(rootSource.contains("internal fun ClocktowerDarkTheme("))
        assertFalse(rootSource.contains("private fun ClocktowerDarkTheme("))
    }

    @Test
    fun `clocktower dark theme declaration has exactly one owner`() {
        val owners = productionRoot
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter {
                it.readText(Charsets.UTF_8)
                    .contains("fun ClocktowerDarkTheme(")
            }
            .map {
                it.relativeTo(productionRoot).invariantSeparatorsPath
            }
            .sorted()
            .toList()

        assertEquals(
            listOf("ClocktowerPresentationTheme.kt"),
            owners,
        )
    }
}
