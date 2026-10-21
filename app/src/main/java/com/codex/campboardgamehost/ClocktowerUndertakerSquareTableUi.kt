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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerUndertakerSeatVisual(
    val state: ClocktowerSquareTableSeatState,
    val badge: String?,
    val isCurrentActor: Boolean,
)

internal fun clocktowerUndertakerSeatVisual(
    seatNumber: Int,
    actorSeat: Int?,
    executedSeat: Int,
    language: String,
): ClocktowerUndertakerSeatVisual = ClocktowerUndertakerSeatVisual(
    state = if (seatNumber == executedSeat) ClocktowerSquareTableSeatState.HighlightedInformation
        else ClocktowerSquareTableSeatState.Neutral,
    badge = if (seatNumber == executedSeat) {
        if (language == "en") "EX" else "处"
    } else null,
    isCurrentActor = seatNumber == actorSeat,
)

internal fun clocktowerUndertakerPlayerDisplayStep(
    displayStep: ClocktowerNightStepUi,
    choice: ClocktowerDirectRoleRevealChoice,
    cards: List<PlayerCard>,
    language: String,
): ClocktowerNightStepUi {
    val executedName = cards.getOrNull(choice.targetSeat - 1)?.name.orEmpty()
    val executedLabel = if (language == "en") {
        "P${choice.targetSeat} $executedName".trim()
    } else {
        "${choice.targetSeat}号 $executedName".trim()
    }
    val lead = if (language == "en") {
        "Yesterday's executed player, $executedLabel, was"
    } else {
        "昨天被处决的 $executedLabel，身份是"
    }
    return displayStep.copy(
        displayKind = ClocktowerDisplayKind.RoleReveal,
        displayTitle = lead,
        displayPrimary = choice.displayLabel,
        displaySecondary = null,
        displayFooter = null,
        displayProposition = InformationProposition.RoleAt(choice.targetSeat, choice.roleId),
    )
}

/** Render one confirmed execution target and direct manual role input or a single typed AI choice. */
@Composable
internal fun ClocktowerUndertakerSquareTableDialog(
    interactionKey: String,
    seats: List<HostSeatPresentation>,
    step: ClocktowerNightStepUi,
    actorSeat: Int?,
    wakeInstruction: String?,
    scriptRoles: List<ClocktowerRole>,
    recommendedOption: ClocktowerDisplayOption?,
    lastPreviewRoleId: String?,
    automaticOnly: Boolean = false,
    roleLabel: (String) -> String,
    language: String,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDirectRoleRevealChoice) -> Unit,
    onDraftChanged: () -> Unit,
) {
    val executedSeat = (step.informationQuestion as? ClocktowerInformationQuestion.Role)?.subjectSeat
        ?.takeIf { it in 1..seats.size } ?: return
    val advised = clocktowerDirectRecommendedRoleReveal(
        step, executedSeat, seats.size, scriptRoles, recommendedOption,
    )
    var editing by remember(interactionKey, executedSeat, automaticOnly) {
        mutableStateOf(!automaticOnly && (advised == null ||
            (lastPreviewRoleId != null && lastPreviewRoleId != advised.roleId.value)))
    }
    var selectedRole by remember(interactionKey, executedSeat, automaticOnly) {
        mutableStateOf(lastPreviewRoleId ?: advised?.roleId?.value)
    }
    fun changeRole(roleId: String) {
        if (selectedRole != roleId) {
            selectedRole = roleId
            onDraftChanged()
        }
    }
    var roleMenuExpanded by remember(executedSeat) { mutableStateOf(false) }
    ClocktowerHostSquareTableScaffold(
        seats = seats,
        language = language,
        previousEnabled = false,

        onHostTools = onHostTools,
        onNext = onNext,
        interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
        seatUiModel = { seat ->
            val content = hostSeatContentPresentation(seat, language)
            val visual = clocktowerUndertakerSeatVisual(
                seat.seatId.number, actorSeat, executedSeat, language,
            )
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(), seatNumber = seat.seatId.number,
                label = content.primaryLabel, detailLabels = content.detailLabels,
                actualAlignment = seat.actualAlignment,
                isAlive = seat.isAlive, hasUnspentGhostVote = seat.hasUnspentGhostVote,
                state = visual.state, isCurrentActor = visual.isCurrentActor, badge = visual.badge,
            )
        },
    ) {
        Column(Modifier.fillMaxSize().padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                if (!editing && advised != null) {
                    ClocktowerSingleRecommendationButtons(
                        recommendations = listOf(advised),
                        language = language,
                        labelOf = { choice ->
                            if (language == "en") "Show: ${choice.displayLabel}" else "展示：${choice.displayLabel}"
                        },
                        onConfirm = onConfirm,
                        onManualSelection = if (automaticOnly) null else ({
                            selectedRole = lastPreviewRoleId ?: advised.roleId.value
                            editing = true
                        }),
                    )
                } else if (automaticOnly) {
                    Text(
                        if (language == "en") "Waiting for a current AI recommendation" else "等待 AI 确定当前信息",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { roleMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedRole?.let(roleLabel) ?:
                                if (language == "en") "Choose character" else "选择角色", maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false },
                        ) {
                            scriptRoles.distinctBy { it.enName }.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(roleLabel(role.enName)) },
                                    onClick = { changeRole(role.enName); roleMenuExpanded = false },
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    val actual = selectedRole?.let {
                        clocktowerDirectRoleRevealChoice(
                            step, executedSeat, seats.size, it, scriptRoles, roleLabel,
                        )
                    }
                    Button(
                        onClick = { actual?.let(onConfirm) },
                        enabled = actual != null, modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (language == "en") "Show to player" else "展示给玩家") }
                    if (advised != null) {
                        TextButton(onClick = {
                            changeRole(advised.roleId.value)
                            editing = false
                        }) { Text(if (language == "en") "Use recommendation" else "恢复推荐") }
                    }
                }
            }
        }
    }
}

