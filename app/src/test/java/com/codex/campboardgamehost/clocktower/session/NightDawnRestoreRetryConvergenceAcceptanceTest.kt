package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** SNE-7.9E acceptance: restore/retry must converge to the uninterrupted Dawn durable state. */
class NightDawnRestoreRetryConvergenceAcceptanceTest {
    private val imp = RoleId("Imp")
    private val poisoner = RoleId("Poisoner")
    private val empath = RoleId("Empath")
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")

    @Test
    fun `partial mechanical Dawn restored without durable facts converges exactly to uninterrupted materialization`() {
        val canonicalIntent = canonicalIntent()
        val uninterrupted = materialize(
            start = emptyMaterialization(choiceGameState(impAlive = true)),
            intent = canonicalIntent,
        )

        val partiallyMaterializedGame = choiceGameState(impAlive = false)
        val restoredIntent = restoredIntent(partiallyMaterializedGame)
        assertEquals(canonicalIntent, restoredIntent)

        val stateFirstStart = emptyMaterialization(partiallyMaterializedGame).let { start ->
            start.copy(
                state = start.state.copy(currentPoisonTargetSeat = null),
            )
        }
        val retried = materialize(
            start = stateFirstStart,
            intent = restoredIntent,
        )

        assertEquals(uninterrupted, retried)
        assertExactlyOnce(retried)

        val replay = materialize(retried, restoredIntent)
        assertEquals(retried, replay)
    }

    @Test
    fun `durable poison clear history with stale mechanical poison converges to uninterrupted materialization`() {
        val intent = canonicalIntent()
        val uninterrupted = materialize(
            start = emptyMaterialization(choiceGameState(impAlive = true)),
            intent = intent,
        )
        val historyFirstStart = uninterrupted.copy(
            state = uninterrupted.state.copy(currentPoisonTargetSeat = EMPATH_SEAT),
        )

        val retried = materialize(historyFirstStart, intent)

        assertEquals(uninterrupted, retried)
        assertExactlyOnce(retried)
    }

    private fun canonicalIntent() = DawnCommitIntent(
        roleChanges = listOf(DawnRoleChangeIntent(targetSeat = POISONER_SEAT, roleId = imp)),
        death = DawnDeathIntent(targetSeat = IMP_SEAT),
        poisonCarry = DawnPoisonCarryIntent(
            targetSeat = null,
            previousTargetSeat = EMPATH_SEAT,
        ),
    )

