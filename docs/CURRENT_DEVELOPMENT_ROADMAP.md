# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-02 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at roadmap refresh:
967fdadaa3b3999d81e49c123d39ea5f0acd7de8

active campaign:
UI Information Presentation Campaign
field-test target: Friday 2026-09-04

latest validated UI stack:
UI-R4B — Night Action Square-Table Surface
branch: codex/ui-r4b-night-action-square-table
last validated executable checkpoint: 11f63647944e3063a8df3a5f2875ffb04d9f3708
PR #76: draft / open / mergeable / unmerged

next implementation slice:
UI-R4C — Field-Test UI Corrections

algorithm campaign after UI stabilization:
EPI-MQ / Productive Uncertainty / PlayerWorldSet
```

Docs-only commits created while refreshing roadmap/handoff may advance the UI-R4B branch beyond the executable checkpoint above. Always distinguish docs-only head movement from new product code.

The UI campaign is intentionally ahead of EPI-MQ because a real group play session is scheduled for Friday 2026-09-04. EPI-MQ is paused, not cancelled.

UI-R1 through UI-R4B are stacked draft work and are **not yet on main**. Do not create the next UI branch from `main` or the stack will be lost.

## 2. Campaign status

```text
UX-R1   dependency / legal-authority audit                     COMPLETE
UX-R2A  shared pair legal-domain authority                    COMPLETE / MERGED
UX-R2B  pair Manual -> legal-domain authority                 COMPLETE / VERIFIED / MERGED
UX-R3   remove global storyteller mode selector               COMPLETE / VERIFIED / MERGED
UX-R4   Top-1 + 0–2 alternatives + persistent Manual         COMPLETE / VERIFIED / MERGED
UX-R5   small-domain specialization                           COMPLETE / VERIFIED / MERGED

UI-R1   reusable square-table seat surface                    COMPLETE / VERIFIED / DRAFT #70
UI-R2   pair Manual dedicated full-screen selection           COMPLETE / VERIFIED / DRAFT #71
UI-R3   player information presentation                       COMPLETE / VERIFIED / DRAFT #72
UI-R4   Fortune Teller two-target + result flow               COMPLETE / VERIFIED / DRAFT #73
HOTFIX  Monk/Ravenkeeper target legality                      COMPLETE / VERIFIED / DRAFT #75
UI-R4B  night-action square-table unification                 COMPLETE / VERIFIED / DRAFT #76
UI-R4C  real-device UI corrections                            ACTIVE NEXT
UI-R5   Friday field-test stabilization                       QUEUED AFTER UI-R4C

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      PAUSED UNTIL UI CAMPAIGN STABLE

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Do not redo Monk/Ravenkeeper legality or UI-R4B. They are complete and validated.

## 3. Active authorities

Primary product reference:

`docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

Current implementation handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_R4C_FIELD_TEST_CORRECTIONS.md`

Previous campaign handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md`

The previous handoff is retained for historical campaign context but is no longer the current resume authority.

Existing semantic/UX authority remains:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

The UI campaign may change presentation and interaction, but must not take ownership of legal-domain or recommendation semantics.

## 4. Frozen permanent architecture

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent invariants:

- Drunk actual identity remains Drunk;
- shown identity is committed once and is not recommendation state;
- Healthy, Poisoned and Drunk of the same perceived role share role semantics before reliability;
- Spy/Recluse registration belongs to semantic truth construction, not recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role must remain playable through a correct Manual/generated clue path even when recommendation support is absent;
- recommendation ranking remains downstream of the complete legal semantic domain;
- Manual is a permanent user authority path, not a recommendation style;
- exact typed outcome identity survives presentation and confirmation;
- stable seat identity must not depend on filtered-list position;
- draft selection and confirmed observation/history remain separate;
- callback / confirmation ordering must not change accidentally;
- `EvilInfo` and other non-role information must remain safe when `roleEnName == null`;
- A3 exact enumeration remains the algorithm correctness baseline;
- A4/ZDD remains shadow/prototype until separately validated.

## 5. Stable recommendation / Manual contract

```text
Complete legal semantic candidate domain
        |
        +--> Manual / direct legal selection
        |
        +--> Recommendation Provider
                 -> presentation
