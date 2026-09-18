package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility

internal data class FirstNightDrunkNumericWholeBundleRequest(
    val drunkSeat: Int,
    val evaluationRecipientSeats: Set<Int>,
    val healthyCore: List<EpistemicObservation>,
) {
    init {
        require(drunkSeat > 0)
        require(evaluationRecipientSeats.isNotEmpty())
        require(evaluationRecipientSeats.all { it > 0 })
        require(healthyCore.all { it.visibility == ObservationVisibility.PUBLIC })
    }
}

internal data class FirstNightDrunkNumericCandidateEvaluation(
    val candidateId: String,
    val value: Int,
    val semanticTruth: SemanticTruth,
    val publicObservation: EpistemicObservation,
    val fullBundleByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
    val marginalByRecipient: List<FirstNightDrunkMarginalDiagnostics>,
)

internal sealed interface FirstNightDrunkNumericWholeBundleEvaluation {
    data class Ready(
        val drunkSeat: Int,
        val shownAbility: RoleId,
        val healthyCoreByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
        val candidates: List<FirstNightDrunkNumericCandidateEvaluation>,
    ) : FirstNightDrunkNumericWholeBundleEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : FirstNightDrunkNumericWholeBundleEvaluation {
        init {
            require(missingCapabilities.isNotEmpty())
        }
    }
}

/** D1C Chef/Empath adapter over the shared Drunk whole-bundle exact seam. */
internal object TroubleBrewingFirstNightDrunkNumericWholeBundleEvaluator {
    private val drunk = RoleId("Drunk")
    private val numericRoles = setOf(RoleId("Chef"), RoleId("Empath"))

    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        request: FirstNightDrunkNumericWholeBundleRequest,
    ): FirstNightDrunkNumericWholeBundleEvaluation {
        val game = context.initialSnapshot.gameState
        val source = requireNotNull(game.playerAt(request.drunkSeat)) {
            "Unknown Drunk source seat ${request.drunkSeat}."
        }
        require(source.actualRole == drunk) {
            "D1C numeric evaluation requires the persistent actual Drunk seat."
        }
        val shownAbility = requireNotNull(source.shownRole) {
            "D1C numeric evaluation requires the Drunk persistent shown role."
        }
        require(shownAbility in numericRoles) {
            "D1C numeric evaluation supports only Drunk shown Chef/Empath."
        }

        val candidates = FirstNightNumericLegalDomain.generate(
            game = game,
            sourceSeat = request.drunkSeat,
            abilityRole = shownAbility,
            reliability = ReliabilityState.DRUNK,
        )
        require(candidates.isNotEmpty()) { "Drunk numeric domain must contain at least one legal candidate." }

        val roles = context.roleDefinitions.toList()
        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val sequence = (request.healthyCore.maxOfOrNull(EpistemicObservation::sequence) ?: 0) + 1
        val exactCandidates = candidates.map { candidate ->
            FirstNightDrunkWholeBundleCandidate(
                candidateId = candidate.candidateId,
                semanticTruth = candidate.semanticTruth,
                publicObservation = publicObservation(
                    game = game,
                    roles = roles,
                    formalSnapshotId = formal.snapshotId,
                    phase = context.initialPhase,
                    round = context.initialRound,
                    sequence = sequence,
                    sourceSeat = request.drunkSeat,
                    shownAbility = shownAbility,
                    candidate = candidate,
                ),
            )
        }

        return when (
            val exact = TroubleBrewingFirstNightDrunkWholeBundleExactEvaluator.evaluate(
                validatedRuleset = validatedRuleset,
                context = context,
                evaluationRecipientSeats = request.evaluationRecipientSeats,
                healthyCore = request.healthyCore,
                candidates = exactCandidates,
            )
        ) {
            is FirstNightDrunkWholeBundleExactEvaluation.Deferred ->
                FirstNightDrunkNumericWholeBundleEvaluation.Deferred(exact.missingCapabilities)

            is FirstNightDrunkWholeBundleExactEvaluation.Ready -> {
                val valueById = candidates.associate { it.candidateId to it.value }
                FirstNightDrunkNumericWholeBundleEvaluation.Ready(
                    drunkSeat = request.drunkSeat,
                    shownAbility = shownAbility,
                    healthyCoreByRecipient = exact.healthyCoreByRecipient,
                    candidates = exact.candidates.map { candidate ->
                        FirstNightDrunkNumericCandidateEvaluation(
                            candidateId = candidate.candidateId,
                            value = valueById.getValue(candidate.candidateId),
                            semanticTruth = candidate.semanticTruth,
                            publicObservation = candidate.publicObservation,
                            fullBundleByRecipient = candidate.fullBundleByRecipient,
                            marginalByRecipient = candidate.marginalByRecipient,
                        )
                    },
                )
            }
        }
    }

    private fun publicObservation(
        game: com.codex.campboardgamehost.clocktower.domain.GameState,
        roles: List<RoleDefinition>,
        formalSnapshotId: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        sourceSeat: Int,
        shownAbility: RoleId,
        candidate: FirstNightNumericLegalCandidate,
    ): EpistemicObservation {
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = shownAbility,
                value = InformationValue.Number(candidate.value),
            ),
            roleDefinitions = roles,
        )
        val privateObservation = EpistemicObservation(
            observationId = "sde-d1-drunk-numeric:${candidate.candidateId}",
            snapshotId = formalSnapshotId,
            phase = phase,
            round = round,
            sequence = sequence,
            sourceSeat = sourceSeat,
            sourceAbility = shownAbility,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
        return FirstNightPublicGoodInfoProjection.project(
            FirstNightInformationBundle(
                bundleId = "sde-d1-drunk-numeric",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "drunk-numeric-clue",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = candidate.candidateId,
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }
}
