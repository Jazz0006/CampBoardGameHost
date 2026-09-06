package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.AbilityObservation
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.domain.clocktowerRoleDefinitionsForScript
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationCandidate
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationFamily
import com.codex.campboardgamehost.clocktower.session.FirstNightInformationRequest
import com.codex.campboardgamehost.clocktower.session.usesAuthoritativePairDomain

/** Host-to-migration adapter. Preserves legacy parity templates; pair legality stays upstream. */
internal fun clocktowerFirstNightInformationRequest(
    displayStep: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
    round: Int,
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    gameSeed: Long,
    poisonTarget: String?,
    language: String,
    automaticStorytellerStyle: RecommendationStyle,
): FirstNightInformationRequest? {
    if (phase != ClocktowerPhase.FirstNight) return null
    val actor = displayStep.actor ?: return null
    val family = FirstNightInformationFamily.entries.firstOrNull { it.role.value == displayStep.roleEnName } ?: return null
    val sourceSeat = cards.indexOf(actor).takeIf { it >= 0 }?.plus(1) ?: return null
    val reliability = when (displayStep.informationReliability) {
        InformationReliability.RELIABLE -> ReliabilityState.RELIABLE
        InformationReliability.DRUNK -> ReliabilityState.DRUNK
        InformationReliability.POISONED -> ReliabilityState.POISONED
    }
    fun legacyCandidate(option: ClocktowerDisplayOption): FirstNightInformationCandidate {
        val primary = option.displayPrimary
        return FirstNightInformationCandidate(clocktowerInformationCandidateId(option), AbilityObservation(
            sourceSeat = sourceSeat,
            perceivedRole = family.role,
            shownRole = primary?.takeIf { option.displayKind == ClocktowerDisplayKind.EitherOne }
                ?.let { shown -> clocktowerRolesForScript(script).firstOrNull { it.nameFor(language) == shown }?.enName ?: shown }
                ?.let(::RoleId),
            candidateSeats = DecisionHistoryRepository.extractSeatNumbers(
                listOf(option.displaySecondary, option.displayFooter), cards.size,
            ).toList(),
            shownNumber = primary?.toIntOrNull(),
            shownAnswer = primary?.let { answer ->
                when (answer) {
                    "有", "Yes" -> YesNoAnswer.YES
                    "没有", "No" -> YesNoAnswer.NO
                    else -> null
                }
            },
            reliability = reliability,
            semanticTruth = if (option.isTruthful) SemanticTruth.TRUE else SemanticTruth.FALSE,
        ),
            qualityTier = if (option.isDefaultRecommendation) {
                QualityTier.RECOMMENDED
            } else {
                QualityTier.ACCEPTABLE_WITH_WARNING
            },
            rankFixedPoint = when {
                option.isDefaultRecommendation -> 1_000_000L
                option.recommendationStyle == automaticStorytellerStyle -> 900_000L
                else -> 800_000L
            },
            reasonCodes = option.reasonCodes,
            warningCodes = option.warningCodes,
        )
    }
    val selectedOption = ClocktowerDisplayOption(
        label = "selected",
        displayKind = displayStep.displayKind,
        displayTitle = displayStep.displayTitle,
        displayPrimary = displayStep.displayPrimary ?: displayStep.tellPlayer,
        displaySecondary = displayStep.displaySecondary,
        displayFooter = displayStep.displayFooter,
        proposition = displayStep.displayProposition,
        isTruthful = displayStep.selectedInformationTruthful != false,
        recommendationStyle = automaticStorytellerStyle,
    )
    // Keep the old presentation set solely for parity telemetry and non-pair fallback.
    val legacyOptions = (displayStep.legacyInformationCandidates + selectedOption)
        .distinctBy(::clocktowerInformationCandidateId)
    val legacyCandidates = legacyOptions.map(::legacyCandidate)
    val migratedCandidates = if (family.usesAuthoritativePairDomain()) {
        val game = cards.toClocktowerGameState(script, gameSeed, poisonTarget)
        val roleDefinitions = clocktowerRoleDefinitionsForScript(script)
        (displayStep.manualInformationCandidates + selectedOption)
            .distinctBy(::clocktowerInformationCandidateId)
            .map { option ->
                FirstNightInformationCandidate(
                    id = clocktowerInformationCandidateId(option),
                    observation = ClocktowerPairManualAuthority.selectedObservation(
                        game = game,
                        roleDefinitions = roleDefinitions,
                        sourceSeat = sourceSeat,
                        abilityRole = family.role,
                        reliability = reliability,
                        selectedOption = option,
                    ),
                    qualityTier = if (option.isDefaultRecommendation) {
                        QualityTier.RECOMMENDED
                    } else {
                        QualityTier.ACCEPTABLE_WITH_WARNING
                    },
                    rankFixedPoint = when {
                        option.isDefaultRecommendation -> 1_000_000L
                        option.recommendationStyle == automaticStorytellerStyle -> 900_000L
                        else -> 800_000L
                    },
                    reasonCodes = option.reasonCodes,
                    warningCodes = option.warningCodes,
                )
            }
    } else {
        legacyOptions.map(::legacyCandidate)
    }
    return FirstNightInformationRequest(
        decisionId = "first-night:${phase.name}:$round:${family.name}:$sourceSeat",
        family = family,
        sourceSeat = sourceSeat,
        reliability = reliability,
        selectedCandidateId = clocktowerInformationCandidateId(selectedOption),
        legacyCandidates = legacyCandidates,
        migratedCandidates = migratedCandidates,
    )
}

