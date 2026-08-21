package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class ActiveRulesetGrimoireReminderTokenTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }

    @Test
    fun `active Trouble Brewing ruleset resolves character and global reminder occurrences`() {
        val ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

        assertEquals(
            GrimoireReminderTokenRef(
                sourceRole = RoleId("Poisoner"),
                scope = GrimoireReminderTokenScope.CHARACTER,
                label = "Poisoned",
                occurrence = 1,
            ),
            ruleset.grimoireReminderToken(
                sourceRole = RoleId("Poisoner"),
                scope = GrimoireReminderTokenScope.CHARACTER,
                occurrence = 1,
            ),
        )
        assertEquals(
            GrimoireReminderTokenRef(
                sourceRole = RoleId("Drunk"),
                scope = GrimoireReminderTokenScope.GLOBAL,
                label = "Is The Drunk",
                occurrence = 1,
            ),
            ruleset.grimoireReminderToken(
                sourceRole = RoleId("Drunk"),
                scope = GrimoireReminderTokenScope.GLOBAL,
                occurrence = 1,
            ),
        )
    }

    @Test
    fun `active ruleset resolver covers every declared reminder occurrence`() {
        val ruleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)

        ruleset.characters.forEach { character ->
            character.reminders.forEachIndexed { index, label ->
                assertEquals(
                    GrimoireReminderTokenRef(
                        sourceRole = character.id,
                        scope = GrimoireReminderTokenScope.CHARACTER,
                        label = label,
                        occurrence = index + 1,
                    ),
                    ruleset.grimoireReminderToken(
                        sourceRole = character.id,
                        scope = GrimoireReminderTokenScope.CHARACTER,
                        occurrence = index + 1,
                    ),
                )
            }
            character.globalReminders.forEachIndexed { index, label ->
                assertEquals(
                    GrimoireReminderTokenRef(
                        sourceRole = character.id,
                        scope = GrimoireReminderTokenScope.GLOBAL,
                        label = label,
                        occurrence = index + 1,
                    ),
                    ruleset.grimoireReminderToken(
                        sourceRole = character.id,
                        scope = GrimoireReminderTokenScope.GLOBAL,
                        occurrence = index + 1,
                    ),
                )
            }
        }
    }

    @Test
    fun `active ruleset resolver fails closed for off script roles`() {
        val noGreaterJoy = catalog.ruleset(ClocktowerScript.NoGreaterJoy)

        assertThrows(IllegalArgumentException::class.java) {
            noGreaterJoy.grimoireReminderToken(
                sourceRole = RoleId("Poisoner"),
                scope = GrimoireReminderTokenScope.CHARACTER,
                occurrence = 1,
            )
        }
    }

    @Test
    fun `active ruleset resolver fails closed outside declared occurrence range`() {
        val troubleBrewing = catalog.ruleset(ClocktowerScript.TroubleBrewing)

        assertThrows(IllegalArgumentException::class.java) {
            troubleBrewing.grimoireReminderToken(
                sourceRole = RoleId("Poisoner"),
                scope = GrimoireReminderTokenScope.CHARACTER,
                occurrence = 2,
            )
        }
    }
}
