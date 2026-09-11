package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSetupRecommendationStyleWiringTest {
    @Test
    fun `first-night setup selection uses the unified automatic Storyteller style`() {
        val source = hostSource()

        assertTrue(source.contains("mutableStateOf(automaticStorytellerStyle)"))
        assertTrue(source.countOccurrences("selectedRecommendationStyle = automaticStorytellerStyle") >= 2)
        assertFalse(source.contains("mutableStateOf(RecommendationStyle.BALANCED)"))
        assertFalse(source.contains("selectedRecommendationStyle = RecommendationStyle.BALANCED"))
    }

    private fun hostSource(): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("ClocktowerHostScreen.kt source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }

    private fun String.countOccurrences(needle: String): Int =
        windowed(size = needle.length, step = 1, partialWindows = false).count { it == needle }
}
