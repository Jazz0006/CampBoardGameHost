package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StrategicWorldKeyTest {
    private val script = ScriptId("strategic-world-key-test")
    private val roles = listOf(
        role("Good A", Alignment.GOOD, CharacterType.TOWNSFOLK),
        role("Good B", Alignment.GOOD, CharacterType.TOWNSFOLK),
        role("Minion A", Alignment.EVIL, CharacterType.MINION),
        role("Minion B", Alignment.EVIL, CharacterType.MINION),
        role("Demon A", Alignment.EVIL, CharacterType.DEMON),
        role("Demon B", Alignment.EVIL, CharacterType.DEMON),
    ).associateBy(RoleDefinition::id)

    @Test
    fun `good-role permutations and non-topology state collapse to one setup strategic key`() {
        val first = world(
            linkedMapOf(
                1 to RoleId("Good A"),
                2 to RoleId("Good B"),
                3 to RoleId("Minion A"),
                4 to RoleId("Demon A"),
            ),
        )
        val second = world(
            linkedMapOf(
                1 to RoleId("Good B"),
                2 to RoleId("Good A"),
                3 to RoleId("Minion A"),
                4 to RoleId("Demon A"),
            ),
        ).copy(
            redHerringSeat = 1,
            shownRolesBySeat = mapOf(2 to RoleId("Good A")),
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_POISONED),
            explanationClusters = setOf(WorldExplanationClusterId("different-explanation")),
        )

        assertEquals(
            StrategicWorldKey.from(first, roles),
            StrategicWorldKey.from(second, roles),
        )
        assertEquals(
            StrategicWorldKey(demonSeat = 4, minionSeats = listOf(3)),
            StrategicWorldKey.from(first, roles),
        )
    }

    @Test
    fun `same evil seat set with Demon Minion swap remains strategically distinct`() {
        val demonAtFour = world(
            linkedMapOf(
                1 to RoleId("Good A"),
                2 to RoleId("Good B"),
                3 to RoleId("Minion A"),
                4 to RoleId("Demon A"),
            ),
        )
        val demonAtThree = world(
            linkedMapOf(
                1 to RoleId("Good A"),
                2 to RoleId("Good B"),
                3 to RoleId("Demon B"),
                4 to RoleId("Minion B"),
            ),
        )

        assertNotEquals(
            StrategicWorldKey.from(demonAtFour, roles),
            StrategicWorldKey.from(demonAtThree, roles),
        )
        assertEquals(
            setOf(3, 4),
            StrategicWorldKey.from(demonAtFour, roles).let { setOf(it.demonSeat) + it.minionSeats },
        )
        assertEquals(
            setOf(3, 4),
            StrategicWorldKey.from(demonAtThree, roles).let { setOf(it.demonSeat) + it.minionSeats },
        )
    }

    @Test
    fun `projection uses role definition character type rather than role name heuristics`() {
        val opaqueRoles = listOf(
            role("Copper", Alignment.GOOD, CharacterType.TOWNSFOLK),
            role("Quartz", Alignment.GOOD, CharacterType.TOWNSFOLK),
            role("Lantern", Alignment.EVIL, CharacterType.MINION),
            role("Harbor", Alignment.EVIL, CharacterType.DEMON),
        ).associateBy(RoleDefinition::id)
        val world = world(
            linkedMapOf(
                1 to RoleId("Copper"),
                2 to RoleId("Quartz"),
                3 to RoleId("Lantern"),
                4 to RoleId("Harbor"),
            ),
        )

        assertEquals(
            StrategicWorldKey(demonSeat = 4, minionSeats = listOf(3)),
            StrategicWorldKey.from(world, opaqueRoles),
        )
    }

    @Test
    fun `historical current-role succession does not rewrite setup strategic key`() {
        val setup = world(
            linkedMapOf(
                1 to RoleId("Good A"),
                2 to RoleId("Good B"),
                3 to RoleId("Minion A"),
                4 to RoleId("Demon A"),
            ),
        )
        val successor = setup.withCurrentRoles(
            linkedMapOf(
                1 to RoleId("Good A"),
                2 to RoleId("Good B"),
                3 to RoleId("Demon A"),
                4 to RoleId("Demon A"),
            ),
        )

        assertEquals(
            StrategicWorldKey(demonSeat = 4, minionSeats = listOf(3)),
            StrategicWorldKey.from(successor, roles),
        )
    }

    private fun world(rolesBySeat: Map<Int, RoleId>) = EnumeratedWorld(
        rolesBySeat = rolesBySeat,
    )

    private fun role(
        name: String,
        alignment: Alignment,
        type: CharacterType,
    ) = RoleDefinition(
        id = RoleId(name),
        alignment = alignment,
        type = type,
        scriptIds = setOf(script),
    )
}
