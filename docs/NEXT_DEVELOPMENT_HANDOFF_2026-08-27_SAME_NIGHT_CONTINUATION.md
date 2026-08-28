# Next Development Handoff — SNE-7.9E Restore + Durable Dawn Integration

> Date: 2026-08-28  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Accepted 7.9D checkpoint: `06f06fb5c0689983c9baaf0bfe1765a8c69a8d16`  
> Handoff status: **7.9A/B/C/D COMPLETE / BROAD GREEN; 7.9E ACTIVE TESTS-FIRST; PR #54 stays draft/unmerged**

## 1. Startup contract

Read before changing code:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`.

Then re-query live `main`, PR #54 head/state and latest checks. Never assume a SHA in this handoff remains live after later work.

## 2. Corrective campaign state

```text
SNE-7.9A  Mayor redirect dependency invalidation          COMPLETE / GREEN
SNE-7.9B  Chambermaid stale-target revalidation           COMPLETE / GREEN
SNE-7.9C  canonical night-death resolution                COMPLETE / BROAD GREEN
SNE-7.9D  succession legality / Dawn                      COMPLETE / BROAD GREEN
SNE-7.9E  real restore + durable Dawn integration          ACTIVE / NEXT
```

Do not mark PR #54 ready or merge it while 7.9E remains active.

## 3. Accepted 7.9C authority

The real Trouble Brewing night-death consumers now converge on:

```text
confirmed checkpoint facts
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death
```

The three previously duplicated production consumers are now:

```text
Dawn durable materialization
public-alive observation preflight
Host death presentation / resolved mechanical event trigger
```

Key production checkpoints:

```text
7139c8f9be8613ac082eafacf484f2c9c84a54f0  Dawn materialization authority
e6c3d9ef7ab50e9e07d0ce570120bd63c68571af  public-alive preflight authority
b6185ccf6b23583c112b040f831e65f2724f1035  Host death consumer authority
```

Do not recreate independent Soldier / Monk / Demon-poison / Mayor death gates in App or Host.

## 4. Accepted 7.9D succession authority

7.9D closed both production Dawn succession and reconstruction legality findings.

Tests-first chain:

```text
8bf44fc78b56b9e6e18971720dbf32304d56724f  RED — reject illegal restored successor
b3eaa92a39eefd0859e908d91b2214bec5811614  canonical Imp succession resolver
9f24652e8a1010092c86e1ab85e4271dee7034d9  pre-self-kill state correction
ba7772218126a6116c9eef96a62338c5e82948d8  GREEN — reconstructor legality

d396a80d6b863486fdb3068ac8decf549bb26841  RED — Forced / None planner coverage
f982e50ca702fe95f623581e0e62347849bf888a  GREEN — planner consumes None / Forced / Choice

50fed86a4b0f33d2bb31eaff62a64af9ab4abdd1  App self-kill Dawn path consumes resolver + planner

943b65b7938930736aae3f307f4894b1859e3a58  RED — successful self-kill with no successor lost mechanical death
6c28d418702024cc614bc6cb04091238aa663c3a  expose canonical self-kill attack outcome + succession
fe7c8761e7c9a7404b2b73964a2c1f1ae38f503e  GREEN — death projected independently from successor existence

06f06fb5c0689983c9baaf0bfe1765a8c69a8d16  [full-ci] protective checkpoint
```

Canonical contract:

```text
confirmed Imp self attack
→ DemonNightAttackSemantics
→ attack actually self-kills?
   no  → no mechanical death, no succession
   yes → reconstruct old Imp MechanicalDeath
       → DemonSuccessionSemantics
          None   → no successor role change
          Forced → forced successor
          Choice → confirmed target must be inside canonical targetSeats
