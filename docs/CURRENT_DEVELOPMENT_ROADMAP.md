# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline:
0eafa9770ca9391928419dadf835f17a1ab00d29

current branch:
codex/trouble-brewing-setup-presets-v2

current Draft PR / CI carrier:
PR #57 — TBSP: integrate Trouble Brewing setup presets
OPEN / DRAFT / NOT MERGED

last fully validated logical code/test checkpoint:
f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4

checkpoint meaning:
TBSP-6I cutover acceptance matrix accepted
NGJ typed regression added
CI #1148 / run 33341819960 SUCCESS
R2 #1071 / run 33341819962 SUCCESS

current work:
TBSP-6J behavior-preserving cleanup

active handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6J_CLEANUP.md

normative TBSP rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Any commits after `f7e877...` that modify documentation only are documentation carriers and are not a new code/test checkpoint.

## 2. Current campaign status

```text
TBSP-0 documentation / campaign plan                         COMPLETE
TBSP-1 final dataset asset + parser + semantic validator     COMPLETE
TBSP-2 deterministic history-aware preset selector           COMPLETE
TBSP-3 deterministic exact deal materialization              COMPLETE
TBSP-4 recommendation lock / selector-owned Drunk identity   COMPLETE
TBSP-5 durable cross-game rotation-history store             COMPLETE
TBSP-6A active setup provenance codec                        COMPLETE
TBSP-6B production setup preparer                            COMPLETE
TBSP-6C production deal-role resolver                        COMPLETE
TBSP-6D Trouble Brewing production start cutover             COMPLETE
TBSP-6E active-game provenance persist/restore               COMPLETE
TBSP-6F true-completion rotation-history wiring              COMPLETE
TBSP-6G-A setup recommendation prewarm core                  COMPLETE
TBSP-6G-B reveal-window production wiring                    COMPLETE
TBSP-6H First Night background precompute                    COMPLETE
TBSP-6I cutover acceptance matrix                            COMPLETE
TBSP-6J cleanup                                              CURRENT
TBSP-6K final full acceptance                                NOT STARTED
A3 immutable setup snapshot                                  DEFERRED UNTIL TBSP ACCEPTANCE
```

PR #57 remains Draft throughout 6J/6K. Do not mark Ready or merge without explicit authorization.

## 3. Protected predecessor correctness baseline

TBSP must preserve the accepted correctness work already on the branch/base, including:

- First Night Fortune Teller base/current-role authority;
- Other Night Fortune Teller canonical same-night effective-state projection;
- current living-Demon UI authority distinct from pending-succession reconstruction;
- poisoned Spy fail-safe behavior: normal wake, no fabricated Grimoire, no false Grimoire observation persistence;
- Dawn poison exactly-once and retry-convergent materialization;
- next-night/Dusk poison expiry exactly-once, restore/retry convergence and durable ordering before Night phase/round advancement.

Do not reopen these semantics because TBSP touches initial setup or First Night lifecycle timing.

## 4. Frozen TBSP authority and architecture

