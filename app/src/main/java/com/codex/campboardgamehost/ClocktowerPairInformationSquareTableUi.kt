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

/** First-night pair: one AI proposal or direct human role + two-seat input, never a Host option grid. */
@Composable
internal fun ClocktowerPairInformationSquareTableDialog(
    interactionKey: String,
    recommendedOption: ClocktowerDisplayOption?,
    scriptRoles: List<ClocktowerRole>,
    abilityRoleEnName: String,
    seats: List<HostSeatPresentation>,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    abilityLabel: String,
    roleLabel: (String) -> String,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    val seatCount = seats.size
    val validRecommendation = remember(interactionKey, recommendedOption, scriptRoles) {
        ClocktowerDirectPairSelection.fromRecommendation(
            recommendedOption, abilityRoleEnName, seatCount, scriptRoles,
        )
    }
    var selection by remember(interactionKey, recommendedOption, scriptRoles) {
        mutableStateOf(validRecommendation ?: ClocktowerDirectPairSelection())
    }
    var editing by remember(interactionKey, recommendedOption, scriptRoles) {
        mutableStateOf(validRecommendation == null)
    }
    fun restoreRecommendation() {
        selection = validRecommendation ?: ClocktowerDirectPairSelection()
        editing = validRecommendation == null
    }

    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = canGoPrevious,
        onPrevious = onPrevious,
        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = if (editing) ClocktowerSquareTableInteractionMode.Selectable
            else ClocktowerSquareTableInteractionMode.ReadOnly,
        onSeatSelected = { seatNumber ->
            if (editing) selection = selection.chooseSeat(seatNumber, seatCount)
        },
        onBack = {
            if (editing && validRecommendation != null) restoreRecommendation()
            else if (canGoPrevious) onPrevious()
        },
        seatUiModel = { seat ->
            val seatNumber = seat.seatId.number
            val selected = seatNumber == selection.firstSeat || seatNumber == selection.secondSeat
            val state = when {
                selected -> ClocktowerSquareTableSeatState.SelectedHighlighted
                editing && selection.roleId != null && !selection.noneSelected ->
                    ClocktowerSquareTableSeatState.Selectable
                else -> ClocktowerSquareTableSeatState.Neutral
            }
            val content = hostSeatContentPresentation(seat, language)
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(),
                seatNumber = seatNumber,
                label = content.primaryLabel,
                detailLabels = content.detailLabels,
                isAlive = seat.isAlive,
                hasUnspentGhostVote = seat.hasUnspentGhostVote,
                state = state,
            ).copy(
                isCurrentActor = seatNumber == actorSeat,
                suppressDefaultStateMarker = selected,
                stateMarkerOverride = clocktowerPairInformationTruthMarker(
                    selected, selection.roleId, seat.actualRole?.roleId,
                ),
            )
        },
    ) {
        if (!editing) {
            Column(
                modifier = Modifier.fillMaxSize().padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                ClocktowerSingleRecommendationButtons(
                    recommendations = listOfNotNull(recommendedOption).takeIf {
                        validRecommendation != null
                    }.orEmpty(),
                    language = language,
                    labelOf = { choice ->
                        val pair = validRecommendation
                        if (pair?.noneSelected == true) {
                            if (language == "en") "None in play" else "场上没有对应角色"
                        } else {
                            val role = pair?.roleId?.let(roleLabel).orEmpty()
                            val selectedSeats = listOfNotNull(pair?.firstSeat, pair?.secondSeat)
                                .joinToString(" + ") { seatNumber ->
                                    if (language == "en") "P$seatNumber" else "${seatNumber}号"
                                }
                            listOf(role, selectedSeats).filter { it.isNotBlank() }
                                .joinToString(" · ").ifBlank { choice.label }
                        }
                    },
                    onConfirm = onConfirm,
                    onManualSelection = { editing = true },
                )
            }
        } else {
            var expanded by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.fillMaxSize().padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                Text(abilityLabel, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(selection.roleId?.let(roleLabel) ?: if (selection.noneSelected) {
                            if (language == "en") "None in play" else "没有对应角色"
                        } else {
                            if (language == "en") "Choose character" else "选择角色"
                        }, maxLines = 1)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        scriptRoles.distinctBy { it.enName }.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(roleLabel(role.enName)) },
                                onClick = {
                                    selection = selection.chooseRole(role.enName)
                                    expanded = false
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (language == "en") "None" else "没有") },
                            onClick = {
                                selection = selection.chooseNone()
                                expanded = false
                            },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    when {
                        selection.noneSelected -> if (language == "en") "No matching character in play" else "场上没有对应角色"
                        selection.roleId == null -> if (language == "en") "Choose character" else "选择角色"
                        selection.firstSeat == null -> if (language == "en") "Choose first player" else "选择第一名玩家"
                        selection.secondSeat == null -> if (language == "en") "Choose second player" else "选择第二名玩家"
                        else -> if (language == "en") "Ready to show" else "可以展示"
                    },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                val option = selection.toDisplayOption(
                    abilityRoleEnName, seatCount, scriptRoles, roleLabel, abilityLabel, language,
                )
                Button(
                    onClick = { option?.let(onConfirm) },
                    enabled = option != null,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (language == "en") "Show to player" else "展示给玩家") }
                if (validRecommendation != null) {
                    TextButton(onClick = ::restoreRecommendation) {
                        Text(if (language == "en") "Use recommendation" else "恢复推荐")
                    }
                }
            }
        }
    }
}

/** Host-private truth marker is not a legality filter and never changes the actual selected fact. */
internal fun clocktowerPairInformationTruthMarker(
    isSelectedCandidate: Boolean,
    selectedRoleId: String?,
    actualRoleId: String?,
): String? = if (isSelectedCandidate && selectedRoleId != null && actualRoleId == selectedRoleId) "✓" else null

