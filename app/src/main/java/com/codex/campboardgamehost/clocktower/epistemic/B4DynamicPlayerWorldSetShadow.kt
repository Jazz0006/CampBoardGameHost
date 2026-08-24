package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger

/**
 * B4's deliberately isolated multi-night adapter.  It is a shadow query service: callers receive
 * only candidate satisfiability/cardinality, never a formal state or a hidden Storyteller target.
 * The A4 production cache and recommendation selectors do not call this class.
 *
 * Supplying [validatedRuleset] opts this shadow into the A3 historical exact baseline. The default
 * constructor deliberately preserves the legacy B4 path until a caller explicitly provides the
 * canonical night-order authority required by historical replay.
 */
class B4DynamicPlayerWorldSetShadow(
    private val runtime: A4PlayerWorldSetRuntime = A4PlayerWorldSetRuntime(),
    private val validatedRuleset: ValidatedClocktowerRuleset? = null,
) {
    fun evaluate(request: B4ShadowRequest): B4ShadowReport {
        validatedRuleset?.let { ruleset ->
            return evaluateHistoricalExact(request, ruleset)
        }

        // ActionFactTimeline is already validated and canonicalized by shared global timeline identity.
        // B4 must not reconstruct ordering authority from the raw domain sequence list.
        val orderedFacts = request.actionTimeline.reducerFacts()
        if (orderedFacts.any { it is ActionFact.Attack || it is ActionFact.Protect || it is ActionFact.RoleChange }) {
            return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
        }
        val reduced = try {
            DynamicActionReducer.reduce(request.initialSnapshot, request.initialPhase, request.initialRound, orderedFacts)
        } catch (_: IllegalArgumentException) {
            return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
        }
        val publicDeaths = orderedFacts.filter { it is ActionFact.Death || it is ActionFact.Execution }
            .map { fact -> InformationProposition.AliveAt((fact as? ActionFact.Death)?.targetSeat ?: (fact as ActionFact.Execution).targetSeat, false) }
        val formal = request.b4FormalGameState(
            snapshot = reduced.snapshot,
            phase = reduced.phase,
            round = reduced.round,
            publicPropositions = publicDeaths,
        )
        val knowledgeBySeat = try {
            A4PlayerKnowledgeFactory.createAll(formal, request.perceivedRolesBySeat, request.observationLog)
                .associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        } catch (_: IllegalArgumentException) {
            return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
        }
        val queries = request.candidates.map { candidate ->
            val knowledge = knowledgeBySeat[candidate.recipientSeat] ?: return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            if (candidate.observation.snapshotId != formal.snapshotId ||
                (candidate.observation.visibility == ObservationVisibility.PRIVATE && candidate.recipientSeat !in candidate.observation.recipientSeats)
            ) return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            // A3's enumerator remains a setup constructor. B4 replays later observations after
            // construction, in shared canonical timeline order, so they constrain worlds rather
            // than being rejected as unsupported setup input.
            val seed = knowledge.copy(publicObservations = emptyList(), privateObservations = emptyList())
            val before = try {
                runtime.build(formal, seed, request.hypothesis, request.roleDefinitions).selected
                    .let { worlds -> knowledge.b4ReplayObservationsInTimelineOrder()
                        .fold(worlds as PlayerWorldSet) { current, observation -> current.require(observation) } }
            } catch (_: IllegalArgumentException) {
                return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            }
            B4CandidateWorldQuery(candidate.candidateId, candidate.recipientSeat, before.cardinality(), before.require(candidate.observation).cardinality())
        }
        return B4ShadowReport(B4ShadowOutcome.READY, queries)
    }

    private fun evaluateHistoricalExact(
        request: B4ShadowRequest,
        ruleset: ValidatedClocktowerRuleset,
    ): B4ShadowReport {
        val setupFormal = try {
            FormalGameState.from(
                snapshot = request.initialSnapshot,
                phase = request.initialPhase,
                round = request.initialRound,
            )
        } catch (_: IllegalArgumentException) {
            return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
        }
        val knowledgeBySeat = try {
            A4PlayerKnowledgeFactory.createAll(
                formal = setupFormal,
                perceivedRolesBySeat = request.perceivedRolesBySeat,
                observationLog = request.observationLog,
            ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        } catch (_: IllegalArgumentException) {
            return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
        }
        val rolesById = request.roleDefinitions.associateBy(RoleDefinition::id)
        if (rolesById.isEmpty()) return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())

        val queries = request.candidates.map { candidate ->
            val knowledge = knowledgeBySeat[candidate.recipientSeat]
                ?: return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            if (candidate.observation.snapshotId != setupFormal.snapshotId ||
                (candidate.observation.visibility == ObservationVisibility.PRIVATE &&
                    candidate.recipientSeat !in candidate.observation.recipientSeats)
            ) {
                return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            }
            val historical = try {
                EnumeratedHistoricalExactBaseline.build(
                    validatedRuleset = ruleset,
                    rulesetRef = request.initialSnapshot.rulesetRef,
                    setupKnowledge = knowledge,
                    hypothesis = request.hypothesis,
                    roleDefinitions = request.roleDefinitions,
                    initialPhase = request.initialPhase,
                    initialRound = request.initialRound,
                    actionTimeline = request.actionTimeline,
                    observationLog = request.observationLog,
                )
            } catch (_: IllegalArgumentException) {
                return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            }
            val beforeWorlds = historical.worldSet.enumeratedWorlds()
            val afterCount = beforeWorlds.count { world ->
                TroubleBrewingWorldObservationEvaluator.evaluate(
                    world = world,
                    roles = rolesById,
                    observation = candidate.observation,
                    hypothesis = request.hypothesis,
                ).matches
            }
            B4CandidateWorldQuery(
                candidateId = candidate.candidateId,
                recipientSeat = candidate.recipientSeat,
                before = exactCardinality(beforeWorlds.size),
                after = exactCardinality(afterCount),
            )
        }
        return B4ShadowReport(B4ShadowOutcome.READY, queries)
    }

    private fun exactCardinality(count: Int): WorldCardinality.Exact =
        WorldCardinality.Exact(BigInteger.valueOf(count.toLong()))
}

