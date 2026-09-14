package com.codex.campboardgamehost

internal sealed interface ClocktowerNightAdvanceDirective {
    data class MoveTo(val stepIndex: Int) : ClocktowerNightAdvanceDirective
    data class AwaitRefreshedFlow(val currentStepIndex: Int) : ClocktowerNightAdvanceDirective
    data object CompleteNight : ClocktowerNightAdvanceDirective
}

/**
 * Decides navigation from the list visible before the current step's confirmation mutates
 * checkpoint-derived flow. Dynamic flow mutations keep the durable cursor on the current renderable
 * step until the refreshed flow can prove either a real next step or explicit night completion.
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
            ClocktowerNightAdvanceDirective.AwaitRefreshedFlow(currentStepIndex)
        else -> ClocktowerNightAdvanceDirective.CompleteNight
    }
}

/** Resolves a pending dynamic advance only against a refreshed, renderable night-step list. */
internal fun clocktowerRefreshedNightAdvanceDirective(
    pending: ClocktowerNightAdvanceDirective.AwaitRefreshedFlow,
    refreshedStepCount: Int,
): ClocktowerNightAdvanceDirective {
    require(refreshedStepCount > 0) { "Night advance reconciliation requires at least one step." }
    require(pending.currentStepIndex in 0 until refreshedStepCount) {
        "Pending night cursor must remain renderable while flow refresh is pending."
    }
    val nextStepIndex = pending.currentStepIndex + 1
    return if (nextStepIndex < refreshedStepCount) {
        ClocktowerNightAdvanceDirective.MoveTo(nextStepIndex)
    } else {
        ClocktowerNightAdvanceDirective.CompleteNight
    }
}
