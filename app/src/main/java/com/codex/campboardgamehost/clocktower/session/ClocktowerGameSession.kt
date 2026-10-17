package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.ClocktowerConfirmedRegistrationPublicationV1
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.SnapshotField
import com.codex.campboardgamehost.clocktower.domain.TroubleBrewingGameSnapshotV1
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.DecisionOutcomeSnapshot
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderPriorDecisionV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.TruthRelation
import com.codex.campboardgamehost.clocktower.rules.MayorRedirectLegalDomain
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.KlutzDeathTriggerEvidenceV1
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.epistemic.bindGlobal
import com.codex.campboardgamehost.clocktower.epistemic.matches

/** Transient result of one session-owned Global observation transition; never persisted as a second state model. */
internal data class GlobalEpistemicObservationCommit(
    val record: RecordedEpistemicObservation,
    val observationLog: EpistemicObservationLog,
    val nextTimelineGlobalSequence: Long,
    val playerInputRevision: Long,
)

/** Transient result of one session-owned Global action transition; never persisted as a second state model. */
internal data class GlobalActionFactCommit(
    val entry: TimelineBoundActionFact,
    val actionTimeline: ActionFactTimeline,
    val nextTimelineGlobalSequence: Long,
)

/**
 * Immutable Compose-facing read model for session identity, revision, and semantic chronology.
 *
 * [GameState] is canonical inside [ClocktowerGameSession], but remains intentionally absent here:
 * production UI still owns [PlayerCard]-style presentation metadata and uses derived GameState
 * projections for pre-session, recommendation, and screen-local reads. Excluding GameState keeps this
 * view narrow and prevents a second broad mechanical read API while preserving the single session writer.
 */
internal data class ClocktowerSessionView(
    val gameId: String,
    val scriptId: ScriptId,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val gameSeed: Long,
    val actionTimeline: ActionFactTimeline,
    val epistemicObservationLog: EpistemicObservationLog,
    val semanticHistoryMode: ClocktowerSemanticHistoryMode,
    val nextTimelineGlobalSequence: Long,
    val phase: StorytellerPhase,
    val round: Int,
    val redHerringSeat: Int?,
    val demonBluffRoleIds: List<RoleId>,
)

/**
 * Ruleset-independent canonical state owned by one production Clocktower session.
 *
 * Scripts without an advanced ruleset still need one identity/revision/history owner.
 * [GameSnapshot] remains the stricter projection for consumers that have a resolved [RulesetRef].
 */
internal data class ClocktowerSessionState(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val gameSeed: Long,
    val gameState: GameState,
    val actionTimeline: ActionFactTimeline = ActionFactTimeline(),
    val epistemicObservationLog: EpistemicObservationLog = EpistemicObservationLog(),
    val semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
    val nextTimelineGlobalSequence: Long = 0L,
    val storytellerPlayerContextBySeat: Map<Int, StorytellerPlayerContextInputV1> = emptyMap(),
    /** Accepted setup decisions are stored once here, not in Compose draft state. */
    val redHerringSeat: Int? = null,
    val demonBluffRoleIds: List<RoleId> = emptyList(),
) {
    private val lastPhaseAdvance: ActionFact.PhaseAdvance?
        get() = actionTimeline.entries.asReversed().firstNotNullOfOrNull { it.fact as? ActionFact.PhaseAdvance }
    val phase: StorytellerPhase get() = lastPhaseAdvance?.phase ?: StorytellerPhase.FIRST_NIGHT
    val round: Int get() = lastPhaseAdvance?.round ?: 1

    init {
        require(gameId.isNotBlank()) { "gameId cannot be blank." }
        require(gameStateRevision >= 0L) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0L) { "playerInputRevision cannot be negative." }
        require(nextTimelineGlobalSequence >= 0L) { "nextTimelineGlobalSequence cannot be negative." }
        require(gameSeed == gameState.seed) {
            "The session gameSeed must match the GameState seed."
        }
        val currentSeats = gameState.players.map { it.seat }.toSet()
        require(storytellerPlayerContextBySeat.keys.all { it in currentSeats }) {
            "Storyteller player-context overrides must reference current GameState seats."
        }
        redHerringSeat?.let { seat ->
            require(gameState.playerAt(seat)?.actualAlignment == Alignment.GOOD) {
                "Committed Red Herring must be an actual Good Session seat."
            }
        }
        require(demonBluffRoleIds.distinct() == demonBluffRoleIds) {
            "Committed Demon bluff roles must be distinct."
        }
        require(demonBluffRoleIds.none { role -> gameState.players.any { it.actualRole == role } }) {
            "Committed Demon bluffs must not be actual in-play roles."
        }
        semanticHistoryMode.requireCompatible(
            actionTimeline = actionTimeline,
            observationLog = epistemicObservationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )
    }
}

