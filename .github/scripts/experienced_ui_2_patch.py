from pathlib import Path
import subprocess

EXPECTED_BLOBS = {
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt": "1ceabd372bcfe8c233df673f529a96ee97a24d16",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt": "79f8c05670fa3fcb04fd8ed3daa568c6edd0e44f",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt": "bc2c98e019eee75a44abf0b7a9032cc157d3fc45",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt": "282d9797f650097c44bac3632a58f1667f5440d3",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt": "c85086f74af81f9323b8a059c8e7cf42973d1b25",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt": "ddc75f7d617c026645dc42cc35891c547f273778",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTableUi.kt": "b711f6854a9ac3d2ca663c0f141ac46535121e9c",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt": "57e00c638a65bc6653e02d780d534f7fced22b28",
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt": "30c288adc18ef265b7ac2058c5f4514078b06112",
    "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt": "9d18510274892c1fdbcef78e51f6a7b1effee0dc",
}


def blob(path: str) -> str:
    return subprocess.check_output(["git", "hash-object", path], text=True).strip()


def replace_once(path: str, old: str, new: str) -> None:
    p = Path(path)
    text = p.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{path}: expected one anchor, found {count}: {old[:120]!r}")
    p.write_text(text.replace(old, new, 1))


def replace_between(path: str, start: str, end: str, replacement: str) -> None:
    p = Path(path)
    text = p.read_text()
    if text.count(start) != 1 or text.count(end) != 1:
        raise SystemExit(f"{path}: non-unique block anchors")
    start_index = text.index(start)
    end_index = text.index(end, start_index)
    p.write_text(text[:start_index] + replacement + text[end_index:])


for path, expected in EXPECTED_BLOBS.items():
    actual = blob(path)
    if actual != expected:
        raise SystemExit(f"blob mismatch for {path}: expected {expected}, got {actual}")

# Structured wake formatting: Experienced uses a colon marker so visual structure can match
# Beginner without becoming a Beginner-mode detection signal.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightActionSquareTableUi.kt",
    '''        val isStructuredGuidance = guidanceLines.size in 2..3 &&\n            (guidanceLines.first().startsWith("唤醒 ") || guidanceLines.first().startsWith("Wake "))''',
    '''        val wakeLine = guidanceLines.firstOrNull()\n        val isStructuredGuidance = guidanceLines.size in 2..3 &&\n            wakeLine != null &&\n            (\n                wakeLine.startsWith("唤醒 ") ||\n                    wakeLine.startsWith("Wake ") ||\n                    wakeLine.startsWith("唤醒：") ||\n                    wakeLine.startsWith("Wake: ")\n            )''',
)

