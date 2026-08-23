package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * End-to-end exact historical baseline for one player's knowledge-safe timeline.
 *
 * Setup enumeration remains the seed authority. Durable actions and observations are then merged
 * under GLOBAL_V1 ordering and projected through [PlayerHistoricalTimeline], so storyteller-only
 * Poison/Protect/Attack targets never become player knowledge.
 *
 * Hidden role transitions are persistent mechanical state. Until A3 models their rule-derived
 * successor branching, any durable [ActionFact.RoleChange] makes the historical engine incomplete;
 * this constructor rejects such histories rather than reporting a partial replay as exact.
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
        require(actionTimeline.entries.none { it.fact is ActionFact.RoleChange }) {
            "A3 exact historical baseline does not yet model hidden RoleChange successor worlds; " +
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
