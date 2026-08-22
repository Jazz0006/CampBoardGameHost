package com.codex.campboardgamehost.clocktower.recommendation.dynamic

/**
 * Owns the reliability-family boundary for information shown by a malfunctioning ability.
 *
 * Rules legality decides whether truthful / false candidates exist. Recommendation style,
 * global game balance, history pressure, and candidate misinformation pressure may rank
 * candidates inside a legal family, but they must not decide whether an impaired ability
 * is truthful versus false.
 */
internal object ImpairedInformationPolicy {
    private const val IMPAIRED_FALSE_FAMILY_MASS_FIXED_POINT = 970_000L

    /**
     * A small truthful allowance keeps impairment from becoming mechanically solvable.
     * If no legal false candidate exists, the selector falls back to the truthful family.
     */
    fun falseFamilyMassFixedPoint(reliability: InformationReliability): Long = when (reliability) {
        InformationReliability.RELIABLE -> 0L
        InformationReliability.DRUNK,
        InformationReliability.POISONED,
        -> IMPAIRED_FALSE_FAMILY_MASS_FIXED_POINT
    }
}
