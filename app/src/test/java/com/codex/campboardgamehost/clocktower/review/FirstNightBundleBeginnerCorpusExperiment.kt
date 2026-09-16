package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit FN-BUNDLE-3 calibration workload.
 *
 * The interaction-rich scenario is scanned exhaustively. The weak-information probe evaluates a
 * bounded deterministic sample of legal public signatures exactly. Neither path evaluates holdout.
 */
class FirstNightBundleBeginnerCorpusExperiment {
    @Test
    fun `generate beginner calibration review corpus`() {
        val richCorpus = FirstNightBundleBeginnerCorpusBuilder.buildCalibration()
        val lowInformation = FirstNightBundleBeginnerLowInformationCalibrationBuilder.build()

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
            append(FirstNightBundleBeginnerLowInformationCalibrationBuilder.renderMarkdown(lowInformation))
            appendLine("FN_BUNDLE_3_CORPUS_END")
        }
        val reportFile = File("build/reports/fn-bundle-3-beginner-corpus.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
