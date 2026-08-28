# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline: c8985cb4991f6c7e5ea02adedb932d2d86452da1
active branch: codex/clocktower-same-night-effective-state-correctness
PR: #54
PR policy: draft / open / unmerged until explicit authorization
```

Latest accepted executable SNE-7 full checkpoint:

```text
70935644daf5c06985420f19833dbda3a160bbfa
```

Evidence at that checkpoint:

```text
CI #933 / run 33153679896 SUCCESS
- full Android unit tests
- assembleDebug
- ASP contract tests
- Real Clingo cross-validation
- CI gate

R2 run 33153679938 SUCCESS
```

Later SNE-7 docs-only closeout checkpoint:

```text
83bafdeef2e8445ee6ef92a3e247d63fdf4b58ce
```

The active docs-cleanup/GCR planning commits after that are documentation only. Re-query PR #54 before relying on a head SHA.

## 2. Current priority — GCR Global Correctness Review Follow-up

Status:

```text
GCR     ACTIVE PLAN / RED TO WRITE
GCR-1   Current Demon authority / cross-night succession         HIGH / MERGE BLOCKER
GCR-2   Poisoned Spy information integrity                       HIGH / MERGE BLOCKER
GCR-3   Typed production acceptance + source-string retirement   MEDIUM
GCR-4   Chambermaid actual wake-history authority                DEFERRED FOLLOW-UP
GCR-5   Durable identity + reconstruction API hardening          DEFERRED FOLLOW-UP
```

Current handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-28_GLOBAL_CORRECTNESS_FOLLOWUP.md
```

Do **not** reopen SNE-7 simply because the global review found new work. SNE-7 proved its scoped same-night/Dawn contracts; GCR addresses gaps outside or above those accepted contracts.

## 3. Why GCR is now merge-blocking

A whole-PR / whole-night correctness review after SNE-7 closure found two issues that existing green tests did not adequately cover.

### GCR-1 — current Demon continuity

After valid Imp succession, historical state may contain both a dead old Imp and a living new Imp. Production still has callsites whose Demon lookup assumes either “first Demon-team card” or “single Imp-role card”. Those assumptions can select the old dead Imp or fail uniqueness after succession.

Required correctness:

```text
Night N:   Imp0 self-kills -> Imp1 becomes current Demon
Night N+1: Imp1 acts normally
Later:     Imp1 can self-kill -> Imp2 becomes current Demon
```

Attack resolution, poison/functioning semantics, succession and Host explanation must consult one typed current-Demon authority.

### GCR-2 — poisoned Spy information side channel

An impaired Spy must not infer poison/drunk state from the app changing the outward interaction into “wake, but no Grimoire/information”. The impaired path must preserve a normal-looking Spy information interaction while preventing any unauthorized true-Grimoire leak and using typed unreliable-information authority.

These two items are merge blockers for PR #54 until tests-first RED/GREEN and broad acceptance are complete.

## 4. GCR execution order

### Phase GCR-0 — RED only

Create typed failing tests for:

```text
1. first Imp successor acts on the next eligible night;
2. repeated succession Imp0 -> Imp1 -> Imp2;
3. mechanical attack + poison/functioning + Host explanation agree on current Demon;
4. poisoned Spy does not expose impairment through missing-information UI shape;
5. poisoned Spy never receives unauthorized true Grimoire content.
```

No production change before the relevant RED is proven to fail for the intended assertion.

### Phase GCR-1 — centralized current Demon authority GREEN

Prefer one typed authority over per-callsite patches. Preserve dead historical Demon role identity; do not “fix” uniqueness by rewriting history.

### Phase GCR-2 — impaired Spy information-integrity GREEN

Keep healthy Spy behavior unchanged. Route unreliable presentation through typed information legality/confirmation authority rather than a UI-only exception.

### Phase GCR-3 — test quality cleanup

Audit remaining source-string/source-order tests. Retire or reduce them when typed production seams exist. Gameplay correctness must not be proved solely by `.kt` text inspection.

High-priority review queue is maintained in:

```text
docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md
```

and the active GCR handoff.

### Phase GCR-4/5 — only after blockers

Follow-up work:

- Chambermaid “woke because own ability” should derive from actual canonical interaction history, not an expanding role-name allowlist;
- verify/freeze player-name uniqueness/immutability if names remain durable night-checkpoint target identity;
- clarify `NightTransactionReconstructor.effectiveState` naming if it represents final reconstructed night state rather than checkpoint-time state;
- consider systematic Dawn crash cut-point fault injection.

These do not automatically block GCR-1/2 unless a typed RED proves otherwise.

## 5. SNE-7 — CLOSED / BROAD GREEN

SNE-7 Same-Night Effective Mechanical State is closed. Do not resume its old handoffs.

Protected result, summarized:

