package com.codex.campboardgamehost.clocktower.review

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.sde.ConfirmationChannelRef
import com.codex.campboardgamehost.clocktower.recommendation.sde.CredibilityDisruptionMechanism
import com.codex.campboardgamehost.clocktower.recommendation.sde.ExactTruthDangerSourceProjection
import com.codex.campboardgamehost.clocktower.recommendation.sde.ExactTruthDangerSourceProjector
import com.codex.campboardgamehost.clocktower.recommendation.sde.RedHerringSetupPrecommitAdapter
import com.codex.campboardgamehost.clocktower.recommendation.sde.TruthCredibilityCandidateEvidence
import com.codex.campboardgamehost.clocktower.recommendation.sde.TruthCredibilityFeaturesProjector
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde3D2EvinTruthCredibilityRegressionTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `Evin Chef one is exact truth danger and seat one Red Herring creates typed disruption without FT hindsight`() {
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

        val exact = ExactTruthDangerSourceProjector.evaluate(
            validatedRuleset = context.validatedRuleset,
            context = context.exactContext,
            claims = setup.ruleDeterminedHealthySourceClaims,
        )
        assertTrue(exact is ExactTruthDangerSourceProjection.Ready)
        val chefImpact = (exact as ExactTruthDangerSourceProjection.Ready)
            .impacts
            .single { it.source == chefSource }

        assertTrue(chefImpact.independentlyConstraining)
        assertTrue(
            chefImpact.exactWorldReduction > BigInteger.ZERO ||
                chefImpact.strategicWorldKeysRemoved.isNotEmpty() ||
                chefImpact.demonSeatsRemoved.isNotEmpty(),
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

        val features = TruthCredibilityFeaturesProjector.project(
            listOf(
                TruthCredibilityCandidateEvidence(
                    candidateId = observedRedHerring.sdeCandidate.candidateId,
                    truthDangerSources = setOf(chefImpact).toList(),
                    credibilityDisruptions = observedRedHerring.credibilityDisruptions.toList(),
                    unresolvedSourceRefs = setup.unresolvedHealthySourceRefs,
                ),
            ),
        ).getValue(observedRedHerring.sdeCandidate.candidateId)

        assertEquals(setOf(chefImpact), features.truthDangerSources)
        assertEquals(
            observedRedHerring.credibilityDisruptions,
            features.credibilityDisruptions,
        )
        assertEquals(setup.unresolvedHealthySourceRefs, features.unresolvedSourceRefs)
    }
}