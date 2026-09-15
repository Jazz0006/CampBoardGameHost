package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger

/**
 * Knowledge-safe historical input for exact hypothetical observation evaluation.
 *
 * [observationLog] is the currently durable recipient-visible history. Hypothetical observations are
 * supplied separately and are never appended here, so each candidate is evaluated exactly once
 * against the same BEFORE baseline.
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

/** One already-bound hypothetical observation to evaluate without mutating durable history. */
internal data class ExactHypotheticalObservationQuery(
    val queryId: String,
    val recipientSeat: Int,
    val observation: EpistemicObservation,
) {
    init {
        require(queryId.isNotBlank()) { "Hypothetical observation query ID cannot be blank." }
        require(recipientSeat > 0) { "Hypothetical observation recipient seat must be positive." }
    }
}

internal data class ExactHypotheticalObservationDiagnostics(
    val queryId: String,
    val recipientSeat: Int,
    val before: WorldCardinality.Exact,
    val after: WorldCardinality.Exact,
)

/**
 * Neutral exact-evaluation result. Unsupported semantics are represented only as [Deferred]; they
 * never carry an empty world set or zero cardinality that could be confused with contradiction.
 */
internal sealed interface ExactHypotheticalObservationEvaluation {
    data class Ready(
        val diagnostics: List<ExactHypotheticalObservationDiagnostics>,
    ) : ExactHypotheticalObservationEvaluation

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : ExactHypotheticalObservationEvaluation {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred hypothetical evaluation must identify missing epistemic capabilities."
            }
        }
    }
}

/**
 * Exact, mutation-free historical hypothetical observation evaluator.
 *
 * This owner contains no recommendation policy and exposes no concrete possible worlds. A single
 * historical BEFORE baseline is built per recipient and shared across that recipient's candidate
 * queries; each hypothetical observation is then tested once against those worlds.
 */
internal object ExactHistoricalHypotheticalObservationEvaluator {
    fun evaluate(
        validatedRuleset: ValidatedClocktowerRuleset,
        context: ExactHistoricalHypotheticalContext,
        queries: List<ExactHypotheticalObservationQuery>,
    ): ExactHypotheticalObservationEvaluation {
        when (
            val availability = EpistemicEvaluationCapabilityBoundary.assess(
                validatedRuleset = validatedRuleset,
                requiredCapabilities = EpistemicEvaluationCapabilityBoundary.HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
            )
        ) {
            EpistemicEvaluationAvailability.Ready -> Unit
            is EpistemicEvaluationAvailability.Deferred ->
                return ExactHypotheticalObservationEvaluation.Deferred(
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
        require(rolesById.isNotEmpty()) { "Exact hypothetical evaluation requires role definitions." }

        val stableQueries = queries.toList()
        stableQueries.forEach { query ->
            require(query.observation.snapshotId == formal.snapshotId) {
                "Hypothetical observation ${query.queryId} is bound to a different formal snapshot."
            }
            require(
                query.observation.visibility == ObservationVisibility.PUBLIC ||
                    query.recipientSeat in query.observation.recipientSeats,
            ) {
                "Hypothetical observation ${query.queryId} is not visible to recipient seat ${query.recipientSeat}."
            }
            require(query.recipientSeat in knowledgeBySeat) {
                "Hypothetical observation ${query.queryId} references unknown recipient seat ${query.recipientSeat}."
            }
        }

        val baselineWorldsBySeat = stableQueries.map(ExactHypotheticalObservationQuery::recipientSeat)
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

        return ExactHypotheticalObservationEvaluation.Ready(
            diagnostics = stableQueries.map { query ->
                val beforeWorlds = baselineWorldsBySeat.getValue(query.recipientSeat)
                val afterCount = beforeWorlds.count { world ->
                    TroubleBrewingWorldObservationEvaluator.evaluate(
                        world = world,
                        roles = rolesById,
                        observation = query.observation,
                        hypothesis = context.hypothesis,
                    ).matches
                }
                ExactHypotheticalObservationDiagnostics(
                    queryId = query.queryId,
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
