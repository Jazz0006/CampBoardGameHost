package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics

internal enum class FeatureUnavailableReason {
    NOT_PROJECTED_YET,
    NOT_APPLICABLE,
    MISSING_CAPABILITY,
}

internal sealed interface FeatureProjection<out T> {
    data class Projected<T>(val value: T) : FeatureProjection<T>
    data class Unavailable(val reason: FeatureUnavailableReason) : FeatureProjection<Nothing>
}

internal data class StrategicDecisionFeatures(
    val demonCoverRetention: StrategicRatio,
    val evilTopologyRetention: StrategicRatio,
    val evilCoverRetention: StrategicRatio,
    val forcedGoodFraction: StrategicRatio.Defined,
    val forcedEvilFraction: StrategicRatio.Defined,
    val forcedGoodSeats: Set<Int>,
    val forcedEvilSeats: Set<Int>,
) {
    init {
        require(forcedGoodSeats.all { it > 0 } && forcedEvilSeats.all { it > 0 })
        require(forcedGoodSeats.intersect(forcedEvilSeats).isEmpty()) {
            "A seat cannot be simultaneously forced Good and forced Evil."
        }
    }
}

internal data class HealthyInformationUtilityFeatures(
    val preservedInformationIds: Set<String> = emptySet(),
    val lostInformationIds: Set<String> = emptySet(),
    val reasonCodes: Set<String> = emptySet(),
)

internal data class TruthCredibilityFeatures(
    val truthDangerReasonCodes: Set<String> = emptySet(),
    val credibilityDisruptionReasonCodes: Set<String> = emptySet(),
)

internal data class RoleFunctionExposureFeatures(
    val exposedSeats: Set<Int> = emptySet(),
    val exposedRoles: Set<RoleId> = emptySet(),
    val reasonCodes: Set<String> = emptySet(),
)

internal data class ImpairedNarrativeFeatures(
    val coherenceReasonCodes: Set<String> = emptySet(),
    val detectabilityReasonCodes: Set<String> = emptySet(),
)

internal data class BluffNarrativeFeatures(
    val claimBurdenReasonCodes: Set<String> = emptySet(),
    val narrativeRouteIds: Set<String> = emptySet(),
)

internal data class DecisionRelationshipFeatures(
    val collisionCandidateIds: Set<String> = emptySet(),
    val supportCandidateIds: Set<String> = emptySet(),
)

internal data class FutureFlexibilityFeatures(
    val retainedRouteIds: Set<String> = emptySet(),
    val lostRouteIds: Set<String> = emptySet(),
    val reasonCodes: Set<String> = emptySet(),
)

/**
 * Independent, interpretable policy inputs.
 *
 * Absence is explicit. There is intentionally no totalScore/globalScalar field and no implicit
 * ordering between dimensions.
 */
internal data class DecisionFeatures(
    val strategic: FeatureProjection<StrategicDecisionFeatures>,
    val confirmationChainImpact: FeatureProjection<ConfirmationChainFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val healthyInformationUtility: FeatureProjection<HealthyInformationUtilityFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val truthCredibility: FeatureProjection<TruthCredibilityFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val roleFunctionExposure: FeatureProjection<RoleFunctionExposureFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val semanticTruth: FeatureProjection<SemanticTruth> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val impairedNarrative: FeatureProjection<ImpairedNarrativeFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val bluffNarrative: FeatureProjection<BluffNarrativeFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val relationships: FeatureProjection<DecisionRelationshipFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
    val futureFlexibility: FeatureProjection<FutureFlexibilityFeatures> =
        FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
) {
    companion object {
        fun unavailable(reason: FeatureUnavailableReason): DecisionFeatures {
            val unavailable = FeatureProjection.Unavailable(reason)
            return DecisionFeatures(
                strategic = unavailable,
                confirmationChainImpact = unavailable,
                healthyInformationUtility = unavailable,
                truthCredibility = unavailable,
                roleFunctionExposure = unavailable,
                semanticTruth = unavailable,
                impairedNarrative = unavailable,
                bluffNarrative = unavailable,
                relationships = unavailable,
                futureFlexibility = unavailable,
            )
        }
    }
}

/** Pure projection over existing exact/topology consequence authority. */
internal object DecisionFeaturesProjector {
    fun project(
        diagnostic: ExactHypotheticalObservationBundleDiagnostics,
        playerCount: Int,
        semanticTruth: SemanticTruth? = null,
    ): DecisionFeatures = project(
        normalized = NormalizedStrategicDiagnosticsProjector.project(diagnostic, playerCount),
        afterStructure = diagnostic.afterStructure,
        playerCount = playerCount,
        semanticTruth = semanticTruth,
    )

    fun project(
        diagnostic: ExactStrategicTopologyBundleDiagnostics,
        playerCount: Int,
        semanticTruth: SemanticTruth? = null,
    ): DecisionFeatures = project(
        normalized = NormalizedStrategicDiagnosticsProjector.project(diagnostic, playerCount),
        afterStructure = diagnostic.afterStructure,
        playerCount = playerCount,
        semanticTruth = semanticTruth,
    )

    private fun project(
        normalized: NormalizedStrategicDiagnostics,
        afterStructure: ExactWorldStructureDiagnostics,
        playerCount: Int,
        semanticTruth: SemanticTruth?,
    ): DecisionFeatures {
        require(playerCount > 0) { "Player count must be positive." }
        require(afterStructure.forcedEvilSeats.all { it in 1..playerCount }) {
            "Forced-evil seats must belong to the current player range."
        }
        return DecisionFeatures(
        strategic = FeatureProjection.Projected(
            StrategicDecisionFeatures(
                demonCoverRetention = normalized.demonCoverRetention,
                evilTopologyRetention = normalized.evilTopologyRetention,
                evilCoverRetention = normalized.evilCoverRetention,
                forcedGoodFraction = normalized.forcedGoodFraction,
                forcedEvilFraction = StrategicRatio.Defined(
                    numerator = afterStructure.forcedEvilSeats.size,
                    denominator = playerCount,
                ),
                forcedGoodSeats = afterStructure.forcedGoodSeats,
                forcedEvilSeats = afterStructure.forcedEvilSeats,
            ),
        ),
            semanticTruth = semanticTruth?.let { FeatureProjection.Projected(it) }
                ?: FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_PROJECTED_YET),
        )
    }
}
