package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSnapshot

/**
 * Disposable identity/freshness reference for an SDE decision that has not been committed.
 *
 * This is deliberately not a lifecycle store. Persistent setup commitments and committed facts
 * remain session-owned. A planned reference carries only stable decision/candidate identity, the
 * existing source revision authority and, when the caller has a validated candidate-space snapshot,
 * that snapshot's semantic identity. It never owns GameState, legal candidate pools, semantic
 * history, or a revision counter.
 */
internal data class PlannedDecisionRef(
    val decisionId: String,
    val candidateId: String,
    val sourceRevision: InformationDecisionRevision,
    val sourceSemanticIdentity: String? = null,
) {
    init {
        require(decisionId.isNotBlank()) { "Planned decision ID cannot be blank." }
        require(candidateId.isNotBlank()) { "Planned candidate ID cannot be blank." }
        require(sourceSemanticIdentity == null || sourceSemanticIdentity.isNotBlank()) {
            "Planned decision semantic identity cannot be blank when present."
        }
    }

    /** Revision-only freshness for decision families without a candidate-space snapshot contract. */
    fun isCurrentFor(currentRevision: InformationDecisionRevision): Boolean =
        sourceRevision == currentRevision

    /**
     * Snapshot-bound freshness for structured information decisions.
     *
     * A revision-only planned reference intentionally ignores an unrelated information snapshot;
     * a snapshot-bound reference requires both the existing revision authority and semantic identity
     * to match. Candidate lists themselves are never copied into this planned reference.
     */
    fun isCurrentFor(currentSnapshot: InformationDecisionSnapshot): Boolean =
        isCurrentFor(currentSnapshot.revision) &&
            (sourceSemanticIdentity == null || sourceSemanticIdentity == currentSnapshot.semanticIdentity)

    companion object {
        fun fromInformationSnapshot(
            decisionId: String,
            candidateId: String,
            snapshot: InformationDecisionSnapshot,
        ): PlannedDecisionRef {
            require(candidateId in snapshot.legalCandidateIds) {
                "Planned candidate '$candidateId' does not belong to the source information snapshot."
            }
            return PlannedDecisionRef(
                decisionId = decisionId,
                candidateId = candidateId,
                sourceRevision = snapshot.revision,
                sourceSemanticIdentity = snapshot.semanticIdentity,
            )
        }
    }
}
