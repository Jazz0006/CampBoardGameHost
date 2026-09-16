package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Explicit FN-BUNDLE-3 calibration workload.
 *
 * This is an experiment runner, not part of FAST or FULL regression. Run it through the dedicated
 * Gradle task `fnBundle3Calibration` when calibration evidence is intentionally being regenerated.
 */
class FirstNightBundleBeginnerCorpusExperiment {
    @Test
    fun `generate calibration review corpus`() {
        val corpus = FirstNightBundleBeginnerCorpusBuilder.build()

        assertEquals(2, corpus.scenarios.count { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION })
        assertEquals(1, corpus.scenarios.count { it.partition == FirstNightBeginnerCorpusPartition.HOLDOUT })
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
