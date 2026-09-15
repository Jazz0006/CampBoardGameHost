# NEXT DEVELOPMENT HANDOFF — EPI-MQ-0.5 Dynamic-Script Extensibility Guard

> Date: 2026-09-13 Australia/Sydney  
> Status: **CURRENT after UI-INFO-1 closeout / PR #121 merge**  
> Program: Epistemic Misinformation Quality / Productive Uncertainty

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`;
6. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
7. query live `main` after PR #121 has merged.

Do not reuse the 2026-09-11 audit baseline as if it were still live. Reconfirm ownership against current `main` before production edits.

## 1. Immediate goal

Resume EPI-MQ with the smallest architecture-first slice:

> **EPI-MQ-0.5 — establish a generic epistemic world-evaluation capability boundary that can report READY vs DEFERRED/UNSUPPORTED semantics without making EPI-MQ depend on Trouble Brewing concrete classes.**

This is a prerequisite to the neutral hypothetical observation evaluator. It is not a recommendation-ranking change.

## 2. First step — read-only live delta audit

Before editing production code, re-audit the live ownership chain around:

```text
InformationDecisionContext / EpistemicObservationDraft
ClocktowerGameSession.preflightGlobalEpistemicObservation
B4DynamicPlayerWorldSetShadow
EnumeratedHistoricalExactBaseline
EnumeratedHistoricalWorldReplay
TroubleBrewingWorldEnumerator / EnumeratedWorldSet
current script/role capability representation
```

Confirm which pieces changed since the 2026-09-11 audit and identify the narrowest durable typed seam for generic capability/evaluation ownership.

The audit must explicitly answer:

- where generic script/role epistemic capability should live;
- how unsupported semantics are represented as DEFERRED rather than fake UNSAT;
- how current Trouble Brewing exact behavior plugs into that seam without becoming the EPI-MQ API;
- whether B4 can later consume the same neutral owner without recommendation -> B4 coupling;
- what minimum test seam proves dynamic-script extensibility without implementing Moonchild/Pukka semantics now.

## 3. Scope fences

EPI-MQ-0.5 may:

- introduce/refine a typed generic epistemic capability contract;
- expose READY / DEFERRED / missing-capability diagnostics;
- adapt existing Trouble Brewing exact machinery behind that contract;
- add durable typed tests at the true epistemic ownership boundary;
- perform narrowly required ownership extraction when the live audit proves it necessary.

EPI-MQ-0.5 must not:

- change recommendation weights or production candidate ranking;
- change legal candidates or gameplay rules;
- activate A4/ZDD as production correctness authority;
- add actual Moonchild/Pukka game semantics merely to prove extensibility;
- copy Storyteller-hidden targets into recipient world constraints;
- create a second legality/candidate model;
- wire recommendation code directly to `B4DynamicPlayerWorldSetShadow`;
- broaden into UI work.

## 4. Stable architecture rules

Preserve the existing dependency direction:

```text
rules/recommendation -> legal typed candidates
session              -> durable state/history/revision/observation identity
epistemic            -> recipient-visible knowledge + exact world reasoning
EPI-MQ               -> future policy consumer of neutral epistemic evaluation
```

Current exact correctness remains:

```text
setup/static:
TroubleBrewingWorldEnumerator + EnumeratedWorldSet

historical/multi-night:
EnumeratedHistoricalExactBaseline + EnumeratedHistoricalWorldReplay
```

A4/ZDD remains shadow/representation work unless a later explicit cutover authorizes otherwise.

## 5. Required behavioral contract

The new generic boundary must make unsupported mechanics explicit. Conceptually:

```text
READY
  diagnostics = ...
```

or:

```text
DEFERRED / UNSUPPORTED_SEMANTICS
  missingCapabilities = [...]
```

Unknown role/script semantics must never silently become:

- an exact-looking world count;
- `UNSAT`;
- an empty candidate result interpreted as contradiction.

`DEFERRED != UNSAT` remains a hard invariant.

## 6. Test direction

Follow `docs/TESTING_STRATEGY.md`.

Prefer focused typed tests under `clocktower/epistemic` proving the durable capability seam. The first slice should cover at least:

- current supported Trouble Brewing semantics report READY;
- an intentionally unsupported role/script capability reports DEFERRED with explicit missing capability;
- capability reporting is deterministic and mutation-free;
- the contract does not expose actual Storyteller-hidden action targets;
- EPI-MQ-facing code can depend on the generic contract without importing Trouble Brewing concrete world-engine classes.

Do not create Host/UI source-string tests for this work.

At a logical checkpoint run T1 plus affected epistemic/history validation. Escalate to Real Clingo/T4 when exact/oracle semantics actually change, not for a purely mechanical interface extraction.

## 7. UI-INFO-1 handoff boundary

UI-INFO-1 is closed by PR #121. Its historical handoff has been moved to `docs/archive/` and is no longer execution authority.

Do not reopen UI-INFO-1 during EPI-MQ unless a new independently reproduced UI regression requires separate prioritization.

## 8. Stop condition for the first implementation slice

Stop after EPI-MQ-0.5 when:

- live ownership has been re-audited;
- the generic capability/deferred boundary exists at the correct owner;
- current Trouble Brewing behavior is preserved;
- unsupported semantics are explicit;
- focused typed tests and required checkpoint validation pass;
- no recommendation ranking/provider behavior has changed.

Then update the roadmap before starting EPI-MQ-1 neutral hypothetical observation evaluation.
