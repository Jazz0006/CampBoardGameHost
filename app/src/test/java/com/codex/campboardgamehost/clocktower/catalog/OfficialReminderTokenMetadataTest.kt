package com.codex.campboardgamehost.clocktower.catalog

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.File

class OfficialReminderTokenMetadataTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `official reminder metadata pins the audited TPI source revision`() {
        assertEquals(
            "f10cd02e3401af227ce406287eaae7bb99a06a42",
            OfficialReminderTokenMetadata.SOURCE_REVISION,
        )
    }

    @Test
    fun `Trouble Brewing built in ruleset carries complete official reminder metadata`() {
        val actual = catalog.ruleset(ClocktowerScript.TroubleBrewing)
            .characters
            .associate { it.id to ReminderLists(it.reminders, it.globalReminders) }

        assertEquals(
            mapOf(
                RoleId("Washerwoman") to ReminderLists(listOf("Townsfolk", "Wrong")),
                RoleId("Librarian") to ReminderLists(listOf("Outsider", "Wrong")),
                RoleId("Investigator") to ReminderLists(listOf("Minion", "Wrong")),
                RoleId("Chef") to ReminderLists(),
                RoleId("Empath") to ReminderLists(),
                RoleId("Fortune Teller") to ReminderLists(listOf("Red Herring")),
                RoleId("Undertaker") to ReminderLists(listOf("Died Today")),
                RoleId("Monk") to ReminderLists(listOf("Safe")),
                RoleId("Ravenkeeper") to ReminderLists(),
                RoleId("Virgin") to ReminderLists(listOf("No Ability")),
                RoleId("Slayer") to ReminderLists(listOf("No Ability")),
                RoleId("Soldier") to ReminderLists(),
                RoleId("Mayor") to ReminderLists(),
                RoleId("Butler") to ReminderLists(listOf("Master")),
                RoleId("Drunk") to ReminderLists(global = listOf("Is The Drunk")),
                RoleId("Recluse") to ReminderLists(),
                RoleId("Saint") to ReminderLists(),
                RoleId("Poisoner") to ReminderLists(listOf("Poisoned")),
                RoleId("Spy") to ReminderLists(),
                RoleId("Scarlet Woman") to ReminderLists(listOf("Is The Demon")),
                RoleId("Baron") to ReminderLists(),
                RoleId("Imp") to ReminderLists(listOf("Dead")),
            ),
            actual,
        )
    }

    @Test
    fun `No Greater Joy built in ruleset carries official reminder metadata for every script role`() {
        val actual = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
            .characters
            .associate { it.id to ReminderLists(it.reminders, it.globalReminders) }

        assertEquals(
            mapOf(
                RoleId("Clockmaker") to ReminderLists(),
                RoleId("Investigator") to ReminderLists(listOf("Minion", "Wrong")),
                RoleId("Empath") to ReminderLists(),
                RoleId("Chambermaid") to ReminderLists(),
                RoleId("Artist") to ReminderLists(listOf("No Ability")),
                RoleId("Sage") to ReminderLists(),
                RoleId("Drunk") to ReminderLists(global = listOf("Is The Drunk")),
                RoleId("Klutz") to ReminderLists(),
                RoleId("Baron") to ReminderLists(),
                RoleId("Scarlet Woman") to ReminderLists(listOf("Is The Demon")),
                RoleId("Imp") to ReminderLists(listOf("Dead")),
            ),
            actual,
        )
    }

    @Test
    fun `character reminder occurrence order is part of ruleset content identity`() {
        val original = character(reminders = listOf("First", "Second"))
        val reordered = original.copy(reminders = listOf("Second", "First"))

        assertNotEquals(hashOf(original), hashOf(reordered))
    }

    @Test
    fun `global reminder occurrence order is part of ruleset content identity`() {
        val original = character(globalReminders = listOf("First Global", "Second Global"))
        val reordered = original.copy(globalReminders = listOf("Second Global", "First Global"))

        assertNotEquals(hashOf(original), hashOf(reordered))
    }

    private fun character(
        reminders: List<String> = emptyList(),
        globalReminders: List<String> = emptyList(),
    ) = ClocktowerCharacterDefinition(
        id = RoleId("Test Role"),
        externalId = "testrole",
        name = "Test Role",
        team = ClocktowerCatalogTeam.TOWNSFOLK,
        abilityText = "Test ability.",
        reminders = reminders,
        globalReminders = globalReminders,
        automationCoverage = RuleCoverage.PARTIAL,
    )

    private fun hashOf(character: ClocktowerCharacterDefinition): String =
        ClocktowerCatalogContentHasher.hash(
            characters = listOf(character),
            firstNightOverride = null,
            otherNightOverride = null,
            bootleggerRules = emptyList(),
        )

    private data class ReminderLists(
        val character: List<String> = emptyList(),
        val global: List<String> = emptyList(),
    )
}
