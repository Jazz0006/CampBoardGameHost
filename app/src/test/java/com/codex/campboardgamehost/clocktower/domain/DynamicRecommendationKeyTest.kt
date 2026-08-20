package com.codex.campboardgamehost.clocktower.domain

import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.*
import org.junit.Test

class DynamicRecommendationKeyTest {
    private val game = TroubleBrewingFixtures.eightPlayerExample()
    private val base = GameSnapshot("dynamic-game", 0, 0, game.seed,
        RulesetRef(game.script, "0123456789abcdef0123456789abcdef", "v1", "official", RuleCoverage.VERIFIED), game)
    private val policy = StorytellerPolicySnapshot("p1", RecommendationStyle.BALANCED, "a1")
    private fun snapshot(gameSnapshot: GameSnapshot = base, poison: Int? = null, locks: List<String> = emptyList(),
                         style: RecommendationStyle = RecommendationStyle.BALANCED) =
        DynamicDecisionSnapshot(gameSnapshot, StorytellerPhase.FIRST_NIGHT, 1, "first-night-info", poisonTargetSeat = poison,
            lockedDecisionTokens = locks, policy = policy.copy(style = style))

    @Test fun `R1 identical snapshots have identical digests and keys`() {
        assertEquals(snapshot(), snapshot())
        assertEquals(snapshot().key(), snapshot().key())
    }

    @Test fun `R2 absent UI state cannot alter revisions or key`() {
        val before = snapshot()
        val afterUiExpand = snapshot()
        assertEquals(before.gameSnapshot.gameStateRevision, afterUiExpand.gameSnapshot.gameStateRevision)
        assertEquals(before.key(), afterUiExpand.key())
    }

    @Test fun `R3 poison draft changes input revision and key only`() {
        val changed = snapshot(base.copy(playerInputRevision = 1), poison = 2)
        assertEquals(0, changed.gameSnapshot.gameStateRevision)
        assertEquals(1, changed.gameSnapshot.playerInputRevision)
        assertNotEquals(snapshot().key(), changed.key())
    }

    @Test fun `R4 poison confirmation changes state revision and supersedes old generation`() {
        val store = DynamicRecommendationGenerationStore<String>()
        val old = store.begin(snapshot(base.copy(playerInputRevision = 1), poison = 2).key())
        val confirmed = store.begin(snapshot(base.copy(gameStateRevision = 1, playerInputRevision = 1), poison = 2).key())
        assertEquals(DynamicPublishResult.Rejected(DynamicGenerationTerminal.SUPERSEDED), store.publishIfCurrent(old, "old"))
        assertEquals(DynamicPublishResult.Published("new"), store.publishIfCurrent(confirmed, "new"))
    }

    @Test fun `R5 style changes key but not mechanical digest`() {
        val balanced = snapshot()
        val gentle = snapshot(style = RecommendationStyle.GENTLE)
        assertEquals(balanced.stateDigest, gentle.stateDigest)
        assertNotEquals(balanced.key(), gentle.key())
    }

    @Test fun `R6 private observation changes only redacted observation digest in key`() {
        val record = RecordedEpistemicObservation("private-role", StorytellerPhase.FIRST_NIGHT, 1, 1, 1, RoleId("Investigator"),
            ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.RoleAt(2, RoleId("Poisoner")))
        val observed = snapshot(base.copy(epistemicObservationLog = EpistemicObservationLog().append(record)))
        assertNotEquals(snapshot().observationLogDigest, observed.observationLogDigest)
        assertFalse(observed.key().toString().contains("Poisoner"))
    }

    @Test fun `canonical sets and lock order produce stable key`() {
        val first = DynamicDecisionSnapshot(base, StorytellerPhase.FIRST_NIGHT, 1, "x", protectedSeats = linkedSetOf(2, 1),
            lockedDecisionTokens = listOf("b", "a"), policy = policy)
        val second = DynamicDecisionSnapshot(base, StorytellerPhase.FIRST_NIGHT, 1, "x", protectedSeats = linkedSetOf(1, 2),
            lockedDecisionTokens = listOf("a", "b"), policy = policy)
        assertEquals(first.key(), second.key())
    }

    @Test fun `C1 cancel and stale generation cannot publish`() {
        val store = DynamicRecommendationGenerationStore<String>()
        val generation = store.begin(snapshot().key())
        store.cancelGame(base.gameId)
        assertEquals(DynamicPublishResult.Rejected(DynamicGenerationTerminal.CANCELLED), store.publishIfCurrent(generation, "late"))
    }

    @Test fun `E1 failure OOM and cancellation remain distinct from ready`() {
        val store = DynamicRecommendationGenerationStore<String>()
        val failed = store.begin(snapshot().key())
        assertEquals(DynamicGenerationTerminal.FAILED, store.fail(failed, IllegalStateException("synthetic")))
        val exhausted = store.begin(snapshot(style = RecommendationStyle.GENTLE).key())
        assertEquals(DynamicGenerationTerminal.RESOURCE_EXHAUSTED, store.fail(exhausted, OutOfMemoryError("synthetic")))
        val cancelled = store.begin(snapshot(style = RecommendationStyle.AGGRESSIVE).key())
        store.cancelGame(base.gameId)
        assertEquals(DynamicGenerationTerminal.CANCELLED, store.terminal(cancelled))
    }
}
