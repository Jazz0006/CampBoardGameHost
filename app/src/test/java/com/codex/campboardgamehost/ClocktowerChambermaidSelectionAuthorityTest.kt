package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerChambermaidSelectionAuthorityTest {
    @Test
    fun `stale Chambermaid target cannot produce a player-visible count`() {
        val resolution = resolveChambermaidSelection(
            first = "Alice",
            second = "Bob",
            eligibleNames = setOf("Bob", "Carol"),
            wokeBecauseOwnAbilityNames = setOf("Alice", "Bob"),
        )

        assertNull(resolution.selection.first)
        assertEquals("Bob", resolution.selection.second)
        assertNull(resolution.wokeCount)
    }

    @Test
    fun `duplicate restored Chambermaid pair cannot produce a count`() {
        val resolution = resolveChambermaidSelection(
            first = "Alice",
            second = "Alice",
            eligibleNames = setOf("Alice", "Bob"),
            wokeBecauseOwnAbilityNames = setOf("Alice"),
        )

        assertEquals("Alice", resolution.selection.first)
        assertNull(resolution.selection.second)
        assertNull(resolution.wokeCount)
    }

    @Test
    fun `incomplete Chambermaid pair cannot produce a count`() {
        val resolution = resolveChambermaidSelection(
            first = null,
            second = "Bob",
            eligibleNames = setOf("Alice", "Bob"),
            wokeBecauseOwnAbilityNames = setOf("Bob"),
        )

        assertNull(resolution.wokeCount)
    }

    @Test
    fun `legal Chambermaid pair can truthfully resolve zero wakes`() {
        val resolution = resolveChambermaidSelection(
            first = "Alice",
            second = "Bob",
            eligibleNames = setOf("Alice", "Bob", "Carol"),
            wokeBecauseOwnAbilityNames = setOf("Carol"),
        )

        assertEquals(0, resolution.wokeCount)
    }

    @Test
    fun `legal Chambermaid pair can truthfully resolve one wake`() {
        val resolution = resolveChambermaidSelection(
            first = "Alice",
            second = "Bob",
            eligibleNames = setOf("Alice", "Bob", "Carol"),
            wokeBecauseOwnAbilityNames = setOf("Bob", "Carol"),
        )

        assertEquals(1, resolution.wokeCount)
    }

    @Test
    fun `legal Chambermaid pair can truthfully resolve two wakes`() {
        val resolution = resolveChambermaidSelection(
            first = "Alice",
            second = "Bob",
            eligibleNames = setOf("Alice", "Bob", "Carol"),
            wokeBecauseOwnAbilityNames = setOf("Alice", "Bob", "Carol"),
        )

        assertEquals(2, resolution.wokeCount)
    }

    /**
     * Coarse production ownership guard only. Chambermaid selection semantics are proved above
     * through the callable typed resolver; this check exists only while the Compose Host remains
     * a non-callable materialization boundary.
     */
    @Test
    fun `production Chambermaid display consumes typed selection authority`() {
        val hostSource = File(
            "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
        ).readText(Charsets.UTF_8)

        assertTrue(hostSource.contains("resolveChambermaidSelection("))
    }
}
