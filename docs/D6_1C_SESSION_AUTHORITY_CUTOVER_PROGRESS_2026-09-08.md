# D6.1c Session Authority Cutover — Progress / Closeout

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-root-reaudit`
> Draft PR: #113 `D6: Clocktower session authority cutover`
> Status: **D6.1c IMPLEMENTATION + OWNERSHIP AUDIT COMPLETE — D6.1d NEXT — DO NOT MERGE YET**

## Checkpoints

```text
live main:
2c495ee547e8327b0d3c3a811f5c891ba34fd863

D6.1c one-shot trigger:
3ce9e506fab3195e65fa028e6a0b8251a100bac1

D6.1c production checkpoint:
bd0161c50a7e4d2c94187c553546696bf6e81aee
refactor: cut over Clocktower session authority

one-shot cleanup head before this docs closeout:
68e8d8c9458a9e9e192c711199e352c9623db712
chore: remove D6.1c one-shot tooling
```

The production checkpoint changed exactly one production file:

- `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt`

The temporary one-shot workflow and patch script were removed automatically after the product commit.

## What D6.1c changed

D6.1c made one live `ClocktowerGameSession` the sole writable authority for the already-proven session subset:

- game ID;
- game seed;
- script identity represented by the session;
- `gameStateRevision`;
- `playerInputRevision`;
- semantic-history mode;
- action timeline;
- epistemic observation log;
- next global timeline sequence.

`CampBoardGameHostApp.kt` now keeps only:

```text
ClocktowerGameSession? = writable owner reference
ClocktowerSessionView? = immutable/read-only Compose projection
```

All cutover mutations go through the session first and then publish a fresh view. App root no longer owns independently writable mirrors for the cutover subset.

## Critical boundary intentionally preserved

D6.1c did **not** make `ClocktowerSessionState.gameState` the live canonical production mechanical state.

Current mechanics still mutate `cards` and related App-root mechanical state. The session `gameState` created/restored in D6.1c is therefore not yet safe for production consumers to treat as canonical between mechanic boundaries.

That ownership problem belongs to D6.1d and must be solved in cohesive, behavior-proven pieces rather than by rewriting all day/night mechanics at once.

## Structural ownership audit — PASS

### Immutable projection

`ActionFactTimeline` and `EpistemicObservationLog` snapshot their caller-owned lists and expose unmodifiable lists; append operations return new value objects. `ClocktowerSessionView` therefore does not expose a mutable history collection that can be used to bypass the session owner.

### App-root writer audit

Post-cutover source audit found no remaining direct App-root mutation of the cutover history/identity/revision subset:

- no direct observation-list `clear/add/addAll` ownership;
- no direct action-timeline assignment ownership;
- no direct game ID/seed/revision/global-cursor setters;
- semantic action/observation mutation routes through `ClocktowerGameSession`;
- observation preflight remains non-mutating.

### Session boundary / archive / recovery audit

Archive/reset/recovery boundaries clear or replace the session owner and publish a fresh projection. They do not mutate the projection as a back door.

Recovery v2 schema/planner/codec were not redesigned by D6.1c.

## Validation evidence

Third fail-closed one-shot run:

```text
D6.1c Clocktower Session Authority One Shot
run 34203877479 — PASS
```

The successful run established:

```text
HEAD/blob locks — PASS
patch application — PASS
exact ownership/semantic audit — PASS
focused validation — PASS
:app:testFast — PASS
product commit + push — PASS
one-shot tooling self-cleanup — PASS
```

Normal PR checks on the same user-authored trigger commit were also green:

```text
CI 34203881890 — PASS
R2 main-thread boundary 34203881911 — PASS
```

The subsequent cleanup commit was authored by `github-actions[bot]`; its CI/R2 workflow records were `action_required` with no jobs, so that cleanup head is not used as the behavioral validation checkpoint. The product cutover already has normal green CI/R2 evidence above.

## Preserved invariants

1. no synthetic NGJ `RulesetRef`;
2. revision cadence/order preserved;
3. action/observation share one monotonic global chronology;
4. observation preflight remains non-mutating;
5. action/observation idempotency contract unchanged;
6. Recovery v2/current-version-only policy unchanged;
7. RecoveryWriteGate/lifecycle persistence topology unchanged;
8. A4 invalidation/durability ordering preserved;
9. no Compose dependency added to session/domain code;
10. restart/archive/recovery cannot retain a stale session owner;
11. Undercover/Werewolf untouched;
12. no intended gameplay or user-visible semantic change.

## D6.1d — NEXT

### Goal

Re-audit the remaining App-root mechanical `GameState` ownership after D6.1c, then cut over the **smallest cohesive state + mutation boundary** whose behavior can be proven equivalent.

### Required first step

Before production edits, build an exact source map of:

- App-root mechanical state holders;
- every `cards` mutation path;
- every `cards.toClocktowerGameState(...)` projection;
- every `advanceClocktowerGameStateRevision()` callsite;
- session `gameState` reads/writes;
- downstream A4/recommendation/Judge/Recovery consumers that currently use revision identity versus actual mechanical state.

Then rank candidate D6.1d slices by ownership cohesion, mutation completeness, behavior evidence, Android/UI coupling and blast radius.

### Guardrails

Do not:

- treat session `gameState` as canonical before the audited cutover;
- rewrite all Clocktower day/night mechanics together;
- reopen PS5 persistence/recovery design;
- change gameplay semantics or Recovery schema;
- create a broad Manager/Controller abstraction;
- split code only to reduce file size;
- start D6.1e cleanup or merge PR #113.

Testing remains risk-based: use existing focused behavior evidence for structural movement, add a typed RED only for a real uncovered behavior/invariant, then run focused + FAST + affected T2/R2 as required.
