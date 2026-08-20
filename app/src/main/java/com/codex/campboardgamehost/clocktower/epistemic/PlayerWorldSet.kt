package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId

/**
 * Player-perspective possible-world contract. Implementations may use enumeration, ZDDs, or a
 * solver, but callers must not depend on their representation.
 */
interface PlayerWorldSet {
    val recipientSeat: Int
    val knowledgeSnapshotId: String
    val hypothesis: EpistemicHypothesis
    val identity: PlayerWorldSetIdentity

    fun isEmpty(): Boolean
    fun cardinality(): WorldCardinality
    fun require(observation: EpistemicObservation): PlayerWorldSet
    fun exclude(observation: EpistemicObservation): PlayerWorldSet
    fun possibleRoles(seat: Int): Set<RoleId>
    fun possibleDemonSeats(): Set<Int>
    fun possibleMinionSeats(): Set<Int>
    fun roleWorldCount(seat: Int, role: RoleId): WorldCardinality
    fun demonWorldCount(seat: Int): WorldCardinality
    fun explanationClusters(): ExplanationClusterSummary

    /** Immutable checkpoint. Restoring returns the exact retained implementation instance. */
    fun checkpoint(): PlayerWorldSetCheckpoint = PlayerWorldSetCheckpoint.create(this)

    fun restore(checkpoint: PlayerWorldSetCheckpoint): PlayerWorldSet = checkpoint.restoreFor(this)

    /**
     * Returns the propositions belonging to candidate observations which preserve at least one
     * world. Callers supply the finite legal output domain; the world engine supplies exact SAT.
     */
    fun possibleValues(candidateObservations: Collection<EpistemicObservation>): Set<InformationProposition> {
        require(candidateObservations.isNotEmpty()) { "possibleValues requires at least one candidate observation." }
        require(candidateObservations.all { observation ->
            observation.visibility == ObservationVisibility.PUBLIC || recipientSeat in observation.recipientSeats
        }) { "Every candidate observation must be visible to the world-set recipient." }
        return candidateObservations.mapNotNullTo(linkedSetOf()) { observation ->
            observation.proposition.takeUnless { require(observation).isEmpty() }
        }
    }
}

class PlayerWorldSetCheckpoint private constructor(
    val identity: PlayerWorldSetIdentity,
    private val retained: PlayerWorldSet,
) {
    internal fun restoreFor(current: PlayerWorldSet): PlayerWorldSet {
        require(current.identity == identity) { "Cannot restore a checkpoint from another player-world identity." }
        return retained
    }

    companion object {
        internal fun create(value: PlayerWorldSet) = PlayerWorldSetCheckpoint(value.identity, value)
    }
}

data class ExplanationClusterSummary(
    val worldCountByCluster: Map<WorldExplanationClusterId, WorldCardinality>,
) {
    init {
        require(worldCountByCluster.keys.none { it.value.isBlank() })
    }

    val clusterCount: Int get() = worldCountByCluster.size
}
