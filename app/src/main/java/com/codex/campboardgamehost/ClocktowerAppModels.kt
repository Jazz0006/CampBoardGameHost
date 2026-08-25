package com.codex.campboardgamehost

internal enum class ClocktowerEventType {
    System,
    Phase,
    RoleAction,
    Information,
    UnreliableInformation,
    Nomination,
    Vote,
    Execution,
    Death,
    RoleChange,
    GameEnd,
}

internal data class ClocktowerEvent(
    val sequence: Int,
    val type: ClocktowerEventType,
    val title: String,
    val detail: String,
    val playerNames: List<String>,
    val phase: ClocktowerPhase,
    val round: Int,
)

internal enum class ClocktowerTeam {
    Townsfolk,
    Outsider,
    Minion,
    Demon,
}

internal enum class ClocktowerPhase {
    FirstNight,
    Dawn,
    Day,
    Night,
}

internal enum class ClocktowerDayMode {
    Overview,
    Slayer,
    Artist,
    Klutz,
    Nomination,
    Vote,
    EndConfirm,
    ExecutionResult,
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
    DemonSuccessor,
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
    Grimoire,
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
