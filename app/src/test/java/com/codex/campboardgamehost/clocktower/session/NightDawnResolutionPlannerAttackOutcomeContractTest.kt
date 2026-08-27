package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import org.junit.Assert.assertNull
import org.junit.Test

/** SNE-7.9C1: Dawn death planning must consume canonical Demon attack/safety facts. */
class NightDawnResolutionPlannerAttackOutcomeContractTest {
    @Test
    fun `canonical no death outcome blocks a caller supplied original death seat`() {
        val transition = NightDawnResolutionPlanner.planValidatedNightDeath(
            baseGameState = gameState(),
            checkpoint = checkpoint(confirmedMayorRedirectTarget = null),
            input = NightDawnDeathResolutionInput(
                originalDeathSeat = 3,
                mayorSeat = null,
                mayorRedirectMayApply = false,
                attackOutcome = DemonNightAttackOutcome.NO_DEATH,
                effectiveNightState = effectiveState(),
                demonRoleIds = setOf(RoleId("Imp")),
            ),
        )

        assertNull(transition.dawnCommitIntent?.death)
    }

    @Test
    fun `canonical Mayor redirect to Demon safe Soldier produces no Dawn death`() {
        val transition = NightDawnResolutionPlanner.planValidatedNightDeath(
            baseGameState = gameState(),
            checkpoint = checkpoint(confirmedMayorRedirectTarget = "Soldier"),
            input = NightDawnDeathResolutionInput(
                originalDeathSeat = 2,
                mayorSeat = 2,
                mayorRedirectMayApply = false,
                attackOutcome = DemonNightAttackOutcome.MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED,
                demonSafeSeats = setOf(3),
                effectiveNightState = effectiveState(),
                demonRoleIds = setOf(RoleId("Imp")),
            ),
        )

        assertNull(transition.dawnCommitIntent?.death)
    }

    private fun effectiveState() = ClocktowerEffectiveNightState(
        effectiveAliveSeats = setOf(1, 2, 3),
        effectiveRoleIdsBySeat = mapOf(
            1 to RoleId("Imp"),
            2 to RoleId("Mayor"),
            3 to RoleId("Soldier"),
        ),
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
                name = "Soldier",
                actualRole = RoleId("Soldier"),
                actualAlignment = Alignment.GOOD,
                actualType = CharacterType.TOWNSFOLK,
                alive = true,
            ),
        ),
        seed = 19L,
    )

    private fun checkpoint(
        confirmedMayorRedirectTarget: String?,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 15L,
        playerInputRevision = 9L,
        nightStarted = true,
        nightStepIndex = 4,
        confirmedAttackTarget = "Mayor",
        attackDraftTarget = "Mayor",
        confirmedPoisonTarget = null,
        poisonDraftTarget = null,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = confirmedMayorRedirectTarget,
        mayorRedirectDraftTarget = confirmedMayorRedirectTarget,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = null,
        confirmedDemonSuccessorTarget = null,
        nextTimelineGlobalSequence = 21L,
    )
}
