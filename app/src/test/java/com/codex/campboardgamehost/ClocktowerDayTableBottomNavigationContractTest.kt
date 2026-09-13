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
    fun `day scaffold delegates window and bottom navigation ownership to shared host scaffold`() {
        val daySource = source(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayTableScaffoldUi.kt",
        )
        val sharedSource = source(
            "src/main/java/com/codex/campboardgamehost/ClocktowerHostFullScreenScaffold.kt",
        )

        assertTrue(daySource.contains("ClocktowerHostFullScreenScaffold("))
        assertFalse(daySource.contains("HostBottomActionBar("))
        assertTrue(sharedSource.contains(".weight(1f)"))
        assertTrue(sharedSource.contains(".clocktowerHostBottomNavigationBarPadding()"))
        assertFalse(sharedSource.contains("navigationBarsIgnoringVisibility"))
        assertEquals(1, Regex("HostBottomActionBar\\(").findAll(sharedSource).count())
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
