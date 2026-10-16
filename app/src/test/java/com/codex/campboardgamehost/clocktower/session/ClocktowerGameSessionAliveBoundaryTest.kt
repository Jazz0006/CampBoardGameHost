package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ClocktowerGameSessionAliveBoundaryTest {
    private val initialState = TroubleBrewingFixtures.eightPlayerExample()

    @Test
    fun `confirmed death preserves other seats and atomically appends public observation`() {
        val targetSeat = 2
        val seeded = initialState.copy(players = initialState.players.map { player ->
            if (player.seat == targetSeat) player.copy(poisoned = true) else player
        })
        val session = ClocktowerGameSession.createProduction(
            gameId = "a1-death",
            gameSeed = seeded.seed,
            initialState = seeded,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val before = session.state.gameState
        val draft = ActionFactDraft.Death("night-death", StorytellerPhase.DAWN, 2, 1, targetSeat)

        val death = session.commitConfirmedPlayerDeath(
            draft = draft,
            expectedGameStateRevision = 0L,
            publishedObservationId = "public-death-seat-2",
        )

        assertEquals(
            requireNotNull(before.playerAt(targetSeat)).copy(alive = false, poisoned = false),
            session.state.gameState.playerAt(targetSeat),
        )
        assertEquals(before.players.filterNot { it.seat == targetSeat },
            session.state.gameState.players.filterNot { it.seat == targetSeat })
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(1L, session.state.playerInputRevision)
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(death, session.state.actionTimeline.entries.single())
        val published = session.state.epistemicObservationLog.records.single()
        assertEquals(1L, published.timelineBinding.let {
            (it as com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding.Global).point.globalSequence
        })
        assertEquals(InformationProposition.AliveAt(targetSeat, false), published.proposition)

        val committed = session.state
        assertEquals(death, session.commitConfirmedPlayerDeath(draft, 0L, "public-death-seat-2"))
        assertEquals(committed, session.state)

        assertNotNull(runCatching {
            session.commitConfirmedPlayerDeath(
                ActionFactDraft.Death("night-death-conflict", StorytellerPhase.DAWN, 2, 1, targetSeat),
                1L,
                "another-observation",
            )
        }.exceptionOrNull())
        assertEquals(committed, session.state)

        // An already committed public record ID cannot be reused for another death.
        assertNotNull(runCatching {
            session.commitConfirmedPlayerDeath(
                ActionFactDraft.Death("another-death", StorytellerPhase.DAWN, 2, 2, 3),
                1L,
                "public-death-seat-2",
            )
        }.exceptionOrNull())
        assertEquals(committed, session.state)
    }

    @Test
    fun `Klutz execution captures poison and alive status before the atomic death`() {
        val role = RoleId("Klutz")
        val klutzState = initialState.copy(players = initialState.players.map { player ->
            if (player.seat == 1) player.copy(
                actualRole = role, shownRole = role,
                actualAlignment = Alignment.GOOD, actualType = CharacterType.OUTSIDER,
                poisoned = true,
            ) else player
        })
        val session = ClocktowerGameSession.createProduction(
            gameId = "a1-klutz-exec",
            gameSeed = klutzState.seed,
            initialState = klutzState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val fact = session.commitConfirmedPlayerDeath(
            ActionFactDraft.Execution("klutz-exec", StorytellerPhase.DAY, 2, 1, 1),
            expectedGameStateRevision = 0L,
            publishedObservationId = "klutz-public",
        ).fact as ActionFact.Execution

        val proof = requireNotNull(fact.klutzDeathTrigger)
        assertEquals(true, proof.wasAlive)
        assertEquals(true, proof.wasPoisoned)
        assertEquals(0L, proof.sourceGameStateRevision)
        assertEquals(false, session.state.gameState.playerAt(1)?.alive)
        assertEquals(false, session.state.gameState.playerAt(1)?.poisoned)
    }

    @Test
    fun `stale revision and invalid death publication cannot create half a transition`() {
        val session = newSession("a1-reject")
        val before = session.state
        assertNotNull(runCatching {
            session.commitConfirmedPlayerDeath(
                ActionFactDraft.Death("stale", StorytellerPhase.DAWN, 2, 1, 1),
                8L,
                "not-published",
            )
        }.exceptionOrNull())
        assertEquals(before, session.state)

        assertNotNull(runCatching {
            session.commitConfirmedPlayerDeath(
                ActionFactDraft.Death("unknown-seat", StorytellerPhase.DAWN, 2, 1, 100),
                0L,
            )
        }.exceptionOrNull())
        assertEquals(before, session.state)
    }

    private fun newSession(gameId: String) = ClocktowerGameSession.createProduction(
        gameId = gameId,
        gameSeed = initialState.seed,
        initialState = initialState,
        semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
    )
}

