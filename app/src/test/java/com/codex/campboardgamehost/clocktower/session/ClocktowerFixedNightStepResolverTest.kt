package com.codex.campboardgamehost.clocktower.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerFixedNightStepResolverTest {
    private val actions = listOf(
        ClocktowerFixedNightStepStatus(NightOperationKind.POISON),
        ClocktowerFixedNightStepStatus(NightOperationKind.PROTECT),
        ClocktowerFixedNightStepStatus(NightOperationKind.ATTACK),
        ClocktowerFixedNightStepStatus(NightOperationKind.BUTLER),
    )

    private fun resolve(
        steps: List<ClocktowerFixedNightStepStatus> = actions,
        accepted: Set<NightOperationKind> = emptySet(),
        cursor: Int = 0,
    ) = clocktowerNextUnperformedFixedNightStepIndex(cursor, steps, accepted)

    @Test fun `poison monk imp butler follow confirmed Session facts in fixed order`() {
        assertEquals(0, resolve())
        assertEquals(1, resolve(accepted = setOf(NightOperationKind.POISON)))
        assertEquals(2, resolve(accepted = setOf(NightOperationKind.POISON, NightOperationKind.PROTECT)))
        assertEquals(3, resolve(accepted = setOf(NightOperationKind.POISON, NightOperationKind.PROTECT, NightOperationKind.ATTACK)))
        assertNull(resolve(accepted = NightOperationKind.entries.toSet()))
    }

    @Test fun `reset UI cursor cannot repeat a confirmed poisoning or attack`() {
        assertEquals(3, resolve(accepted = setOf(
            NightOperationKind.POISON, NightOperationKind.PROTECT, NightOperationKind.ATTACK,
        ), cursor = 0))
    }

    @Test fun `confirmed death of later actor skips its wakeup but not the next one`() {
        val steps = listOf(
            ClocktowerFixedNightStepStatus(NightOperationKind.POISON),
            ClocktowerFixedNightStepStatus(NightOperationKind.PROTECT, actorAlive = false),
            ClocktowerFixedNightStepStatus(NightOperationKind.ATTACK),
            ClocktowerFixedNightStepStatus(NightOperationKind.BUTLER),
        )
        assertEquals(2, resolve(steps, setOf(NightOperationKind.POISON)))
    }

    @Test fun `information role and ravenkeeper trigger are never inferred completed`() {
        val informationStep = ClocktowerFixedNightStepStatus(kind = null, actorAlive = false)
        val steps = listOf(actions[0], informationStep, actions[2], actions[3])
        assertEquals(1, resolve(steps, setOf(NightOperationKind.POISON)))
        assertEquals(1, resolve(steps, NightOperationKind.entries.toSet()))
    }

    @Test fun `nonreal mechanical placeholder does not require waking a nonexistent actor`() {
        assertEquals(1, resolve(listOf(
            ClocktowerFixedNightStepStatus(NightOperationKind.POISON, actorAlive = false, realAction = false),
            ClocktowerFixedNightStepStatus(NightOperationKind.ATTACK),
        )))
    }

    @Test fun `stale presentation cursor cannot skip still uncommitted mechanical actions`() {
        assertEquals(0, resolve(cursor = actions.size))
        assertEquals(2, resolve(
            accepted = setOf(NightOperationKind.POISON, NightOperationKind.PROTECT),
            cursor = actions.size,
        ))
        assertNull(resolve(accepted = NightOperationKind.entries.toSet(), cursor = actions.size))
        assertThrows(IllegalArgumentException::class.java) { resolve(cursor = -1) }
    }

    @Test fun `mixed night preserves uncommitted information barrier while preventing mechanic skip`() {
        val steps = listOf(actions[0], ClocktowerFixedNightStepStatus(kind = null), actions[1], actions[2], actions[3])
        // The information role is not inferred completed after Poisoner.
        assertEquals(1, resolve(steps, setOf(NightOperationKind.POISON), cursor = 0))
        // The UI may advance past the information screen, but it cannot bypass Monk.
        assertEquals(2, resolve(steps, setOf(NightOperationKind.POISON), cursor = 4))
        assertEquals(3, resolve(steps, setOf(
            NightOperationKind.POISON, NightOperationKind.PROTECT,
        ), cursor = 4))
    }
}
