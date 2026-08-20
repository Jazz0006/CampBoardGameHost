package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.RuleCharacterText
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetKnowledge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerRulesetPersistenceBasisTest {
    private val imp = RoleId("Imp")
    private val scarletWoman = RoleId("Scarlet Woman")
    private val poisoner = RoleId("Poisoner")
    private val empath = RoleId("Empath")
    private val knowledge = RulesetKnowledge(
        scriptId = ScriptId("trouble_brewing"),
        characters = listOf(
            RuleCharacterText(imp, "Demon ability"),
            RuleCharacterText(scarletWoman, "Minion ability"),
            RuleCharacterText(poisoner, "Minion ability"),
            RuleCharacterText(empath, "Townsfolk ability"),
        ),
        firstNightOrder = listOf(scarletWoman, poisoner, empath),
        otherNightOrder = listOf(poisoner, scarletWoman, imp, empath),
        jinxes = emptyList(),
    )

    @Test
    fun `ruleset basis JSON round trip is stable and sorted`() {
        val basis = ClocktowerRulesetPersistenceBasis(setOf(scarletWoman, imp, empath))

        val encoded = ClocktowerRulesetPersistenceBasisJsonCodec.encode(basis)
        assertEquals(listOf("Empath", "Imp", "Scarlet Woman"), encoded.toStringList())
        assertEquals(basis, ClocktowerRulesetPersistenceBasisJsonCodec.decode(encoded))
    }

    @Test
    fun `immutable basis preserves original ruleset identity after Scarlet Woman becomes Imp`() {
        val basis = ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath))
        val persisted = TroubleBrewingRulesetPersistence.refFor(knowledge, basis)

        val mutableCurrentRoleHash = RulesetContentHasher.hash(
            knowledge = knowledge,
            inPlayRoleIds = setOf(imp, empath),
        )
        assertNotEquals(persisted.scriptContentHash, mutableCurrentRoleHash)

        val restored = TroubleBrewingRulesetPersistence.resolveForRestore(
            knowledge = knowledge,
            persistedRef = persisted,
            basis = basis,
            allowLegacyFallback = false,
        )
        assertEquals(persisted, restored)
    }

    @Test
    fun `version 2 rejects missing or stale ruleset reference`() {
        val basis = ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath))
        val expected = TroubleBrewingRulesetPersistence.refFor(knowledge, basis)

        assertFails {
            TroubleBrewingRulesetPersistence.resolveForRestore(
                knowledge = knowledge,
                persistedRef = null,
                basis = basis,
                allowLegacyFallback = false,
            )
        }
        assertFails {
            TroubleBrewingRulesetPersistence.resolveForRestore(
                knowledge = knowledge,
                persistedRef = expected.copy(scriptContentHash = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                basis = basis,
                allowLegacyFallback = false,
            )
        }
    }

    @Test
    fun `legacy restore may reconstruct a missing ref when assignments are still unique`() {
        val basis = ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath))

        assertEquals(
            basis,
            TroubleBrewingRulesetPersistence.resolveLegacyBasisForRestore(
                knowledge = knowledge,
                assignedRoleIds = listOf(imp, scarletWoman, empath),
                persistedRef = null,
            ),
        )
        assertEquals(
            TroubleBrewingRulesetPersistence.refFor(knowledge, basis),
            TroubleBrewingRulesetPersistence.resolveForRestore(
                knowledge = knowledge,
                persistedRef = null,
                basis = basis,
                allowLegacyFallback = true,
            ),
        )
    }

    @Test
    fun `legacy basis fails closed when succession changed a role and old ref is missing`() {
        assertFails {
            TroubleBrewingRulesetPersistence.resolveLegacyBasisForRestore(
                knowledge = knowledge,
                assignedRoleIds = listOf(imp, imp, empath),
                persistedRef = null,
            )
        }
    }

    @Test
    fun `legacy basis recovers Scarlet Woman from persisted ref after promotion`() {
        val originalBasis = ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath))
        val persisted = TroubleBrewingRulesetPersistence.refFor(knowledge, originalBasis)

        val recovered = TroubleBrewingRulesetPersistence.resolveLegacyBasisForRestore(
            knowledge = knowledge,
            assignedRoleIds = listOf(imp, imp, empath),
            persistedRef = persisted,
        )

        assertEquals(originalBasis, recovered)
    }

    @Test
    fun `legacy basis recovery is hash driven and works for a non Scarlet Woman Imp successor`() {
        val originalBasis = ClocktowerRulesetPersistenceBasis(setOf(imp, poisoner, empath))
        val persisted = TroubleBrewingRulesetPersistence.refFor(knowledge, originalBasis)

        val recovered = TroubleBrewingRulesetPersistence.resolveLegacyBasisForRestore(
            knowledge = knowledge,
            assignedRoleIds = listOf(imp, imp, empath),
            persistedRef = persisted,
        )

        assertEquals(originalBasis, recovered)
    }

    @Test
    fun `legacy basis never accepts a stale ref`() {
        assertFails {
            TroubleBrewingRulesetPersistence.resolveLegacyBasisForRestore(
                knowledge = knowledge,
                assignedRoleIds = listOf(imp, imp, empath),
                persistedRef = TroubleBrewingRulesetPersistence
                    .refFor(knowledge, ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath)))
                    .copy(scriptContentHash = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
            )
        }
    }

    @Test
    fun `basis rejects empty role set`() {
        assertFails { ClocktowerRulesetPersistenceBasis(emptySet()) }
    }

    private fun org.json.JSONArray.toStringList(): List<String> =
        (0 until length()).map(::getString)

    private fun assertFails(block: () -> Unit) {
        var failed = false
        try {
            block()
        } catch (_: IllegalArgumentException) {
            failed = true
        } catch (_: IllegalStateException) {
            failed = true
        }
        assertTrue("Expected ruleset persistence validation to fail closed.", failed)
    }
}
