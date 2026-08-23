package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * End-to-end exact historical baseline for one player's knowledge-safe timeline.
 *
 * Setup enumeration remains the seed authority. Durable actions and observations are then merged
 * under GLOBAL_V1 ordering and projected through [PlayerHistoricalTimeline]. Hidden Poison targets
 * are safe to omit from that projection because the replay independently branches every legal
 * Poisoner target; the storyteller-selected target never becomes player knowledge.
 *
 * Attack, Protect, and RoleChange can alter persistent or same-night mechanical state, but their
 * rule-derived hidden successor branching is not modeled yet. Histories containing those actions
 * are rejected rather than allowing a knowledge-safe projection to be mistaken for an exact replay.
 */
internal object EnumeratedHistoricalExactBaseline {
    fun build(
        rulesetRef: RulesetRef,
        setupKnowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
        initialPhase: StorytellerPhase,
        initialRound: Int,
        actionTimeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): EnumeratedHistoricalReplayResult {
        actionTimeline.requireCompatibleWith(observationLog)
        val unsupportedHiddenMechanic = actionTimeline.entries.firstNotNullOfOrNull { entry ->
            when (entry.fact) {
                is ActionFact.Attack -> "Attack"
                is ActionFact.Protect -> "Protect"
                is ActionFact.RoleChange -> "RoleChange"
                else -> null
            }
        }
        require(unsupportedHiddenMechanic == null) {
            "A3 exact historical baseline does not yet model hidden $unsupportedHiddenMechanic successor worlds; " +
                "refusing to report a partial replay as exact."
        }

        val initialWorldSet = TroubleBrewingWorldEnumerator.enumerate(
            rulesetRef = rulesetRef,
            knowledge = setupKnowledge,
            hypothesis = hypothesis,
            roleDefinitions = roleDefinitions,
        )
        val events = PlayerHistoricalTimeline.project(
            recipientSeat = setupKnowledge.recipientSeat,
            actionTimeline = actionTimeline,
            observationLog = observationLog,
        )
        return EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = initialWorldSet,
            formalSnapshotId = setupKnowledge.formalSnapshotId,
            initialPhase = initialPhase,
            initialRound = initialRound,
            events = events,
        )
    }
}
