package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** P1 recovery contract for state-first Day -> Night poison expiry retries. */
class DuskPoisonExpiryRecoveryAuthorityTest {
    @Test
    fun `first-night poison remains recoverable at first dusk`() {
        val timeline = ActionFactTimeline(
            listOf(
                poisonEntry(
                    actionId = "first-night-poison",
                    phase = StorytellerPhase.FIRST_NIGHT,
                    round = 1,
                    targetSeat = 3,
                    globalSequence = 0L,
                ),
            ),
        )

        assertEquals(3, DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(timeline, 1))
    }

    @Test
    fun `ordinary-night poison remains recoverable at later dusk`() {
        val timeline = ActionFactTimeline(
            listOf(
                poisonEntry(
                    actionId = "night-poison",
                    phase = StorytellerPhase.NIGHT,
                    round = 4,
                    targetSeat = 2,
                    globalSequence = 0L,
                ),
            ),
        )

        assertEquals(2, DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(timeline, 4))
    }

    @Test
    fun `durable dusk clear becomes latest authority for fully durable retry`() {
        val timeline = ActionFactTimeline(
            listOf(
                poisonEntry(
                    actionId = "night-poison",
                    phase = StorytellerPhase.NIGHT,
                    round = 4,
                    targetSeat = 2,
                    globalSequence = 0L,
                ),
                poisonEntry(
                    actionId = "dusk-clear",
                    phase = StorytellerPhase.DAY,
                    round = 4,
                    targetSeat = null,
                    globalSequence = 1L,
                ),
            ),
        )

        assertNull(DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(timeline, 4))
    }

    @Test
    fun `other rounds do not supply previous poison authority`() {
        val timeline = ActionFactTimeline(
            listOf(
                poisonEntry(
                    actionId = "old-poison",
                    phase = StorytellerPhase.NIGHT,
                    round = 3,
                    targetSeat = 2,
                    globalSequence = 0L,
                ),
            ),
        )

        assertNull(DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(timeline, 4))
    }

    private fun poisonEntry(
        actionId: String,
        phase: StorytellerPhase,
        round: Int,
        targetSeat: Int?,
        globalSequence: Long,
    ) = TimelineBoundActionFact(
        fact = ActionFact.Poison(
            actionId = actionId,
            sequence = globalSequence,
            targetSeat = targetSeat,
        ),
        point = TimelinePoint(
            phase = phase,
            round = round,
            sequence = globalSequence.toInt(),
            globalSequence = globalSequence,
        ),
    )
}
