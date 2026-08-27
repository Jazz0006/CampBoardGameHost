package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.flow.ClocktowerInteractionId
import com.codex.campboardgamehost.clocktower.rules.AbilitySubject
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightCursor
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightState
import com.codex.campboardgamehost.clocktower.rules.ClocktowerEffectiveNightStateProjector
import com.codex.campboardgamehost.clocktower.rules.ClocktowerInteractionBoundary
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackContext
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackOutcome
import com.codex.campboardgamehost.clocktower.rules.DemonNightAttackSemantics
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionContext
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionResolution
import com.codex.campboardgamehost.clocktower.rules.DemonSuccessionSemantics
import com.codex.campboardgamehost.clocktower.rules.PoisonEffectLifecycle
import com.codex.campboardgamehost.clocktower.rules.ResolvedNightMechanicalEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SNE-7 behavior matrix that composes existing typed seams without inspecting production source.
 *
 * These contracts intentionally stop before the not-yet-extracted Dawn planner. They lock the
 * checkpoint/reducer/projector/rule semantics that the planner must consume rather than duplicate.
 */
class SNE7NightTransactionBehaviorMatrixTest {
    private val poisonerInteraction = ClocktowerInteractionId("other_night:role:Poisoner")
    private val impInteraction = ClocktowerInteractionId("other_night:role:Imp")
    private val successorInteraction = ClocktowerInteractionId("other_night:event:imp:demon_successor")
    private val fortuneTellerInteraction = ClocktowerInteractionId("other_night:role:Fortune Teller")
    private val empathInteraction = ClocktowerInteractionId("other_night:role:Empath")
    private val canonicalPlan = listOf(
        poisonerInteraction,
        impInteraction,
        successorInteraction,
        fortuneTellerInteraction,
        empathInteraction,
    )

    @Test
    fun `confirmed successor survives checkpoint restore and Previous while role change remains authoritative`() {
        val restored = restore(
            checkpoint(
                nightStepIndex = 3,
                demonSuccessorDraftTarget = "Player 2",
                confirmedDemonSuccessorTarget = "Player 2",
            ),
        )

        val afterPrevious = NightCheckpointReducer.reduce(
            restored,
            NightResolutionEvent.MovePrevious,
        )
        val effective = projectedAfterSuccessor(afterPrevious)

        assertEquals(2, afterPrevious.nightStepIndex)
        assertEquals("Player 2", afterPrevious.confirmedDemonSuccessorTarget)
        assertEquals(RoleId("Imp"), effective.currentRoleId(2))
    }

    @Test
    fun `editing successor draft after restore leaves old confirmed successor mechanically authoritative`() {
        val restored = restore(
            checkpoint(
                demonSuccessorDraftTarget = "Player 2",
                confirmedDemonSuccessorTarget = "Player 2",
            ),
        )

        val edited = NightCheckpointReducer.reduce(
            restored,
            NightResolutionEvent.EditDemonSuccessorDraft("Player 3"),
        )
        val effective = projectedAfterSuccessor(edited)

        assertEquals("Player 3", edited.demonSuccessorDraftTarget)
        assertEquals("Player 2", edited.confirmedDemonSuccessorTarget)
        assertEquals(RoleId("Imp"), effective.currentRoleId(2))
        assertEquals(RoleId("Minion"), effective.currentRoleId(3))
    }

    @Test
    fun `changed confirmed upstream attack invalidates successor confirmation but preserves its draft`() {
        val restored = restore(
            checkpoint(
                confirmedAttackTarget = "Player 1",
                attackDraftTarget = "Player 4",
                demonSuccessorDraftTarget = "Player 2",
                confirmedDemonSuccessorTarget = "Player 2",
            ),
        )

        val reconfirmed = NightCheckpointReducer.reduce(
            restored,
            NightResolutionEvent.ConfirmDemonAttack,
        )

        assertEquals("Player 4", reconfirmed.confirmedAttackTarget)
        assertNull(reconfirmed.confirmedDemonSuccessorTarget)
        assertEquals("Player 2", reconfirmed.demonSuccessorDraftTarget)
    }

