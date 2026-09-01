from pathlib import Path
import subprocess

BASE_HEAD = "458a71d34538389252fd6efac8d1820a54654285"
TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
TARGET_BLOB = "7e7b382211bd9fd38f7703efd059623e184c77db"
TEST = Path("app/src/test/java/com/codex/campboardgamehost/StructuredEmpathInformationAdapterTest.kt")
TEST_BLOB = "258aa012b1b74838b0126532a31fe6406b657a00"
SCRIPT = "tools/oneshot_patch_458a71d.py"
WORKFLOW = ".github/workflows/oneshot_patch_458a71d.yml"
FOCUSED = [
    "./gradlew",
    ":app:testDebugUnitTest",
    "--tests",
    "com.codex.campboardgamehost.ClocktowerHostSelectionSemanticsCharacterizationTest",
    "--no-daemon",
]
STRUCTURED = [
    "./gradlew",
    ":app:testDebugUnitTest",
    "--tests",
    "com.codex.campboardgamehost.StructuredEmpathInformationAdapterTest",
    "--no-daemon",
]


def run(*args: str) -> str:
    print("+", " ".join(args), flush=True)
    return subprocess.check_output(args, text=True).strip()


def check(*args: str) -> None:
    print("+", " ".join(args), flush=True)
    subprocess.check_call(args)


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one anchor, found {count}")
    return text.replace(old, new, 1)


# The retrigger commit is exactly four temporary commits above the accepted
# product base: initial script/workflow plus this script/workflow refinement.
actual_base = run("git", "rev-parse", "HEAD~4")
if actual_base != BASE_HEAD:
    raise SystemExit(f"base drift: expected {BASE_HEAD}, got {actual_base}")
for path, expected_blob in ((TARGET, TARGET_BLOB), (TEST, TEST_BLOB)):
    actual_blob = run("git", "hash-object", str(path))
    if actual_blob != expected_blob:
        raise SystemExit(f"target drift for {path}: expected blob {expected_blob}, got {actual_blob}")

# T0 on the unmodified product tree.
check(*FOCUSED)
check(*STRUCTURED)

