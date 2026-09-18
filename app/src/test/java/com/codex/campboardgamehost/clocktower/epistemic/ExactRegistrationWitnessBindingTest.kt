package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import java.io.File
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExactRegistrationWitnessBindingTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val rulesetRef = validatedRuleset.toRulesetRef(
        rulesetVersion = "sde-2b-registration-binding-test",
        sourceRevision = "official",
    )
    private val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val perceived = snapshot.gameState.players.associate { player ->
        player.seat to (player.shownRole ?: player.actualRole)
    }
    private val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)

    @Test
    fun `exact query distinguishes unbound natural and selected Spy registration witness branches`() {
        val observation = EpistemicObservation(
            observationId = "sde-2b-washerwoman-either",
            snapshotId = formal.snapshotId,
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 3,
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Empath")),
                    InformationProposition.RoleAt(4, RoleId("Empath")),
                ),
            ),
        )
        val worlds = baselineWorlds()
        val rolesById = roles.associateBy(RoleDefinition::id)
        val evaluations = worlds.associateWith { world ->
            TroubleBrewingWorldObservationEvaluator.evaluate(
                world = world,
                roles = rolesById,
                observation = observation,
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            )
        }
        val selectedSpyWitness = evaluations.values
            .asSequence()
            .flatMap { it.registrationWitnesses.asSequence() }
            .first { witness ->
                witness.any { fact ->
                    fact.reason == RegistrationReason.SPY_ABILITY &&
                        fact.registeredRole == RoleId("Empath")
                }
            }

        val unboundExpected = evaluations.values.count { it.matches }
        val naturalExpected = evaluations.values.count { result ->
            emptySet<com.codex.campboardgamehost.clocktower.domain.RegistrationFact>() in
                result.registrationWitnesses
        }
        val spyExpected = evaluations.values.count { result ->
            selectedSpyWitness in result.registrationWitnesses
        }
        assertTrue(unboundExpected > naturalExpected)
        assertTrue(spyExpected > 0)
        assertTrue(spyExpected < unboundExpected)

        val result = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = ExactHistoricalHypotheticalContext(
                initialSnapshot = snapshot,
                initialPhase = StorytellerPhase.FIRST_NIGHT,
                initialRound = 1,
                actionTimeline = ActionFactTimeline(),
                perceivedRolesBySeat = perceived,
                observationLog = EpistemicObservationLog(),
                hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
                roleDefinitions = roles,
            ),
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "unbound",
                    recipientSeat = 1,
                    observations = listOf(observation),
                ),
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "natural",
                    recipientSeat = 1,
                    observations = listOf(observation),
                    registrationWitnessBindings = listOf(
                        ExactRegistrationWitnessBinding(
                            observationId = observation.observationId,
                            registrations = emptySet(),
                        ),
                    ),
                ),
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "spy",
                    recipientSeat = 1,
                    observations = listOf(observation),
                    registrationWitnessBindings = listOf(
                        ExactRegistrationWitnessBinding(
                            observationId = observation.observationId,
                            registrations = selectedSpyWitness,
                        ),
                    ),
                ),
            ),
        )

        assertTrue(result is ExactHypotheticalObservationBundleEvaluation.Ready)
        val diagnostics = (result as ExactHypotheticalObservationBundleEvaluation.Ready)
            .diagnostics.associateBy(ExactHypotheticalObservationBundleDiagnostics::bundleId)
        assertEquals(exact(unboundExpected), diagnostics.getValue("unbound").after)
        assertEquals(exact(naturalExpected), diagnostics.getValue("natural").after)
        assertEquals(exact(spyExpected), diagnostics.getValue("spy").after)
    }

    private fun baselineWorlds(): List<EnumeratedWorld> {
        val knowledge = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceived,
            observationLog = EpistemicObservationLog(),
        ).first { it.recipientSeat == 1 }
        return TroubleBrewingWorldEnumerator.stream(
            rulesetRef = rulesetRef,
            knowledge = knowledge,
            roleDefinitions = roles,
        ).worlds.filter { world ->
            knowledge.setupKnowledge.all { proposition ->
                TroubleBrewingWorldObservationEvaluator.evaluateKnownFact(
                    world = world,
                    roles = roles.associateBy(RoleDefinition::id),
                    proposition = proposition,
                )
            }
        }.toList()
    }

    private fun exact(value: Int): WorldCardinality.Exact =
        WorldCardinality.Exact(BigInteger.valueOf(value.toLong()))
}
