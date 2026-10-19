package com.codex.campboardgamehost

internal data class PlayerCard(
    val name: String,
    val roleLabel: String? = null,
    val actualRoleLabel: String? = null,
    val clocktowerTeam: ClocktowerTeam? = null,
    val clocktowerRole: ClocktowerRole? = null,
    val clocktowerShownRole: ClocktowerRole? = null,
    val eliminatedRound: Int? = null,
)

internal data class GameOutcome(
    val title: String,
    val summary: String,
    val reason: String,
)
