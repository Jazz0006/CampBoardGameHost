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

    /**
     * Recovers the immutable setup-time role basis from a version-1 save.
     *
     * Version 1 did not persist the basis separately. If every restored actual role is still unique,
     * the current assignments are sufficient evidence. If succession has replaced one or more roles
     * with another Imp, the old RulesetRef hash is required and is used to identify the unique
     * setup-time role set. A changed-role save without that evidence is ambiguous and fails closed.
     */
    fun resolveLegacyBasisForRestore(
        knowledge: RulesetKnowledge,
        assignedRoleIds: List<RoleId>,
        persistedRef: RulesetRef?,
    ): ClocktowerRulesetPersistenceBasis {
        require(knowledge.scriptId == SCRIPT_ID) {
            "Legacy Trouble Brewing basis recovery requires trouble_brewing ruleset knowledge."
        }
        require(assignedRoleIds.isNotEmpty()) {
            "Legacy Trouble Brewing save is missing assigned role IDs."
        }
        val knownRoleIds = knowledge.characters.map { it.roleId }.toSet()
        require(assignedRoleIds.all { it in knownRoleIds }) {
            "Legacy Trouble Brewing save contains a role outside the current ruleset knowledge."
        }

        val currentRoleIds = assignedRoleIds.toSet()
        val replacedRoleCount = assignedRoleIds.size - currentRoleIds.size
        if (replacedRoleCount == 0) {
            val basis = ClocktowerRulesetPersistenceBasis(currentRoleIds)
            persistedRef?.let { ref ->
                require(ref == refFor(knowledge, basis)) {
                    "Legacy Trouble Brewing save has an incompatible ruleset reference."
                }
            }
            return basis
        }

        require(persistedRef != null) {
            "Legacy Trouble Brewing save changed assigned roles but has no ruleset reference to recover the setup basis."
        }
        val missingCandidates = (knownRoleIds - currentRoleIds).sortedBy { it.value }
        require(replacedRoleCount <= missingCandidates.size) {
            "Legacy Trouble Brewing save cannot recover its setup role basis."
        }

        val matches = combinations(missingCandidates, replacedRoleCount)
            .map { missingRoleIds ->
                ClocktowerRulesetPersistenceBasis(currentRoleIds + missingRoleIds)
            }
            .filter { basis -> refFor(knowledge, basis) == persistedRef }
            .take(2)
            .toList()
        require(matches.size == 1) {
            "Legacy Trouble Brewing save does not uniquely identify its setup role basis."
        }
        return matches.single()
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

    private fun <T> combinations(values: List<T>, choose: Int): Sequence<List<T>> = sequence {
        require(choose in 0..values.size) { "Invalid combination size '$choose'." }
        if (choose == 0) {
            yield(emptyList())
            return@sequence
        }
        for (index in 0..values.size - choose) {
            for (tail in combinations(values.subList(index + 1, values.size), choose - 1)) {
                yield(listOf(values[index]) + tail)
            }
        }
    }
}
