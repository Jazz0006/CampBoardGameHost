package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.CurrentDemonAuthority
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackContext
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackSemantics
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionContext
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionSemantics

internal data class TroubleBrewingImpSelfKillResolution(
    val attackOutcome: DemonNightAttackOutcome,
    val successionResolution: DemonSuccessionResolution,
) {
    val demonActuallyDied: Boolean
        get() = attackOutcome == DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED
}

/**
 * Trouble Brewing production composition for the Imp self-kill succession boundary.
 *
 * This adapter owns no succession rule of its own. It derives current confirmed attack facts from
 * the unfinished-night checkpoint, delegates attack precedence to [DemonNightAttackSemantics], and
 * then delegates successor legality to [DemonSuccessionSemantics]. Generic non-self Demon death
 * succession remains outside this SNE-7.9D slice.
 */
internal fun resolveTroubleBrewingImpSelfKill(
    baseGameState: GameState,
    checkpoint: ClocktowerNightCheckpoint,
    demonRoleId: RoleId,
): TroubleBrewingImpSelfKillResolution {
    val target = checkpoint.confirmedAttackTarget
        ?.let { targetName -> baseGameState.players.singleOrNull { it.name == targetName } }
        ?: return TroubleBrewingImpSelfKillResolution(
            attackOutcome = DemonNightAttackOutcome.NO_DEATH,
            successionResolution = DemonSuccessionResolution.None,
        )
    val currentLiveAttacker = CurrentDemonAuthority.resolveLive(
        candidates = baseGameState.players,
        isAlive = PlayerState::alive,
        isDemon = { player -> player.actualRole == demonRoleId },
    )
    val attacker = currentLiveAttacker
        ?: target.takeIf { player ->
            // Same-night reconstruction can observe the confirmed self-kill after the attacking
            // Imp has already been materialized dead, before a live successor exists.
            !player.alive && player.actualRole == demonRoleId
        }
        ?: return TroubleBrewingImpSelfKillResolution(
            attackOutcome = DemonNightAttackOutcome.NO_DEATH,
            successionResolution = DemonSuccessionResolution.None,
        )
    if (attacker.seat != target.seat) {
        return TroubleBrewingImpSelfKillResolution(
            attackOutcome = DemonNightAttackOutcome.NO_DEATH,
            successionResolution = DemonSuccessionResolution.None,
        )
    }

    // Reconstruction may receive a base snapshot in which the attacking Imp is already publicly
    // dead. Succession legality is evaluated at the instant before that confirmed self-kill.
    val preSelfKillAttacker = attacker.toAbilitySubject(checkpoint).copy(isAlive = true)
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
            attacker = preSelfKillAttacker,
            target = preSelfKillAttacker,
            targetIsAttacker = true,
            targetProtectedByFunctioningMonk = targetProtectedByFunctioningMonk,
        ),
    )
    if (attackOutcome != DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED) {
        return TroubleBrewingImpSelfKillResolution(
            attackOutcome = attackOutcome,
            successionResolution = DemonSuccessionResolution.None,
        )
    }

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
    val aliveCountBeforeDemonDeath =
        baseGameState.players.count(PlayerState::alive) + if (attacker.alive) 0 else 1
    val successionResolution = DemonSuccessionSemantics.resolve(
        DemonSuccessionContext(
            demonActuallyDied = true,
            demonDeathWasImpSelfKill = true,
            aliveCountBeforeDemonDeath = aliveCountBeforeDemonDeath,
            functioningScarletWomanSeat = functioningScarletWomanSeat,
            livingMinionSeats = livingMinionSeats,
        ),
    )

    return TroubleBrewingImpSelfKillResolution(
        attackOutcome = attackOutcome,
        successionResolution = successionResolution,
    )
}

internal fun resolveTroubleBrewingImpSelfKillSuccession(
    baseGameState: GameState,
    checkpoint: ClocktowerNightCheckpoint,
    demonRoleId: RoleId,
): DemonSuccessionResolution = resolveTroubleBrewingImpSelfKill(
    baseGameState = baseGameState,
    checkpoint = checkpoint,
    demonRoleId = demonRoleId,
).successionResolution

private fun PlayerState.toAbilitySubject(
    checkpoint: ClocktowerNightCheckpoint,
): AbilitySubject = AbilitySubject(
    actualRole = actualRole.value,
    shownRole = shownRole?.value,
    isPoisoned = poisoned || checkpoint.confirmedPoisonTarget == name,
    isAlive = alive,
)
