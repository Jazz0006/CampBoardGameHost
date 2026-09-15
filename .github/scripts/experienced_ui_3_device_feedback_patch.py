from pathlib import Path

ROOT = Path('.')


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding='utf-8')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected exactly one anchor, found {count}')
    return text.replace(old, new, 1)


# Shared square-table registration-hint palette.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerSquareTableUi.kt'
text = read(path)
text = replace_once(
    text,
    '    HighlightedInformation,\n    Disabled,\n',
    '    HighlightedInformation,\n    RegistrationHint,\n    Disabled,\n',
    'square-table enum',
)
text = replace_once(
    text,
    '                            ClocktowerSquareTableSeatState.HighlightedInformation,\n',
    '                            ClocktowerSquareTableSeatState.HighlightedInformation,\n                            ClocktowerSquareTableSeatState.RegistrationHint,\n',
    'square-table bold states',
)
text = replace_once(
    text,
    '''        ClocktowerSquareTableSeatState.HighlightedInformation -> ClocktowerSquareTableSeatPalette(\n            container = colors.tertiaryContainer,\n            content = colors.onTertiaryContainer,\n            border = colors.tertiary,\n            borderWidth = 3.dp,\n        )\n        ClocktowerSquareTableSeatState.Disabled -> ClocktowerSquareTableSeatPalette(\n''',
    '''        ClocktowerSquareTableSeatState.HighlightedInformation -> ClocktowerSquareTableSeatPalette(\n            container = colors.tertiaryContainer,\n            content = colors.onTertiaryContainer,\n            border = colors.tertiary,\n            borderWidth = 3.dp,\n        )\n        ClocktowerSquareTableSeatState.RegistrationHint -> ClocktowerSquareTableSeatPalette(\n            container = colors.errorContainer.copy(alpha = 0.72f),\n            content = colors.onErrorContainer,\n            border = colors.error.copy(alpha = 0.78f),\n            borderWidth = 3.dp,\n        )\n        ClocktowerSquareTableSeatState.Disabled -> ClocktowerSquareTableSeatPalette(\n''',
    'square-table registration palette',
)
text = replace_once(
    text,
    '    ClocktowerSquareTableSeatState.HighlightedInformation -> "★"\n    ClocktowerSquareTableSeatState.Disabled -> "×"\n',
    '    ClocktowerSquareTableSeatState.HighlightedInformation -> "★"\n    ClocktowerSquareTableSeatState.RegistrationHint -> null\n    ClocktowerSquareTableSeatState.Disabled -> "×"\n',
    'square-table registration marker',
)
write(path, text)


