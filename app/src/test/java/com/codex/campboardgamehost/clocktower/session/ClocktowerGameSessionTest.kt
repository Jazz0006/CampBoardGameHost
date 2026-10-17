package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.PlayerExperienceLevelV1
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.domain.StorytellerDeclaredPressureLevelV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.StorytellerPlayerContextInputV1
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationDraft
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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
    fun `storyteller player context is session owned and invalidates provider freshness only`() {
        val session = newSession()
        val custom = StorytellerPlayerContextInputV1(
            experienceLevel = PlayerExperienceLevelV1.BEGINNER,
            claimedRoleIds = listOf(RoleId("Saint"), RoleId("Empath")),
            pressureLevel = StorytellerDeclaredPressureLevelV1.HIGH,
        )

        assertEquals(StorytellerPlayerContextInputV1.DEFAULT, session.storytellerPlayerContext(1))

        val changed = session.updateStorytellerPlayerContext(1, custom)

        assertEquals(1L, changed.playerInputRevision)
        assertEquals(0L, changed.gameStateRevision)
        assertEquals(custom, session.storytellerPlayerContext(1))
        assertEquals(custom, session.state.storytellerPlayerContextBySeat[1])

        val unchanged = session.updateStorytellerPlayerContext(1, custom)
        assertSame(changed, unchanged)
        assertEquals(1L, session.state.playerInputRevision)

        session.updateStorytellerPlayerContext(1, StorytellerPlayerContextInputV1.DEFAULT)
        assertEquals(2L, session.state.playerInputRevision)
        assertEquals(0L, session.state.gameStateRevision)
        assertTrue(1 !in session.state.storytellerPlayerContextBySeat)
        assertEquals(StorytellerPlayerContextInputV1.DEFAULT, session.storytellerPlayerContext(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `storyteller player context rejects seats outside the current game`() {
        newSession().updateStorytellerPlayerContext(
            seat = 99,
            input = StorytellerPlayerContextInputV1(
                experienceLevel = PlayerExperienceLevelV1.EXPERT,
            ),
        )
    }

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
        val before = session.state

        val after = session.updateGameState(initialState)

        assertSame(before, after)
        assertEquals(0, after.gameStateRevision)
    }

    @Test
    fun `epistemic observation history persists across state revisions`() {
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

        assertEquals(1, session.snapshot.epistemicObservationLog.records.size)
        assertEquals("dawn-one-dead", session.snapshot.epistemicObservationLog.records.single().recordId)
        assertEquals(1, session.snapshot.playerInputRevision)
        assertEquals(1, session.snapshot.gameStateRevision)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `session rejects attempts to replace the persisted game seed`() {
        newSession().updateGameState(initialState.copy(seed = initialState.seed + 1))
    }

    @Test
    fun `production session can represent No Greater Joy without synthetic ruleset ref`() {
        val ngjState = initialState.copy(script = ScriptId("no-greater-joy"))

        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-production",
            gameSeed = ngjState.seed,
            initialState = ngjState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )

        assertEquals("ngj-production", session.state.gameId)
        assertEquals(ngjState.script, session.state.gameState.script)
        assertEquals(ngjState.seed, session.state.gameSeed)
        assertEquals(ClocktowerSemanticHistoryMode.GLOBAL_V1, session.state.semanticHistoryMode)
    }

    @Test
    fun `causal journal belongs to the live Session as its single owner`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "journal-session",
            gameSeed = initialState.seed,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val owned = session.causalDecisionJournal
        assertSame(owned, session.causalDecisionJournal)
        val archive = owned.archive()
        assertEquals("journal-session", archive.gameId)
    }

    @Test
    fun `accepted setup choices are Session owned and appear in canonical history`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "single-writer-setup",
            gameSeed = initialState.seed,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val bluffRoles = listOf(RoleId("Saint"), RoleId("Librarian"), RoleId("Monk"))
        session.commitRedHerringSeat(4, StorytellerPhase.FIRST_NIGHT, 1, 1)
        session.commitDemonBluffs(bluffRoles, StorytellerPhase.FIRST_NIGHT, 1, 2)

        assertEquals(4, session.state.redHerringSeat)
        assertEquals(bluffRoles, session.state.demonBluffRoleIds)
        assertEquals(2L, session.state.playerInputRevision)
        assertEquals(0L, session.state.gameStateRevision)
        assertEquals(4, session.view.redHerringSeat)
        assertEquals(bluffRoles, session.view.demonBluffRoleIds)
        assertSame(session.state, session.commitRedHerringSeat(
            4, StorytellerPhase.FIRST_NIGHT, 1, 1,
        ))
        assertSame(session.state, session.commitDemonBluffs(
            bluffRoles, StorytellerPhase.FIRST_NIGHT, 1, 2,
        ))
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(
            listOf("red-herring-choice", "demon-bluff-choice"),
            session.state.actionTimeline.reducerFacts().map {
                when (it) {
                    is com.codex.campboardgamehost.clocktower.domain.ActionFact.RedHerringChoice ->
                        "red-herring-choice"
                    is com.codex.campboardgamehost.clocktower.domain.ActionFact.DemonBluffChoice ->
                        "demon-bluff-choice"
                    else -> "other"
                }
            },
        )
        val replay = com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer.reduce(
            initialSnapshot = session.toGameSnapshot(rulesetRef),
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            facts = session.state.actionTimeline.reducerFacts(),
        )
        assertEquals(4, replay.redHerringSeat)
        assertEquals(bluffRoles, replay.demonBluffRoleIds)

        assertThrows(IllegalArgumentException::class.java) {
            session.commitRedHerringSeat(8, StorytellerPhase.FIRST_NIGHT, 1, 3) // Imp is Evil.
        }
        assertThrows(IllegalArgumentException::class.java) {
            session.commitDemonBluffs(
                listOf(RoleId("Saint"), RoleId("Saint")), StorytellerPhase.FIRST_NIGHT, 1, 3,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            session.commitDemonBluffs(
                listOf(RoleId("Chef")), StorytellerPhase.FIRST_NIGHT, 1, 3,
            ) // Already in play.
        }
        assertEquals(2L, session.state.playerInputRevision)
    }

    @Test
    fun `production revision can advance even when game state is unchanged`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "explicit-revision",
            gameSeed = initialState.seed,
            initialState = initialState,
        )

        session.advanceGameStateRevision()
        session.advanceGameStateRevision()

        assertEquals(2L, session.state.gameStateRevision)
        assertEquals(initialState, session.state.gameState)
    }

    @Test
    fun `global observation preflight does not mutate production session`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "preflight",
            gameSeed = initialState.seed,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val before = session.state

        val proposed = session.preflightGlobalEpistemicObservation(globalPublicDraft("preflight-public"))

        assertSame(before, session.state)
        assertEquals(1L, proposed.nextTimelineGlobalSequence)
        assertEquals(1L, proposed.playerInputRevision)
        assertEquals(listOf("preflight-public"), proposed.observationLog.records.map { it.recordId })
    }

    @Test
    fun `session view publishes cutover safe identity revisions and chronology`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "observable-view",
            gameSeed = initialState.seed,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.advanceGameStateRevision()
        session.commitAcceptedTestAction(
            ActionFactDraft.Poison(
                actionId = "view-poison",
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
                targetSeat = 2,
            ),
        )
        session.commitGlobalEpistemicObservation(globalPublicDraft("view-public"))

        val view = session.view

        assertEquals("observable-view", view.gameId)
        assertEquals(initialState.script, view.scriptId)
        assertEquals(initialState.seed, view.gameSeed)
        assertEquals(2L, view.gameStateRevision)
        assertEquals(1L, view.playerInputRevision)
        assertEquals(ClocktowerSemanticHistoryMode.GLOBAL_V1, view.semanticHistoryMode)
        assertEquals(listOf("view-poison"), view.actionTimeline.reducerFacts().map { it.actionId })
        assertEquals(listOf("view-public"), view.epistemicObservationLog.records.map { it.recordId })
        assertEquals(2L, view.nextTimelineGlobalSequence)
    }

    @Test
    fun `production session can project strict GameSnapshot when ruleset ref is available`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "strict-projection",
            gameSeed = initialState.seed,
            initialState = initialState,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.advanceGameStateRevision()
        session.recordPlayerInput()

        val snapshot = session.toGameSnapshot(rulesetRef)

        assertEquals(session.state.gameId, snapshot.gameId)
        assertEquals(session.state.gameStateRevision, snapshot.gameStateRevision)
        assertEquals(session.state.playerInputRevision, snapshot.playerInputRevision)
        assertEquals(session.state.gameSeed, snapshot.gameSeed)
        assertEquals(session.state.gameState, snapshot.gameState)
        assertEquals(rulesetRef, snapshot.rulesetRef)
    }

    private fun newSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "game-2026-08-06-001",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
    )

    private fun globalPublicDraft(recordId: String): EpistemicObservationDraft = EpistemicObservationDraft(
        recordId = recordId,
        phase = StorytellerPhase.DAWN,
        round = 1,
        sequence = 0,
        sourceSeat = null,
        sourceAbility = null,
        visibility = ObservationVisibility.PUBLIC,
        recipientSeats = emptySet(),
        reliability = ObservationReliability.NOT_ABILITY_INFORMATION,
        proposition = InformationProposition.AliveAt(2, false),
    )
}
