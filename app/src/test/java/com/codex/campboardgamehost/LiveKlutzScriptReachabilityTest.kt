package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.commitAcceptedTestAction

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.session.commitConfirmedPlayerDeath
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unlike historical synthetic TB+Klutz+Spy tests, these are derived from the actual
 * production script rosters and establish true reachable No Greater Joy mechanics.
 */
class LiveKlutzScriptReachabilityTest {
    private val ngj = ClocktowerScript.NoGreaterJoy
    private val tb = ClocktowerScript.TroubleBrewing
    private val definitions = clocktowerRoleDefinitionsForScript(ngj)
    private val defsByRole = definitions.associateBy { it.id.value }

    private fun state(
        script: ScriptId = ScriptId("no_greater_joy"),
        klutzAlive: Boolean = false,
        evilAlive: Boolean = true,
    ): GameState = GameState(
        script = script,
        seed = 75L,
        players = listOf(
            "Klutz", "Baron", "Artist", "Imp", "Empath",
        ).mapIndexed { i, name ->
            val role = requireNotNull(defsByRole[name])
            PlayerState(
                seat = i + 1, name = "Player ${i + 1}",
                actualRole = role.id, actualAlignment = role.alignment,
                actualType = role.type,
                alive = when (i) {
                    0 -> klutzAlive
                    1 -> evilAlive
                    else -> true
                },
            )
        },
    )

    @Test fun `production scripts prohibit synthetic TB Klutz Spy and NGJ Spy`() {
        val trouble = clocktowerRolesForScript(tb).mapTo(mutableSetOf()) { it.enName }
        val joy = clocktowerRolesForScript(ngj).mapTo(mutableSetOf()) { it.enName }
        assertTrue(setOf("Spy", "Recluse", "Virgin", "Slayer", "Imp").all { it in trouble })
        assertFalse("Klutz" in trouble)
        assertFalse("Artist" in trouble)
        assertTrue(setOf("Klutz", "Artist", "Imp", "Baron").all { it in joy })
        assertTrue(setOf("Spy", "Recluse", "Virgin", "Slayer", "Poisoner").none { it in joy })
        assertFalse("Klutz" in trouble && "Spy" in trouble)
        assertFalse("Klutz" in joy && "Spy" in joy)
    }

    @Test fun `canonical NGJ death provenance is recorded and does not substitute for learning time`() {
        val alive = state(klutzAlive = true)
        val session = ClocktowerGameSession.createProduction(
            gameId = "real-ngj-klutz", gameSeed = 75L, initialState = alive,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val death = session.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "death-at-night", StorytellerPhase.NIGHT, 2, 1, 1,
        ), 0L)
        val deathProof = (death.fact as ActionFact.Death).klutzDeathTrigger
        assertEquals(RoleId("Klutz"), deathProof?.actualRole)
        assertEquals(false, deathProof?.wasPoisoned)
    }
    private fun daySession(): ClocktowerGameSession {
        val s = ClocktowerGameSession.createProduction("ngj-real", 75L, state(klutzAlive = true), ClocktowerSemanticHistoryMode.GLOBAL_V1)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("first-day", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, StorytellerPhase.DAY, 1), s.state.gameStateRevision)
        return s
    }

    @Test fun `actual NGJ death directly triggers one public choice with no announcement event`() {
        val s = daySession()
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death("real-death", StorytellerPhase.DAY, 1, s.nextInputSequence, 1), s.state.gameStateRevision)
        assertEquals("real-death", s.state.pendingKlutzDeath()?.actionId)
        val before = s.state.actionTimeline.entries.size
        s.confirmKlutzChoice("real-death", 3)
        val accepted = s.state
        s.confirmKlutzChoice("real-death", 3)
        assertEquals(accepted, s.state)
        assertEquals(before + 1, s.state.actionTimeline.entries.size)
        assertEquals("real-death", (s.state.actionTimeline.entries.last().fact as ActionFact.KlutzChoice).deathActionId)
        assertEquals(null, s.state.pendingKlutzDeath())
        assertEquals(null, s.state.outcome)
    }

    @Test fun `choosing an evil player ends the game while a dead target cannot be chosen`() {
        val s = daySession()
        s.commitConfirmedPlayerDeath(ActionFactDraft.Death("klutz-death", StorytellerPhase.DAY, 1, s.nextInputSequence, 1), s.state.gameStateRevision)
        val before = s.state
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) { s.confirmKlutzChoice("klutz-death", 1) }
        assertEquals(before, s.state)
        s.confirmKlutzChoice("klutz-death", 2)
        assertEquals(GameEndReason.KLUTZ, s.state.outcome?.reason)
        assertEquals(StorytellerPhase.DAY, s.state.phase)
    }

    @Test fun `unrecorded dead Klutz does not fabricate a player action`() {
        val s = ClocktowerGameSession.createProduction("unknown", 75L, state(), ClocktowerSemanticHistoryMode.GLOBAL_V1)
        s.commitConfirmedPhaseAdvance(ActionFactDraft.PhaseAdvance("day", StorytellerPhase.FIRST_NIGHT, 1,
            s.nextInputSequence, StorytellerPhase.DAY, 1), 0L)
        assertEquals(null, s.state.pendingKlutzDeath())
    }
}