# Decouple square-table selection color from its default 1/2/check marker and give host-only
# warning badges a semantic tone (used by the Fortune Teller red-herring cue).
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    '''internal enum class ClocktowerSquareTableInteractionMode {\n    ReadOnly,\n    Selectable,\n}\n\ninternal data class ClocktowerSquareTableSeatUiModel(''',
    '''internal enum class ClocktowerSquareTableInteractionMode {\n    ReadOnly,\n    Selectable,\n}\n\ninternal enum class ClocktowerSquareTableBadgeTone {\n    Default,\n    Warning,\n}\n\ninternal data class ClocktowerSquareTableSeatUiModel(''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    '''    val motionKey: String = seatId,\n    val badge: String? = null,\n    val isAlive: Boolean = true,''',
    '''    val motionKey: String = seatId,\n    val stateMarkerOverride: String? = null,\n    val suppressDefaultStateMarker: Boolean = false,\n    val badge: String? = null,\n    val badgeTone: ClocktowerSquareTableBadgeTone = ClocktowerSquareTableBadgeTone.Default,\n    val isAlive: Boolean = true,''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    '''            clocktowerSquareTableStateMarker(seat.state)?.let { marker ->\n                Text(''',
    '''            val stateMarker = seat.stateMarkerOverride\n                ?: if (seat.suppressDefaultStateMarker) null else clocktowerSquareTableStateMarker(seat.state)\n            stateMarker?.let { marker ->\n                Text(''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt",
    '''                Text(\n                    text = badge,\n                    fontSize = 10.sp,''',
    '''                Text(\n                    text = badge,\n                    color = if (seat.badgeTone == ClocktowerSquareTableBadgeTone.Warning) {\n                        Color(0xFFFFC107)\n                    } else {\n                        palette.content\n                    },\n                    fontSize = 10.sp,''',
)

# Pair information: both candidates use the same purple selected style. Only the candidate whose
# authoritative actual role equals the shown role gets a check. Reliability never drives the mark.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt",
    '''internal fun clocktowerPairInformationSeatState(\n    selection: ClocktowerPairManualSelectionModel,\n    seatNumber: Int,\n    editing: Boolean,\n): ClocktowerSquareTableSeatState {\n    if (!editing) {\n        return when (seatNumber) {\n            selection.selectedFirstSeat -> ClocktowerSquareTableSeatState.SelectedFirst\n            selection.selectedSecondSeat -> ClocktowerSquareTableSeatState.SelectedSecond\n            else -> ClocktowerSquareTableSeatState.Neutral\n        }\n    }\n    return clocktowerPairManualSeatState(selection, seatNumber)\n}''',
    '''internal fun clocktowerPairInformationSeatState(\n    selection: ClocktowerPairManualSelectionModel,\n    seatNumber: Int,\n    editing: Boolean,\n): ClocktowerSquareTableSeatState {\n    if (!editing) {\n        return if (\n            seatNumber == selection.selectedFirstSeat ||\n            seatNumber == selection.selectedSecondSeat\n        ) {\n            ClocktowerSquareTableSeatState.SelectedHighlighted\n        } else {\n            ClocktowerSquareTableSeatState.Neutral\n        }\n    }\n    return when (val state = clocktowerPairManualSeatState(selection, seatNumber)) {\n        ClocktowerSquareTableSeatState.SelectedFirst,\n        ClocktowerSquareTableSeatState.SelectedSecond -> ClocktowerSquareTableSeatState.SelectedHighlighted\n        else -> state\n    }\n}''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt",
    '''            clocktowerPairManualSquareTableSeat(\n                seat = seat,\n                language = language,\n                state = seatPresentation.targetState,\n            ).copy(isCurrentActor = seatPresentation.isCurrentActor)''',
    '''            val isSelectedCandidate = !beginnerMode &&\n                (seat.seatId.number == selection.selectedFirstSeat ||\n                    seat.seatId.number == selection.selectedSecondSeat)\n            val isActualShownRole = isSelectedCandidate &&\n                selection.selectedRoleId != null &&\n                seat.actualRole?.roleId == selection.selectedRoleId\n            clocktowerPairManualSquareTableSeat(\n                seat = seat,\n                language = language,\n                state = seatPresentation.targetState,\n            ).copy(\n                isCurrentActor = seatPresentation.isCurrentActor,\n                suppressDefaultStateMarker = isSelectedCandidate,\n                stateMarkerOverride = if (isActualShownRole) "✓" else null,\n            )''',
)

# Empath/Chef: remove teaching legends; preserve role, numeric alternatives and all typed evidence.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt",
    '''                    Spacer(Modifier.height(4.dp))\n                    Text(\n                        text = if (choices.size > 1) {\n                            if (language == "en") {\n                                "N neighbour scope · ★ evil · R Recluse · ✓★ counted in selected result"\n                            } else {\n                                "邻 能力范围 · ★ 邪恶提示 · 隐 隐士 · ✓★ 当前结果计入"\n                            }\n                        } else {\n                            if (language == "en") {\n                                "N neighbour scope · ★ evil · R Recluse"\n                            } else {\n                                "邻 能力范围 · ★ 邪恶提示 · 隐 隐士"\n                            }\n                        },\n                        style = MaterialTheme.typography.labelSmall,\n                        color = MaterialTheme.colorScheme.onSurfaceVariant,\n                        textAlign = TextAlign.Center,\n                    )\n                    Spacer(Modifier.height(8.dp))\n''',
    '''                    Spacer(Modifier.height(8.dp))\n''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt",
    '''                    Spacer(Modifier.height(4.dp))\n                    Text(\n                        text = if (language == "en") {\n                            "★ evil · R Recluse · ✓★ counted in the selected result"\n                        } else {\n                            "★ 邪恶提示 · 隐 隐士 · ✓★ 当前结果计入相邻对"\n                        },\n                        style = MaterialTheme.typography.labelSmall,\n                        color = MaterialTheme.colorScheme.onSurfaceVariant,\n                        textAlign = TextAlign.Center,\n                    )\n                    Spacer(Modifier.height(8.dp))\n''',
    '''                    Spacer(Modifier.height(8.dp))\n''',
)

# Ravenkeeper / Undertaker: keep legal manual alternatives, remove duplicated teaching/context prose.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTableUi.kt",
    '''                ClocktowerNightActionWakeInstruction(wakeInstruction)\n                Text(\n                    text = if (language == "en") "Ravenkeeper" else "守鸦人",\n                    style = MaterialTheme.typography.titleSmall,\n                    fontWeight = FontWeight.Bold,\n                    textAlign = TextAlign.Center,\n                )\n                Spacer(Modifier.height(4.dp))\n                Text(\n                    text = if (selectedSeat == null) {\n                        if (language == "en") {\n                            "Select the player chosen by the Ravenkeeper"\n                        } else {\n                            "选择守鸦人要查验的玩家"\n                        }\n                    } else {\n                        if (language == "en") "Selected target: P$selectedSeat" else "查验目标：P$selectedSeat"\n                    },\n                    style = MaterialTheme.typography.bodySmall,\n                    fontWeight = if (selectedSeat != null) FontWeight.SemiBold else FontWeight.Normal,\n                    textAlign = TextAlign.Center,\n                )\n                Spacer(Modifier.height(8.dp))\n''',
    '''                ClocktowerNightActionWakeInstruction(wakeInstruction)\n''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerRavenkeeperSquareTableUi.kt",
    '''                            if (language == "en") {\n                                "Show information: ${choice.displayLabel}"\n                            } else {\n                                "展示信息：${choice.displayLabel}"\n                            },''',
    '''                            if (language == "en") "Show to player" else "展示给玩家",''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt",
    '''                    ClocktowerNightActionWakeInstruction(wakeInstruction)\n                    Text(\n                        text = if (language == "en") "Undertaker" else "送葬者",\n                        style = MaterialTheme.typography.titleSmall,\n                        fontWeight = FontWeight.Bold,\n                        textAlign = TextAlign.Center,\n                    )\n                    Spacer(Modifier.height(4.dp))\n                    Text(\n                        text = if (language == "en") {\n                            "EX marks today's executed player · read-only context"\n                        } else {\n                            "处 标记今天被处决的玩家 · 仅作信息上下文"\n                        },\n                        style = MaterialTheme.typography.labelSmall,\n                        color = MaterialTheme.colorScheme.onSurfaceVariant,\n                        textAlign = TextAlign.Center,\n                    )\n                    Spacer(Modifier.height(8.dp))\n''',
    '''                    ClocktowerNightActionWakeInstruction(wakeInstruction)\n''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt",
    '''                            if (language == "en") {\n                                "Show information: ${selectedChoice.displayLabel}"\n                            } else {\n                                "展示信息：${selectedChoice.displayLabel}"\n                            },''',
    '''                            if (language == "en") "Show to player" else "展示给玩家",''',
)

