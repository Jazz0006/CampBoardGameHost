package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.session.InformationDecisionContext
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRequestIdentity
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionView
import com.codex.campboardgamehost.clocktower.session.ConfirmedInformationDecision
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
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
    val maxElapsedMillis: Long = 1_500,
) {
    init {
        require(supportedPlayerCount > 0 && maxHistoricalEntries >= 0 && maxElapsedMillis >= 0)
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
    val elapsedMillis: Long = 0,
    val coarseHeapDeltaBytes: Long = 0,
    val traceKey: DecisionTraceKey? = null,
    val failureType: String? = null,
)

internal data class SdePostCommitCorrelationReport(
    val completed: Boolean,
    val failureType: String? = null,
)

/** Diagnostic follow-up only; callers invoke this after the canonical session commit has succeeded. */
internal object SdePostCommitCorrelationCoordinator {
    fun correlate(
        store: DecisionTraceArchiveStore,
        confirmed: ConfirmedInformationDecision,
        committedObservation: RecordedEpistemicObservation,
        postCommitSession: ClocktowerSessionView,
    ): SdePostCommitCorrelationReport = try {
        val completed = store.correlateCommittedChoiceIfPresent(
            policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
            confirmed = confirmed,
            committedObservation = committedObservation,
            postCommitSession = postCommitSession,
        )
        SdePostCommitCorrelationReport(completed = completed)
    } catch (failure: Throwable) {
        SdePostCommitCorrelationReport(
            completed = false,
            failureType = failure::class.java.simpleName,
        )
    }
}

/**
 * Failure-isolated diagnostic runtime wrapper over the C2 offline coordinator.
 * Cancellation propagates; all other diagnostic failures are reported and never become game commits.
 */
internal object SdeRuntimeShadowCoordinator {
    suspend fun <T : DynamicInformationOutcome> evaluate(
        replayInput: SdeHistoricalReplayInput,
        decisionContext: InformationDecisionContext<T>,
        validatedRuleset: ValidatedClocktowerRuleset,
        roleDefinitions: Collection<RoleDefinition>,
        currentIdentity: () -> SdeRuntimeShadowIdentity?,
        appendTrace: (DecisionTrace) -> Boolean,
        limits: SdeRuntimeShadowLimits = SdeRuntimeShadowLimits(),
        nanoTime: () -> Long = System::nanoTime,
        usedHeapBytes: () -> Long = {
            Runtime.getRuntime().let { runtime -> runtime.totalMemory() - runtime.freeMemory() }
        },
        evaluateOffline: (suspend () -> SdeOfflineReplayEvaluation)? = null,
    ): SdeRuntimeShadowReport {
        val historyEntries = replayInput.actionTimeline.entries.size + replayInput.observationLog.records.size
        if (replayInput.committedSetup.playerCount != limits.supportedPlayerCount ||
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
        val started = nanoTime()
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
            val elapsedMillis = ((nanoTime() - started).coerceAtLeast(0L)) / 1_000_000L
            val heapDelta = (usedHeapBytes() - heapBefore).coerceAtLeast(0L)
            when {
                currentIdentity() != expectedIdentity -> SdeRuntimeShadowReport(
                    SdeRuntimeShadowOutcome.STALE, elapsedMillis, heapDelta,
                )
                elapsedMillis > limits.maxElapsedMillis -> SdeRuntimeShadowReport(
                    SdeRuntimeShadowOutcome.OVER_BUDGET, elapsedMillis, heapDelta,
                )
                !appendTrace(evaluation.pendingTrace) -> SdeRuntimeShadowReport(
                    SdeRuntimeShadowOutcome.STORAGE_REJECTED, elapsedMillis, heapDelta,
                )
                else -> SdeRuntimeShadowReport(
                    if (evaluation.pendingTrace.featureEvaluation is DecisionFeatureEvaluation.Deferred) {
                        SdeRuntimeShadowOutcome.STORED_DEFERRED
                    } else {
                        SdeRuntimeShadowOutcome.STORED
                    },
                    elapsedMillis,
                    heapDelta,
                    evaluation.pendingTrace.archiveKey,
                )
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Throwable) {
            SdeRuntimeShadowReport(
                outcome = SdeRuntimeShadowOutcome.FAILED,
                elapsedMillis = ((nanoTime() - started).coerceAtLeast(0L)) / 1_000_000L,
                coarseHeapDeltaBytes = (usedHeapBytes() - heapBefore).coerceAtLeast(0L),
                failureType = failure::class.java.simpleName,
            )
        }
    }
}
