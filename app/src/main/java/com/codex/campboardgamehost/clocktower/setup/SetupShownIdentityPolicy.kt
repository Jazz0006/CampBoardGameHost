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

/** Durable template metadata lookup identity; actual-role composition remains in SetupCandidate. */
internal data class TemplateShownIdentityPolicyKey(
    val providerId: String,
    val candidateId: String,
) {
    init {
        require(providerId.isNotBlank()) { "Template shown-identity providerId cannot be blank." }
        require(candidateId.isNotBlank()) { "Template shown-identity candidateId cannot be blank." }
    }
}

/** Provider-neutral source of already-normalized template shown-identity metadata. */
internal fun interface TemplateShownIdentityPolicySource {
    fun find(key: TemplateShownIdentityPolicyKey): SetupShownIdentityPolicy?
}

/**
 * Pure S6A resolver from a selected actual-role candidate to legal shown-identity options.
 *
 * The current supported override mechanic is Drunk. The policy representation is intentionally
 * plural so later supported setup-time identity mechanics do not require putting shown roles into
 * SetupCandidate. Unsupported or inconsistent metadata fails closed.
 */
internal class SetupShownIdentityPolicyResolver(
    private val templatePolicySource: TemplateShownIdentityPolicySource = TemplateShownIdentityPolicySource { null },
) {
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

        return when (candidate.provenance.sourceKind) {
            SetupSourceKind.TEMPLATE -> resolveTemplate(
                candidate = candidate,
                ruleset = ruleset,
                drunk = drunk,
                hasDrunk = hasDrunk,
            )

            SetupSourceKind.GENERATED -> resolveGenerated(
                candidate = candidate,
                ruleset = ruleset,
                drunk = drunk,
                hasDrunk = hasDrunk,
            )
        }
    }

    private fun resolveTemplate(
        candidate: SetupCandidate,
        ruleset: ValidatedClocktowerRuleset,
        drunk: ClocktowerCharacterDefinition?,
        hasDrunk: Boolean,
    ): SetupShownIdentityPolicy {
        val candidateId = requireNotNull(candidate.provenance.candidateId) {
            "Template shown-identity resolution requires a durable candidateId."
        }
        val key = TemplateShownIdentityPolicyKey(
            providerId = candidate.provenance.providerId,
            candidateId = candidateId,
        )
        val policy = requireNotNull(templatePolicySource.find(key)) {
            "No shown-identity metadata exists for template '${key.providerId}/${key.candidateId}'."
        }

        if (!hasDrunk) {
            require(policy.overrides.isEmpty()) {
                "Template without Drunk cannot declare a shown-identity override."
            }
            return policy
        }

        val drunkRole = requireNotNull(drunk).id
        require(policy.overrides.size == 1 && policy.overrides.single().actualRole == drunkRole) {
            "Current template shown-identity policy must contain exactly the Drunk override when Drunk is in play."
        }
        validateDrunkOptions(
            options = policy.overrides.single().legalShownRoles,
            candidate = candidate,
            ruleset = ruleset,
        )
        return policy
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

    private fun validateDrunkOptions(
        options: List<RoleId>,
        candidate: SetupCandidate,
        ruleset: ValidatedClocktowerRuleset,
    ) {
        val actualRoles = candidate.actualRoles.toSet()
        options.forEach { shownRole ->
            val definition = requireNotNull(ruleset.characterRegistry.findByRoleId(shownRole)) {
                "Template shown role '${shownRole.value}' is not present in the validated ruleset."
            }
            require(definition.team == ClocktowerCatalogTeam.TOWNSFOLK) {
                "Drunk shown role '${shownRole.value}' must be a Townsfolk."
            }
            require(shownRole !in actualRoles) {
                "Drunk shown role '${shownRole.value}' is already an actual in-play role."
            }
        }
    }

    private companion object {
        const val DRUNK_EXTERNAL_ID = "drunk"
    }
}
