package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import java.util.Collections

/**
 * Neutral, typed and read-only history context; never a rules engine, recommender or registration
 * adjudicator. The sourceRevision and exclusive global cutoff describe the SAME frozen session.
 */
internal class StorytellerProviderHistoryPrefixV1 private constructor(
    val gameId: String,
    val sourceRevision: StorytellerProviderRevisionV1,
    val historyMode: ClocktowerSemanticHistoryMode,
    val cutoffSource: StorytellerProviderHistoryCutoffSourceV1,
    val exclusiveGlobalSequence: Long?,
    entries: List<StorytellerProviderHistoryEntryV1>,
    coverage: Map<StorytellerProviderHistoryDimensionV1, StorytellerProviderHistoryCoverageV1>,
) {
    val entries: List<StorytellerProviderHistoryEntryV1> =
        Collections.unmodifiableList(entries.map { it.defensiveCopy() })
    val coverage: Map<StorytellerProviderHistoryDimensionV1, StorytellerProviderHistoryCoverageV1> =
        Collections.unmodifiableMap(LinkedHashMap(coverage))

    init {
        require(gameId.isNotBlank())
        require(this.coverage.keys == StorytellerProviderHistoryDimensionV1.entries.toSet()) {
            "Provider history must report coverage for every dimension."
        }
        when (cutoffSource) {
            StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED -> {
                require(historyMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
                require(exclusiveGlobalSequence != null && exclusiveGlobalSequence >= 0L)
            }
            StorytellerProviderHistoryCutoffSourceV1.UNAVAILABLE -> {
                require(exclusiveGlobalSequence == null && this.entries.isEmpty()) {
                    "Unavailable chronology cannot contain reconstructed global events."
                }
            }
        }
        require(this.entries.zipWithNext().all { (a, b) -> a.point.globalSequence < b.point.globalSequence }) {
            "Provider history entries must be in strict game-wide sequence order."
        }
        require(this.entries.all { exclusiveGlobalSequence != null && it.point.globalSequence < exclusiveGlobalSequence }) {
            "Provider history must not expose entries at or beyond the exclusive cutoff."
        }
        require(this.entries.map { it.kind to it.entryId }.distinct().size == this.entries.size) {
            "Provider history has duplicate typed event identities."
        }
    }

    companion object {
        fun capturedLive(
            gameId: String,
            sourceRevision: StorytellerProviderRevisionV1,
            exclusiveGlobalSequence: Long,
            entries: List<StorytellerProviderHistoryEntryV1>,
        ): StorytellerProviderHistoryPrefixV1 = StorytellerProviderHistoryPrefixV1(
            gameId, sourceRevision, ClocktowerSemanticHistoryMode.GLOBAL_V1,
            StorytellerProviderHistoryCutoffSourceV1.LIVE_CAPTURED, exclusiveGlobalSequence,
            entries, liveCoverage(),
        )

        fun unavailableLegacy(
            gameId: String,
            sourceRevision: StorytellerProviderRevisionV1,
        ): StorytellerProviderHistoryPrefixV1 = StorytellerProviderHistoryPrefixV1(
            gameId, sourceRevision, ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
            StorytellerProviderHistoryCutoffSourceV1.UNAVAILABLE, null, emptyList(),
            StorytellerProviderHistoryDimensionV1.entries.associateWith {
                StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.UNRECONSTRUCTABLE,
                    "LEGACY_LOCAL_NO_GLOBAL_ORDER",
                )
            },
        )

        /**
         * Historical recommendation input WITHOUT a durably frozen at-decision cursor is not
         * replayable. Even an identical game/player revision cannot recover global ordering.
         */
        fun unavailableHistoricalCutoff(
            gameId: String,
            sourceRevision: StorytellerProviderRevisionV1,
        ): StorytellerProviderHistoryPrefixV1 = StorytellerProviderHistoryPrefixV1(
            gameId, sourceRevision, ClocktowerSemanticHistoryMode.GLOBAL_V1,
            StorytellerProviderHistoryCutoffSourceV1.UNAVAILABLE, null, emptyList(),
            StorytellerProviderHistoryDimensionV1.entries.associateWith {
                StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.UNRECONSTRUCTABLE,
                    "HISTORICAL_CUTOFF_UNAVAILABLE",
                )
            },
        )

        private fun liveCoverage(): Map<StorytellerProviderHistoryDimensionV1, StorytellerProviderHistoryCoverageV1> =
            mapOf(
                StorytellerProviderHistoryDimensionV1.MECHANICAL to StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.PARTIAL, "ACTION_PRODUCER_SUBSET",
                ),
                StorytellerProviderHistoryDimensionV1.PRIVATE_INFORMATION to StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.PARTIAL, "OBSERVATION_PRODUCER_SUBSET",
                ),
                StorytellerProviderHistoryDimensionV1.PUBLIC_NARRATIVE to StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.PARTIAL, "PUBLIC_OBSERVATION_PRODUCER_SUBSET",
                ),
                StorytellerProviderHistoryDimensionV1.REGISTRATION_RULINGS to StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.UNKNOWN, "NO_TYPED_EXPLICIT_REGISTRATION_PRODUCER",
                ),
                StorytellerProviderHistoryDimensionV1.PRIOR_DECISIONS to StorytellerProviderHistoryCoverageV1(
                    StorytellerProviderHistoryCoverageStateV1.UNKNOWN, "NO_DURABLE_CAUSAL_DECISION_ARCHIVE",
                ),
            )
    }

    override fun equals(other: Any?): Boolean = other is StorytellerProviderHistoryPrefixV1 &&
        gameId == other.gameId && sourceRevision == other.sourceRevision &&
        historyMode == other.historyMode && cutoffSource == other.cutoffSource &&
        exclusiveGlobalSequence == other.exclusiveGlobalSequence &&
        entries == other.entries && coverage == other.coverage

    override fun hashCode(): Int = listOf(
        gameId, sourceRevision, historyMode, cutoffSource, exclusiveGlobalSequence, entries, coverage,
    ).hashCode()

    override fun toString(): String = "StorytellerProviderHistoryPrefixV1(gameId=$gameId, cutoff=$exclusiveGlobalSequence, entries=$entries, coverage=$coverage)"
}

