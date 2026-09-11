from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationUi.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in ClocktowerRegistrationUi.kt")
text = raw.decode("utf-8")


def replace_exact(label: str, old: str, new: str, expected_count: int = 1) -> None:
    global text
    count = text.count(old)
    if count != expected_count:
        raise SystemExit(f"{label}: expected {expected_count} anchor(s), found {count}")
    text = text.replace(old, new)


replace_exact(
    "remove legacy style selector import",
    "import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector\n",
    "",
)

replace_exact(
    "spy automatic registration selection",
    """    val automaticRecommendation = WeightedStableSelector.selectStyle(
        registrationPool?.candidatesFor(SelectionExecutionPolicy.AUTO)?.map { it.payload }.orEmpty(),
        automaticStorytellerStyle,
        ClocktowerRegistrationRecommendationOption::style,
    )
    val automaticStyleLabel = when (automaticStorytellerStyle) {
        RecommendationStyle.GENTLE -> if (language == \"en\") \"gentle\" else \"稳健\"
        RecommendationStyle.BALANCED -> if (language == \"en\") \"balanced\" else \"均衡\"
        RecommendationStyle.AGGRESSIVE -> if (language == \"en\") \"aggressive\" else \"激进\"
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRecommendation, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRecommendation != null) {
""",
    """    val automaticRuling = if (automaticStorytellerInfo && enabled) {
        val decisionKey = selectionAudit?.selectionId
            ?: \"spy-registration-fallback:${spy.name}:${roles.map { it.enName }.sorted().joinToString(\",\")}\"
        clocktowerTemporaryRegistrationSelection(
            legalSpecialRoleEnNames = roles.map { it.enName },
            decisionKey = decisionKey,
        ).selected.payload
    } else {
        null
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRuling, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRuling != null) {
""",
)

replace_exact(
    "spy automatic registration application",
    """                        selectedFamilyId = if (automaticRecommendation.usesSpecialRegistration) \"special-registration\" else \"actual-registration\",
                    ),
                )
            }
            onRegistersGoodChange(automaticRecommendation.usesSpecialRegistration)
            if (automaticRecommendation.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                automaticRecommendation.registeredRoleEnName?.let(onRoleChange)
            }
""",
    """                        selectedFamilyId = if (automaticRuling.usesSpecialRegistration) \"special-registration\" else \"actual-registration\",
                    ),
                )
            }
            onRegistersGoodChange(automaticRuling.usesSpecialRegistration)
            if (automaticRuling.usesSpecialRegistration && detail == ClocktowerRegistrationDetail.Role) {
                automaticRuling.registeredRoleEnName?.let(onRoleChange)
            }
""",
)

replace_exact(
    "spy automatic registration presentation",
    """            if (automaticStorytellerInfo && automaticRecommendation != null) {
                Text(
                    if (language == \"en\") \"Automatic $automaticStyleLabel ruling\" else \"已自动采用${automaticStyleLabel}裁定\",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(automaticRecommendation.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RecommendationReasonSummary(
                    automaticRecommendation.reasonCodes,
                    automaticRecommendation.warningCodes,
                    language,
                )
""",
    """            if (automaticStorytellerInfo && automaticRuling != null) {
                Text(
                    if (language == \"en\") \"System ruling applied\" else \"系统已自动裁定\",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                val registeredRoleLabel = automaticRuling.registeredRoleEnName?.let { selectedRole ->
                    roles.firstOrNull { it.enName == selectedRole }?.nameFor(language) ?: selectedRole
                }
                Text(
                    when {
                        !automaticRuling.usesSpecialRegistration -> if (language == \"en\") \"Use actual identity\" else \"按真实身份登记\"
                        detail == ClocktowerRegistrationDetail.Role && registeredRoleLabel != null -> {
                            if (language == \"en\") \"Register as $registeredRoleLabel\" else \"登记为 $registeredRoleLabel\"
                        }
                        else -> if (language == \"en\") \"Register as good\" else \"登记为善良\"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
""",
)

