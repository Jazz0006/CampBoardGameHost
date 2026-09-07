package com.codex.campboardgamehost

import org.json.JSONObject

internal sealed interface RecoveryApplicationResult {
    object Absent : RecoveryApplicationResult
    object Applied : RecoveryApplicationResult
    data class Rejected(val reason: RecoveryRejectionReason) : RecoveryApplicationResult
}

/**
 * Atomic control boundary for process-loss recovery.
 *
 * No live-session mutation may occur before [prepare] has returned a fully validated plan. Once a
 * plan is ready, the old session boundary is crossed immediately before the single application
 * callback. Rejected recovery is cleared without entering either callback.
 */
internal object RecoveryApplicationCoordinator {
    fun apply(
        raw: JSONObject?,
        prepare: (JSONObject) -> RecoveryPlanPreparation,
        clearRejected: () -> Unit,
        crossSessionBoundary: () -> Unit,
        applyValidated: (ValidatedRecoveryPlan) -> Unit,
    ): RecoveryApplicationResult {
        if (raw == null) return RecoveryApplicationResult.Absent

        return when (val preparation = prepare(raw)) {
            is RecoveryPlanPreparation.Rejected -> {
                clearRejected()
                RecoveryApplicationResult.Rejected(preparation.reason)
            }
            is RecoveryPlanPreparation.Ready -> {
                crossSessionBoundary()
                applyValidated(preparation.plan)
                RecoveryApplicationResult.Applied
            }
        }
    }
}
