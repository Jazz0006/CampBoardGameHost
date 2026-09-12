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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Square-table composition for first-night pair information (Washerwoman/Librarian/Investigator).
 *
 * Recommendation ranking and Manual legality stay outside this file. This surface owns only the
 * transition from a read-only recommended preview into in-place Manual editing.
 */
@Composable
internal fun ClocktowerPairInformationSquareTableDialog(
    interactionKey: String,
    presentation: ClocktowerPairManualPresentation,
    recommendedOption: ClocktowerDisplayOption?,
    seats: List<HostSeatPresentation>,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    abilityLabel: String,
    roleLabel: (String) -> String,
    allowManualEditing: Boolean,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    val canonicalRecommendedOption = remember(interactionKey, presentation, recommendedOption) {
        ClocktowerPairManualAuthority.canonicalManualOption(presentation, recommendedOption)
    }
    val recommendedSelection = remember(interactionKey, presentation, canonicalRecommendedOption) {
        ClocktowerPairManualSelectionModel.from(presentation, canonicalRecommendedOption)
    }
    var selection by remember(interactionKey, presentation, canonicalRecommendedOption) {
        mutableStateOf(recommendedSelection)
    }
    var editing by remember(interactionKey, presentation, canonicalRecommendedOption, allowManualEditing) {
        mutableStateOf(allowManualEditing && recommendedSelection.resolvedOption == null)
    }

    fun restoreRecommendation() {
        selection = recommendedSelection
        editing = allowManualEditing && recommendedSelection.resolvedOption == null
    }

    Dialog(
        onDismissRequest = {
            if (editing && recommendedSelection.resolvedOption != null) {
                restoreRecommendation()
            } else if (canGoPrevious) {
                onPrevious()
            }
        },
        properties = DialogProperties(
                         usePlatformDefaultWidth = false,
                         decorFitsSystemWindows = false,
                     ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ClocktowerSquareTableSeatSurface(
                    seats = seats.map { seat ->
                        val seatPresentation = clocktowerPairInformationSeatPresentation(
                            selection = selection,
                            seatNumber = seat.seatId.number,
                            editing = editing,
                            actorSeat = actorSeat,
                        )
                        clocktowerPairManualSquareTableSeat(
                            seat = seat,
                            language = language,
                            state = seatPresentation.targetState,
                        ).copy(isCurrentActor = seatPresentation.isCurrentActor)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    interactionMode = if (editing) {
                        ClocktowerSquareTableInteractionMode.Selectable
                    } else {
                        ClocktowerSquareTableInteractionMode.ReadOnly
                    },
                    onSeatClick = { seatKey ->
                        if (editing) {
                            seats.firstOrNull { seat -> seat.seatId.renderKey() == seatKey }
                                ?.seatId
                                ?.number
                                ?.let { seatNumber -> selection = selection.selectSeat(seatNumber) }
                        }
                    },
                ) {
                    ClocktowerPairInformationCenterControls(
                        wakeInstruction = wakeInstruction,
                        abilityLabel = abilityLabel,
                        selection = selection,
                        recommendedSelection = recommendedSelection,
                        editing = editing,
                        roleLabel = roleLabel,
                        allowManualEditing = allowManualEditing,
                        language = language,
                        onSelectionChange = { selection = it },
                        onStartEditing = { if (allowManualEditing) editing = true },
                        onRestoreRecommendation = ::restoreRecommendation,
                        onConfirm = onConfirm,
                    )
                }

                ClocktowerNightBottomActionBar(
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onPrevious = onPrevious,
                    onHostTools = onHostTools,
                    onNext = onNext,
                )
            }
        }
    }
}

/**
 * Recommended mode is read-only: only the recommended pair is accented. Manual mode delegates
 * selectable/selected/disabled projection to the same authority used by the existing pair Manual
 * picker, so legal second-seat continuations remain visually authoritative.
 */
internal fun clocktowerPairInformationSeatState(
    selection: ClocktowerPairManualSelectionModel,
    seatNumber: Int,
    editing: Boolean,
): ClocktowerSquareTableSeatState {
    if (!editing) {
        return when (seatNumber) {
            selection.selectedFirstSeat -> ClocktowerSquareTableSeatState.SelectedFirst
            selection.selectedSecondSeat -> ClocktowerSquareTableSeatState.SelectedSecond
            else -> ClocktowerSquareTableSeatState.Neutral
        }
    }
    return clocktowerPairManualSeatState(selection, seatNumber)
}

