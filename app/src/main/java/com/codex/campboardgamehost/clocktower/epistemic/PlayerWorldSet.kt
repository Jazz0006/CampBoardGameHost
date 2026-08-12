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
}

data class ExplanationClusterSummary(
    val worldCountByCluster: Map<WorldExplanationClusterId, WorldCardinality>,
) {
    init {
        require(worldCountByCluster.keys.none { it.value.isBlank() })
    }

    val clusterCount: Int get() = worldCountByCluster.size
}
