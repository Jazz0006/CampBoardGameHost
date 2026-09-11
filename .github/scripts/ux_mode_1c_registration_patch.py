from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerRegistrationUi.kt")
raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected CRLF/CR in ClocktowerRegistrationUi.kt")
text = raw.decode("utf-8")


def replace_exact(source: str, label: str, old: str, new: str, expected_count: int = 1) -> str:
    count = source.count(old)
    if count != expected_count:
        raise SystemExit(f"{label}: expected {expected_count} anchor(s), found {count}")
    return source.replace(old, new)


text = replace_exact(
    text,
    "remove legacy style selector import",
    "import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector\n",
    "",
)

spy_marker = "internal fun SpyRegistrationPanel("
recluse_marker = "internal fun RecluseRegistrationPanel("
if text.count(spy_marker) != 1 or text.count(recluse_marker) != 1:
    raise SystemExit("Expected exactly one Spy and one Recluse registration panel")
spy_start = text.index(spy_marker)
recluse_start = text.index(recluse_marker)
if spy_start >= recluse_start:
    raise SystemExit("Unexpected registration panel order")
prefix = text[:spy_start]
spy = text[spy_start:recluse_start]
recluse = text[recluse_start:]

common_old_selection = """    val automaticRecommendation = WeightedStableSelector.selectStyle(
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
"""

spy = replace_exact(
    spy,
    "spy automatic registration selection",
    common_old_selection,
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

spy = replace_exact(
    spy,
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

spy = replace_exact(
    spy,
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

recluse = replace_exact(
    recluse,
    "recluse automatic registration selection",
    common_old_selection,
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

recluse = replace_exact(
    recluse,
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

recluse = replace_exact(
    recluse,
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

text = prefix + spy + recluse
if "WeightedStableSelector" in text:
    raise SystemExit("legacy style selector remains in registration UI")
if "automaticStyleLabel" in text or "Automatic $automaticStyleLabel ruling" in text:
    raise SystemExit("legacy style wording remains in registration UI")
if text.count("clocktowerTemporaryRegistrationSelection(") != 2:
    raise SystemExit("expected both Spy and Recluse to use temporary registration policy")

TARGET.write_text(text, encoding="utf-8", newline="\n")
