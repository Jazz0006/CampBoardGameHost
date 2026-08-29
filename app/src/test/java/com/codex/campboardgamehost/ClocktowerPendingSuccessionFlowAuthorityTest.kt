package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.session.ClocktowerNightCheckpoint
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerPendingSuccessionFlowAuthorityTest {
    @Test
    fun `pending Imp identity keeps canonical successor requirement after old Imp is dead`() {
        val state = GameState(
            script = ScriptId("Trouble Brewing"),
            players = listOf(
                player(1, "Imp0", "Imp", CharacterType.DEMON, alive = false),
                player(2, "Poisoner", "Poisoner", CharacterType.MINION, alive = true),
                player(3, "Empath", "Empath", CharacterType.TOWNSFOLK, alive = true, alignment = Alignment.GOOD),
                player(4, "Monk", "Monk", CharacterType.TOWNSFOLK, alive = true, alignment = Alignment.GOOD),
                player(5, "Chef", "Chef", CharacterType.TOWNSFOLK, alive = true, alignment = Alignment.GOOD),
            ),
            seed = 29L,
        )
        val checkpoint = ClocktowerNightCheckpoint(
            phaseName = "Night",
            round = 3,
            gameStateRevision = 12L,
            playerInputRevision = 7L,
            nightStarted = true,
            nightStepIndex = 4,
            confirmedAttackTarget = "Imp0",
            attackDraftTarget = "Imp0",
            confirmedPoisonTarget = null,
            poisonDraftTarget = null,
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

        assertEquals(
            DemonSuccessionResolution.Choice(targetSeats = setOf(2)),
            resolveNightDemonSuccessionForHost(
                baseGameState = state,
                checkpoint = checkpoint,
                currentDemonHostContext = null,
                demonRoleId = RoleId("Imp"),
            ),
        )
    }

    private fun player(
        seat: Int,
        name: String,
        role: String,
        type: CharacterType,
        alive: Boolean,
        alignment: Alignment = Alignment.EVIL,
    ) = PlayerState(
        seat = seat,
        name = name,
        actualRole = RoleId(role),
        actualAlignment = alignment,
        actualType = type,
        alive = alive,
    )
}
