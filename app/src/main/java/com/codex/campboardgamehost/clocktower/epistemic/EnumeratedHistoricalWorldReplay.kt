package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase

/**
 * Time-aware exact-world snapshot used by the A3 historical baseline.
 *
 * This deliberately does not implement [PlayerWorldSet] yet. Historical transition semantics need
 * to become complete before the production-facing world-set identity/API is allowed to depend on
 * them. The snapshot reuses the existing exact observation evaluator rather than maintaining a
 * second rules implementation.
 */
internal class EnumeratedHistoricalWorldSetSnapshot private constructor(
    private val recipientSeat: Int,
    private val hypothesis: EpistemicHypothesis,
    private val roles: Map<RoleId, RoleDefinition>,
    worlds: List<EnumeratedWorld>,
) {
    private val worlds: List<EnumeratedWorld> = worlds.toList()

    fun isEmpty(): Boolean = worlds.isEmpty()

    internal fun enumeratedWorlds(): List<EnumeratedWorld> = worlds.toList()

    internal fun eliminate(seat: Int): EnumeratedHistoricalWorldSetSnapshot {
        require(seat > 0) { "Historical elimination seat must be positive." }
        require(worlds.all { seat in it.rolesBySeat }) {
            "Historical elimination references unknown seat $seat."
        }
        return copy(worlds.map { world ->
            val abilityStates = if (world.currentRolesBySeat.getValue(seat).value.equals("Poisoner", ignoreCase = true)) {
                world.abilityStatesBySeat.filterValues { it != AbilityState.MALFUNCTIONING_POISONED }
            } else {
                world.abilityStatesBySeat
            }
            world.copy(
                aliveSeats = world.aliveSeats - seat,
                abilityStatesBySeat = abilityStates,
            )
        })
    }

    /**
     * Expires the prior Poisoner choice at dusk, then branches the new hidden choice independently
     * in every exact world where the Poisoner still has their ability. The target is deliberately
     * selected from every player seat, including dead players; no storyteller-selected target is
     * consumed here. A Drunk target keeps the existing collapsed MALFUNCTIONING_DRUNK encoding.
     */
    internal fun beginNight(): EnumeratedHistoricalWorldSetSnapshot = copy(
        worlds.flatMap { world ->
            val expiredAbilityStates = world.abilityStatesBySeat.filterValues {
                it != AbilityState.MALFUNCTIONING_POISONED
            }
            val expiredWorld = world.copy(abilityStatesBySeat = expiredAbilityStates)
            val poisonerSeat = world.currentRolesBySeat.entries.singleOrNull {
                it.value.value.equals("Poisoner", ignoreCase = true)
            }?.key
            if (poisonerSeat == null || poisonerSeat !in world.aliveSeats) {
                listOf(expiredWorld)
            } else {
                world.rolesBySeat.keys.map { targetSeat ->
                    val nextAbilityStates = if (
                        expiredAbilityStates[targetSeat] == AbilityState.MALFUNCTIONING_DRUNK
                    ) {
                        expiredAbilityStates
                    } else {
                        expiredAbilityStates + (targetSeat to AbilityState.MALFUNCTIONING_POISONED)
                    }
                    expiredWorld.copy(abilityStatesBySeat = nextAbilityStates)
                }
            }
        },
    )

    /**
     * Applies the complete rule-derived Trouble Brewing Other Night transition to each current
     * possible world. Triggered Ravenkeeper information or a public night-death fact may prove that
     * one publicly alive seat changed to dead at this Demon step; neither supplies a hidden target.
     */
    internal fun materializeOtherNight(
        triggeredObservation: RecordedEpistemicObservation? = null,
        confirmedPublicDeathSeat: Int? = null,
    ): EnumeratedHistoricalWorldSetSnapshot {
        require(triggeredObservation == null || confirmedPublicDeathSeat == null) {
            "One Other Night transition cannot use two independent death confirmations."
        }
        confirmedPublicDeathSeat?.let { seat ->
            require(seat > 0) { "Confirmed public night-death seat must be positive." }
            require(worlds.all { seat in it.rolesBySeat }) {
                "Confirmed public night death references unknown seat $seat."
            }
        }
        return copy(
            worlds.flatMap { world ->
                val materialized = EnumeratedWorldOtherNightMechanicsMaterializer.materialize(world)
                require(materialized.unresolvedBranches.isEmpty()) {
                    "Historical Other Night replay cannot continue with unresolved rule-derived branches."
                }
                val requiredDeathSeat = when {
                    triggeredObservation.isRavenkeeperNightObservation() ->
                        requireNotNull(triggeredObservation?.sourceSeat) {
                            "Ravenkeeper night observation must identify its source seat."
                        }
                    confirmedPublicDeathSeat != null -> confirmedPublicDeathSeat
                    else -> null
                }
                if (requiredDeathSeat == null) {
                    materialized.resolvedWorlds
                } else if (requiredDeathSeat !in world.aliveSeats) {
                    emptyList()
                } else {
                    materialized.resolvedWorlds.filter { nextWorld ->
                        requiredDeathSeat !in nextWorld.aliveSeats
                    }
                }
            },
        )
    }

    /**
     * Retains the current successor states which are mechanically present in [other]. Explanation
     * clusters are deliberately ignored so later visible observations can keep their provenance while
     * public outcomes constrain only mechanical state.
     */
    internal fun intersectMechanicalStates(
        other: EnumeratedHistoricalWorldSetSnapshot,
    ): EnumeratedHistoricalWorldSetSnapshot {
        require(recipientSeat == other.recipientSeat && hypothesis == other.hypothesis && roles == other.roles) {
            "Historical mechanical-state intersection requires snapshots from the same world set."
        }
        return copy(
            worlds.filter { current ->
                other.worlds.any { candidate -> current.sameMechanicalState(candidate) }
            },
        )
    }

    internal fun require(
        record: RecordedEpistemicObservation,
        formalSnapshotId: String,
    ): EnumeratedHistoricalWorldSetSnapshot {
        require(record.visibility == ObservationVisibility.PUBLIC || recipientSeat in record.recipientSeats) {
            "Historical observation ${record.recordId} is not visible to recipient seat $recipientSeat."
        }
        val observation = record.bindTo(formalSnapshotId)
        val retained = worlds.mapNotNull { world ->
            if (!sourceEligibleAtCurrentState(world, record)) return@mapNotNull null
            val result = TroubleBrewingWorldObservationEvaluator.evaluate(world, roles, observation, hypothesis)
            if (result.matches) world.withClusters(result.clusters) else null
        }
        return copy(retained)
    }

    private fun sourceEligibleAtCurrentState(
        world: EnumeratedWorld,
        record: RecordedEpistemicObservation,
    ): Boolean {
        if (record.reliability == ObservationReliability.NOT_ABILITY_INFORMATION) return true
        if (record.phase != StorytellerPhase.FIRST_NIGHT && record.phase != StorytellerPhase.NIGHT) return true
        val sourceSeat = record.sourceSeat ?: return false
        val sourceAbility = record.sourceAbility ?: return false
        return EnumeratedWorldNightObservationAliveStateCompatibility.isCompatible(
            world = world,
            sourceSeat = sourceSeat,
            sourceAbility = sourceAbility,
        )
    }

    private fun EnumeratedWorld.sameMechanicalState(other: EnumeratedWorld): Boolean =
        rolesBySeat == other.rolesBySeat &&
            currentRolesBySeat == other.currentRolesBySeat &&
            redHerringSeat == other.redHerringSeat &&
            shownRolesBySeat == other.shownRolesBySeat &&
            aliveSeats == other.aliveSeats &&
            abilityStatesBySeat == other.abilityStatesBySeat

    private fun copy(nextWorlds: List<EnumeratedWorld>) = EnumeratedHistoricalWorldSetSnapshot(
        recipientSeat = recipientSeat,
        hypothesis = hypothesis,
        roles = roles,
        worlds = EnumeratedWorldMechanicalConvergence.converge(nextWorlds),
    )

    companion object {
        fun from(initial: EnumeratedWorldSet): EnumeratedHistoricalWorldSetSnapshot =
            EnumeratedHistoricalWorldSetSnapshot(
                recipientSeat = initial.recipientSeat,
                hypothesis = initial.hypothesis,
                roles = initial.roleDefinitions().associateBy(RoleDefinition::id),
                worlds = initial.enumeratedWorlds(),
            )
    }
}