/**
 * The player who must be woken is a separate visual dimension from the players referenced by the
 * information. The square-table card can therefore keep the actor border while also retaining a
 * pair-selection state when those concepts happen to overlap.
 */
internal fun clocktowerPairInformationSeatPresentation(
    selection: ClocktowerPairManualSelectionModel,
    seatNumber: Int,
    editing: Boolean,
    actorSeat: Int?,
): ClocktowerNightActionSeatPresentation = ClocktowerNightActionSeatPresentation(
    targetState = clocktowerPairInformationSeatState(
        selection = selection,
        seatNumber = seatNumber,
        editing = editing,
    ),
    isCurrentActor = seatNumber == actorSeat,
)

@Composable
private fun ClocktowerPairInformationCenterControls(
    wakeInstruction: String?,
    abilityLabel: String,
    selection: ClocktowerPairManualSelectionModel,
    recommendedSelection: ClocktowerPairManualSelectionModel,
    editing: Boolean,
    roleLabel: (String) -> String,
    allowManualEditing: Boolean,
    language: String,
    onSelectionChange: (ClocktowerPairManualSelectionModel) -> Unit,
    onStartEditing: () -> Unit,
    onRestoreRecommendation: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val hasRecommendation = recommendedSelection.resolvedOption != null

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
                text = abilityLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (editing) {
                    if (language == "en") "Manual edit" else "手动编辑"
                } else {
                    if (language == "en") "Recommended information" else "推荐信息"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))

            if (editing) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { roleMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            when {
                                selection.isZeroCaseSelected -> if (language == "en") "0 / None in play" else "0 / 无此类角色"
                                selection.selectedRoleId != null -> roleLabel(selection.selectedRoleId)
                                else -> if (language == "en") "Choose character" else "选择身份"
                            },
                            maxLines = 1,
                        )
                    }
                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false },
                    ) {
                        selection.roleIds.forEach { roleId ->
                            DropdownMenuItem(
                                text = { Text(roleLabel(roleId)) },
                                onClick = {
                                    onSelectionChange(selection.selectRole(roleId))
                                    roleMenuExpanded = false
                                },
                            )
                        }
                        if (selection.hasZeroCase) {
                            DropdownMenuItem(
                                text = { Text(if (language == "en") "0 / None in play" else "0 / 无此类角色") },
                                onClick = {
                                    onSelectionChange(selection.selectZeroCase())
                                    roleMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            when {
                selection.isZeroCaseSelected -> {
                    Text(
                        text = if (language == "en") "No character of this type is in play" else "没有此类角色在场",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }

                selection.selectedRoleId != null -> {
                    val first = selection.selectedFirstSeat
                    val second = selection.selectedSecondSeat
                    if (first != null && second != null) {
                        Text(
                            text = "P$first + P$second",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = if (language == "en") {
                                "One of these players is ${roleLabel(selection.selectedRoleId)}"
                            } else {
                                "其中一人是 ${roleLabel(selection.selectedRoleId)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    } else if (editing) {
                        Text(
                            text = if (language == "en") "Select two players on the table" else "请在桌上选择两名玩家",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                editing -> {
                    Text(
                        text = if (language == "en") "Choose a character, then select two players" else "先选择身份，再在桌上选择两名玩家",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            selection.resolvedOption?.let { resolved ->
                Button(
                    onClick = { onConfirm(resolved) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (language == "en") "Show this information" else "展示此信息")
                }
            }

            if (allowManualEditing && !editing && (selection.roleIds.isNotEmpty() || selection.hasZeroCase)) {
                OutlinedButton(
                    onClick = onStartEditing,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (language == "en") "Choose manually" else "手动选择")
                }
            } else if (allowManualEditing && editing && hasRecommendation) {
                TextButton(onClick = onRestoreRecommendation) {
                    Text(if (language == "en") "Restore recommendation" else "恢复推荐")
                }
            }
        }
    }
}
