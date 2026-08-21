package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GrimoireTruthBindingPersistenceTest {
    private val seats = listOf(
        GrimoireSeatView(
            seat = 1,
            displayedRole = RoleId("Spy"),
            alive = true,
        ),
    )

    @Test
    fun `old schema v2 grimoire without binding decodes as legacy display only`() {
        val decoded = EpistemicSemanticJson.decodeInformationProposition(
            """{"kind":"grimoire-state","seats":[{"alive":true,"displayedRole":"Spy","ruleReminderTokens":[],"seat":1}]}""".replace("\\\"", "\""),
        ) as InformationProposition.GrimoireState

        assertEquals(GrimoireTruthBinding.LEGACY_DISPLAY_ONLY, decoded.truthBinding)
        assertEquals(seats, decoded.seats)
    }

    @Test
    fun `legacy grimoire keeps canonical schema v2 json shape unchanged`() {
        val encoded = EpistemicSemanticJson.encode(InformationProposition.GrimoireState(seats))

        assertFalse(encoded.contains("truthBinding"))
        assertEquals(
            """{"kind":"grimoire-state","seats":[{"alive":true,"displayedRole":"Spy","ruleReminderTokens":[],"seat":1}]}""".replace("\\\"", "\""),
            encoded,
        )
    }

    @Test
    fun `verified exact grimoire round trips explicit truth binding`() {
        val original = InformationProposition.GrimoireState(
            seats = seats,
            truthBinding = GrimoireTruthBinding.VERIFIED_EXACT,
        )
        val encoded = EpistemicSemanticJson.encode(original)
        val decoded = EpistemicSemanticJson.decodeInformationProposition(encoded)

        assertTrue(encoded.contains("\"truthBinding\":\"verified-exact\""))
        assertEquals(original, decoded)
    }

    @Test
    fun `present null grimoire truth binding fails closed`() {
        assertThrows(IllegalArgumentException::class.java) {
            EpistemicSemanticJson.decodeInformationProposition(
                """{"kind":"grimoire-state","seats":[{"alive":true,"displayedRole":"Spy","ruleReminderTokens":[],"seat":1}],"truthBinding":null}""".replace("\\\"", "\""),
            )
        }
    }

    @Test
    fun `same physical payload with different truth binding has different semantic identity`() {
        val legacy = InformationProposition.GrimoireState(seats)
        val exact = InformationProposition.GrimoireState(
            seats = seats,
            truthBinding = GrimoireTruthBinding.VERIFIED_EXACT,
        )

        assertNotEquals(legacy, exact)
        assertNotEquals(EpistemicSemanticJson.encode(legacy), EpistemicSemanticJson.encode(exact))
    }
}
