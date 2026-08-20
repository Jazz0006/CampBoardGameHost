package com.codex.campboardgamehost.clocktower.flow

import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCatalogTeam
import com.codex.campboardgamehost.clocktower.catalog.ClocktowerCharacterDefinition
import com.codex.campboardgamehost.clocktower.catalog.NightOrderToken
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.RoleId

internal enum class ClocktowerNightFlowPhase {
    FIRST_NIGHT,
    OTHER_NIGHT,
}

internal data class ClocktowerFlowContext(
    val playerCount: Int,
    val inPlayRoleIds: Set<RoleId>,
) {
    init {
        require(playerCount > 0) { "Clocktower flow playerCount must be positive." }
        require(inPlayRoleIds.isNotEmpty()) { "Clocktower flow requires at least one in-play role." }
    }
}

/**
 * Pure R5.5 shadow planner for script-aware base night ordering.
 *
 * This planner deliberately does not resolve triggered interactions, player choices, role malfunction,
 * deaths, or UI state. Those remain separate S1 contracts. Its only responsibility is to turn the
 * normalized ruleset plus current table composition into the ordered base night tokens that are
 * eligible to exist.
 */
internal class ClocktowerFlowPlanner {
    fun planNight(
        ruleset: ValidatedClocktowerRuleset,
        phase: ClocktowerNightFlowPhase,
        context: ClocktowerFlowContext,
    ): List<NightOrderToken> {
        val scriptRoleIds = ruleset.script.characterIds.toSet()
        require(context.inPlayRoleIds.all { it in scriptRoleIds }) {
            "Clocktower flow context contains a role that is not part of the active script."
        }

        val charactersById = ruleset.characters.associateBy { it.id }
        val sourceOrder = when (phase) {
            ClocktowerNightFlowPhase.FIRST_NIGHT ->
                ruleset.script.firstNightOverride ?: derivedOrder(ruleset.characters, firstNight = true)
            ClocktowerNightFlowPhase.OTHER_NIGHT ->
                ruleset.script.otherNightOverride ?: derivedOrder(ruleset.characters, firstNight = false)
        }

        return ensureNightBoundaries(sourceOrder)
            .filter { token ->
                when (token) {
                    is NightOrderToken.Character -> token.roleId in context.inPlayRoleIds
                    NightOrderToken.System.DUSK,
                    NightOrderToken.System.DAWN -> true
                    NightOrderToken.System.MINION_INFO ->
                        phase == ClocktowerNightFlowPhase.FIRST_NIGHT &&
                            context.playerCount >= STANDARD_EVIL_INFO_MIN_PLAYERS &&
                            context.inPlayRoleIds.any { roleId ->
                                charactersById[roleId]?.team == ClocktowerCatalogTeam.MINION
                            }
                    NightOrderToken.System.DEMON_INFO ->
                        phase == ClocktowerNightFlowPhase.FIRST_NIGHT &&
                            context.playerCount >= STANDARD_EVIL_INFO_MIN_PLAYERS &&
                            context.inPlayRoleIds.any { roleId ->
                                charactersById[roleId]?.team == ClocktowerCatalogTeam.DEMON
                            }
                }
            }
    }

    private fun derivedOrder(
        characters: List<ClocktowerCharacterDefinition>,
        firstNight: Boolean,
    ): List<NightOrderToken> {
        val orderedCharacters = characters
            .mapNotNull { character ->
                val priority = if (firstNight) character.firstNightOrder else character.otherNightOrder
                character.takeIf { priority.signum() > 0 }?.let { priority to it }
            }
            .sortedWith(compareBy<Pair<java.math.BigDecimal, ClocktowerCharacterDefinition>> { it.first }
                .thenBy { it.second.externalId })
            .map { (_, character) -> NightOrderToken.Character(character.id) }

        return buildList {
            add(NightOrderToken.System.DUSK)
            if (firstNight) {
                add(NightOrderToken.System.MINION_INFO)
                add(NightOrderToken.System.DEMON_INFO)
            }
            addAll(orderedCharacters)
            add(NightOrderToken.System.DAWN)
        }
    }

    private fun ensureNightBoundaries(tokens: List<NightOrderToken>): List<NightOrderToken> = buildList {
        if (tokens.firstOrNull() != NightOrderToken.System.DUSK) {
            add(NightOrderToken.System.DUSK)
        }
        addAll(tokens)
        if (tokens.lastOrNull() != NightOrderToken.System.DAWN) {
            add(NightOrderToken.System.DAWN)
        }
    }

    private companion object {
        const val STANDARD_EVIL_INFO_MIN_PLAYERS = 7
    }
}
