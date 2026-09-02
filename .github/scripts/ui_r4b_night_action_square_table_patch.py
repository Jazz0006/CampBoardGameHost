from pathlib import Path


def read_text(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}; refusing implicit normalization")
    return raw.decode("utf-8")


def replace_exact(text: str, label: str, old: str, new: str, expected: int = 1) -> str:
    count = text.count(old)
    if count != expected:
        raise SystemExit(f"{label}: expected {expected} exact anchor occurrence(s), found {count}")
    return text.replace(old, new, expected)


# 1. Player-facing typed subject highlighting.
player_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerPlayerDisplayUi.kt")
player = read_text(player_path)
old_highlight = '''internal fun clocktowerPlayerDisplayHighlightedSeats(
    step: ClocktowerNightStepUi,
): Set<Int> {
    if (step.displayKind != ClocktowerDisplayKind.EitherOne) return emptySet()
    val anyOf = step.displayProposition as? InformationProposition.AnyOf ?: return emptySet()
    val roleAt = anyOf.alternatives.map { it as? InformationProposition.RoleAt ?: return emptySet() }
    if (roleAt.size != 2) return emptySet()
    if (roleAt.map { it.role }.distinct().size != 1) return emptySet()
    val seats = roleAt.map { it.seat }.distinct()
    return if (seats.size == 2) seats.toSet() else emptySet()
}
'''
new_highlight = '''internal fun clocktowerPlayerDisplayHighlightedSeats(
    step: ClocktowerNightStepUi,
): Set<Int> = when (step.displayKind) {
    ClocktowerDisplayKind.EitherOne -> {
        val anyOf = step.displayProposition as? InformationProposition.AnyOf ?: return emptySet()
        val roleAt = anyOf.alternatives.map { it as? InformationProposition.RoleAt ?: return emptySet() }
        if (roleAt.size != 2 || roleAt.map { it.role }.distinct().size != 1) return emptySet()
        roleAt.map { it.seat }.distinct().takeIf { it.size == 2 }?.toSet().orEmpty()
    }

    ClocktowerDisplayKind.RoleReveal ->
        (step.displayProposition as? InformationProposition.RoleAt)?.let { setOf(it.seat) }.orEmpty()

    ClocktowerDisplayKind.Number ->
        (step.displayProposition as? InformationProposition.NumericResult)
            ?.subjectSeats
            ?.distinct()
            ?.takeIf { it.size in 1..2 }
            ?.toSet()
            .orEmpty()

    ClocktowerDisplayKind.YesNo ->
        (step.displayProposition as? InformationProposition.BooleanResult)
            ?.subjectSeats
            ?.distinct()
            ?.takeIf { it.size in 1..2 }
            ?.toSet()
            .orEmpty()

    else -> emptySet()
}
'''
player = replace_exact(player, "player typed highlight helper", old_highlight, new_highlight)
player_path.write_text(player, encoding="utf-8", newline="\n")


# 2. Chambermaid materialization must carry typed subject seats for reliable and discretionary values.
host_path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
host = read_text(host_path)
chambermaid_action_anchor = '''                action = ClocktowerNightAction.Chambermaid,
                displaySecondary = listOfNotNull(chambermaidResolution.selection.first, chambermaidResolution.selection.second)
'''
chambermaid_action_replacement = '''                action = ClocktowerNightAction.Chambermaid,
                displayProposition = chambermaidResult?.toIntOrNull()?.let { value ->
                    val firstTargetName = chambermaidResolution.selection.first
                    val secondTargetName = chambermaidResolution.selection.second
                    if (firstTargetName != null && secondTargetName != null) {
                        roleActor("Chambermaid")?.let { actor ->
                            clocktowerChambermaidDisplayProposition(
                                cards = cards,
                                actor = actor,
                                firstTargetName = firstTargetName,
                                secondTargetName = secondTargetName,
                                value = value,
                            )
                        }
                    } else {
                        null
                    }
                },
                displaySecondary = listOfNotNull(chambermaidResolution.selection.first, chambermaidResolution.selection.second)
'''
host = replace_exact(
    host,
    "Chambermaid reliable typed proposition",
    chambermaid_action_anchor,
    chambermaid_action_replacement,
    expected=2,
)
chambermaid_option_anchor = '''                            secondary = listOfNotNull(chambermaidResolution.selection.first, chambermaidResolution.selection.second)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                        )
'''
chambermaid_option_replacement = '''                            secondary = listOfNotNull(chambermaidResolution.selection.first, chambermaidResolution.selection.second)
                                .mapNotNull { name -> cards.firstOrNull { it.name == name } }
                                .joinToString("   ") { seatNumberText(it) }
                                .takeIf { it.isNotBlank() },
                            propositionForValue = { value ->
                                clocktowerChambermaidDisplayProposition(
                                    cards = cards,
                                    actor = actor,
                                    firstTargetName = requireNotNull(chambermaidResolution.selection.first),
                                    secondTargetName = requireNotNull(chambermaidResolution.selection.second),
                                    value = value,
                                )
                            },
                        )
'''
host = replace_exact(
    host,
    "Chambermaid discretionary typed propositions",
    chambermaid_option_anchor,
    chambermaid_option_replacement,
    expected=2,
)
host_path.write_text(host, encoding="utf-8", newline="\n")


