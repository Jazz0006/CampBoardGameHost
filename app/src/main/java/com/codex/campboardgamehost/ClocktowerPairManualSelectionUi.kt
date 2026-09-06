package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

internal fun clocktowerPairManualSquareTableSeat(
    seat: HostSeatPresentation,
    language: String,
    state: ClocktowerSquareTableSeatState,
): ClocktowerSquareTableSeatUiModel {
    val content = hostSeatContentPresentation(seat, language)
    return ClocktowerSquareTableSeatUiModel(
        seatId = seat.seatId.renderKey(),
        seatNumber = seat.seatId.number,
        label = content.primaryLabel,
        detailLabels = content.detailLabels,
        state = state,
    )
}

@Composable
internal fun ClocktowerPairManualSelectionDialog(
    interactionKey: String,
    presentation: ClocktowerPairManualPresentation,
    seats: List<HostSeatPresentation>,
    roleLabel: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    var selection by remember(interactionKey, presentation) {
        mutableStateOf(ClocktowerPairManualSelectionModel.from(presentation))
    }
    val language = LocalContext.current.resources.configuration.locales[0].language

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    clocktowerPairManualSquareTableSeat(
                        seat = seat,
                        language = language,
                        state = clocktowerPairManualSeatState(selection, seat.seatId.number),
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = ClocktowerSquareTableInteractionMode.Selectable,
                onSeatClick = { seatKey ->
                    val seatNumber = seats
                        .firstOrNull { seat -> seat.seatId.renderKey() == seatKey }
                        ?.seatId
                        ?.number
                    if (seatNumber != null) {
                        selection = selection.selectSeat(seatNumber)
                    }
                },
            ) {
                ClocktowerPairManualCenterControls(
                    selection = selection,
                    roleLabel = roleLabel,
                    onSelectionChange = { selection = it },
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerPairManualCenterControls(
    selection: ClocktowerPairManualSelectionModel,
    roleLabel: (String) -> String,
    onSelectionChange: (ClocktowerPairManualSelectionModel) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Manual / 手动选择展示信息",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))

        when {
            selection.isZeroCaseSelected -> {
                Text(
                    text = "0 / 无此类角色",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = { onSelectionChange(selection.clearChoice()) }) {
                    Text("更改选择")
                }
            }

            selection.selectedRoleId == null -> {
                Text(
                    text = "先选择展示角色",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(2.dp))
                if (selection.roleIds.isEmpty() && !selection.hasZeroCase) {
                    Text(
                        text = "没有可用的手动信息",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(selection.roleIds, key = { it }) { roleId ->
                            OutlinedButton(
                                onClick = { onSelectionChange(selection.selectRole(roleId)) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(roleLabel(roleId), maxLines = 1)
                            }
                        }
                        if (selection.hasZeroCase) {
                            item(key = "zero-case") {
                                OutlinedButton(
                                    onClick = { onSelectionChange(selection.selectZeroCase()) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("0 / 无此类角色")
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Text(
                    text = roleLabel(selection.selectedRoleId),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = when {
                        selection.selectedFirstSeat == null -> "选择第一位玩家"
                        selection.selectedSecondSeat == null -> "选择第二位玩家"
                        else -> "已选择两位玩家"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = { onSelectionChange(selection.clearChoice()) }) {
                    Text("更改角色")
                }
            }
        }

        selection.resolvedOption?.let { resolved ->
            Button(
                onClick = { onConfirm(resolved) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("展示此手动信息")
            }
            Spacer(Modifier.height(2.dp))
        }

        TextButton(onClick = onDismiss) {
            Text("取消")
        }
    }
}

private fun clocktowerPairManualSeatState(
    selection: ClocktowerPairManualSelectionModel,
    seatNumber: Int,
): ClocktowerSquareTableSeatState {
    if (selection.isZeroCaseSelected) return ClocktowerSquareTableSeatState.Disabled

    val roleId = selection.selectedRoleId ?: return ClocktowerSquareTableSeatState.Neutral
    val first = selection.selectedFirstSeat
    val second = selection.selectedSecondSeat
    if (seatNumber == first) return ClocktowerSquareTableSeatState.SelectedFirst
    if (seatNumber == second) return ClocktowerSquareTableSeatState.SelectedSecond

    val validFirst = selection.firstSeats(roleId)
    if (first == null) {
        return if (seatNumber in validFirst) {
            ClocktowerSquareTableSeatState.Selectable
        } else {
            ClocktowerSquareTableSeatState.Disabled
        }
    }

    val validSecond = selection.secondSeats(roleId, first)
    return if (seatNumber in validSecond) {
        ClocktowerSquareTableSeatState.Selectable
    } else {
        ClocktowerSquareTableSeatState.Disabled
    }
}
