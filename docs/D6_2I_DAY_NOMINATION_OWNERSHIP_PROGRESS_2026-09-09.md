# D6.2i Day Nomination Transient Ownership Progress

> Date: 2026-09-09 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-2-ui-composition` / draft PR #115
> Status: **COMPLETE / FULL CI + R2 PASS**

## Checkpoints

```text
main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
BASE_DOCS_HEAD: bffa52095585bd9ee37e77fce31c2a71219ce895
validated production: 5e0891e7611787300b01d83b889f27a903c0768b
production tree: fb7a087cfa3c6e90abaf4c036eeda10c50768259
```

The production commit is directly parented by BASE_DOCS_HEAD. Later closeout commits change documentation only and must not be confused with the validated production checkpoint.

## D6.2i — COMPLETE / VALIDATED: Day nomination transient ownership

Production checkpoint: `5e0891e7611787300b01d83b889f27a903c0768b`.

- App no longer declares, resets or forwards the nomination pair or dead outer vote count.
- Judge owns `nominatorName` and `nomineeName` with `remember(gameId, round)`.
- Cancel nomination and confirm vote still clear both names; cancel vote retains the pair.
- Day mode, ghost-vote authority, highest-vote mechanics, Virgin callbacks and Recovery remain external and unchanged.

Verified boundary metrics:

```text
Judge parameters:                  92 -> 89
on... callbacks:                   34 unchanged
function-valued providers:          3 unchanged
MutableState parameters:            8 -> 5
App explicit remembered state vals: 9 -> 6
App delegated remembered vars:     38 unchanged
Combined scalar state declarations:47 -> 44
```

**Metric correction:** the old expected `App-root vars 36 -> 33` had no reproducible counting definition. The counts above include root `clocktower*` declarations directly initialized with `remember { mutableStateOf(...) }`, both delegated `var` and explicit `val`; they exclude derived views and state lists. Earlier campaign figures are historical, not the current measured total.

Exact production diff: App +0/-12; Host +2/-11; exactly two files and one commit from `bffa52095585bd9ee37e77fce31c2a71219ce895`.

Acceptance:

```text
CI 34291666237 — PASS / FULL
  Android full JVM + debug APK — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
R2 34291666241 — PASS
```

Local Gradle could not download its distribution because of restricted network access. Compilation and the existing nomination/vote tests were validated by the remote full JVM suite, not by a claimed local GREEN. No new tests or production seams were introduced.

## Next — residual composition re-audit

D6.2i is closed. Before another production slice, re-rank the remaining live UI ownership boundaries using the current 89-parameter signature. Day mode and durable vote state still have legitimate external writers; Night navigation remains checkpoint/Recovery-coupled. Do not move those groups wholesale or expand this slice to a Day dispatcher, Recovery redesign or orphan-helper cleanup.

PR #115 remains OPEN / DRAFT. Merge requires explicit user authorization.

## Lifecycle and invariant evidence

The complete sources were searched before editing. Each removed App state had exactly four occurrences: declaration, Day reset, Recovery-entry reset and Judge forwarding. The dead Judge count had a parameter, delegate and five zero-only assignments, with no read consumer.

Day completion and continuing Virgin execution advance round before Night. The keyed nomination pair therefore expires at that transition. New game identity also expires it. Recovery already re-enters a safe mode rather than restoring an in-progress nomination; its schema is unchanged. Day Klutz follows pair-clearing vote completion, while Night Klutz occurs in the advanced round. Nomination cancellation and vote confirmation retain explicit pair clearing; vote cancellation returns to nomination with the pair intact.

The durable vote transaction, ghost-vote publication, highest-vote updates and event ordering are unchanged. Virgin preflight/registration/execution callbacks are byte-for-byte unchanged. No session/domain/persistence files or tests changed.

Existing coverage retained:

- `ClocktowerDayNominationGestureTest` — legal live seats, directional drag, illegal/self targets.
- `ClocktowerTableVoteStateTest` — vote ordering, pending selection, ghost authority and unknown nominee rejection.
- `ClocktowerVoteTransactionTest` — qualifying/high/tied/low votes and ghost-vote consumption.

These 10 existing test methods are included by the unfiltered full JVM entry point. They cover nomination/vote contracts; Compose lifetime equivalence is supported by source/lifecycle audit, not claimed as a new instrumented UI test. No real-device smoke test was performed in this environment.

## Exact size and diff

| File | Before bytes / lines | After bytes / lines | Diff |
|---|---:|---:|---:|
| CampBoardGameHostApp.kt | 237,870 / 4,250 | 237,112 / 4,238 | +0 / -12 |
| ClocktowerHostScreen.kt | 281,655 / 4,764 | 281,354 / 4,755 | +2 / -11 |

`git diff --check` passed. Remote compare confirmed ahead=1, behind=0 and exactly the two production files. Removed declarations/parameters/count references are absent; durable inputs and the 34 on-callbacks remain.

## Execution and acceptance

The user's current Work instruction superseded the Chat-only large-file writer restriction. Source edits were made in a complete local checkout using exact-count replacements validated before any write. Local Gradle failed before compilation while downloading Gradle 9.5.0 with `Network is unreachable`; no local test result is claimed.

CLI Git push lacked credentials. The connected GitHub API synchronized local file bytes with matching Git blob hashes:

```text
App:  f6a2db29542cea30823c68157140ab5d1da10e0e
Host: 4b214d814a073a5f4498f3d3f90b05740d5e58ba
```

The complete remote tree matched the local committed tree, and the branch was updated with force=false. The local checkout was then aligned with the identical remote tree. No temporary workflow, history squash, force-push or main update was used.

The required compilation/test gate ran remotely after synchronization because local dependencies were unavailable. This execution-order deviation is explicit: production was not accepted until the clean-head FULL CI completed.

- [FULL CI 34291666237](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34291666237): full Android unit tests and debug APK, ASP, Real Clingo and aggregate CI gate passed.
- [R2 34291666241](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34291666241): passed.

The full Android step uses `:app:testFull :app:assembleDebug --no-daemon --rerun-tasks`. FAST is deliberately skipped by full routing; no separate local focused/FAST pass is claimed.
