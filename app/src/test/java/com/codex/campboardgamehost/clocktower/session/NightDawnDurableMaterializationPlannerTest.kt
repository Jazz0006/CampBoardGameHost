package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** SNE-7.9E: Dawn durable effects need stable identity independent of callback retries. */
class NightDawnDurableMaterializationPlannerTest {
    private val poisonClear = DawnPoisonCarryIntent(
        targetSeat = null,
        previousTargetSeat = 3,
    )
    private val intent = DawnCommitIntent(
        roleChanges = listOf(DawnRoleChangeIntent(targetSeat = 2, roleId = RoleId("Imp"))),
        death = DawnDeathIntent(targetSeat = 1),
        poisonCarry = poisonClear,
    )

    @Test
    fun `first Dawn materialization requests each durable effect exactly once`() {
        val plan = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState(),
            advanceToDawn = true,
        )

        val death = requireNotNull(plan.death)
        assertTrue(death.stateMutationRequired)
        assertNotNull(death.actionIdToCommit)
        assertNotNull(death.publicAliveObservationIdToCommit)

        val roleChange = plan.roleChanges.single()
        assertEquals(DawnRoleChangeIntent(2, RoleId("Imp")), roleChange.intent)
        assertTrue(roleChange.stateMutationRequired)
        assertNotNull(roleChange.actionIdToCommit)

        val poison = requireNotNull(plan.poison)
        assertEquals(poisonClear, poison.intent)
        assertTrue(poison.stateMutationRequired)
        assertEquals("dawn-game-17-3-poison-seat-3-to-none", poison.actionIdToCommit)

        val phaseAdvance = requireNotNull(plan.phaseAdvance)
        assertEquals(StorytellerPhase.DAWN, phaseAdvance.targetPhase)
        assertTrue(phaseAdvance.stateMutationRequired)
        assertNotNull(phaseAdvance.actionIdToCommit)
    }

    @Test
    fun `replaying fully durable Dawn intent produces no duplicate mutation history or phase effect`() {
        val first = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState(),
            advanceToDawn = true,
        )
        val firstDeath = requireNotNull(first.death)
        val firstRoleChange = first.roleChanges.single()
        val firstPoison = requireNotNull(first.poison)
        val firstPhase = requireNotNull(first.phaseAdvance)

        val replay = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState().copy(
                aliveSeats = setOf(2, 3),
                roleIdsBySeat = mapOf(
                    1 to RoleId("Imp"),
                    2 to RoleId("Imp"),
                    3 to RoleId("Empath"),
                ),
                currentPhase = StorytellerPhase.DAWN,
                currentPoisonTargetSeat = null,
                committedActionIds = setOf(
                    requireNotNull(firstDeath.actionIdToCommit),
                    requireNotNull(firstRoleChange.actionIdToCommit),
                    requireNotNull(firstPoison.actionIdToCommit),
                    requireNotNull(firstPhase.actionIdToCommit),
                ),
                committedObservationRecordIds = setOf(
                    requireNotNull(firstDeath.publicAliveObservationIdToCommit),
                ),
            ),
            advanceToDawn = true,
        )

        val replayDeath = requireNotNull(replay.death)
        assertFalse(replayDeath.stateMutationRequired)
        assertNull(replayDeath.actionIdToCommit)
        assertNull(replayDeath.publicAliveObservationIdToCommit)

        val replayRoleChange = replay.roleChanges.single()
        assertFalse(replayRoleChange.stateMutationRequired)
        assertNull(replayRoleChange.actionIdToCommit)

        val replayPoison = requireNotNull(replay.poison)
        assertFalse(replayPoison.stateMutationRequired)
        assertNull(replayPoison.actionIdToCommit)

        val replayPhase = requireNotNull(replay.phaseAdvance)
        assertFalse(replayPhase.stateMutationRequired)
        assertNull(replayPhase.actionIdToCommit)
    }

    @Test
    fun `partial retry repairs missing durable history without repeating already applied death mutation`() {
        val first = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState(),
            advanceToDawn = true,
        )
        val retry = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState().copy(aliveSeats = setOf(2, 3)),
            advanceToDawn = true,
        )

        val firstDeath = requireNotNull(first.death)
        val retryDeath = requireNotNull(retry.death)
        assertFalse(retryDeath.stateMutationRequired)
        assertEquals(firstDeath.actionIdToCommit, retryDeath.actionIdToCommit)
        assertEquals(
            firstDeath.publicAliveObservationIdToCommit,
            retryDeath.publicAliveObservationIdToCommit,
        )
    }

    @Test
    fun `poison state already cleared but history missing still repairs stable clear action`() {
        val first = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState(),
            advanceToDawn = true,
        )
        val firstPoison = requireNotNull(first.poison)

        val retry = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState().copy(currentPoisonTargetSeat = null),
            advanceToDawn = true,
        )
        val retryPoison = requireNotNull(retry.poison)

        assertFalse(retryPoison.stateMutationRequired)
        assertEquals(firstPoison.actionIdToCommit, retryPoison.actionIdToCommit)
    }

    @Test
    fun `poison clear history already present but state stale repairs state without duplicate history`() {
        val first = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState(),
            advanceToDawn = true,
        )
        val firstPoison = requireNotNull(first.poison)

        val retry = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = intent,
            state = initialState().copy(
                committedActionIds = setOf(requireNotNull(firstPoison.actionIdToCommit)),
            ),
            advanceToDawn = true,
        )
        val retryPoison = requireNotNull(retry.poison)

        assertTrue(retryPoison.stateMutationRequired)
        assertNull(retryPoison.actionIdToCommit)
    }

    @Test
    fun `unchanged poison carry requires neither new Dawn history nor state mutation`() {
        val carryIntent = DawnCommitIntent(
            poisonCarry = DawnPoisonCarryIntent(
                targetSeat = 3,
                previousTargetSeat = 3,
            ),
        )
        val plan = NightDawnDurableMaterializationPlanner.plan(
            gameId = "game-17",
            round = 3,
            intent = carryIntent,
            state = initialState(),
            advanceToDawn = false,
        )

        val poison = requireNotNull(plan.poison)
        assertFalse(poison.stateMutationRequired)
        assertNull(poison.actionIdToCommit)
    }

    private fun initialState() = DawnDurableMaterializationState(
        aliveSeats = setOf(1, 2, 3),
        roleIdsBySeat = mapOf(
            1 to RoleId("Imp"),
            2 to RoleId("Poisoner"),
            3 to RoleId("Empath"),
        ),
        currentPhase = StorytellerPhase.NIGHT,
        committedActionIds = emptySet(),
        committedObservationRecordIds = emptySet(),
        currentPoisonTargetSeat = 3,
    )
}