    private fun restoredIntent(partiallyMaterializedGame: GameState): DawnCommitIntent {
        val restored = NightTransactionRestoreComposition.restore(
            persistedCheckpointValues = checkpoint().persistedValues(),
            baseGameState = partiallyMaterializedGame,
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = imp,
        )

        assertFalse(restored.reconstruction.effectiveState.isMechanicallyAlive(IMP_SEAT))
        assertEquals(imp, restored.reconstruction.effectiveState.currentRoleId(POISONER_SEAT))
        assertEquals(poisoner, partiallyMaterializedGame.playerAt(POISONER_SEAT)?.actualRole)

        val deathTransition = NightDawnResolutionPlanner.planValidatedNightDeath(
            baseGameState = partiallyMaterializedGame,
            checkpoint = restored.checkpoint,
            input = NightDawnDeathResolutionInput(
                originalDeathSeat = IMP_SEAT,
                mayorSeat = null,
                mayorRedirectMayApply = false,
                attackOutcome = DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
                effectiveNightState = restored.reconstruction.effectiveState,
                demonRoleIds = setOf(imp),
            ),
        )
        val successionTransition = NightDawnResolutionPlanner.planDemonSuccession(
            baseGameState = partiallyMaterializedGame,
            checkpoint = restored.checkpoint,
            successionResolution = DemonSuccessionResolution.Choice(targetSeats = setOf(POISONER_SEAT)),
            demonRoleId = imp,
        )
        val identityTransition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = partiallyMaterializedGame,
            checkpoint = successionTransition.checkpoint,
            demonRoleId = imp,
            poisonResolutionInput = NightDawnPoisonResolutionInput(
                poisonerSeat = POISONER_SEAT,
                poisonerRoleId = poisoner,
                effectiveNightState = restored.reconstruction.effectiveState,
            ),
        )
        val identityIntent = requireNotNull(identityTransition.dawnCommitIntent)
        return DawnCommitIntent(
            roleChanges = identityIntent.roleChanges,
            death = requireNotNull(deathTransition.dawnCommitIntent).death,
            poisonCarry = identityIntent.poisonCarry,
        )
    }

    private fun materialize(
        start: MaterializedDawn,
        intent: DawnCommitIntent,
    ): MaterializedDawn {
        var aliveSeats = start.state.aliveSeats
        val roleIdsBySeat = start.state.roleIdsBySeat.toMutableMap()
        var currentPoisonTargetSeat = start.state.currentPoisonTargetSeat
        var currentPhase = start.state.currentPhase
        var actionTimeline = start.actionTimeline
        var observationLog = start.observationLog
        var nextTimelineGlobalSequence = start.nextTimelineGlobalSequence
        var playerInputRevision = start.playerInputRevision

        val plannerState = start.state.copy(
            committedActionIds = actionTimeline.entries.mapTo(linkedSetOf()) { it.fact.actionId },
            committedObservationRecordIds = observationLog.records.mapTo(linkedSetOf()) { it.recordId },
        )
        val plan = NightDawnDurableMaterializationPlanner.plan(
            gameId = GAME_ID,
            round = ROUND,
            intent = intent,
            state = plannerState,
            advanceToDawn = true,
        )

        fun commitAction(draft: ActionFactDraft) {
            val committed = ClocktowerGameSession.commitGlobalActionFact(
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                actionTimeline = actionTimeline,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                draft = draft,
            )
            actionTimeline = committed.actionTimeline
            nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence
        }

        fun commitObservation(draft: EpistemicObservationDraft) {
            val committed = ClocktowerGameSession.commitGlobalEpistemicObservation(
                semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
                observationLog = observationLog,
                nextTimelineGlobalSequence = nextTimelineGlobalSequence,
                playerInputRevision = playerInputRevision,
                draft = draft,
                actionTimeline = actionTimeline,
            )
            observationLog = committed.observationLog
            nextTimelineGlobalSequence = committed.nextTimelineGlobalSequence
            playerInputRevision = committed.playerInputRevision
        }

        plan.death?.let { death ->
            death.actionIdToCommit?.let { actionId ->
                commitAction(ActionFactDraft.Death(
                    actionId = actionId,
                    phase = StorytellerPhase.NIGHT,
                    round = ROUND,
                    sequence = DEATH_SEQUENCE,
                    targetSeat = death.intent.targetSeat,
                ))
            }
            if (death.stateMutationRequired) {
                aliveSeats = aliveSeats - death.intent.targetSeat
            }
            death.publicAliveObservationIdToCommit?.let { recordId ->
                commitObservation(EpistemicObservationDraft(
                    recordId = recordId,
                    phase = StorytellerPhase.NIGHT,
                    round = ROUND,
                    sequence = DEATH_SEQUENCE,
                    sourceSeat = null,
                    sourceAbility = null,
                    visibility = ObservationVisibility.PUBLIC,
                    recipientSeats = emptySet(),
                    reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                    proposition = InformationProposition.AliveAt(death.intent.targetSeat, false),
                ))
            }
        }

        plan.roleChanges.forEachIndexed { index, roleChange ->
            assertEquals(imp, roleChange.intent.roleId)
            roleChange.actionIdToCommit?.let { actionId ->
                commitAction(ActionFactDraft.RoleChange(
                    actionId = actionId,
                    phase = StorytellerPhase.NIGHT,
                    round = ROUND,
                    sequence = ROLE_SEQUENCE + index,
                    targetSeat = roleChange.intent.targetSeat,
                    role = roleChange.intent.roleId,
                    alignment = Alignment.EVIL,
                    type = CharacterType.DEMON,
                ))
            }
            if (roleChange.stateMutationRequired) {
                roleIdsBySeat[roleChange.intent.targetSeat] = roleChange.intent.roleId
            }
        }

        plan.poison?.let { poison ->
            poison.actionIdToCommit?.let { actionId ->
                commitAction(ActionFactDraft.Poison(
                    actionId = actionId,
                    phase = StorytellerPhase.NIGHT,
                    round = ROUND,
                    sequence = POISON_SEQUENCE,
                    targetSeat = poison.intent.targetSeat,
                ))
            }
            if (poison.stateMutationRequired) {
                currentPoisonTargetSeat = poison.intent.targetSeat
            }
        }

        plan.phaseAdvance?.let { phaseAdvance ->
            phaseAdvance.actionIdToCommit?.let { actionId ->
                commitAction(ActionFactDraft.PhaseAdvance(
                    actionId = actionId,
                    phase = StorytellerPhase.NIGHT,
                    round = ROUND,
                    sequence = PHASE_SEQUENCE,
                    nextPhase = phaseAdvance.targetPhase,
                    nextRound = ROUND,
                ))
            }
            if (phaseAdvance.stateMutationRequired) {
                currentPhase = phaseAdvance.targetPhase
            }
        }

        return MaterializedDawn(
            state = DawnDurableMaterializationState(
                aliveSeats = aliveSeats,
                roleIdsBySeat = roleIdsBySeat.toMap(),
                currentPhase = currentPhase,
                committedActionIds = actionTimeline.entries.mapTo(linkedSetOf()) { it.fact.actionId },
                committedObservationRecordIds = observationLog.records.mapTo(linkedSetOf()) { it.recordId },
                currentPoisonTargetSeat = currentPoisonTargetSeat,
            ),
            actionTimeline = actionTimeline,
            observationLog = observationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            playerInputRevision = playerInputRevision,
        )
    }

    private fun assertExactlyOnce(materialized: MaterializedDawn) {
        val facts = materialized.actionTimeline.reducerFacts()
        assertEquals(5, facts.size)
        assertEquals(5, facts.map { it.actionId }.distinct().size)
        assertEquals(1, facts.count { it is ActionFact.Death })
        assertEquals(1, facts.count { it is ActionFact.RoleChange })
        assertEquals(2, facts.count { it is ActionFact.Poison })
        assertEquals(1, facts.count { it.actionId == DAWN_POISON_CLEAR_ACTION_ID })
        assertEquals(1, facts.count { it is ActionFact.PhaseAdvance })

        assertEquals(1, materialized.observationLog.records.size)
        assertEquals(
            InformationProposition.AliveAt(seat = IMP_SEAT, alive = false),
            materialized.observationLog.records.single().proposition,
        )
        assertEquals(6L, materialized.nextTimelineGlobalSequence)
        assertEquals(1L, materialized.playerInputRevision)
        assertEquals(setOf(POISONER_SEAT, EMPATH_SEAT), materialized.state.aliveSeats)
        assertEquals(imp, materialized.state.roleIdsBySeat[POISONER_SEAT])
        assertEquals(null, materialized.state.currentPoisonTargetSeat)
        assertEquals(StorytellerPhase.DAWN, materialized.state.currentPhase)
    }

    private fun emptyMaterialization(gameState: GameState): MaterializedDawn {
        val initialAction = ClocktowerGameSession.commitGlobalActionFact(
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
            actionTimeline = ActionFactTimeline(),
            observationLog = EpistemicObservationLog(),
            nextTimelineGlobalSequence = 0L,
            draft = ActionFactDraft.Poison(
                actionId = INITIAL_POISON_ACTION_ID,
                phase = StorytellerPhase.NIGHT,
                round = ROUND,
                sequence = INITIAL_POISON_SEQUENCE,
                targetSeat = EMPATH_SEAT,
            ),
        )
        return MaterializedDawn(
            state = DawnDurableMaterializationState(
                aliveSeats = gameState.players.filter { it.alive }.mapTo(linkedSetOf()) { it.seat },
                roleIdsBySeat = gameState.players.associate { it.seat to it.actualRole },
                currentPhase = StorytellerPhase.NIGHT,
                committedActionIds = initialAction.actionTimeline.entries
                    .mapTo(linkedSetOf()) { it.fact.actionId },
                committedObservationRecordIds = emptySet(),
                currentPoisonTargetSeat = EMPATH_SEAT,
            ),
            actionTimeline = initialAction.actionTimeline,
            observationLog = EpistemicObservationLog(),
            nextTimelineGlobalSequence = initialAction.nextTimelineGlobalSequence,
            playerInputRevision = 0L,
        )
    }

    private fun choiceGameState(impAlive: Boolean) = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            player(IMP_SEAT, "Imp", imp, CharacterType.DEMON, Alignment.EVIL, alive = impAlive),
            player(POISONER_SEAT, "Poisoner", poisoner, CharacterType.MINION, Alignment.EVIL),
            player(EMPATH_SEAT, "Empath", empath, CharacterType.TOWNSFOLK, Alignment.GOOD),
        ),
        seed = 17L,
    )

    private fun player(
        seat: Int,
        name: String,
        role: RoleId,
        type: CharacterType,
        alignment: Alignment,
        alive: Boolean = true,
    ) = PlayerState(
        seat = seat,
        name = name,
        actualRole = role,
        actualAlignment = alignment,
        actualType = type,
        alive = alive,
    )

    private fun checkpoint() = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = ROUND,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 2,
        confirmedAttackTarget = "Imp",
        attackDraftTarget = "Imp",
        confirmedPoisonTarget = "Empath",
        poisonDraftTarget = "Empath",
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = "Poisoner",
        confirmedDemonSuccessorTarget = "Poisoner",
        nextTimelineGlobalSequence = 17L,
    )

    private data class MaterializedDawn(
        val state: DawnDurableMaterializationState,
        val actionTimeline: ActionFactTimeline,
        val observationLog: EpistemicObservationLog,
        val nextTimelineGlobalSequence: Long,
        val playerInputRevision: Long,
    )

    private companion object {
        const val GAME_ID = "game-17"
        const val ROUND = 3
        const val IMP_SEAT = 1
        const val POISONER_SEAT = 2
        const val EMPATH_SEAT = 3
        const val INITIAL_POISON_SEQUENCE = 9
        const val DEATH_SEQUENCE = 10
        const val ROLE_SEQUENCE = 11
        const val POISON_SEQUENCE = 12
        const val PHASE_SEQUENCE = 13
        const val INITIAL_POISON_ACTION_ID = "night-poison-seat-3"
        const val DAWN_POISON_CLEAR_ACTION_ID = "dawn-game-17-3-poison-seat-3-to-none"
    }
}
