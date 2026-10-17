package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRegistrationInteractionStateTest {
    @Test
    fun `manual Spy choice retains role across toggles and separate interaction keys`() {
        val state = ClocktowerRegistrationInteractionState()
        val virginKey = "Day:1:Virgin:Spy"
        val empathKey = "Night:1:Empath:Alice"

        state.chooseSpy(virginKey, good = true, defaultRole = "Washerwoman")
        state.chooseSpyRole(virginKey, "Librarian")
        state.chooseSpy(virginKey, good = false, defaultRole = "Washerwoman")
        assertFalse(state.spyIsGood(virginKey))
        assertEquals("Librarian", state.spyRole(virginKey))

        state.chooseSpy(empathKey, good = true, defaultRole = null)
        assertTrue(state.spyIsGood(empathKey))
        assertNull(state.spyRole(empathKey))
        state.chooseSpy(virginKey, good = true, defaultRole = "Washerwoman")
        assertEquals("Librarian", state.spyRole(virginKey))
        assertFalse(state.spyIsGood(null))
    }

    @Test
    fun `manual Recluse choice retains role and can use the empty legal fallback`() {
        val state = ClocktowerRegistrationInteractionState()
        val key = "Night:2:FortuneTellerRecluse:Bob"

        state.chooseRecluse(key, evil = true, defaultRole = "")
        assertEquals("", state.recluseRole(key))
        state.chooseRecluseRole(key, "Imp")
        state.chooseRecluse(key, evil = false, defaultRole = "Scarlet Woman")
        assertFalse(state.recluseIsEvil(key))
        assertEquals("Imp", state.recluseRole(key))
        state.chooseRecluse(key, evil = true, defaultRole = "Scarlet Woman")
        assertEquals("Imp", state.recluseRole(key))
    }

    @Test
    fun `recording markers are independent and require explicit manual choices`() {
        val state = ClocktowerRegistrationInteractionState()
        val spyKey = "Day:1:Virgin:Spy"
        val recluseKey = "Night:1:FortuneTellerRecluse:Bob"
        state.chooseSpy(spyKey, good = false)
        state.chooseRecluse(recluseKey, evil = true)
    }

    @Test
    fun `generic manual choice distinguishes untouched from explicit false and discards inactive role`() {
        val state = ClocktowerRegistrationInteractionState()
        val spy = "FirstNight:1:Empath:Spy"
        val recluse = "FirstNight:1:Empath:Recluse"
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.SPY, spy))
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.RECLUSE, recluse))
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.SPY, null))

        // Editing an explanatory role alone is not an explicit ruling.
        state.chooseSpyRole(spy, "Librarian")
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.SPY, spy))

        // Explicit false is NOT the same as no recorded choice.
        state.chooseSpy(spy, good = false)
        assertEquals(
            ClocktowerManualRegistrationChoice(
                ClocktowerRegistrationSubject.SPY, spy, usesSpecialRegistration = false,
                selectedRegisteredRoleEnName = null,
            ),
            state.explicitChoice(ClocktowerRegistrationSubject.SPY, spy),
        )
        state.chooseSpy(spy, good = true)
        assertEquals("Librarian",
            state.explicitChoice(ClocktowerRegistrationSubject.SPY, spy)?.selectedRegisteredRoleEnName)
        state.chooseSpy(spy, good = false)
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.SPY, spy)?.selectedRegisteredRoleEnName)

        state.chooseRecluse(recluse, evil = false)
        assertEquals(false,
            state.explicitChoice(ClocktowerRegistrationSubject.RECLUSE, recluse)?.usesSpecialRegistration)
        state.chooseRecluse(recluse, evil = true, defaultRole = "")
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.RECLUSE, recluse)?.selectedRegisteredRoleEnName)
        state.chooseRecluseRole(recluse, "Imp")
        assertEquals("Imp",
            state.explicitChoice(ClocktowerRegistrationSubject.RECLUSE, recluse)?.selectedRegisteredRoleEnName)
        assertNull(state.explicitChoice(ClocktowerRegistrationSubject.RECLUSE, "Night:2:ChefRecluse:Recluse"))
    }


}