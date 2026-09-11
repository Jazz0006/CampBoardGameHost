package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerLegacyNightInformationSurfaceRetirementContractTest {
    @Test
    fun `night information is owned by dedicated square table surfaces`() {
        val source = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")

        assertTrue(source.contains("ClocktowerPairInformationSquareTableDialog("))
        assertTrue(source.contains("ClocktowerEmpathSquareTableDialog("))
        assertTrue(source.contains("ClocktowerChefSquareTableDialog("))
        assertTrue(source.contains("ClocktowerNewDemonIdentitySquareTableDialog("))

        assertFalse(source.contains("StructuredNumberInformationDecisionPanel("))
        assertFalse(source.contains("ClocktowerPairRecommendationPresentationSection("))
        assertFalse(source.contains("SpyRegistrationPanel("))
        assertFalse(source.contains("RecluseRegistrationPanel("))
        assertFalse(source.contains("HostActionSection("))
        assertFalse(source.contains("推荐给说书人的完整信息"))
        assertFalse(source.contains("This ability is unreliable. Choose a result to show."))
        assertFalse(source.contains("stringResource(R.string.clocktower_host_show_to_player)"))
    }

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
        val source = sourceFile("src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt")

        assertTrue(source.contains("allowManualEditing: Boolean"))
        assertTrue(source.contains("allowManualEditing &&"))
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
