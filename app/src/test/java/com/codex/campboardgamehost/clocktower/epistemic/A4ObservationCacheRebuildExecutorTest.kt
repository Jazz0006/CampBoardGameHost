package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class A4ObservationCacheRebuildExecutorTest {
    @Test fun `private record rebuilds only its recipient and reuses exact unaffected values`() {
        val formal = formal()
        val prior = EpistemicObservationLog()
        val appended = record("private-2", ObservationVisibility.PRIVATE, setOf(2))
        val request = request(formal, prior.append(appended), appended.recordId)
        val cache = A4ShadowWorldSetCache()
        val previousKnowledge = knowledge(formal, prior)
        seed(cache, request, previousKnowledge)
        val beforeUnchanged = cache.read(request.cacheKey(previousKnowledge.getValue(1)))
        val calls = mutableListOf<Int>()
        val executor = A4ObservationCacheRebuildExecutor(cache) { active, recipientKnowledge ->
            calls += recipientKnowledge.recipientSeat
            worldSet(active, recipientKnowledge)
        }

        val report = executor.execute(request)

        assertEquals(listOf(2), calls)
        assertEquals(A4ObservationCacheRebuildOutcome.READY_REBUILT, report.entries.single { it.recipientSeat == 2 }.outcome)
        assertTrue(report.entries.filter { it.recipientSeat != 2 }.all { it.outcome == A4ObservationCacheRebuildOutcome.READY_REUSED })
        assertSame(beforeUnchanged, cache.read(request.cacheKey(knowledge(formal, request.observationLog).getValue(1))))
    }

    @Test fun `public record rebuilds every recipient sequentially and missing values remain a rebuild`() {
        val formal = formal()
        val appended = record("public", ObservationVisibility.PUBLIC, emptySet())
        val request = request(formal, EpistemicObservationLog().append(appended), appended.recordId)
        val cache = A4ShadowWorldSetCache()
        val calls = mutableListOf<Int>()
        val executor = A4ObservationCacheRebuildExecutor(cache) { active, recipientKnowledge ->
            calls += recipientKnowledge.recipientSeat
            worldSet(active, recipientKnowledge)
        }

        val report = executor.execute(request)

        assertEquals(listOf(1, 2, 3, 4, 5), calls)
        assertTrue(report.entries.all { it.outcome == A4ObservationCacheRebuildOutcome.MISSING_REBUILT })
        assertTrue(report.entries.all { entry -> cache.read(requireNotNull(entry.key)) != null })
    }

    @Test fun `rebuild derives knowledge from all prior durable records in canonical order`() {
        val formal = formal()
        val first = record("first", ObservationVisibility.PRIVATE, setOf(2), sequence = 0)
        val second = record("second", ObservationVisibility.PRIVATE, setOf(2), sequence = 1)
        val appended = record("third", ObservationVisibility.PRIVATE, setOf(2), sequence = 2)
        val request = request(formal, EpistemicObservationLog().append(second).append(first).append(appended), appended.recordId)
        val cache = A4ShadowWorldSetCache()
        var seenPrivateSequences = emptyList<Int>()
        val executor = A4ObservationCacheRebuildExecutor(cache) { active, recipientKnowledge ->
            seenPrivateSequences = recipientKnowledge.privateObservations.map { it.sequence }
            worldSet(active, recipientKnowledge)
        }

        executor.execute(request)

        assertEquals(listOf(0, 1, 2), seenPrivateSequences)
    }

    @Test fun `unaffected identity mismatch fails rather than reusing or rebuilding`() {
        val formal = formal()
        val appended = record("private-2", ObservationVisibility.PRIVATE, setOf(2))
        val request = request(formal, EpistemicObservationLog().append(appended), appended.recordId)
        val before = knowledge(formal, EpistemicObservationLog()).getValue(1)
        val after = knowledge(formal, request.observationLog).getValue(2)

        assertEquals(
            A4ObservationCacheRebuildOutcome.FAILED_UNAFFECTED_IDENTITY_CHANGED,
            A4ShadowWorldSetCache().unaffectedOutcome(request.cacheKey(before), request.cacheKey(after)),
        )
    }

    @Test fun `new generation rejects an old generation commit without clearing exact cached values`() {
        val formal = formal()
        val request = request(formal, EpistemicObservationLog(), "unused").copy(appendedRecordId = "missing")
        val knowledge = knowledge(formal, EpistemicObservationLog()).getValue(1)
        val cache = A4ShadowWorldSetCache()
        val oldGeneration = cache.beginVersion(request.cacheScope())
        val newGeneration = cache.beginVersion(request.copy(playerInputRevision = 1).cacheScope())

        assertFalse(cache.commitIfCurrent(oldGeneration, request.cacheKey(knowledge), worldSet(request, knowledge)))
        assertTrue(cache.commitIfCurrent(newGeneration, request.cacheKey(knowledge), worldSet(request, knowledge)))
    }

    @Test fun `newer revision during a build makes completed and queued work stale`() {
        val formal = formal()
        val appended = record("public", ObservationVisibility.PUBLIC, emptySet())
        val request = request(formal, EpistemicObservationLog().append(appended), appended.recordId)
        val cache = A4ShadowWorldSetCache()
        val calls = mutableListOf<Int>()
        val executor = A4ObservationCacheRebuildExecutor(cache) { active, recipientKnowledge ->
            calls += recipientKnowledge.recipientSeat
            cache.beginVersion(active.copy(playerInputRevision = 1).cacheScope())
            worldSet(active, recipientKnowledge)
        }

        val report = executor.execute(request)

        assertEquals(listOf(1), calls)
        assertTrue(report.entries.all { it.outcome == A4ObservationCacheRebuildOutcome.STALE })
        assertTrue(report.entries.all { entry -> cache.read(requireNotNull(entry.key)) == null })
    }

    @Test fun `cancellation leaves the completed entry ready and queued work cancelled`() {
        val formal = formal()
        val appended = record("public", ObservationVisibility.PUBLIC, emptySet())
        val request = request(formal, EpistemicObservationLog().append(appended), appended.recordId)
        val cancelled = AtomicBoolean(false)
        val calls = mutableListOf<Int>()
        val executor = A4ObservationCacheRebuildExecutor(A4ShadowWorldSetCache()) { active, recipientKnowledge ->
            calls += recipientKnowledge.recipientSeat
            cancelled.set(true)
            worldSet(active, recipientKnowledge)
        }

        val report = executor.execute(request) { cancelled.get() }

        assertEquals(listOf(1), calls)
        assertEquals(A4ObservationCacheRebuildOutcome.MISSING_REBUILT, report.entries.first().outcome)
        assertTrue(report.entries.drop(1).all { it.outcome == A4ObservationCacheRebuildOutcome.CANCELLED })
    }

    @Test fun `ordinary failure continues while OOM stops without a cardinality`() {
        val formal = formal()
        val appended = record("public", ObservationVisibility.PUBLIC, emptySet())
        val request = request(formal, EpistemicObservationLog().append(appended), appended.recordId)
        val ordinary = A4ObservationCacheRebuildExecutor(A4ShadowWorldSetCache()) { active, recipientKnowledge ->
            if (recipientKnowledge.recipientSeat == 1) error("synthetic")
            worldSet(active, recipientKnowledge)
        }.execute(request)
        val resource = A4ObservationCacheRebuildExecutor(A4ShadowWorldSetCache()) { _, _ ->
            throw OutOfMemoryError("synthetic")
        }.execute(request)

        assertEquals(A4ObservationCacheRebuildOutcome.FAILED, ordinary.entries.first().outcome)
        assertNull(ordinary.entries.first().cardinality)
        assertTrue(ordinary.entries.drop(1).all { it.outcome == A4ObservationCacheRebuildOutcome.MISSING_REBUILT })
        assertEquals(A4ObservationCacheRebuildOutcome.RESOURCE_EXHAUSTED, resource.entries.first().outcome)
        assertNull(resource.entries.first().cardinality)
        assertTrue(resource.entries.drop(1).all { it.outcome == A4ObservationCacheRebuildOutcome.CANCELLED })
    }

    @Test fun `non-first-night observation defers without invoking the builder`() {
        val appended = record("day", ObservationVisibility.PUBLIC, emptySet())
        val dayFormal = formal().copy(phase = StorytellerPhase.DAY)
        val request = request(dayFormal, EpistemicObservationLog().append(appended), appended.recordId)
        var calls = 0
        val report = A4ObservationCacheRebuildExecutor(A4ShadowWorldSetCache()) { active, recipientKnowledge ->
            calls += 1
            worldSet(active, recipientKnowledge)
        }.execute(request)

        assertEquals(0, calls)
        assertTrue(report.entries.all { it.outcome == A4ObservationCacheRebuildOutcome.DEFERRED_B4 })
    }

    @Test fun `report log is deterministic and contains only aggregate cache telemetry`() {
        val formal = formal()
        val appended = record("private-2", ObservationVisibility.PRIVATE, setOf(2))
        val request = request(formal, EpistemicObservationLog().append(appended), appended.recordId)
        val report = A4ObservationCacheRebuildReport(
            entries = listOf(
                A4ObservationCacheRebuildEntry(1, A4ObservationCacheRebuildOutcome.READY_REUSED, null),
                A4ObservationCacheRebuildEntry(2, A4ObservationCacheRebuildOutcome.MISSING_REBUILT, null),
            ),
            affectedSeats = listOf(2),
            totalBuildMillis = 7,
            coarseMaxHeapDeltaBytes = 11,
        )

        assertEquals(
            report.toLogLine(request),
            report.toLogLine(request),
        )
        assertTrue(report.toLogLine(request).contains("status=1:READY_REUSED,2:MISSING_REBUILT"))
        assertFalse(report.toLogLine(request).contains("Spy"))
    }

    private fun formal(): FormalGameState = FormalGameState.from(A4RuntimeFixtures.snapshot(), StorytellerPhase.FIRST_NIGHT, 1)

    private fun request(
        formal: FormalGameState,
        log: EpistemicObservationLog,
        appendedRecordId: String,
    ) = A4ObservationCacheRebuildRequest(
        formal = formal,
        playerInputRevision = 0,
        perceivedRolesBySeat = formal.players.associate { it.seat to (it.shownRole ?: it.actualRole) },
        observationLog = log,
        appendedRecordId = appendedRecordId,
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
    )

    private fun knowledge(formal: FormalGameState, log: EpistemicObservationLog) = A4PlayerKnowledgeFactory.createAll(
        formal,
        formal.players.associate { it.seat to (it.shownRole ?: it.actualRole) },
        log,
    ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)

    private fun seed(
        cache: A4ShadowWorldSetCache,
        request: A4ObservationCacheRebuildRequest,
        knowledge: Map<Int, PlayerKnowledgeSnapshot>,
    ) {
        val generation = cache.beginVersion(request.cacheScope())
        knowledge.forEach { (_, value) ->
            assertTrue(cache.commitIfCurrent(generation, request.cacheKey(value), worldSet(request, value)))
        }
    }

    private fun worldSet(request: A4ObservationCacheRebuildRequest, knowledge: PlayerKnowledgeSnapshot): PlayerWorldSet =
        EnumeratedWorldSet.fromWorlds(
            request.formal.rulesetRef,
            knowledge,
            request.hypothesis,
            request.roleDefinitions,
            listOf(EnumeratedWorld(request.formal.players.associate { it.seat to it.actualRole })),
        )

    private fun record(
        id: String,
        visibility: ObservationVisibility,
        recipients: Set<Int>,
        sequence: Int = 0,
    ) = RecordedEpistemicObservation(
        recordId = id,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = sequence,
        sourceSeat = null,
        sourceAbility = null,
        visibility = visibility,
        recipientSeats = recipients,
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.RoleInPlay(RoleId("Spy"), false),
    )
}
