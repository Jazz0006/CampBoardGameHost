from pathlib import Path


def replace_once(path: str, old: str, new: str) -> None:
    p = Path(path)
    text = p.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{path}: expected exactly one anchor, found {count}")
    p.write_text(text.replace(old, new, 1))


def replace_first(path: str, old: str, new: str) -> None:
    p = Path(path)
    text = p.read_text()
    if old not in text:
        raise SystemExit(f"{path}: anchor not found")
    p.write_text(text.replace(old, new, 1))


Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerSeatNumberLabel.kt").write_text(
    '''package com.codex.campboardgamehost

/** Presentation-only seat number label. Typed [ClocktowerSeatId] remains the semantic authority. */
internal fun clocktowerSeatNumberLabel(
    seatNumber: Int,
    languageCode: String,
): String {
    require(seatNumber > 0) { "Seat number must be positive" }
    return if (languageCode.lowercase() == "zh") "${seatNumber}号" else "#$seatNumber"
}
'''
)

Path("app/src/test/java/com/codex/campboardgamehost/ClocktowerSeatNumberLabelTest.kt").write_text(
    '''package com.codex.campboardgamehost

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ClocktowerSeatNumberLabelTest {
    @Test
    fun `seat numbers localize to Chinese suffix and English hash`() {
        assertEquals("2号", clocktowerSeatNumberLabel(2, "zh"))
        assertEquals("#10", clocktowerSeatNumberLabel(10, "en"))
    }

    @Test
    fun `seat number labels fail closed for invalid physical identity`() {
        assertThrows(IllegalArgumentException::class.java) {
            clocktowerSeatNumberLabel(0, "zh")
        }
    }
}
'''
)

host_ui = "app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt"
replace_once(
    host_ui,
    '''    dragEnabled: Boolean = false,\n    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },\n''',
    '''    dragEnabled: Boolean = false,\n    neutralSelectionChrome: Boolean = false,\n    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },\n''',
)
replace_once(
    host_ui,
    '''            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n            )\n''',
    '''            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n            )\n''',
)
replace_once(
    host_ui,
    '''private fun HostTableSeatFrame.toSquareTableSeatUiModel(\n    motionKey: String,\n): ClocktowerSquareTableSeatUiModel =\n    ClocktowerSquareTableSeatUiModel(\n        seatId = seat.seatId.renderKey(),\n        seatNumber = seat.seatId.number,\n        label = hostTablePrimarySeatLabel(seat),\n        state = squareTableSeatState(),\n        motionKey = motionKey,\n    )\n''',
    '''private fun HostTableSeatFrame.toSquareTableSeatUiModel(\n    motionKey: String,\n    neutralSelectionChrome: Boolean,\n): ClocktowerSquareTableSeatUiModel =\n    ClocktowerSquareTableSeatUiModel(\n        seatId = seat.seatId.renderKey(),\n        seatNumber = seat.seatId.number,\n        label = hostTablePrimarySeatLabel(seat),\n        state = if (neutralSelectionChrome) {\n            ClocktowerSquareTableSeatState.Neutral\n        } else {\n            squareTableSeatState()\n        },\n        isInteractionEnabled = isSelectable && !isLocked,\n        motionKey = motionKey,\n    )\n''',
)

