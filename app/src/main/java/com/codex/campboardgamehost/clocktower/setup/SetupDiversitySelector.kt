package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.RoleId

enum class SetupExactRepeatPolicy {
    ALLOW,
    REJECT_WHEN_ALTERNATIVE,
    REJECT,
}

internal data class SetupDiversityPolicy(
    val historyWeights: List<Long> = listOf(100L, 65L, 40L, 20L, 10L),
    val exactRepeatPolicy: SetupExactRepeatPolicy = SetupExactRepeatPolicy.REJECT_WHEN_ALTERNATIVE,
    val lastGameMaxOverlapFixedPoint: Long = SetupDiversityScorer.FIXED_POINT_SCALE,
    val overlapFallbackStepFixedPoint: Long = 50_000L,
    val minimumNoveltyWeightFixedPoint: Long = 200_000L,
) {
    init {
        require(historyWeights.isNotEmpty()) { "Setup diversity history weights cannot be empty." }
        require(historyWeights.all { it >= 0L } && historyWeights.any { it > 0L }) {
            "Setup diversity history weights must be non-negative with positive total weight."
        }
        require(lastGameMaxOverlapFixedPoint in 0L..SetupDiversityScorer.FIXED_POINT_SCALE) {
            "Setup diversity last-game max overlap must be within fixed-point range."
        }
        require(overlapFallbackStepFixedPoint in 1L..SetupDiversityScorer.FIXED_POINT_SCALE) {
            "Setup diversity overlap fallback step must be positive and within fixed-point range."
        }
        require(minimumNoveltyWeightFixedPoint in 1L..SetupDiversityScorer.FIXED_POINT_SCALE) {
            "Setup diversity minimum novelty weight must be positive and within fixed-point range."
        }
    }
}

internal data class SetupDiversityScore(
    val weightedOverlapFixedPoint: Long,
    val noveltyWeightFixedPoint: Long,
)

internal object SetupDiversityScorer {
    const val FIXED_POINT_SCALE: Long = 1_000_000L

    fun score(
        candidate: SetupCandidate,
        candidatePool: List<SetupCandidate>,
        history: SetupDiversityHistory,
        policy: SetupDiversityPolicy,
    ): SetupDiversityScore {
        validateCandidatePool(candidatePool)
        require(candidate in candidatePool) {
            "Setup diversity candidate must belong to the candidate pool being scored."
        }

        val invariantRoleCounts = invariantRoleCounts(candidatePool)
        val relevantHistory = relevantHistory(candidate, history, policy.historyWeights.size)
        if (relevantHistory.isEmpty()) {
            return SetupDiversityScore(
                weightedOverlapFixedPoint = 0L,
                noveltyWeightFixedPoint = FIXED_POINT_SCALE,
            )
        }

        val availableWeights = policy.historyWeights.take(relevantHistory.size)
        val denominator = availableWeights.fold(0L, Math::addExact)
        require(denominator > 0L) {
            "Setup diversity available history weights must have positive total weight."
        }
        val numerator = relevantHistory.indices.fold(0L) { total, age ->
            val overlap = overlapFixedPoint(
                candidateRoles = candidate.actualRoles,
                previousRoles = relevantHistory[age].actualRoles,
                invariantRoleCounts = invariantRoleCounts,
            )
            Math.addExact(total, Math.multiplyExact(overlap, availableWeights[age]))
        }
        val weightedOverlap = numerator / denominator
        val noveltyWeight = maxOf(
            policy.minimumNoveltyWeightFixedPoint,
            FIXED_POINT_SCALE - weightedOverlap,
        )
        return SetupDiversityScore(
            weightedOverlapFixedPoint = weightedOverlap,
            noveltyWeightFixedPoint = noveltyWeight,
        )
    }

    internal fun overlapFixedPoint(
        candidateRoles: List<RoleId>,
        previousRoles: List<RoleId>,
        invariantRoleCounts: Map<RoleId, Int>,
    ): Long {
        val candidateVariableRoles = subtractInvariant(candidateRoles, invariantRoleCounts)
        if (candidateVariableRoles.isEmpty()) return 0L
        val previousVariableRoles = subtractInvariant(previousRoles, invariantRoleCounts)
        val previousCounts = previousVariableRoles.groupingBy { it }.eachCount().toMutableMap()
        var overlapCount = 0
        candidateVariableRoles.forEach { role ->
            val remaining = previousCounts[role] ?: 0
            if (remaining > 0) {
                overlapCount += 1
                previousCounts[role] = remaining - 1
            }
        }
        return overlapCount.toLong() * FIXED_POINT_SCALE / candidateVariableRoles.size.toLong()
    }

    internal fun invariantRoleCounts(candidatePool: List<SetupCandidate>): Map<RoleId, Int> {
        validateCandidatePool(candidatePool)
        val allRoles = candidatePool.flatMap(SetupCandidate::actualRoles).distinct()
        return allRoles.mapNotNull { role ->
            val minimumCount = candidatePool.minOf { candidate -> candidate.actualRoles.count { it == role } }
            role.takeIf { minimumCount > 0 }?.let { it to minimumCount }
        }.toMap()
    }

    private fun subtractInvariant(
        roles: List<RoleId>,
        invariantRoleCounts: Map<RoleId, Int>,
    ): List<RoleId> {
        val remainingInvariant = invariantRoleCounts.toMutableMap()
        return roles.filter { role ->
            val remaining = remainingInvariant[role] ?: 0
            if (remaining > 0) {
                remainingInvariant[role] = remaining - 1
                false
            } else {
                true
            }
        }
    }