# Pair info: recommended Experienced view mirrors Beginner, with Change as the only extra action.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTableUi.kt'
text = read(path)
text = replace_once(
    text,
    '''                stateMarkerOverride = clocktowerPairInformationTruthMarker(\n                    isSelectedCandidate = isSelectedCandidate,\n                    selectedRoleId = selection.selectedRoleId,\n                    actualRoleId = seat.actualRole?.roleId,\n                ),\n''',
    '''                stateMarkerOverride = clocktowerPairInformationTruthMarker(\n                    isSelectedCandidate = isSelectedCandidate,\n                    selectedRoleId = selection.selectedRoleId,\n                    actualRoleId = seat.actualRole?.roleId,\n                    selectedOption = selection.resolvedOption,\n                ),\n''',
    'pair truth marker call',
)
old_helper = '''internal fun clocktowerPairInformationTruthMarker(\n    isSelectedCandidate: Boolean,\n    selectedRoleId: String?,\n    actualRoleId: String?,\n): String? = if (\n    isSelectedCandidate &&\n    selectedRoleId != null &&\n    actualRoleId == selectedRoleId\n) {\n    "✓"\n} else {\n    null\n}\n'''
new_helper = '''internal fun clocktowerPairInformationTruthMarker(\n    isSelectedCandidate: Boolean,\n    selectedRoleId: String?,\n    actualRoleId: String?,\n    selectedOption: ClocktowerDisplayOption?,\n): String? {\n    if (!isSelectedCandidate || selectedRoleId == null) return null\n    val directTruth = actualRoleId == selectedRoleId\n    val spyRegistrationTruth = actualRoleId == "Spy" &&\n        selectedOption?.spyRegistersGood == true &&\n        selectedOption.spyRegisteredRoleEnName == selectedRoleId\n    val recluseRegistrationTruth = actualRoleId == "Recluse" &&\n        selectedOption?.recluseRegistersEvil == true &&\n        selectedOption.recluseRegisteredRoleEnName == selectedRoleId\n    return if (directTruth || spyRegistrationTruth || recluseRegistrationTruth) "✓" else null\n}\n'''
text = replace_once(text, old_helper, new_helper, 'pair truth helper')
old_beginner = '''    if (beginnerMode) {\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .padding(6.dp),\n            horizontalAlignment = Alignment.CenterHorizontally,\n            verticalArrangement = Arrangement.Center,\n        ) {\n            ClocktowerNightActionWakeInstruction(wakeInstruction)\n            OutlinedButton(\n                onClick = { selection.resolvedOption?.let(onConfirm) },\n                enabled = selection.resolvedOption != null,\n                modifier = Modifier.fillMaxWidth(),\n            ) {\n                Text(if (language == "en") "Show to player" else "展示给玩家")\n            }\n        }\n        return\n    }\n\n    var roleMenuExpanded by remember { mutableStateOf(false) }\n'''
new_beginner = '''    if (beginnerMode || !editing) {\n        Column(\n            modifier = Modifier\n                .fillMaxSize()\n                .padding(6.dp),\n            horizontalAlignment = Alignment.CenterHorizontally,\n            verticalArrangement = Arrangement.Center,\n        ) {\n            ClocktowerNightActionWakeInstruction(wakeInstruction)\n            OutlinedButton(\n                onClick = { selection.resolvedOption?.let(onConfirm) },\n                enabled = selection.resolvedOption != null,\n                modifier = Modifier.fillMaxWidth(),\n            ) {\n                Text(if (language == "en") "Show to player" else "展示给玩家")\n            }\n            if (!beginnerMode && allowManualEditing) {\n                Spacer(Modifier.height(4.dp))\n                TextButton(onClick = onStartEditing) {\n                    Text(if (language == "en") "Change" else "修改信息")\n                }\n            }\n        }\n        return\n    }\n\n    var roleMenuExpanded by remember { mutableStateOf(false) }\n'''
text = replace_once(text, old_beginner, new_beginner, 'pair compact recommended view')
write(path, text)


# Shared Experienced numeric choice rows.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerExperiencedNumericChoiceUi.kt'
if (ROOT / path).exists():
    raise SystemExit('numeric choice component unexpectedly already exists')
write(path, '''package com.codex.campboardgamehost\n\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.OutlinedButton\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\n\n/** Compact Experienced-only picker: recommended result is always the first one-tap action. */\n@Composable\ninternal fun <T> ClocktowerExperiencedNumericChoiceRows(\n    choices: List<T>,\n    recommendedChoice: T,\n    language: String,\n    valueOf: (T) -> Int,\n    onConfirm: (T) -> Unit,\n) {\n    Text(\n        text = if (language == "en") "Recommended information" else "推荐信息",\n        style = MaterialTheme.typography.bodySmall,\n        fontWeight = FontWeight.SemiBold,\n    )\n    Spacer(Modifier.height(4.dp))\n    Button(\n        onClick = { onConfirm(recommendedChoice) },\n        modifier = Modifier.fillMaxWidth(),\n    ) {\n        Text(\n            if (language == "en") "Show: ${valueOf(recommendedChoice)}" else "展示：${valueOf(recommendedChoice)}",\n            maxLines = 1,\n        )\n    }\n\n    val alternatives = choices.filterNot { it == recommendedChoice }\n    if (alternatives.isNotEmpty()) {\n        Spacer(Modifier.height(8.dp))\n        Text(\n            text = if (language == "en") "Alternative information" else "备选信息",\n            style = MaterialTheme.typography.bodySmall,\n            fontWeight = FontWeight.SemiBold,\n        )\n        Spacer(Modifier.height(4.dp))\n        alternatives.chunked(3).forEach { rowChoices ->\n            Row(\n                modifier = Modifier.fillMaxWidth(),\n                horizontalArrangement = Arrangement.spacedBy(4.dp),\n            ) {\n                rowChoices.forEach { choice ->\n                    OutlinedButton(\n                        onClick = { onConfirm(choice) },\n                        modifier = Modifier.weight(1f),\n                    ) {\n                        Text(valueOf(choice).toString(), maxLines = 1)\n                    }\n                }\n                repeat(3 - rowChoices.size) { Spacer(Modifier.weight(1f)) }\n            }\n            Spacer(Modifier.height(4.dp))\n        }\n    }\n}\n''')


