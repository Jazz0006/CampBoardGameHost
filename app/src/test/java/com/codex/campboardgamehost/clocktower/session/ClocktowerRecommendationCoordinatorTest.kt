package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DynamicInformationOutcome
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicGenerationContext
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability
import com.codex.campboardgamehost.clocktower.recommendation.dynamic.UnreliableCategoricalCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerRecommendationCoordinatorTest {
    @Test
    fun `coordinator resolves neutral structured information`() {
        val coordinator = ClocktowerRecommendationCoordinator()
        val information = coordinator.resolveInformation(
            InformationResolutionRequest.Category(
                candidates = listOf(
                    UnreliableCategoricalCandidate("truth", true),
                    UnreliableCategoricalCandidate("lie", false),
                ),
                generation = DynamicGenerationContext(
                    abilityRole = RoleId("Empath"),
                    recipientSeat = 2,
                    reliability = InformationReliability.POISONED,
                ),
            ),
        )

        assertEquals(
            setOf("truth", "lie"),
            information.map { (it.candidate.outcome as DynamicInformationOutcome.Category).id }.toSet(),
        )
    }
}
