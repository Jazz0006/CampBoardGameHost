package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool

internal data class ClocktowerDisplayOption(
    val label: String,
    val displayKind: ClocktowerDisplayKind,
    val displayTitle: String,
    val displayPrimary: String?,
    val displaySecondary: String?,
    val displayFooter: String?,
    /** Exact player-visible statement; never reconstruct it from localized display strings. */
    val proposition: InformationProposition? = null,
    val spyRegistersGood: Boolean? = null,
    val spyRegisteredRoleEnName: String? = null,
    val recluseRegistersEvil: Boolean? = null,
    val recluseRegisteredRoleEnName: String? = null,
    val recommendationStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    val isTruthful: Boolean = true,
    val misinformationPressure: Int = 0,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

/** Canonical semantic ID shared by legacy, unified-pool and first-night shadow paths. */
internal fun clocktowerInformationCandidateId(option: ClocktowerDisplayOption): String = listOf(
    option.displayKind.name,
    option.proposition?.toString().orEmpty(),
    option.displayPrimary.orEmpty(),
    option.displaySecondary.orEmpty(),
    option.displayFooter.orEmpty(),
    option.spyRegistersGood?.toString().orEmpty(),
    option.spyRegisteredRoleEnName.orEmpty(),
    option.recluseRegistersEvil?.toString().orEmpty(),
    option.recluseRegisteredRoleEnName.orEmpty(),
    option.isTruthful.toString(),
).joinToString("|")

internal data class ClocktowerDecisionOption(
    val label: String,
    val targetName: String,
    val explanation: String,
    val recommendationStyle: RecommendationStyle = RecommendationStyle.BALANCED,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

internal fun unifiedDecisionPool(
    options: List<ClocktowerDecisionOption>,
    familyId: String,
): UnifiedSelectionPool<ClocktowerDecisionOption>? = options
    .takeIf { it.isNotEmpty() }
    ?.let { candidates ->
        UnifiedSelectionPool(candidates.map { option ->
            UnifiedSelectionCandidate(
                candidateId = listOf(option.recommendationStyle.name, option.targetName).joinToString("|"),
                familyId = familyId,
                legality = UnifiedCandidateLegality.LEGAL,
                epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
                qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
                rankFixedPoint = if (option.isDefaultRecommendation) 1_000_000L else 800_000L,
                reasonCodes = option.reasonCodes,
                warningCodes = option.warningCodes,
                payload = option,
            )
        })
    }

internal fun unifiedFirstNightInformationPool(
    options: List<ClocktowerDisplayOption>,
    familyId: String,
    automaticStyle: RecommendationStyle,
): UnifiedSelectionPool<ClocktowerDisplayOption> = UnifiedSelectionPool(options.map { option ->
    UnifiedSelectionCandidate(
        candidateId = clocktowerInformationCandidateId(option),
        familyId = familyId,
        legality = UnifiedCandidateLegality.LEGAL,
        epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
        qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
        rankFixedPoint = when {
            option.isDefaultRecommendation -> 1_000_000L
            option.recommendationStyle == automaticStyle -> 900_000L
            else -> 800_000L
        },
        reasonCodes = option.reasonCodes,
        warningCodes = option.warningCodes,
        payload = option,
    )
})

internal data class ClocktowerRegistrationRecommendationOption(
    val label: String,
    val usesSpecialRegistration: Boolean,
    val registeredRoleEnName: String?,
    val style: RecommendationStyle,
    val isDefaultRecommendation: Boolean = false,
    val reasonCodes: List<String> = emptyList(),
    val warningCodes: List<String> = emptyList(),
)

/** Shared registration pool: style changes selection, never candidate legality or ordering data. */
internal fun unifiedRegistrationPool(
    options: List<ClocktowerRegistrationRecommendationOption>,
): UnifiedSelectionPool<ClocktowerRegistrationRecommendationOption>? = options
    .takeIf { it.isNotEmpty() }
    ?.let { candidates ->
        UnifiedSelectionPool(candidates.map { option ->
            UnifiedSelectionCandidate(
                candidateId = listOf(
                    option.style.name,
                    option.usesSpecialRegistration,
                    option.registeredRoleEnName.orEmpty(),
                ).joinToString("|"),
                familyId = if (option.usesSpecialRegistration) "special-registration" else "actual-registration",
                legality = UnifiedCandidateLegality.LEGAL,
                epistemicStatus = UnifiedEpistemicStatus.VERIFIED,
                qualityTier = if (option.isDefaultRecommendation) QualityTier.RECOMMENDED else QualityTier.ACCEPTABLE_WITH_WARNING,
                rankFixedPoint = if (option.isDefaultRecommendation) 1_000_000L else 800_000L,
                reasonCodes = option.reasonCodes,
                warningCodes = option.warningCodes,
                payload = option,
            )
        })
    }
