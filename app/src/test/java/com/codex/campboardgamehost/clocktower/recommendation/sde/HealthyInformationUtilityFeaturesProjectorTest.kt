package com.codex.campboardgamehost.clocktower.recommendation.sde

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthyInformationUtilityFeaturesProjectorTest {
    @Test
    fun `preserves global healthy routes while a same recipient route becomes supported redundant`() {
        val otherRecipient = historical("other-recipient", recipientSeat = 4)
        val sameRecipient = historical("same-recipient", recipientSeat = 2)

        val features = HealthyInformationUtilityFeaturesProjector.project(
            HealthyInformationCandidateEvidence(
                candidateId = "candidate",
                recipientSeat = 2,
                candidateHistoryFeasibleAfter = true,
                currentCandidateHealthy = false,
                currentCandidateIndependentlyConstraining = false,
                historicalRoutes = listOf(
                    routeEvidence(
                        ref = otherRecipient,
                        independentlyUsableBefore = true,
                        relationToCurrentCandidate = null,
                    ),
                    routeEvidence(
                        ref = sameRecipient,
                        independentlyUsableBefore = true,
                        relationToCurrentCandidate =
                            ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION,
                    ),
                ),
            ),
        )

        assertEquals(setOf(otherRecipient, sameRecipient), features.usableHealthyRouteRefsBefore)
        assertEquals(setOf(otherRecipient, sameRecipient), features.independentHealthyRouteRefsBefore)
        assertEquals(setOf(otherRecipient, sameRecipient), features.usableHealthyRouteRefsAfter)
        assertEquals(setOf(otherRecipient), features.independentHealthyRouteRefsAfter)
        assertTrue(features.lostHealthyRouteRefs.isEmpty())
        assertFalse(features.removesLastUsableHealthyRoute)
        assertEquals(setOf(sameRecipient), features.newlyRedundantHealthyRouteRefs)
        assertTrue(features.contradictedHealthyRouteRefs.isEmpty())
        assertTrue(features.hasAnyUsableHealthyRouteAfter)
        assertTrue(features.hasAnyIndependentHealthyRouteAfter)
        assertEquals(null, features.currentCandidateHealthyRouteRef)
    }

    @Test
    fun `candidate that contradicts the only healthy route leaves no usable healthy route`() {
        val route = historical("only-route", recipientSeat = 2)

        val features = HealthyInformationUtilityFeaturesProjector.project(
            HealthyInformationCandidateEvidence(
                candidateId = "candidate",
                recipientSeat = 2,
                candidateHistoryFeasibleAfter = false,
                currentCandidateHealthy = false,
                currentCandidateIndependentlyConstraining = false,
                historicalRoutes = listOf(
                    routeEvidence(
                        ref = route,
                        independentlyUsableBefore = true,
                        relationToCurrentCandidate =
                            ConfirmationObservationRelation.CONTRADICTS_EXISTING_OBSERVATION,
                    ),
                ),
            ),
        )

        assertEquals(setOf(route), features.usableHealthyRouteRefsBefore)
        assertEquals(setOf(route), features.independentHealthyRouteRefsBefore)
        assertEquals(setOf(route), features.contradictedHealthyRouteRefs)
        assertEquals(setOf(route), features.lostHealthyRouteRefs)
        assertTrue(features.removesLastUsableHealthyRoute)
        assertTrue(features.usableHealthyRouteRefsAfter.isEmpty())
        assertTrue(features.independentHealthyRouteRefsAfter.isEmpty())
        assertFalse(features.hasAnyUsableHealthyRouteAfter)
        assertFalse(features.hasAnyIndependentHealthyRouteAfter)
    }

    @Test
    fun `infeasible whole history does not falsely label an unrelated healthy route contradicted`() {
        val route = historical("unrelated-route", recipientSeat = 4)

        val features = HealthyInformationUtilityFeaturesProjector.project(
            HealthyInformationCandidateEvidence(
                candidateId = "candidate",
                recipientSeat = 2,
                candidateHistoryFeasibleAfter = false,
                currentCandidateHealthy = false,
                currentCandidateIndependentlyConstraining = false,
                historicalRoutes = listOf(
                    routeEvidence(
                        ref = route,
                        independentlyUsableBefore = true,
                        relationToCurrentCandidate =
                            ConfirmationObservationRelation.NO_CONTRIBUTION,
                    ),
                ),
            ),
        )

        assertTrue(features.usableHealthyRouteRefsAfter.isEmpty())
        assertTrue(features.independentHealthyRouteRefsAfter.isEmpty())
        assertTrue(features.contradictedHealthyRouteRefs.isEmpty())
        assertTrue(features.newlyRedundantHealthyRouteRefs.isEmpty())
        assertEquals(setOf(route), features.usableHealthyRouteRefsBefore)
        assertEquals(setOf(route), features.lostHealthyRouteRefs)
        assertTrue(features.removesLastUsableHealthyRoute)
    }

    @Test
    fun `functioning informative candidate creates a healthy independent route without a budget`() {
        val features = HealthyInformationUtilityFeaturesProjector.project(
            HealthyInformationCandidateEvidence(
                candidateId = "healthy-current",
                recipientSeat = 3,
                candidateHistoryFeasibleAfter = true,
                currentCandidateHealthy = true,
                currentCandidateIndependentlyConstraining = true,
                historicalRoutes = emptyList(),
            ),
        )

        val current = HealthyInformationRouteRef.CurrentCandidate(
            candidateId = "healthy-current",
            recipientSeat = 3,
        )
        assertEquals(current, features.currentCandidateHealthyRouteRef)
        assertTrue(features.usableHealthyRouteRefsBefore.isEmpty())
        assertTrue(features.independentHealthyRouteRefsBefore.isEmpty())
        assertFalse(features.removesLastUsableHealthyRoute)
        assertEquals(setOf(current), features.usableHealthyRouteRefsAfter)
        assertEquals(setOf(current), features.independentHealthyRouteRefsAfter)
        assertTrue(features.hasAnyUsableHealthyRouteAfter)
        assertTrue(features.hasAnyIndependentHealthyRouteAfter)
        assertTrue(features.newlyRedundantHealthyRouteRefs.isEmpty())
        assertTrue(features.contradictedHealthyRouteRefs.isEmpty())
    }

    @Test
    fun `functioning redundant candidate remains healthy without pretending it is independent`() {
        val features = HealthyInformationUtilityFeaturesProjector.project(
            HealthyInformationCandidateEvidence(
                candidateId = "healthy-redundant",
                recipientSeat = 5,
                candidateHistoryFeasibleAfter = true,
                currentCandidateHealthy = true,
                currentCandidateIndependentlyConstraining = false,
                historicalRoutes = emptyList(),
            ),
        )

        val current = HealthyInformationRouteRef.CurrentCandidate(
            candidateId = "healthy-redundant",
            recipientSeat = 5,
        )
        assertEquals(setOf(current), features.usableHealthyRouteRefsAfter)
        assertTrue(features.independentHealthyRouteRefsAfter.isEmpty())
        assertTrue(features.hasAnyUsableHealthyRouteAfter)
        assertFalse(features.hasAnyIndependentHealthyRouteAfter)
    }

    private fun historical(
        recordId: String,
        recipientSeat: Int,
    ) = HealthyInformationRouteRef.HistoricalObservation(
        recordId = recordId,
        recipientSeat = recipientSeat,
    )

    private fun routeEvidence(
        ref: HealthyInformationRouteRef.HistoricalObservation,
        independentlyUsableBefore: Boolean,
        relationToCurrentCandidate: ConfirmationObservationRelation?,
    ) = HistoricalHealthyInformationRouteEvidence(
        routeRef = ref,
        independentlyUsableBefore = independentlyUsableBefore,
        relationToCurrentCandidate = relationToCurrentCandidate,
    )
}
