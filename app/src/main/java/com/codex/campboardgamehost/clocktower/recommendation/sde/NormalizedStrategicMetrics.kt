package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics

/**
 * Exact integer ratio used by SDE strategic diagnostics.
 *
 * Integer numerator/denominator remain the source of truth. Callers may project to floating point
 * for reporting or later calibration, but recommendation semantics must not depend on an opaque
 * accumulated floating-point score.
 */
internal data class StrategicRetentionRatio(
    val numerator: Int,
    val denominator: Int,
) {
    init {
        require(denominator > 0) { "Strategic metric denominator must be positive." }
        require(numerator in 0..denominator) {
            "Strategic metric numerator must be between zero and its denominator."
        }
    }

    fun asDouble(): Double = numerator.toDouble() / denominator.toDouble()
}

internal data class NormalizedStrategicMetrics(
    val demonCoverRetention: StrategicRetentionRatio,
    val evilTopologyRetention: StrategicRetentionRatio,
    val evilCoverRetention: StrategicRetentionRatio,
    val forcedGoodFraction: StrategicRetentionRatio,
)

internal enum class NormalizedStrategicMetricBaselineDimension {
    EXACT_WORLDS,
    DEMON_COVER,
    EVIL_TOPOLOGY,
    EVIL_COVER,
}

internal sealed interface NormalizedStrategicMetricsEvaluation {
    data class Ready(
        val metrics: NormalizedStrategicMetrics,
    ) : NormalizedStrategicMetricsEvaluation

    /** The candidate has no surviving exact witness and must be rejected before quality metrics. */
    data object Unsatisfiable : NormalizedStrategicMetricsEvaluation

    /**
     * A baseline denominator required for normalization is absent. This is invalid/undefined
     * evidence rather than a zero-retention result.
     */
    data class UndefinedBaseline(
        val dimensions: Set<NormalizedStrategicMetricBaselineDimension>,
    ) : NormalizedStrategicMetricsEvaluation {
        init {
            require(dimensions.isNotEmpty()) {
                "Undefined strategic baseline must identify at least one missing denominator."
            }
        }
    }
}

/**
 * Pure SDE projection over exact BEFORE/AFTER structure.
 *
 * The epistemic evaluator remains the sole owner of mechanical worlds and exact structure. This
 * projection neither enumerates worlds nor defines recommendation policy; it only normalizes exact
 * descriptive evidence so different player-count regimes can be compared without weighting raw role
 * permutations as distinct strategic states.
 */
internal object NormalizedStrategicMetricsProjection {
    fun evaluate(
        diagnostics: ExactHypotheticalObservationBundleDiagnostics,
        playerCount: Int,
    ): NormalizedStrategicMetricsEvaluation {
        require(playerCount > 0) { "Normalized strategic metrics require a positive player count." }
        require(diagnostics.after.value <= diagnostics.before.value) {
            "Hypothetical exact AFTER cardinality cannot exceed its BEFORE baseline."
        }

        if (diagnostics.before.value.signum() == 0) {
            return NormalizedStrategicMetricsEvaluation.UndefinedBaseline(
                setOf(NormalizedStrategicMetricBaselineDimension.EXACT_WORLDS),
            )
        }

        val before = diagnostics.beforeStructure
        val missingDenominators = buildSet {
            if (before.demonCoverSize == 0) {
                add(NormalizedStrategicMetricBaselineDimension.DEMON_COVER)
            }
            if (before.distinctStrategicWorldCount == 0) {
                add(NormalizedStrategicMetricBaselineDimension.EVIL_TOPOLOGY)
            }
            if (before.evilCoverSize == 0) {
                add(NormalizedStrategicMetricBaselineDimension.EVIL_COVER)
            }
        }
        if (missingDenominators.isNotEmpty()) {
            return NormalizedStrategicMetricsEvaluation.UndefinedBaseline(missingDenominators)
        }

        if (diagnostics.after.value.signum() == 0) {
            return NormalizedStrategicMetricsEvaluation.Unsatisfiable
        }

        val after = diagnostics.afterStructure
        require(after.forcedGoodSeats.size <= playerCount) {
            "Forced-good seat count cannot exceed player count."
        }

        return NormalizedStrategicMetricsEvaluation.Ready(
            NormalizedStrategicMetrics(
                demonCoverRetention = StrategicRetentionRatio(
                    numerator = after.demonCoverSize,
                    denominator = before.demonCoverSize,
                ),
                evilTopologyRetention = StrategicRetentionRatio(
                    numerator = after.distinctStrategicWorldCount,
                    denominator = before.distinctStrategicWorldCount,
                ),
                evilCoverRetention = StrategicRetentionRatio(
                    numerator = after.evilCoverSize,
                    denominator = before.evilCoverSize,
                ),
                forcedGoodFraction = StrategicRetentionRatio(
                    numerator = after.forcedGoodSeats.size,
                    denominator = playerCount,
                ),
            ),
        )
    }
}