# Chef: special registration color and direct recommended/alternate rows.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerChefSquareTableUi.kt'
text = read(path)
text = replace_once(
    text,
    '''internal data class ClocktowerChefSeatVisual(\n    val state: ClocktowerSquareTableSeatState,\n    val badge: String?,\n    val isCurrentActor: Boolean,\n)\n''',
    '''internal data class ClocktowerChefSeatVisual(\n    val state: ClocktowerSquareTableSeatState,\n    val badge: String?,\n    val marker: String?,\n    val isCurrentActor: Boolean,\n)\n''',
    'chef visual model',
)
old_chef_visual = '''internal fun clocktowerChefSeatVisual(\n    seatNumber: Int,\n    actorSeat: Int?,\n    actualEvilSeats: Set<Int>,\n    recluseSeat: Int?,\n    effectivePairSeats: Set<Int>,\n    language: String,\n): ClocktowerChefSeatVisual = ClocktowerChefSeatVisual(\n    state = when {\n        seatNumber in effectivePairSeats -> ClocktowerSquareTableSeatState.SelectedHighlighted\n        seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation\n        else -> ClocktowerSquareTableSeatState.Neutral\n    },\n    badge = if (seatNumber == recluseSeat) {\n        if (language == "en") "R" else "隐"\n    } else {\n        null\n    },\n    isCurrentActor = seatNumber == actorSeat,\n)\n'''
new_chef_visual = '''internal fun clocktowerChefSeatVisual(\n    seatNumber: Int,\n    actorSeat: Int?,\n    actualEvilSeats: Set<Int>,\n    spySeat: Int?,\n    recluseSeat: Int?,\n    effectivePairSeats: Set<Int>,\n    language: String,\n): ClocktowerChefSeatVisual {\n    val isSpy = seatNumber == spySeat\n    val isRecluse = seatNumber == recluseSeat\n    val isRegistrationRole = isSpy || isRecluse\n    val participatesInCountedPair = seatNumber in effectivePairSeats\n    return ClocktowerChefSeatVisual(\n        state = when {\n            isRegistrationRole -> ClocktowerSquareTableSeatState.RegistrationHint\n            participatesInCountedPair -> ClocktowerSquareTableSeatState.SelectedHighlighted\n            seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation\n            else -> ClocktowerSquareTableSeatState.Neutral\n        },\n        badge = when {\n            isSpy -> if (language == "en") "S" else "间"\n            isRecluse -> if (language == "en") "R" else "隐"\n            else -> null\n        },\n        marker = if (isRegistrationRole && participatesInCountedPair) "★" else null,\n        isCurrentActor = seatNumber == actorSeat,\n    )\n}\n'''
text = replace_once(text, old_chef_visual, new_chef_visual, 'chef visual helper')
text = replace_once(
    text,
    '''internal fun clocktowerChefRecluseSeat(players: List<PlayerState>): Int? = players\n    .firstOrNull { player -> player.actualRole.value == "Recluse" }\n    ?.seat\n''',
    '''internal fun clocktowerChefSpySeat(players: List<PlayerState>): Int? = players\n    .firstOrNull { player -> player.actualRole.value == "Spy" }\n    ?.seat\n\ninternal fun clocktowerChefRecluseSeat(players: List<PlayerState>): Int? = players\n    .firstOrNull { player -> player.actualRole.value == "Recluse" }\n    ?.seat\n''',
    'chef spy seat helper',
)
text = replace_once(
    text,
    '''    actualEvilSeats: Set<Int>,\n    recluseSeat: Int?,\n    choices: List<ClocktowerChefResultChoice>,\n''',
    '''    actualEvilSeats: Set<Int>,\n    spySeat: Int?,\n    recluseSeat: Int?,\n    choices: List<ClocktowerChefResultChoice>,\n''',
    'chef dialog spy param',
)
text = replace_once(
    text,
    '''    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice.key) }\n    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice\n    val displayedPairSeats = clocktowerChefDisplayedPairSeats(choices, selectedChoice.key)\n''',
    '''    val displayedPairSeats = clocktowerChefDisplayedPairSeats(choices, initialChoice.key)\n''',
    'chef recommended witness state',
)
text = replace_once(
    text,
    '''                actualEvilSeats = actualEvilSeats,\n                recluseSeat = recluseSeat,\n                effectivePairSeats = displayedPairSeats,\n''',
    '''                actualEvilSeats = actualEvilSeats,\n                spySeat = spySeat,\n                recluseSeat = recluseSeat,\n                effectivePairSeats = displayedPairSeats,\n''',
    'chef visual call spy param',
)
text = replace_once(
    text,
    '''                badge = visual.badge,\n            )\n''',
    '''                badge = visual.badge,\n                stateMarkerOverride = visual.marker,\n            )\n''',
    'chef visual marker wiring',
)
chef_start = text.index('                    if (choices.size > 1) {')
chef_tail = '''                    ClocktowerExperiencedNumericChoiceRows(\n                        choices = choices,\n                        recommendedChoice = initialChoice,\n                        language = language,\n                        valueOf = ClocktowerChefResultChoice::value,\n                        onConfirm = onConfirm,\n                    )\n                }\n            }\n    }\n}\n'''
text = text[:chef_start] + chef_tail
write(path, text)


