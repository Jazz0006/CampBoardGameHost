package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerCommittedSetupRecommendationLockTest {
    @Test
    fun `committed setup decision overrides mutable lock of the same kind`() {
        val mutableLocks = listOf(
            StorytellerDecision.RedHerring(2),
            StorytellerDecision.DemonBluffs(
                listOf(RoleId("Chef"), RoleId("Empath"), RoleId("Mayor")),
            ),
        )
        val committed = listOf(StorytellerDecision.RedHerring(5))

        val merged = recommendationLocksWithCommittedSetupDecisions(
            mutableLocks = mutableLocks,
            committedDecisions = committed,
        )

        assertEquals(
            listOf(
                StorytellerDecision.DemonBluffs(
                    listOf(RoleId("Chef"), RoleId("Empath"), RoleId("Mayor")),
                ),
                StorytellerDecision.RedHerring(5),
            ),
            merged,
        )
    }

    @Test
    fun `committed setup lock kinds must be unique`() {
        assertThrows(IllegalArgumentException::class.java) {
            recommendationLocksWithCommittedSetupDecisions(
                mutableLocks = emptyList(),
                committedDecisions = listOf(
                    StorytellerDecision.RedHerring(2),
                    StorytellerDecision.RedHerring(5),
                ),
            )
        }
    }
}
