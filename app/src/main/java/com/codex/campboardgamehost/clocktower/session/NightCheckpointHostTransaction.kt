package com.codex.campboardgamehost.clocktower.session

/**
 * App/Host-facing revision ownership emitted by a checkpoint-local transaction.
 *
 * The adapter never performs durable side effects itself. App/session code remains responsible for
 * actually advancing revisions, recording timeline/history facts, and materializing public state.
 */
internal enum class NightCheckpointRevisionIntent {
    NONE,
    PLAYER_INPUT,
    GAME_STATE,
}

internal data class NightCheckpointHostTransactionResult(
    val checkpoint: ClocktowerNightCheckpoint,
    val revisionIntent: NightCheckpointRevisionIntent,
)

/**
 * Small JVM-callable boundary between Host/App callbacks and [NightCheckpointReducer].
 *
 * This is an adapter, not a second state owner: [ClocktowerNightCheckpoint] remains the sole durable
 * unfinished-night checkpoint and [NightCheckpointReducer] remains the transition authority.
 */
internal object NightCheckpointHostTransaction {
    fun editDemonSuccessor(
        checkpoint: ClocktowerNightCheckpoint,
        selectedTarget: String?,
    ): NightCheckpointHostTransactionResult = NightCheckpointHostTransactionResult(
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.EditDemonSuccessorDraft(selectedTarget),
        ),
        revisionIntent = NightCheckpointRevisionIntent.PLAYER_INPUT,
    )

    fun confirmPoison(
        checkpoint: ClocktowerNightCheckpoint,
    ): NightCheckpointHostTransactionResult {
        val reducedCheckpoint = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmPoison,
        )
        return NightCheckpointHostTransactionResult(
            checkpoint = reducedCheckpoint,
            revisionIntent = if (
                reducedCheckpoint.confirmedPoisonTarget != checkpoint.confirmedPoisonTarget
            ) {
                NightCheckpointRevisionIntent.GAME_STATE
            } else {
                NightCheckpointRevisionIntent.NONE
            },
        )
    }

    fun confirmMonkProtection(
        checkpoint: ClocktowerNightCheckpoint,
    ): NightCheckpointHostTransactionResult {
        val reducedCheckpoint = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmMonkProtection,
        )
        return NightCheckpointHostTransactionResult(
            checkpoint = reducedCheckpoint,
            revisionIntent = if (
                reducedCheckpoint.confirmedMonkTarget != checkpoint.confirmedMonkTarget
            ) {
                NightCheckpointRevisionIntent.GAME_STATE
            } else {
                NightCheckpointRevisionIntent.NONE
            },
        )
    }

    fun confirmDemonAttack(
        checkpoint: ClocktowerNightCheckpoint,
    ): NightCheckpointHostTransactionResult {
        val reducedCheckpoint = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmDemonAttack,
        )
        return NightCheckpointHostTransactionResult(
            checkpoint = reducedCheckpoint,
            revisionIntent = if (
                reducedCheckpoint.confirmedAttackTarget != checkpoint.confirmedAttackTarget
            ) {
                NightCheckpointRevisionIntent.GAME_STATE
            } else {
                NightCheckpointRevisionIntent.NONE
            },
        )
    }

    fun confirmDemonSuccessor(
        checkpoint: ClocktowerNightCheckpoint,
    ): NightCheckpointHostTransactionResult {
        val reducedCheckpoint = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.ConfirmDemonSuccessor,
        )
        return NightCheckpointHostTransactionResult(
            checkpoint = reducedCheckpoint,
            revisionIntent = if (
                reducedCheckpoint.confirmedDemonSuccessorTarget != checkpoint.confirmedDemonSuccessorTarget
            ) {
                NightCheckpointRevisionIntent.GAME_STATE
            } else {
                NightCheckpointRevisionIntent.NONE
            },
        )
    }

    fun movePrevious(
        checkpoint: ClocktowerNightCheckpoint,
    ): NightCheckpointHostTransactionResult = NightCheckpointHostTransactionResult(
        checkpoint = NightCheckpointReducer.reduce(
            checkpoint = checkpoint,
            event = NightResolutionEvent.MovePrevious,
        ),
        revisionIntent = NightCheckpointRevisionIntent.NONE,
    )
}
