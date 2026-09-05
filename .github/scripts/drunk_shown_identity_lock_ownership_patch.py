from pathlib import Path

path = Path(
    "app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt"
)
raw = path.read_bytes()

if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

replacements = [
    (
        """import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
""",
        """import com.codex.campboardgamehost.clocktower.recommendation.RecommendationUiState
import com.codex.campboardgamehost.clocktower.recommendation.SetupRecommendationLockPolicy
import com.codex.campboardgamehost.clocktower.recommendation.WeightedStableSelector
""",
    ),
    (
        """    val recommendationCards = cards.toList()
    val committedIdentityDecisions = cards.firstOrNull { it.clocktowerRole?.enName == \"Drunk\" }
        ?.clocktowerShownRole
        ?.let { shownRole ->
            buildList<StorytellerDecision> {
                add(StorytellerDecision.DrunkShownRole(RoleId(shownRole.enName)))
            }
        }
        .orEmpty()
    fun preservingCommittedIdentity(decisions: List<StorytellerDecision>): List<StorytellerDecision> {
        val committedKinds = committedIdentityDecisions.mapTo(hashSetOf(), StorytellerDecision::kind)
        return committedIdentityDecisions + decisions.filterNot { it.kind() in committedKinds }
    }
    var recommendationUiState by remember(recommendationKey) {
""",
        """    val recommendationCards = cards.toList()
    var recommendationUiState by remember(recommendationKey) {
""",
    ),
    (
        """    var lockedRecommendationDecisions by remember(recommendationKey) {
        mutableStateOf(committedIdentityDecisions)
    }
""",
        """    var lockedRecommendationDecisions by remember(recommendationKey) {
        mutableStateOf(SetupRecommendationLockPolicy.initialLocks())
    }
""",
    ),
    (
        """                onReevaluate = { nextLockedDecisions ->
                    lockedRecommendationDecisions = preservingCommittedIdentity(nextLockedDecisions)
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                    appliedRecommendationStyle = null
                },
                onClearLocks = {
                    lockedRecommendationDecisions = committedIdentityDecisions
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                },
""",
        """                onReevaluate = { nextLockedDecisions ->
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.replaceWith(nextLockedDecisions)
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                    appliedRecommendationStyle = null
                },
                onClearLocks = {
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.clear()
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                },
""",
    ),
]

counts = [text.count(old) for old, _ in replacements]
for index, count in enumerate(counts, start=1):
    if count != 1:
        raise SystemExit(f"Expected exactly one patch anchor #{index}, found {count}")

for old, new in replacements:
    text = text.replace(old, new, 1)

for index, (old, _) in enumerate(replacements, start=1):
    if old in text:
        raise SystemExit(f"Original patch anchor #{index} remains after replacement")

required = [
    "SetupRecommendationLockPolicy.initialLocks()",
    "SetupRecommendationLockPolicy.replaceWith(nextLockedDecisions)",
    "SetupRecommendationLockPolicy.clear()",
]
for token in required:
    if token not in text:
        raise SystemExit(f"Required semantic token missing after patch: {token}")

for forbidden in [
    "val committedIdentityDecisions =",
    "fun preservingCommittedIdentity(",
    "mutableStateOf(committedIdentityDecisions)",
    "lockedRecommendationDecisions = committedIdentityDecisions",
]:
    if forbidden in text:
        raise SystemExit(f"Forbidden legacy lock-ownership token remains: {forbidden}")

path.write_text(text, encoding="utf-8", newline="\n")
