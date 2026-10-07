package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleCandidateFactorKind
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleDeferredComplexity
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightBundleEntryControl

/**
 * Evidence-qualified, score-free topology features for one legal Drunk-assignment candidate.
 *
 * These are exact setup facts. They carry no preference, severity, threshold or policy meaning.
 */
internal data class DrunkAssignmentTopologyFeatures(
    val previousSeat: Int,
    val nextSeat: Int,
    val adjacentEvilSeats: Set<Int>,
    val adjacentDemonSeats: Set<Int>,
    val adjacentMinionSeats: Set<Int>,
) {
    init {
        require(previousSeat > 0 && nextSeat > 0) {
            "Drunk-assignment topology neighbour seats must be positive."
        }
        require(adjacentEvilSeats.all { it == previousSeat || it == nextSeat }) {
            "Drunk-assignment adjacent Evil seats must be immediate neighbours."
        }
        require(adjacentDemonSeats.all { it in adjacentEvilSeats }) {
            "Drunk-assignment adjacent Demon seats must also be Evil neighbours."
        }
        require(adjacentMinionSeats.all { it in adjacentEvilSeats }) {
            "Drunk-assignment adjacent Minion seats must also be Evil neighbours."
        }
        require(adjacentDemonSeats.intersect(adjacentMinionSeats).isEmpty()) {
            "One adjacent seat cannot be both Demon and Minion."
        }
    }
}

/**
 * One candidate-seat first-night information route copied from the accepted DLB-3B1 consequence
 * envelope. The factor identity and legal option count remain owned upstream.
 */
internal data class DrunkAssignmentFirstNightInformationFactorFeatures(
    val factorId: String,
    val kind: FirstNightBundleCandidateFactorKind,
    val control: FirstNightBundleEntryControl,
    val optionCount: Int,
) {
    init {
        require(factorId.isNotBlank()) {
            "Drunk-assignment first-night factor ID cannot be blank."
        }
        require(optionCount >= 0) {
            "Drunk-assignment first-night option count cannot be negative."
        }
    }

    val hasMultipleLegalOutputs: Boolean
        get() = optionCount > 1
}

/**
 * Known candidate-seat first-night impaired-information opportunity.
 *
 * An empty [factors] list means the current first-night ecology owner knows that this shown role
 * contributes no candidate-seat PUBLIC_GOOD_INFO factor. Empty therefore means known-none, not
 * unavailable.
 */
internal data class DrunkAssignmentFirstNightInformationOpportunityFeatures(
    val factors: List<DrunkAssignmentFirstNightInformationFactorFeatures>,
) {
    init {
        require(
            factors.map(DrunkAssignmentFirstNightInformationFactorFeatures::factorId)
                .distinct()
                .size == factors.size,
        ) {
            "Drunk-assignment first-night factor IDs must be unique."
        }
    }

    val hasStorytellerControlledRoute: Boolean
        get() = factors.any {
            it.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
        }

    val hasMultiOutputStorytellerControlledRoute: Boolean
        get() = factors.any {
            it.control == FirstNightBundleEntryControl.STORYTELLER_CONTROLLED &&
                it.hasMultipleLegalOutputs
        }
}

/**
 * Reserved semantic family for multi-night / longitudinal impaired narrative opportunity.
 *
 * DLB-3B2 deliberately has no implementation because the accepted 3B1 owner is first-night-only.
 * The corresponding projection must remain MISSING_CAPABILITY until a broader consequence owner is
 * introduced and evidence-validated.
 */
internal sealed interface DrunkAssignmentLongitudinalNarrativeOpportunityFeatures

internal data class DrunkAssignmentFeatureLimitations(
    val excludedPlayerControlledElements: Set<String>,
    val deferredComplexities: Set<FirstNightBundleDeferredComplexity>,
) {
    init {
        require(excludedPlayerControlledElements.none(String::isBlank)) {
            "Drunk-assignment excluded player-controlled element IDs cannot be blank."
        }
    }
}

/**
 * Dedicated Drunk-assignment feature surface.
 *
 * This is intentionally separate from ordinary [DecisionFeatures]. It is descriptive only and
 * carries no recommendation-policy or selection authority.
 */
internal data class DrunkAssignmentFeatures(
    val topology: FeatureProjection<DrunkAssignmentTopologyFeatures>,
    val firstNightInformationOpportunity:
        FeatureProjection<DrunkAssignmentFirstNightInformationOpportunityFeatures>,
    val longitudinalNarrativeOpportunity:
        FeatureProjection<DrunkAssignmentLongitudinalNarrativeOpportunityFeatures>,
    val limitations: DrunkAssignmentFeatureLimitations,
)

internal data class CandidateDrunkAssignmentFeatures(
    val candidateId: String,
    val features: DrunkAssignmentFeatures,
) {
    init {
        require(candidateId.isNotBlank()) {
            "Drunk-assignment feature candidate ID cannot be blank."
        }
    }
}

internal data class DrunkAssignmentFeatureEvaluation(
    val candidates: List<CandidateDrunkAssignmentFeatures>,
) {
    init {
        require(candidates.isNotEmpty()) {
            "Drunk-assignment feature evaluation requires candidates."
        }
        require(
            candidates.map(CandidateDrunkAssignmentFeatures::candidateId).distinct().size ==
                candidates.size,
        ) {
            "Drunk-assignment feature candidate IDs must be unique."
        }
    }

    val candidateIds: List<String>
        get() = candidates.map(CandidateDrunkAssignmentFeatures::candidateId)
}

/**
 * Pure DLB-3B2 projection from the accepted 3B1 descriptive consequence envelope.
 *
 * No legal domain is regenerated and no policy semantics are introduced here.
 */
internal object DrunkAssignmentFeaturesProjector {
    fun project(
        envelope: DrunkSetupConsequenceEnvelope,
    ): DrunkAssignmentFeatures =
        DrunkAssignmentFeatures(
            topology = FeatureProjection.Projected(
                DrunkAssignmentTopologyFeatures(
                    previousSeat = envelope.topology.previousSeat,
                    nextSeat = envelope.topology.nextSeat,
                    adjacentEvilSeats = envelope.topology.adjacentEvilSeats,
                    adjacentDemonSeats = envelope.topology.adjacentDemonSeats,
                    adjacentMinionSeats = envelope.topology.adjacentMinionSeats,
                ),
            ),
            firstNightInformationOpportunity = FeatureProjection.Projected(
                DrunkAssignmentFirstNightInformationOpportunityFeatures(
                    factors = envelope.firstNightInformation.factors.map { factor ->
                        DrunkAssignmentFirstNightInformationFactorFeatures(
                            factorId = factor.factorId,
                            kind = factor.kind,
                            control = factor.control,
                            optionCount = factor.optionCount,
                        )
                    },
                ),
            ),
            longitudinalNarrativeOpportunity = FeatureProjection.Unavailable(
                FeatureUnavailableReason.MISSING_CAPABILITY,
            ),
            limitations = DrunkAssignmentFeatureLimitations(
                excludedPlayerControlledElements =
                    envelope.excludedPlayerControlledElements,
                deferredComplexities = envelope.deferredComplexities,
            ),
        )
}
