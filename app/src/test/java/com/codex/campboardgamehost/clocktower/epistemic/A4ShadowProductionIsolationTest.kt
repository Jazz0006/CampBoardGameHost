package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class A4ShadowProductionIsolationTest {
    @Test fun `shadow cache readiness cannot mutate canonical game state`() {
        val snapshot = A4RuntimeFixtures.snapshot()
        val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()
        val gameStateBefore = snapshot.gameState

        val formal = FormalGameState.from(snapshot, StorytellerPhase.FIRST_NIGHT, 1)
        val perceivedRolesBySeat = formal.players.associate { player ->
            player.seat to (player.shownRole ?: player.actualRole)
        }
        val knowledgeBySeat = A4PlayerKnowledgeFactory.createAll(
            formal = formal,
            perceivedRolesBySeat = perceivedRolesBySeat,
            observationLog = EpistemicObservationLog(),
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

        assertEquals(gameStateBefore, snapshot.gameState)
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
            observationLog = EpistemicObservationLog(),
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
