# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
merged main code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

merged PR:
PR #57 — TBSP: integrate Trouble Brewing setup presets
MERGED / CLOSED

post-merge full validation:
CI #1179 / run 33346311357 SUCCESS
Android :app:testFull + :app:assembleDebug SUCCESS
ASP contract tests SUCCESS
Real Clingo cross-validation SUCCESS
CI aggregate gate SUCCESS

accepted TBSP production checkpoint:
4c8108c91be188d33435233efb9aba26397f6b87

final pre-merge T4 checkpoint:
45a60a3c32c7471c68d89b7fb886c4dbb00f1781

current work:
TBSP campaign COMPLETE / MERGED
MS-SETUP generic multi-script setup architecture — CURRENT PLANNING CAMPAIGN
MS-S0 fresh live-state + ownership audit — NEXT

active implementation handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md

normative TBSP rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md

final TBSP acceptance checkpoint:
docs/TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md
```

Commits after `4c8108c...` that only remove temporary one-shot CI files or update documentation are carriers on top of the accepted production tree, not new production checkpoints.

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
TBSP-6J cleanup                                              COMPLETE
TBSP-6K final full acceptance                                COMPLETE
TBSP-6L provenance durability repair                         COMPLETE
MS-SETUP generic multi-script setup architecture             CURRENT PLANNING CAMPAIGN
MS-S0 fresh live-state + ownership audit                       NEXT
A3 immutable setup snapshot                                  DEFERRED / NOT CURRENT
```

PR #57 is merged into `main` at `98ee982ef3590822cd06ac72a047b49afac3cfd6`. MS-SETUP must continue from fresh live `main` on a new branch after MS-S0 ownership audit.

## 3. Protected predecessor correctness baseline

TBSP preserves the accepted correctness work already on the branch/base, including:

- First Night Fortune Teller base/current-role authority;
- Other Night Fortune Teller canonical same-night effective-state projection;
- current living-Demon UI authority distinct from pending-succession reconstruction;
- poisoned Spy fail-safe behavior: normal wake, no fabricated Grimoire, no false Grimoire observation persistence;
- Dawn poison exactly-once and retry-convergent materialization;
- next-night/Dusk poison expiry exactly-once, restore/retry convergence and durable ordering before Night phase/round advancement.

These semantics remain protected predecessor behavior for future work.

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

Do not regenerate or reformat it without an explicit future dataset campaign.

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

No Greater Joy remains on its accepted existing setup-generation path until MS-SETUP deliberately generalizes setup ownership with parity evidence.

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

## 7. TBSP-6J cleanup — COMPLETE

Production cleanup checkpoint:

```text
68d29c53a0a37f2c30b9d88ed8967d5d9548b4bc
```

One-shot cleanup commit:

```text
ab1a57393a9abfd774dcdf4776f81134ed19a81a
```

Docs/final-gate checkpoint:

```text
d3b3993327a86c7dbd091346b11e7e6a95541637
```

Exact production change:

```text
resetDealState(..., preparedSetupPlan: RecommendationPlan? = null)
-> remove unused preparedSetupPlan parameter

resetDealState(GameKind.Clocktower, script, preparedSeed, preparedSetupPlan)
-> resetDealState(GameKind.Clocktower, script, preparedSeed)
```

The local legacy/NGJ `preparedSetupPlan` calculation remains present and still derives `recommendedDrunkShownRole`; no setup or NGJ behavior changed.

Validation:

```text
one-shot run 33342673392                     SUCCESS
:app:compileDebugKotlin                       SUCCESS
NoGreaterJoySetupRegressionTest --rerun-tasks SUCCESS
:app:testFast --rerun-tasks                  SUCCESS
git diff --check / exact one-file audit      SUCCESS
normal PR CI #1155 / run 33342927330         SUCCESS
R2 #1078 / run 33342927359                   SUCCESS
```

6J is accepted.

## 8. TBSP-6K — final full acceptance — COMPLETE

