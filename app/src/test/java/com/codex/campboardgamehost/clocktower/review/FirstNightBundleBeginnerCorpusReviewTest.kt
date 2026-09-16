package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleBeginnerCorpusReviewTest {
    @Test
    fun `review report exposes calibration evidence while sealing holdout`() {
        val diagnostics = FirstNightBeginnerDiagnostics(
            recipientSeat = 1,
            beforeWorldCount = BigInteger.valueOf(100),
            afterWorldCount = BigInteger.valueOf(25),
            demonCoverSize = 4,
            distinctEvilTeamConfigurationCount = 3,
            forcedGoodSeats = setOf(1),
            forcedEvilSeats = emptySet(),
            evilCoverSize = 5,
        )
        val calibrationItem = FirstNightBeginnerCorpusItem(
            itemId = "calibration:sig-a",
            scenarioId = "calibration",
            partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
            label = FirstNightBeginnerCorpusLabel.UNREVIEWED,
            signatureId = "sig-a",
            multiplicity = BigInteger.ONE,
            selectionReasons = setOf(FirstNightBeginnerSelectionReason.LOWER_QUARTILE_AFTER),
            publicObservations = listOf("seat-1/public-claim: synthetic calibration claim"),
            anchorDiagnostics = diagnostics,
            anchorLeaveOneOut = listOf(
                FirstNightBeginnerLeaveOneOutDiagnostics(
                    omittedObservation = "seat-1/public-claim: synthetic calibration claim",
                    diagnostics = diagnostics.copy(afterWorldCount = BigInteger.valueOf(50)),
                ),
            ),
        )
        val holdoutItem = calibrationItem.copy(
            itemId = "sealed-holdout:sig-secret",
            scenarioId = "sealed-holdout",
            partition = FirstNightBeginnerCorpusPartition.HOLDOUT,
            signatureId = "sig-secret",
            publicObservations = listOf("SECRET_HOLDOUT_CLAIM"),
        )
        val corpus = FirstNightBeginnerCorpus(
            scenarios = listOf(
                FirstNightBeginnerCorpusScenario(
                    scenarioId = "calibration",
                    partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
                    anchorRecipientSeat = 1,
                    seating = listOf(1 to RoleId("Washerwoman")),
                    rawCompleteBundleCount = BigInteger.TEN,
                    distinctProjectedSignatureCount = 2,
                    items = listOf(calibrationItem),
                ),
                FirstNightBeginnerCorpusScenario(
                    scenarioId = "sealed-holdout",
                    partition = FirstNightBeginnerCorpusPartition.HOLDOUT,
                    anchorRecipientSeat = 1,
                    seating = listOf(1 to RoleId("Chef")),
                    rawCompleteBundleCount = BigInteger.TEN,
                    distinctProjectedSignatureCount = 2,
                    items = listOf(holdoutItem),
                ),
            ),
        )

        val report = FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(corpus)

        assertTrue(report.contains("calibration"))
        assertTrue(report.contains("synthetic calibration claim"))
        assertTrue(report.contains("Sealed holdout scenarios: 1"))
        assertFalse(report.contains("sealed-holdout"))
        assertFalse(report.contains("sig-secret"))
        assertFalse(report.contains("SECRET_HOLDOUT_CLAIM"))
        assertEquals(FirstNightBeginnerCorpusLabel.UNREVIEWED, calibrationItem.label)
    }

    @Test
    fun `review labels remain explicit human judgment categories`() {
        assertEquals(
            setOf(
                FirstNightBeginnerCorpusLabel.UNREVIEWED,
                FirstNightBeginnerCorpusLabel.BAD_TOO_STRONG,
                FirstNightBeginnerCorpusLabel.ACCEPTABLE,
                FirstNightBeginnerCorpusLabel.BAD_TOO_WEAK,
                FirstNightBeginnerCorpusLabel.UNCERTAIN,
            ),
            FirstNightBeginnerCorpusLabel.entries.toSet(),
        )
    }
}