# Empath: only authoritative living-neighbour scope gets evidence; Spy/Recluse use special color.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTableUi.kt'
text = read(path)
text = replace_once(
    text,
    '''internal data class ClocktowerEmpathSeatVisual(\n    val state: ClocktowerSquareTableSeatState,\n    val badge: String?,\n    val isCurrentActor: Boolean,\n)\n''',
    '''internal data class ClocktowerEmpathSeatVisual(\n    val state: ClocktowerSquareTableSeatState,\n    val badge: String?,\n    val marker: String?,\n    val isCurrentActor: Boolean,\n)\n''',
    'empath visual model',
)
text = replace_once(
    text,
    '''internal fun clocktowerEmpathRecluseSeat(players: List<PlayerState>): Int? = players\n    .firstOrNull { player -> player.actualRole.value == "Recluse" }\n    ?.seat\n''',
    '''internal fun clocktowerEmpathSpySeat(players: List<PlayerState>): Int? = players\n    .firstOrNull { player -> player.actualRole.value == "Spy" }\n    ?.seat\n\ninternal fun clocktowerEmpathRecluseSeat(players: List<PlayerState>): Int? = players\n    .firstOrNull { player -> player.actualRole.value == "Recluse" }\n    ?.seat\n''',
    'empath spy seat helper',
)
old_empath_visual = '''internal fun clocktowerEmpathSeatVisual(\n    seatNumber: Int,\n    actorSeat: Int?,\n    scopeSeats: Set<Int>,\n    actualEvilSeats: Set<Int>,\n    recluseSeat: Int?,\n    contributingSeats: Set<Int>,\n    language: String,\n): ClocktowerEmpathSeatVisual {\n    val inScope = seatNumber in scopeSeats\n    val isRecluse = seatNumber == recluseSeat\n    val badge = when {\n        inScope && isRecluse -> if (language == "en") "N·R" else "邻·隐"\n        inScope -> if (language == "en") "N" else "邻"\n        isRecluse -> if (language == "en") "R" else "隐"\n        else -> null\n    }\n    return ClocktowerEmpathSeatVisual(\n        state = when {\n            seatNumber in contributingSeats -> ClocktowerSquareTableSeatState.SelectedHighlighted\n            seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation\n            else -> ClocktowerSquareTableSeatState.Neutral\n        },\n        badge = badge,\n        isCurrentActor = seatNumber == actorSeat,\n    )\n}\n'''
new_empath_visual = '''internal fun clocktowerEmpathSeatVisual(\n    seatNumber: Int,\n    actorSeat: Int?,\n    scopeSeats: Set<Int>,\n    actualEvilSeats: Set<Int>,\n    spySeat: Int?,\n    recluseSeat: Int?,\n    contributingSeats: Set<Int>,\n    language: String,\n): ClocktowerEmpathSeatVisual {\n    val inScope = seatNumber in scopeSeats\n    val isSpy = inScope && seatNumber == spySeat\n    val isRecluse = inScope && seatNumber == recluseSeat\n    val isRegistrationRole = isSpy || isRecluse\n    val contributes = seatNumber in contributingSeats\n    val badge = when {\n        isSpy -> if (language == "en") "N·S" else "邻·间"\n        isRecluse -> if (language == "en") "N·R" else "邻·隐"\n        inScope -> if (language == "en") "N" else "邻"\n        else -> null\n    }\n    return ClocktowerEmpathSeatVisual(\n        state = when {\n            !inScope -> ClocktowerSquareTableSeatState.Neutral\n            isRegistrationRole -> ClocktowerSquareTableSeatState.RegistrationHint\n            contributes -> ClocktowerSquareTableSeatState.SelectedHighlighted\n            seatNumber in actualEvilSeats -> ClocktowerSquareTableSeatState.HighlightedInformation\n            else -> ClocktowerSquareTableSeatState.Neutral\n        },\n        badge = badge,\n        marker = if (isRegistrationRole && contributes) "★" else null,\n        isCurrentActor = seatNumber == actorSeat,\n    )\n}\n'''
text = replace_once(text, old_empath_visual, new_empath_visual, 'empath visual helper')
text = replace_once(
    text,
    '''    actualEvilSeats: Set<Int>,\n    recluseSeat: Int?,\n    choices: List<ClocktowerEmpathResultChoice>,\n''',
    '''    actualEvilSeats: Set<Int>,\n    spySeat: Int?,\n    recluseSeat: Int?,\n    choices: List<ClocktowerEmpathResultChoice>,\n''',
    'empath dialog spy param',
)
text = replace_once(
    text,
    '''    var selectedKey by remember(choices.map { it.key }) { mutableStateOf(initialChoice.key) }\n    val selectedChoice = choices.firstOrNull { it.key == selectedKey } ?: initialChoice\n    val displayedContributionSeats = clocktowerEmpathDisplayedContributionSeats(choices, selectedChoice.key)\n''',
    '''    val displayedContributionSeats = clocktowerEmpathDisplayedContributionSeats(choices, initialChoice.key)\n''',
    'empath recommended witness state',
)
text = replace_once(
    text,
    '''                scopeSeats = selectedChoice.scopeSeats,\n                actualEvilSeats = actualEvilSeats,\n                recluseSeat = recluseSeat,\n                contributingSeats = displayedContributionSeats,\n''',
    '''                scopeSeats = initialChoice.scopeSeats,\n                actualEvilSeats = actualEvilSeats,\n                spySeat = spySeat,\n                recluseSeat = recluseSeat,\n                contributingSeats = displayedContributionSeats,\n''',
    'empath visual call recommendation',
)
text = replace_once(
    text,
    '''                badge = visual.badge,\n            )\n''',
    '''                badge = visual.badge,\n                stateMarkerOverride = visual.marker,\n            )\n''',
    'empath visual marker wiring',
)
empath_start = text.index('                    if (choices.size > 1) {')
empath_tail = '''                    ClocktowerExperiencedNumericChoiceRows(\n                        choices = choices,\n                        recommendedChoice = initialChoice,\n                        language = language,\n                        valueOf = ClocktowerEmpathResultChoice::value,\n                        onConfirm = onConfirm,\n                    )\n                }\n            }\n    }\n}\n'''
text = text[:empath_start] + empath_tail
write(path, text)