Final T4 trigger checkpoint:

```text
6b80b7ade7235d890bd2a492ed8b33a19c43ffaa
[full-ci] test: run TBSP 6K final acceptance
```

Final checkpoint document:

```text
docs/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md
```

Same-head T4 evidence:

```text
CI #1158 / run 33343377258                    SUCCESS
Classify changes / full checkpoint selected   SUCCESS
Android :app:testFull                         SUCCESS
Android :app:assembleDebug                    SUCCESS
44 Android/Gradle actionable tasks executed   SUCCESS
ASP contract tests                            SUCCESS
Real Clingo cross-validation                  SUCCESS
CI aggregate gate                             SUCCESS
R2 #1081 / run 33343377271                    SUCCESS
```

The Android full job explicitly ran:

```text
./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks
```

This executed the complete intentional Android JVM suite through `:app:testDebugUnitTest` and the debug APK build. No repair slice was identified during 6K itself; the later post-acceptance global audit identified the narrow 6L provenance durability cut-point.

Final PR scope audit found no 6K production/test change. The accumulated PR remains confined to accepted TBSP setup/session/persistence/App/Host changes, matching tests, and campaign documentation.

**TBSP-1 through TBSP-6K were accepted by the 6K gate. P1–P16 were revalidated on the integrated branch.**

### 8.1 TBSP-6L — post-acceptance provenance durability repair — COMPLETE

The post-6K global audit found a narrow process-death cut-point: `resetDealState()` persisted an active-game snapshot before `committedTroubleBrewingSetupSelection` was written back, leaving a small window in which cards/seed could survive restore while exact original preset provenance was absent.

Accepted repair evidence:

```text
test checkpoint:        8406bdf39a1203d8c69f5a51f7c94474516477ff
production checkpoint: 4c8108c91be188d33435233efb9aba26397f6b87
cleanup carrier:       d0bcbb6f6eaf9bfe31a81bc0f9c7efd73dc591fd
T4 trigger:            45a60a3c32c7471c68d89b7fb886c4dbb00f1781
one-shot run:          33344478383 SUCCESS
CI #1167:              33344886176 SUCCESS
R2 #1090:              33344886170 SUCCESS
```

The production diff is exactly one added call to `persistActiveGameStateIfNeeded()` immediately after the committed TB setup selection assignment. Focused RED/GREEN, `:app:testFast`, exact diff audit, full Android JVM tests/APK, ASP contracts, Real Clingo, CI gate and R2 all passed.

**TBSP-1 through TBSP-6L are now complete and accepted.**

PR #57 remains Draft until the user explicitly authorizes Ready/merge.

### 8.2 Next campaign — MS-SETUP generic multi-script setup architecture

TBSP-6L is accepted. MS-SETUP is now the next planned campaign, but implementation has not started.

Goal: make setup selection script-neutral so every Clocktower script automatically supports both template-backed and generated setup modes without adding new App-root `if (script == ...)` branches.

Default contract:

```text
script + playerCount + seed + diversity history
-> query optional template repository
-> templates available: build candidates from templates
-> no templates: build legal generated candidates from the script/ruleset
-> common diversity selector
-> deterministic committed setup
-> persist exact setup provenance
```

Required semantics:

- **default is no template**: a newly supported script remains playable through legal seeded random generation;
- **optional templates**: when a script/player-count has curated templates, choose from that template pool instead of broad random role composition;
- both sources feed one common diversity/rotation policy rather than separate ad-hoc retry loops;
- generated candidates must avoid duplicate roles inside one setup and template candidates must be semantically validated;
- cross-game diversity should consider exact-repeat and role-overlap history for both template-backed and generated modes;
- Drunk-like shown identity selection belongs to committed setup generation: template-backed mode uses allowed template options, generated mode chooses a legal shown identity from the script/ruleset;
- shown-identity repetition should participate in diversity scoring without treating mere seat reshuffles as a distinct setup;
- setup generation must be seeded/deterministic so the committed setup can be replayed/restored exactly;
- script-specific setup modifiers belong to ruleset/setup metadata or typed policy, not growing App-root conditionals;
- adding a future script with no templates should require no setup-architecture code change beyond providing its ruleset/roles;
- later adding templates for that script should switch the candidate source without changing App start wiring.

