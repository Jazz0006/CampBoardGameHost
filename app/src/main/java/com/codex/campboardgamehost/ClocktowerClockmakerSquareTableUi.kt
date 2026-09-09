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

internal enum class ClocktowerClockmakerResultSourceKind {
    DisplayOption,
    LegacyUnreliable,
    Direct,
}

internal data class ClocktowerClockmakerResultChoice(
    val key: String,
    val displayValue: String,
    val selectionLabel: String,
    val sourceKind: ClocktowerClockmakerResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val recommended: Boolean = false,
)

/**
 * Clockmaker's current option domain remains authoritative. This presentation adapter deliberately
 * treats the shown number as opaque display content instead of parsing it back into semantic state.
 */
internal fun clocktowerClockmakerResultChoices(
    step: ClocktowerNightStepUi,
    automaticStorytellerInfo: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
): List<ClocktowerClockmakerResultChoice> {
    if (step.roleEnName != "Clockmaker" || step.actor == null || !step.isRealAction) return emptyList()

    fun fromOptions(
        options: List<ClocktowerDisplayOption>,
        sourceKind: ClocktowerClockmakerResultSourceKind,
    ): List<ClocktowerClockmakerResultChoice> {
        if (options.isEmpty()) return emptyList()
        return options.map { option ->
            val value = option.displayPrimary?.takeIf { it.isNotBlank() } ?: return emptyList()
            ClocktowerClockmakerResultChoice(
                key = clocktowerInformationCandidateId(option),
                displayValue = value,
                selectionLabel = option.label.takeIf { it.isNotBlank() } ?: value,
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
                sourceKind = ClocktowerClockmakerResultSourceKind.DisplayOption,
            )
        }
    } else if (step.displayOptions.isNotEmpty()) {
        return fromOptions(
            options = step.displayOptions,
            sourceKind = ClocktowerClockmakerResultSourceKind.LegacyUnreliable,
        )
    }

    if (step.displayKind != ClocktowerDisplayKind.Number) return emptyList()
    val value = step.displayPrimary?.takeIf { it.isNotBlank() } ?: return emptyList()
    return listOf(
        ClocktowerClockmakerResultChoice(
            key = "direct|$value",
            displayValue = value,
            selectionLabel = value,
            sourceKind = ClocktowerClockmakerResultSourceKind.Direct,
            recommended = true,
        ),
    )
}

internal fun clocktowerClockmakerSeatPresentation(
    seatNumber: Int,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = ClocktowerSquareTableSeatState.Neutral,
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
internal fun ClocktowerClockmakerSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    choices: List<ClocktowerClockmakerResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerClockmakerResultChoice) -> Unit,
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
            clocktowerClockmakerSeatPresentation(
                seatNumber = seatNumber,
                actorSeat = actorSeat,
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
                    text = if (language == "en") "Clockmaker" else "钟表匠",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (language == "en") {
                        "Show the distance from the Demon to the nearest Minion"
                    } else {
                        "展示恶魔到最近爪牙的距离"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))

                if (choices.size > 1) {
                    Text(
                        text = if (language == "en") "Choose the number to show" else "选择要展示的数字",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
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

                Button(
                    onClick = { onConfirm(selectedChoice) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (language == "en") {
                            "Show information: ${selectedChoice.displayValue}"
                        } else {
                            "展示信息：${selectedChoice.displayValue}"
                        },
                        maxLines = 1,
                    )
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
