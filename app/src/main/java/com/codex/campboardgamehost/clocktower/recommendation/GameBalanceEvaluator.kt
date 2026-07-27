package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PublicBalanceHint
import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import kotlin.math.roundToInt

/**
 * Estimates which alignment is ahead from the private Grimoire state.
 * Positive values mean evil is ahead; negative values mean good is ahead.
 */
internal object GameBalanceEvaluator {
    data class Assessment(
        val evilAdvantage: Int,
        val hint: PublicBalanceHint,
    )

    fun evaluate(
        game: GameState,
        round: Int,
        spentAbilitySeats: Set<Int> = emptySet(),
        informationPressureBySeat: Map<Int, Int> = emptyMap(),
    ): Assessment {
        val good = game.players.filter { it.actualAlignment == Alignment.GOOD }
        val evil = game.players.filter { it.actualAlignment == Alignment.EVIL }
        if (good.isEmpty() || evil.isEmpty()) return Assessment(0, PublicBalanceHint.UNKNOWN)

        val goodLossRate = good.count { !it.alive }.toDouble() / good.size
        val evilLossRate = evil.count { !it.alive }.toDouble() / evil.size
        var score = ((goodLossRate - evilLossRate) * 70.0).roundToInt()

        val alive = game.players.count { it.alive }
        val aliveEvil = evil.count { it.alive }
        if (alive <= 3 && aliveEvil > 0) score += 12
        else if (alive <= 5 && aliveEvil > 0) score += 5

        val spentGood = spentAbilitySeats.count { game.playerAt(it)?.actualAlignment == Alignment.GOOD }
        val spentEvil = spentAbilitySeats.count { game.playerAt(it)?.actualAlignment == Alignment.EVIL }
        score += (spentGood - spentEvil) * 3

        val goodInformationPressure = informationPressureBySeat
            .filterKeys { game.playerAt(it)?.actualAlignment == Alignment.GOOD }
            .values
            .sum()
        score += (goodInformationPressure / 4).coerceAtMost(12)

        if (round >= 4 && score != 0) score += if (score > 0) 3 else -3
        score = score.coerceIn(-100, 100)
        val hint = when {
            score >= 15 -> PublicBalanceHint.EVIL_AHEAD
            score <= -15 -> PublicBalanceHint.GOOD_AHEAD
            else -> PublicBalanceHint.BALANCED
        }
        return Assessment(score, hint)
    }

    /**
     * Information shown to good players is made clearer when evil is ahead and
     * more deceptive when good is ahead. The configured style remains the base.
     */
    fun adjustInformationStyle(
        configured: RecommendationStyle,
        evilAdvantage: Int,
    ): RecommendationStyle = when {
        evilAdvantage >= 55 -> RecommendationStyle.GENTLE
        evilAdvantage >= 20 -> when (configured) {
            RecommendationStyle.AGGRESSIVE -> RecommendationStyle.BALANCED
            RecommendationStyle.BALANCED -> RecommendationStyle.GENTLE
            RecommendationStyle.GENTLE -> RecommendationStyle.GENTLE
        }
        evilAdvantage <= -55 -> RecommendationStyle.AGGRESSIVE
        evilAdvantage <= -20 -> when (configured) {
            RecommendationStyle.GENTLE -> RecommendationStyle.BALANCED
            RecommendationStyle.BALANCED -> RecommendationStyle.AGGRESSIVE
            RecommendationStyle.AGGRESSIVE -> RecommendationStyle.AGGRESSIVE
        }
        else -> configured
    }
}
