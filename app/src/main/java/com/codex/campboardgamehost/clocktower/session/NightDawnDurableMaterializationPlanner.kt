package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * Current durable projection relevant to one Dawn commit retry.
 *
 * Mechanical state and durable history are intentionally tracked independently: a retry may find
 * that a state mutation already happened while its stable action/observation record is still
 * missing, or vice versa.
 */
internal data class DawnDurableMaterializationState(
    val aliveSeats: Set<Int>,
    val roleIdsBySeat: Map<Int, RoleId>,
    val currentPhase: StorytellerPhase,
    val committedActionIds: Set<String>,
    val committedObservationRecordIds: Set<String>,
    val currentPoisonTargetSeat: Int? = null,
) {
    init {
        require(aliveSeats.all { it > 0 }) { "Alive seats must be positive." }
        require(roleIdsBySeat.keys.all { it > 0 }) { "Role seats must be positive." }
        require(currentPoisonTargetSeat == null || currentPoisonTargetSeat > 0) {
            "Current poison target seat must be positive."
        }
        require(committedActionIds.none { it.isBlank() }) { "Committed action IDs cannot be blank." }
        require(committedObservationRecordIds.none { it.isBlank() }) {
            "Committed observation record IDs cannot be blank."
        }
    }
}

internal data class DawnDeathMaterializationPlan(
    val intent: DawnDeathIntent,
    val stateMutationRequired: Boolean,
    val actionIdToCommit: String?,
    val publicAliveObservationIdToCommit: String?,
)

internal data class DawnRoleChangeMaterializationPlan(
    val intent: DawnRoleChangeIntent,
    val stateMutationRequired: Boolean,
    val actionIdToCommit: String?,
)

internal data class DawnPoisonMaterializationPlan(
    val intent: DawnPoisonCarryIntent,
    val stateMutationRequired: Boolean,
    val actionIdToCommit: String?,
)

internal data class DawnPhaseAdvanceMaterializationPlan(
    val targetPhase: StorytellerPhase,
    val stateMutationRequired: Boolean,
    val actionIdToCommit: String?,
)

internal data class DawnDurableMaterializationPlan(
    val death: DawnDeathMaterializationPlan?,
    val roleChanges: List<DawnRoleChangeMaterializationPlan>,
    val poison: DawnPoisonMaterializationPlan?,
    val phaseAdvance: DawnPhaseAdvanceMaterializationPlan?,
)

/**
 * Pure exactly-once planner for materializing an already-resolved [DawnCommitIntent].
 *
 * IDs depend only on durable game/round/effect identity. They deliberately do not include mutable
 * game-state revisions, event counters, callback counts, or timeline cursor values, so replaying
 * the same Dawn intent can repair partial persistence without creating duplicate history.
 */
internal object NightDawnDurableMaterializationPlanner {
    fun plan(
        gameId: String,
        round: Int,
        intent: DawnCommitIntent,
        state: DawnDurableMaterializationState,
        advanceToDawn: Boolean,
    ): DawnDurableMaterializationPlan {
        require(gameId.isNotBlank()) { "Dawn materialization game ID cannot be blank." }
        require(round > 0) { "Dawn materialization round must be positive." }

        val death = intent.death?.let { deathIntent ->
            val actionId = stableId(
                gameId = gameId,
                round = round,
                effect = "death-${deathIntent.targetSeat}",
            )
            val observationId = stableObservationId(
                gameId = gameId,
                round = round,
                targetSeat = deathIntent.targetSeat,
            )
            DawnDeathMaterializationPlan(
                intent = deathIntent,
                stateMutationRequired = deathIntent.targetSeat in state.aliveSeats,
                actionIdToCommit = actionId.takeUnless(state.committedActionIds::contains),
                publicAliveObservationIdToCommit = observationId
                    .takeUnless(state.committedObservationRecordIds::contains),
            )
        }

        val roleChanges = intent.roleChanges.map { roleChange ->
            val actionId = stableId(
                gameId = gameId,
                round = round,
                effect = "role-${roleChange.targetSeat}-${stableToken(roleChange.roleId.value)}",
            )
            DawnRoleChangeMaterializationPlan(
                intent = roleChange,
                stateMutationRequired = state.roleIdsBySeat[roleChange.targetSeat] != roleChange.roleId,
                actionIdToCommit = actionId.takeUnless(state.committedActionIds::contains),
            )
        }

        val poison = intent.poisonCarry?.let { poisonIntent ->
            val transitionActionId = if (poisonIntent.previousTargetSeat != poisonIntent.targetSeat) {
                stableId(
                    gameId = gameId,
                    round = round,
                    effect = "poison-${poisonSeatToken(poisonIntent.previousTargetSeat)}-to-${poisonSeatToken(poisonIntent.targetSeat)}",
                )
            } else {
                null
            }
            DawnPoisonMaterializationPlan(
                intent = poisonIntent,
                stateMutationRequired = state.currentPoisonTargetSeat != poisonIntent.targetSeat,
                actionIdToCommit = transitionActionId
                    ?.takeUnless(state.committedActionIds::contains),
            )
        }

        val phaseAdvance = if (advanceToDawn) {
            val actionId = stableId(
                gameId = gameId,
                round = round,
                effect = "phase-dawn",
            )
            DawnPhaseAdvanceMaterializationPlan(
                targetPhase = StorytellerPhase.DAWN,
                stateMutationRequired = state.currentPhase != StorytellerPhase.DAWN,
                actionIdToCommit = actionId.takeUnless(state.committedActionIds::contains),
            )
        } else {
            null
        }

        return DawnDurableMaterializationPlan(
            death = death,
            roleChanges = roleChanges,
            poison = poison,
            phaseAdvance = phaseAdvance,
        )
    }

    private fun stableId(
        gameId: String,
        round: Int,
        effect: String,
    ): String = "dawn-${stableToken(gameId)}-$round-$effect"

    private fun stableObservationId(
        gameId: String,
        round: Int,
        targetSeat: Int,
    ): String = "public-alive-dawn-${stableToken(gameId)}-$round-$targetSeat"

    private fun poisonSeatToken(seat: Int?): String = seat?.let { "seat-$it" } ?: "none"

    private fun stableToken(value: String): String = buildString(value.length) {
        value.lowercase().forEach { character ->
            when {
                character.isLetterOrDigit() -> append(character)
                lastOrNull() != '-' -> append('-')
            }
        }
    }.trim('-').ifEmpty { "value" }
}
