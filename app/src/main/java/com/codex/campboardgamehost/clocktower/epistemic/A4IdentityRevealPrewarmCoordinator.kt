package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition

/**
 * Debug/shadow-only coordinator for using the private identity-reveal interval as an A4 prewarm
 * window. Builds are deliberately sequential; cancellation invalidates the session immediately and
 * an already-running exact build may finish only to have its result discarded as stale.
 */
class A4IdentityRevealPrewarmCoordinator(
    private val builder: A4IdentityRevealPrewarmBuilder = A4IdentityRevealPrewarmBuilder { request, knowledge ->
        val result = A4PlayerWorldSetRuntime(
            A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_SHADOW),
        ).build(request.formal, knowledge, request.hypothesis, request.roleDefinitions)
        checkNotNull(result.zddShadow) { "Identity-reveal prewarming must retain only the ZDD shadow." }
    },
    private val nanoTime: () -> Long = System::nanoTime,
    private val heapBytes: () -> Long = { Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() },
    private val cache: A4ShadowWorldSetCache = A4ShadowWorldSetCache(),
) {
    private val lock = Any()
    private var nextSessionId = 0L
    private var activeSessionId: Long? = null
    private val sessions = linkedMapOf<Long, SessionState>()

    fun start(request: A4IdentityRevealPrewarmRequest): A4IdentityRevealPrewarmSession {
        require(request.rollout == A4WorldEngineRollout.ZDD_SHADOW) {
            "Identity-reveal prewarming is shadow-only until the device gate passes."
        }
        synchronized(lock) {
            activeSessionId?.let(::cancelLocked)
            val sessionId = ++nextSessionId
            val keys = request.knowledgeBySeat.mapValues { (_, knowledge) -> request.cacheKey(knowledge) }
            val generation = cache.beginVersion(request.cacheScope())
            sessions[sessionId] = SessionState(
                request = request,
                generation = generation,
                heapStartBytes = heapBytes(),
                entries = keys.mapValuesTo(linkedMapOf()) { (seat, key) ->
                    MutableEntry(
                        key = key,
                        status = if (cache.read(key) != null) {
                            A4IdentityRevealPrewarmStatus.READY
                        } else {
                            A4IdentityRevealPrewarmStatus.QUEUED
                        },
                        elapsedMillis = if (cache.read(key) != null) 0L else null,
                        cardinality = cache.read(key)?.cardinality(),
                    )
                },
            )
            activeSessionId = sessionId
            trimOldSessions()
            return A4IdentityRevealPrewarmSession(sessionId)
        }
    }

    /** Runs all missing recipients on the caller's worker thread in one bounded sequential queue. */
    fun run(
        session: A4IdentityRevealPrewarmSession,
        prioritizedRecipientSeat: Int? = null,
    ): A4IdentityRevealPrewarmReport {
        val order = synchronized(lock) {
            val state = sessions[session.id] ?: return emptyReport(session)
            buildList {
                prioritizedRecipientSeat
                    ?.takeIf(state.request.knowledgeBySeat::containsKey)
                    ?.let(::add)
                addAll(state.request.revealOrder.filterNot(::contains))
            }
        }
        for (seat in order) {
            val work = synchronized(lock) {
                if (activeSessionId != session.id) return reportLocked(session)
                val state = sessions.getValue(session.id)
                val entry = state.entries.getValue(seat)
                if (entry.status == A4IdentityRevealPrewarmStatus.READY) {
                    null
                } else {
                    entry.status = A4IdentityRevealPrewarmStatus.BUILDING
                    state.request to state.request.knowledgeBySeat.getValue(seat)
                }
            } ?: continue

            val started = nanoTime()
            var failure: Throwable? = null
            val result = try {
                builder.build(work.first, work.second).also { worldSet ->
                    check(worldSet.recipientSeat == seat) {
                        "Prewarm builder returned another recipient's world set."
                    }
                    check(worldSet.knowledgeSnapshotId == work.second.knowledgeSnapshotId) {
                        "Prewarm builder returned another knowledge snapshot."
                    }
                    check(worldSet.hypothesis == work.first.hypothesis) {
                        "Prewarm builder returned another hypothesis."
                    }
                    check(worldSet.identity == work.first.cacheKey(work.second).worldSetIdentity) {
                        "Prewarm builder returned another world-set identity."
                    }
                }
            } catch (error: Exception) {
                failure = error
                null
            } catch (error: OutOfMemoryError) {
                failure = error
                null
            }
            if (result == null) {
                val elapsed = elapsedMillis(started)
                synchronized(lock) {
                    sessions[session.id]?.let(::recordHeapLocked)
                    sessions[session.id]?.entries?.get(seat)?.apply {
                        status = if (activeSessionId == session.id) {
                            A4IdentityRevealPrewarmStatus.FAILED
                        } else {
                            A4IdentityRevealPrewarmStatus.STALE
                        }
                        elapsedMillis = elapsed
                        failureType = failure?.javaClass?.simpleName
                    }
                }
                if (failure is OutOfMemoryError) {
                    synchronized(lock) { cancelLocked(session.id) }
                    break
                }
                continue
            }
            val elapsed = elapsedMillis(started)
            synchronized(lock) {
                val state = sessions[session.id]
                state?.let(::recordHeapLocked)
                val entry = state?.entries?.get(seat)
                if (state == null || entry == null || activeSessionId != session.id) {
                    entry?.apply {
                        status = A4IdentityRevealPrewarmStatus.STALE
                        elapsedMillis = elapsed
                    }
                } else {
                    if (cache.commitIfCurrent(state.generation, entry.key, result)) {
                        entry.status = A4IdentityRevealPrewarmStatus.READY
                        entry.elapsedMillis = elapsed
                        entry.cardinality = result.cardinality()
                    } else {
                        entry.status = A4IdentityRevealPrewarmStatus.STALE
                        entry.elapsedMillis = elapsed
                    }
                }
            }
        }
        return synchronized(lock) { reportLocked(session) }
    }

    /**
     * Invalidates publication synchronously. The exact worker may still finish its current build,
     * but its result is then stale; [acknowledgementMillis] is the device-measurable cancellation
     * response required by the production gate.
     */
    fun cancel(session: A4IdentityRevealPrewarmSession): A4IdentityRevealCancellationReport {
        val started = nanoTime()
        val cancelledEntries = synchronized(lock) {
            if (activeSessionId == session.id) cancelLocked(session.id) else 0
        }
        return A4IdentityRevealCancellationReport(
            session = session,
            acknowledgementMillis = elapsedMillis(started),
            cancelledEntries = cancelledEntries,
        )
    }

    fun ready(key: A4IdentityRevealPrewarmCacheKey): PlayerWorldSet? = cache.read(key)

    /**
     * Readiness-only probe for the first storyteller recommendation request. It deliberately never
     * returns a world set, so a shadow value cannot influence a production recommendation.
     */
    fun probe(request: A4IdentityRevealPrewarmRequest): A4IdentityRevealPrewarmDemandReport =
        synchronized(lock) {
            val readySeats = request.revealOrder.filter { seat ->
                cache.read(request.cacheKey(request.knowledgeBySeat.getValue(seat))) != null
            }
            A4IdentityRevealPrewarmDemandReport(
                gameId = request.formal.gameId,
                gameStateRevision = request.formal.gameStateRevision,
                playerInputRevision = request.playerInputRevision,
                recipientSeats = request.revealOrder,
                readySeats = readySeats,
            )
        }

    fun report(session: A4IdentityRevealPrewarmSession): A4IdentityRevealPrewarmReport =
        synchronized(lock) { reportLocked(session) }

    private fun cancelLocked(sessionId: Long): Int {
        sessions[sessionId]?.let { cache.cancel(it.generation) }
        var cancelledEntries = 0
        sessions[sessionId]?.entries?.values?.forEach { entry ->
            if (entry.status == A4IdentityRevealPrewarmStatus.QUEUED ||
                entry.status == A4IdentityRevealPrewarmStatus.BUILDING
            ) {
                entry.status = A4IdentityRevealPrewarmStatus.CANCELLED
                cancelledEntries += 1
            }
        }
        if (activeSessionId == sessionId) activeSessionId = null
        return cancelledEntries
    }

    private fun reportLocked(session: A4IdentityRevealPrewarmSession): A4IdentityRevealPrewarmReport {
        val state = sessions[session.id] ?: return emptyReport(session)
        val endHeapBytes = heapBytes()
        state.coarseMaxHeapBytes = maxOf(state.coarseMaxHeapBytes, endHeapBytes)
        return A4IdentityRevealPrewarmReport(
            session = session,
            entries = state.entries.map { (seat, value) -> value.freeze(seat) },
            active = activeSessionId == session.id,
            coarseMaxHeapDeltaBytes = (state.coarseMaxHeapBytes - state.heapStartBytes).coerceAtLeast(0L),
            coarseEndHeapDeltaBytes = (endHeapBytes - state.heapStartBytes).coerceAtLeast(0L),
        )
    }

    private fun emptyReport(session: A4IdentityRevealPrewarmSession) =
        A4IdentityRevealPrewarmReport(
            session = session,
            entries = emptyList(),
            active = false,
            coarseMaxHeapDeltaBytes = 0L,
            coarseEndHeapDeltaBytes = 0L,
        )

    /** Coarse process-heap snapshot; not a retained-size or Android-profiler measurement. */
    private fun recordHeapLocked(state: SessionState) {
        state.coarseMaxHeapBytes = maxOf(state.coarseMaxHeapBytes, heapBytes())
    }

    private fun elapsedMillis(started: Long): Long = ((nanoTime() - started) / NANOS_PER_MILLI).coerceAtLeast(0L)

    private fun trimOldSessions() {
        while (sessions.size > MAX_RETAINED_SESSION_REPORTS) {
            sessions.remove(sessions.keys.first())
        }
    }

    private data class SessionState(
        val request: A4IdentityRevealPrewarmRequest,
        val generation: A4ShadowCacheGeneration,
        val heapStartBytes: Long,
        var coarseMaxHeapBytes: Long = heapStartBytes,
        val entries: MutableMap<Int, MutableEntry>,
    )

    private data class MutableEntry(
        val key: A4IdentityRevealPrewarmCacheKey,
        var status: A4IdentityRevealPrewarmStatus,
        var elapsedMillis: Long?,
        var cardinality: WorldCardinality?,
        var failureType: String? = null,
    ) {
        fun freeze(recipientSeat: Int) = A4IdentityRevealPrewarmEntry(
            recipientSeat = recipientSeat,
            key = key,
            status = status,
            elapsedMillis = elapsedMillis,
            cardinality = cardinality,
            failureType = failureType,
        )
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
        const val MAX_RETAINED_SESSION_REPORTS = 4
    }
}

