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
import org.junit.Assert.assertTrue
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
        val canonicalIntent = DawnCommitIntent(
            roleChanges = listOf(DawnRoleChangeIntent(targetSeat = 2, roleId = imp)),
            death = DawnDeathIntent(targetSeat = 1),
        )
        val uninterrupted = materialize(
            start = emptyMaterialization(choiceGameState(impAlive = true)),
            intent = canonicalIntent,
        )

        val partiallyMaterializedGame = choiceGameState(impAlive = false)
        val persistedCheckpoint = checkpoint().persistedValues()
        val restored = NightTransactionRestoreComposition.restore(
            persistedCheckpointValues = persistedCheckpoint,
            baseGameState = partiallyMaterializedGame,
            canonicalInteractionIds = listOf(impInteraction, successorInteraction, empathInteraction),
            demonSuccessorInteractionId = successorInteraction,
            demonRoleId = imp,
        )

        assertFalse(restored.reconstruction.effectiveState.isMechanicallyAlive(1))
        assertEquals(imp, restored.reconstruction.effectiveState.currentRoleId(2))
        assertEquals(poisoner, partiallyMaterializedGame.playerAt(2)?.actualRole)

        val deathTransition = NightDawnResolutionPlanner.planValidatedNightDeath(
            baseGameState = partiallyMaterializedGame,
            checkpoint = restored.checkpoint,
            input = NightDawnDeathResolutionInput(
                originalDeathSeat = 1,
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
            successionResolution = DemonSuccessionResolution.Choice(targetSeats = setOf(2)),
            demonRoleId = imp,
        )
        val identityTransition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = partiallyMaterializedGame,
            checkpoint = successionTransition.checkpoint,
            demonRoleId = imp,
        )
        val restoredIntent = DawnCommitIntent(
            roleChanges = requireNotNull(identityTransition.dawnCommitIntent).roleChanges,
            death = requireNotNull(deathTransition.dawnCommitIntent).death,
        )
        assertEquals(canonicalIntent, restoredIntent)

        val retried = materialize(
            start = emptyMaterialization(partiallyMaterializedGame),
            intent = restoredIntent,
        )

        assertEquals(uninterrupted, retried)
        assertExactlyOnce(retried)

        val replay = materialize(retried, restoredIntent)
        assertEquals(retried, replay)
    }

    private fun materialize(
        start: MaterializedDawn,
        intent: DawnCommitIntent,
    ): MaterializedDawn {
        var aliveSeats = start.state.aliveSeats
        val roleIdsBySeat = start.state.roleIdsBySeat.toMutableMap()
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
            ),
            actionTimeline = actionTimeline,
            observationLog = observationLog,
            nextTimelineGlobalSequence = nextTimelineGlobalSequence,
            playerInputRevision = playerInputRevision,
        )
    }

    private fun assertExactlyOnce(materialized: MaterializedDawn) {
        val facts = materialized.actionTimeline.reducerFacts()
        assertEquals(3, facts.size)
        assertEquals(3, facts.map { it.actionId }.distinct().size)
        assertEquals(1, facts.count { it is ActionFact.Death })
        assertEquals(1, facts.count { it is ActionFact.RoleChange })
        assertEquals(1, facts.count { it is ActionFact.PhaseAdvance })

        assertEquals(1, materialized.observationLog.records.size)
        assertEquals(
            InformationProposition.AliveAt(seat = 1, alive = false),
            materialized.observationLog.records.single().proposition,
        )
        assertEquals(4L, materialized.nextTimelineGlobalSequence)
        assertEquals(1L, materialized.playerInputRevision)
        assertEquals(setOf(2, 3), materialized.state.aliveSeats)
        assertEquals(imp, materialized.state.roleIdsBySeat[2])
        assertEquals(StorytellerPhase.DAWN, materialized.state.currentPhase)
    }

    private fun emptyMaterialization(gameState: GameState) = MaterializedDawn(
        state = DawnDurableMaterializationState(
            aliveSeats = gameState.players.filter { it.alive }.mapTo(linkedSetOf()) { it.seat },
            roleIdsBySeat = gameState.players.associate { it.seat to it.actualRole },
            currentPhase = StorytellerPhase.NIGHT,
            committedActionIds = emptySet(),
            committedObservationRecordIds = emptySet(),
        ),
        actionTimeline = ActionFactTimeline(),
        observationLog = EpistemicObservationLog(),
        nextTimelineGlobalSequence = 0L,
        playerInputRevision = 0L,
    )

    private fun choiceGameState(impAlive: Boolean) = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            player(1, "Imp", imp, CharacterType.DEMON, Alignment.EVIL, alive = impAlive),
            player(2, "Poisoner", poisoner, CharacterType.MINION, Alignment.EVIL),
            player(3, "Empath", empath, CharacterType.TOWNSFOLK, Alignment.GOOD),
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
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
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
        const val DEATH_SEQUENCE = 10
        const val ROLE_SEQUENCE = 11
        const val PHASE_SEQUENCE = 12
    }
}
