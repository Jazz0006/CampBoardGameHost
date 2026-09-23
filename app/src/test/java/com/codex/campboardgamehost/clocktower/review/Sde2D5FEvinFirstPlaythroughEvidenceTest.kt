package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.ReliabilityState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.epistemic.BooleanMetric
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.rules.FortuneTellerInformationSemantics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal data class Sde2D5FEvinFirstPlaythroughCandidateEvidence(
    val caseId: String,
    val primaryUrl: String,
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val game: GameState,
    val redHerringSeat: Int,
    val redHerringLegal: Boolean,
    val observedDemonBluffs: Set<RoleId>,
    val washerwoman: Sde2D5FExpertObservedPairDecisionEvidence,
    val chef: Sde2D5FExpertObservedNumericDecisionEvidence,
    val fortuneTellerTargets: Pair<Int, Int>,
    val fortuneTellerTargetsLegal: Boolean,
    val fortuneTeller: Sde2D5FExpertObservedBooleanDecisionEvidence,
)

/**
 * Primary-verified material Night-1 slice from Evin's 2019 eight-player Trouble Brewing game.
 *
 * The postgame primary screenshot also exposes the Demon bluff icons: Recluse, Slayer, and Soldier.
 * They do not participate in the production legality domains exercised by the Washerwoman, Chef,
 * Red Herring, or fixed-target Fortune Teller observations below.
 */
internal object Sde2D5FEvinFirstPlaythroughCandidateBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)

    fun build(): Sde2D5FEvinFirstPlaythroughCandidateEvidence {
        val game = reconstructedNightOneGame()
        val redHerringSeat = 1
        val observedDemonBluffs = setOf(
            RoleId("Recluse"),
            RoleId("Slayer"),
            RoleId("Soldier"),
        )
        val legalRedHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .map { candidate -> (candidate.outcome as SetupClueOutcome.RedHerring).seat }
            .toSet()
        val washerwoman = Sde2D5FExpertObservedPairEvidenceProjector.project(
            game = game,
            sourceSeat = 3,
            abilityRole = RoleId("Washerwoman"),
            reliability = ReliabilityState.RELIABLE,
            observedShownRole = RoleId("Undertaker"),
            observedCandidateSeats = listOf(5, 7),
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-evin-01-washerwoman",
        )
        val chef = Sde2D5FExpertObservedNumericEvidenceProjector.project(
            game = game,
            sourceSeat = 1,
            abilityRole = RoleId("Chef"),
            reliability = ReliabilityState.RELIABLE,
            observedValue = 1,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-evin-01-chef",
        )
        val fortuneTellerTargets = 1 to 3
        val fortuneTeller = Sde2D5FExpertObservedBooleanEvidenceProjector.project(
            game = game,
            sourceSeat = 2,
            abilityRole = RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = listOf(fortuneTellerTargets.first, fortuneTellerTargets.second),
            observedValue = true,
            redHerringSeat = redHerringSeat,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-evin-01-fortune-teller",
        )

        return Sde2D5FEvinFirstPlaythroughCandidateEvidence(
            caseId = "goldcand-evin-01",
            primaryUrl = "https://www.youtube.com/watch?v=4sfa8_kNxsQ",
            verificationStatus = Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFIED,
            game = game,
            redHerringSeat = redHerringSeat,
            redHerringLegal = redHerringSeat in legalRedHerrings,
            observedDemonBluffs = observedDemonBluffs,
            washerwoman = washerwoman,
            chef = chef,
            fortuneTellerTargets = fortuneTellerTargets,
            fortuneTellerTargetsLegal =
                fortuneTellerTargets in FortuneTellerInformationSemantics.legalTargetPairs(game, 2),
            fortuneTeller = fortuneTeller,
        )
    }

    private fun reconstructedNightOneGame(): GameState = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 20190421L,
        players = listOf(
            player(1, "Doug", "Chef"),
            player(2, "Claire", "Fortune Teller"),
            player(3, "Sarah-Regina", "Washerwoman"),
            player(4, "Lewis", "Scarlet Woman"),
            player(5, "Julian", "Imp"),
            player(6, "Marianna", "Drunk", shownRole = "Monk"),
            player(7, "Filip", "Undertaker"),
            player(8, "Michael", "Virgin"),
        ),
    )

    private fun player(
        seat: Int,
        name: String,
        actualRole: String,
        shownRole: String = actualRole,
    ): PlayerState {
        val actual = definitionsById.getValue(RoleId(actualRole))
        return PlayerState(
            seat = seat,
            name = name,
            actualRole = actual.id,
            actualAlignment = actual.alignment,
            actualType = actual.type,
            shownRole = RoleId(shownRole),
        )
    }
}

class Sde2D5FEvinFirstPlaythroughEvidenceTest {
    private val evidence by lazy { Sde2D5FEvinFirstPlaythroughCandidateBuilder.build() }

    @Test
    fun `Evin case preserves primary-verified material Night-1 state`() {
        assertEquals("goldcand-evin-01", evidence.caseId)
        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFIED,
            evidence.verificationStatus,
        )
        assertEquals(8, evidence.game.players.size)
        assertEquals(RoleId("Drunk"), evidence.game.playerAt(6)?.actualRole)
        assertEquals(RoleId("Monk"), evidence.game.playerAt(6)?.shownRole)
        assertEquals(RoleId("Scarlet Woman"), evidence.game.playerAt(4)?.actualRole)
        assertEquals(RoleId("Imp"), evidence.game.playerAt(5)?.actualRole)
        assertTrue(evidence.redHerringLegal)
        assertEquals(1, evidence.redHerringSeat)
        assertEquals(
            setOf(RoleId("Recluse"), RoleId("Slayer"), RoleId("Soldier")),
            evidence.observedDemonBluffs,
        )
        assertTrue(evidence.fortuneTellerTargetsLegal)
        assertEquals(1 to 3, evidence.fortuneTellerTargets)
    }

    @Test
    fun `Evin Washerwoman observed Julian or Filip as Undertaker is one production-legal true clue`() {
        val observed = evidence.washerwoman.observedAlternative

        assertEquals(RoleId("Undertaker"), observed.shownRole)
        assertEquals(listOf(5, 7), observed.candidateSeats)
        assertTrue(observed.matchesReconstructedActualWorld)
        assertTrue(observed.exactRegistrationWitnesses.any { it.isEmpty() })
        assertTrue(evidence.washerwoman.alternatives.size > 1)
    }

    @Test
    fun `Evin Chef one is forced by the adjacent Scarlet Woman and Imp`() {
        assertEquals(1, evidence.chef.observedValue)
        assertEquals(setOf(1), evidence.chef.alternatives.map { it.value }.toSet())
        assertTrue(evidence.chef.observedAlternative.registrationWitnesses.any { it.isEmpty() })
    }

    @Test
    fun `Evin Fortune Teller yes is forced after player targets include the Red Herring`() {
        assertTrue(evidence.fortuneTeller.observedValue)
        assertEquals(setOf(true), evidence.fortuneTeller.alternatives.map { it.value }.toSet())
        assertTrue(evidence.fortuneTeller.observedAlternative.registrationWitnesses.any { it.isEmpty() })
    }
}
