package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.rules.FortuneTellerInformationSemantics

internal data class FirstNightDrunkFortuneTellerWholeBundleRequest(
    val drunkSeat: Int,
    val selectedTargetSeats: List<Int>,
    val evaluationRecipientSeats: Set<Int>,
    val healthyCore: List<EpistemicObservation>,
) {
    init {
        require(drunkSeat > 0)
        require(selectedTargetSeats.size == 2 && selectedTargetSeats.distinct().size == 2) {
            "Fortune Teller robustness input requires exactly two distinct player-selected seats."
        }
        require(evaluationRecipientSeats.isNotEmpty())
        require(evaluationRecipientSeats.all { it > 0 })
        require(healthyCore.all { it.visibility == ObservationVisibility.PUBLIC })
    }
}

internal data class FirstNightDrunkFortuneTellerCandidateEvaluation(
    val candidateId: String,
    val answer: YesNoAnswer,
    val semanticTruth: SemanticTruth,
    val publicObservation: EpistemicObservation,
    val fullBundleByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
    val marginalByRecipient: List<FirstNightDrunkMarginalDiagnostics>,
)

internal sealed interface FirstNightDrunkFortuneTellerWholeBundleEvaluation {
    data class Ready(
        val drunkSeat: Int,
        val selectedTargetSeats: List<Int>,
        val healthyCoreByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
        val candidates: List<FirstNightDrunkFortuneTellerCandidateEvaluation>,
    ) : FirstNightDrunkFortuneTellerWholeBundleEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : FirstNightDrunkFortuneTellerWholeBundleEvaluation {
        init {
            require(missingCapabilities.isNotEmpty())
        }
    }
}

/**
 * D1D Fortune Teller adapter.
 *
 * The selected target pair is player-controlled robustness input. The Drunk result itself is the
 * complete Yes/No surface domain. Canonical semantic truth is NOT_APPLICABLE because an actual Drunk
 * has no canonical Fortune Teller Red Herring; exact functioning counterworlds retain their own
 * mechanically legal Red Herring state.
 */
internal object TroubleBrewingFirstNightDrunkFortuneTellerWholeBundleEvaluator {
    private val drunk = RoleId("Drunk")
    private val fortuneTeller = RoleId("Fortune Teller")

    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        request: FirstNightDrunkFortuneTellerWholeBundleRequest,
    ): FirstNightDrunkFortuneTellerWholeBundleEvaluation {
        val game = context.initialSnapshot.gameState
        val source = requireNotNull(game.playerAt(request.drunkSeat)) {
            "Unknown Drunk source seat ${request.drunkSeat}."
        }
        require(source.actualRole == drunk && source.shownRole == fortuneTeller) {
            "D1D Fortune Teller evaluation requires the persistent Drunk shown Fortune Teller."
        }

        val canonicalTargets = request.selectedTargetSeats.sorted()
        val legalTargetPairs = FortuneTellerInformationSemantics
            .legalTargetPairs(game, request.drunkSeat)
            .map { listOf(it.first, it.second) }
        require(canonicalTargets in legalTargetPairs) {
            "Selected Fortune Teller targets are not legal for the perceived Fortune Teller ability."
        }

        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val sequence = (request.healthyCore.maxOfOrNull(EpistemicObservation::sequence) ?: 0) + 1
        val candidates = listOf(YesNoAnswer.NO, YesNoAnswer.YES).map { answer ->
            FirstNightDrunkWholeBundleCandidate(
                candidateId = if (answer == YesNoAnswer.YES) "answer-yes" else "answer-no",
                semanticTruth = SemanticTruth.NOT_APPLICABLE,
                publicObservation = publicObservation(
                    formalSnapshotId = formal.snapshotId,
                    phase = context.initialPhase,
                    round = context.initialRound,
                    sequence = sequence,
                    sourceSeat = request.drunkSeat,
                    selectedTargetSeats = canonicalTargets,
                    answer = answer,
                ),
            )
        }

        return when (
            val exact = TroubleBrewingFirstNightDrunkWholeBundleExactEvaluator.evaluate(
                validatedRuleset = validatedRuleset,
                context = context,
                evaluationRecipientSeats = request.evaluationRecipientSeats,
                healthyCore = request.healthyCore,
                candidates = candidates,
            )
        ) {
            is FirstNightDrunkWholeBundleExactEvaluation.Deferred ->
                FirstNightDrunkFortuneTellerWholeBundleEvaluation.Deferred(exact.missingCapabilities)

            is FirstNightDrunkWholeBundleExactEvaluation.Ready -> {
                val answerById = mapOf(
                    "answer-no" to YesNoAnswer.NO,
                    "answer-yes" to YesNoAnswer.YES,
                )
                FirstNightDrunkFortuneTellerWholeBundleEvaluation.Ready(
                    drunkSeat = request.drunkSeat,
                    selectedTargetSeats = canonicalTargets,
                    healthyCoreByRecipient = exact.healthyCoreByRecipient,
                    candidates = exact.candidates.map { candidate ->
                        FirstNightDrunkFortuneTellerCandidateEvaluation(
                            candidateId = candidate.candidateId,
                            answer = answerById.getValue(candidate.candidateId),
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
        formalSnapshotId: String,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
        sourceSeat: Int,
        selectedTargetSeats: List<Int>,
        answer: YesNoAnswer,
    ): EpistemicObservation {
        val candidateId = if (answer == YesNoAnswer.YES) "answer-yes" else "answer-no"
        val information = EffectDraft.PlayerInformation(
            recipientSeat = sourceSeat,
            sourceAbility = fortuneTeller,
            value = InformationValue.YesNo(answer),
        )
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materializeFortuneTeller(
            information = information,
            targetSeats = selectedTargetSeats,
        )
        val privateObservation = EpistemicObservation(
            observationId = "sde-d1-drunk-ft:$candidateId",
            snapshotId = formalSnapshotId,
            phase = phase,
            round = round,
            sequence = sequence,
            sourceSeat = sourceSeat,
            sourceAbility = fortuneTeller,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
        return FirstNightPublicGoodInfoProjection.project(
            FirstNightInformationBundle(
                bundleId = "sde-d1-drunk-ft",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "drunk-fortune-teller-clue",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = candidateId,
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }
}
