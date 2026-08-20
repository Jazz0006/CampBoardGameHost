package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.rules.RulesetContentHasher
import com.codex.campboardgamehost.clocktower.rules.RulesetKnowledge
import org.json.JSONArray

/**
 * Immutable role-set basis for the existing Trouble Brewing RulesetRef hash.
 *
 * The basis is captured from the actual roles at setup time and must not follow later role changes
 * such as Scarlet Woman or Imp succession. It is persistence evidence for reproducing the existing
 * A3/A4 correctness identity; it is deliberately separate from normalized script content identity.
 */
internal data class ClocktowerRulesetPersistenceBasis(
    val roleIds: Set<RoleId>,
) {
    init {
        require(roleIds.isNotEmpty()) { "Clocktower ruleset persistence basis cannot be empty." }
    }
}

internal object ClocktowerRulesetPersistenceBasisJsonCodec {
    fun encode(basis: ClocktowerRulesetPersistenceBasis): JSONArray = JSONArray().apply {
        basis.roleIds
            .sortedBy { it.value }
            .forEach { roleId -> put(roleId.value) }
    }

    fun decode(json: JSONArray): ClocktowerRulesetPersistenceBasis {
        val values = (0 until json.length()).map { index ->
            json.optString(index)
                .takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException(
                    "Clocktower ruleset persistence basis contains a blank role id at index $index.",
                )
        }
        require(values.distinct().size == values.size) {
            "Clocktower ruleset persistence basis contains duplicate role ids."
        }
        return ClocktowerRulesetPersistenceBasis(values.map(::RoleId).toSet())
    }
}

internal object TroubleBrewingRulesetPersistence {
    const val RULESET_VERSION = "trouble-brewing-v1"
    const val SOURCE_REVISION = "official-wiki-2026-08-06"

    private val SCRIPT_ID = ScriptId("trouble_brewing")

    fun refFor(
        knowledge: RulesetKnowledge,
        basis: ClocktowerRulesetPersistenceBasis,
    ): RulesetRef {
        require(knowledge.scriptId == SCRIPT_ID) {
            "Trouble Brewing ruleset persistence requires the trouble_brewing ruleset knowledge."
        }
        return RulesetRef(
            scriptId = knowledge.scriptId,
            scriptContentHash = RulesetContentHasher.hash(knowledge, basis.roleIds),
            rulesetVersion = RULESET_VERSION,
            sourceRevision = SOURCE_REVISION,
            coverage = RuleCoverage.PARTIAL,
        )
    }

    fun resolveForRestore(
        knowledge: RulesetKnowledge,
        persistedRef: RulesetRef?,
        basis: ClocktowerRulesetPersistenceBasis,
        allowLegacyFallback: Boolean,
    ): RulesetRef {
        val expected = refFor(knowledge, basis)
        if (persistedRef == null) {
            require(allowLegacyFallback) {
                "Version 2 Trouble Brewing save is missing its ruleset reference."
            }
            return expected
        }
        require(persistedRef == expected) {
            if (allowLegacyFallback) {
                "Legacy Trouble Brewing save has an incompatible ruleset reference."
            } else {
                "Version 2 Trouble Brewing save has an incompatible ruleset reference."
            }
        }
        return persistedRef
    }
}
