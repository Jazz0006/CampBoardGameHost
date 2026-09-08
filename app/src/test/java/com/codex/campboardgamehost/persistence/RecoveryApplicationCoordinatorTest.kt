package com.codex.campboardgamehost

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RecoveryApplicationCoordinatorTest {
    @Test
    fun readyPlanCrossesSessionBoundaryImmediatelyBeforeSingleApply() {
        val raw = JSONObject().put("marker", "raw")
        val plan = undercoverPlan()
        val events = mutableListOf<String>()
        var appliedPlan: ValidatedRecoveryPlan? = null

        val result = RecoveryApplicationCoordinator.apply(
            raw = raw,
            prepare = {
                events += "prepare"
                RecoveryPlanPreparation.Ready(plan)
            },
            clearRejected = { events += "clear" },
            crossSessionBoundary = { events += "boundary" },
            applyValidated = {
                events += "apply"
                appliedPlan = it
            },
        )

        assertEquals(RecoveryApplicationResult.Applied, result)
        assertEquals(listOf("prepare", "boundary", "apply"), events)
        assertSame(plan, appliedPlan)
    }

    @Test
    fun rejectedRecoveryClearsWithoutBoundaryOrLiveMutation() {
        val events = mutableListOf<String>()

        val result = RecoveryApplicationCoordinator.apply(
            raw = JSONObject(),
            prepare = {
                events += "prepare"
                RecoveryPlanPreparation.Rejected(RecoveryRejectionReason.Expired)
            },
            clearRejected = { events += "clear" },
            crossSessionBoundary = { events += "boundary" },
            applyValidated = { events += "apply" },
        )

        assertEquals(
            RecoveryApplicationResult.Rejected(RecoveryRejectionReason.Expired),
            result,
        )
        assertEquals(listOf("prepare", "clear"), events)
    }

    @Test
    fun absentRecoveryDoesNothing() {
        val events = mutableListOf<String>()

        val result = RecoveryApplicationCoordinator.apply(
            raw = null,
            prepare = {
                events += "prepare"
                error("prepare must not run")
            },
            clearRejected = { events += "clear" },
            crossSessionBoundary = { events += "boundary" },
            applyValidated = { events += "apply" },
        )

        assertEquals(RecoveryApplicationResult.Absent, result)
        assertEquals(emptyList<String>(), events)
    }

    private fun undercoverPlan(): ValidatedRecoveryPlan {
        val snapshot = RecoverySnapshot(
            compatibilityToken = "test-current",
            savedAtMillis = 1L,
            game = UndercoverRecovery(
                entryPoint = RecoveryEntryPoint.Stable,
                currentDealIndex = 0,
                round = 1,
                cards = listOf(
                    PlayerCard("Alice", Role.Civilian, "cat"),
                    PlayerCard("Bob", Role.Undercover, "dog"),
                ),
                records = emptyList(),
                outcome = null,
                undercoverCount = 1,
                includeBlank = false,
                lastWordsMode = LastWordsMode.FirstDay,
            ),
        )
        return ValidatedRecoveryPlan(
            snapshot = snapshot,
            safeReentry = RecoverySafeReentry.UndercoverGame,
            presentResults = false,
        )
    }
}
