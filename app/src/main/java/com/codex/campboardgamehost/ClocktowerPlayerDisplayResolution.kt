package com.codex.campboardgamehost

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
): ClocktowerNightStepUi = copy(
    tellPlayer = option.displayPrimary,
    displayKind = option.displayKind,
    displayTitle = option.displayTitle,
    displayPrimary = option.displayPrimary,
    displaySecondary = option.displaySecondary,
    displayFooter = option.displayFooter,
    displayProposition = option.proposition,
    selectedInformationTruthful = option.isTruthful,
    displayOptions = emptyList(),
)

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
