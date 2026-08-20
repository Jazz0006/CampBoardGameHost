package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class A4IdentityRevealPrewarmCoordinatorTest {
    @Test fun `cache key and world-set identity distinguish hypotheses but not runtime input revision`() {
        val request = request()
        val knowledge = request.knowledgeBySeat.getValue(1)

        val credibleKey = request.cacheKey(knowledge)
        val functioningKey = request.copy(
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
        ).cacheKey(knowledge)
        val nextInputKey = request.copy(
            playerInputRevision = request.playerInputRevision + 1,
        ).cacheKey(knowledge)

        assertTrue(credibleKey != functioningKey)
        assertTrue(credibleKey.worldSetIdentity != functioningKey.worldSetIdentity)
        assertEquals(credibleKey, nextInputKey)
    }

    @Test fun `identity mismatches fail without writing a cache value`() {
        val request = request()
        val firstKnowledge = request.knowledgeBySeat.getValue(1)
        val secondKnowledge = request.knowledgeBySeat.getValue(2)
        val mismatches = listOf<A4IdentityRevealPrewarmBuilder>(
            A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                val wrongSeat = if (knowledge.recipientSeat == 1) 2 else 1
                worldSet(activeRequest, activeRequest.knowledgeBySeat.getValue(wrongSeat))
            },
            A4IdentityRevealPrewarmBuilder { activeRequest, _ ->
                worldSet(activeRequest, firstKnowledge.copy(knowledgeSnapshotId = secondKnowledge.knowledgeSnapshotId))
            },
            A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                EnumeratedWorldSet.fromWorlds(
                    rulesetRef = activeRequest.formal.rulesetRef,
                    knowledge = knowledge,
                    hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
                    roleDefinitions = activeRequest.roleDefinitions,
                    worlds = listOf(EnumeratedWorld(activeRequest.formal.players.associate { it.seat to it.actualRole })),
                )
            },
            A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                EnumeratedWorldSet.fromWorlds(
                    rulesetRef = activeRequest.formal.rulesetRef.copy(scriptContentHash = "a".repeat(32)),
                    knowledge = knowledge,
                    hypothesis = activeRequest.hypothesis,
                    roleDefinitions = activeRequest.roleDefinitions,
                    worlds = listOf(EnumeratedWorld(activeRequest.formal.players.associate { it.seat to it.actualRole })),
                )
            },
        )

        mismatches.forEach { builder ->
            val coordinator = A4IdentityRevealPrewarmCoordinator(builder = builder)
            val report = coordinator.run(coordinator.start(request))
            val failed = report.entries.first()

            assertEquals(A4IdentityRevealPrewarmStatus.FAILED, failed.status)
            assertEquals("IllegalStateException", failed.failureType)
            assertTrue(report.entries.all { coordinator.ready(it.key) == null })
        }
    }

    @Test fun `builds recipients sequentially with the displayed seat first and reuses exact keys`() {
        val request = request()
        val order = mutableListOf<Int>()
        val clock = AtomicLong()
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                order += knowledge.recipientSeat
                worldSet(activeRequest, knowledge)
            },
            nanoTime = { clock.addAndGet(1_000_000L) },
        )

        val firstSession = coordinator.start(request)
        val first = coordinator.run(firstSession, prioritizedRecipientSeat = 3)

        assertEquals(listOf(3, 1, 2, 4, 5), order)
        assertEquals(5, first.readyCount)
        assertTrue(first.entries.all { it.status == A4IdentityRevealPrewarmStatus.READY })
        first.entries.forEach { entry ->
            assertEquals(entry.recipientSeat, coordinator.ready(entry.key)?.recipientSeat)
        }

        val retained = first.entries.associate { it.key to coordinator.ready(it.key) }
        val secondSession = coordinator.start(request)
        val second = coordinator.run(secondSession, prioritizedRecipientSeat = 5)

        assertEquals("A second identical session must not rebuild ready entries.", 5, order.size)
        assertTrue(second.entries.all { it.elapsedMillis == 0L })
        retained.forEach { (key, value) -> assertSame(value, coordinator.ready(key)) }
    }

    @Test fun `cancellation discards an in-flight result and never exposes it as ready`() {
        val request = request()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                entered.countDown()
                assertTrue(release.await(5, TimeUnit.SECONDS))
                worldSet(activeRequest, knowledge)
            },
        )
        val session = coordinator.start(request)
        val worker = thread(start = true, name = "a4-prewarm-test") { coordinator.run(session) }

        assertTrue(entered.await(5, TimeUnit.SECONDS))
        val cancellation = coordinator.cancel(session)
        assertTrue(cancellation.acknowledgementMillis >= 0)
        assertEquals(5, cancellation.cancelledEntries)
        release.countDown()
        worker.join(5_000)

        val report = coordinator.report(session)
        assertEquals(A4IdentityRevealPrewarmStatus.STALE, report.entries.first().status)
        assertTrue(report.entries.drop(1).all { it.status == A4IdentityRevealPrewarmStatus.CANCELLED })
        assertTrue(report.entries.all { coordinator.ready(it.key) == null })
    }

    @Test fun `a newer formal revision cannot hit an old cache key`() {
        val firstRequest = request()
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder(::worldSet),
        )
        val firstSession = coordinator.start(firstRequest)
        val firstReport = coordinator.run(firstSession)
        val oldKey = firstReport.entries.first().key
        assertTrue(coordinator.ready(oldKey) != null)

        val nextSnapshot = A4RuntimeFixtures.snapshot().copy(gameStateRevision = 1)
        val nextRequest = request(nextSnapshot)
        val nextSession = coordinator.start(nextRequest)
        val nextKey = coordinator.report(nextSession).entries.first().key

        assertTrue(oldKey != nextKey)
        assertNull(coordinator.ready(nextKey))
    }

    @Test fun `demand probe reports only exact revision bound cache hits`() {
        val firstRequest = request()
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder(::worldSet),
        )
        coordinator.run(coordinator.start(firstRequest))

        val hit = coordinator.probe(firstRequest)
        val nextRequest = request(A4RuntimeFixtures.snapshot().copy(gameStateRevision = 1))
        val miss = coordinator.probe(nextRequest)

        assertEquals(5, hit.readyCount)
        assertTrue(hit.missingSeats.isEmpty())
        assertEquals(0, miss.readyCount)
        assertEquals(listOf(1, 2, 3, 4, 5), miss.missingSeats)
    }

    @Test fun `builder failure is telemetry state rather than an empty logical result`() {
        val request = request()
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                if (knowledge.recipientSeat == 1) error("synthetic build failure")
                worldSet(activeRequest, knowledge)
            },
        )

        val report = coordinator.run(coordinator.start(request))
        val failed = report.entries.first()

        assertEquals(A4IdentityRevealPrewarmStatus.FAILED, failed.status)
        assertEquals("IllegalStateException", failed.failureType)
        assertNull(failed.cardinality)
        assertNull(coordinator.ready(failed.key))
        assertEquals(4, report.readyCount)
    }

    @Test fun `resource exhaustion stops the queue without producing UNSAT`() {
        val request = request()
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { _, _ -> throw OutOfMemoryError("synthetic") },
        )

        val report = coordinator.run(coordinator.start(request))

        assertEquals(A4IdentityRevealPrewarmStatus.FAILED, report.entries.first().status)
        assertEquals("OutOfMemoryError", report.entries.first().failureType)
        assertNull(report.entries.first().cardinality)
        assertTrue(report.entries.drop(1).all { it.status == A4IdentityRevealPrewarmStatus.CANCELLED })
        assertEquals(0, report.readyCount)
        assertEquals(false, report.active)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validated rollout cannot enter identity reveal prewarming`() {
        A4IdentityRevealPrewarmCoordinator().start(
            request().copy(rollout = A4WorldEngineRollout.ZDD_DEVICE_VALIDATED),
        )
    }

    @Test fun `frame telemetry reports percentiles and jank thresholds without a first-frame sample`() {
        val telemetry = A4MainThreadFrameTelemetry()
        listOf(0L, 16L, 49L, 82L, 140L, 198L).forEach { millis ->
            telemetry.recordFrame(millis * 1_000_000L)
        }

        val summary = telemetry.summary()

        assertEquals(5, summary.sampleCount)
        assertEquals(33L, summary.p50Millis)
        assertEquals(58L, summary.p95Millis)
        assertEquals(4, summary.over32MillisCount)
        assertEquals(2, summary.over50MillisCount)
        assertEquals(58L, summary.maxMillis)
    }

    @Test fun `prewarm report exposes a coarse process heap delta without claiming retained size`() {
        val request = request()
        var heapBytes = 100L
        val coordinator = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                heapBytes += 40L
                worldSet(activeRequest, knowledge)
            },
            heapBytes = { heapBytes },
        )

        val report = coordinator.run(coordinator.start(request))

        assertEquals(200L, report.coarseMaxHeapDeltaBytes)
        assertEquals(200L, report.coarseEndHeapDeltaBytes)
    }

    private fun request(
        snapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot = A4RuntimeFixtures.snapshot(),
    ): A4IdentityRevealPrewarmRequest {
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceived = formal.players.associate { it.seat to it.shownRole.orElse(it.actualRole) }
        val knowledge = A4PlayerKnowledgeFactory.createAll(formal, perceived, emptyList())
            .associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        return A4IdentityRevealPrewarmRequest(
            formal = formal,
            playerInputRevision = snapshot.playerInputRevision,
            knowledgeBySeat = knowledge,
            revealOrder = formal.players.map { it.seat },
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )
    }

    private fun worldSet(
        request: A4IdentityRevealPrewarmRequest,
        knowledge: PlayerKnowledgeSnapshot,
    ): PlayerWorldSet = EnumeratedWorldSet.fromWorlds(
        rulesetRef = request.formal.rulesetRef,
        knowledge = knowledge,
        hypothesis = request.hypothesis,
        roleDefinitions = request.roleDefinitions,
        worlds = listOf(EnumeratedWorld(request.formal.players.associate { it.seat to it.actualRole })),
    )

    private fun RoleId?.orElse(fallback: RoleId): RoleId = this ?: fallback
}
