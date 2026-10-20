package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.toClocktowerGameState
import com.codex.campboardgamehost.clocktower.flow.ClocktowerNightFlowPhase
import com.codex.campboardgamehost.clocktower.flow.ClocktowerProductionNightStepIdentity
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import com.codex.campboardgamehost.clocktower.session.ClocktowerGameSession
import com.codex.campboardgamehost.clocktower.session.commitConfirmedPlayerDeath
import com.codex.campboardgamehost.clocktower.session.commitConfirmedActualRoleChange
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactDraft
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ClocktowerNightHostProjectionTest {
    @Test
    fun `unconfirmed attack must not wake Ravenkeeper or suppress surviving actor`() {
        val cards = listOf(
            card("Imp", "Imp", ClocktowerTeam.Demon),
            card("Poisoner", "Poisoner", ClocktowerTeam.Minion),
            card("Ravenkeeper", "Ravenkeeper", ClocktowerTeam.Townsfolk),
            card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            card("Chef", "Chef", ClocktowerTeam.Townsfolk),
        )
        val projection = ClocktowerNightHostProjectionFactory.project(
            cards = cards,
            gameState = cards.toClocktowerGameState(ClocktowerScript.TroubleBrewing, 17L),
            ruleset = builtInRuleset(),
            phase = ClocktowerPhase.Night,
            poisonTarget = null,
            checkpoint = checkpoint(confirmedAttackTarget = "Ravenkeeper"),
            pendingNightNewDemonIdentityName = null,
            lastExecutedName = null,
        )
        val impInteractionId = ClocktowerProductionNightStepIdentity
            .role(RoleId("Imp"))
            .interactionId(ClocktowerNightFlowPhase.OTHER_NIGHT)

        assertEquals(null, projection.ravenkeeperTrigger)
        assertEquals(null, projection.sageNightDeath)
        assertEquals(null, projection.resolvedDeathForRemainingWakeups)
        assertTrue(projection.otherNightInteractions.any { it.id == impInteractionId })
        assertTrue(projection.chambermaidTargetCards.any { it.name == "Ravenkeeper" })
        assertTrue(projection.effectiveAbilitySubjectForRole("Ravenkeeper", cards[2])?.isAlive == true)
        assertEquals(RoleId("Ravenkeeper").value,
            projection.effectiveAbilitySubjectForRole("Ravenkeeper", cards[2])?.actualRole)
        assertEquals(null, projection.ravenkeeperDeathTriggerAbilityState)

    }

    @Test
    fun `confirmed Session death wakes Ravenkeeper even though no longer alive`() {
        val cards = listOf(
            card("Imp", "Imp", ClocktowerTeam.Demon),
            card("Poisoner", "Poisoner", ClocktowerTeam.Minion),
            card("Ravenkeeper", "Ravenkeeper", ClocktowerTeam.Townsfolk),
            card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            card("Chef", "Chef", ClocktowerTeam.Townsfolk),
        )
        val game = cards.toClocktowerGameState(ClocktowerScript.TroubleBrewing, 17L)
        val deathConfirmed = game.copy(players = game.players.map {
            if (it.seat == 3) it.copy(alive = false) else it
        })
        val projection = ClocktowerNightHostProjectionFactory.project(
            cards = cards,
            gameState = deathConfirmed,
            ruleset = builtInRuleset(),
            phase = ClocktowerPhase.Night,
            poisonTarget = null,
            checkpoint = checkpoint("Ravenkeeper").copy(
                acceptedOperationKinds = setOf(com.codex.campboardgamehost.clocktower.session.NightOperationKind.ATTACK),
            ),
            pendingNightNewDemonIdentityName = null,
            lastExecutedName = null,
            confirmedNightDeathSeats = listOf(3),
        )
        assertEquals("Ravenkeeper", projection.ravenkeeperTrigger?.name)
        assertEquals("Ravenkeeper", projection.resolvedDeathForRemainingWakeups)
        assertFalse(projection.chambermaidTargetCards.any { it.name == "Ravenkeeper" })
        assertTrue(projection.otherNightInteractions.any {
            it.roleId == RoleId("Ravenkeeper")
        })
    }

    @Test
    fun `night base alive and role maps are read from canonical GameState, not stale cards`() {
        val cards = listOf(
            card("Imp", "Imp", ClocktowerTeam.Demon),
            card("Poisoner", "Poisoner", ClocktowerTeam.Minion),
            card("Ravenkeeper", "Ravenkeeper", ClocktowerTeam.Townsfolk),
            card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            card("Chef", "Chef", ClocktowerTeam.Townsfolk),
        )
        val accepted = cards.toClocktowerGameState(ClocktowerScript.TroubleBrewing, 17L)
            .let { game ->
                game.copy(players = game.players.map { player ->
                    if (player.seat == 4) player.copy(actualRole = RoleId("Chef"), alive = false) else player
                })
            }
        // The deliberately stale UI cards still show Empath alive in seat 4.
        val projection = ClocktowerNightHostProjectionFactory.project(
            cards = cards,
            gameState = accepted,
            ruleset = builtInRuleset(),
            phase = ClocktowerPhase.Night,
            poisonTarget = null,
            checkpoint = checkpoint(confirmedAttackTarget = "Ravenkeeper"),
            pendingNightNewDemonIdentityName = null,
            lastExecutedName = null,
        )
        assertFalse(projection.chambermaidTargetCards.any { it.name == "Empath" })
        val subject = requireNotNull(projection.effectiveAbilitySubjectForRole("Empath", cards[3]))
        assertFalse(subject.isAlive)
        assertEquals("Chef", subject.actualRole)

    }

    @Test
    fun `successor selection and back navigation never create death or role facts`() {
        val cards = listOf(
            card("Imp", "Imp", ClocktowerTeam.Demon),
            card("Poisoner", "Poisoner", ClocktowerTeam.Minion),
            card("Empath", "Empath", ClocktowerTeam.Townsfolk),
            card("Chef", "Chef", ClocktowerTeam.Townsfolk),
            card("Soldier", "Soldier", ClocktowerTeam.Townsfolk),
        )
        val game = cards.toClocktowerGameState(ClocktowerScript.TroubleBrewing, 17L)
        val session = ClocktowerGameSession.createProduction(
            "night-canonical", game.seed, game,
            com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode.GLOBAL_V1,
        )
        session.commitConfirmedGameAction(
            ActionFactDraft.Attack("self-attack", StorytellerPhase.NIGHT, 2, 1, 1), game, 0L,
        )
        val acceptedSelection = session.state
        val selectedCheckpoint = checkpoint("Imp")
        val initialProjection = ClocktowerNightHostProjectionFactory.project(
            cards, session.state.gameState, builtInRuleset(), ClocktowerPhase.Night, null,
            selectedCheckpoint, null, null,
        )
        assertTrue(initialProjection.chambermaidTargetCards.any { it.name == "Imp" })
        assertEquals("Poisoner", initialProjection.effectiveAbilitySubjectForRole("Poisoner", cards[1])?.actualRole)
        assertEquals(acceptedSelection, session.state)
        assertEquals(listOf("self-attack"), session.state.actionTimeline.entries.map { it.fact.actionId })
        assertTrue(session.state.gameState.playerAt(1)?.alive == true)
        assertEquals(RoleId("Poisoner"), session.state.gameState.playerAt(2)?.actualRole)

        val death = ActionFactDraft.Death("actual-death", StorytellerPhase.NIGHT, 2, 2, 1)
        session.commitConfirmedPlayerDeath(death, session.state.gameStateRevision)
        val role = ActionFactDraft.RoleChange(
            "actual-role", StorytellerPhase.NIGHT, 2, 3, 2, RoleId("Imp"), Alignment.EVIL, CharacterType.DEMON,
        )
        session.commitConfirmedActualRoleChange(role, session.state.gameStateRevision)
        val committed = session.state
        // Stale cards and old navigation/selection cannot resurrect or reassign committed facts.
        val projection = ClocktowerNightHostProjectionFactory.project(
            cards, session.state.gameState, builtInRuleset(), ClocktowerPhase.Night, null,
            selectedCheckpoint, null, null,
        )
        assertFalse(projection.chambermaidTargetCards.any { it.name == "Imp" })
        assertEquals("Imp", projection.effectiveAbilitySubjectForRole("Poisoner", cards[1])?.actualRole)
        session.commitConfirmedPlayerDeath(death, 1L)
        session.commitConfirmedActualRoleChange(role, 2L)
        assertEquals(committed, session.state)
        assertEquals(listOf(0L, 1L, 2L), session.state.actionTimeline.entries.map { it.point.globalSequence })
        assertTrue(session.state.actionTimeline.entries[1].fact is ActionFact.Death)
        assertTrue(session.state.actionTimeline.entries[2].fact is ActionFact.RoleChange)
    }

    private fun builtInRuleset() = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets", assetPath).readText(Charsets.UTF_8)
    }.ruleset(ClocktowerScript.TroubleBrewing)

    private fun checkpoint(confirmedAttackTarget: String?) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 2,
        gameStateRevision = 1L,
        playerInputRevision = 1L,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = confirmedAttackTarget,
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
    )

    private fun card(name: String, roleName: String, team: ClocktowerTeam) = PlayerCard(
        name = name,
        clocktowerTeam = team,
        clocktowerRole = ClocktowerRole(
            team = team,
            zhName = roleName,
            enName = roleName,
            zhDescription = "",
            enDescription = "",
        ),
    )
}
