# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-13 Australia/Sydney  
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
UX-MODE-1 Beginner / Experienced mode             COMPLETE / merged via PR #120
UI-INFO-1 information filtering & layout          COMPLETE / merged via PR #121

EPI-MQ / Productive Uncertainty                   CURRENT — resume at EPI-MQ-0.5
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign documents are historical evidence, not default execution authority.

## 2. Immediate priority — EPI-MQ-0.5

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md`

Primary architecture/audit reference:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Primary product/algorithm references:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`;
- `docs/epistemic_reference_matrix.md`;
- `docs/asp_oracle_cross_validation.md`.

Immediate first step:

> **Reconfirm live `main` after PR #121 merges, then perform a read-only delta audit of the EPI-MQ ownership chain before editing production code.**

The first implementation target is the EPI-MQ-0.5 dynamic-script extensibility guard: a generic epistemic capability/evaluation boundary that preserves current Trouble Brewing exact behavior while representing unsupported future role/script semantics as explicit DEFERRED/UNSUPPORTED results.

## 3. EPI-MQ scope fences

EPI-MQ-0.5 may:

- define/refine a generic epistemic capability contract;
- expose READY / DEFERRED / missing-capability diagnostics;
- adapt current exact Trouble Brewing world reasoning behind that contract;
- add typed tests at the epistemic ownership boundary;
- perform only the ownership extraction required to establish that seam.

EPI-MQ-0.5 must not:

- change recommendation ranking or weights;
- change gameplay rules or legal candidates;
- activate A4/ZDD as production correctness authority;
- directly couple recommendation code to `B4DynamicPlayerWorldSetShadow`;
- leak Storyteller-hidden targets into recipient knowledge;
- implement Moonchild/Pukka mechanics merely to prove extensibility;
- reopen UI-INFO-1 without a new independently reproduced UI regression.

Stable rule:

> **`DEFERRED != UNSAT`. Unknown or unsupported script semantics must never be presented as exact contradiction or exact world count.**

## 4. UI-INFO-1 closeout

UI-INFO-1 is accepted from the product/device perspective and merged through PR #121.

Final PR head before merge:

`56b7137bb5f21e88b058295deb809d1d7608d5c5`

Merge commit on `main`:

`0cdcf14e39c1c16accf540382427e1264ef36b2a`

Final production verification head before docs-only closeout:

`99a141b54e1a4d274dbc354427bd49f3f11c9edc`

Latest production fix checkpoint:

`962ed3830d42fe4c6cdab711e9e0559ef82491bc`
`fix: restore Dawn announcement host surface`

At the final production verification head:

- CI run `34752163421` PASS;
- R2 run `34752163454` PASS;
- the Dawn restoration one-shot completed RED -> focused GREEN -> `:app:testFast` -> exact diff audit successfully;
- real-device acceptance was reported PASS after the Dawn-page restoration.

The final docs-only PR head also passed:

- CI run `34756727412` PASS;
- R2 run `34756727420` PASS.

### Stable UI-INFO-1 outcomes

The campaign established or repaired the following product behavior without changing gameplay semantics or legal-candidate authority:

- Beginner night guidance emphasizes wake role/player and immediate action rather than low-value explanatory text;
- Drunk presentation can route through the shown role's square-table surface while retaining actual Drunk truth/state ownership;
- Night and Day square-table flows share Activity-root fullscreen/navigation ownership rather than Night-specific platform Dialog ownership;
- real unreliable-information steps retain square-table host-surface ownership;
- transient zero/non-finite host-table geometry is rejected at the Compose renderability boundary rather than crashing strict geometry code;
- Beginner automatic Demon succession advances after the deterministic successor is applied instead of exposing a blank/manual ruling shell;
- unnecessary standalone Beginner Mayor redirect presentation was removed while Mayor semantics remain intact;
- Dawn remains a real host-facing announcement/review surface;
- the obsolete Dawn "full-screen announcement" button and secondary player-facing full-screen display page are removed;
- Day starts only after the Storyteller explicitly confirms the Dawn announcement.

The former active UI-INFO-1 handoff is historical and now lives under `docs/archive/`.

## 5. EPI-MQ architecture continuity

The 2026-09-11 EPI-MQ audit remains the design basis, but its old live baseline must not be assumed current.

Current intended sequence:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
            - generic epistemic world-engine/capability seam
            - explicit READY vs DEFERRED/UNSUPPORTED result
            - no EPI-MQ dependency on Trouble Brewing concrete classes

EPI-MQ-1    neutral hypothetical observation evaluator
            - exact
            - recipient-knowledge-safe
            - mutation-free
            - BEFORE / AFTER diagnostics
            - B4 shadow reuses the neutral owner

EPI-MQ-2    credibility / immediate contradiction / impairment-exposure gates

EPI-MQ-3+   productive-uncertainty metrics and ranking
```

No recommendation weights or production selections change during EPI-MQ-0.5 or the first neutral evaluator extraction.

Current exact correctness authority remains:

```text
Static/setup exact correctness:
TroubleBrewingWorldEnumerator + EnumeratedWorldSet

Historical/multi-night exact correctness:
EnumeratedHistoricalExactBaseline + EnumeratedHistoricalWorldReplay
```

A4/ZDD remains shadow/representation work until an explicit later cutover.

## 6. Testing continuity

Follow `docs/TESTING_STRATEGY.md`.

For EPI-MQ-0.5:

- use typed T0 tests at the epistemic ownership boundary;
- T1 `:app:testFast` at the logical checkpoint;
- run affected history/enumeration validation when ownership or exact behavior changes;
- Real Clingo/T4 remains an acceptance gate when exact/oracle semantics change, not for a purely mechanical interface extraction;
- do not create Host/UI source-string tests for EPI-MQ capability behavior.

## 7. Default reading order for the next development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md`;
5. `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`;
6. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
7. query live `main`;
8. inspect only the live ownership surfaces required for the EPI-MQ-0.5 delta audit.

## 8. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
