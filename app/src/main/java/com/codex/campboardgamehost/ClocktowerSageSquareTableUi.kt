package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

internal enum class ClocktowerSageResultSourceKind {
    DisplayOption,
    LegacyUnreliable,
    Direct,
}

internal data class ClocktowerSageResultChoice(
    val key: String,
    val subjectSeats: List<Int>,
    val displayPrimary: String,
    val selectionLabel: String,
    val sourceKind: ClocktowerSageResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val recommended: Boolean = false,
)

private fun validSageSubjectSeats(
    seats: List<Int>,
    seatCount: Int,
): List<Int>? = seats
    .takeIf { it.size == 2 && it.distinct().size == 2 && it.all { seat -> seat in 1..seatCount } }
    ?.sorted()

/**
 * Projects already-created Sage information choices into typed spatial presentation. The projector
 * never parses displaySecondary. In particular, unreliable options may carry typed seat identity
 * while deliberately keeping proposition == null.
 */
internal fun clocktowerSageResultChoices(
    step: ClocktowerNightStepUi,
    seatCount: Int,
    automaticStorytellerInfo: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
): List<ClocktowerSageResultChoice> {
    if (step.roleEnName != "Sage" || step.actor == null || !step.isRealAction || seatCount <= 0) return emptyList()

    fun fromOptions(
        options: List<ClocktowerDisplayOption>,
        sourceKind: ClocktowerSageResultSourceKind,
    ): List<ClocktowerSageResultChoice> {
        if (options.isEmpty()) return emptyList()
        return options.map { option ->
            if (option.displayKind != ClocktowerDisplayKind.EitherOne) return emptyList()
            val seats = validSageSubjectSeats(option.presentationSubjectSeats, seatCount) ?: return emptyList()
            val primary = option.displayPrimary?.takeIf { it.isNotBlank() } ?: return emptyList()
            ClocktowerSageResultChoice(
                key = "${clocktowerInformationCandidateId(option)}|presentation:${seats.joinToString(":")}",
                subjectSeats = seats,
                displayPrimary = primary,
                selectionLabel = option.label.takeIf { it.isNotBlank() }
                    ?: seats.joinToString(" + ") { seat -> "P$seat" },
                sourceKind = sourceKind,
                displayOption = option,
                recommended = option.isDefaultRecommendation,
            )
        }.distinctBy { it.key }
    }

    if (automaticStorytellerInfo) {
        automaticDisplayOption?.let { option ->
            return fromOptions(
                options = listOf(option),
                sourceKind = ClocktowerSageResultSourceKind.DisplayOption,
            )
        }
    } else if (step.displayOptions.isNotEmpty()) {
        return fromOptions(
            options = step.displayOptions,
            sourceKind = ClocktowerSageResultSourceKind.LegacyUnreliable,
        )
    } else if (step.recommendedDisplayOptions.isNotEmpty()) {
        return fromOptions(
            options = step.recommendedDisplayOptions,
            sourceKind = ClocktowerSageResultSourceKind.DisplayOption,
        )
    }

    if (step.displayKind != ClocktowerDisplayKind.EitherOne) return emptyList()
    val seats = validSageSubjectSeats(step.presentationSubjectSeats, seatCount) ?: return emptyList()
    val primary = step.displayPrimary?.takeIf { it.isNotBlank() } ?: return emptyList()
    return listOf(
        ClocktowerSageResultChoice(
            key = "direct|${seats.joinToString(":")}|$primary",
            subjectSeats = seats,
            displayPrimary = primary,
            selectionLabel = seats.joinToString(" + ") { seat -> "P$seat" },
            sourceKind = ClocktowerSageResultSourceKind.Direct,
            recommended = true,
        ),
    )
}

internal fun clocktowerSageSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
    subjectSeats: List<Int>,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = when (seatNumber) {
        subjectSeats.getOrNull(0) -> ClocktowerSquareTableSeatState.SelectedFirst
        subjectSeats.getOrNull(1) -> ClocktowerSquareTableSeatState.SelectedSecond
        else -> ClocktowerSquareTableSeatState.Neutral
    },
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerSageSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    choices: List<ClocktowerSageResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerSageResultChoice) -> Unit,
) {
    if (choices.isEmpty()) return
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.first()
    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice.key) }
    var menuExpanded by remember(choices.map { it.key }) { mutableStateOf(false) }
    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice

    ClocktowerNightActionSquareTableDialog(
        seats = seats,
        enabled = false,
        language = language,
        seatPresentation = { seatNumber ->
            clocktowerSageSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
                subjectSeats = selectedChoice.subjectSeats,
            )
        },
        onSeatSelected = {},
        canGoPrevious = canGoPrevious,
        onPrevious = onPrevious,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                Text(
                    text = if (language == "en") "Sage" else "贤者",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (language == "en") {
                        "One highlighted player is the Demon"
                    } else {
                        "高亮的两名玩家中有一名是恶魔"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))

                if (choices.size > 1) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(selectedChoice.selectionLabel, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            choices.forEach { choice ->
                                DropdownMenuItem(
                                    text = { Text(choice.selectionLabel) },
                                    onClick = {
                                        selectedKey = choice.key
                                        menuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Text(
                    text = selectedChoice.subjectSeats.joinToString(" + ") { seat -> "P$seat" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { onConfirm(selectedChoice) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (language == "en") "Show this information" else "展示此信息")
                }
            }

            ClocktowerSquareTableStepNavigation(
                language = language,
                canGoPrevious = canGoPrevious,
                onPrevious = onPrevious,
                onNext = onNext,
            )
        }
    }
}
