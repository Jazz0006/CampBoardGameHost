package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.rules.NoGreaterJoyKlutzChoiceRuleV1
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.NoGreaterJoyKlutzHistoryProducerV1
import com.codex.campboardgamehost.clocktower.session.synchronizePlayerDeathWithinCurrentRevision
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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

    @Test fun `real NGJ Klutz chooses evil good loses or chooses good continues`() {
        val game = state()
        val evil = NoGreaterJoyKlutzChoiceRuleV1.resolve(game, 1, 2, definitions)
        assertTrue(evil.evilWins)
        assertEquals(Alignment.EVIL, evil.chosenActualAlignment)
        val good = NoGreaterJoyKlutzChoiceRuleV1.resolve(game, 1, 3, definitions)
        assertFalse(good.evilWins)
        assertEquals(Alignment.GOOD, good.chosenActualAlignment)
    }

    @Test fun `real NGJ Klutz requires dead actual role and living actual target`() {
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(state(klutzAlive = true), 1, 2, definitions)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(state(evilAlive = false), 1, 2, definitions)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(state(), 1, 1, definitions)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(state(), 4, 2, definitions)
        }
    }

    @Test fun `legacy synthetic TB Klutz Spy and extra poisoner are not production evidence`() {
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(
                state(script = ScriptId("trouble_brewing")), 1, 2, definitions,
            )
        }
        val withSpy = state().copy(players = state().players.map {
            if (it.seat == 2) it.copy(actualRole = RoleId("Spy")) else it
        })
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(withSpy, 1, 2, definitions)
        }
        val fakeRoles = definitions + RoleDefinition(
            RoleId("Poisoner"), Alignment.EVIL, CharacterType.MINION,
            setOf(ScriptId("no_greater_joy")),
        )
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzChoiceRuleV1.resolve(state(), 1, 2, fakeRoles)
        }
    }

    @Test fun `canonical NGJ death provenance persists and does not substitute for learning time`() {
        val alive = state(klutzAlive = true)
        val session = ClocktowerGameSession.createProduction(
            gameId = "real-ngj-klutz", gameSeed = 75L, initialState = alive,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        val death = session.commitGlobalActionFact(ActionFactDraft.Death(
            "death-at-night", StorytellerPhase.DAWN, 2, 1, 1,
        ))
        val deathProof = (death.fact as ActionFact.Death).klutzDeathTrigger
        assertEquals(RoleId("Klutz"), deathProof?.actualRole)
        assertEquals(false, deathProof?.wasPoisoned)
        session.synchronizePlayerDeathWithinCurrentRevision(1)
        val archive = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(session.state.actionTimeline)
        val restored = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
            org.json.JSONObject().put(ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, archive),
        )
        assertEquals(session.state.actionTimeline, restored)
        // The independently rule-owned choice occurs when the dead Klutz learns of death.
        assertTrue(NoGreaterJoyKlutzChoiceRuleV1.resolve(
            session.state.gameState, 1, 2, definitions,
        ).evilWins)
    }
    @Test fun `actual NGJ announcement then public choice is globally ordered and persisted`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-learn-real", gameSeed = 75L,
            initialState = state(klutzAlive = true),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalActionFact(ActionFactDraft.Death(
            "night-death", StorytellerPhase.DAWN, 2, 1, 1,
        ))
        session.synchronizePlayerDeathWithinCurrentRevision(1)
        val learn = requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "learn-public", 2, 2,
        ))
        assertEquals("night-death", learn.deathActionId)
        assertTrue(learn.functioningWhenLearned)
        session.commitGlobalActionFact(learn)
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "duplicate-learn", 2, 3,
        ))
        val chosen = requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 2, "public-choice", 2, 3,
        ))
        assertEquals("learn-public", chosen.learnedActionId)
        session.commitGlobalActionFact(chosen)
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 3, "duplicate-choice", 2, 4,
        ))
        val timeline = session.state.actionTimeline
        assertEquals(listOf("night-death", "learn-public", "public-choice"),
            timeline.entries.map { it.fact.actionId })
        val archive = ClocktowerSemanticHistoryPersistence.encodeActionTimeline(timeline)
        val recovered = ClocktowerSemanticHistoryPersistence.decodeActionTimeline(
            org.json.JSONObject().put(ClocktowerSemanticHistoryPersistence.ACTION_TIMELINE_KEY, archive),
        )
        assertEquals(timeline, recovered)
        val public = com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalTimeline.project(
            recipientSeat = 3, actionTimeline = recovered,
            observationLog = com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog(),
        )
        assertTrue(public.none { it.actionIdOrNull() == "learn-public" })
        assertTrue(public.any {
            it is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicKlutzChoice &&
                it.klutzSeat == 1 && it.chosenSeat == 2
        })
    }

    private fun com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.actionIdOrNull(): String? =
        when (this) {
            is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicDeath -> actionId
            is com.codex.campboardgamehost.clocktower.epistemic.PlayerHistoricalEvent.PublicKlutzChoice -> actionId
            else -> null
        }

    @Test fun `legacy NGJ death with no predeath provenance stays unknown and cannot invent learned event`() {
        val game = state(klutzAlive = false)
        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-legacy-unknown", gameSeed = 75L, initialState = game,
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalActionFact(ActionFactDraft.Death(
            "legacy-no-proof", StorytellerPhase.DAWN, 2, 1, 1,
        ))
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "invented-learn", 2, 2,
        ))
        assertEquals(null, NoGreaterJoyKlutzHistoryProducerV1.choice(
            session.state, 1, 2, "invented-choice", 2, 3,
        ))
    }

    @Test fun `Klutz cannot record a chosen dead player even after valid learned action`() {
        val session = ClocktowerGameSession.createProduction(
            gameId = "ngj-chosen-dead", gameSeed = 75L,
            initialState = state(klutzAlive = true, evilAlive = false),
            semanticHistoryMode = ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitGlobalActionFact(ActionFactDraft.Death(
            "klutz-night", StorytellerPhase.DAWN, 2, 1, 1,
        ))
        session.synchronizePlayerDeathWithinCurrentRevision(1)
        session.commitGlobalActionFact(requireNotNull(NoGreaterJoyKlutzHistoryProducerV1.learned(
            session.state, 1, "learn", 2, 2,
        )))
        assertThrows(IllegalArgumentException::class.java) {
            NoGreaterJoyKlutzHistoryProducerV1.choice(session.state, 1, 2, "invalid-choice", 2, 3)
        }
    }

}
