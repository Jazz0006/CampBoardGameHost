package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class HostSeatingRosterTest {
    @Test
    fun `confirm seats freezes current physical order into typed seat ids`() {
        val roster = confirmHostSeating(listOf("Casey", "Alice", "Bob"))

        assertEquals(
            listOf(ClocktowerSeatId(1), ClocktowerSeatId(2), ClocktowerSeatId(3)),
            roster.seats.map(HostSeatAssignment::seatId),
        )
        assertEquals(listOf("Casey", "Alice", "Bob"), roster.playerNames)
    }

    @Test
    fun `confirmed roster is independent from later arrangement list mutation`() {
        val arrangement = mutableListOf("Alice", "Bob", "Casey")
        val roster = confirmHostSeating(arrangement)

        arrangement.reverse()
        arrangement[0] = "Changed"

        assertEquals(listOf("Alice", "Bob", "Casey"), roster.playerNames)
    }

    @Test
    fun `setup presentation uses confirmed typed seats without inventing roles`() {
        val roster = confirmHostSeating(listOf("Alice", "Bob"))

        val presentations = roster.toHostSeatPresentations()

        assertEquals(listOf(ClocktowerSeatId(1), ClocktowerSeatId(2)), presentations.map { it.seatId })
        assertEquals(listOf("Alice", "Bob"), presentations.map { it.playerName })
        assertEquals(listOf(null, null), presentations.map { it.actualRole })
        assertEquals(listOf(null, null), presentations.map { it.shownRole })
    }

    @Test
    fun `blank or duplicate player identity fails closed at confirmation boundary`() {
        listOf(
            listOf("Alice", ""),
            listOf("Alice", "Alice"),
        ).forEach { invalid ->
            try {
                confirmHostSeating(invalid)
                fail("Expected invalid confirmed seating to be rejected: $invalid")
            } catch (_: IllegalArgumentException) {
                // Expected.
            }
        }
    }

    @Test
    fun `confirmed seat ids feed the permanent spatial contract unchanged`() {
        val roster = confirmHostSeating(listOf("Alice", "Bob", "Casey", "Dana", "Evan"))
        val presentations = roster.toHostSeatPresentations()

        val slots = hostTableSeatFrames(
            seats = presentations,
            interaction = HostTableInteractionState(),
            layout = layoutFor(presentations.size),
        ).associate { it.seat.seatId to it.spatialSlot }

        assertEquals(roster.seats.map { it.seatId }.toSet(), slots.keys)
    }

    @Test
    fun `production player names require confirmed physical seating`() {
        try {
            HostSeatingSetupFlow().playerNamesFor()
            fail("Expected unconfirmed seating to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun `single game settings use the confirmed roster and can reopen seating`() {
        val confirmed = HostSeatingSetupFlow()
            .confirmSeats(listOf("Alice", "Bob", "Casey", "Dana", "Evan"))
        assertEquals(listOf("Alice", "Bob", "Casey", "Dana", "Evan"), confirmed.playerNamesFor())
        assertNull(confirmed.reopenSeating().confirmedSeating)
    }

    @Test
    fun `frozen roster is not rewritten when arrangement changes`() {
        val arrangement = mutableListOf("Alice", "Bob", "Casey", "Dana", "Evan")
        val confirmed = HostSeatingSetupFlow().confirmSeats(arrangement)
        arrangement.reverse()
        assertEquals(listOf("Alice", "Bob", "Casey", "Dana", "Evan"), confirmed.playerNamesFor())
    }

    private fun layoutFor(playerCount: Int): HostTableLayout = hostTableLayout(
        playerCount = playerCount,
        constraints = HostTableLayoutConstraints(
            availableWidth = 360f,
            availableHeight = 600f,
            seatCardWidth = 64f,
            seatCardHeight = 50f,
            minimumSafeSeparation = 4f,
            centerWorkspaceWidth = 200f,
            centerWorkspaceHeight = 312f,
        ),
    )
}
