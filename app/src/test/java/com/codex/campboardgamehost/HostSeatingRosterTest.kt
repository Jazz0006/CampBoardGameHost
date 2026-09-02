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

        val slots = hostTableSeatFrames(
            seats = roster.toHostSeatPresentations(),
            interaction = HostTableInteractionState(),
        ).associate { it.seat.seatId to it.spatialSlot }

        assertEquals(roster.seats.map { it.seatId }.toSet(), slots.keys)
    }

    @Test
    fun `game choice is impossible before seats are confirmed`() {
        try {
            HostSeatingSetupFlow().chooseGame(GameKind.Clocktower)
            fail("Expected game choice before seat confirmation to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun `game selection and settings keep the same confirmed seating`() {
        val confirmed = HostSeatingSetupFlow()
            .confirmSeats(listOf("Alice", "Bob", "Casey", "Dana", "Evan"))
        val selected = confirmed.chooseGame(GameKind.Clocktower)
        val backAtGameSelection = selected.returnToGameSelection()

        assertEquals(confirmed.confirmedSeating, selected.confirmedSeating)
        assertEquals(confirmed.confirmedSeating, backAtGameSelection.confirmedSeating)
        assertEquals(GameKind.Clocktower, selected.selectedGame)
        assertNull(backAtGameSelection.selectedGame)
    }

    @Test
    fun `returning to seating explicitly releases the old confirmation`() {
        val reopened = HostSeatingSetupFlow()
            .confirmSeats(listOf("Alice", "Bob", "Casey"))
            .chooseGame(GameKind.Undercover)
            .reopenSeating()

        assertNull(reopened.confirmedSeating)
        assertNull(reopened.selectedGame)
    }

    @Test
    fun `production player names come only from the frozen confirmed roster`() {
        val arrangement = mutableListOf("Alice", "Bob", "Casey", "Dana")
        val selected = HostSeatingSetupFlow()
            .confirmSeats(arrangement)
            .chooseGame(GameKind.Werewolf)

        arrangement.reverse()
        arrangement[0] = "Changed"

        assertEquals(
            listOf("Alice", "Bob", "Casey", "Dana"),
            selected.playerNamesFor(GameKind.Werewolf),
        )
    }

    @Test
    fun `production player names reject a game that is not the selected game`() {
        val selected = HostSeatingSetupFlow()
            .confirmSeats(listOf("Alice", "Bob", "Casey", "Dana", "Evan"))
            .chooseGame(GameKind.Clocktower)

        try {
            selected.playerNamesFor(GameKind.Undercover)
            fail("Expected mismatched production game authority to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }
}
