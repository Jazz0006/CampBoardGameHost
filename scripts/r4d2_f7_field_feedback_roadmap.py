from pathlib import Path

path = Path("docs/CURRENT_DEVELOPMENT_ROADMAP.md")
text = path.read_text(encoding="utf-8")


def replace_once(old: str, new: str) -> None:
    global text
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"expected exactly one occurrence, found {count}: {old!r}")
    text = text.replace(old, new, 1)


replace_once(
    "active development target:\nUI-R4D-2F / F7 — real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal",
    "active development target:\nUI-R4D-2F / F7 — real-device defect correction + closeout after 2026-09-03 field feedback",
)
replace_once(
    "UI-R4D-2F F7 real-device closeout                                      ACTIVE NEXT",
    "UI-R4D-2F F7 real-device defect correction / closeout                   ACTIVE / FIELD DEFECTS CONFIRMED",
)
replace_once(
    "Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, F4 resolved-display authority, F5 typed pair Player Reveal presentation, or F6 Host seat/state typography.",
    "Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F2 drag ordering, F3 seating navigation, F4 source-agnostic resolved-display authority, or F5 typed pair identity/privacy authority. F7 MAY replace F1's edge-wise spacing policy, simplify setup-only seat presentation, and reorder F5 visual hierarchy because real-device evidence showed those presentation policies are not yet acceptable. Preserve the underlying stable ring, typed identity, shared render/drag geometry, privacy boundary, and Manual/recommendation convergence.",
)
replace_once(
    "`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_PERSISTENT_HOST_TABLE.md`",
    "`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_F7_REAL_DEVICE_CORRECTIONS.md`",
)
replace_once(
    "- portrait constraints may allocate more seats to left/right than top/bottom according to actual edge capacity;",
    "- final F7 layout samples one continuous rounded-rectangle perimeter so adjacent seat spacing is visually uniform across edges and corners;",
)

