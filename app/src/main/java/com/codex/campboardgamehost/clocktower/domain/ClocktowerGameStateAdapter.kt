package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.ClocktowerTeam
import com.codex.campboardgamehost.PlayerCard

internal fun List<PlayerCard>.toClocktowerPlayerStates(
    poisonedPlayerName: String? = null,
): List<PlayerState> = mapIndexed { index, card ->
    val role = requireNotNull(card.clocktowerRole) {
        "Player ${card.name} does not have a Clocktower role."
    }
    val team = requireNotNull(card.clocktowerTeam) {
        "Player ${card.name} does not have a Clocktower team."
    }
    PlayerState(
        seat = index + 1,
        name = card.name,
        actualRole = RoleId(role.enName),
        actualAlignment = team.toDomainAlignment(),
        actualType = team.toDomainCharacterType(),
        shownRole = card.clocktowerShownRole?.enName?.let(::RoleId),
        alive = card.eliminatedRound == null,
        poisoned = card.name == poisonedPlayerName && card.eliminatedRound == null,
    )
}

internal fun List<PlayerCard>.toClocktowerGameState(
    script: ClocktowerScript,
    seed: Long,
    poisonedPlayerName: String? = null,
): GameState = GameState(
    script = script.toRecommendationScriptId(),
    players = toClocktowerPlayerStates(poisonedPlayerName),
    seed = seed,
)

private fun ClocktowerTeam.toDomainAlignment(): Alignment = when (this) {
    ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider -> Alignment.GOOD
    ClocktowerTeam.Minion, ClocktowerTeam.Demon -> Alignment.EVIL
}

private fun ClocktowerTeam.toDomainCharacterType(): CharacterType = when (this) {
    ClocktowerTeam.Townsfolk -> CharacterType.TOWNSFOLK
    ClocktowerTeam.Outsider -> CharacterType.OUTSIDER
    ClocktowerTeam.Minion -> CharacterType.MINION
    ClocktowerTeam.Demon -> CharacterType.DEMON
}
