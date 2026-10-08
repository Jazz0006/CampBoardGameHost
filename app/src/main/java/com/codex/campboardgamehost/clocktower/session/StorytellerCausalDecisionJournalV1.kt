package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryCutoffSourceV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryPrefixV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPlayerContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotPosition
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSetupState
import java.util.Collections

/**
 * Exact typed snapshot identity, not just game/input revisions. Contents are immutable scalar
 * snapshots; no localized role labels or inference from a later GameState.
 */
internal data class FrozenStorytellerSnapshotIdentityV1(
    val gameId: String,
    val gameSeed: Long,
    val position: TroubleBrewingSnapshotPosition,
    val setupState: TroubleBrewingSnapshotSetupState,
    val seats: List<TroubleBrewingSnapshotSeat>,
)

/**
 * At-request-time captured data. Caller must retain it as-is; the current Session's cursor is NOT
 * a substitute for this frozen prefix when historical decisions are reviewed.
 */
internal class FrozenStorytellerDecisionPrefixV1 internal constructor(
    val identity: StorytellerProviderDecisionIdentityV1,
    val revision: StorytellerProviderRevisionV1,
    val snapshotIdentity: FrozenStorytellerSnapshotIdentityV1,
    val historyPrefix: StorytellerProviderHistoryPrefixV1,
    players: List<StorytellerProviderPlayerContextV1>,
    legalCandidateIds: List<String>,
) {
    val players: List<StorytellerProviderPlayerContextV1> = Collections.unmodifiableList(
        players.map { it.copy(claimedRoleIds = Collections.unmodifiableList(it.claimedRoleIds.toList())) },
    )
    val legalCandidateIds: List<String> = Collections.unmodifiableList(legalCandidateIds.toList())
    val exclusiveGlobalSequence: Long = requireNotNull(historyPrefix.exclusiveGlobalSequence)

    init {
        require(identity.gameId == snapshotIdentity.gameId && identity.gameId == historyPrefix.gameId)
        require(historyPrefix.sourceRevision == revision)
        require(historyPrefix.cutoffSource == StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED)
        require(legalCandidateIds.isNotEmpty() && legalCandidateIds.distinct().size == legalCandidateIds.size)
    }

    override fun equals(other: Any?): Boolean = other is FrozenStorytellerDecisionPrefixV1 &&
        identity == other.identity && revision == other.revision &&
        snapshotIdentity == other.snapshotIdentity && historyPrefix == other.historyPrefix &&
        players == other.players && legalCandidateIds == other.legalCandidateIds

    override fun hashCode(): Int = listOf(
        identity, revision, snapshotIdentity, historyPrefix, players, legalCandidateIds,
    ).hashCode()
}

/**
 * R1C2A: fail-closed capture seam for an already-materialized neutral Provider request.
 *
 * No network/recommender, no write to Game Engine, and no reconstruction from legacy/local facts.
 * Must be called with the frozen SessionState that actually produced the request.
 */
