package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** P1 hotfix: Dawn must retain an explicit poison-clear materialization intent. */
class NightDawnPoisonClearIntentTest {
    @Test
    fun `Poisoner becoming Imp keeps an explicit Dawn poison clear intent`() {
        val transition = NightDawnResolutionPlanner.confirmNewDemonIdentity(
            baseGameState = gameState(),
            checkpoint = checkpoint(),
            demonRoleId = RoleId("Imp"),
            poisonResolutionInput = NightDawnPoisonResolutionInput(
                poisonerSeat = 2,
                poisonerRoleId = RoleId("Poisoner"),
                effectiveNightState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = setOf(2, 3),
                    effectiveRoleIdsBySeat = mapOf(
                        2 to RoleId("Poisoner"),
                        3 to RoleId("Monk"),
                    ),
                ),
            ),
        )

        val intent = requireNotNull(transition.dawnCommitIntent)
        assertEquals(2, intent.roleChanges.single().targetSeat)
        val poison = assertNotNull(intent.poisonCarry).let { requireNotNull(intent.poisonCarry) }
        assertEquals(3, poison.previousTargetSeat)
        assertNull(poison.targetSeat)
    }

    private fun gameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            PlayerState(
                seat = 1,
                name = "Imp",
                actualRole = RoleId("Imp"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.DEMON,
                alive = false,
            ),
            PlayerState(
                seat = 2,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = true,
            ),
            PlayerState(
                seat = 3,
                name = "Monk",
                actualRole = RoleId("Monk"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 17L,
    )

    private fun checkpoint() = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = "Imp",
        attackDraftTarget = "Imp",
        confirmedPoisonTarget = "Monk",
        poisonDraftTarget = "Monk",
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = "Poisoner",
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = "Poisoner",
        confirmedDemonSuccessorTarget = "Poisoner",
        nextTimelineGlobalSequence = 17L,
    )
}
