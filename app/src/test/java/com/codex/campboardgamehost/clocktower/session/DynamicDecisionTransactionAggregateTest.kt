package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

class DynamicDecisionTransactionAggregateTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val initial = GameSnapshot(
        "transaction-game", 0, 0, game.seed,
        RulesetRef(game.script, "0123456789abcdef0123456789abcdef", "v1", "official", RuleCoverage.VERIFIED), game,
    )
    private val policy = StorytellerPolicySnapshot("policy-1", RecommendationStyle.BALANCED, "algorithm-1")

    @Test fun `C2 duplicate confirmation creates one event observation and action effect`() {
        val aggregate = aggregate()
        val request = request(observation = observation("shown-1"), facts = listOf(ActionFact.Death("death-1", 1, 2)))

        assertTrue(aggregate.commit(request) is DynamicCommitResult.Applied)
        val duplicate = aggregate.commit(request)

        assertTrue(duplicate is DynamicCommitResult.AlreadyApplied)
        val state = aggregate.snapshot()
        assertEquals(1, state.events.size)
        assertEquals(1, state.current.snapshot.epistemicObservationLog.records.size)
        assertFalse(state.current.snapshot.gameState.playerAt(2)!!.alive)
        assertEquals(1, state.current.snapshot.gameStateRevision)
    }

    @Test fun `C3 stale key leaves every observable store unchanged`() {
        val aggregate = aggregate()
        val before = aggregate.snapshot()
        aggregate.refreshCurrentKey(key(initial.copy(playerInputRevision = 1)))

        val result = aggregate.commit(request(facts = listOf(ActionFact.Death("death-1", 1, 2))))

        assertTrue(result is DynamicCommitResult.Stale)
        assertEquals(before, aggregate.snapshot())
    }

    @Test fun `P2 shown observation remains after a later action`() {
        val aggregate = aggregate()
        aggregate.commit(request(observation = observation("shown-1")))
        val afterFirst = aggregate.snapshot()
        aggregate.refreshCurrentKey(key(afterFirst.current.snapshot))

        assertTrue(aggregate.commit(request(id = "event-2", key = aggregate.currentKey(), facts = listOf(ActionFact.Poison("poison-1", 1, 3)))) is DynamicCommitResult.Applied)
        val afterSecond = aggregate.snapshot()

        assertEquals(listOf("shown-1"), afterSecond.current.snapshot.epistemicObservationLog.records.map { it.recordId })
        assertTrue(afterSecond.current.snapshot.gameState.playerAt(3)!!.poisoned)
    }

    @Test fun `ordered facts replay identically and duplicate facts fail before mutation`() {
        val facts = listOf(
            ActionFact.RoleChange("role-2", 2, 2, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
            ActionFact.Death("death-1", 1, 3),
            ActionFact.PhaseAdvance("phase-3", 3, StorytellerPhase.DAWN, 2),
        )
        val first = DynamicActionReducer.reduce(initial, StorytellerPhase.FIRST_NIGHT, 1, facts)
        val second = DynamicActionReducer.reduce(initial, StorytellerPhase.FIRST_NIGHT, 1, facts.reversed())
        assertEquals(first, second)

        val aggregate = aggregate()
        val before = aggregate.snapshot()
        val failed = aggregate.commit(request(facts = listOf(ActionFact.Death("same", 1, 2), ActionFact.Death("same", 2, 3))))
        assertTrue(failed is DynamicCommitResult.Failed)
        assertEquals(before, aggregate.snapshot())
    }

    @Test fun `T1 death role change and phase advance invalidate downstream night state`() {
        val facts = listOf(
            ActionFact.Protect("protect-1", 1, 2),
            ActionFact.Attack("attack-2", 2, 8),
            ActionFact.Death("imp-self-kill-3", 3, 8),
            ActionFact.RoleChange("scarlet-succeeds-4", 4, 7, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON),
            ActionFact.PhaseAdvance("dawn-5", 5, StorytellerPhase.DAWN, 2),
        )

        val state = DynamicActionReducer.reduce(initial, StorytellerPhase.NIGHT, 1, facts)

        assertFalse(state.snapshot.gameState.playerAt(8)!!.alive)
        assertEquals(RoleId("Imp"), state.snapshot.gameState.playerAt(7)!!.actualRole)
        assertEquals(CharacterType.DEMON, state.snapshot.gameState.playerAt(7)!!.actualType)
        assertEquals(StorytellerPhase.DAWN, state.phase)
        assertEquals(2, state.round)
        assertTrue(state.protectedSeats.isEmpty())
        assertNull(state.pendingAttackSeat)
        assertEquals(5, state.snapshot.gameStateRevision)
    }

    @Test fun `scenarios 7 and 10 replay the same persisted night facts after interruption`() {
        val persistedFacts = listOf(
            ActionFact.Poison("poison-empath-1", 1, 2),
            ActionFact.Attack("attack-chef-2", 2, 1),
            ActionFact.Death("death-chef-3", 3, 1),
            ActionFact.PhaseAdvance("dawn-4", 4, StorytellerPhase.DAWN, 2),
        )

        val uninterrupted = DynamicActionReducer.reduce(initial, StorytellerPhase.NIGHT, 1, persistedFacts)
        val restored = DynamicActionReducer.reduce(initial, StorytellerPhase.NIGHT, 1, persistedFacts.reversed())

        assertEquals(uninterrupted, restored)
        assertTrue(restored.snapshot.gameState.playerAt(2)!!.poisoned)
        assertFalse(restored.snapshot.gameState.playerAt(1)!!.alive)
        assertEquals(StorytellerPhase.DAWN, restored.phase)
    }

    @Test fun `observation append failure is atomic`() {
        val present = observation("shown-1")
        val seeded = initial.copy(epistemicObservationLog = EpistemicObservationLog().append(present))
        val aggregate = aggregate(seeded)
        val before = aggregate.snapshot()

        val result = aggregate.commit(request(key = aggregate.currentKey(), observation = present, facts = listOf(ActionFact.Death("death-1", 1, 2))))

        assertTrue(result is DynamicCommitResult.Failed)
        assertEquals(before, aggregate.snapshot())
    }

    private fun aggregate(snapshot: GameSnapshot = initial): DynamicDecisionTransactionAggregate =
        DynamicDecisionTransactionAggregate(DynamicTransactionState(snapshot, StorytellerPhase.FIRST_NIGHT, 1), key(snapshot))

    private fun key(snapshot: GameSnapshot) = DynamicDecisionSnapshot(
        snapshot, StorytellerPhase.FIRST_NIGHT, 1, "info", policy = policy,
    ).key()

    private fun request(
        id: String = "event-1",
        key: DynamicRecommendationKey = key(initial),
        observation: RecordedEpistemicObservation? = null,
        facts: List<ActionFact> = emptyList(),
    ) = DynamicCommitRequest(key, event(id, key), observation, facts)

    private fun observation(id: String) = RecordedEpistemicObservation(
        id, StorytellerPhase.FIRST_NIGHT, 1, 1, 1, RoleId("Chef"), ObservationVisibility.PRIVATE, setOf(1),
        ObservationReliability.RECEIVED_AS_FUNCTIONING,
        InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..8).toList(), 1),
    )

    private fun event(id: String, key: DynamicRecommendationKey) = StorytellerDecisionEvent(
        id, "request-$id", "transaction-game:$id", key.gameStateRevision, key.playerInputRevision, initial.rulesetRef,
        "algorithm-1", "selector-1", 1, key.stateDigest, key.decisionHistoryDigest, "candidate-1",
        DecisionOutcomeSnapshot("information", mapOf("value" to "0")), AbilityState.FUNCTIONING,
        TruthRelation.TRUE_TO_ACTUAL_STATE, emptyList(), QualityTier.RECOMMENDED, 1, 1,
        emptyMap(), "pool", listOf(CandidateAuditSummary("candidate-1", "family", QualityTier.RECOMMENDED, 1, 1, listOf("ok"))),
        listOf("ok"), DecisionEventStatus.PROPOSED,
    )
}