# Undertaker player display: sentence first, then unified large role reveal.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTableUi.kt'
text = read(path)
anchor = '''internal fun clocktowerUndertakerSeatVisual(\n    seatNumber: Int,\n    actorSeat: Int?,\n    executedSeat: Int,\n    language: String,\n): ClocktowerUndertakerSeatVisual = ClocktowerUndertakerSeatVisual(\n    state = if (seatNumber == executedSeat) {\n        ClocktowerSquareTableSeatState.HighlightedInformation\n    } else {\n        ClocktowerSquareTableSeatState.Neutral\n    },\n    badge = if (seatNumber == executedSeat) {\n        if (language == "en") "EX" else "处"\n    } else {\n        null\n    },\n    isCurrentActor = seatNumber == actorSeat,\n)\n'''
addition = anchor + '''\ninternal fun clocktowerUndertakerPlayerDisplayStep(\n    displayStep: ClocktowerNightStepUi,\n    choice: ClocktowerUndertakerResultChoice,\n    cards: List<PlayerCard>,\n    language: String,\n): ClocktowerNightStepUi {\n    val executedName = cards.getOrNull(choice.executedSeat - 1)?.name.orEmpty()\n    val executedLabel = if (language == "en") {\n        "P${choice.executedSeat} $executedName".trim()\n    } else {\n        "${choice.executedSeat}号 $executedName".trim()\n    }\n    val lead = if (language == "en") {\n        "Yesterday's executed player, $executedLabel, was"\n    } else {\n        "昨天被处决的 $executedLabel，身份是"\n    }\n    return displayStep.copy(\n        displayKind = ClocktowerDisplayKind.RoleReveal,\n        displayTitle = lead,\n        displayPrimary = choice.displayLabel,\n        displaySecondary = null,\n        displayFooter = null,\n    )\n}\n'''
text = replace_once(text, anchor, addition, 'undertaker display helper')
write(path, text)