internal data class EnumeratedHistoricalReplayResult(
    val worldSet: EnumeratedHistoricalWorldSetSnapshot,
    val phase: StorytellerPhase,
    val round: Int,
    val lastGlobalSequence: Long?,
)

/**
 * Replays only the knowledge-safe [PlayerHistoricalEvent] projection.
 *
 * Hidden Poison/Protect/Attack/RoleChange truth is intentionally absent from this input type. When
 * [validatedRuleset] is supplied, rule-derived Other Night mechanics are inserted at the canonical
 * Imp boundary relative to visible ability observations, without inventing a durable timeline point.
 */
internal object EnumeratedHistoricalWorldReplay {
    fun replay(
        initialWorldSet: EnumeratedWorldSet,
        formalSnapshotId: String,
        initialPhase: StorytellerPhase,
        initialRound: Int,
        events: List<PlayerHistoricalEvent>,
        validatedRuleset: ValidatedClocktowerRuleset? = null,
    ): EnumeratedHistoricalReplayResult {
        require(formalSnapshotId.isNotBlank()) { "Historical replay formal snapshot ID cannot be blank." }
        require(initialRound > 0) { "Historical replay initial round must be positive." }
        require(events.zipWithNext().all { (left, right) ->
            left.point.globalSequence < right.point.globalSequence
        }) { "Historical replay events must use strictly increasing globalSequence order." }

        var worldSet = EnumeratedHistoricalWorldSetSnapshot.from(initialWorldSet)
        var phase = initialPhase
        var round = initialRound
        var lastGlobalSequence: Long? = null
        var otherNightMechanicsApplied = false
        var otherNightPreMechanicsWorldSet: EnumeratedHistoricalWorldSetSnapshot? = null

        fun materializeOtherNight(
            triggeredObservation: RecordedEpistemicObservation? = null,
            confirmedPublicDeathSeat: Int? = null,
        ) {
            otherNightPreMechanicsWorldSet = worldSet
            worldSet = worldSet.materializeOtherNight(
                triggeredObservation = triggeredObservation,
                confirmedPublicDeathSeat = confirmedPublicDeathSeat,
            )
            otherNightMechanicsApplied = true
        }

        events.forEach { event ->
            when (event) {
                is PlayerHistoricalEvent.PublicExecution -> {
                    worldSet = worldSet.eliminate(event.targetSeat)
                }
                is PlayerHistoricalEvent.PublicDeath -> {
                    if (phase == StorytellerPhase.NIGHT && validatedRuleset != null) {
                        if (!otherNightMechanicsApplied) {
                            materializeOtherNight(confirmedPublicDeathSeat = event.targetSeat)
                        } else {
                            val preMechanics = requireNotNull(otherNightPreMechanicsWorldSet) {
                                "Applied Other Night mechanics must retain their pre-transition snapshot."
                            }
                            val deathCompatible = preMechanics.materializeOtherNight(
                                confirmedPublicDeathSeat = event.targetSeat,
                            )
                            val reconciled = worldSet.intersectMechanicalStates(deathCompatible)
                            worldSet = if (reconciled.isEmpty()) {
                                worldSet.eliminate(event.targetSeat)
                            } else {
                                reconciled
                            }
                        }
                    } else {
                        worldSet = worldSet.eliminate(event.targetSeat)
                    }
                }
                is PlayerHistoricalEvent.PhaseAdvance -> {
                    require(event.round > 0) { "Historical phase advance round must be positive." }
                    if (
                        phase == StorytellerPhase.NIGHT &&
                        event.phase == StorytellerPhase.DAY &&
                        !otherNightMechanicsApplied &&
                        validatedRuleset != null
                    ) {
                        materializeOtherNight()
                    }
                    if (phase == StorytellerPhase.DAY && event.phase == StorytellerPhase.NIGHT) {
                        worldSet = worldSet.beginNight()
                        otherNightMechanicsApplied = false
                        otherNightPreMechanicsWorldSet = null
                    }
                    phase = event.phase
                    round = event.round
                }
                is PlayerHistoricalEvent.Observation -> {
                    val recordPoint = (event.record.timelineBinding as? ObservationTimelineBinding.Global)?.point
                        ?: throw IllegalArgumentException(
                            "Exact historical replay requires globally bound observations.",
                        )
                    require(recordPoint == event.point) {
                        "Historical observation event point must match its durable record binding."
                    }
                    if (
                        phase == StorytellerPhase.NIGHT &&
                        !otherNightMechanicsApplied &&
                        validatedRuleset != null &&
                        occursAfterImp(
                            ruleset = validatedRuleset,
                            worldSet = worldSet,
                            record = event.record,
                        )
                    ) {
                        materializeOtherNight(
                            triggeredObservation = event.record.takeIf {
                                it.isRavenkeeperNightObservation()
                            },
                        )
                    }
                    worldSet = worldSet.require(event.record, formalSnapshotId)
                }
            }
            lastGlobalSequence = event.point.globalSequence
        }

        return EnumeratedHistoricalReplayResult(
            worldSet = worldSet,
            phase = phase,
            round = round,
            lastGlobalSequence = lastGlobalSequence,
        )
    }

