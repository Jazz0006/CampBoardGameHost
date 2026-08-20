package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Test

class FirstNightInformationLifecycleTest {
    @Test fun `state changes invalidate drafts but preserve displayed information`() {
        val shown = FirstNightInformationLifecycle().publish("drunk-librarian").display("drunk-librarian")
        val changed = shown.publish("chef").invalidateUnshown()
        assertEquals(setOf("drunk-librarian"), changed.displayedDecisionIds)
        assertEquals(emptySet<String>(), changed.readyDecisionIds)
        assertEquals(1, changed.generation)
    }
}
