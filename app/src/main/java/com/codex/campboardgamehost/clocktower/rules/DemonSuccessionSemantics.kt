package com.codex.campboardgamehost.clocktower.rules

internal data class DemonSuccessionContext(
    val demonActuallyDied: Boolean,
    val demonDeathWasImpSelfKill: Boolean,
    val aliveCountBeforeDemonDeath: Int,
    val functioningScarletWomanSeat: Int?,
    val livingMinionSeats: Set<Int>,
)

internal sealed interface DemonSuccessionResolution {
    data object None : DemonSuccessionResolution
    data class Choice(val targetSeats: Set<Int>) : DemonSuccessionResolution
    data class Forced(val targetSeat: Int) : DemonSuccessionResolution
}

internal object DemonSuccessionSemantics {
    fun resolve(context: DemonSuccessionContext): DemonSuccessionResolution {
        if (!context.demonActuallyDied) return DemonSuccessionResolution.None
        if (context.aliveCountBeforeDemonDeath >= 5 && context.functioningScarletWomanSeat != null) {
            return DemonSuccessionResolution.Forced(context.functioningScarletWomanSeat)
        }
        if (!context.demonDeathWasImpSelfKill || context.livingMinionSeats.isEmpty()) {
            return DemonSuccessionResolution.None
        }
        return DemonSuccessionResolution.Choice(context.livingMinionSeats)
    }
}
