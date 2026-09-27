package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.sde.ConfirmationChannelRef
import com.codex.campboardgamehost.clocktower.recommendation.sde.CredibilityDisruptionMechanism
import com.codex.campboardgamehost.clocktower.recommendation.sde.RedHerringSetupPrecommitAdapter
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde3D2EvinTruthCredibilityRegressionTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Evin Chef one is a strategic truth danger anchor and seat one Red Herring creates typed disruption without FT hindsight`() {
        val evin = Sde2D5FEvinFirstPlaythroughCandidateBuilder.build()
        val context = Sde2D5FExpertObservedConsequenceProjector.context(
            caseId = "sde-3d2-evin-truth-credibility",
            game = evin.game,
            roleDefinitions = roles,
        )
        val setup = RedHerringSetupPrecommitAdapter.project(
            gameId = context.exactContext.initialSnapshot.gameId,
            game = evin.game,
            roleDefinitions = roles,
            sourceRevision = InformationDecisionRevision(
                gameStateRevision = 0L,
                playerInputRevision = 0L,
            ),
        )

        val chefSource = ConfirmationChannelRef.Source(
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
        )
        assertEquals(setOf(chefSource), setup.ruleDeterminedHealthySourceRefs)
        assertEquals(
            setOf(
                ConfirmationChannelRef.Source(
                    sourceSeat = 3,
                    sourceAbility = RoleId("Washerwoman"),
                ),
            ),
            setup.unresolvedHealthySourceRefs,
        )
        assertEquals(setOf("fortune-teller-target"), setup.excludedPlayerControlledElementIds)
        assertTrue(
            setup.ruleDeterminedHealthySourceClaims.none {
                it.source.sourceAbility == RoleId("Fortune Teller")
            },
        )

        val calibration = Sde2D5FEvinFirstPlaythroughConsequenceCalibrationBuilder.build()
        assertEquals("value-1", calibration.chefObservedCandidateId)
        val observedChef = calibration.chef.alternatives
            .single { it.candidateId == calibration.chefObservedCandidateId }
        assertTrue(
            observedChef.byRecipient.all { recipient ->
                recipient.candidateTopologyStructure.distinctStrategicWorldCount <=
                    recipient.prefixTopologyStructure.distinctStrategicWorldCount
            },
        )

        val observedRedHerring = setup.candidates.single { it.targetSeat == evin.redHerringSeat }
        assertEquals(1, observedRedHerring.targetSeat)
        assertEquals(1, observedRedHerring.credibilityDisruptions.size)
        assertEquals(
            chefSource,
            observedRedHerring.credibilityDisruptions.single().affectedSource,
        )
        assertEquals(
            CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE,
            observedRedHerring.credibilityDisruptions.single().mechanism,
        )

        assertTrue(
            setup.ruleDeterminedHealthySourceClaims.single().source == chefSource,
        )
    }
}