package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerRuleOnlyCompactChoiceTest {
    @Test
    fun `compact shortcut requires precisely one legal result`() {
        assertNull(clocktowerRuleOnlyCompactChoice(emptyList<Int>()))
        assertNull(clocktowerRuleOnlyCompactChoice(listOf(0, 1)))
        assertEquals(0, clocktowerRuleOnlyCompactChoice(listOf(0)))
    }

    @Test
    fun `duplicate rows still do not imply unique Host authority`() {
        assertNull(clocktowerRuleOnlyCompactChoice(listOf("same", "same")))
    }
}
