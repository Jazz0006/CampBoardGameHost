package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.DecisionEvaluation
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle

internal data class FamilyProbabilityBudget(
    val massByFamily: Map<String, Long>,
) {
    init {
        require(massByFamily.isNotEmpty()) { "At least one family probability mass is required." }
        require(massByFamily.keys.all { it.isNotBlank() }) { "Family IDs cannot be blank." }
        require(massByFamily.values.all { it >= 0 }) { "Family probability mass cannot be negative." }
        require(massByFamily.values.any { it > 0 }) { "At least one family must have positive mass." }
    }
}

internal data class WeightedSelection<T>(
    val selected: DecisionEvaluation<T>,
    val finalProbabilityByCandidate: Map<String, Long>,
    val decisionSeed: Long,
)

internal object WeightedStableSelector {
    const val FIXED_POINT_SCALE = 1_000_000L

    fun <T> selectPreferred(
        options: List<T>,
        isPreferred: (T) -> Boolean,
    ): T? = options.firstOrNull(isPreferred) ?: options.firstOrNull()

    fun <T> selectStyle(
        options: List<T>,
        style: RecommendationStyle,
        styleOf: (T) -> RecommendationStyle,
    ): T? = options.firstOrNull { styleOf(it) == style }
        ?: options.firstOrNull { styleOf(it) == RecommendationStyle.BALANCED }
        ?: options.firstOrNull()

    fun <T> select(
        pool: List<DecisionEvaluation<T>>,
        familyBudget: FamilyProbabilityBudget,
        decisionSeed: Long,
    ): WeightedSelection<T>? {
        if (pool.isEmpty()) return null
        require(pool.map { it.candidate.candidateId }.distinct().size == pool.size) {
            "candidateId must be unique within one selection request."
        }
        require(pool.all { it.withinFamilyWeightFixedPoint > 0 }) {
            "Every pooled candidate must have a positive within-family weight."
        }

        val candidatesByFamily = pool
            .groupBy { it.candidate.candidateFamilyId }
            .toSortedMap()
        require(candidatesByFamily.keys.all { (familyBudget.massByFamily[it] ?: 0L) > 0L }) {
            "Every pooled candidate family must have positive configured probability mass."
        }

        val activeFamilyMass = proportionalAllocation(
            weightedIds = candidatesByFamily.keys.map { familyId ->
                WeightedId(familyId, familyBudget.massByFamily.getValue(familyId))
            },
            total = FIXED_POINT_SCALE,
        )
        val finalWeights = linkedMapOf<String, Long>()
        candidatesByFamily.forEach { (familyId, familyCandidates) ->
            finalWeights.putAll(
                proportionalAllocation(
                    weightedIds = familyCandidates.map {
                        WeightedId(it.candidate.candidateId, it.withinFamilyWeightFixedPoint)
                    },
                    total = activeFamilyMass.getValue(familyId),
                ),
            )
        }
        check(finalWeights.values.sum() == FIXED_POINT_SCALE) {
            "Final candidate probability must equal the fixed-point scale."
        }

        val canonicalPool = pool.sortedBy { it.candidate.candidateId }
        val ticket = java.lang.Long.remainderUnsigned(decisionSeed, FIXED_POINT_SCALE)
        var cumulative = 0L
        val chosen = canonicalPool.first { evaluation ->
            cumulative += finalWeights.getValue(evaluation.candidate.candidateId)
            ticket < cumulative
        }
        val selectedWithProbability = chosen.copy(
            finalProbabilityFixedPoint = finalWeights.getValue(chosen.candidate.candidateId),
        )
        return WeightedSelection(
            selected = selectedWithProbability,
            finalProbabilityByCandidate = finalWeights.toSortedMap(),
            decisionSeed = decisionSeed,
        )
    }

    private data class WeightedId(
        val id: String,
        val weight: Long,
    )

    private data class AllocationRemainder(
        val id: String,
        val remainder: Long,
    )

    private fun proportionalAllocation(
        weightedIds: List<WeightedId>,
        total: Long,
    ): Map<String, Long> {
        require(weightedIds.isNotEmpty()) { "Cannot allocate an empty weight set." }
        require(weightedIds.all { it.weight > 0 }) { "Allocation weights must be positive." }
        require(total > 0) { "Allocation total must be positive." }
        val weightSum = weightedIds.fold(0L) { sum, weighted -> Math.addExact(sum, weighted.weight) }

        val allocation = weightedIds.associate { weighted ->
            weighted.id to Math.multiplyExact(total, weighted.weight) / weightSum
        }.toMutableMap()
        val allocated = allocation.values.sum()
        val remaining = total - allocated
        val remainderOrder = weightedIds
            .map { weighted ->
                AllocationRemainder(
                    id = weighted.id,
                    remainder = Math.multiplyExact(total, weighted.weight) % weightSum,
                )
            }
            .sortedWith(compareByDescending<AllocationRemainder> { it.remainder }.thenBy { it.id })
        repeat(remaining.toInt()) { index ->
            val id = remainderOrder[index].id
            allocation[id] = allocation.getValue(id) + 1L
        }
        return allocation.toSortedMap()
    }
}