internal object StorytellerDecisionPrefixCaptureV1 {
    fun capture(
        request: StorytellerProviderRequestV1,
        sessionState: ClocktowerSessionState,
    ): FrozenStorytellerDecisionPrefixV1 {
        require(sessionState.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            "Historical decision capture needs a verifiable global history."
        }
        require(request.identity.gameId == sessionState.gameId)
        require(request.sourceRevision == StorytellerProviderRevisionV1(
            sessionState.gameStateRevision, sessionState.playerInputRevision,
        ))
        // Prior typed registrations enter this context only through this journal's verified
        // commits. Unverified candidate witnesses are still forbidden in the commit path.

        val snapshot = (request.state as? com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameStateV1.TroubleBrewing)
            ?.snapshot ?: error("R1C2A supports only the typed Trouble Brewing snapshot.")
        require(snapshot.gameId == sessionState.gameId && snapshot.gameSeed == sessionState.gameSeed)
        val currentSeats = sessionState.gameState.players.sortedBy { it.seat }
        require(snapshot.grimoireSeats.map { it.seat } == currentSeats.map { it.seat })
        snapshot.grimoireSeats.zip(currentSeats).forEach { (seat, player) ->
            (seat.actualRoleId as? SnapshotField.Known<String>)?.let {
                require(it.value == player.actualRole.value) { "Snapshot actual role diverges from frozen Session." }
            }
            (seat.shownRoleId as? SnapshotField.Known<String>)?.let {
                require(it.value == (player.shownRole ?: player.actualRole).value) { "Snapshot shown role diverges from frozen Session." }
            }
            (seat.alive as? SnapshotField.Known<Boolean>)?.let {
                require(it.value == player.alive) { "Snapshot life state diverges from frozen Session." }
            }
            (seat.poisoned as? SnapshotField.Known<Boolean>)?.let {
                require(it.value == player.poisoned) { "Snapshot poison state diverges from frozen Session." }
            }
        }
        (snapshot.position.gameStateRevision as? SnapshotField.Known<Long>)?.let {
            require(it.value == request.sourceRevision.gameStateRevision)
        }
        (snapshot.position.playerInputRevision as? SnapshotField.Known<Long>)?.let {
            require(it.value == request.sourceRevision.playerInputRevision)
        }
        val actual = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            sessionState, request.sourceRevision,
        )
        require(request.gameContext.historyPrefix == actual) {
            "Request must already contain exactly the captured live Session history and cutoff."
        }
        require(request.gameContext.players.map { it.seat } == snapshot.grimoireSeats.map { it.seat }) {
            "Frozen player context must cover the same canonical seats."
        }
        return FrozenStorytellerDecisionPrefixV1(
            identity = request.identity,
            revision = request.sourceRevision,
            snapshotIdentity = FrozenStorytellerSnapshotIdentityV1(
                gameId = snapshot.gameId,
                gameSeed = snapshot.gameSeed,
                position = snapshot.position,
                setupState = snapshot.setupState,
                seats = Collections.unmodifiableList(snapshot.grimoireSeats.toList()),
            ),
            historyPrefix = actual,
            players = request.gameContext.players,
            legalCandidateIds = request.legalCandidateIds,
        )
    }
}

/**
 * Exact ordered journal entries used by the current-format Recovery codec. Unlike a final
 * DecisionHistoryProjection, corrections retain their original position in the chronology.
 */
internal sealed interface StorytellerCausalJournalRecordV1 {
    data class Captured(val frozen: FrozenStorytellerDecisionPrefixV1) : StorytellerCausalJournalRecordV1
    data class Committed(
        val decisionId: String,
        val value: StorytellerProviderPriorDecisionV1,
    ) : StorytellerCausalJournalRecordV1
    data class Corrected(
        val correctionId: String,
        val replacedEventId: String,
        val replacementEventId: String,
    ) : StorytellerCausalJournalRecordV1
}

internal data class StorytellerCausalJournalArchiveV1(
    val gameId: String,
    val records: List<StorytellerCausalJournalRecordV1>,
) {
    init { require(gameId.isNotBlank()) }
}

/**
 * R1C2A in-memory reference implementation of independent decision chronology.
 * Distinct from the mechanical global timeline: two decisions may occur at the same game/input
 * revision and the same mechanical cursor, yet a later correction must not rewrite an old prefix.
 *
 * NO production writer or Recovery codec exists yet. Do not claim this is durable or exhaustive.
 */
internal class StorytellerCausalDecisionJournalV1(private val gameId: String) {
    private sealed interface Entry {
        data class Captured(val frozen: FrozenStorytellerDecisionPrefixV1) : Entry
        data class Committed(val decisionId: String, val value: StorytellerProviderPriorDecisionV1) : Entry
        data class Corrected(
            val correctionId: String,
            val replacedEventId: String,
            val replacementEventId: String,
        ) : Entry
    }

    private val entries = mutableListOf<Entry>()

    init { require(gameId.isNotBlank()) }