# Demon succession compact guidance.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerSingleTargetInteractionUi.kt'
text = read(path)
text = replace_once(
    text,
    '''        helper = if (compact) {\n            null\n        } else if (isMayor) {\n''',
    '''        helper = if (compact) {\n            if (!isMayor) {\n                if (language == "en") "Choose the Minion who becomes the new Imp" else "选择继任小恶魔的爪牙"\n            } else {\n                null\n            }\n        } else if (isMayor) {\n''',
    'succession compact helper',
)
write(path, text)


# Large NightStep wiring: Spy seat and Undertaker display transform.
path = 'app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt'
text = read(path)
text = replace_once(
    text,
    '''    fun showRecommendedDisplayOption(option: ClocktowerDisplayOption) {\n''',
    '''    fun showRecommendedDisplayOption(\n        option: ClocktowerDisplayOption,\n        transformDisplayStep: (ClocktowerNightStepUi) -> ClocktowerNightStepUi = { it },\n    ) {\n''',
    'night-step display transform signature',
)
text = replace_once(
    text,
    '''        onShowPlayerDisplay(resolveClocktowerPlayerDisplay(step, option))\n    }\n''',
    '''        onShowPlayerDisplay(transformDisplayStep(resolveClocktowerPlayerDisplay(step, option)))\n    }\n''',
    'night-step display transform apply',
)
old_undertaker_show = '''    fun showUndertakerChoice(choice: ClocktowerUndertakerResultChoice) {\n        when (choice.sourceKind) {\n            ClocktowerUndertakerResultSourceKind.DisplayOption -> {\n                choice.displayOption?.let(::showRecommendedDisplayOption)\n            }\n\n            ClocktowerUndertakerResultSourceKind.LegacyUnreliable -> {\n                choice.displayOption?.let { option ->\n                    onShowPlayerDisplay(resolveClocktowerLegacyUnreliablePlayerDisplay(step, option))\n                }\n            }\n\n            ClocktowerUndertakerResultSourceKind.Direct -> onShowPlayerDisplay(step)\n        }\n    }\n'''
new_undertaker_show = '''    fun showUndertakerChoice(choice: ClocktowerUndertakerResultChoice) {\n        fun undertakerDisplay(displayStep: ClocktowerNightStepUi): ClocktowerNightStepUi =\n            clocktowerUndertakerPlayerDisplayStep(displayStep, choice, cards, language)\n\n        when (choice.sourceKind) {\n            ClocktowerUndertakerResultSourceKind.DisplayOption -> {\n                choice.displayOption?.let { option ->\n                    showRecommendedDisplayOption(option, ::undertakerDisplay)\n                }\n            }\n\n            ClocktowerUndertakerResultSourceKind.LegacyUnreliable -> {\n                choice.displayOption?.let { option ->\n                    onShowPlayerDisplay(undertakerDisplay(resolveClocktowerLegacyUnreliablePlayerDisplay(step, option)))\n                }\n            }\n\n            ClocktowerUndertakerResultSourceKind.Direct -> onShowPlayerDisplay(undertakerDisplay(step))\n        }\n    }\n'''
text = replace_once(text, old_undertaker_show, new_undertaker_show, 'undertaker show transform')
text = replace_once(
    text,
    '''                    actualEvilSeats = clocktowerChefActualEvilSeats(chefPlayers),\n                    recluseSeat = clocktowerChefRecluseSeat(chefPlayers),\n''',
    '''                    actualEvilSeats = clocktowerChefActualEvilSeats(chefPlayers),\n                    spySeat = clocktowerChefSpySeat(chefPlayers),\n                    recluseSeat = clocktowerChefRecluseSeat(chefPlayers),\n''',
    'chef spy wiring',
)
text = replace_once(
    text,
    '''                    actualEvilSeats = clocktowerEmpathActualEvilSeats(empathPlayers),\n                    recluseSeat = clocktowerEmpathRecluseSeat(empathPlayers),\n''',
    '''                    actualEvilSeats = clocktowerEmpathActualEvilSeats(empathPlayers),\n                    spySeat = clocktowerEmpathSpySeat(empathPlayers),\n                    recluseSeat = clocktowerEmpathRecluseSeat(empathPlayers),\n''',
    'empath spy wiring',
)
write(path, text)


