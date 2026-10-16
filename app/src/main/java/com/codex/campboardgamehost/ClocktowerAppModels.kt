package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode

internal enum class ClocktowerEventType {
    RoleAction,
    Information,
    UnreliableInformation,
    Nomination,
    Vote,
    Execution,
    Death,
    RoleChange,
}

/**
 * UI narration is not a second Host confirmation. In GLOBAL_V1, mechanical/public actions
 * already have their own accepted Session fact, and private information has its observation.
 * LEGACY_LOCAL still uses revision invalidation for old role-action callbacks.
 */
internal fun clocktowerNarrationNeedsGameRevision(
    type: ClocktowerEventType,
    historyMode: ClocktowerSemanticHistoryMode,
    confirmedRoleNarrationOnly: Boolean = false,
): Boolean = when (type) {
    ClocktowerEventType.Information, ClocktowerEventType.UnreliableInformation -> false
    ClocktowerEventType.Nomination, ClocktowerEventType.Vote ->
        historyMode != ClocktowerSemanticHistoryMode.GLOBAL_V1
    ClocktowerEventType.RoleAction ->
        historyMode != ClocktowerSemanticHistoryMode.GLOBAL_V1 || !confirmedRoleNarrationOnly
    else -> true
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
