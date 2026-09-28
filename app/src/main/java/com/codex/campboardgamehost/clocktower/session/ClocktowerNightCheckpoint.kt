package com.codex.campboardgamehost.clocktower.session

/**
 * Typed same-night transaction checkpoint.
 *
 * The values deliberately distinguish player drafts from confirmed mechanical facts. Active
 * Recovery persists only the current durable subset through its own typed codec; this model itself
 * is not a persistence wire contract.
 */
internal data class ClocktowerNightCheckpoint(
    val phaseName: String,
    val round: Int,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val nightStarted: Boolean,
    val nightStepIndex: Int,
    val confirmedAttackTarget: String?,
    val attackDraftTarget: String?,
    val confirmedPoisonTarget: String?,
    val poisonDraftTarget: String?,
    val confirmedMonkTarget: String?,
    val monkDraftTarget: String?,
    val confirmedMayorRedirectTarget: String?,
    val mayorRedirectDraftTarget: String?,
    val pendingNewDemonName: String?,
    val pendingNightNewDemonIdentityName: String? = null,
    val demonSuccessorDraftTarget: String?,
    val confirmedDemonSuccessorTarget: String? = null,
    val nextTimelineGlobalSequence: Long = 0L,
) {
    init {
        require(round > 0)
        require(gameStateRevision >= 0 && playerInputRevision >= 0)
        require(nightStepIndex >= 0)
        require(nextTimelineGlobalSequence >= 0) { "nextTimelineGlobalSequence cannot be negative." }
    }

}
