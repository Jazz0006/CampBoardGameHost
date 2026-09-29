package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDemonBluffPresentationTest {
    @Test
    fun `uncommitted bluff recommendation is not player presentation state`() {
        val plans = listOf(
            plan(RecommendationStyle.GENTLE, "Chef", "Empath", "Saint"),
            plan(RecommendationStyle.BALANCED, "Mayor", "Monk", "Undertaker"),
            plan(RecommendationStyle.AGGRESSIVE, "Virgin", "Slayer", "Soldier"),
        )

        val result = demonBluffRoleNamesForPresentation(
            automaticStorytellerInfo = false,
            appliedRoleNames = emptyList(),
            setupPlans = plans,
            storytellerStyle = RecommendationStyle.AGGRESSIVE,
        )

        assertNull(result)
    }

    @Test
    fun `demon info barrier commits only one current legal recommended triple`() {
        val plans = listOf(
            plan(RecommendationStyle.BALANCED, "Mayor", "Monk", "Undertaker"),
        )
        val legal = listOf(role("Mayor"), role("Monk"), role("Undertaker"), role("Chef"))

        assertEquals(
            listOf("Mayor", "Monk", "Undertaker"),
            demonBluffRoleNamesToCommitAtBarrier(
                isDemonInfoStep = true,
                isRealAction = true,
                committedRoleNames = emptyList(),
                setupPlans = plans,
                storytellerStyle = RecommendationStyle.BALANCED,
                legalRoles = legal,
            ),
        )
        assertNull(
            demonBluffRoleNamesToCommitAtBarrier(
                isDemonInfoStep = false,
                isRealAction = true,
                committedRoleNames = emptyList(),
                setupPlans = plans,
                storytellerStyle = RecommendationStyle.BALANCED,
                legalRoles = legal,
            ),
        )
        assertNull(
            demonBluffRoleNamesToCommitAtBarrier(
                isDemonInfoStep = true,
                isRealAction = false,
                committedRoleNames = emptyList(),
                setupPlans = plans,
                storytellerStyle = RecommendationStyle.BALANCED,
                legalRoles = legal,
            ),
        )
        assertNull(
            demonBluffRoleNamesToCommitAtBarrier(
                isDemonInfoStep = true,
                isRealAction = true,
                committedRoleNames = listOf("Chef", "Empath", "Saint"),
                setupPlans = plans,
                storytellerStyle = RecommendationStyle.BALANCED,
                legalRoles = legal,
            ),
        )
        assertNull(
            demonBluffRoleNamesToCommitAtBarrier(
                isDemonInfoStep = true,
                isRealAction = true,
                committedRoleNames = emptyList(),
                setupPlans = emptyList(),
                storytellerStyle = RecommendationStyle.BALANCED,
                legalRoles = legal,
            ),
        )
        assertNull(
            demonBluffRoleNamesToCommitAtBarrier(
                isDemonInfoStep = true,
                isRealAction = true,
                committedRoleNames = emptyList(),
                setupPlans = listOf(
                    plan(RecommendationStyle.BALANCED, "Mayor", "Monk", "Soldier"),
                ),
                storytellerStyle = RecommendationStyle.BALANCED,
                legalRoles = legal,
            ),
        )
    }

    @Test
    fun `applied bluff commitment wins over later recommendation changes`() {
        val applied = listOf("Mayor", "Monk", "Undertaker")
        val changedPlans = listOf(
            plan(RecommendationStyle.BALANCED, "Virgin", "Slayer", "Soldier"),
        )

        val automatic = demonBluffRoleNamesForPresentation(
            automaticStorytellerInfo = true,
            appliedRoleNames = applied,
            setupPlans = changedPlans,
            storytellerStyle = RecommendationStyle.BALANCED,
        )
        val manual = demonBluffRoleNamesForPresentation(
            automaticStorytellerInfo = false,
            appliedRoleNames = applied,
            setupPlans = changedPlans,
            storytellerStyle = RecommendationStyle.BALANCED,
        )

        assertEquals(applied, automatic)
        assertEquals(applied, manual)
    }

    @Test
    fun `exact three recommended roles resolve in recommendation order`() {
        val legal = listOf(
            role("Chef"),
            role("Mayor"),
            role("Monk"),
            role("Undertaker"),
        )

        val result = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk", "Undertaker"),
            legalRoles = legal,
        )

        assertEquals(
            listOf("Mayor", "Monk", "Undertaker"),
            (result as DemonBluffPresentationResolution.Ready).roles.map { it.enName },
        )
    }

    @Test
    fun `missing recommendation stays pending and never becomes first three legal roles`() {
        val result = resolveDemonBluffPresentation(
            recommendedRoleNames = null,
            legalRoles = listOf(role("Chef"), role("Empath"), role("Fortune Teller"), role("Mayor")),
        )

        assertTrue(result is DemonBluffPresentationResolution.Pending)
    }

    @Test
    fun `partial or unresolved recommendation is invalid and never silently substituted`() {
        val legal = listOf(role("Chef"), role("Empath"), role("Mayor"), role("Monk"))

        val partial = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk"),
            legalRoles = legal,
        )
        val unresolved = resolveDemonBluffPresentation(
            recommendedRoleNames = listOf("Mayor", "Monk", "Undertaker"),
            legalRoles = legal,
        )

        assertTrue(partial is DemonBluffPresentationResolution.Invalid)
        assertEquals(
            listOf("Undertaker"),
            (unresolved as DemonBluffPresentationResolution.Invalid).unresolvedRoleNames,
        )
    }

    private fun plan(
        style: RecommendationStyle,
        first: String,
        second: String,
        third: String,
    ) = RecommendationPlan(
        decisions = listOf(
            StorytellerDecision.DemonBluffs(
                listOf(RoleId(first), RoleId(second), RoleId(third)),
            ),
        ),
        observations = emptyList(),
        qualityTier = QualityTier.RECOMMENDED,
        style = style,
        totalScore = 0,
        scoreItems = emptyList(),
        warnings = emptyList(),
        effectSignature = PlanEffectSignature(
            demonBluffs = setOf(RoleId(first), RoleId(second), RoleId(third)),
        ),
    )

    private fun role(enName: String) = ClocktowerRole(
        team = ClocktowerTeam.Townsfolk,
        zhName = enName,
        enName = enName,
        zhDescription = "",
        enDescription = "",
    )
}
