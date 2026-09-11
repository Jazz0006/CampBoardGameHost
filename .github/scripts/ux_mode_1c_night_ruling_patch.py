from pathlib import Path

REGISTRATION = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationUi.kt")
NIGHT_STEP = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")


def read_lf(path: Path) -> str:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected CRLF/CR in {path}")
    return raw.decode("utf-8")


def replace_exact(source: str, label: str, old: str, new: str, expected_count: int = 1) -> str:
    count = source.count(old)
    if count != expected_count:
        raise SystemExit(f"{label}: expected {expected_count} anchor(s), found {count}")
    return source.replace(old, new)


registration = read_lf(REGISTRATION)

spy_marker = "internal fun SpyRegistrationPanel("
recluse_marker = "internal fun RecluseRegistrationPanel("
if registration.count(spy_marker) != 1 or registration.count(recluse_marker) != 1:
    raise SystemExit("Expected exactly one Spy and one Recluse registration panel")
spy_start = registration.index(spy_marker)
recluse_start = registration.index(recluse_marker)
prefix = registration[:spy_start]
spy = registration[spy_start:recluse_start]
recluse = registration[recluse_start:]

for label, source_name in (("spy", "spy"), ("recluse", "recluse")):
    target = spy if label == "spy" else recluse
    target = replace_exact(
        target,
        f"{label} stable automatic decision key parameter",
        """    selectionAudit: SelectionAuditContext? = null,
    enabled: Boolean,
""",
        """    selectionAudit: SelectionAuditContext? = null,
    automaticDecisionKey: String? = null,
    enabled: Boolean,
""",
    )
    old_seed = (
        """        val decisionKey = selectionAudit?.selectionId
            ?: \"spy-registration-fallback:${spy.name}:${roles.map { it.enName }.sorted().joinToString(\",\")}\"
"""
        if label == "spy"
        else
        """        val decisionKey = selectionAudit?.selectionId
            ?: \"recluse-registration-fallback:${recluse.name}:${roles.map { it.enName }.sorted().joinToString(\",\")}\"
"""
    )
    new_seed = (
        """        val decisionKey = automaticDecisionKey
            ?: \"spy-registration-fallback:${spy.name}:${roles.map { it.enName }.sorted().joinToString(\",\")}\"
"""
        if label == "spy"
        else
        """        val decisionKey = automaticDecisionKey
            ?: \"recluse-registration-fallback:${recluse.name}:${roles.map { it.enName }.sorted().joinToString(\",\")}\"
"""
    )
    target = replace_exact(target, f"{label} revision-free seed source", old_seed, new_seed)
    target = replace_exact(
        target,
        f"{label} stable key effect identity",
        "LaunchedEffect(automaticStorytellerInfo, enabled, automaticRuling, selectionAudit?.selectionId)",
        "LaunchedEffect(automaticStorytellerInfo, enabled, automaticRuling, automaticDecisionKey, selectionAudit?.selectionId)",
    )
    if label == "spy":
        spy = target
    else:
        recluse = target

registration = prefix + spy + recluse
if registration.count("automaticDecisionKey: String? = null") != 2:
    raise SystemExit("Expected stable automatic decision key on both registration panels")
if "val decisionKey = selectionAudit?.selectionId" in registration:
    raise SystemExit("Registration automatic seed still depends on mutable selection audit identity")
REGISTRATION.write_text(registration, encoding="utf-8", newline="\n")

night = read_lf(NIGHT_STEP)
night = replace_exact(
    night,
    "remove dynamic ruling style selector import",
    "import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector\n",
    "",
)

