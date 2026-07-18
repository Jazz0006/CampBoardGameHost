package com.codex.campboardgamehost.clocktower.rules

internal object PoisonEffectLifecycle {
    fun afterNight(target: String?, poisonerAlive: Boolean): String? = target.takeIf { poisonerAlive }

    fun atStartOfNextNight(): String? = null
}
