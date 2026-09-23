package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.epistemic.ExactWorldStructureDiagnostics
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightHealthyRecipientExactDiagnostics
import com.codex.campboardgamehost.clocktower.recommendation.FirstNightLeaveOneOutDiagnostics
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D5BundleConfirmationChainEvidenceTest {
    @Test
    fun `leave one out evidence distinguishes single clue strength from multi channel interaction`() {
        val baseline = structure(
            setOf(key(1, 6), key(2, 6), key(3, 6), key(4, 6)),
        )
        val full = diagnostics(
            seat = 1,
            before = baseline,
            after = structure(setOf(key(1, 6))),
            beforeWorlds = 100,
            afterWorlds = 20,
        )
        val removeA = diagnostics(
            seat = 1,
            before = baseline,
            after = structure(setOf(key(1, 6), key(2, 6))),
            beforeWorlds = 100,
            afterWorlds = 40,
        )
        val removeB = diagnostics(
            seat = 1,
            before = baseline,
            after = structure(setOf(key(1, 6), key(3, 6))),
            beforeWorlds = 100,
            afterWorlds = 45,
        )

        val evidence = Sde2D5BundleConfirmationChainEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            signatureId = "multi-channel",
            fullByRecipient = listOf(full),
            leaveOneOut = listOf(
                FirstNightLeaveOneOutDiagnostics("clue-a", listOf(removeA)),
                FirstNightLeaveOneOutDiagnostics("clue-b", listOf(removeB)),
            ),
        )

        assertEquals(2, evidence.worstGoodRecipientRestoringClueCount)
        assertTrue(evidence.hasMultiChannelCollapse)
        assertEquals(setOf("clue-a", "clue-b"), evidence.worstGoodRecipient.restoringObservationKeys)
        assertTrue(evidence.worstGoodRecipient.leaveOneOut.all { it.restoresStrategicTopology })

        val single = Sde2D5BundleConfirmationChainEvidenceProjector.project(
            playerCount = 7,
            profileKind = Sde2D5SetupProfileKind.STANDARD,
            signatureId = "single-channel",
            fullByRecipient = listOf(full),
            leaveOneOut = listOf(
                FirstNightLeaveOneOutDiagnostics("strong-clue", listOf(removeA)),
                FirstNightLeaveOneOutDiagnostics("redundant-clue", listOf(full)),
            ),
        )
        assertEquals(1, single.worstGoodRecipientRestoringClueCount)
        assertFalse(single.hasMultiChannelCollapse)

        val selected = Sde2D5BundleConfirmationChainEvidenceSelector.selectReviewContrasts(
            listOf(evidence, single),
        )
        assertEquals(
            setOf(
                Sde2D5BundleConfirmationSelectionReason.LEAST_INTERACTION_REFERENCE,
                Sde2D5BundleConfirmationSelectionReason.MOST_INTERACTION_REFERENCE,
            ),
            selected.flatMapTo(linkedSetOf()) { it.selectionReasons },
        )
    }

    private fun diagnostics(
        seat: Int,
        before: ExactWorldStructureDiagnostics,
        after: ExactWorldStructureDiagnostics,
        beforeWorlds: Long,
        afterWorlds: Long,
    ) = FirstNightHealthyRecipientExactDiagnostics(
        recipientSeat = seat,
        before = WorldCardinality.Exact(BigInteger.valueOf(beforeWorlds)),
        after = WorldCardinality.Exact(BigInteger.valueOf(afterWorlds)),
        beforeStructure = before,
        afterStructure = after,
    )

    private fun structure(
        keys: Set<StrategicWorldKey>,
    ) = ExactWorldStructureDiagnostics(
        possibleDemonSeats = keys.mapTo(linkedSetOf(), StrategicWorldKey::demonSeat),
        evilTeamSeatConfigurations = keys.mapTo(linkedSetOf()) { key ->
            setOf(key.demonSeat) + key.minionSeats
        },
        strategicWorldKeys = keys,
        forcedGoodSeats = emptySet(),
        forcedEvilSeats = emptySet(),
        evilCoverSeats = keys.flatMapTo(linkedSetOf()) { key ->
            listOf(key.demonSeat) + key.minionSeats
        },
    )

    private fun key(demon: Int, minion: Int) =
        StrategicWorldKey(demonSeat = demon, minionSeats = listOf(minion))
}
