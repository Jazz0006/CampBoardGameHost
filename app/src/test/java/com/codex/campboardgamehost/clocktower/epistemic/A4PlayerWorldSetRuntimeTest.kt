package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class A4PlayerWorldSetRuntimeTest {
    @Test fun `snapshot entry point uses player knowledge and stays enumerated by default`() {
        val snapshot = snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val knowledge = knowledge(formal)

        val result = A4PlayerWorldSetRuntime().build(
            snapshot, StorytellerPhase.FIRST_NIGHT, 1, knowledge,
            EpistemicHypothesis.MECHANICALLY_CREDIBLE, TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(result.selected is EnumeratedWorldSet)
        assertNull(result.zddShadow)
        assertEquals(1, result.telemetry.size)
        assertEquals(A4WorldSetRepresentation.ENUMERATED, result.telemetry.single().representation)
    }

    @Test fun `shadow mode measures matching ZDD without selecting it`() {
        val snapshot = snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val result = A4PlayerWorldSetRuntime(
            A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_SHADOW),
        ).build(
            snapshot, StorytellerPhase.FIRST_NIGHT, 1, knowledge(formal),
            EpistemicHypothesis.MECHANICALLY_CREDIBLE, TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(result.selected is EnumeratedWorldSet)
        assertTrue(result.zddShadow is ZddPlayerWorldSet)
        assertEquals(result.selected.cardinality(), result.zddShadow?.cardinality())
        assertEquals(setOf(A4WorldSetRepresentation.ENUMERATED, A4WorldSetRepresentation.ZDD),
            result.telemetry.map { it.representation }.toSet())
    }

    @Test fun `factory knowledge keeps the returned shadow identity bound to the cache key identity`() {
        val snapshot = snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val knowledge = A4PlayerKnowledgeFactory.createAll(
            formal,
            formal.players.associate { it.seat to (it.shownRole ?: it.actualRole) },
            emptyList(),
        ).first()

        val result = A4PlayerWorldSetRuntime(A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_SHADOW)).build(
            formal, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE, TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertEquals(
            PlayerWorldSetIdentity.create(formal.rulesetRef, knowledge, EpistemicHypothesis.MECHANICALLY_CREDIBLE),
            result.zddShadow?.identity,
        )
    }

    @Test fun `device validated mode selects direct ZDD without constructing enumerated baseline`() {
        val snapshot = snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val result = A4PlayerWorldSetRuntime(
            A4WorldEngineRuntimePolicy(A4WorldEngineRollout.ZDD_DEVICE_VALIDATED),
        ).build(
            snapshot, StorytellerPhase.FIRST_NIGHT, 1, knowledge(formal),
            EpistemicHypothesis.MECHANICALLY_CREDIBLE, TroubleBrewingFixtures.fullRoleDefinitions(),
        )

        assertTrue(result.selected is ZddPlayerWorldSet)
        assertNull(result.zddShadow)
        assertEquals(listOf(A4WorldSetRepresentation.ZDD), result.telemetry.map { it.representation })
    }

    private fun knowledge(formal: FormalGameState) = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = "runtime-player-one",
        formalSnapshotId = formal.snapshotId,
        recipientSeat = 1,
        perceivedRole = RoleId("Chef"),
    )

    private fun snapshot(): GameSnapshot {
        val roles = listOf("Chef", "Empath", "Washerwoman", "Poisoner", "Imp")
        return GameSnapshot(
            gameId = "runtime-game",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = 7L,
            rulesetRef = RulesetRef(ScriptId("trouble_brewing"), "0123456789abcdef0123456789abcdef", "runtime", "official", RuleCoverage.VERIFIED),
            gameState = GameState(
                ScriptId("trouble_brewing"),
                roles.mapIndexed { index, role -> PlayerState(index + 1, "P${index + 1}", RoleId(role),
                    if (role in setOf("Poisoner", "Imp")) com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL else com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD,
                    when (role) {
                        "Poisoner" -> com.codex.campboardgamehost.clocktower.domain.CharacterType.MINION
                        "Imp" -> com.codex.campboardgamehost.clocktower.domain.CharacterType.DEMON
                        else -> com.codex.campboardgamehost.clocktower.domain.CharacterType.TOWNSFOLK
                    }) },
                7L,
            ),
        )
    }
}
