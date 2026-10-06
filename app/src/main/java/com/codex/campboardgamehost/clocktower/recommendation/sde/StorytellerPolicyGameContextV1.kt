package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.history.DecisionHistoryRepository
import com.codex.campboardgamehost.clocktower.session.ClocktowerSessionState
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision

internal data class StorytellerPlayerContextV1(
    val seat: Int,
    val experienceLevel: PlayerExperienceLevelV1,
    val claimedRoleIds: List<RoleId>,
    val pressureLevel: StorytellerDeclaredPressureLevelV1?,
) {
    init {
        require(seat > 0) { "Storyteller player-context seat must be positive." }
        require(claimedRoleIds.distinct().size == claimedRoleIds.size) {
            "Storyteller player-context claimed roles must be unique."
        }
    }
}

/**
 * Policy-neutral committed prior-decision projection.
 *
 * Legacy selector versions, scores, probabilities and candidate audits are deliberately excluded:
 * they are historical policy artifacts, not trusted context for the general provider.
 */
internal data class StorytellerPriorDecisionContextV1(
    val eventId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val selectedCandidateId: String,
    val selectedOutcome: DecisionOutcomeSnapshot,
    val abilityState: AbilityState,
    val truthRelation: TruthRelation,
    val registrations: List<RegistrationFact>,
) {
    init {
        require(eventId.isNotBlank())
        require(gameStateRevision >= 0)
        require(playerInputRevision >= 0)
        require(selectedCandidateId.isNotBlank())
    }

    companion object {
        fun fromEvent(event: StorytellerDecisionEvent): StorytellerPriorDecisionContextV1 =
            StorytellerPriorDecisionContextV1(
                eventId = event.eventId,
                gameStateRevision = event.gameStateRevision,
                playerInputRevision = event.playerInputRevision,
                selectedCandidateId = event.selectedCandidateId,
                selectedOutcome = event.selectedOutcomeSnapshot,
                abilityState = event.abilityState,
                truthRelation = event.truthRelation,
                registrations = event.registrations.toList(),
            )
    }
}

internal data class StorytellerPolicyGameContextV1(
    val players: List<StorytellerPlayerContextV1>,
    val priorDecisions: List<StorytellerPriorDecisionContextV1>,
) {
    init {
        require(players.map { it.seat }.distinct().size == players.size) {
            "Storyteller player-context seats must be unique."
        }
        require(priorDecisions.map { it.eventId }.distinct().size == priorDecisions.size) {
            "Storyteller prior-decision event IDs must be unique."
        }
    }
}

/**
 * Builds the complete current-game enrichment prefix for one provider request.
 *
 * The builder reconstructs context from Host-owned snapshot/history inputs on every invocation. It
 * never depends on provider conversation memory. Corrected/failed/proposed events are resolved by
 * DecisionHistoryRepository and any event newer than the current request revision is excluded.
 */
internal object StorytellerPolicyGameContextBuilderV1 {
    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        sourceRevision: InformationDecisionRevision,
        playerInputsBySeat: Map<Int, StorytellerPlayerContextInputV1> = emptyMap(),
        decisionHistory: DecisionHistoryArchive = DecisionHistoryArchive(),
    ): StorytellerPolicyGameContextV1 {
        val snapshotSeats = snapshot.grimoireSeats.map { it.seat }
        require(playerInputsBySeat.keys.all { it in snapshotSeats }) {
            "Storyteller player-context overrides must reference current snapshot seats."
        }

        val players = snapshotSeats.map { seat ->
            val input = playerInputsBySeat[seat] ?: StorytellerPlayerContextInputV1.DEFAULT
            StorytellerPlayerContextV1(
                seat = seat,
                experienceLevel = input.experienceLevel,
                claimedRoleIds = input.claimedRoleIds.toList(),
                pressureLevel = input.pressureLevel,
            )
        }

        val priorDecisions = DecisionHistoryRepository(decisionHistory)
            .project()
            .effectiveEvents
            .asSequence()
            .filter { event ->
                event.gameStateRevision <= sourceRevision.gameStateRevision &&
                    event.playerInputRevision <= sourceRevision.playerInputRevision
            }
            .map(StorytellerPriorDecisionContextV1::fromEvent)
            .toList()

        return StorytellerPolicyGameContextV1(
            players = players,
            priorDecisions = priorDecisions,
        )
    }

    fun build(
        snapshot: TroubleBrewingGameSnapshotV1,
        sourceRevision: InformationDecisionRevision,
        sessionState: ClocktowerSessionState,
    ): StorytellerPolicyGameContextV1 {
        require(sessionState.gameId == snapshot.gameId) {
            "Storyteller provider context must use the current session game."
        }
        require(sessionState.gameStateRevision == sourceRevision.gameStateRevision) {
            "Storyteller provider context game-state revision must match the request source revision."
        }
        require(sessionState.playerInputRevision == sourceRevision.playerInputRevision) {
            "Storyteller provider context player-input revision must match the request source revision."
        }
        return build(
            snapshot = snapshot,
            sourceRevision = sourceRevision,
            playerInputsBySeat = sessionState.storytellerPlayerContextBySeat,
            decisionHistory = sessionState.decisionHistory,
        )
    }
}

