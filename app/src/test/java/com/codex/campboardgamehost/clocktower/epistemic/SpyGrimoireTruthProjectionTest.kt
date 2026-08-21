package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.ClocktowerScript
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.RoleId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class SpyGrimoireTruthProjectionTest {
    private val catalog = BuiltInClocktowerRulesetCatalog { assetPath ->
        File("src/main/assets/$assetPath").readText(Charsets.UTF_8)
    }
    private val troubleBrewing = catalog.ruleset(ClocktowerScript.TroubleBrewing)

    @Test
    fun `explicit grimoire truth projects display state and active ruleset token placements`() {
        val input = GrimoireTruthSnapshotInput(
            expectedSeatRoster = setOf(1, 2, 3),
            seats = listOf(
                GrimoireSeatTruth(1, RoleId("Fortune Teller"), alive = true),
                GrimoireSeatTruth(2, RoleId("Poisoner"), alive = true),
                GrimoireSeatTruth(3, RoleId("Spy"), alive = true),
            ),
            reminderPlacements = listOf(
                GrimoireReminderPlacement(
                    targetSeat = 1,
                    sourceRole = RoleId("Poisoner"),
                    scope = GrimoireReminderTokenScope.CHARACTER,
                    occurrence = 1,
                ),
                GrimoireReminderPlacement(
                    targetSeat = 2,
                    sourceRole = RoleId("Fortune Teller"),
                    scope = GrimoireReminderTokenScope.CHARACTER,
                    occurrence = 1,
                ),
            ),
        )

        assertEquals(
            InformationProposition.GrimoireState(
                listOf(
                    GrimoireSeatView(
                        1,
                        RoleId("Fortune Teller"),
                        true,
                        listOf(troubleBrewing.grimoireReminderToken(
                            RoleId("Poisoner"),
                            GrimoireReminderTokenScope.CHARACTER,
                            1,
                        )),
                    ),
                    GrimoireSeatView(
                        2,
                        RoleId("Poisoner"),
                        true,
                        listOf(troubleBrewing.grimoireReminderToken(
                            RoleId("Fortune Teller"),
                            GrimoireReminderTokenScope.CHARACTER,
                            1,
                        )),
                    ),
                    GrimoireSeatView(3, RoleId("Spy"), true),
                ),
            ),
            SpyGrimoireTruthProjector.project(troubleBrewing, input),
        )
    }

    @Test
    fun `projection preserves explicit displayed role instead of inferring actual or shown role`() {
        val input = GrimoireTruthSnapshotInput(
            expectedSeatRoster = setOf(1, 2),
            seats = listOf(
                GrimoireSeatTruth(1, RoleId("Empath"), alive = true),
                GrimoireSeatTruth(2, RoleId("Spy"), alive = false),
            ),
        )

        val result = SpyGrimoireTruthProjector.project(troubleBrewing, input)

        assertEquals(RoleId("Empath"), result.seats[0].displayedRole)
        assertEquals(false, result.seats[1].alive)
    }

    @Test
    fun `snapshot rejects a sparse seat list that omits expected grimoire truth`() {
        assertThrows(IllegalArgumentException::class.java) {
            GrimoireTruthSnapshotInput(
                expectedSeatRoster = setOf(1, 2, 3),
                seats = listOf(
                    GrimoireSeatTruth(1, RoleId("Empath"), alive = true),
                    GrimoireSeatTruth(3, RoleId("Spy"), alive = true),
                ),
            )
        }
    }

    @Test
    fun `snapshot rejects a seat outside the expected grimoire roster`() {
        assertThrows(IllegalArgumentException::class.java) {
            GrimoireTruthSnapshotInput(
                expectedSeatRoster = setOf(1, 2),
                seats = listOf(
                    GrimoireSeatTruth(1, RoleId("Empath"), alive = true),
                    GrimoireSeatTruth(2, RoleId("Spy"), alive = true),
                    GrimoireSeatTruth(3, RoleId("Imp"), alive = true),
                ),
            )
        }
    }

    @Test
    fun `projection fails closed for reminder source outside active script`() {
        val noGreaterJoy = catalog.ruleset(ClocktowerScript.NoGreaterJoy)
        val input = GrimoireTruthSnapshotInput(
            expectedSeatRoster = setOf(1),
            seats = listOf(GrimoireSeatTruth(1, RoleId("Imp"), alive = true)),
            reminderPlacements = listOf(
                GrimoireReminderPlacement(
                    targetSeat = 1,
                    sourceRole = RoleId("Poisoner"),
                    scope = GrimoireReminderTokenScope.CHARACTER,
                    occurrence = 1,
                ),
            ),
        )

        assertThrows(IllegalArgumentException::class.java) {
            SpyGrimoireTruthProjector.project(noGreaterJoy, input)
        }
    }

    @Test
    fun `projection fails closed for displayed role outside active script`() {
        val input = GrimoireTruthSnapshotInput(
            expectedSeatRoster = setOf(1),
            seats = listOf(GrimoireSeatTruth(1, RoleId("Clockmaker"), alive = true)),
        )

        assertThrows(IllegalArgumentException::class.java) {
            SpyGrimoireTruthProjector.project(troubleBrewing, input)
        }
    }

    @Test
    fun `one physical reminder occurrence cannot be placed on two seats`() {
        val placement = GrimoireReminderPlacement(
            targetSeat = 1,
            sourceRole = RoleId("Poisoner"),
            scope = GrimoireReminderTokenScope.CHARACTER,
            occurrence = 1,
        )
        val input = GrimoireTruthSnapshotInput(
            expectedSeatRoster = setOf(1, 2),
            seats = listOf(
                GrimoireSeatTruth(1, RoleId("Empath"), alive = true),
                GrimoireSeatTruth(2, RoleId("Spy"), alive = true),
            ),
            reminderPlacements = listOf(placement, placement.copy(targetSeat = 2)),
        )

        assertThrows(IllegalArgumentException::class.java) {
            SpyGrimoireTruthProjector.project(troubleBrewing, input)
        }
    }

    @Test
    fun `projector canonicalizes token order on each seat`() {
        val input = GrimoireTruthSnapshotInput(
            expectedSeatRoster = setOf(1),
            seats = listOf(GrimoireSeatTruth(1, RoleId("Empath"), alive = true)),
            reminderPlacements = listOf(
                GrimoireReminderPlacement(1, RoleId("Poisoner"), GrimoireReminderTokenScope.CHARACTER, 1),
                GrimoireReminderPlacement(1, RoleId("Fortune Teller"), GrimoireReminderTokenScope.CHARACTER, 1),
            ),
        )

        val tokens = SpyGrimoireTruthProjector.project(troubleBrewing, input).seats.single().reminderTokens

        assertEquals(tokens.sorted(), tokens)
    }

    @Test
    fun `caller mutation cannot rewrite explicit grimoire truth input`() {
        val expectedSeatRoster = mutableSetOf(1)
        val seats = mutableListOf(GrimoireSeatTruth(1, RoleId("Empath"), alive = true))
        val placements = mutableListOf<GrimoireReminderPlacement>()
        val input = GrimoireTruthSnapshotInput(expectedSeatRoster, seats, placements)

        expectedSeatRoster += 2
        seats += GrimoireSeatTruth(2, RoleId("Spy"), alive = true)
        placements += GrimoireReminderPlacement(
            1,
            RoleId("Poisoner"),
            GrimoireReminderTokenScope.CHARACTER,
            1,
        )

        assertEquals(setOf(1), input.expectedSeatRoster)
        assertEquals(1, input.seats.size)
        assertEquals(emptyList<GrimoireReminderPlacement>(), input.reminderPlacements)
    }
}