    @Test
    fun `Poisoner becoming Demon ends effective poison while restored raw poison fact remains confirmed`() {
        val restored = restore(
            checkpoint(
                confirmedPoisonTarget = "Player 3",
                poisonDraftTarget = "Player 3",
                demonSuccessorDraftTarget = "Player 2",
                confirmedDemonSuccessorTarget = "Player 2",
            ),
        )
        val effective = projectedAfterSuccessor(restored)

        val effectivePoisonTarget = PoisonEffectLifecycle.effectiveTarget(
            confirmedTarget = restored.confirmedPoisonTarget,
            sourceActionResolved = true,
            sourceAbilityFunctioning = effective.currentRoleId(2) == RoleId("Poisoner"),
        )

        assertEquals("Player 3", restored.confirmedPoisonTarget)
        assertEquals(RoleId("Imp"), effective.currentRoleId(2))
        assertNull(effectivePoisonTarget)
    }

    @Test
    fun `Fortune Teller targeting new living Demon is Yes after successor role change`() {
        val state = fortuneTellerStateAfterSuccession()

        assertTrue(state.isMechanicallyAlive(2))
        assertEquals(RoleId("Imp"), state.currentRoleId(2))
        assertTrue(fortuneTellerDemonMatch(state, targetSeat = 2))
    }

    @Test
    fun `Fortune Teller targeting old dead Demon is still Yes for that night`() {
        val state = fortuneTellerStateAfterSuccession()

        assertFalse(state.isMechanicallyAlive(1))
        assertEquals(RoleId("Imp"), state.currentRoleId(1))
        assertTrue(fortuneTellerDemonMatch(state, targetSeat = 1))
    }

    @Test
    fun `Monk protected Imp self kill produces no succession`() {
        val attackOutcome = DemonNightAttackSemantics.resolve(
            DemonNightAttackContext(
                attacker = subject("Imp"),
                target = subject("Imp"),
                targetIsAttacker = true,
                targetProtectedByFunctioningMonk = true,
            ),
        )
        val succession = DemonSuccessionSemantics.resolve(
            DemonSuccessionContext(
                demonActuallyDied = attackOutcome == DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = 2,
                livingMinionSeats = setOf(2, 3),
            ),
        )

        assertEquals(DemonNightAttackOutcome.NO_DEATH, attackOutcome)
        assertEquals(DemonSuccessionResolution.None, succession)
    }

    @Test
    fun `successful self kill at five alive forces functioning Scarlet Woman`() {
        val attackOutcome = DemonNightAttackSemantics.resolve(
            DemonNightAttackContext(
                attacker = subject("Imp"),
                target = subject("Imp"),
                targetIsAttacker = true,
                targetProtectedByFunctioningMonk = false,
            ),
        )
        val succession = DemonSuccessionSemantics.resolve(
            DemonSuccessionContext(
                demonActuallyDied = attackOutcome == DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = 2,
                livingMinionSeats = setOf(2, 3),
            ),
        )

        assertEquals(DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED, attackOutcome)
        assertEquals(DemonSuccessionResolution.Forced(2), succession)
    }

    @Test
    fun `successful self kill with nonfunctioning Scarlet Woman keeps ordinary living Minion choice`() {
        val attackOutcome = DemonNightAttackSemantics.resolve(
            DemonNightAttackContext(
                attacker = subject("Imp"),
                target = subject("Imp"),
                targetIsAttacker = true,
                targetProtectedByFunctioningMonk = false,
            ),
        )
        val succession = DemonSuccessionSemantics.resolve(
            DemonSuccessionContext(
                demonActuallyDied = attackOutcome == DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED,
                demonDeathWasImpSelfKill = true,
                aliveCountBeforeDemonDeath = 5,
                functioningScarletWomanSeat = null,
                livingMinionSeats = setOf(2, 3),
            ),
        )

        assertEquals(DemonNightAttackOutcome.IMP_SELF_KILL_SUCCESSOR_REQUIRED, attackOutcome)
        assertEquals(DemonSuccessionResolution.Choice(setOf(2, 3)), succession)
    }

    @Test
    fun `same persisted checkpoint and canonical plan reconstruct identical effective state`() {
        val persisted = checkpoint(
            confirmedPoisonTarget = "Player 3",
            poisonDraftTarget = "Player 3",
            demonSuccessorDraftTarget = "Player 2",
            confirmedDemonSuccessorTarget = "Player 2",
        ).persistedValues()

        val first = projectedAfterSuccessor(ClocktowerNightCheckpoint.fromPersistedValues(persisted))
        val second = projectedAfterSuccessor(ClocktowerNightCheckpoint.fromPersistedValues(persisted))

        assertEquals(first, second)
        assertEquals(RoleId("Poisoner"), baseRoles()[2])
        assertEquals(RoleId("Imp"), first.currentRoleId(2))
        assertEquals(1, canonicalPlan.count { it == impInteraction })
    }