# 3. Fortune Teller full-screen dialog must retain normal night-step navigation.
fortune_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerFortuneTellerSquareTableUi.kt")
fortune = read_text(fortune_path)
fortune = replace_exact(
    fortune,
    "Fortune Teller dialog onNext signature",
    '''    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
) {
''',
    '''    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
''',
)
fortune = replace_exact(
    fortune,
    "Fortune Teller center call onNext",
    '''                    onAutomaticResultSelected = onAutomaticResultSelected,
                    onPrevious = onPrevious,
                )
''',
    '''                    onAutomaticResultSelected = onAutomaticResultSelected,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
''',
)
fortune = replace_exact(
    fortune,
    "Fortune Teller center signature onNext",
    '''    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
) {
''',
    '''    onAutomaticResultSelected: (Boolean) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
''',
)
fortune = replace_exact(
    fortune,
    "Fortune Teller next navigation",
    '''        if (canGoPrevious) {
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onPrevious) {
                Text(if (language == "en") "Previous step" else "上一步")
            }
        }
''',
    '''        Spacer(Modifier.height(6.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (language == "en") "Finish / Next" else "完成 / 下一步")
        }

        if (canGoPrevious) {
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onPrevious) {
                Text(if (language == "en") "Previous step" else "上一步")
            }
        }
''',
)
fortune_path.write_text(fortune, encoding="utf-8", newline="\n")


# 4. NightStep orchestration: feed established candidate authorities into the reusable square-table owners.
night_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
night = read_text(night_path)
when_anchor = '''
        when (step.action) {
'''
when_replacement = '''
        val nightActionSeats = cards.mapIndexed { index, card ->
            ClocktowerNightActionSeatUiModel(
                seatId = "seat-${index + 1}",
                seatNumber = index + 1,
                label = card.name,
            )
        }
        fun seatNumberForName(name: String?): Int? = name
            ?.let { selected -> cards.indexOfFirst { card -> card.name == selected } }
            ?.takeIf { index -> index >= 0 }
            ?.plus(1)
        fun selectableSeatNumbers(candidates: List<PlayerCard>): Set<Int> = candidates
            .mapNotNull { candidate -> seatNumberForName(candidate.name) }
            .toSet()

        when (step.action) {
'''
night = replace_exact(night, "night action seat projection", when_anchor, when_replacement)

poison_old = '''            ClocktowerNightAction.Poison -> {
                HostActionSection(title = stringResource(R.string.clocktower_host_choose_poison_target)) {
                    SelectablePlayerChips(
                        cards = aliveCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
poison_new = '''            ClocktowerNightAction.Poison -> {
                val candidates = aliveCards
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_poison_target),
                    helper = null,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
'''
night = replace_exact(night, "Poisoner square-table action", poison_old, poison_new)

butler_old = '''            ClocktowerNightAction.ButlerMaster -> {
                HostActionSection(
                    title = if (LocalContext.current.resources.configuration.locales[0].language == "en") "Choose the Butler's master" else "选择管家的主人",
                ) {
                    SelectablePlayerChips(
                        cards = cards.filter { it.name != step.actor?.name },
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
butler_new = '''            ClocktowerNightAction.ButlerMaster -> {
                val candidates = cards.filter { it.name != step.actor?.name }
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = if (language == "en") "Choose the Butler's master" else "选择管家的主人",
                    helper = null,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
'''
night = replace_exact(night, "Butler square-table action", butler_old, butler_new)

monk_old = '''            ClocktowerNightAction.MonkProtect -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                ) {
                    SelectablePlayerChips(
                        cards = clocktowerMonkTargetCards(cards, step.actor?.name),
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
monk_new = '''            ClocktowerNightAction.MonkProtect -> {
                val candidates = clocktowerMonkTargetCards(cards, step.actor?.name)
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_monk_protect),
                    helper = stringResource(R.string.clocktower_host_choose_monk_protect_hint),
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
'''
night = replace_exact(night, "Monk square-table action", monk_old, monk_new)

night = replace_exact(
    night,
    "Fortune Teller next wiring",
    '''                    onAutomaticResultSelected = ::showStructuredFortuneTellerResult,
                    onPrevious = onPrevious,
                )
''',
    '''                    onAutomaticResultSelected = ::showStructuredFortuneTellerResult,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
''',
)

chambermaid_old = '''            ClocktowerNightAction.Chambermaid -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val candidates = chambermaidTargetCards.filter { it.name != step.actor?.name }
                    SelectableSeatNumbers(
                        cards = candidates,
                        selectedName = chambermaidFirst,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectChambermaidFirst,
                    )
                    SelectableSeatNumbers(
                        cards = candidates.filter { it.name != chambermaidFirst },
                        selectedName = chambermaidSecond,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectChambermaidSecond,
                    )
                    if (step.displayOptions.isEmpty()) {
                        Button(
                            onClick = { onShowPlayerDisplay(step) },
                            enabled = step.isRealAction && chambermaidFirst != null && chambermaidSecond != null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(if (language == "en") "Check and show" else "查询并展示")
                        }
                    } else {
                        Text(if (language == "en") "This ability is unreliable. Choose the final result below." else "能力不可靠：请在下方选择最终展示结果。", color = Color(0xFF8C4B20))
                    }
                }
            }
