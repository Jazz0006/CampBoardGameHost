from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise AssertionError(f"{label}: expected exactly one match, got {count}")
    return text.replace(old, new, 1)


handoff = Path("docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_UNIFICATION.md")
text = handoff.read_text()
text = replace_once(
    text,
    "Status: **ACTIVE — UI-NAV-1D COMPLETE / UI-NAV-1E NEXT**",
    "Status: **ACTIVE — UI-NAV-1E + UI-NAV-1F COMPLETE / UI-NAV-1G VALIDATION**",
    "handoff status",
)

start = text.index("### UI-NAV-1E — Identity Reveal square-table controller")
end = text.index("### UI-NAV-1G — validation / closeout", start)
replacement = '''### UI-NAV-1E — Identity Reveal square-table controller — COMPLETE / PASS

Production checkpoint:

`ba2bd1f934dc4f49519fea4f7fbeba309235004b`

Cleanup head after one-shot scaffolding removal:

`35cb178c3c85f0b21cbc57360e8979f49330b1d0`

Validation:

```text
one-shot run 34474761127 PASS
- exact four-production-file diff audit PASS
- identity privacy contract audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS
- production push PASS
- one-shot cleanup PASS
```

Implemented behavior:

- `Screen.PassPhone` is now the privacy-safe Storyteller square-table identity controller for Clocktower;
- the controller receives seat/name data only and does not receive role, alignment, drunk/poison state, hidden state, or other Storyteller secrets;
- `currentDealIndex` remains the single identity cursor;
- Previous/Next move only that cursor and never auto-reveal;
- first-seat Previous is disabled;
- explicit Show Identity opens the isolated player-facing `Screen.RevealCard`;
- Clocktower RevealCard Hide returns to the same Storyteller seat and does not auto-advance;
- Undercover reveal/pass behavior is unchanged;
- final-seat Next enters the existing `ClocktowerJudge` first-night-ready boundary with `nightStarted == false`;
- that first-night boundary exposes Previous back to the final identity seat;
- later-night ready boundaries keep Previous disabled;
- no second identity cursor, navigation owner, phase owner, or persistence/recovery owner was introduced.

### UI-NAV-1F — Settings composition under Host Tools — COMPLETE / PASS

Production checkpoint:

`6dbb0d5bccd9adca8487947fb7e66f60f64937dc`

Cleanup head after one-shot scaffolding removal:

`1abba7b11052ff8d044eef71f115b4d61f4f5ec1`

Validation:

```text
one-shot run 34475818260 PASS
- exact three-production-file baseline/diff audit PASS
- Settings ownership/composition audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS
- production push PASS
- one-shot cleanup PASS
```

Implemented behavior:

- `HostToolTab.Settings` is a fourth tab in the existing root-owned `HostGameToolsScreen`;
- `AppSettingsScreen.kt` now exposes reusable `SettingsContent` while retaining `SettingsScreen` as the legacy route wrapper;
- App-root remains the only owner of `languageMode`, `storytellerAutomationMode`, common-player state, persistence writes, and mutations;
- Host Tools receives Settings as a composable content slot only; no duplicate Settings state exists inside Host Tools;
- legacy `Screen.Settings` remains in place;
- no route/state-machine redesign, no second Host Tools owner, and no Settings persistence migration was introduced.

'''
text = text[:start] + replacement + text[end:]
text = replace_once(
    text,
    "### UI-NAV-1G — validation / closeout",
    "### UI-NAV-1G — validation / closeout — ACTIVE",
    "1G header",
)