night = replace_exact(
    night,
    "replace legacy dynamic automatic ruling selector",
    """    val dynamicDecisionFamily = when (step.action) {
        ClocktowerNightAction.MayorRedirect -> \"mayor-redirect\"
        ClocktowerNightAction.DemonSuccessor -> \"demon-succession\"
        else -> null
    }
    val dynamicDecisionPool = dynamicDecisionFamily
        ?.let { family -> unifiedDecisionPool(step.decisionOptions, family) }
    val automaticDecisionOptions = dynamicDecisionPool
        ?.candidatesFor(SelectionExecutionPolicy.AUTO)
        ?.map { it.payload }
        ?: step.decisionOptions
    val assistedDecisionOptions = dynamicDecisionPool
        ?.candidatesFor(SelectionExecutionPolicy.ASSISTED)
        ?.map { it.payload }
        ?: step.decisionOptions
    val automaticDecision = WeightedStableSelector.selectStyle(
        automaticDecisionOptions,
        automaticStorytellerStyle,
        ClocktowerDecisionOption::recommendationStyle,
    )
    val automaticDecisionTargetName = automaticDecision?.targetName
        ?: demonSuccessorTargetCards.singleOrNull()?.takeIf {
            step.action == ClocktowerNightAction.DemonSuccessor
        }?.name
""",
    """    fun seatNumberForAutomaticCandidate(card: PlayerCard): Int? = cards
        .indexOfFirst { candidate -> candidate.name == card.name }
        .takeIf { index -> index >= 0 }
        ?.plus(1)
    val automaticMayorTargetName = if (
        automaticStorytellerInfo &&
        step.isRealAction &&
        step.action == ClocktowerNightAction.MayorRedirect
    ) {
        val mayor = aliveCards.firstOrNull { card -> card.clocktowerRole?.enName == \"Mayor\" }
        val mayorSeat = mayor?.let(::seatNumberForAutomaticCandidate)
        mayorSeat?.let { resolvedMayorSeat ->
            val livingTownsfolkSeats = clocktowerTemporaryMayorEligibleTownsfolkSeats(
                candidates = mayorRedirectTargetCards.mapNotNull { candidate ->
                    val candidateSeat = seatNumberForAutomaticCandidate(candidate) ?: return@mapNotNull null
                    val candidateTeam = candidate.clocktowerTeam ?: return@mapNotNull null
                    ClocktowerTemporaryMayorCandidate(
                        seat = candidateSeat,
                        team = candidateTeam,
                        alive = candidate.eliminatedRound == null,
                    )
                },
                mayorSeat = resolvedMayorSeat,
            )
            val selectedSeat = clocktowerTemporaryMayorSelection(
                mayorSeat = resolvedMayorSeat,
                livingTownsfolkSeats = livingTownsfolkSeats,
                decisionKey = clocktowerTemporaryNightDecisionKey(
                    gameId = gameId,
                    phase = phase,
                    round = round,
                    sequence = sequence,
                    family = \"mayor-redirect\",
                ),
            ).selected.payload
            cards.getOrNull(selectedSeat - 1)?.name
        }
    } else {
        null
    }
    val automaticDemonSuccessorTargetName = if (
        automaticStorytellerInfo &&
        step.isRealAction &&
        step.action == ClocktowerNightAction.DemonSuccessor
    ) {
        clocktowerTemporaryDemonSuccessorSelection(
            eligible = demonSuccessorTargetCards.mapNotNull { candidate ->
                val candidateSeat = seatNumberForAutomaticCandidate(candidate) ?: return@mapNotNull null
                val roleEnName = candidate.clocktowerRole?.enName ?: return@mapNotNull null
                temporaryDemonSuccessorChoice(candidateSeat, roleEnName)
            },
            decisionKey = clocktowerTemporaryNightDecisionKey(
                gameId = gameId,
                phase = phase,
                round = round,
                sequence = sequence,
                family = \"demon-succession\",
            ),
        )?.selected?.payload?.seat?.let { seat -> cards.getOrNull(seat - 1)?.name }
    } else {
        null
    }
    val automaticDecisionTargetName = when (step.action) {
        ClocktowerNightAction.MayorRedirect -> automaticMayorTargetName
        ClocktowerNightAction.DemonSuccessor -> automaticDemonSuccessorTargetName
        else -> null
    }
""",
)

