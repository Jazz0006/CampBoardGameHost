package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerSageSquareTableWiringTest {
    @Test
    fun `Sage materialization is owned outside protected Host`() {
        val hostSource = sourceFile(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        )
        val sageSource = sourceFile(
            "src/main/java/com/codex/campboardgamehost/ClocktowerSageStepMaterializer.kt",
        )

        assertTrue(hostSource.contains("clocktowerSageStepMaterializer("))
        assertFalse(hostSource.contains("fun recommendedSageOptions("))
        assertTrue(sageSource.contains("internal fun clocktowerSageDisplayOptions("))
        assertTrue(sageSource.contains("presentationSubjectSeats"))
    }

    @Test
    fun `Sage owns result presentation inside square table`() {
        val source = nightStepSource()

        assertTrue(source.contains("val sageResultChoices = clocktowerSageResultChoices("))
        assertTrue(source.contains("val usesSageSquareTable = sageResultChoices.isNotEmpty()"))
        assertTrue(source.contains("ClocktowerSageSquareTableDialog("))
        assertTrue(source.contains("fun showSageChoice(choice: ClocktowerSageResultChoice)"))
        assertTrue(source.contains("!usesSageSquareTable &&"))
    }

    private fun nightStepSource(): String = sourceFile(
        "src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    )

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
