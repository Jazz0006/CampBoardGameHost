package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codex.campboardgamehost.clocktower.domain.Alignment as ClocktowerAlignment
import com.codex.campboardgamehost.clocktower.domain.PlayerState
import com.codex.campboardgamehost.clocktower.epistemic.InformationProposition
import com.codex.campboardgamehost.clocktower.epistemic.NumericMetric
import com.codex.campboardgamehost.clocktower.session.StructuredNumberInformationUiModel

internal enum class ClocktowerEmpathResultSourceKind {
    DisplayOption,
    Structured,
    Direct,
}

internal data class ClocktowerEmpathResultChoice(
    val key: String,
    val value: Int,
    val sourceKind: ClocktowerEmpathResultSourceKind,
    val displayOption: ClocktowerDisplayOption? = null,
    val structuredCandidateId: String? = null,
    val recommended: Boolean = false,
    val scopeSeats: Set<Int> = emptySet(),
    val contributingSeats: Set<Int> = emptySet(),
)

internal data class ClocktowerEmpathSeatVisual(
    val state: ClocktowerSquareTableSeatState,
    val badge: String?,
    val isCurrentActor: Boolean,
)

/**
 * Empath scope is domain-owned. In particular, dead players may be skipped when the materializer
 * chooses the two living neighbours, so the UI must consume typed subjectSeats rather than infer
 * adjacency from square-table geometry.
 */
internal fun clocktowerEmpathScopeSeats(
    proposition: InformationProposition?,
    seatCount: Int,
): Set<Int> {
    val numeric = proposition as? InformationProposition.NumericResult ?: return emptySet()
    if (numeric.metric != NumericMetric.LIVING_EVIL_NEIGHBOURS) return emptySet()
    if (numeric.subjectSeats.isEmpty()) return emptySet()
    if (numeric.subjectSeats.any { it !in 1..seatCount }) return emptySet()
    if (numeric.subjectSeats.distinct().size != numeric.subjectSeats.size) return emptySet()
    return numeric.subjectSeats.toCollection(linkedSetOf())
}

internal fun clocktowerEmpathActualEvilSeats(players: List<PlayerState>): Set<Int> = players
    .filter { player -> player.actualAlignment == ClocktowerAlignment.EVIL }
    .mapTo(linkedSetOf()) { player -> player.seat }

internal fun clocktowerEmpathRecluseSeat(players: List<PlayerState>): Int? = players
    .firstOrNull { player -> player.actualRole.value == "Recluse" }
    ?.seat

internal fun clocktowerEmpathEffectiveEvilSeats(
    players: List<PlayerState>,
    option: ClocktowerDisplayOption?,
): Set<Int> {
    val effective = clocktowerEmpathActualEvilSeats(players).toMutableSet()
    players.firstOrNull { it.actualRole.value == "Spy" }?.let { spy ->
        when (option?.spyRegistersGood) {
            true -> effective.remove(spy.seat)
            false -> effective.add(spy.seat)
            null -> Unit
        }
    }
    players.firstOrNull { it.actualRole.value == "Recluse" }?.let { recluse ->
        when (option?.recluseRegistersEvil) {
            true -> effective.add(recluse.seat)
            false -> effective.remove(recluse.seat)
            null -> Unit
        }
    }
    return effective
}

/**
 * Projection of an already-selected typed registration witness. A mismatch between the numeric
 * result and that witness fails closed so the UI never invents which neighbour caused a result.
 */
internal fun clocktowerEmpathContributingSeats(
    players: List<PlayerState>,
    option: ClocktowerDisplayOption?,
    value: Int,
): Set<Int> {
    val scopeSeats = clocktowerEmpathScopeSeats(option?.proposition, players.size)
    if (scopeSeats.isEmpty()) return emptySet()
    val contributing = clocktowerEmpathEffectiveEvilSeats(players, option)
        .filterTo(linkedSetOf()) { it in scopeSeats }
    return if (contributing.size == value) contributing else emptySet()
}