    private fun relevantHistory(
        candidate: SetupCandidate,
        history: SetupDiversityHistory,
        limit: Int,
    ): List<SetupDiversityRecord> = history.recentSetups.asSequence()
        .filter { it.script == candidate.script && it.playerCount == candidate.playerCount }
        .take(limit)
        .toList()

    internal fun validateCandidatePool(candidatePool: List<SetupCandidate>) {
        require(candidatePool.isNotEmpty()) { "Setup diversity candidate pool cannot be empty." }
        val script = candidatePool.first().script
        val playerCount = candidatePool.first().playerCount
        require(candidatePool.all { it.script == script }) {
            "Setup diversity candidate pool cannot mix scripts."
        }
        require(candidatePool.all { it.playerCount == playerCount }) {
            "Setup diversity candidate pool cannot mix player counts."
        }
    }
}

internal class SetupDiversitySelector(
    private val policy: SetupDiversityPolicy = SetupDiversityPolicy(),
) {
    fun select(
        candidates: List<SetupCandidate>,
        history: SetupDiversityHistory,
        selectionSeed: Long,
    ): SetupCandidate {
        SetupDiversityScorer.validateCandidatePool(candidates)
        val canonicalPool = candidates.sortedBy(::stableCandidateKey)
        require(canonicalPool.map(::stableCandidateKey).distinct().size == canonicalPool.size) {
            "Setup diversity candidate identities must be unique within a selection pool."
        }

        val invariantRoleCounts = SetupDiversityScorer.invariantRoleCounts(canonicalPool)
        val previous = history.recentSetups.firstOrNull {
            it.script == canonicalPool.first().script && it.playerCount == canonicalPool.first().playerCount
        }
        val exactRepeatEligible = exactRepeatEligible(canonicalPool, previous)
        val overlapEligible = overlapEligible(
            candidates = exactRepeatEligible,
            previous = previous,
            invariantRoleCounts = invariantRoleCounts,
        )
        require(overlapEligible.isNotEmpty()) {
            "Setup diversity selection has no eligible candidate."
        }

        if (overlapEligible.size == 1) return overlapEligible.single()

        val weightedCandidates = overlapEligible.map { candidate ->
            val score = SetupDiversityScorer.score(
                candidate = candidate,
                candidatePool = canonicalPool,
                history = history,
                policy = policy,
            )
            WeightedCandidate(candidate, score.noveltyWeightFixedPoint)
        }
        val totalWeight = weightedCandidates.fold(0L) { total, candidate ->
            Math.addExact(total, candidate.weight)
        }
        require(totalWeight > 0L) { "Setup diversity candidate weights must have positive total weight." }

        val script = canonicalPool.first().script
        val playerCount = canonicalPool.first().playerCount
        val drawSeed = MurmurHash3.low64Utf8(
            "setup-diversity-v1|${script.value}|$playerCount|$selectionSeed",
        )
        val draw = java.lang.Long.remainderUnsigned(drawSeed, totalWeight)
        var cumulative = 0L
        weightedCandidates.forEach { candidate ->
            cumulative = Math.addExact(cumulative, candidate.weight)
            if (draw < cumulative) return candidate.candidate
        }
        error("Setup diversity weighted selection did not resolve a candidate.")
    }

    private fun exactRepeatEligible(
        candidates: List<SetupCandidate>,
        previous: SetupDiversityRecord?,
    ): List<SetupCandidate> {
        if (previous == null || policy.exactRepeatPolicy == SetupExactRepeatPolicy.ALLOW) return candidates

        val nonRepeats = candidates.filterNot { it.actualRoles == previous.actualRoles }
        return when (policy.exactRepeatPolicy) {
            SetupExactRepeatPolicy.ALLOW -> candidates
            SetupExactRepeatPolicy.REJECT_WHEN_ALTERNATIVE -> nonRepeats.ifEmpty { candidates }
            SetupExactRepeatPolicy.REJECT -> {
                require(nonRepeats.isNotEmpty()) {
                    "No setup candidate remains after strict exact-repeat rejection."
                }
                nonRepeats
            }
        }
    }

    private fun overlapEligible(
        candidates: List<SetupCandidate>,
        previous: SetupDiversityRecord?,
        invariantRoleCounts: Map<RoleId, Int>,
    ): List<SetupCandidate> {
        if (previous == null || policy.lastGameMaxOverlapFixedPoint >= SetupDiversityScorer.FIXED_POINT_SCALE) {
            return candidates
        }

        var maxOverlap = policy.lastGameMaxOverlapFixedPoint
        while (true) {
            val eligible = candidates.filter { candidate ->
                SetupDiversityScorer.overlapFixedPoint(
                    candidateRoles = candidate.actualRoles,
                    previousRoles = previous.actualRoles,
                    invariantRoleCounts = invariantRoleCounts,
                ) <= maxOverlap
            }
            if (eligible.isNotEmpty()) return eligible
            if (maxOverlap >= SetupDiversityScorer.FIXED_POINT_SCALE) return emptyList()
            maxOverlap = minOf(
                SetupDiversityScorer.FIXED_POINT_SCALE,
                Math.addExact(maxOverlap, policy.overlapFallbackStepFixedPoint),
            )
        }
    }

    private fun stableCandidateKey(candidate: SetupCandidate): String = listOf(
        candidate.provenance.sourceKind.name,
        candidate.provenance.providerId,
        candidate.provenance.candidateId.orEmpty(),
        candidate.actualRoles.joinToString(",") { it.value },
    ).joinToString("|")

    private data class WeightedCandidate(
        val candidate: SetupCandidate,
        val weight: Long,
    )
}
