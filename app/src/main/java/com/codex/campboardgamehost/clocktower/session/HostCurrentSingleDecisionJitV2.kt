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

    /**
     * A cached UNPUBLISHED choice survives exclusively routine actions only
     * when the Host can prove no other change. This never promotes an old
     * provider response ID, stale proposal or unaccepted strategy memory.
     *
     * Revisions, candidate domain, visible meaning, GameState, player context,
     * decision archive, memory and phase must be unchanged. Additionally the
     * action/observation suffix must account for EVERY consumed global slot.
     * If any condition is uncertain, require new strategic review.
     */
    private fun onlyProvenRoutineHistoryChanged(previous: Scope, current: Scope): Boolean {
        if (previous.phase != current.phase || previous.round != current.round ||
            previous.decision != current.decision ||
            previous.memoryVersion != current.memoryVersion) return false
        val old = previous.frozen
        val now = current.frozen
        if (old.gameId != now.gameId || old.gameSeed != now.gameSeed ||
            old.gameStateRevision != now.gameStateRevision ||
            old.playerInputRevision != now.playerInputRevision ||
            old.gameState != now.gameState ||
            old.decisionHistory != now.decisionHistory ||
            old.storytellerPlayerContextBySeat != now.storytellerPlayerContextBySeat ||
            old.crossGameHistory != now.crossGameHistory ||
            now.nextTimelineGlobalSequence <= old.nextTimelineGlobalSequence) return false

        val delta = runCatching { HostCausalEventDeltaV2.since(old, now) }.getOrNull()
            ?: return false
        if (!delta.noDetectedSnapshotFallback || delta.newlyLoggedFacts.isEmpty() ||
            delta.newlyAppendedDecisions.isNotEmpty() ||
            delta.newlyAppendedCorrections.isNotEmpty()) return false
        // Do not silently ignore allocated but unrecorded global cursor positions.
        if (delta.newlyLoggedFacts.size.toLong() !=
            now.nextTimelineGlobalSequence - old.nextTimelineGlobalSequence) return false
        if (delta.newlyLoggedFacts.withIndex().any { (index, item) ->
                item.point.globalSequence != old.nextTimelineGlobalSequence + index
            }) return false
        return HostAiDecisionRelevantFactsV2.project(delta).isEmpty()
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
        // Exact replay or PROVEN routine, unrelated Host history only.
        // This does not bypass the fresh legal-candidate/meaning check.
        if (cache != null &&
            current.decision.legalCandidates.any { it.candidateId == cache.candidateId } &&
            (cache.scope == current ||
                onlyProvenRoutineHistoryChanged(cache.scope, current))) {
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