→ NightDawnResolutionPlanner owns continuation / pending new Demon / Dawn gating
```

The functioning-Monk-protects-Imp case is explicitly covered so `DemonSuccessionResolution.None` cannot be misread as proof that the old Imp died.

## 5. 7.9D validation evidence

### Expected RED

```text
CI #911
Android FAST selected
913 tests / exactly 1 intended failure
NightTransactionReconstructorSuccessionLegalityTest
FULL Android step skipped
R2 #838 SUCCESS
```

### GREEN

```text
CI #913 SUCCESS
Android FAST SUCCESS
FULL Android step skipped
CI gate SUCCESS
R2 #840 SUCCESS
```

### Full acceptance

```text
CI #914 SUCCESS
Android full unit tests + assembleDebug SUCCESS
ASP contract tests SUCCESS
Real Clingo cross-validation SUCCESS
CI gate SUCCESS
R2 #841 SUCCESS
```

This also validates the hardened CI routing:

```text
ordinary app micro-commit → FAST
[full-ci] checkpoint      → FULL + assemble + ASP + Clingo
```

## 6. Active 7.9E problem

The required real flow is:

```text
persist / restore
→ NightTransactionReconstructor
→ reconstructed same-night state
→ canonical death / succession resolution
→ Dawn durable materialization
→ ActionFact / observation
→ phase transition
```

Current App audit shows:

- `activeGameSnapshotJson()` persists a `ClocktowerNightCheckpoint` through `persistedValues()`;
- `restoreSavedGame()` builds `restoredNightCheckpoint` through `ClocktowerNightCheckpoint.fromPersistedValues(...)`;
- restore validates semantic history with `requireCompatible(...)`;
- restore copies all raw checkpoint fields back into App state;
- **real App restore does not call `NightTransactionReconstructor`**;
- existing lifecycle integration smoke stops before App-owned durable Dawn side effects.

Therefore the next work is not a generic persistence rewrite. It is to connect the already-valid durable checkpoint to the already-valid reconstructor and then prove the resulting canonical Dawn consequences are materialized exactly once.

## 7. 7.9E first RED target

Start with a directly callable typed production seam, not a large Compose rewrite.

Minimum scenario:

```text
restored unfinished Trouble Brewing night
confirmed Imp self-kill
confirmed legal successor
canonical interaction plan includes successor interaction
```

Required behavior:

```text
restore composition
→ NightTransactionReconstructor
→ old Imp mechanically dead
→ successor current role = Imp
→ public/base GameState remains unchanged
```

This must prove that real restore activates derived same-night mechanics without early durable/public mutation.

After that GREEN, extend the same production composition through:

```text
canonical Dawn death / succession intent
→ durable death / role change exactly once
→ ActionFact exactly once
→ public AliveAt(false) observation exactly once
→ phase transition exactly once
```

Do not jump straight to source-string assertions or mutate `cards.eliminatedRound` during restore to make the test pass.

## 8. Architecture invariants

- `ClocktowerNightCheckpoint` remains the sole durable unfinished-night state owner;
- `GameState` / existing session timeline remain durable game-history authority;
- restore reconstructs from durable inputs, not replayed `NightResolutionEvent` commands;
- mechanical death and public announcement remain distinct;
- same-night `RoleChanged` is derived before Dawn materialization, not written early to base cards;
- stable seat identity never comes from filtered-list reindexing;
- draft fields cannot impersonate confirmed facts;
- rules determine legality; UI only presents/selects legal choices;
- pure semantics may support future cases, but production activates only validated slices;
- no second persisted coordinator and no event-sourcing architecture is introduced.

## 9. Scope exclusions

Do not expand 7.9E into:

```text
generic non-self Demon-death succession
custom-script Demon succession
A4/B4 authority promotion
recommendation tuning
App-root decomposition
unrelated Host/UI cleanup
PR merge/readiness work
```

## 10. Writer / CI contract

Small tests/docs/source:

```text
Chat + GitHub connector
```

Large App edits:

```text
exact live-head lock
→ complete source candidate
→ detached blob/tree/commit
→ exact parent/diff/scope audit
→ fast-forward only after audit passes
```

The GitHub connector has successfully handled the ~228 KiB App file; size alone is no longer a reason to switch tools. Never construct a whole-file replacement from truncated content.

Tests-first cadence:

```text
RED
→ prove exact intended failure
→ minimal GREEN
→ focused validation where applicable
→ remote parent/diff/scope audit
→ ordinary app micro-commits use FAST CI
→ [full-ci] only at logical acceptance checkpoints
```

## 11. Exact next-start instruction

```text
1. re-query live PR #54 head after the docs-only synchronization commits;
2. audit existing lifecycle integration test and any current restore/session adapters;
3. design the smallest typed 7.9E RED for restored checkpoint → real reconstruction composition;
4. prove no public/base mutation while reconstructed old-Imp death/new-Imp role is effective;
5. minimal GREEN through a reusable production seam;
6. only then extend to App-owned durable Dawn materialization and exactly-once timeline/observation effects;
7. keep PR #54 draft/unmerged.
```
