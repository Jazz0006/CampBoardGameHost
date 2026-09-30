# DLB + App/Host Decomposition Implementation Route — 2026-09-29

> Date: 2026-09-29 Australia/Sydney  
> Last synchronized: 2026-09-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT IMPLEMENTATION ROUTE / ARCHITECTURE AUTHORITY FOR DLB**  
> Baseline at route creation: live `main`; always query the actual HEAD before executable work.  
> Inputs:
> - `APP_HOST_DECOMPOSITION_PLAN_AUDIT_2026-09-28.md`
> - `DRUNK_LATE_BINDING_AND_FIRST_NIGHT_DECISION_SEQUENCE_AUDIT_2026-09-28.md`
> - root `AGENTS.md`
> - `TESTING_STRATEGY.md`
>
> This route changes product/setup lifecycle semantics. It supersedes older assumptions that the exact Drunk seat/shown identity is already committed before seat assignment or is an immutable input to SDE. It does **not** silently modify the frozen `BEGINNER_CONSERVATIVE_V1` policy.

## 1. Product decision

Trouble Brewing with a Drunk now follows this contract:

```text
preset / visible-roster intent
-> realize visible Townsfolk identities
-> assign shown identities to seats
-> generate rules-owned legal Drunk-seat candidates
-> evaluate candidates against hypothetical effective setups
-> choose/confirm Drunk
-> commit final actual/shown role map
-> reveal player identities
-> run staged first-night decisions at their latest safe dependency barriers
```

The template no longer binds one seat/token to Drunk before seating. The existing `drunk_as_options` asset remains a transitional visible-roster source only; after seating, the added Townsfolk identity has no privileged Drunk ownership.

Experienced Storyteller mode may manually choose any rules-legal Drunk candidate. Beginner mode may auto-select only after the production cutover gate in section 6 is satisfied.

## 2. Combined architecture decision

The 2026-09-28 decomposition audit is a guardrail, not a prerequisite campaign.

DLB is the product/architecture mainline. Decomposition is performed only where the new lifecycle creates a stable natural owner or where an independent maintenance slice is already clearly safe.

Therefore:

- do **not** first execute A1 -> A2 -> H1 -> H2 -> A3 and only then begin DLB;
- do **not** bundle independent App storage cleanup into DLB production PRs;
- do **not** revive the D6/R3 generic transaction-controller idea;
- do **not** create a generic setup/first-night effect controller;
- do **not** introduce a God `HostState`, `AppContext`, `StorytellerDecisionContext`, callback bag or second session authority.

The target is smaller **change context radius**, not a line-count campaign.

The long-horizon ownership target is now recorded in `CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md`: Setup Generation, the canonical Game Session/Game Engine, and the read-only Storyteller Recommendation Engine are separate responsibilities connected by typed contracts. This does **not** expand DLB scope. DLB-5H1 remains presentation-only; DLB-6 and the later H2 re-audit should retire or narrow stale contracts in the direction of that target without creating a second session authority or a God recommendation context.

## 3. Architecture pre-flight

Architecture pre-flight:
- current owner: Trouble Brewing preset/selector/deal setup owners, App setup startup wiring, Host first-night orchestration, SDE/recommendation adapters.
- proposed responsibility: late-bound Drunk assignment plus dependency-barrier first-night sequencing, using narrow rules/setup/SDE/session/presentation owners.
- authoritative state owner(s): rules/setup own legality; SDE evaluates legal alternatives; `ClocktowerGameSession`/committed setup own canonical game truth after commit; player-controlled choices remain player-owned; Compose owns only local transient interaction state.
- narrow typed input/output seam: realized shown-seat assignment -> legal Drunk candidate set -> hypothetical projected state per candidate -> typed decision/trace -> canonical Drunk commit; first-night barriers consume committed facts and produce typed ready/commit states.
- keep in current owner / extract: extract new pure/legal/projection/decision seams; keep App/Host as wiring/route/lifetime owners; do not extract heterogeneous transaction application or all setup effects.
- reason: DLB changes ownership and timing. The stable boundary must be established before adding new UI/runtime behavior to the protected App/Host files.

## 4. Implementation sequence

### DLB-0 — typed intermediate setup contract

Introduce a setup stage that contains:

- selected preset / realized visible roster;
- whether a Drunk exists;
- shown identities assigned to stable seats;
- **no committed Drunk seat yet**.

Tests must prove a Drunk preset creates one extra visible Townsfolk identity without binding that identity to Drunk.