    private fun occursAfterImp(
        ruleset: ValidatedClocktowerRuleset,
        worldSet: EnumeratedHistoricalWorldSetSnapshot,
        record: RecordedEpistemicObservation,
    ): Boolean {
        if (record.reliability == ObservationReliability.NOT_ABILITY_INFORMATION) return false
        if (record.phase != StorytellerPhase.NIGHT) return false

        val placements = worldSet.enumeratedWorlds().mapNotNull { world ->
            val anchor = EnumeratedWorldNightObservationAnchoring.anchorOrNull(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
                record = record,
            ) ?: return@mapNotNull null
            val schedule = EnumeratedWorldNightSchedule.plan(
                ruleset = ruleset,
                phase = ClocktowerNightFlowPhase.OTHER_NIGHT,
                world = world,
            )
            val impIndex = schedule.indexOf(NightOrderToken.Character(RoleId("Imp")))
            if (impIndex < 0) return@mapNotNull null
            anchor.scheduleIndex > impIndex
        }.toSet()

        require(placements.size <= 1) {
            "The same durable observation cannot occur on both sides of the canonical Imp boundary."
        }
        return placements.singleOrNull() == true
    }
}

private fun RecordedEpistemicObservation?.isRavenkeeperNightObservation(): Boolean =
    this != null &&
        phase == StorytellerPhase.NIGHT &&
        reliability != ObservationReliability.NOT_ABILITY_INFORMATION &&
        sourceAbility?.value.equals("Ravenkeeper", ignoreCase = true)