package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EnumeratedHistoricalDynamicRoleStateTest {
    private val script = ScriptId("trouble_brewing")
    private val ruleset = RulesetRef(
        script,
        "0123456789abcdef0123456789abcdef",
        "a3-h5-dynamic-role-test",
        "official",
        RuleCoverage.VERIFIED,
    )
    private val roles = listOf(
        role("Empath", CharacterType.TOWNSFOLK),
        role("Chef", CharacterType.TOWNSFOLK),
        role("Poisoner", CharacterType.MINION),
        role("Scarlet Woman", CharacterType.MINION),
        role("Imp", CharacterType.DEMON),
    )
    private val setupRoles = linkedMapOf(
        1 to RoleId("Empath"),
        2 to RoleId("Poisoner"),
        3 to RoleId("Scarlet Woman"),
        4 to RoleId("Imp"),
        5 to RoleId("Chef"),
    )

    @Test
    fun `H5 historical successor role is dynamic while setup identity remains immutable`() {
        val setupWorld = EnumeratedWorld(
            rolesBySeat = setupRoles,
            aliveSeats = setOf(1, 2, 3, 5),
        )
        val successorWorld = setupWorld.withCurrentRolesForH5(
            setupRoles + (3 to RoleId("Imp")),
        )

        assertEquals(setupRoles, successorWorld.rolesBySeat)
        assertEquals(setupRoles.size, successorWorld.rolesBySeat.values.distinct().size)
        assertEquals(RoleId("Imp"), successorWorld.currentRolesForH5().getValue(3))
        assertEquals(RoleId("Imp"), successorWorld.currentRolesForH5().getValue(4))

        val worldSet = EnumeratedWorldSet.fromWorlds(
            rulesetRef = ruleset,
            knowledge = PlayerKnowledgeSnapshot(
                knowledgeSnapshotId = "knowledge-a3-h5-dynamic-role",
                formalSnapshotId = "snapshot-a3-h5-dynamic-role",
                recipientSeat = 1,
                perceivedRole = RoleId("Empath"),
                setupKnowledge = listOf(InformationProposition.SetupProfile(2, 0, 2, 1)),
            ),
            hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            roleDefinitions = roles,
            worlds = listOf(successorWorld),
        )

        assertEquals(setOf(3), worldSet.possibleDemonSeats())
    }

    private fun EnumeratedWorld.withCurrentRolesForH5(currentRoles: Map<Int, RoleId>): EnumeratedWorld {
        val method = javaClass.methods.singleOrNull {
            it.name == "withCurrentRoles" && it.parameterCount == 1
        }
        assertNotNull(
            "H5 requires an explicit dynamic current-role transition seam separate from setup rolesBySeat.",
            method,
        )
        return method!!.invoke(this, currentRoles) as EnumeratedWorld
    }

    @Suppress("UNCHECKED_CAST")
    private fun EnumeratedWorld.currentRolesForH5(): Map<Int, RoleId> {
        val getter = javaClass.methods.singleOrNull {
            it.name == "getCurrentRolesBySeat" && it.parameterCount == 0
        }
        assertNotNull(
            "H5 requires currentRolesBySeat to be inspectable independently from immutable setup rolesBySeat.",
            getter,
        )
        return getter!!.invoke(this) as Map<Int, RoleId>
    }

    private fun role(name: String, type: CharacterType) = RoleDefinition(
        id = RoleId(name),
        alignment = if (type == CharacterType.TOWNSFOLK) Alignment.GOOD else Alignment.EVIL,
        type = type,
        scriptIds = setOf(script),
    )
}
