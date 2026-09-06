package com.codex.campboardgamehost

/** Supplied target eligibility, not a second rules or mutable selection owner. */
internal data class ClocktowerSingleTargetSelection(
    val selectedSeat: Int?,
    val selectableSeats: Set<Int>,
    val enabled: Boolean,
)

internal sealed interface ClocktowerSingleTargetEvent {
    data class SelectSeat(val seat: Int) : ClocktowerSingleTargetEvent
    data object ShowResult : ClocktowerSingleTargetEvent
    data object Previous : ClocktowerSingleTargetEvent
    data object Next : ClocktowerSingleTargetEvent
}

internal data class ClocktowerSingleTargetAbilityPresentation(
    val action: ClocktowerNightAction,
    val selection: ClocktowerSingleTargetSelection,
    val actorSeat: Int?,
    val wakeInstruction: String?,
    val canShowResult: Boolean,
)

internal fun clocktowerSingleTargetAbilityPresentation(
    action: ClocktowerNightAction,
    selection: ClocktowerSingleTargetSelection,
    actorSeat: Int?,
    wakeInstruction: String?,
    canShowResult: Boolean,
): ClocktowerSingleTargetAbilityPresentation? = when (action) {
    ClocktowerNightAction.RedHerring -> if (selection.enabled) {
        ClocktowerSingleTargetAbilityPresentation(action, selection, null, null, false)
    } else null
    ClocktowerNightAction.Poison, ClocktowerNightAction.ButlerMaster,
    ClocktowerNightAction.MonkProtect, ClocktowerNightAction.DemonKill,
    ClocktowerNightAction.Ravenkeeper -> ClocktowerSingleTargetAbilityPresentation(
        action, selection, actorSeat, wakeInstruction,
        canShowResult && action == ClocktowerNightAction.Ravenkeeper,
    )
    else -> null
}

/** Storyteller-only ruling presentation; no player actor/wake cue belongs to this family. */
internal data class ClocktowerNightRulingPresentation(
    val action: ClocktowerNightAction,
    val selection: ClocktowerSingleTargetSelection,
    val mayorSeat: Int?,
    val explanation: String,
)

internal fun clocktowerNightRulingPresentation(
    action: ClocktowerNightAction,
    selection: ClocktowerSingleTargetSelection,
    automatic: Boolean,
    mayorSeat: Int?,
    explanation: String,
): ClocktowerNightRulingPresentation? {
    if (automatic) return null
    return when (action) {
        ClocktowerNightAction.MayorRedirect -> ClocktowerNightRulingPresentation(
            action,
            selection.copy(
                selectableSeats = if (mayorSeat != null) selection.selectableSeats else emptySet(),
                enabled = selection.enabled && mayorSeat != null,
            ),
            mayorSeat, explanation,
        )
        ClocktowerNightAction.DemonSuccessor -> ClocktowerNightRulingPresentation(action, selection, null, explanation)
        else -> null
    }
}
