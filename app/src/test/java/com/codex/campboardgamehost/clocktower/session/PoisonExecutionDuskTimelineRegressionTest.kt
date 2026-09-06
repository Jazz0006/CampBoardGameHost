package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Regression for the real-device shape: First Night poison -> Day 1 Poisoner execution -> Dusk expiry. */
class PoisonExecutionDuskTimelineRegressionTest {
    @Test
    fun `Poisoner execution with active poison can clear at dusk advance night and restore`() {
        val initialState = TroubleBrewingFixtures.eightPlayerExample().let { state ->
            state.copy(
                players = state.players.map { player ->
                    when (player.seat) {
                        POISON_TARGET_SEAT -> player.copy(poisoned = false)
                        POISONER_SEAT -> player.copy(
                            actualRole = RoleId("Poisoner"),
                            actualType = CharacterType.MINION,
                            shownRole = RoleId("Poisoner"),
                        )
                        else -> player
                    }
                },
            )
        }
        val rulesetRef = RulesetRef(
            scriptId = initialState.script,
            scriptContentHash = "e12f6425ece137da02477a642235c797",
            rulesetVersion = "trouble-brewing-v1",
            sourceRevision = "official-wiki-2026-08-06",
            coverage = RuleCoverage.VERIFIED,
        )
        val session = ClocktowerGameSession.create(
            gameId = GAME_ID,
            gameSeed = initialState.seed,
            rulesetRef = rulesetRef,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )

        session.commitGlobalActionFact(
            ActionFactDraft.Poison(
                actionId = "first-night-poison",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = ROUND,
                sequence = 4,
                targetSeat = POISON_TARGET_SEAT,
            ),
        )
        session.updateGameState(
            session.snapshot.gameState.copy(
                players = session.snapshot.gameState.players.map { player ->
                    if (player.seat == POISON_TARGET_SEAT) player.copy(poisoned = true) else player
                },
            ),
        )

        session.updateGameState(
            session.snapshot.gameState.copy(
                players = session.snapshot.gameState.players.map { player ->
                    if (player.seat == POISONER_SEAT) player.copy(alive = false) else player
                },
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.Execution(
                actionId = "day-1-execution-poisoner",
                phase = StorytellerPhase.DAY,
                round = ROUND,
                sequence = EXECUTION_LOCAL_SEQUENCE,
                targetSeat = POISONER_SEAT,
            ),
        )
        session.commitGlobalEpistemicObservation(
            EpistemicObservationDraft(
                recordId = "public-alive-$GAME_ID-$EXECUTION_LOCAL_SEQUENCE-$POISONER_SEAT",
                phase = StorytellerPhase.DAY,
                round = ROUND,
                sequence = EXECUTION_LOCAL_SEQUENCE,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(POISONER_SEAT, false),
            ),
        )

        val durablePreviousPoisonTarget = DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(
            actionTimeline = session.snapshot.actionTimeline,
            round = ROUND,
        )
        val expiryPlan = requireNotNull(
            DuskPoisonExpiryMaterializationPlanner.plan(
                gameId = GAME_ID,
                round = ROUND,
                previousTargetSeat = durablePreviousPoisonTarget,
                state = DuskPoisonExpiryMaterializationState(
                    currentPoisonTargetSeat = POISON_TARGET_SEAT,
                    committedActionIds = session.snapshot.actionTimeline.entries
                        .map { it.fact.actionId }
                        .toSet(),
                ),
            ),
        )
        assertTrue(expiryPlan.stateMutationRequired)
        val clearActionId = requireNotNull(expiryPlan.actionIdToCommit)

        session.commitGlobalActionFact(
            ActionFactDraft.Poison(
                actionId = clearActionId,
                phase = StorytellerPhase.DAY,
                round = ROUND,
                sequence = DUSK_LOCAL_SEQUENCE,
                targetSeat = null,
            ),
        )
        session.updateGameState(
            session.snapshot.gameState.copy(
                players = session.snapshot.gameState.players.map { player ->
                    if (player.seat == POISON_TARGET_SEAT) player.copy(poisoned = false) else player
                },
            ),
        )
        session.commitGlobalActionFact(
            ActionFactDraft.PhaseAdvance(
                actionId = "phase-night-2",
                phase = StorytellerPhase.DAY,
                round = ROUND,
                sequence = DUSK_LOCAL_SEQUENCE,
                nextPhase = StorytellerPhase.NIGHT,
                nextRound = ROUND + 1,
            ),
        )

        val restored = ClocktowerGameSession.restore(session.snapshot)
        assertEquals(session.snapshot, restored.snapshot)
        assertEquals(5L, restored.snapshot.nextTimelineGlobalSequence)
        assertEquals(
            listOf(0L, 1L, 3L, 4L),
            restored.snapshot.actionTimeline.entries.map { it.point.globalSequence },
        )
        assertEquals(
            listOf(2L),
            restored.snapshot.epistemicObservationLog.records.map { record ->
                val binding = record.timelineBinding as com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding.Global
                binding.point.globalSequence
            },
        )
        assertFalse(restored.snapshot.gameState.playerAt(POISONER_SEAT)!!.alive)
        assertFalse(restored.snapshot.gameState.playerAt(POISON_TARGET_SEAT)!!.poisoned)

        val retryPreviousTarget = DuskPoisonExpiryRecoveryAuthority.latestTargetSeatForRound(
            actionTimeline = restored.snapshot.actionTimeline,
            round = ROUND,
        )
        assertNull(retryPreviousTarget)
    }

    private companion object {
        const val GAME_ID = "poison-execution-regression"
        const val ROUND = 1
        const val POISONER_SEAT = 7
        const val POISON_TARGET_SEAT = 2
        const val EXECUTION_LOCAL_SEQUENCE = 12
        const val DUSK_LOCAL_SEQUENCE = 13
    }
}
