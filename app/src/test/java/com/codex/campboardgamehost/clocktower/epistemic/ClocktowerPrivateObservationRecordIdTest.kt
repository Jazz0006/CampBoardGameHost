package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClocktowerPrivateObservationRecordIdTest {
    @Test
    fun `private observation identity is stable for duplicates and changes with shown statement`() {
        val first = InformationProposition.BooleanResult(
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = 1,
            subjectSeats = listOf(2, 3),
            value = false,
        )
        val same = InformationProposition.BooleanResult(
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = 1,
            subjectSeats = listOf(2, 3),
            value = false,
        )
        val corrected = InformationProposition.BooleanResult(
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            sourceSeat = 1,
            subjectSeats = listOf(2, 4),
            value = true,
        )

        val firstId = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            roleEnName = "Fortune Teller",
            actorSeat = 1,
            proposition = first,
        )
        val sameId = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            roleEnName = "Fortune Teller",
            actorSeat = 1,
            proposition = same,
        )
        val correctedId = clocktowerPrivateObservationRecordId(
            gameId = "game-1",
            phase = ClocktowerPhase.Night,
            round = 2,
            roleEnName = "Fortune Teller",
            actorSeat = 1,
            proposition = corrected,
        )

        assertEquals(firstId, sameId)
        assertNotEquals(firstId, correctedId)
    }
}
