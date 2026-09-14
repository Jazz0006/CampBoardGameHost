package com.codex.campboardgamehost

internal enum class ClocktowerNightFullScreenSurface {
    SingleTarget,
    FortuneTeller,
    Chambermaid,
    Ravenkeeper,
    Ruling,
    EvilInformation,
    PairInformation,
    Chef,
    Empath,
    Undertaker,
    Spy,
    Clockmaker,
    Sage,
    PlainInformation,
}

/**
 * The sole presentation-dispatch result for one materialized night step.
 *
 * This owns neither rules legality nor navigation. It prevents the Activity shell and the
 * renderer from independently guessing whether a step owns the host workspace and which concrete
 * surface must render there.
 */
internal sealed interface ClocktowerNightSurfacePlan {
    data object LegacyInline : ClocktowerNightSurfacePlan

    data class FullScreen(
        val surface: ClocktowerNightFullScreenSurface,
    ) : ClocktowerNightSurfacePlan
}

internal val ClocktowerNightSurfacePlan.ownsFullScreenHostSurface: Boolean
    get() = this is ClocktowerNightSurfacePlan.FullScreen

private val pairInformationRoles = setOf("Washerwoman", "Librarian", "Investigator")

/**
 * Resolves one materialized step to one authoritative presentation family.
 *
 * Action identity takes precedence over information identity. A real residual information step
 * always has the explicit PlainInformation fallback, so a full-screen claim without a renderer is
 * not representable by this contract.
 */
internal fun clocktowerNightSurfacePlan(
    step: ClocktowerNightStepUi,
    phase: ClocktowerPhase,
): ClocktowerNightSurfacePlan {
    if (!step.isRealAction) return ClocktowerNightSurfacePlan.LegacyInline

    val presentationRoleEnName = clocktowerNightPresentationRoleEnName(
        stepRoleEnName = step.roleEnName,
        actor = step.actor,
    )

    val actionSurface = when (step.action) {
        ClocktowerNightAction.RedHerring,
        ClocktowerNightAction.Poison,
        ClocktowerNightAction.ButlerMaster,
        ClocktowerNightAction.MonkProtect,
        ClocktowerNightAction.DemonKill -> ClocktowerNightFullScreenSurface.SingleTarget

        ClocktowerNightAction.FortuneTeller -> ClocktowerNightFullScreenSurface.FortuneTeller
        ClocktowerNightAction.Chambermaid -> ClocktowerNightFullScreenSurface.Chambermaid
        ClocktowerNightAction.Ravenkeeper -> ClocktowerNightFullScreenSurface.Ravenkeeper
        ClocktowerNightAction.MayorRedirect,
        ClocktowerNightAction.DemonSuccessor -> ClocktowerNightFullScreenSurface.Ruling

        ClocktowerNightAction.NewDemonIdentity -> ClocktowerNightFullScreenSurface.EvilInformation
        ClocktowerNightAction.None -> null
    }
    if (actionSurface != null) return ClocktowerNightSurfacePlan.FullScreen(actionSurface)

    val informationSurface = when {
        step.displayKind == ClocktowerDisplayKind.EvilInfo ->
            ClocktowerNightFullScreenSurface.EvilInformation

        phase == ClocktowerPhase.FirstNight &&
            presentationRoleEnName in pairInformationRoles &&
            step.manualInformationCandidates.isNotEmpty() ->
            ClocktowerNightFullScreenSurface.PairInformation

        presentationRoleEnName == "Chef" -> ClocktowerNightFullScreenSurface.Chef
        presentationRoleEnName == "Empath" -> ClocktowerNightFullScreenSurface.Empath
        presentationRoleEnName == "Undertaker" -> ClocktowerNightFullScreenSurface.Undertaker
        presentationRoleEnName == "Spy" -> ClocktowerNightFullScreenSurface.Spy
        presentationRoleEnName == "Clockmaker" -> ClocktowerNightFullScreenSurface.Clockmaker
        presentationRoleEnName == "Sage" -> ClocktowerNightFullScreenSurface.Sage
        else -> ClocktowerNightFullScreenSurface.PlainInformation
    }
    return ClocktowerNightSurfacePlan.FullScreen(informationSurface)
}
