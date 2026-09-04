# Next Development Handoff — UI Information Presentation Campaign

> Date: 2026-09-02 Australia/Sydney  
> Status: **ACTIVE NEXT IMPLEMENTATION CAMPAIGN**  
> Live baseline when this handoff was written: `main@6f1ee4513cd149120c453c3b2623f989903a2493`  
> Field-test target: **Friday 2026-09-04**  
> Product reference: `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 1. Why this campaign is inserted now

UX-R1 through UX-R5 established the semantic/legal/recommendation/Manual foundations. EPI-MQ / Productive Uncertainty was scheduled next, but the immediate product priority has changed because a real group play session is planned for Friday 2026-09-04.

The near-term value is therefore higher from making the existing information interaction fast, readable and reliable on a real phone before improving recommendation intelligence.

EPI-MQ is **paused, not cancelled**. Resume it after this UI campaign reaches a field-test-stable checkpoint.

## 2. Current live baseline

At handoff creation:

```text
main = 6f1ee4513cd149120c453c3b2623f989903a2493
```

The latest merged change is the first-night evil-information display crash fix.

Important implication for this campaign:

- Minion/Demon introduction `EvilInfo` is not necessarily a role-ability information step;
- UI work must not reintroduce assumptions that every display step has `roleEnName`;
- the new full-screen display path must preserve the safe reliability handling introduced by the hotfix.

Always re-query live `main` before starting a branch; do not trust this stored SHA if `main` has moved.

## 3. Campaign goal

Deliver a coherent Storyteller information interaction based on a reusable full-screen square-table surface:

```text
night step
   -> recommendation or Manual
   -> table-based target/seat interaction where applicable
   -> fixed result OR Storyteller legal choice
   -> full-screen player display
   -> existing typed confirmation/history path
```

The campaign is presentation/interaction work. It must not become a new semantic or recommendation engine.

## 4. Campaign naming

Use a separate UI namespace so the already-queued `UX-R6` legacy-ranking replacement keeps its existing meaning.

Recommended slices:

```text
UI-R1  reusable square-table seat surface
UI-R2  pair Manual dedicated full-screen selection
UI-R3  unified full-screen player information display
UI-R4  Fortune Teller two-target + fixed/discretionary result flow
UI-R5  Friday field-test stabilization / cross-flow regression checkpoint
```

Do not merge all five concerns into one giant implementation commit.

## 5. Frozen invariants from previous campaigns

The UI campaign must preserve:

```text
complete legal semantic domain
        |
        +--> Manual
        |
        +--> Recommendation Provider
```

and:

- Manual is independent of recommendation coverage;
- recommendation never owns legality;
- normal execution is Storyteller-confirmed / ASSISTED;
- pair Manual commits exact typed legal candidates;
- Spy/Recluse registration identity is preserved;
- small-domain Number/Boolean choices remain typed;
- Fortune Teller Boolean proposition remains bound to exact actor + selected seats;
- localized labels are not parsed to reconstruct semantic values;
- draft selection remains distinct from confirmed observation/history;
- display/reliability handling must remain safe when `roleEnName == null` for non-role information such as evil-team introductions.

## 6. UI-R1 — reusable square-table seat surface

### Objective

Create one reusable composable/presentation owner for seat layout and selection/highlight states.

### Product behavior

The component should:

- arrange all players around four sides of the available rectangular/square region;
- use most of the available phone viewport;
- keep stable seat order;
- show seat number + readable player label;
- expose center content as a slot/child area;
- support selectable and read-only modes;
- support at least these visual states:

```text
neutral
selectable
selected-first
selected-second
highlighted-information
disabled
```

Selection meaning must not rely on color alone.

### Preferred ownership

Do **not** add the full implementation into `ClocktowerNightStepUi.kt`.

Prefer a dedicated owner such as:

```text
ClocktowerSquareTableUi.kt
```

with a small typed seat-presentation model.

The exact filename is implementation-owned; the architectural requirement is the dedicated owner.

### Tests / evidence

A new RED is required only for durable behavior that can be tested without locking Compose source shape.

Prefer pure geometry/state tests for:

- every supplied seat appears exactly once;
- stable seat identity/order is preserved;
- selected/highlighted state is attached to stable seat identity;
- layout assignment works for supported player counts used by Trouble Brewing.

Do not write source-string tests asserting exact Compose calls or pixels.

### Stop condition

UI-R1 is complete when a real screen can render the table independently with stable seat identity and selection/highlight state. Do not yet redesign Manual or Fortune Teller in this slice unless only minimal demo wiring is required.

## 7. UI-R2 — pair Manual dedicated full-screen selection

### Objective

Replace the current inline/expanded pair Manual form with a dedicated table-first interaction for first-night:

- Washerwoman;
- Librarian;
- Investigator.

### Flow

```text
normal recommendation screen
  -> Manual
  -> dedicated Manual screen
  -> choose role / special zero-case
  -> choose seat 1
  -> choose seat 2
  -> resolve exact legal typed candidate
  -> confirm/display
