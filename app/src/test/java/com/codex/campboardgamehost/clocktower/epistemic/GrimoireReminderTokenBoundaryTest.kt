package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GrimoireReminderTokenBoundaryTest {
    private val poisoner = ClocktowerCharacterDefinition(
        id = RoleId("Poisoner"),
        externalId = "poisoner",
        name = "Poisoner",
        team = ClocktowerCatalogTeam.MINION,
        abilityText = "Each night, choose a player: they are poisoned tonight and tomorrow day.",
        reminders = listOf("Poisoned", "Poisoned"),
        globalReminders = listOf("Global Poison Marker"),
        automationCoverage = RuleCoverage.PARTIAL,
    )

    @Test fun `ruleset reminder occurrence resolves to a typed grimoire token reference`() {
        val first = poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 1)
        val second = poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 2)
        val global = poisoner.grimoireReminderToken(GrimoireReminderTokenScope.GLOBAL, occurrence = 1)

        assertEquals(RoleId("Poisoner"), first.sourceRole)
        assertEquals("Poisoned", first.label)
        assertEquals(1, first.occurrence)
        assertEquals(GrimoireReminderTokenScope.CHARACTER, first.scope)
        assertNotEquals(first, second)
        assertEquals("Global Poison Marker", global.label)
        assertEquals(GrimoireReminderTokenScope.GLOBAL, global.scope)
    }

    @Test fun `ruleset reminder occurrence fails closed outside declared metadata`() {
        assertThrows(IllegalArgumentException::class.java) {
            poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 3)
        }
        assertThrows(IllegalArgumentException::class.java) {
            poisoner.copy(reminders = emptyList())
                .grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 1)
        }
    }

    @Test fun `same reminder label copies remain distinguishable by occurrence`() {
        val tokens = listOf(
            poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 2),
            poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 1),
        ).sorted()

        assertEquals(listOf(1, 2), tokens.map(GrimoireReminderTokenRef::occurrence))
        val seat = GrimoireSeatView(2, RoleId("Spy"), true, tokens)
        assertEquals(2, seat.reminderTokens.size)
    }

    @Test fun `same physical reminder token cannot appear on two grimoire seats`() {
        val token = poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 1)

        assertThrows(IllegalArgumentException::class.java) {
            InformationProposition.GrimoireState(
                listOf(
                    GrimoireSeatView(1, RoleId("Investigator"), true, listOf(token)),
                    GrimoireSeatView(2, RoleId("Spy"), true, listOf(token)),
                ),
            )
        }
    }

    @Test fun `typed rule-backed reminder tokens round trip canonically in spy grimoire`() {
        val token = poisoner.grimoireReminderToken(GrimoireReminderTokenScope.CHARACTER, occurrence = 1)
        val grimoire = InformationProposition.GrimoireState(
            listOf(
                GrimoireSeatView(1, RoleId("Investigator"), true),
                GrimoireSeatView(2, RoleId("Spy"), true, listOf(token)),
            ),
        )

        val json = EpistemicSemanticJson.encode(grimoire)
        assertEquals(grimoire, EpistemicSemanticJson.decodeInformationProposition(json))
        assertTrue(json.contains("\"ruleReminderTokens\""))
        assertTrue(json.contains("\"sourceRole\":\"Poisoner\""))
        assertTrue(json.contains("\"scope\":\"CHARACTER\""))
        assertTrue(json.contains("\"occurrence\":1"))
    }

    @Test fun `nonempty legacy raw reminder tokens require explicit migration`() {
        val legacy = """{"kind":"grimoire-state","seats":[{"alive":true,"displayedRole":"Spy","reminderTokens":["spy"],"seat":2}]}"""

        val error = assertThrows(IllegalArgumentException::class.java) {
            EpistemicSemanticJson.decodeInformationProposition(legacy)
        }

        assertTrue(error.message.orEmpty().contains("migration", ignoreCase = true))
    }

    @Test fun `legacy empty reminder list is safely equivalent to no typed reminder tokens`() {
        val legacy = """{"kind":"grimoire-state","seats":[{"alive":true,"displayedRole":"Spy","reminderTokens":[],"seat":2}]}"""

        assertEquals(
            InformationProposition.GrimoireState(listOf(GrimoireSeatView(2, RoleId("Spy"), true))),
            EpistemicSemanticJson.decodeInformationProposition(legacy),
        )
    }
}
