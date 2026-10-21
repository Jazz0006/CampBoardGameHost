package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

/** Legal shown-role options for one actual role whose setup identity must differ. */
internal class ShownIdentityOverrideOptions(
    val actualRole: RoleId,
    legalShownRoles: List<RoleId>,
) {
    val legalShownRoles: List<RoleId> = legalShownRoles.sortedBy(RoleId::value)

    init {
        require(this.legalShownRoles.isNotEmpty()) {
            "Shown-identity override requires at least one legal shown role."
        }
        require(this.legalShownRoles.distinct().size == this.legalShownRoles.size) {
            "Shown-identity override legal shown roles must be unique."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is ShownIdentityOverrideOptions &&
            actualRole == other.actualRole &&
            legalShownRoles == other.legalShownRoles

    override fun hashCode(): Int {
        var result = actualRole.hashCode()
        result = 31 * result + legalShownRoles.hashCode()
        return result
    }

    override fun toString(): String =
        "ShownIdentityOverrideOptions(actualRole=$actualRole, legalShownRoles=$legalShownRoles)"
}

/**
 * Canonical pre-seat shown-identity policy for a selected actual-role composition.
 *
 * An empty [overrides] list is the explicit no-override policy. This stage exposes legal options
 * only; choosing a shown role belongs to the later commitment stage.
 */
internal class SetupShownIdentityPolicy(
    overrides: List<ShownIdentityOverrideOptions>,
) {
    val overrides: List<ShownIdentityOverrideOptions> = overrides.sortedBy { it.actualRole.value }

    val requiresOverride: Boolean
        get() = overrides.isNotEmpty()

    init {
        require(this.overrides.map { it.actualRole }.distinct().size == this.overrides.size) {
            "Shown-identity policy cannot contain duplicate actual-role overrides."
        }
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is SetupShownIdentityPolicy && overrides == other.overrides

    override fun hashCode(): Int = overrides.hashCode()

    override fun toString(): String = "SetupShownIdentityPolicy(overrides=$overrides)"

    companion object {
        val NO_OVERRIDE = SetupShownIdentityPolicy(emptyList())
    }
}

/** Shown-role options for the actual generated NGJ setup; TB commits its shown roster separately. */
internal class SetupShownIdentityPolicyResolver {
    fun resolve(
        candidate: SetupCandidate,
        ruleset: ValidatedClocktowerRuleset,
    ): SetupShownIdentityPolicy {
        require(candidate.script == ruleset.script.id) {
            "Shown-identity resolver cannot apply a ruleset for another script."
        }
        candidate.actualRoles.forEach { roleId ->
            requireNotNull(ruleset.characterRegistry.findByRoleId(roleId)) {
                "Setup candidate role '${roleId.value}' is not present in the validated ruleset."
            }
        }

        val drunk = ruleset.characterRegistry.findByExternalId(DRUNK_EXTERNAL_ID)
        val hasDrunk = drunk != null && drunk.id in candidate.actualRoles
        if (hasDrunk) {
            require(drunk.team == ClocktowerCatalogTeam.OUTSIDER) {
                "Canonical Drunk shown-identity semantics require Drunk to be an Outsider."
            }
        }

        require(candidate.provenance.sourceKind == SetupSourceKind.GENERATED) {
            "Shown-identity options belong to generated setup."
        }
        return resolveGenerated(candidate, ruleset, drunk, hasDrunk)
    }

    private fun resolveGenerated(
        candidate: SetupCandidate,
        ruleset: ValidatedClocktowerRuleset,
        drunk: ClocktowerCharacterDefinition?,
        hasDrunk: Boolean,
    ): SetupShownIdentityPolicy {
        if (!hasDrunk) return SetupShownIdentityPolicy.NO_OVERRIDE

        val actualRoles = candidate.actualRoles.toSet()
        val legalShownRoles = ruleset.characters
            .asSequence()
            .filter { it.team == ClocktowerCatalogTeam.TOWNSFOLK }
            .map { it.id }
            .filterNot { it in actualRoles }
            .sortedBy(RoleId::value)
            .toList()
        require(legalShownRoles.isNotEmpty()) {
            "Generated setup contains Drunk but has no unused Townsfolk legal for the shown identity."
        }

        return SetupShownIdentityPolicy(
            overrides = listOf(
                ShownIdentityOverrideOptions(
                    actualRole = requireNotNull(drunk).id,
                    legalShownRoles = legalShownRoles,
                ),
            ),
        )
    }

    private companion object {
        const val DRUNK_EXTERNAL_ID = "drunk"
    }
}
