package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

internal fun clocktowerFortuneTellerSeatState(
    seatNumber: Int,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
): ClocktowerSquareTableSeatState = when {
    selectedSeats.getOrNull(0) == seatNumber -> ClocktowerSquareTableSeatState.SelectedFirst
    selectedSeats.getOrNull(1) == seatNumber -> ClocktowerSquareTableSeatState.SelectedSecond
    seatNumber in selectableSeats -> ClocktowerSquareTableSeatState.Selectable
    else -> ClocktowerSquareTableSeatState.Disabled
}

internal fun clocktowerFortuneTellerResultActions(
    legalResults: Set<Boolean>,
    recommendedResult: Boolean?,
): List<Boolean> {
    if (legalResults.isEmpty()) return emptyList()

    return buildList {
        recommendedResult
            ?.takeIf { it in legalResults }
            ?.let(::add)
        legalResults
            .asSequence()
            .filterNot { it == recommendedResult }
            .sortedDescending()
            .forEach(::add)
    }
}

@Composable
internal fun ClocktowerFortuneTellerSquareTableDialog(
    seats: List<HostSeatPresentation>,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    legalResults: Set<Boolean>,
    recommendedResult: Boolean?,
    automaticStorytellerInfo: Boolean,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onResultSelected: (Boolean) -> Unit,
    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
            if (canGoPrevious) onPrevious()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = clocktowerFortuneTellerSeatState(
                            seatNumber = seat.seatId.number,
                            selectedSeats = selectedSeats,
                            selectableSeats = if (enabled) selectableSeats else emptySet(),
                        ),
                        isCurrentActor = seat.seatId.number == actorSeat,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = if (enabled) {
                    ClocktowerSquareTableInteractionMode.Selectable
                } else {
                    ClocktowerSquareTableInteractionMode.ReadOnly
                },
                onSeatClick = { renderKey ->
                    seats.firstOrNull { seat -> seat.seatId.renderKey() == renderKey }
                        ?.seatId
                        ?.number
                        ?.let(onSeatSelected)
                },
            ) {
                ClocktowerFortuneTellerCenterControls(
                    wakeInstruction = wakeInstruction,
                    selectedSeats = selectedSeats,
                    legalResults = legalResults,
                    recommendedResult = recommendedResult,
                    automaticStorytellerInfo = automaticStorytellerInfo,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onResultSelected = onResultSelected,
                    onAutomaticResultSelected = onAutomaticResultSelected,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerFortuneTellerCenterControls(
    wakeInstruction: String?,
    selectedSeats: List<Int>,
    legalResults: Set<Boolean>,
    recommendedResult: Boolean?,
    automaticStorytellerInfo: Boolean,
    language: String,
    canGoPrevious: Boolean,
    onResultSelected: (Boolean) -> Unit,
    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val actions = clocktowerFortuneTellerResultActions(
        legalResults = legalResults,
        recommendedResult = recommendedResult,
    )
    val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ClocktowerNightActionWakeInstruction(wakeInstruction)
        Text(
            text = if (language == "en") "Fortune Teller" else "占卜师",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))

        if (!completePair) {
            Text(
                text = if (language == "en") "Select two players" else "选择两名玩家",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            if (selectedSeats.size == 1) {
                Text(
                    text = if (language == "en") {
                        "First: P${selectedSeats.first()}"
                    } else {
                        "第一名：P${selectedSeats.first()}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Text(
                text = if (language == "en") {
                    "Selected: P${selectedSeats[0]} + P${selectedSeats[1]}"
                } else {
                    "已选择：P${selectedSeats[0]} + P${selectedSeats[1]}"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))

            when {
                actions.isEmpty() -> {
                    Text(
                        text = if (language == "en") "No legal result is available." else "当前没有可用的合法结果。",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }

                automaticStorytellerInfo -> {
                    val automaticResult = recommendedResult
                        ?.takeIf { it in legalResults }
                        ?: actions.first()
                    Text(
                        text = if (actions.size == 1) {
                            if (language == "en") "Result determined" else "结果已确定"
                        } else {
                            if (language == "en") "Automatic recommendation" else "自动推荐"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { onAutomaticResultSelected(automaticResult) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(clocktowerFortuneTellerResultLabel(automaticResult, language))
                    }
                }

                actions.size == 1 -> {
                    Text(
                        text = if (language == "en") "Result determined" else "结果已确定",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { onResultSelected(actions.single()) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(clocktowerFortuneTellerResultLabel(actions.single(), language))
                    }
                }

                else -> {
                    Text(
                        text = if (language == "en") "Storyteller choice" else "说书人可裁定",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    actions.forEachIndexed { index, value ->
                        Text(
                            text = if (index == 0 && value == recommendedResult) {
                                if (language == "en") "Recommended" else "推荐"
                            } else {
                                if (language == "en") "Other legal result" else "另一个合法结果"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                        if (index == 0 && value == recommendedResult) {
                            Button(
                                onClick = { onResultSelected(value) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(clocktowerFortuneTellerResultLabel(value, language))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onResultSelected(value) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(clocktowerFortuneTellerResultLabel(value, language))
                            }
                        }
                        if (index != actions.lastIndex) Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (language == "en") "Finish / Next" else "完成 / 下一步")
        }

        if (canGoPrevious) {
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onPrevious) {
                Text(if (language == "en") "Previous step" else "上一步")
            }
        }
    }
}

private fun clocktowerFortuneTellerResultLabel(
    value: Boolean,
    language: String,
): String = when {
    language == "en" && value -> "YES"
    language == "en" -> "NO"
    value -> "有"
    else -> "没有"
}
