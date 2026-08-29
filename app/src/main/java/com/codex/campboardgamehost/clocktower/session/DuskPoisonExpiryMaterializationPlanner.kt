package com.codex.campboardgamehost.clocktower.session

/**
 * Durable projection relevant to one end-of-day poison expiry retry.
 *
 * The caller supplies the semantic previous poison target separately from the current mechanical
 * state because either side may already have been persisted when a retry begins.
 */
internal data class DuskPoisonExpiryMaterializationState(
    val currentPoisonTargetSeat: Int?,
    val committedActionIds: Set<String>,
) {
    init {
        require(currentPoisonTargetSeat == null || currentPoisonTargetSeat > 0) {
            "Current poison target seat must be positive."
        }
        require(committedActionIds.none { it.isBlank() }) {
            "Committed action IDs cannot be blank."
        }
    }
}

internal data class DuskPoisonExpiryMaterializationPlan(
    val previousTargetSeat: Int,
    val stateMutationRequired: Boolean,
    val actionIdToCommit: String?,
)

/**
 * Pure exactly-once planner for poison expiry at the outgoing Day/Dusk boundary.
 *
 * The stable action identity depends only on game, outgoing round, and semantic poison transition.
 * It deliberately excludes mutable event/revision counters and timeline cursors. The caller must
 * materialize this plan before making the following Night phase durable so a partial retry still
 * has the outgoing Day transition as its owner.
 */
internal object DuskPoisonExpiryMaterializationPlanner {
    fun plan(
        gameId: String,
        round: Int,
        previousTargetSeat: Int?,
        state: DuskPoisonExpiryMaterializationState,
    ): DuskPoisonExpiryMaterializationPlan? {
        require(gameId.isNotBlank()) { "Dusk poison expiry game ID cannot be blank." }
        require(round > 0) { "Dusk poison expiry round must be positive." }
        require(previousTargetSeat == null || previousTargetSeat > 0) {
            "Previous poison target seat must be positive."
        }

        val previousSeat = previousTargetSeat ?: return null
        val actionId = "dusk-${stableToken(gameId)}-$round-poison-seat-$previousSeat-to-none"
        return DuskPoisonExpiryMaterializationPlan(
            previousTargetSeat = previousSeat,
            stateMutationRequired = state.currentPoisonTargetSeat != null,
            actionIdToCommit = actionId.takeUnless(state.committedActionIds::contains),
        )
    }

    private fun stableToken(value: String): String = buildString(value.length) {
        value.lowercase().forEach { character ->
            when {
                character.isLetterOrDigit() -> append(character)
                lastOrNull() != '-' -> append('-')
            }
        }
    }.trim('-').ifEmpty { "value" }
}