# Pair truth-marker regression tests including typed Spy/Recluse registrations.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTablePresentationTest.kt'
text = read(path)
text = text.replace(
    '                actualRoleId = "Chef",\n            ),',
    '                actualRoleId = "Chef",\n                selectedOption = option("Chef", 1, 4),\n            ),',
)
text = text.replace(
    '                actualRoleId = "Empath",\n            ),',
    '                actualRoleId = "Empath",\n                selectedOption = option("Chef", 1, 4),\n            ),',
)
text = text.replace(
    '                actualRoleId = "Chef",\n            ),\n        )\n        assertNull(\n            clocktowerPairInformationTruthMarker(\n                isSelectedCandidate = true,\n                selectedRoleId = null,',
    '                actualRoleId = "Chef",\n                selectedOption = option("Chef", 1, 4),\n            ),\n        )\n        assertNull(\n            clocktowerPairInformationTruthMarker(\n                isSelectedCandidate = true,\n                selectedRoleId = null,',
    1,
)
text = replace_once(
    text,
    '''                selectedRoleId = null,\n                actualRoleId = "Chef",\n            ),\n        )\n    }\n''',
    '''                selectedRoleId = null,\n                actualRoleId = "Chef",\n                selectedOption = option("Chef", 1, 4),\n            ),\n        )\n    }\n\n    @Test\n    fun `truth marker includes typed Spy and Recluse registration hits`() {\n        val spyOption = option("Washerwoman", 2, 5).copy(\n            spyRegistersGood = true,\n            spyRegisteredRoleEnName = "Washerwoman",\n        )\n        val recluseOption = option("Poisoner", 3, 6).copy(\n            recluseRegistersEvil = true,\n            recluseRegisteredRoleEnName = "Poisoner",\n        )\n\n        assertEquals(\n            "✓",\n            clocktowerPairInformationTruthMarker(true, "Washerwoman", "Spy", spyOption),\n        )\n        assertEquals(\n            "✓",\n            clocktowerPairInformationTruthMarker(true, "Poisoner", "Recluse", recluseOption),\n        )\n        assertNull(clocktowerPairInformationTruthMarker(true, "Chef", "Spy", spyOption))\n        assertNull(clocktowerPairInformationTruthMarker(true, "Chef", "Recluse", recluseOption))\n    }\n''',
    'pair registration truth tests',
)
write(path, text)


