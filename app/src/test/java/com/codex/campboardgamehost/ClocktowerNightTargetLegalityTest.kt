package com.codex.campboardgamehost

// Durable target-legality contract; initial PR state intentionally RED until production helpers exist.
import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerNightTargetLegalityTest {
    private fun card(name: String, eliminatedRound: Int? = null) = PlayerCard(
        name = name,
        role = Role.Civilian,
        eliminatedRound = eliminatedRound,
    )

    @Test
    fun `Monk may choose living or dead players but not self`() {
        val monk = card("Monk")
        val alive = card("Alive")
        val dead = card("Dead", eliminatedRound = 1)

        assertEquals(
            listOf("Alive", "Dead"),
            clocktowerMonkTargetCards(
                cards = listOf(monk, alive, dead),
                actorName = monk.name,
            ).map { it.name },
        )
    }

    @Test
    fun `Ravenkeeper may choose any player including self and dead players`() {
        val ravenkeeper = card("Ravenkeeper", eliminatedRound = 2)
        val alive = card("Alive")
        val dead = card("Dead", eliminatedRound = 1)

        assertEquals(
            listOf("Ravenkeeper", "Alive", "Dead"),
            clocktowerRavenkeeperTargetCards(
                cards = listOf(ravenkeeper, alive, dead),
            ).map { it.name },
        )
    }
}
