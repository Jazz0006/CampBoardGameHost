package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

/**
 * The Storyteller selects the actual Yes/No information after the player's two-seat
 * query. This does not compute which answer would be true or legal in the game.
 */
internal fun clocktowerDirectBooleanResult(
    step: ClocktowerNightStepUi,
    actorSeat: Int,
    selectedSeats: List<Int>,
    value: Boolean,
    language: String,
): ClocktowerNightStepUi? {
    if (step.action != ClocktowerNightAction.FortuneTeller ||
        actorSeat <= 0 || selectedSeats.size != 2 ||
        selectedSeats.any { it <= 0 } || selectedSeats.distinct().size != 2
    ) return null
    val shown = if (language == "en") {
        if (value) "Yes" else "No"
    } else {
        if (value) "有" else "没有"
    }
    val option = ClocktowerDisplayOption(
        label = shown,
        displayKind = ClocktowerDisplayKind.YesNo,
        displayTitle = step.displayTitle,
        displayPrimary = shown,
        displaySecondary = null,
        displayFooter = null,
        proposition = InformationProposition.BooleanResult(
            BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            actorSeat, selectedSeats, value,
        ),
        presentationSubjectSeats = selectedSeats,
    )
    return resolveClocktowerPlayerDisplay(step, option).copy(
        selectedInformationTruthful = null,
    )
}
