package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * B4's deliberately isolated multi-night adapter.  It is a shadow query service: callers receive
 * only candidate satisfiability/cardinality, never a formal state or a poison target.  The A4
 * production cache and recommendation selectors do not call this class.
 */
class B4DynamicPlayerWorldSetShadow(
    private val runtime: A4PlayerWorldSetRuntime = A4PlayerWorldSetRuntime(),
) {
    fun evaluate(request: B4ShadowRequest): B4ShadowReport {
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
        val formal = FormalGameState.from(
            snapshot = reduced.snapshot,
            phase = reduced.phase,
            round = reduced.round,
            publicPropositions = publicDeaths,
            timeline = orderedFacts,
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
            // construction, in stable timeline order, so they constrain worlds rather than being
            // rejected as unsupported setup input.
            val seed = knowledge.copy(publicObservations = emptyList(), privateObservations = emptyList())
            val before = try {
                runtime.build(formal, seed, request.hypothesis, request.roleDefinitions).selected
                    .let { worlds -> (knowledge.publicObservations + knowledge.privateObservations)
                        .sortedWith(compareBy<EpistemicObservation>({ it.round }, { it.sequence }, { it.observationId }))
                        .fold(worlds as PlayerWorldSet) { current, observation -> current.require(observation) } }
            } catch (_: IllegalArgumentException) {
                return B4ShadowReport(B4ShadowOutcome.DEFERRED_B4, emptyList())
            }
            B4CandidateWorldQuery(candidate.candidateId, candidate.recipientSeat, before.cardinality(), before.require(candidate.observation).cardinality())
        }
        return B4ShadowReport(B4ShadowOutcome.READY, queries)
    }
}

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
)

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
