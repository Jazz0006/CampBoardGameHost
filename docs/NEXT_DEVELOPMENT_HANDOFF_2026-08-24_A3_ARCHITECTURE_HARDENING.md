# A3 Historical Multi-Night Exact Baseline — Paused Handoff

> Date: 2026-08-24  
> Status: **PAUSED AT SAFE CHECKPOINT / PR #48 DRAFT / DO NOT MERGE**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Active A3 branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Stable `main` at closeout: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Latest fully validated **A3 code** checkpoint: `c678b25cd2a750a02f0cb1a05632d31e58ffd048`  
> Gates: **CI #675 SUCCESS / R2 #608 SUCCESS / Android + ASP + Real Clingo GREEN**

This handoff intentionally freezes A3/B4 work before setup-snapshot ownership and returns project focus to App-root source decomposition S7.

## 1. Resume protocol for A3 later

Before editing A3 again:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. query live `main`;
5. query PR #48 live head/state/checks;
6. exact-compare any docs-only PR head back to code checkpoint `c678b25cd2a750a02f0cb1a05632d31e58ffd048`;
7. continue only when the user explicitly chooses to resume A3;
8. do not merge, mark ready, rebase, force-push, or widen scope without explicit authorization.

Documentation-only closeout commits are expected after the code checkpoint and do not represent additional A3 code.

## 2. Completed A3 hardening state

```text
H1 GREEN  historical seed / exactly-once durable observations
H2 GREEN  state-aware ability eligibility + Ravenkeeper exception
H3 GREEN  mechanical convergence independent of provenance
H4 GREEN  Trouble Brewing-only support guard
H5 GREEN  immutable setup roles + dynamic currentRolesBySeat
H6 GREEN  incremental state-aware historical replay
H7 GREEN  knowledge-safe hidden mechanics integration
  H7.1  current-Demon attack branching
  H7.2  current-Monk protection branching
  H7.3  Other Night materialization boundary
  H7.4  Imp self-kill succession
  H7.5  self-kill materializer integration
  H7.6  Mayor night-death branching
  H7.7  Mayor materializer integration
  H7.8  canonical Other Night replay transition
  H7.9  public night-death reconciliation
  H7.10 no-public-death dawn reconciliation
  H7.11 persisted hidden-action payload isolation
```

No known Trouble Brewing H1–H7 correctness blocker remains at the historical exact-baseline boundary.

## 3. Protected knowledge-safety contract

`PlayerHistoricalTimeline` keeps only player-visible chronology:

```text
PublicExecution
PublicDeath
PhaseAdvance
recipient-visible Observation
```

Persisted Storyteller-hidden facts:

```text
Poison
Protect
Attack
RoleChange
```

may exist in GLOBAL history, but their hidden payloads and hidden occurrence points are omitted before player reasoning. Rule-derived hidden mechanics must be regenerated from rules + possible-world state.

Never consume the actual Storyteller-selected hidden target as player knowledge.

## 4. Latest integration work after H7.11

### 4.1 B4 historical-exact shadow bridge

Validated checkpoint:

```text
01a9aea20b4f224c6e7f911eef6c3fadb3f62be9
CI #672 SUCCESS
R2 #605 SUCCESS
```

`B4DynamicPlayerWorldSetShadow` retains its old public/default path. Historical exact behavior requires an explicit module-internal validated-ruleset opt-in. It still returns only a shadow report/cardinality and cannot alter production recommendation authority.

The bridge tests persisted `Protect` / `Attack` / `RoleChange` and proves B4 exact-shadow cardinalities match `EnumeratedHistoricalExactBaseline`.

### 4.2 Real session history -> B4 shadow seam

RED:

```text
91016ba67d2ceabdfdd0d6e31a127d6023e40c89
message: test(a3): lock session historical shadow bridge

ClocktowerB4HistoricalShadowCoordinatorTest.kt only
+137
production changes = 0
docs changes = 0
CI #673 expected compile RED: ClocktowerB4HistoricalShadowCoordinator missing
R2 #606 / ASP / Real Clingo GREEN
```

Final GREEN:

```text
c678b25cd2a750a02f0cb1a05632d31e58ffd048
ClocktowerB4HistoricalShadowCoordinator.kt
+75 from RED
RED test unchanged
CI #675 SUCCESS
R2 #608 SUCCESS
Android / ASP / Real Clingo SUCCESS
```

