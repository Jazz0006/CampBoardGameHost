# CampBoardGameHost — App-root S9 Handoff / Persistence Boundary Audit

> Refreshed: 2026-08-25 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Purpose: preserve the completed S9.1 state plus the audited-but-not-yet-implemented S9.2 plan so work can resume after the current correctness bug is fixed.
> This document supersedes the S9 planning sections of `NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_S7.md`.
> `docs/CURRENT_DEVELOPMENT_ROADMAP.md` remains the project-level status document; this file is the detailed S9 execution handoff.

## 1. Live structural checkpoint

Stable integration base before merging the current decomposition stack:

```text
main = 0311c3bb54ea71be69bc60a4aae642e0f39cd900
```

S9.1 branch:

```text
codex/source-decomposition-app-root-s9-1-json-primitives
```

Known lineage before this documentation commit:

```text
78cbaab473f433fb98d69f1bebfd7a69ae11edaa  S8.2 GREEN
  -> 377a3cd25691e50e04df31ae211bc069e3456364  docs closeout / S9.1 handoff
  -> d0ba789984eaf18db01c331f9a21dc842a6ae2eb  S9.1 RED
  -> 561fc3240c88c0f9c532bbf131e152aa99bad33e  S9.1 GREEN
```

S9.1 remote structural / exact-diff audit: **PASS**.

No GitHub Actions workflow runs were observed on `561fc3240c88c0f9c532bbf131e152aa99bad33e`, so do **not** describe S9.1 as remotely CI-green until a PR/full applicable gate has actually run.

## 2. S9.1 — App JSON primitives — CLOSED

S9.1 moved exactly nine JSON primitives from `CampBoardGameHostApp.kt` to:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/AppJsonPrimitives.kt
```

Moved symbols:

```text
enumByName
JSONObject.putNullableString
JSONObject.putNullableInt
JSONObject.putNullableBoolean
JSONObject.optNullableString
JSONObject.optNullableInt
JSONObject.optNullableBoolean
stringsToJsonArray
JSONArray.toStringList
```

Production visibility change was exactly nine instances of:

```text
private -> internal
```

S9.1 did **not** change:

- JSON keys or schema;
- snapshot / restore behavior;
- SharedPreferences keys or timing;
- persistence identity;
- model codecs;
- consumers / callsites;
- Clocktower catalog authority;
- Compose state / effects;
- Clocktower live transaction ordering;
- A3/A4/B4/recommendation authority.

Post-S9.1 sizes:

```text
CampBoardGameHostApp.kt          ~205,456 bytes
persistence/AppJsonPrimitives.kt ~1,335 bytes
```

The previous strategy of continuing with 1–5 KiB micro-slices is now abandoned. Future App-root decomposition must remove a genuine responsibility module of roughly >=15–20 KiB, preferably >=20–30 KiB, or the decomposition should stop.

## 3. Priority interruption — correctness bug before S9.2 implementation

A newly confirmed important correctness bug exists in the Clocktower information-decision / automatic recommendation path. It is not part of the App-root persistence subsystem.

Therefore the approved sequence is:

```text
S9.1 audit + merge
-> branch from new main
-> fix information-decision correctness bug tests-first
-> merge bug fix
-> only then consider resuming S9.2
```

Do not mix the correctness fix with S9.2 persistence extraction.

The bug fix may establish a small correct authority boundary if needed, but must not trigger a new size-driven `ClocktowerHostScreen.kt` decomposition. `ClocktowerHostScreen.kt` remains under new-responsibility growth freeze.

## 4. S9 persistence architecture audit — COMPLETE / PASS

A fresh audit of the remaining ~205 KiB Root concluded that **persistence is the only remaining App-root responsibility currently large and coherent enough to justify another structural slice**.

Estimated persistence-related surface:

```text
raw persistence-related code            ~45–55 KiB
safe expected Root reduction             ~25–35 KiB
possible with coherent prefs/history     ~35–45 KiB
```

These are planning estimates, not acceptance targets. Do not maximize extracted byte count at the cost of coupling or authority clarity.

Other remaining Root candidates were rejected or deferred:

```text
legacy Clocktower catalog/distribution/assignment  ~12–16 KiB  medium risk; too coupled to legacy/private catalog authority
prefs/common-player/game-history alone             ~8–12 KiB  too small alone
leaf JSON codecs                                    ~4–6 KiB   too small alone
A4 prewarm/cache/lifecycle                          ~18–25 KiB  protected/high risk; no size-driven move
setup/reset/start orchestration                     ~15–20 KiB  medium-high orchestration risk
outcome/presentation helpers                        ~5–8 KiB   too small
Clocktower Judge/live transaction wiring            ~50–60+ KiB extremely high risk; protected
```

Conclusion:

> If App-root decomposition resumes, perform at most one meaningful **Active Game Persistence Boundary** extraction, then re-audit. If no new natural >=15–20 KiB low-coupling responsibility remains, formally end App-root decomposition. Do not force Root to 50 KiB.

## 5. Recommended S9.2 architecture — Active Game Persistence Boundary

The correct conceptual seam is:

```text
untrusted persisted JSON
-> completely validated immutable restore state
-> Root live Compose commit
```

The decomposition should separate serialization/parsing from live application state ownership and mutation.

### 5.1 `ActiveGameSnapshotCodec`

Recommended owner:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameSnapshotCodec.kt
```

