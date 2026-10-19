package com.codex.campboardgamehost.clocktower.session

/**
 * Typed same-night transaction checkpoint.
 *
 * The values deliberately distinguish player drafts from confirmed mechanical facts. This
 * model is an input to live night transactions, not a persistence wire contract.
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
    val nextTimelineGlobalSequence: Long = 0L,
    /** Read-only projection of the Session-owned staged attack's stable step identity. */
    val pendingNightAttackStepId: String? = null,
    /** Read-only target projection for hypothetical later-night wake/death-trigger planning. */
    val pendingNightAttackTargetName: String? = null,
    val pendingNightAttackTargetSeat: Int? = null,
) {
    init {
        require(round > 0)
        require(gameStateRevision >= 0 && playerInputRevision >= 0)
        require(nightStepIndex >= 0)
        require(nextTimelineGlobalSequence >= 0) { "nextTimelineGlobalSequence cannot be negative." }
    }

}
