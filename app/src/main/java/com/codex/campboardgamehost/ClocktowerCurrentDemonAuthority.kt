package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.CurrentDemonAuthority

internal data class CurrentDemonHostContext(
    val actor: PlayerCard,
    val isPoisoned: Boolean,
)

internal fun resolveCurrentDemonCard(cards: List<PlayerCard>): PlayerCard? =
    CurrentDemonAuthority.resolveLive(
        candidates = cards,
        isAlive = { card -> card.eliminatedRound == null },
        isDemon = { card -> card.clocktowerTeam == ClocktowerTeam.Demon },
    )

internal fun resolveCurrentDemonHostContext(
    cards: List<PlayerCard>,
    poisonedPlayerName: String?,
): CurrentDemonHostContext? = resolveCurrentDemonCard(cards)?.let { actor ->
    CurrentDemonHostContext(
        actor = actor,
        isPoisoned = actor.name == poisonedPlayerName,
    )
}

internal fun resolveNightReconstructionDemonRoleId(
    cards: List<PlayerCard>,
    currentDemonHostContext: CurrentDemonHostContext?,
    confirmedDemonAttackerName: String?,
): RoleId? = currentDemonHostContext
    ?.actor
    ?.clocktowerRole
    ?.enName
    ?.let(::RoleId)