/** Preserves the request's already-validated global action identity when B4 materializes formal state. */
internal fun B4ShadowRequest.b4FormalGameState(
    snapshot: GameSnapshot,
    phase: StorytellerPhase,
    round: Int,
    publicPropositions: List<InformationProposition>,
): FormalGameState = FormalGameState.from(
    snapshot = snapshot,
    phase = phase,
    round = round,
    publicPropositions = publicPropositions,
    timeline = actionTimeline.reducerFacts(),
    actionTimelineBinding = FormalActionTimelineBinding.Global(actionTimeline),
)

/** Recombines recipient-visible public/private observations under the shared timeline authority. */
internal fun PlayerKnowledgeSnapshot.b4ReplayObservationsInTimelineOrder(): List<EpistemicObservation> =
    (publicObservations + privateObservations).canonicalTimelineOrder()

data class B4ShadowRequest(
    val initialSnapshot: GameSnapshot,
    val initialPhase: StorytellerPhase,
    val initialRound: Int,
    val actionTimeline: ActionFactTimeline,
    val perceivedRolesBySeat: Map<Int, RoleId>,
    val observationLog: EpistemicObservationLog,
    val hypothesis: EpistemicHypothesis,
    val roleDefinitions: Collection<RoleDefinition>,
    val candidates: List<B4ShadowCandidate>,
) {
    init {
        actionTimeline.requireCompatibleWith(observationLog)
    }
}

data class B4ShadowCandidate(val candidateId: String, val recipientSeat: Int, val observation: EpistemicObservation) {
    init { require(candidateId.isNotBlank() && recipientSeat > 0) }
}

enum class B4ShadowOutcome { READY, DEFERRED_B4 }

data class B4CandidateWorldQuery(
    val candidateId: String,
    val recipientSeat: Int,
    val before: WorldCardinality,
    val after: WorldCardinality,
)

data class B4ShadowReport(val outcome: B4ShadowOutcome, val queries: List<B4CandidateWorldQuery>)