internal fun clocktowerEmpathSeatVisual(
    seatNumber: Int,
    actorSeat: Int?,
    scopeSeats: Set<Int>,
    actualEvilSeats: Set<Int>,
    recluseSeat: Int?,
    contributingSeats: Set<Int>,
    language: String,
): ClocktowerEmpathSeatVisual {
    val inScope = seatNumber in scopeSeats
    val isRecluse = seatNumber == recluseSeat
    val badge = when {
        inScope && isRecluse -> if (language == "en") "N·R" else "邻·隐"
        inScope -> if (language == "en") "N" else "邻"
        isRecluse -> if (language == "en") "R" else "隐"
        else -> null
    }
    return ClocktowerEmpathSeatVisual(
        state = when {
            seatNumber in contributingSeats -> ClocktowerSquareTableSeatState.SelectedHighlighted
            seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation
            else -> ClocktowerSquareTableSeatState.Neutral
        },
        badge = badge,
        isCurrentActor = seatNumber == actorSeat,
    )
}

internal fun clocktowerEmpathResultChoices(
    step: ClocktowerNightStepUi,
    players: List<PlayerState>,
    automaticStorytellerInfo: Boolean,
    automaticDisplayOption: ClocktowerDisplayOption?,
    resultFirstRegistrationCandidates: List<ClocktowerDisplayOption>,
    structuredNumberUiModel: StructuredNumberInformationUiModel?,
): List<ClocktowerEmpathResultChoice> {
    fun numeric(option: ClocktowerDisplayOption): InformationProposition.NumericResult? =
        (option.proposition as? InformationProposition.NumericResult)
            ?.takeIf { it.metric == NumericMetric.LIVING_EVIL_NEIGHBOURS }

    fun choiceFor(option: ClocktowerDisplayOption, recommended: Boolean): ClocktowerEmpathResultChoice? {
        val proposition = numeric(option) ?: return null
        val scope = clocktowerEmpathScopeSeats(proposition, players.size)
        if (scope.isEmpty()) return null
        return ClocktowerEmpathResultChoice(
            key = clocktowerInformationCandidateId(option),
            value = proposition.value,
            sourceKind = ClocktowerEmpathResultSourceKind.DisplayOption,
            displayOption = option,
            recommended = recommended,
            scopeSeats = scope,
            contributingSeats = clocktowerEmpathContributingSeats(players, option, proposition.value),
        )
    }

    if (automaticStorytellerInfo) {
        return listOfNotNull(automaticDisplayOption?.let { choiceFor(it, recommended = true) })
    }

    if (resultFirstRegistrationCandidates.isNotEmpty()) {
        return resultFirstRegistrationCandidates.mapNotNull { option ->
            choiceFor(option, recommended = option.isDefaultRecommendation)
        }
    }

    structuredNumberUiModel?.let { model ->
        val scope = sequenceOf(
            step.displayProposition,
            step.legacyInformationCandidates.firstOrNull()?.proposition,
            step.displayOptions.firstOrNull()?.proposition,
        ).map { proposition -> clocktowerEmpathScopeSeats(proposition, players.size) }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
        if (scope.isEmpty()) return emptyList()
        return model.choices.map { choice ->
            ClocktowerEmpathResultChoice(
                key = choice.candidateId,
                value = choice.value,
                sourceKind = ClocktowerEmpathResultSourceKind.Structured,
                structuredCandidateId = choice.candidateId,
                recommended = choice.recommended,
                scopeSeats = scope,
                // An unreliable Empath may legally receive an arbitrary result. No truthful
                // Spy/Recluse seat witness exists for that arbitrary number, so do not fabricate one.
                contributingSeats = emptySet(),
            )
        }
    }

    val direct = (step.displayProposition as? InformationProposition.NumericResult)
        ?.takeIf { it.metric == NumericMetric.LIVING_EVIL_NEIGHBOURS }
        ?: return emptyList()
    val scope = clocktowerEmpathScopeSeats(direct, players.size)
    if (scope.isEmpty()) return emptyList()
    val directOption = ClocktowerDisplayOption(
        label = direct.value.toString(),
        displayKind = ClocktowerDisplayKind.Number,
        displayTitle = step.title,
        displayPrimary = direct.value.toString(),
        displaySecondary = null,
        displayFooter = null,
        proposition = direct,
        isTruthful = true,
    )
    return listOf(
        ClocktowerEmpathResultChoice(
            key = "direct|${direct.value}|${scope.joinToString(",")}",
            value = direct.value,
            sourceKind = ClocktowerEmpathResultSourceKind.Direct,
            recommended = true,
            scopeSeats = scope,
            contributingSeats = clocktowerEmpathContributingSeats(players, directOption, direct.value),
        ),
    )
}