square_ui = "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt"
replace_once(
    square_ui,
    'import androidx.compose.ui.platform.LocalDensity\n',
    'import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalDensity\n',
)
replace_once(
    square_ui,
    '''    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,\n    val motionKey: String = seatId,\n)\n''',
    '''    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,\n    val isInteractionEnabled: Boolean = state in setOf(\n        ClocktowerSquareTableSeatState.Selectable,\n        ClocktowerSquareTableSeatState.SelectedFirst,\n        ClocktowerSquareTableSeatState.SelectedSecond,\n    ),\n    val motionKey: String = seatId,\n)\n''',
)
replace_once(
    square_ui,
    '''    val canSelect = interactionMode == ClocktowerSquareTableInteractionMode.Selectable &&\n        seat.state in setOf(\n            ClocktowerSquareTableSeatState.Selectable,\n            ClocktowerSquareTableSeatState.SelectedFirst,\n            ClocktowerSquareTableSeatState.SelectedSecond,\n        )\n    val palette = clocktowerSquareTableSeatPalette(seat.state)\n''',
    '''    val canSelect = interactionMode == ClocktowerSquareTableInteractionMode.Selectable &&\n        seat.isInteractionEnabled\n    val palette = clocktowerSquareTableSeatPalette(seat.state)\n    val language = LocalContext.current.resources.configuration.locales[0].language\n''',
)
replace_once(
    square_ui,
    '                    text = "#${seat.seatNumber}",\n',
    '                    text = clocktowerSeatNumberLabel(seat.seatNumber, language),\n',
)
replace_once(
    square_ui,
    '        tonalElevation = if (canSelect) 2.dp else 0.dp,\n',
    '        tonalElevation = if (canSelect && seat.state != ClocktowerSquareTableSeatState.Neutral) 2.dp else 0.dp,\n',
)

player_ui = "app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt"
replace_once(
    player_ui,
    'import androidx.compose.ui.graphics.Color\n',
    'import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.platform.LocalContext\n',
)
replace_once(
    player_ui,
    '''private fun ClocktowerPairPlayerRevealContent(\n    presentation: ClocktowerPairPlayerRevealPresentation,\n) {\n    Column(\n''',
    '''private fun ClocktowerPairPlayerRevealContent(\n    presentation: ClocktowerPairPlayerRevealPresentation,\n) {\n    val language = LocalContext.current.resources.configuration.locales[0].language\n    Column(\n''',
)
replace_once(
    player_ui,
    '                        text = "#${seat.seatId.number}",\n',
    '                        text = clocktowerSeatNumberLabel(seat.seatId.number, language),\n',
)

setup_ui = "app/src/main/java/com/codex/campboardgamehost/SeatingFirstSetupUi.kt"
replace_once(setup_ui, 'import androidx.compose.foundation.background\n', '')
replace_once(
    setup_ui,
    'import androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.Text\n',
    'import androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.Surface\nimport androidx.compose.material3.Text\n',
)
replace_once(
    setup_ui,
    '''    val selectedIndex = selectedPlayerName?.let(playerNames::indexOf)?.takeIf { it >= 0 }\n    val selectedSeatId = selectedIndex?.let { ClocktowerSeatId(it + 1) }\n    val interaction = HostTableInteractionState(\n        mode = if (seats.isEmpty()) HostTableInteractionMode.ReadOnly else HostTableInteractionMode.Selection,\n        selectableSeatIds = seats.map { it.seatId }.toSet(),\n        selectedSeatIds = selectedSeatId?.let(::listOf).orEmpty(),\n    )\n''',
    '''    val selectedIndex = selectedPlayerName?.let(playerNames::indexOf)?.takeIf { it >= 0 }\n    val interaction = HostTableInteractionState(\n        mode = if (seats.isEmpty()) HostTableInteractionMode.ReadOnly else HostTableInteractionMode.Selection,\n        selectableSeatIds = seats.map { it.seatId }.toSet(),\n    )\n''',
)

root_old = '''    ClocktowerDarkTheme {\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .background(MaterialTheme.colorScheme.background)\n                .padding(12.dp),\n            verticalArrangement = Arrangement.spacedBy(8.dp),\n        ) {\n'''
root_new = '''    ClocktowerDarkTheme {\n        Surface(\n            modifier = Modifier.fillMaxSize(),\n            color = MaterialTheme.colorScheme.background,\n            contentColor = MaterialTheme.colorScheme.onBackground,\n        ) {\n            Column(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .padding(12.dp),\n                verticalArrangement = Arrangement.spacedBy(8.dp),\n            ) {\n'''
replace_first(setup_ui, root_old, root_new)

