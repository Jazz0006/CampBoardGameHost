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
    private val intent = DawnCommitIntent(
        roleChanges = listOf(DawnRoleChangeIntent(targetSeat = 2, roleId = RoleId("Imp"))),
        death = DawnDeathIntent(targetSeat = 1),
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
                committedActionIds = setOf(
                    requireNotNull(firstDeath.actionIdToCommit),
                    requireNotNull(firstRoleChange.actionIdToCommit),
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
    )
}