    fun archive(): StorytellerCausalJournalArchiveV1 = StorytellerCausalJournalArchiveV1(
        gameId, Collections.unmodifiableList(entries.map { entry ->
            when (entry) {
                is Entry.Captured -> StorytellerCausalJournalRecordV1.Captured(entry.frozen)
                is Entry.Committed -> StorytellerCausalJournalRecordV1.Committed(
                    entry.decisionId, immutableDecision(entry.value),
                )
                is Entry.Corrected -> StorytellerCausalJournalRecordV1.Corrected(
                    entry.correctionId, entry.replacedEventId, entry.replacementEventId,
                )
            }
        }),
    )

    companion object {
        /**
         * Restore from typed Recovery, validating every historical capture against the *actual*
         * recovered append-only global facts. No future event or amended correction is admitted.
         */
        fun restore(
            archive: StorytellerCausalJournalArchiveV1,
            currentSession: ClocktowerSessionState,
        ): StorytellerCausalDecisionJournalV1 {
            require(archive.gameId == currentSession.gameId)
            require(currentSession.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
            val journal = StorytellerCausalDecisionJournalV1(archive.gameId)
            archive.records.forEach { record ->
                when (record) {
                    is StorytellerCausalJournalRecordV1.Captured -> {
                        val frozen = record.frozen
                        require(frozen.identity.gameId == archive.gameId)
                        require(frozen.exclusiveGlobalSequence <= currentSession.nextTimelineGlobalSequence)
                        require(journal.entries.none {
                            it is Entry.Captured && it.frozen.identity.decisionId == frozen.identity.decisionId
                        })
                        require(frozen.revision.gameStateRevision <= currentSession.gameStateRevision)
                        require(frozen.revision.playerInputRevision <= currentSession.playerInputRevision)
                        val full = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
                            currentSession,
                            StorytellerProviderRevisionV1(
                                currentSession.gameStateRevision,
                                currentSession.playerInputRevision,
                            ),
                        )
                        val rebuilt = StorytellerProviderHistoryPrefixV1.capturedLive(
                            archive.gameId, frozen.revision, frozen.exclusiveGlobalSequence,
                            full.entries.filter { it.point.globalSequence < frozen.exclusiveGlobalSequence },
                        )
                        require(frozen.historyPrefix == rebuilt) {
                            "Saved decision history differs from canonical recovered facts."
                        }
                        journal.entries += Entry.Captured(frozen)
                    }
                    is StorytellerCausalJournalRecordV1.Committed ->
                        journal.commit(record.decisionId, record.value)
                    is StorytellerCausalJournalRecordV1.Corrected ->
                        journal.correct(record.correctionId, record.replacedEventId, record.replacementEventId)
                }
            }
            return journal
        }
    }

    private fun immutableDecision(event: StorytellerProviderPriorDecisionV1) = event.copy(
        selectedOutcome = event.selectedOutcome.copy(
            canonicalFields = Collections.unmodifiableMap(LinkedHashMap(event.selectedOutcome.canonicalFields.toSortedMap())),
        ),
        registrations = Collections.unmodifiableList(event.registrations.toList()),
    )

    /** Preview uses only THIS journal's known prior decisions, not a revised legacy archive. */
    fun contextForRequest(
        snapshot: TroubleBrewingGameSnapshotV1,
        sessionState: ClocktowerSessionState,
    ): StorytellerProviderGameContextV1 {
        require(snapshot.gameId == gameId && sessionState.gameId == gameId)
        val revision = StorytellerProviderRevisionV1(
            sessionState.gameStateRevision, sessionState.playerInputRevision,
        )
        val base = StorytellerProviderGameContextBuilderV1.build(snapshot, revision, sessionState)
        return base.copy(priorDecisions = effectiveBefore(entries.size))
    }

    fun captureBeforeDecision(
        request: StorytellerProviderRequestV1,
        sessionState: ClocktowerSessionState,
    ): FrozenStorytellerDecisionPrefixV1 {
        require(request.identity.gameId == gameId)
        require(entries.none { it is Entry.Captured && it.frozen.identity.decisionId == request.identity.decisionId }) {
            "Decision capture identity already exists; do not replace its frozen prefix."
        }
        require(request.gameContext.priorDecisions == effectiveBefore(entries.size)) {
            "The new decision must see exactly the available causal history at this instant."
        }
        val frozen = StorytellerDecisionPrefixCaptureV1.capture(request, sessionState)
        entries += Entry.Captured(frozen)
        return frozen
    }

