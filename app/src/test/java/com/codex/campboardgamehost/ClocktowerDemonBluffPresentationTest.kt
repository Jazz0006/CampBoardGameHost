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

    @Test
    fun `manual bluff selection is legal only and capped at three`() {
        val legal = setOf("Chef", "Empath", "Mayor", "Monk")

        val one = toggleManualDemonBluffSelection(emptyList(), "Chef", legal)
        val two = toggleManualDemonBluffSelection(one, "Empath", legal)
        val three = toggleManualDemonBluffSelection(two, "Mayor", legal)

        assertEquals(listOf("Chef", "Empath", "Mayor"), three)
        assertEquals(three, toggleManualDemonBluffSelection(three, "Monk", legal))
        assertEquals(three, toggleManualDemonBluffSelection(three, "Spy", legal))
        assertEquals(
            listOf("Chef", "Mayor"),
            toggleManualDemonBluffSelection(three, "Empath", legal),
        )
    }

    @Test
    fun `manual bluff selection is ready only for an exact legal triple`() {
        val legal = listOf(role("Chef"), role("Empath"), role("Mayor"), role("Monk"))

        assertTrue(
            manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath", "Mayor"),
                legalRoles = legal,
            ),
        )
        assertTrue(
            !manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath"),
                legalRoles = legal,
            ),
        )
        assertTrue(
            !manualDemonBluffSelectionReady(
                selectedRoleNames = listOf("Chef", "Empath", "Spy"),
                legalRoles = legal,
            ),
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