# Fortune Teller: same-color pair, no 1/2 markers, persistent yellow red-herring cue, and only the
# result controls after the two players are selected.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt",
    '''    selectedSeats.getOrNull(0) == seatNumber -> ClocktowerSquareTableSeatState.SelectedFirst\n    selectedSeats.getOrNull(1) == seatNumber -> ClocktowerSquareTableSeatState.SelectedSecond''',
    '''    seatNumber in selectedSeats -> ClocktowerSquareTableSeatState.SelectedHighlighted''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt",
    '''    wakeInstruction: String? = null,\n    legalResults: Set<Boolean>,''',
    '''    wakeInstruction: String? = null,\n    redHerringSeat: Int? = null,\n    legalResults: Set<Boolean>,''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt",
    '''    if (clocktowerUsesBeginnerCompactNightGuidance(wakeInstruction)) {\n        val actions = clocktowerFortuneTellerResultActions(legalResults, recommendedResult)\n        val result = recommendedResult?.takeIf { it in legalResults } ?: actions.firstOrNull()\n        val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2\n        ClocktowerBeginnerTwoTargetRevealDialog(\n            seats = seats,\n            actorSeat = actorSeat,\n            selectedSeats = selectedSeats,\n            selectableSeats = selectableSeats,\n            enabled = enabled,\n            wakeInstruction = wakeInstruction,\n            language = language,\n            canGoPrevious = canGoPrevious,\n            onSeatSelected = onSeatSelected,\n            onPrevious = onPrevious,\n            onHostTools = onHostTools,\n            onNext = onNext,\n            onShow = if (completePair && result != null) ({\n                if (automaticStorytellerInfo) onAutomaticResultSelected(result) else onResultSelected(result)\n            }) else null,\n        )\n        return\n    }\n''',
    '''''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt",
    '''            ClocktowerSquareTableSeatUiModel(\n                seatId = seat.seatId.renderKey(),\n                seatNumber = seat.seatId.number,\n                label = content.primaryLabel,\n                detailLabels = content.detailLabels,\n                isAlive = seat.isAlive,\n                hasUnspentGhostVote = seat.hasUnspentGhostVote,\n                state = clocktowerFortuneTellerSeatState(\n                    seatNumber = seat.seatId.number,\n                    selectedSeats = selectedSeats,\n                    selectableSeats = if (enabled) selectableSeats else emptySet(),\n                ),\n                isCurrentActor = seat.seatId.number == actorSeat,\n            )''',
    '''            val isSelected = seat.seatId.number in selectedSeats\n            val isRedHerring = seat.seatId.number == redHerringSeat\n            ClocktowerSquareTableSeatUiModel(\n                seatId = seat.seatId.renderKey(),\n                seatNumber = seat.seatId.number,\n                label = content.primaryLabel,\n                detailLabels = content.detailLabels,\n                isAlive = seat.isAlive,\n                hasUnspentGhostVote = seat.hasUnspentGhostVote,\n                state = clocktowerFortuneTellerSeatState(\n                    seatNumber = seat.seatId.number,\n                    selectedSeats = selectedSeats,\n                    selectableSeats = if (enabled) selectableSeats else emptySet(),\n                ),\n                isCurrentActor = seat.seatId.number == actorSeat,\n                suppressDefaultStateMarker = isSelected,\n                badge = if (isRedHerring) {\n                    if (language == "en") "RH" else "鲱"\n                } else {\n                    null\n                },\n                badgeTone = if (isRedHerring) {\n                    ClocktowerSquareTableBadgeTone.Warning\n                } else {\n                    ClocktowerSquareTableBadgeTone.Default\n                },\n            )''',
)
new_ft_center = '''@Composable\nprivate fun ClocktowerFortuneTellerCenterControls(\n    wakeInstruction: String?,\n    selectedSeats: List<Int>,\n    legalResults: Set<Boolean>,\n    recommendedResult: Boolean?,\n    automaticStorytellerInfo: Boolean,\n    language: String,\n    onResultSelected: (Boolean) -> Unit,\n    onAutomaticResultSelected: (Boolean) -> Unit,\n) {\n    val actions = clocktowerFortuneTellerResultActions(\n        legalResults = legalResults,\n        recommendedResult = recommendedResult,\n    )\n    val completePair = selectedSeats.size == 2 && selectedSeats.distinct().size == 2\n\n    Column(\n        modifier = Modifier\n            .fillMaxSize()\n            .padding(6.dp),\n        horizontalAlignment = Alignment.CenterHorizontally,\n        verticalArrangement = Arrangement.Center,\n    ) {\n        ClocktowerNightActionWakeInstruction(wakeInstruction)\n        if (completePair) {\n            when {\n                actions.isEmpty() -> {\n                    Text(\n                        text = if (language == "en") "No legal result" else "无可用结果",\n                        color = MaterialTheme.colorScheme.error,\n                        style = MaterialTheme.typography.bodySmall,\n                        textAlign = TextAlign.Center,\n                    )\n                }\n\n                automaticStorytellerInfo -> {\n                    val result = recommendedResult?.takeIf { it in legalResults } ?: actions.first()\n                    Button(\n                        onClick = { onAutomaticResultSelected(result) },\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(clocktowerFortuneTellerResultLabel(result, language))\n                    }\n                }\n\n                actions.size == 1 -> {\n                    Button(\n                        onClick = { onResultSelected(actions.single()) },\n                        modifier = Modifier.fillMaxWidth(),\n                    ) {\n                        Text(clocktowerFortuneTellerResultLabel(actions.single(), language))\n                    }\n                }\n\n                else -> actions.forEachIndexed { index, value ->\n                    Text(\n                        text = if (index == 0 && value == recommendedResult) {\n                            if (language == "en") "Recommended" else "推荐"\n                        } else {\n                            if (language == "en") "Other option" else "另一个选项"\n                        },\n                        style = MaterialTheme.typography.labelSmall,\n                        textAlign = TextAlign.Center,\n                    )\n                    if (index == 0 && value == recommendedResult) {\n                        Button(\n                            onClick = { onResultSelected(value) },\n                            modifier = Modifier.fillMaxWidth(),\n                        ) {\n                            Text(clocktowerFortuneTellerResultLabel(value, language))\n                        }\n                    } else {\n                        OutlinedButton(\n                            onClick = { onResultSelected(value) },\n                            modifier = Modifier.fillMaxWidth(),\n                        ) {\n                            Text(clocktowerFortuneTellerResultLabel(value, language))\n                        }\n                    }\n                    if (index != actions.lastIndex) Spacer(Modifier.height(4.dp))\n                }\n            }\n        }\n        Spacer(Modifier.height(6.dp))\n    }\n}\n'''
replace_between(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt",
    "@Composable\nprivate fun ClocktowerFortuneTellerCenterControls(",
    "\nprivate fun clocktowerFortuneTellerResultLabel(",
    new_ft_center,
)

# Host -> NightStep wiring for the persistent red-herring cue.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt",
    '''                fortuneTellerFirst = fortuneTellerFirst,\n                fortuneTellerSecond = fortuneTellerSecond,\n                chambermaidFirst = chambermaidResolution.selection.first,''',
    '''                fortuneTellerFirst = fortuneTellerFirst,\n                fortuneTellerSecond = fortuneTellerSecond,\n                redHerring = redHerring,\n                chambermaidFirst = chambermaidResolution.selection.first,''',
)

# NightStep wiring: compact wake guidance for both modes, compact evil-team group projection, and
# red-herring seat projection. No legality/recommendation/session ownership changes.
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    '''    fortuneTellerFirst: String?,\n    fortuneTellerSecond: String?,\n    chambermaidFirst: String?,''',
    '''    fortuneTellerFirst: String?,\n    fortuneTellerSecond: String?,\n    redHerring: String?,\n    chambermaidFirst: String?,''',
)
old_guidance = '''    val beginnerGuidance = if (automaticStorytellerInfo) {\n        clocktowerBeginnerNightGuidance(\n            action = step.action,\n            actor = step.actor,\n            cards = cards,\n            language = language,\n            groupTeam = step.actor\n                ?.clocktowerRole\n                ?.team\n                ?.takeIf { team ->\n                    step.displayKind == ClocktowerDisplayKind.EvilInfo &&\n                        team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)\n                },\n        )\n    } else {\n        null\n    }\n    val command = beginnerGuidance?.asWakeInstruction() ?: when {'''
new_guidance = '''    val compactNightGuidance = clocktowerBeginnerNightGuidance(\n        action = step.action,\n        actor = step.actor,\n        cards = cards,\n        language = language,\n        groupTeam = step.actor\n            ?.clocktowerRole\n            ?.team\n            ?.takeIf { team ->\n                step.displayKind == ClocktowerDisplayKind.EvilInfo &&\n                    team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)\n            },\n    )\n    val command = compactNightGuidance?.asWakeInstruction(\n        experiencedFormat = !automaticStorytellerInfo,\n    ) ?: when {'''
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    old_guidance,
    new_guidance,
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    '''        val actionActorSeat = seatNumberForName(step.actor?.name)\n        val pairSquareTablePresentation = if (''',
    '''        val actionActorSeat = seatNumberForName(step.actor?.name)\n        val redHerringSeat = seatNumberForName(redHerring)\n        val evilInfoTeam = step.actor\n            ?.clocktowerRole\n            ?.team\n            ?.takeIf { team ->\n                step.displayKind == ClocktowerDisplayKind.EvilInfo &&\n                    team in setOf(ClocktowerTeam.Minion, ClocktowerTeam.Demon)\n            }\n        val evilInfoTeamCards = evilInfoTeam\n            ?.let { team -> cards.filter { card -> card.clocktowerRole?.team == team } }\n            .orEmpty()\n        val evilInfoHighlightedSeats = if (evilInfoTeam == ClocktowerTeam.Minion) {\n            evilInfoTeamCards.mapNotNull { card -> seatNumberForName(card.name) }.toSet()\n        } else {\n            emptySet()\n        }\n        val evilInfoGroupLines = if (evilInfoTeam == ClocktowerTeam.Minion) {\n            evilInfoTeamCards.mapNotNull { card ->\n                seatNumberForName(card.name)?.let { seat ->\n                    if (language == "en") "P$seat ${card.name}" else "${seat}号 ${card.name}"\n                }\n            }\n        } else {\n            emptyList()\n        }\n        val pairSquareTablePresentation = if (''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    '''                wakeInstruction = command,\n                beginnerMode = automaticStorytellerInfo,\n            )''',
    '''                wakeInstruction = command,\n                beginnerMode = automaticStorytellerInfo,\n                compactTeamWake = evilInfoTeam != null,\n                highlightedSeats = evilInfoHighlightedSeats,\n                groupLines = evilInfoGroupLines,\n            )''',
)
replace_once(
    "app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt",
    '''                    actorSeat = actionActorSeat,\n                    wakeInstruction = command,\n                    legalResults = fortuneTellerLegalResults,''',
    '''                    actorSeat = actionActorSeat,\n                    wakeInstruction = command,\n                    redHerringSeat = redHerringSeat,\n                    legalResults = fortuneTellerLegalResults,''',
)

print("Experienced UI 2 patch applied successfully")
