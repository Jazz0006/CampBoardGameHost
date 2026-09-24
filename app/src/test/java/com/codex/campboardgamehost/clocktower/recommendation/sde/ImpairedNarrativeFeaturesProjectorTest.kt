package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImpairedNarrativeFeaturesProjectorTest {
    @Test
    fun `classifies avoidable narrative break without a score`() {
        val priorIds = setOf("history-a", "history-b")
        val projected = ImpairedNarrativeFeaturesProjector.project(
            listOf(
                evidence(
                    candidateId = "break",
                    abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                    lifetime = ImpairmentLifetime.PERSISTENT_SETUP_BOUND,
                    priorIds = priorIds,
                    confirmation = confirmation(
                        impact("history-a", ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION),
                        impact("history-b", ConfirmationObservationRelation.NO_CONTRIBUTION),
                    ),
                ),
                evidence(
                    candidateId = "compatible",
                    abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                    lifetime = ImpairmentLifetime.PERSISTENT_SETUP_BOUND,
                    priorIds = priorIds,
                    confirmation = confirmation(
                        impact("history-a", ConfirmationObservationRelation.NO_CONTRIBUTION),
                        impact("history-b", ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION),
                    ),
                ),
            ),
        )

        val broken = (projected.getValue("break") as FeatureProjection.Projected).value
        assertEquals(
            ImpairedNarrativeRelation.BREAKS_PRIOR_IMPAIRED_NARRATIVE,
            broken.relation,
        )
        assertEquals(setOf("history-a"), broken.contradictoryPriorObservationIds)
        assertEquals(NarrativeTransitionNecessity.AVOIDABLE, broken.transitionNecessity)
        assertEquals(
            ImpairedNarrativeDetectabilitySignal.AVOIDABLE_NARRATIVE_BREAK,
            broken.detectabilitySignal,
        )
        assertEquals(ImpairmentLifetime.PERSISTENT_SETUP_BOUND, broken.impairmentLifetime)

        val compatible = (projected.getValue("compatible") as FeatureProjection.Projected).value
        assertEquals(
            ImpairedNarrativeRelation.COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE,
            compatible.relation,
        )
        assertEquals(NarrativeTransitionNecessity.NONE, compatible.transitionNecessity)
        assertEquals(ImpairedNarrativeDetectabilitySignal.NONE, compatible.detectabilitySignal)
    }

    @Test
    fun `classifies a temporary episode break as forced when every legal candidate breaks it`() {
        val projected = ImpairedNarrativeFeaturesProjector.project(
            listOf(
                evidence(
                    candidateId = "only",
                    abilityState = AbilityState.MALFUNCTIONING_POISONED,
                    lifetime = ImpairmentLifetime.TEMPORARY_ACTION_BOUND,
                    priorIds = setOf("poisoned-history"),
                    confirmation = confirmation(
                        impact(
                            "poisoned-history",
                            ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION,
                        ),
                    ),
                ),
            ),
        )

        val feature = (projected.getValue("only") as FeatureProjection.Projected).value
        assertEquals(ImpairmentLifetime.TEMPORARY_ACTION_BOUND, feature.impairmentLifetime)
        assertEquals(NarrativeTransitionNecessity.FORCED, feature.transitionNecessity)
        assertEquals(
            ImpairedNarrativeDetectabilitySignal.FORCED_NARRATIVE_BREAK,
            feature.detectabilitySignal,
        )
    }

    @Test
    fun `does not pretend to repair a narrative whose historical baseline already collapsed`() {
        val projected = ImpairedNarrativeFeaturesProjector.project(
            listOf(
                evidence(
                    candidateId = "candidate",
                    abilityState = AbilityState.MALFUNCTIONING_DRUNK,
                    lifetime = ImpairmentLifetime.PERSISTENT_SETUP_BOUND,
                    priorIds = setOf("collapsed-history"),
                    confirmation = confirmation(
                        impact(
                            "collapsed-history",
                            ConfirmationObservationRelation.BASELINE_ALREADY_COLLAPSED,
                        ),
                    ),
                ),
            ),
        )

        val feature = (projected.getValue("candidate") as FeatureProjection.Projected).value
        assertEquals(
            ImpairedNarrativeRelation.PRIOR_NARRATIVE_ALREADY_INFEASIBLE,
            feature.relation,
        )
        assertEquals(NarrativeTransitionNecessity.NONE, feature.transitionNecessity)
        assertEquals(
            ImpairedNarrativeDetectabilitySignal.PRIOR_NARRATIVE_ALREADY_INFEASIBLE,
            feature.detectabilitySignal,
        )
    }

    @Test
    fun `functioning ability is not an impaired narrative decision`() {
        val projected = ImpairedNarrativeFeaturesProjector.project(
            listOf(
                ImpairedNarrativeCandidateEvidence(
                    candidateId = "healthy",
                    abilityState = AbilityState.FUNCTIONING,
                    impairmentLifetime = null,
                    priorImpairedObservationIds = emptySet(),
                    confirmation = FeatureProjection.Projected(confirmation()),
                ),
            ),
        )

        assertEquals(
            FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE),
            projected.getValue("healthy"),
        )
    }

    private fun evidence(
        candidateId: String,
        abilityState: AbilityState,
        lifetime: ImpairmentLifetime,
        priorIds: Set<String>,
        confirmation: ConfirmationChainFeatures,
    ) = ImpairedNarrativeCandidateEvidence(
        candidateId = candidateId,
        abilityState = abilityState,
        impairmentLifetime = lifetime,
        priorImpairedObservationIds = priorIds,
        confirmation = FeatureProjection.Projected(confirmation),
    )

    private fun confirmation(
        vararg impacts: HistoricalObservationConfirmationImpact,
    ) = ConfirmationChainFeatures(
        candidateChannel = ConfirmationChannelRef.Interaction("current"),
        historicalObservationImpacts = impacts.toList(),
    )

    private fun impact(
        recordId: String,
        relation: ConfirmationObservationRelation,
    ) = HistoricalObservationConfirmationImpact(
        provenance = ConfirmationObservationProvenance(
            observationRef = SdeHistoricalObservationRef(
                recordId = recordId,
                globalSequence = recordId.hashCode().toLong().let { if (it < 0L) -it else it },
            ),
            sourceSeat = 2,
            sourceAbility = RoleId("historical-channel"),
            channel = ConfirmationChannelRef.Observation(recordId),
        ),
        relation = relation,
        restoration = ConfirmationAmbiguityRestoration(
            restoredExactWorldCount = BigInteger.ZERO,
            restoredStrategicWorldKeys = emptySet(),
            restoredDemonSeats = emptySet(),
        ),
        authenticatesDistinctSource = false,
    )
}