old_f7 = """#### F7 — ACTIVE NEXT / remaining field-test closeout

Real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.

Only after F7 is clean should R4D-3 become active.
"""
new_f7 = """#### F7 — ACTIVE / REAL-DEVICE DEFECTS CONFIRMED

The 2026-09-03 field-test APK produced useful partial acceptance evidence:

- long-press drag and cross-corner reorder behave correctly on device;
- current seat placement is visually uneven because F1 spaces each edge independently rather than sampling one continuous rounded-rectangle perimeter;
- Seating still exposes redundant Earlier/Later buttons and setup-only selectable/selected circles/colors that are no longer desirable once drag works;
- Chinese seat labels still render as `#N` instead of `N号`;
- the Seating title/content contrast remains unreliable because the dark-theme screen root does not establish a dependable dark `contentColor` boundary;
- Game Selection places `Edit seats`, `Choose game`, and player count in one top row instead of giving game choice primary hierarchy and seat editing a clearly separated secondary action;
- typed pair Player Reveal has improved font size but the vertical semantic order is wrong for `EitherOne` and `YesNo`/two-subject results;
- Manual pair selection can resolve a legal option and close its dialog but fail to open Player Reveal, returning to the information-selection surface instead.

F7 is therefore no longer a pure acceptance checklist. It is a bounded correction gate with the following implementation order.

##### F7.1 — Manual pair -> Player Reveal lifecycle regression — FIRST

Treat the failed handoff as a correctness bug, not visual polish.

Current investigated boundary:

```text
Manual pair dialog
-> legal ClocktowerDisplayOption
-> showRecommendedDisplayOption(option)
-> resolveClocktowerPlayerDisplay(step, option)
-> onShowPlayerDisplay(displayStep)
-> publication/lifecycle guards
-> playerDisplayStep = displayStep
-> Player Reveal
```

The Manual option itself reaches the common F4 source-agnostic path. `informationDecisionPublicationAllowed()` is not the likely blocker for ordinary Manual pair options because they do not carry structured confirmation metadata. The remaining investigated boundary is first-night information publication / already-displayed lifecycle state before `playerDisplayStep` is assigned.

Tests-first requirement:

- establish a focused behavior RED reproducing the exact legal Manual pair -> no Player Reveal failure;
- prove the first valid Manual handoff opens sanitized Player Reveal exactly once;
- preserve duplicate-publication protection when re-entering a completed night step;
- preserve exactly-once durable observation/event semantics;
- do not weaken lifecycle guards globally merely to make the UI open.

##### F7.2 — continuous rounded-rectangle perimeter layout

Replace only the F1 **edge-wise spacing policy**, not the stable-ring architecture.

Target geometry:

```text
actual available rounded-rectangle perimeter
-> one clockwise path length
-> N approximately equal path-length intervals
-> N deterministic HostTableSpatialSlot values
-> SAME slots for rendering and drag hit-testing
```

Required invariants:

- contiguous deterministic `ringIndex` remains the order authority;
- render and drag continue to consume the same slot ring;
- 5 / 8 / 12 / 15 players form one visually balanced rounded rectangle rather than four independently spaced edge groups;
- adjacent perimeter distance is approximately uniform, including across corners;
- center workspace clearance and seat-card non-overlap remain fail-closed constraints;
- cross-corner drag behavior already accepted on device must remain unchanged semantically.

This supersedes F1's visual assumption that proportional per-edge allocation is the final product layout. F1's typed slot ring and shared geometry remain permanent.

##### F7.3 — Seating setup simplification + dark-theme/localization correction

Seating is an arrangement screen, not a general selection screen.

- remove Earlier/Later buttons; drag is the reorder authority;
- keep Remove/edit functionality reachable without showing setup seats as `Selectable` / `SelectedFirst`;
- normal setup seat cards remain visually Neutral even when a player is the current remove/edit subject;
- do **not** remove shared `Selectable` / `SelectedFirst` / `SelectedSecond` states globally because Manual/two-target flows still need non-color selection markers;
- localize seat labels: Chinese `1号`, `2号`, ...; English keeps `#1`, `#2`, ...;
- fix the dark screen root with a real Material content-color boundary (for example `Surface(background/onBackground)` semantics) instead of fixing only the title with a hard-coded color;
- verify `安排玩家与座位` and ordinary unstyled text remain readable in Chinese and English.

##### F7.4 — Game Selection hierarchy

Recompose the center so the primary task is obvious:

```text
Choose game
[ Blood on the Clocktower ]
[ Who is Undercover      ]
[ Werewolf               ]
---------------------------
Edit seats
```

- move the `选择游戏 / Choose game` prompt immediately above the game buttons;
- place `重新安排座位 / Edit seats` below the game actions with clear secondary separation;
- retain player-count context without competing with the primary task;
- preserve F3 visible/system Back semantics and confirmed-seating authority.

##### F7.5 — Player Reveal semantic ordering + seat-label localization

Keep F4/F5 typed identity/privacy architecture, but correct visual reading order by result kind.

`EitherOne` example:

```text
男爵
在下面两位玩家之中
2号      10号
Alice    Ken
```

Two-subject `YesNo` example:

```text
查询下面两位玩家
2号      10号
Alice    Ken
有
```

Policy:

- `EitherOne`: primary role/result -> player-visible context/footer -> two seats;
- `YesNo`: query/context -> two seats -> Yes/No result;
- two-subject `Number`: query/context -> two seats -> numeric result where that ordering matches the ability meaning;
- continue deriving pair identity only from typed propositions, never localized `displaySecondary` parsing;
- Manual and recommendation resolving to the same final outcome must still render identically;
- Player Reveal remains sanitized full screen with **no Host table** and no Storyteller-only explanation fallback.

##### F7.6 — final device acceptance

After F7.1-F7.5 are executable and validated, generate a fresh field-test APK and repeat real-device acceptance:

- 5 players: balanced perimeter, readable labels, no clipping/overlap;
- 8 players: same;
- 12 players: same;
- 15 players: same;
- cross-corner drag still preserves expected ordering and stable identity;
- Seating has no Earlier/Later controls and no setup selection circles/colors;
- Chinese seat labels use `N号`; English uses `#N`;
- Seating/Game Selection dark-theme text is readable;
- Game Selection hierarchy matches the intended primary/secondary actions;
- recommendation pair -> sanitized Player Reveal;
- Manual pair -> the **same** sanitized Player Reveal;
- `EitherOne`, Fortune Teller `YesNo`, and a two-subject `Number` result follow the intended reading order;
- no recommendation/internal explanation/Host-state leak.

Only after F7.6 is clean should R4D-3 become active.
"""
replace_once(old_f7, new_f7)

replace_once(
    "Resume from **R4D-2F / F3** after confirming the current live head and distinguishing docs-only head movement from F2 executable cleanup head `0e3b23dd96c164affd8540e1926ffdcada0fc3c6`. Do **not** start R4D-3 until F1-F7 closeout is complete. Do not merge #78 or #79 without explicit user authorization.",
    "Resume from **R4D-2F / F7.1 Manual pair -> Player Reveal lifecycle regression** after confirming the current live head and distinguishing later docs-only head movement from the last validated executable F6 cleanup head `e5109cc95ebb18fdb51a336c24fd4d96e388a0c4`. Preserve F2 cross-corner drag (accepted on device), F3 navigation, F4 source-agnostic Manual/recommendation convergence, and F5 typed Player Reveal privacy/identity. Do **not** start R4D-3 until F7.1-F7.6 are clean. Do not merge #78 or #79 without explicit user authorization.",
)
replace_once(
    "| UI-R4D-2F / F3 seating return + Android Back | ACTIVE NEXT |\n| UI-R4D-2F / F4-F7 remaining closeout | QUEUED |",
    "| UI-R4D-2F / F3 seating return + Android Back | COMPLETE / VERIFIED |\n| UI-R4D-2F / F4 source-agnostic resolved display | COMPLETE / VERIFIED |\n| UI-R4D-2F / F5 typed pair Player Reveal authority | COMPLETE / VERIFIED; visual order correction pending F7.5 |\n| UI-R4D-2F / F6 Host seat/state typography | COMPLETE / VERIFIED; screen-root contrast correction pending F7.3 |\n| UI-R4D-2F / F7 real-device defect correction | ACTIVE — start F7.1 |",
)

path.write_text(text, encoding="utf-8")
