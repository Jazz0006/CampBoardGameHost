package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition

internal data class ClocktowerPairManualSeatUiModel(
    val seatId: String,
    val seatNumber: Int,
    val label: String,
)

private data class ClocktowerPairManualCandidate(
    val option: ClocktowerDisplayOption,
    val roleId: String,
    val seats: List<Int>,
)

internal data class ClocktowerPairManualSelectionModel private constructor(
    private val pairCandidates: List<ClocktowerPairManualCandidate>,
    private val zeroCaseOption: ClocktowerDisplayOption?,
    val selectedRoleId: String? = null,
    val selectedFirstSeat: Int? = null,
    val selectedSecondSeat: Int? = null,
    val isZeroCaseSelected: Boolean = false,
) {
    val roleIds: List<String>
        get() = pairCandidates.map { it.roleId }.distinct()

    val hasZeroCase: Boolean
        get() = zeroCaseOption != null

    val resolvedOption: ClocktowerDisplayOption?
        get() {
            if (isZeroCaseSelected) return zeroCaseOption
            val roleId = selectedRoleId ?: return null
            val first = selectedFirstSeat ?: return null
            val second = selectedSecondSeat ?: return null
            if (first == second) return null
            val selectedSeats = listOf(first, second).sorted()
            return pairCandidates.firstOrNull { candidate ->
                candidate.roleId == roleId && candidate.seats == selectedSeats
            }?.option
        }

    fun firstSeats(roleId: String): List<Int> = pairCandidates
        .asSequence()
        .filter { it.roleId == roleId }
        .flatMap { it.seats.asSequence() }
        .distinct()
        .sorted()
        .toList()

    fun secondSeats(roleId: String, firstSeat: Int): List<Int> = pairCandidates
        .asSequence()
        .filter { candidate -> candidate.roleId == roleId && firstSeat in candidate.seats }
        .mapNotNull { candidate -> candidate.seats.firstOrNull { it != firstSeat } }
        .distinct()
        .sorted()
        .toList()

    fun selectRole(roleId: String): ClocktowerPairManualSelectionModel {
        if (roleId !in roleIds) return this
        if (roleId == selectedRoleId && !isZeroCaseSelected) return this
        return copy(
            selectedRoleId = roleId,
            selectedFirstSeat = null,
            selectedSecondSeat = null,
            isZeroCaseSelected = false,
        )
    }

    fun selectZeroCase(): ClocktowerPairManualSelectionModel {
        if (!hasZeroCase) return this
        return copy(
            selectedRoleId = null,
            selectedFirstSeat = null,
            selectedSecondSeat = null,
            isZeroCaseSelected = true,
        )
    }

    fun clearChoice(): ClocktowerPairManualSelectionModel = copy(
        selectedRoleId = null,
        selectedFirstSeat = null,
        selectedSecondSeat = null,
        isZeroCaseSelected = false,
    )

    fun selectSeat(seatNumber: Int): ClocktowerPairManualSelectionModel {
        val roleId = selectedRoleId ?: return this
        val validFirstSeats = firstSeats(roleId)
        if (seatNumber !in validFirstSeats) return this

        val first = selectedFirstSeat
        val second = selectedSecondSeat
        if (first == null) {
            return copy(
                selectedFirstSeat = seatNumber,
                selectedSecondSeat = null,
                isZeroCaseSelected = false,
            )
        }

        if (second == null) {
            if (seatNumber == first) {
                return copy(selectedFirstSeat = null, selectedSecondSeat = null)
            }
            if (seatNumber in secondSeats(roleId, first)) {
                return copy(selectedSecondSeat = seatNumber)
            }
            return copy(selectedFirstSeat = seatNumber, selectedSecondSeat = null)
        }

        if (seatNumber == second) {
            return copy(selectedSecondSeat = null)
        }
        if (seatNumber == first) {
            return copy(selectedFirstSeat = second, selectedSecondSeat = null)
        }
        if (seatNumber in secondSeats(roleId, first)) {
            return copy(selectedSecondSeat = seatNumber)
        }

        val retainedSecond = second.takeIf { existingSecond ->
            existingSecond != seatNumber && existingSecond in secondSeats(roleId, seatNumber)
        }
        return copy(
            selectedFirstSeat = seatNumber,
            selectedSecondSeat = retainedSecond,
        )
    }

    companion object {
        internal fun from(
            options: List<ClocktowerDisplayOption>,
        ): ClocktowerPairManualSelectionModel {
            val pairs = mutableListOf<ClocktowerPairManualCandidate>()
            var zeroCase: ClocktowerDisplayOption? = null

            options.forEach { option ->
                when (val proposition = option.proposition) {
                    is InformationProposition.AnyOf -> {
                        val roleAt = proposition.alternatives
                            .mapNotNull { it as? InformationProposition.RoleAt }
                        if (roleAt.size != proposition.alternatives.size) return@forEach
                        val roleId = roleAt.map { it.role.value }.distinct().singleOrNull()
                            ?: return@forEach
                        val seats = roleAt.map { it.seat }.distinct().sorted()
                        if (seats.size != 2) return@forEach
                        pairs += ClocktowerPairManualCandidate(
                            option = option,
                            roleId = roleId,
                            seats = seats,
                        )
                    }

                    is InformationProposition.AllOf -> {
                        val roleInPlay = proposition.propositions
                            .mapNotNull { it as? InformationProposition.RoleInPlay }
                        if (
                            zeroCase == null &&
                            roleInPlay.size == proposition.propositions.size &&
                            roleInPlay.isNotEmpty() &&
                            roleInPlay.all { !it.inPlay }
                        ) {
                            zeroCase = option
                        }
                    }

                    else -> Unit
                }
            }

            return ClocktowerPairManualSelectionModel(
                pairCandidates = pairs,
                zeroCaseOption = zeroCase,
            )
        }
    }
}

