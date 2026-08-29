package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.CurrentDemonAuthority
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.resolveTroubleBrewingImpSelfKillSuccession

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
): RoleId? {
    val liveDemonCards = cards.filter { card ->
        card.eliminatedRound == null && card.clocktowerTeam == ClocktowerTeam.Demon
    }
    val authorityCard = when (liveDemonCards.size) {
        0 -> confirmedDemonAttackerName
            ?.let { attackerName -> cards.singleOrNull { it.name == attackerName } }
            ?.takeIf { attacker ->
                attacker.eliminatedRound != null &&
                    attacker.clocktowerTeam == ClocktowerTeam.Demon
            }
        1 -> currentDemonHostContext?.actor
        else -> null
    }
    return authorityCard
        ?.clocktowerRole
        ?.enName
        ?.let(::RoleId)
}

internal fun resolveNightDemonSuccessionForHost(
    baseGameState: GameState,
    checkpoint: ClocktowerNightCheckpoint,
    currentDemonHostContext: CurrentDemonHostContext?,
    demonRoleId: RoleId?,
): DemonSuccessionResolution {
    demonRoleId ?: return DemonSuccessionResolution.None
    val liveDemonCount = baseGameState.players.count { player ->
        player.alive && player.actualRole == demonRoleId
    }
    if (liveDemonCount > 1 || (liveDemonCount == 1 && currentDemonHostContext == null)) {
        return DemonSuccessionResolution.None
    }
    return resolveTroubleBrewingImpSelfKillSuccession(
        baseGameState = baseGameState,
        checkpoint = checkpoint,
        demonRoleId = demonRoleId,
    )
}
