package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.A4RuntimeFixtures
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicHypothesis
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservationLog
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalContext
import com.codex.campboardgamehost.clocktower.epistemic.ExactHistoricalHypotheticalObservationBundleEvaluator
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleEvaluation
import com.codex.campboardgamehost.clocktower.epistemic.ExactHypotheticalObservationBundleQuery
import com.codex.campboardgamehost.clocktower.epistemic.ExactRegistrationWitnessBinding
import com.codex.campboardgamehost.clocktower.epistemic.FormalGameState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationDomain
import com.codex.campboardgamehost.clocktower.rules.TroubleBrewingRegistrationSubject
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2RegistrationWitnessForwardingTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val validatedRuleset = catalog.ruleset(ClocktowerScript.TroubleBrewing)
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `pair legal candidate projects its selected Spy witness without re-deciding legality`() {
        val game = pairGame()
        val legal = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
            reliability = ReliabilityState.RELIABLE,
        )
        val spyCandidate = legal.single { candidate ->
            candidate.semanticTruth == SemanticTruth.TRUE &&
                candidate.outcome.shownRole == RoleId("Chef") &&
                candidate.outcome.candidateSeats == listOf(2, 3) &&
                candidate.registrations.isNotEmpty()
        }
        val observation = pairObservation(
            game = game,
            candidateId = spyCandidate.candidateId,
            shownRole = requireNotNull(spyCandidate.outcome.shownRole),
            seats = spyCandidate.outcome.candidateSeats,
        )

        val exact = PairInformationExactConsequenceAdapter.fromLegalCandidate(
            candidate = spyCandidate,
            observation = observation,
        )

        assertEquals(spyCandidate.candidateId, exact.candidateId)
        assertEquals(1, exact.registrationWitnessBindings.size)
        val binding = exact.registrationWitnessBindings.single()
        assertEquals(observation.observationId, binding.observationId)
        assertEquals(spyCandidate.registrations.toSet(), binding.registrations)
        assertEquals(listOf(observation), exact.observations)

        // The adapter binds an already-authoritative witness. It does not mutate or reclassify the game.
        assertEquals(RoleId("Spy"), game.playerAt(2)?.actualRole)
    }

    @Test
    fun `natural pair candidate binds the explicit empty witness rather than existential registration`() {
        val game = pairGame()
        val legal = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = RoleId("Washerwoman"),
            reliability = ReliabilityState.RELIABLE,
        )
        val natural = legal.single { candidate ->
            candidate.semanticTruth == SemanticTruth.TRUE &&
                candidate.outcome.shownRole == RoleId("Empath") &&
                candidate.outcome.candidateSeats == listOf(2, 3) &&
                candidate.registrations.isEmpty()
        }
        val observation = pairObservation(
            game = game,
            candidateId = natural.candidateId,
            shownRole = requireNotNull(natural.outcome.shownRole),
            seats = natural.outcome.candidateSeats,
        )

        val exact = PairInformationExactConsequenceAdapter.fromLegalCandidate(
            candidate = natural,
            observation = observation,
        )

        assertEquals(1, exact.registrationWitnessBindings.size)
        assertTrue(exact.registrationWitnessBindings.single().registrations.isEmpty())
    }

    @Test
    fun `SDE forwards selected registration witness exactly to the epistemic evaluator`() {
        val rulesetRef = validatedRuleset.toRulesetRef(
            rulesetVersion = "sde-2b-witness-forwarding",
            sourceRevision = "official",
        )
        val snapshot = A4RuntimeFixtures.snapshot().copy(rulesetRef = rulesetRef)
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceived = snapshot.gameState.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val observation = EpistemicObservation(
            observationId = "sde-2b-spy-empath",
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
        val empathDefinition = roles.single { it.id == RoleId("Empath") }
        val selected = requireNotNull(
            TroubleBrewingRegistrationDomain.resolve(
                subject = TroubleBrewingRegistrationSubject(
                    seat = 4,
                    actualRole = RoleId("Spy"),
                    actualAlignment = Alignment.EVIL,
                    actualType = CharacterType.MINION,
                ),
                allowedRoles = listOf(empathDefinition),
                question = RegistrationQuestion.ROLE,
            ).special.single().registrationFact(
                interactionId = "generator-local-registration",
                question = RegistrationQuestion.ROLE,
            ),
        )
        val binding = ExactRegistrationWitnessBinding(
            observationId = observation.observationId,
            registrations = setOf(selected),
        )
        val exactContext = ExactHistoricalHypotheticalContext(
            initialSnapshot = snapshot,
            initialPhase = StorytellerPhase.FIRST_NIGHT,
            initialRound = 1,
            actionTimeline = ActionFactTimeline(),
            perceivedRolesBySeat = perceived,
            observationLog = EpistemicObservationLog(),
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roles,
        )

        val direct = ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
            validatedRuleset = validatedRuleset,
            context = exactContext,
            queries = listOf(
                ExactHypotheticalObservationBundleQuery(
                    bundleId = "direct",
                    recipientSeat = 1,
                    observations = listOf(observation),
                    registrationWitnessBindings = listOf(binding),
                ),
            ),
        )
        assertTrue(direct is ExactHypotheticalObservationBundleEvaluation.Ready)
        val expected = (direct as ExactHypotheticalObservationBundleEvaluation.Ready).diagnostics.single()

        val viaSde = StorytellerDecisionEngine.evaluateExactConsequences(
            request = ExactConsequenceRequest(
                decisionId = "registration-forwarding",
                candidates = listOf(
                    ExactConsequenceCandidate(
                        candidateId = "spy-empath",
                        recipientSeat = 1,
                        observations = listOf(observation),
                        registrationWitnessBindings = listOf(binding),
                    ),
                ),
            ),
            context = ExactConsequenceContext(
                validatedRuleset = validatedRuleset,
                exactContext = exactContext,
            ),
        )

        assertTrue(viaSde is ExactConsequenceEvaluation.Ready)
        val consequence = (viaSde as ExactConsequenceEvaluation.Ready).consequences.single()
        assertEquals(expected.before, consequence.diagnostics.before)
        assertEquals(expected.after, consequence.diagnostics.after)
        assertEquals(expected.beforeStructure, consequence.diagnostics.beforeStructure)
        assertEquals(expected.afterStructure, consequence.diagnostics.afterStructure)
    }

    private fun pairObservation(
        game: GameState,
        candidateId: String,
        shownRole: RoleId,
        seats: List<Int>,
    ): EpistemicObservation {
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = 1,
                sourceAbility = RoleId("Washerwoman"),
                value = InformationValue.PlayerPair(
                    shownRole = shownRole,
                    seats = seats,
                ),
            ),
            roleDefinitions = roles,
        )
        return EpistemicObservation(
            observationId = "sde-2b-$candidateId",
            snapshotId = "sde-2b-pair-projection",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 5,
            sourceSeat = 1,
            sourceAbility = RoleId("Washerwoman"),
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
    }

    private fun pairGame(): GameState = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 20260918L,
        players = listOf(
            player(1, "Washerwoman", CharacterType.TOWNSFOLK),
            player(2, "Spy", CharacterType.MINION),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Recluse", CharacterType.OUTSIDER),
            player(5, "Imp", CharacterType.DEMON),
        ),
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ): PlayerState = PlayerState(
        seat = seat,
        name = "P$seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )
}
