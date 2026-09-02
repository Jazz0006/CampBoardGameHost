# Next Development Handoff — UI-R4C Field-Test Corrections

> Date: 2026-09-02 Australia/Sydney  
> Status: **ACTIVE NEXT IMPLEMENTATION SLICE**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Field-test target: Friday 2026-09-04  
> Do not merge without explicit user authorization.

## 1. Current verified checkpoint

The UI information campaign has progressed beyond the original UI-R1..R4 plan.

Current validated stack:

```text
main
-> UI-R1 reusable square-table surface
-> UI-R2 pair Manual square-table selection
-> UI-R3 player information presentation
-> UI-R4 Fortune Teller square-table adjudication
-> Monk/Ravenkeeper target-legality hotfix
-> UI-R4B night-action square-table unification
```

Live `main` at handoff refresh:

```text
967fdadaa3b3999d81e49c123d39ea5f0acd7de8
```

UI-R4B branch:

```text
codex/ui-r4b-night-action-square-table
last validated executable checkpoint = 11f63647944e3063a8df3a5f2875ffb04d9f3708
PR #76 = draft / open / mergeable / unmerged
```

Docs-only commits created while refreshing this handoff may advance the branch head beyond the executable checkpoint above. The next conversation must re-query the live branch and distinguish docs-only head movement from new product code.

UI-R4B acceptance is complete:

- reusable single-target square-table interaction;
- reusable two-target square-table interaction;
- Monk/Ravenkeeper/Chambermaid core night-action migration;
- generic typed subject-seat highlighting for supported player information;
- focused tests GREEN;
- `:app:testFast` GREEN;
- full CI GREEN;
- R2 main-thread boundary GREEN;
- temporary large-file/APK-export workflows removed.

The Monk/Ravenkeeper legality hotfix is also complete and validated in draft PR #75. Do not redo either of these slices.

## 2. Why UI-R4C is inserted before R5

Real-device testing after UI-R4B exposed four presentation problems. These are product corrections, not mere stabilization noise, so they must be fixed before the feature-freeze / field-test stabilization slice.

Therefore the current route is:

```text
UI-R4B COMPLETE
-> UI-R4C FIELD-TEST UI CORRECTIONS   <-- ACTIVE NEXT
-> UI-R5 STABILIZATION / REAL-DEVICE WALKTHROUGH
-> resume EPI-MQ after UI campaign is stable
```

Do not start EPI-MQ, ranking redesign, Host/App decomposition, or unrelated refactors during UI-R4C.

## 3. UI-R4C issue 1 — player-facing reveal should not show the square table

### Problem observed on real device

UI-R3 generalized final player information into a square-table display. On a phone this makes final information unnecessarily busy and reduces the space available for the actual clue/result.

The square table is valuable for the **Storyteller interaction surface**: choosing players, seeing the seating context, and adjudicating an action.

It should not automatically remain on the **player-facing reveal surface**.

### Correct product boundary

```text
Storyteller action / target selection
-> square-table-first

Player-facing final information reveal
-> information-first full-screen
```

Player-facing reveal should prioritize:

- the result/clue itself;
- large readable typography;
- minimal chrome;
- safe return/close interaction;
- no hidden Storyteller-only state.

Typed subject-seat identity must remain available in the information model/history path, but the reveal UI does not need to render the entire table merely because subject seats exist.

### Preserve

- exact typed proposition;
- confirmation/history lifecycle;
- `EvilInfo` safety when `roleEnName == null`;
- no Poisoned/Drunk/truth/recommendation leakage;
- callback/commit ordering.

Do not solve this by deleting typed subject-seat projection from the domain/presentation model.

## 4. UI-R4C issue 2 — Storyteller square-table seats need role context

### Problem observed on real device

Seat cards currently emphasize seat number + player name. For Storyteller night actions this is insufficient: the Storyteller needs role identity visible at the table without mentally mapping names back to the Grimoire.

### Required Storyteller seat content

Define/extend a typed Storyteller seat-presentation model that can render:

```text
seat number
player name
actual role identity
shown/perceived role identity when it differs
```

For an ordinary player, one role label is enough.

For the Drunk, the Storyteller must be able to see both:

```text
actual: Drunk
shown as: <committed shown role>
```

Do not infer Drunk/shown identity from localized labels inside the Compose layer. Consume the committed actual/shown identity already owned upstream.

### Long-name requirement

Real player names must not break seat layout.

Use a durable presentation rule such as:

- adaptive font size / constrained text fitting;
- bounded line count;
- only use abbreviation when the presentation model explicitly provides one or the rule is deterministic and non-ambiguous.

Do not silently truncate different players into indistinguishable labels.

### Privacy boundary

This richer role-bearing seat card is **Storyteller-only**. It must not leak into player-facing final information reveal.

## 5. UI-R4C issue 3 — Manual role labels are incorrectly English

### Problem observed on real device

Pair Manual role selection exposes internal English `RoleId`/role names while the application is in Chinese mode.

### Required correction

Semantic identity remains the stable typed role id.

Presentation must map that semantic id through the existing script/role localization authority and show the role name in the current app language.

```text
typed RoleId
-> existing role definition / localization mapping
-> current-language display label
```

Do not parse localized labels back into role ids.

