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

/** SNE-7.9A RED: Mayor redirect confirmation depends on upstream same-night mechanics. */
class MayorRedirectDependencyInvalidationTest {
    @Test
    fun `changed Poison confirmation invalidates Mayor redirect but preserves its draft`() {
        val reduced = NightCheckpointReducer.reduce(
            checkpoint(
                confirmedPoisonTarget = "Imp",
                poisonDraftTarget = "Monk",
                confirmedMayorRedirectTarget = "Empath",
                mayorRedirectDraftTarget = "Empath",
            ),
            NightResolutionEvent.ConfirmPoison,
        )

        assertEquals("Monk", reduced.confirmedPoisonTarget)
        assertNull(reduced.confirmedMayorRedirectTarget)
        assertEquals("Empath", reduced.mayorRedirectDraftTarget)
    }

    @Test
    fun `changed Monk confirmation invalidates Mayor redirect but preserves its draft`() {
        val reduced = NightCheckpointReducer.reduce(
            checkpoint(
                confirmedMonkTarget = "Empath",
                monkDraftTarget = "Mayor",
                confirmedMayorRedirectTarget = "Empath",
                mayorRedirectDraftTarget = "Empath",
            ),
            NightResolutionEvent.ConfirmMonkProtection,
        )

        assertEquals("Mayor", reduced.confirmedMonkTarget)
        assertNull(reduced.confirmedMayorRedirectTarget)
        assertEquals("Empath", reduced.mayorRedirectDraftTarget)
    }

    @Test
    fun `changed Demon attack confirmation invalidates Mayor redirect but preserves its draft`() {
        val reduced = NightCheckpointReducer.reduce(
            checkpoint(
                confirmedAttackTarget = "Mayor",
                attackDraftTarget = "Empath",
                confirmedMayorRedirectTarget = "Empath",
                mayorRedirectDraftTarget = "Empath",
            ),
            NightResolutionEvent.ConfirmDemonAttack,
        )

        assertEquals("Empath", reduced.confirmedAttackTarget)
        assertNull(reduced.confirmedMayorRedirectTarget)
        assertEquals("Empath", reduced.mayorRedirectDraftTarget)
    }

    @Test
    fun `idempotent upstream reconfirmations preserve Mayor redirect confirmation`() {
        val original = checkpoint(
            confirmedAttackTarget = "Mayor",
            attackDraftTarget = "Mayor",
            confirmedPoisonTarget = "Empath",
            poisonDraftTarget = "Empath",
            confirmedMonkTarget = "Monk",
            monkDraftTarget = "Monk",
            confirmedMayorRedirectTarget = "Empath",
            mayorRedirectDraftTarget = "Empath",
        )

        val afterAttack = NightCheckpointReducer.reduce(original, NightResolutionEvent.ConfirmDemonAttack)
        val afterPoison = NightCheckpointReducer.reduce(afterAttack, NightResolutionEvent.ConfirmPoison)
        val afterMonk = NightCheckpointReducer.reduce(afterPoison, NightResolutionEvent.ConfirmMonkProtection)

        assertEquals("Empath", afterMonk.confirmedMayorRedirectTarget)
        assertEquals("Empath", afterMonk.mayorRedirectDraftTarget)
    }

    @Test
    fun `changing Monk protection to Mayor prevents stale redirect from moving Dawn death to old target`() {
        val reduced = NightCheckpointReducer.reduce(
            checkpoint(
                confirmedAttackTarget = "Mayor",
                attackDraftTarget = "Mayor",
                confirmedMonkTarget = "Empath",
                monkDraftTarget = "Mayor",
                confirmedMayorRedirectTarget = "Empath",
                mayorRedirectDraftTarget = "Empath",
            ),
            NightResolutionEvent.ConfirmMonkProtection,
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
