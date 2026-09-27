package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RedHerringSetupPrecommitAdapterTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)
    private val game = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 17L,
        players = listOf(
            player(1, "Chef", "Chef"),
            player(2, "Fortune Teller", "Fortune Teller"),
            player(3, "Washerwoman", "Washerwoman"),
            player(4, "Poisoner", "Poisoner"),
            player(5, "Imp", "Imp"),
        ),
    )
    private val revision = InformationDecisionRevision(
        gameStateRevision = 3L,
        playerInputRevision = 5L,
    )

    @Test
    fun `adapter consumes exactly the existing legal red herring candidates`() {
        val legal = SetupCandidateGenerator.generateRedHerringCandidates(game)
        val projected = RedHerringSetupPrecommitAdapter.project(
            gameId = "game-1",
            game = game,
            roleDefinitions = roles,
            sourceRevision = revision,
        )

        assertEquals(
            legal.map { it.candidateId },
            projected.candidates.map { it.sdeCandidate.candidateId },
        )
        assertEquals(
            legal.map { (it.outcome as SetupClueOutcome.RedHerring).seat },
            projected.candidates.map { it.targetSeat },
        )
        projected.candidates.forEach { candidate ->
            assertEquals(
                SdeDecisionLifecycleStage.SetupPrecommit,
                candidate.sdeCandidate.lifecycleStage,
            )
            assertEquals(
                candidate.sdeCandidate.candidateId,
                candidate.sdeCandidate.legalOutcomeIdentity,
            )
            assertEquals(
                "SetupCandidateGenerator",
                candidate.sdeCandidate.legalityProvenance.ownerId,
            )
        }
    }

    @Test
    fun `precommit candidate exposes typed future commit ref without pretending it is already committed`() {
        val projected = RedHerringSetupPrecommitAdapter.project(
            gameId = "game-1",
            game = game,
            roleDefinitions = roles,
            sourceRevision = revision,
        )

        projected.candidates.forEach { candidate ->
            assertEquals(
                SdeCommittedDecisionInputKind.RED_HERRING,
                candidate.proposedCommitRef.kind,
            )
            val bindings = candidate.sdeCandidate.inputBindings as SdeDecisionInputBindings.Captured
            assertTrue(bindings.committedInputRefs.isEmpty())
            assertTrue(bindings.playerControlledInputRefs.isEmpty())
        }
    }

    @Test
    fun `night one source audit separates determined and unresolved healthy information without FT hindsight`() {
        val projected = RedHerringSetupPrecommitAdapter.project(
            gameId = "game-1",
            game = game,
            roleDefinitions = roles,
            sourceRevision = revision,
        )

        assertEquals(
            setOf(
                ConfirmationChannelRef.Source(
                    sourceSeat = 1,
                    sourceAbility = RoleId("Chef"),
                ),
            ),
            projected.ruleDeterminedHealthySourceRefs,
        )
        assertEquals(
            setOf(
                ConfirmationChannelRef.Source(
                    sourceSeat = 3,
                    sourceAbility = RoleId("Washerwoman"),
                ),
            ),
            projected.unresolvedHealthySourceRefs,
        )
        assertEquals(
            setOf("fortune-teller-target"),
            projected.excludedPlayerControlledElementIds,
        )
        projected.candidates.forEach { candidate ->
            val bindings = candidate.sdeCandidate.inputBindings as SdeDecisionInputBindings.Captured
            assertTrue(bindings.playerControlledInputRefs.isEmpty())
        }
    }

    @Test
    fun `red herring targeting an information source creates only typed mechanism evidence`() {
        val projected = RedHerringSetupPrecommitAdapter.project(
            gameId = "game-1",
            game = game,
            roleDefinitions = roles,
            sourceRevision = revision,
        )

        val chef = projected.candidates.single { it.targetSeat == 1 }
        assertEquals(1, chef.credibilityDisruptions.size)
        assertEquals(
            ConfirmationChannelRef.Source(1, RoleId("Chef")),
            chef.credibilityDisruptions.single().affectedSource,
        )
        assertEquals(
            CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE,
            chef.credibilityDisruptions.single().mechanism,
        )

        val fortuneTeller = projected.candidates.single { it.targetSeat == 2 }
        assertTrue(fortuneTeller.credibilityDisruptions.isEmpty())

        val washerwoman = projected.candidates.single { it.targetSeat == 3 }
        assertEquals(
            ConfirmationChannelRef.Source(3, RoleId("Washerwoman")),
            washerwoman.credibilityDisruptions.single().affectedSource,
        )
    }

    private fun player(
        seat: Int,
        name: String,
        role: String,
    ): PlayerState {
        val definition = definitionsById.getValue(RoleId(role))
        return PlayerState(
            seat = seat,
            name = name,
            actualRole = definition.id,
            actualAlignment = definition.alignment,
            actualType = definition.type,
            shownRole = definition.id,
        )
    }
}