The test uses a real `ClocktowerGameSession` to commit persisted hidden actions, then passes the resulting session snapshot to the new internal coordinator. The coordinator's report must equal a direct exact B4 shadow evaluation over the same GLOBAL action/observation history.

Current production-isolated chain:

```text
ClocktowerGameSession
-> current GameSnapshot GLOBAL actionTimeline + epistemicObservationLog
-> ClocktowerB4HistoricalShadowCoordinator
-> B4DynamicPlayerWorldSetShadow(validatedRuleset)
-> EnumeratedHistoricalExactBaseline
-> B4ShadowReport only
```

The coordinator fails closed when setup/current session identity is incompatible, the current revision predates setup, or semantic history is not `GLOBAL_V1`.

## 5. What has deliberately NOT changed

```text
ClocktowerGameSession state ownership      unchanged
persistence schema / restore               unchanged
ClocktowerRecommendationCoordinator        unchanged
Host recommendation authority              unchanged
A4/ZDD selector authority                  unchanged
other scripts                              unsupported by A3 exact baseline
App-root decomposition                     separate workstream
```

Therefore PR #48 remains a safe draft checkpoint rather than an authority rollout.

## 6. Next A3 blocker when this work is resumed

**Setup snapshot ownership** is next, and is NOT STARTED.

Historical exact replay needs an immutable setup snapshot. Current app/session persistence stores the current snapshot plus GLOBAL history rather than a second durable setup snapshot. A future tests-first design must decide:

```text
1. who owns setupSnapshot for a live session;
2. whether setupSnapshot becomes persisted data;
3. how restore obtains the exact setup replay origin;
4. whether restored sessions without reliable setup provenance should DEFER_B4;
5. where a real runtime shadow invocation belongs after ownership is solved.
```

Do not simply add a second state copy to `ClocktowerGameSession` or persistence. Restore/schema semantics must be resolved first.

Do not combine that future work with Host authority, A4/ZDD promotion, other scripts, history UI, misinformation work, or App-root decomposition.

## 7. Immediate next project task: App-root source decomposition S7

The earlier Clocktower Host source decomposition is already complete through **A13** and merged. Do not resume at A9/A10.

The remaining structural workstream is App-root decomposition:

```text
S0–S6  CLOSED / MERGED checkpoint
S7     NEXT = fresh architecture audit
```

Read first:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_S7.md
```

That handoff records:

```text
CampBoardGameHostApp.kt original   325,556 bytes
after S6                         229,822 bytes
net reduction                     95,734 bytes (~29.4%)
```

The 50 KiB target is a maintainability guideline, not a hard gate. S7 must begin with a **fresh architecture audit**, not automatic implementation and not an old candidate ranking.

Important live-state correction at this A3 closeout:

```text
old branch: codex/source-decomposition-app-root
relative to current main: ahead 0 / behind 3
```

So do not continue writing directly on that stale branch. Re-query live `main` and relevant PR/branch state, then choose the correct fresh branch/worktree strategy from current main before creating an S7 RED.

S7 audit must determine:

```text
1. exact next cohesive owner filename;
2. exact declarations to move;
3. all direct cross-file dependencies;
4. required visibility changes;
5. Root state/effect/transaction ownership that must remain;
6. stale characterization tests needing semantic updates;
7. focused RED failure reasons;
8. production allowlist;
9. full validation commands;
10. exact-diff criteria and STOP conditions.
```

Protected boundaries:

```text
- Clocktower live transaction ordering
- Compose effect lifetime (`LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberUpdatedState`)
- persistence / restore / archive ownership
- A4 lifecycle/cache/prewarm effects
- Clocktower planner/projector/materializer/session authority
- A3/B4 behavior
```

If no natural low-risk slice remains, stop the App-root pass rather than manufacturing an artificial owner solely to reduce bytes.

## 8. Documentation discipline

The user explicitly does **not** want documentation updated after every small RED/GREEN slice or SHA/CI change.

Update authoritative docs only when one of these is true:

```text
- a meaningful phase is formally closed;
- roadmap/next target materially changes;
- a cross-conversation handoff is needed.
```

This file is being updated because A3 is intentionally paused and the project is switching back to App-root source decomposition S7.
