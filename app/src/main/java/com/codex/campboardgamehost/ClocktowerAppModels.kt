package com.codex.campboardgamehost

internal enum class ClocktowerTeam {
    Townsfolk,
    Outsider,
    Minion,
    Demon,
}

internal enum class ClocktowerPhase {
    FirstNight,
    Day,
    Night,
}

internal enum class ClocktowerDayMode {
    Overview,
    Slayer,
    Artist,
    Nomination,
}

internal enum class ClocktowerNightAction {
    None,
    RedHerring,
    Poison,
    ButlerMaster,
    MonkProtect,
    Chambermaid,
    FortuneTeller,
    NewDemonIdentity,
    DemonKill,
    MayorRedirect,
    Ravenkeeper,
}

internal enum class ClocktowerDisplayKind {
    None,
    EitherOne,
    Number,
    YesNo,
    RoleReveal,
    Plain,
    EvilInfo,
}

internal enum class ClocktowerScript {
    TroubleBrewing,
    NoGreaterJoy,
    BadMoonRising,
}

internal data class ClocktowerRole(
    val team: ClocktowerTeam,
    val zhName: String,
    val enName: String,
    val zhDescription: String,
    val enDescription: String,
)

// Shared by NGJ/BMR presentation and ability metadata; avoid script-order-dependent lookup.
internal val chambermaidRole = ClocktowerRole(
    ClocktowerTeam.Townsfolk, "侍女", "Chambermaid",
    "每晚选择两名除你以外的存活玩家，得知其中有几人因能力醒来。",
    "Each night, choose 2 alive players (not yourself): you learn how many woke tonight due to their ability.",
)
