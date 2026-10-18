package com.codex.campboardgamehost
import org.junit.Assert.*
import org.junit.Test
class ClocktowerChambermaidSelectionAuthorityTest {
    @Test fun `unknown duplicate and incomplete query targets remain incomplete`() {
        val missing = revalidateTwoPlayerSelection("Alice", "Bob", setOf("Bob", "Carol"))
        assertNull(missing.first)
        assertEquals("Bob", missing.second)
        assertFalse(missing.isComplete)
        val duplicate = revalidateTwoPlayerSelection("Alice", "Alice", setOf("Alice", "Bob"))
        assertEquals("Alice", duplicate.first)
        assertNull(duplicate.second)
        assertFalse(duplicate.isComplete)
        assertFalse(revalidateTwoPlayerSelection(null, "Bob", setOf("Alice", "Bob")).isComplete)
        assertTrue(revalidateTwoPlayerSelection("Alice", "Bob", setOf("Alice", "Bob")).isComplete)
    }
}
