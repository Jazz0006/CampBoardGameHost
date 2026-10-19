package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionIdentityV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderGameContextV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingSnapshotSeat
import java.util.Collections

/**
 * Identity and status required to validate a confirmed ruling. No setup or player-profile copy.
 */
internal data class FrozenStorytellerSnapshotIdentityV1(
    val gameId: String,
    val phase: SnapshotField<StorytellerPhase>,
    val seats: List<TroubleBrewingSnapshotSeat>,
)

/**
 * Commit validation context, not a historical request archive. The single observation is the
 * actual just-published information; full history stays in the Session.
 */
internal class FrozenStorytellerDecisionPrefixV1 internal constructor(
    val identity: StorytellerProviderDecisionIdentityV1,
    val revision: StorytellerProviderRevisionV1,
    val snapshotIdentity: FrozenStorytellerSnapshotIdentityV1,
    val exclusiveGlobalSequence: Long,
    val publishedObservation: StorytellerProviderHistoryEntryV1.Observation?,
    legalCandidateIds: List<String>,
) {
    val legalCandidateIds: List<String> = Collections.unmodifiableList(legalCandidateIds.toList())

    init {
        require(identity.gameId == snapshotIdentity.gameId)
        require(exclusiveGlobalSequence >= 0)
        require(publishedObservation == null ||
            publishedObservation.point.globalSequence == exclusiveGlobalSequence - 1)
        require(legalCandidateIds.isNotEmpty() && legalCandidateIds.distinct().size == legalCandidateIds.size)
    }

    override fun equals(other: Any?): Boolean = other is FrozenStorytellerDecisionPrefixV1 &&
        identity == other.identity && revision == other.revision &&
        snapshotIdentity == other.snapshotIdentity && exclusiveGlobalSequence == other.exclusiveGlobalSequence &&
        publishedObservation == other.publishedObservation && legalCandidateIds == other.legalCandidateIds

    override fun hashCode(): Int = listOf(
        identity, revision, snapshotIdentity, exclusiveGlobalSequence, publishedObservation, legalCandidateIds,
    ).hashCode()
}

/**
 * R1C2A: fail-closed capture seam for an already-materialized neutral Provider request.
 *
 * No network/recommender, no write to Game Engine, and no reconstruction from legacy/local facts.
 * Must be called with the frozen SessionState that actually produced the request.
 */
internal object StorytellerDecisionPrefixCaptureV1 {
    /**
     * Runtime GameState RoleId uses names such as "Fortune Teller" while the verified TB
     * projection has canonical external IDs such as "fortuneteller". They refer to the same
     * identity, not two different characters. Never permit a genuinely different role.
     */
    private fun sameCharacterId(snapshotId: String, sessionId: String): Boolean =
        snapshotId == sessionId ||
            snapshotId.filter { it.isLetterOrDigit() }.lowercase() ==
                sessionId.filter { it.isLetterOrDigit() }.lowercase()

    fun capture(
        request: StorytellerProviderRequestV1,
        sessionState: ClocktowerSessionState,
    ): FrozenStorytellerDecisionPrefixV1 {
        require(sessionState.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
            "Decision validation needs a verifiable global history."
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
                require(sameCharacterId(it.value, player.actualRole.value)) {
                    "Snapshot actual role diverges from frozen Session."
                }
            }
            (seat.shownRoleId as? SnapshotField.Known<String>)?.let {
                require(sameCharacterId(it.value, (player.shownRole ?: player.actualRole).value)) {
                    "Snapshot shown role diverges from frozen Session."
                }
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
        val validationSeats = when (val context = request.decisionContext) {
            is StorytellerProviderDecisionContextV1.RegistrationResolution -> setOf(context.subjectSeat)
            is StorytellerProviderDecisionContextV1.DayAbilityRegistration -> setOf(context.actorSeat, context.subjectSeat)
            else -> emptySet()
        }
        return FrozenStorytellerDecisionPrefixV1(
            identity = request.identity,
            revision = request.sourceRevision,
            snapshotIdentity = FrozenStorytellerSnapshotIdentityV1(
                gameId = snapshot.gameId,
                phase = snapshot.position.phase,
                seats = Collections.unmodifiableList(snapshot.grimoireSeats.filter { it.seat in validationSeats }),
            ),
            exclusiveGlobalSequence = requireNotNull(actual.exclusiveGlobalSequence),
            publishedObservation = if (request.decisionContext is StorytellerProviderDecisionContextV1.RegistrationResolution)
                actual.entries.lastOrNull() as? StorytellerProviderHistoryEntryV1.Observation else null,
            legalCandidateIds = request.legalCandidateIds,
        )
    }
}

/**
 * Exact ordered journal entries used by atomic commit validation. Unlike a final
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
 * Session-owned, append-only decision/correction chronology, distinct from mechanical facts.
 * Two rulings may share one game/input revision and cursor; corrections retain causal order.
 * Production first-publication rulings are staged and accepted with the observation,
 * while later explicit corrections append new journal events. The journal is validated during
 * atomic commits and is not an independent game-state authority.
 */
internal class StorytellerCausalDecisionJournalV1(private val gameId: String) {
    private val entries = mutableListOf<StorytellerCausalJournalRecordV1>()

    init { require(gameId.isNotBlank()) }

    fun archive(): StorytellerCausalJournalArchiveV1 = StorytellerCausalJournalArchiveV1(
        gameId, Collections.unmodifiableList(entries.toList()),
    )

