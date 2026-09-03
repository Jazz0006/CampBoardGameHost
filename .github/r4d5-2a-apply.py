from pathlib import Path


def replace_once(path: str, old: str, new: str) -> None:
    file = Path(path)
    text = file.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one match in {path}, found {count}: {old[:80]!r}")
    file.write_text(text.replace(old, new, 1))


# 1) Typed Host-table multi-selection mode.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableContract.kt",
    """internal enum class HostTableInteractionMode {\n    ReadOnly,\n    Selection,\n    OrderedSelection,\n    Sequential,\n}\n""",
    """internal enum class HostTableInteractionMode {\n    ReadOnly,\n    Selection,\n    OrderedSelection,\n    MultiSelection,\n    Sequential,\n}\n""",
)

# 2) Shared HostTableShell: optional seat badge + multi-select chrome mapping.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt",
    """    neutralSelectionChrome: Boolean = false,\n    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },\n""",
    """    neutralSelectionChrome: Boolean = false,\n    seatBadge: (HostSeatPresentation) -> String? = { null },\n    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt",
    """            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n            )\n""",
    """            frame.toSquareTableSeatUiModel(\n                motionKey = seatMotionKey(frame.seat),\n                neutralSelectionChrome = neutralSelectionChrome,\n                interactionMode = interaction.mode,\n                badge = seatBadge(frame.seat),\n            )\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt",
    """private fun HostTableSeatFrame.toSquareTableSeatUiModel(\n    motionKey: String,\n    neutralSelectionChrome: Boolean,\n): ClocktowerSquareTableSeatUiModel =\n""",
    """private fun HostTableSeatFrame.toSquareTableSeatUiModel(\n    motionKey: String,\n    neutralSelectionChrome: Boolean,\n    interactionMode: HostTableInteractionMode,\n    badge: String?,\n): ClocktowerSquareTableSeatUiModel =\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerHostTableUi.kt",
    """        state = if (neutralSelectionChrome) {\n            ClocktowerSquareTableSeatState.Neutral\n        } else {\n            squareTableSeatState()\n        },\n        isInteractionEnabled = isSelectable && !isLocked,\n        motionKey = motionKey,\n    )\n\nprivate fun HostTableSeatFrame.squareTableSeatState(): ClocktowerSquareTableSeatState = when {\n    isLocked -> ClocktowerSquareTableSeatState.Disabled\n    selectionOrder == 1 -> ClocktowerSquareTableSeatState.SelectedFirst\n    selectionOrder == 2 -> ClocktowerSquareTableSeatState.SelectedSecond\n    isSelected -> ClocktowerSquareTableSeatState.SelectedFirst\n    isCurrent -> ClocktowerSquareTableSeatState.HighlightedInformation\n    isHighlighted -> ClocktowerSquareTableSeatState.HighlightedInformation\n    isSelectable -> ClocktowerSquareTableSeatState.Selectable\n    else -> ClocktowerSquareTableSeatState.Neutral\n}\n""",
    """        state = if (neutralSelectionChrome) {\n            ClocktowerSquareTableSeatState.Neutral\n        } else {\n            squareTableSeatState(interactionMode)\n        },\n        isInteractionEnabled = isSelectable && !isLocked,\n        motionKey = motionKey,\n        badge = badge,\n    )\n\nprivate fun HostTableSeatFrame.squareTableSeatState(\n    interactionMode: HostTableInteractionMode,\n): ClocktowerSquareTableSeatState = when {\n    isLocked -> ClocktowerSquareTableSeatState.Disabled\n    interactionMode == HostTableInteractionMode.MultiSelection && isSelected && isHighlighted ->\n        ClocktowerSquareTableSeatState.SelectedHighlighted\n    interactionMode == HostTableInteractionMode.MultiSelection && isSelected ->\n        ClocktowerSquareTableSeatState.Selected\n    interactionMode == HostTableInteractionMode.MultiSelection && isHighlighted ->\n        ClocktowerSquareTableSeatState.HighlightedInformation\n    selectionOrder == 1 -> ClocktowerSquareTableSeatState.SelectedFirst\n    selectionOrder == 2 -> ClocktowerSquareTableSeatState.SelectedSecond\n    isSelected -> ClocktowerSquareTableSeatState.SelectedFirst\n    isCurrent -> ClocktowerSquareTableSeatState.HighlightedInformation\n    isHighlighted -> ClocktowerSquareTableSeatState.HighlightedInformation\n    isSelectable -> ClocktowerSquareTableSeatState.Selectable\n    else -> ClocktowerSquareTableSeatState.Neutral\n}\n""",
)

# 3) Generic square-table visual states for arbitrary checked seats.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    """    SelectedFirst,\n    SelectedSecond,\n    HighlightedInformation,\n""",
    """    SelectedFirst,\n    SelectedSecond,\n    Selected,\n    SelectedHighlighted,\n    HighlightedInformation,\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    """        ClocktowerSquareTableSeatState.SelectedFirst,\n        ClocktowerSquareTableSeatState.SelectedSecond,\n    ),\n    val motionKey: String = seatId,\n)\n""",
    """        ClocktowerSquareTableSeatState.SelectedFirst,\n        ClocktowerSquareTableSeatState.SelectedSecond,\n        ClocktowerSquareTableSeatState.Selected,\n        ClocktowerSquareTableSeatState.SelectedHighlighted,\n    ),\n    val motionKey: String = seatId,\n    val badge: String? = null,\n)\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    """                Text(\n                    text = clocktowerSeatNumberLabel(seat.seatNumber, language),\n                    fontSize = 15.sp,\n                    lineHeight = 16.sp,\n                    fontWeight = FontWeight.Black,\n                )\n            }\n""",
    """                Text(\n                    text = clocktowerSeatNumberLabel(seat.seatNumber, language),\n                    fontSize = 15.sp,\n                    lineHeight = 16.sp,\n                    fontWeight = FontWeight.Black,\n                )\n                seat.badge?.let { badge ->\n                    Text(\n                        text = badge,\n                        fontSize = 10.sp,\n                        lineHeight = 12.sp,\n                        fontWeight = FontWeight.Black,\n                        modifier = Modifier.padding(start = 2.dp),\n                    )\n                }\n            }\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    """                        ClocktowerSquareTableSeatState.SelectedFirst,\n                        ClocktowerSquareTableSeatState.SelectedSecond,\n                        ClocktowerSquareTableSeatState.HighlightedInformation,\n""",
    """                        ClocktowerSquareTableSeatState.SelectedFirst,\n                        ClocktowerSquareTableSeatState.SelectedSecond,\n                        ClocktowerSquareTableSeatState.Selected,\n                        ClocktowerSquareTableSeatState.SelectedHighlighted,\n                        ClocktowerSquareTableSeatState.HighlightedInformation,\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    """        ClocktowerSquareTableSeatState.SelectedSecond -> ClocktowerSquareTableSeatPalette(\n            container = colors.secondaryContainer,\n            content = colors.onSecondaryContainer,\n            border = colors.secondary,\n            borderWidth = 3.dp,\n        )\n        ClocktowerSquareTableSeatState.HighlightedInformation -> ClocktowerSquareTableSeatPalette(\n""",
    """        ClocktowerSquareTableSeatState.SelectedSecond -> ClocktowerSquareTableSeatPalette(\n            container = colors.secondaryContainer,\n            content = colors.onSecondaryContainer,\n            border = colors.secondary,\n            borderWidth = 3.dp,\n        )\n        ClocktowerSquareTableSeatState.Selected -> ClocktowerSquareTableSeatPalette(\n            container = colors.primaryContainer,\n            content = colors.onPrimaryContainer,\n            border = colors.primary,\n            borderWidth = 3.dp,\n        )\n        ClocktowerSquareTableSeatState.SelectedHighlighted -> ClocktowerSquareTableSeatPalette(\n            container = colors.tertiaryContainer,\n            content = colors.onTertiaryContainer,\n            border = colors.primary,\n            borderWidth = 3.5.dp,\n        )\n        ClocktowerSquareTableSeatState.HighlightedInformation -> ClocktowerSquareTableSeatPalette(\n""",
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    """    ClocktowerSquareTableSeatState.SelectedFirst -> \"①\"\n    ClocktowerSquareTableSeatState.SelectedSecond -> \"②\"\n    ClocktowerSquareTableSeatState.HighlightedInformation -> \"★\"\n""",
    """    ClocktowerSquareTableSeatState.SelectedFirst -> \"①\"\n    ClocktowerSquareTableSeatState.SelectedSecond -> \"②\"\n    ClocktowerSquareTableSeatState.Selected -> \"✓\"\n    ClocktowerSquareTableSeatState.SelectedHighlighted -> \"✓★\"\n    ClocktowerSquareTableSeatState.HighlightedInformation -> \"★\"\n""",
)

# 4) Pure pending voter authority.
Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerTableVoteState.kt").write_text(r'''package com.codex.campboardgamehost

internal data class ClocktowerTableVoteState(
    val seats: List<HostSeatPresentation>,
    val nomineeSeatId: ClocktowerSeatId,
    val orderedSeatIds: List<ClocktowerSeatId>,
    val selectableSeatIds: Set<ClocktowerSeatId>,
    val selectedVoterSeatIds: Set<ClocktowerSeatId>,
    val interaction: HostTableInteractionState,
) {
    val voteCount: Int
        get() = selectedVoterSeatIds.size

    fun togglePendingVoter(seatId: ClocktowerSeatId): ClocktowerTableVoteState {
        if (seatId !in selectableSeatIds) return this
        val nextSelected = if (seatId in selectedVoterSeatIds) {
            selectedVoterSeatIds - seatId
        } else {
            selectedVoterSeatIds + seatId
        }
        return clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = nomineeSeatId,
            selectedVoterSeatIds = nextSelected,
        )
    }
}

