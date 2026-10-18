package com.codex.campboardgamehost

internal fun clocktowerManualInformationDomain(
    options: List<ClocktowerDisplayOption>,
): List<ClocktowerDisplayOption> = options
    .distinctBy(::clocktowerInformationCandidateId)
    .sortedBy(::clocktowerInformationCandidateId)
    .map { option ->
        val neutralLabel = listOfNotNull(
            option.displayPrimary?.takeIf { it.isNotBlank() },
            option.displaySecondary?.takeIf { it.isNotBlank() },
        ).joinToString(" · ").ifBlank { option.label }
        option.copy(
            label = neutralLabel,
            misinformationPressure = 0,
            isDefaultRecommendation = false,
            reasonCodes = emptyList(),
            warningCodes = emptyList(),
        )
    }