# Chef presentation tests: signatures + special registration state.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerChefSquareTablePresentationTest.kt'
text = read(path)
text = text.replace('            recluseSeat = ', '            spySeat = null,\n            recluseSeat = ')
text = text.replace(
    '        assertEquals(ClocktowerSquareTableSeatState.Neutral, visualRecluse.state)\n',
    '        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, visualRecluse.state)\n',
)
text = text.replace(
    '        assertEquals(ClocktowerSquareTableSeatState.SelectedHighlighted, recluseVisual.state)\n',
    '        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, recluseVisual.state)\n        assertEquals("★", recluseVisual.marker)\n',
)
text = replace_once(
    text,
    '''        assertEquals(setOf(2), clocktowerChefEffectiveEvilSeats(players, option))\n        assertEquals(emptySet<Int>(), clocktowerChefEffectivePairSeats(players, option, value = 0))\n        assertEquals(setOf(2, 3), clocktowerChefActualEvilSeats(players))\n''',
    '''        assertEquals(setOf(2), clocktowerChefEffectiveEvilSeats(players, option))\n        assertEquals(emptySet<Int>(), clocktowerChefEffectivePairSeats(players, option, value = 0))\n        assertEquals(setOf(2, 3), clocktowerChefActualEvilSeats(players))\n        val spyVisual = clocktowerChefSeatVisual(\n            seatNumber = 3,\n            actorSeat = 1,\n            actualEvilSeats = setOf(2, 3),\n            spySeat = 3,\n            recluseSeat = null,\n            effectivePairSeats = emptySet(),\n            language = "zh",\n        )\n        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, spyVisual.state)\n        assertEquals("间", spyVisual.badge)\n''',
    'chef spy special test',
)
write(path, text)


# Empath presentation tests: signatures + scope-only highlighting + special registration state.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTablePresentationTest.kt'
text = read(path)
text = text.replace('            recluseSeat = ', '            spySeat = null,\n            recluseSeat = ')
text = text.replace(
    '        assertEquals(ClocktowerSquareTableSeatState.Neutral, recluse.state)\n',
    '        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, recluse.state)\n',
)
insert_after = '''        assertTrue(actor.isCurrentActor)\n        assertFalse(evil.isCurrentActor)\n'''
replacement = insert_after + '''\n        val unrelatedEvil = clocktowerEmpathSeatVisual(\n            seatNumber = 3,\n            actorSeat = 1,\n            scopeSeats = setOf(2, 5),\n            actualEvilSeats = setOf(2, 3),\n            spySeat = null,\n            recluseSeat = 5,\n            contributingSeats = emptySet(),\n            language = "zh",\n        )\n        assertEquals(ClocktowerSquareTableSeatState.Neutral, unrelatedEvil.state)\n        assertEquals(null, unrelatedEvil.badge)\n'''
text = replace_once(text, insert_after, replacement, 'empath unrelated evil test')
text = replace_once(
    text,
    '''        assertEquals(emptySet<Int>(), clocktowerEmpathContributingSeats(players, option, value = 0))\n        assertEquals(setOf(2), clocktowerEmpathActualEvilSeats(players))\n''',
    '''        assertEquals(emptySet<Int>(), clocktowerEmpathContributingSeats(players, option, value = 0))\n        assertEquals(setOf(2), clocktowerEmpathActualEvilSeats(players))\n        val spyVisual = clocktowerEmpathSeatVisual(\n            seatNumber = 2,\n            actorSeat = 1,\n            scopeSeats = setOf(2, 3),\n            actualEvilSeats = setOf(2),\n            spySeat = 2,\n            recluseSeat = null,\n            contributingSeats = emptySet(),\n            language = "zh",\n        )\n        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, spyVisual.state)\n        assertEquals("邻·间", spyVisual.badge)\n''',
    'empath spy special test',
)
write(path, text)


# Undertaker unified reveal copy contract.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTablePresentationTest.kt'
text = read(path)
anchor = '''    @Test\n    fun `executed context and current actor are independent visual dimensions`() {\n'''
new_test = '''    @Test\n    fun `player reveal leads with executed player and shows role as primary`() {\n        val choice = ClocktowerUndertakerResultChoice(\n            key = "chef",\n            executedSeat = 4,\n            roleId = RoleId("Chef"),\n            displayLabel = "厨师",\n            sourceKind = ClocktowerUndertakerResultSourceKind.Direct,\n        )\n        val cards = List(7) { index -> PlayerCard(name = "P${index + 1}") }\n        val display = clocktowerUndertakerPlayerDisplayStep(undertakerStep(), choice, cards, "zh")\n\n        assertEquals("昨天被处决的 4号 P4，身份是", display.displayTitle)\n        assertEquals("厨师", display.displayPrimary)\n        assertNull(display.displayFooter)\n    }\n\n''' + anchor
text = replace_once(text, anchor, new_test, 'undertaker reveal test')
write(path, text)

print('Experienced UI 3 device feedback patch applied successfully')
