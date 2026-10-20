package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Navigation must follow the Host's refreshed interaction identities, not old list indices. */
class ClocktowerDynamicNightAdvanceTest {
    private fun after(
        completed: String,
        acceptedPrefix: List<String>,
        refreshed: List<String>,
    ) = clocktowerResolvedNightAdvanceDirective(
        confirmed = ClocktowerConfirmedNightAdvance(
            completedInteractionId = completed,
            completedAndEarlierInteractionIds = acceptedPrefix,
            revisionBeforeCommit = 0L,
            awaitCommittedRevision = true,
        ),
        refreshedInteractionIds = refreshed,
    )

    @Test fun `normal Poisoner Monk Imp Butler order advances to the actual next role`() {
        assertEquals(
            ClocktowerNightAdvanceDirective.MoveTo(3),
            after("imp", listOf("poison", "monk", "imp"),
                listOf("poison", "monk", "imp", "butler")),
        )
    }

    @Test fun `death of an earlier actor cannot shift the Imp cursor into the wrong role`() {
        assertEquals(
            ClocktowerNightAdvanceDirective.MoveTo(2),
            after("imp", listOf("poison", "monk", "imp"),
                listOf("poison", "imp", "butler")),
        )
    }

    @Test fun `committed attack adds death-trigger Ravenkeeper before Butler`() {
        assertEquals(
            ClocktowerNightAdvanceDirective.MoveTo(3),
            after("imp", listOf("poison", "monk", "imp"),
                listOf("poison", "monk", "imp", "ravenkeeper", "butler")),
        )
    }

    @Test fun `a disappearing current actor anchors to the last surviving preceding step`() {
        assertEquals(
            ClocktowerNightAdvanceDirective.MoveTo(2),
            after("imp", listOf("poison", "monk", "imp"),
                listOf("poison", "monk", "butler")),
        )
    }

    @Test fun `night completes after Host removes slain final actor`() {
        assertEquals(
            ClocktowerNightAdvanceDirective.CompleteNight,
            after("imp", listOf("poison", "monk", "imp"), listOf("poison", "monk", "imp")),
        )
    }

    @Test fun `Host refuses duplicated interaction identities`() {
        assertThrows(IllegalArgumentException::class.java) {
            after("imp", listOf("poison", "imp"), listOf("poison", "imp", "imp"))
        }
    }
}
