package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterRegistry
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3

/**
 * Transitional DLB visible-roster option selector.
 *
 * This reproduces the former Trouble Brewing shown-identity deterministic draw so DLB-1 changes
 * ownership/timing without also changing which drunk_as_options identity a given setup seed realizes.
 * The returned role is an added visible Townsfolk identity, not a committed Drunk shown identity.
 */
internal object TroubleBrewingVisibleRosterOptionSelector {
    fun selectAddedTownsfolk(
        candidate: SetupCandidate,
        preset: TroubleBrewingSetupPreset,
        characterRegistry: ClocktowerCharacterRegistry,
        setupSeed: Long,
    ): String? {
        if (DRUNK_EXTERNAL_ID !in preset.outsiders) return null

        val actualRoles = candidate.actualRoles.toSet()
        val drunkRole = requireNotNull(characterRegistry.findByExternalId(DRUNK_EXTERNAL_ID)) {
            "Validated Trouble Brewing metadata requires the canonical Drunk role."
        }.id
        require(drunkRole in actualRoles) {
            "Trouble Brewing Drunk candidate must contain the canonical Drunk role."
        }

        val legalVisibleRoles = preset.drunkAsOptions
            .map { externalId ->
                requireNotNull(characterRegistry.findByExternalId(externalId)) {
                    "Validated Trouble Brewing visible-roster option '$externalId' is missing from the registry."
                }.id
            }
            .sortedBy { it.value }
        require(legalVisibleRoles.isNotEmpty()) {
            "Trouble Brewing Drunk preset '${preset.id}' has no visible-roster Townsfolk options."
        }
        require(drunkRole !in legalVisibleRoles) {
            "Trouble Brewing visible-roster options cannot contain Drunk."
        }
        require(legalVisibleRoles.none { it in actualRoles }) {
            "Trouble Brewing visible-roster option must not already be an actual in-play role."
        }

        val drawSeed = MurmurHash3.low64Utf8(
            seedMaterial(
                candidate = candidate,
                actualRole = drunkRole.value,
                legalShownRoles = legalVisibleRoles.map { it.value },
                setupSeed = setupSeed,
            ),
        )
        val selectedIndex =
            java.lang.Long.remainderUnsigned(drawSeed, legalVisibleRoles.size.toLong()).toInt()
        return requireNotNull(characterRegistry.findByRoleId(legalVisibleRoles[selectedIndex])) {
            "Selected Trouble Brewing visible-roster role is missing from the registry."
        }.externalId
    }

    private fun seedMaterial(
        candidate: SetupCandidate,
        actualRole: String,
        legalShownRoles: List<String>,
        setupSeed: Long,
    ): String = buildString {
        append(LEGACY_NAMESPACE)
        appendField(candidate.script.value)
        appendField(candidate.provenance.sourceKind.name)
        appendField(candidate.provenance.providerId)
        appendField(candidate.provenance.candidateId.orEmpty())
        candidate.actualRoles.forEach { role -> appendField(role.value) }
        appendField(actualRole)
        legalShownRoles.forEach { role -> appendField(role) }
        appendField(setupSeed.toString())
    }

    private fun StringBuilder.appendField(value: String) {
        append('|')
        append(value.length)
        append(':')
        append(value)
    }

    private const val LEGACY_NAMESPACE = "setup-shown-identity-v1"
    private const val DRUNK_EXTERNAL_ID = "drunk"
}
