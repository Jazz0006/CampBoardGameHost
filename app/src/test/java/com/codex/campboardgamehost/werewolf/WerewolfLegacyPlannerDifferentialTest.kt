package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WerewolfLegacyPlannerDifferentialTest {
    private val roleRegistry = WerewolfRoleRegistry.builtIn()
    private val boardRegistry = WerewolfBoardRegistry.builtIn(roleRegistry)
    private val planner = WerewolfFlowPlanner()

    private val legacyJudgeSource by lazy {
        File("src/main/java/com/codex/campboardgamehost/werewolf/WerewolfHostScreen.kt")
            .readText(Charsets.UTF_8)
    }

    @Test
    fun `production legacy step builder remains the shadow differential anchor`() {
        val start = legacyJudgeSource.indexOf("val steps = buildList {")
        val end = legacyJudgeSource.indexOf("val currentIndex = stepIndex.coerceIn", start.coerceAtLeast(0))
        assertTrue("Legacy Werewolf step builder must remain discoverable", start >= 0 && end > start)

        val stepBuilder = legacyJudgeSource.substring(start, end)
        listOf(
            "add(WerewolfJudgeStep.Wolves)",
            "if (cards.any { it.role == Role.Seer }) add(WerewolfJudgeStep.Seer)",
            "if (cards.any { it.role == Role.Witch }) add(WerewolfJudgeStep.Witch)",
            "if (cards.any { it.role == Role.Hunter }) add(WerewolfJudgeStep.Hunter)",
            "add(WerewolfJudgeStep.Dawn)",
            "add(WerewolfJudgeStep.DayVote)",
        ).forEach { fragment ->
            assertTrue("Legacy Werewolf flow contract moved or changed: $fragment", fragment in stepBuilder)
        }

        // Current production includes a role step when that role exists in dealt cards, even if the
        // player has since died. S3 is structural migration and must not silently change that rule.
        assertFalse("Legacy role-step eligibility unexpectedly became alive-only", "eliminatedRound" in stepBuilder)
    }

    @Test
    fun `every current classic template is legacy-order equivalent`() {
        werewolfTemplates.forEach { template ->
            val board = boardRegistry.require(WerewolfBoardId("classic_${template.playerCount}"))
            val actual = planner.plan(board, roleRegistry).map { it.legacyStep }

            assertEquals(
                "Legacy/planner mismatch for ${template.playerCount}-player template",
                legacyStepsFor(template),
                actual,
            )
        }
    }

    @Test
    fun `shadow interaction identity is deterministic for equivalent board content`() {
        val first = WerewolfBoardDefinition.create(
            id = WerewolfBoardId("identity_a"),
            name = "Identity A",
            roleDeck = linkedMapOf(
                WerewolfRoleIds.WEREWOLF to 2,
                WerewolfRoleIds.SEER to 1,
                WerewolfRoleIds.WITCH to 1,
                WerewolfRoleIds.HUNTER to 1,
                WerewolfRoleIds.VILLAGER to 3,
            ),
        )
        val second = WerewolfBoardDefinition.create(
            id = WerewolfBoardId("identity_b"),
            name = "Identity B",
            roleDeck = linkedMapOf(
                WerewolfRoleIds.VILLAGER to 3,
                WerewolfRoleIds.HUNTER to 1,
                WerewolfRoleIds.WITCH to 1,
                WerewolfRoleIds.SEER to 1,
                WerewolfRoleIds.WEREWOLF to 2,
            ),
        )

        assertEquals(first.contentHash, second.contentHash)
        assertEquals(
            planner.plan(first, roleRegistry).map { it.id },
            planner.plan(second, roleRegistry).map { it.id },
        )
    }

    @Test
    fun `board definition rejects non-positive role counts instead of silently rewriting composition`() {
        val failure = runCatching {
            WerewolfBoardDefinition.create(
                id = WerewolfBoardId("invalid_count"),
                name = "Invalid Count",
                roleDeck = linkedMapOf(
                    WerewolfRoleIds.WEREWOLF to 2,
                    WerewolfRoleIds.SEER to 0,
                    WerewolfRoleIds.VILLAGER to 6,
                ),
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    private fun legacyStepsFor(template: WerewolfTemplate): List<WerewolfJudgeStep> = buildList {
        add(WerewolfJudgeStep.Wolves)
        if (template.includeSeer) add(WerewolfJudgeStep.Seer)
        if (template.includeWitch) add(WerewolfJudgeStep.Witch)
        if (template.includeHunter) add(WerewolfJudgeStep.Hunter)
        add(WerewolfJudgeStep.Dawn)
        add(WerewolfJudgeStep.DayVote)
    }
}
