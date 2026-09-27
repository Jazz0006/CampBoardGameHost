package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionTraceArchivePersistenceLaneTest {
    @Test
    fun `serialized concurrent appends preserve both archive updates`() = runBlocking {
        var stored: String? = null
        val firstWriteStarted = CountDownLatch(1)
        val releaseFirstWrite = CountDownLatch(1)
        val writes = AtomicInteger(0)
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw ->
                if (writes.incrementAndGet() == 1) {
                    firstWriteStarted.countDown()
                    assertTrue(releaseFirstWrite.await(5, TimeUnit.SECONDS))
                }
                stored = raw
                true
            },
        )
        val lane = DecisionTraceArchivePersistenceLane(store)

        val first = async(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("first"))
        }
        assertTrue(firstWriteStarted.await(5, TimeUnit.SECONDS))
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("second"))
        }
        releaseFirstWrite.countDown()

        assertEquals(DecisionTracePersistenceOutcome.COMPLETED, first.await().outcome)
        assertEquals(DecisionTracePersistenceOutcome.COMPLETED, second.await().outcome)
        assertEquals(
            setOf("first", "second"),
            store.load().traces.map(DecisionTrace::decisionId).toSet(),
        )
        assertEquals(2, writes.get())
    }

    @Test
    fun `queued append rechecks current identity after preceding persistence`() = runBlocking {
        var stored: String? = null
        val firstWriteStarted = CountDownLatch(1)
        val releaseFirstWrite = CountDownLatch(1)
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw ->
                if (stored == null) {
                    firstWriteStarted.countDown()
                    assertTrue(releaseFirstWrite.await(5, TimeUnit.SECONDS))
                }
                stored = raw
                true
            },
        )
        val lane = DecisionTraceArchivePersistenceLane(store)

        val first = async(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("first"))
        }
        assertTrue(firstWriteStarted.await(5, TimeUnit.SECONDS))
        val stale = async(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("stale"), stillCurrent = { false })
        }
        releaseFirstWrite.countDown()

        assertEquals(DecisionTracePersistenceOutcome.COMPLETED, first.await().outcome)
        assertEquals(DecisionTracePersistenceOutcome.STALE, stale.await().outcome)
        assertEquals(listOf("first"), store.load().traces.map(DecisionTrace::decisionId))
    }

    @Test
    fun `cancelled queued append never enters durable archive`() = runBlocking {
        var stored: String? = null
        val firstWriteStarted = CountDownLatch(1)
        val releaseFirstWrite = CountDownLatch(1)
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw ->
                if (stored == null) {
                    firstWriteStarted.countDown()
                    assertTrue(releaseFirstWrite.await(5, TimeUnit.SECONDS))
                }
                stored = raw
                true
            },
        )
        val lane = DecisionTraceArchivePersistenceLane(store)

        val first = async(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("first"))
        }
        assertTrue(firstWriteStarted.await(5, TimeUnit.SECONDS))
        val cancelled = launch(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("cancelled"))
        }
        cancelled.cancel()
        releaseFirstWrite.countDown()
        cancelled.join()

        assertEquals(DecisionTracePersistenceOutcome.COMPLETED, first.await().outcome)
        assertEquals(listOf("first"), store.load().traces.map(DecisionTrace::decisionId))
    }

    @Test
    fun `admitted persistence finishes even if caller is cancelled during physical write`() = runBlocking {
        var stored: String? = null
        val writeStarted = CountDownLatch(1)
        val releaseWrite = CountDownLatch(1)
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw ->
                writeStarted.countDown()
                assertTrue(releaseWrite.await(5, TimeUnit.SECONDS))
                stored = raw
                true
            },
        )
        val lane = DecisionTraceArchivePersistenceLane(store)

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            lane.append(pendingTrace("accepted"))
        }
        assertTrue(writeStarted.await(5, TimeUnit.SECONDS))
        job.cancel()
        releaseWrite.countDown()
        job.join()

        assertEquals(listOf("accepted"), store.load().traces.map(DecisionTrace::decisionId))
    }

    @Test
    fun `physical persistence runs on configured background dispatcher`() = runBlocking {
        var writeThreadName: String? = null
        var stored: String? = null
        val dispatcher = Executors.newSingleThreadExecutor { task ->
            Thread(task, "rh-e-persistence-test")
        }.asCoroutineDispatcher()
        try {
            val store = DecisionTraceArchiveStore(
                readRaw = { stored },
                writeRaw = { raw ->
                    writeThreadName = Thread.currentThread().name
                    stored = raw
                    true
                },
            )
            val lane = DecisionTraceArchivePersistenceLane(store, dispatcher = dispatcher)

            assertEquals(
                DecisionTracePersistenceOutcome.COMPLETED,
                lane.append(pendingTrace("background")).outcome,
            )
            assertEquals("rh-e-persistence-test", writeThreadName)
        } finally {
            dispatcher.close()
        }
    }

    @Test
    fun `persistence report separates queue and physical write latency`() = runBlocking {
        var stored: String? = null
        val nanos = listOf(0L, 3_000_000L, 5_000_000L, 12_000_000L).iterator()
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw -> stored = raw; true },
        )
        val lane = DecisionTraceArchivePersistenceLane(
            store = store,
            dispatcher = Dispatchers.Unconfined,
            nanoTime = { nanos.next() },
        )

        val report = lane.append(pendingTrace("timed"))

        assertEquals(DecisionTracePersistenceOutcome.COMPLETED, report.outcome)
        assertEquals(3L, report.queueMillis)
        assertEquals(7L, report.persistenceMillis)
    }

    @Test
    fun `retention prunes oldest committed trace before pending correlation material`() {
        var stored: String? = null
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw -> stored = raw; true },
            retentionPolicy = DecisionTraceArchiveRetentionPolicy(maxTraceCount = 2),
        )
        val oldestCommitted = committedTrace("oldest")
        val pending = pendingTrace("pending")
        val newest = pendingTrace("newest")

        assertTrue(store.append(oldestCommitted))
        assertTrue(store.append(pending))
        assertTrue(store.append(newest))

        assertEquals(
            listOf("pending", "newest"),
            store.load().traces.map(DecisionTrace::decisionId),
        )
    }

    @Test
    fun `retention rejects growth when all bounded entries remain pending`() {
        var stored: String? = null
        var writes = 0
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw -> writes += 1; stored = raw; true },
            retentionPolicy = DecisionTraceArchiveRetentionPolicy(maxTraceCount = 2),
        )

        assertTrue(store.append(pendingTrace("one")))
        assertTrue(store.append(pendingTrace("two")))
        assertFalse(store.append(pendingTrace("three")))

        assertEquals(2, writes)
        assertEquals(listOf("one", "two"), store.load().traces.map(DecisionTrace::decisionId))
    }

    private fun pendingTrace(decisionId: String): DecisionTrace {
        val candidateId = "candidate-$decisionId"
        val candidates = listOf(candidateId)
        return DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("rh-e-test"),
            decisionId = decisionId,
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
            ),
            sourceRevision = InformationDecisionRevision(
                gameStateRevision = 1L,
                playerInputRevision = 1L,
            ),
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = "rh-e-game",
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = candidates,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidates,
                missingCapabilities = setOf(EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateIds = candidates,
                reasons = setOf(PolicyDeferralCode("rh-e-test")),
            ),
            policySelection = null,
        )
    }

    private fun committedTrace(decisionId: String): DecisionTrace {
        val pending = pendingTrace(decisionId)
        return pending.copy(
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = pending.legalCandidateIds.single(),
                source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
                manualOverride = false,
            ),
        )
    }
}