night = replace_exact(
    night,
    "spy stable automatic decision key wiring",
    """                    selectionAudit = selectionAudit?.copy(selectionId = \"$informationDecisionKey|spy-registration\"),
                    onRegistersGoodChange = onSpyRegistrationGoodChange,
""",
    """                    selectionAudit = selectionAudit?.copy(selectionId = \"$informationDecisionKey|spy-registration\"),
                    automaticDecisionKey = clocktowerTemporaryNightDecisionKey(
                        gameId = gameId,
                        phase = phase,
                        round = round,
                        sequence = sequence,
                        family = \"spy-registration:${step.spyRegistrationKey}\",
                    ),
                    onRegistersGoodChange = onSpyRegistrationGoodChange,
""",
)

night = replace_exact(
    night,
    "recluse stable automatic decision key wiring",
    """                    selectionAudit = selectionAudit?.copy(selectionId = \"$informationDecisionKey|recluse-registration\"),
                    onRegistersEvilChange = onRecluseRegistrationEvilChange,
""",
    """                    selectionAudit = selectionAudit?.copy(selectionId = \"$informationDecisionKey|recluse-registration\"),
                    automaticDecisionKey = clocktowerTemporaryNightDecisionKey(
                        gameId = gameId,
                        phase = phase,
                        round = round,
                        sequence = sequence,
                        family = \"recluse-registration:${step.recluseRegistrationKey}\",
                    ),
                    onRegistersEvilChange = onRecluseRegistrationEvilChange,
""",
)

night = replace_exact(
    night,
    "experienced-only dynamic ruling controls",
    """            if (step.decisionOptions.isNotEmpty()) {
                HostActionSection(
                    title = if (language == \"en\") \"Recommended ruling\" else \"推荐裁定\",
                    helper = if (automaticStorytellerInfo) {
                        if (language == \"en\") \"The selected automatic ruling has been applied.\" else \"已采用当前自动模式的裁定。\"
                    } else if (language == \"en\") {
                        \"The balanced option is the beginner default. You can still choose manually below.\"
                    } else {
                        if (language == \"en\") \"The balanced option is the beginner default; you can still choose manually below.\" else \"平衡方案是新手默认建议；仍可在下方手动裁定。\"
                    },
                ) {
                    step.decisionOptions
                        .filter { !automaticStorytellerInfo || it == automaticDecision }
                        .forEach { option ->
                        if (automaticStorytellerInfo) {
                            Text(option.label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else
                        if (automaticStorytellerInfo || option.isDefaultRecommendation) {
                            Button(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        }
                    }
                }
            }
""",
    """            if (step.decisionOptions.isNotEmpty() && !automaticStorytellerInfo) {
                HostActionSection(
                    title = if (language == \"en\") \"Recommended ruling\" else \"推荐裁定\",
                    helper = if (language == \"en\") {
                        \"Choose a recommendation below, or adjust the ruling manually on the table.\"
                    } else {
                        \"可采用下方推荐，也可在方桌中手动调整裁定。\"
                    },
                ) {
                    step.decisionOptions.forEach { option ->
                        if (option.isDefaultRecommendation) {
                            Button(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onSelectName(option.targetName) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(option.label)
                            }
                        }
                    }
                }
            }
""",
)

for forbidden in (
    "WeightedStableSelector",
    "automaticDecisionOptions",
    "assistedDecisionOptions",
    "val automaticDecision =",
    "The balanced option is the beginner default",
    "平衡方案是新手默认建议",
    "The selected automatic ruling has been applied.",
    "已采用当前自动模式的裁定。",
):
    if forbidden in night:
        raise SystemExit(f"Legacy dynamic automatic ruling token remains: {forbidden}")

required = (
    "clocktowerTemporaryMayorSelection(",
    "clocktowerTemporaryDemonSuccessorSelection(",
    "family = \"mayor-redirect\"",
    "family = \"demon-succession\"",
    "automaticDecisionKey = clocktowerTemporaryNightDecisionKey(",
    "step.decisionOptions.isNotEmpty() && !automaticStorytellerInfo",
)
for token in required:
    if token not in night:
        raise SystemExit(f"Missing UX-MODE-1C night ruling token: {token}")

NIGHT_STEP.write_text(night, encoding="utf-8", newline="\n")