'''
chambermaid_new = '''            ClocktowerNightAction.Chambermaid -> {
                val candidates = chambermaidTargetCards.filter { it.name != step.actor?.name }
                val selectedSeats = listOfNotNull(chambermaidFirst, chambermaidSecond)
                    .mapNotNull(::seatNumberForName)
                val candidateSeats = selectableSeatNumbers(candidates)
                val selectableSeats = when (selectedSeats.size) {
                    0 -> candidateSeats
                    1 -> candidateSeats - selectedSeats.first()
                    else -> emptySet()
                }
                val resultOptions = when {
                    step.displayOptions.isEmpty() -> emptyList()
                    automaticStorytellerInfo -> listOfNotNull(automaticDisplayOption)
                    else -> step.displayOptions
                }
                ClocktowerChambermaidSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeats = selectedSeats,
                    selectableSeats = selectableSeats,
                    enabled = step.isRealAction,
                    resultOptions = resultOptions,
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let { name ->
                            when (twoPlayerSelectionAction(chambermaidFirst, chambermaidSecond, name)) {
                                TwoPlayerSelectionAction.ToggleFirst -> onSelectChambermaidFirst(name)
                                TwoPlayerSelectionAction.ToggleSecond -> onSelectChambermaidSecond(name)
                                TwoPlayerSelectionAction.RejectLimit -> Unit
                            }
                        }
                    },
                    onShowDeterminedResult = { onShowPlayerDisplay(step) },
                    onResultSelected = ::showRecommendedDisplayOption,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
'''
night = replace_exact(night, "Chambermaid square-table action", chambermaid_old, chambermaid_new)

demon_old = '''            ClocktowerNightAction.DemonKill -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_choose_night_death),
                    helper = stringResource(R.string.clocktower_host_choose_night_death_hint),
                ) {
                    SelectablePlayerChips(
                        cards = aliveCards,
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
demon_new = '''            ClocktowerNightAction.DemonKill -> {
                val candidates = aliveCards
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_choose_night_death),
                    helper = stringResource(R.string.clocktower_host_choose_night_death_hint),
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
            }
'''
night = replace_exact(night, "Demon kill square-table action", demon_old, demon_new)

raven_old = '''            ClocktowerNightAction.Ravenkeeper -> {
                HostActionSection(
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                ) {
                    SelectablePlayerChips(
                        cards = clocktowerRavenkeeperTargetCards(cards),
                        selectedName = selectedName,
                        enabled = step.isRealAction,
                        allCards = cards,
                        onSelect = onSelectName,
                    )
                }
            }
'''
raven_new = '''            ClocktowerNightAction.Ravenkeeper -> {
                val candidates = clocktowerRavenkeeperTargetCards(cards)
                ClocktowerSingleTargetSquareTableDialog(
                    seats = nightActionSeats,
                    selectedSeat = seatNumberForName(selectedName),
                    selectableSeats = selectableSeatNumbers(candidates),
                    enabled = step.isRealAction,
                    title = stringResource(R.string.clocktower_host_ravenkeeper_target),
                    helper = stringResource(R.string.clocktower_host_ravenkeeper_target_hint),
                    language = language,
                    canGoPrevious = canGoPrevious,
                    onSeatSelected = { seatNumber ->
                        cards.getOrNull(seatNumber - 1)?.name?.let(onSelectName)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                    secondaryActionLabel = stringResource(R.string.clocktower_host_show_to_player),
                    secondaryActionEnabled = step.tellPlayer?.isNotBlank() == true && step.displayKind != ClocktowerDisplayKind.None,
                    onSecondaryAction = { onShowPlayerDisplay(step) },
                )
            }
'''
night = replace_exact(night, "Ravenkeeper square-table action", raven_old, raven_new)

night_path.write_text(night, encoding="utf-8", newline="\n")

# Final postconditions.
checks = {
    player_path: ["ClocktowerDisplayKind.RoleReveal", "InformationProposition.NumericResult", "InformationProposition.BooleanResult"],
    host_path: ["clocktowerChambermaidDisplayProposition("],
    fortune_path: ["onNext: () -> Unit", '"Finish / Next"'],
    night_path: [
        "ClocktowerSingleTargetSquareTableDialog(",
        "ClocktowerChambermaidSquareTableDialog(",
        "val candidates = clocktowerRavenkeeperTargetCards(cards)",
        "val candidates = clocktowerMonkTargetCards(cards, step.actor?.name)",
        "onNext = onNext",
    ],
}
for path, tokens in checks.items():
    content = path.read_text(encoding="utf-8")
    for token in tokens:
        if token not in content:
            raise SystemExit(f"Missing postcondition in {path}: {token}")
