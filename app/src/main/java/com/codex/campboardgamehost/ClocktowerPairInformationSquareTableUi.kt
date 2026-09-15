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
    val beginnerMode = !allowManualEditing
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

    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = if (editing) {
            ClocktowerSquareTableInteractionMode.Selectable
        } else {
            ClocktowerSquareTableInteractionMode.ReadOnly
        },
        onSeatSelected = { seatNumber ->
            if (editing) selection = selection.selectSeat(seatNumber)
        },
        onBack = {
            if (editing && recommendedSelection.resolvedOption != null) {
                restoreRecommendation()
            } else if (canGoPrevious) {
                onPrevious()
            }
        },
        seatUiModel = { seat ->
            val seatPresentation = if (beginnerMode) {
                clocktowerBeginnerPairSeatPresentation(
                    seatNumber = seat.seatId.number,
                    actorSeat = actorSeat,
                )
            } else {
                clocktowerPairInformationSeatPresentation(
                    selection = selection,
                    seatNumber = seat.seatId.number,
                    editing = editing,
                    actorSeat = actorSeat,
                )
            }
            val isSelectedCandidate = !beginnerMode &&
                (seat.seatId.number == selection.selectedFirstSeat ||
                    seat.seatId.number == selection.selectedSecondSeat)
            clocktowerPairManualSquareTableSeat(
                seat = seat,
                language = language,
                state = seatPresentation.targetState,
            ).copy(
                isCurrentActor = seatPresentation.isCurrentActor,
                suppressDefaultStateMarker = isSelectedCandidate,
                stateMarkerOverride = clocktowerPairInformationTruthMarker(
                    isSelectedCandidate = isSelectedCandidate,
                    selectedRoleId = selection.selectedRoleId,
                    actualRoleId = seat.actualRole?.roleId,
                ),
            )
        },
    ) {
        ClocktowerPairInformationCenterControls(
            wakeInstruction = wakeInstruction,
            beginnerMode = beginnerMode,
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
}

/**
 * Host-private truth cue for Experienced pair information. Reliability is deliberately absent from
 * this projection: the check means only that this selected candidate's authoritative actual role
 * equals the role being shown. A poisoned/unreliable result can therefore still carry a check when
 * it happens to be truthful, while a false candidate never receives one.
 */
internal fun clocktowerPairInformationTruthMarker(
    isSelectedCandidate: Boolean,
    selectedRoleId: String?,
    actualRoleId: String?,
): String? = if (
    isSelectedCandidate &&
    selectedRoleId != null &&
    actualRoleId == selectedRoleId
) {
    "✓"
} else {
    null
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
        return if (
            seatNumber == selection.selectedFirstSeat ||
            seatNumber == selection.selectedSecondSeat
        ) {
            ClocktowerSquareTableSeatState.SelectedHighlighted
        } else {
            ClocktowerSquareTableSeatState.Neutral
        }
    }
    return when (val state = clocktowerPairManualSeatState(selection, seatNumber)) {
        ClocktowerSquareTableSeatState.SelectedFirst,
        ClocktowerSquareTableSeatState.SelectedSecond -> ClocktowerSquareTableSeatState.SelectedHighlighted
        else -> state
    }
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
    beginnerMode: Boolean,
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
    if (beginnerMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            ClocktowerNightActionWakeInstruction(wakeInstruction)
            OutlinedButton(
                onClick = { selection.resolvedOption?.let(onConfirm) },
                enabled = selection.resolvedOption != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Show to player" else "展示给玩家")
            }
        }
        return
    }

    var roleMenuExpanded by remember { mutableStateOf(false) }
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
            Spacer(Modifier.height(4.dp))

            if (editing) {
                if (selection.isZeroCaseSelected) {
                    Text(
                        text = if (language == "en") "No matching character in play" else "场上没有对应角色",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = when {
                            selection.selectedRoleId == null -> if (language == "en") "Choose a character" else "选择角色"
                            selection.selectedFirstSeat == null -> if (language == "en") "Choose the first player" else "选择第一名玩家"
                            selection.selectedSecondSeat == null -> if (language == "en") "Choose the second player" else "选择第二名玩家"
                            else -> if (language == "en") "Ready to show" else "可以展示"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { roleMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                selection.selectedRoleId?.let(roleLabel)
                                    ?: if (language == "en") "Choose character" else "选择角色",
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
                                    text = { Text(if (language == "en") "None" else "没有") },
                                    onClick = {
                                        onSelectionChange(selection.selectZeroCase())
                                        roleMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            } else {
                val roleId = selection.selectedRoleId
                if (roleId != null) {
                    Text(
                        text = roleLabel(roleId),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            val resolved = selection.resolvedOption
            Button(
                onClick = { resolved?.let(onConfirm) },
                enabled = resolved != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (language == "en") "Show to player" else "展示给玩家")
            }
            if (allowManualEditing) {
                Spacer(Modifier.height(4.dp))
                if (editing) {
                    if (recommendedSelection.resolvedOption != null) {
                        TextButton(onClick = onRestoreRecommendation) {
                            Text(if (language == "en") "Use recommendation" else "恢复推荐")
                        }
                    }
                } else {
                    TextButton(onClick = onStartEditing) {
                        Text(if (language == "en") "Change" else "修改")
                    }
                }
            }
        }
    }
}
