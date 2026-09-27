package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility

/**
 * One rule-determined functioning information claim whose exact consequence can be evaluated without
 * introducing any Storyteller choice or future player-controlled input.
 */
internal data class TruthDangerExactSourceClaim(
    val source: ConfirmationChannelRef.Source,
    val proposition: InformationProposition,
) {
    init {
        require(source.sourceSeat != null) {
            "Exact truth-danger projection requires a source seat."
        }
        require(source.sourceAbility != null) {
            "Exact truth-danger projection requires a source ability."
        }
    }
}

internal sealed interface ExactTruthDangerSourceProjection {
    data class Ready(
        val impacts: Set<TruthDangerSourceImpact>,
    ) : ExactTruthDangerSourceProjection

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : ExactTruthDangerSourceProjection {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred truth-danger projection must identify missing exact capability."
            }
        }
    }
}

/**
 * Pure exact projector for rule-determined healthy information.
 *
 * Truth danger is intentionally measured from the information source's own functioning ability
 * observation. Public/social credibility is a separate feature dimension; this projector therefore
 * does not model claims, trust, Red-Herring hit probability, target ecology, or candidate ordering.
 */
internal object ExactTruthDangerSourceProjector {
    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        claims: Collection<TruthDangerExactSourceClaim>,
    ): ExactTruthDangerSourceProjection {
        val canonicalClaims = claims
            .sortedWith(
                compareBy<TruthDangerExactSourceClaim>(
                    { requireNotNull(it.source.sourceSeat) },
                    { requireNotNull(it.source.sourceAbility).value },
                ),
            )
        require(canonicalClaims.map(TruthDangerExactSourceClaim::source).distinct().size == canonicalClaims.size) {
            "Exact truth-danger projection may evaluate each source at most once."
        }
        if (canonicalClaims.isEmpty()) {
            return ExactTruthDangerSourceProjection.Ready(emptySet())
        }

        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val queries = canonicalClaims.mapIndexed { index, claim ->
            val seat = requireNotNull(claim.source.sourceSeat)
            val ability = requireNotNull(claim.source.sourceAbility)
            val observation = EpistemicObservation(
                observationId = "sde-c4-truth-$index-seat-$seat-${ability.value}",
                snapshotId = formal.snapshotId,
                phase = context.initialPhase,
                round = context.initialRound,
                sequence = index,
                sourceSeat = seat,
                sourceAbility = ability,
                visibility = ObservationVisibility.PRIVATE,
                recipientSeats = setOf(seat),
                reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
                proposition = claim.proposition,
            )
            ExactHypotheticalObservationBundleQuery(
                bundleId = "sde-c4-truth-$index-seat-$seat",
                recipientSeat = seat,
                observations = listOf(observation),
            )
        }

        return when (
            val evaluation = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                validatedRuleset = validatedRuleset,
                context = context,
                queries = queries,
            )
        ) {
            is ExactHypotheticalObservationBundleEvaluation.Deferred ->
                ExactTruthDangerSourceProjection.Deferred(evaluation.missingCapabilities)

            is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                require(evaluation.diagnostics.size == canonicalClaims.size) {
                    "Exact truth-danger evaluator returned a different diagnostic count."
                }
                val impacts = canonicalClaims.zip(evaluation.diagnostics).mapTo(linkedSetOf()) {
                        (claim, diagnostic) ->
                    val exactReduction = diagnostic.before.value - diagnostic.after.value
                    require(exactReduction.signum() >= 0) {
                        "Healthy information cannot increase exact-world cardinality."
                    }
                    TruthDangerSourceImpact(
                        source = claim.source,
                        exactWorldReduction = exactReduction,
                        strategicWorldKeysRemoved =
                            diagnostic.beforeStructure.strategicWorldKeys -
                                diagnostic.afterStructure.strategicWorldKeys,
                        demonSeatsRemoved =
                            diagnostic.beforeStructure.possibleDemonSeats -
                                diagnostic.afterStructure.possibleDemonSeats,
                    )
                }
                ExactTruthDangerSourceProjection.Ready(impacts)
            }
        }
    }
}