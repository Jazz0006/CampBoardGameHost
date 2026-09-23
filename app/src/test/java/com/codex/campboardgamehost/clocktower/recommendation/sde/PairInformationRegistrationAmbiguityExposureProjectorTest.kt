package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.EffectDraft
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.InformationValue
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicObservation
import com.codex.campboardgamehost.clocktower.epistemic.ObservationReliability
import com.codex.campboardgamehost.clocktower.epistemic.ObservationVisibility
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.NaturalPairInformationCandidateGenerator
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalCandidate
import com.codex.campboardgamehost.clocktower.recommendation.PairInformationLegalDomain
import com.codex.campboardgamehost.clocktower.recommendation.TroubleBrewingFirstNightInformationPropositionMaterializer
import org.junit.Assert.assertTrue
import org.junit.Test

class PairInformationRegistrationAmbiguityExposureProjectorTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `actual registration-capable role exposure is avoidable when registered truth preserves ambiguity`() {
        val fixture = project(
            game = game(
                player(1, "Investigator", CharacterType.TOWNSFOLK),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Spy", CharacterType.MINION),
                player(4, "Recluse", CharacterType.OUTSIDER),
                player(5, "Imp", CharacterType.DEMON),
            ),
            abilityRole = RoleId("Investigator"),
        )
        val target = RoleFunctionExposureTargetRef(
            seat = 3,
            role = RoleId("Spy"),
            recipientSeat = 1,
            capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
        )

        val projected = RoleFunctionExposureFeaturesProjector.project(fixture.evidence)
        val exposing = projected.values.filter { target in it.directlyExposedTargets }

        assertTrue(exposing.isNotEmpty())
        assertTrue(exposing.all { target in it.avoidableExposureTargets })
        assertTrue(exposing.all { target !in it.forcedExposureTargets })

        val registeredAlternativeIds = fixture.legal
            .filter { candidate ->
                candidate.registrations.any { registration ->
                    registration.reason == RegistrationReason.RECLUSE_ABILITY
                }
            }
            .mapTo(linkedSetOf(), PairInformationLegalCandidate::candidateId)
        assertTrue(registeredAlternativeIds.isNotEmpty())
        assertTrue(registeredAlternativeIds.all { candidateId ->
            target !in projected.getValue(candidateId).directlyExposedTargets
        })
        assertTrue(fixture.exact
            .filter { it.candidateId in registeredAlternativeIds }
            .all { exact ->
                exact.registrationWitnessBindings.single().registrations.isNotEmpty()
            })
    }

    @Test
    fun `actual registration-capable role exposure is forced when every legal clue names it`() {
        val fixture = project(
            game = game(
                player(1, "Investigator", CharacterType.TOWNSFOLK),
                player(2, "Chef", CharacterType.TOWNSFOLK),
                player(3, "Spy", CharacterType.MINION),
                player(4, "Saint", CharacterType.OUTSIDER),
                player(5, "Imp", CharacterType.DEMON),
            ),
            abilityRole = RoleId("Investigator"),
        )
        val target = RoleFunctionExposureTargetRef(
            seat = 3,
            role = RoleId("Spy"),
            recipientSeat = 1,
            capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
        )

        val projected = RoleFunctionExposureFeaturesProjector.project(fixture.evidence)

        assertTrue(projected.isNotEmpty())
        assertTrue(projected.values.all { it.directlyExposedTargets == setOf(target) })
        assertTrue(projected.values.all { it.forcedExposureTargets == setOf(target) })
        assertTrue(projected.values.all { it.avoidableExposureTargets.isEmpty() })
    }

    @Test
    fun `registration ambiguity projection is symmetric for another pair ability without role-name policy`() {
        val fixture = project(
            game = game(
                player(1, "Librarian", CharacterType.TOWNSFOLK),
                player(2, "Spy", CharacterType.MINION),
                player(3, "Recluse", CharacterType.OUTSIDER),
                player(4, "Chef", CharacterType.TOWNSFOLK),
                player(5, "Imp", CharacterType.DEMON),
            ),
            abilityRole = RoleId("Librarian"),
        )
        val target = RoleFunctionExposureTargetRef(
            seat = 3,
            role = RoleId("Recluse"),
            recipientSeat = 1,
            capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
        )

        val projected = RoleFunctionExposureFeaturesProjector.project(fixture.evidence)

        assertTrue(projected.values.any { target in it.directlyExposedTargets })
        assertTrue(projected.values.any { target !in it.directlyExposedTargets })
        assertTrue(projected.values
            .filter { target in it.directlyExposedTargets }
            .all { target in it.avoidableExposureTargets })
    }

    private fun project(
        game: GameState,
        abilityRole: RoleId,
    ): ProjectionFixture {
        val natural = NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace(
            game = game,
            sourceSeat = 1,
            abilityRole = abilityRole,
            roleDefinitions = roles,
        )
        val legal = PairInformationLegalDomain.generate(
            game = game,
            roleDefinitions = roles,
            sourceSeat = 1,
            abilityRole = abilityRole,
            reliability = ReliabilityState.RELIABLE,
        )
        val exact = legal.map { candidate ->
            PairInformationExactConsequenceAdapter.fromLegalCandidate(
                candidate = candidate,
                observation = pairObservation(
                    game = game,
                    abilityRole = abilityRole,
                    candidate = candidate,
                ),
            )
        }

        return ProjectionFixture(
            legal = legal,
            exact = exact,
            evidence = PairInformationRegistrationAmbiguityExposureProjector.project(
                game = game,
                naturalCandidates = natural,
                legalCandidates = legal,
                exactCandidates = exact,
            ),
        )
    }

    private fun pairObservation(
        game: GameState,
        abilityRole: RoleId,
        candidate: PairInformationLegalCandidate,
    ): EpistemicObservation {
        val shownRole = requireNotNull(candidate.outcome.shownRole)
        val proposition = TroubleBrewingFirstNightInformationPropositionMaterializer.materialize(
            game = game,
            information = EffectDraft.PlayerInformation(
                recipientSeat = 1,
                sourceAbility = abilityRole,
                value = InformationValue.PlayerPair(
                    shownRole = shownRole,
                    seats = candidate.outcome.candidateSeats,
                ),
            ),
            roleDefinitions = roles,
        )
        return EpistemicObservation(
            observationId = "sde-3b5:" + candidate.candidateId,
            snapshotId = "sde-3b5-pair-registration-exposure",
            phase = StorytellerPhase.FIRST_NIGHT,
            round = 1,
            sequence = 5,
            sourceSeat = 1,
            sourceAbility = abilityRole,
            visibility = ObservationVisibility.PRIVATE,
            recipientSeats = setOf(1),
            reliability = ObservationReliability.RECEIVED_AS_FUNCTIONING,
            proposition = proposition,
        )
    }

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20260923L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(role),
    )

    private data class ProjectionFixture(
        val legal: List<PairInformationLegalCandidate>,
        val exact: List<ExactConsequenceCandidate>,
        val evidence: List<RoleFunctionExposureCandidateEvidence>,
    )
}
