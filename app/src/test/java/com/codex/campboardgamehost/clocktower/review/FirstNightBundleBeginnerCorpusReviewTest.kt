package com.codex.campboardgamehost.clocktower.review

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

        println(FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(corpus))
    }
}
