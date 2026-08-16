package com.codex.campboardgamehost.clocktower.epistemic

/**
 * Explicit A4 rollout and degradation policy.
 *
 * It does not change exact world-set semantics: a slow operation still completes exactly. The
 * caller may only use [recommendation] to reduce optional presentation/search work or choose a
 * more supervised storyteller mode. ZDD is disabled until the device gate is recorded as passed.
 */
data class A4WorldEngineRuntimePolicy(
    val rollout: A4WorldEngineRollout = A4WorldEngineRollout.ENUMERATED_ONLY,
    val targetOperationMillis: Long = 15,
    val provisionalMaximumOperationMillis: Long = 50,
) {
    init {
        require(targetOperationMillis > 0)
        require(provisionalMaximumOperationMillis >= targetOperationMillis)
    }

    fun preferredRepresentation(): A4WorldSetRepresentation = when (rollout) {
        A4WorldEngineRollout.ENUMERATED_ONLY,
        A4WorldEngineRollout.ZDD_SHADOW -> A4WorldSetRepresentation.ENUMERATED
        A4WorldEngineRollout.ZDD_DEVICE_VALIDATED -> A4WorldSetRepresentation.ZDD
    }

    fun shadowsZdd(): Boolean = rollout == A4WorldEngineRollout.ZDD_SHADOW

    /**
     * A recommendation for optional work after the exact operation has completed. This must never
     * turn SAT into UNSAT, truncate the world set, or change the selected registration facts.
     */
    fun recommendation(elapsedMillis: Long, repeatedOverBudget: Boolean = false): A4DegradationRecommendation = when {
        elapsedMillis <= targetOperationMillis -> A4DegradationRecommendation.NONE
        elapsedMillis <= provisionalMaximumOperationMillis -> A4DegradationRecommendation.REDUCE_EXPLANATION_DETAIL
        repeatedOverBudget -> A4DegradationRecommendation.MANUAL_ONLY
        else -> A4DegradationRecommendation.ASSISTED
    }
}

enum class A4WorldEngineRollout {
    /** Current safe default: the transparent A3 materialized baseline is the runtime engine. */
    ENUMERATED_ONLY,

    /** Measure ZDD alongside the baseline; its result must not drive UI or storyteller decisions. */
    ZDD_SHADOW,

    /** May select ZDD after correctness and POCO X5/X8 device gates are recorded as passed. */
    ZDD_DEVICE_VALIDATED,
}

enum class A4WorldSetRepresentation { ENUMERATED, ZDD }

enum class A4DegradationRecommendation {
    NONE,
    REDUCE_EXPLANATION_DETAIL,
    ASSISTED,
    MANUAL_ONLY,
}

/** One replayable record of an exact world-set operation, suitable for the A4 device gate. */
data class A4WorldEngineTelemetry(
    val operation: A4WorldEngineOperation,
    val representation: A4WorldSetRepresentation,
    val filterStrategy: ZddFilterStrategy? = null,
    val elapsedMillis: Long,
    val cardinality: WorldCardinality,
    val recommendation: A4DegradationRecommendation,
)

enum class A4WorldEngineOperation { BUILD, REQUIRE, EXCLUDE, QUERY }

/** Small in-process sink; the future game-snapshot integration owns durable analytics/export. */
class A4WorldEngineTelemetryRecorder {
    private val records = mutableListOf<A4WorldEngineTelemetry>()

    @Synchronized fun record(value: A4WorldEngineTelemetry) {
        records += value
    }

    @Synchronized fun snapshot(): List<A4WorldEngineTelemetry> = records.toList()
}
