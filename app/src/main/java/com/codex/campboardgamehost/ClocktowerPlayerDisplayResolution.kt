package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot

/**
 * Projects already-selected information without selecting, authorizing or publishing it.
 * Manual and recommended ordinary choices have identical output for the same resolved option.
 * The returned step still contains Storyteller context; this is not a privacy-sanitized payload.
 */
internal fun resolveClocktowerPlayerDisplay(
    step: ClocktowerNightStepUi,
    option: ClocktowerDisplayOption,
): ClocktowerNightStepUi = step.withDisplayOption(option).copy(recommendedDisplayOptions = emptyList())

/** Compatibility path: the legacy unreliable picker clears only its manual display options. */
internal fun resolveClocktowerLegacyUnreliablePlayerDisplay(
    step: ClocktowerNightStepUi,
    option: ClocktowerDisplayOption,
): ClocktowerNightStepUi = step.withDisplayOption(option)

/**
 * Presentation-only normalization for player display. New-Demon identity is a private role reveal
 * even though the host-flow step intentionally carries displayKind=None.
 */
internal fun clocktowerPlayerDisplayStep(step: ClocktowerNightStepUi): ClocktowerNightStepUi =
    if (
        step.action == ClocktowerNightAction.NewDemonIdentity &&
        step.displayKind == ClocktowerDisplayKind.None &&
        !step.tellPlayer.isNullOrBlank()
    ) {
        step.copy(
            displayKind = ClocktowerDisplayKind.RoleReveal,
            displayPrimary = step.displayPrimary ?: step.tellPlayer,
        )
    } else {
        step
    }

/** Numeric presentation falls back per field; the confirmed draft remains semantic authority. */
internal fun resolveClocktowerNumericPlayerDisplay(
    step: ClocktowerNightStepUi,
    template: ClocktowerDisplayOption?,
    value: Int,
    truthful: Boolean,
    confirmed: ConfirmedInformationDecision,
    expectedSnapshot: InformationDecisionSnapshot,
): ClocktowerNightStepUi = step.copy(
    tellPlayer = value.toString(),
    displayKind = ClocktowerDisplayKind.Number,
    displayTitle = template?.displayTitle ?: step.displayTitle,
    displayPrimary = value.toString(),
    displaySecondary = template?.displaySecondary ?: step.displaySecondary,
    displayFooter = template?.displayFooter ?: step.displayFooter ?: step.explanation,
    presentationSubjectSeats = template?.presentationSubjectSeats ?: step.presentationSubjectSeats,
    selectedInformationTruthful = truthful,
).withConfirmedInformation(confirmed, expectedSnapshot)

/** Without a matching Boolean option, preserve existing text/kind/truth rather than inventing it. */
internal fun resolveClocktowerBooleanPlayerDisplay(
    step: ClocktowerNightStepUi,
    option: ClocktowerDisplayOption?,
    confirmed: ConfirmedInformationDecision,
    expectedSnapshot: InformationDecisionSnapshot,
): ClocktowerNightStepUi = (option?.let { step.withDisplayOption(it) } ?: step)
    .withConfirmedInformation(confirmed, expectedSnapshot)

private fun ClocktowerNightStepUi.withDisplayOption(
    option: ClocktowerDisplayOption,
): ClocktowerNightStepUi {
    val anchorSeat = (displayProposition as? InformationProposition.RoleAt)?.seat
    val optionSeat = option.presentationSubjectSeats.singleOrNull()
    val shownRole = option.presentationRoleId
    // A role's exact ID and subject seat come from selected typed presentation metadata,
    // NEVER its localized display label or the truthful role in the original step.
    val shownProposition = option.proposition ?: if (
        option.displayKind == ClocktowerDisplayKind.RoleReveal && shownRole != null &&
        (optionSeat ?: anchorSeat) != null && (anchorSeat == null || optionSeat == null || optionSeat == anchorSeat)
    ) {
        InformationProposition.RoleAt(requireNotNull(optionSeat ?: anchorSeat), RoleId(shownRole))
    } else null
    return copy(
        tellPlayer = option.displayPrimary,
        displayKind = option.displayKind,
        displayTitle = option.displayTitle,
        displayPrimary = option.displayPrimary,
        displaySecondary = option.displaySecondary,
        displayFooter = option.displayFooter,
        displayProposition = shownProposition,
        presentationSubjectSeats = option.presentationSubjectSeats,
        selectedInformationTruthful = option.isTruthful,
        displayOptions = emptyList(),
    )
}

private fun ClocktowerNightStepUi.withConfirmedInformation(
    confirmed: ConfirmedInformationDecision,
    expectedSnapshot: InformationDecisionSnapshot,
): ClocktowerNightStepUi = copy(
    displayProposition = confirmed.draft.proposition,
    informationDecisionConfirmation = confirmed,
    informationDecisionExpectedSnapshot = expectedSnapshot,
    displayOptions = emptyList(),
    recommendedDisplayOptions = emptyList(),
)
