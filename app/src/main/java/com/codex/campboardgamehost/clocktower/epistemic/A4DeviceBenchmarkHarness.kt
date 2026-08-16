package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import kotlin.system.measureNanoTime

/**
 * Device-callable A4 benchmark. Call only from a debug/diagnostic action, never a storyteller turn.
 * Heap deltas are coarse JVM observations, not retained-size or Android peak-memory measurements.
 */
object A4DeviceBenchmarkHarness {
    fun run(
        deviceLabel: String,
        formal: FormalGameState,
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
        cases: List<A4DeviceBenchmarkCase>,
        samples: Int = DEFAULT_SAMPLES,
    ): A4DeviceBenchmarkReport {
        require(deviceLabel.isNotBlank())
        require(samples >= 3) { "At least three samples are needed for warm percentiles." }
        require(cases.isNotEmpty())
        require(knowledge.formalSnapshotId == formal.snapshotId)
        require(formal.players.size == DIAGNOSTIC_PLAYER_COUNT) {
            "The A4 device diagnostic currently supports only the validated $DIAGNOSTIC_PLAYER_COUNT-player fixture; " +
                "larger exact enumeration can exhaust the device heap before ZDD compression."
        }
        val structuralKnowledge = knowledge.copy(
            setupKnowledge = (knowledge.setupKnowledge + InformationProposition.PlayerCount(formal.players.size)).distinct(),
        )
        val constructionMicros = mutableListOf<Long>()
        val worldGenerationMicros = mutableListOf<Long>()
        val prefixInsertionMicros = mutableListOf<Long>()
        val canonicalizationMicros = mutableListOf<Long>()
        val caseMicros = cases.associateWith { mutableListOf<Long>() }
        var nodeCount = 0
        var cardinality = WorldCardinality.Exact(java.math.BigInteger.ZERO)
        var maxHeapDelta = 0L

        repeat(samples) {
            forceGc()
            val beforeHeap = usedHeapBytes()
            lateinit var zdd: ZddPlayerWorldSet
            lateinit var measured: A4MeasuredZddConstruction
            constructionMicros += measureNanoTime {
                measured = ZddPlayerWorldSet.enumerateDirectMeasured(
                    formal.rulesetRef, structuralKnowledge, hypothesis, roleDefinitions,
                )
                zdd = measured.worldSet
            } / NANOS_PER_MICRO
            worldGenerationMicros += measured.metrics.worldGenerationMicros
            prefixInsertionMicros += measured.metrics.prefixInsertionMicros
            canonicalizationMicros += measured.metrics.canonicalizationMicros
            maxHeapDelta = maxOf(maxHeapDelta, (usedHeapBytes() - beforeHeap).coerceAtLeast(0))
            nodeCount = zdd.nodeCount()
            cardinality = zdd.cardinality() as WorldCardinality.Exact
            cases.forEach { case ->
                lateinit var result: ZddPlayerWorldSet
                caseMicros.getValue(case) += measureNanoTime { result = zdd.require(case.observation) } / NANOS_PER_MICRO
                require(result.lastFilterStrategy == case.expectedStrategy) {
                    "${case.label} expected ${case.expectedStrategy}, got ${result.lastFilterStrategy}."
                }
            }
        }
        return A4DeviceBenchmarkReport(
            deviceLabel = deviceLabel,
            sampleCount = samples,
            worldCardinality = cardinality,
            nodeCount = nodeCount,
            construction = A4LatencyPercentiles.from(constructionMicros.drop(1)),
            constructionPhases = A4ConstructionPhaseBenchmarks(
                exactWorldCount = cardinality.value.toLong(),
                worldGeneration = A4LatencyPercentiles.from(worldGenerationMicros.drop(1)),
                prefixInsertion = A4LatencyPercentiles.from(prefixInsertionMicros.drop(1)),
                canonicalization = A4LatencyPercentiles.from(canonicalizationMicros.drop(1)),
            ),
            coarseMaxBuildHeapDeltaBytes = maxHeapDelta,
            filters = cases.map { case -> A4DeviceFilterBenchmark(
                case.label, case.expectedStrategy, A4LatencyPercentiles.from(caseMicros.getValue(case).drop(1)),
            ) },
        )
    }

    private fun forceGc() = repeat(2) { Runtime.getRuntime().gc() }
    private fun usedHeapBytes(): Long = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
    private const val NANOS_PER_MICRO = 1_000L
    private const val DEFAULT_SAMPLES = 11
    private const val DIAGNOSTIC_PLAYER_COUNT = 5
}

data class A4DeviceBenchmarkCase(
    val label: String,
    val observation: EpistemicObservation,
    val expectedStrategy: ZddFilterStrategy,
) {
    init { require(label.isNotBlank()) }
}

data class A4LatencyPercentiles(val p50Micros: Long, val p95Micros: Long) {
    companion object {
        fun from(values: List<Long>): A4LatencyPercentiles {
            require(values.isNotEmpty())
            val sorted = values.sorted()
            return A4LatencyPercentiles(
                p50Micros = sorted[sorted.size / 2],
                p95Micros = sorted[(sorted.size * 95 + 99) / 100 - 1],
            )
        }
    }
}

data class A4DeviceFilterBenchmark(
    val label: String,
    val strategy: ZddFilterStrategy,
    val latency: A4LatencyPercentiles,
)

data class A4ConstructionPhaseBenchmarks(
    val exactWorldCount: Long,
    val worldGeneration: A4LatencyPercentiles,
    val prefixInsertion: A4LatencyPercentiles,
    val canonicalization: A4LatencyPercentiles,
)

data class A4DeviceBenchmarkReport(
    val deviceLabel: String,
    val sampleCount: Int,
    val worldCardinality: WorldCardinality.Exact,
    val nodeCount: Int,
    val construction: A4LatencyPercentiles,
    val constructionPhases: A4ConstructionPhaseBenchmarks,
    val coarseMaxBuildHeapDeltaBytes: Long,
    val filters: List<A4DeviceFilterBenchmark>,
) {
    /** Stable single-line output intended for Android logcat, bug reports, or manual capture. */
    fun toLogLine(): String = buildString {
        append("A4_DEVICE_BENCHMARK device=").append(deviceLabel)
        append(" samples=").append(sampleCount)
        append(" worlds=").append(worldCardinality.value)
        append(" nodes=").append(nodeCount)
        append(" buildP50Us=").append(construction.p50Micros)
        append(" buildP95Us=").append(construction.p95Micros)
        append(" generationP50P95Us=")
            .append(constructionPhases.worldGeneration.p50Micros).append('/')
            .append(constructionPhases.worldGeneration.p95Micros)
        append(" prefixInsertP50P95Us=")
            .append(constructionPhases.prefixInsertion.p50Micros).append('/')
            .append(constructionPhases.prefixInsertion.p95Micros)
        append(" canonicalizeP50P95Us=")
            .append(constructionPhases.canonicalization.p50Micros).append('/')
            .append(constructionPhases.canonicalization.p95Micros)
        append(" coarseMaxBuildHeapDeltaBytes=").append(coarseMaxBuildHeapDeltaBytes)
        filters.forEach { filter ->
            append(" filter[").append(filter.label).append("]=")
            append(filter.strategy).append(":")
            append(filter.latency.p50Micros).append('/').append(filter.latency.p95Micros).append("us")
        }
    }
}
