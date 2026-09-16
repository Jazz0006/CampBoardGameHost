package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleBeginnerCorpusReviewTest {
    @Test
    fun `review report exposes calibration evidence and only a sealed holdout count`() {
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
        val calibrationScenario = FirstNightBeginnerCorpusScenario(
            scenarioId = "calibration",
            partition = FirstNightBeginnerCorpusPartition.CALIBRATION,
            anchorRecipientSeat = 1,
            seating = listOf(1 to RoleId("Washerwoman")),
            rawCompleteBundleCount = BigInteger.TEN,
            distinctProjectedSignatureCount = 2,
            items = listOf(calibrationItem),
        )
        val corpus = FirstNightBeginnerCorpus(listOf(calibrationScenario))

        val report = FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(
            corpus = corpus,
            sealedHoldoutScenarioCount = 1,
        )

        assertTrue(report.contains("calibration"))
        assertTrue(report.contains("synthetic calibration claim"))
        assertTrue(report.contains("Sealed holdout scenarios: 1"))
        assertTrue(report.contains("Holdout diagnostics are not evaluated during calibration."))
        assertEquals(FirstNightBeginnerCorpusLabel.UNREVIEWED, calibrationItem.label)
    }

    @Test
    fun `review export rejects an evaluated holdout scenario`() {
        val holdoutScenario = FirstNightBeginnerCorpusScenario(
            scenarioId = "holdout-should-not-be-evaluated",
            partition = FirstNightBeginnerCorpusPartition.HOLDOUT,
            anchorRecipientSeat = 1,
            seating = listOf(1 to RoleId("Chef")),
            rawCompleteBundleCount = BigInteger.ONE,
            distinctProjectedSignatureCount = 1,
            items = emptyList(),
        )

        assertThrows(IllegalArgumentException::class.java) {
            FirstNightBundleBeginnerCorpusBuilder.renderMarkdown(
                FirstNightBeginnerCorpus(listOf(holdoutScenario)),
            )
        }
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
