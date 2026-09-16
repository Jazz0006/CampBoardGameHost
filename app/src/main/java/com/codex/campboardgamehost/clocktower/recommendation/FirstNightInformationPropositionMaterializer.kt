package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.rules.FixedInformationEvaluator

/**
 * Thin FN-BUNDLE adapter from already-legal Trouble Brewing player information to epistemic syntax.
 *
 * This object does not decide whether information is legal or true. Pair legality remains owned by
 * [NaturalPairInformationCandidateGenerator], numeric truth remains owned by the rules package, and
 * impaired-information policy remains owned by its caller. The only state-derived structure added
 * here is the Empath subject-seat list, delegated to [FixedInformationEvaluator.livingNeighbors].
 */
internal object TroubleBrewingFirstNightInformationPropositionMaterializer {
    private val washerwoman = RoleId("Washerwoman")
    private val librarian = RoleId("Librarian")
    private val investigator = RoleId("Investigator")
    private val chef = RoleId("Chef")
    private val empath = RoleId("Empath")
    private val pairAbilities = setOf(washerwoman, librarian, investigator)

    fun materialize(
        game: GameState,
        information: EffectDraft.PlayerInformation,
        roleDefinitions: List<RoleDefinition>,
    ): InformationProposition = when {
        information.sourceAbility in pairAbilities -> materializePairInformation(
            game = game,
            information = information,
            roleDefinitions = roleDefinitions,
        )
        information.sourceAbility == chef -> materializeChef(game, information)
        information.sourceAbility == empath -> materializeEmpath(game, information)
        else -> throw IllegalArgumentException(
            "Unsupported FN-BUNDLE-1 first-night ability ${information.sourceAbility.value}.",
        )
    }

    private fun materializePairInformation(
        game: GameState,
        information: EffectDraft.PlayerInformation,
        roleDefinitions: List<RoleDefinition>,
    ): InformationProposition = when (val value = information.value) {
        is InformationValue.PlayerPair -> {
            val shownRole = requireNotNull(value.shownRole) {
                "Pair information for ${information.sourceAbility.value} requires a shown role."
            }
            InformationProposition.AnyOf(
                value.seats.sorted().map { seat -> InformationProposition.RoleAt(seat, shownRole) },
            )
        }
        is InformationValue.NoCharacters -> {
            require(information.sourceAbility == librarian && value.characterType == CharacterType.OUTSIDER) {
                "Only Librarian zero-Outsider information is supported by the first FN-BUNDLE materializer."
            }
            noCharactersInPlay(
                game = game,
                characterType = value.characterType,
                roleDefinitions = roleDefinitions,
            )
        }
        else -> throw IllegalArgumentException(
            "Unsupported ${information.sourceAbility.value} information value ${value::class.simpleName}.",
        )
    }

    private fun noCharactersInPlay(
        game: GameState,
        characterType: CharacterType,
        roleDefinitions: List<RoleDefinition>,
    ): InformationProposition {
        val rolesOfType = roleDefinitions
            .asSequence()
            .filter { definition -> game.script in definition.scriptIds && definition.type == characterType }
            .map(RoleDefinition::id)
            .distinct()
            .sortedBy(RoleId::value)
            .toList()
        require(rolesOfType.isNotEmpty()) {
            "No script roles found for $characterType; cannot materialize zero-character information."
        }
        return InformationProposition.AllOf(
            rolesOfType.map { role ->
                InformationProposition.Not(InformationProposition.RoleInPlay(role))
            },
        )
    }

    private fun materializeChef(
        game: GameState,
        information: EffectDraft.PlayerInformation,
    ): InformationProposition.NumericResult {
        val value = information.value as? InformationValue.Number
            ?: throw IllegalArgumentException("Chef information must be numeric.")
        return InformationProposition.NumericResult(
            metric = NumericMetric.ADJACENT_EVIL_PAIRS,
            sourceSeat = information.recipientSeat,
            subjectSeats = game.players.sortedBy { it.seat }.map { it.seat },
            value = value.value,
        )
    }

    private fun materializeEmpath(
        game: GameState,
        information: EffectDraft.PlayerInformation,
    ): InformationProposition.NumericResult {
        val value = information.value as? InformationValue.Number
            ?: throw IllegalArgumentException("Empath information must be numeric.")
        val subjectSeats = FixedInformationEvaluator
            .livingNeighbors(game.players, information.recipientSeat)
            .map { it.seat }
        return InformationProposition.NumericResult(
            metric = NumericMetric.LIVING_EVIL_NEIGHBOURS,
            sourceSeat = information.recipientSeat,
            subjectSeats = subjectSeats,
            value = value.value,
        )
    }
}
