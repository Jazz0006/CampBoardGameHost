package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal enum class DecisionTracePersistenceOutcome {
    COMPLETED,
    REJECTED,
    STALE,
    FAILED,
}

internal data class DecisionTracePersistenceReport(
    val outcome: DecisionTracePersistenceOutcome,
    val queueMillis: Long = 0L,
    val persistenceMillis: Long = 0L,
    val failureType: String? = null,
)

/**
 * Single mutation lane for the local DecisionTrace diagnostic archive.
 *
 * Waiting for admission remains cancellable. Once a mutation owns the lane and passes its current
 * identity gate, the blocking read-modify-write is completed on the configured background
 * dispatcher under NonCancellable so caller cancellation cannot leave an admitted physical write
 * half-executed. Canonical game/session state is never owned by this lane.
 */
internal class DecisionTraceArchivePersistenceLane(
    private val store: DecisionTraceArchiveStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val nanoTime: () -> Long = System::nanoTime,
) {
    private val mutationMutex = Mutex()

    suspend fun append(
        trace: DecisionTrace,
        stillCurrent: () -> Boolean = { true },
    ): DecisionTracePersistenceReport =
        mutate(stillCurrent = stillCurrent) {
            store.append(trace)
        }

    suspend fun correlateCommittedChoiceIfPresent(
        policyVersion: PolicyVersion,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
        overrideReason: DecisionTraceOverrideReason? = null,
    ): DecisionTracePersistenceReport =
        mutate(stillCurrent = { true }) {
            store.correlateCommittedChoiceIfPresent(
                policyVersion = policyVersion,
                confirmed = confirmed,
                committedObservation = committedObservation,
                postCommitSession = postCommitSession,
                overrideReason = overrideReason,
            )
        }

    private suspend fun mutate(
        stillCurrent: () -> Boolean,
        mutation: () -> Boolean,
    ): DecisionTracePersistenceReport {
        val queuedAt = nanoTime()
        mutationMutex.lock()
        val admittedAt = nanoTime()
        val queueMillis = elapsedMillis(queuedAt, admittedAt)
        try {
            coroutineContext.ensureActive()
            if (!stillCurrent()) {
                return DecisionTracePersistenceReport(
                    outcome = DecisionTracePersistenceOutcome.STALE,
                    queueMillis = queueMillis,
                )
            }

            val persistenceStartedAt = nanoTime()
            return try {
                val completed = withContext(NonCancellable + dispatcher) {
                    mutation()
                }
                val persistenceFinishedAt = nanoTime()
                DecisionTracePersistenceReport(
                    outcome = if (completed) {
                        DecisionTracePersistenceOutcome.COMPLETED
                    } else {
                        DecisionTracePersistenceOutcome.REJECTED
                    },
                    queueMillis = queueMillis,
                    persistenceMillis = elapsedMillis(
                        persistenceStartedAt,
                        persistenceFinishedAt,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                DecisionTracePersistenceReport(
                    outcome = DecisionTracePersistenceOutcome.FAILED,
                    queueMillis = queueMillis,
                    persistenceMillis = elapsedMillis(
                        persistenceStartedAt,
                        nanoTime(),
                    ),
                    failureType = failure::class.java.simpleName,
                )
            }
        } finally {
            mutationMutex.unlock()
        }
    }

    private fun elapsedMillis(startNanos: Long, endNanos: Long): Long =
        (endNanos - startNanos).coerceAtLeast(0L) / 1_000_000L
}
