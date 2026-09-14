package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.SetupProvenance
import com.codex.campboardgamehost.clocktower.domain.SetupSourceKind

/**
 * Pure deterministic source for a legal pre-seat actual-role composition.
 *
 * This stage owns only composition generation. Seating, shown identities, diversity/history policy,
 * persistence and production-flow cutover are deliberately later concerns.
 */
internal class GeneratedSetupCandidateSource(
    private val providerId: String,
    private val ruleset: ValidatedClocktowerRuleset,
) : SetupCandidateSource {
    init {
        require(providerId.isNotBlank()) { "Generated setup providerId cannot be blank." }
    }

    override fun candidates(request: SetupCandidateRequest): List<SetupCandidate> {
        require(request.script == ruleset.script.id) {
            "Generated setup source for '${ruleset.script.id.value}' cannot serve '${request.script.value}'."
        }

        val base = clocktowerSetupDistribution(request.playerCount)
        val minions = selectRoles(
            team = ClocktowerCatalogTeam.MINION,
            count = base.minions,
            request = request,
        )
        val hasBaron = minions.any { it.externalId == BARON_EXTERNAL_ID }
        val availableOutsiders = rolesFor(ClocktowerCatalogTeam.OUTSIDER).size
        val outsiderCount = if (hasBaron) {
            minOf(base.outsiders + BARON_EXTRA_OUTSIDERS, availableOutsiders)
        } else {
            base.outsiders
        }
        val outsiderDelta = outsiderCount - base.outsiders
        val townsfolkCount = base.townsfolk - outsiderDelta
        require(townsfolkCount >= 0) {
            "Setup modifiers require more Townsfolk replacements than the base distribution permits."
        }

        val selected =
            selectRoles(ClocktowerCatalogTeam.TOWNSFOLK, townsfolkCount, request) +
                selectRoles(ClocktowerCatalogTeam.OUTSIDER, outsiderCount, request) +
                minions +
                selectRoles(ClocktowerCatalogTeam.DEMON, base.demons, request)

        val unsupportedSetupRoles = selected.filter { role ->
            role.setup && role.externalId != BARON_EXTERNAL_ID
        }
        require(unsupportedSetupRoles.isEmpty()) {
            "Generated setup source does not yet support setup modifiers for: " +
                unsupportedSetupRoles.joinToString { it.externalId }
        }
        require(selected.size == request.playerCount) {
            "Generated setup composition size ${selected.size} does not match requested player count ${request.playerCount}."
        }

        return listOf(
            SetupCandidate(
                script = request.script,
                actualRoles = selected.map(ClocktowerCharacterDefinition::id),
                provenance = SetupProvenance(
                    sourceKind = SetupSourceKind.GENERATED,
                    providerId = providerId,
                ),
            ),
        )
    }

    private fun selectRoles(
        team: ClocktowerCatalogTeam,
        count: Int,
        request: SetupCandidateRequest,
    ): List<ClocktowerCharacterDefinition> {
        val available = rolesFor(team)
        require(available.size >= count) {
            "Script '${request.script.value}' has ${available.size} $team roles but setup requires $count."
        }
        if (count == 0) return emptyList()

        return available
            .sortedWith(
                compareBy<ClocktowerCharacterDefinition>(
                    { role -> seededRank(request, team.name, role) },
                    { role -> role.id.value },
                ),
            )
            .take(count)
    }

    private fun rolesFor(team: ClocktowerCatalogTeam): List<ClocktowerCharacterDefinition> =
        ruleset.characters.filter { it.team == team }

    private fun seededRank(
        request: SetupCandidateRequest,
        namespace: String,
        role: ClocktowerCharacterDefinition,
    ): Long {
        var hash = FNV_OFFSET_BASIS xor request.setupSeed
        hash = appendStableText(hash, request.script.value)
        hash = appendStableText(hash, request.playerCount.toString())
        hash = appendStableText(hash, namespace)
        hash = appendStableText(hash, role.id.value)
        return mix64(hash)
    }

    private fun appendStableText(initial: Long, text: String): Long {
        var hash = initial
        text.forEach { character ->
            hash = (hash xor character.code.toLong()) * FNV_PRIME
        }
        return (hash xor FIELD_SEPARATOR) * FNV_PRIME
    }

    private fun mix64(input: Long): Long {
        var value = input + MIX_GAMMA
        value = (value xor (value ushr 30)) * MIX_MULTIPLIER_1
        value = (value xor (value ushr 27)) * MIX_MULTIPLIER_2
        return value xor (value ushr 31)
    }

    private companion object {
        const val BARON_EXTERNAL_ID = "baron"
        const val BARON_EXTRA_OUTSIDERS = 2
        const val FNV_OFFSET_BASIS = -3750763034362895579L
        const val FNV_PRIME = 1099511628211L
        const val FIELD_SEPARATOR = 255L
        const val MIX_GAMMA = -7046029254386353131L
        const val MIX_MULTIPLIER_1 = -4658895280553007687L
        const val MIX_MULTIPLIER_2 = -7723592293110705685L
    }
}
