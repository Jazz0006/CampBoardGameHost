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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun ClocktowerChambermaidSquareTableDialog(
    seats: List<HostSeatPresentation>,
    selectedSeats: List<Int>,
    selectableSeats: Set<Int>,
    enabled: Boolean,
    resultOptions: List<ClocktowerDisplayOption>,
    language: String,
    canGoPrevious: Boolean,
    onSeatSelected: (Int) -> Unit,
    onShowDeterminedResult: () -> Unit,
    onResultSelected: (ClocktowerDisplayOption) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = enabled,
        language = language,
        seatState = { seatNumber ->
            clocktowerTwoTargetSeatState(
                seatNumber = seatNumber,
                selectedSeats = selectedSeats,
                selectableSeats = if (enabled) selectableSeats else emptySet(),
            )
        },
        onSeatSelected = onSeatSelected,
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
    ) {
        ClocktowerChambermaidCenterControls(
            selectedSeats = selectedSeats,
            resultOptions = resultOptions,
            language = language,
            canGoPrevious = canGoPrevious,
            onShowDeterminedResult = onShowDeterminedResult,
            onResultSelected = onResultSelected,
            onPrevious = onPrevious,
            onNext = onNext,
        )
    }
}

@Composable
private fun ClocktowerChambermaidCenterControls(
    selectedSeats: List<Int>,
    resultOptions: List<ClocktowerDisplayOption>,
    language: String,
    canGoPrevious: Boolean,
    onShowDeterminedResult: () -> Unit,
    onResultSelected: (ClocktowerDisplayOption) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2
    val orderedOptions = resultOptions.sortedBy { option -> if (option.isDefaultRecommendation) 0 else 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (language == "en") "Chambermaid" else "侍女",
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
            selectedSeats.firstOrNull()?.let { first ->
                Text(
                    text = if (language == "en") "First: P$first" else "第一名：P$first",
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
            Spacer(Modifier.height(6.dp))

            if (orderedOptions.isEmpty()) {
                Button(
                    onClick = onShowDeterminedResult,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (language == "en") "Check and show" else "查询并展示")
                }
            } else {
                Text(
                    text = if (language == "en") "Choose the result to show" else "选择最终展示结果",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                orderedOptions.forEach { option ->
                    Spacer(Modifier.height(4.dp))
                    if (option.isDefaultRecommendation) {
                        Button(
                            onClick = { onResultSelected(option) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(option.label)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onResultSelected(option) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(option.label)
                        }
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
            TextButton(onClick = onPrevious) {
                Text(if (language == "en") "Previous step" else "上一步")
            }
        }
    }
}
