package com.codex.campboardgamehost.clocktower.flow

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Coarse production-adapter guard for the non-callable First Night Host boundary.
 *
 * Planner ordering, interaction projection and stable materialization identities are proved by
 * typed flow tests plus AppRootDynamicFlowDecompositionGuardTest. This source check retains only
 * the adapter invariant that actual roles stay separate from Drunk waking identities.
 */
class ClocktowerProductionFirstNightWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `production first-night adapter keeps actual and Drunk waking identities separate`() {
        val actualRolesBlock = source
            .substringAfter("val firstNightActualRoleIds = buildSet")
            .substringBefore("val firstNightWakingRoleIds = buildSet")
        val plannerInputBlock = source
            .substringAfter("val firstNightWakingRoleIds = buildSet")
            .substringBefore("val otherNightWakingRoleIds = buildSet")

        assertFalse(actualRolesBlock.contains("clocktowerShownRole"))
        assertTrue(plannerInputBlock.contains("clocktowerShownRole"))
        assertTrue(source.contains("inPlayRoleIds = firstNightWakingRoleIds"))
        assertTrue(source.contains("actualRoleIds = firstNightActualRoleIds"))
    }
}
