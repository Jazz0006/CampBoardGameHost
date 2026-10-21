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
    fun `confirmed private information persists across a real death commit`() {
        val session = ClocktowerGameSession.createProduction(
            "confirmed-information", initialState.seed, initialState, ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val received = session.commitGlobalEpistemicObservation(EpistemicObservationDraft(
            recordId = "chef-received-one", phase = StorytellerPhase.FIRST_NIGHT,
            round = 1, sequence = session.nextInputSequence, sourceSeat = 1, sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE, recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.NumericResult(
                com.codex.campboardgamehost.clocktower.epistemic.NumericMetric.ADJACENT_EVIL_PAIRS,
                1, initialState.players.map { it.seat }, 1,
            ),
        ))
        session.commitConfirmedPlayerDeath(
            ActionFactDraft.Death("confirmed-death", StorytellerPhase.NIGHT, 2, session.nextInputSequence, 1),
            expectedGameStateRevision = session.state.gameStateRevision,
        )

        assertEquals(listOf(received), session.state.epistemicObservationLog.records)
        assertEquals(setOf(1), received.recipientSeats)
        assertEquals(ObservationVisibility.PRIVATE, received.visibility)
        assertEquals(false, session.state.gameState.playerAt(1)!!.alive)
        assertEquals(1L, session.state.playerInputRevision)
        assertEquals(1L, session.state.gameStateRevision)
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(0L, (received.timelineBinding as
            com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding.Global).point.globalSequence)
        assertEquals(1L, session.state.actionTimeline.entries.single().point.globalSequence)
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
        assertSame(session.state, session.commitRedHerringSeat(
            4, StorytellerPhase.FIRST_NIGHT, 1, 1,
        ))
        assertSame(session.state, session.commitDemonBluffs(
            bluffRoles, StorytellerPhase.FIRST_NIGHT, 1, 2,
        ))
        assertEquals(2L, session.state.nextTimelineGlobalSequence)
        assertEquals(
            listOf("red-herring-choice", "demon-bluff-choice"),
            session.state.actionTimeline.entries.map { it.fact }.map {
                when (it) {
                    is com.codex.campboardgamehost.clocktower.domain.ActionFact.RedHerringChoice ->
                        "red-herring-choice"
                    is com.codex.campboardgamehost.clocktower.domain.ActionFact.DemonBluffChoice ->
                        "demon-bluff-choice"
                    else -> "other"
                }
            },
        )
        session.commitRedHerringSeat(8, StorytellerPhase.FIRST_NIGHT, 1, 3)
        assertEquals(8, session.state.redHerringSeat)
        session.commitDemonBluffs(listOf(RoleId("Chef")), StorytellerPhase.FIRST_NIGHT, 1, 4)
        assertEquals(listOf(RoleId("Chef")), session.state.demonBluffRoleIds)
        val confirmed = session.state
        assertThrows(IllegalArgumentException::class.java) {
            session.commitRedHerringSeat(99, StorytellerPhase.FIRST_NIGHT, 1, 5)
        }
        assertThrows(IllegalArgumentException::class.java) {
            session.commitDemonBluffs(
                listOf(RoleId("Saint"), RoleId("Saint")), StorytellerPhase.FIRST_NIGHT, 1, 3,
            )
        }
        assertSame(confirmed, session.state)
        assertEquals(4L, session.state.playerInputRevision)
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
    fun `one Session state supplies identity revisions and chronology`() {
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

        val view = session.state

        assertEquals("observable-view", view.gameId)
        assertEquals(initialState.script, view.gameState.script)
        assertEquals(initialState.seed, view.gameSeed)
        assertEquals(2L, view.gameStateRevision)
        assertEquals(1L, view.playerInputRevision)
        assertEquals(ClocktowerSemanticHistoryMode.GLOBAL_V1, view.semanticHistoryMode)
        assertEquals(listOf("view-poison"), view.actionTimeline.entries.map { it.fact }.map { it.actionId })
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

    @Test fun `stale information response cannot mutate the single Session writer`() {
        val session = ClocktowerGameSession.createProduction("stale-reply", initialState.seed,
            initialState, semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1)
        val requested = StorytellerDecisionRevision(session.state.gameStateRevision, session.state.playerInputRevision)
        session.recordPlayerInput()
        val before = session.state
        assertThrows(IllegalStateException::class.java) {
            session.commitGlobalEpistemicObservation(globalPublicDraft("stale"), requested)
        }
        assertSame(before, session.state)
        session.commitGlobalEpistemicObservation(globalPublicDraft("fresh"),
            StorytellerDecisionRevision(before.gameStateRevision, before.playerInputRevision))
        assertEquals(listOf("fresh"), session.state.epistemicObservationLog.records.map { it.recordId })
        assertEquals(1L, session.state.nextTimelineGlobalSequence)
    }

    private fun newSession(): ClocktowerGameSession = ClocktowerGameSession.create(
        gameId = "game-2026-08-06-001",
        gameSeed = initialState.seed,
        rulesetRef = rulesetRef,
        initialState = initialState,
    )

    private fun globalPublicDraft(recordId: String): EpistemicObservationDraft = EpistemicObservationDraft(
        recordId = recordId,
        phase = StorytellerPhase.DAY,
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
