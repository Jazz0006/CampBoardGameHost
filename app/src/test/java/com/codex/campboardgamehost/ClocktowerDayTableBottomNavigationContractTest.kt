package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDayTableBottomNavigationContractTest {
    private val dayTableSources = listOf(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayOverviewTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerPendingNominationTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerVoteTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerSlayerTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerArtistTableUi.kt",
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerKlutzTableUi.kt",
    )

    @Test
    fun `all persistent day tables use shared scaffold for navigation outside the table`() {
        dayTableSources.forEach { relativeText ->
            val source = source(relativeText)
            assertTrue(
                "$relativeText must use ClocktowerDayTableScaffold",
                source.contains("ClocktowerDayTableScaffold("),
            )
        }
    }

    @Test
    fun `shared day scaffold reserves table space then uses shared safe bottom inset policy`() {
        val source = source(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayTableScaffoldUi.kt",
        )
        val tableIndex = source.indexOf("tableContent()")
        val navigationIndex = source.indexOf("HostBottomActionBar(")

        assertTrue("table content must be rendered", tableIndex >= 0)
        assertTrue("bottom navigation must follow the table content", navigationIndex > tableIndex)
        assertTrue(source.contains(".weight(1f)"))
        assertTrue(source.contains(".clocktowerHostBottomNavigationBarPadding()"))
        assertFalse(source.contains("WindowInsets.navigationBars"))
        assertFalse(source.contains("navigationBarsIgnoringVisibility"))
        assertEquals(1, Regex("HostBottomActionBar\\(").findAll(source).count())
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
