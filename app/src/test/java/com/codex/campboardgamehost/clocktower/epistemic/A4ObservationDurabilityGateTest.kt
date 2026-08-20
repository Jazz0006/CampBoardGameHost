package com.codex.campboardgamehost.clocktower.epistemic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class A4ObservationDurabilityGateTest {
    @Test fun `failed persistence never releases pending observation`() {
        val gate = A4ObservationDurabilityGate()
        gate.markPending("record-1")
        assertNull(gate.releaseAfterPersistence(false))
        assertEquals("record-1", gate.releaseAfterPersistence(true))
        assertNull(gate.releaseAfterPersistence(true))
    }

    @Test fun `newest pending observation is released after durable persistence`() {
        val gate = A4ObservationDurabilityGate()
        gate.markPending("record-1")
        gate.markPending("record-2")
        assertEquals("record-2", gate.releaseAfterPersistence(true))
        assertNull(gate.releaseAfterPersistence(true))
    }

    @Test fun `clear prevents stale observation from crossing a session boundary`() {
        val gate = A4ObservationDurabilityGate()
        gate.markPending("record-1")
        gate.clear()
        assertNull(gate.releaseAfterPersistence(true))
    }
}
