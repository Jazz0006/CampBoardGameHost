package com.codex.campboardgamehost

internal enum class GameKind {
    Undercover,
    Werewolf,
    Clocktower,
}

internal enum class Role {
    Civilian,
    Undercover,
    Blank,
    Villager,
    Werewolf,
    Seer,
    Witch,
    Hunter,
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
    val note: String? = null,
)

internal data class GameOutcome(
    val title: String,
    val summary: String,
    val reason: String,
)

internal data class SavedGamePreview(
    val title: String,
    val subtitle: String,
    val savedAtLabel: String?,
)

internal data class ArchivedGameReview(
    val id: Long,
    val archivedAtMillis: Long,
    val gameKind: GameKind,
    val round: Int,
    val cards: List<PlayerCard>,
    val records: List<EliminationRecord>,
    val events: List<ClocktowerEvent>,
    val outcome: GameOutcome?,
)
