package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptDefinition
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveGameSaveInvariantValidatorTest {
    private val troubleBrewing = ClocktowerScriptDefinition(
        id = ScriptId("trouble_brewing"),
        name = "Trouble Brewing",
        author = null,
        characterIds = listOf(RoleId("Imp"), RoleId("Empath"), RoleId("Drunk")),
        firstNightOverride = null,
        otherNightOverride = null,
        bootleggerRules = emptyList(),
        source = ClocktowerScriptSource.BUILTIN_OFFICIAL,
        contentHash = "11111111111111111111111111111111",
    )
    private val roleRegistry = WerewolfRoleRegistry.builtIn()

    @Test
    fun `Clocktower session validation accepts an assigned subset from the selected script`() {
        ClocktowerActiveSessionValidator.validateForRecoverySave(
            script = troubleBrewing,
            assignedRoleIds = listOf(RoleId("Imp"), RoleId("Empath")),
        )
    }

    @Test
    fun `Clocktower session validation rejects empty assigned roles`() {
        assertFails {
            ClocktowerActiveSessionValidator.validateForRecoverySave(
                script = troubleBrewing,
                assignedRoleIds = emptyList(),
            )
        }
    }

    @Test
    fun `Clocktower session validation rejects a role outside the selected script`() {
        assertFails {
            ClocktowerActiveSessionValidator.validateForRecoverySave(
                script = troubleBrewing,
                assignedRoleIds = listOf(RoleId("Imp"), RoleId("Sage")),
            )
        }
    }

    @Test
    fun `Werewolf save validation accepts a mechanically exact assigned deck`() {
        WerewolfActiveGameSaveValidator(roleRegistry).validate(
            assignedRoles = listOf(
                Role.Werewolf, Role.Werewolf, Role.Seer, Role.Witch, Role.Hunter,
                Role.Villager, Role.Villager, Role.Villager,
            ),
            werewolfCount = 2,
            includeSeer = true,
            includeWitch = true,
            includeHunter = true,
        )
    }

    @Test
    fun `Werewolf save validation rejects zero werewolves`() {
        assertFails {
            WerewolfActiveGameSaveValidator(roleRegistry).validate(
                assignedRoles = listOf(Role.Villager, Role.Villager, Role.Villager, Role.Villager),
                werewolfCount = 0,
                includeSeer = false,
                includeWitch = false,
                includeHunter = false,
            )
        }
    }

    @Test
    fun `Werewolf save validation rejects an assigned deck that disagrees with mechanics`() {
        assertFails {
            WerewolfActiveGameSaveValidator(roleRegistry).validate(
                assignedRoles = listOf(Role.Werewolf, Role.Seer, Role.Seer, Role.Villager),
                werewolfCount = 1,
                includeSeer = true,
                includeWitch = false,
                includeHunter = false,
            )
        }
    }

    @Test
    fun `Werewolf save validation rejects roles outside the Werewolf registry`() {
        assertFails {
            WerewolfActiveGameSaveValidator(roleRegistry).validate(
                assignedRoles = listOf(Role.Werewolf, Role.Civilian, Role.Villager, Role.Villager),
                werewolfCount = 1,
                includeSeer = false,
                includeWitch = false,
                includeHunter = false,
            )
        }
    }

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected active-game save validation to fail closed.", failed)
    }
}
