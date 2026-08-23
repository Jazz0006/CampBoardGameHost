package com.codex.campboardgamehost.clocktower.flow

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerActualRoleFlowWiringTest {
    private val source = File(
        "src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    ).readText(Charsets.UTF_8)

    @Test
    fun `first-night production passes actual roles separately from waking identities`() {
        assertTrue(source.contains("val firstNightActualRoleIds = buildSet"))
        assertTrue(source.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertTrue(source.contains("actualRoleIds = firstNightActualRoleIds"))
    }

    @Test
    fun `actual-role set does not add Drunk shown identity`() {
        val actualRolesBlock = source
            .substringAfter("val firstNightActualRoleIds = buildSet")
            .substringBefore("val firstNightWakingRoleIds = buildSet")

        assertTrue(actualRolesBlock.contains("card.clocktowerRole?.enName?.let { add(RoleId(it)) }"))
        assertFalse(actualRolesBlock.contains("clocktowerShownRole"))
    }
}