internal fun clocktowerEmpathDisplayedContributionSeats(
    choices: List<ClocktowerEmpathResultChoice>,
    selectedKey: String,
): Set<Int> {
    if (choices.size <= 1) return emptySet()
    return choices.firstOrNull { it.key == selectedKey }?.contributingSeats.orEmpty()
}

@Composable
internal fun ClocktowerEmpathSquareTableDialog(
    seats: List<HostSeatPresentation>,
    actorSeat: Int?,
    wakeInstruction: String?,
    actualEvilSeats: Set<Int>,
    recluseSeat: Int?,
    choices: List<ClocktowerEmpathResultChoice>,
    language: String,
    canGoPrevious: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onConfirm: (ClocktowerEmpathResultChoice) -> Unit,
) {
    if (choices.isEmpty()) return
    val initialChoice = choices.firstOrNull { it.recommended } ?: choices.first()
    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice.key) }
    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice
    val displayedContributionSeats = clocktowerEmpathDisplayedContributionSeats(choices, selectedChoice.key)

    Dialog(
        onDismissRequest = { if (canGoPrevious) onPrevious() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ClocktowerSquareTableSeatSurface(
                seats = seats.map { seat ->
                    val content = hostSeatContentPresentation(seat, language)
                    val visual = clocktowerEmpathSeatVisual(
                        seatNumber = seat.seatId.number,
                        actorSeat = actorSeat,
                        scopeSeats = selectedChoice.scopeSeats,
                        actualEvilSeats = actualEvilSeats,
                        recluseSeat = recluseSeat,
                        contributingSeats = displayedContributionSeats,
                        language = language,
                    )
                    ClocktowerSquareTableSeatUiModel(
                        seatId = seat.seatId.renderKey(),
                        seatNumber = seat.seatId.number,
                        label = content.primaryLabel,
                        detailLabels = content.detailLabels,
                        state = visual.state,
                        isCurrentActor = visual.isCurrentActor,
                        badge = visual.badge,
                    )
                },
                modifier = Modifier.fillMaxSize(),
                interactionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
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
                            text = if (language == "en") "Empath" else "共情者",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (choices.size > 1) {
                                if (language == "en") {
                                    "N neighbour scope · ★ evil · R Recluse · ✓★ counted in selected result"
                                } else {
                                    "邻 能力范围 · ★ 邪恶提示 · 隐 隐士 · ✓★ 当前结果计入"
                                }
                            } else {
                                if (language == "en") {
                                    "N neighbour scope · ★ evil · R Recluse"
                                } else {
                                    "邻 能力范围 · ★ 邪恶提示 · 隐 隐士"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            choices.chunked(3).forEach { rowChoices ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    rowChoices.forEach { choice ->
                                        if (choice.key == selectedChoice.key) {
                                            Button(
                                                onClick = { selectedKey = choice.key },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(choice.value.toString(), maxLines = 1)
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { selectedKey = choice.key },
                                                modifier = Modifier.weight(1f),
                                            ) {
                                                Text(choice.value.toString(), maxLines = 1)
                                            }
                                        }
                                    }
                                    repeat(3 - rowChoices.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        Button(
                            onClick = { onConfirm(selectedChoice) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (language == "en") {
                                    "Show information: ${selectedChoice.value}"
                                } else {
                                    "展示信息：${selectedChoice.value}"
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
    }
}
