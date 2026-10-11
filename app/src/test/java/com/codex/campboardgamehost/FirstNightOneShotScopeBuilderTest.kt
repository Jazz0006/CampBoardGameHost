package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightOneShotScopeBuilderTest {
    private fun role(id: String): ClocktowerRole =
        completeTroubleBrewingRoles.single { it.enName == id }

    private fun cards(): List<PlayerCard> = listOf(
        "Chef", "Empath", "Investigator", "Fortune Teller",
        "Poisoner", "Imp", "Saint", "Soldier",
    ).mapIndexed { i, id ->
        val r = role(id)
        PlayerCard(
            name = "P${i + 1}",
            role = Role.Civilian,
            word = "",
            clocktowerRole = r,
            clocktowerShownRole = r,
            clocktowerTeam = r.team,
        )
    }

    private fun step(
        actor: PlayerCard, roleName: String,
        options: List<ClocktowerDisplayOption> = emptyList(),
    ): ClocktowerNightStepUi = ClocktowerNightStepUi(
        title = roleName,
        actor = actor,
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = null,
        explanation = "",
        roleEnName = roleName,
        manualInformationCandidates = options,
    )

    @Test
    fun `real eight-player opening includes information bluffs and deferred player choices`() {
        val roster = cards()
        val legalOptions = listOf(
            ClocktowerDisplayOption(
                label = "Baron at 5/7",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayTitle = "Investigator",
                displayPrimary = "Baron",
                displaySecondary = "5,7",
                displayFooter = null,
                isTruthful = true,
            ),
            ClocktowerDisplayOption(
                label = "Poisoner at 5/6",
                displayKind = ClocktowerDisplayKind.EitherOne,
                displayTitle = "Investigator",
                displayPrimary = "Poisoner",
                displaySecondary = "5,6",
                displayFooter = null,
                isTruthful = true,
            ),
        )
        val scope = clocktowerFirstNightOneShotScope(
            "opening", 0, 0, ClocktowerScript.TroubleBrewing, roster,
            listOf(
                step(roster[2], "Investigator", legalOptions),
                step(roster[3], "Fortune Teller"),
            ),
        )
        val investigator = scope.availableDecisions.single {
            it.decisionId == "first-night:Investigator:seat-3"
        }
        assertEquals(2, investigator.legalCandidateIds.size)
        assertTrue(investigator.legalCandidateIds.all { it.startsWith("fn-") })
        assertTrue(investigator.candidateDescriptions.values.any { "Poisoner" in it })
        assertEquals(3, scope.legalDemonBluffRoleIds!!.distinct().take(3).size)
        assertFalse("Imp" in scope.legalDemonBluffRoleIds)
        assertFalse("Investigator" in scope.legalDemonBluffRoleIds)
        assertTrue("first-night:Fortune Teller:seat-4" in scope.deferredDecisionIds)
        assertTrue("dependency.poisoner-target-unconfirmed" in scope.deferredDecisionIds)
        assertTrue(scope.availableDecisions.any { it.family == "RED_HERRING" })
    }

    @Test
    fun `fixed Chef number remains visible even without discretionary Manual candidates`() {
        val roster = cards()
        val result = ClocktowerDisplayOption(
            label = "fixed",
            displayKind = ClocktowerDisplayKind.Number,
            displayTitle = "Chef",
            displayPrimary = "1",
            displaySecondary = null,
            displayFooter = null,
        )
        val fixedStep = step(roster[0], "Chef").copy(
            displayPrimary = "1",
            legacyInformationCandidates = listOf(result),
        )
        val scope = clocktowerFirstNightOneShotScope(
            "deterministic", 0, 0, ClocktowerScript.TroubleBrewing, roster,
            listOf(fixedStep),
        )
        val chef = scope.availableDecisions.single {
            it.decisionId == "first-night:Chef:seat-1"
        }
        assertEquals(listOf(clocktowerOneShotCandidateId(result)), chef.legalCandidateIds)
        assertFalse(chef.decisionId in scope.deferredDecisionIds)
    }

    @Test
    fun `same Host information gives the same opaque candidate identity`() {
        val option = ClocktowerDisplayOption(
            label = "anything",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Investigator",
            displayPrimary = "Poisoner",
            displaySecondary = "P5 + P7",
            displayFooter = null,
        )
        assertEquals(clocktowerOneShotCandidateId(option), clocktowerOneShotCandidateId(option.copy()))
        assertFalse(clocktowerOneShotCandidateId(option) ==
            clocktowerOneShotCandidateId(option.copy(displayPrimary = "Baron")))
    }
}
