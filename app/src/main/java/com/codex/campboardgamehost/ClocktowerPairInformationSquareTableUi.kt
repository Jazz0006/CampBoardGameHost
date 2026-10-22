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
    lastPreviewProposition: com.codex.campboardgamehost.clocktower.epistemic.InformationProposition? = null,
    automaticOnly: Boolean = false,
    scriptRoles: List<ClocktowerRole>,
    abilityRoleEnName: String,
    seats: List<HostSeatPresentation>,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    abilityLabel: String,
    roleLabel: (String) -> String,
    language: String,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
    onDraftChanged: () -> Unit,
) {
    val seatCount = seats.size
    val validRecommendation = remember(interactionKey, recommendedOption, scriptRoles) {
        ClocktowerDirectPairSelection.fromRecommendation(
            recommendedOption, abilityRoleEnName, seatCount, scriptRoles,
        )
    }
    // Opening the player fullscreen temporarily unmounts this square table.
    // Restore the last *previewed* pair (manual or recommended) on return.
    val lastPreviewSelection = ClocktowerDirectPairSelection.fromProposition(
        lastPreviewProposition, abilityRoleEnName, seatCount, scriptRoles,
    )
    // An asynchronous AI recommendation must not erase a Storyteller's in-progress edit.
    var selection by remember(interactionKey, scriptRoles, automaticOnly) {
        mutableStateOf(lastPreviewSelection ?: validRecommendation ?: ClocktowerDirectPairSelection())
    }
    var editing by remember(interactionKey, scriptRoles, automaticOnly) {
        mutableStateOf(!automaticOnly &&
            (validRecommendation == null || (lastPreviewSelection != null &&
                lastPreviewSelection != validRecommendation)))
    }
    // Keep the last preview highlighted until the Storyteller edits or previews again.
    val highlightedSelection = if (!editing && lastPreviewSelection == null && validRecommendation != null)
        validRecommendation else selection
    fun changeSelection(next: ClocktowerDirectPairSelection) {
        if (next != selection) {
            selection = next
            onDraftChanged()
        }
    }
    fun restoreRecommendation() {
        changeSelection(validRecommendation ?: ClocktowerDirectPairSelection())
        editing = validRecommendation == null && !automaticOnly
    }

    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = false,

        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = if (editing) ClocktowerSquareTableInteractionMode.Selectable
            else ClocktowerSquareTableInteractionMode.ReadOnly,
        onSeatSelected = { seatNumber ->
            if (editing) changeSelection(selection.chooseSeat(seatNumber, seatCount))
        },
        onBack = {
            if (editing && validRecommendation != null) restoreRecommendation()
        },
        seatUiModel = { seat ->
            val seatNumber = seat.seatId.number
            val selected = seatNumber == highlightedSelection.firstSeat ||
                seatNumber == highlightedSelection.secondSeat
            val content = hostSeatContentPresentation(seat, language)
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(),
                seatNumber = seatNumber,
                label = content.primaryLabel,
                detailLabels = content.detailLabels,
                actualAlignment = seat.actualAlignment,
                isAlive = seat.isAlive,
                hasUnspentGhostVote = seat.hasUnspentGhostVote,
                isSelected = selected,
                selectionOrder = when (seatNumber) {
                    highlightedSelection.firstSeat -> 1
                    highlightedSelection.secondSeat -> 2
                    else -> null
                },
                isCandidate = editing && selection.roleId != null && !selection.noneSelected,
                isInteractionEnabled = editing && selection.roleId != null && !selection.noneSelected,
                isCurrentActor = seatNumber == actorSeat,
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
                    onManualSelection = if (automaticOnly) null else ({
                        selection = highlightedSelection
                        editing = true
                    }),
                )
                if (automaticOnly && validRecommendation == null) {
                    Text(
                        if (language == "en") "Waiting for a current AI recommendation" else "等待 AI 确定当前信息",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
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
                                    changeSelection(selection.chooseRole(role.enName))
                                    expanded = false
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(if (language == "en") "None" else "没有") },
                            onClick = {
                                changeSelection(selection.chooseNone())
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

