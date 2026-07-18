package com.codex.campboardgamehost.clocktower.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationInteractionRulesTest {
    @Test
    fun `reliable information ability keeps its registration interaction`() {
        assertEquals(
            "Night:2:Empath:spy",
            RegistrationInteractionRules.effectiveRegistrationKey(
                key = "Night:2:Empath:spy",
                informationAbilityReliable = true,
            ),
        )
    }

    @Test
    fun `unreliable information ability chooses final information without registration`() {
        assertNull(
            RegistrationInteractionRules.effectiveRegistrationKey(
                key = "Night:2:Empath:spy",
                informationAbilityReliable = false,
            ),
        )
    }
}
