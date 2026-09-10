package com.codex.campboardgamehost

internal sealed interface ClocktowerNightAdvanceDirective {
    data class MoveTo(val stepIndex: Int) : ClocktowerNightAdvanceDirective
    data object CompleteNight : ClocktowerNightAdvanceDirective
}

/**
 * Decides navigation from the list visible before the current step's confirmation mutates
 * checkpoint-derived flow. Dynamic flow mutations must not complete the night from that stale list.
 */
internal fun clocktowerNightAdvanceDirective(
    currentStepIndex: Int,
    currentStepCount: Int,
    flowMayExpandAfterConfirmation: Boolean,
): ClocktowerNightAdvanceDirective {
    require(currentStepCount > 0) { "Night advance requires at least one step." }
    require(currentStepIndex in 0 until currentStepCount) { "Current night step must be in range." }
    return when {
        currentStepIndex < currentStepCount - 1 ->
            ClocktowerNightAdvanceDirective.MoveTo(currentStepIndex + 1)
        flowMayExpandAfterConfirmation ->
            ClocktowerNightAdvanceDirective.MoveTo(currentStepIndex + 1)
        else -> ClocktowerNightAdvanceDirective.CompleteNight
    }
}

/**
 * A requested index at or past the refreshed list size proves that the confirmation did not insert
 * any new work at that slot. Otherwise the refreshed list owns the newly inserted trigger step.
 */
internal fun clocktowerDeferredNightAdvanceShouldComplete(
    requestedStepIndex: Int,
    refreshedStepCount: Int,
): Boolean {
    require(requestedStepIndex >= 0) { "Requested night step index must be non-negative." }
    require(refreshedStepCount > 0) { "Night advance reconciliation requires at least one step." }
    return requestedStepIndex >= refreshedStepCount
}