immediate = text.index("## 10. Immediate next action")
text = text[:immediate] + '''## 10. Immediate next action

Execute **UI-NAV-1G final validation / closeout**.

Required closeout evidence:

1. re-confirm live `main`, PR #118 and branch head;
2. run whole-PR `diff --check` / changed-path audit and confirm no transient one-shot workflow/script remains;
3. preserve the explicit identity privacy and Settings ownership contracts from 1E/1F;
4. create a user-authored `[full-ci]` logical checkpoint so CI selects T4 exactly as required by `docs/TESTING_STRATEGY.md`;
5. require T4 PASS: Android `testFull` + debug APK, ASP contract tests, Real Clingo cross-validation, aggregate CI gate;
6. require the R2 main-thread boundary check to pass for that checkpoint;
7. keep PR #118 Draft until visual/device acceptance is explicitly decided;
8. perform emulator/real-device visual acceptance for the unified bottom navigation, identity controller/reveal boundary, and the four-tab Host Tools layout. In particular, inspect the narrow-screen fit of the History tab label after Settings was added;
9. after automated T4 PASS, update this handoff/roadmap/PR description with the final checkpoint and mark UI-NAV-1 implementation complete while recording any device-only follow-up separately;
10. do not merge PR #118 without explicit user direction.
'''
handoff.write_text(text)

roadmap = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
text = roadmap.read_text()
text = replace_once(
    text,
    "UI-NAV-1 global navigation visual unification     CURRENT — 1D COMPLETE / 1E NEXT",
    "UI-NAV-1 global navigation visual unification     CURRENT — 1E + 1F COMPLETE / 1G VALIDATION",
    "roadmap state",
)
text = replace_once(
    text,
    "> **CURRENT: UI-NAV-1D is complete. Proceed directly with UI-NAV-1E identity delivery before EPI-MQ-0.**",
    "> **CURRENT: UI-NAV-1E and UI-NAV-1F are complete. Execute UI-NAV-1G full acceptance and device/visual closeout before EPI-MQ-0.**",
    "roadmap current paragraph",
)
text = replace_once(
    text,
    "UI-NAV-1E  migrate identity delivery to privacy-safe square table      NEXT\nUI-NAV-1F  isolate Settings-under-Host-Tools composition if still narrow\nUI-NAV-1G  focused/full validation + real-device visual acceptance + closeout",
    "UI-NAV-1E  migrate identity delivery to privacy-safe square table      COMPLETE / PASS\nUI-NAV-1F  isolate Settings-under-Host-Tools composition                    COMPLETE / PASS\nUI-NAV-1G  T4 full validation + real-device visual acceptance + closeout   CURRENT",
    "roadmap sequence",
)
text = text.replace(
    "The remaining persistent `Screen.Game` top Host Tools chrome is gone. Pre-game surfaces reserve/disable Host Tools rather than inventing a second owner. Settings relocation remains UI-NAV-1F.",
    "The remaining persistent `Screen.Game` top Host Tools chrome is gone. Pre-game surfaces reserve/disable Host Tools rather than inventing a second owner. Settings composition under Host Tools is now complete without moving Settings ownership out of App root.",
    1,
)
marker = "### Phase-boundary product refinement\n"
completion = '''### UI-NAV-1E / 1F completion evidence

```text
1E identity production: ba2bd1f934dc4f49519fea4f7fbeba309235004b
1E cleanup head: 35cb178c3c85f0b21cbc57360e8979f49330b1d0
1E one-shot: 34474761127 PASS
- exact four-file diff PASS
- privacy contract audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS

1F Settings production: 6dbb0d5bccd9adca8487947fb7e66f60f64937dc
1F cleanup head: 1abba7b11052ff8d044eef71f115b4d61f4f5ec1
1F one-shot: 34475818260 PASS
- exact three-file diff PASS
- Settings ownership/composition audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS
```

Identity delivery now uses the Storyteller square-table controller with a single App-root cursor and an isolated player reveal surface. Settings now appears as a tab inside the existing Host Tools overlay through reusable `SettingsContent`; App root remains the only Settings state/persistence owner and legacy `Screen.Settings` remains available.

UI-NAV-1G must use a user-authored `[full-ci]` checkpoint because `docs/TESTING_STRATEGY.md` defines T4 as the logical acceptance tier. Automated T4 is not replaced by the preceding 1E/1F FAST one-shots. Real-device visual acceptance remains a separate final product check.

'''
if "### UI-NAV-1E / 1F completion evidence" not in text:
    text = replace_once(text, marker, completion + marker, "roadmap completion insertion")
roadmap.write_text(text)

print("UI-NAV-1 docs reconciliation PASS")