/**
 * Full stateless-provider invocation envelope. Required decision input remains in GSP-2A; this
 * GSP-2B envelope adds reconstructable current-game enrichment.
 */
internal data class StorytellerPolicyInvocationV1(
    val request: StorytellerPolicyRequestV1,
    val gameContext: StorytellerPolicyGameContextV1,
) {
    init {
        val snapshotSeats = request.input.snapshot.grimoireSeats.map { it.seat }
        require(gameContext.players.map { it.seat } == snapshotSeats) {
            "Provider invocation must carry exactly one player context for every snapshot seat in canonical order."
        }
        require(gameContext.priorDecisions.all { decision ->
            decision.gameStateRevision <= request.input.sourceRevision.gameStateRevision &&
                decision.playerInputRevision <= request.input.sourceRevision.playerInputRevision
        }) {
            "Provider invocation cannot contain future decision history."
        }
    }
}

internal enum class StorytellerDecisionEpisodeSelectionSourceV1 {
    STORYTELLER_MANUAL,
    PROVIDER_SUGGESTION,
    RULE_DETERMINISTIC,
}

internal data class StorytellerDecisionEpisodeSelectionV1(
    val candidateId: String,
    val source: StorytellerDecisionEpisodeSelectionSourceV1,
) {
    init {
        require(candidateId.isNotBlank())
    }
}

/**
 * Immutable replay/analysis record for one recommendation episode.
 *
 * This is not canonical game state. The committed selection is a reference to Host-owned candidate
 * identity and can be absent while the decision is still pending.
 */
internal data class StorytellerDecisionEpisodeV1(
    val invocation: StorytellerPolicyInvocationV1,
    val providerResponse: StorytellerPolicyResponseV1? = null,
    val committedSelection: StorytellerDecisionEpisodeSelectionV1? = null,
) {
    init {
        providerResponse?.let { response ->
            require(
                StorytellerPolicyResponseValidatorV1.validate(invocation.request, response) !is
                    StorytellerPolicyValidationV1.Rejected,
            ) {
                "Decision episode cannot retain an invalid provider response."
            }
        }

        committedSelection?.let { selection ->
            require(selection.candidateId in invocation.request.input.legalCandidateIds) {
                "Decision episode committed selection must belong to the request legal domain."
            }
            if (selection.source == StorytellerDecisionEpisodeSelectionSourceV1.PROVIDER_SUGGESTION) {
                val recommendation =
                    (providerResponse?.outcome as? StorytellerPolicyOutcomeV1.Recommendation)
                        ?: throw IllegalArgumentException(
                            "Provider-suggestion selection requires a validated recommendation response.",
                        )
                val suggestedIds = buildSet {
                    add(recommendation.primary.candidateId)
                    recommendation.alternatives.forEach { add(it.candidateId) }
                }
                require(selection.candidateId in suggestedIds) {
                    "Provider-suggestion selection must reference the primary or an explicit alternative."
                }
            }
        }
    }
}
