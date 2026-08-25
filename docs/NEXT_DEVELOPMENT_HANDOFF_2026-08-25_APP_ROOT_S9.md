# CampBoardGameHost — Deferred S9.2 Active Game Persistence Boundary Handoff

> Refreshed: 2026-08-25 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Status: **DEFERRED / architecture audit complete / implementation not started**
> Current stable code baseline at deferral: `5367603d2d7150e7ba88f19d061eb04f8da20aeb`
> PR #51: **MERGED / CLOSED — App-root through S9.1**
> Current priority before this task: `NEXT_DEVELOPMENT_HANDOFF_2026-08-25_INFORMATION_DECISION_CORRECTNESS_BUG.md`

This file is the detailed execution handoff for **one possible future App-root slice only**. It is not the current project-status authority; `docs/CURRENT_DEVELOPMENT_ROADMAP.md` is.

## 1. Current structural state

S9.1 is complete and merged through PR #51.

Final S9.1 result:

```text
CampBoardGameHostApp.kt          ~205,456 bytes
persistence/AppJsonPrimitives.kt ~1,335 bytes
```

S9.1 moved exactly nine pure JSON helpers into `persistence/AppJsonPrimitives.kt`, with exactly nine `private -> internal` visibility changes and no schema/callsite/persistence behavior changes.

PR #51 ran the current applicable remote gates successfully, including full Android unit tests + debug APK build and the final CI gate.

The old 1–5 KiB micro-slice strategy is retired. Future App-root work is justified only when it exposes a real responsibility module of roughly >=15–20 KiB, preferably >=20–30 KiB.

## 2. Why S9.2 is deferred

A correctness bug was confirmed in the Clocktower information-decision / automatic-recommendation authority path.

Approved order:

```text
PR #51 / S9.1 merged
-> fix information-decision correctness bug on a dedicated hotfix branch
-> merge correctness fix
-> re-query/re-audit live main
-> only then decide whether S9.2 still proceeds
```

Do not combine the correctness fix with this persistence extraction.

## 3. Architecture audit conclusion

The fresh post-S9.1 Root audit found only one remaining responsibility currently large and coherent enough to justify another structural slice:

```text
Active Game Persistence Boundary
```

Planning estimates only:

```text
raw persistence-related surface            ~45–55 KiB
safe expected Root reduction                ~25–35 KiB
possible with coherent prefs/history        ~35–45 KiB
```

Do not optimize for the upper byte estimate. Stop if the clean architecture boundary yields less.

Other candidates were rejected/deferred:

```text
legacy Clocktower catalog/distribution/assignment  ~12–16 KiB  coupled to private/legacy catalog authority
prefs/common-player/game-history alone             ~8–12 KiB  too small alone
leaf JSON codecs                                    ~4–6 KiB   too small alone
A4 prewarm/cache/lifecycle                          ~18–25 KiB  protected/high risk
setup/reset/start orchestration                     ~15–20 KiB  orchestration/high coupling
outcome/presentation helpers                        ~5–8 KiB   too small
Clocktower Judge/live transaction wiring            ~50–60+ KiB protected/extremely high risk
```

If S9.2 completes successfully, perform a fresh Root audit. If no new natural >=15–20 KiB low-coupling owner remains, formally **END App-root decomposition**. Do not force Root below 50 KiB.

## 4. Correct conceptual seam

The architecture should become:

```text
untrusted persisted JSON
-> completely validated immutable restore state
-> Root live Compose commit
```

The structural goal is not “move JSON code”. The goal is to separate **serialization/parsing** from **live application state ownership and mutation**.

## 5. Recommended production owners

### 5.1 `ActiveGameSnapshotCodec`

Recommended path:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameSnapshotCodec.kt
```

Responsibilities:

- encode an immutable persistence state into `JSONObject`;
- own/delegate common encoding for PlayerCard, EliminationRecord, GameOutcome and ClocktowerEvent when clean;
- delegate semantic-history, epistemic, night-checkpoint and ruleset representations to their existing authorities;
- preserve exact existing keys/null/schema behavior.

Must not own:

- when persistence happens;
- Compose lifecycle/effects;
- SharedPreferences timing;
- archive transaction timing;
- A4 durability timing;
- live Clocktower state mutation.

### 5.2 `ActiveGameRestoreParser`

Recommended path:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameRestoreParser.kt
```

