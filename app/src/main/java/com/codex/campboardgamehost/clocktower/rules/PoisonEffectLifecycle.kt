package com.codex.campboardgamehost.clocktower.rules

internal object PoisonEffectLifecycle {
    fun effectiveTarget(
        confirmedTarget: String?,
        sourceActionResolved: Boolean,
        sourceAbilityFunctioning: Boolean,
    ): String? = confirmedTarget.takeIf { sourceActionResolved && sourceAbilityFunctioning }

    fun afterNight(target: String?, poisonerAlive: Boolean): String? = target.takeIf { poisonerAlive }

    fun atStartOfNextNight(): String? = null
}
