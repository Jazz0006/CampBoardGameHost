package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit FN-BUNDLE-3 calibration workload.
 *
 * Real production 7-player presets are the calibration authority for Stage 7A. The first real-preset
 * pilot evaluates one deterministic seating topology and at most three deterministic legal public
 * bundle points per healthy-compatible preset. The older interaction-rich and weak-information
 * fixtures remain synthetic stress probes. Every selected point uses exact possible-world evaluation;
 * neither real-preset calibration nor stress probes evaluate holdout.
 */
class FirstNightBundleBeginnerCorpusExperiment {
    @Test
    fun `generate beginner calibration review corpus`() {
        val realPresets = FirstNightBundleBeginnerRealPresetCalibrationBuilder.build()
        val richCorpus = FirstNightBundleBeginnerCorpusBuilder.buildCalibration()
        val lowInformation = FirstNightBundleBeginnerLowInformationCalibrationBuilder.build()

        assertTrue(realPresets.totalSevenPlayerPresetCount > 0)
        assertTrue(realPresets.eligibleHealthyPresetCount > 0)
        assertEquals(realPresets.eligibleHealthyPresetCount, realPresets.scenarios.size)
        assertEquals(
            realPresets.totalSevenPlayerPresetCount,
            realPresets.eligibleHealthyPresetCount + realPresets.excludedStagedPresetCount,
        )
        assertTrue(realPresets.scenarios.map { it.presetId }.distinct().size == realPresets.scenarios.size)
        assertTrue(realPresets.scenarios.all { scenario ->
            scenario.seating.size == 7 &&
                scenario.points.size in 1..3 &&
                scenario.legalCompleteBundleCount.signum() > 0 &&
                scenario.points.all { point ->
                    point.diagnostics.afterWorldCount.signum() > 0 &&
                        point.diagnostics.afterWorldCount <= point.diagnostics.beforeWorldCount
                }
        })

        assertEquals(1, richCorpus.scenarios.size)
        assertTrue(richCorpus.scenarios.all { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION })
        assertTrue(richCorpus.items.isNotEmpty())
        assertTrue(richCorpus.items.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(richCorpus.items.all { it.selectionReasons.isNotEmpty() })
        assertTrue(richCorpus.items.all { it.anchorDiagnostics.afterWorldCount.signum() > 0 })
        assertTrue(richCorpus.items.all {
            it.anchorDiagnostics.afterWorldCount <= it.anchorDiagnostics.beforeWorldCount
        })
        assertTrue(lowInformation.legalWasherwomanCandidateCount >= lowInformation.selectedPointCount)
        assertTrue(lowInformation.selectedPointCount in 1..3)
        assertTrue(lowInformation.points.all { point ->
            point.diagnostics.afterWorldCount.signum() > 0 &&
                point.diagnostics.afterWorldCount <= point.diagnostics.beforeWorldCount
        })

        val report = buildString {
            append(FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(richCorpus).removeSuffix("FN_BUNDLE_3_CORPUS_END\n"))
            appendLine()
            append(FirstNightBundleBeginnerRealPresetCalibrationBuilder.renderMarkdown(realPresets))
            appendLine()
            appendLine("## Synthetic weak-information stress probe")
            appendLine()
            append(FirstNightBundleBeginnerLowInformationCalibrationBuilder.renderMarkdown(lowInformation))
            appendLine("FN_BUNDLE_3_CORPUS_END")
        }
        val reportFile = File("build/reports/fn-bundle-3-beginner-corpus.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
