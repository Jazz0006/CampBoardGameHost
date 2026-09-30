package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClocktowerFirstNightEvilInformationPresentationTest {
    private val minion = PlayerCard("Minion", Role.Civilian, "")
    private val demon = PlayerCard("Demon", Role.Civilian, "")

    @Test
    fun `normal evil information preserves prepared Minion and Demon presentation`() {
        val steps = buildClocktowerFirstNightEvilInformationSteps(
            minionActor = minion,
            demonActor = demon,
            shouldGiveInformation = true,
            text = preparedText(),
        )

        assertTrue(steps.minion.isRealAction)
        assertEquals(minion, steps.minion.actor)
        assertEquals("Wake minions", steps.minion.storytellerAction)
        assertEquals("Demon is P7", steps.minion.tellPlayer)
        assertEquals(ClocktowerDisplayKind.EvilInfo, steps.minion.displayKind)
        assertEquals("Demon\nP7", steps.minion.displayPrimary)
        assertEquals("Wake P2 · P5", steps.minion.wakeText)

        assertTrue(steps.demon.isRealAction)
        assertEquals(demon, steps.demon.actor)
        assertEquals("Wake demon", steps.demon.storytellerAction)
        assertEquals("Minions P2 · P5\nBluffs Mayor · Monk · Undertaker", steps.demon.tellPlayer)
        assertEquals(ClocktowerDisplayKind.EvilInfo, steps.demon.displayKind)
        assertEquals("Minions\nP2 · P5", steps.demon.displayPrimary)
        assertEquals("Bluffs\nMayor · Monk · Undertaker", steps.demon.displaySecondary)
    }

    @Test
    fun `pending or invalid bluff presentation keeps Demon reveal unavailable without inventing content`() {
        val steps = buildClocktowerFirstNightEvilInformationSteps(
            minionActor = minion,
            demonActor = demon,
            shouldGiveInformation = true,
            text = preparedText().copy(
                demonTellPlayer = null,
                demonDisplaySecondary = null,
                demonExplain = "Bluffs unavailable",
            ),
        )

        assertTrue(steps.demon.isRealAction)
        assertEquals(ClocktowerDisplayKind.EvilInfo, steps.demon.displayKind)
        assertNull(steps.demon.tellPlayer)
        assertNull(steps.demon.displaySecondary)
        assertEquals("Bluffs unavailable", steps.demon.explanation)
    }

    @Test
    fun `small game suppresses both evil information actions and player display`() {
        val steps = buildClocktowerFirstNightEvilInformationSteps(
            minionActor = minion,
            demonActor = demon,
            shouldGiveInformation = false,
            text = preparedText(),
        )

        assertFalse(steps.minion.isRealAction)
        assertNull(steps.minion.actor)
        assertEquals("Small game", steps.minion.reason)
        assertEquals("Placeholder", steps.minion.storytellerAction)
        assertNull(steps.minion.tellPlayer)
        assertEquals(ClocktowerDisplayKind.None, steps.minion.displayKind)

        assertFalse(steps.demon.isRealAction)
        assertNull(steps.demon.actor)
        assertEquals("Small game", steps.demon.reason)
        assertEquals("Placeholder", steps.demon.storytellerAction)
        assertNull(steps.demon.tellPlayer)
        assertEquals(ClocktowerDisplayKind.None, steps.demon.displayKind)
    }

    @Test
    fun `missing Minion remains a placeholder while Demon information stays real`() {
        val steps = buildClocktowerFirstNightEvilInformationSteps(
            minionActor = null,
            demonActor = demon,
            shouldGiveInformation = true,
            text = preparedText(),
        )

        assertFalse(steps.minion.isRealAction)
        assertEquals("No minions", steps.minion.reason)
        assertEquals(ClocktowerDisplayKind.None, steps.minion.displayKind)

        assertTrue(steps.demon.isRealAction)
        assertEquals(ClocktowerDisplayKind.EvilInfo, steps.demon.displayKind)
    }

    private fun preparedText() = ClocktowerFirstNightEvilInformationPreparedText(
        minionTitle = "Minion info",
        demonTitle = "Demon info",
        smallGameNoEvilInfoReason = "Small game",
        noMinionsReason = "No minions",
        noDemonReason = "No demon",
        placeholderAction = "Placeholder",
        minionActionText = "Wake minions",
        minionTellPlayer = "Demon is P7",
        minionExplain = "Show demon",
        smallGameNoEvilInfoExplain = "No evil info",
        minionDisplayPrimary = "Demon\nP7",
        minionWakeText = "Wake P2 · P5",
        demonActionText = "Wake demon",
        demonTellPlayer = "Minions P2 · P5\nBluffs Mayor · Monk · Undertaker",
        demonExplain = "Show minions and bluffs",
        demonDisplayPrimary = "Minions\nP2 · P5",
        demonDisplaySecondary = "Bluffs\nMayor · Monk · Undertaker",
    )
}
