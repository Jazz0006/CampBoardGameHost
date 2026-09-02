package com.codex.campboardgamehost

/**
 * Result-first projection for information affected by Spy/Recluse registration.
 *
 * Different legal registration witnesses may produce the same player-visible proposition. The
 * Storyteller chooses that final proposition once; the first candidate's witness remains attached
 * so the existing registration callback can commit a deterministic legal ruling before display.
 */
internal fun distinctClocktowerFinalInformationResults(
    candidates: List<ClocktowerDisplayOption>,
): List<ClocktowerDisplayOption> = candidates.distinctBy(::clocktowerFinalInformationResultId)

internal fun clocktowerFinalInformationResultId(option: ClocktowerDisplayOption): String = listOf(
    option.displayKind.name,
    option.proposition?.toString().orEmpty(),
    option.displayPrimary.orEmpty(),
    option.displaySecondary.orEmpty(),
    option.displayFooter.orEmpty(),
).joinToString("|")

internal fun ClocktowerNightStepUi.usesResultFirstRegistrationDomain(): Boolean =
    manualInformationCandidates.isNotEmpty() &&
        (spyRegistrationKey != null || recluseRegistrationKey != null)