### DLB-1 — visible-roster deal cutover

Reinterpret the selected `drunk_as_options` item as an added/replacement visible Townsfolk identity.

Retire `selectedDrunkShownRole` from the new runtime setup contract.

Preserve fallback equivalence: selecting the transitional added Townsfolk as Drunk must reproduce the previous final actual/shown roster semantics.

### DLB-2 — legal candidate domain + hypothetical projector

Rules/setup code owns legal Drunk-seat enumeration.

For each legal candidate, a pure projector produces the hypothetical effective setup:

```text
candidate.actualRole = Drunk
candidate.actualType = Outsider
candidate.actualAlignment = Good
candidate.shownRole = original dealt Townsfolk
all other seats unchanged
```

The projector must never mutate canonical session state.

This projector is **not** the same responsibility as Host's runtime `dynamicStorytellerState()`; do not merge them into one broad context.

### DLB-3 — SDE Drunk decision + DecisionTrace, shadow first

Add a typed Drunk-seat decision surface and trace/replay correlation.

SDE consumes rules-legal candidates and existing exact/whole-ecology consequence machinery.

Initially this is **shadow-only**. Do not alter `BEGINNER_CONSERVATIVE_V1` weights, survivor ordering or selector semantics to manufacture a preferred Drunk.

Evidence collection in ClocktowerEvidenceLab for reconstructable expert Drunk-assignment choices is the calibration input for this surface.

### DLB-4 — canonical Drunk commit before reveal — COMPLETE / ACCEPTED

Accepted executable checkpoint: `5606371c68b97beb01418ede2bb2c19db7e69053`; CI #3580 / R2 #3313 GREEN.

Create one canonical commit boundary:

```text
shown-seat assignment
+ confirmed legal Drunk candidate
-> final CommittedClocktowerSetup / GameState
```

Only after this boundary may player identity reveal and first-night prewarm begin.

Rotation/completion persistence must consume this final committed setup, not the pre-Drunk intermediate plan.

### DLB-4A — Storyteller Drunk selection UX — COMPLETE / ACCEPTED

Accepted executable checkpoint: `c85448831e73c82868e118c7f47a0ed889267c0c`; CI #3586 / R2 #3318 GREEN.

```text
shown identities assigned to seats
-> legal candidates + optional authorized recommendation
-> EXPERIENCED: assisted "Choose the Drunk" interaction
-> BEGINNER: production automatic-selection authority only after cutover gate
-> one shared canonical commit
-> player identity reveal
```

Manual UI may select only rules-legal candidates. No Drunk-ordering policy is currently authorized, so the Experienced request carries no recommendation and exposes the full legal domain.

Until the later production cutover gate is satisfied, Beginner preserves the already-existing DLB-4 compatibility-confirmed candidate only as a transitional playable baseline. That compatibility path is not a policy recommendation, does not read `DRUNK_ASSIGNMENT_SHADOW_V1`, and must not be described as the authorized automatic-selection cutover.

Do not persist the unconfirmed setup UI draft into Emergency Recovery. If the process dies before Drunk confirmation, restart/re-enter setup rather than restoring a half-committed game. Emergency Recovery begins from finalized current-version setup/game truth.

### DLB-5 — staged first-night dependency planner — COMPLETE / ACCEPTED

Merged PR: **#183 — `DLB-5: stage first-night dependency barriers`**; squash merge `6293a3bb94778338db61f5a1708a1d283d6e8b6f`.

Latest production executable checkpoint: `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5`; CI #3600 / R2 #3331 GREEN. Final exact-head T4 acceptance: `3fd1714ce2e18b969e5039bcf6a58f8775745b9d`; CI #3603 / R2 #3334 GREEN, including Android `testFull + assembleDebug`, ASP contracts and Real Clingo cross-validation.

Implemented:

- **DLB-5.1 COMPLETE:** pure typed dependency planner for latest-safe first-night setup commitment;
- **DLB-5.2 COMPLETE:** Red Herring no longer commits at setup recommendation readiness; the current legal recommendation commits only at the earliest semantic/observation barrier, with committed value fed back as a recommendation lock;
- **DLB-5.3 COMPLETE:** Demon bluffs no longer commit at setup-plan application; the exact current legal recommended triple commits at the Demon-info presentation barrier and Pending/Invalid states fail closed;
- **DLB-5.4 COMPLETE:** the existing Poisoner / first-night information lifecycle was audited and proved correct with no production rewrite: poison confirmation advances canonical revision, invalidates only unshown drafts, preserves displayed observations, and regenerates later information from current effective poison state plus committed history.
- **DLB-5.5 COMPLETE:** obsolete setup auto-apply wiring for Red Herring / Demon bluffs is removed while setup recommendation/shadow evaluation remains available.

