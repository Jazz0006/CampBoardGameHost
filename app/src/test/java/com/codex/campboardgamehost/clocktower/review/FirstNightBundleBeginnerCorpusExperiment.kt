package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit FN-BUNDLE-3 exhaustive calibration workload.
 *
 * This is an experiment runner, not part of FAST or FULL regression. It evaluates calibration only;
 * sealed holdout scenarios are not evaluated until candidate gates are frozen.
 */
class FirstNightBundleBeginnerCorpusExperiment {
    @Test
    fun `generate exhaustive interaction-rich calibration review corpus`() {
        val corpus = FirstNightBundleBeginnerCorpusBuilder.buildCalibration()

        assertEquals(1, corpus.scenarios.size)
        assertTrue(corpus.scenarios.all { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION })
        assertTrue(corpus.items.isNotEmpty())
        assertTrue(corpus.items.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(corpus.items.all { it.selectionReasons.isNotEmpty() })
        assertTrue(corpus.items.all { it.anchorDiagnostics.afterWorldCount.signum() > 0 })
        assertTrue(corpus.items.all {
            it.anchorDiagnostics.afterWorldCount <= it.anchorDiagnostics.beforeWorldCount
        })

        val report = FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(corpus)
        val reportFile = File("build/reports/fn-bundle-3-beginner-corpus.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
