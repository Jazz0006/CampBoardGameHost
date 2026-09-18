package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import java.math.BigInteger

/**
 * D1B input: one already-composed public HealthyCore and the persistent Drunk source seat.
 *
 * Healthy information selection stays owned by the wider whole-bundle composer. This seam owns only
 * the still-uncommitted Drunk pair clue dimension and compares each legal candidate against the same
 * HealthyCore without mutating session history.
 */
internal data class FirstNightDrunkPairWholeBundleRequest(
    val drunkSeat: Int,
    val evaluationRecipientSeats: Set<Int>,
    val healthyCore: List<EpistemicObservation>,
) {
    init {
        require(drunkSeat > 0) { "Drunk source seat must be positive." }
        require(evaluationRecipientSeats.isNotEmpty()) {
            "Drunk whole-bundle evaluation requires at least one recipient."
        }
        require(evaluationRecipientSeats.all { it > 0 }) {
            "Drunk whole-bundle recipients must be positive seats."
        }
        require(healthyCore.all { it.visibility == ObservationVisibility.PUBLIC }) {
            "D1B HealthyCore must contain only public projected observations."
        }
    }
}

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

internal data class FirstNightDrunkPairCandidateEvaluation(
    val candidateId: String,
    val semanticTruth: SemanticTruth,
    val publicObservation: EpistemicObservation,
    val fullBundleByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
    val marginalByRecipient: List<FirstNightDrunkMarginalDiagnostics>,
)

internal sealed interface FirstNightDrunkPairWholeBundleEvaluation {
    data class Ready(
        val drunkSeat: Int,
        val shownAbility: RoleId,
        val healthyCoreByRecipient: List<ExactHypotheticalObservationBundleDiagnostics>,
        val candidates: List<FirstNightDrunkPairCandidateEvaluation>,
    ) : FirstNightDrunkPairWholeBundleEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : FirstNightDrunkPairWholeBundleEvaluation {
        init {
            require(missingCapabilities.isNotEmpty())
        }
    }
}

/**
 * D1B pair slice of the generalized first-night whole-bundle owner.
 *
 * Candidate legality comes from [PairInformationLegalDomain]. Mechanical consequences come only
 * from the exact epistemic evaluator. This object does not score candidates, reselect the Drunk shown
 * role, bind registration policy, or commit an observation.
 */
