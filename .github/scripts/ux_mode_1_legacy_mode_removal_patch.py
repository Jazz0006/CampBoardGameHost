from pathlib import Path

LEGACY_MODE = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerAutomationMode.kt")
POLICY = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicy.kt")
POOL = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/recommendation/UnifiedSelectionPool.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
HISTORY = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHistoryScreen.kt")
NIGHT = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerNightScreen.kt")
DAY = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerDayScreen.kt")
POLICY_TEST = Path("app/src/test/java/com/codex/campboardgamehost/clocktower/domain/StorytellerRecommendationUxPolicyTest.kt")
POOL_TEST = Path("app/src/test/java/com/codex/campboardgamehost/clocktower/recommendation/UnifiedSelectionPoolTest.kt")

IMPORT = b"import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode\n"


def read_bytes(path: Path) -> bytes:
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}")
    return raw


def replace_once(path: Path, old: bytes, new: bytes) -> None:
    raw = read_bytes(path)
    count = raw.count(old)
    if count != 1:
        raise SystemExit(f"Expected anchor exactly once in {path}, found {count}")
    path.write_bytes(raw.replace(old, new, 1))


for path in (HOST, HISTORY, NIGHT, DAY):
    replace_once(path, IMPORT, b"")

policy_old = b'''\n        /**\n         * Transitional compatibility path until App-root persistence is migrated in UX-MODE-1B.\n         * Keep current live behavior unchanged while the typed experience-mode contract lands.\n         */\n        fun fromLegacyMode(@Suppress("UNUSED_PARAMETER") legacyMode: StorytellerAutomationMode): StorytellerRecommendationUxPolicy =\n            StorytellerRecommendationUxPolicy(\n                automaticExecution = false,\n                recommendationStyle = RecommendationStyle.BALANCED,\n                showManualAlternatives = true,\n                recommendedOptionLimit = 3,\n            )\n'''
replace_once(POLICY, policy_old, b"")

pool_old = b'''import com.codex.campboardgamehost.clocktower.domain.QualityTier\nimport com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode\n\n/** The domain execution policy; persisted [StorytellerAutomationMode] values remain unchanged. */\nenum class SelectionExecutionPolicy {\n    AUTO,\n    ASSISTED,\n}\n\nfun StorytellerAutomationMode.executionPolicy(): SelectionExecutionPolicy =\n    if (isAutomatic) SelectionExecutionPolicy.AUTO else SelectionExecutionPolicy.ASSISTED\n\n'''
pool_new = b'''import com.codex.campboardgamehost.clocktower.domain.QualityTier\n\n/** The domain execution policy shared by Beginner automatic and Experienced assisted flows. */\nenum class SelectionExecutionPolicy {\n    AUTO,\n    ASSISTED,\n}\n\n'''
replace_once(POOL, pool_old, pool_new)

pool_test_import = b"import com.codex.campboardgamehost.clocktower.domain.StorytellerAutomationMode\n"
replace_once(POOL_TEST, pool_test_import, b"")
pool_test_old = b'''\n    @Test fun `manual preference maps to assisted without changing its persisted value`() {\n        assertEquals("manual", StorytellerAutomationMode.MANUAL.prefsValue)\n        assertEquals(SelectionExecutionPolicy.ASSISTED, StorytellerAutomationMode.MANUAL.executionPolicy())\n        assertEquals(SelectionExecutionPolicy.AUTO, StorytellerAutomationMode.AUTO_BALANCED.executionPolicy())\n    }\n'''
replace_once(POOL_TEST, pool_test_old, b"")

for path in (LEGACY_MODE, POLICY_TEST):
    if not path.is_file():
        raise SystemExit(f"Expected legacy file to exist before deletion: {path}")
    read_bytes(path)
    path.unlink()

for path in (POLICY, POOL, HOST, HISTORY, NIGHT, DAY, POOL_TEST):
    raw = read_bytes(path)
    if b"StorytellerAutomationMode" in raw:
        raise SystemExit(f"Legacy StorytellerAutomationMode reference remains in {path}")
    if b"fromLegacyMode" in raw:
        raise SystemExit(f"Legacy fromLegacyMode reference remains in {path}")

if LEGACY_MODE.exists() or POLICY_TEST.exists():
    raise SystemExit("Legacy files were not deleted")
