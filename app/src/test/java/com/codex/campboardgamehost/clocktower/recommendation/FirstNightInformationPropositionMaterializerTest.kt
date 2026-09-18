package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.domain.YesNoAnswer
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorld
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingWorldObservationEvaluator
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightInformationPropositionMaterializerTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rolesById = roles.associateBy { it.id }

    @Test
    fun `washerwoman and investigator pairs preserve registration-aware exact semantics`() {
        val game = game(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Spy", CharacterType.MINION),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Recluse", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )
        val world = world(game)

        val washerwoman = information(
            recipientSeat = 1,
            ability = "Washerwoman",
            value = InformationValue.PlayerPair(RoleId("Librarian"), listOf(2, 3)),
        )
        val washerwomanProposition = materialize(game, washerwoman)
        assertEquals(
            InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Librarian")),
                    InformationProposition.RoleAt(3, RoleId("Librarian")),
                ),
            ),
            washerwomanProposition,
        )
        val washerwomanResult = evaluate(world, washerwoman, washerwomanProposition)
        assertTrue(washerwomanResult.matches)
        assertTrue(washerwomanResult.registrationFacts.any { it.subjectSeat == 2 })

        val investigator = information(
            recipientSeat = 3,
            ability = "Investigator",
            value = InformationValue.PlayerPair(RoleId("Poisoner"), listOf(4, 2)),
        )
        val investigatorProposition = materialize(game, investigator)
        assertEquals(
            InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Poisoner")),
                    InformationProposition.RoleAt(4, RoleId("Poisoner")),
                ),
            ),
            investigatorProposition,
        )
        val investigatorResult = evaluate(world, investigator, investigatorProposition)
        assertTrue(investigatorResult.matches)
        assertTrue(investigatorResult.registrationFacts.any { it.subjectSeat == 4 })
    }

    @Test
    fun `librarian zero outsider materialization matches the canonical actual-target legality`() {
        val noOutsiderGame = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Spy", CharacterType.MINION),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Empath", CharacterType.TOWNSFOLK),
            player(5, "Imp", CharacterType.DEMON),
        )
        val zero = information(
            recipientSeat = 1,
            ability = "Librarian",
            value = InformationValue.NoCharacters(CharacterType.OUTSIDER),
        )
        val proposition = materialize(noOutsiderGame, zero)

        val allOf = proposition as InformationProposition.AllOf
        val expectedOutsiderRoles = roles
            .filter { noOutsiderGame.script in it.scriptIds && it.type == CharacterType.OUTSIDER }
            .map { it.id }
            .toSet()
        val excludedRoles = allOf.propositions.map { clause ->
            val not = clause as InformationProposition.Not
            (not.proposition as InformationProposition.RoleInPlay).role
        }.toSet()
        assertEquals(expectedOutsiderRoles, excludedRoles)
        assertTrue(evaluate(world(noOutsiderGame), zero, proposition).matches)

        val recluseGame = game(
            player(1, "Librarian", CharacterType.TOWNSFOLK),
            player(2, "Spy", CharacterType.MINION),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Recluse", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        )
        assertFalse(evaluate(world(recluseGame), zero, proposition).matches)
    }

    @Test
    fun `chef and empath numbers use existing table and living-neighbour structure`() {
        val game = game(
            player(1, "Chef", CharacterType.TOWNSFOLK),
            player(2, "Poisoner", CharacterType.MINION),
            player(3, "Imp", CharacterType.DEMON),
            player(4, "Empath", CharacterType.TOWNSFOLK),
            player(5, "Mayor", CharacterType.TOWNSFOLK),
        )
        val world = world(game)

        val chef = information(1, "Chef", InformationValue.Number(1))
        val chefProposition = materialize(game, chef) as InformationProposition.NumericResult
        assertEquals(NumericMetric.ADJACENT_EVIL_PAIRS, chefProposition.metric)
        assertEquals(listOf(1, 2, 3, 4, 5), chefProposition.subjectSeats)
        assertEquals(1, chefProposition.value)
        assertTrue(evaluate(world, chef, chefProposition).matches)

        val empath = information(4, "Empath", InformationValue.Number(1))
        val empathProposition = materialize(game, empath) as InformationProposition.NumericResult
        assertEquals(NumericMetric.LIVING_EVIL_NEIGHBOURS, empathProposition.metric)
        assertEquals(listOf(3, 5), empathProposition.subjectSeats)
        assertEquals(1, empathProposition.value)
        assertTrue(evaluate(world, empath, empathProposition).matches)
    }

    @Test
    fun `Fortune Teller materialization preserves typed target pair and Yes No result`() {
        val yes = information(2, "Fortune Teller", InformationValue.YesNo(YesNoAnswer.YES))
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materializeFortuneTeller(
            information = yes,
            targetSeats = listOf(5, 3),
        )

        assertEquals(BooleanMetric.DEMON_OR_RED_HERRING_PRESENT, proposition.metric)
        assertEquals(2, proposition.sourceSeat)
        assertEquals(listOf(3, 5), proposition.subjectSeats)
        assertTrue(proposition.value)
    }

    @Test
    fun `representative drunk false information survives mechanically credible exact evaluation only`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Chef"),
            player(2, "Poisoner", CharacterType.MINION),
            player(3, "Imp", CharacterType.DEMON),
            player(4, "Empath", CharacterType.TOWNSFOLK),
            player(5, "Mayor", CharacterType.TOWNSFOLK),
        )
        val falseChef = information(1, "Chef", InformationValue.Number(0))
        val proposition = materialize(game, falseChef)
        val world = world(
            game = game,
            abilityStates = mapOf(1 to AbilityState.MALFUNCTIONING_DRUNK),
        )
        val observation = observation(falseChef, proposition)

        assertTrue(
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = observation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches,
        )
        assertFalse(
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = observation,
                hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            ).matches,
        )
    }

    private fun materialize(
        game: GameState,
        information: EffectDraft.PlayerInformation,
    ): InformationProposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
        game = game,
        information = information,
        roleDefinitions = roles,
    )

    private fun evaluate(
        world: EnumeratedWorld,
        information: EffectDraft.PlayerInformation,
        proposition: InformationProposition,
    ) = TroubleBrewingWorldObservationEvaluator.evaluate(
        world = world,
        roles = rolesById,
        observation = observation(information, proposition),
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
    )

    private fun observation(
        information: EffectDraft.PlayerInformation,
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = "fn-bundle-1-${information.sourceAbility.value.lowercase().replace(' ', '-')}-${information.recipientSeat}",
        snapshotId = "fn-bundle-1-fixture",
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = 0,
        sourceSeat = information.recipientSeat,
        sourceAbility = information.sourceAbility,
        visibility = ObservationVisibility.PRIVATE,
        recipientSeats = setOf(information.recipientSeat),
        reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
        proposition = proposition,
    )

    private fun information(
        recipientSeat: Int,
        ability: String,
        value: InformationValue,
    ) = EffectDraft.PlayerInformation(
        recipientSeat = recipientSeat,
        sourceAbility = RoleId(ability),
        value = value,
    )

    private fun game(vararg players: PlayerState) = GameState(
        script = TroubleBrewingFixtures.scriptId,
        players = players.toList(),
        seed = 20260916L,
    )

    private fun world(
        game: GameState,
        abilityStates: Map<Int, AbilityState> = emptyMap(),
    ) = EnumeratedWorld(
        rolesBySeat = game.players.sortedBy { it.seat }.associate { it.seat to it.actualRole },
        shownRolesBySeat = game.players.mapNotNull { player ->
            player.shownRole?.let { shown -> player.seat to shown }
        }.toMap(),
        aliveSeats = game.players.filter { it.alive }.mapTo(linkedSetOf()) { it.seat },
        abilityStatesBySeat = abilityStates,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