Proposed implementation slices:

```text
MS-S1  generic CommittedClocktowerSetup / provenance model
MS-S2  generic SetupCandidate + candidate-source contract
MS-S3  optional TemplateRepository keyed by script + player count
MS-S4  deterministic seeded GeneratedSetupCandidateSource
MS-S5  common cross-game SetupDiversityHistory / scorer / selector
MS-S6  generic shown-identity policy, including Drunk-style roles
MS-S7  adapt existing TB 480-preset pipeline to the generic contract without behavior drift
MS-S8  adapt NGJ/no-template path to generated candidates and prove parity
MS-S9  acceptance: a new no-template script works without App-root branching; adding templates requires data/provider registration only
```

The current TB-specific preset classes may be adapted or wrapped first; do not rename/rewrite them speculatively before a fresh MS-SETUP architecture audit. Preserve the accepted TB rotation semantics until parity tests prove a generic owner can replace them.

## 9. Accepted TBSP invariants

The integrated TBSP branch satisfies:

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

All P1–P16 were accepted by 6I, preserved by 6J, and revalidated by the 6K T4 integrated gate.

## 10. Testing cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Use risk-based evidence:

- add RED tests for behavior contracts/bugs where they create durable value;
- do not add source-string RED tests merely to force an implementation detail;
- use focused T0 evidence for the owning behavior;
- use T1 `:app:testFast` at logical checkpoints;
- use T2/T3 according to affected semantic surfaces;
- use T4 `:app:testFull` at explicit full-acceptance checkpoints;
- remote CI/R2 and focused/local evidence serve different purposes.

TBSP has completed its T4 acceptance. Do not rerun its full campaign gate during ordinary future micro-slices unless the changed semantic area or a new acceptance checkpoint requires it.

## 11. Active documentation

Current project authority:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/TESTING_STRATEGY.md
docs/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md when TB rotation semantics are relevant
```

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6K_FINAL_ACCEPTANCE.md` is now historical execution guidance for the completed 6K gate.

No MS-SETUP implementation handoff exists yet. Before starting MS-S1, perform a fresh live-state/ownership audit and write the narrow implementation handoff rather than treating the proposed slices above as permission to redesign production immediately.

A3 is no longer blocked by an incomplete TBSP campaign, but it remains deferred and is not the current work because MS-SETUP has been explicitly prioritized next.

## 12. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/TESTING_STRATEGY.md`;
4. re-query live `main`, PR #57 state/head/checks, and whether the accepted TBSP branch has since been merged or otherwise moved;
5. treat `68d29c53a0a37f2c30b9d88ed8967d5d9548b4bc` as the accepted TBSP production checkpoint and `6b80b7ade7235d890bd2a492ed8b33a19c43ffaa` as the final T4 acceptance checkpoint;
6. do not reopen TBSP P1–P16 without a concrete regression or explicit product change;
7. next planned work is MS-SETUP generic multi-script setup architecture;
8. before MS-S1 production implementation, audit existing TB-specific setup owners, current generic/random generation, persistence/provenance, history/rotation, and script/ruleset setup modifiers;
9. establish a script-neutral contract where optional templates and generated candidates share deterministic selection/diversity semantics;
10. preserve accepted TB behavior and NGJ behavior with parity evidence during genericization;
11. do not resume A3/A4/ZDD/Mayor/Imp/App-Host decomposition unless roadmap/user priority changes;
12. PR #57 remains Draft; do not mark Ready or merge without explicit authorization.

## 13. Deferred / queued work registry

| Deferred area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | NEXT / NOT STARTED |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | DEFERRED / NOT CURRENT |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