```

### Required behavior

- consume `step.manualInformationCandidates` / established legal-domain authority;
- do not rebuild legality in UI;
- no localized-label parsing;
- role selection constrains legal seat continuations;
- first-seat selection constrains legal second seats where practical;
- allow correction/toggle before confirmation;
- require two distinct seats for pair candidates;
- Librarian legal zero-Outsider remains a direct option;
- Investigator zero-Minion remains impossible;
- recommendation remains available when returning to the previous surface;
- Manual state must reset correctly when the owning interaction changes.

### Presentation

The square table occupies the majority of the screen. Role/special-case selection and confirmation controls should not push the table into a small card.

### Recommendation reason cleanup

As part of the same normal recommendation surface, remove/hide the current recommendation reason/warning prose from ordinary product mode.

Do not delete diagnostic data from lower layers merely because it is hidden from the normal UI.

### Test strategy

Behavior-first contracts worth protecting:

- every legal Manual candidate remains reachable;
- no recommendation-only candidate becomes legal through the UI;
- selected role + seats resolve to the exact typed legal candidate;
- special zero-case behavior remains correct;
- changing first seat cannot retain an invalid stale second seat;
- Manual can function when recommendation set is empty.

Use existing UX-R2B tests where they already prove the contract; add new tests only for new selection-state behavior.

## 8. UI-R3 — unified full-screen player information display

### Objective

Replace the current information display with a full-screen square-table player-facing surface where seat-highlighted clues can be understood quickly.

### Display families for first checkpoint

Prioritize the already-supported Trouble Brewing families used in current production:

- pair information (Washerwoman / Librarian / Investigator);
- numeric information (Chef / Empath);
- Fortune Teller Yes/No after UI-R4 wiring;
- EvilInfo / Demon/Minion introduction must remain safe and must not require role ability identity.

### Player-facing privacy contract

Never show:

- Poisoned / Drunk / unreliable status;
- truth/falsity flag;
- recommendation reason;
- hidden Storyteller target facts;
- actual role identity when only shown/perceived information is intended.

### Storyteller lifecycle

Keep explicit separation:

```text
selection/configuration
-> Storyteller ready/preview
-> player-facing display
-> return/close
-> existing confirmation/history path
```

Do not change transaction/callback ordering casually. If the current callback commits before/after display in a specific protected order, preserve it unless a bug is separately characterized.

### Highlight mapping

Use typed proposition/structured display data to derive highlighted seats where available.

Do not parse display strings to determine which seats to highlight.

For display families without target seats, keep the table visually neutral and put the result in the center.

### Regression requirement

Add/retain explicit coverage that `EvilInfo` display with `roleEnName == null` remains safe. The newly merged first-night crash test is part of the acceptance baseline and must not be bypassed by a parallel display implementation.

## 9. UI-R4 — Fortune Teller two-target and result flow

### Objective

Turn Fortune Teller into one direct table interaction instead of a fragmented target/result workflow.

### Target selection

```text
select first player
select second distinct player
```

Both selections remain visible on the square table.

### Result presentation

After two legal targets are selected, use the complete typed Boolean legal domain from the UX-R5 foundation.

#### One legal result

```text
Result determined
[ YES ]
```

or

```text
Result determined
[ NO ]
```

Only one action is shown.

#### Two legal results

```text
Storyteller choice
Recommended
[ primary ]

