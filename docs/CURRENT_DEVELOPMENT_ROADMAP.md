# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-12 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition                               COMPLETE / merged
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / merged via PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / merged via PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / merged via PR #119
EPI-MQ-0 baseline / ownership re-audit            COMPLETE — old EPI-MQ-1 = MODIFY
UX-MODE-1 Beginner / Experienced mode             COMPLETE / accepted / merge via PR #120

UI-INFO-1 information filtering & layout          CURRENT — next focused UI campaign
EPI-MQ / Productive Uncertainty                   QUEUED after UI-INFO-1 unless reprioritized
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign documents are historical evidence, not default execution authority.

## 2. Immediate priority — UI-INFO-1

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-12_UI_INFO_FILTERING_AND_LAYOUT.md`

Primary product reference:

`docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

Immediate first step:

> **Read-only audit of information filtering, hierarchy, typography, spacing and overflow across the live Storyteller UI before changing production Compose.**

The audit should classify visible information by operational value and identify reusable layout seams. Prefer removing/reducing low-value information over shrinking fonts.

Primary phone portrait remains the target, including dense 12–15 player layouts.

## 3. UI-INFO-1 scope fences

UI-INFO-1 may change:

- which already-available information is shown on a given Storyteller surface;
- information hierarchy and grouping;
- headings, badges and redundant explanatory copy;
- typography, wrapping, spacing and center-content composition;
- reusable presentation/layout primitives when the audit proves common ownership.

UI-INFO-1 must not change:

- gameplay rules;
- legal candidate domains;
- recommendation ranking semantics;
- deterministic automatic-ruling identity;
- player-owned vs Storyteller-owned choice authority;
- global Previous / Host Tools / Next navigation placement;
- Android system-bar/inset behavior without a newly reproduced regression;
- EPI-MQ / Productive Uncertainty algorithms.

## 4. UX-MODE-1 closeout

UX-MODE-1 is accepted from the product/device perspective and PR #120 is authorized for merge after documentation closeout and passing merge gates.

Final branch checkpoint before docs closeout:

`57a0905153b06a0cfe809d36af4b0e0403fe8e3b`

At that checkpoint:

- PR #120 was open, Draft and mergeable;
- CI run `34664124993` PASS;
- R2 run `34664124978` PASS;
- real-device POCO X8 Pro acceptance reported no remaining issue for the final safe-area fix.

### Stable UX-MODE product contract

There is one user-facing Experienced-mode switch. Fresh/default state is Beginner.

Critical invariant:

> **Beginner and Experienced share one rules / legal-candidate / recommendation pipeline. Mode changes interaction authority and presentation only.**

Until EPI-MQ replaces the temporary provider behavior:

- both modes consume the same internal `RecommendationStyle.AGGRESSIVE` provider;
- Beginner auto-uses Top-1 for Storyteller-owned strategic decisions;
- Experienced exposes legal manual alternatives;
- player-owned choices remain manual;
- automatic probabilistic decisions use stable semantic identity and do not re-roll on recomposition/navigation/restore.

Temporary policies remain:

```text
Spy/Recluse registration:
90% legal special / false registration
10% actual registration

Mayor:
90% redirect to eligible living Townsfolk when one exists
10% Mayor dies
otherwise Mayor dies

non-forced Demon succession:
Baron 4 > Scarlet Woman 3 > Spy 2 > Poisoner 1
```

Rules legality remains authoritative upstream.

### Acceptance fixes completed inside PR #120

The campaign also closed real-device/UI regressions discovered during acceptance, including:

- first-night evil-team / bluff square-table presentation convergence;
- Spy/Recluse automatic-selection audit ownership crash family;
- removal/retirement of old generic Storyteller UI islands where normal production flow had moved to square-table ownership;
- Day Overview / Nomination / Vote / Slayer / Artist / Klutz navigation moved below the square table into a shared Day scaffold;
- full-screen night Compose dialogs explicitly own edge-to-edge window fitting;
- night bottom controls protected from device navigation-bar overlap;
- Day bottom navigation changed from `navigationBarsIgnoringVisibility` to visibility-aware `navigationBars`, preventing permanent dead space on immersive/auto-hide devices while still respecting visible navigation bars.

Real-device testing on POCO X8 Pro reported the final Day/navigation safe-area behavior as normal.

## 5. EPI-MQ direction after UI-INFO-1

EPI-MQ-0 remains complete as an architecture/ownership audit.

Authoritative design/audit record:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

When resumed, re-audit against then-live `main` and continue approximately:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility / contradiction / impairment-exposure gates
EPI-MQ-3+   productive-uncertainty metrics and ranking
```

Do not activate A4/ZDD or replace the recommendation provider as part of the initial evaluator extraction.

## 6. Testing continuity

Follow `docs/TESTING_STRATEGY.md`.

For UI-INFO-1:

- T0 focused contracts during each slice;
- T1 `:app:testFast` after meaningful production checkpoints;
- broader Android/build validation for shared/high-fanout UI primitives as risk requires;
- real-device validation for visual readability/density claims that unit tests cannot prove.

Do not weaken rules/semantic tests merely to simplify presentation.

## 7. Default reading order for the next development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-12_UI_INFO_FILTERING_AND_LAYOUT.md`;
5. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
6. query live `main`;
7. inspect only the live UI owners required for the read-only audit.

## 8. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
