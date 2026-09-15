package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId

/**
 * Exact epistemic semantics that a higher-level evaluator may require.
 *
 * Capabilities are intentionally finer-grained than script support so future dynamic scripts can
 * become partially supported without pretending that every advanced epistemic operation is ready.
 */
internal enum class EpistemicEvaluationCapability {
    EXACT_HISTORICAL_REPLAY,
    EXACT_HYPOTHETICAL_OBSERVATION,
}

/**
 * Capability-only result. It deliberately carries no world cardinality: unsupported semantics are
 * deferred, never represented as an empty/UNSAT world set.
 */
internal sealed interface EpistemicEvaluationAvailability {
    object Ready : EpistemicEvaluationAvailability

    data class Deferred(
        val missingCapabilities: Set<EpistemicEvaluationCapability>,
    ) : EpistemicEvaluationAvailability {
        init {
            require(missingCapabilities.isNotEmpty()) {
                "Deferred epistemic evaluation must identify at least one missing capability."
            }
        }
    }
}

/**
 * Current semantic coverage boundary for exact historical hypothetical evaluation.
 *
 * Trouble Brewing is the only script whose current exact historical replay and hypothetical
 * observation semantics are both established. Other scripts must defer until their role/script
 * semantics are explicitly added; unknown semantics must never silently fall through to Trouble
 * Brewing reasoning.
 */
internal object EpistemicEvaluationCapabilityBoundary {
    val HISTORICAL_HYPOTHETICAL_REQUIREMENTS: Set<EpistemicEvaluationCapability> = setOf(
        EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY,
        EpistemicEvaluationCapability.EXACT_HYPOTHETICAL_OBSERVATION,
    )

    private val troubleBrewing = ScriptId("trouble_brewing")
    private val supportedCapabilitiesByScript: Map<ScriptId, Set<EpistemicEvaluationCapability>> = mapOf(
        troubleBrewing to HISTORICAL_HYPOTHETICAL_REQUIREMENTS,
    )

    fun assess(
        rulesetRef: RulesetRef,
        requiredCapabilities: Set<EpistemicEvaluationCapability>,
    ): EpistemicEvaluationAvailability {
        require(requiredCapabilities.isNotEmpty()) {
            "Epistemic capability assessment requires at least one requested capability."
        }
        val supported = supportedCapabilitiesByScript[rulesetRef.scriptId].orEmpty()
        val missing = requiredCapabilities - supported
        return if (missing.isEmpty()) {
            EpistemicEvaluationAvailability.Ready
        } else {
            EpistemicEvaluationAvailability.Deferred(missing)
        }
    }
}
