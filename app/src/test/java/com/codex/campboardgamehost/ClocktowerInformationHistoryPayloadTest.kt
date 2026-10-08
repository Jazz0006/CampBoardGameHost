package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Test

class ClocktowerInformationHistoryPayloadTest {
    private val playerNames = listOf("Alice", "Bob", "Carol")

    @Test
    fun `English history detail follows each player-visible display kind`() {
        val cases = listOf(
            Triple(ClocktowerDisplayKind.EitherOne, "Washerwoman: seats 2 / 3", "Washerwoman"),
            Triple(ClocktowerDisplayKind.Number, "Evil neighbors: 1", "1"),
            Triple(ClocktowerDisplayKind.YesNo, "Checked seats 2 + 3: Yes", "Yes"),
            Triple(ClocktowerDisplayKind.RoleReveal, "Spy", "Spy"),
            Triple(ClocktowerDisplayKind.Grimoire, "Spy viewed the grimoire", "grimoire"),
            Triple(ClocktowerDisplayKind.Plain, "shown", "shown"),
        )
        cases.forEach { (kind, expected, primary) ->
            val payload = project(
                step(
                    kind = kind,
                    primary = primary,
                    secondary = " 2   3 ",
                    footer = "Evil neighbors",
                ),
            )
            assertEquals(kind.name, expected, payload.detail)
            assertEquals(kind.name, ClocktowerEventType.Information, payload.type)
        }
    }

    @Test
    fun `Chinese history detail retains the existing separators and wording`() {
        val cases = listOf(
            ClocktowerDisplayKind.EitherOne to "角色 在 2 / 3 号之中",
            ClocktowerDisplayKind.Number to "信息：角色",
            ClocktowerDisplayKind.YesNo to "查验 2 + 3 号：角色",
            ClocktowerDisplayKind.Grimoire to "间谍查看了魔典",
        )
        cases.forEach { (kind, expected) ->
            val payload = project(step(kind, "角色", "2   3", "信息"), language = "zh")
            assertEquals(kind.name, expected, payload.detail)
        }
    }

    @Test
    fun `history keeps actor first and resolves distinct referenced seats from secondary and footer`() {
        val payload = project(step(
            kind = ClocktowerDisplayKind.YesNo,
            primary = "Yes",
            secondary = "2   1",
            footer = "Player 3 and 2",
        ))

        assertEquals(listOf("Alice", "Bob", "Carol"), payload.playerNames)
    }

    @Test
    fun `unreliable and misleading titles depend on the selected truthfulness`() {
        val base = step(ClocktowerDisplayKind.RoleReveal, "Spy", null, null)
        val unreliable = project(base.copy(selectedInformationTruthful = true), unreliable = true)
        val misleading = project(base.copy(selectedInformationTruthful = false), unreliable = true)
        val reliable = project(base.copy(selectedInformationTruthful = false), unreliable = false)

        assertEquals(ClocktowerEventType.UnreliableInformation, unreliable.type)
        assertEquals("Ravenkeeper information (unreliable)", unreliable.title)
        assertEquals(ClocktowerEventType.UnreliableInformation, misleading.type)
        assertEquals("Ravenkeeper information (misleading)", misleading.title)
        assertEquals(ClocktowerEventType.Information, reliable.type)
        assertEquals("Ravenkeeper information", reliable.title)
    }

    @Test
    fun `legacy information falls back to tell-player text when display primary is absent`() {
        val step = step(ClocktowerDisplayKind.Number, "unused", null, "Evil neighbors")
            .copy(displayPrimary = null, tellPlayer = "2")

        assertEquals("Evil neighbors: 2", project(step).detail)
    }

    private fun project(
        step: ClocktowerNightStepUi,
        language: String = "en",
        unreliable: Boolean = false,
    ) = clocktowerInformationHistoryPayload(
        displayStep = step,
        orderedPlayerNames = playerNames,
        unreliable = unreliable,
        text = { zh, en -> if (language == "zh") zh else en },
    )

    private fun step(
        kind: ClocktowerDisplayKind,
        primary: String,
        secondary: String?,
        footer: String?,
    ) = ClocktowerNightStepUi(
        title = "Ravenkeeper",
        actor = PlayerCard("Alice", Role.Civilian, ""),
        isRealAction = true,
        reason = "",
        storytellerAction = "",
        tellPlayer = null,
        explanation = "",
        displayKind = kind,
        displayTitle = "Ravenkeeper information",
        displayPrimary = primary,
        displaySecondary = secondary,
        displayFooter = footer,
    )
}
