package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Test

class ClocktowerStorytellerLegacyModeRemovalContractTest {
    @Test
    fun `legacy storyteller automation mode compatibility surface is removed`() {
        val sourceRoot = sourceRoot()
        val legacyMode = sourceRoot.resolve(
            "com/codex/campboardgamehost/clocktower/domain/StorytellerAutomationMode.kt",
        )
        assertFalse("Legacy storyteller automation mode file still exists", Files.exists(legacyMode))

        val productionFiles = listOf(
            "com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicy.kt",
            "com/codex/campboardgamehost/clocktower/recommendation/UnifiedSelectionPool.kt",
            "com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
            "com/codex/campboardgamehost/clocktower/ui/ClocktowerHistoryScreen.kt",
            "com/codex/campboardgamehost/clocktower/ui/ClocktowerNightScreen.kt",
            "com/codex/campboardgamehost/clocktower/ui/ClocktowerDayScreen.kt",
        )
        productionFiles.forEach { relative ->
            val source = String(Files.readAllBytes(sourceRoot.resolve(relative)), Charsets.UTF_8)
            assertFalse("$relative still references StorytellerAutomationMode", source.contains("StorytellerAutomationMode"))
            assertFalse("$relative still exposes fromLegacyMode", source.contains("fromLegacyMode"))
        }
    }

    private fun sourceRoot(): Path {
        val relative = Path.of("src/main/java")
        val fromRoot = Path.of("app").resolve(relative)
        return when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("main source root not found from ${Path.of("").toAbsolutePath()}")
        }
    }
}
