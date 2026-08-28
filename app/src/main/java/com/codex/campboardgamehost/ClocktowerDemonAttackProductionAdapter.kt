package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackContext
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackSemantics
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.NightDawnDeathResolutionInput
import com.codex.campboardgamehost.clocktower.session.NightDawnResolutionPlanner

/**
 * Production adapter from current PlayerCard state into the canonical Trouble Brewing Demon attack
 * semantics. This adapter resolves only the direct Imp attack outcome; Mayor redirect and Demon
 * succession remain separate rule-owned choice boundaries.
 */
internal fun resolveTroubleBrewingDemonNightAttackOutcome(
    cards: List<PlayerCard>,
    targetName: String?,
    poisonedPlayerName: String?,
    monkProtectedTargetName: String?,
): DemonNightAttackOutcome {
    val attacker = resolveCurrentDemonCard(cards)
        ?.takeIf { card -> card.clocktowerRole?.enName == "Imp" }
        ?: return DemonNightAttackOutcome.NO_DEATH
    val target = targetName
        ?.let { name -> cards.firstOrNull { card -> card.name == name } }
        ?: return DemonNightAttackOutcome.NO_DEATH

    val functioningMonkProtectsTarget =
        monkProtectedTargetName == target.name &&
            cards.any { card ->
                card.eliminatedRound == null &&
                    AbilityFunctioningSemantics.functionsAs(
                        card.abilitySubject(poisonedPlayerName),
                        "Monk",
                    )
            }

    return DemonNightAttackSemantics.resolve(
        DemonNightAttackContext(
            attacker = attacker.abilitySubject(poisonedPlayerName),
            target = target.abilitySubject(poisonedPlayerName),
            targetIsAttacker = target.name == attacker.name,
            targetProtectedByFunctioningMonk = functioningMonkProtectsTarget,
        ),
    )
}

internal data class TroubleBrewingDawnDeathFacts(
    val attackOutcome: DemonNightAttackOutcome,
    val originalDeathSeat: Int?,
    val mayorSeat: Int?,
    val demonSafeSeats: Set<Int>,
)

/**
 * Canonical direct-attack facts consumed by Dawn death planning. All Demon-safety seats are derived
 * through the same production adapter instead of reimplementing Soldier/Monk/poison checks.
 */
internal fun resolveTroubleBrewingDawnDeathFacts(
    cards: List<PlayerCard>,
    targetName: String?,
    poisonedPlayerName: String?,
    monkProtectedTargetName: String?,
): TroubleBrewingDawnDeathFacts {
    val attackOutcome = resolveTroubleBrewingDemonNightAttackOutcome(
        cards = cards,
        targetName = targetName,
        poisonedPlayerName = poisonedPlayerName,
        monkProtectedTargetName = monkProtectedTargetName,
    )
    val originalDeathSeat = targetName
        ?.let { name -> cards.indexOfFirst { card -> card.name == name } }
        ?.takeIf { index -> index >= 0 }
        ?.plus(1)
    val mayorSeat = originalDeathSeat.takeIf {
        attackOutcome == DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
    }
    val demonSafeSeats = cards.mapIndexedNotNull { index, card ->
        (index + 1).takeIf {
            card.eliminatedRound == null &&
                resolveTroubleBrewingDemonNightAttackOutcome(
                    cards = cards,
                    targetName = card.name,
                    poisonedPlayerName = poisonedPlayerName,
                    monkProtectedTargetName = monkProtectedTargetName,
                ) == DemonNightAttackOutcome.NO_DEATH
        }
    }.toSet()

    return TroubleBrewingDawnDeathFacts(
        attackOutcome = attackOutcome,
        originalDeathSeat = originalDeathSeat,
        mayorSeat = mayorSeat,
        demonSafeSeats = demonSafeSeats,
    )
}

internal data class TroubleBrewingDawnDeathResolution(
    val facts: TroubleBrewingDawnDeathFacts,
    val resolvedDeathSeat: Int?,
    val resolvedDeathName: String?,
) {
    val mayorRedirectEligible: Boolean
        get() = facts.mayorSeat != null
}

/**
 * Checkpoint-backed production composition used by non-durable consumers such as Host. Confirmed
 * unfinished-night facts remain the input authority; the existing Dawn planner remains the death
 * and Mayor-redirect authority.
 */
internal fun resolveTroubleBrewingDawnDeathResolution(
    cards: List<PlayerCard>,
    script: ClocktowerScript,
    gameSeed: Long,
    checkpoint: ClocktowerNightCheckpoint,
): TroubleBrewingDawnDeathResolution {
    val facts = resolveTroubleBrewingDawnDeathFacts(
        cards = cards,
        targetName = checkpoint.confirmedAttackTarget,
        poisonedPlayerName = checkpoint.confirmedPoisonTarget,
        monkProtectedTargetName = checkpoint.confirmedMonkTarget,
    )
    val baseGameState = cards.toClocktowerGameState(
        script,
        gameSeed,
        poisonedPlayerName = checkpoint.confirmedPoisonTarget,
    )
    val effectiveNightState = ClocktowerEffectiveNightState(
        effectiveAliveSeats = cards.mapIndexedNotNull { index, card ->
            (index + 1).takeIf { card.eliminatedRound == null }
        }.toSet(),
        effectiveRoleIdsBySeat = cards.mapIndexedNotNull { index, card ->
            card.clocktowerRole?.let { role -> index + 1 to RoleId(role.enName) }
        }.toMap(),
    )
    val demonRoleIds = cards.mapNotNull { card ->
        card.clocktowerRole
            ?.takeIf { card.clocktowerTeam == ClocktowerTeam.Demon }
            ?.let { role -> RoleId(role.enName) }
    }.toSet()
    val deathTransition = NightDawnResolutionPlanner.planValidatedNightDeath(
        baseGameState = baseGameState,
        checkpoint = checkpoint,
        input = NightDawnDeathResolutionInput(
            originalDeathSeat = facts.originalDeathSeat,
            mayorSeat = facts.mayorSeat,
            mayorRedirectMayApply = facts.mayorSeat != null,
            attackOutcome = facts.attackOutcome,
            demonSafeSeats = facts.demonSafeSeats,
            effectiveNightState = effectiveNightState,
            demonRoleIds = demonRoleIds,
        ),
    )
    val resolvedDeathSeat = deathTransition.dawnCommitIntent?.death?.targetSeat
    val resolvedDeathName = resolvedDeathSeat
        ?.let { targetSeat -> cards.getOrNull(targetSeat - 1)?.name }

    return TroubleBrewingDawnDeathResolution(
        facts = facts,
        resolvedDeathSeat = resolvedDeathSeat,
        resolvedDeathName = resolvedDeathName,
    )
}
