package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

/**
 * Stable identity for one policy evaluation of one canonical decision revision.
 *
 * Evidence checkpoint is intentionally not part of this key. If the semantics behind one policy
 * version change, that must become a new policy version rather than silently creating two traces
 * with the same policy identity and different evidence provenance.
 */
internal data class DecisionTraceKey(
    val gameId: String,
    val decisionId: String,
    val lifecycleStage: SdeDecisionLifecycleStage,
    val sourceRevision: InformationDecisionRevision,
    val policyVersion: PolicyVersion,
) {
    init {
        require(gameId.isNotBlank()) { "DecisionTrace key game ID cannot be blank." }
        require(decisionId.isNotBlank()) { "DecisionTrace key decision ID cannot be blank." }
    }
}

internal val DecisionTrace.archiveKey: DecisionTraceKey
    get() {
        val prefix = historyPrefixRef as? SdeHistoricalPrefixRef.Global
            ?: throw IllegalArgumentException(
                "Only traces bound to a canonical global history prefix can enter the replay archive.",
            )
        return DecisionTraceKey(
            gameId = prefix.gameId,
            decisionId = decisionId,
            lifecycleStage = lifecycleStage,
            sourceRevision = sourceRevision,
            policyVersion = policySnapshot.policyVersion,
        )
    }

/**
 * Immutable owner of replayable DecisionTrace records.
 *
 * The archive stores diagnostic evaluations only. It does not own or reconstruct canonical game
 * facts; every admitted trace must already reference a Global ActionFactTimeline /
 * EpistemicObservationLog prefix.
 */
internal class DecisionTraceArchive(
    traces: List<DecisionTrace> = emptyList(),
) {
    val traces: List<DecisionTrace> = traces.toList()

    init {
        val keys = this.traces.map { it.archiveKey }
        require(keys.distinct().size == keys.size) {
            "DecisionTrace archive keys must be unique."
        }
    }

    fun append(trace: DecisionTrace): DecisionTraceArchive {
        val key = trace.archiveKey
        val existing = traces.firstOrNull { it.archiveKey == key }
        if (existing == null) {
            return DecisionTraceArchive(traces + trace)
        }
        require(existing == trace) {
            "DecisionTrace archive key already exists with different trace content."
        }
        return this
    }

    fun find(key: DecisionTraceKey): DecisionTrace? =
        traces.firstOrNull { it.archiveKey == key }

    override fun equals(other: Any?): Boolean =
        other is DecisionTraceArchive && traces == other.traces

    override fun hashCode(): Int = traces.hashCode()

    override fun toString(): String = "DecisionTraceArchive(traces=$traces)"
}
