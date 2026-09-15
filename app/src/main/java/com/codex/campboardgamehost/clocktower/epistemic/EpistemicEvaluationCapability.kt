package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerScriptSource
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
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
 * Only the validated built-in official Trouble Brewing ruleset is currently backed by both the
 * exact historical replay and hypothetical-observation semantics used below this boundary.
 * Checking the validated ruleset (rather than only a ScriptId) is deliberate: imported/homebrew
 * content must not inherit exact support merely by reusing `trouble_brewing` as an identifier.
 */
internal object EpistemicEvaluationCapabilityBoundary {
    val HISTORICAL_HYPOTHETICAL_REQUIREMENTS: Set<EpistemicEvaluationCapability> = setOf(
        EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY,
        EpistemicEvaluationCapability.EXACT_HYPOTHETICAL_OBSERVATION,
    )

    private val troubleBrewing = ScriptId("trouble_brewing")

    fun assess(
        validatedRuleset: ValidatedClocktowerRuleset,
        requiredCapabilities: Set<EpistemicEvaluationCapability>,
    ): EpistemicEvaluationAvailability {
        require(requiredCapabilities.isNotEmpty()) {
            "Epistemic capability assessment requires at least one requested capability."
        }
        val supported = if (
            validatedRuleset.script.id == troubleBrewing &&
            validatedRuleset.script.source == ClocktowerScriptSource.BUILTIN_OFFICIAL
        ) {
            HISTORICAL_HYPOTHETICAL_REQUIREMENTS
        } else {
            emptySet()
        }
        val missing = requiredCapabilities - supported
        return if (missing.isEmpty()) {
            EpistemicEvaluationAvailability.Ready
        } else {
            EpistemicEvaluationAvailability.Deferred(missing)
        }
    }
}