Responsibility:

- encode an immutable persistence state into `JSONObject`;
- own/delegate common model encoding for PlayerCard, EliminationRecord, GameOutcome and ClocktowerEvent where architecture permits;
- delegate semantic-history, epistemic, night-checkpoint and ruleset representations to their existing authorities;
- preserve exact existing keys/null behavior/schema semantics.

Must **not** own:

- when persistence happens;
- Compose lifecycle/effects;
- SharedPreferences timing;
- A4 durability timing;
- archive transaction timing;
- live Clocktower state mutation.

### 5.2 `ActiveGameRestoreParser`

Recommended owner:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameRestoreParser.kt
```

Responsibility:

```text
JSONObject
-> schema validation
-> persisted identity validation
-> ruleset basis/ref validation
-> semantic-history compatibility validation
-> epistemic/checkpoint/model parsing
-> ValidatedActiveGameRestore
```

It must never mutate live Compose/application state.

Root must receive a completely validated immutable result before the first live state mutation.

### 5.3 Optional `AppPreferencesStore` / `GameHistoryStore`

Only include storage adapters if they remain a coherent responsibility and materially improve the boundary.

Possible responsibility:

- raw save/load/clear of active-game JSON;
- general preferences/common-player persistence;
- history/archive storage.

Root still owns *when* those operations are invoked and the live/archive transaction boundaries.

### 5.4 Keep `savedGamePreviewFromJson` presentation-side unless proven otherwise

`savedGamePreviewFromJson` is not an ordinary leaf codec. It depends on presentation/application context such as `Context`, resources, private `Screen`, game-specific presentation and persistence coordinator behavior.

Do not widen `Screen`, move presentation authority, or drag resource-based preview construction into the persistence codec merely to reduce Root size.

## 6. Contracts S9.2 must preserve exactly

`ActiveGamePersistenceCoordinator` remains the authority for current schema/version, persisted identity, compatibility and fail-closed restore gating.

The existing restore ordering contract must remain semantically equivalent:

```text
schema version
-> persistence identity
-> ruleset basis/ref validation
-> semantic-history / checkpoint / representation compatibility
-> only then live state mutation
```

Protect all of the following:

- unknown/unsupported schema fails closed;
- malformed required JSON behavior remains unchanged;
- persisted identity validation remains unchanged;
- ruleset validation remains unchanged;
- semantic-history compatibility remains unchanged;
- unfinished-night checkpoint restore remains exact;
- draft versus confirmed night targets remain distinct;
- no live Compose state mutation occurs before validation completes;
- archive/history behavior remains unchanged;
- restored-session behavior remains unchanged;
- active-state save/load/clear behavior remains unchanged;
- saved-game preview decoding behavior remains unchanged;
- semantic-history persistence remains unchanged;
- epistemic observation persistence remains unchanged;
- `ClocktowerNightCheckpoint` persistence remains unchanged;
- ruleset basis/ref persistence remains unchanged.

No schema/version bump is expected or allowed for a pure structural extraction.

## 7. Special dependency rules

### 7.1 PlayerCard role resolution

Current PlayerCard restore is not a leaf operation:

```text
playerCardFromJson
-> clocktowerRoleByName
-> completeClocktowerRoles
```

Do **not** widen or relocate the legacy catalog simply to make the parser compile.

Preferred seam:

```text
(String?) -> ClocktowerRole?
```

Pass a narrow role resolver dependency into the restore parser from Root, preserving the current `clocktowerRoleByName` semantics and catalog authority exactly.

Abort the extraction if implementation would require:

- widening `clocktowerRoleByName`;
- widening/moving `completeClocktowerRoles`;
- duplicating a Clocktower catalog;
- changing unknown-role/fallback behavior.

### 7.2 Epistemic persistence

Existing epistemic persistence delegates to `EpistemicSemanticJson` with explicit fail-closed/error semantics.

The new parser/codec may delegate to that authority, but must not silently normalize malformed entries or create a second epistemic serialization authority.

### 7.3 Night checkpoint

Reuse `ClocktowerNightCheckpoint`; do not flatten its authority into a new giant persistence DTO.

An unfinished night must continue to preserve independently:

- confirmed Demon attack vs draft attack target;
- confirmed Poison target vs draft;
- confirmed Monk target vs draft;
- confirmed Mayor redirect vs draft;
- pending new Demon / successor information;
- timeline cursor.

## 8. Data model / visibility guidance

Target **zero existing production symbol visibility widenings** in S9.2.

New architecture types may be `internal` where needed, but do not change existing private declarations such as:

```text
private Screen
private clocktowerRoleByName
private completeClocktowerRoles
private savedGamePreviewFromJson
```

If many existing private symbols must become `internal`, treat that as evidence that the proposed boundary is wrong and stop.

Do not introduce a giant flat DTO whose only purpose is to mirror dozens of Compose variables and pass them to another file. A persistence model is justified only if it represents a genuine cohesive persisted schema/state and is protected by round-trip tests.

Prefer cohesive nested persistence models and reuse existing domain/session representations.

## 9. S9.2 tests-first plan

When S9.2 is resumed, start with RED only. Do not edit production in the same first step.

### 9.1 `ActiveGameRestoreParserTest`

Add focused behavior tests for at least:

```text
unknown schema -> reject
malformed required JSON -> reject
identity mismatch -> reject
invalid ruleset basis/ref -> reject
incompatible semantic-history mode/cursor -> reject
malformed epistemic history -> reject
malformed night checkpoint -> reject
parse failure -> no partially usable restore result
```

### 9.2 Production ownership/boundary test

Add a structural/production-wiring test that protects the real invariant:

```text
Root restoreSavedGame()
-> obtains ValidatedActiveGameRestore from parser
-> only then performs first live mutation
```

The parser owner must not contain Compose/live state constructs such as:

```text
remember
mutableStateOf
LaunchedEffect
DisposableEffect
cards.clear()
playerNames.clear()
```

Existing source-string tests that merely freeze validation text inside Root should be evolved to protect this new owner/ordering boundary rather than preserving obsolete temporary code location.

### 9.3 Mid-night exact restore fixture

Create a fixture that round-trips/restores a nontrivial unfinished night containing distinct confirmed and draft state, for example:

```text
confirmed attack = P8
draft attack     = P1
confirmed poison = P2
draft poison     = P3
confirmed Monk   = P4
draft Monk       = P5
confirmed Mayor  = P6
draft Mayor      = P7
semantic mode    = GLOBAL_V1
cursor           = N
```

Verify exact preservation. Reuse `ClocktowerNightCheckpoint` semantics rather than reimplementing them.

### 9.4 Snapshot/restore schema round-trip

Protect:

```text
ActiveGameSnapshotCodec.encode(state)
-> ActiveGameRestoreParser.parse(json)
-> equivalent persisted/validated state
```

Compare exact behavior for:

- keys and nullable values;
- cards and elimination records;
- events and game outcome;
- persistence identity;
- ruleset basis/ref;
- semantic-history state;
- epistemic history;
- unfinished-night checkpoint.

### 9.5 PlayerCard lookup authority test

Protect that the parser uses the supplied existing role resolver semantics:

- known role resolves identically;
- unknown role behaves identically to current production;
- no fallback/inference is introduced;
- no catalog duplication exists in persistence owners.

## 10. Existing regression contracts that remain required

At minimum preserve and run the relevant existing tests around:

```text
ActiveGameProductionPersistenceWiringTest
ActiveGameSemanticHistoryProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
ClocktowerNightCheckpointTest
ActiveGamePersistenceCoordinator / identity / ruleset persistence tests
restore / migration tests affected by the change
```

Important current contracts include:

- `identityForSave` remains used on snapshot;
- persisted identity remains encoded and validated on restore;
- immutable ruleset basis/ref is persisted and resolved before mutation;
- selected script is not inferred from player count or assigned roles;
- semantic mode/cursor/observations/checkpoint are validated before mutation;
- restored semantic mode/cursor are written only after validation;
- GLOBAL observation duplicate/revision/durability ordering remains unchanged;
- night checkpoint legacy compatibility and malformed cursor rejection remain unchanged.

## 11. S9.2 production/test allowlist

Preferred production allowlist:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameSnapshotCodec.kt
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameRestoreParser.kt
optional: app/src/main/java/com/codex/campboardgamehost/persistence/AppPreferencesStore.kt
```

