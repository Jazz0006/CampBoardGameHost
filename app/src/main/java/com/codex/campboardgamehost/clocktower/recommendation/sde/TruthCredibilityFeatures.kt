package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import java.math.BigInteger

/**
 * Score-free exact consequence of one healthy information source.
 *
 * This is descriptive evidence only. It deliberately contains no threshold, weight, role bonus,
 * expected hit rate or candidate ordering.
 */
internal data class TruthDangerSourceImpact(
    val source: ConfirmationChannelRef.Source,
    val exactWorldReduction: BigInteger,
    val strategicWorldKeysRemoved: Set<StrategicWorldKey>,
    val demonSeatsRemoved: Set<Int>,
) {
    init {
        require(exactWorldReduction >= BigInteger.ZERO) {
            "Truth-danger exact-world reduction cannot be negative."
        }
        require(demonSeatsRemoved.all { it > 0 }) {
            "Truth-danger removed Demon seats must be positive."
        }
    }

    val independentlyConstraining: Boolean
        get() =
            exactWorldReduction > BigInteger.ZERO ||
                strategicWorldKeysRemoved.isNotEmpty() ||
                demonSeatsRemoved.isNotEmpty()
}

internal enum class CredibilityDisruptionMechanism {
    RED_HERRING_FALSE_POSITIVE,
}

/**
 * Typed rule-level path by which a committed input can undermine confidence in one healthy source.
 *
 * The mechanism records possibility, not a prediction that a later player-controlled choice will
 * activate it.
 */
internal data class CredibilityDisruptionImpact(
    val committedInputRef: CommittedDecisionInputRef,
    val affectedSource: ConfirmationChannelRef.Source,
    val mechanism: CredibilityDisruptionMechanism,
) {
    init {
        when (mechanism) {
            CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE ->
                require(committedInputRef.kind == SdeCommittedDecisionInputKind.RED_HERRING) {
                    "Red-Herring credibility disruption requires a RED_HERRING committed input."
                }
        }
    }
}

/**
 * Candidate-scoped evidence supplied to the pure truth/credibility projector.
 *
 * Unresolved sources are explicit because an uncommitted or non-rule-determined future information
 * value must never be silently treated as harmless.
 */
internal data class TruthCredibilityCandidateEvidence(
    val candidateId: String,
    val truthDangerSources: List<TruthDangerSourceImpact> = emptyList(),
    val credibilityDisruptions: List<CredibilityDisruptionImpact> = emptyList(),
    val unresolvedSourceRefs: Set<ConfirmationChannelRef.Source> = emptySet(),
) {
    init {
        require(candidateId.isNotBlank()) {
            "Truth/credibility evidence requires a stable candidate ID."
        }
        require(truthDangerSources.map(TruthDangerSourceImpact::source).distinct().size == truthDangerSources.size) {
            "Truth/credibility evidence may contain each resolved source at most once."
        }
        require(credibilityDisruptions.distinct().size == credibilityDisruptions.size) {
            "Truth/credibility evidence cannot duplicate one disruption."
        }
        require(
            truthDangerSources.none { it.source in unresolvedSourceRefs },
        ) {
            "An unresolved source cannot simultaneously carry resolved truth-danger evidence."
        }
    }
}

/**
 * Typed, role-agnostic truth-danger / credibility-disruption feature surface.
 *
 * The legacy reason-code sets remain only for DecisionTrace schema-v1 read compatibility. New C4
 * projection and schema-v2 persistence use the typed fields; legacy reads migrate into the current
 * in-memory trace schema without inventing typed provenance.
 */
internal data class TruthCredibilityFeatures(
    val truthDangerSources: Set<TruthDangerSourceImpact> = emptySet(),
    val credibilityDisruptions: Set<CredibilityDisruptionImpact> = emptySet(),
    val unresolvedSourceRefs: Set<ConfirmationChannelRef.Source> = emptySet(),
    val truthDangerReasonCodes: Set<String> = emptySet(),
    val credibilityDisruptionReasonCodes: Set<String> = emptySet(),
) {
    init {
        require(truthDangerReasonCodes.none(String::isBlank)) {
            "Truth-danger reason codes cannot be blank."
        }
        require(credibilityDisruptionReasonCodes.none(String::isBlank)) {
            "Credibility-disruption reason codes cannot be blank."
        }
        require(truthDangerSources.none { it.source in unresolvedSourceRefs }) {
            "An unresolved source cannot simultaneously carry resolved truth-danger features."
        }
    }

    val hasTypedMaterial: Boolean
        get() =
            truthDangerSources.isNotEmpty() ||
                credibilityDisruptions.isNotEmpty() ||
                unresolvedSourceRefs.isNotEmpty()
}

internal object TruthCredibilityFeaturesProjector {
    fun project(
        candidates: List<TruthCredibilityCandidateEvidence>,
    ): Map<String, TruthCredibilityFeatures> {
        require(
            candidates.map(TruthCredibilityCandidateEvidence::candidateId).distinct().size ==
                candidates.size,
        ) {
            "Truth/credibility candidates must have unique IDs."
        }

        return candidates.associate { evidence ->
            evidence.candidateId to TruthCredibilityFeatures(
                truthDangerSources = evidence.truthDangerSources.toCollection(linkedSetOf()),
                credibilityDisruptions =
                    evidence.credibilityDisruptions.toCollection(linkedSetOf()),
                unresolvedSourceRefs = evidence.unresolvedSourceRefs.toCollection(linkedSetOf()),
            )
        }
    }
}