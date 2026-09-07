package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.RuleCharacterText
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetKnowledge
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
    fun `immutable basis preserves original ruleset identity after Scarlet Woman becomes Imp`() {
        val basis = ClocktowerRulesetPersistenceBasis(setOf(imp, scarletWoman, empath))
        val currentRef = TroubleBrewingRulesetPersistence.refFor(knowledge, basis)

        val mutableCurrentRoleHash = RulesetContentHasher.hash(
            knowledge = knowledge,
            inPlayRoleIds = setOf(imp, empath),
        )
        assertNotEquals(currentRef.scriptContentHash, mutableCurrentRoleHash)
    }

    @Test
    fun `basis rejects empty role set`() {
        assertFails { ClocktowerRulesetPersistenceBasis(emptySet()) }
    }

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
