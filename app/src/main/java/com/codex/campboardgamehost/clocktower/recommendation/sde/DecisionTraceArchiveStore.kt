package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision

/**
 * Count-bounded retention for the local diagnostic archive.
 *
 * Pending traces are correlation material and are never evicted. Oldest committed traces are
 * retired first. A new append is rejected rather than allowing unbounded growth when every retained
 * entry is still pending.
 */
internal data class DecisionTraceArchiveRetentionPolicy(
    val maxTraceCount: Int = DEFAULT_MAX_TRACE_COUNT,
) {
    init {
        require(maxTraceCount > 0) { "DecisionTrace retention count must be positive." }
    }

    fun admitAppend(archive: DecisionTraceArchive): DecisionTraceArchive? {
        val retained = pruneOldestCommittedOverflow(archive)
        return retained.takeIf { it.traces.size <= maxTraceCount }
    }

    fun afterFinalization(archive: DecisionTraceArchive): DecisionTraceArchive =
        pruneOldestCommittedOverflow(archive)

    private fun pruneOldestCommittedOverflow(
        archive: DecisionTraceArchive,
    ): DecisionTraceArchive {
        var overflow = (archive.traces.size - maxTraceCount).coerceAtLeast(0)
        if (overflow == 0) return archive

        val retained = buildList(archive.traces.size) {
            archive.traces.forEach { trace ->
                if (
                    overflow > 0 &&
                    trace.actualChoice is DecisionTraceActualChoice.Committed
                ) {
                    overflow -= 1
                } else {
                    add(trace)
                }
            }
        }
        return if (retained.size == archive.traces.size) {
            archive
        } else {
            DecisionTraceArchive(retained)
        }
    }

    companion object {
        const val DEFAULT_MAX_TRACE_COUNT: Int = 128
    }
}

/**
 * Durable adapter for the immutable DecisionTrace archive.
 *
 * The store owns no mutable game/history state. Every operation reconstructs one complete immutable
 * archive from raw persistence, applies the archive contract, and writes a complete replacement only
 * when a new trace was admitted or one existing Pending trace was authoritatively finalized.
 *
 * Mutation serialization is owned by DecisionTraceArchivePersistenceLane. This store intentionally
 * remains synchronous so the Android transport can use one blocking durable commit inside that lane.
 */
internal class DecisionTraceArchiveStore(
    private val readRaw: () -> String?,
    private val writeRaw: (String) -> Boolean,
    private val retentionPolicy: DecisionTraceArchiveRetentionPolicy =
        DecisionTraceArchiveRetentionPolicy(),
) {
    fun load(): DecisionTraceArchive {
        val raw = readRaw()
        if (raw.isNullOrBlank()) return DecisionTraceArchive()
        return DecisionTraceArchiveJsonCodec.decode(raw)
    }

    fun append(trace: DecisionTrace): Boolean {
        val current = load()
        val appended = current.append(trace)
        if (appended === current) return true
        val retained = retentionPolicy.admitAppend(appended) ?: return false
        return writeRaw(DecisionTraceArchiveJsonCodec.encode(retained))
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
        return correlateLoadedArchive(
            current = current,
            key = key,
            confirmed = confirmed,
            committedObservation = committedObservation,
            postCommitSession = postCommitSession,
            overrideReason = overrideReason,
        )
    }

    /**
     * Correlates the only pending diagnostic trace for this canonical decision, when one exists.
     * Absence is expected when runtime shadow evaluation was ineligible, stale, cancelled or failed.
     *
     * The lookup and finalization intentionally share one decoded archive snapshot. The serialized
     * persistence lane prevents another mutation from entering between this read and replacement.
     */
    fun correlateCommittedChoiceIfPresent(
        policyVersion: PolicyVersion,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
        overrideReason: DecisionTraceOverrideReason? = null,
    ): Boolean {
        val current = load()
        val matches = current.traces.filter { trace ->
            trace.decisionId == confirmed.contextSnapshot.semanticIdentity &&
                trace.sourceRevision == confirmed.contextSnapshot.revision &&
                trace.policySnapshot.policyVersion == policyVersion &&
                trace.actualChoice is DecisionTraceActualChoice.Pending
        }
        if (matches.isEmpty()) return true
        require(matches.size == 1) {
            "DecisionTrace correlation requires exactly one matching pending diagnostic trace."
        }
        return correlateLoadedArchive(
            current = current,
            key = matches.single().archiveKey,
            confirmed = confirmed,
            committedObservation = committedObservation,
            postCommitSession = postCommitSession,
            overrideReason = overrideReason,
        )
    }

    private fun correlateLoadedArchive(
        current: DecisionTraceArchive,
        key: DecisionTraceKey,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
        overrideReason: DecisionTraceOverrideReason?,
    ): Boolean {
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
        val retained = retentionPolicy.afterFinalization(updated)
        return writeRaw(DecisionTraceArchiveJsonCodec.encode(retained))
    }
}

