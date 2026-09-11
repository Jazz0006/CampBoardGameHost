package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.PlanEffectSignature
import com.codex.campboardgamehost.clocktower.domain.QualityTier
import com.codex.campboardgamehost.clocktower.domain.RecommendationPlan
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecision
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerDemonBluffPresentationTest {
    @Test
    fun `manual mode consumes the current storyteller style instead of hidden balanced fallback`() {
        val plans = listOf(
            plan(RecommendationStyle.GENTLE, "Chef", "Empath", "Saint"),
            plan(RecommendationStyle.BALANCED, "Mayor", "Monk", "Undertaker"),
            plan(RecommendationStyle.AGGRESSIVE, "Virgin", "Slayer", "Soldier"),
        )

        val result = demonBluffRoleNamesForPresentation(
            automaticStorytellerInfo = false,
            appliedRoleNames = emptyList(),
            setupPlans = plans,
            preferredManualStyle = RecommendationStyle.AGGRESSIVE,
        )

        assertEquals(listOf("Virgin", "Slayer", "Soldier"), result)
    }

    @Test
    fun `manual Demon bluff fallback is explicitly wired to the unified storyteller style`() {
        val presentationSource = source("ClocktowerDemonBluffPresentation.kt")
        val hostSource = source("clocktower/ui/ClocktowerHostScreen.kt")

        assertFalse(
            presentationSource.contains(
                "preferredManualStyle: RecommendationStyle = RecommendationStyle.BALANCED",
            ),
        )
        assertTrue(presentationSource.contains("storytellerStyle: RecommendationStyle,"))
        assertTrue(hostSource.contains("storytellerStyle = automaticStorytellerStyle,"))
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

    private fun source(relativePath: String): String {
        val relative = Path.of("src/main/java/com/codex/campboardgamehost").resolve(relativePath)
        val fromRoot = Path.of("app").resolve(relative)
        val path = when {
            Files.exists(relative) -> relative
            Files.exists(fromRoot) -> fromRoot
            else -> error("$relativePath source not found from ${Path.of("").toAbsolutePath()}")
        }
        return String(Files.readAllBytes(path), Charsets.UTF_8)
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
