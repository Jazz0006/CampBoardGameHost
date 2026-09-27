package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRequestIdentity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal data class SdeRuntimeShadowIdentity(
    val requestIdentity: InformationDecisionRequestIdentity,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val nextTimelineGlobalSequence: Long,
) {
    val gameId: String get() = requestIdentity.gameId

    companion object {
        fun from(
            input: SdeHistoricalReplayInput,
            requestIdentity: InformationDecisionRequestIdentity,
        ): SdeRuntimeShadowIdentity {
            require(requestIdentity.gameId == input.gameId) {
                "Runtime shadow request identity must belong to the replay game."
            }
            return SdeRuntimeShadowIdentity(
                requestIdentity = requestIdentity,
                gameStateRevision = input.gameStateRevision,
                playerInputRevision = input.playerInputRevision,
                nextTimelineGlobalSequence = input.nextTimelineGlobalSequence,
            )
        }
    }
}

internal data class SdeRuntimeShadowLimits(
    val supportedPlayerCount: Int = 5,
    val maxHistoricalEntries: Int = 16,
    val maxEvaluationMillis: Long = 1_500,
) {
    init {
        require(
            supportedPlayerCount > 0 &&
                maxHistoricalEntries >= 0 &&
                maxEvaluationMillis >= 0,
        )
    }
}

internal enum class SdeRuntimeShadowOutcome {
    STORED,
    STORED_DEFERRED,
    INELIGIBLE,
    STALE,
    OVER_BUDGET,
    STORAGE_REJECTED,
    FAILED,
}

internal data class SdeRuntimeShadowReport(
    val outcome: SdeRuntimeShadowOutcome,
    val evaluationMillis: Long = 0L,
    val persistenceQueueMillis: Long = 0L,
    val persistenceMillis: Long = 0L,
    val totalElapsedMillis: Long = 0L,
    val coarseHeapDeltaBytes: Long = 0L,
    val traceKey: DecisionTraceKey? = null,
    val failureType: String? = null,
)

internal data class SdePostCommitCorrelationReport(
    val completed: Boolean,
    val queueMillis: Long = 0L,
    val persistenceMillis: Long = 0L,
    val failureType: String? = null,
)

/** Diagnostic follow-up only; callers invoke this after the canonical session commit has succeeded. */
internal object SdePostCommitCorrelationCoordinator {
    suspend fun correlate(
        persistenceLane: DecisionTraceArchivePersistenceLane,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
    ): SdePostCommitCorrelationReport = try {
        val persistence = persistenceLane.correlateCommittedChoiceIfPresent(
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
            confirmed = confirmed,
            committedObservation = committedObservation,
            postCommitSession = postCommitSession,
        )
        SdePostCommitCorrelationReport(
            completed = persistence.outcome == DecisionTracePersistenceOutcome.COMPLETED,
            queueMillis = persistence.queueMillis,
            persistenceMillis = persistence.persistenceMillis,
            failureType = when (persistence.outcome) {
                DecisionTracePersistenceOutcome.COMPLETED -> null
                DecisionTracePersistenceOutcome.REJECTED -> "PersistenceRejected"
                DecisionTracePersistenceOutcome.STALE -> "PersistenceStale"
                DecisionTracePersistenceOutcome.FAILED ->
                    persistence.failureType ?: "PersistenceFailed"
            },
        )
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Throwable) {
        SdePostCommitCorrelationReport(
            completed = false,
            failureType = failure::class.java.simpleName,
        )
    }
}

/**
 * Failure-isolated diagnostic runtime wrapper over the C2 offline coordinator.
 *
 * Evaluation and diagnostic persistence are separate latency boundaries. Cancellation propagates;
 * all other diagnostic failures are reported and never become game commits.
 */
