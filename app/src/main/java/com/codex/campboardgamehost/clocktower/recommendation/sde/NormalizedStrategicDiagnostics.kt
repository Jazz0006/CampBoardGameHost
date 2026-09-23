package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactStrategicTopologyBundleDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics

/**
 * Exact bounded ratio used by player-count-normalized SDE diagnostics.
 *
 * The raw numerator/denominator remain visible so calibration can distinguish equal percentages
 * supported by different baseline sizes. Empty baselines are explicit rather than NaN/Infinity.
 */
internal sealed interface StrategicRatio {
    fun valueOrNull(): Double?

    data class Defined(
        val numerator: Int,
        val denominator: Int,
    ) : StrategicRatio {
        init {
            require(denominator > 0) { "A defined strategic ratio requires a positive denominator." }
            require(numerator in 0..denominator) {
                "A bounded strategic ratio requires numerator in 0..denominator."
            }
        }

        override fun valueOrNull(): Double = numerator.toDouble() / denominator.toDouble()
    }

    object Undefined : StrategicRatio {
        override fun valueOrNull(): Double? = null
    }

    companion object {
        fun bounded(numerator: Int, denominator: Int): StrategicRatio =
            if (denominator == 0) {
                require(numerator == 0) {
                    "A zero-baseline strategic ratio cannot retain a non-zero numerator."
                }
                Undefined
            } else {
                Defined(numerator, denominator)
            }
    }
}

internal data class NormalizedStrategicDiagnostics(
    val demonCoverRetention: StrategicRatio,
    val evilTopologyRetention: StrategicRatio,
    val evilCoverRetention: StrategicRatio,
    val forcedGoodFraction: StrategicRatio.Defined,
    val forcedEvilFraction: StrategicRatio.Defined,
)

/**
 * Pure SDE projection over epistemic-owned exact BEFORE / AFTER structure.
 *
 * This is diagnostic evidence only. It does not rank candidates or change recommendation policy.
 */
internal object NormalizedStrategicDiagnosticsProjector {
    fun project(
        diagnostic: ExactHypotheticalObservationBundleDiagnostics,
        playerCount: Int,
    ): NormalizedStrategicDiagnostics =
        project(
            before = diagnostic.beforeStructure,
            after = diagnostic.afterStructure,
            playerCount = playerCount,
        )

    fun project(
        diagnostic: ExactStrategicTopologyBundleDiagnostics,
        playerCount: Int,
    ): NormalizedStrategicDiagnostics =
        project(
            before = diagnostic.beforeStructure,
            after = diagnostic.afterStructure,
            playerCount = playerCount,
        )

    fun project(
        before: ExactWorldStructureDiagnostics,
        after: ExactWorldStructureDiagnostics,
        playerCount: Int,
    ): NormalizedStrategicDiagnostics {
        require(playerCount > 0) { "Normalized strategic diagnostics require a positive player count." }

        require(after.possibleDemonSeats.all(before.possibleDemonSeats::contains)) {
            "AFTER Demon cover must be a subset of the exact BEFORE cover."
        }
        require(after.strategicWorldKeys.all(before.strategicWorldKeys::contains)) {
            "AFTER strategic worlds must be a subset of the exact BEFORE strategic worlds."
        }
        require(after.evilCoverSeats.all(before.evilCoverSeats::contains)) {
            "AFTER evil cover must be a subset of the exact BEFORE evil cover."
        }
        require(after.forcedGoodSeats.all { it in 1..playerCount }) {
            "Forced-good seats must belong to the current player range."
        }
        require(after.forcedEvilSeats.all { it in 1..playerCount }) {
            "Forced-evil seats must belong to the current player range."
        }

        return NormalizedStrategicDiagnostics(
            demonCoverRetention = StrategicRatio.bounded(
                numerator = after.demonCoverSize,
                denominator = before.demonCoverSize,
            ),
            evilTopologyRetention = StrategicRatio.bounded(
                numerator = after.distinctStrategicWorldCount,
                denominator = before.distinctStrategicWorldCount,
            ),
            evilCoverRetention = StrategicRatio.bounded(
                numerator = after.evilCoverSize,
                denominator = before.evilCoverSize,
            ),
            forcedGoodFraction = StrategicRatio.Defined(
                numerator = after.forcedGoodSeats.size,
                denominator = playerCount,
            ),
            forcedEvilFraction = StrategicRatio.Defined(
                numerator = after.forcedEvilSeats.size,
                denominator = playerCount,
            ),
        )
    }
}
