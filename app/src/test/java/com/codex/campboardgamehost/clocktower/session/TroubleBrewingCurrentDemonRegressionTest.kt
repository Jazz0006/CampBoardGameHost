package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import org.junit.Assert.assertEquals
import org.junit.Test

/** GCR-1 regressions for durable current-Demon identity after Imp succession. */
class TroubleBrewingCurrentDemonRegressionTest {
    @Test
    fun `dead historical Imp does not obscure live current Imp on next night`() {
        val state = GameState(
            script = ScriptId("Trouble Brewing"),
            players = listOf(
                player(
                    seat = 1,
                    name = "Imp0",
                    role = "Imp",
                    type = CharacterType.DEMON,
                    alive = false,
                ),
                player(
                    seat = 2,
                    name = "Imp1",
                    role = "Imp",
                    type = CharacterType.DEMON,
                    alive = true,
                ),
                player(
                    seat = 3,
                    name = "Poisoner",
                    role = "Poisoner",
                    type = CharacterType.MINION,
                    alive = true,
                ),
                player(
                    seat = 4,
                    name = "Washerwoman",
                    role = "Washerwoman",
                    type = CharacterType.TOWNSFOLK,
                    alive = true,
                    alignment = Alignment.GOOD,
                ),
            ),
            seed = 101L,
        )
        val checkpoint = checkpoint(confirmedAttackTarget = "Imp1")

        val resolution = resolveTroubleBrewingImpSelfKillSuccession(
            baseGameState = state,
            checkpoint = checkpoint,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(
            DemonSuccessionResolution.Choice(targetSeats = setOf(3)),
            resolution,
        )
    }

    @Test
    fun `repeated succession resolves second successor after two historical Imps`() {
        val state = GameState(
            script = ScriptId("Trouble Brewing"),
            players = listOf(
                player(
                    seat = 1,
                    name = "Imp0",
                    role = "Imp",
                    type = CharacterType.DEMON,
                    alive = false,
                ),
                player(
                    seat = 2,
                    name = "Imp1",
                    role = "Imp",
                    type = CharacterType.DEMON,
                    alive = false,
                ),
                player(
                    seat = 3,
                    name = "Imp2",
                    role = "Imp",
                    type = CharacterType.DEMON,
                    alive = true,
                ),
                player(
                    seat = 4,
                    name = "Baron",
                    role = "Baron",
                    type = CharacterType.MINION,
                    alive = true,
                ),
                player(
                    seat = 5,
                    name = "Washerwoman",
                    role = "Washerwoman",
                    type = CharacterType.TOWNSFOLK,
                    alive = true,
                    alignment = Alignment.GOOD,
                ),
            ),
            seed = 102L,
        )
        val checkpoint = checkpoint(confirmedAttackTarget = "Imp2")

        val resolution = resolveTroubleBrewingImpSelfKillSuccession(
            baseGameState = state,
            checkpoint = checkpoint,
            demonRoleId = RoleId("Imp"),
        )

        assertEquals(
            DemonSuccessionResolution.Choice(targetSeats = setOf(4)),
            resolution,
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

    private fun checkpoint(confirmedAttackTarget: String) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = confirmedAttackTarget,
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = "Poisoner",
        confirmedDemonSuccessorTarget = "Poisoner",
        nextTimelineGlobalSequence = 17L,
    )
}
