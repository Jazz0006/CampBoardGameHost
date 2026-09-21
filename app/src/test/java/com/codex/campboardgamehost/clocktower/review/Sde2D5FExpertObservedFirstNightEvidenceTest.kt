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

internal data class Sde2D5FAStudInScarletCandidateEvidence(
    val caseId: String,
    val primaryUrl: String,
    val verificationStatus: Sde2D5FPrimaryVerificationStatus,
    val game: GameState,
    val observedDemonBluffs: Set<RoleId>,
    val observedDemonBluffsLegal: Boolean,
    val redHerringSeat: Int,
    val redHerringLegal: Boolean,
    val chef: Sde2D5FExpertObservedNumericDecisionEvidence,
    val drunkEmpath: Sde2D5FExpertObservedNumericDecisionEvidence,
    val fortuneTellerTargets: Pair<Int, Int>,
    val fortuneTellerTargetsLegal: Boolean,
    val fortuneTeller: Sde2D5FExpertObservedBooleanDecisionEvidence,
)

/**
 * First expert-observed D5F-B4 reconstruction candidate.
 *
 * The detailed state is reconstructed from the public episode index, while the YouTube recording is
 * the primary source. Until the material Night-1 state is checked against the primary recording,
 * this remains a GOLD candidate and must not be promoted to admitted GOLD evidence.
 */
