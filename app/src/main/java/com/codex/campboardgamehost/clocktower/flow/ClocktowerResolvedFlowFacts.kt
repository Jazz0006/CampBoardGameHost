package com.codex.campboardgamehost.clocktower.flow

/**
 * Rules-layer outcomes that are already resolved before flow projection.
 *
 * These facts deliberately do not encode how an execution, death, protection, poison state, or
 * succession decision was derived. The shadow flow layer only consumes the result so it does not
 * become a second rules engine.
 */
internal enum class ClocktowerResolvedFlowFact {
    EXECUTION_OCCURRED_TODAY,
    RAVENKEEPER_DIED_AT_NIGHT,
    MAYOR_REDIRECT_ELIGIBLE,
    DEMON_SUCCESSION_REQUIRED,
}

internal data class ClocktowerResolvedFlowFacts(
    val facts: Set<ClocktowerResolvedFlowFact> = emptySet(),
) {
    operator fun contains(fact: ClocktowerResolvedFlowFact): Boolean = fact in facts

    companion object {
        val EMPTY = ClocktowerResolvedFlowFacts()
    }
}