Plus narrowly corresponding persistence tests.

Forbidden scope expansion:

```text
ClocktowerHostScreen.kt
clocktower/catalog/**
ClocktowerGameSession authority
A4 planner/projector/materializer/cache/prewarm authority
recommendation selection authority
ASP/rules semantics
build configuration/resources
```

Also forbidden:

- JSON key/schema/version changes;
- SharedPreferences key changes;
- live transaction reordering;
- archive/history behavior changes;
- restore semantics changes;
- existing production visibility widening;
- unrelated cleanup.

## 12. Validation ladder for resumed S9.2

Follow `docs/TESTING_STRATEGY.md`.

Expected ladder:

```text
RED / T0:
  new parser/codec/boundary tests
  + existing focused persistence wiring contracts

GREEN / T0:
  same focused set after mechanical extraction

T1:
  :app:testFast

T2:
  affected persistence coordinator / identity / ruleset / semantic-history /
  epistemic-observation / ClocktowerNightCheckpoint / restore / migration tests
  + :app:assembleDebug where applicable

T3:
  only if the actual implementation changes representation/semantic areas that trigger it

PR T4:
  applicable Android full gate under TESTING_STRATEGY
```

Do not treat `UP-TO-DATE` or `FROM-CACHE` alone as proof that tests executed.