fun interface A4IdentityRevealPrewarmBuilder {
    fun build(
        request: A4IdentityRevealPrewarmRequest,
        knowledge: PlayerKnowledgeSnapshot,
    ): PlayerWorldSet
}

data class A4IdentityRevealPrewarmRequest(
    val formal: FormalGameState,
    val playerInputRevision: Long,
    val knowledgeBySeat: Map<Int, PlayerKnowledgeSnapshot>,
    val revealOrder: List<Int>,
    val hypothesis: EpistemicHypothesis,
    val roleDefinitions: Collection<RoleDefinition>,
    val rollout: A4WorldEngineRollout = A4WorldEngineRollout.ZDD_SHADOW,
) {
    init {
        require(playerInputRevision >= 0)
        val formalSeats = formal.players.map { it.seat }.toSet()
        require(knowledgeBySeat.keys == formalSeats) { "Prewarm requires one knowledge snapshot per player." }
        require(revealOrder.toSet() == formalSeats && revealOrder.size == formalSeats.size) {
            "Reveal order must contain every recipient exactly once."
        }
        require(knowledgeBySeat.values.all { it.formalSnapshotId == formal.snapshotId }) {
            "Every prewarm knowledge snapshot must be bound to the formal state."
        }
    }

    fun cacheKey(knowledge: PlayerKnowledgeSnapshot) = A4IdentityRevealPrewarmCacheKey(
        gameId = formal.gameId,
        gameStateRevision = formal.gameStateRevision,
        formalSnapshotId = formal.snapshotId,
        recipientSeat = knowledge.recipientSeat,
        knowledgeSnapshotId = knowledge.knowledgeSnapshotId,
        worldSetIdentity = PlayerWorldSetIdentity.create(formal.rulesetRef, knowledge, hypothesis),
        rollout = rollout,
    )

    fun cacheScope() = A4ShadowCacheScope(
        gameId = formal.gameId,
        gameStateRevision = formal.gameStateRevision,
        playerInputRevision = playerInputRevision,
        formalSnapshotId = formal.snapshotId,
        rollout = rollout,
    )
}

