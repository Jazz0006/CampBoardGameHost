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
    val authorityCard = currentDemonHostContext?.actor
        ?: confirmedDemonAttackerName
            ?.let { attackerName -> cards.singleOrNull { it.name == attackerName } }
            ?.takeIf { attacker ->
                attacker.eliminatedRound != null &&
                    attacker.clocktowerTeam == ClocktowerTeam.Demon
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
    if (currentDemonHostContext == null || demonRoleId == null) {
        return DemonSuccessionResolution.None
    }
    return resolveTroubleBrewingImpSelfKillSuccession(
        baseGameState = baseGameState,
        checkpoint = checkpoint,
        demonRoleId = demonRoleId,
    )
}
