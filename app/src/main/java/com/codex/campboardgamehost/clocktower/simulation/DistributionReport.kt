package com.codex.campboardgamehost.clocktower.simulation

import kotlin.math.ln
import kotlin.math.sqrt

internal data class DistributionBucket(
    val candidateId: String,
    val count: Int,
    val targetProbability: Double,
    val actualProbability: Double,
    val confidence95Low: Double,
    val confidence95High: Double,
) {
    val targetInsideConfidence95: Boolean
        get() = targetProbability in confidence95Low..confidence95High
}

internal data class DistributionReport(
    val sampleSize: Int,
    val buckets: List<DistributionBucket>,
    val strataCounts: Map<String, Int>,
    val entropyBits: Double,
    val maximumShare: Double,
    val longestIdenticalRun: Int,
    val playerPressureP95: Int,
    val pairedDifferenceRate: Double,
) {
    fun toMarkdown(): String = buildString {
        appendLine("STORYTELLER_V4_CALIBRATION_START")
        appendLine("Sample size: $sampleSize")
        appendLine("Strata: ${strataCounts.size}")
        appendLine("Entropy: ${"%.4f".format(entropyBits)} bits")
        appendLine("Maximum candidate share: ${"%.4f".format(maximumShare)}")
        appendLine("Longest identical run: $longestIdenticalRun")
        appendLine("Player pressure P95: $playerPressureP95")
        appendLine("Paired difference rate: ${"%.4f".format(pairedDifferenceRate)}")
        appendLine("95% target coverage: ${buckets.count { it.targetInsideConfidence95 }}/${buckets.size}")
        appendLine("STORYTELLER_V4_CALIBRATION_END")
    }

    companion object {
        fun fromObservations(observations: List<SimulationObservation>): DistributionReport {
            require(observations.isNotEmpty())
            val sampleSize = observations.size
            val counts = observations.groupingBy { it.candidateId }.eachCount().toSortedMap()
            val targetTotals = mutableMapOf<String, Double>()
            observations.forEach { observation ->
                observation.targetProbabilityByCandidate.forEach { (candidateId, probability) ->
                    targetTotals[candidateId] = targetTotals.getOrDefault(candidateId, 0.0) + probability
                }
            }
            val buckets = (counts.keys + targetTotals.keys).sorted().map { candidateId ->
                val count = counts.getOrDefault(candidateId, 0)
                val actual = count.toDouble() / sampleSize
                val target = targetTotals.getOrDefault(candidateId, 0.0) / sampleSize
                val interval = wilson95(count, sampleSize)
                DistributionBucket(candidateId, count, target, actual, interval.first, interval.second)
            }
            val probabilities = counts.values.map { it.toDouble() / sampleSize }
            val pressures = observations.map { it.playerPressure }.sorted()
            return DistributionReport(
                sampleSize = sampleSize,
                buckets = buckets,
                strataCounts = observations.groupingBy { it.stratum }.eachCount().toSortedMap(),
                entropyBits = -probabilities.sumOf { probability -> probability * (ln(probability) / ln(2.0)) },
                maximumShare = probabilities.maxOrNull() ?: 0.0,
                longestIdenticalRun = longestRun(observations.map { it.candidateId }),
                playerPressureP95 = pressures[((pressures.size - 1) * 95) / 100],
                pairedDifferenceRate = observations.count { it.candidateId != it.baselineCandidateId }.toDouble() / sampleSize,
            )
        }

        private fun wilson95(successes: Int, total: Int): Pair<Double, Double> {
            val z = 1.959963984540054
            val p = successes.toDouble() / total
            val denominator = 1.0 + z * z / total
            val center = (p + z * z / (2.0 * total)) / denominator
            val margin = z * sqrt((p * (1.0 - p) + z * z / (4.0 * total)) / total) / denominator
            return (center - margin).coerceAtLeast(0.0) to (center + margin).coerceAtMost(1.0)
        }

        private fun longestRun(values: List<String>): Int {
            var previous: String? = null
            var current = 0
            var longest = 0
            values.forEach { value ->
                current = if (value == previous) current + 1 else 1
                previous = value
                longest = maxOf(longest, current)
            }
            return longest
        }
    }
}

internal data class SimulationObservation(
    val stratum: String,
    val candidateId: String,
    val baselineCandidateId: String,
    val playerPressure: Int,
    val targetProbabilityByCandidate: Map<String, Double>,
)
