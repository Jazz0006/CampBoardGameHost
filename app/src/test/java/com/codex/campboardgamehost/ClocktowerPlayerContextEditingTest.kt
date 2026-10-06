package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerPlayerContextEditingTest {
    @Test
    fun togglingClaimsPreservesStorytellerSelectionOrder() {
        val initial = ClocktowerPlayerContextEditState.from(
            StorytellerPlayerContextInputV1(
                experienceLevel = PlayerExperienceLevelV1.BEGINNER,
                claimedRoleIds = listOf(RoleId("Chef")),
                pressureLevel = StorytellerDeclaredPressureLevelV1.MEDIUM,
            ),
        )

        val edited = initial
            .toggleClaim(RoleId("Saint"))
            .toggleClaim(RoleId("Chef"))

        assertEquals(listOf(RoleId("Saint")), edited.claimedRoleIds)
        assertEquals(
            StorytellerPlayerContextInputV1(
                experienceLevel = PlayerExperienceLevelV1.BEGINNER,
                claimedRoleIds = listOf(RoleId("Saint")),
                pressureLevel = StorytellerDeclaredPressureLevelV1.MEDIUM,
            ),
            edited.toInput(),
        )
    }
}
