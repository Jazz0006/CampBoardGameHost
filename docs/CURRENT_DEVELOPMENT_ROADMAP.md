# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-02 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
last merged architecture/product slice:
#68 — UX-R5: specialize small-domain presentation
COMPLETE / VERIFIED / MERGED

UX-R5 merge commit:
563470a2c3b4e3dc10732e00827e33ebee00884a

latest live main hotfix:
#69 — first-night evil information display crash
MERGED / REAL-DEVICE VERIFIED

main at roadmap update:
967fdadaa3b3999d81e49c123d39ea5f0acd7de8

active campaign:
UI Information Presentation Campaign
field-test target: Friday 2026-09-04

current active slice:
UI-R5 — Friday field-test stabilization

algorithm campaign after UI stabilization:
EPI-MQ / Productive Uncertainty / PlayerWorldSet
```

Completed slice contracts, RED/GREEN evidence, CI checkpoints, and merge details live in:

`docs/COMPLETED_DEVELOPMENT_HISTORY.md`

The UI campaign is intentionally inserted before EPI-MQ because a real group play session is scheduled for Friday 2026-09-04. EPI-MQ is paused, not cancelled.

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
UI-R3   unified full-screen player information display        COMPLETE / VERIFIED / DRAFT #72
UI-R4   Fortune Teller two-target + result flow               COMPLETE / VERIFIED / DRAFT #73
UI-R5   Friday field-test stabilization                       ACTIVE — REAL-DEVICE WALKTHROUGH PENDING

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      PAUSED UNTIL UI-R5 STABLE

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Current UI stack:

```text
main
-> UI-R1 #70
-> UI-R2 #71
-> UI-R3 #72
-> UI-R4 #73
-> UI-R5 stabilization branch
```

Do not add new UI features during UI-R5. Stabilization is limited to real-device acceptance, narrow correctness fixes if discovered, final validation, and documentation/checkpoint maintenance.

## 3. Active UI authorities

Primary product reference:

`docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

Implementation handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md`

Existing semantic/UX authority remains:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

The UI campaign must improve interaction and presentation **without changing the existing legal-domain/recommendation authority model**.

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

The UI campaign may change where/how these controls appear, but not their semantic ownership.

## 6. Active target — UI Information Presentation Campaign

### UI-R1 — square-table foundation — COMPLETE / VERIFIED

Reusable table-first seat presentation now:

- uses the phone perimeter efficiently;
- supports stable seat identity;
- supports selectable/read-only modes;
- supports neutral/selectable/selected-first/selected-second/highlighted/disabled states;
- leaves a center slot for clue/result/instructions;
- lives in a dedicated UI owner rather than adding a large visual implementation to `ClocktowerNightStepUi.kt`.

### UI-R2 — pair Manual — COMPLETE / VERIFIED

For Washerwoman / Librarian / Investigator:

```text
Manual
-> dedicated full-screen surface
-> choose role / special zero-case
-> select two legal seats on square table
-> exact typed legal candidate
-> confirm/display
```

Normal recommendation reason/warning prose is hidden while diagnostic data/ranking/legal authority remains unchanged.

### UI-R3 — full-screen player display — COMPLETE / VERIFIED

Square-table player-facing display now provides:

- relevant seats highlighted from typed data;
- clue/result in center;
- minimal chrome;
- no Poisoned/Drunk/truth/recommendation-reason leakage;
- EvilInfo with no role ability remains safe.

### UI-R4 — Fortune Teller — COMPLETE / VERIFIED

The table remains visible while selecting two distinct targets.

After selection:

```text
one legal result
-> one button only

both Yes/No legal
-> recommended primary
-> other legal result immediately visible
```

Result identity remains bound to the exact selected pair and Foundation confirmation semantics remain intact.

### UI-R5 — Friday stabilization — ACTIVE

Freeze features and perform real-device walkthrough covering at least:

- Minion introduction;
- Demon introduction;
- pair recommendation and Manual;
- Chef / Empath;
- Fortune Teller;
- impaired/discretionary information;
- full-screen display return/navigation.

Field-test usability outranks optional animation/theme polish.

UI-R5 is not complete until the real-device walkthrough is recorded as acceptable and final full CI/R2 is green.

## 7. Testing / implementation strategy

Authority: `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Use risk-based tests-first.