Do not create an independent hard-coded translation table if the app already has role definitions/name mapping.

Add a small durable test around the presentation mapping if a pure mapping seam is introduced.

## 6. UI-R4C issue 4 — remove duplicate Spy registration/identity UI from pair Manual

### Problem observed on real device

For Washerwoman/Librarian-style pair Manual selection, legal typed candidates can already carry exact Spy registration semantics. The current UI additionally surfaces a large Spy registration/identity choice layer, forcing the Storyteller to make an extra choice that duplicates semantic information already present in the selected candidate.

### Correct boundary

```text
complete typed legal pair candidate
(including exact Spy/Recluse registration facts when applicable)
-> Manual presentation
-> choose exact legal candidate
-> confirm exact candidate
```

The UI must **not** ask the Storyteller to recreate or separately choose registration identity when that identity is already part of the candidate.

### Critical guard

Remove only the redundant UI/presentation layer.

Do **not** remove or flatten:

- Spy/Recluse registration facts from legal candidates;
- candidate identity;
- confirmation semantics;
- durable observation/history semantics;
- existing legality authority.

If multiple candidates differ semantically only by registration identity and that distinction is necessary downstream, preserve the exact candidate choice through the typed Manual flow without exposing a second giant registration panel.

## 7. Recommended implementation order

Use four narrow commits/slices rather than one broad UI rewrite:

```text
R4C-1  Manual current-language role labels
R4C-2  Storyteller square-table richer seat presentation
R4C-3  player-facing information-first reveal (remove table)
R4C-4  remove duplicate Spy registration UI while preserving exact candidate semantics
```

Reasoning:

- R4C-1 is narrow and low risk;
- R4C-2 establishes the correct Storyteller-only seat model before further table usage;
- R4C-3 corrects the UI-R3 player-facing boundary;
- R4C-4 touches registration-sensitive presentation and should be handled last with existing pair legal-domain tests protecting semantics.

If code ownership makes R4C-2/R4C-3 safer in the opposite order, that is acceptable, but keep their model/privacy boundaries separate.

## 8. Branch / PR strategy

Do **not** branch from `main`: UI-R1..R4B are still stacked draft PRs and are not present on main.

Create UI-R4C from the exact live UI-R4B head after re-querying it:

```text
base branch: codex/ui-r4b-night-action-square-table
suggested branch: codex/ui-r4c-field-test-ui-corrections
suggested PR: UI-R4C: fix real-device information UI regressions
```

Keep it draft and stacked on UI-R4B.

Before editing, re-confirm:

- live `main`;
- PR #75 state/head;
- PR #76 state/head/checks;
- UI-R4B branch head;
- whether commits after `11f63647944e3063a8df3a5f2875ffb04d9f3708` are docs-only or executable;
- that no new product commit exists without fresh validation.

## 9. Tests-first / validation strategy

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Permanent tests should protect behavior, not Compose source shape.

High-value R4C contracts:

- Manual role presentation uses current-language role name while preserving typed RoleId;
- Storyteller seat model exposes actual/shown role separately where appropriate;
- ordinary seat model does not invent a second shown role when actual == shown;
- player-facing reveal model does not require square-table seat rendering;
- player reveal remains privacy-safe;
- pair Manual confirmation preserves the exact typed candidate including registration semantics;
- removing duplicate registration controls does not expand or shrink the legal domain.

Do not add permanent source-string tests saying a Composable contains/does not contain a specific call.

For large/protected files such as `ClocktowerNightStepUi.kt` or `clocktower/ui/ClocktowerHostScreen.kt`, use the repository-approved exact-anchor one-shot Python patch workflow. Do not directly perform casual large-file contents replacements.

At each meaningful executable checkpoint:

```text
focused tests
-> :app:testFast
```

At UI-R4C completion, run ordinary CI/R2; use `[full-ci]` when required by classifier/risk or when broad Host/night orchestration wiring changed.

## 10. Scope guards

UI-R4C must not include:

- recommendation ranking/scoring changes;
- new legal-domain rules;
- EPI-MQ / Productive Uncertainty;
- A4/ZDD rollout;
- Mayor redirect or Imp succession redesign;
- Host/App decomposition;
- persistence/history redesign;
- broad theme/animation work;
- unrelated rule corrections.

If another correctness bug is discovered, characterize it separately rather than hiding it in presentation code.

## 11. R5 after UI-R4C

Only after all four real-device issues are corrected and automated checks are green should R5 resume as a **feature freeze / stabilization** slice.

R5 should then perform a real-device walkthrough covering at least:

- Minion introduction;
- Demon introduction;
- pair recommendation;
- pair Manual;
- Spy/Recluse registration-sensitive pair clue paths;
- Chef / Empath;
- Fortune Teller;
- Chambermaid;
- Ravenkeeper;
- Monk and other single-target night actions;
- impaired/discretionary information;
- player-facing reveal readability and return/navigation;
- long player names;
- Drunk actual/shown identity on Storyteller table only.

R5 is not the place for new interaction features.

## 12. New-conversation resume instruction

Use the handoff prompt supplied by the current conversation together with these authorities:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_R4C_FIELD_TEST_CORRECTIONS.md
```

The new conversation must re-query live GitHub before editing and must not merge any stacked PR without explicit user authorization.