    companion object {
        /**
         * Stage an isolated journal for an atomic Session transaction. Validate each retained
         * observation against canonical facts; this does not reconstruct historical requests.
         */
        fun restore(
            archive: StorytellerCausalJournalArchiveV1,
            currentSession: ClocktowerSessionState,
        ): StorytellerCausalDecisionJournalV1 {
            require(archive.gameId == currentSession.gameId)
            require(currentSession.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
            val journal = StorytellerCausalDecisionJournalV1(archive.gameId)
            val history by lazy {
                StorytellerProviderHistoryPrefixMaterializerV1.captureLive(currentSession,
                    StorytellerProviderRevisionV1(currentSession.gameStateRevision, currentSession.playerInputRevision))
            }
            archive.records.forEach { record ->
                when (record) {
                    is StorytellerCausalJournalRecordV1.Captured -> {
                        val frozen = record.frozen
                        require(frozen.identity.gameId == archive.gameId)
                        require(frozen.exclusiveGlobalSequence <= currentSession.nextTimelineGlobalSequence)
                        require(journal.entries.none {
                            it is StorytellerCausalJournalRecordV1.Captured && it.frozen.identity.decisionId == frozen.identity.decisionId
                        })
                        require(frozen.revision.gameStateRevision <= currentSession.gameStateRevision)
                        require(frozen.revision.playerInputRevision <= currentSession.playerInputRevision)
                        frozen.publishedObservation?.let { observation ->
                            require(history.entries.singleOrNull { it.entryId == observation.entryId } == observation) {
                                "Registration observation differs from canonical Session facts."
                            }
                        }
                        journal.entries += StorytellerCausalJournalRecordV1.Captured(frozen)
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
        return base.copy(priorDecisions = effectiveNow())
    }

    fun captureBeforeDecision(
        request: StorytellerProviderRequestV1,
        sessionState: ClocktowerSessionState,
    ): FrozenStorytellerDecisionPrefixV1 {
        require(request.identity.gameId == gameId)
        require(entries.none { it is StorytellerCausalJournalRecordV1.Captured && it.frozen.identity.decisionId == request.identity.decisionId }) {
            "Decision capture identity already exists; do not replace its frozen prefix."
        }
        require(request.gameContext.priorDecisions == effectiveNow()) {
            "The new decision must see exactly the available causal history at this instant."
        }
        val frozen = StorytellerDecisionPrefixCaptureV1.capture(request, sessionState)
        entries += StorytellerCausalJournalRecordV1.Captured(frozen)
        return frozen
    }

    /** Preserve decision identity and event integrity without inferring registration witnesses. */
    fun commit(decisionId: String, event: StorytellerProviderPriorDecisionV1) {
        val frozen = captureFor(decisionId)
        if (frozen.identity.decisionTypeId ==
            com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION
        ) {
            DayAbilityRegistrationRulingProducerV1.validateCommitted(frozen, event)
        } else {
            require(event.registrations.isEmpty()) {
                "Only the verified registration producer can commit explicit RegistrationFact values."
            }
        }
        require(event.selectedCandidateId in frozen.legalCandidateIds)
        require(event.gameStateRevision == frozen.revision.gameStateRevision &&
            event.playerInputRevision == frozen.revision.playerInputRevision)
        require(entries.none { it is StorytellerCausalJournalRecordV1.Committed && (it.decisionId == decisionId || it.value.eventId == event.eventId) }) {
            "A decision and its commit event must each be unique."
        }
        entries += StorytellerCausalJournalRecordV1.Committed(decisionId, immutableDecision(event))
    }

    /**
     * A correction is a NEW event after both commits. A replacement must have been committed later
     * than the replaced event. Therefore no cycles or "future was already known" are possible.
     */
    fun correct(correctionId: String, replacedEventId: String, replacementEventId: String) {
        require(correctionId.isNotBlank() && replacedEventId != replacementEventId)
        val prior = entries.mapIndexedNotNull { index, entry ->
            (entry as? StorytellerCausalJournalRecordV1.Committed)?.let { index to it.value.eventId }
        }.toMap()
        val earlier = prior.entries.firstOrNull { it.value == replacedEventId }?.key
            ?: error("Correction requires an existing replaced commit.")
        val later = prior.entries.firstOrNull { it.value == replacementEventId }?.key
            ?: error("Correction requires an existing replacement commit.")
        require(later > earlier) { "Replacement must causally follow the replaced commit." }
        require(entries.none {
            it is StorytellerCausalJournalRecordV1.Corrected && (it.correctionId == correctionId ||
                it.replacedEventId == replacedEventId || it.replacementEventId == replacementEventId)
        })
        entries += StorytellerCausalJournalRecordV1.Corrected(correctionId, replacedEventId, replacementEventId)
    }

    /** Current effective decisions for the next Host-owned decision boundary. */
    fun effectiveNow(): List<StorytellerProviderPriorDecisionV1> {
        val prior = entries
        val replaced = prior.filterIsInstance<StorytellerCausalJournalRecordV1.Corrected>().mapTo(mutableSetOf()) { it.replacedEventId }
        return prior.filterIsInstance<StorytellerCausalJournalRecordV1.Committed>()
            .map { it.value }
            .filterNot { it.eventId in replaced }
            .map(::immutableDecision)
    }

    private fun captureFor(decisionId: String): FrozenStorytellerDecisionPrefixV1 =
        entries.filterIsInstance<StorytellerCausalJournalRecordV1.Captured>()
            .singleOrNull { it.frozen.identity.decisionId == decisionId }?.frozen
            ?: error("No exact prior decision capture exists.")
}