data class A4IdentityRevealPrewarmCacheKey(
    val gameId: String,
    val gameStateRevision: Long,
    val formalSnapshotId: String,
    val recipientSeat: Int,
    val knowledgeSnapshotId: String,
    val worldSetIdentity: PlayerWorldSetIdentity,
    val rollout: A4WorldEngineRollout,
)

@JvmInline
value class A4IdentityRevealPrewarmSession internal constructor(internal val id: Long)

enum class A4IdentityRevealPrewarmStatus { QUEUED, BUILDING, READY, CANCELLED, STALE, FAILED }

data class A4IdentityRevealPrewarmEntry(
    val recipientSeat: Int,
    val key: A4IdentityRevealPrewarmCacheKey,
    val status: A4IdentityRevealPrewarmStatus,
    val elapsedMillis: Long?,
    val cardinality: WorldCardinality?,
    val failureType: String?,
)

data class A4IdentityRevealPrewarmReport(
    val session: A4IdentityRevealPrewarmSession,
    val entries: List<A4IdentityRevealPrewarmEntry>,
    val active: Boolean,
    val coarseMaxHeapDeltaBytes: Long,
    val coarseEndHeapDeltaBytes: Long,
) {
    val readyCount: Int get() = entries.count { it.status == A4IdentityRevealPrewarmStatus.READY }
    val totalElapsedMillis: Long get() = entries.sumOf { it.elapsedMillis ?: 0L }

    fun toLogLine(frameTelemetry: A4MainThreadFrameTelemetrySummary? = null): String = buildString {
        append("A4_IDENTITY_PREWARM recipients=").append(entries.size)
        append(" ready=").append(readyCount)
        append(" totalBuildMs=").append(totalElapsedMillis)
        append(" coarseMaxHeapDeltaBytes=").append(coarseMaxHeapDeltaBytes)
        append(" coarseEndHeapDeltaBytes=").append(coarseEndHeapDeltaBytes)
        append(" status=").append(entries.joinToString(",") { "${it.recipientSeat}:${it.status.name}" })
        frameTelemetry?.let { telemetry ->
            append(" frameSamples=").append(telemetry.sampleCount)
            append(" mainThreadFrameP50P95Ms=").append(telemetry.p50Millis).append('/').append(telemetry.p95Millis)
            append(" frameOver32Ms=").append(telemetry.over32MillisCount)
            append(" frameOver50Ms=").append(telemetry.over50MillisCount)
            append(" mainThreadMaxFrameIntervalMs=").append(telemetry.maxMillis)
        }
    }
}

