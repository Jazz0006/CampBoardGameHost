package com.codex.campboardgamehost.clocktower.simulation

import com.codex.campboardgamehost.clocktower.config.DecisionRiskClass
import com.codex.campboardgamehost.clocktower.config.DecisionScoreTolerances
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToleranceCalibrationTest {
    @Test
    fun `fixed seed calibration is reproducible`() {
        assertEquals(ToleranceCalibration.run(500), ToleranceCalibration.run(500))
    }

    @Test
    fun `formal fifty thousand sample validates final score tolerances`() {
        val calibration = ToleranceCalibration.run()
        val report = calibration.distribution

        assertEquals(ToleranceCalibration.FORMAL_SAMPLE_SIZE, calibration.sampleSize)
        assertEquals(135, report.strataCounts.size)
        assertTrue(report.strataCounts.values.max() - report.strataCounts.values.min() <= 1)
        assertEquals(0, calibration.outsideToleranceSelections)
        assertEquals(
            mapOf(
                DecisionRiskClass.SETUP_PLAN to 32,
                DecisionRiskClass.SETUP_ITEM to 6,
                DecisionRiskClass.DYNAMIC_INFORMATION to 4,
                DecisionRiskClass.HIGH_IMPACT_DYNAMIC to 2,
                DecisionRiskClass.RULE_ONLY to 0,
            ),
            calibration.tolerances,
        )
        assertEquals(DecisionScoreTolerances.CALIBRATION_VERSION, calibration.calibrationVersion)
        assertTrue(report.maximumShare < 0.01)
        assertTrue(report.entropyBits > 7.0)
        assertTrue(report.pairedDifferenceRate in 0.25..0.50)
        assertTrue(report.playerPressureP95 <= 3)
        assertTrue(report.buckets.count { it.targetInsideConfidence95 }.toDouble() / report.buckets.size >= 0.90)
        println(report.toMarkdown())
    }
}