High-value UI campaign contracts:

- square-table state attaches to stable seat identity;
- pair Manual still reaches the complete legal domain;
- impossible pair combinations never become selectable legal candidates;
- selected typed candidate is unchanged by presentation;
- changing a target invalidates stale dependent selection/result state;
- Fortune Teller fixed-vs-discretionary result presentation follows the complete legal Boolean domain;
- player-facing display never exposes hidden reliability/truth state;
- first-night EvilInfo regression remains protected.

Avoid brittle source-string or exact-pixel tests.

Prefer:

```text
pure seat/layout/state behavior
-> typed selection/presentation behavior
-> existing structured adapter/integration tests
-> Compose/runtime validation where it adds durable value
```

At logical checkpoints run `:app:testFast`; final field-test candidate receives normal CI/R2 and broad Android validation according to risk/classifier behavior. UI-R5 final candidate should use `[full-ci]` so full Android, ASP and Real Clingo validation are not silently skipped.

## 8. Source ownership / growth guards

`ClocktowerNightStepUi.kt` is already large. New reusable UI should prefer dedicated files.

`ClocktowerHostScreen.kt` remains a protected orchestration owner and must not become the home for new visual policy.

For large/truncated wiring changes follow the repository-approved route:

```text
small tests/new files via GitHub connector
-> exact-anchor one-shot workflow + separate Python patch script
-> focused evidence
-> :app:testFast checkpoint
-> exact diff allowlist
-> self-removal
```

Do not start App/Host decomposition as part of this deadline-driven campaign.

## 9. UI campaign scope guards

Do not expand this campaign into:

- EPI-MQ / Productive Uncertainty implementation;
- new recommendation ranking/scoring/diversity behavior;
- new legality ownership in recommendation/UI code;
- PlayerWorldSet production recommendation integration;
- A4/ZDD production rollout;
- broad Host/App decomposition;
- unrelated persistence/history redesign;
- broad theme/animation work;
- unsupported-script expansion merely for UI completeness.

If a correctness bug is discovered, isolate and characterize it rather than hiding it inside a visual refactor.

## 10. EPI-MQ after UI stabilization

After UI-R5 reaches a stable field-test checkpoint, restore EPI-MQ as the active algorithm campaign.

Primary authorities remain:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Target route remains:

```text
PlayerKnowledgeSnapshot
-> PlayerWorldSet BEFORE
-> hypothetical player-visible observation
-> PlayerWorldSet AFTER
-> epistemic metrics
-> misinformation-world quality
-> Productive Uncertainty
-> cognitive-consistency Recommendation Provider
```

Do not redesign the new table/Manual/display interaction when EPI-MQ resumes; the algorithm should improve recommendations behind the stable UI.

## 11. Documentation authority / new-conversation resume protocol

Active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
docs/COMPLETED_DEVELOPMENT_HISTORY.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Start the next implementation conversation by reading the first four files above, then re-query live `main` and the live UI-R1..R5 stack.

Recommended resume instruction:

```text
请读取根目录 AGENTS.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md。重新确认 live main 和 UI-R1..R5 stack，然后从 UI-R5 Friday field-test stabilization 继续。不要增加新功能；先完成 full CI/R2 和真机 walkthrough，发现 correctness bug 时单独窄修，不要开始 EPI-MQ，不要自行 merge。
```

## 12. Deferred / queued registry

| Area | Status |
|---|---|
| UI-R1 square-table foundation | COMPLETE / VERIFIED / DRAFT #70 |
| UI-R2 pair Manual redesign | COMPLETE / VERIFIED / DRAFT #71 |
| UI-R3 player information display | COMPLETE / VERIFIED / DRAFT #72 |
| UI-R4 Fortune Teller interaction | COMPLETE / VERIFIED / DRAFT #73 |
| UI-R5 field-test stabilization | ACTIVE — REAL-DEVICE WALKTHROUGH PENDING |
| EPI-MQ Productive Uncertainty | PAUSED UNTIL UI-R5 STABLE |
| ALG cognitive-consistency / PlayerWorldSet | PAUSED UNTIL UI-R5 STABLE |
| UX-R6 legacy ranking replacement | QUEUED AFTER EPI-MQ |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
