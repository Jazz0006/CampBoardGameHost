package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GrimoireWorldFilteringSemanticsTest {
    private val script = ScriptId("trouble_brewing")
    private val roles = listOf(
        role("Empath", Alignment.GOOD, CharacterType.TOWNSFOLK),
        role("Mayor", Alignment.GOOD, CharacterType.TOWNSFOLK),
        role("Fortune Teller", Alignment.GOOD, CharacterType.TOWNSFOLK),
        role("Monk", Alignment.GOOD, CharacterType.TOWNSFOLK),
        role("Drunk", Alignment.GOOD, CharacterType.OUTSIDER),
        role("Poisoner", Alignment.EVIL, CharacterType.MINION),
        role("Spy", Alignment.EVIL, CharacterType.MINION),
    ).associateBy(RoleDefinition::id)

    @Test
    fun `only audited TB reminder identities expose current world constraints`() {
        assertEquals(
            GrimoireReminderWorldConstraint.POISONER_TARGET,
            TroubleBrewingGrimoireReminderSemantics.worldConstraint(
                token("Poisoner", GrimoireReminderTokenScope.CHARACTER, "Poisoned"),
            ),
        )
        assertEquals(
            GrimoireReminderWorldConstraint.RED_HERRING,
            TroubleBrewingGrimoireReminderSemantics.worldConstraint(
                token("Fortune Teller", GrimoireReminderTokenScope.CHARACTER, "Red Herring"),
            ),
        )
        assertEquals(
            GrimoireReminderWorldConstraint.DRUNK_IDENTITY,
            TroubleBrewingGrimoireReminderSemantics.worldConstraint(
                token("Drunk", GrimoireReminderTokenScope.GLOBAL, "Is The Drunk"),
            ),
        )
        assertNull(
            TroubleBrewingGrimoireReminderSemantics.worldConstraint(
                token("Monk", GrimoireReminderTokenScope.CHARACTER, "Safe"),
            ),
        )
    }

    @Test
    fun `recognized mechanical reminder identity rejects a spoofed label`() {
        assertThrows(IllegalArgumentException::class.java) {
            TroubleBrewingGrimoireReminderSemantics.worldConstraint(
                token("Poisoner", GrimoireReminderTokenScope.CHARACTER, "Not Poisoned"),
            )
        }
    }

    @Test
    fun `exact grimoire proposition cannot match a sparse world roster`() {
        val world = world(
            1 to "Empath",
            2 to "Poisoner",
            3 to "Spy",
        )
        val sparse = grimoire(
            view(1, "Empath"),
            view(2, "Poisoner"),
        )

        assertFalse(matches(world, sparse))
    }

    @Test
    fun `grimoire character token is physical display truth not player perceived shown role`() {
        val world = EnumeratedWorld(
            rolesBySeat = sortedMapOf(1 to RoleId("Empath"), 2 to RoleId("Spy")),
            shownRolesBySeat = mapOf(1 to RoleId("Mayor")),
        )
        val grimoire = grimoire(
            view(1, "Empath"),
            view(2, "Spy"),
        )

        assertTrue(matches(world, grimoire))
    }

    @Test
    fun `drunk reminder binds actual Drunk to the out of play Townsfolk character token`() {
        val world = EnumeratedWorld(
            rolesBySeat = sortedMapOf(1 to RoleId("Drunk"), 2 to RoleId("Spy")),
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_DRUNK),
        )
        val correct = grimoire(
            view(1, "Mayor", drunkToken()),
            view(2, "Spy"),
        )
        val missingMarker = grimoire(
            view(1, "Mayor"),
            view(2, "Spy"),
        )
        val markerOnWrongSeat = grimoire(
            view(1, "Mayor"),
            view(2, "Spy", drunkToken()),
        )

        assertTrue(matches(world, correct))
        assertFalse(matches(world, missingMarker))
        assertFalse(matches(world, markerOnWrongSeat))
    }

    @Test
    fun `red herring reminder must exactly match the world red herring seat`() {
        val world = EnumeratedWorld(
            rolesBySeat = sortedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Fortune Teller"),
                3 to RoleId("Spy"),
            ),
            redHerringSeat = 1,
        )
        val correct = grimoire(
            view(1, "Empath", redHerringToken()),
            view(2, "Fortune Teller"),
            view(3, "Spy"),
        )
        val missingMarker = grimoire(
            view(1, "Empath"),
            view(2, "Fortune Teller"),
            view(3, "Spy"),
        )
        val wrongSeat = grimoire(
            view(1, "Empath"),
            view(2, "Fortune Teller"),
            view(3, "Spy", redHerringToken()),
        )

        assertTrue(matches(world, correct))
        assertFalse(matches(world, missingMarker))
        assertFalse(matches(world, wrongSeat))
    }

    @Test
    fun `poisoned reminder must exactly match the current Poisoner target`() {
        val world = EnumeratedWorld(
            rolesBySeat = sortedMapOf(
                1 to RoleId("Empath"),
                2 to RoleId("Poisoner"),
                3 to RoleId("Spy"),
            ),
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_POISONED),
        )
        val correct = grimoire(
            view(1, "Empath", poisonedToken()),
            view(2, "Poisoner"),
            view(3, "Spy"),
        )
        val missingMarker = grimoire(
            view(1, "Empath"),
            view(2, "Poisoner"),
            view(3, "Spy"),
        )

        assertTrue(matches(world, correct))
        assertFalse(matches(world, missingMarker))
    }

    @Test
    fun `poisoned reminder still identifies a Poisoner target that is already the Drunk`() {
        val world = EnumeratedWorld(
            rolesBySeat = sortedMapOf(
                1 to RoleId("Drunk"),
                2 to RoleId("Poisoner"),
                3 to RoleId("Spy"),
            ),
            abilityStatesBySeat = mapOf(1 to AbilityState.MALFUNCTIONING_DRUNK),
        )
        val correct = grimoire(
            view(1, "Mayor", drunkToken(), poisonedToken()),
            view(2, "Poisoner"),
            view(3, "Spy"),
        )
        val poisonOnWrongSeat = grimoire(
            view(1, "Mayor", drunkToken()),
            view(2, "Poisoner"),
            view(3, "Spy", poisonedToken()),
        )

        assertTrue(matches(world, correct))
        assertFalse(matches(world, poisonOnWrongSeat))
    }

    @Test
    fun `visible but unmodelled reminder does not eliminate a mechanically matching world`() {
        val world = world(
            1 to "Empath",
            2 to "Monk",
            3 to "Spy",
        )
        val withSafeReminder = grimoire(
            view(1, "Empath", token("Monk", GrimoireReminderTokenScope.CHARACTER, "Safe")),
            view(2, "Monk"),
            view(3, "Spy"),
        )

        assertTrue(matches(world, withSafeReminder))
    }

    private fun matches(
        world: EnumeratedWorld,
        grimoire: InformationProposition.GrimoireState,
    ): Boolean = TroubleBrewingWorldObservationEvaluator.evaluateKnownFact(world, roles, grimoire)

    private fun world(vararg seats: Pair<Int, String>): EnumeratedWorld = EnumeratedWorld(
        rolesBySeat = seats.associate { (seat, role) -> seat to RoleId(role) }.toSortedMap(),
    )

    private fun grimoire(vararg seats: GrimoireSeatView): InformationProposition.GrimoireState =
        InformationProposition.GrimoireState(seats.toList())

    private fun view(
        seat: Int,
        displayedRole: String,
        vararg tokens: GrimoireReminderTokenRef,
    ): GrimoireSeatView = GrimoireSeatView(
        seat = seat,
        displayedRole = RoleId(displayedRole),
        alive = true,
        reminderTokens = tokens.sorted(),
    )

    private fun poisonedToken(): GrimoireReminderTokenRef =
        token("Poisoner", GrimoireReminderTokenScope.CHARACTER, "Poisoned")

    private fun redHerringToken(): GrimoireReminderTokenRef =
        token("Fortune Teller", GrimoireReminderTokenScope.CHARACTER, "Red Herring")

    private fun drunkToken(): GrimoireReminderTokenRef =
        token("Drunk", GrimoireReminderTokenScope.GLOBAL, "Is The Drunk")

    private fun token(
        sourceRole: String,
        scope: GrimoireReminderTokenScope,
        label: String,
        occurrence: Int = 1,
    ): GrimoireReminderTokenRef = GrimoireReminderTokenRef(
        sourceRole = RoleId(sourceRole),
        scope = scope,
        label = label,
        occurrence = occurrence,
    )

    private fun role(
        id: String,
        alignment: Alignment,
        type: CharacterType,
    ): RoleDefinition = RoleDefinition(RoleId(id), alignment, type, setOf(script))
}
