package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorld
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.epistemic.TroubleBrewingSetupProfiles
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import com.codex.campboardgamehost.clocktower.recommendation.setup.SetupCandidateGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sde2D4PlayerCountCorrectnessTest {
    private val roles = TroubleBrewingFixtures.fullRoleDefinitions()
    private val rolesById = roles.associateBy(RoleDefinition::id)

    @Test
    fun `standard and Baron setup profiles cover every supported player count`() {
        for (playerCount in 5..15) {
            val standard = TroubleBrewingSetupProfiles.standard(playerCount)
            val baron = TroubleBrewingSetupProfiles.withBaron(playerCount)

            assertEquals(playerCount, standard.totalPlayers())
            assertEquals(playerCount, baron.totalPlayers())
            assertEquals(standard.townsfolk - 2, baron.townsfolk)
            assertEquals(standard.outsiders + 2, baron.outsiders)
            assertEquals(standard.minions, baron.minions)
            assertEquals(1, standard.demons)
            assertEquals(1, baron.demons)
            assertEquals(listOf(standard, baron).distinct(), TroubleBrewingSetupProfiles.legalProfiles(playerCount))
        }
    }

    @Test
    fun `strategic setup key generalizes across 5 to 15 players and collapses good-role permutations`() {
        for (playerCount in 5..15) {
            val game = standardGame(playerCount)
            val rolesBySeat = game.players.associateTo(linkedMapOf()) { it.seat to it.actualRole }
            val world = EnumeratedWorld(rolesBySeat = rolesBySeat)
            val key = StrategicWorldKey.from(world, rolesById)
            val expectedDemonSeat = game.players.single { it.actualType == CharacterType.DEMON }.seat
            val expectedMinionSeats = game.players
                .filter { it.actualType == CharacterType.MINION }
                .map(PlayerState::seat)
                .sorted()

            assertEquals(expectedDemonSeat, key.demonSeat)
            assertEquals(expectedMinionSeats, key.minionSeats)
            assertEquals(TroubleBrewingSetupProfiles.standard(playerCount).minions, key.minionSeats.size)

            val goodSeats = game.players.filter { it.actualType.isGoodType() }.map(PlayerState::seat)
            val permutedGoodRoles = rolesBySeat.toMutableMap()
            val firstGood = goodSeats[0]
            val secondGood = goodSeats[1]
            val firstRole = permutedGoodRoles.getValue(firstGood)
            permutedGoodRoles[firstGood] = permutedGoodRoles.getValue(secondGood)
            permutedGoodRoles[secondGood] = firstRole
            assertEquals(
                key,
                StrategicWorldKey.from(
                    EnumeratedWorld(rolesBySeat = permutedGoodRoles.toSortedMap()),
                    rolesById,
                ),
            )

            val swappedEvilRoles = rolesBySeat.toMutableMap()
            val firstMinion = expectedMinionSeats.first()
            val demonRole = swappedEvilRoles.getValue(expectedDemonSeat)
            swappedEvilRoles[expectedDemonSeat] = swappedEvilRoles.getValue(firstMinion)
            swappedEvilRoles[firstMinion] = demonRole
            assertNotEquals(
                key,
                StrategicWorldKey.from(
                    EnumeratedWorld(rolesBySeat = swappedEvilRoles.toSortedMap()),
                    rolesById,
                ),
            )
        }
    }

    @Test
    fun `Demon bluff candidate count remains complete and unique across 5 to 15 players`() {
        val expectedCounts = mapOf(
            5 to 0,
            6 to 0,
            7 to 220,
            8 to 165,
            9 to 120,
            10 to 120,
            11 to 84,
            12 to 56,
            13 to 56,
            14 to 35,
            15 to 20,
        )

        expectedCounts.forEach { (playerCount, expectedCount) ->
            val game = standardGame(playerCount)
            val inPlay = game.players.mapTo(linkedSetOf(), PlayerState::actualRole)
            val candidates = SetupCandidateGenerator.generateDemonBluffCandidates(game, roles)

            assertEquals("Unexpected bluff count at $playerCount players", expectedCount, candidates.size)
            assertEquals(candidates.size, candidates.map { it.candidateId }.distinct().size)
            assertTrue(candidates.all { candidate ->
                val bluffRoles = (candidate.outcome as com.codex.campboardgamehost.clocktower.domain.SetupClueOutcome.DemonBluffs).roles
                bluffRoles.size == 3 &&
                    bluffRoles.distinct().size == 3 &&
                    bluffRoles.none(inPlay::contains) &&
                    bluffRoles.all { role -> rolesById.getValue(role).type.isGoodType() }
            })
        }
    }

    private fun standardGame(playerCount: Int): GameState {
        val profile = TroubleBrewingSetupProfiles.standard(playerCount)
        val byType = roles.groupBy(RoleDefinition::type)
        val selected = buildList {
            addAll(byType.getValue(CharacterType.TOWNSFOLK).take(profile.townsfolk))
            addAll(byType.getValue(CharacterType.OUTSIDER).take(profile.outsiders))
            addAll(
                byType.getValue(CharacterType.MINION)
                    .filterNot { it.id.value.equals("Baron", ignoreCase = true) }
                    .take(profile.minions),
            )
            addAll(byType.getValue(CharacterType.DEMON).take(profile.demons))
        }
        require(selected.size == playerCount)

        return GameState(
            script = TroubleBrewingFixtures.scriptId,
            seed = 2026091800L + playerCount,
            players = selected.mapIndexed { index, role ->
                PlayerState(
                    seat = index + 1,
                    name = "D2D4 P${index + 1}",
                    actualRole = role.id,
                    actualAlignment = role.alignment,
                    actualType = role.type,
                    shownRole = role.id,
                )
            },
        )
    }

    private fun com.codex.campboardgamehost.clocktower.epistemic.InformationProposition.SetupProfile.totalPlayers(): Int =
        townsfolk + outsiders + minions + demons

    private fun CharacterType.isGoodType(): Boolean =
        this == CharacterType.TOWNSFOLK || this == CharacterType.OUTSIDER
}
