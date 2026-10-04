package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationCandidate
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClocktowerRegistrationAuthorityTest {
    private val actual = TroubleBrewingRegistrationCandidate(
        subjectSeat = 2,
        registeredRole = RoleId("Spy"),
        registeredType = CharacterType.MINION,
        registeredAlignment = Alignment.EVIL,
    )

    @Test
    fun onlyActualRegistrationAutoResolvesAsRuleDeterministic() {
        assertEquals(
            ClocktowerAutomaticRegistrationRuling(
                usesSpecialRegistration = false,
                registeredRoleEnName = null,
            ),
            clocktowerRuleDeterministicRegistrationRuling(
                TroubleBrewingRegistrationResolution(
                    actual = actual,
                    special = emptyList(),
                ),
            ),
        )
    }

    @Test
    fun multipleLegalRegistrationsRequireManualChoice() {
        val special = TroubleBrewingRegistrationCandidate(
            subjectSeat = 2,
            registeredRole = RoleId("Washerwoman"),
            registeredType = CharacterType.TOWNSFOLK,
            registeredAlignment = Alignment.GOOD,
            specialReason = RegistrationReason.SPY_ABILITY,
        )

        assertNull(
            clocktowerRuleDeterministicRegistrationRuling(
                TroubleBrewingRegistrationResolution(
                    actual = actual,
                    special = listOf(special),
                ),
            ),
        )
    }
}
