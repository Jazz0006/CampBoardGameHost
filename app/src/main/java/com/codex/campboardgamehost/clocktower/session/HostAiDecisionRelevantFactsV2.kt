package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1

/**
 * AI-EVENT-JIT-1D: a deliberately SMALL view of already committed Host history.
 *
 * The Host still stores its ordinary nominations/votes for rules, display and
 * recovery. They simply are not sent as individual strategic updates in P2.
 * This is not a new ledger, writer or source of GameState.
 *
 * New action variants must be classified explicitly (sealed exhaustive when)
 * rather than silently omitted from a model continuation.
 */
internal object HostAiDecisionRelevantFactsV2 {
    fun mattersToCurrentStrategy(entry: StorytellerProviderHistoryEntryV1): Boolean =
        when (entry) {
            is StorytellerProviderHistoryEntryV1.Observation -> true
            is StorytellerProviderHistoryEntryV1.Action -> when (val fact = entry.fact) {
                is ActionFact.Vote -> false
                is ActionFact.Nomination -> fact.firstVirginNomination
                // A phase transition is part of current causal context, not a UI click.
                is ActionFact.PhaseAdvance -> true
                is ActionFact.Poison,
                is ActionFact.Protect,
                is ActionFact.Attack,
                is ActionFact.Execution,
                is ActionFact.Death,
                is ActionFact.NoExecution,
                is ActionFact.SlayerShot,
                is ActionFact.KlutzLearnedDeath,
                is ActionFact.KlutzChoice,
                is ActionFact.RoleChange -> true
            }
        }

    /**
     * Preserve original global sequence identities. Gaps are permitted here
     * because omitted votes remain in the HOST timeline; they must NOT be
     * re-numbered. Completeness / full-snapshot fallback is decided elsewhere.
     */
    fun project(delta: HostCausalEventDeltaV2.Delta): List<StorytellerProviderHistoryEntryV1> =
        delta.newlyLoggedFacts.filter(::mattersToCurrentStrategy)
}
