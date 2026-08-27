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
