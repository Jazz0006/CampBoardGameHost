# SDE RH-E runtime persistence/timing completion — 2026-09-27

> Status: **COMPLETE / accepted executable checkpoint**
>
> Current execution authority remains `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and `docs/NEXT_DEVELOPMENT_HANDOFF.md`.

## 1. Accepted identity

Formal continuation:

- branch: `codex/sde-history-prefix-route-closure`
- executable checkpoint: `f51a295983e8e203119dd50693af343c1ec23906`
- executable tree: `2babb1fba00b46dfc676efb6f090386b7a73826f`

Because Draft PR #157 is intentionally divergent from live `main`, remote acceptance used validation-only Draft PR #162. Its final validation head `bf66363385420f507f92a729b496ff002791dee5` has the exact same executable tree as the formal continuation checkpoint.

Final remote evidence:

- CI #3478: GREEN
- R2 main-thread boundary #3231: GREEN
- Android `:app:testFull :app:assembleDebug --no-daemon --rerun-tasks`: GREEN
- ASP contract tests: GREEN
- Real Clingo cross-validation: GREEN
- CI gate: GREEN

Validation-only PR #162 was closed without merge after acceptance; it is evidence transport only.

## 2. Closed architecture gaps

RH-E establishes one serialized diagnostic archive mutation lane. `DecisionTraceArchivePersistenceLane` orders append and post-commit correlation, keeps queue admission cancellable, rechecks stale identity before an append is admitted, and completes an already-admitted physical read/modify/write under `NonCancellable` on the configured background dispatcher.

The Android persistence owner is process-scoped through `DecisionTraceArchivePreferencesStorage.persistenceLaneFromContext(...)`. This is deliberate: an admitted write can outlive one Activity/Composition instance, so reconstruction must recover the same Mutex owner rather than create another lane over the same SharedPreferences archive.

`CampBoardGameHostApp` only triggers diagnostic work. Canonical game/session commit remains synchronous and authoritative before diagnostic correlation is launched. Diagnostic failure cannot roll back or invalidate canonical state.

## 3. Ordering and retention contract

Accepted ordering behavior:

- overlapping append/append mutations serialize and cannot lose either admitted update;
- if canonical commit wins before a queued shadow append is admitted, the refreshed identity gate rejects that append as stale;
- if the append is admitted first, post-commit correlation queues behind the same lane and finalizes the pending trace afterward;
- exact repeated correlation remains idempotent;
- cancellation while queued prevents admission, while cancellation after admission cannot abandon the accepted physical mutation.

Archive growth is count-bounded. Oldest committed traces are retired first; pending traces needed for correlation are never evicted. If the bound is saturated entirely by pending traces, a new diagnostic append is rejected rather than allowing unbounded growth.

## 4. Timing contract

Runtime reporting now separates:

- evaluation latency;
- persistence-queue latency;
- physical persistence latency;
- total request-to-diagnostic-completion latency.

The evaluation budget remains an evaluation-only budget. It is not described as an end-to-end timeout.

A static audit found and repaired a zero-millisecond sentinel defect: a genuine sub-millisecond completed evaluation can round to `0 ms`, so completion is tracked with nullable state rather than using `0L` as “not completed”.

## 5. Validation repair note

The first exact-tree validation head `93fce192024cc44e9446a97069b717bdcdc0d642` ran CI #3477 / R2 #3230. R2, ASP and Real Clingo passed, while Android FULL exposed one defect in a newly added test only: the test compared an executor thread name exactly, but coroutine debug may decorate that name.

The contract test was repaired to compare the actual `Thread` object identity instead. No production behavior changed in that repair. Formal commit `f51a295983e8e203119dd50693af343c1ec23906` and validation head `bf66363385420f507f92a729b496ff002791dee5` then passed the complete remote acceptance gate.

## 6. Exit / next gate

RH-E is complete. No V1 policy behavior or visible recommendation cutover was changed.

The next action is a fresh integration audit of Draft PR #157 versus live `main`. That audit may describe conflicts and propose an integration route, but **must not** rebase, resolve conflicts, mark ready, merge or force-push without explicit user authorization.
