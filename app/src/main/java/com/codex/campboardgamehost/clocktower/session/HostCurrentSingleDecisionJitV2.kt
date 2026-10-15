package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderCandidatePayloadV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1

/**
 * AI-EVENT-JIT-1A: a pure Host-owned gate for ONE CURRENT choice.
 *
 * This is not a recommender, an event writer or a model transport. An unchanged
 * private proposal can be reused; neither a cached nor a model proposal can
 * publish information or select an unresolved registration witness.
 */
internal object HostCurrentSingleDecisionJitV2 {
    internal data class Source(
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val phase: StorytellerPhase,
        val round: Int,
        val decisionId: String,
        val memoryVersion: Int,
    )

    /** Frozen actual Host facts, including actions, observations and decisions. */
    internal data class Scope(
        val frozen: ClocktowerSessionState,
        val phase: StorytellerPhase,
        val round: Int,
        val decision: StorytellerStrategicContinuationV2.OpenDecision,
        val memoryVersion: Int,
    ) {
        init {
            require(frozen.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
            require(round >= 1 && memoryVersion >= 0)
            require(decision.legalCandidates.isNotEmpty())
            require(decision.legalCandidates.map { it.candidateId }.distinct().size ==
                decision.legalCandidates.size)
            require(decision.sourceSeat == null ||
                frozen.gameState.playerAt(decision.sourceSeat) != null)
            if (decision.family == "NIGHT_INFORMATION") {
                require(decision.sourceSeat != null)
                require(frozen.epistemicObservationLog.records.none {
                    it.phase == phase && it.round == round &&
                        it.sourceSeat == decision.sourceSeat
                }) { "Already published information cannot become an open JIT decision." }
            }
        }

        val source: Source get() = Source(
            frozen.gameId, frozen.gameStateRevision, frozen.playerInputRevision,
            frozen.nextTimelineGlobalSequence, phase, round,
            decision.decisionId, memoryVersion,
        )
    }

    /** Production caller must form this from a fresh Host pending decision. */
    fun capture(
        frozen: ClocktowerSessionState,
        phase: StorytellerPhase,
        round: Int,
        pending: StorytellerProviderRequestV1,
        memoryVersion: Int,
    ): Scope {
        val legal = HostCurrentOpenDecisionInventoryV2.fromPendingRequests(
            frozen, phase, round, listOf(pending)).single()
        return Scope(frozen, phase, round, legal, memoryVersion)
    }

    internal data class PrivateCache(val scope: Scope, val candidateId: String)

    internal sealed interface Route {
        /** Visible meaning only: no implicit choice between registration witnesses. */
        data class HostDetermined(val visibleMeaning: String) : Route
        data class CacheHit(val candidateId: String) : Route
        data object ModelRequired : Route
    }

    internal data class Proposal(val source: Source, val candidateId: String)

    /** Equivalent witnesses of the same visible clue are not separate AI choices. */
    private fun visibleMeaning(payload: StorytellerProviderCandidatePayloadV1): String? =
        when (payload) {
            is StorytellerProviderCandidatePayloadV1.ScalarResult ->
                "scalar:" + payload.value
            is StorytellerProviderCandidatePayloadV1.PairInformation ->
                payload.shownRoleId?.let { role ->
                    "pair:" + role + ":" + payload.candidateSeats.sorted().joinToString(",")
                }
            else -> null
        }

    fun route(current: Scope, cache: PrivateCache?): Route {
        if (current.decision.family == "NIGHT_INFORMATION") {
            val meanings = current.decision.legalCandidates.map {
                visibleMeaning(it.payload)
            }
            if (meanings.all { it != null } && meanings.distinct().size == 1) {
                return Route.HostDetermined(requireNotNull(meanings.first()))
            }
        }
        // Conservative first slice: any Host fact, input, candidate meaning,
        // cursor, position or strategy-version change invalidates private cache.
        // Later unrelated-event retention needs a proved dependency certificate.
        if (cache != null && cache.scope == current &&
            current.decision.legalCandidates.any { it.candidateId == cache.candidateId }) {
            return Route.CacheHit(cache.candidateId)
        }
        return Route.ModelRequired
    }

    /** Return the Host candidate only; never mutate GameState or player observations. */
    fun verifyProposal(
        requested: Scope,
        fresh: Scope,
        model: Proposal,
    ): String {
        require(requested == fresh) { "Host facts or legal candidate semantics changed." }
        require(model.source == requested.source) {
            "Model source must match one current Host game, decision, cursor and strategy version."
        }
        require(requested.decision.legalCandidates.count {
            it.candidateId == model.candidateId
        } == 1) { "Illegal or ambiguous model candidate ID." }
        return model.candidateId
    }
}
