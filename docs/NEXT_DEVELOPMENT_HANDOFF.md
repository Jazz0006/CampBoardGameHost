# CampBoardGameHost — Next Development Handoff

> Updated: 2026-09-27 Australia/Sydney  
> Current continuation: `codex/sde-history-prefix-route-closure`  
> Active Draft PR: **#157**  
> Next executable slice: **RH-E — serialized diagnostic persistence/timing hardening**

This file is deliberately compact. Completed checkpoint detail belongs in linked completion/audit/archive documents rather than being copied forward indefinitely.

## 1. Read first

1. `AGENTS.md`
2. `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`
3. `docs/TESTING_STRATEGY.md`
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
5. this handoff
6. `docs/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md` §6 RH-E
7. `docs/archive/checkpoints/sde/SDE_PRE_RHE_REPOSITORY_CLEANUP_CHECKPOINT_2026-09-27.md`

Only read older SDE slice audits when a concrete ownership/history question requires them.

## 2. Startup rule

Do not assume recorded branch/HEAD/PR state is still live.

Start by checking live remote state through the **GitHub Connector**: `main`, PR #157 exact head/state/draft/mergeability and current checks.

If the task is continuing an existing Mini MCP/Codex large-file worktree or otherwise depends on local unpushed state, also inspect the configured local workspace before editing so that current work is never overwritten or lost.

Requirements:

- preserve the current working tree;
- do not reset/discard unrelated work;
- #157 stays Draft;
- no merge, ready transition, rebase, force-push or main-conflict resolution without explicit authorization.

The local `main` ref may lag live `origin/main`; that is not a reason to mutate it during ordinary RH-E work.

## 3. Current state

~~~text
SDE-3A                                  COMPLETE
SDE-3B / BEGINNER_CONSERVATIVE_V1      COMPLETE / FROZEN
SDE-3C DecisionTrace / replay          COMPLETE
CR-A / CR-B / CR-C                     COMPLETE
C4 / SDE-3D2                           COMPLETE
IF-D durable replay                    COMPLETE
RH-E                                    NEXT
C5 / V2                                BLOCKED ON E3/E4
SDE-3E cutover                         BLOCKED PER SURFACE
~~~

Repository cleanup state:

- obsolete/validation-only PRs are closed;
- PR #109 is closed after confirming its old half-state is outside the current short-horizon Recovery contract;
- first-wave temporary validation remote branches are removed;
- remaining branch pruning is hygiene only and must preserve unique historical documentation/evidence.

## 4. Recovery boundary

Recovery is for recent emergency process interruption only.

Accepted contract:

~~~text
current format
+ exact current compatibility token
+ <=4h freshness
-> current validated recovery
~~~

Do **not** introduce old-format compatibility, migration, tolerant reconstruction or long-term save semantics.

#109 must not be resurrected as a compatibility project. A future bug requires a fresh reproduction from a current-format/current-version write.

## 5. RH-E problem statement

Current diagnostic runtime has correct canonical-state isolation but incomplete persistence/timing hardening:

- evaluation runs off the UI path, but archive load/decode/encode/write and post-commit lookup can still execute synchronously on the caller thread;
- archive operations are read-modify-write and must not be made naively parallel;
- current elapsed accounting does not cleanly separate evaluation, persistence and full end-to-end latency;
- archive growth/retention is not bounded for broader runtime collection.

RH-E must close these gaps without changing visible recommendation policy.

## 6. RH-E required invariants

### Ownership

- canonical game commit remains owned by `ClocktowerGameSession`;
- diagnostic trace persistence remains non-authoritative;
- one explicit serialized persistence owner must order archive mutations;
- App/Compose should only trigger bounded orchestration, not own archive concurrency semantics.

### Failure isolation

- successful canonical commit never depends on diagnostic durability;
- storage failure cannot roll back or invalidate canonical state;
- stale identity/cancelled evaluation must not publish a new trace;
- already-accepted archive mutations must not be lost because a later caller is cancelled.

### Ordering / idempotency

Preserve atomic append semantics, exact pending-trace correlation, successful retry idempotency, no lost update from concurrent read-modify-write, and no duplicate semantic trace created by retry.

### Timing

Expose separate measurements for evaluation, persistence/serialization and total request-to-diagnostic-completion latency.

Do not call the existing evaluation budget an end-to-end hard timeout until persistence is included in the model.

### Storage growth

Before broadening runtime collection, define a bounded retention policy at the archive owner. Do not evict material still required by active correlation or current calibration/export workflows.

## 7. Architecture / fanout audit before edits

Map at minimum:

- `SdeRuntimeShadowCoordinator`;
- `SdePostCommitCorrelationCoordinator`;
- `DecisionTraceArchiveStore`;
- `DecisionTraceArchivePreferencesStorage`;
- every archive append/correlation/load caller;
- App callbacks that trigger prepared-decision and post-commit diagnostic work;
- cancellation/job ownership and lifecycle boundaries.

Classify each path as serialized-mutation, read-only-safe or intentionally out of scope. Re-run the fanout search after implementation.

## 8. Execution / validation workflow

Use `docs/AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md` as the current execution-path authority:

- GitHub Connector is the default repository / PR / CI / result-analysis interface;
- Mini MCP + Luna is the large/truncated-file mechanical execution supplement after Chat fixes the design;
- strong Codex may be used read-only when complete local context is genuinely required before design;
- N3150 is retired from the default validation path;
- GitHub CI/R2 is the normal Android validation and independent acceptance surface.

### Tests-first acceptance plan

Use the smallest durable typed seam. Expected coverage includes:

- overlapping append/append cannot lose either trace;
- append/correlate ordering preserves the valid final trace;
- repeated exact correlation remains idempotent;
- slow storage does not block canonical commit;
- queued work reports persistence and total latency correctly;
- cancellation/stale decision prevents inappropriate publication but does not undo accepted persistence;
- retention does not evict a trace still required for pending correlation;
- bounded growth is deterministic;
- existing codec/migration/replay tests remain green;
- V1 output/reasons/selection remain unchanged.

Then:

~~~text
focused RH-E tests
-> affected persistence/runtime-shadow/correlation tests
-> :app:testFast
-> triggered T2
-> logical checkpoint :app:testFull :app:assembleDebug
-> required remote CI/R2 acceptance
~~~

Do not manufacture a RED for pure structural moves; do require RED/GREEN for new concurrency/retention/timing behavior.

## 9. Explicitly forbidden scope

RH-E does not authorize:

- `BEGINNER_CONSERVATIVE_V2`;
- new preference weights/thresholds;
- visible recommendation cutover;
- broader player-count support;
- Traveller expansion;
- second mutable history/recovery store;
- Recovery version migration;
- main integration/merge;
- unrelated UI cleanup.

## 10. Exit condition

RH-E is complete only when serialized persistence ownership, concurrency/idempotency, separate timing, slow-storage/backlog/cancellation behavior and bounded retention are all tested; canonical commit independence remains intact; focused/FAST/affected/full validation passes; required remote acceptance is green; and the compact roadmap/handoff are updated without copying old implementation logs back into them.

After RH-E, perform a fresh #157 versus live-main integration audit.
