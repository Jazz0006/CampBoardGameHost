package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1

/**
 * AI-EVENT-JIT-1D: a deliberately SMALL view of already committed Host history.
 *
 * Real nominations and per-seat votes are strategic context and MUST remain
 * in the ordered AI event suffix. Host keeps using the existing timeline;
 * this projection neither invents facts nor creates a parallel GameState.
 *
 * New action variants must be classified explicitly (sealed exhaustive when)
 * rather than silently omitted from a model continuation.
 */
internal object HostAiDecisionRelevantFactsV2 {
    fun mattersToCurrentStrategy(entry: StorytellerProviderHistoryEntryV1): Boolean =
        when (entry) {
            is StorytellerProviderHistoryEntryV1.Observation -> true
            is StorytellerProviderHistoryEntryV1.Action -> when (val fact = entry.fact) {
                // All confirmed voting groups matter: never erase ordinary
                // nominations/voters merely because no ability was triggered.
                is ActionFact.Vote,
                is ActionFact.Nomination -> true
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
     * Preserve original global sequence identities. Missing positions are not
     * reconstructed; completeness / full-snapshot fallback is handled elsewhere.
     */
    fun project(delta: HostCausalEventDeltaV2.Delta): List<StorytellerProviderHistoryEntryV1> =
        delta.newlyLoggedFacts.filter(::mattersToCurrentStrategy)
}