## 13. Execution workflow when S9.2 resumes

`CampBoardGameHostApp.kt` remains a very large file. Use the established Path B workflow.

### ChatGPT owns

- re-querying live `main` and target branch lineage;
- architecture/boundary decisions;
- exact symbol inventory;
- RED test design;
- changed-file allowlist;
- visibility/dependency audit;
- validation ladder;
- exact-diff acceptance criteria;
- STOP/abort decisions;
- post-push remote audit.

### Luna/local Codex owns mechanical execution

- work in the user's full local Git worktree;
- create the approved RED first;
- run focused RED and commit it separately;
- mechanically move only approved code;
- make minimal compile-required imports/wiring changes;
- run approved T0/T1/T2;
- `git diff --check` and exact local diff audit;
- commit/push only after gates pass.

Luna must not choose a different architecture, widen scope, repair unrelated tests, move protected authority, rebase, merge or force-push.

## 14. Hard abort / STOP gates

Abort S9.2 rather than forcing the decomposition if implementation requires any of the following:

- widening the legacy Clocktower role catalog;
- moving/exposing private `Screen`;
- duplicating Clocktower catalog authority;
- changing role lookup semantics;
- changing persistence timing;
- moving A4 durability timing;
- moving archive transaction timing;
- touching protected Clocktower live transaction callbacks;
- creating a giant flat parameter/DTO bag solely to move bytes;
- broad existing production visibility widening;
- schema/key/version changes not independently required by product behavior.

After a successful S9.2, remeasure Root and perform a fresh responsibility audit.

If no remaining natural low-coupling responsibility reaches roughly >=15–20 KiB, record:

```text
APP-ROOT DECOMPOSITION = ENDED
```

Do not return to 3–5 KiB micro-slices and do not force the Root below 50 KiB.

## 15. Resume checklist after the correctness bug is merged

When returning to this work:

1. Read root `AGENTS.md`.
2. Read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.
3. Read `docs/TESTING_STRATEGY.md`.
4. Read this S9 handoff.
5. Re-query live `main`, S9/decomposition branch history and relevant checks; do not trust stale SHAs blindly.
6. Confirm the information-decision bug fix is already merged and outside the S9.2 diff.
7. Decide whether S9.2 is still worth doing under the >=15–20 KiB natural-module threshold.
8. If yes, start a fresh S9.2 branch from the then-current stable `main` and begin with RED only.
9. After S9.2 GREEN, perform a fresh Root audit and stop decomposition unless another genuinely large coherent boundary exists.
