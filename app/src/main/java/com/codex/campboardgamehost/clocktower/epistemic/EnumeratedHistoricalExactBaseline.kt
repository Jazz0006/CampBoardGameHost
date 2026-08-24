package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase

/**
 * End-to-end exact historical baseline for one player's knowledge-safe timeline.
 *
 * Setup enumeration remains the seed authority. Durable actions and observations are then merged
 * under GLOBAL_V1 ordering and projected through [PlayerHistoricalTimeline]. Hidden Poison targets
 * are safe to omit from that projection because setup enumeration branches the first-night target
 * and historical replay independently branches every legal later-night Poisoner target; the
 * storyteller-selected target never becomes player knowledge.
 *
 * [validatedRuleset] is the canonical night-order authority. Visible ability observations retain
 * their durable GLOBAL_V1 identities, while each possible world must independently prove that the
 * observed chronology can be anchored to the ruleset's canonical night schedule.
 *
 * Attack, Protect, and RoleChange can alter persistent or same-night mechanical state, but their
 * rule-derived hidden successor branching is not modeled yet. Histories containing those actions
 * are rejected rather than allowing a knowledge-safe projection to be mistaken for an exact replay.
 */
internal object EnumeratedHistoricalExactBaseline {
    fun build(
        validatedRuleset: ValidatedClocktowerRuleset,
        rulesetRef: RulesetRef,
        setupKnowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
        initialPhase: StorytellerPhase,
        initialRound: Int,
        actionTimeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): EnumeratedHistoricalReplayResult {
        require(validatedRuleset.script.id == rulesetRef.scriptId) {
            "Validated ruleset script does not match exact-baseline RulesetRef."
        }
        require(validatedRuleset.script.contentHash == rulesetRef.scriptContentHash) {
            "Validated ruleset content hash does not match exact-baseline RulesetRef."
        }
        require(validatedRuleset.coverage == rulesetRef.coverage) {
            "Validated ruleset coverage does not match exact-baseline RulesetRef."
        }
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
        val chronologyCompatibleWorlds = retainNightChronologyCompatibleWorlds(
            validatedRuleset = validatedRuleset,
            worlds = initialWorldSet.enumeratedWorlds(),
            events = events,
        )
        val chronologyCompatibleWorldSet = EnumeratedWorldSet.fromWorlds(
            rulesetRef = rulesetRef,
            knowledge = setupKnowledge,
            hypothesis = hypothesis,
            roleDefinitions = roleDefinitions,
            worlds = chronologyCompatibleWorlds,
        )
        return EnumeratedHistoricalWorldReplay.replay(
            initialWorldSet = chronologyCompatibleWorldSet,
            formalSnapshotId = setupKnowledge.formalSnapshotId,
            initialPhase = initialPhase,
            initialRound = initialRound,
            events = events,
        )
    }

    private fun retainNightChronologyCompatibleWorlds(
        validatedRuleset: ValidatedClocktowerRuleset,
        worlds: List<EnumeratedWorld>,
        events: List<PlayerHistoricalEvent>,
    ): List<EnumeratedWorld> {
        val nightObservationGroups = events.mapNotNull { event ->
            val record = (event as? PlayerHistoricalEvent.Observation)?.record ?: return@mapNotNull null
            if (record.reliability == ObservationReliability.NOT_ABILITY_INFORMATION) return@mapNotNull null
            val flowPhase = when (record.phase) {
                StorytellerPhase.FIRST_NIGHT -> ClocktowerNightFlowPhase.FIRST_NIGHT
                StorytellerPhase.NIGHT -> ClocktowerNightFlowPhase.OTHER_NIGHT
                else -> return@mapNotNull null
            }
            Triple(flowPhase, record.round, record)
        }.groupBy(
            keySelector = { (flowPhase, round, _) -> flowPhase to round },
            valueTransform = { (_, _, record) -> record },
        )

        if (nightObservationGroups.isEmpty()) return worlds

        return worlds.filter { world ->
            nightObservationGroups.all { (night, observations) ->
                EnumeratedWorldNightReplayPlanning.planAbilityObservationsOrNull(
                    ruleset = validatedRuleset,
                    phase = night.first,
                    world = world,
                    observations = observations,
                ) != null
            }
        }
    }
}