Responsibilities:

```text
JSONObject
-> schema validation
-> persisted identity validation
-> ruleset basis/ref validation
-> semantic-history compatibility validation
-> epistemic/checkpoint/model parsing
-> ValidatedActiveGameRestore
```

The parser must never mutate live Compose/application state.

Root must receive a completely validated immutable result before the first live state mutation.

### 5.3 Optional `AppPreferencesStore` / `GameHistoryStore`

Include only if storage adapters remain a coherent responsibility and materially improve the boundary.

Possible responsibilities:

- raw save/load/clear of active-game JSON;
- general preferences/common-player persistence;
- history/archive storage.

Root still owns **when** these operations occur and the live/archive transaction boundaries.

### 5.4 Keep `savedGamePreviewFromJson` presentation-side unless proven otherwise

`savedGamePreviewFromJson` depends on application/presentation context (`Context`, resources, private `Screen`, game presentation, persistence coordinator behavior).

Do not widen `Screen` or drag presentation construction into persistence just to remove bytes from Root.

## 6. Restore contracts that must remain exact

`ActiveGamePersistenceCoordinator` remains the authority for current schema/version, persisted identity, compatibility and fail-closed restore gating.

Required semantic ordering:

```text
schema version
-> persistence identity
-> ruleset basis/ref validation
-> semantic-history/checkpoint/representation compatibility
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
- no live Compose mutation occurs before validation completes;
- archive/history behavior remains unchanged;
- restored-session behavior remains unchanged;
- active-state save/load/clear behavior remains unchanged;
- saved-game preview behavior remains unchanged;
- semantic-history persistence remains unchanged;
- epistemic observation persistence remains unchanged;
- `ClocktowerNightCheckpoint` persistence remains unchanged;
- ruleset basis/ref persistence remains unchanged.

No schema/version bump is expected or allowed for a pure structural extraction.

## 7. Special dependency rules

### 7.1 PlayerCard role resolution

Current restore dependency:

```text
playerCardFromJson
-> clocktowerRoleByName
-> completeClocktowerRoles
```

Do **not** widen or relocate the legacy catalog merely to make a new parser compile.

Preferred seam:

```text
(String?) -> ClocktowerRole?
```

Supply the existing semantics through a narrow resolver dependency from Root.

Abort if implementation requires:

- widening `clocktowerRoleByName`;
- widening/moving `completeClocktowerRoles`;
- duplicating Clocktower catalog authority;
- changing unknown-role/fallback behavior.

### 7.2 Epistemic persistence

Continue delegating to `EpistemicSemanticJson` and preserve its explicit fail-closed/error behavior. Do not create a second epistemic serializer or silently normalize malformed entries.

### 7.3 Night checkpoint

Reuse `ClocktowerNightCheckpoint`. Do not flatten it into a giant persistence DTO.

An unfinished night must still distinguish:

- confirmed Demon attack vs draft attack;
- confirmed Poison vs draft Poison;
- confirmed Monk vs draft Monk;
- confirmed Mayor redirect vs draft redirect;
- pending new Demon/successor state;
- timeline cursor.

## 8. Visibility / data-model guidance

Target **zero existing production symbol visibility widenings**.

New architecture types may be `internal`, but do not change existing private declarations such as:

```text
private Screen
private clocktowerRoleByName
private completeClocktowerRoles
private savedGamePreviewFromJson
```

If many existing private symbols must become `internal`, the boundary is probably wrong. Stop and re-audit.

Do not introduce a flat DTO whose main purpose is to mirror dozens of Compose variables. A persistence model is justified only if it is a genuine cohesive persisted-state representation protected by round-trip tests.

## 9. Tests-first RED plan

Resume with tests only. Do not edit production in the first step.

### RED A — `ActiveGameRestoreParserTest`

Cover at least:

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

### RED B — production restore boundary

Protect the real ownership invariant:

```text
Root restoreSavedGame()
-> obtains ValidatedActiveGameRestore from parser
-> only then performs first live mutation
```

The parser owner must not contain live Compose/application mutation constructs such as:

```text
remember
mutableStateOf
LaunchedEffect
DisposableEffect
cards.clear()
playerNames.clear()
```

Existing source-string tests that freeze validation text inside Root should be evolved to assert the new owner/ordering boundary rather than obsolete file location.

### RED C — exact unfinished-night fixture

Round-trip/restore a nontrivial checkpoint, for example:

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

Verify exact preservation through existing `ClocktowerNightCheckpoint` semantics.

### RED D — snapshot/restore round-trip

Protect:

```text
ActiveGameSnapshotCodec.encode(state)
-> ActiveGameRestoreParser.parse(json)
-> equivalent persisted/validated state
```

Compare exact behavior for keys/nulls, cards, records, events, outcome, identity, ruleset, semantic history, epistemic history and night checkpoint.

### RED E — PlayerCard role lookup authority

Prove:

- known role resolves exactly as current production;
- unknown role behavior is unchanged;
- no fallback/inference is introduced;
- no catalog duplication appears in persistence owners.

## 10. Existing regression contracts

At minimum preserve/execute relevant coverage around:

```text
ActiveGameProductionPersistenceWiringTest
ActiveGameSemanticHistoryProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
ClocktowerNightCheckpointTest
ActiveGamePersistenceCoordinator / identity / ruleset persistence tests
affected restore / migration tests
```

Key contracts include:

- `identityForSave` still participates in snapshot persistence;
- persisted identity is encoded and validated on restore;
- immutable ruleset basis/ref resolves before mutation;
- selected script is not inferred from player count or assigned roles;
- semantic mode/cursor/observations/checkpoint validate before mutation;
- restored semantic state is written only after validation;
- GLOBAL observation duplicate/revision/durability ordering is unchanged;
- legacy checkpoint compatibility and malformed cursor rejection are unchanged.

## 11. Preferred allowlist

Production:

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
recommendation authority
ASP/rules semantics
build/resources
```

