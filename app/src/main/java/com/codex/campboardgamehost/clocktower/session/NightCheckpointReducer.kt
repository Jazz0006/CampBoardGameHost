package com.codex.campboardgamehost.clocktower.session

/**
 * Transient UI/application commands for the unfinished-night checkpoint.
 *
 * These commands are deliberately not persisted and are not a replay/event-sourcing authority.
 */
internal sealed interface NightResolutionEvent {
    data object MovePrevious : NightResolutionEvent

    data class EditDemonAttackDraft(
        val target: String?,
    ) : NightResolutionEvent

    data object ConfirmDemonAttack : NightResolutionEvent
}

/**
 * Pure transition owner for checkpoint-local draft, confirmation, navigation, and invalidation.
 *
 * ClocktowerNightCheckpoint remains the sole durable unfinished-night state. This reducer returns
 * a replacement checkpoint; it does not commit timeline/history state.
 */
internal object NightCheckpointReducer {
    fun reduce(
        checkpoint: ClocktowerNightCheckpoint,
        event: NightResolutionEvent,
    ): ClocktowerNightCheckpoint = when (event) {
        NightResolutionEvent.MovePrevious -> checkpoint.copy(
            nightStepIndex = (checkpoint.nightStepIndex - 1).coerceAtLeast(0),
        )

        is NightResolutionEvent.EditDemonAttackDraft -> checkpoint.copy(
            attackDraftTarget = event.target,
        )

        NightResolutionEvent.ConfirmDemonAttack -> {
            val confirmedValueChanged = checkpoint.confirmedAttackTarget != checkpoint.attackDraftTarget
            checkpoint.copy(
                confirmedAttackTarget = checkpoint.attackDraftTarget,
                confirmedDemonSuccessorTarget = if (confirmedValueChanged) {
                    null
                } else {
                    checkpoint.confirmedDemonSuccessorTarget
                },
            )
        }
    }
}