The Red Herring rule remains a generic **observation/dependency barrier**, not a named `if (Spy)` heuristic. Current Trouble Brewing Spy is one concrete observer; future Grimoire viewers inherit the same semantic barrier.

DLB-5 is COMPLETE / ACCEPTED after 5.4 closed with no production gap and final exact-head T4/R2 acceptance passed.

### DLB-5H1 — first-night evil-information presentation extraction — COMPLETE / ACCEPTED

Accepted executable checkpoint: `47136b7d03d72452b70bf3defa847578b30fb011`; CI #3609 / R2 #3337 GREEN with Android FAST executed successfully.

The boundary audit confirmed a narrow cohesive seam and production was cut over without behavior changes:

```text
already-projected evil-team presentation facts
+ existing committed bluff presentation resolution
+ localization/language
-> prepared Minion/Demon ClocktowerNightStepUi presentation steps
```

`ClocktowerFirstNightEvilInformationPresentation` now owns localized Minion/Demon presentation assembly. `ClocktowerHostScreen` still owns upstream actor/fact projection and the materializer registry; dependency ordering, setup legality, recommendation selection, Demon-bluff commit timing and canonical session mutation remain in their prior owners.

H1 also retires the transitional presentation adapter that accepted ignored setup-plan/style arguments. Player presentation now consumes the committed Demon-bluff role-name state directly, while setup plans/style remain only on the separate commit-at-barrier path.

Presentation still must not choose bluff candidates, mutate session state, own setup legality, or own dependency timing.

### DLB-6 — retire old contract

After production callers are cut over:

- remove obsolete `selectedDrunkShownRole` production/persistence ownership;
- remove Trouble Brewing pre-seat Drunk commitment;
- retire obsolete score/history coupling that treats the transitional added Townsfolk as privileged;
- update replay/DecisionTrace/current-version persistence contracts;
- rerun producer/consumer searches and remove superseded source-shape tests.

No legacy Recovery-format migration is required; current-only Recovery remains the product contract. Archive/history/rotation persistence are separate surfaces and must be deliberately migrated/retired rather than assumed equivalent to Recovery.

### DLB-7 — acceptance

Required coverage includes:

- legal visible roster for every Drunk template;
- every eligible dealt Townsfolk can be projected as Drunk;
- no non-Townsfolk legal candidate;
- exactly one committed Drunk when required;
- shown identity remains the dealt Townsfolk identity;
- Drunk-as-Fortune-Teller wakes as Fortune Teller but does not create actual-Fortune-Teller Red Herring ownership;
- actual Fortune Teller gets a Red Herring before any observer/dependency requires it;
- poison confirmation changes canonical revision and invalidates stale downstream plans;
- DecisionTrace/replay correlates the selected Drunk candidate;
- current-version Recovery restores finalized setup only;
- non-Drunk templates remain behaviorally unchanged.

## 5. Decomposition maintenance lane

Independent decomposition items remain separate from the DLB behavior campaign:

- **A1 App preferences storage:** safe independent maintenance; may run before or after DLB, but is not a prerequisite.
- **A2 archive storage:** separate maintenance PR; preserve archive-specific durability/order/capacity semantics.
- **H1:** COMPLETE / ACCEPTED through DLB-5H1; localized first-night evil-information presentation is extracted without moving dependency, legality, recommendation or session ownership.
- **H2 dynamic recommendation-state projection:** re-audit after DLB-2/DLB-5. Do not conflate it with the Drunk hypothetical projector.
- **A3 presentation catalog:** defer until DLB setup/presentation fan-out stabilizes.
- **R3 transaction extraction:** remains NO-GO.
- **generic setup-effect owner:** remains NO-GO.
- **Recovery R7:** remains a separate optional ownership follow-up and is not part of DLB unless new evidence proves a direct need.

Each independent architecture slice gets its own branch/PR and acceptance evidence.

## 6. Shadow -> production cutover gate

A shadow recommendation is not production authority.

Before Beginner mode may automatically commit the SDE-selected Drunk candidate, the Drunk-assignment surface must have:

1. stable rules-owned legal candidate domain;
2. stable hypothetical consequence projection;
3. DecisionTrace/replay coverage over real/reconstructable cases;
4. explicit fallback behavior;
5. evidence sufficient for any ordering/preference actually consumed;
6. an explicitly versioned Drunk-assignment selection contract;
7. affected T2/T4 validation and independent remote acceptance.

