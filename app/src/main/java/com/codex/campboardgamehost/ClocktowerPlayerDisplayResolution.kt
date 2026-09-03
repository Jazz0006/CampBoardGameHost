package com.codex.campboardgamehost

/**
 * Finalizes one already-resolved Storyteller display choice into the payload consumed by the
 * player-facing renderer.
 *
 * The choice source is intentionally not an input: manual selection and recommendation selection
 * must produce the same Player Reveal payload whenever they resolve to the same display option.
 */
internal fun resolveClocktowerPlayerDisplay(
    step: ClocktowerNightStepUi,
    option: ClocktowerDisplayOption,
): ClocktowerNightStepUi = step.copy(
    tellPlayer = option.displayPrimary,
    displayKind = option.displayKind,
    displayTitle = option.displayTitle,
    displayPrimary = option.displayPrimary,
    displaySecondary = option.displaySecondary,
    displayFooter = option.displayFooter,
    displayProposition = option.proposition,
    selectedInformationTruthful = option.isTruthful,
    displayOptions = emptyList(),
    recommendedDisplayOptions = emptyList(),
)