internal fun clocktowerTableVoteState(
    seats: List<HostSeatPresentation>,
    nomineeSeatId: ClocktowerSeatId,
    selectedVoterSeatIds: Set<ClocktowerSeatId> = emptySet(),
): ClocktowerTableVoteState {
    require(seats.isNotEmpty()) { "Table vote requires at least one physical seat" }
    val canonicalSeats = seats.sortedBy { seat -> seat.seatId.number }
    val expectedSeatIds = (1..canonicalSeats.size).map(::ClocktowerSeatId)
    require(canonicalSeats.map(HostSeatPresentation::seatId) == expectedSeatIds) {
        "Table vote requires unique contiguous physical seats from seat 1"
    }
    require(nomineeSeatId in expectedSeatIds) {
        "Table vote nominee must belong to the physical table"
    }

    val nomineeIndex = expectedSeatIds.indexOf(nomineeSeatId)
    val orderedSeatIds = expectedSeatIds.drop(nomineeIndex + 1) +
        expectedSeatIds.take(nomineeIndex + 1)
    val selectableSeatIds = canonicalSeats
        .asSequence()
        .filter(HostSeatPresentation::isAlive)
        .map(HostSeatPresentation::seatId)
        .toSet()
    require(selectedVoterSeatIds.all { seatId -> seatId in selectableSeatIds }) {
        "Pending voter selection must contain only currently selectable seats"
    }
    val orderedSelected = orderedSeatIds.filter { seatId -> seatId in selectedVoterSeatIds }

    return ClocktowerTableVoteState(
        seats = canonicalSeats,
        nomineeSeatId = nomineeSeatId,
        orderedSeatIds = orderedSeatIds,
        selectableSeatIds = selectableSeatIds,
        selectedVoterSeatIds = selectedVoterSeatIds,
        interaction = HostTableInteractionState(
            mode = HostTableInteractionMode.MultiSelection,
            selectableSeatIds = selectableSeatIds,
            selectedSeatIds = orderedSelected,
            highlightedSeatIds = setOf(nomineeSeatId),
            lockedSeatIds = expectedSeatIds.toSet() - selectableSeatIds,
        ),
    )
}
''')

# 5) Persistent-table vote UI.
Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerVoteTableUi.kt").write_text(r'''package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** In-place Day voting workspace. Seat taps remain pending until the Storyteller confirms. */
@Composable
internal fun ClocktowerVoteTableScreen(
    round: Int,
    cards: List<PlayerCard>,
    tableState: ClocktowerDayOverviewTableState,
    executionThreshold: Int,
    nominatorName: String?,
    nomineeName: String?,
    highestVoteText: String,
    actionsEnabled: Boolean,
    onConfirm: (Set<ClocktowerSeatId>) -> Unit,
    onCancel: () -> Unit,
) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun text(zh: String, en: String): String = if (language == "en") en else zh
    val nominatorSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nominatorName }
    val nomineeSeat = tableState.seats.firstOrNull { seat -> seat.playerName == nomineeName }

    ClocktowerDarkTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            if (nomineeSeat == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text("投票目标无效，请返回提名。", "Invalid vote target; return to nomination."),
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onCancel) {
                        Text(text("返回提名", "Return to nomination"))
                    }
                }
                return@Surface
            }

            var voteState by remember(tableState.seats, nomineeSeat.seatId) {
                mutableStateOf(
                    clocktowerTableVoteState(
                        seats = tableState.seats,
                        nomineeSeatId = nomineeSeat.seatId,
                    ),
                )
            }
            val nominationLink = nominatorSeat
                ?.takeIf { seat -> seat.seatId != nomineeSeat.seatId }
                ?.let { seat -> HostTableDirectionalLink(seat.seatId, nomineeSeat.seatId) }
            val firstVoterSeat = voteState.orderedSeatIds.firstOrNull()
                ?.let { seatId -> voteState.seats.firstOrNull { seat -> seat.seatId == seatId } }

            HostTableShell(
                seats = voteState.seats,
                modifier = Modifier.fillMaxSize(),
                interaction = voteState.interaction,
                onSeatClick = { seatId -> voteState = voteState.togglePendingVoter(seatId) },
                directionalLink = nominationLink,
                seatBadge = { seat ->
                    if (seat.seatId == nomineeSeat.seatId) {
                        text("${voteState.voteCount}票", "${voteState.voteCount}v")
                    } else {
                        null
                    }
                },
                centerContent = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text(
                            text = text("第 $round 天 · 投票", "Day $round · Vote"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text(
                                "正在对 ${playerSeatLabel(cards, nomineeName)} 投票",
                                "Voting on ${playerSeatLabel(cards, nomineeName)}",
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text(
                                "${voteState.voteCount} 票 / $executionThreshold 票可处决",
                                "${voteState.voteCount} votes / $executionThreshold to execute",
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = firstVoterSeat?.let { firstSeat ->
                                text(
                                    "从 ${playerSeatLabel(cards, firstSeat.playerName)} 开始，顺时针；${playerSeatLabel(cards, nomineeName)} 最后一票。举手就点头像，再点一次取消。",
                                    "Start with ${playerSeatLabel(cards, firstSeat.playerName)}, move clockwise, and count ${playerSeatLabel(cards, nomineeName)} last. Tap raised hands; tap again to undo.",
                                )
                            } ?: text(
                                "顺时针计票，被提名人最后。",
                                "Count clockwise, with the nominee last.",
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = highestVoteText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                        )
                        Button(
                            onClick = { onConfirm(voteState.selectedVoterSeatIds) },
                            enabled = actionsEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text("确认投票", "Confirm vote"), fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text("取消投票", "Cancel vote"))
                        }
                    }
                },
            )
        }
    }
}
''')

# 6) Replace the production Stepper path with the table-voter workspace; keep current result authority.
host_path = "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
old_vote = r'''        ClocktowerVoteScreen(
            round = round,
            cards = cards,
            aliveCount = publicAliveCards.size,
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            voteCount = currentVoteCount,
            highestVoteText = highestVoteText,
            actionsEnabled = gameOutcome == null,
            onVoteCountChange = { currentVoteCount = it },
            onRecordAndContinue = {
                recordVoteEvent()
                recordCurrentVote()
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
            onRecordAndEndDay = {
                recordVoteEvent()
                onSelectExecution(recordCurrentVote())
                dayMode = ClocktowerDayMode.EndConfirm
            },
            onBack = {
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Nomination
            },
        )
'''
new_vote = r'''        ClocktowerVoteTableScreen(
            round = round,
            cards = cards,
            tableState = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
            ),
            executionThreshold = executionThreshold,
            nominatorName = nominatorName,
            nomineeName = nomineeName,
            highestVoteText = highestVoteText,
            actionsEnabled = gameOutcome == null,
            onConfirm = { selectedVoterSeatIds ->
                currentVoteCount = selectedVoterSeatIds.size
                recordVoteEvent()
                recordCurrentVote()
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
            onCancel = {
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Nomination
            },
        )
'''
replace_once(host_path, old_vote, new_vote)
