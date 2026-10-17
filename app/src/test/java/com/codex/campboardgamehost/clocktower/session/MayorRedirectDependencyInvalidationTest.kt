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

/** A changed upstream confirmation must not reuse a stale Mayor redirect at Dawn. */
class MayorRedirectDependencyInvalidationTest {
    @Test
    fun `changing Monk protection to Mayor prevents stale redirect from moving Dawn death to old target`() {
        val targets = ClocktowerNightTargets(attackSeat = 2, monkSeat = 3, mayorRedirectSeat = 3)
            .withProtection(2)
        val reduced = checkpoint(
            confirmedAttackTarget = "Mayor",
            confirmedMonkTarget = targets.monkSeat?.let { gameState().playerAt(it)?.name },
            confirmedMayorRedirectTarget = targets.mayorRedirectSeat?.let { gameState().playerAt(it)?.name },
            mayorRedirectDraftTarget = "Empath",
        )

        val transition = NightDawnResolutionPlanner.planValidatedNightDeath(
            baseGameState = gameState(),
            checkpoint = reduced,
            input = NightDawnDeathResolutionInput(
                originalDeathSeat = 2,
                mayorSeat = 2,
                mayorRedirectMayApply = true,
                effectiveNightState = ClocktowerEffectiveNightState(
                    effectiveAliveSeats = setOf(1, 2, 3, 4),
                    effectiveRoleIdsBySeat = mapOf(
                        1 to RoleId("Imp"),
                        2 to RoleId("Mayor"),
                        3 to RoleId("Empath"),
                        4 to RoleId("Monk"),
                    ),
                ),
                demonRoleIds = setOf(RoleId("Imp")),
            ),
        )

        assertNull(reduced.confirmedMayorRedirectTarget)
        assertEquals("Empath", reduced.mayorRedirectDraftTarget)
        assertEquals(2, transition.dawnCommitIntent?.death?.targetSeat)
    }

    private fun checkpoint(
        confirmedAttackTarget: String? = "Mayor",
        attackDraftTarget: String? = confirmedAttackTarget,
        confirmedPoisonTarget: String? = null,
        poisonDraftTarget: String? = confirmedPoisonTarget,
        confirmedMonkTarget: String? = null,
        monkDraftTarget: String? = confirmedMonkTarget,
        confirmedMayorRedirectTarget: String? = null,
        mayorRedirectDraftTarget: String? = confirmedMayorRedirectTarget,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = attackDraftTarget,
        confirmedPoisonTarget = confirmedPoisonTarget,
        poisonDraftTarget = poisonDraftTarget,
        confirmedMonkTarget = confirmedMonkTarget,
        monkDraftTarget = monkDraftTarget,
        confirmedMayorRedirectTarget = confirmedMayorRedirectTarget,
        mayorRedirectDraftTarget = mayorRedirectDraftTarget,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = null,
        confirmedDemonSuccessorTarget = null,
        nextTimelineGlobalSequence = 17L,
    )

    private fun gameState() = GameState(
        script = ScriptId("Trouble Brewing"),
        players = listOf(
            PlayerState(
                seat = 1,
                name = "Imp",
                actualRole = RoleId("Imp"),
                actualAlignment = Alignment.EVIL,
                actualType = CharacterType.DEMON,
                alive = true,
            ),
            PlayerState(
                seat = 2,
                name = "Mayor",
                actualRole = RoleId("Mayor"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
            PlayerState(
                seat = 3,
                name = "Empath",
                actualRole = RoleId("Empath"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
            PlayerState(
                seat = 4,
                name = "Monk",
                actualRole = RoleId("Monk"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 19L,
    )
}
