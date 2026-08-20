package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/** Rebuilds only the recipient-scoped ZDD shadows affected by one durably appended observation. */
class A4ObservationCacheRebuildExecutor(
    private val cache: A4ShadowWorldSetCache,
    private val builder: A4ObservationCacheRebuildBuilder = A4ObservationCacheRebuildBuilder { request, knowledge ->
        checkNotNull(
            A4PlayerWorldSetRuntime(A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_SHADOW))
                .build(request.formal, knowledge, request.hypothesis, request.roleDefinitions)
                .zddShadow,
        ) { "Observation rebuild must retain only the ZDD shadow." }
    },
) {
    fun execute(
        request: A4ObservationCacheRebuildRequest,
        isCancelled: () -> Boolean = { false },
    ): A4ObservationCacheRebuildReport {
        val startedNanos = System.nanoTime()
        val heapStart = heapBytes()
        request.validate()
        val appended = request.observationLog.records.single { it.recordId == request.appendedRecordId }
        val allSeats = request.formal.players.map { it.seat }.toSortedSet()
        val affected = A4ObservationCacheUpdateCoordinator.affectedSeats(appended, allSeats)
        if (request.formal.phase != StorytellerPhase.FIRST_NIGHT || request.formal.round != 1) {
            return report(affected, affected.sorted().map { seat ->
                A4ObservationCacheRebuildEntry(seat, A4ObservationCacheRebuildOutcome.DEFERRED_B4, null)
            }, startedNanos, heapStart)
        }

        val previousLog = EpistemicObservationLog(request.observationLog.records.filterNot {
            it.recordId == request.appendedRecordId
        })
        val before = A4PlayerKnowledgeFactory.createAll(request.formal, request.perceivedRolesBySeat, previousLog)
            .associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val after = A4PlayerKnowledgeFactory.createAll(request.formal, request.perceivedRolesBySeat, request.observationLog)
            .associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val generation = cache.beginVersion(request.cacheScope())

        val entries = mutableListOf<A4ObservationCacheRebuildEntry>()
        var queueStopped = false
        for (seat in allSeats) {
            val beforeKey = request.cacheKey(before.getValue(seat))
            val afterKnowledge = after.getValue(seat)
            val afterKey = request.cacheKey(afterKnowledge)
            if (queueStopped || isCancelled()) {
                cache.cancel(generation)
                queueStopped = true
                entries += A4ObservationCacheRebuildEntry(seat, A4ObservationCacheRebuildOutcome.CANCELLED, afterKey)
            } else if (seat !in affected) {
                entries += A4ObservationCacheRebuildEntry(seat, cache.unaffectedOutcome(beforeKey, afterKey), afterKey)
            } else if (!cache.isCurrent(generation)) {
                entries += A4ObservationCacheRebuildEntry(seat, A4ObservationCacheRebuildOutcome.STALE, afterKey)
            } else {
                val wasReady = cache.read(beforeKey) != null
                val value = try {
                    builder.build(request, afterKnowledge).also {
                        check(it.recipientSeat == seat) { "Rebuild builder returned another recipient's world set." }
                        check(it.knowledgeSnapshotId == afterKnowledge.knowledgeSnapshotId) {
                            "Rebuild builder returned another knowledge snapshot."
                        }
                        check(it.hypothesis == request.hypothesis) { "Rebuild builder returned another hypothesis." }
                        check(it.identity == afterKey.worldSetIdentity) { "Rebuild builder returned another world-set identity." }
                    }
                } catch (error: OutOfMemoryError) {
                    cache.cancel(generation)
                    queueStopped = true
                    entries += A4ObservationCacheRebuildEntry(
                        seat, A4ObservationCacheRebuildOutcome.RESOURCE_EXHAUSTED, afterKey, failureType = error.javaClass.simpleName,
                    )
                    continue
                } catch (error: Exception) {
                    entries += A4ObservationCacheRebuildEntry(
                        seat, A4ObservationCacheRebuildOutcome.FAILED, afterKey, failureType = error.javaClass.simpleName,
                    )
                    continue
                }
                if (isCancelled()) {
                    cache.cancel(generation)
                    queueStopped = true
                    entries += A4ObservationCacheRebuildEntry(
                        seat, A4ObservationCacheRebuildOutcome.CANCELLED, afterKey,
                    )
                    continue
                }
                val outcome = if (cache.commitIfCurrent(generation, afterKey, value)) {
                    if (wasReady) A4ObservationCacheRebuildOutcome.READY_REBUILT
                    else A4ObservationCacheRebuildOutcome.MISSING_REBUILT
                } else {
                    A4ObservationCacheRebuildOutcome.STALE
                }
                entries += A4ObservationCacheRebuildEntry(seat, outcome, afterKey, value.cardinality())
            }
        }
        return report(affected, entries, startedNanos, heapStart)
    }

    private fun report(
        affected: Set<Int>,
        entries: List<A4ObservationCacheRebuildEntry>,
        startedNanos: Long,
        heapStart: Long,
    ) = A4ObservationCacheRebuildReport(
        entries = entries,
        affectedSeats = affected.sorted(),
        totalBuildMillis = ((System.nanoTime() - startedNanos) / NANOS_PER_MILLI).coerceAtLeast(0L),
        coarseEndHeapDeltaBytes = (heapBytes() - heapStart).coerceAtLeast(0L),
    )

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
        fun heapBytes(): Long = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }
}

