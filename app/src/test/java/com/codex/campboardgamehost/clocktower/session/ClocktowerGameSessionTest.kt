package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.history.HistoricalClueSignature
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ClocktowerGameSessionTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()
    private val rulesetRef = RulesetRef(
        scriptId = initialState.script,
        scriptContentHash = "e12f6425ece137da02477a642235c797",
        rulesetVersion = "trouble-brewing-v1",
        sourceRevision = "official-wiki-2026-08-06",
        coverage = RuleCoverage.VERIFIED,
    )

    @Test
    fun `game state and player input revisions advance independently`() {
        val session = newSession()

        session.recordPlayerInput()
        session.updateGameState(
            initialState.copy(players = initialState.players.mapIndexed { index, player ->
                if (index == 0) player.copy(alive = false) else player
            }),
        )

        assertEquals(1, session.snapshot.playerInputRevision)
        assertEquals(1, session.snapshot.gameStateRevision)
        assertEquals(initialState.seed, session.snapshot.gameSeed)
    }

    @Test
    fun `unchanged game state does not create a revision`() {
        val session = newSession()
        val before = session.snapshot

        val after = session.updateGameState(initialState)

        assertSame(before, after)
        assertEquals(0, after.gameStateRevision)
    }

    @Test
    fun `restored session retains the persisted game seed`() {
        val original = newSession().also { it.recordPlayerInput() }
        val restored = ClocktowerGameSession.restore(original.snapshot)

        assertEquals(original.snapshot, restored.snapshot)
        assertEquals(initialState.seed, restored.snapshot.gameSeed)
    }

    @Test
    fun `completed game signature survives snapshot restore`() {
        val signature = HistoricalClueSignature(
            decisionType = "setup-plan",
            drunkShownRole = com.codex.campboardgamehost.clocktower.domain.RoleId("Monk"),
        )
        val original = newSession().also { it.recordCompletedGameSignature(signature) }

        val restored = ClocktowerGameSession.restore(original.snapshot)

        assertEquals(listOf(signature), restored.snapshot.crossGameHistory.recentSignatures)
    }

    @Test
    fun `epistemic observation history persists across state revisions and restore`() {
        val session = newSession()
        session.recordEpistemicObservation(
            RecordedEpistemicObservation(
                recordId = "dawn-one-dead",
                phase = StorytellerPhase.DAWN,
                round = 1,
                sequence = 0,
                sourceSeat = null,
                sourceAbility = null,
                visibility = ObservationVisibility.PUBLIC,
                recipientSeats = emptySet(),
                reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
                proposition = InformationProposition.AliveAt(1, false),
            ),
        )
        session.updateGameState(initialState.copy(players = initialState.players.mapIndexed { index, player ->
            if (index == 0) player.copy(alive = false) else player
        }))

        val restored = ClocktowerGameSession.restore(session.snapshot)

        assertEquals(1, restored.snapshot.epistemicObservationLog.records.size)
        assertEquals("dawn-one-dead", restored.snapshot.epistemicObservationLog.records.single().recordId)
        assertEquals(1, restored.snapshot.playerInputRevision)
        assertEquals(1, restored.snapshot.gameStateRevision)
    }

    @Test
    fun `restored epistemic history rebuilds identical recipient knowledge`() {
        val session = newSession()
        session.recordEpistemicObservation(RecordedEpistemicObservation(
            "public-death", StorytellerPhase.DAWN, 1, 0, null, null,
            ObservationVisibility.PUBLIC, emptySet(), ObservationReliability.NOT_ABILITY_INFORMATION,
            InformationProposition.AliveAt(2, false),
        ))
        session.recordEpistemicObservation(RecordedEpistemicObservation(
            "chef-info", StorytellerPhase.FIRST_NIGHT, 1, 4, 1, RoleId("Chef"),
            ObservationVisibility.PRIVATE, setOf(1), ObservationReliability.RECEIVED_AS_FUNCTIONING,
            InformationProposition.NumericResult(NumericMetric.ADJACENT_EVIL_PAIRS, 1, (1..8).toList(), 1),
        ))
        val roles = initialState.players.associate { it.seat to (it.shownRole ?: it.actualRole) }
        fun knowledge(snapshot: com.codex.campboardgamehost.clocktower.domain.GameSnapshot) =
            A4PlayerKnowledgeFactory.createAll(FormalGameState.from(snapshot, StorytellerPhase.DAWN, 1), roles, snapshot.epistemicObservationLog)

        val before = knowledge(session.snapshot)
        val after = knowledge(ClocktowerGameSession.restore(session.snapshot).snapshot)

        assertEquals(before, after)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `session rejects attempts to replace the persisted game seed`() {
        newSession().updateGameState(initialState.copy(seed = initialState.seed + 1))
    }

    private fun newSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "game-2026-08-06-001",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
    )
}