replace_once(
    setup_ui,
    '''                    dragEnabled = seats.size > 1,\n                    seatMotionKey = HostSeatPresentation::playerName,\n''',
    '''                    dragEnabled = seats.size > 1,\n                    neutralSelectionChrome = true,\n                    seatMotionKey = HostSeatPresentation::playerName,\n''',
)
replace_once(
    setup_ui,
    '''                                text = text(\n                                    "座位 ${index + 1} · ${playerNames[index]}",\n                                    "Seat ${index + 1} · ${playerNames[index]}",\n                                ),\n''',
    '''                                text = "${clocktowerSeatNumberLabel(index + 1, language)} · ${playerNames[index]}",\n''',
)
replace_once(
    setup_ui,
    '''                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {\n                                OutlinedButton(\n                                    onClick = { onMoveCurrentPlayerTo(index, index - 1) },\n                                    enabled = index > 0,\n                                ) {\n                                    Text(text("前移", "Earlier"))\n                                }\n                                OutlinedButton(\n                                    onClick = { onMoveCurrentPlayerTo(index, index + 1) },\n                                    enabled = index < playerNames.lastIndex,\n                                ) {\n                                    Text(text("后移", "Later"))\n                                }\n                                OutlinedButton(\n                                    onClick = {\n                                        onRemoveCurrentPlayer(index)\n                                        selectedPlayerName = null\n                                    },\n                                ) {\n                                    Text(text("移除", "Remove"))\n                                }\n                            }\n''',
    '''                            OutlinedButton(\n                                onClick = {\n                                    onRemoveCurrentPlayer(index)\n                                    selectedPlayerName = null\n                                },\n                            ) {\n                                Text(text("移除", "Remove"))\n                            }\n''',
)
replace_once(
    setup_ui,
    '''            ) {\n                Text(\n                    text = text("确定座位", "Confirm seats"),\n                    fontWeight = FontWeight.Bold,\n                )\n            }\n        }\n    }\n}\n\n/** Choose the hosted game only after the physical seating roster is frozen. */\n''',
    '''            ) {\n                Text(\n                    text = text("确定座位", "Confirm seats"),\n                    fontWeight = FontWeight.Bold,\n                )\n            }\n            }\n        }\n    }\n}\n\n/** Choose the hosted game only after the physical seating roster is frozen. */\n''',
)

# After replacing the first root, exactly one old root remains: Game Selection.
replace_once(setup_ui, root_old, root_new)
replace_once(
    setup_ui,
    '''            HostTableShell(\n                seats = seating.toHostSeatPresentations(),\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .weight(1f),\n            ) {\n                Column(\n                    modifier = Modifier.fillMaxWidth(),\n                    verticalArrangement = Arrangement.spacedBy(8.dp),\n                ) {\n                    Button(\n                        onClick = onOpenClocktowerSettings,\n                        enabled = playerCount >= MIN_CLOCKTOWER_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("血染钟楼", "Blood on the Clocktower"))\n                    }\n                    OutlinedButton(\n                        onClick = onOpenUndercoverSettings,\n                        enabled = playerCount >= MIN_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("谁是卧底", "Who is Undercover"))\n                    }\n                    OutlinedButton(\n                        onClick = onOpenWerewolfSettings,\n                        enabled = playerCount >= MIN_WEREWOLF_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("狼人杀", "Werewolf"))\n                    }\n                }\n            }\n        }\n    }\n}\n''',
    '''            HostTableShell(\n                seats = seating.toHostSeatPresentations(),\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .weight(1f),\n            ) {\n                Column(\n                    modifier = Modifier.fillMaxWidth(),\n                    verticalArrangement = Arrangement.spacedBy(8.dp),\n                ) {\n                    Button(\n                        onClick = onOpenClocktowerSettings,\n                        enabled = playerCount >= MIN_CLOCKTOWER_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("血染钟楼", "Blood on the Clocktower"))\n                    }\n                    OutlinedButton(\n                        onClick = onOpenUndercoverSettings,\n                        enabled = playerCount >= MIN_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("谁是卧底", "Who is Undercover"))\n                    }\n                    OutlinedButton(\n                        onClick = onOpenWerewolfSettings,\n                        enabled = playerCount >= MIN_WEREWOLF_PLAYERS,\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(text("狼人杀", "Werewolf"))\n                    }\n                }\n            }\n            }\n        }\n    }\n}\n''',
)
