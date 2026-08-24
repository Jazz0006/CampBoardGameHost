package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase

/**
 * End-to-end exact historical baseline for one player's knowledge-safe timeline.
 *
 * Setup enumeration remains the seed authority. Durable observations are stripped from that seed
 * and consumed only through the GLOBAL_V1 historical timeline. Durable actions and observations are
 * merged under GLOBAL_V1 ordering and projected through [PlayerHistoricalTimeline]. Hidden Poison
 * targets are safe to omit from that projection because setup enumeration branches the first-night
 * target and historical replay independently branches every legal later-night Poisoner target; the
 * storyteller-selected target never becomes player knowledge.
 *
 * [validatedRuleset] is the canonical night-order authority. Visible ability observations retain
 * their durable GLOBAL_V1 identities, while each possible world must independently prove that the
 * observed chronology can be anchored to the ruleset's canonical night schedule. Historical replay
 * also uses that schedule only as a knowledge-neutral ordering substrate for rule-derived hidden
 * Other Night mechanics; it never invents hidden GLOBAL_V1 events.
 *
 * Attack, Protect, and RoleChange persisted action facts remain fail-closed. The rule-derived hidden
 * Other Night transition is now replayable without consuming those Storyteller-selected payloads,
 * but guard relaxation is a separate architecture slice.
 */
internal object EnumeratedHistoricalExactBaseline {
    private val SUPPORTED_SCRIPT = ScriptId("trouble_brewing")

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
        require(rulesetRef.scriptId == SUPPORTED_SCRIPT) {
            "A3 historical exact support currently covers Trouble Brewing only; " +
                "refusing to apply Trouble Brewing exact reasoning to script ${rulesetRef.scriptId.value}."
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
            "A3 exact historical baseline does not yet accept persisted hidden $unsupportedHiddenMechanic facts; " +
                "refusing to consume Storyteller-selected hidden payloads before the guard-relaxation slice."
        }

        val historicalSeedKnowledge = setupKnowledge.copy(
            publicObservations = emptyList(),
            privateObservations = emptyList(),
        )
        val initialWorldSet = TroubleBrewingWorldEnumerator.enumerate(
            rulesetRef = rulesetRef,
            knowledge = historicalSeedKnowledge,
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
            knowledge = historicalSeedKnowledge,
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
            validatedRuleset = validatedRuleset,
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
