# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
live main at MS-S0/MS-S0.5 audit:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated main code checkpoint:
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
MS-SETUP generic multi-script setup architecture — CURRENT CAMPAIGN
MS-S0 fresh live-state + ownership audit — COMPLETE
MS-S0.5 recovery scope reduction audit — COMPLETE
MS-S1 generic CommittedClocktowerSetup / provenance — NEXT

current MS-SETUP branch:
codex/ms-setup-generic-architecture
base: eed51bade5163790316a31e8295e2e841df90357

active implementation handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md

recovery scope audit:
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md

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
MS-SETUP generic multi-script setup architecture             CURRENT CAMPAIGN
MS-S0 fresh live-state + ownership audit                     COMPLETE
MS-S0.5 recovery scope reduction audit                       COMPLETE
MS-S1 generic committed setup/provenance                     NEXT
A3 immutable setup snapshot                                  DEFERRED / NOT CURRENT
```

PR #57 is merged into `main` at `98ee982ef3590822cd06ac72a047b49afac3cfd6`. MS-SETUP now continues on `codex/ms-setup-generic-architecture`; production implementation has not started.

## 3. Protected predecessor correctness baseline

TBSP preserves the accepted correctness work already on the branch/base, including:

- First Night Fortune Teller base/current-role authority;
- Other Night Fortune Teller canonical same-night effective-state projection;
- current living-Demon UI authority distinct from pending-succession reconstruction;
- poisoned Spy fail-safe behavior: normal wake, no fabricated Grimoire, no false Grimoire observation persistence;
- Dawn poison exactly-once and retry-convergent materialization;
- next-night/Dusk poison expiry exactly-once, restore/retry convergence and durable ordering before Night phase/round advancement.

These semantics remain protected predecessor behavior for future work.

The 2026-08-31 recovery-scope decision changes the **product continuation promise**, not these committed-domain transaction guarantees. Exactly-once, retry and convergence semantics remain protected where they preserve committed game facts.

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

Historical TBSP acceptance proved that restore decodes the committed TB provenance without invoking selector/preparer and that older supported saves without TB provenance do not fabricate an initial selection.

For **future MS-SETUP architecture**, this is transitional behavior: the target authority is the exact persisted `CommittedClocktowerSetup`; provenance is source/audit metadata and must not be the reconstruction recipe.

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

Final PR scope audit found no 6K production/test change. The accumulated PR remained confined to accepted TBSP setup/session/persistence/App/Host changes, matching tests and campaign documentation.

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

**TBSP-1 through TBSP-6L are complete, accepted and merged.**

### 8.2 Current campaign — MS-SETUP generic multi-script setup architecture

MS-S0 and MS-S0.5 planning audits are complete. Production implementation has not started; MS-S1 is next.

Goal: make setup selection script-neutral so every Clocktower script automatically supports both template-backed and generated setup modes without adding new App-root `if (script == ...)` branches.

Current generic setup contract:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup policy/provider
-> query optional template candidate source
-> templates available: build validated template candidates
-> no templates: build legal generated candidates from the script/ruleset
-> common deterministic diversity selector
-> commit shown-identity decisions
-> CommittedClocktowerSetup
```

`CommittedClocktowerSetup` is a persistence-independent immutable domain fact. Setup generation ends there.

Recovery/persistence is an outer concern:

```text
CommittedClocktowerSetup + committed game facts
-> stable domain checkpoint
-> process death / supported restart
-> restore exact committed facts
-> continue/restart from the next safe domain/action boundary
```

The setup candidate source, selector, shown-identity chooser and recommendation system must never be rerun to reconstruct an already committed setup.

Required setup semantics:

- **default is no template**: a newly supported script remains playable through legal deterministic seeded generation;
- **optional templates**: when a script/player-count has curated templates, choose from that template pool instead of broad random role composition;
- both sources feed one common diversity/rotation selection layer;
- generated candidates avoid duplicate roles and satisfy ruleset/setup modifiers;
- template candidates are semantically validated before selection;
- cross-game diversity considers semantic setup identity and role overlap without treating seat reshuffling as a distinct setup;
- Drunk-like shown identity is committed during setup generation and cannot later reroll;
- same seed + same candidate inputs/history produces the same selection;
- provenance records source/audit metadata, not the facts needed to reconstruct a committed setup;
- script-specific setup modifiers belong to ruleset/setup metadata or typed policy rather than App-root conditionals;
- adding a future no-template script requires no setup-architecture branch;
- later adding templates requires provider/data registration, not App start rewiring.

Confirmed recovery product boundary:

- best-effort crash / Android process-death recovery to the latest supported **stable committed domain checkpoint** remains;
- committed setup and committed game facts restore exactly;
- transient UI state and uncommitted decisions may be discarded/restarted;
- exact arbitrary unfinished-night/UI continuation is no longer a product requirement;
- “play half today and continue tomorrow” is not a supported design goal;
- indefinite cross-version compatibility for unfinished saves is not required; an incompatible save may be rejected/discarded by explicit policy;
- completed-game setup/diversity history remains independently durable;
- exactly-once/retry/convergence mechanics that protect committed state remain protected.