The frozen `BEGINNER_CONSERVATIVE_V1` must not silently absorb a new Drunk candidate preference, rejection or survivor-refinement semantic. If production Drunk selection requires new policy semantics, version that decision surface explicitly rather than mutating V1.

The first formal cutover audit is COMPLETE / NOT PASSED. Conditions 1–4 are satisfied for the current legal/projection/replay/fallback contracts; condition 5 fails because the current evidence does not authorize a general candidate ordering; condition 6 fails because `DRUNK_ASSIGNMENT_SHADOW_V1` is deliberately deferral-only rather than a production selection contract; condition 7 is therefore not reached. Authority: `docs/DLB_DRUNK_ASSIGNMENT_PRODUCTION_CUTOVER_GATE_AUDIT_2026-09-30.md`.

Until a later cutover audit passes, production must keep the documented `CompatibilityImmediate` Beginner fallback. DLB-6 retirement is also blocked because that transitional fallback still depends on compatibility state that DLB-6 is intended to remove.

## 7. Persistence boundaries

Keep these distinct:

- Emergency Recovery: current format + exact compatibility + short horizon; finalized game/setup truth only.
- setup rotation/completion history: product history used by setup selection; any old `selectedDrunkShownRole` meaning must be explicitly migrated/retired.
- Game archive: separate product surface; A2 owns only archive storage if implemented.
- DecisionTrace/replay: diagnostic/calibration evidence; read-only relative to canonical game truth.

"Recovery needs no old-format migration" must never be generalized into "all persisted historical records need no deliberate migration decision."

## 8. Documentation authority and supersession

This route supersedes older active statements that:

- Drunk shown identity is setup-persistent before seat assignment;
- exact Drunk identity is an input to setup recommendation/SDE;
- Red Herring is universally precomputed with the initial setup bundle;
- first-night Storyteller-controlled decisions can be treated as one immutable setup-time bundle.

Older documents remain valid only for historical evidence, completed implementation checkpoints, or unaffected architecture principles.

In particular:

- `SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md` remains historical evidence for committed-clue immutability and poison revision behavior, but its pre-DLB Drunk ownership conclusion is superseded.
- `SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md` remains evidence/policy background, but its setup-persistent Drunk and precommit sequencing model is superseded.
- the old TBSP production cutover contract is archived and no longer normative.

## 9. Validation and change-scope discipline

Follow `TESTING_STRATEGY.md`.

DLB changes behavior/product semantics and architecture ownership; mechanical cleanup should be separated where practical.

For each slice:

- use existing typed tests where they already protect the contract;
- add RED only for genuinely new/uncovered stable behavior;
- prefer typed seams over source-string wiring tests;
- run T0 -> T1 and affected T2 as the slice warrants;
- use T4/CI/R2 for logical acceptance checkpoints according to the test strategy;
- inspect exact changed-file allowlist and semantic diff before acceptance.

Do not combine A1/A2/A3 cleanup, broad renaming, unrelated dead-code cleanup or Recovery R7 with a DLB behavior slice.

## 10. Immediate execution order

```text
document authority convergence (this route)
-> DLB-0
-> DLB-1
-> DLB-2
-> DLB-3 shadow + trace/replay
-> DLB-4 canonical commit COMPLETE
-> DLB-4A experienced assisted UX COMPLETE
-> DLB-5.1 / 5.2 / 5.3 / 5.4 / 5.5 COMPLETE
-> DLB-5 final acceptance + PR #183 merge COMPLETE / ACCEPTED
-> DLB-5H1 narrow presentation extraction COMPLETE / ACCEPTED
-> Drunk-assignment production cutover gate audit COMPLETE / NOT PASSED
-> targeted recommendation-context capability + candidate-ordering evidence work NEXT
-> new production-capable versioned Drunk policy only when evidence permits
-> re-run cutover gate
-> Beginner automatic Drunk authority only if PASS
-> DLB-6 old-contract retirement only after replacement selection/fallback authority exists
-> DLB-7 acceptance
-> re-audit H2 / A3
```

A1/A2 may proceed as separate maintenance work without blocking this sequence.

C5 / `BEGINNER_CONSERVATIVE_V2` for the existing first-night policy remains independently blocked on qualifying E3/E4 evidence. DLB evidence acquisition and C5 evidence acquisition may run in parallel but must not be conflated.