internal fun A4ShadowWorldSetCache.unaffectedOutcome(
    beforeKey: A4IdentityRevealPrewarmCacheKey,
    afterKey: A4IdentityRevealPrewarmCacheKey,
): A4ObservationCacheRebuildOutcome = when {
    beforeKey != afterKey -> A4ObservationCacheRebuildOutcome.FAILED_UNAFFECTED_IDENTITY_CHANGED
    read(beforeKey) != null -> A4ObservationCacheRebuildOutcome.READY_REUSED
    else -> A4ObservationCacheRebuildOutcome.MISSING_UNAFFECTED
}

fun interface A4ObservationCacheRebuildBuilder {
    fun build(request: A4ObservationCacheRebuildRequest, knowledge: PlayerKnowledgeSnapshot): PlayerWorldSet
}

data class A4ObservationCacheRebuildRequest(
    val formal: FormalGameState,
    val playerInputRevision: Long,
    val perceivedRolesBySeat: Map<Int, RoleId>,
    val observationLog: EpistemicObservationLog,
    val appendedRecordId: String,
    val hypothesis: EpistemicHypothesis,
    val roleDefinitions: Collection<RoleDefinition>,
    val rollout: A4WorldEngineRollout = A4WorldEngineRollout.ZDD_SHADOW,
) {
    internal fun validate() {
        require(rollout == A4WorldEngineRollout.ZDD_SHADOW) { "Observation rebuild is shadow-only." }
        require(playerInputRevision >= 0)
        require(perceivedRolesBySeat.keys == formal.players.map { it.seat }.toSet())
        require(observationLog.records.count { it.recordId == appendedRecordId } == 1)
        require(observationLog.records.all { record ->
            record.recipientSeats.all { it in formal.players.map { player -> player.seat } } &&
                (record.sourceSeat == null || record.sourceSeat in formal.players.map { player -> player.seat })
        })
    }

    internal fun cacheKey(knowledge: PlayerKnowledgeSnapshot) = A4IdentityRevealPrewarmCacheKey(
        gameId = formal.gameId,
        gameStateRevision = formal.gameStateRevision,
        formalSnapshotId = formal.snapshotId,
        recipientSeat = knowledge.recipientSeat,
        knowledgeSnapshotId = knowledge.knowledgeSnapshotId,
        worldSetIdentity = PlayerWorldSetIdentity.create(formal.rulesetRef, knowledge, hypothesis),
        rollout = rollout,
    )

    internal fun cacheScope() = A4ShadowCacheScope(
        formal.gameId, formal.gameStateRevision, playerInputRevision, formal.snapshotId, rollout,
    )
}

enum class A4ObservationCacheRebuildOutcome {
    READY_REBUILT,
    READY_REUSED,
    MISSING_REBUILT,
    MISSING_UNAFFECTED,
    DEFERRED_B4,
    CANCELLED,
    STALE,
    FAILED,
    RESOURCE_EXHAUSTED,
    FAILED_UNAFFECTED_IDENTITY_CHANGED,
}

data class A4ObservationCacheRebuildEntry(
    val recipientSeat: Int,
    val outcome: A4ObservationCacheRebuildOutcome,
    val key: A4IdentityRevealPrewarmCacheKey?,
    val cardinality: WorldCardinality? = null,
    val failureType: String? = null,
)

data class A4ObservationCacheRebuildReport(
    val entries: List<A4ObservationCacheRebuildEntry>,
    val affectedSeats: List<Int> = emptyList(),
    val totalBuildMillis: Long = 0L,
    val coarseEndHeapDeltaBytes: Long = 0L,
) {
    fun toLogLine(request: A4ObservationCacheRebuildRequest): String = buildString {
        append("A4_OBSERVATION_CACHE_UPDATE")
        append(" gameId=").append(request.formal.gameId)
        append(" gameStateRevision=").append(request.formal.gameStateRevision)
        append(" playerInputRevision=").append(request.playerInputRevision)
        append(" recordId=").append(request.appendedRecordId)
        append(" phase=").append(request.formal.phase)
        append(" round=").append(request.formal.round)
        append(" affected=").append(affectedSeats.joinToString(","))
        append(" status=").append(entries.joinToString(",") { entry ->
            "${entry.recipientSeat}:${entry.outcome}" + entry.failureType?.let { "/$it" }.orEmpty()
        })
        append(" totalBuildMs=").append(totalBuildMillis)
        append(" coarseEndHeapDeltaBytes=").append(coarseEndHeapDeltaBytes)
    }
}
