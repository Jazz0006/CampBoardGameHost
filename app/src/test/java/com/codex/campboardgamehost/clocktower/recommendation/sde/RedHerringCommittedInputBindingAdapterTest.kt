package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RedHerringCommittedInputBindingAdapterTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val definitionsById = roles.associateBy(RoleDefinition::id)
    private val game = GameState(
        script = TroubleBrewingFixtures.scriptId,
        seed = 17L,
        players = listOf(
            player(1, "Alice", "Chef"),
            player(2, "Bob", "Fortune Teller"),
            player(3, "Claire", "Washerwoman"),
            player(4, "Derek", "Poisoner"),
            player(5, "Eve", "Imp"),
        ),
    )
    private val projection = RedHerringSetupPrecommitAdapter.project(
        gameId = "game-1",
        game = game,
        roleDefinitions = roles,
        sourceRevision = InformationDecisionRevision(3L, 5L),
    )

    @Test
    fun `external committed player name binds the exact setup-owned proposed red herring ref`() {
        val expected = projection.candidates.single { it.targetSeat == 1 }.proposedCommitRef

        val binding = RedHerringCommittedInputBindingAdapter.bind(
            game = game,
            projection = projection,
            committedTargetName = "Alice",
        )

        assertEquals(
            SdeDecisionInputBindings.Captured(
                committedInputRefs = setOf(expected),
                playerControlledInputRefs = emptySet(),
            ),
            binding,
        )
        assertEquals(SdeCommittedDecisionInputKind.RED_HERRING, expected.kind)
    }

    @Test
    fun `binding fails closed for unknown ambiguous or setup-illegal target names`() {
        assertThrows(IllegalArgumentException::class.java) {
            RedHerringCommittedInputBindingAdapter.bind(
                game = game,
                projection = projection,
                committedTargetName = "Missing",
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            RedHerringCommittedInputBindingAdapter.bind(
                game = game,
                projection = projection,
                committedTargetName = "Derek",
            )
        }

        val duplicateNames = game.copy(
            players = game.players.map { player ->
                if (player.seat == 2) player.copy(name = "Alice") else player
            },
        )
        assertThrows(IllegalArgumentException::class.java) {
            RedHerringCommittedInputBindingAdapter.bind(
                game = duplicateNames,
                projection = projection,
                committedTargetName = "Alice",
            )
        }
    }

    @Test
    fun `binding contract contains no player controlled fortune teller target input`() {
        val binding = RedHerringCommittedInputBindingAdapter.bind(
            game = game,
            projection = projection,
            committedTargetName = "Claire",
        )

        assertTrue(binding.playerControlledInputRefs.isEmpty())
        assertEquals(
            setOf(SdeCommittedDecisionInputKind.RED_HERRING),
            binding.committedInputRefs.mapTo(linkedSetOf(), CommittedDecisionInputRef::kind),
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