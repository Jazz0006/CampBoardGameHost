package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerNightFullScreenOwnershipTest {
    @Test
    fun `every real action resolves to one typed full screen surface`() {
        val expectedByAction = mapOf(
            ClocktowerNightAction.RedHerring to ClocktowerNightFullScreenSurface.SingleTarget,
            ClocktowerNightAction.Poison to ClocktowerNightFullScreenSurface.SingleTarget,
            ClocktowerNightAction.ButlerMaster to ClocktowerNightFullScreenSurface.SingleTarget,
            ClocktowerNightAction.MonkProtect to ClocktowerNightFullScreenSurface.SingleTarget,
            ClocktowerNightAction.DemonKill to ClocktowerNightFullScreenSurface.SingleTarget,
            ClocktowerNightAction.FortuneTeller to ClocktowerNightFullScreenSurface.FortuneTeller,
            ClocktowerNightAction.Chambermaid to ClocktowerNightFullScreenSurface.Chambermaid,
            ClocktowerNightAction.Ravenkeeper to ClocktowerNightFullScreenSurface.Ravenkeeper,
            ClocktowerNightAction.MayorRedirect to ClocktowerNightFullScreenSurface.Ruling,
            ClocktowerNightAction.DemonSuccessor to ClocktowerNightFullScreenSurface.Ruling,
            ClocktowerNightAction.NewDemonIdentity to ClocktowerNightFullScreenSurface.EvilInformation,
        )
        assertEquals(
            ClocktowerNightAction.entries.filterNotTo(linkedSetOf()) { it == ClocktowerNightAction.None },
            expectedByAction.keys,
        )

        expectedByAction.forEach { (action, surface) ->
            assertEquals(
                ClocktowerNightSurfacePlan.FullScreen(surface),
                clocktowerNightSurfacePlan(step(action = action), ClocktowerPhase.Night),
            )
        }
    }

    @Test
    fun `real information families resolve through one exhaustive typed plan`() {
        val expectedByRole = mapOf(
            "Clockmaker" to ClocktowerNightFullScreenSurface.Clockmaker,
            "Chef" to ClocktowerNightFullScreenSurface.Chef,
            "Empath" to ClocktowerNightFullScreenSurface.Empath,
            "Undertaker" to ClocktowerNightFullScreenSurface.Undertaker,
            "Spy" to ClocktowerNightFullScreenSurface.Spy,
            "Sage" to ClocktowerNightFullScreenSurface.Sage,
        )

        expectedByRole.forEach { (role, surface) ->
            assertEquals(
                ClocktowerNightSurfacePlan.FullScreen(surface),
                clocktowerNightSurfacePlan(step(roleEnName = role), ClocktowerPhase.FirstNight),
            )
        }
        assertEquals(
            ClocktowerNightSurfacePlan.FullScreen(ClocktowerNightFullScreenSurface.EvilInformation),
            clocktowerNightSurfacePlan(
                step(displayKind = ClocktowerDisplayKind.EvilInfo),
                ClocktowerPhase.FirstNight,
            ),
        )
    }

    @Test
    fun `pair information requires a real first night manual domain and otherwise has plain fallback`() {
        val candidate = ClocktowerDisplayOption(
            label = "pair",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Investigator information",
            displayPrimary = "Poisoner",
            displaySecondary = "2 3",
            displayFooter = null,
        )

        assertEquals(
            ClocktowerNightSurfacePlan.FullScreen(ClocktowerNightFullScreenSurface.PairInformation),
            clocktowerNightSurfacePlan(
                step(roleEnName = "Investigator", manualInformationCandidates = listOf(candidate)),
                ClocktowerPhase.FirstNight,
            ),
        )
        assertEquals(
            ClocktowerNightSurfacePlan.FullScreen(ClocktowerNightFullScreenSurface.PlainInformation),
            clocktowerNightSurfacePlan(step(roleEnName = "Investigator"), ClocktowerPhase.FirstNight),
        )
        assertEquals(
            ClocktowerNightSurfacePlan.FullScreen(ClocktowerNightFullScreenSurface.PlainInformation),
            clocktowerNightSurfacePlan(
                step(roleEnName = "Investigator", manualInformationCandidates = listOf(candidate)),
                ClocktowerPhase.Night,
            ),
        )
    }

    @Test
    fun `residual real information has an explicit plain surface while non-real step stays legacy`() {
        assertEquals(
            ClocktowerNightSurfacePlan.FullScreen(ClocktowerNightFullScreenSurface.PlainInformation),
            clocktowerNightSurfacePlan(step(), ClocktowerPhase.Night),
        )
        assertEquals(
            ClocktowerNightSurfacePlan.LegacyInline,
            clocktowerNightSurfacePlan(step(isRealAction = false), ClocktowerPhase.Night),
        )
    }

    @Test
    fun `new demon identity uses private read only square table in beginner mode`() {
        val presentation = clocktowerEvilInfoSquareTablePresentation(
            step = newDemonIdentityStep(),
            actorSeat = 3,
            wakeInstruction = "Wake player 3",
            beginnerMode = true,
        )

        assertNotNull(presentation)
        assertEquals(3, presentation?.actorSeat)
        assertTrue(presentation?.showPlayerDisplayAction == true)
        assertFalse(presentation?.showHostDetails ?: true)
    }

    @Test
    fun `new demon identity player handoff projects role reveal`() {
        val displayStep = clocktowerPlayerDisplayStep(newDemonIdentityStep())

        assertEquals(ClocktowerDisplayKind.RoleReveal, displayStep.displayKind)
        assertEquals("You are now the Imp", displayStep.displayPrimary)
    }

    private fun step(
        isRealAction: Boolean = true,
        action: ClocktowerNightAction = ClocktowerNightAction.None,
        displayKind: ClocktowerDisplayKind = ClocktowerDisplayKind.None,
        roleEnName: String? = null,
        manualInformationCandidates: List<ClocktowerDisplayOption> = emptyList(),
    ): ClocktowerNightStepUi = ClocktowerNightStepUi(
        title = roleEnName ?: action.name,
        actor = null,
        isRealAction = isRealAction,
        reason = "",
        storytellerAction = "",
        tellPlayer = null,
        explanation = "",
        action = action,
        displayKind = displayKind,
        roleEnName = roleEnName,
        manualInformationCandidates = manualInformationCandidates,
    )

    private fun newDemonIdentityStep(): ClocktowerNightStepUi = step(
        action = ClocktowerNightAction.NewDemonIdentity,
    ).copy(
        title = "New Demon",
        storytellerAction = "Wake the new Demon",
        tellPlayer = "You are now the Imp",
        displayTitle = "New Demon Identity",
    )
}
