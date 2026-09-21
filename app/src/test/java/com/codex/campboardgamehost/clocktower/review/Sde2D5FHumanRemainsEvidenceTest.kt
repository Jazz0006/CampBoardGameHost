package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FortuneTellerInformationSemantics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal data class Sde2D5FHumanRemainsCandidateEvidence(
    val caseId: String,
    val primaryUrl: String,
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val game: GameState,
    val observedDemonBluffs: Set<RoleId>,
    val observedDemonBluffsLegal: Boolean,
    val redHerringSeat: Int,
    val redHerringLegal: Boolean,
    val poisonerTargetSeat: Int,
    val washerwoman: Sde2D5FExpertObservedPairDecisionEvidence,
    val fortuneTeller: Sde2D5FExpertObservedBooleanDecisionEvidence,
    val fortuneTellerTargetsLegal: Boolean,
)

internal object Sde2D5FHumanRemainsCandidateBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)

    fun build(): Sde2D5FHumanRemainsCandidateEvidence {
        val game = reconstructedNightOneGame()
        val observedBluffs = setOf(
            RoleId("Empath"),
            RoleId("Recluse"),
            RoleId("Investigator"),
        )
        val legalBluffs = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
            .map { candidate ->
                (candidate.outcome as SetupClueOutcome.DemonBluffs).roles.toSet()
            }
            .toSet()
        val redHerringSeat = 3
        val legalRedHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .map { candidate -> (candidate.outcome as SetupClueOutcome.RedHerring).seat }
            .toSet()

        val washerwoman = Sde2D5FExpertObservedPairEvidenceProjector.project(
            game = game,
            sourceSeat = 6,
            abilityRole = RoleId("Washerwoman"),
            reliability = ReliabilityState.POISONED,
            observedShownRole = RoleId("Empath"),
            observedCandidateSeats = listOf(2, 5),
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-02-washerwoman",
        )
        val targets = 1 to 2
        val fortuneTeller = Sde2D5FExpertObservedBooleanEvidenceProjector.project(
            game = game,
            sourceSeat = 4,
            abilityRole = RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = listOf(targets.first, targets.second),
            observedValue = false,
            redHerringSeat = redHerringSeat,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-02-fortune-teller",
        )

        return Sde2D5FHumanRemainsCandidateEvidence(
            caseId = "goldcand-ben-02",
            primaryUrl = "https://www.youtube.com/watch?v=dU35D9vtum8",
            verificationStatus = Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            game = game,
            observedDemonBluffs = observedBluffs,
            observedDemonBluffsLegal = observedBluffs in legalBluffs,
            redHerringSeat = redHerringSeat,
            redHerringLegal = redHerringSeat in legalRedHerrings,
            poisonerTargetSeat = 6,
            washerwoman = washerwoman,
            fortuneTeller = fortuneTeller,
            fortuneTellerTargetsLegal =
                targets in FortuneTellerInformationSemantics.legalTargetPairs(game, 4),
        )
    }

    private fun reconstructedNightOneGame(): GameState = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 20210330L,
        players = listOf(
            player(1, "Adam", "Undertaker"),
            player(2, "Isaac", "Poisoner"),
            player(3, "Blair", "Slayer"),
            player(4, "Brooke", "Fortune Teller"),
            player(5, "Carley", "Imp"),
            player(6, "Jon", "Washerwoman", poisoned = true),
            player(7, "Laurie", "Soldier"),
            player(8, "Sully", "Butler"),
        ),
    )

    private fun player(
        seat: Int,
        name: String,
        actualRole: String,
        poisoned: Boolean = false,
    ): PlayerState {
        val actual = definitionsById.getValue(RoleId(actualRole))
        return PlayerState(
            seat = seat,
            name = name,
            actualRole = actual.id,
            actualAlignment = actual.alignment,
            actualType = actual.type,
            shownRole = actual.id,
            poisoned = poisoned,
        )
    }
}

class Sde2D5FHumanRemainsEvidenceTest {
    private val evidence by lazy { Sde2D5FHumanRemainsCandidateBuilder.build() }

    @Test
    fun `Human Remains candidate reconstructs poisoned first-night state without claiming GOLD verification`() {
        assertEquals("goldcand-ben-02", evidence.caseId)
        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            evidence.verificationStatus,
        )
        assertEquals(8, evidence.game.players.size)
        assertEquals(RoleId("Poisoner"), evidence.game.playerAt(2)?.actualRole)
        assertTrue(evidence.game.playerAt(evidence.poisonerTargetSeat)?.poisoned == true)

        assertTrue(evidence.observedDemonBluffsLegal)
        assertTrue(evidence.redHerringLegal)
        assertEquals(3, evidence.redHerringSeat)
        assertTrue(evidence.fortuneTellerTargetsLegal)
    }

    @Test
    fun `poisoned Washerwoman observed Empath pair is a legal false output anchored to a Demon bluff`() {
        val observed = evidence.washerwoman.observedAlternative

        assertEquals(RoleId("Empath"), observed.shownRole)
        assertEquals(listOf(2, 5), observed.candidateSeats)
        assertEquals(SemanticTruth.FALSE, observed.semanticTruth)
        assertTrue(observed.matchesReconstructedActualWorld)
        assertTrue(emptySet() in observed.exactRegistrationWitnesses)
        assertTrue(observed.shownRole in evidence.observedDemonBluffs)

        assertEquals(RoleId("Poisoner"), evidence.game.playerAt(2)?.actualRole)
        assertEquals(RoleId("Imp"), evidence.game.playerAt(5)?.actualRole)
    }

    @Test
    fun `Human Remains Fortune Teller no is forced after fixed targets and Red Herring in the reconstructed world`() {
        assertEquals(listOf(false), evidence.fortuneTeller.alternatives.map { it.value })
        assertEquals(false, evidence.fortuneTeller.observedValue)
        assertTrue(emptySet() in evidence.fortuneTeller.observedAlternative.registrationWitnesses)
    }
}