MS-S0.5 source audit confirmed that `ClocktowerNightCheckpoint`, `NightTransactionRestoreComposition` and `NightTransactionRestoreCompositionTest` currently implement/protect exact unfinished-night state including draft fields. These are migration/retirement candidates, not immediate deletion targets. `NightDawnRestoreRetryConvergenceAcceptanceTest` and corresponding recovery authorities protect committed transaction convergence and are retained.

Revised implementation slices:

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                       COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance   NEXT
MS-S2   generic SetupCandidate + source contract + setup policy/provider registry
MS-S3   optional TemplateRepository keyed by script + player count
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S6R  stable-domain-checkpoint persistence/recovery adapter + legacy retirement gate
MS-S7   adapt TB 480-preset pipeline; preserve parity; exact committed setup owns restore
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   acceptance: future no-template script needs no App-root setup branch; templates are provider/data registration only
```

Do not rename/rewrite current TB-specific preset classes speculatively before their generic seam is required. Preserve accepted TB rotation semantics until typed parity proves a generic owner can replace/wrap them.

Detailed current handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Recovery decision/audit:

`docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md`

## 9. Accepted TBSP invariants

The integrated TBSP baseline satisfies:

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
P11 An active restore does not select a new preset.
P12 Invalid TB preset data never silently falls back to broad random TB setup.
P13 Identity dealing/reveal does not synchronously wait for complex setup/First Night calculation.
P14 Background setup/First Night work consumes committed exact input and cannot mutate/reroll identities.
P15 Only true completed TB games enter rotation history.
P16 Completion persistence is retry-safe and records the original initial selection.
```

All P1–P16 were accepted by 6I, preserved by 6J/6L and revalidated by the integrated T4/post-merge baseline.

MS-SETUP may replace the historical mechanism behind P11 with direct exact committed-setup restoration; the invariant remains that restore never reselects/rerolls an already committed setup.

## 10. Testing cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Use risk-based evidence:

- add RED tests for behavior contracts/bugs where they create durable value;
- do not add source-string RED tests merely to force an implementation detail;
- use focused T0 evidence for the owning behavior;
- use T1 `:app:testFast` at logical checkpoints;
- use T2/T3 according to affected semantic surfaces;
- use T4 `:app:testFull` at explicit full-acceptance checkpoints;
- remote CI/R2 and focused/local evidence serve different purposes;
- tests may be retired when their product contract is explicitly obsolete and surviving evidence protects every still-required invariant.

MS-S1 should not manufacture a RED for a pure immutable-model introduction. Add typed tests only for real durable invariants introduced by the model. Persistence round-trip evidence belongs to MS-S6R unless a pure codec is deliberately part of MS-S1; deterministic generation belongs to MS-S4; NGJ parity belongs to MS-S8.

TBSP has completed its T4 acceptance. Do not rerun its full campaign gate during ordinary future micro-slices unless the changed semantic area or a new acceptance checkpoint requires it.

## 11. Active documentation

Current project authority:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
docs/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md when TB rotation semantics are relevant
```

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6K_FINAL_ACCEPTANCE.md` is historical execution guidance for the completed TBSP gate.

The MS-SETUP handoff is now the active implementation plan. MS-S0/MS-S0.5 are planning-only checkpoints; MS-S1 production implementation has not started.

A3 remains deferred and is not current work.

## 12. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`;
4. read `docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md` when setup persistence/recovery is relevant;
5. read `docs/TESTING_STRATEGY.md` and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
6. re-query live `main`, `codex/ms-setup-generic-architecture` and any relevant PR/checks;
7. treat `98ee982ef3590822cd06ac72a047b49afac3cfd6` as the fully validated merged TBSP code checkpoint and later docs-only `main` commits as carriers unless live audit proves otherwise;
8. do not reopen TBSP P1–P16 without a concrete regression or an explicit product/architecture migration described by MS-SETUP;
9. next production work is MS-S1 only: freeze and implement the persistence-independent exact `CommittedClocktowerSetup` / provenance contract;
10. do not wire App/Host/TB/NGJ active persistence in MS-S1;
11. preserve the new recovery boundary: stable committed checkpoint recovery only; do not add new exact draft/UI resume obligations;
12. do not delete legacy restore/recovery code until replacement and call-site/typed-evidence proof identifies it as obsolete rather than transactional correctness infrastructure;
13. do not resume A3/A4/ZDD/Mayor/Imp/App-Host decomposition unless roadmap/user priority changes;
14. do not merge, mark Ready, force-push or broaden a PR without explicit user authorization.

## 13. Deferred / queued work registry

| Deferred area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT; MS-S1 NEXT |
| MS-S6R stable checkpoint recovery + legacy resume retirement | PLANNED WITHIN MS-SETUP |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP; re-evaluate against S0.5 boundary |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | DEFERRED / NOT CURRENT |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S0.5/MS-S6R PLANNING; no broad App decomposition |