package com.codex.campboardgamehost.clocktower.review

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleBeginnerCorpusReviewTest {
    @Test
    fun `pilot corpus keeps scenario-level holdout isolation and raw review evidence`() {
        val corpus = FirstNightBundleBeginnerCorpusBuilder.build()

        assertEquals(3, corpus.scenarios.size)
        assertTrue(corpus.items.isNotEmpty())
        assertTrue(corpus.items.all { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED })
        assertTrue(corpus.items.any { it.partition == FirstNightBeginnerCorpusPartition.CALIBRATION })
        assertTrue(corpus.items.any { it.partition == FirstNightBeginnerCorpusPartition.HOLDOUT })

        val scenarioPartitions = corpus.scenarios.associate { it.scenarioId to it.partition }
        assertEquals(corpus.scenarios.size, scenarioPartitions.size)
        corpus.items.forEach { item ->
            assertEquals(scenarioPartitions.getValue(item.scenarioId), item.partition)
            assertTrue(item.selectionReasons.isNotEmpty())
            assertTrue(item.publicObservations.isNotEmpty())
            assertTrue(item.perspectives.isNotEmpty())
            assertTrue(item.anchorLeaveOneOut.isNotEmpty())
            assertTrue(item.perspectives.all { perspective ->
                perspective.afterWorldCount.signum() > 0 &&
                    perspective.afterWorldCount <= perspective.beforeWorldCount
            })
        }

        corpus.scenarios.forEach { scenario ->
            val expectedGoodSeats = scenario.seating
                .filterNot { (_, role) -> role.value in setOf("Scarlet Woman", "Baron", "Imp") }
                .map { it.first }
                .sorted()
            assertTrue(scenario.items.size in 4..8)
            scenario.items.forEach { item ->
                assertEquals(expectedGoodSeats, item.perspectives.map { it.recipientSeat }.sorted())
            }
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
