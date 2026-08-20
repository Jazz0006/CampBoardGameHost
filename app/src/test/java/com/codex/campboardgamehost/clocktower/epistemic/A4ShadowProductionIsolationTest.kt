package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.ClocktowerRecommendationCoordinator
import com.codex.campboardgamehost.clocktower.session.SetupCoordinationRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class A4ShadowProductionIsolationTest {
    @Test fun `shadow cache readiness cannot change production setup recommendation`() {
        val snapshot = A4RuntimeFixtures.snapshot()
        val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
        val productionRequest = SetupCoordinationRequest(snapshot.gameState, roleDefinitions)
        val productionCoordinator = ClocktowerRecommendationCoordinator()

        val before = productionCoordinator.recommendSetup(productionRequest)
        assertTrue(before.plans.isNotEmpty())

        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceivedRolesBySeat = formal.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceivedRolesBySeat,
            observationLog = emptyList(),
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val shadowRequest = A4IdentityRevealPrewarmRequest(
            formal = formal,
            playerInputRevision = snapshot.playerInputRevision,
            knowledgeBySeat = knowledgeBySeat,
            revealOrder = formal.players.map { it.seat },
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = roleDefinitions,
        )
        val shadow = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                EnumeratedWorldSet.fromWorlds(
                    rulesetRef = activeRequest.formal.rulesetRef,
                    knowledge = knowledge,
                    hypothesis = activeRequest.hypothesis,
                    roleDefinitions = activeRequest.roleDefinitions,
                    worlds = listOf(
                        EnumeratedWorld(activeRequest.formal.players.associate { it.seat to it.actualRole }),
                    ),
                )
            },
        )

        val miss = shadow.probe(shadowRequest)
        assertEquals(0, miss.readyCount)

        val shadowReport = shadow.run(shadow.start(shadowRequest))
        assertEquals(formal.players.size, shadowReport.readyCount)
        val hit = shadow.probe(shadowRequest)
        assertEquals(formal.players.size, hit.readyCount)

        val after = productionCoordinator.recommendSetup(productionRequest)

        assertEquals(before, after)
        assertEquals(before.plans, after.plans)
    }

    @Test fun `production demand probe exposes readiness only and never a cached world set`() {
        val snapshot = A4RuntimeFixtures.snapshot()
        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceivedRolesBySeat = formal.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceivedRolesBySeat,
            observationLog = emptyList(),
        ).associateBy(PlayerKnowledgeSnapshot::recipientSeat)
        val request = A4IdentityRevealPrewarmRequest(
            formal = formal,
            playerInputRevision = snapshot.playerInputRevision,
            knowledgeBySeat = knowledgeBySeat,
            revealOrder = formal.players.map { it.seat },
            hypothesis = EpistemicHypothesis.MECHANICALLY_CREDIBLE,
            roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions(),
        )
        val shadow = A4IdentityRevealPrewarmCoordinator(
            builder = A4IdentityRevealPrewarmBuilder { activeRequest, knowledge ->
                EnumeratedWorldSet.fromWorlds(
                    rulesetRef = activeRequest.formal.rulesetRef,
                    knowledge = knowledge,
                    hypothesis = activeRequest.hypothesis,
                    roleDefinitions = activeRequest.roleDefinitions,
                    worlds = listOf(
                        EnumeratedWorld(activeRequest.formal.players.associate { it.seat to it.actualRole }),
                    ),
                )
            },
        )

        shadow.run(shadow.start(request))
        val probe = shadow.probe(request)

        assertEquals(formal.gameId, probe.gameId)
        assertEquals(formal.gameStateRevision, probe.gameStateRevision)
        assertEquals(snapshot.playerInputRevision, probe.playerInputRevision)
        assertEquals(formal.players.map { it.seat }, probe.recipientSeats)
        assertEquals(formal.players.map { it.seat }, probe.readySeats)
        assertTrue(probe.missingSeats.isEmpty())
    }
}
