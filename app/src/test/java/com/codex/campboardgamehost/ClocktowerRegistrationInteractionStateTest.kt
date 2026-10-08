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
    fun `recommended witness updates both selections before caller records and clears only false roles`() {
        val state = ClocktowerRegistrationInteractionState()
        val spyKey = "FirstNight:1:Chef:spy"
        val recluseKey = "FirstNight:1:ChefRecluse:Bob"
        state.applyRecommendedWitness(spyKey, recluseKey, option(
            spyGood = true,
            spyRole = "Washerwoman",
            recluseEvil = true,
            recluseRole = "Imp",
        ))
        assertTrue(state.spyIsGood(spyKey))
        assertEquals("Washerwoman", state.spyRole(spyKey))
        assertTrue(state.recluseIsEvil(recluseKey))
        assertEquals("Imp", state.recluseRole(recluseKey))
        assertTrue(state.spyWillRecord(spyKey))

        state.applyRecommendedWitness(spyKey, recluseKey, option(spyGood = false, recluseEvil = false))
        assertFalse(state.spyIsGood(spyKey))
        assertNull(state.spyRole(spyKey))
        assertFalse(state.recluseIsEvil(recluseKey))
        assertNull(state.recluseRole(recluseKey))
    }

    @Test
    fun `recording markers are independent and only mark each key once`() {
        val state = ClocktowerRegistrationInteractionState()
        val spyKey = "Day:1:Virgin:Spy"
        val recluseKey = "Night:1:FortuneTellerRecluse:Bob"
        assertFalse(state.spyWillRecord(null))
        assertTrue(state.spyWillRecord(spyKey))
        assertTrue(state.markSpyRecorded(spyKey))
        assertFalse(state.spyWillRecord(spyKey))
        assertFalse(state.markSpyRecorded(spyKey))
        assertTrue(state.markSpyRecorded("Day:2:Virgin:Spy"))
        assertTrue(state.markRecluseRecorded(recluseKey))
        assertFalse(state.markRecluseRecorded(recluseKey))
    }

    private fun option(
        spyGood: Boolean? = null,
        spyRole: String? = null,
        recluseEvil: Boolean? = null,
        recluseRole: String? = null,
    ) = ClocktowerDisplayOption(
        label = "result",
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = "Chef information",
        displayPrimary = "1",
        displaySecondary = null,
        displayFooter = null,
        spyRegistersGood = spyGood,
        spyRegisteredRoleEnName = spyRole,
        recluseRegistersEvil = recluseEvil,
        recluseRegisteredRoleEnName = recluseRole,
    )
}
