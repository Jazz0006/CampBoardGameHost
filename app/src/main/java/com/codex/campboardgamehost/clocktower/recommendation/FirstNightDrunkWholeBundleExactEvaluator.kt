package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import java.math.BigInteger

/** Structural delta only. Selection policy remains a later SDE concern. */
internal data class FirstNightDrunkMarginalDiagnostics(
    val recipientSeat: Int,
    val rawWorldsRemoved: BigInteger,
    val removedDemonSeats: Set<Int>,
    val removedEvilTeamConfigurations: Set<Set<Int>>,
    val newlyForcedGoodSeats: Set<Int>,
    val newlyForcedEvilSeats: Set<Int>,
    val removedEvilCoverSeats: Set<Int>,
)

internal data class FirstNightDrunkWholeBundleCandidate(
    val candidateId: String,
    val semanticTruth: SemanticTruth,
    val publicObservation: EpistemicObservation,
) {
    init {
        require(candidateId.isNotBlank())
        require(publicObservation.visibility == ObservationVisibility.PUBLIC)
    }
}

internal data class FirstNightDrunkWholeBundleCandidateExactEvaluation(
    val candidateId: String,
    val semanticTruth: SemanticTruth,
    val publicObservation: EpistemicObservation,
    val fullBundleByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
    val marginalByRecipient: List<FirstNightDrunkMarginalDiagnostics>,
)

internal sealed interface FirstNightDrunkWholeBundleExactEvaluation {
    data class Ready(
        val healthyCoreByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
        val candidates: List<FirstNightDrunkWholeBundleCandidateExactEvaluation>,
    ) : FirstNightDrunkWholeBundleExactEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : FirstNightDrunkWholeBundleExactEvaluation {
        init {
            require(missingCapabilities.isNotEmpty())
        }
    }
}

/**
 * Shared exact consequence seam for D1B/D1C/D1D Drunk candidates.
 *
 * Legal candidate generation and typed clue materialization stay with their role-specific adapters.
 * This object only compares one fixed HealthyCore against each already-public candidate observation
 * in the same exact-world authority.
 */
internal object TroubleBrewingFirstNightDrunkWholeBundleExactEvaluator {
    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        evaluationRecipientSeats: Set<Int>,
        healthyCore: List<EpistemicObservation>,
        candidates: List<FirstNightDrunkWholeBundleCandidate>,
    ): FirstNightDrunkWholeBundleExactEvaluation {
        require(evaluationRecipientSeats.isNotEmpty())
        require(evaluationRecipientSeats.all { it > 0 })
        require(healthyCore.all { it.visibility == ObservationVisibility.PUBLIC })
        require(candidates.isNotEmpty())
        require(candidates.map { it.candidateId }.distinct().size == candidates.size)

        val recipientSeats = evaluationRecipientSeats.toSortedSet()
        val queries = buildList {
            recipientSeats.forEach { recipientSeat ->
                add(
                    ExactHypotheticalObservationBundleQuery(
                        bundleId = healthyCoreQueryId(recipientSeat),
                        recipientSeat = recipientSeat,
                        observations = healthyCore,
                    ),
                )
            }
            candidates.forEachIndexed { candidateIndex, candidate ->
                recipientSeats.forEach { recipientSeat ->
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = fullBundleQueryId(candidateIndex, recipientSeat),
                            recipientSeat = recipientSeat,
                            observations = healthyCore + candidate.publicObservation,
                        ),
                    )
                }
            }
        }

        return when (
            val exact = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
                validatedRuleset = validatedRuleset,
                context = context,
                queries = queries,
            )
        ) {
            is ExactHypotheticalObservationBundleEvaluation.Deferred ->
                FirstNightDrunkWholeBundleExactEvaluation.Deferred(exact.missingCapabilities)

            is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                val byId = exact.diagnostics.associateBy(ExactHypotheticalObservationBundleDiagnostics::bundleId)
                val healthyCoreDiagnostics = recipientSeats.map { recipientSeat ->
                    byId.getValue(healthyCoreQueryId(recipientSeat))
                }
                val coreByRecipient = healthyCoreDiagnostics
                    .associateBy(ExactHypotheticalObservationBundleDiagnostics::recipientSeat)
                val completedCandidates = candidates.mapIndexed { candidateIndex, candidate ->
                    val fullBundle = recipientSeats.map { recipientSeat ->
                        byId.getValue(fullBundleQueryId(candidateIndex, recipientSeat))
                    }
                    FirstNightDrunkWholeBundleCandidateExactEvaluation(
                        candidateId = candidate.candidateId,
                        semanticTruth = candidate.semanticTruth,
                        publicObservation = candidate.publicObservation,
                        fullBundleByRecipient = fullBundle,
                        marginalByRecipient = fullBundle.map { full ->
                            marginal(
                                healthyCore = coreByRecipient.getValue(full.recipientSeat),
                                fullBundle = full,
                            )
                        },
                    )
                }
                FirstNightDrunkWholeBundleExactEvaluation.Ready(
                    healthyCoreByRecipient = healthyCoreDiagnostics,
                    candidates = completedCandidates,
                )
            }
        }
    }

    private fun marginal(
        healthyCore: ExactHypotheticalObservationBundleDiagnostics,
        fullBundle: ExactHypotheticalObservationBundleDiagnostics,
    ): FirstNightDrunkMarginalDiagnostics {
        require(healthyCore.recipientSeat == fullBundle.recipientSeat)
        require(fullBundle.after.value <= healthyCore.after.value) {
            "Adding a Drunk clue cannot create exact worlds."
        }
        require(
            fullBundle.afterStructure.possibleDemonSeats.all(
                healthyCore.afterStructure.possibleDemonSeats::contains,
            ),
        )
        require(
            fullBundle.afterStructure.evilTeamSeatConfigurations.all(
                healthyCore.afterStructure.evilTeamSeatConfigurations::contains,
            ),
        )
        require(
            fullBundle.afterStructure.evilCoverSeats.all(
                healthyCore.afterStructure.evilCoverSeats::contains,
            ),
        )

        return FirstNightDrunkMarginalDiagnostics(
            recipientSeat = fullBundle.recipientSeat,
            rawWorldsRemoved = healthyCore.after.value - fullBundle.after.value,
            removedDemonSeats =
                healthyCore.afterStructure.possibleDemonSeats - fullBundle.afterStructure.possibleDemonSeats,
            removedEvilTeamConfigurations =
                healthyCore.afterStructure.evilTeamSeatConfigurations -
                    fullBundle.afterStructure.evilTeamSeatConfigurations,
            newlyForcedGoodSeats =
                fullBundle.afterStructure.forcedGoodSeats - healthyCore.afterStructure.forcedGoodSeats,
            newlyForcedEvilSeats =
                fullBundle.afterStructure.forcedEvilSeats - healthyCore.afterStructure.forcedEvilSeats,
            removedEvilCoverSeats =
                healthyCore.afterStructure.evilCoverSeats - fullBundle.afterStructure.evilCoverSeats,
        )
    }

    private fun healthyCoreQueryId(recipientSeat: Int): String =
        "sde-d1:healthy-core:recipient-$recipientSeat"

    private fun fullBundleQueryId(candidateIndex: Int, recipientSeat: Int): String =
        "sde-d1:full-$candidateIndex:recipient-$recipientSeat"
}