internal enum class StorytellerProviderHistoryCutoffSourceV1 { LIVE_CAPTURED, UNAVAILABLE }
internal enum class StorytellerProviderHistoryDimensionV1 {
    MECHANICAL, PRIVATE_INFORMATION, PUBLIC_NARRATIVE, REGISTRATION_RULINGS, PRIOR_DECISIONS,
}
internal enum class StorytellerProviderHistoryCoverageStateV1 { PARTIAL, UNKNOWN, UNRECONSTRUCTABLE }
internal data class StorytellerProviderHistoryCoverageV1(
    val state: StorytellerProviderHistoryCoverageStateV1,
    val reasonCode: String,
) {
    init { require(reasonCode.isNotBlank()) }
}

internal sealed interface StorytellerProviderHistoryEntryV1 {
    val entryId: String
    val point: TimelinePoint
    val kind: String

    data class Action(
        val fact: ActionFact,
        override val point: TimelinePoint,
    ) : StorytellerProviderHistoryEntryV1 {
        override val entryId: String get() = fact.actionId
        override val kind: String get() = "action"
        init { require(fact.sequence == point.globalSequence) }
    }

    data class Observation(
        override val entryId: String,
        override val point: TimelinePoint,
        val sourceSeat: Int?,
        val sourceAbility: RoleId?,
        val visibility: ObservationVisibility,
        val recipientSeats: Set<Int>,
        val reliability: ObservationReliability,
        val proposition: InformationProposition,
    ) : StorytellerProviderHistoryEntryV1 {
        override val kind: String get() = "observation"
        init {
            require(entryId.isNotBlank())
            require(visibility != ObservationVisibility.PRIVATE || recipientSeats.isNotEmpty())
            require(visibility != ObservationVisibility.PUBLIC || recipientSeats.isEmpty())
        }
    }
}

/** Deep-copy collection-valued propositions before a provider can retain them past the request. */
private fun InformationProposition.defensiveCopy(): InformationProposition = when (this) {
    is InformationProposition.AnyOf -> InformationProposition.AnyOf(
        Collections.unmodifiableList(alternatives.map { it.defensiveCopy() }),
    )
    is InformationProposition.AllOf -> InformationProposition.AllOf(
        Collections.unmodifiableList(propositions.map { it.defensiveCopy() }),
    )
    is InformationProposition.Not -> InformationProposition.Not(proposition.defensiveCopy())
    is InformationProposition.NumericResult -> copy(
        subjectSeats = Collections.unmodifiableList(subjectSeats.toList()),
    )
    is InformationProposition.BooleanResult -> copy(
        subjectSeats = Collections.unmodifiableList(subjectSeats.toList()),
    )
    is InformationProposition.GrimoireState -> InformationProposition.GrimoireState(
        seats = seats.map { seat ->
            com.codex.campboardgamehost.clocktower.epistemic.GrimoireSeatView(
                seat.seat, seat.displayedRole, seat.alive, seat.reminderTokens,
            )
        },
        truthBinding = truthBinding,
    )
    else -> this // All remaining proposition variants contain immutable scalar values.
}

private fun StorytellerProviderHistoryEntryV1.defensiveCopy(): StorytellerProviderHistoryEntryV1 = when (this) {
    is StorytellerProviderHistoryEntryV1.Action -> copy()
    is StorytellerProviderHistoryEntryV1.Observation -> copy(
        recipientSeats = Collections.unmodifiableSet(LinkedHashSet(recipientSeats)),
        proposition = proposition.defensiveCopy(),
    )
}
