from pathlib import Path

PRESENTATION = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentation.kt")
HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
TEST = Path("app/src/test/java/com/codex/campboardgamehost/ClocktowerDemonBluffPresentationTest.kt")

for path in (PRESENTATION, HOST, TEST):
    raw = path.read_bytes()
    if b"\r\n" in raw or b"\r" in raw:
        raise SystemExit(f"Unexpected line ending in {path}; refusing implicit normalization")

presentation = PRESENTATION.read_text(encoding="utf-8")
host = HOST.read_text(encoding="utf-8")
test = TEST.read_text(encoding="utf-8")

presentation_replacements = [
    (
        """ * MANUAL has no setup-plan apply step, so it consumes the default BALANCED setup recommendation
 * directly once that recommendation is ready. Other setup decisions remain manual Storyteller
""",
        """ * MANUAL has no setup-plan apply step, so it consumes the setup recommendation for the same
 * current Storyteller style supplied by the host. Other setup decisions remain manual Storyteller
""",
    ),
    (
        "    preferredManualStyle: RecommendationStyle = RecommendationStyle.BALANCED,\n",
        "    storytellerStyle: RecommendationStyle,\n",
    ),
    (
        "        style = preferredManualStyle,\n",
        "        style = storytellerStyle,\n",
    ),
]

counts = [presentation.count(old) for old, _ in presentation_replacements]
if counts != [1, 1, 1]:
    raise SystemExit(f"Expected each Demon bluff presentation anchor exactly once, found {counts}")

patched_presentation = presentation
for old, new in presentation_replacements:
    patched_presentation = patched_presentation.replace(old, new, 1)

host_old = """    val demonBluffRoleNames = demonBluffRoleNamesForPresentation(
        automaticStorytellerInfo = automaticStorytellerInfo,
        appliedRoleNames = recommendedDemonBluffRoleNames,
        setupPlans = setupPlansForDemonBluffs,
    )
"""
host_new = """    val demonBluffRoleNames = demonBluffRoleNamesForPresentation(
        automaticStorytellerInfo = automaticStorytellerInfo,
        appliedRoleNames = recommendedDemonBluffRoleNames,
        setupPlans = setupPlansForDemonBluffs,
        storytellerStyle = automaticStorytellerStyle,
    )
"""
if host.count(host_old) != 1:
    raise SystemExit(f"Expected Demon bluff HostScreen anchor exactly once, found {host.count(host_old)}")
patched_host = host.replace(host_old, host_new, 1)

test_old = "            preferredManualStyle = RecommendationStyle.AGGRESSIVE,\n"
test_new = "            storytellerStyle = RecommendationStyle.AGGRESSIVE,\n"
if test.count(test_old) != 1:
    raise SystemExit(f"Expected Demon bluff test API anchor exactly once, found {test.count(test_old)}")
patched_test = test.replace(test_old, test_new, 1)

if "preferredManualStyle: RecommendationStyle = RecommendationStyle.BALANCED" in patched_presentation:
    raise SystemExit("Hidden BALANCED Demon bluff fallback remains")
if patched_presentation.count("storytellerStyle: RecommendationStyle,") != 1:
    raise SystemExit("Expected exactly one required Demon bluff storytellerStyle parameter")
if patched_presentation.count("style = storytellerStyle,") != 1:
    raise SystemExit("Expected selector to consume storytellerStyle exactly once")
if patched_host.count("storytellerStyle = automaticStorytellerStyle,") != 1:
    raise SystemExit("Host must pass the unified automaticStorytellerStyle exactly once")
if patched_test.count("storytellerStyle = RecommendationStyle.AGGRESSIVE,") != 1:
    raise SystemExit("Semantic test must exercise the explicit aggressive storytellerStyle")
if "manual Demon bluff fallback is explicitly wired to the unified storyteller style" not in patched_test:
    raise SystemExit("Demon bluff wiring RED assertion must remain intact")

PRESENTATION.write_text(patched_presentation, encoding="utf-8", newline="\n")
HOST.write_text(patched_host, encoding="utf-8", newline="\n")
TEST.write_text(patched_test, encoding="utf-8", newline="\n")
