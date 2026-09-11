from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")

raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")

text = raw.decode("utf-8")

replacements = [
    (
        """    var selectedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf(RecommendationStyle.BALANCED)
    }
""",
        """    var selectedRecommendationStyle by remember(recommendationKey) {
        mutableStateOf(automaticStorytellerStyle)
    }
""",
    ),
    (
        """                onReevaluate = { nextLockedDecisions ->
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.replaceWith(nextLockedDecisions)
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                    appliedRecommendationStyle = null
                },
""",
        """                onReevaluate = { nextLockedDecisions ->
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.replaceWith(nextLockedDecisions)
                    selectedRecommendationStyle = automaticStorytellerStyle
                    appliedRecommendationStyle = null
                },
""",
    ),
    (
        """                onClearLocks = {
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.clear()
                    selectedRecommendationStyle = RecommendationStyle.BALANCED
                },
""",
        """                onClearLocks = {
                    lockedRecommendationDecisions = SetupRecommendationLockPolicy.clear()
                    selectedRecommendationStyle = automaticStorytellerStyle
                },
""",
    ),
]

counts = [text.count(old) for old, _ in replacements]
if counts != [1, 1, 1]:
    raise SystemExit(f"Expected each setup-style patch anchor exactly once, found {counts}")

patched = text
for old, new in replacements:
    patched = patched.replace(old, new, 1)

for old, _ in replacements:
    if old in patched:
        raise SystemExit("Original setup-style patch anchor remains after replacement")

if patched.count("mutableStateOf(automaticStorytellerStyle)") != 1:
    raise SystemExit("Expected exactly one automatic setup-style initial state")
if patched.count("selectedRecommendationStyle = automaticStorytellerStyle") != 2:
    raise SystemExit("Expected exactly two automatic setup-style resets")
if patched.count("fun recommendedDrunkInvestigatorOption(actor: PlayerCard): ClocktowerDisplayOption?") != 1:
    raise SystemExit("Drunk Investigator recommendation helper must remain intact")
if patched.count("listOfNotNull(recommendedDrunkInvestigatorOption(actor))") != 1:
    raise SystemExit("Drunk Investigator recommendation helper call must remain intact")

TARGET.write_text(patched, encoding="utf-8", newline="\n")
