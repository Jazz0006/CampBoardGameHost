package com.codex.campboardgamehost

private val clocktowerNightSquareTableActions = setOf(
    ClocktowerNightAction.RedHerring,
    ClocktowerNightAction.Poison,
    ClocktowerNightAction.ButlerMaster,
    ClocktowerNightAction.MonkProtect,
    ClocktowerNightAction.DemonKill,
    ClocktowerNightAction.Ravenkeeper,
    ClocktowerNightAction.FortuneTeller,
    ClocktowerNightAction.Chambermaid,
    ClocktowerNightAction.MayorRedirect,
    ClocktowerNightAction.DemonSuccessor,
)

internal fun clocktowerNightActionOwnsSquareTable(action: ClocktowerNightAction): Boolean =
    action in clocktowerNightSquareTableActions

/** True when the current night step owns the Activity-root host workspace instead of a legacy card. */
internal fun clocktowerNightUsesFullScreenHostSurface(
    isRealAction: Boolean,
    action: ClocktowerNightAction,
    displayKind: ClocktowerDisplayKind,
): Boolean =
    isRealAction &&
        (
            clocktowerNightActionOwnsSquareTable(action) ||
                displayKind != ClocktowerDisplayKind.None ||
                action == ClocktowerNightAction.None
            )
