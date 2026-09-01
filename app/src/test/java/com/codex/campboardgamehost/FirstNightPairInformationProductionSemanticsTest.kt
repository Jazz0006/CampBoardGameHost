package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.GameState
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.fixtures.TroubleBrewingFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstNightPairInformationProductionSemanticsTest {
    private val roleDefinitions = TroubleBrewingFixtures.fullRoleDefinitions()

    @Test
    fun `drunk shown librarian can truthfully receive no outsiders when the only outsider is self`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Librarian"),
            player(2, "Chef", CharacterType.TOWNSFOLK),
            player(3, "Empath", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val noOutsider = ClocktowerDisplayOption(
            label = "No Outsiders",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Librarian information",
            displayPrimary = "No Outsiders",
            displaySecondary = null,
            displayFooter = null,
            proposition = InformationProposition.AllOf(
                roleDefinitions
                    .filter { it.type == CharacterType.OUTSIDER }
                    .map { InformationProposition.RoleInPlay(it.id, false) },
            ),
            isTruthful = false,
            misinformationPressure = 3,
        )

        val firstNight = projectFirstNightPairInformationOptions(
            phase = ClocktowerPhase.FirstNight,
            roleEnName = "Librarian",
            sourceSeat = 1,
            game = game,
            roleDefinitions = roleDefinitions,
            options = listOf(noOutsider),
        )
        val otherNight = projectFirstNightPairInformationOptions(
            phase = ClocktowerPhase.Night,
            roleEnName = "Librarian",
            sourceSeat = 1,
            game = game,
            roleDefinitions = roleDefinitions,
            options = listOf(noOutsider),
        )

        assertTrue(firstNight.single().isTruthful)
        assertEquals(0, firstNight.single().misinformationPressure)
        assertEquals(listOf(noOutsider), otherNight)
    }

    @Test
    fun `drunk shown washerwoman preserves spy registered townsfolk truth`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Washerwoman"),
            player(2, "Spy", CharacterType.MINION),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val registeredTruth = pairOption(
            title = "Washerwoman information",
            shownRole = "Monk",
            firstSeat = 2,
            secondSeat = 3,
        )

        val projected = projectFirstNightPairInformationOptions(
            phase = ClocktowerPhase.FirstNight,
            roleEnName = "Washerwoman",
            sourceSeat = 1,
            game = game,
            roleDefinitions = roleDefinitions,
            options = listOf(registeredTruth),
        ).single()

        assertTrue(projected.isTruthful)
        assertEquals(0, projected.misinformationPressure)
        assertEquals(true, projected.spyRegistersGood)
        assertEquals("Monk", projected.spyRegisteredRoleEnName)
    }

    @Test
    fun `drunk shown librarian preserves spy registered outsider truth when there are no natural outsiders`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Librarian"),
            player(2, "Spy", CharacterType.MINION),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val registeredTruth = pairOption(
            title = "Librarian information",
            shownRole = "Saint",
            firstSeat = 2,
            secondSeat = 3,
        )

        val projected = projectFirstNightPairInformationOptions(
            phase = ClocktowerPhase.FirstNight,
            roleEnName = "Librarian",
            sourceSeat = 1,
            game = game,
            roleDefinitions = roleDefinitions,
            options = listOf(registeredTruth),
        ).single()

        assertTrue(projected.isTruthful)
        assertEquals(0, projected.misinformationPressure)
        assertEquals(true, projected.spyRegistersGood)
        assertEquals("Saint", projected.spyRegisteredRoleEnName)
    }

    @Test
    fun `drunk shown investigator preserves recluse registered minion truth`() {
        val game = game(
            player(1, "Drunk", CharacterType.OUTSIDER, shownRole = "Investigator"),
            player(2, "Recluse", CharacterType.OUTSIDER),
            player(3, "Chef", CharacterType.TOWNSFOLK),
            player(4, "Poisoner", CharacterType.MINION),
            player(5, "Imp", CharacterType.DEMON),
        )
        val registeredTruth = ClocktowerDisplayOption(
            label = "Scarlet Woman: 2 / 3",
            displayKind = ClocktowerDisplayKind.EitherOne,
            displayTitle = "Investigator information",
            displayPrimary = "Scarlet Woman",
            displaySecondary = "2 / 3",
            displayFooter = null,
            proposition = InformationProposition.AnyOf(
                listOf(
                    InformationProposition.RoleAt(2, RoleId("Scarlet Woman")),
                    InformationProposition.RoleAt(3, RoleId("Scarlet Woman")),
                ),
            ),
            isTruthful = false,
            misinformationPressure = 3,
        )

        val projected = projectFirstNightPairInformationOptions(
            phase = ClocktowerPhase.FirstNight,
            roleEnName = "Investigator",
            sourceSeat = 1,
            game = game,
            roleDefinitions = roleDefinitions,
            options = listOf(registeredTruth),
        ).single()

        assertTrue(projected.isTruthful)
        assertEquals(0, projected.misinformationPressure)
        assertEquals(true, projected.recluseRegistersEvil)
        assertEquals("Scarlet Woman", projected.recluseRegisteredRoleEnName)
    }

    private fun pairOption(
        title: String,
        shownRole: String,
        firstSeat: Int,
        secondSeat: Int,
    ) = ClocktowerDisplayOption(
        label = "$shownRole: $firstSeat / $secondSeat",
        displayKind = ClocktowerDisplayKind.EitherOne,
        displayTitle = title,
        displayPrimary = shownRole,
        displaySecondary = "$firstSeat / $secondSeat",
        displayFooter = null,
        proposition = InformationProposition.AnyOf(
            listOf(
                InformationProposition.RoleAt(firstSeat, RoleId(shownRole)),
                InformationProposition.RoleAt(secondSeat, RoleId(shownRole)),
            ),
        ),
        isTruthful = false,
        misinformationPressure = 3,
    )

    private fun game(vararg players: PlayerState) = GameState(
        script = ScriptId("trouble_brewing"),
        players = players.toList(),
        seed = 20260901L,
    )

    private fun player(
        seat: Int,
        role: String,
        type: CharacterType,
        shownRole: String = role,
    ) = PlayerState(
        seat = seat,
        name = "Player $seat",
        actualRole = RoleId(role),
        actualAlignment = when (type) {
            CharacterType.TOWNSFOLK, CharacterType.OUTSIDER -> Alignment.GOOD
            CharacterType.MINION, CharacterType.DEMON -> Alignment.EVIL
        },
        actualType = type,
        shownRole = RoleId(shownRole),
    )
}
