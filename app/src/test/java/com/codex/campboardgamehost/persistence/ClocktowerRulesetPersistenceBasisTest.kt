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
    private val empath = RoleId("Empath")
    private val knowledge = RulesetKnowledge(
        scriptId = ScriptId("trouble_brewing"),
        characters = listOf(
            RuleCharacterText(imp, "Demon ability"),
            RuleCharacterText(scarletWoman, "Minion ability"),
            RuleCharacterText(empath, "Townsfolk ability"),
        ),
        firstNightOrder = listOf(scarletWoman, empath),
        otherNightOrder = listOf(scarletWoman, imp, empath),
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
    fun `legacy restore may reconstruct a missing ref but never accepts a stale one`() {
        val basis = ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath))
        val expected = TroubleBrewingRulesetPersistence.refFor(knowledge, basis)

        assertEquals(
            expected,
            TroubleBrewingRulesetPersistence.resolveForRestore(
                knowledge = knowledge,
                persistedRef = null,
                basis = basis,
                allowLegacyFallback = true,
            ),
        )
        assertFails {
            TroubleBrewingRulesetPersistence.resolveForRestore(
                knowledge = knowledge,
                persistedRef = expected.copy(scriptContentHash = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
                basis = basis,
                allowLegacyFallback = true,
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
