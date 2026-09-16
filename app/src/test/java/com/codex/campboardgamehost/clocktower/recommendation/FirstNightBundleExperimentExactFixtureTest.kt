package com.codex.campboardgamehost.clocktower.recommendation

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4PlayerKnowledgeFactory
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedHistoricalExactBaseline
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorld
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.epistemic.TimelineBoundActionFact
import com.codex.campboardgamehost.clocktower.epistemic.TimelinePoint
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingWorldObservationEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.WorldCardinality
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightBundleExperimentExactFixtureTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "fn-bundle-1-exact-fixture",
        sourceRevision = "official",
    )
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rolesById = roles.associateBy { it.id }

    @Test
    fun `healthy public claims constrain truthful good speakers while preserving evil bluff worlds`() {
        val game = game(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Empath", CharacterType.TOWNSFOLK),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-1-five-player",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef,
            gameState = game,
        )
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceived = game.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val timeline = timelineOf(emptyList())
        val observationLog = EpistemicObservationLog()
        val information = listOf(
            information(1, "Washerwoman", InformationValue.PlayerPair(RoleId("Empath"), listOf(2, 4))),
            information(2, "Empath", InformationValue.Number(0)),
        )
        val entries = information.mapIndexed { index, value ->
            FirstNightInformationBundleEntry(
                entryId = "info-${index + 1}",
                control = if (value.sourceAbility == RoleId("Empath")) {
                    FirstNightBundleEntryControl.RULE_DETERMINED
                } else {
                    FirstNightBundleEntryControl.STORYTELLER_CONTROLLED
                },
                observation = observation(
                    formal = formal,
                    information = value,
                    sequence = index,
                    proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                        game = game,
                        information = value,
                        roleDefinitions = roles,
                    ),
                ),
                profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
            )
        }
        val bundle = FirstNightInformationBundle("healthy-five-player", entries)
        val context = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = timeline,
            perceivedRolesBySeat = perceived,
            observationLog = observationLog,
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )

        val evaluation = FirstNightBundleExperimentEvaluator.evaluateBeginnerPublicGoodInfo(
            validatedRuleset = validatedRuleset,
            context = context,
            bundle = bundle,
            evaluationRecipientSeats = setOf(1),
        )

        assertTrue(evaluation is FirstNightBundleExperimentEvaluation.Ready)
        val ready = evaluation as FirstNightBundleExperimentEvaluation.Ready
        assertEquals(2, ready.publicObservationCount)
        val diagnostic = ready.recipientDiagnostics.single()
        val baseline = baselineWorlds(
            validatedRuleset = validatedRuleset,
            formal = formal,
            perceived = perceived,
            timeline = timeline,
            observationLog = observationLog,
        )
        val projected = FirstNightPublicGoodInfoProjection.project(bundle)
        assertTrue(projected.all { it.proposition is InformationProposition.AnyOf })
        assertTrue(projected.none { it.proposition is InformationProposition.ShownRoleAt })
        assertTrue(projected.all { it.reliability == ObservationReliability.NOT_ABILITY_INFORMATION })

        val expectedAfter = matchingWorldCount(baseline, projected)
        assertEquals(exact(baseline.size), diagnostic.before)
        assertEquals(exact(expectedAfter), diagnostic.after)
        assertTrue(diagnostic.after.value > BigInteger.ZERO)
        assertTrue(diagnostic.after.value < diagnostic.before.value)

        val empathClaim = projected.single { it.sourceSeat == 2 }
        val evilLiarWorld = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Washerwoman"),
                2 to RoleId("Poisoner"),
                3 to RoleId("Chef"),
                4 to RoleId("Empath"),
                5 to RoleId("Imp"),
            ),
            shownRolesBySeat = linkedMapOf(
                1 to RoleId("Washerwoman"),
                2 to RoleId("Poisoner"),
                3 to RoleId("Chef"),
                4 to RoleId("Empath"),
                5 to RoleId("Imp"),
            ),
        )
        assertTrue(
            "An evil speaker must be able to make the same public claim without the claimed identity being oracle truth.",
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = evilLiarWorld,
                roles = rolesById,
                observation = empathClaim,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches,
        )
    }

    @Test
    fun `healthy public claim model does not silently treat Drunk false information as truthful exact evidence`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Chef"),
            player(2, "Washerwoman", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Mayor", CharacterType.TOWNSFOLK),
            player(5, "Poisoner", CharacterType.MINION),
            player(6, "Imp", CharacterType.DEMON),
        )
        val snapshot = GameSnapshot(
            gameId = "fn-bundle-1-drunk-six-player",
            gameStateRevision = 0,
            playerInputRevision = 0,
            gameSeed = game.seed,
            rulesetRef = rulesetRef,
            gameState = game,
        )
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val falseChef = information(1, "Chef", InformationValue.Number(0))
        val bundle = FirstNightInformationBundle(
            bundleId = "drunk-shown-chef",
            entries = listOf(
                FirstNightInformationBundleEntry(
                    entryId = "drunk-chef-clue",
                    control = FirstNightBundleEntryControl.STORYTELLER_CONTROLLED,
                    observation = observation(
                        formal = formal,
                        information = falseChef,
                        sequence = 0,
                        proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
                            game = game,
                            information = falseChef,
                            roleDefinitions = roles,
                        ),
                    ),
                    profileExposure = FirstNightBundleProfileExposure.PUBLIC_GOOD_INFO,
                ),
            ),
        )
        val projected = FirstNightPublicGoodInfoProjection.project(bundle)
        assertEquals(1, projected.size)
        val publicClaim = projected.single()
        assertTrue(publicClaim.proposition is InformationProposition.AnyOf)
        assertEquals(ObservationReliability.NOT_ABILITY_INFORMATION, publicClaim.reliability)
        assertEquals(null, publicClaim.sourceAbility)
        assertFalse(projected.any { it.proposition == InformationProposition.RoleAt(1, RoleId("Drunk")) })

        val drunkWorld = EnumeratedWorld(
            rolesBySeat = linkedMapOf(
                1 to RoleId("Drunk"),
                2 to RoleId("Washerwoman"),
                3 to RoleId("Empath"),
                4 to RoleId("Mayor"),
                5 to RoleId("Poisoner"),
                6 to RoleId("Imp"),
            ),
            shownRolesBySeat = linkedMapOf(
                1 to RoleId("Chef"),
                2 to RoleId("Washerwoman"),
                3 to RoleId("Empath"),
                4 to RoleId("Mayor"),
                5 to RoleId("Poisoner"),
                6 to RoleId("Imp"),
            ),
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_DRUNK),
        )
        assertEquals(AbilityState.MALFUNCTIONING_DRUNK, drunkWorld.abilityStatesBySeat[1])

        assertFalse(
            "Drunk false-info public claims are a later staged model, not healthy-stage truthful evidence.",
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = drunkWorld,
                roles = rolesById,
                observation = publicClaim,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches,
        )
        assertFalse(
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = drunkWorld,
                roles = rolesById,
                observation = publicClaim,
                hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            ).matches,
        )

        val strictShownObservation = publicClaim.copy(
            observationId = "strict-shown-chef",
            sourceSeat = null,
            proposition = InformationProposition.ShownRoleAt(1, RoleId("Chef")),
        )
        assertTrue(
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = drunkWorld,
                roles = rolesById,
                observation = strictShownObservation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches,
        )
        assertTrue(
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = drunkWorld,
                roles = rolesById,
                observation = strictShownObservation,
                hypothesis = EpistemicHypothesis.FUNCTIONING_ONLY,
            ).matches,
        )

        val wrongShownWorld = drunkWorld.copy(
            shownRolesBySeat = drunkWorld.shownRolesBySeat + (1 to RoleId("Empath")),
        )
        assertFalse(
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = wrongShownWorld,
                roles = rolesById,
                observation = strictShownObservation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches,
        )
    }

    private fun matchingWorldCount(
        worlds: List<EnumeratedWorld>,
        observations: List<EpistemicObservation>,
    ): Int = worlds.count { world ->
        observations.all { projectedObservation ->
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = projectedObservation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            ).matches
        }
    }

    private fun baselineWorlds(
        validatedRuleset: ValidatedClocktowerRuleset,
        formal: FormalGameState,
        perceived: Map<Int, RoleId>,
        timeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ) = EnumeratedHistoricalExactBaseline.build(
        validatedRuleset = validatedRuleset,
        rulesetRef = rulesetRef,
        setupKnowledge = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceived,
            observationLog = observationLog,
        ).first { it.recipientSeat == 1 },
        hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
        roleDefinitions = roles,
        initialPhase = StorytellerPhase.FIRST_NIGHT,
        initialRound = 1,
        actionTimeline = timeline,
        observationLog = observationLog,
    ).worldSet.enumeratedWorlds()

    private fun observation(
        formal: FormalGameState,
        information: EffectDraft.PlayerInformation,
        sequence: Int,
        proposition: InformationProposition,
    ) = EpistemicObservation(
        observationId = "private-${information.sourceAbility.value.lowercase().replace(' ', '-')}-${information.recipientSeat}",
        snapshotId = formal.snapshotId,
        phase = StorytellerPhase.FIRST_NIGHT,
        round = 1,
        sequence = sequence,
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

    private fun exact(count: Int): WorldCardinality.Exact =
        WorldCardinality.Exact(BigInteger.valueOf(count.toLong()))

    private fun timelineOf(facts: List<ActionFact>): ActionFactTimeline =
        ActionFactTimeline(
            facts.map { fact ->
                TimelineBoundActionFact(
                    fact = fact,
                    point = TimelinePoint(
                        phase = StorytellerPhase.FIRST_NIGHT,
                        round = 1,
                        sequence = fact.sequence.toInt(),
                        globalSequence = fact.sequence,
                    ),
                )
            },
        )
}
