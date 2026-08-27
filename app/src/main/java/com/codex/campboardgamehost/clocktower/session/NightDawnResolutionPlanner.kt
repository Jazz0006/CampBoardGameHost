package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution

/**
 * SNE-7 typed planner seam scaffold.
 *
 * This file deliberately defines the compile-time contract before behavior is implemented.
 * The planner remains pure and does not mutate GameState or own durable timeline state.
 */
internal enum class NightResolutionContinuation {
    AWAIT_DEMON_SUCCESSOR,
    AWAIT_NEW_DEMON_IDENTITY,
    DAWN,
}

internal data class DawnRoleChangeIntent(
    val targetSeat: Int,
    val roleId: RoleId,
)

internal data class DawnCommitIntent(
    val roleChanges: List<DawnRoleChangeIntent> = emptyList(),
)

internal data class NightDawnResolutionTransition(
    val checkpoint: ClocktowerNightCheckpoint,
    val continuation: NightResolutionContinuation,
    val dawnCommitIntent: DawnCommitIntent?,
    val outcomeEvaluationAllowed: Boolean,
)

internal object NightDawnResolutionPlanner {
    fun planDemonSuccession(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        successionResolution: DemonSuccessionResolution,
        demonRoleId: RoleId,
    ): NightDawnResolutionTransition {
        @Suppress("UNUSED_VARIABLE")
        val contractInputs = listOf(baseGameState.seed, successionResolution.hashCode(), demonRoleId.value.hashCode())
        return NightDawnResolutionTransition(
            checkpoint = checkpoint,
            continuation = NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR,
            dawnCommitIntent = null,
            outcomeEvaluationAllowed = false,
        )
    }

    fun confirmNewDemonIdentity(
        baseGameState: GameState,
        checkpoint: ClocktowerNightCheckpoint,
        demonRoleId: RoleId,
    ): NightDawnResolutionTransition {
        @Suppress("UNUSED_VARIABLE")
        val contractInputs = listOf(baseGameState.seed, demonRoleId.value.hashCode())
        return NightDawnResolutionTransition(
            checkpoint = checkpoint,
            continuation = NightResolutionContinuation.AWAIT_DEMON_SUCCESSOR,
            dawnCommitIntent = null,
            outcomeEvaluationAllowed = false,
        )
    }
}
