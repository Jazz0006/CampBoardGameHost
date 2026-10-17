package com.codex.campboardgamehost

internal enum class GameKind {
    Undercover,
    Clocktower,
}

internal enum class Role {
    Civilian,
    Undercover,
    Blank,
}

internal data class PlayerCard(
    val name: String,
    val role: Role,
    val word: String,
    val roleLabel: String? = null,
    val actualRoleLabel: String? = null,
    val clocktowerTeam: ClocktowerTeam? = null,
    val clocktowerRole: ClocktowerRole? = null,
    val clocktowerShownRole: ClocktowerRole? = null,
    val eliminatedRound: Int? = null,
)

internal data class EliminationRecord(
    val round: Int,
    val playerName: String,
)

internal data class GameOutcome(
    val title: String,
    val summary: String,
    val reason: String,
)
