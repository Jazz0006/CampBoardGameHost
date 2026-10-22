package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.session.HostPairResolution
import com.codex.campboardgamehost.clocktower.session.HostPairRoleOption
import com.codex.campboardgamehost.clocktower.session.HostPairRoleSource

/** A suggestion is only current if the Session can actually display it for these two seats. */
internal fun clocktowerCurrentPairRecommendation(
    option: ClocktowerDisplayOption?,
    abilityRole: String,
    seatCount: Int,
    scriptRoles: List<ClocktowerRole>,
    resolvePair: (Int?, Int?) -> HostPairResolution?,
): ClocktowerDirectPairSelection? {
    if (option?.displayKind != ClocktowerDisplayKind.EitherOne) return null
    val candidate = ClocktowerDirectPairSelection.fromRecommendation(
        option, abilityRole, seatCount, scriptRoles,
    ) ?: return null
    if (candidate.noneSelected) return candidate.takeIf {
        abilityRole == "Librarian" && resolvePair(null, null)?.canShowNoOutsiders == true
    }
    val first = candidate.firstSeat ?: return null
    val second = candidate.secondSeat ?: return null
    return candidate.takeIf { chosen ->
        resolvePair(first, second)?.options?.any { it.role.value == chosen.roleId } == true
    }
}

