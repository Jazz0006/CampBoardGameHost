package com.codex.campboardgamehost.clocktower.session

/**
 * Persisted continuation point for an unfinished Clocktower night.
 *
 * The values deliberately distinguish player drafts from confirmed mechanical
 * facts. Keeping this boundary pure Kotlin lets restore behavior be verified
 * without Compose or Android storage.
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
    val demonSuccessorDraftTarget: String?,
) {
    init {
        require(round > 0)
        require(gameStateRevision >= 0 && playerInputRevision >= 0)
        require(nightStepIndex >= 0)
    }

    fun persistedValues(): Map<String, Any?> = mapOf(
        "clocktowerPhase" to phaseName,
        "round" to round,
        "clocktowerGameStateRevision" to gameStateRevision,
        "clocktowerPlayerInputRevision" to playerInputRevision,
        "clocktowerNightStarted" to nightStarted,
        "clocktowerNightStepIndex" to nightStepIndex,
        "clocktowerPendingNightDeath" to confirmedAttackTarget,
        "clocktowerDemonAttackDraftTarget" to attackDraftTarget,
        "clocktowerConfirmedPoisonTarget" to confirmedPoisonTarget,
        "clocktowerPoisonTarget" to poisonDraftTarget,
        "clocktowerConfirmedMonkProtectedTarget" to confirmedMonkTarget,
        "clocktowerMonkProtectedTarget" to monkDraftTarget,
        "clocktowerConfirmedMayorRedirectTarget" to confirmedMayorRedirectTarget,
        "clocktowerMayorRedirectTarget" to mayorRedirectDraftTarget,
        "clocktowerPendingNewDemonName" to pendingNewDemonName,
        "clocktowerDemonSuccessorTarget" to demonSuccessorDraftTarget,
    )

    companion object {
        fun fromPersistedValues(values: Map<String, Any?>): ClocktowerNightCheckpoint = ClocktowerNightCheckpoint(
            phaseName = values.string("clocktowerPhase") ?: "FirstNight",
            round = values.int("round")?.coerceAtLeast(1) ?: 1,
            gameStateRevision = values.long("clocktowerGameStateRevision")?.coerceAtLeast(0) ?: 0,
            playerInputRevision = values.long("clocktowerPlayerInputRevision")?.coerceAtLeast(0) ?: 0,
            nightStarted = values.boolean("clocktowerNightStarted") ?: false,
            nightStepIndex = values.int("clocktowerNightStepIndex")?.coerceAtLeast(0) ?: 0,
            confirmedAttackTarget = values.string("clocktowerPendingNightDeath"),
            attackDraftTarget = values.string("clocktowerDemonAttackDraftTarget")
                ?: values.string("clocktowerPendingNightDeath"),
            confirmedPoisonTarget = values.string("clocktowerConfirmedPoisonTarget")
                ?: values.string("clocktowerPoisonTarget"),
            poisonDraftTarget = values.string("clocktowerPoisonTarget"),
            confirmedMonkTarget = values.string("clocktowerConfirmedMonkProtectedTarget")
                ?: values.string("clocktowerMonkProtectedTarget"),
            monkDraftTarget = values.string("clocktowerMonkProtectedTarget"),
            confirmedMayorRedirectTarget = values.string("clocktowerConfirmedMayorRedirectTarget")
                ?: values.string("clocktowerMayorRedirectTarget"),
            mayorRedirectDraftTarget = values.string("clocktowerMayorRedirectTarget"),
            pendingNewDemonName = values.string("clocktowerPendingNewDemonName"),
            demonSuccessorDraftTarget = values.string("clocktowerDemonSuccessorTarget"),
        )

        private fun Map<String, Any?>.string(key: String): String? = this[key] as? String
        private fun Map<String, Any?>.int(key: String): Int? = (this[key] as? Number)?.toInt()
        private fun Map<String, Any?>.long(key: String): Long? = (this[key] as? Number)?.toLong()
        private fun Map<String, Any?>.boolean(key: String): Boolean? = this[key] as? Boolean
    }
}