internal object Sde2D5FAStudInScarletCandidateBuilder {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)

    fun build(): Sde2D5FAStudInScarletCandidateEvidence {
        val game = reconstructedGame()
        val observedBluffs = setOf(
            RoleId("Saint"),
            RoleId("Slayer"),
            RoleId("Soldier"),
        )
        val legalBluffs = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)
            .map { candidate ->
                (candidate.outcome as SetupClueOutcome.DemonBluffs).roles.toSet()
            }
            .toSet()
        val redHerringSeat = 9
        val legalRedHerrings = SetupCandidateGenerator.generateRedHerringCandidates(game)
            .map { candidate -> (candidate.outcome as SetupClueOutcome.RedHerring).seat }
            .toSet()

        val chefEvidence = Sde2D5FExpertObservedNumericEvidenceProjector.project(
            game = game,
            sourceSeat = 8,
            abilityRole = RoleId("Chef"),
            reliability = ReliabilityState.RELIABLE,
            observedValue = 1,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-01-chef",
        )
        val drunkEmpathEvidence = Sde2D5FExpertObservedNumericEvidenceProjector.project(
            game = game,
            sourceSeat = 9,
            abilityRole = RoleId("Empath"),
            reliability = ReliabilityState.DRUNK,
            observedValue = 0,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-01-drunk-empath",
        )
        val fortuneTellerTargets = 4 to 5
        val fortuneTellerEvidence = Sde2D5FExpertObservedBooleanEvidenceProjector.project(
            game = game,
            sourceSeat = 3,
            abilityRole = RoleId("Fortune Teller"),
            metric = BooleanMetric.DEMON_OR_RED_HERRING_PRESENT,
            subjectSeats = listOf(fortuneTellerTargets.first, fortuneTellerTargets.second),
            observedValue = true,
            redHerringSeat = redHerringSeat,
            roleDefinitions = roles,
            observationIdPrefix = "d5f-goldcand-ben-01-fortune-teller",
        )

        return Sde2D5FAStudInScarletCandidateEvidence(
            caseId = "goldcand-ben-01",
            primaryUrl = "https://www.youtube.com/watch?v=qZBvRfM3Xow",
            verificationStatus = Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            game = game,
            observedDemonBluffs = observedBluffs,
            observedDemonBluffsLegal = observedBluffs in legalBluffs,
            redHerringSeat = redHerringSeat,
            redHerringLegal = redHerringSeat in legalRedHerrings,
            chef = chefEvidence,
            drunkEmpath = drunkEmpathEvidence,
            fortuneTellerTargets = fortuneTellerTargets,
            fortuneTellerTargetsLegal =
                fortuneTellerTargets in FortuneTellerInformationSemantics.legalTargetPairs(game, 3),
            fortuneTeller = fortuneTellerEvidence,
        )
    }

    private fun reconstructedGame(): GameState = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 20210205L,
        players = listOf(
            player(1, "Luke", "Imp"),
            player(2, "Oli", "Ravenkeeper"),
            player(3, "Blair", "Fortune Teller"),
            player(4, "Tom", "Monk"),
            player(5, "Elliott", "Recluse"),
            player(6, "Laurie", "Scarlet Woman"),
            player(7, "Isaac", "Undertaker"),
            player(8, "Jon", "Chef"),
            player(9, "Sullivan", "Drunk", shownRole = "Empath"),
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

class Sde2D5FExpertObservedFirstNightEvidenceTest {
    private val evidence by lazy { Sde2D5FAStudInScarletCandidateBuilder.build() }

    @Test
    fun `A Stud candidate reconstructs observed setup choices without claiming GOLD verification`() {
        assertEquals("goldcand-ben-01", evidence.caseId)
        assertEquals(
            Sde2D5FPrimaryVerificationStatus.PRIMARY_VERIFICATION_PENDING,
            evidence.verificationStatus,
        )
        assertEquals(9, evidence.game.players.size)
        assertEquals(RoleId("Drunk"), evidence.game.playerAt(9)?.actualRole)
        assertEquals(RoleId("Empath"), evidence.game.playerAt(9)?.shownRole)

        assertTrue(evidence.observedDemonBluffsLegal)
        assertTrue(evidence.redHerringLegal)
        assertEquals(setOf(RoleId("Saint"), RoleId("Slayer"), RoleId("Soldier")), evidence.observedDemonBluffs)
        assertEquals(9, evidence.redHerringSeat)

        assertEquals(0, evidence.drunkEmpath.observedValue)
        assertEquals(listOf(0, 1, 2), evidence.drunkEmpath.alternatives.map { it.value })
        assertTrue(evidence.fortuneTellerTargetsLegal)
        assertEquals(4 to 5, evidence.fortuneTellerTargets)
    }

    @Test
    fun `A Stud Fortune Teller observed yes and unchosen no remain legal registration alternatives`() {
        val alternatives = evidence.fortuneTeller.alternatives.associateBy { it.value }

        assertEquals(setOf(false, true), alternatives.keys)
        assertTrue(evidence.fortuneTeller.observedValue)
        assertTrue(evidence.fortuneTeller.observedAlternative.value)

        val no = alternatives.getValue(false)
        assertTrue(emptySet() in no.registrationWitnesses)

        val yes = alternatives.getValue(true)
        assertFalse(emptySet() in yes.registrationWitnesses)
        assertTrue(
            yes.registrationWitnesses.any { witness ->
                witness.size == 1 &&
                    witness.single().subjectSeat == 5 &&
                    witness.single().reason == RegistrationReason.RECLUSE_ABILITY
            },
        )
    }

    @Test
    fun `numeric evidence projector preserves registration witnesses for the same Empath decision shape`() {
        val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
        val definitions = roleDefinitions.associateBy(RoleDefinition::id)
        fun player(seat: Int, roleName: String): PlayerState {
            val role = definitions.getValue(RoleId(roleName))
            return PlayerState(
                seat = seat,
                name = "P$seat",
                actualRole = role.id,
                actualAlignment = role.alignment,
                actualType = role.type,
                shownRole = role.id,
            )
        }
        val game = GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = 20260921L,
            players = listOf(
                player(1, "Recluse"),
                player(2, "Empath"),
                player(3, "Chef"),
                player(4, "Monk"),
                player(5, "Butler"),
                player(6, "Baron"),
                player(7, "Imp"),
            ),
        )

        val projected = Sde2D5FExpertObservedNumericEvidenceProjector.project(
            game = game,
            sourceSeat = 2,
            abilityRole = RoleId("Empath"),
            reliability = ReliabilityState.RELIABLE,
            observedValue = 1,
            roleDefinitions = roleDefinitions,
            observationIdPrefix = "d5f-numeric-shape-empath",
        )
        val alternatives = projected.alternatives.associateBy { it.value }

        assertEquals(setOf(0, 1), alternatives.keys)
        assertTrue(emptySet() in alternatives.getValue(0).registrationWitnesses)
        assertTrue(
            alternatives.getValue(1).registrationWitnesses.any { witness ->
                witness.size == 1 &&
                    witness.single().subjectSeat == 1 &&
                    witness.single().reason == RegistrationReason.RECLUSE_ABILITY
            },
        )
    }

    @Test
    fun `A Stud Chef observed one and unchosen zero remain separate legal value plus witness alternatives`() {
        val alternatives = evidence.chef.alternatives.associateBy { it.value }

        assertEquals(setOf(0, 1), alternatives.keys)
        assertEquals(1, evidence.chef.observedValue)
        assertEquals(1, evidence.chef.observedAlternative.value)

        val zero = alternatives.getValue(0)
        assertTrue(zero.matchesReconstructedActualWorld)
        assertTrue(emptySet() in zero.registrationWitnesses)

        val one = alternatives.getValue(1)
        assertTrue(one.matchesReconstructedActualWorld)
        assertFalse(emptySet() in one.registrationWitnesses)
        assertTrue(
            one.registrationWitnesses.any { witness ->
                witness.size == 1 &&
                    witness.single().subjectSeat == 5 &&
                    witness.single().reason == RegistrationReason.RECLUSE_ABILITY
            },
        )
    }
}