text = TARGET.read_text(encoding="utf-8")
text = replace_once(
    text,
    "import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle\nimport com.codex.campboardgamehost.clocktower.domain.StorytellerPhase\n",
    "import com.codex.campboardgamehost.clocktower.domain.RecommendationStyle\nimport com.codex.campboardgamehost.clocktower.domain.StorytellerPhase\nimport com.codex.campboardgamehost.clocktower.domain.toClocktowerPlayerStates\n",
    "domain adapter import",
)
text = replace_once(
    text,
    "import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator\nimport com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext\n",
    "import com.codex.campboardgamehost.clocktower.recommendation.dynamic.DynamicCandidateGenerator\nimport com.codex.campboardgamehost.clocktower.recommendation.dynamic.InformationReliability\nimport com.codex.campboardgamehost.clocktower.recommendation.dynamic.SelectionAuditContext\n",
    "reliability import",
)
text = replace_once(
    text,
    """    // B7.3's first production slice: a single complete first-night pool is
    // projected differently by execution policy. Later-night families retain
    // their legacy lists until individually migrated.
    val firstNightPool = step.legacyInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: \"first-night-information\",
            automaticStyle = automaticStorytellerStyle,
        ) }
""",
    """    // B7.3's first production slice: a single complete first-night pool is
    // projected differently by execution policy. Later-night families retain
    // their legacy lists until individually migrated.
    val firstNightNumericSourceSeat = step.legacyInformationCandidates
        .asSequence()
        .mapNotNull { (it.proposition as? InformationProposition.NumericResult)?.sourceSeat }
        .firstOrNull()
    val projectedFirstNightInformationCandidates = firstNightNumericSourceSeat?.let { sourceSeat ->
        val poisonedPlayerName = cards
            .getOrNull(sourceSeat - 1)
            ?.name
            ?.takeIf { step.informationReliability == InformationReliability.POISONED }
        projectFirstNightNumericInformationOptions(
            phase = phase,
            roleEnName = step.roleEnName.orEmpty(),
            sourceSeat = sourceSeat,
            players = cards.toClocktowerPlayerStates(poisonedPlayerName = poisonedPlayerName),
            options = step.legacyInformationCandidates,
        )
    } ?: step.legacyInformationCandidates
    val firstNightPool = projectedFirstNightInformationCandidates
        .takeIf { phase == ClocktowerPhase.FirstNight && it.isNotEmpty() }
        ?.let { options -> unifiedFirstNightInformationPool(
            options = options,
            familyId = step.roleEnName ?: \"first-night-information\",
            automaticStyle = automaticStorytellerStyle,
        ) }
""",
    "project before unified first-night pool",
)
text = replace_once(
    text,
    """        step.legacyInformationCandidates,
        automaticStorytellerStyle,
""",
    """        projectedFirstNightInformationCandidates,
        automaticStorytellerStyle,
""",
    "benchmark remember key",
)
text = replace_once(
    text,
    """            val options = requireNotNull(step.legacyInformationCandidates.takeIf {
                phase == ClocktowerPhase.FirstNight && it.isNotEmpty()
            })
""",
    """            val options = requireNotNull(projectedFirstNightInformationCandidates.takeIf {
                phase == ClocktowerPhase.FirstNight && it.isNotEmpty()
            })
""",
    "benchmark projected pool",
)
text = replace_once(
    text,
    """    val structuredEmpathTruthValue = step
        .takeIf { it.roleEnName == \"Empath\" }
        ?.legacyInformationCandidates
        ?.firstOrNull { it.isTruthful }
        ?.let(::numericOptionValue)
        ?: (step.displayProposition as? InformationProposition.NumericResult)?.value
        ?: step.tellPlayer?.toIntOrNull()
""",
    """    val structuredEmpathTruthValue = step
        .takeIf { it.roleEnName == \"Empath\" }
        ?.legacyInformationCandidates
        ?.firstOrNull { it.isTruthful }
        ?.let(::numericOptionValue)
        ?: (step.displayProposition as? InformationProposition.NumericResult)?.value
        ?: step.tellPlayer?.toIntOrNull()
    fun structuredEmpathSelectionIsTruthful(value: Int): Boolean =
        projectedFirstNightInformationCandidates
            .firstOrNull { numericOptionValue(it) == value }
            ?.isTruthful
            ?: (value == structuredEmpathTruthValue)
""",
    "structured Empath semantic truth classifier",
)
text = replace_once(
    text,
    "truthful = value == structuredEmpathTruthValue,",
    "truthful = structuredEmpathSelectionIsTruthful(value),",
    "structured Empath audit truth",
)
text = replace_once(
    text,
    "selectedInformationTruthful = value == structuredEmpathTruthValue,",
    "selectedInformationTruthful = structuredEmpathSelectionIsTruthful(value),",
    "structured Empath committed truth",
)
TARGET.write_text(text, encoding="utf-8")

test_text = TEST.read_text(encoding="utf-8")
test_text = replace_once(
    test_text,
    "        assertTrue(panelBlock.contains(\"truthful = value == structuredEmpathTruthValue\"))\n",
    "",
    "remove obsolete structured Empath source-string truth assertion",
)
TEST.write_text(test_text, encoding="utf-8")

check("git", "diff", "--check")
print(run("git", "diff", "--", str(TARGET), str(TEST)), flush=True)

# Re-run the semantic projection and affected structured adapter suites, then T1.
check(*FOCUSED)
check(*STRUCTURED)
check("./gradlew", ":app:testDebugUnitTest", "--no-daemon")

check("git", "config", "user.name", "github-actions[bot]")
check("git", "config", "user.email", "41898282+github-actions[bot]@users.noreply.github.com")
check("git", "add", str(TARGET), str(TEST))
check("git", "commit", "-m", "fix(ms-s6d): wire first-night numeric projection")
check("git", "push", "origin", "HEAD:codex/ms-setup-generic-architecture")

# Self-clean temporary writer artifacts; net product diff retains only product +
# removal of the obsolete source-coupled assertion uncovered by T1.
check("git", "rm", SCRIPT, WORKFLOW)
check("git", "commit", "-m", "chore: remove one-shot S6D UI wiring patch")
check("git", "push", "origin", "HEAD:codex/ms-setup-generic-architecture")
