package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WerewolfRegistryFlowPlannerTest {
    private val roleRegistry = WerewolfRoleRegistry.builtIn()
    private val boardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry)
    private val planner = WerewolfFlowPlanner()

    @Test
    fun `built-in role registry owns legacy wake and action metadata`() {
        assertEquals(
            setOf(
                WerewolfRoleIds.VILLAGER,
                WerewolfRoleIds.WEREWOLF,
                WerewolfRoleIds.SEER,
                WerewolfRoleIds.WITCH,
                WerewolfRoleIds.HUNTER,
            ),
            roleRegistry.definitions.map { it.id }.toSet(),
        )

        val villager = roleRegistry.require(WerewolfRoleIds.VILLAGER)
        assertEquals(Role.Villager, villager.legacyRole)
        assertNull(villager.interaction)

        assertRoleInteraction(
            roleId = WerewolfRoleIds.WEREWOLF,
            legacyRole = Role.Werewolf,
            legacyStep = WerewolfJudgeStep.Wolves,
            order = 10,
            kind = WerewolfInteractionKind.ROLE_ACTION,
        )
        assertRoleInteraction(
            roleId = WerewolfRoleIds.SEER,
            legacyRole = Role.Seer,
            legacyStep = WerewolfJudgeStep.Seer,
            order = 20,
            kind = WerewolfInteractionKind.ROLE_ACTION,
        )
        assertRoleInteraction(
            roleId = WerewolfRoleIds.WITCH,
            legacyRole = Role.Witch,
            legacyStep = WerewolfJudgeStep.Witch,
            order = 30,
            kind = WerewolfInteractionKind.ROLE_ACTION,
        )
        assertRoleInteraction(
            roleId = WerewolfRoleIds.HUNTER,
            legacyRole = Role.Hunter,
            legacyStep = WerewolfJudgeStep.Hunter,
            order = 40,
            kind = WerewolfInteractionKind.ROLE_STATUS,
        )
    }

    @Test
    fun `built-in boards preserve every current legacy template composition`() {
        assertEquals(werewolfTemplates.size, boardRegistry.definitions.size)

        werewolfTemplates.forEach { template ->
            val board = boardRegistry.require(WerewolfBoardId("classic_${template.playerCount}"))
            assertEquals(template.playerCount, board.playerCount)
            assertEquals(expectedDeck(template), board.roleDeck)
            assertTrue(board.contentHash.matches(Regex("[0-9a-f]{32}")))
        }
    }

    @Test
    fun `classic eight player flow is legacy step order equivalent`() {
        val board = boardRegistry.require(WerewolfBoardId("classic_8"))

        assertEquals(
            listOf(
                WerewolfJudgeStep.Wolves,
                WerewolfJudgeStep.Seer,
                WerewolfJudgeStep.Witch,
                WerewolfJudgeStep.Hunter,
                WerewolfJudgeStep.Dawn,
                WerewolfJudgeStep.DayVote,
            ),
            planner.plan(board, roleRegistry).map { it.legacyStep },
        )
    }

    @Test
    fun `optional legacy steps are derived from board roles instead of a fixed list`() {
        val board = boardRegistry.require(WerewolfBoardId("classic_6"))

        assertEquals(
            listOf(
                WerewolfJudgeStep.Wolves,
                WerewolfJudgeStep.Seer,
                WerewolfJudgeStep.Witch,
                WerewolfJudgeStep.Dawn,
                WerewolfJudgeStep.DayVote,
            ),
            planner.plan(board, roleRegistry).map { it.legacyStep },
        )
    }

    @Test
    fun `new board composed only from registered roles needs no planner branch`() {
        val customBoard = WerewolfBoardDefinition.create(
            id = WerewolfBoardId("custom_8_hunter_no_witch"),
            name = "Custom 8 Hunter",
            roleDeck = linkedMapOf(
                WerewolfRoleIds.WEREWOLF to 2,
                WerewolfRoleIds.SEER to 1,
                WerewolfRoleIds.HUNTER to 1,
                WerewolfRoleIds.VILLAGER to 4,
            ),
        )

        val interactions = planner.plan(customBoard, roleRegistry)

        assertEquals(
            listOf(
                WerewolfJudgeStep.Wolves,
                WerewolfJudgeStep.Seer,
                WerewolfJudgeStep.Hunter,
                WerewolfJudgeStep.Dawn,
                WerewolfJudgeStep.DayVote,
            ),
            interactions.map { it.legacyStep },
        )
        assertEquals(interactions.size, interactions.map { it.id }.distinct().size)
    }

    @Test
    fun `house rule options are separate from board composition identity`() {
        val board = boardRegistry.require(WerewolfBoardId("classic_8"))
        val noLastWords = WerewolfRuleOptions(lastWordsMode = LastWordsMode.None)
        val alwaysLastWords = WerewolfRuleOptions(lastWordsMode = LastWordsMode.Always)

        assertNotEquals(noLastWords, alwaysLastWords)
        assertEquals(8, board.playerCount)
        assertFalse(
            File("src/main/java/com/codex/campboardgamehost/werewolf/WerewolfBoardRegistry.kt")
                .readText(Charsets.UTF_8)
                .contains("LastWordsMode"),
        )
    }

    @Test
    fun `flow planner core stays role-name and board-name agnostic`() {
        val source = File(
            "src/main/java/com/codex/campboardgamehost/werewolf/WerewolfFlowPlanner.kt",
        ).readText(Charsets.UTF_8)

        listOf(
            "Role.Werewolf",
            "Role.Seer",
            "Role.Witch",
            "Role.Hunter",
            "WerewolfJudgeStep.Wolves",
            "WerewolfJudgeStep.Seer",
            "WerewolfJudgeStep.Witch",
            "WerewolfJudgeStep.Hunter",
            "classic_4",
            "classic_8",
            "classic_12",
        ).forEach { forbidden ->
            assertFalse("WerewolfFlowPlanner core must not hardcode $forbidden", forbidden in source)
        }
    }

    private fun assertRoleInteraction(
        roleId: WerewolfRoleId,
        legacyRole: Role,
        legacyStep: WerewolfJudgeStep,
        order: Int,
        kind: WerewolfInteractionKind,
    ) {
        val definition = roleRegistry.require(roleId)
        assertEquals(legacyRole, definition.legacyRole)
        val interaction = requireNotNull(definition.interaction)
        assertEquals(legacyStep, interaction.legacyStep)
        assertEquals(order, interaction.order)
        assertEquals(kind, interaction.kind)
    }

    private fun expectedDeck(template: WerewolfTemplate): Map<WerewolfRoleId, Int> {
        val specialCount = listOf(template.includeSeer, template.includeWitch, template.includeHunter).count { it }
        val villagerCount = template.playerCount - template.werewolfCount - specialCount
        return linkedMapOf<WerewolfRoleId, Int>().apply {
            put(WerewolfRoleIds.WEREWOLF, template.werewolfCount)
            if (template.includeSeer) put(WerewolfRoleIds.SEER, 1)
            if (template.includeWitch) put(WerewolfRoleIds.WITCH, 1)
            if (template.includeHunter) put(WerewolfRoleIds.HUNTER, 1)
            if (villagerCount > 0) put(WerewolfRoleIds.VILLAGER, villagerCount)
        }
    }
}