```

Permanent conditions:

```text
recommendation unavailable != manual unavailable
```

Normal execution remains Storyteller-confirmed / ASSISTED.

For pair/combinatorial domains:

```text
Top-1 recommendation
+ 0–2 alternatives
+ persistent Manual over complete legal domain
```

For naturally small domains:

```text
primary recommendation
+ all remaining legal outcomes when the full domain comfortably fits
```

UI-R4C must not change these semantics.

## 6. Completed UI-R1..R4B checkpoint

The current UI stack has already delivered:

- reusable full-screen square-table seat surface;
- dedicated pair Manual square-table selection;
- normal recommendation reason/warning clutter hidden from product UI;
- Fortune Teller two-target square-table flow with typed Boolean adjudication;
- corrected Monk/Ravenkeeper target legality;
- reusable single-target and two-target night-action square-table surfaces;
- Monk/Ravenkeeper/Chambermaid core action migration;
- typed subject-seat projection for supported information propositions;
- full focused / `:app:testFast` / CI / R2 validation for the latest executable checkpoint;
- temporary large-file and APK-export workflows cleaned up.

These are completed prerequisites, not active work.

## 7. Active target — UI-R4C Field-Test UI Corrections

Real-device testing after UI-R4B exposed four product issues.

### R4C-1 — Manual role labels must follow current app language

Problem:

- pair Manual role selection exposes internal English role names / `RoleId` presentation in Chinese mode.

Target:

```text
typed RoleId
-> existing role definition/localization mapping
-> current-language role label
```

Do not parse localized labels back into semantic ids and do not create a duplicate hard-coded translation authority.

### R4C-2 — Storyteller square-table seat cards need role context

Problem:

- seat number + player name alone is insufficient during Storyteller night action.

Target Storyteller-only seat model:

```text
seat number
player name
actual role
shown/perceived role when different
```

For the Drunk, the Storyteller should see both actual Drunk identity and the committed shown role.

Long player names must fit safely through adaptive typography / bounded layout. Do not allow ambiguous truncation.

### R4C-3 — player-facing final information should be information-first, not table-first

Real-device testing showed that keeping the square table on the phone while the player reads the final clue/result wastes space and makes the reveal too busy.

Correct product boundary:

```text
Storyteller target/action interaction -> square-table-first
Player-facing final reveal            -> information-first full-screen
```

Preserve typed subject-seat identity in the model/history path, but do not require the player reveal to render the whole table.

Player reveal must remain privacy-safe and must not expose actual identity, Poisoned/Drunk state, truth flags, recommendation reasons, or other Storyteller-only context.

### R4C-4 — remove duplicate Spy registration/identity UI in pair Manual

Problem:

- exact typed pair legal candidates already carry Spy/Recluse registration semantics where applicable;
- UI adds a second large registration/identity choice layer, duplicating semantics and creating unnecessary interaction.

Target:

```text
exact typed legal pair candidate
(including registration semantics)
-> Manual presentation
-> exact candidate confirmation
```

Remove only the redundant presentation/control layer.

Do not remove or flatten registration facts, candidate identity, legality, confirmation, or durable observation semantics.

## 8. Recommended UI-R4C implementation order

```text
R4C-1  localized Manual role labels
R4C-2  richer Storyteller square-table seat presentation
R4C-3  information-first player reveal
R4C-4  remove duplicate Spy registration UI
```

Prefer narrow, auditable commits and behavioral tests.

Suggested branch:

```text
codex/ui-r4c-field-test-ui-corrections
```

Base it on the **live UI-R4B head**, not on main.

Suggested draft PR title:

```text
UI-R4C: fix real-device information UI regressions
```

Do not merge automatically.

## 9. UI-R5 after UI-R4C

UI-R5 becomes active only after all four R4C issues are corrected and the executable checkpoint is green.

R5 is a feature freeze / real-device stabilization pass, not a place for another redesign.

Required real-device walkthrough should cover at least:

- Minion introduction;
- Demon introduction;
- pair recommendation and Manual;
- Spy/Recluse registration-sensitive pair clues;
- Chef / Empath;
- Fortune Teller;
- Chambermaid;
- Ravenkeeper;
- Monk and other single-target night actions;
- impaired/discretionary information;
- long player names;
- Drunk actual/shown identity on Storyteller table only;
- player-facing reveal readability and return/navigation.

Field-test usability outranks optional animation/theme polish.

## 10. Testing / implementation strategy

Authority: `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Use risk-based tests-first.