Other legal result
[ other ]
```

No third Manual drill-down exists for a Boolean domain.

### Required invariants

- selected pair identity is exact and ordered/normalized according to existing semantic authority;
- Yes/No proposition remains bound to the selected pair;
- Red Herring and Recluse behavior remains owned by existing rules/registration semantics;
- recommendation affects primary placement only;
- no label parsing;
- changing either target invalidates stale result confirmation/model state;
- result is not available until two distinct legal targets exist.

### Tests

Prefer typed state/adapter tests for:

- target pair identity;
- one-result vs two-result presentation state;
- recommended primary does not hide the other legal Boolean result;
- changing target pair changes the bound proposition and invalidates stale confirmation.

## 10. UI-R5 — Friday field-test stabilization

### Objective

Do not add more features. Freeze the interaction and validate the actual phone experience before Friday play.

### Required walkthrough

At minimum manually exercise on a real device:

1. first-night Minion display;
2. first-night Demon display;
3. Washerwoman recommendation -> player display;
4. Washerwoman Manual -> role + two seats -> player display;
5. Librarian zero-Outsider path if easy to configure;
6. Investigator Manual legality;
7. Chef/Empath small-domain display;
8. Fortune Teller select two players -> result -> player display;
9. Drunk/Poisoned discretionary information path;
10. previous/next navigation and return from full-screen display.

### Visual acceptance

- no major text clipping on the real phone;
- seat labels readable;
- selected/highlighted states obvious;
- table remains the dominant visual area;
- no accidental Storyteller-only reliability/truth leakage to player display;
- no obvious double-scroll / tiny nested-card interaction;
- no crash in first-night evil information display.

### Automated checkpoint

At logical completion:

- relevant focused behavior tests GREEN;
- `./gradlew :app:testFast`;
- full CI/R2 before merge;
- `[full-ci]` if the final UI wiring touches broad Host/night orchestration or if classifier would otherwise skip meaningful Android validation.

## 11. Implementation order and merge strategy

Recommended sequence:

```text
fresh branch from live main
  -> UI-R1
  -> UI-R2
  -> checkpoint
  -> UI-R3
  -> UI-R4
  -> UI-R5 stabilization
  -> final full CI / real-device validation
  -> merge with explicit user authorization
```

Because the Friday deadline is near, related slices may live in one Draft PR, but commits/checkpoints must remain separable and auditable.

Recommended branch:

```text
codex/ui-information-table-campaign
```

Recommended Draft PR title:

```text
UI: redesign Storyteller information interaction
```

Do not merge automatically.

## 12. File ownership / growth constraints

Current `ClocktowerNightStepUi.kt` is already large. Treat it as orchestration/wiring, not the home for hundreds of lines of new table UI.

Prefer new cohesive files for:

- square-table geometry/state;
- square-table Compose presentation;
- Manual pair selection state/presentation;
- player information display presentation;
- Fortune Teller table/result presentation if a generic owner cannot cover it cleanly.

`ClocktowerHostScreen.kt` remains a protected orchestration owner. Do not put new visual policy there.

If large-file wiring is needed, follow `AGENTS.md`:

```text
small tests/new files via GitHub connector
-> exact-anchor GitHub Actions one-shot for large/truncated wiring
-> focused evidence
-> :app:testFast at checkpoint
-> exact diff allowlist
-> self-remove one-shot files
```

## 13. Scope guards

Do not expand this campaign into:

- EPI-MQ / Productive Uncertainty implementation;
- new recommendation scoring or diversity heuristics;
- legal-domain redesign;
- PlayerWorldSet ranking rollout;
- A4/ZDD production rollout;
- Host/App decomposition campaign;
- unrelated persistence/history redesign;
- broad theme redesign;
- animation framework work;
- support for unsupported scripts solely for UI completeness.

If a true correctness bug is discovered during UI work, isolate it as a narrow hotfix/contract rather than quietly mixing it into visual refactoring.

## 14. New-conversation resume instructions

Start the next implementation conversation with:

```text
请读取根目录 AGENTS.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md。

先重新确认 live main 和最近的 first-night evil info crash hotfix 已在 main，然后从 UI-R1 reusable square-table seat surface 开始。按 handoff 的 slice 边界实施，优先保证 2026-09-04 周五真机组局可用。不要开始 EPI-MQ、不要改变 recommendation/legal semantics、不要自行 merge。
```

The new conversation should re-audit live source before editing and create a fresh implementation branch from the then-current `main`.

## 15. Campaign completion condition

The campaign is complete when:

- the reusable square-table surface is production-wired;
- pair Manual is dedicated/table-first;
- normal recommendation reason clutter is removed;
- player-facing information display is full-screen/table-first and privacy-safe;
- Fortune Teller has the two-target + fixed/discretionary result flow;
- typed legal/recommendation/confirmation foundations remain intact;
- Friday real-device walkthrough is acceptable;
- final CI/R2 gates are green;
- only then update completed history and restore EPI-MQ as the active campaign.
