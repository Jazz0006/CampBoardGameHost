package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger

/**
 * Knowledge-safe historical input for exact hypothetical bundle evaluation.
 *
 * [observationLog] contains only the currently durable recipient-visible history. Hypothetical
 * observations are supplied separately, so the BEFORE baseline never contains a candidate that is
 * then applied again during evaluation.
 */
internal data class ExactHistoricalHypotheticalContext(
    val initialSnapshot: GameSnapshot,
    val initialPhase: StorytellerPhase,
    val initialRound: Int,
    val actionTimeline: ActionFactTimeline,
    val perceivedRolesBySeat: Map<Int, RoleId>,
    val observationLog: EpistemicObservationLog,
    val hypothesis: EpistemicHypothesis,
    val roleDefinitions: Collection<RoleDefinition>,
) {
    init {
        require(initialRound > 0) { "Historical hypothetical evaluation requires a positive initial round." }
        actionTimeline.requireCompatibleWith(observationLog)
    }
}

/**
 * A complete set of hypothetical observations that must hold together in the same exact world.
 *
 * The evaluator deliberately accepts already-bound [EpistemicObservation] values. Draft-to-public
 * projection belongs to the bundle-composition/profile layer; exact epistemic evaluation must not
 * silently reinterpret observation visibility or timeline identity.
 */
internal data class ExactHypotheticalObservationBundleQuery(
    val bundleId: String,
    val recipientSeat: Int,
    val observations: List<EpistemicObservation>,
) {
    init {
        require(bundleId.isNotBlank()) { "Hypothetical observation bundle ID cannot be blank." }
        require(recipientSeat > 0) { "Hypothetical observation bundle recipient seat must be positive." }
    }
}

internal data class ExactHypotheticalObservationBundleDiagnostics(
    val bundleId: String,
    val recipientSeat: Int,
    val before: WorldCardinality.Exact,
    val after: WorldCardinality.Exact,
)

/**
 * Unsupported exact semantics are represented only as [Deferred]. A supported bundle which leaves
 * zero worlds is still [Ready] with an exact zero AFTER cardinality; DEFERRED must never mean UNSAT.
 */
internal sealed interface ExactHypotheticalObservationBundleEvaluation {
    data class Ready(
        val diagnostics: List<ExactHypotheticalObservationBundleDiagnostics>,
    ) : ExactHypotheticalObservationBundleEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : ExactHypotheticalObservationBundleEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred hypothetical bundle evaluation must identify missing epistemic capabilities."
            }
        }
    }
}

/**
 * Exact, mutation-free historical evaluator for composed hypothetical observations.
 *
 * One historical BEFORE baseline is built per recipient and shared by every bundle for that
 * recipient. Every observation in a bundle is evaluated against the same candidate world, so bundle
 * semantics are logical conjunction rather than a sum or merge of independently scored clues.
 * Trouble Brewing observation truth/registration/malfunction semantics remain owned by
 * [TroubleBrewingWorldObservationEvaluator].
 */
internal object ExactHistoricalHypotheticalObservationBundleEvaluator {
    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        queries: List<ExactHypotheticalObservationBundleQuery>,
    ): ExactHypotheticalObservationBundleEvaluation {
        when (
            val availability = EpistemicEvaluationCapabilityBoundary.assess(
                validatedRuleset = validatedRuleset,
                requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            )
        ) {
            EpistemicEvaluationAvailability.Ready -> Unit
            is EpistemicEvaluationAvailability.Deferred ->
                return ExactHypotheticalObservationBundleEvaluation.Deferred(
                    missingCapabilities = availability.missingCapabilities,
                )
        }

        val snapshot = context.initialSnapshot
        val formal = FormalGameState.from(
            snapshot = snapshot,
            phase = context.initialPhase,
            round = context.initialRound,
        )
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = context.perceivedRolesBySeat.toMap(),
            observationLog = context.observationLog,
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val roles = context.roleDefinitions.toList()
        val rolesById = roles.associateBy(RoleDefinition::id)
        require(rolesById.isNotEmpty()) { "Exact hypothetical bundle evaluation requires role definitions." }

        val stableQueries = queries.map { query ->
            query.copy(observations = query.observations.toList())
        }
        stableQueries.forEach { query ->
            require(query.recipientSeat in knowledgeBySeat) {
                "Hypothetical observation bundle ${query.bundleId} references unknown recipient seat ${query.recipientSeat}."
            }
            query.observations.forEach { observation ->
                require(observation.snapshotId == formal.snapshotId) {
                    "Hypothetical observation ${observation.observationId} in bundle ${query.bundleId} is bound to a different formal snapshot."
                }
                require(
                    observation.visibility == ObservationVisibility.PUBLIC ||
                        query.recipientSeat in observation.recipientSeats,
                ) {
                    "Hypothetical observation ${observation.observationId} in bundle ${query.bundleId} is not visible to recipient seat ${query.recipientSeat}."
                }
            }
        }

        val baselineWorldsBySeat = stableQueries
            .map(ExactHypotheticalObservationBundleQuery::recipientSeat)
            .distinct()
            .associateWith { recipientSeat ->
                val knowledge = knowledgeBySeat.getValue(recipientSeat)
                EnumeratedHistoricalExactBaseline.build(
                    validatedRuleset = validatedRuleset,
                    rulesetRef = snapshot.rulesetRef,
                    setupKnowledge = knowledge,
                    hypothesis = context.hypothesis,
                    roleDefinitions = roles,
                    initialPhase = context.initialPhase,
                    initialRound = context.initialRound,
                    actionTimeline = context.actionTimeline,
                    observationLog = context.observationLog,
                ).worldSet.enumeratedWorlds()
            }

        return ExactHypotheticalObservationBundleEvaluation.Ready(
            diagnostics = stableQueries.map { query ->
                val beforeWorlds = baselineWorldsBySeat.getValue(query.recipientSeat)
                val afterCount = beforeWorlds.count { world ->
                    query.observations.all { observation ->
                        TroubleBrewingWorldObservationEvaluator.evaluate(
                            world = world,
                            roles = rolesById,
                            observation = observation,
                            hypothesis = context.hypothesis,
                        ).matches
                    }
                }
                ExactHypotheticalObservationBundleDiagnostics(
                    bundleId = query.bundleId,
                    recipientSeat = query.recipientSeat,
                    before = exactCardinality(beforeWorlds.size),
                    after = exactCardinality(afterCount),
                )
            },
        )
    }

    private fun exactCardinality(count: Int): WorldCardinality.Exact =
        WorldCardinality.Exact(BigInteger.valueOf(count.toLong()))
}