    /** The Host may commit only a confirmed legal outcome, not an unselected registration witness. */
    fun commit(decisionId: String, event: StorytellerProviderPriorDecisionV1) {
        val frozen = captureFor(decisionId)
        if (frozen.identity.decisionTypeId ==
            com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1.REGISTRATION_RESOLUTION
        ) {
            StorytellerRegistrationRulingProducerV1.validateCommitted(frozen, event)
        } else {
            require(event.registrations.isEmpty()) {
                "Only the verified registration producer can commit explicit RegistrationFact values."
            }
        }
        require(event.selectedCandidateId in frozen.legalCandidateIds)
        require(event.gameStateRevision == frozen.revision.gameStateRevision &&
            event.playerInputRevision == frozen.revision.playerInputRevision)
        require(entries.none { it is Entry.Committed && (it.decisionId == decisionId || it.value.eventId == event.eventId) }) {
            "A decision and its commit event must each be unique."
        }
        entries += Entry.Committed(decisionId, immutableDecision(event))
    }

    /**
     * A correction is a NEW event after both commits. A replacement must have been committed later
     * than the replaced event. Therefore no cycles or "future was already known" are possible.
     */
    fun correct(correctionId: String, replacedEventId: String, replacementEventId: String) {
        require(correctionId.isNotBlank() && replacedEventId != replacementEventId)
        val prior = entries.mapIndexedNotNull { index, entry ->
            (entry as? Entry.Committed)?.let { index to it.value.eventId }
        }.toMap()
        val earlier = prior.entries.firstOrNull { it.value == replacedEventId }?.key
            ?: error("Correction requires an existing replaced commit.")
        val later = prior.entries.firstOrNull { it.value == replacementEventId }?.key
            ?: error("Correction requires an existing replacement commit.")
        require(later > earlier) { "Replacement must causally follow the replaced commit." }
        require(entries.none {
            it is Entry.Corrected && (it.correctionId == correctionId ||
                it.replacedEventId == replacedEventId || it.replacementEventId == replacementEventId)
        })
        entries += Entry.Corrected(correctionId, replacedEventId, replacementEventId)
    }

    /** Current effective decisions for the next Host-owned decision boundary. */
    fun effectiveNow(): List<StorytellerProviderPriorDecisionV1> = effectiveBefore(entries.size)

    /** Full frozen record survives later commits/corrections unchanged. */
    fun frozenAt(decisionId: String): FrozenStorytellerDecisionPrefixV1 = captureFor(decisionId)

    /** Effective decisions observable STRICTLY BEFORE this capture; no later correction rewrites it. */
    fun effectiveAt(decisionId: String): List<StorytellerProviderPriorDecisionV1> {
        val cutoff = entries.indexOfFirst {
            it is Entry.Captured && it.frozen.identity.decisionId == decisionId
        }
        require(cutoff >= 0) { "Unknown frozen decision." }
        return effectiveBefore(cutoff)
    }

    private fun effectiveBefore(exclusiveOrdinal: Int): List<StorytellerProviderPriorDecisionV1> {
        val prior = entries.take(exclusiveOrdinal)
        val replaced = prior.filterIsInstance<Entry.Corrected>().mapTo(mutableSetOf()) { it.replacedEventId }
        return prior.filterIsInstance<Entry.Committed>()
            .map { it.value }
            .filterNot { it.eventId in replaced }
            .map(::immutableDecision)
    }

    private fun captureFor(decisionId: String): FrozenStorytellerDecisionPrefixV1 =
        entries.filterIsInstance<Entry.Captured>()
            .singleOrNull { it.frozen.identity.decisionId == decisionId }?.frozen
            ?: error("No exact prior decision capture exists.")
}
