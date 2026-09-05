# NEXT DEVELOPMENT HANDOFF — Night Step UI Ownership Decomposition

> Status: **CURRENT ACTIVE HANDOFF**  
> Date: 2026-09-05 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Purpose

The next architecture campaign is to reduce coupling in the Night Step UI after the recent Host Table / inline-wake work has landed.

This is **ownership decomposition**, not a file-size cleanup campaign.

Primary reference:

`docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`

This handoff is subordinate to root `AGENTS.md` and `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

## 2. Known live baseline at handoff creation

At 2026-09-05 documentation sync, live `main` was:

```text
6d787172d4084e0af9ab74cb35e06f492cbb19fd
Merge PR #102: Fix manual Demon bluff consistency
```

Recent integrated milestones:

```text
PR #99  R4D-6 Host Table integration
PR #100 UI-N1 inline wake cues + shared square-table readability
PR #101 same-night dead-role wake-step fix
PR #102 Manual Demon bluff consistency fix
```

Re-query live `main` before doing any implementation; do not assume this SHA remains current.

## 3. Important superseded product assumption

The older 2026-09-04 UI-N1 handoff described an explicit:

```text
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

acknowledgement lifecycle.

PR #100 reflects the later user-approved product decision:

- no separate WAKE acknowledgement state;
- actor/wake cue appears on the same persistent square table as target selection;
- actor state is visually/semantically orthogonal to legal/selected/disabled target state;
- selection remains editable until the existing finish/next boundary.

Do not resurrect the obsolete explicit wake-phase state during decomposition.

## 4. First required step — read-only fresh ownership audit

Before production edits, inspect live `main` and build a current ownership map for the Night Step UI cluster.

At minimum inspect:

```text
ClocktowerNightStepUi.kt
HostTableShell / square-table renderer and presentation models
HostSeatPresentation / actor-cue path
ClocktowerPairManualAuthority.kt
ClocktowerPairRecommendationPresentationUi.kt
StructuredNumericInformationAdapter.kt
StructuredBooleanInformationAdapter.kt
ClocktowerHostSelectionSemantics.kt
relevant target-selection / materializer owners
relevant typed tests
```

Record for each responsibility:

- authoritative state owner;
- domain/rules owner;
- side-effect owner;
- rendering owner;
- dependency direction;
- current parameter/callback surface;
- existing owning evidence.

Do not edit production code during this first audit unless the user explicitly asks to proceed directly through implementation.

## 5. Strong candidate seams from the earlier audit

### A. Pair Manual presentation + UI-local state

Preferred direction:

```text
PairInformationLegalDomain
-> ClocktowerPairManualAuthority
-> PairManualPresentationModel
-> PairManualSelectionSection / dedicated screen
-> typed selection intent/result
```

The UI must not re-parse proposition grammar to recreate legal meaning.

### B. Player-display projection

Centralize repeated mapping from selected semantic result / `ClocktowerDisplayOption` to sanitized player-facing display state.

The question “what exactly will the player see?” should have one pure presentation projection authority rather than many event-handler `step.copy(...)` blocks.

### C. Structured information preparation

Move role/action/seat/value preparation upstream so Compose consumes a prepared numeric/boolean/none presentation model.

### D. Interaction renderer families

Only after narrow contracts exist, group roles by real interaction lifecycle (single-target, two-target, Storyteller ruling) rather than extracting one role per file or moving the giant action `when` unchanged.

### E. Recommendation/audit/diagnostics cleanup

Move benchmark/logging/diagnostics and broad orchestration only after higher-value ownership seams are stable.

## 6. Slice selection rule

The earlier S1–S5 order is provisional.

Choose the first implementation slice only after the current audit shows that it:

1. removes one coherent responsibility from the broad owner;
2. has a narrow typed input/result boundary;
3. does not require a God context;
4. preserves state lifetime and domain authority;
5. has strong existing typed evidence or a clear durable characterization gap;
6. materially reduces change context radius.

Pair Manual is the default candidate, not a mandate.

## 7. Test/evidence contract

Follow the integrated architecture + test-first policy in `AGENTS.md`.

For a behavior-preserving extraction:

```text
identify existing owning tests
-> confirm baseline if useful
-> add durable characterization only for real uncovered risk
-> extract/refactor
-> run smallest affected typed evidence / compile checks
-> exact diff + invariant audit
-> retire superseded source-shape assertions
```

Do not add a RED simply because a file moves.

If the slice creates a new durable typed seam that represents a real contract, add direct contract coverage when existing tests do not prove it.

Known source-shape debt around structured Empath/Night Step wiring should be re-evaluated when typed seams replace the old local wiring; do not preserve obsolete source spelling/order for those tests.

## 8. Architecture guardrails

The first decomposition campaign must not:

- create `NightStepContext` / broad `State` / `Args` parameter bags;
- create vague `Utils`, `Helpers`, `Manager`, `Common` owners;
- move code while keeping a dependency on the whole old screen/Host;
- widen visibility only to permit extraction;
- hoist UI-local transient state into session/game state without a lifetime requirement;
- move domain legality into UI;
- introduce one file per tiny function/role;
- change gameplay behavior as an incidental refactor;
- redesign recommendation quality / EPI-MQ.

## 9. Product constraints to preserve

- persistent square-table physical seat identity;
- current actor cue on the same table as action selection;
- target legality remains typed upstream authority;
- Pair Manual legal domain remains authoritative;
- Storyteller actual/shown role detail remains private from Player Reveal;
- deterministic vs Storyteller-discretion information paths remain distinguishable;
- Manual selection direction remains compatible with a dedicated full-screen workflow;
- recommendation-rationale UI should not pretend to have meaningful explanations before the quality/consistency system can provide them.

## 10. Stop / checkpoint rule

After the fresh audit and first-slice design, report:

- proposed owner and API;
- file allowlist;
- state ownership before/after;
- dependency direction before/after;
- evidence plan;
- source-string tests to retire/narrow;
- expected change-context-radius improvement.

Then implement only within that approved slice. Do not expand into the next candidate seam merely because the first extraction is convenient.

## 11. Next phases after architecture checkpoint

```text
Night Step ownership decomposition checkpoint
-> UI-R5 real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty
-> UX-R6 recommendation-provider replacement
```
