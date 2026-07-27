package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle
import kotlin.math.abs

internal enum class InformationReliability {
    RELIABLE,
    DRUNK,
    POISONED,
}

internal object AutomaticInformationPolicy {
    fun misinformationProbability(
        reliability: InformationReliability,
        style: RecommendationStyle,
        evilAdvantage: Int,
        recentMisinformationStreak: Int = 0,
        minimumMisinformationPressure: Int = 0,
    ): Double {
        if (reliability == InformationReliability.RELIABLE) return 0.0
        val base = when (reliability) {
            InformationReliability.DRUNK -> when (style) {
                RecommendationStyle.GENTLE -> 0.55
                RecommendationStyle.BALANCED -> 0.65
                RecommendationStyle.AGGRESSIVE -> 0.75
            }
            InformationReliability.POISONED -> when (style) {
                RecommendationStyle.GENTLE -> 0.70
                RecommendationStyle.BALANCED -> 0.82
                RecommendationStyle.AGGRESSIVE -> 0.92
            }
            InformationReliability.RELIABLE -> 0.0
        }
        val balanceAdjustment = (-evilAdvantage * 0.0015).coerceIn(-0.15, 0.10)
        val streakAdjustment = if (recentMisinformationStreak >= 2) {
            -((recentMisinformationStreak - 1).coerceAtMost(2) * 0.05)
        } else {
            0.0
        }
        val impactAdjustment = -((minimumMisinformationPressure - 3).coerceAtLeast(0) * 0.025)
        val range = when (reliability) {
            InformationReliability.DRUNK -> 0.52..0.85
            InformationReliability.POISONED -> 0.60..0.95
            InformationReliability.RELIABLE -> 0.0..0.0
        }
        return (base + balanceAdjustment + streakAdjustment + impactAdjustment)
            .coerceIn(range.start, range.endInclusive)
    }

    fun <T> select(
        options: List<T>,
        reliability: InformationReliability,
        style: RecommendationStyle,
        evilAdvantage: Int,
        stableKey: String,
        recentMisinformationStreak: Int,
        isTruthful: (T) -> Boolean,
        misinformationPressure: (T) -> Int,
        styleOf: (T) -> RecommendationStyle,
    ): T? {
        if (options.isEmpty()) return null
        val truthful = options.filter(isTruthful)
        val misleading = options.filterNot(isTruthful)
        if (reliability == InformationReliability.RELIABLE || misleading.isEmpty()) {
            return chooseWithin(truthful.ifEmpty { options }, style, stableKey, misinformationPressure, styleOf)
        }
        if (truthful.isEmpty()) {
            return chooseWithin(misleading, style, stableKey, misinformationPressure, styleOf)
        }
        val probability = misinformationProbability(
            reliability = reliability,
            style = style,
            evilAdvantage = evilAdvantage,
            recentMisinformationStreak = recentMisinformationStreak,
            minimumMisinformationPressure = misleading.minOf(misinformationPressure),
        )
        val chosenGroup = if (stableUnit("$stableKey:truth") < probability) misleading else truthful
        return chooseWithin(chosenGroup, style, "$stableKey:choice", misinformationPressure, styleOf)
    }

    private fun <T> chooseWithin(
        options: List<T>,
        style: RecommendationStyle,
        stableKey: String,
        misinformationPressure: (T) -> Int,
        styleOf: (T) -> RecommendationStyle,
    ): T? {
        if (options.isEmpty()) return null
        val preferredPressure = when (style) {
            RecommendationStyle.GENTLE -> 1
            RecommendationStyle.BALANCED -> 2
            RecommendationStyle.AGGRESSIVE -> 4
        }
        val ranked = options.sortedWith(
            compareBy<T> { abs(styleOf(it).ordinal - style.ordinal) }
                .thenBy { abs(misinformationPressure(it) - preferredPressure) }
                .thenBy { stableUnit("$stableKey:${it.hashCode()}") },
        )
        return ranked.first()
    }

    private fun stableUnit(key: String): Double {
        var hash = 0xcbf29ce484222325UL
        key.encodeToByteArray().forEach { byte ->
            hash = (hash xor byte.toUByte().toULong()) * 0x100000001b3UL
        }
        return (hash.toLong().ushr(11).toDouble() / (1L shl 53).toDouble())
    }
}