replace_exact(
    "recluse automatic registration selection",
    """    val automaticRecommendation = WeightedStableSelector.selectStyle(
        registrationPool?.candidatesFor(SelectionExecutionPolicy.AUTO)?.map { it.payload }.orEmpty(),
        automaticStorytellerStyle,
        ClocktowerRegistrationRecommendationOption::style,
    )
    val automaticStyleLabel = when (automaticStorytellerStyle) {
        RecommendationStyle.GENTLE -> if (language == \"en\") \"gentle\" else \"稳健\"
        RecommendationStyle.BALANCED -> if (language == \"en\") \"balanced\" else \"均衡\"
        RecommendationStyle.AGGRESSIVE -> if (language == \"en\") \"aggressive\" else \"激进\"
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRecommendation, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRecommendation != null) {
""",
    """    val automaticRuling = if (automaticStorytellerInfo && enabled) {
        val decisionKey = selectionAudit?.selectionId
            ?: \"recluse-registration-fallback:${recluse.name}:${roles.map { it.enName }.sorted().joinToString(\",\")}\"
        clocktowerTemporaryRegistrationSelection(
            legalSpecialRoleEnNames = roles.map { it.enName },
            decisionKey = decisionKey,
        ).selected.payload
    } else {
        null
    }
    LaunchedEffect(automaticStorytellerInfo, enabled, automaticRuling, selectionAudit?.selectionId) {
        if (automaticStorytellerInfo && enabled && automaticRuling != null) {
""",
)

replace_exact(
    "recluse automatic registration application",
    """                        selectedFamilyId = if (automaticRecommendation.usesSpecialRegistration) \"special-registration\" else \"actual-registration\",
                    ),
                )
            }
            onRegistersEvilChange(automaticRecommendation.usesSpecialRegistration)
            if (automaticRecommendation.usesSpecialRegistration) {
                automaticRecommendation.registeredRoleEnName?.let(onRoleChange)
            }
""",
    """                        selectedFamilyId = if (automaticRuling.usesSpecialRegistration) \"special-registration\" else \"actual-registration\",
                    ),
                )
            }
            onRegistersEvilChange(automaticRuling.usesSpecialRegistration)
            if (automaticRuling.usesSpecialRegistration) {
                automaticRuling.registeredRoleEnName?.let(onRoleChange)
            }
""",
)

replace_exact(
    "recluse automatic registration presentation",
    """            if (automaticStorytellerInfo && automaticRecommendation != null) {
                Text(
                    if (language == \"en\") \"Automatic $automaticStyleLabel ruling\" else \"已自动采用${automaticStyleLabel}裁定\",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(automaticRecommendation.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RecommendationReasonSummary(
                    automaticRecommendation.reasonCodes,
                    automaticRecommendation.warningCodes,
                    language,
                )
""",
    """            if (automaticStorytellerInfo && automaticRuling != null) {
                Text(
                    if (language == \"en\") \"System ruling applied\" else \"系统已自动裁定\",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                val registeredRoleLabel = automaticRuling.registeredRoleEnName?.let { selectedRole ->
                    roles.firstOrNull { it.enName == selectedRole }?.nameFor(language) ?: selectedRole
                }
                Text(
                    if (!automaticRuling.usesSpecialRegistration) {
                        if (language == \"en\") \"Use actual identity\" else \"按真实身份登记\"
                    } else if (registeredRoleLabel != null) {
                        if (language == \"en\") \"Register as $registeredRoleLabel\" else \"登记为 $registeredRoleLabel\"
                    } else {
                        if (language == \"en\") \"Register as evil\" else \"登记为邪恶\"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
""",
)

if "WeightedStableSelector" in text:
    raise SystemExit("legacy style selector remains in registration UI")
if "automaticStyleLabel" in text or "Automatic $automaticStyleLabel ruling" in text:
    raise SystemExit("legacy style wording remains in registration UI")
if text.count("clocktowerTemporaryRegistrationSelection(") != 2:
    raise SystemExit("expected both Spy and Recluse to use temporary registration policy")

TARGET.write_text(text, encoding="utf-8", newline="\n")