Frozen dataset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
player counts: 5..15
preset count: 480
Drunk presets: 208
Drunk options per Drunk preset: exactly 3
```

Do not regenerate or reformat it.

Accepted authority chain:

```text
frozen curated preset dataset
-> history-aware selector
-> selected preset + selector-owned Drunk shown role
-> deterministic tb-seat-v1 deal materialization
-> commit actual/shown identities
-> immediate PassPhone / RevealCard
-> reveal window prewarms setup recommendation and First Night work off main thread
-> exact consumer reuses READY result, safely awaits BUSY at point of use, or recomputes on MISS/stale input
```

Background work must never reroll or mutate committed actual/shown identities.

No Greater Joy remains on its existing setup-generation path.

## 5. Accepted TBSP ownership through 6H

### TBSP-1 through 5

Accepted typed owners prove:

- dataset/schema/pool identity and role legality;
- exact team composition including Baron represented once;
- deterministic history-aware preset + Drunk-option selection;
- exact-repeat rejection, player-count overlap thresholds, decay weights and soft rotation penalties;
- deterministic `tb-seat-v1` deal materialization;
- actual Drunk remains Drunk and shown Drunk role equals selector-owned choice;
- recommendation lock cannot replace the selected Drunk shown identity;
- durable cross-game rotation history is gameId-idempotent, conflict-safe, bounded and isolated by player-count/dataset/schema.

Rotation history is diversity memory, not A3 immutable setup snapshot authority.

### TBSP-6A through 6F

Accepted production cutover proves:

```text
Trouble Brewing start
-> newClocktowerSeed()
-> load/validate final dataset
-> load matching rotation history
-> prepare preset + Drunk shown role + exact deal
-> resolve committed PlayerCards
-> reset with prepared seed
-> persist exact setup provenance
```

Restore decodes exact provenance and does not invoke selector/preparer. Older supported saves without TB provenance restore with provenance `null` and do not fabricate an initial selection.

True-completion rotation-history gate is:

```text
Clocktower
+ Trouble Brewing
+ gameOutcome != null
+ committed TB setup provenance
-> recordCompletedGame(clocktowerGameId, original selection)
```

Restart/abandon/archive without an outcome does not enter rotation history. Failed durable history persistence blocks clearing so retry can converge.

### TBSP-6G-B — reveal-window setup recommendation prewarm

Product commit:

```text
52378a6887553fb37692def96c1657110151f114
```

Accepted behavior:

- committed TB deal enters reveal before recommendation prewarm is dispatched;
- prewarm runs off main thread;
- exact request reuses READY result;
- stale/mismatched request cannot be consumed as current;
- cache miss retains a safe existing computation path;
- non-TB behavior remains unchanged.

### TBSP-6H — First Night background precompute

Production code commit:

```text
ff1c99fe97552dc65f3d1bf8326bdb451c8e25a0
```

Docs checkpoint:

```text
aeed30411aefa0b27b107c966341c3a7b9cddaf5
```

Accepted behavior:

- First Night input is built from the committed TB deal and prepared seed;
- reveal remains immediate and precompute launches on `Dispatchers.Default`;
- exact READY is reused;
- exact BUSY safely waits at the consumer boundary rather than blocking reveal/main thread;
- MISS/stale input recomputes the exact requested input;
- stale background work cannot overwrite newer exact state;
- non-TB/provider-null behavior retains the existing fallback.

## 6. TBSP-6I acceptance matrix — COMPLETE

Accepted logical checkpoint:

```text
f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4
```

New durable test evidence:

```text
app/src/test/java/com/codex/campboardgamehost/NoGreaterJoySetupRegressionTest.kt
```

It locks the existing No Greater Joy role pool, 5/6-player distributions and start eligibility. The commit changes no production code.

Same-head validation:

```text
CI #1148 / run 33341819960                  SUCCESS
Android FAST unit tests (:app:testFast)     SUCCESS
CI gate                                      SUCCESS
R2 #1071 / run 33341819962                  SUCCESS
```

P8–P16 closeout:

```text
P8  ACCEPTED structurally: setup preparation is inside the explicit Start callback; recomposition does not invoke it.
P9  ACCEPTED structurally: script-selection onBack only navigates; onStart is the setup/start callback.
P10 ACCEPTED typed: NoGreaterJoySetupRegressionTest.
P11 ACCEPTED: exact provenance round-trip + restore does not invoke selector/preparer.
P12 ACCEPTED typed: invalid preset raises validation failure; no broad-random fallback deal is produced.
P13 ACCEPTED typed: reveal precedes background dispatch and no expensive build occurs synchronously.
P14 ACCEPTED typed + wiring: exact-input READY/BUSY/MISS/stale semantics; requests derive from committed deal and do not mutate identities.
P15 ACCEPTED: true-completion gate excludes incomplete Restart/abandon/archive.
P16 ACCEPTED typed: same gameId/selection retry writes once; conflicting gameId reuse is rejected; original committed selection is recorded.
```

There is no existing `app/src/androidTest` Compose instrumentation harness. Do not introduce one solely to duplicate the static event-wiring facts behind P8/P9, and do not create new source-string tests merely to restate those facts.

## 7. Current work — TBSP-6J cleanup

Active handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6J_CLEANUP.md
```

A concrete dormant pass-through is confirmed in `CampBoardGameHostApp.kt`:

