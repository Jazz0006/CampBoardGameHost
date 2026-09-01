package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PairInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedCandidateLegality
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedEpistemicStatus
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionCandidate
import com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool
import com.codex.campboardgamehost.clocktower.rules.FirstNightNumericInformationSemantics
import com.codex.campboardgamehost.clocktower.rules.PairInformationDisplaySemantics
import kotlin.math.abs

internal enum class TwoPlayerSelectionAction {
    ToggleFirst,
    ToggleSecond,
    RejectLimit,
}

internal fun twoPlayerSelectionAction(
    first: String?,
    second: String?,
    selectedName: String,
): TwoPlayerSelectionAction = when {
    selectedName == first -> TwoPlayerSelectionAction.ToggleFirst
    selectedName == second -> TwoPlayerSelectionAction.ToggleSecond
    first == null -> TwoPlayerSelectionAction.ToggleFirst
    second == null -> TwoPlayerSelectionAction.ToggleSecond
    else -> TwoPlayerSelectionAction.RejectLimit
}

internal data class RevalidatedTwoPlayerSelection(
    val first: String?,
    val second: String?,
) {
    val isComplete: Boolean
        get() = first != null && second != null && first != second
}

internal fun revalidateTwoPlayerSelection(
    first: String?,
    second: String?,
    eligibleNames: Set<String>,
): RevalidatedTwoPlayerSelection {
    val revalidatedFirst = first?.takeIf { it in eligibleNames }
    val revalidatedSecond = second?.takeIf { it in eligibleNames && it != revalidatedFirst }
    return RevalidatedTwoPlayerSelection(
        first = revalidatedFirst,
        second = revalidatedSecond,
    )
}

internal data class ChambermaidSelectionResolution(
    val selection: RevalidatedTwoPlayerSelection,
    val wokeCount: Int?,
)

internal fun resolveChambermaidSelection(
    first: String?,
    second: String?,
    eligibleNames: Set<String>,
    wokeBecauseOwnAbilityNames: Set<String>,
): ChambermaidSelectionResolution {
    val selection = revalidateTwoPlayerSelection(
        first = first,
        second = second,
        eligibleNames = eligibleNames,
    )
    val wokeCount = if (selection.isComplete) {
        listOfNotNull(selection.first, selection.second)
            .count { it in wokeBecauseOwnAbilityNames }
    } else {
        null
    }
    return ChambermaidSelectionResolution(
        selection = selection,
        wokeCount = wokeCount,
    )
}

internal fun shouldAutoAdvanceRedHerring(
    automaticStorytellerInfo: Boolean,
    isRedHerringStep: Boolean,
    isRealAction: Boolean,
    hasSelectedRedHerring: Boolean,
): Boolean = automaticStorytellerInfo &&
    isRedHerringStep &&
    (!isRealAction || hasSelectedRedHerring)

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

internal fun projectFirstNightNumericInformationOptions(
    phase: ClocktowerPhase,
    roleEnName: String,
    sourceSeat: Int,
    players: List<PlayerState>,
    options: List<ClocktowerDisplayOption>,
): List<ClocktowerDisplayOption> {
    if (phase != ClocktowerPhase.FirstNight || roleEnName !in setOf("Chef", "Empath")) return options

    val currentRegisteredValue = options.asSequence()
        .filter { it.isTruthful }
        .mapNotNull { option ->
            (option.proposition as? InformationProposition.NumericResult)
                ?.takeIf { it.sourceSeat == sourceSeat }
                ?.value
        }
        .firstOrNull()
        ?: options.asSequence()
            .mapNotNull { option ->
                (option.proposition as? InformationProposition.NumericResult)
                    ?.takeIf { it.sourceSeat == sourceSeat }
                    ?.value
            }
            .firstOrNull()
        ?: return options

    val truthfulValues = FirstNightNumericInformationSemantics.recommendationTruthValues(
        players = players,
        sourceSeat = sourceSeat,
        currentRegisteredValue = currentRegisteredValue,
    )

    return options.map { option ->
        val proposition = option.proposition as? InformationProposition.NumericResult
            ?: return@map option
        if (proposition.sourceSeat != sourceSeat) return@map option

        val pressure = truthfulValues.minOfOrNull { truthfulValue ->
            abs(proposition.value - truthfulValue)
        } ?: option.misinformationPressure
        option.copy(
            isTruthful = proposition.value in truthfulValues,
            misinformationPressure = pressure,
        )
    }
}

