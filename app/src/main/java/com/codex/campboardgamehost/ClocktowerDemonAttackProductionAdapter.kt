package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.rules.AbilityFunctioningSemantics
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackContext
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackSemantics

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
    val attacker = cards.firstOrNull { card ->
        card.eliminatedRound == null &&
            card.clocktowerTeam == ClocktowerTeam.Demon &&
            card.clocktowerRole?.enName == "Imp"
    } ?: return DemonNightAttackOutcome.NO_DEATH
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
