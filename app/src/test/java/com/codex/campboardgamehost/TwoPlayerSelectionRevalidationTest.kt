package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoPlayerSelectionRevalidationTest {
    private val eligibleNames = setOf("Alice", "Bob", "Carol")

    @Test
    fun `two still eligible distinct players remain selected`() {
        val result = revalidateTwoPlayerSelection(
            first = "Alice",
            second = "Bob",
            eligible = eligibleNames,
        )

        assertEquals("Alice", result.first)
        assertEquals("Bob", result.second)
        assertTrue(result.isComplete)
    }

    @Test
    fun `stale first selection clears only first slot`() {
        val result = revalidateTwoPlayerSelection(
            first = "Stale",
            second = "Bob",
            eligible = eligibleNames,
        )

        assertNull(result.first)
        assertEquals("Bob", result.second)
        assertFalse(result.isComplete)
    }

    @Test
    fun `stale second selection clears only second slot`() {
        val result = revalidateTwoPlayerSelection(
            first = "Alice",
            second = "Stale",
            eligible = eligibleNames,
        )

        assertEquals("Alice", result.first)
        assertNull(result.second)
        assertFalse(result.isComplete)
    }

    @Test
    fun `duplicate restored selection keeps first and clears second deterministically`() {
        val result = revalidateTwoPlayerSelection(
            first = "Alice",
            second = "Alice",
            eligible = eligibleNames,
        )

        assertEquals("Alice", result.first)
        assertNull(result.second)
        assertFalse(result.isComplete)
    }

    @Test
    fun `one legal selection is incomplete`() {
        val result = revalidateTwoPlayerSelection(
            first = null,
            second = "Bob",
            eligible = eligibleNames,
        )

        assertNull(result.first)
        assertEquals("Bob", result.second)
        assertFalse(result.isComplete)
    }

    @Test
    fun `two legal distinct selections are complete`() {
        val result = revalidateTwoPlayerSelection(
            first = "Carol",
            second = "Alice",
            eligible = eligibleNames,
        )

        assertTrue(result.isComplete)
    }
}