Also forbidden:

- JSON key/schema/version changes;
- SharedPreferences key changes;
- live transaction reordering;
- archive/history behavior changes;
- restore behavior changes;
- existing production visibility widening;
- unrelated cleanup.

## 12. Validation ladder

Follow `docs/TESTING_STRATEGY.md`.

Expected:

```text
RED / T0:
  new parser/codec/boundary tests
  + focused existing persistence contracts

GREEN / T0:
  same focused set

T1:
  :app:testFast

T2:
  affected persistence / identity / ruleset / semantic-history /
  epistemic / checkpoint / restore / migration tests
  + :app:assembleDebug where applicable

T3:
  only if the actual change triggers a semantic family under TESTING_STRATEGY

PR T4:
  applicable full Android gate
```

Do not treat `UP-TO-DATE` / `FROM-CACHE` alone as proof that tests executed.

## 13. Large-file execution workflow

`CampBoardGameHostApp.kt` remains very large. Use the established ChatGPT + Luna/local workflow.

ChatGPT owns:

- live-state audit;
- boundary/authority decisions;
- RED design;
- exact symbol inventory;
- visibility/dependency audit;
- allowlist;
- validation ladder;
- exact-diff criteria;
- STOP decisions;
- post-push remote audit.

Luna/local Codex owns mechanical execution in the user's full worktree after the RED contract is fixed.

## 14. Hard STOP / abort gates

Abort S9.2 rather than forcing the extraction if it requires:

- widening the legacy Clocktower role catalog;
- moving/exposing private `Screen`;
- duplicating catalog authority;
- changing role lookup semantics;
- changing persistence timing;
- changing A4 durability timing;
- changing archive transaction timing;
- a giant flat parameter/DTO bag solely to move bytes;
- touching protected Clocktower live-transaction callbacks;
- changing schema/restore behavior.

After any successful S9.2 implementation, remeasure and re-audit. If no natural >=15–20 KiB boundary remains, end App-root decomposition.

## 15. Resume protocol

When this task is eventually resumed:

1. confirm the Information Decision correctness bug has been merged;
2. read root `AGENTS.md`;
3. read `docs/README.md` and `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. read this handoff and `docs/TESTING_STRATEGY.md`;
5. query live `main` and record the actual SHA;
6. re-audit the persistence region because the Root may have changed since `5367603d...`;
7. proceed only if the same architecture remains valid and the >=15–20 KiB threshold is still met;
8. establish RED before production edits;
9. stop on any abort gate above.
