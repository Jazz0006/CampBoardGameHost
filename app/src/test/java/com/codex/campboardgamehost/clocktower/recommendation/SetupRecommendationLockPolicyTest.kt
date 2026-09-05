package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupRecommendationLockPolicyTest {
    @Test
    fun `mutable setup locks start and clear empty`() {
        assertTrue(SetupRecommendationLockPolicy.initialLocks().isEmpty())
        assertTrue(SetupRecommendationLockPolicy.clear().isEmpty())
    }

    @Test
    fun `committed shown identity is excluded while mutable decisions are preserved`() {
        val redHerring = StorytellerDecision.RedHerring(seat = 4)
        val demonBluffs = StorytellerDecision.DemonBluffs(
            roles = listOf(RoleId("Chef"), RoleId("Monk"), RoleId("Soldier")),
        )

        val locks = SetupRecommendationLockPolicy.replaceWith(
            listOf(
                redHerring,
                StorytellerDecision.DrunkShownRole(RoleId("Investigator")),
                demonBluffs,
            ),
        )

        assertEquals(listOf(redHerring, demonBluffs), locks)
    }
}
