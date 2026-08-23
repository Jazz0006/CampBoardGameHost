package com.codex.campboardgamehost

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerStorytellerRecommendationCardOwnershipTest {
    private fun findRepositoryRoot(): File {
        val knownHostSource = "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
        val workingDirectory = System.getProperty("user.dir") ?: error("Working directory is unavailable")
        var directory = File(workingDirectory).absoluteFile
        while (true) {
            if (File(directory, knownHostSource).isFile) return directory
            val parent = directory.parentFile ?: error("Repository root not found from ${directory.path}")
            if (parent == directory) error("Repository root not found from ${directory.path}")
            directory = parent
        }
    }

    private val repoRoot = findRepositoryRoot()
    private val host = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    )
    private val recommendationUi = File(
        repoRoot,
        "app/src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt",
    )

    @Test
    fun `storyteller recommendation card and editor live in dedicated ui owner`() {
        assertTrue("Host source must exist", host.isFile)
        assertTrue("Dedicated recommendation UI source must exist", recommendationUi.isFile)

        val hostText = host.readText()
        val recommendationUiText = recommendationUi.readText()

        assertFalse(
            "StorytellerRecommendationCard must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun StorytellerRecommendationCard("),
        )
        assertFalse(
            "RecommendationDecisionEditor must no longer live in ClocktowerHostScreen.kt",
            hostText.contains("fun RecommendationDecisionEditor("),
        )
        assertTrue(
            "StorytellerRecommendationCard must live in the dedicated recommendation UI source",
            recommendationUiText.contains("internal fun StorytellerRecommendationCard("),
        )
        assertTrue(
            "RecommendationDecisionEditor must live in the dedicated recommendation UI source",
            recommendationUiText.contains("internal fun RecommendationDecisionEditor("),
        )
    }
}
