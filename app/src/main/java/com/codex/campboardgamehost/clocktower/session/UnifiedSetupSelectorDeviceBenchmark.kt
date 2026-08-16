package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCandidate
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditCommit
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditDimensions
import com.codex.campboardgamehost.clocktower.recommendation.SelectionAuditRecord
import com.codex.campboardgamehost.clocktower.recommendation.SelectionDistributionTelemetryRecorder
import com.codex.campboardgamehost.clocktower.recommendation.SelectionExecutionPolicy
import kotlin.system.measureNanoTime

/** Debug-only timing probe for the live unified setup-selector boundary. */
internal object UnifiedSetupSelectorDeviceBenchmark {
    fun run(
        coordinator: ClocktowerRecommendationCoordinator,
        plans: List<RecommendationPlan>,
        playerCount: Int,
        style: RecommendationStyle,
        samples: Int = DEFAULT_SAMPLES,
    ): UnifiedSetupSelectorDeviceBenchmarkReport {
        require(plans.isNotEmpty()) { "A setup selector diagnostic requires plans." }
        require(playerCount > 0)
        require(samples >= 3)
        val buildMicros = mutableListOf<Long>()
        val selectMicros = mutableListOf<Long>()
        val commitMicros = mutableListOf<Long>()
        val dimensions = SelectionAuditDimensions(playerCount, StorytellerPhase.FIRST_NIGHT, style)

        repeat(samples) { sample ->
            lateinit var pool: com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool<RecommendationPlan>
            buildMicros += measureNanoTime {
                pool = requireNotNull(coordinator.unifiedSetupPool(plans))
            } / NANOS_PER_MICRO
            lateinit var selected: RecommendationPlan
            selectMicros += measureNanoTime {
                selected = requireNotNull(coordinator.selectSetupPlan(pool, style))
            } / NANOS_PER_MICRO
            val selectedCandidate = requireNotNull(
                pool.candidatesFor(SelectionExecutionPolicy.AUTO).firstOrNull { it.payload == selected },
            )
            commitMicros += measureNanoTime {
                val telemetry = SelectionDistributionTelemetryRecorder()
                telemetry.recordPreview(
                    SelectionAuditRecord(
                        selectionId = "device-setup-selector-$sample",
                        dimensions = dimensions,
                        candidates = pool.rankedCandidates.map { candidate ->
                            SelectionAuditCandidate(candidate.familyId, candidate.qualityTier)
                        },
                    ),
                )
                telemetry.recordCommittedSelection(
                    SelectionAuditCommit(
                        selectionId = "device-setup-selector-$sample",
                        dimensions = dimensions,
                        selectedFamilyId = selectedCandidate.familyId,
                    ),
                )
            } / NANOS_PER_MICRO
        }
        return UnifiedSetupSelectorDeviceBenchmarkReport(
            sampleCount = samples,
            candidateCount = plans.size,
            poolBuild = SelectorLatencyPercentiles.from(buildMicros.drop(1)),
            autoSelect = SelectorLatencyPercentiles.from(selectMicros.drop(1)),
            telemetryCommit = SelectorLatencyPercentiles.from(commitMicros.drop(1)),
        )
    }

    private const val DEFAULT_SAMPLES = 11
    private const val NANOS_PER_MICRO = 1_000L
}

internal fun ClocktowerRecommendationCoordinator.selectSetupPlan(
    pool: com.codex.campboardgamehost.clocktower.recommendation.UnifiedSelectionPool<RecommendationPlan>,
    style: RecommendationStyle,
): RecommendationPlan? = com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector.selectStyle(
    pool.candidatesFor(SelectionExecutionPolicy.AUTO).map { it.payload },
    style,
    RecommendationPlan::style,
)

internal data class SelectorLatencyPercentiles(val p50Micros: Long, val p95Micros: Long) {
    companion object {
        fun from(values: List<Long>): SelectorLatencyPercentiles {
            require(values.isNotEmpty())
            val sorted = values.sorted()
            return SelectorLatencyPercentiles(
                p50Micros = sorted[sorted.size / 2],
                p95Micros = sorted[(sorted.size * 95 + 99) / 100 - 1],
            )
        }
    }
}

/** Aggregate-only result for logcat/bug reports; it contains no plan or player data. */
internal data class UnifiedSetupSelectorDeviceBenchmarkReport(
    val sampleCount: Int,
    val candidateCount: Int,
    val poolBuild: SelectorLatencyPercentiles,
    val autoSelect: SelectorLatencyPercentiles,
    val telemetryCommit: SelectorLatencyPercentiles,
) {
    fun toLogLine(): String = "UNIFIED_SETUP_SELECTOR_DEVICE_BENCHMARK samples=$sampleCount " +
        "candidates=$candidateCount " +
        "poolBuildP50P95Us=${poolBuild.p50Micros}/${poolBuild.p95Micros} " +
        "autoSelectP50P95Us=${autoSelect.p50Micros}/${autoSelect.p95Micros} " +
        "telemetryCommitP50P95Us=${telemetryCommit.p50Micros}/${telemetryCommit.p95Micros}"
}