```kotlin
fun resetDealState(
    nextGameKind: GameKind,
    clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
    preparedClocktowerSeed: Long? = null,
    preparedSetupPlan: RecommendationPlan? = null,
)
```

`resetDealState` does not consume `preparedSetupPlan`. The legacy/NGJ Clocktower start path computes a local `preparedSetupPlan` and uses it to derive `recommendedDrunkShownRole`; that local computation is behaviorally meaningful, but passing it into `resetDealState` is not.

### 6J intended slice

```text
remove unused resetDealState preparedSetupPlan parameter
-> change the four-argument call to three arguments
-> remove resulting unused import only if genuinely unused
-> compile / focused existing tests / :app:testFast
-> exact diff + R2/CI
-> stop
```

This is behavior-preserving dead-parameter cleanup. Do not manufacture a RED test for the implementation detail.

Do not broaden 6J into selector changes, dataset changes, NGJ behavior changes, background-lifecycle redesign, persistence redesign, A3/A4/ZDD, Mayor/Imp work or App/Host decomposition.

## 8. TBSP-6K — final full acceptance

6K begins only after 6J is accepted.

Required final gate:

```text
all focused TBSP acceptance GREEN
-> :app:testFast
-> affected T2/T3 when required by docs/TESTING_STRATEGY.md
-> :app:testFull
-> R2
-> final GitHub CI
-> exact diff / scope audit
```

Then report acceptance state. PR #57 still remains Draft until the user explicitly authorizes Ready/merge.

## 9. Merge-blocking invariants

The TBSP branch must continue to satisfy:

```text
P1  TB actual roles originate from selected preset.
P2  Baron is never applied a second time.
P3  Drunk actual identity remains Drunk.
P4  Drunk shown role comes only from selected preset options.
P5  Later recommendation cannot replace selected Drunk shown role.
P6  Same dataset/history/seed reproduces same initial setup.
P7  Start selects/materializes setup only once.
P8  Compose recomposition cannot reroll a started setup.
P9  Navigation before Start does not commit a preset selection.
P10 No Greater Joy behavior remains unchanged.
P11 Restore does not select a new preset.
P12 Invalid TB preset data never silently falls back to broad random TB setup.
P13 Identity dealing/reveal does not synchronously wait for complex setup/First Night calculation.
P14 Background setup/First Night work consumes committed exact input and cannot mutate/reroll identities.
P15 Only true completed TB games enter rotation history.
P16 Completion persistence is retry-safe and records the original initial selection.
```

All P1–P16 are accepted as of TBSP-6I. 6J must preserve them; 6K revalidates the integrated branch.

## 10. Testing cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Use risk-based evidence:

- add RED tests for behavior contracts/bugs where they create durable value;
- do not add source-string RED tests merely to force an implementation detail;
- use focused T0 evidence for the owning behavior;
- use T1 `:app:testFast` at logical checkpoints;
- run T4 `:app:testFull` at TBSP-6K final acceptance;
- remote CI/R2 and local/focused evidence serve different purposes.

For 6J specifically, compile + existing focused tests + T1 + exact diff is the appropriate behavior-preserving cleanup evidence unless a real regression appears.

## 11. Active documentation

Use these as current instructions:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6J_CLEANUP.md
docs/TESTING_STRATEGY.md
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md when rotation semantics are relevant
```

Earlier TBSP execution handoffs/checkpoints remain historical evidence. Git history preserves their exact content.

A3 remains deferred until this roadmap explicitly reactivates it after TBSP production acceptance.

## 12. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6J_CLEANUP.md`;
4. read `docs/TESTING_STRATEGY.md`;
5. re-query live `main`, PR #57 head/state/checks and branch comparison;
6. distinguish docs-only carriers from last validated code/test checkpoint `f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4`;
7. continue with the narrow TBSP-6J dead-parameter cleanup;
8. do not create an implementation-detail RED merely to remove the dead parameter;
9. preserve P1–P16, Dawn/Dusk exactly-once behavior and No Greater Joy behavior;
10. stop after 6J acceptance; do not begin 6K in the same micro-slice;
11. do not resume A3/A4/ZDD/Mayor/Imp-succession work;
12. keep PR #57 Draft and do not merge or mark Ready without explicit authorization.

## 13. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP PRODUCTION ACCEPTANCE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
