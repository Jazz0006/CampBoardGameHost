package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Game-independent first screen of the hosted-session flow.
 *
 * The physical table exists immediately. Player arrangement remains editable until the explicit
 * Confirm seats action freezes it into [ConfirmedHostSeating].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SeatingFirstSetupScreen(
    savedGamePreview: SavedGamePreview?,
    commonPlayers: List<String>,
    playerNames: List<String>,
    onAddCurrentPlayer: (String) -> Unit,
    onRemoveCurrentPlayer: (Int) -> Unit,
    onMoveCurrentPlayerTo: (Int, Int) -> Unit,
    onResumeSavedGame: () -> Unit,
    onDiscardSavedGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onConfirmSeats: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh

    var selectedPlayerName by remember { mutableStateOf<String?>(null) }
    var newPlayerName by remember { mutableStateOf("") }

    val seats = playerNames.mapIndexed { index, playerName ->
        HostSeatPresentation(
            seatId = ClocktowerSeatId(index + 1),
            playerName = playerName,
            isAlive = true,
        )
    }
    val selectedIndex = selectedPlayerName?.let(playerNames::indexOf)?.takeIf { it >= 0 }
    val interaction = HostTableInteractionState(
        mode = if (seats.isEmpty()) HostTableInteractionMode.ReadOnly else HostTableInteractionMode.Selection,
        selectableSeatIds = seats.map { it.seatId }.toSet(),
    )
    val trimmedNewPlayerName = newPlayerName.trim()
    val canAddTypedPlayer = trimmedNewPlayerName.isNotEmpty() &&
        trimmedNewPlayerName !in playerNames &&
        playerNames.size < MAX_PLAYERS
    val availableCommonPlayers = commonPlayers
        .filterNot(playerNames::contains)
        .distinct()

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = text("安排玩家与座位", "Arrange players and seats"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = text(
                            "长按拖动座位；确认后整局保持相同位置",
                            "Long-press and drag seats; confirmed positions stay fixed for the whole game",
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = onOpenSettings) {
                    Text("⚙")
                }
            }

            savedGamePreview?.let { preview ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = preview.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(onClick = onResumeSavedGame) {
                        Text(text("继续", "Resume"))
                    }
                    TextButton(onClick = onDiscardSavedGame) {
                        Text(text("放弃", "Discard"))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                HostTableShell(
                    seats = seats,
                    modifier = Modifier.fillMaxSize(),
                    interaction = interaction,
                    onSeatClick = { seatId ->
                        selectedPlayerName = playerNames.getOrNull(seatId.number - 1)
                    },
                    dragEnabled = seats.size > 1,
                    neutralSelectionChrome = true,
                    seatMotionKey = HostSeatPresentation::playerName,
                    onSeatDragCommit = { seatId, targetRingIndex ->
                        val fromIndex = seatId.number - 1
                        if (
                            fromIndex in playerNames.indices &&
                            targetRingIndex in playerNames.indices &&
                            fromIndex != targetRingIndex
                        ) {
                            onMoveCurrentPlayerTo(fromIndex, targetRingIndex)
                        }
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        selectedIndex?.let { index ->
                            Text(
                                text = "${clocktowerSeatNumberLabel(index + 1, language)} · ${playerNames[index]}",
                                fontWeight = FontWeight.SemiBold,
                            )
                            OutlinedButton(
                                onClick = {
                                    onRemoveCurrentPlayer(index)
                                    selectedPlayerName = null
                                },
                            ) {
                                Text(text("移除", "Remove"))
                            }
                        }

                        if (availableCommonPlayers.isNotEmpty()) {
                            Text(
                                text = text("常用玩家", "Recent players"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                availableCommonPlayers.forEach { playerName ->
                                    OutlinedButton(
                                        onClick = { onAddCurrentPlayer(playerName) },
                                        enabled = playerNames.size < MAX_PLAYERS,
                                    ) {
                                        Text(playerName)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { newPlayerName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(text("新玩家", "New player")) },
                            singleLine = true,
                        )
                        Button(
                            onClick = {
                                onAddCurrentPlayer(trimmedNewPlayerName)
                                newPlayerName = ""
                            },
                            enabled = canAddTypedPlayer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text("添加玩家", "Add player"))
                        }
                    }
                }
            }

            Button(
                onClick = onConfirmSeats,
                enabled = playerNames.size >= MIN_PLAYERS,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = text("确定座位", "Confirm seats"),
                    fontWeight = FontWeight.Bold,
                )
            }
            }
        }
    }
}

/** Choose the hosted game only after the physical seating roster is frozen. */
@Composable
internal fun SeatingFirstGameSelectionScreen(
    seating: ConfirmedHostSeating,
    onBackToSeating: () -> Unit,
    onOpenUndercoverSettings: () -> Unit,
    onOpenWerewolfSettings: () -> Unit,
    onOpenClocktowerSettings: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val playerCount = seating.seats.size

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackToSeating) {
                    Text(text("重新安排座位", "Edit seats"))
                }
                Text(
                    text = text("选择游戏", "Choose game"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(text("${playerCount}人", "$playerCount players"))
            }

            HostTableShell(
                seats = seating.toHostSeatPresentations(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onOpenClocktowerSettings,
                        enabled = playerCount >= MIN_CLOCKTOWER_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("血染钟楼", "Blood on the Clocktower"))
                    }
                    OutlinedButton(
                        onClick = onOpenUndercoverSettings,
                        enabled = playerCount >= MIN_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("谁是卧底", "Who is Undercover"))
                    }
                    OutlinedButton(
                        onClick = onOpenWerewolfSettings,
                        enabled = playerCount >= MIN_WEREWOLF_PLAYERS,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text("狼人杀", "Werewolf"))
                    }
                }
            }
            }
        }
    }
}