internal class ClocktowerGameSession private constructor(
    initialState: ClocktowerSessionState,
    private val defaultRulesetRef: RulesetRef?,
) {
    var state: ClocktowerSessionState = initialState
        private set

    /** Read live at each confirmation, including multiple commits before UI recomposition. */
    val nextInputSequence: Int
        get() {
            check(state.nextTimelineGlobalSequence < Int.MAX_VALUE) { "Input sequence exhausted." }
            return state.nextTimelineGlobalSequence.toInt() + 1
        }

    /**
     * One causal decision journal belongs to this game session, not to the root Compose
     * screen. Atomic commits validate this journal against the Session history.
     * Decision producers still use its existing validated append/correct interface.
     */
    var causalDecisionJournal: StorytellerCausalDecisionJournalV1 =
        StorytellerCausalDecisionJournalV1(initialState.gameId)
        private set

    /** Read-only cutover projection suitable for publishing through an observable UI boundary. */
    val view: ClocktowerSessionView
        get() = ClocktowerSessionView(
            gameId = state.gameId,
            scriptId = state.gameState.script,
            gameStateRevision = state.gameStateRevision,
            playerInputRevision = state.playerInputRevision,
            gameSeed = state.gameSeed,
            actionTimeline = state.actionTimeline,
            epistemicObservationLog = state.epistemicObservationLog,
            semanticHistoryMode = state.semanticHistoryMode,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            phase = state.phase,
            round = state.round,
            redHerringSeat = state.redHerringSeat,
            demonBluffRoleIds = state.demonBluffRoleIds,
        )

    /**
     * Backward-compatible strict snapshot view for ruleset-backed sessions.
     * Production sessions without an advanced ruleset must use [state] until a real ref is available.
     */
    val snapshot: GameSnapshot
        get() {
            val rulesetRef = requireNotNull(defaultRulesetRef) {
                "This production session has no default advanced RulesetRef; use state or toGameSnapshot(ref)."
            }
            return toGameSnapshot(rulesetRef)
        }

    fun toGameSnapshot(rulesetRef: RulesetRef): GameSnapshot = GameSnapshot(
        gameId = state.gameId,
        gameStateRevision = state.gameStateRevision,
        playerInputRevision = state.playerInputRevision,
        gameSeed = state.gameSeed,
        rulesetRef = rulesetRef,
        gameState = state.gameState,
        actionTimeline = state.actionTimeline,
        epistemicObservationLog = state.epistemicObservationLog,
        semanticHistoryMode = state.semanticHistoryMode,
        nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
    )

    fun updateGameState(nextState: GameState): ClocktowerSessionState {
        require(nextState.seed == state.gameSeed) {
            "A game session cannot replace its persisted gameSeed."
        }
        require(nextState.script == state.gameState.script) {
            "A game session cannot replace its script."
        }
        if (nextState == state.gameState) return state
        return updateState(
            state.copy(
                gameStateRevision = state.gameStateRevision + 1,
                gameState = nextState,
            ),
        )
    }

    /** Production event/decision boundary whose accepted revision cadence is independent of state equality. */
    fun advanceGameStateRevision(): ClocktowerSessionState = updateState(
        state.copy(gameStateRevision = state.gameStateRevision + 1),
    )

    fun recordPlayerInput(): ClocktowerSessionState = updateState(
        state.copy(playerInputRevision = state.playerInputRevision + 1),
    )

    /**
     * Commit a confirmed Host setup choice and the globally sequenced fact in the SAME
     * Session state transition. The UI only projects the accepted result.
     */
    fun commitRedHerringSeat(
        seat: Int?,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
    ): ClocktowerSessionState {
        require(phase == StorytellerPhase.FIRST_NIGHT && round == 1) {
            "Red Herring may only be set during the first-night setup."
        }
        if (seat != null) {
            val player = requireNotNull(state.gameState.playerAt(seat)) {
                "Unknown Red Herring seat $seat."
            }
            require(player.actualAlignment == Alignment.GOOD) {
                "Red Herring must be an actually good player."
            }
        }
        if (state.redHerringSeat == seat) return state
        check(state.playerInputRevision != Long.MAX_VALUE)
        val committed = commitGlobalActionFact(
            semanticHistoryMode = state.semanticHistoryMode,
            actionTimeline = state.actionTimeline,
            observationLog = state.epistemicObservationLog,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            draft = ActionFactDraft.RedHerringChoice(
                actionId = "red-herring-${state.gameId}-${state.nextTimelineGlobalSequence}",
                phase = phase, round = round, sequence = sequence, selectedSeat = seat,
            ),
        )
        return updateState(state.copy(
            redHerringSeat = seat,
            playerInputRevision = state.playerInputRevision + 1,
            actionTimeline = committed.actionTimeline,
            nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
        ))
    }

    fun commitDemonBluffs(
        roles: List<RoleId>,
        phase: StorytellerPhase,
        round: Int,
        sequence: Int,
    ): ClocktowerSessionState {
        require(phase == StorytellerPhase.FIRST_NIGHT && round == 1) {
            "Demon bluffs may only be set during the first-night setup."
        }
        require(roles.distinct().size == roles.size) { "Demon bluff roles must be distinct." }
        require(roles.none { role -> state.gameState.players.any {
            it.actualRole == role
        } }) { "Demon bluffs cannot contain an actual in-play role." }
        if (state.demonBluffRoleIds == roles) return state
        check(state.playerInputRevision != Long.MAX_VALUE)
        val committed = commitGlobalActionFact(
            semanticHistoryMode = state.semanticHistoryMode,
            actionTimeline = state.actionTimeline,
            observationLog = state.epistemicObservationLog,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            draft = ActionFactDraft.DemonBluffChoice(
                actionId = "demon-bluffs-${state.gameId}-${state.nextTimelineGlobalSequence}",
                phase = phase, round = round, sequence = sequence, roles = roles.toList(),
            ),
        )
        return updateState(state.copy(
            demonBluffRoleIds = roles.toList(),
            playerInputRevision = state.playerInputRevision + 1,
            actionTimeline = committed.actionTimeline,
            nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
        ))
    }

    fun storytellerPlayerContext(seat: Int): StorytellerPlayerContextInputV1 {
        require(state.gameState.playerAt(seat) != null) {
            "Storyteller player context must reference a current GameState seat."
        }
        return state.storytellerPlayerContextBySeat[seat] ?: StorytellerPlayerContextInputV1.DEFAULT
    }

    fun updateStorytellerPlayerContext(
        seat: Int,
        input: StorytellerPlayerContextInputV1,
    ): ClocktowerSessionState {
        require(state.gameState.playerAt(seat) != null) {
            "Storyteller player context must reference a current GameState seat."
        }
        val current = storytellerPlayerContext(seat)
        if (current == input) return state
        check(state.playerInputRevision != Long.MAX_VALUE) {
            "Player input revision exhausted."
        }
        val nextOverrides = if (input.isDefault) {
            state.storytellerPlayerContextBySeat - seat
        } else {
            state.storytellerPlayerContextBySeat + (seat to input)
        }
        return updateState(
            state.copy(
                playerInputRevision = state.playerInputRevision + 1,
                storytellerPlayerContextBySeat = nextOverrides,
            ),
        )
    }

    /** Shared Host preflight: capture pre-death evidence before any Session mechanics change. */
    private fun prepareGlobalActionDraft(draft: ActionFactDraft): ActionFactDraft {
        val deathSeat = when (draft) {
            is ActionFactDraft.Death -> draft.targetSeat
            is ActionFactDraft.Execution -> draft.targetSeat
            else -> null
        }
        // External callers cannot assert death-time ability state. Read the one canonical
        // game state BEFORE death clears poison, then atomically bind with the death action.
        val evidence = deathSeat?.let { seat ->
            state.gameState.playerAt(seat)?.takeIf { it.actualRole.value == "Klutz" && it.alive }
                ?.let { player ->
                    KlutzDeathTriggerEvidenceV1(
                        actualRole = player.actualRole,
                        wasAlive = player.alive,
                        wasPoisoned = player.poisoned,
                        sourceGameStateRevision = state.gameStateRevision,
                    )
                }
        }
        val priorProof = state.actionTimeline.entries
            .firstOrNull { it.fact.actionId == draft.actionId }
            ?.fact.let { prior ->
                when (prior) {
                    is com.codex.campboardgamehost.clocktower.domain.ActionFact.Death ->
                        prior.klutzDeathTrigger
                    is com.codex.campboardgamehost.clocktower.domain.ActionFact.Execution ->
                        prior.klutzDeathTrigger
                    else -> null
                }
            }
        val prepared = when (draft) {
            is ActionFactDraft.Death -> {
                require(draft.klutzDeathTrigger == null) { "Klutz death proof must be generated by the session." }
                draft.copy(klutzDeathTrigger = evidence ?: priorProof)
            }
            is ActionFactDraft.Execution -> {
                require(draft.klutzDeathTrigger == null) { "Klutz death proof must be generated by the session." }
                draft.copy(klutzDeathTrigger = evidence ?: priorProof)
            }
            else -> draft
        }
        return prepared
    }

    /** A completed Butler step cannot be recorded twice by revisiting the night screen. */
    fun confirmButlerMaster(
        phase: StorytellerPhase,
        round: Int,
        butlerSeat: Int,
        masterSeat: Int,
    ): TimelineBoundActionFact {
        state.actionTimeline.entries.firstOrNull { entry ->
            entry.point.phase == phase && entry.point.round == round &&
                (entry.fact as? ActionFact.ButlerMasterChoice)?.butlerSeat == butlerSeat
        }?.let { return it }
        require(phase == StorytellerPhase.FIRST_NIGHT || phase == StorytellerPhase.NIGHT)
        require(butlerSeat != masterSeat) { "Butler cannot choose themself." }
        val actor = requireNotNull(state.gameState.playerAt(butlerSeat))
        requireNotNull(state.gameState.playerAt(masterSeat))
        require(actor.alive &&
            (actor.actualRole.value == "Butler" || actor.shownRole?.value == "Butler")) {
            "Confirmed Butler master requires an active Butler (or Drunk shown Butler)."
        }
        return commitConfirmedGameAction(
            draft = ActionFactDraft.ButlerMasterChoice(
                actionId = "butler-master-${state.gameId}-${phase.name}-$round-$butlerSeat",
                phase = phase, round = round, sequence = nextInputSequence,
                butlerSeat = butlerSeat, masterSeat = masterSeat,
            ),
            nextGameState = state.gameState,
            expectedGameStateRevision = state.gameStateRevision,
        )
    }

    /**
     * One confirmed Host mechanical step: preflight the action and state, then publish exactly
     * one Session transition carrying both the mechanical projection and global action fact.
     * An already accepted action ID is an idempotent no-op, even after the revision advances.
     * The Host caller remains responsible for the role/phase-specific legal decision.
     */
    fun commitConfirmedGameAction(
        draft: ActionFactDraft,
        nextGameState: GameState,
        expectedGameStateRevision: Long,
        publishedObservation: EpistemicObservationDraft? = null,
    ): TimelineBoundActionFact {
        require(nextGameState.seed == state.gameSeed && nextGameState.script == state.gameState.script) {
            "A confirmed step cannot change the session seed or script."
        }
        val prepared = prepareGlobalActionDraft(draft)
        val committed = commitGlobalActionFact(
            semanticHistoryMode = state.semanticHistoryMode,
            actionTimeline = state.actionTimeline,
            observationLog = state.epistemicObservationLog,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            draft = prepared,
        )
        val observationCommit = publishedObservation?.let { observation ->
            commitGlobalEpistemicObservation(
                semanticHistoryMode = state.semanticHistoryMode,
                observationLog = state.epistemicObservationLog,
                nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
                playerInputRevision = state.playerInputRevision,
                draft = observation,
                actionTimeline = committed.actionTimeline,
            )
        }
        if (committed.actionTimeline === state.actionTimeline) {
            // Replaying an accepted action must not silently append a missing publication.
            check(observationCommit == null ||
                observationCommit.observationLog === state.epistemicObservationLog) {
                "Previously committed action has no matching published observation."
            }
            return committed.entry
        }
        check(state.gameStateRevision == expectedGameStateRevision) {
            "Stale confirmed step: expected game-state revision $expectedGameStateRevision, " +
                "actual ${state.gameStateRevision}."
        }
        check(state.gameStateRevision != Long.MAX_VALUE) { "Game-state revision exhausted." }
        updateState(state.copy(
            gameStateRevision = state.gameStateRevision + 1,
            gameState = nextGameState,
            actionTimeline = committed.actionTimeline,
            epistemicObservationLog = observationCommit?.observationLog ?: state.epistemicObservationLog,
            playerInputRevision = observationCommit?.playerInputRevision ?: state.playerInputRevision,
            nextTimelineGlobalSequence = observationCommit?.nextTimelineGlobalSequence
                ?: committed.nextTimelineGlobalSequence,
        ))
        return committed.entry
    }

    /**
     * Confirm a real phase/round progression in the same Session state transition as its
     * globally ordered PhaseAdvance fact. The mechanical player roster is unchanged; the
     * accepted progress is carried by the typed history until UI progress projection converges.
     */
    fun commitConfirmedPhaseAdvance(
        draft: ActionFactDraft.PhaseAdvance,
        expectedGameStateRevision: Long,
    ): TimelineBoundActionFact {
        val legal = when (draft.phase) {
            StorytellerPhase.FIRST_NIGHT ->
                draft.nextPhase == StorytellerPhase.DAY && draft.nextRound == draft.round
            StorytellerPhase.NIGHT ->
                draft.nextPhase in setOf(StorytellerPhase.DAWN, StorytellerPhase.DAY) &&
                    draft.nextRound == draft.round
            StorytellerPhase.DAWN ->
                draft.nextPhase == StorytellerPhase.DAY && draft.nextRound == draft.round
            StorytellerPhase.DAY ->
                (draft.nextPhase == StorytellerPhase.NIGHT && draft.nextRound == draft.round + 1) ||
                    (draft.nextPhase == StorytellerPhase.DAWN && draft.nextRound == draft.round)
        }
        require(legal) {
            "Illegal confirmed Clocktower phase progression: ${draft.phase}/${draft.round} " +
                "to ${draft.nextPhase}/${draft.nextRound}."
        }
        val previouslyAcceptedPhase = state.actionTimeline.entries.asReversed()
            .firstNotNullOfOrNull { it.fact as? ActionFact.PhaseAdvance }
        val alreadyAccepted = state.actionTimeline.entries.any { it.fact.actionId == draft.actionId }
        check(alreadyAccepted || previouslyAcceptedPhase == null ||
            (previouslyAcceptedPhase.phase == draft.phase && previouslyAcceptedPhase.round == draft.round)) {
            "Phase transition must start from the last accepted PhaseAdvance in Session history."
        }
        return commitConfirmedGameAction(
            draft = draft,
            nextGameState = state.gameState,
            expectedGameStateRevision = expectedGameStateRevision,
        )
    }

    /**
     * Mayor redirect acceptance: stage the full causal capture/decision/correction off-session,
     * validate the rules-owned target, then commit its typed action, revision and timeline cursor.
     * There is no live journal write before the Session action succeeds.
     */
    fun commitConfirmedMayorRedirect(
        draft: ActionFactDraft.MayorRedirectChoice,
        request: StorytellerProviderRequestV1,
        candidateId: String,
        expectedGameStateRevision: Long,
    ): TimelineBoundActionFact {
        require(draft.phase == StorytellerPhase.NIGHT)
        state.actionTimeline.entries.firstOrNull { it.fact.actionId == draft.actionId }?.let { accepted ->
            require(draft.matches(accepted)) { "Mayor action ID already committed with different content." }
            return accepted
        }
        check(state.gameStateRevision == expectedGameStateRevision) {
            "Stale Mayor confirmation."
        }
        require(request.identity.gameId == state.gameId)
        require(request.sourceRevision == StorytellerProviderRevisionV1(
            state.gameStateRevision, state.playerInputRevision,
        ))
        require(candidateId == MayorRedirectDecisionBoundary.candidateIdForSeat(draft.targetSeat))
        val mayor = requireNotNull(state.gameState.playerAt(draft.mayorSeat))
        require(mayor.actualRole == RoleId("Mayor") && mayor.alive)
        require(draft.targetSeat in MayorRedirectLegalDomain.resolve(
            state.gameState, draft.mayorSeat,
        ).legalTargetSeats) { "Mayor redirect target is not legal in the current Session." }

        val stagedJournal = StorytellerCausalDecisionJournalV1.restore(
            causalDecisionJournal.archive(), state,
        )
        val previous = stagedJournal.effectiveNow().lastOrNull {
            it.eventId.startsWith("confirmed:night:${draft.round}:mayor-redirect:")
        }
        val frozen = stagedJournal.captureBeforeDecision(request, state)
        val eventId = "confirmed:${request.identity.decisionId}"
        stagedJournal.commit(request.identity.decisionId, StorytellerProviderPriorDecisionV1(
            eventId = eventId,
            gameStateRevision = frozen.revision.gameStateRevision,
            playerInputRevision = frozen.revision.playerInputRevision,
            selectedCandidateId = candidateId,
            selectedOutcome = DecisionOutcomeSnapshot(
                decisionType = "mayor-redirect",
                canonicalFields = sortedMapOf("targetSeat" to draft.targetSeat.toString()),
            ),
            abilityState = AbilityState.FUNCTIONING,
            truthRelation = TruthRelation.NOT_APPLICABLE,
            registrations = emptyList(),
        ))
        previous?.let {
            stagedJournal.correct(
                correctionId = "revised:$eventId",
                replacedEventId = it.eventId,
                replacementEventId = eventId,
            )
        }
        val committed = commitConfirmedGameAction(
            draft = draft,
            nextGameState = state.gameState,
            expectedGameStateRevision = expectedGameStateRevision,
        )
        causalDecisionJournal = stagedJournal
        return committed
    }

    /** Non-mutating validation/projection for production flows that must persist before commit. */
    fun preflightGlobalEpistemicObservation(draft: EpistemicObservationDraft): GlobalEpistemicObservationCommit =
        commitGlobalEpistemicObservation(
            semanticHistoryMode = state.semanticHistoryMode,
            observationLog = state.epistemicObservationLog,
            nextTimelineGlobalSequence = state.nextTimelineGlobalSequence,
            playerInputRevision = state.playerInputRevision,
            draft = draft,
            actionTimeline = state.actionTimeline,
        )

    /** Atomic instance authority for newly produced Global observations. */
    fun commitGlobalEpistemicObservation(draft: EpistemicObservationDraft): RecordedEpistemicObservation {
        val committed = preflightGlobalEpistemicObservation(draft)
        if (
            committed.observationLog === state.epistemicObservationLog &&
            committed.nextTimelineGlobalSequence == state.nextTimelineGlobalSequence &&
            committed.playerInputRevision == state.playerInputRevision
        ) {
            return committed.record
        }
        updateState(
            state.copy(
                playerInputRevision = committed.playerInputRevision,
                epistemicObservationLog = committed.observationLog,
                nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence,
            ),
        )
        return committed.record
    }

    /**
     * Accept one actual private information publication, with every accompanying registration
     * ruling, as one Session-owned synchronous boundary. Validate and apply to a throwaway
     * Session/journal first; publish neither history nor rulings if any choice is illegal.
     * This does not store a second GameState, and later explicit corrections remain separate.
     */
    fun commitConfirmedPrivateInformationPublication(
        draft: EpistemicObservationDraft,
        registration: ClocktowerConfirmedRegistrationPublicationV1? = null,
        registrationSnapshot: TroubleBrewingGameSnapshotV1? = null,
        legalRoles: List<RoleDefinition> = emptyList(),
    ): RecordedEpistemicObservation {
        val prepared = preflightGlobalEpistemicObservation(draft)
        if (registration != null) {
            require(draft.visibility == ObservationVisibility.PRIVATE &&
                draft.reliability == ObservationReliability.RECEIVED_AS_FUNCTIONING &&
                draft.sourceAbility != null
            ) { "A confirmed registration requires a functioning private ability observation." }
            require(registration.observationRecordId == draft.recordId &&
                registration.shownProposition == draft.proposition &&
                registration.sourceSeat == draft.sourceSeat
            ) { "Registration must reference exactly the private result actually being published." }
        }
        val stagedState = state.copy(
            playerInputRevision = prepared.playerInputRevision,
            epistemicObservationLog = prepared.observationLog,
            nextTimelineGlobalSequence = prepared.nextTimelineGlobalSequence,
        )
        val stagedJournal = if (registration == null) null else {
            val snapshot = requireNotNull(registrationSnapshot) {
                "Registration publication requires the rules-projected current snapshot."
            }
            // A restored journal is local to this transaction. The live journal is never
            // mutated before every registration decision has passed Host legality.
            val trial = ClocktowerGameSession(stagedState, defaultRulesetRef)
            val trialJournal = StorytellerCausalDecisionJournalV1.restore(
                causalDecisionJournal.archive(), stagedState,
            )
            trial.causalDecisionJournal = trialJournal
            // The original runtime snapshot was taken before this very observation.
            // The frozen ruling must refer to the newly accepted player-input revision.
            val trialSnapshot = TroubleBrewingGameSnapshotV1(
                gameId = snapshot.gameId,
                gameSeed = snapshot.gameSeed,
                position = snapshot.position.copy(
                    gameStateRevision = SnapshotField.Known(stagedState.gameStateRevision),
                    playerInputRevision = SnapshotField.Known(stagedState.playerInputRevision),
                ),
                grimoireSeats = snapshot.grimoireSeats,
                setupState = snapshot.setupState,
            )
            ClocktowerConfirmedRegistrationHostWriterV1.commit(
                publication = registration,
                session = trial,
                journal = trialJournal,
                snapshot = trialSnapshot,
                legalRoles = legalRoles,
            )
            trialJournal
        }
        updateState(stagedState)
        if (stagedJournal != null) causalDecisionJournal = stagedJournal
        return prepared.record
    }

    /** Records pre-cutover LegacyLocal information only; Global producers must use the draft API. */
    fun recordEpistemicObservation(record: RecordedEpistemicObservation): ClocktowerSessionState {
        require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.LEGACY_LOCAL) {
            "Direct durable observation recording is reserved for LEGACY_LOCAL history."
        }
        require(record.timelineBinding === ObservationTimelineBinding.LegacyLocal) {
            "LEGACY_LOCAL session cannot accept a pre-bound Global observation."
        }
        return updateState(
            state.copy(
                playerInputRevision = state.playerInputRevision + 1,
                epistemicObservationLog = state.epistemicObservationLog.append(record),
            ),
        )
    }

    private fun updateState(nextState: ClocktowerSessionState): ClocktowerSessionState {
        if (nextState == state) return state
        state = nextState
        return state
    }

    companion object {
        /**
         * Stateless pure transition retained behind the instance API and deterministic replay/contracts.
         * It owns the same cursor/log semantics without requiring a synthetic RulesetRef for scripts whose
         * advanced ruleset is not loaded. Production mutation enters through the live session instance.
         * Mechanical action capture does not increment game/input revisions: the production state
         * mutation that the fact records remains the owner of those revisions.
         */
        fun commitGlobalActionFact(
            semanticHistoryMode: ClocktowerSemanticHistoryMode,
            actionTimeline: ActionFactTimeline,
            observationLog: EpistemicObservationLog,
            nextTimelineGlobalSequence: Long,
            draft: ActionFactDraft,
        ): GlobalActionFactCommit {
            require(semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
                "Global action commit requires GLOBAL_V1 semantic history."
            }
            semanticHistoryMode.requireCompatible(
                actionTimeline = actionTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )

            actionTimeline.entries.firstOrNull { it.fact.actionId == draft.actionId }?.let { existing ->
                require(draft.matches(existing)) {
                    "Action ID '${draft.actionId}' is already committed with different content."
                }
                return GlobalActionFactCommit(
                    entry = existing,
                    actionTimeline = actionTimeline,
                    nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                )
            }

            val point = nextTimelinePoint(
                phase = draft.phase,
                round = draft.round,
                sequence = draft.sequence,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )
            val entry = draft.bindGlobal(point)
            val nextTimeline = actionTimeline.append(entry)
            val nextCursor = point.globalSequence + 1
            semanticHistoryMode.requireCompatible(
                actionTimeline = nextTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextCursor,
            )
            return GlobalActionFactCommit(
                entry = entry,
                actionTimeline = nextTimeline,
                nextTimelineGlobalSequence = nextCursor,
            )
        }

        /**
         * Stateless pure transition retained behind the instance API and deterministic replay/contracts.
         * It owns the same cursor/log/revision semantics without requiring a synthetic RulesetRef for scripts
         * whose advanced ruleset is not loaded. Production mutation enters through the live session instance.
         */
        fun commitGlobalEpistemicObservation(
            semanticHistoryMode: ClocktowerSemanticHistoryMode,
            observationLog: EpistemicObservationLog,
            nextTimelineGlobalSequence: Long,
            playerInputRevision: Long,
            draft: EpistemicObservationDraft,
            actionTimeline: ActionFactTimeline = ActionFactTimeline(),
        ): GlobalEpistemicObservationCommit {
            require(semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1) {
                "Global observation commit requires GLOBAL_V1 semantic history."
            }
            require(playerInputRevision >= 0L) { "playerInputRevision cannot be negative." }
            semanticHistoryMode.requireCompatible(
                actionTimeline = actionTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )

            observationLog.records.firstOrNull { it.recordId == draft.recordId }?.let { existing ->
                require(draft.matches(existing)) {
                    "Observation record ID '${draft.recordId}' is already committed with different content."
                }
                require(existing.timelineBinding is ObservationTimelineBinding.Global) {
                    "GLOBAL_V1 history cannot reuse a LegacyLocal observation record ID."
                }
                return GlobalEpistemicObservationCommit(
                    record = existing,
                    observationLog = observationLog,
                    nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                    playerInputRevision = playerInputRevision,
                )
            }

            check(playerInputRevision != Long.MAX_VALUE) { "Player input revision exhausted." }
            val point = nextTimelinePoint(
                phase = draft.phase,
                round = draft.round,
                sequence = draft.sequence,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            )
            val record = draft.bindGlobal(point)
            val nextLog = observationLog.append(record)
            val nextCursor = point.globalSequence + 1
            semanticHistoryMode.requireCompatible(
                actionTimeline = actionTimeline,
                observationLog = nextLog,
                nextTimelineGlobalSequence = nextCursor,
            )
            return GlobalEpistemicObservationCommit(
                record = record,
                observationLog = nextLog,
                nextTimelineGlobalSequence = nextCursor,
                playerInputRevision = playerInputRevision + 1,
            )
        }

        fun create(
            gameId: String,
            gameSeed: Long,
            rulesetRef: RulesetRef,
            initialState: GameState,
            semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
        ): ClocktowerGameSession {
            val snapshot = GameSnapshot(
                gameId = gameId,
                gameStateRevision = 0,
                playerInputRevision = 0,
                gameSeed = gameSeed,
                rulesetRef = rulesetRef,
                gameState = initialState,
                semanticHistoryMode = semanticHistoryMode,
            )
            return ClocktowerGameSession(
                initialState = snapshot.toSessionState(),
                defaultRulesetRef = rulesetRef,
            )
        }

        fun createProduction(
            gameId: String,
            gameSeed: Long,
            initialState: GameState,
            semanticHistoryMode: ClocktowerSemanticHistoryMode = ClocktowerSemanticHistoryMode.LEGACY_LOCAL,
            initialPlayerContextBySeat: Map<Int, StorytellerPlayerContextInputV1> = emptyMap(),
        ): ClocktowerGameSession = ClocktowerGameSession(
            initialState = ClocktowerSessionState(
                gameId = gameId,
                gameStateRevision = 0L,
                playerInputRevision = 0L,
                gameSeed = gameSeed,
                gameState = initialState,
                semanticHistoryMode = semanticHistoryMode,
                // Immutable initial seat snapshot of persistent player profiles.
                // Later night requests reuse this Host state, not mutable app settings.
                storytellerPlayerContextBySeat = initialPlayerContextBySeat
                    .filterValues { !it.isDefault },
            ),
            defaultRulesetRef = null,
        )

        private fun GameSnapshot.toSessionState(): ClocktowerSessionState = ClocktowerSessionState(
            gameId = gameId,
            gameStateRevision = gameStateRevision,
            playerInputRevision = playerInputRevision,
            gameSeed = gameSeed,
            gameState = gameState,
            actionTimeline = actionTimeline,
            epistemicObservationLog = epistemicObservationLog,
            semanticHistoryMode = semanticHistoryMode,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
        )

        private fun nextTimelinePoint(
            phase: StorytellerPhase,
            round: Int,
            sequence: Int,
            nextTimelineGlobalSequence: Long,
        ): TimelinePoint {
            require(nextTimelineGlobalSequence >= 0L) { "Timeline global sequence cannot be negative." }
            check(nextTimelineGlobalSequence != Long.MAX_VALUE) { "Timeline global sequence exhausted." }
            return TimelinePoint(
                phase = phase,
                round = round,
                sequence = sequence,
                globalSequence = nextTimelineGlobalSequence,
            )
        }
    }
}
