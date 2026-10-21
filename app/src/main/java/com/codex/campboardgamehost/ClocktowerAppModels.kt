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
}

internal data class ClocktowerRole(
    val team: ClocktowerTeam,
    val zhName: String,
    val enName: String,
    val zhDescription: String,
    val enDescription: String,
)