/** Stage 4: physical seats first, one Session-scoped role shortlist second; Next records the only Observation. */
@Composable
internal fun ClocktowerPairInformationSquareTableDialog(
    interactionKey: String,
    recommendedOption: ClocktowerDisplayOption?,
    lastPreviewProposition: InformationProposition? = null,
    automaticOnly: Boolean = false,
    scriptRoles: List<ClocktowerRole>,
    abilityRoleEnName: String,
    seats: List<HostSeatPresentation>,
    actorSeat: Int? = null,
    wakeInstruction: String? = null,
    abilityLabel: String,
    roleLabel: (String) -> String,
    language: String,
    resolvePair: (Int?, Int?) -> HostPairResolution?,
    onHostTools: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
    onDraftChanged: () -> Unit,
) {
    val seatCount = seats.size
    val validRecommendation = clocktowerCurrentPairRecommendation(
        recommendedOption, abilityRoleEnName, seatCount, scriptRoles, resolvePair,
    )
    val previousPreview = ClocktowerDirectPairSelection.fromProposition(
        lastPreviewProposition, abilityRoleEnName, seatCount, scriptRoles,
    )
    var selection by remember(interactionKey, automaticOnly) {
        mutableStateOf(previousPreview ?: ClocktowerDirectPairSelection())
    }
    var editing by remember(interactionKey, automaticOnly) {
        mutableStateOf(!automaticOnly &&
            (validRecommendation == null || (previousPreview != null && previousPreview != validRecommendation)))
    }
    val activeSelection = if (!editing && validRecommendation != null) validRecommendation else selection
    fun change(next: ClocktowerDirectPairSelection) {
        if (next != selection) {
            selection = next
            onDraftChanged()
        }
    }
    fun selectSeat(seat: Int) {
        val next = selection.chooseSeat(seat, seatCount)
        val choices = resolvePair(next.firstSeat, next.secondSeat)?.options.orEmpty()
        val validRole = next.roleId?.takeIf { id -> choices.any { it.role.value == id } }
        change(next.copy(roleId = validRole, noneSelected = false))
    }
    fun startEditing() {
        selection = previousPreview ?: validRecommendation ?: ClocktowerDirectPairSelection()
        editing = true
    }
    val allCandidates = resolvePair(null, null)
    val hasPair = selection.firstSeat != null && selection.secondSeat != null &&
        selection.firstSeat != selection.secondSeat
    val options = if (hasPair) resolvePair(selection.firstSeat, selection.secondSeat)?.options.orEmpty()
        else emptyList()
    val chosenRole = if (options.size == 1) options.single().role.value
        else selection.roleId?.takeIf { id -> options.any { it.role.value == id } }
    val canShowNone = abilityRoleEnName == "Librarian" &&
        allCandidates?.canShowNoOutsiders == true
    val absent = ClocktowerDirectPairSelection(noneSelected = true).toDisplayOption(
        abilityRoleEnName, seatCount, scriptRoles, roleLabel, abilityLabel, language,
    )

    ClocktowerHostSquareTableScaffold(
        seats = seats, language = language, previousEnabled = false,
        onHostTools = onHostTools, onNext = onNext,
        interactionMode = if (editing) ClocktowerSquareTableInteractionMode.Selectable
            else ClocktowerSquareTableInteractionMode.ReadOnly,
        onSeatSelected = { if (editing) selectSeat(it) },
        onBack = {
            if (editing && validRecommendation != null) {
                selection = validRecommendation
                editing = false
            }
        },
        seatUiModel = { seat ->
            val number = seat.seatId.number
            val content = hostSeatContentPresentation(seat, language)
            val selected = number == activeSelection.firstSeat ||
                number == activeSelection.secondSeat
            ClocktowerSquareTableSeatUiModel(
                seatId = seat.seatId.renderKey(), seatNumber = number,
                label = content.primaryLabel, detailLabels = content.detailLabels,
                actualAlignment = seat.actualAlignment, isAlive = seat.isAlive,
                hasUnspentGhostVote = seat.hasUnspentGhostVote,
                isSelected = selected,
                selectionOrder = when (number) {
                    activeSelection.firstSeat -> 1
                    activeSelection.secondSeat -> 2
                    else -> null
                },
                isCandidate = editing && !selected &&
                    number in allCandidates?.ordinaryCandidateSeats.orEmpty(),
                isSpecialCandidate = editing && !selected &&
                    number in allCandidates?.specialCandidateSeats.orEmpty(),
                // No dashed border does not mean the physical seat is disabled.
                isInteractionEnabled = editing,
                isCurrentActor = number == actorSeat,
            )
        },
    ) {
        if (!editing) {
            Column(Modifier.fillMaxSize().padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                ClocktowerSingleRecommendationButtons(
                    recommendations = listOfNotNull(recommendedOption).takeIf {
                        validRecommendation != null
                    }.orEmpty(),
                    language = language,
                    labelOf = { choice ->
                        if (validRecommendation?.noneSelected == true) {
                            if (language == "en") "No Outsiders" else "没有外来者"
                        } else {
                            val role = validRecommendation?.roleId?.let(roleLabel).orEmpty()
                            val pair = listOfNotNull(
                                validRecommendation?.firstSeat, validRecommendation?.secondSeat,
                            ).joinToString(" + ") {
                                if (language == "en") "P" + it else it.toString() + "号"
                            }
                            listOf(role, pair).filter(String::isNotEmpty).joinToString(" · ")
                                .ifBlank { choice.label }
                        }
                    },
                    onConfirm = onConfirm,
                    onManualSelection = if (automaticOnly) null else ::startEditing,
                )
                if (automaticOnly && validRecommendation == null) Text(
                    if (language == "en") "Waiting for a valid current AI recommendation"
                    else "等待当前有效的 AI 推荐",
                )
            }
        } else {
            var expanded by remember(interactionKey) { mutableStateOf(false) }
            Column(Modifier.fillMaxSize().padding(6.dp)
                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                ClocktowerNightActionWakeInstruction(wakeInstruction)
                Text(abilityLabel, style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center)
                Text(when {
                    selection.firstSeat == null -> if (language == "en") "Choose first player" else "选择第一名玩家"
                    selection.secondSeat == null -> if (language == "en") "Choose second player" else "选择第二名玩家"
                    options.isEmpty() -> if (language == "en")
                        "No matching role for this pair; choose other seats"
                        else "此组合无可展示角色，请选择其他座位"
                    else -> if (language == "en") "Choose information to show" else "选择展示的信息"
                }, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

                if (hasPair && options.size > 1) {
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(chosenRole?.let(roleLabel) ?: if (language == "en")
                                "Choose character" else "选择角色", maxLines = 1)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            val groups = options.groupBy(HostPairRoleOption::source)
                            listOf(HostPairRoleSource.IN_PLAY, HostPairRoleSource.DEMON_BLUFF,
                                HostPairRoleSource.SCRIPT_FALLBACK).forEach { source ->
                                val group = groups[source].orEmpty()
                                if (group.isNotEmpty()) {
                                    if (groups.size > 1) Text(when (source) {
                                        HostPairRoleSource.IN_PLAY -> if (language == "en") "In play" else "在场角色"
                                        HostPairRoleSource.DEMON_BLUFF -> if (language == "en") "Demon bluffs" else "恶魔伪装"
                                        HostPairRoleSource.SCRIPT_FALLBACK -> if (language == "en") "Script roles" else "剧本角色"
                                    }, modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall)
                                    group.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(roleLabel(item.role.value)) },
                                            onClick = {
                                                change(selection.chooseRole(item.role.value))
                                                expanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (hasPair && chosenRole != null) {
                    val display = selection.copy(roleId = chosenRole, noneSelected = false)
                        .toDisplayOption(
                            abilityRoleEnName, seatCount, scriptRoles, roleLabel,
                            abilityLabel, language,
                        )
                    val name = roleLabel(chosenRole)
                    Button(onClick = { display?.let(onConfirm) }, enabled = display != null,
                        modifier = Modifier.fillMaxWidth()) {
                        Text(if (language == "en")
                            "Show to player: " + name + " · P" + selection.firstSeat + " + P" + selection.secondSeat
                            else "展示给玩家：" + selection.firstSeat + "号与" + selection.secondSeat + "号中有一位是" + name,
                            maxLines = 2)
                    }
                }
                if (canShowNone && absent != null) {
                    Button(onClick = { onConfirm(absent) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (language == "en") "Show to player: No Outsiders"
                            else "展示给玩家：没有外来者")
                    }
                }
            }
        }
    }
}


