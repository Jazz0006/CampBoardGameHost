package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ClocktowerPrivateObservationRecordIdTest {
    @Test fun `observation identity is the stable role step not the previewed result`() {
        fun id(stepId: String) = clocktowerPrivateObservationRecordId(
            gameId = "game-1", phase = ClocktowerPhase.Night, round = 2,
            roleEnName = "Fortune Teller", actorSeat = 1, stepId = stepId,
        )
        // Displaying another candidate before Next must not create another event identity.
        assertEquals(id("ft-step"), id("ft-step"))
        assertNotEquals(id("ft-step"), id("another-night-action"))
        assertNotEquals(id("ft-step"), clocktowerPrivateObservationRecordId(
            "game-1", ClocktowerPhase.Night, 3, "Fortune Teller", 1, "ft-step",
        ))
    }
}
