package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import kotlin.system.measureNanoTime

/** Debug-only, aggregate-only latency probe for a concrete unified selection-pool boundary. */
internal object UnifiedSelectionPoolDeviceBenchmark {
    fun <T> run(
        poolFactory: () -> UnifiedSelectionPool<T>,
        playerCount: Int,
        phase: StorytellerPhase,
        style: RecommendationStyle,
        styleOf: (T) -> RecommendationStyle,
        samples: Int = DEFAULT_SAMPLES,
    ): UnifiedSelectionPoolDeviceBenchmarkReport {
        require(playerCount > 0)
        require(samples >= 3)
        val buildMicros = mutableListOf<Long>()
        val selectMicros = mutableListOf<Long>()
        val commitMicros = mutableListOf<Long>()
        val dimensions = SelectionAuditDimensions(playerCount, phase, style)
        var candidateCount = 0

        repeat(samples) { sample ->
            lateinit var pool: UnifiedSelectionPool<T>
            buildMicros += measureNanoTime { pool = poolFactory() } / NANOS_PER_MICRO
            candidateCount = pool.rankedCandidates.size
            var selectedCandidate: UnifiedSelectionCandidate<T>? = null
            selectMicros += measureNanoTime {
                val selected = requireNotNull(
                    WeightedStableSelector.selectStyle(
                        pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.payload },
                        style,
                        styleOf,
                    ),
                )
                selectedCandidate = requireNotNull(
                    pool.candidatesFor(SelectionExecutionPolicy.AUTO).firstOrNull { it.payload == selected },
                )
            } / NANOS_PER_MICRO
            val committedCandidate = requireNotNull(selectedCandidate)
            commitMicros += measureNanoTime {
                val telemetry = SelectionDistributionTelemetryRecorder()
                telemetry.recordPreview(
                    SelectionAuditRecord(
                        selectionId = "unified-pool-device-$sample",
                        dimensions = dimensions,
                        candidates = pool.rankedCandidates.map { candidate ->
                            SelectionAuditCandidate(candidate.familyId, candidate.qualityTier)
                        },
                    ),
                )
                telemetry.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = "unified-pool-device-$sample",
                        dimensions = dimensions,
                        selectedFamilyId = committedCandidate.familyId,
                    ),
                )
            } / NANOS_PER_MICRO
        }
        return UnifiedSelectionPoolDeviceBenchmarkReport(
            sampleCount = samples,
            candidateCount = candidateCount,
            poolBuild = UnifiedPoolLatencyPercentiles.from(buildMicros.drop(1)),
            autoSelect = UnifiedPoolLatencyPercentiles.from(selectMicros.drop(1)),
            telemetryCommit = UnifiedPoolLatencyPercentiles.from(commitMicros.drop(1)),
        )
    }

    private const val DEFAULT_SAMPLES = 11
    private const val NANOS_PER_MICRO = 1_000L
}

internal data class UnifiedPoolLatencyPercentiles(val p50Micros: Long, val p95Micros: Long) {
    companion object {
        fun from(values: List<Long>): UnifiedPoolLatencyPercentiles {
            require(values.isNotEmpty())
            val sorted = values.sorted()
            return UnifiedPoolLatencyPercentiles(
                p50Micros = sorted[sorted.size / 2],
                p95Micros = sorted[(sorted.size * 95 + 99) / 100 - 1],
            )
        }
    }
}

internal data class UnifiedSelectionPoolDeviceBenchmarkReport(
    val sampleCount: Int,
    val candidateCount: Int,
    val poolBuild: UnifiedPoolLatencyPercentiles,
    val autoSelect: UnifiedPoolLatencyPercentiles,
    val telemetryCommit: UnifiedPoolLatencyPercentiles,
) {
    fun toLogLine(family: String): String = "UNIFIED_SELECTION_POOL_DEVICE_BENCHMARK family=$family " +
        "samples=$sampleCount candidates=$candidateCount " +
        "poolBuildP50P95Us=${poolBuild.p50Micros}/${poolBuild.p95Micros} " +
        "autoSelectP50P95Us=${autoSelect.p50Micros}/${autoSelect.p95Micros} " +
        "telemetryCommitP50P95Us=${telemetryCommit.p50Micros}/${telemetryCommit.p95Micros}"
}
