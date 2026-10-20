package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRedHerringAutoAdvancePolicyTest {
    @Test
    fun `stale prior step AI error or busy never blocks a current Host-determined result`() {
        assertFalse(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = "round1-chef",
            busy = true, error = "old error"))
        assertFalse(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = null,
            busy = true, error = null))
        assertFalse(clocktowerCurrentAiRequestBlocked(
            currentKey = null, adviceKey = null,
            busy = true, error = "old error"))
        assertTrue(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = "round2-ft-1-3",
            busy = true, error = null))
        assertTrue(clocktowerCurrentAiRequestBlocked(
            currentKey = "round2-ft-1-3", adviceKey = "round2-ft-1-3",
            busy = false, error = "current error"))
    }
}
