package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

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
            val abilityStates = if (world.rolesBySeat.getValue(seat).value.equals("Poisoner", ignoreCase = true)) {
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
            val poisonerSeat = world.rolesBySeat.entries.singleOrNull {
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

    internal fun require(
        record: RecordedEpistemicObservation,
        formalSnapshotId: String,
    ): EnumeratedHistoricalWorldSetSnapshot {
        require(record.visibility == ObservationVisibility.PUBLIC || recipientSeat in record.recipientSeats) {
            "Historical observation ${record.recordId} is not visible to recipient seat $recipientSeat."
        }
        val observation = record.bindTo(formalSnapshotId)
        val retained = worlds.mapNotNull { world ->
            val result = TroubleBrewingWorldObservationEvaluator.evaluate(world, roles, observation, hypothesis)
            if (result.matches) world.withClusters(result.clusters) else null
        }
        return copy(retained)
    }

    private fun copy(nextWorlds: List<EnumeratedWorld>) = EnumeratedHistoricalWorldSetSnapshot(
        recipientSeat = recipientSeat,
        hypothesis = hypothesis,
        roles = roles,
        worlds = nextWorlds,
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
 * Hidden Poison/Protect/Attack/RoleChange truth is intentionally absent from this input type. Later
 * A3 slices may branch hidden successor worlds from public rule constraints, but must never consume
 * the Storyteller's actual hidden target as player knowledge.
 */
internal object EnumeratedHistoricalWorldReplay {
    fun replay(
        initialWorldSet: EnumeratedWorldSet,
        formalSnapshotId: String,
        initialPhase: StorytellerPhase,
        initialRound: Int,
        events: List<PlayerHistoricalEvent>,
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

        events.forEach { event ->
            when (event) {
                is PlayerHistoricalEvent.PublicExecution -> {
                    worldSet = worldSet.eliminate(event.targetSeat)
                }
                is PlayerHistoricalEvent.PublicDeath -> {
                    worldSet = worldSet.eliminate(event.targetSeat)
                }
                is PlayerHistoricalEvent.PhaseAdvance -> {
                    require(event.round > 0) { "Historical phase advance round must be positive." }
                    if (phase == StorytellerPhase.DAY && event.phase == StorytellerPhase.NIGHT) {
                        worldSet = worldSet.beginNight()
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
}
