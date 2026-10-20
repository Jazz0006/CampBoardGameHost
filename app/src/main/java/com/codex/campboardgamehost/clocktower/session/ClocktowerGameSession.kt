package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionContext
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionSemantics
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRequestV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.KlutzDeathTriggerEvidenceV1
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.epistemic.bindGlobal
import com.codex.campboardgamehost.clocktower.epistemic.matches

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
            nightTargets = state.nightTargets,
            voteStanding = state.voteStanding,
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
            requireNotNull(state.gameState.playerAt(seat)) {
                "Unknown Red Herring seat $seat."
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

    /**
     * One confirmed Host mechanical step: preflight the action and state, then publish exactly
     * one Session transition carrying both the mechanical projection and global action fact.
     * An already accepted action ID is an idempotent no-op, even after the revision advances.
     * The caller supplies the actual confirmed result; no gameplay legality verdict is required.
     */
    fun commitConfirmedGameAction(
        draft: ActionFactDraft,
        nextGameState: GameState,
        expectedGameStateRevision: Long,
        publishedObservation: EpistemicObservationDraft? = null,
        preserveNightChoices: Boolean = false,
        publishedObservations: List<EpistemicObservationDraft> = emptyList(),
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
        // Preflight any actual information provided to players before atomically updating Session.
        var nextObservations = state.epistemicObservationLog
        var nextCursor = committed.nextTimelineGlobalSequence
        var nextInputRevision = state.playerInputRevision
        (listOfNotNull(publishedObservation) + publishedObservations).forEach { observation ->
            val result = commitGlobalEpistemicObservation(
                semanticHistoryMode = state.semanticHistoryMode,
                observationLog = nextObservations,
                nextTimelineGlobalSequence = nextCursor,
                playerInputRevision = nextInputRevision,
                draft = observation,
                actionTimeline = committed.actionTimeline,
            )
            nextObservations = result.observationLog
            nextCursor = result.nextTimelineGlobalSequence
            nextInputRevision = result.playerInputRevision
        }
        if (committed.actionTimeline === state.actionTimeline) {
            check(nextObservations === state.epistemicObservationLog) {
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
            nightTargets = state.nightTargets.after(
                committed.entry,
                priorPoisonSeat = if (committed.entry.fact is ActionFact.Poison)
                    (state.actionTimeline.entries.asReversed().firstOrNull {
                        it.point.round == committed.entry.point.round && it.fact is ActionFact.Poison
                    }?.fact as? ActionFact.Poison)?.targetSeat else null,
                preserveNightChoices = preserveNightChoices,
            ),
            voteStanding = when (val fact = committed.entry.fact) {
                is ActionFact.Vote -> state.voteStanding.confirm(fact.nomineeSeat,
                    fact.voterSeats.size, (state.gameState.players.count { it.alive } + 1) / 2)
                is ActionFact.PhaseAdvance -> if (fact.phase == StorytellerPhase.NIGHT ||
                    (fact.phase == StorytellerPhase.DAY && draft.phase != StorytellerPhase.DAY))
                    ClocktowerVoteStanding() else state.voteStanding
                else -> state.voteStanding
            },
            actionTimeline = committed.actionTimeline,
            epistemicObservationLog = nextObservations,
            playerInputRevision = nextInputRevision,
            nextTimelineGlobalSequence = nextCursor,
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
        if (draft.phase == StorytellerPhase.NIGHT) {
            check(state.mayorRulingSeat() == null) {
                "The confirmed Imp attack on the Mayor must be resolved before Day."
            }
        }
        val legal = when (draft.phase) {
            StorytellerPhase.FIRST_NIGHT, StorytellerPhase.NIGHT ->
                draft.nextPhase == StorytellerPhase.DAY && draft.nextRound == draft.round
            StorytellerPhase.DAY ->
                draft.nextPhase == StorytellerPhase.NIGHT && draft.nextRound == draft.round + 1
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
        // All night deaths were settled at their real causes. Moving to Day never invents
        // a duplicate PUBLIC AliveAt observation or a separate disclosure transaction.
        // Day UI reads this round's confirmed Death facts directly from Session.
        return commitConfirmedGameAction(
            draft = draft,
            nextGameState = state.gameState,
            expectedGameStateRevision = expectedGameStateRevision,
        )
    }

    /**
     * A functioning Mayor's redirect is an immediate, irreversible attack-result decision.
     * The private choice and any actual Death are committed together in ONE Session update.
     * This is not a later-night draft and cannot be amended by a second action ID.
     */
    fun commitConfirmedMayorRedirect(
        draft: ActionFactDraft.MayorRedirectChoice,
        request: StorytellerProviderRequestV1,
        expectedGameStateRevision: Long,
    ): TimelineBoundActionFact = confirmAtomically {
        require(draft.phase == StorytellerPhase.NIGHT && state.phase == draft.phase &&
            draft.round == state.round)
        state.actionTimeline.entries.firstOrNull {
            it.point.phase == StorytellerPhase.NIGHT && it.point.round == draft.round &&
                it.fact is ActionFact.MayorRedirectChoice
        }?.let { accepted ->
            check(accepted.fact.actionId == draft.actionId && draft.matches(accepted)) {
                "The Mayor ruling is already committed; it cannot be amended."
            }
            return@confirmAtomically accepted
        }
        check(state.gameStateRevision == expectedGameStateRevision) { "Stale Mayor confirmation." }
        require(request.identity.gameId == state.gameId)
        require(request.sourceRevision == StorytellerProviderRevisionV1(
            state.gameStateRevision, state.playerInputRevision))
        val question = request.decisionContext as?
            com.codex.campboardgamehost.clocktower.domain.StorytellerProviderDecisionContextV1.MayorRedirect
        require(question?.mayorSeat == draft.mayorSeat) { "Mayor request source differs from the actual action." }
        val mayor = requireNotNull(state.gameState.playerAt(draft.mayorSeat))
        val target = requireNotNull(state.gameState.playerAt(draft.targetSeat)) {
            "Unknown Mayor redirect target seat."
        }
        check(state.nightTargets.attackSeat == draft.mayorSeat &&
            mayor.alive && mayor.actualRole == RoleId("Mayor") && !mayor.poisoned) {
            "Mayor ruling requires a confirmed attack against a functioning living Mayor."
        }
        // The attack's deterministic boundary already checked a functioning Monk on the Mayor.
        val effectiveMonkProtection = state.nightTargets.monkSeat == draft.targetSeat &&
            state.gameState.players.any {
                it.alive && !it.poisoned && it.actualRole == RoleId("Monk")
            }
        val noDeath = !target.alive || effectiveMonkProtection ||
            (target.actualRole == RoleId("Soldier") && !target.poisoned)
        val deathSeat = draft.targetSeat.takeUnless { noDeath }
        val inheritedImpSeat = if (deathSeat != null && target.actualRole == RoleId("Imp")) {
            val roster = state.gameState.players
            (DemonSuccessionSemantics.resolve(DemonSuccessionContext(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = false,
                aliveCountBeforeDemonDeath = roster.count { it.alive },
                functioningScarletWomanSeat = roster.singleOrNull {
                    it.alive && !it.poisoned && it.actualRole == RoleId("Scarlet Woman")
                }?.seat,
                livingMinionSeats = roster.filter {
                    it.alive && it.actualType == CharacterType.MINION
                }.mapTo(linkedSetOf()) { it.seat },
            )) as? DemonSuccessionResolution.Forced)?.targetSeat
        } else null
        val ruling = commitGlobalActionFact(
            state.semanticHistoryMode, state.actionTimeline, state.epistemicObservationLog,
            state.nextTimelineGlobalSequence, prepareGlobalActionDraft(draft),
        )
        val death = deathSeat?.let { seat ->
            check(ruling.nextTimelineGlobalSequence < Int.MAX_VALUE)
            commitGlobalActionFact(
                state.semanticHistoryMode, ruling.actionTimeline, state.epistemicObservationLog,
                ruling.nextTimelineGlobalSequence,
                prepareGlobalActionDraft(ActionFactDraft.Death(
                    ConfirmedNightEffectIdentity.death(state.gameId, draft.round, seat),
                    StorytellerPhase.NIGHT, draft.round,
                    ruling.nextTimelineGlobalSequence.toInt() + 1, seat,
                )),
            )
        }
        val identity = inheritedImpSeat?.let { seat ->
            val predecessor = requireNotNull(death)
            check(predecessor.nextTimelineGlobalSequence < Int.MAX_VALUE)
            commitGlobalActionFact(
                state.semanticHistoryMode, predecessor.actionTimeline, state.epistemicObservationLog,
                predecessor.nextTimelineGlobalSequence,
                prepareGlobalActionDraft(ActionFactDraft.RoleChange(
                    ConfirmedNightEffectIdentity.roleChange(state.gameId, draft.round, seat, RoleId("Imp")),
                    StorytellerPhase.NIGHT, draft.round,
                    predecessor.nextTimelineGlobalSequence.toInt() + 1,
                    seat, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON,
                )),
            )
        }
        val facts = 1L + (if (death != null) 1L else 0L) +
            (if (identity != null) 1L else 0L)
        check(state.gameStateRevision <= Long.MAX_VALUE - facts)
        updateState(state.copy(
            gameStateRevision = state.gameStateRevision + facts,
            gameState = state.gameState.copy(players = state.gameState.players.map { player ->
                val afterDeath = if (player.seat == deathSeat)
                    player.copy(alive = false, poisoned = false) else player
                if (player.seat == inheritedImpSeat) afterDeath.copy(
                    actualRole = RoleId("Imp"),
                    actualAlignment = Alignment.EVIL,
                    actualType = CharacterType.DEMON,
                ) else afterDeath
            }),
            actionTimeline = identity?.actionTimeline ?: death?.actionTimeline ?: ruling.actionTimeline,
            nextTimelineGlobalSequence = identity?.nextTimelineGlobalSequence ?:
                death?.nextTimelineGlobalSequence ?: ruling.nextTimelineGlobalSequence,
            nightTargets = state.nightTargets.copy(mayorRedirectSeat = draft.targetSeat),
        ))
        finishIfDecided()
        ruling.entry
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
    fun commitGlobalEpistemicObservation(
        draft: EpistemicObservationDraft,
        expectedRevision: StorytellerDecisionRevision? = null,
    ): RecordedEpistemicObservation {
        val committed = preflightGlobalEpistemicObservation(draft)
        if (
            committed.observationLog === state.epistemicObservationLog &&
            committed.nextTimelineGlobalSequence == state.nextTimelineGlobalSequence &&
            committed.playerInputRevision == state.playerInputRevision
        ) {
            return committed.record
        }
        check(expectedRevision == null || expectedRevision ==
            StorytellerDecisionRevision(state.gameStateRevision, state.playerInputRevision)) {
            "Stale information publication."
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

    /**
     * A night action is accepted on its OWN Next button, with no intermediate pending cache.
     * Re-entering a previously accepted step is idempotent for the same choice; attempting
     * to change it is rejected, including after UI recomposition or a duplicate click.
     */
    private fun confirmNightOperation(
        kind: NightOperationKind,
        targetSeat: Int?,
        phase: StorytellerPhase,
        round: Int,
        stepId: String,
        actorSeat: Int? = null,
    ): TimelineBoundActionFact {
        require(kind != NightOperationKind.ATTACK)
        require(phase == state.phase && round == state.round)
        require(phase in setOf(StorytellerPhase.FIRST_NIGHT, StorytellerPhase.NIGHT))
        require(kind != NightOperationKind.PROTECT || phase == StorytellerPhase.NIGHT)
        require(stepId.isNotBlank())
        targetSeat?.let { requireNotNull(state.gameState.playerAt(it)) { "Unknown night target seat $it." } }
        require(kind == NightOperationKind.POISON || targetSeat != null)
        if (kind == NightOperationKind.BUTLER) {
            val actor = requireNotNull(actorSeat?.let(state.gameState::playerAt)) {
                "Butler must have a current actor seat."
            }
            require(actor.alive && (actor.actualRole.value == "Butler" ||
                actor.shownRole?.value == "Butler"))
            require(actorSeat != targetSeat) { "Butler cannot choose themself." }
        }
        val key = "night-${kind.name.lowercase()}-${state.gameId}-${phase.name}-$round-$stepId-"
        val actionId = "$key${actorSeat ?: "none"}-${targetSeat ?: "none"}"
        state.actionTimeline.entries.firstOrNull {
            it.point.phase == phase && it.point.round == round && it.fact.actionId.startsWith(key)
        }?.let { accepted ->
            check(accepted.fact.actionId == actionId) {
                "This night action is committed and cannot be amended."
            }
            return accepted
        }
        val fact = when (kind) {
            NightOperationKind.POISON ->
                ActionFactDraft.Poison(actionId, phase, round, nextInputSequence, targetSeat)
            NightOperationKind.PROTECT ->
                ActionFactDraft.Protect(actionId, phase, round, nextInputSequence, requireNotNull(targetSeat))
            NightOperationKind.BUTLER ->
                ActionFactDraft.ButlerMasterChoice(actionId, phase, round, nextInputSequence,
                    requireNotNull(actorSeat), requireNotNull(targetSeat))
            NightOperationKind.ATTACK -> error("Imp uses its own immediate attack settlement.")
        }
        return when (fact) {
            is ActionFactDraft.Poison ->
                commitConfirmedPoisonTransition(fact, state.gameStateRevision)
            else ->
                commitConfirmedGameAction(fact, state.gameState, state.gameStateRevision)
        }
    }

    fun confirmNightPoison(targetSeat: Int?, phase: StorytellerPhase, round: Int, stepId: String) =
        confirmNightOperation(NightOperationKind.POISON, targetSeat, phase, round, stepId)

    fun confirmNightProtection(targetSeat: Int, phase: StorytellerPhase, round: Int, stepId: String) =
        confirmNightOperation(NightOperationKind.PROTECT, targetSeat, phase, round, stepId)

    fun confirmNightButlerMaster(actorSeat: Int, masterSeat: Int, phase: StorytellerPhase, round: Int, stepId: String) =
        confirmNightOperation(NightOperationKind.BUTLER, masterSeat, phase, round, stepId, actorSeat)

    /**
     * The forced mechanical result of a functioning Imp's attack. This reads only the
     * accepted Session roster and already-committed protection, not UI cards or a forecast.
     * Mayor redirection and Imp self-kill are deliberately separate decision boundaries.
     */
    private fun deterministicImpDeathSeat(targetSeat: Int): Int? {
        val players = state.gameState.players
        val attacker = players.singleOrNull { it.alive && it.actualRole == RoleId("Imp") }
            ?: return null
        if (attacker.poisoned) return null
        val target = requireNotNull(state.gameState.playerAt(targetSeat))
        if (!target.alive) return null
        val effectiveProtection = state.nightTargets.monkSeat == targetSeat &&
            players.any { it.alive && !it.poisoned && it.actualRole == RoleId("Monk") }
        if (effectiveProtection) return null
        if (target.seat == attacker.seat) return null // Self-kill is settled with succession below.
        if (!target.poisoned && target.actualRole in setOf(RoleId("Soldier"), RoleId("Mayor"))) {
            return null // Soldier is safe; Mayor requires an actual redirect-or-stay decision.
        }
        return targetSeat
    }

    /**
     * Imp Attack + actual Death + any chosen successor RoleChange form one Session transaction.
     * These mechanical facts do not expose hidden causes or roles on the day table.
     */
    private fun acceptDeterministicNightAttack(
        targetSeat: Int,
        phase: StorytellerPhase,
        round: Int,
        actionId: String,
        successorSeat: Int?,
    ): TimelineBoundActionFact {
        val roster = state.gameState.players
        val imp = roster.singleOrNull { it.alive && it.actualRole == RoleId("Imp") }
        val monkProtectsTarget = state.nightTargets.monkSeat == targetSeat &&
            roster.any { it.alive && !it.poisoned && it.actualRole == RoleId("Monk") }
        val selfKill = imp != null && imp.seat == targetSeat &&
            !imp.poisoned && !monkProtectsTarget
        val succession = if (selfKill) {
            DemonSuccessionSemantics.resolve(DemonSuccessionContext(
                demonActuallyDied = true,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = roster.count { it.alive },
                functioningScarletWomanSeat = roster.singleOrNull {
                    it.alive && !it.poisoned && it.actualRole == RoleId("Scarlet Woman")
                }?.seat,
                livingMinionSeats = roster.filter {
                    it.alive && it.actualType == CharacterType.MINION
                }.mapTo(linkedSetOf()) { it.seat },
            ))
        } else DemonSuccessionResolution.None
        val promotedSeat = when (succession) {
            is DemonSuccessionResolution.Forced -> {
                require(successorSeat == succession.targetSeat) {
                    "A functioning Scarlet Woman must inherit this Imp death."
                }
                successorSeat
            }
            is DemonSuccessionResolution.Choice -> {
                require(successorSeat in succession.targetSeats) {
                    "Choose a living Minion as the new Imp before confirming self-kill."
                }
                successorSeat
            }
            DemonSuccessionResolution.None -> {
                require(successorSeat == null) {
                    "A successor cannot be selected when the Imp did not die or none can inherit."
                }
                null
            }
        }
        val deathSeat = if (selfKill) targetSeat else deterministicImpDeathSeat(targetSeat)
        val attack = commitGlobalActionFact(
            state.semanticHistoryMode, state.actionTimeline, state.epistemicObservationLog,
            state.nextTimelineGlobalSequence,
            prepareGlobalActionDraft(ActionFactDraft.Attack(
                actionId, phase, round, nextInputSequence, targetSeat,
            )),
        )
        val death = deathSeat?.let { seat ->
            check(attack.nextTimelineGlobalSequence < Int.MAX_VALUE)
            commitGlobalActionFact(
                state.semanticHistoryMode, attack.actionTimeline, state.epistemicObservationLog,
                attack.nextTimelineGlobalSequence,
                prepareGlobalActionDraft(ActionFactDraft.Death(
                    ConfirmedNightEffectIdentity.death(state.gameId, round, seat),
                    StorytellerPhase.NIGHT, round,
                    attack.nextTimelineGlobalSequence.toInt() + 1, seat,
                )),
            )
        }
        val identity = promotedSeat?.let { seat ->
            val predecessor = requireNotNull(death)
            check(predecessor.nextTimelineGlobalSequence < Int.MAX_VALUE)
            commitGlobalActionFact(
                state.semanticHistoryMode, predecessor.actionTimeline, state.epistemicObservationLog,
                predecessor.nextTimelineGlobalSequence,
                prepareGlobalActionDraft(ActionFactDraft.RoleChange(
                    ConfirmedNightEffectIdentity.roleChange(state.gameId, round, seat, RoleId("Imp")),
                    StorytellerPhase.NIGHT, round,
                    predecessor.nextTimelineGlobalSequence.toInt() + 1,
                    seat, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON,
                )),
            )
        }
        val totalFacts = 1L + (if (death != null) 1L else 0L) +
            (if (identity != null) 1L else 0L)
        check(state.gameStateRevision <= Long.MAX_VALUE - totalFacts)
        updateState(state.copy(
            gameStateRevision = state.gameStateRevision + totalFacts,
            actionTimeline = identity?.actionTimeline ?: death?.actionTimeline ?: attack.actionTimeline,
            nextTimelineGlobalSequence = identity?.nextTimelineGlobalSequence ?:
                death?.nextTimelineGlobalSequence ?: attack.nextTimelineGlobalSequence,
            gameState = state.gameState.copy(players = roster.map { player ->
                val afterDeath = if (player.seat == deathSeat)
                    player.copy(alive = false, poisoned = false) else player
                if (player.seat == promotedSeat) afterDeath.copy(
                    actualRole = RoleId("Imp"),
                    actualAlignment = Alignment.EVIL,
                    actualType = CharacterType.DEMON,
                ) else afterDeath
            }),
            nightTargets = state.nightTargets.withAttack(targetSeat),
        ))
        return attack.entry
    }

    private fun playersAfterConfirmedNightDeath(seat: Int) = state.gameState.players.map { player ->
        if (player.seat == seat) player.copy(alive = false, poisoned = false) else player
    }

    /**
     * The Imp's irreversible step boundary. The selected target is a UI draft until this
     * invocation. Attack and forced Death become facts in the SAME Session update.
     * The idempotence check is keyed by this night's stable step identity.
     */
    fun confirmNightAttack(
        targetSeat: Int,
        phase: StorytellerPhase,
        round: Int,
        stepId: String,
        successorSeat: Int? = null,
    ): TimelineBoundActionFact = confirmAtomically {
        require(phase == StorytellerPhase.NIGHT && phase == state.phase && round == state.round)
        require(stepId.isNotBlank())
        requireNotNull(state.gameState.playerAt(targetSeat)) { "Unknown Imp target $targetSeat." }
        val prefix = "night-attack-${state.gameId}-${phase.name}-$round-$stepId-"
        val actionId = "${prefix}none-$targetSeat"
        val previous = state.actionTimeline.entries.firstOrNull {
            it.point.phase == phase && it.point.round == round &&
                it.fact is ActionFact.Attack && it.fact.actionId.startsWith(prefix)
        }
        if (previous != null) {
            val previouslyPromoted = state.actionTimeline.entries.firstOrNull {
                it.point.phase == StorytellerPhase.NIGHT && it.point.round == round &&
                    it.fact is ActionFact.RoleChange &&
                    (it.fact as ActionFact.RoleChange).role == RoleId("Imp")
            }?.let { (it.fact as ActionFact.RoleChange).targetSeat }
            check(previous.fact.actionId == actionId && previouslyPromoted == successorSeat) {
                "The Imp attack and its successor are already committed and cannot change."
            }
            return@confirmAtomically previous
        }
        val accepted = acceptDeterministicNightAttack(targetSeat, phase, round, actionId, successorSeat)
        finishIfDecided()
        accepted
    }

    private var persist: ((ClocktowerSessionState) -> Unit)? = null
    private var confirmingBatch = false

    fun persistWith(save: (ClocktowerSessionState) -> Unit) {
        save(state)
        persist = save
    }

    /** Main-thread confirmation boundary: persist the entire accepted responsibility once. */
    internal fun <T> confirmAtomically(confirm: () -> T): T {
        check(!confirmingBatch)
        val before = state
        confirmingBatch = true
        try {
            val result = confirm()
            if (state != before) persist?.invoke(state)
            return result
        } catch (failure: Throwable) {
            state = before
            throw failure
        } finally {
            confirmingBatch = false
        }
    }

    private fun updateState(nextState: ClocktowerSessionState): ClocktowerSessionState {
        if (nextState == state) return state
        if (!confirmingBatch) persist?.invoke(nextState)
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

        fun restore(confirmed: ClocktowerSessionState): ClocktowerGameSession =
            ClocktowerGameSession(confirmed, null)

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
