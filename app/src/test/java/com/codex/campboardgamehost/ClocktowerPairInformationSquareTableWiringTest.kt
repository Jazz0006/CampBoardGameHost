package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerPairInformationSquareTableWiringTest {
    @Test
    fun `first night pair square table is not disabled by automatic mode`() {
        val source = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
        val pairRouting = source.substringAfter("val pairRecommendationPresentation =")
            .substringBefore("val usesResultFirstRegistration =")

        assertTrue(pairRouting.contains("Washerwoman"))
        assertTrue(pairRouting.contains("Librarian"))
        assertTrue(pairRouting.contains("Investigator"))
        assertFalse(pairRouting.contains("!automaticStorytellerInfo"))
    }

    @Test
    fun `pair square table gates manual editing instead of surface ownership`() {
        val nightSource = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
        val pairSource = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt")

        assertTrue(nightSource.contains("allowManualEditing = !automaticStorytellerInfo"))
        assertTrue(pairSource.contains("allowManualEditing: Boolean"))
        assertTrue(pairSource.contains("allowManualEditing &&"))
    }

    private fun sourceFile(relativeText: String): String {
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
