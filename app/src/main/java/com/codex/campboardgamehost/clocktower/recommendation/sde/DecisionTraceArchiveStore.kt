package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision

/**
 * Durable adapter for the immutable DecisionTrace archive.
 *
 * The store owns no mutable game/history state. Every operation reconstructs one complete immutable
 * archive from raw persistence, applies the archive contract, and writes a complete replacement only
 * when a new trace was admitted or one existing Pending trace was authoritatively finalized.
 */
internal class DecisionTraceArchiveStore(
    private val readRaw: () -> String?,
    private val writeRaw: (String) -> Boolean,
) {
    fun load(): DecisionTraceArchive {
        val raw = readRaw()
        if (raw.isNullOrBlank()) return DecisionTraceArchive()
        return DecisionTraceArchiveJsonCodec.decode(raw)
    }

    fun append(trace: DecisionTrace): Boolean {
        val current = load()
        val updated = current.append(trace)
        if (updated === current) return true
        return writeRaw(DecisionTraceArchiveJsonCodec.encode(updated))
    }

    /**
     * Finalizes one persisted pending trace only from evidence that canonical observation commit has
     * already succeeded. This method neither confirms the choice nor mutates canonical history.
     */
    fun correlateCommittedChoice(
        key: DecisionTraceKey,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
        overrideReason: DecisionTraceOverrideReason? = null,
    ): Boolean {
        val current = load()
        val existing = requireNotNull(current.find(key)) {
            "DecisionTrace correlation requires an existing archive entry."
        }
        val pending = when (existing.actualChoice) {
            DecisionTraceActualChoice.Pending -> existing
            is DecisionTraceActualChoice.Committed ->
                existing.copy(actualChoice = DecisionTraceActualChoice.Pending)
        }
        val finalized = DecisionTraceAuthoritativeChoiceCorrelator.finalize(
            trace = pending,
            confirmed = confirmed,
            committedObservation = committedObservation,
            postCommitSession = postCommitSession,
            overrideReason = overrideReason,
        )
        val updated = current.finalizeActualChoice(finalized)
        if (updated === current) return true
        return writeRaw(DecisionTraceArchiveJsonCodec.encode(updated))
    }

    /**
     * Correlates the only pending diagnostic trace for this canonical decision, when one exists.
     * Absence is expected when runtime shadow evaluation was ineligible, stale, cancelled or failed.
     */
    fun correlateCommittedChoiceIfPresent(
        policyVersion: PolicyVersion,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
        overrideReason: DecisionTraceOverrideReason? = null,
    ): Boolean {
        val matches = load().traces.filter { trace ->
            trace.decisionId == confirmed.contextSnapshot.semanticIdentity &&
                trace.sourceRevision == confirmed.contextSnapshot.revision &&
                trace.policySnapshot.policyVersion == policyVersion &&
                trace.actualChoice is DecisionTraceActualChoice.Pending
        }
        if (matches.isEmpty()) return true
        require(matches.size == 1) {
            "DecisionTrace correlation requires exactly one matching pending diagnostic trace."
        }
        return correlateCommittedChoice(
            key = matches.single().archiveKey,
            confirmed = confirmed,
            committedObservation = committedObservation,
            postCommitSession = postCommitSession,
            overrideReason = overrideReason,
        )
    }
}