    private fun projectedAfterSuccessor(checkpoint: ClocktowerNightCheckpoint) =
        ClocktowerEffectiveNightStateProjector.projectAt(
            baseAliveSeats = setOf(1, 2, 3),
            canonicalInteractionIds = canonicalPlan,
            confirmedEvents = listOfNotNull(
                checkpoint.confirmedDemonSuccessorTarget?.let {
                    ResolvedNightMechanicalEvent.RoleChanged(
                        targetSeat = 2,
                        roleId = RoleId("Imp"),
                        effectiveAt = ClocktowerEffectiveNightCursor(
                            successorInteraction,
                            ClocktowerInteractionBoundary.AFTER,
                        ),
                    )
                },
            ),
            cursor = ClocktowerEffectiveNightCursor(
                empathInteraction,
                ClocktowerInteractionBoundary.BEFORE,
            ),
            baseRoleIdsBySeat = baseRoles(),
        )

    private fun fortuneTellerStateAfterSuccession(): ClocktowerEffectiveNightState =
        ClocktowerEffectiveNightStateProjector.projectAt(
            baseAliveSeats = setOf(1, 2, 3),
            canonicalInteractionIds = canonicalPlan,
            confirmedEvents = listOf(
                ResolvedNightMechanicalEvent.MechanicalDeath(
                    targetSeat = 1,
                    effectiveAt = ClocktowerEffectiveNightCursor(
                        impInteraction,
                        ClocktowerInteractionBoundary.AFTER,
                    ),
                ),
                ResolvedNightMechanicalEvent.RoleChanged(
                    targetSeat = 2,
                    roleId = RoleId("Imp"),
                    effectiveAt = ClocktowerEffectiveNightCursor(
                        successorInteraction,
                        ClocktowerInteractionBoundary.AFTER,
                    ),
                ),
            ),
            cursor = ClocktowerEffectiveNightCursor(
                fortuneTellerInteraction,
                ClocktowerInteractionBoundary.BEFORE,
            ),
            baseRoleIdsBySeat = baseRoles(),
        )

    private fun fortuneTellerDemonMatch(
        state: ClocktowerEffectiveNightState,
        targetSeat: Int,
    ): Boolean = state.currentRoleId(targetSeat) == RoleId("Imp")

    private fun restore(checkpoint: ClocktowerNightCheckpoint): ClocktowerNightCheckpoint =
        ClocktowerNightCheckpoint.fromPersistedValues(checkpoint.persistedValues())

    private fun baseRoles(): Map<Int, RoleId> = mapOf(
        1 to RoleId("Imp"),
        2 to RoleId("Poisoner"),
        3 to RoleId("Minion"),
    )

    private fun subject(role: String) = AbilitySubject(
        actualRole = role,
        shownRole = role,
        isPoisoned = false,
        isAlive = true,
    )

    private fun checkpoint(
        nightStepIndex: Int = 3,
        confirmedAttackTarget: String? = "Player 1",
        attackDraftTarget: String? = confirmedAttackTarget,
        confirmedPoisonTarget: String? = null,
        poisonDraftTarget: String? = confirmedPoisonTarget,
        demonSuccessorDraftTarget: String? = null,
        confirmedDemonSuccessorTarget: String? = null,
    ) = ClocktowerNightCheckpoint(
        phaseName = "Night",
        round = 3,
        gameStateRevision = 12L,
        playerInputRevision = 7L,
        nightStarted = true,
        nightStepIndex = nightStepIndex,
        confirmedAttackTarget = confirmedAttackTarget,
        attackDraftTarget = attackDraftTarget,
        confirmedPoisonTarget = confirmedPoisonTarget,
        poisonDraftTarget = poisonDraftTarget,
        confirmedMonkTarget = null,
        monkDraftTarget = null,
        confirmedMayorRedirectTarget = null,
        mayorRedirectDraftTarget = null,
        pendingNewDemonName = null,
        pendingNightNewDemonIdentityName = null,
        demonSuccessorDraftTarget = demonSuccessorDraftTarget,
        confirmedDemonSuccessorTarget = confirmedDemonSuccessorTarget,
        nextTimelineGlobalSequence = 17L,
    )
}
