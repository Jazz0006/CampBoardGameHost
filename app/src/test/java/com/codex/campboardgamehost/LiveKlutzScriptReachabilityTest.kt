package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.commitAcceptedTestAction

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.NoGreaterJoyKlutzHistoryProducerV1
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
    @Test fun `actual NGJ announcement then public choice is globally ordered`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-learn-real", gameSeed = 75L,
            initialState = state(klutzAlive = true),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "night-death", StorytellerPhase.NIGHT, 2, 1, 1,
        ), 0L)
        val learn = requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "learn-public", 2, 2,
        ))
        assertEquals("night-death", learn.deathActionId)
        assertTrue(learn.functioningWhenLearned)
        session.commitAcceptedTestAction(learn)
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "duplicate-learn", 2, 3,
        ))
        val chosen = requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 2, "public-choice", 2, 3,
        ))
        assertEquals("learn-public", chosen.learnedActionId)
        session.commitAcceptedTestAction(chosen)
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 3, "duplicate-choice", 2, 4,
        ))
        val timeline = session.state.actionTimeline
        assertEquals(listOf("night-death", "learn-public", "public-choice"),
            timeline.entries.map { it.fact.actionId })
    }

    @Test fun `legacy NGJ death with no predeath provenance stays unknown and cannot invent learned event`() {
        val game = state(klutzAlive = false)
        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-legacy-unknown", gameSeed = 75L, initialState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitAcceptedTestAction(ActionFactDraft.Death(
            "legacy-no-proof", StorytellerPhase.NIGHT, 2, 1, 1,
        ))
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "invented-learn", 2, 2,
        ))
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 2, "invented-choice", 2, 3,
        ))
    }

    @Test fun `actual chosen seat is recorded without Host team or alive rule adjudication`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-choice-fact", gameSeed = 75L,
            initialState = state(klutzAlive = true, evilAlive = false),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitConfirmedPlayerDeath(ActionFactDraft.Death(
            "klutz-night", StorytellerPhase.NIGHT, 2, 1, 1,
        ), 0L)
        session.commitAcceptedTestAction(requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "learn", 2, 2,
        )))
        val choice = requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 2, "actual-choice", 2, 3,
        ))
        assertEquals(2, choice.chosenSeat)
        assertEquals("learn", choice.learnedActionId)
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 99, "unknown-seat", 2, 4,
        ))
    }

}
