package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerHostSelectionSemanticsOwnershipTest {
    private val productionRoot = File(
        "src/main/java/com/codex/campboardgamehost",
    )

    private val appRootFile = File(
        productionRoot,
        "CampBoardGameHostApp.kt",
    )

    private val selectionSemanticsFile = File(
        productionRoot,
        "ClocktowerHostSelectionSemantics.kt",
    )

    @Test
    fun `selection semantics have a dedicated owner`() {
        assertTrue(selectionSemanticsFile.exists())

        val rootSource = appRootFile.readText(Charsets.UTF_8)
        val ownerSource = selectionSemanticsFile.takeIf { it.exists() }?.readText(Charsets.UTF_8).orEmpty()

        assertFalse(rootSource.contains("internal enum class TwoPlayerSelectionAction"))
        assertFalse(rootSource.contains("internal fun twoPlayerSelectionAction("))
        assertFalse(rootSource.contains("internal fun shouldAutoAdvanceRedHerring("))

        assertTrue(ownerSource.contains("internal enum class TwoPlayerSelectionAction"))
        assertTrue(ownerSource.contains("internal fun twoPlayerSelectionAction("))
        assertTrue(ownerSource.contains("internal fun shouldAutoAdvanceRedHerring("))
    }

    @Test
    fun `selection semantics have exactly one production owner`() {
        val declarations = listOf(
            "internal enum class TwoPlayerSelectionAction",
            "internal fun twoPlayerSelectionAction(",
            "internal fun shouldAutoAdvanceRedHerring(",
        )

        declarations.forEach { declaration ->
            val owners = productionRoot
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .filter { it.readText(Charsets.UTF_8).contains(declaration) }
                .map { it.relativeTo(productionRoot).invariantSeparatorsPath }
                .sorted()
                .toList()

            assertEquals(listOf("ClocktowerHostSelectionSemantics.kt"), owners)
        }
    }

    @Test
    fun `S7 point five UI helpers remain root owned`() {
        val rootSource = appRootFile.readText(Charsets.UTF_8)
        val ownerSource = selectionSemanticsFile.takeIf { it.exists() }?.readText(Charsets.UTF_8).orEmpty()

        listOf(
            "internal fun SelectablePlayerChips(",
            "internal fun SelectableTwoPlayerChips(",
            "internal fun SelectableSeatNumbers(",
        ).forEach { declaration ->
            assertTrue(rootSource.contains(declaration))
            assertFalse(ownerSource.contains(declaration))
        }
    }
}
