package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FortuneTellerInformationSemantics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal data class Sde2D5FLiveAndImpPersonCandidateEvidence(
    val caseId: String,
    val primaryUrl: String,
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val game: GameState,
    val observedDemonBluffsLegal: Boolean,
    val redHerringSeat: Int,
    val redHerringLegal: Boolean,
    val poisonerTargetSeat: Int,
    val librarian: Sde2D5FExpertObservedPairDecisionEvidence,
    val chef: Sde2D5FExpertObservedNumericDecisionEvidence,
    val fortuneTeller: Sde2D5FExpertObservedBooleanDecisionEvidence,
    val fortuneTellerTargetsLegal: Boolean,
)

internal object Sde2D5FLiveAndImpPersonCandidateBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)

    fun build(): Sde2D5FLiveAndImpPersonCandidateEvidence {
        val game = reconstructedNightOneGame()
        val observedBluffs = setOf(
            RoleId("Investigator"),
            RoleId("Empath"),
            RoleId("Saint"),
        )
        val legalBluffs = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
            .map { candidate ->
                (candidate.outcome as SetupClueOutcome.DemonBluffs).roles.toSet()
            }
            .toSet()
        val redHerringSeat = 2
        val legalRedHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .map { candidate -> (candidate.outcome as SetupClueOutcome.RedHerring).seat }
            .toSet()
        val librarian = Sde2D5FExpertObservedPairEvidenceProjector.project(
            game = game,
            sourceSeat = 3,
            abilityRole = RoleId("Librarian"),
            reliability = ReliabilityState.RELIABLE,
            observedShownRole = RoleId("Recluse"),
            observedCandidateSeats = listOf(5, 8),
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-03-librarian",
        )
        val chef = Sde2D5FExpertObservedNumericEvidenceProjector.project(
            game = game,
            sourceSeat = 5,
            abilityRole = RoleId("Chef"),
            reliability = ReliabilityState.RELIABLE,
            observedValue = 1,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-03-chef",
        )
        val targets = 5 to 8
        val fortuneTeller = Sde2D5FExpertObservedBooleanEvidenceProjector.project(
            game = game,
            sourceSeat = 2,
            abilityRole = RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = listOf(targets.first, targets.second),
            observedValue = true,
            redHerringSeat = redHerringSeat,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-03-fortune-teller",
        )

        return Sde2D5FLiveAndImpPersonCandidateEvidence(
            caseId = "goldcand-ben-03",
            primaryUrl = "https://www.youtube.com/watch?v=m14N28Lq-jM",
            verificationStatus = Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            game = game,
            observedDemonBluffsLegal = observedBluffs in legalBluffs,
            redHerringSeat = redHerringSeat,
            redHerringLegal = redHerringSeat in legalRedHerrings,
            poisonerTargetSeat = 6,
            librarian = librarian,
            chef = chef,
            fortuneTeller = fortuneTeller,
            fortuneTellerTargetsLegal =
                targets in FortuneTellerInformationSemantics.legalTargetPairs(game, 2),
        )
    }

    private fun reconstructedNightOneGame(): GameState = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 20220514L,
        players = listOf(
            player(1, "Oli", "Baron"),
            player(2, "Carley", "Fortune Teller"),
            player(3, "Jon", "Librarian"),
            player(4, "Brooke", "Drunk", shownRole = "Undertaker"),
            player(5, "Adam", "Chef"),
            player(6, "Isaac", "Monk", poisoned = true),
            player(7, "Sullivan", "Poisoner"),
            player(8, "Laurie", "Recluse"),
            player(9, "Dom", "Mayor"),
            player(10, "Blair", "Imp"),
        ),
    )

    private fun player(
        seat: Int,
        name: String,
        actualRole: String,
        shownRole: String = actualRole,
        poisoned: Boolean = false,
    ): PlayerState {
        val actual = definitionsById.getValue(RoleId(actualRole))
        return PlayerState(
            seat = seat,
            name = name,
            actualRole = actual.id,
            actualAlignment = actual.alignment,
            actualType = actual.type,
            shownRole = RoleId(shownRole),
            poisoned = poisoned,
        )
    }
}

class Sde2D5FLiveAndImpPersonEvidenceTest {
    private val evidence by lazy { Sde2D5FLiveAndImpPersonCandidateBuilder.build() }

    @Test
    fun `Live and Imp-Person candidate keeps primary verification pending and setup choices legal`() {
        assertEquals("goldcand-ben-03", evidence.caseId)
        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            evidence.verificationStatus,
        )
        assertEquals(10, evidence.game.players.size)
        assertEquals(RoleId("Drunk"), evidence.game.playerAt(4)?.actualRole)
        assertEquals(RoleId("Undertaker"), evidence.game.playerAt(4)?.shownRole)
        assertTrue(evidence.game.playerAt(evidence.poisonerTargetSeat)?.poisoned == true)

        assertTrue(evidence.observedDemonBluffsLegal)
        assertTrue(evidence.redHerringLegal)
        assertEquals(2, evidence.redHerringSeat)
        assertTrue(evidence.fortuneTellerTargetsLegal)
    }

    @Test
    fun `Live Librarian observed Recluse pair is one legal choice rather than a forced calibration label`() {
        val observed = evidence.librarian.observedAlternative

        assertEquals(RoleId("Recluse"), observed.shownRole)
        assertEquals(listOf(5, 8), observed.candidateSeats)
        assertTrue(observed.matchesReconstructedActualWorld)
        assertTrue(emptySet() in observed.exactRegistrationWitnesses)
        assertTrue(evidence.librarian.alternatives.size > 1)
    }

    @Test
    fun `Live Chef one uses natural witness while Recluse evil registration remains a legal two counterfactual`() {
        val alternatives = evidence.chef.alternatives.associateBy { it.value }

        assertEquals(setOf(1, 2), alternatives.keys)
        assertEquals(1, evidence.chef.observedValue)
        assertTrue(emptySet() in alternatives.getValue(1).registrationWitnesses)

        val two = alternatives.getValue(2)
        assertFalse(emptySet() in two.registrationWitnesses)
        assertTrue(
            two.registrationWitnesses.any { witness ->
                witness.size == 1 &&
                    witness.single().subjectSeat == 8 &&
                    witness.single().reason == RegistrationReason.RECLUSE_ABILITY
            },
        )
    }

    @Test
    fun `Live Fortune Teller yes uses Recluse Demon witness while no remains legal in the same interaction`() {
        val alternatives = evidence.fortuneTeller.alternatives.associateBy { it.value }

        assertEquals(setOf(false, true), alternatives.keys)
        assertTrue(emptySet() in alternatives.getValue(false).registrationWitnesses)

        val yes = alternatives.getValue(true)
        assertFalse(emptySet() in yes.registrationWitnesses)
        assertTrue(
            yes.registrationWitnesses.any { witness ->
                witness.size == 1 &&
                    witness.single().subjectSeat == 8 &&
                    witness.single().reason == RegistrationReason.RECLUSE_ABILITY
            },
        )
    }
}
