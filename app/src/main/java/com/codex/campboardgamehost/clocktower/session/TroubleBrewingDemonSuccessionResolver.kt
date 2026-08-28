package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackContext
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackSemantics
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionContext
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionSemantics

/**
 * Trouble Brewing production composition for the Imp self-kill succession boundary.
 *
 * This adapter owns no succession rule of its own. It derives current confirmed attack facts from
 * the unfinished-night checkpoint, delegates attack precedence to [DemonNightAttackSemantics], and
 * then delegates successor legality to [DemonSuccessionSemantics]. Generic non-self Demon death
 * succession remains outside this SNE-7.9D slice.
 */
internal fun resolveTroubleBrewingImpSelfKillSuccession(
    baseGameState: GameState,
    checkpoint: ClocktowerNightCheckpoint,
    demonRoleId: RoleId,
): DemonSuccessionResolution {
    val attacker = baseGameState.players.singleOrNull { player ->
        player.alive && player.actualRole == demonRoleId
    } ?: return DemonSuccessionResolution.None
    val target = checkpoint.confirmedAttackTarget
        ?.let { targetName -> baseGameState.players.singleOrNull { it.name == targetName } }
        ?: return DemonSuccessionResolution.None

    val targetProtectedByFunctioningMonk =
        checkpoint.confirmedMonkTarget == target.name &&
            baseGameState.players.any { player ->
                AbilityFunctioningSemantics.functionsAs(
                    player.toAbilitySubject(checkpoint),
                    "Monk",
                )
            }
    val attackOutcome = DemonNightAttackSemantics.resolve(
        DemonNightAttackContext(
            attacker = attacker.toAbilitySubject(checkpoint),
            target = target.toAbilitySubject(checkpoint),
            targetIsAttacker = attacker.seat == target.seat,
            targetProtectedByFunctioningMonk = targetProtectedByFunctioningMonk,
        ),
    )
    val selfKillRequiresSuccession =
        attackOutcome == DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED
    if (!selfKillRequiresSuccession) return DemonSuccessionResolution.None

    val functioningScarletWomanSeat = baseGameState.players
        .singleOrNull { player ->
            AbilityFunctioningSemantics.functionsAs(
                player.toAbilitySubject(checkpoint),
                "Scarlet Woman",
            )
        }
        ?.seat
    val livingMinionSeats = baseGameState.players
        .filter { player -> player.alive && player.actualType == CharacterType.MINION }
        .map(PlayerState::seat)
        .toSet()

    return DemonSuccessionSemantics.resolve(
        DemonSuccessionContext(
            demonActuallyDied = true,
            demonDeathWasImpSelfKill = true,
            aliveCountBeforeDemonDeath = baseGameState.players.count(PlayerState::alive),
            functioningScarletWomanSeat = functioningScarletWomanSeat,
            livingMinionSeats = livingMinionSeats,
        ),
    )
}

private fun PlayerState.toAbilitySubject(
    checkpoint: ClocktowerNightCheckpoint,
): AbilitySubject = AbilitySubject(
    actualRole = actualRole.value,
    shownRole = shownRole?.value,
    isPoisoned = poisoned || checkpoint.confirmedPoisonTarget == name,
    isAlive = alive,
)
