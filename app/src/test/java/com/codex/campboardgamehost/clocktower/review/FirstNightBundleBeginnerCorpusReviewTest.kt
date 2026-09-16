package com.codex.campboardgamehost.clocktower.review

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleBeginnerCorpusReviewTest {
    @Test
    fun `pilot corpus keeps scenario-level holdout isolation and raw review evidence`() {
        val corpus = FirstNightBundleBeginnerCorpusBuilder.build()

        assertEquals(2, corpus.scenarios.size)
        assertEquals(
            setOf(FirstNightBeginnerCorpusPartition.CALIBRATION, FirstNightBeginnerCorpusPartition.HOLDOUT),
            corpus.scenarios.map { it.partition }.toSet(),
        )
        assertTrue(corpus.items.isNotEmpty())
        assertTrue(corpus.items.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })

        val scenarioPartitions = corpus.scenarios.associate { it.scenarioId to it.partition }
        assertEquals(corpus.scenarios.size, scenarioPartitions.size)
        corpus.items.forEach { item ->
            assertEquals(scenarioPartitions.getValue(item.scenarioId), item.partition)
            assertTrue(item.selectionReasons.isNotEmpty())
            assertTrue(item.publicObservations.isNotEmpty())
            assertTrue(item.anchorLeaveOneOut.isNotEmpty())
            assertTrue(item.anchorDiagnostics.afterWorldCount.signum() > 0)
            assertTrue(item.anchorDiagnostics.afterWorldCount <= item.anchorDiagnostics.beforeWorldCount)
        }

        corpus.scenarios.forEach { scenario ->
            assertTrue(scenario.items.size in 4..8)
            assertTrue(scenario.items.all { item ->
                item.anchorDiagnostics.recipientSeat == scenario.anchorRecipientSeat
            })
        }

        assertTrue(corpus.items.any {
            FirstNightBeginnerSelectionReason.LARGEST_LEAVE_ONE_OUT_RECOVERY in it.selectionReasons
        })
        assertFalse(corpus.items.any { item ->
            item.selectionReasons.isEmpty() || item.signatureId.isBlank() || item.itemId.isBlank()
        })

        val report = FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(corpus)
        val calibrationIds = corpus.scenarios
            .filter { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION }
            .map { it.scenarioId }
        val holdoutIds = corpus.scenarios
            .filter { it.partition == FirstNightBeginnerCorpusPartition.HOLDOUT }
            .map { it.scenarioId }
        assertTrue(calibrationIds.all(report::contains))
        assertTrue(holdoutIds.none(report::contains))
        assertTrue(report.contains("Sealed holdout scenarios: ${holdoutIds.size}"))

        val reportFile = File("build/reports/fn-bundle-3-beginner-corpus.md")
        requireNotNull(reportFile.parentFile).mkdirs()
        reportFile.writeText(report, Charsets.UTF_8)
    }
}