High-value UI-R4C contracts:

- Manual role presentation uses current-language label while preserving typed role identity;
- Storyteller seat presentation exposes actual/shown identity as separate typed values;
- player-facing reveal does not depend on table rendering and remains privacy-safe;
- exact pair candidate survives Manual presentation and confirmation;
- Spy/Recluse registration semantics remain unchanged when duplicate UI is removed;
- no legal candidate is lost or invented by presentation changes;
- first-night `EvilInfo` remains safe.

Avoid brittle source-string or exact-pixel permanent tests.

For large/protected files such as `ClocktowerNightStepUi.kt` or `clocktower/ui/ClocktowerHostScreen.kt`, use the repository-approved exact-anchor one-shot Python patch workflow.

At meaningful executable checkpoints:

```text
focused behavior tests
-> :app:testFast
```

At UI-R4C completion run ordinary CI/R2 and `[full-ci]` where risk/classifier requires broad Android validation.

## 11. Source ownership / growth guards

`ClocktowerNightStepUi.kt` remains an orchestration/wiring owner, not the home for new visual policy.

`ClocktowerHostScreen.kt` remains protected orchestration.

New presentation logic should prefer small cohesive owners/models.

Do not create a generic `Utils`, `Helpers`, `Manager`, or God context merely to reduce argument count.

## 12. UI campaign scope guards

Do not expand UI-R4C into:

- EPI-MQ / Productive Uncertainty implementation;
- new recommendation ranking/scoring/diversity behavior;
- new legality ownership in recommendation/UI code;
- PlayerWorldSet production recommendation integration;
- A4/ZDD production rollout;
- broad Host/App decomposition;
- unrelated persistence/history redesign;
- Mayor redirect / Imp succession redesign;
- broad theme/animation work;
- unsupported-script expansion merely for UI completeness.

If another correctness bug is discovered, isolate and characterize it rather than hiding it inside visual refactoring.

## 13. EPI-MQ after UI stabilization

After UI-R5 reaches a stable field-test checkpoint, restore EPI-MQ as the active algorithm campaign.

Primary authorities remain:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Do not redesign the stabilized interaction when EPI-MQ resumes; the algorithm should improve recommendations behind the stable UI/decision contract.

## 14. Documentation authority / new-conversation resume protocol

Current active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_R4C_FIELD_TEST_CORRECTIONS.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
docs/TESTING_STRATEGY.md
docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md
```

The previous `NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md` is historical campaign context and should not be used as the current resume target.

New conversation must first re-query live `main`, PR #75, PR #76 and UI-R4B head/checks before creating UI-R4C. It must distinguish docs-only commits after the last validated executable checkpoint from product changes.

## 15. Deferred / queued registry

| Area | Status |
|---|---|
| UI-R1 square-table foundation | COMPLETE / VERIFIED / DRAFT |
| UI-R2 pair Manual redesign | COMPLETE / VERIFIED / DRAFT |
| UI-R3 player information presentation | COMPLETE / VERIFIED / DRAFT; player reveal boundary to be corrected in R4C |
| UI-R4 Fortune Teller interaction | COMPLETE / VERIFIED / DRAFT |
| Monk/Ravenkeeper legality hotfix | COMPLETE / VERIFIED / DRAFT |
| UI-R4B night action surface | COMPLETE / VERIFIED / DRAFT |
| UI-R4C field-test UI corrections | NEXT IMMEDIATE SLICE |
| UI-R5 field-test stabilization | QUEUED AFTER UI-R4C |
| EPI-MQ Productive Uncertainty | PAUSED UNTIL UI-R5 STABLE |
| ALG cognitive-consistency / PlayerWorldSet | PAUSED UNTIL UI-R5 STABLE |
| UX-R6 legacy ranking replacement | QUEUED AFTER EPI-MQ |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
