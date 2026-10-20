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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Structural seat selection only; no Sage rules or demon-location candidate engine. */
internal fun clocktowerSageToggledPair(
    selectedSeats: List<Int>,
    tappedSeat: Int,
    seatCount: Int,
): List<Int> {
    if (tappedSeat !in 1..seatCount) return selectedSeats
    return when {
        tappedSeat in selectedSeats -> selectedSeats - tappedSeat
        selectedSeats.size >= 2 -> listOf(tappedSeat)
        else -> selectedSeats + tappedSeat
    }
}

internal fun clocktowerSageValidPair(seats: List<Int>, seatCount: Int): Boolean =
    seats.size == 2 && seats.distinct().size == 2 && seats.all { it in 1..seatCount }

internal fun clocktowerSageSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
    subjectSeats: List<Int>,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = when (seatNumber) {
        subjectSeats.getOrNull(0) -> ClocktowerSquareTableSeatState.SelectedFirst
        subjectSeats.getOrNull(1) -> ClocktowerSquareTableSeatState.SelectedSecond
        else -> ClocktowerSquareTableSeatState.Selectable
    },
    isCurrentActor = seatNumber == actorSeat,
)

/**
 * AI advice is a preselection. A human can instead tap any two actual seats;
 * neither path needs an exhaustive list of supposedly legal Sage pairs.
 */
@Composable
internal fun ClocktowerSageSquareTableDialog(
    interactionKey: String,
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    recommendedSeats: List<Int>?,
    lastPreviewSeats: List<Int>?,
    automaticOnly: Boolean = false,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirmRecommended: () -> Unit,
    onConfirmManual: (List<Int>) -> Unit,
    onDraftChanged: () -> Unit,
) {
    val hasRecommendation = recommendedSeats?.let { clocktowerSageValidPair(it, seats.size) } == true
    var manualSelection by remember(interactionKey, actorSeat, seats.size, automaticOnly) {
        mutableStateOf(!automaticOnly && (!hasRecommendation ||
            (lastPreviewSeats != null && lastPreviewSeats != recommendedSeats)))
    }
    var manualSeats by remember(interactionKey, actorSeat, seats.size, automaticOnly) {
        mutableStateOf(lastPreviewSeats ?: recommendedSeats.orEmpty())
    }
    val shownSeats = if (manualSelection) manualSeats else lastPreviewSeats ?: recommendedSeats.orEmpty()
    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = manualSelection,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSageSeatPresentation(seatNumber, actorSeat, shownSeats)
        },
        onSeatSelected = { seat ->
            if (manualSelection) {
                val next = clocktowerSageToggledPair(manualSeats, seat, seats.size)
                if (next != manualSeats) {
                    manualSeats = next
                    onDraftChanged()
                }
            }
        },
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            Text(
                text = if (language == "en") "Sage · Choose two seats" else "贤者 · 选择两名玩家",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (language == "en") "Tap two seats to show to the Sage" else "轻点方桌上两个座位，向贤者展示",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = shownSeats.joinToString(" + ") { "P$it" }.ifEmpty {
                    if (language == "en") "No seats selected" else "尚未选择座位"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            if (automaticOnly && !hasRecommendation) {
                Text(
                    if (language == "en") "Waiting for a current AI suggestion" else "等待 AI 确定当前信息",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (manualSelection) {
                Button(
                    enabled = clocktowerSageValidPair(manualSeats, seats.size),
                    onClick = { onConfirmManual(manualSeats.sorted()) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (language == "en") "Show this pair" else "展示这两名玩家") }
                if (hasRecommendation) {
                    OutlinedButton(onClick = {
                        manualSeats = recommendedSeats.orEmpty()
                        onDraftChanged()
                        manualSelection = false
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (language == "en") "Return to AI suggestion" else "返回 AI 推荐")
                    }
                }
            } else {
                Button(onClick = onConfirmRecommended, modifier = Modifier.fillMaxWidth()) {
                    Text(if (language == "en") "Show AI-selected pair" else "向玩家展示推荐信息")
                }
                if (!automaticOnly) {
                    OutlinedButton(onClick = {
                        manualSeats = shownSeats
                        manualSelection = true
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (language == "en") "Choose seats manually" else "手动选择座位")
                    }
                }
            }
        }
    }
}