internal object SdeRuntimeShadowCoordinator {
    suspend fun <T : DynamicInformationOutcome> evaluate(
        replayInput: SdeHistoricalReplayInput,
        decisionContext: InformationDecisionContext<T>,
        validatedRuleset: ValidatedClocktowerRuleset,
        roleDefinitions: Collection<RoleDefinition>,
        currentIdentity: () -> SdeRuntimeShadowIdentity?,
        persistTrace: suspend (
            trace: DecisionTrace,
            stillCurrent: () -> Boolean,
        ) -> DecisionTracePersistenceReport,
        limits: SdeRuntimeShadowLimits = SdeRuntimeShadowLimits(),
        nanoTime: () -> Long = System::nanoTime,
        usedHeapBytes: () -> Long = {
            Runtime.getRuntime().let { runtime -> runtime.totalMemory() - runtime.freeMemory() }
        },
        evaluateOffline: (suspend () -> SdeOfflineReplayEvaluation)? = null,
    ): SdeRuntimeShadowReport {
        val historyEntries =
            replayInput.actionTimeline.entries.size + replayInput.observationLog.records.size
        if (
            replayInput.committedSetup.playerCount != limits.supportedPlayerCount ||
            historyEntries > limits.maxHistoricalEntries
        ) {
            return SdeRuntimeShadowReport(SdeRuntimeShadowOutcome.INELIGIBLE)
        }
        if (decisionContext.requestIdentity.gameId != replayInput.gameId) {
            return SdeRuntimeShadowReport(SdeRuntimeShadowOutcome.STALE)
        }
        val expectedIdentity = SdeRuntimeShadowIdentity.from(
            input = replayInput,
            requestIdentity = decisionContext.requestIdentity,
        )
        if (currentIdentity() != expectedIdentity) {
            return SdeRuntimeShadowReport(SdeRuntimeShadowOutcome.STALE)
        }

        val heapBefore = usedHeapBytes()
        val totalStartedAt = nanoTime()
        var completedEvaluationMillis: Long? = null
        return try {
            coroutineContext.ensureActive()
            val evaluation = evaluateOffline?.invoke() ?: withContext(Dispatchers.Default) {
                SdeOfflineReplayCoordinator.evaluate(
                    replayInput = replayInput,
                    decisionContext = decisionContext,
                    validatedRuleset = validatedRuleset,
                    roleDefinitions = roleDefinitions,
                )
            }
            coroutineContext.ensureActive()

            val evaluationFinishedAt = nanoTime()
            val evaluationMillis = elapsedMillis(totalStartedAt, evaluationFinishedAt)
            completedEvaluationMillis = evaluationMillis
            val heapDelta = (usedHeapBytes() - heapBefore).coerceAtLeast(0L)

            when {
                currentIdentity() != expectedIdentity ->
                    reportBeforePersistence(
                        outcome = SdeRuntimeShadowOutcome.STALE,
                        evaluationMillis = evaluationMillis,
                        totalStartedAt = totalStartedAt,
                        nanoTime = nanoTime,
                        heapDelta = heapDelta,
                    )

                evaluationMillis > limits.maxEvaluationMillis ->
                    reportBeforePersistence(
                        outcome = SdeRuntimeShadowOutcome.OVER_BUDGET,
                        evaluationMillis = evaluationMillis,
                        totalStartedAt = totalStartedAt,
                        nanoTime = nanoTime,
                        heapDelta = heapDelta,
                    )

                else -> {
                    val persistence = persistTrace(evaluation.pendingTrace) {
                        currentIdentity() == expectedIdentity
                    }
                    val totalElapsedMillis = elapsedMillis(totalStartedAt, nanoTime())
                    when (persistence.outcome) {
                        DecisionTracePersistenceOutcome.COMPLETED ->
                            SdeRuntimeShadowReport(
                                outcome =
                                    if (
                                        evaluation.pendingTrace.featureEvaluation
                                            is DecisionFeatureEvaluation.Deferred
                                    ) {
                                        SdeRuntimeShadowOutcome.STORED_DEFERRED
                                    } else {
                                        SdeRuntimeShadowOutcome.STORED
                                    },
                                evaluationMillis = evaluationMillis,
                                persistenceQueueMillis = persistence.queueMillis,
                                persistenceMillis = persistence.persistenceMillis,
                                totalElapsedMillis = totalElapsedMillis,
                                coarseHeapDeltaBytes = heapDelta,
                                traceKey = evaluation.pendingTrace.archiveKey,
                            )

                        DecisionTracePersistenceOutcome.REJECTED ->
                            SdeRuntimeShadowReport(
                                outcome = SdeRuntimeShadowOutcome.STORAGE_REJECTED,
                                evaluationMillis = evaluationMillis,
                                persistenceQueueMillis = persistence.queueMillis,
                                persistenceMillis = persistence.persistenceMillis,
                                totalElapsedMillis = totalElapsedMillis,
                                coarseHeapDeltaBytes = heapDelta,
                            )

                        DecisionTracePersistenceOutcome.STALE ->
                            SdeRuntimeShadowReport(
                                outcome = SdeRuntimeShadowOutcome.STALE,
                                evaluationMillis = evaluationMillis,
                                persistenceQueueMillis = persistence.queueMillis,
                                persistenceMillis = persistence.persistenceMillis,
                                totalElapsedMillis = totalElapsedMillis,
                                coarseHeapDeltaBytes = heapDelta,
                            )

                        DecisionTracePersistenceOutcome.FAILED ->
                            SdeRuntimeShadowReport(
                                outcome = SdeRuntimeShadowOutcome.FAILED,
                                evaluationMillis = evaluationMillis,
                                persistenceQueueMillis = persistence.queueMillis,
                                persistenceMillis = persistence.persistenceMillis,
                                totalElapsedMillis = totalElapsedMillis,
                                coarseHeapDeltaBytes = heapDelta,
                                failureType =
                                    persistence.failureType ?: "PersistenceFailed",
                            )
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Throwable) {
            val totalElapsedMillis = elapsedMillis(totalStartedAt, nanoTime())
            SdeRuntimeShadowReport(
                outcome = SdeRuntimeShadowOutcome.FAILED,
                evaluationMillis = completedEvaluationMillis ?: totalElapsedMillis,
                totalElapsedMillis = totalElapsedMillis,
                coarseHeapDeltaBytes = (usedHeapBytes() - heapBefore).coerceAtLeast(0L),
                failureType = failure::class.java.simpleName,
            )
        }
    }

    private fun reportBeforePersistence(
        outcome: SdeRuntimeShadowOutcome,
        evaluationMillis: Long,
        totalStartedAt: Long,
        nanoTime: () -> Long,
        heapDelta: Long,
    ): SdeRuntimeShadowReport =
        SdeRuntimeShadowReport(
            outcome = outcome,
            evaluationMillis = evaluationMillis,
            totalElapsedMillis = elapsedMillis(totalStartedAt, nanoTime()),
            coarseHeapDeltaBytes = heapDelta,
        )

    private fun elapsedMillis(startNanos: Long, endNanos: Long): Long =
        (endNanos - startNanos).coerceAtLeast(0L) / 1_000_000L
}

