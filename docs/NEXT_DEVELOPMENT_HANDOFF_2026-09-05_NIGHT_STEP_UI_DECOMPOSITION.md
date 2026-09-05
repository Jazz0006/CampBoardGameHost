# NEXT DEVELOPMENT HANDOFF — Night Step UI Ownership Decomposition

> Status: **CURRENT ACTIVE HANDOFF**  
> Date: 2026-09-06 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Purpose

The next architecture campaign is to reduce coupling in the Night Step UI after the recent Host Table / inline-wake / Manual bluff / Drunk ownership work has landed.

This is **ownership decomposition**, not a file-size cleanup campaign.

Primary reference:

`docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`

This handoff is subordinate to root `AGENTS.md` and `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

## 2. Confirmed live baseline at handoff refresh

At 2026-09-06 documentation refresh, live `main` was:

```text
93f99e0576be7b93d479ffa931bae3e4083c25af
Fix Drunk shown-identity ownership boundary (#105)
```

Recent integrated milestones:

```text
PR #99  R4D-6 Host Table integration
PR #100 UI-N1 inline wake cues + shared square-table readability
PR #101 same-night dead-role wake-step fix
PR #102 Manual Demon bluff consistency fix
PR #104 obsolete source-wiring guard retirement
PR #105 Drunk shown-identity ownership repair
```

Re-query live `main` before doing any implementation; do not assume this SHA remains current.

The earlier handoff warning that the Drunk ownership repair might still be unmerged is now obsolete. PR #105 is integrated into `main`.

## 3. Product assumptions that must not regress

The older 2026-09-04 UI-N1 design described an explicit:

```text
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

acknowledgement lifecycle.

PR #100 reflects the later user-approved product decision:

- no separate WAKE acknowledgement state;
- actor/wake cue appears on the same persistent square table as target selection;
- actor state is visually/semantically orthogonal to legal/selected/disabled target state;
- selection remains editable until the existing finish/next boundary.

Also preserve:

- Drunk shown identity as committed setup state, not recommendation lock state;
- Manual Demon bluff display consuming the intended recommendation triple rather than arbitrary legal fallback;
- recommendation-rationale UI remaining minimal until quality/consistency algorithms can provide meaningful explanations.

Do not resurrect superseded lifecycle or ownership assumptions during decomposition.

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

## 7. Required first-slice report

Before implementation of the selected first slice, produce a concise architecture report containing:

```text
selected responsibility
current owner
new owner
input contract
output / intent contract
state lifetime before -> after
dependency direction before -> after
file allowlist
focused evidence
obsolete source-shape tests to retire/narrow
expected context-radius reduction
```

The first slice should be independently reviewable and behavior-preserving. Do not bundle a second seam merely because nearby code is convenient to move.

## 8. Test/evidence contract

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

## 9. Architecture guardrails

The decomposition campaign must not:

- create `NightStepContext` / broad `State` / `Args` parameter bags;
- create vague `Utils`, `Helpers`, `Manager`, `Common` owners;
- move code while keeping a dependency on the whole old screen/Host;
- widen visibility only to permit extraction;
- hoist UI-local transient state into session/game state without a lifetime requirement;
- move domain legality into UI;
- introduce one file per tiny function/role;
- change gameplay behavior as an incidental refactor;
- redesign recommendation quality / EPI-MQ;
- start Demon Bluff Recommendation V1 inside the decomposition slice.

## 10. Product constraints to preserve

- persistent square-table physical seat identity;
- current actor cue on the same table as action selection;
- target legality remains typed upstream authority;
- Pair Manual legal domain remains authoritative;
- Storyteller actual/shown role detail remains private from Player Reveal;
- deterministic vs Storyteller-discretion information paths remain distinguishable;
- Manual selection direction remains compatible with a dedicated full-screen workflow;
- recommendation-rationale UI should not pretend to have meaningful explanations before the quality/consistency system can provide them.

## 11. Architecture checkpoint completion rule

The campaign reaches its checkpoint when the selected decomposition work has produced a meaningfully smaller Night Step change context radius and the following hold:

- coherent responsibilities have moved to narrow owners rather than merely new files;
- `ClocktowerNightStepUi.kt` is materially more composition-oriented;
- no God context or reverse dependency was introduced;
- existing behavior is preserved by typed evidence / compile checks / exact diff audit;
- obsolete source-shape tests created by old ownership have been retired or narrowed;
- further decomposition candidates can be deferred without leaving a half-migrated ownership boundary.

Do not chase a numeric file-size target if ownership is already clean enough for the next product phase.

## 12. Next phases after architecture checkpoint

The approved order after this decomposition campaign is:

```text
Night Step ownership decomposition checkpoint
-> UI-R5 real-device stabilization / feature freeze
-> Demon Bluff Recommendation V1
-> EPI-MQ / Productive Uncertainty
-> UX-R6 recommendation-provider replacement
-> Beginner Storyteller Mode policy rollout
```

Demon Bluff V1 authority:

`docs/DEMON_BLUFF_RECOMMENDATION_V1_PLAN_2026-09-06.md`

Important boundary:

- Demon Bluff V1 is setup-time package recommendation and can be implemented before EPI-MQ;
- it must not pull historical world replay or cognitive-consistency work forward;
- EPI-MQ remains the later owner of hypothetical-world / productive-uncertainty capability;
- a future Bluff V2 may consume EPI-MQ outputs after that foundation is complete.

## 13. Start command for the next development session

A new development session can begin with this instruction:

```text
Read root AGENTS.md, docs/CURRENT_DEVELOPMENT_ROADMAP.md,
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md,
and docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md.
Re-query live main, then perform the required read-only Night Step UI ownership audit.
Select exactly one first behavior-preserving decomposition slice and report its owner/API,
file allowlist, state/dependency changes, evidence plan, obsolete source-shape tests,
and expected change-context-radius reduction before production edits.
Do not redesign gameplay, recommendation quality, Demon Bluff V1, or EPI-MQ in this slice.
```
