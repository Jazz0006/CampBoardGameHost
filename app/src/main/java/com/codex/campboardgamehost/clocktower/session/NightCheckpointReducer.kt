package com.codex.campboardgamehost.clocktower.session

/**
 * Transient UI/application commands for the unfinished-night checkpoint.
 *
 * These commands are deliberately not persisted and are not a replay/event-sourcing authority.
 */
internal sealed interface NightResolutionEvent {
    data object MovePrevious : NightResolutionEvent

    data class EditPoisonDraft(
        val target: String?,
    ) : NightResolutionEvent

    data object ConfirmPoison : NightResolutionEvent

    data class EditMonkProtectionDraft(
        val target: String?,
    ) : NightResolutionEvent

    data object ConfirmMonkProtection : NightResolutionEvent

    data class EditDemonAttackDraft(
        val target: String?,
    ) : NightResolutionEvent

    data object ConfirmDemonAttack : NightResolutionEvent

    data class EditMayorRedirectDraft(
        val target: String?,
    ) : NightResolutionEvent

    data object ConfirmMayorRedirect : NightResolutionEvent

    data class EditDemonSuccessorDraft(
        val target: String?,
    ) : NightResolutionEvent

    data object ConfirmDemonSuccessor : NightResolutionEvent
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

        is NightResolutionEvent.EditPoisonDraft -> checkpoint.copy(
            poisonDraftTarget = event.target,
        )

        NightResolutionEvent.ConfirmPoison -> checkpoint.copy(
            confirmedPoisonTarget = checkpoint.poisonDraftTarget,
            confirmedMayorRedirectTarget = preserveDependentConfirmationUnlessConfirmedValueChanged(
                previousConfirmedValue = checkpoint.confirmedPoisonTarget,
                nextConfirmedValue = checkpoint.poisonDraftTarget,
                confirmedDependentTarget = checkpoint.confirmedMayorRedirectTarget,
            ),
            confirmedDemonSuccessorTarget = preserveDependentConfirmationUnlessConfirmedValueChanged(
                previousConfirmedValue = checkpoint.confirmedPoisonTarget,
                nextConfirmedValue = checkpoint.poisonDraftTarget,
                confirmedDependentTarget = checkpoint.confirmedDemonSuccessorTarget,
            ),
        )

        is NightResolutionEvent.EditMonkProtectionDraft -> checkpoint.copy(
            monkDraftTarget = event.target,
        )

        NightResolutionEvent.ConfirmMonkProtection -> checkpoint.copy(
            confirmedMonkTarget = checkpoint.monkDraftTarget,
            confirmedMayorRedirectTarget = preserveDependentConfirmationUnlessConfirmedValueChanged(
                previousConfirmedValue = checkpoint.confirmedMonkTarget,
                nextConfirmedValue = checkpoint.monkDraftTarget,
                confirmedDependentTarget = checkpoint.confirmedMayorRedirectTarget,
            ),
            confirmedDemonSuccessorTarget = preserveDependentConfirmationUnlessConfirmedValueChanged(
                previousConfirmedValue = checkpoint.confirmedMonkTarget,
                nextConfirmedValue = checkpoint.monkDraftTarget,
                confirmedDependentTarget = checkpoint.confirmedDemonSuccessorTarget,
            ),
        )

        is NightResolutionEvent.EditDemonAttackDraft -> checkpoint.copy(
            attackDraftTarget = event.target,
        )

        NightResolutionEvent.ConfirmDemonAttack -> checkpoint.copy(
            confirmedAttackTarget = checkpoint.attackDraftTarget,
            confirmedMayorRedirectTarget = preserveDependentConfirmationUnlessConfirmedValueChanged(
                previousConfirmedValue = checkpoint.confirmedAttackTarget,
                nextConfirmedValue = checkpoint.attackDraftTarget,
                confirmedDependentTarget = checkpoint.confirmedMayorRedirectTarget,
            ),
            confirmedDemonSuccessorTarget = preserveDependentConfirmationUnlessConfirmedValueChanged(
                previousConfirmedValue = checkpoint.confirmedAttackTarget,
                nextConfirmedValue = checkpoint.attackDraftTarget,
                confirmedDependentTarget = checkpoint.confirmedDemonSuccessorTarget,
            ),
        )

        is NightResolutionEvent.EditMayorRedirectDraft -> checkpoint.copy(
            mayorRedirectDraftTarget = event.target,
        )

        NightResolutionEvent.ConfirmMayorRedirect -> checkpoint.copy(
            confirmedMayorRedirectTarget = checkpoint.mayorRedirectDraftTarget,
        )

        is NightResolutionEvent.EditDemonSuccessorDraft -> checkpoint.copy(
            demonSuccessorDraftTarget = event.target,
        )

        NightResolutionEvent.ConfirmDemonSuccessor -> checkpoint.copy(
            confirmedDemonSuccessorTarget = checkpoint.demonSuccessorDraftTarget,
        )
    }

    private fun preserveDependentConfirmationUnlessConfirmedValueChanged(
        previousConfirmedValue: String?,
        nextConfirmedValue: String?,
        confirmedDependentTarget: String?,
    ): String? = if (previousConfirmedValue == nextConfirmedValue) {
        confirmedDependentTarget
    } else {
        null
    }
}
