package com.codex.campboardgamehost.clocktower.epistemic

import org.junit.Test

/** Reuse the experiment's correctness checks without its full sweep or performance report. */
class TopologyBundleBoundaryTest {
    @Test
    fun `bundle is feasible and bounded across player count regimes`() {
        Sde2D4TopologyBundlePerformanceTest().verifyPlayerCounts(
            listOf(5, 8, 12, 15), recordMeasurements = false,
        )
    }
}