private data class FirstNightPairInformationKey(
    val shownRole: RoleId?,
    val candidateSeats: List<Int>,
)

private data class FirstNightPairInformationTruth(
    val spyRegisteredRoleEnName: String?,
    val recluseRegisteredRoleEnName: String?,
)

private fun PairInformationOutcome.firstNightPairInformationKey(): FirstNightPairInformationKey =
    FirstNightPairInformationKey(
        shownRole = shownRole,
        candidateSeats = candidateSeats,
    )

private fun InformationProposition.firstNightPairInformationKey(): FirstNightPairInformationKey? = when (this) {
    is InformationProposition.AnyOf -> {
        val roleAt = alternatives.map { it as? InformationProposition.RoleAt ?: return null }
        val shownRole = roleAt.map { it.role }.distinct().singleOrNull() ?: return null
        FirstNightPairInformationKey(
            shownRole = shownRole,
            candidateSeats = roleAt.map { it.seat }.distinct().sorted(),
        )
    }

    is InformationProposition.AllOf -> {
        if (propositions.isEmpty() || propositions.any {
                val roleInPlay = it as? InformationProposition.RoleInPlay
                roleInPlay == null || roleInPlay.inPlay
            }
        ) {
            null
        } else {
            FirstNightPairInformationKey(shownRole = null, candidateSeats = emptyList())
        }
    }

    else -> null
}

internal fun projectFirstNightPairInformationOptions(
    phase: ClocktowerPhase,
    roleEnName: String,
    sourceSeat: Int,
    game: GameState,
    roleDefinitions: List<RoleDefinition>,
    options: List<ClocktowerDisplayOption>,
): List<ClocktowerDisplayOption> {
    if (
        phase != ClocktowerPhase.FirstNight ||
        roleEnName !in setOf("Washerwoman", "Librarian", "Investigator")
    ) {
        return options
    }

    val healthyTruths = NaturalPairInformationCandidateGenerator
        .generateHealthyInformationSpace(
            game = game,
            sourceSeat = sourceSeat,
            abilityRole = RoleId(roleEnName),
            roleDefinitions = roleDefinitions,
        )
        .groupBy { it.outcome.firstNightPairInformationKey() }
        .mapValues { (_, candidates) ->
            val semanticCandidate = candidates.firstOrNull { it.registrations.isEmpty() }
                ?: candidates.minBy { it.candidateId }
            FirstNightPairInformationTruth(
                spyRegisteredRoleEnName = semanticCandidate.registrations
                    .firstOrNull { it.reason == RegistrationReason.SPY_ABILITY }
                    ?.registeredRole
                    ?.value,
                recluseRegisteredRoleEnName = semanticCandidate.registrations
                    .firstOrNull { it.reason == RegistrationReason.RECLUSE_ABILITY }
                    ?.registeredRole
                    ?.value,
            )
        }

    return options.map { option ->
        val key = option.proposition?.firstNightPairInformationKey() ?: return@map option
        val truth = healthyTruths[key]
        val spyRegistrationRole = truth?.spyRegisteredRoleEnName
        val recluseRegistrationRole = truth?.recluseRegisteredRoleEnName
        option.copy(
            isTruthful = truth != null,
            misinformationPressure = if (truth != null) 0 else option.misinformationPressure.coerceAtLeast(1),
            spyRegistersGood = spyRegistrationRole?.let { true },
            spyRegisteredRoleEnName = spyRegistrationRole,
            recluseRegistersEvil = recluseRegistrationRole?.let { true },
            recluseRegisteredRoleEnName = recluseRegistrationRole,
        )
    }
}

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

private fun ClocktowerDisplayOption.isLegalFirstNightPairDisplay(familyId: String): Boolean {
    if (familyId !in setOf("Washerwoman", "Librarian", "Investigator")) return true
    val key = proposition?.firstNightPairInformationKey() ?: return true
    val isZeroCharacterOutcome = key.shownRole == null && key.candidateSeats.isEmpty()
    return !isZeroCharacterOutcome ||
        PairInformationDisplaySemantics.allowsZeroCharacterOutcome(RoleId(familyId))
}

internal fun unifiedFirstNightInformationPool(
    options: List<ClocktowerDisplayOption>,
    familyId: String,
    automaticStyle: RecommendationStyle,
): UnifiedSelectionPool<ClocktowerDisplayOption> = UnifiedSelectionPool(
    options
        .filter { option -> option.isLegalFirstNightPairDisplay(familyId) }
        .map { option ->
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
        },
)

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