internal fun clocktowerPairManualSelectionModel(
    candidates: List<ClocktowerDisplayOption>,
): ClocktowerPairManualSelectionModel = ClocktowerPairManualSelectionModel.from(candidates)

@Composable
internal fun ClocktowerPairManualSelectionDialog(
    interactionKey: String,
    candidates: List<ClocktowerDisplayOption>,
    seats: List<ClocktowerPairManualSeatUiModel>,
    roleLabel: (String) -> String,
    onDismiss: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    var selection by remember(interactionKey, candidates) {
        mutableStateOf(clocktowerPairManualSelectionModel(candidates))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId,
                        seatNumber = seat.seatNumber,
                        label = seat.label,
                        state = clocktowerPairManualSeatState(selection, seat.seatNumber),
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = ClocktowerSquareTableInteractionMode.Selectable,
                onSeatClick = { seatId ->
                    val seatNumber = seats.firstOrNull { it.seatId == seatId }?.seatNumber
                    if (seatNumber != null) {
                        selection = selection.selectSeat(seatNumber)
                    }
                },
            ) {
                ClocktowerPairManualCenterControls(
                    selection = selection,
                    roleLabel = roleLabel,
                    onSelectionChange = { selection = it },
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun ClocktowerPairManualCenterControls(
    selection: ClocktowerPairManualSelectionModel,
    roleLabel: (String) -> String,
    onSelectionChange: (ClocktowerPairManualSelectionModel) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ClocktowerDisplayOption) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Manual / 手动选择展示信息",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))

        when {
            selection.isZeroCaseSelected -> {
                Text(
                    text = "0 / 无此类角色",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = { onSelectionChange(selection.clearChoice()) }) {
                    Text("更改选择")
                }
            }

            selection.selectedRoleId == null -> {
                Text(
                    text = "先选择展示角色",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(2.dp))
                if (selection.roleIds.isEmpty() && !selection.hasZeroCase) {
                    Text(
                        text = "没有可用的手动信息",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(selection.roleIds, key = { it }) { roleId ->
                            OutlinedButton(
                                onClick = { onSelectionChange(selection.selectRole(roleId)) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(roleLabel(roleId), maxLines = 1)
                            }
                        }
                        if (selection.hasZeroCase) {
                            item(key = "zero-case") {
                                OutlinedButton(
                                    onClick = { onSelectionChange(selection.selectZeroCase()) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("0 / 无此类角色")
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Text(
                    text = roleLabel(selection.selectedRoleId),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = when {
                        selection.selectedFirstSeat == null -> "选择第一位玩家"
                        selection.selectedSecondSeat == null -> "选择第二位玩家"
                        else -> "已选择两位玩家"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                TextButton(onClick = { onSelectionChange(selection.clearChoice()) }) {
                    Text("更改角色")
                }
            }
        }

        selection.resolvedOption?.let { resolved ->
            Button(
                onClick = { onConfirm(resolved) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("展示此手动信息")
            }
            Spacer(Modifier.height(2.dp))
        }

        TextButton(onClick = onDismiss) {
            Text("取消")
        }
    }
}

private fun clocktowerPairManualSeatState(
    selection: ClocktowerPairManualSelectionModel,
    seatNumber: Int,
): ClocktowerSquareTableSeatState {
    if (selection.isZeroCaseSelected) return ClocktowerSquareTableSeatState.Disabled

    val roleId = selection.selectedRoleId ?: return ClocktowerSquareTableSeatState.Neutral
    val first = selection.selectedFirstSeat
    val second = selection.selectedSecondSeat
    if (seatNumber == first) return ClocktowerSquareTableSeatState.SelectedFirst
    if (seatNumber == second) return ClocktowerSquareTableSeatState.SelectedSecond

    val validFirst = selection.firstSeats(roleId)
    if (first == null) {
        return if (seatNumber in validFirst) {
            ClocktowerSquareTableSeatState.Selectable
        } else {
            ClocktowerSquareTableSeatState.Disabled
        }
    }

    val validSecond = selection.secondSeats(roleId, first)
    return if (seatNumber in validSecond) {
        ClocktowerSquareTableSeatState.Selectable
    } else {
        ClocktowerSquareTableSeatState.Disabled
    }
}
