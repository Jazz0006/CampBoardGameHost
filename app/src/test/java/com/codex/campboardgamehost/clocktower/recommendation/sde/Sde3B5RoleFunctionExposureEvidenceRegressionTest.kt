package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import com.codex.campboardgamehost.clocktower.review.Sde2D5FLiveAndImpPersonCandidateBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bounded E2 regression from goldcand-ben-03 (Live and Imp-Person).
 *
 * This test uses the existing executable reconstruction only to validate the generic exposure
 * feature shape. It does not treat the observed Storyteller choice as a preference label.
 */
class Sde3B5RoleFunctionExposureEvidenceRegressionTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `goldcand ben03 direct Librarian reveal is avoidable exposure of a later registration capable subject`() {
        val evidence = Sde2D5FLiveAndImpPersonCandidateBuilder.build()
        assertEquals("goldcand-ben-03", evidence.caseId)

        val game = evidence.game
        val sourceSeat = evidence.librarian.sourceSeat
        val abilityRole = evidence.librarian.abilityRole
        val natural = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            roleDefinitions = roles,
        )
        val legal = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = sourceSeat,
            abilityRole = abilityRole,
            reliability = ReliabilityState.RELIABLE,
        )
        val exact = legal.map { candidate ->
            PairInformationExactConsequenceAdapter.fromLegalCandidate(
                candidate = candidate,
                observation = observation(
                    game = game,
                    sourceSeat = sourceSeat,
                    abilityRole = abilityRole,
                    candidate = candidate,
                ),
            )
        }

        val baseEvidence = PairInformationRegistrationAmbiguityExposureProjector.project(
            game = game,
            naturalCandidates = natural,
            legalCandidates = legal,
            exactCandidates = exact,
        )
        val projected = RoleFunctionExposureFeaturesProjector.project(baseEvidence)

        val observed = evidence.librarian.observedAlternative
        val observedFeatures = projected.getValue(observed.candidateId)
        val registrationSubjectSeat = 8
        val registrationSubjectRole = requireNotNull(game.playerAt(registrationSubjectSeat)).actualRole
        val target = RoleFunctionExposureTargetRef(
            seat = registrationSubjectSeat,
            role = registrationSubjectRole,
            recipientSeat = sourceSeat,
            capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
        )

        assertEquals(RoleId("Recluse"), registrationSubjectRole)
        assertTrue(target in observedFeatures.directlyExposedTargets)
        assertTrue(target in observedFeatures.newlyExposedTargets)
        assertTrue(target in observedFeatures.avoidableExposureTargets)
        assertFalse(target in observedFeatures.forcedExposureTargets)
        assertTrue(observedFeatures.alreadyExposedTargets.isEmpty())
        assertTrue(observedFeatures.confirmationAmplifiedTargets.isEmpty())

        assertTrue(
            legal.any { candidate ->
                target !in projected.getValue(candidate.candidateId).directlyExposedTargets
            },
        )

        val observedLaterRegistration = evidence.fortuneTeller.observedAlternative
            .registrationWitnesses
            .any { witness ->
                witness.any { registration ->
                    registration.subjectSeat == registrationSubjectSeat &&
                        registration.reason == RegistrationReason.RECLUSE_ABILITY
                }
            }
        assertTrue(observedLaterRegistration)
    }

    private fun observation(
        game: com.codex.campboardgamehost.clocktower.domain.GameState,
        sourceSeat: Int,
        abilityRole: RoleId,
        candidate: PairInformationLegalCandidate,
    ): EpistemicObservation {
        val shownRole = requireNotNull(candidate.outcome.shownRole)
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = sourceSeat,
                sourceAbility = abilityRole,
                value = InformationValue.PlayerPair(
                    shownRole = shownRole,
                    seats = candidate.outcome.candidateSeats,
                ),
            ),
            roleDefinitions = roles,
        )
        return EpistemicObservation(
            observationId = "sde-3b5-e2:" + candidate.candidateId,
            snapshotId = "sde-3b5-e2-goldcand-ben-03",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 1,
            sourceSeat = sourceSeat,
            sourceAbility = abilityRole,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(sourceSeat),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
    }
}