internal object TroubleBrewingFirstNightDrunkPairWholeBundleEvaluator {
    private val drunk = RoleId("Drunk")
    private val pairRoles = setOf(
        RoleId("Washerwoman"),
        RoleId("Librarian"),
        RoleId("Investigator"),
    )

    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        request: FirstNightDrunkPairWholeBundleRequest,
    ): FirstNightDrunkPairWholeBundleEvaluation {
        val game = context.initialSnapshot.gameState
        val source = requireNotNull(game.playerAt(request.drunkSeat)) {
            "Unknown Drunk source seat ${request.drunkSeat}."
        }
        require(source.actualRole == drunk) {
            "D1B pair evaluation requires the persistent actual Drunk seat."
        }
        val shownAbility = requireNotNull(source.shownRole) {
            "D1B pair evaluation requires the Drunk persistent shown role."
        }
        require(shownAbility in pairRoles) {
            "D1B pair evaluation supports only Drunk shown pair-information roles."
        }

        val roles = context.roleDefinitions.toList()
        val candidates = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = request.drunkSeat,
            abilityRole = shownAbility,
            reliability = ReliabilityState.DRUNK,
        ).sortedBy { it.candidateId }
        require(candidates.isNotEmpty()) { "Drunk pair domain must contain at least one legal candidate." }

        val formal = FormalGameState.from(
            context.initialSnapshot,
            context.initialPhase,
            context.initialRound,
        )
        val sequence = (request.healthyCore.maxOfOrNull(EpistemicObservation::sequence) ?: 0) + 1
        val publicByCandidateId = candidates.associate { candidate ->
            candidate.candidateId to publicObservation(
                game = game,
                roles = roles,
                formal = formal,
                sequence = sequence,
                sourceSeat = request.drunkSeat,
                shownAbility = shownAbility,
                candidate = candidate,
            )
        }

        val recipientSeats = request.evaluationRecipientSeats.toSortedSet()
        val queries = buildList {
            recipientSeats.forEach { recipientSeat ->
                add(
                    ExactHypotheticalObservationBundleQuery(
                        bundleId = healthyCoreQueryId(recipientSeat),
                        recipientSeat = recipientSeat,
                        observations = request.healthyCore,
                    ),
                )
            }
            candidates.forEach { candidate ->
                val publicObservation = publicByCandidateId.getValue(candidate.candidateId)
                recipientSeats.forEach { recipientSeat ->
                    add(
                        ExactHypotheticalObservationBundleQuery(
                            bundleId = fullBundleQueryId(candidate.candidateId, recipientSeat),
                            recipientSeat = recipientSeat,
                            observations = request.healthyCore + publicObservation,
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
                FirstNightDrunkPairWholeBundleEvaluation.Deferred(exact.missingCapabilities)

            is ExactHypotheticalObservationBundleEvaluation.Ready -> {
                val byId = exact.diagnostics.associateBy(ExactHypotheticalObservationBundleDiagnostics::bundleId)
                val healthyCore = recipientSeats.map { recipientSeat ->
                    byId.getValue(healthyCoreQueryId(recipientSeat))
                }
                val coreByRecipient = healthyCore.associateBy(ExactHypotheticalObservationBundleDiagnostics::recipientSeat)
                val completedCandidates = candidates.map { candidate ->
                    val fullBundle = recipientSeats.map { recipientSeat ->
                        byId.getValue(fullBundleQueryId(candidate.candidateId, recipientSeat))
                    }
                    FirstNightDrunkPairCandidateEvaluation(
                        candidateId = candidate.candidateId,
                        semanticTruth = candidate.semanticTruth,
                        publicObservation = publicByCandidateId.getValue(candidate.candidateId),
                        fullBundleByRecipient = fullBundle,
                        marginalByRecipient = fullBundle.map { full ->
                            marginal(
                                healthyCore = coreByRecipient.getValue(full.recipientSeat),
                                fullBundle = full,
                            )
                        },
                    )
                }
                FirstNightDrunkPairWholeBundleEvaluation.Ready(
                    drunkSeat = request.drunkSeat,
                    shownAbility = shownAbility,
                    healthyCoreByRecipient = healthyCore,
                    candidates = completedCandidates,
                )
            }
        }
    }

    private fun publicObservation(
        game: com.codex.campboardgamehost.clocktower.domain.GameState,
        roles: List<com.codex.campboardgamehost.clocktower.domain.RoleDefinition>,
        formal: FormalGameState,
        sequence: Int,
        sourceSeat: Int,
        shownAbility: RoleId,
        candidate: PairInformationLegalCandidate,
    ): EpistemicObservation {
        val outcome = candidate.outcome
        val informationValue = if (outcome.shownRole == null) {
            InformationValue.NoCharacters(CharacterType.OUTSIDER)
        } else {
            InformationValue.PlayerPair(
                shownRole = outcome.shownRole,
                seats = outcome.candidateSeats,
            )
        }
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = shownAbility,
                value = informationValue,
            ),
            roleDefinitions = roles,
        )
        val privateObservation = EpistemicObservation(
            observationId = "sde-d1-drunk-pair:${candidate.candidateId}",
            snapshotId = formal.snapshotId,
            phase = contextPhase(formal),
            round = formal.round,
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
                bundleId = "sde-d1-drunk-pair:${candidate.candidateId}",
                entries = listOf(
                    FirstNightInformationBundleEntry(
                        entryId = "drunk-pair-clue",
                        control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                        sourceChoiceId = candidate.candidateId,
                        observation = privateObservation,
                        profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                    ),
                ),
            ),
        ).single()
    }

    private fun contextPhase(formal: FormalGameState) = formal.phase

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

    private fun fullBundleQueryId(candidateId: String, recipientSeat: Int): String =
        "sde-d1:full:$candidateId:recipient-$recipientSeat"
}
