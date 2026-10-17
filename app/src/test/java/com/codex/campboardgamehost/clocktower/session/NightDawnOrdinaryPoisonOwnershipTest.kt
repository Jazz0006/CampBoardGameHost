package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Ordinary Dawn preserves the confirmed target when deciding poison expiry. */
class NightDawnOrdinaryPoisonOwnershipTest {
    @Test
    fun `dead Poisoner ordinary Dawn produces an explicit clear`() {
        val poisonIntent = requireNotNull(
            NightDawnResolutionPlanner.planPoisonCarry(
                baseGameState = gameState(),
                checkpoint = checkpoint(),
                input = deadPoisonerInput(),
            ),
        )
        assertEquals(EMPATH_SEAT, poisonIntent.previousTargetSeat)
        assertNull(poisonIntent.targetSeat)

    }

    private fun deadPoisonerInput() = NightDawnPoisonResolutionInput(
        poisonerSeat = POISONER_SEAT,
        poisonerRoleId = RoleId("Poisoner"),
        effectiveNightState = ClocktowerEffectiveNightState(
            effectiveAliveSeats = setOf(EMPATH_SEAT),
            effectiveRoleIdsBySeat = mapOf(
                POISONER_SEAT to RoleId("Poisoner"),
                EMPATH_SEAT to RoleId("Empath"),
            ),
        ),
    )

    private fun gameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            PlayerState(
                seat = POISONER_SEAT,
                name = "Poisoner",
                actualRole = RoleId("Poisoner"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.MINION,
                alive = false,
            ),
            PlayerState(
                seat = EMPATH_SEAT,
                name = "Empath",
                actualRole = RoleId("Empath"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 29L,
    )

    private fun checkpoint() = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = ROUND,
        gameStateRevision = 8L,
        playerInputRevision = 5L,
        nightStarted = true,
        nightStepIndex = 3,
        confirmedAttackTarget = "Poisoner",
        attackDraftTarget = "Poisoner",
        confirmedPoisonTarget = "Empath",
        poisonDraftTarget = "Empath",
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = null,
        confirmedDemonSuccessorTarget = null,
        nextTimelineGlobalSequence = 11L,
    )

    private companion object {
        const val ROUND = 4
        const val POISONER_SEAT = 1
        const val EMPATH_SEAT = 2
    }
}