```text
canonical night interactions
+ confirmed hidden choices
+ cursor-relative mechanical events
-> ClocktowerEffectiveNightState
-> later actor eligibility / information / death resolution
-> canonical DawnCommitIntent
-> idempotent durable Dawn materialization
-> restore/retry convergence
```

Key accepted checkpoints:

```text
730c494f9972ec6425563d04a05c7b2984dda16e
  production Dawn AliveAt durable observation GREEN

61387b473ff18e174b211a80962eed6cf0228ed6
  restore/retry convergence acceptance

70935644daf5c06985420f19833dbda3a160bbfa
  full T4 acceptance
```

Durable contracts to preserve:

- `ClocktowerNightCheckpoint` owns unfinished-night durable state;
- GameState + action/observation timelines own durable historical truth;
- mechanical death and public death announcement are distinct;
- stable durable IDs do not depend on mutable revisions/event counters;
- current role/effective state must be read at the correct same-night interaction point;
- Dawn replay/retry must be idempotent and exactly-once at history level.

Historical SNE execution handoffs/micro-checkpoints were removed from the active docs root and consolidated under:

```text
docs/archive/SNE7_AND_PRE_GCR_HANDOFF_CLOSEOUT_2026-08-28.md
```

## 6. Other completed architecture foundations

### A3 Historical Exact hardening

H1–H7 are complete/green. Preserve:

- setup roles vs dynamic current roles;
- state-aware historical replay;
- hidden-mechanics knowledge safety;
- Trouble Brewing-only fail-closed exact support;
- mechanically identical-world convergence.

The only remaining A3 item is immutable setup-snapshot ownership/persistence, which is deferred.

### B4 historical-exact shadow bridge

Production-isolated shadow bridge is green. It does not own Host recommendation authority and must not be promoted incidentally during GCR.

### App-root decomposition

Through S9.1 is complete/merged. S9.2 Active Game Persistence Boundary remains a deferred architecture option, not current work.

## 7. Deferred work registry

These are intentionally **not current tasks**:

| Deferred area | Status | Resume source |
|---|---|---|
| A3 immutable setup snapshot ownership/persistence | NOT STARTED | `docs/archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md` |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED | `docs/archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md` |
| Chambermaid actual wake-history authority | GCR follow-up | active GCR handoff |
| Night checkpoint stable identity hardening | GCR follow-up | active GCR handoff |
| Dawn systematic crash cut-point matrix | GCR follow-up | active GCR handoff |
| generic custom-script Demon succession | NOT AUTHORIZED | future dedicated design |
| Mayor redirect to Demon with generic succession | deliberately constrained | future dedicated design |
| Host/A4/ZDD recommendation promotion | NOT AUTHORIZED | existing architecture docs |
| history UI / misinformation tuning | NOT CURRENT | future roadmap decision |

A deferred task must be re-audited against live `main` before implementation. Its old handoff is context, not authority.

## 8. Source-string test policy

Gameplay/rules correctness should be typed.

Allowed long-term source inspection is limited to coarse architecture/ownership assertions where there is no callable seam. Source-string tests must not freeze incidental implementation details or stand in for rules behavior.

During GCR-3 classify each candidate:

```text
A typed replacement exists -> retire
behavior matters but no seam -> introduce narrow callable seam, then replace
pure architecture guard -> keep coarse only
obsolete implementation-shape assertion -> delete
```

Do not recreate previously retired source-string tests merely to restore coverage counts.

## 9. Test gates

Follow `docs/TESTING_STRATEGY.md`.

Tests-first minimum for each correctness change:

```text
T0 focused typed RED
-> assertion-level RED provenance
-> minimal GREEN
-> focused affected regressions
-> T1 :app:testFast
-> T2/T3 when the changed semantic family requires them
```

Before overall GCR merge-blocking closure, require a real T4 execution:

```text
./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks
```

plus applicable full CI semantic routes (including ASP / Real Clingo when selected) and R2 main-thread-boundary success.

A skipped, zero-job, cached-only or `UP-TO-DATE` route is not proof that a required gate executed.

## 10. Scope / branch discipline

Until explicit authorization:

- keep PR #54 draft/open/unmerged;
- do not mark ready;
- do not merge;
- do not rebase/force-push;
- do not mix GCR blockers with A3, S9.2, A4/ZDD, recommendation tuning or generic custom-script work;
- keep exact diff audits for large App/Host edits;
- preserve complete-worktree safety for `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt`.

## 11. Current next action

**Next implementation action is GCR-1 RED, not production GREEN.**

Start by proving the cross-night successor failure through a typed scenario that contains a dead historical Imp and a living successor Imp and exercises the real current-Demon consumer. Then add the repeated-succession and authority-convergence REDs before selecting the minimal production seam.

Do not begin GCR-2 production changes until its own information-integrity RED is established.
