package com.codex.campboardgamehost

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightStepResultSurfaceOwnershipTest {
    @Test
    fun `dedicated Chambermaid surface exclusively owns Chambermaid results`() {
        val source = nightStepSource()

        assertTrue(source.contains("ClocktowerChambermaidSquareTableDialog("))
        val actionOwnership = source.substringAfter("val actionOwnsSquareTable = step.action in setOf(")
            .substringBefore(")")
        assertTrue(actionOwnership.contains("ClocktowerNightAction.Chambermaid"))

        val plainFallback = source.substringAfter("val plainInformationDisplayStep =")
            .substringBefore("val plainInformationSquareTablePresentation =")
        assertTrue(plainFallback.contains("!actionOwnsSquareTable"))
        assertFalse(source.contains("step.action != ClocktowerNightAction.Chambermaid"))
    }

    @Test
    fun `superseded generic non-pair result-first surface is retired`() {
        val source = nightStepSource()

        assertFalse(source.contains("val nonPairResultFirstCandidates ="))
        assertFalse(source.contains("nonPairResultFirstCandidates.isNotEmpty()"))
        assertFalse(source.contains("This ability is unreliable. Choose a result to show."))
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