/** Aggregate-only cancellation measurement; it never includes a world set or player knowledge. */
data class A4IdentityRevealCancellationReport(
    val session: A4IdentityRevealPrewarmSession,
    val acknowledgementMillis: Long,
    val cancelledEntries: Int,
) {
    init {
        require(acknowledgementMillis >= 0)
        require(cancelledEntries >= 0)
    }

    fun toLogLine(): String = "A4_IDENTITY_PREWARM_CANCEL session=${session.id} " +
        "acknowledgementMs=$acknowledgementMillis cancelledEntries=$cancelledEntries"
}

/** Main-thread frame-interval distribution for a bounded debug measurement window. */
class A4MainThreadFrameTelemetry {
    private var previousFrameNanos: Long? = null
    private val intervalsMillis = mutableListOf<Long>()

    /** The first frame establishes a baseline and therefore contributes no interval sample. */
    fun recordFrame(frameNanos: Long) {
        previousFrameNanos?.let { previous ->
            intervalsMillis += ((frameNanos - previous) / NANOS_PER_MILLI).coerceAtLeast(0L)
        }
        previousFrameNanos = frameNanos
    }

    fun summary(): A4MainThreadFrameTelemetrySummary {
        if (intervalsMillis.isEmpty()) return A4MainThreadFrameTelemetrySummary.EMPTY
        val sorted = intervalsMillis.sorted()
        return A4MainThreadFrameTelemetrySummary(
            sampleCount = sorted.size,
            p50Millis = percentile(sorted, 0.50),
            p95Millis = percentile(sorted, 0.95),
            over32MillisCount = sorted.count { it > FRAME_BUDGET_32_MILLIS },
            over50MillisCount = sorted.count { it > FRAME_BUDGET_50_MILLIS },
            maxMillis = sorted.last(),
        )
    }

    private fun percentile(sorted: List<Long>, fraction: Double): Long =
        sorted[(kotlin.math.ceil(sorted.size * fraction).toInt() - 1).coerceIn(0, sorted.lastIndex)]

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
        const val FRAME_BUDGET_32_MILLIS = 32L
        const val FRAME_BUDGET_50_MILLIS = 50L
    }
}

data class A4MainThreadFrameTelemetrySummary(
    val sampleCount: Int,
    val p50Millis: Long?,
    val p95Millis: Long?,
    val over32MillisCount: Int,
    val over50MillisCount: Int,
    val maxMillis: Long?,
) {
    companion object {
        val EMPTY = A4MainThreadFrameTelemetrySummary(0, null, null, 0, 0, null)
    }
}

/** Aggregate-only result used to prove cache availability without exposing a cached world set. */
data class A4IdentityRevealPrewarmDemandReport(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val recipientSeats: List<Int>,
    val readySeats: List<Int>,
) {
    val recipientCount: Int get() = recipientSeats.size
    val readyCount: Int get() = readySeats.size
    val missingSeats: List<Int> get() = recipientSeats.filterNot(readySeats::contains)

    fun toLogLine(): String = buildString {
        append("A4_IDENTITY_PREWARM_DEMAND recipients=").append(recipientCount)
        append(" ready=").append(readyCount)
        append(" missingSeats=").append(missingSeats.joinToString(",").ifBlank { "none" })
        append(" gameStateRevision=").append(gameStateRevision)
        append(" playerInputRevision=").append(playerInputRevision)
    }
}
