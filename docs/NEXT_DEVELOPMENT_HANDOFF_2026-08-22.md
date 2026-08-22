# NEXT DEVELOPMENT HANDOFF — 2026-08-22 (POST PR #27)

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Specialized design: `docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`  
> Current next task: **Storyteller Information Decision Foundation**  
> Status: **CURRENT HANDOFF**

## 1. Current trusted baseline

The source baseline immediately before this docs-only handoff refresh is:

```text
main / PR #27 merge commit                    5bbb607ae408d5d9d25812825200304054a7aced
PR #28 Drunk/Poison mechanical correctness    MERGED
PR #24 Production Semantic-History Foundation MERGED
PR #29 Impaired Information Semantics         MERGED
PR #27 Global Observation Ownership           MERGED
repository visibility                          PUBLIC
GitHub Actions public runners                  VERIFIED GREEN
```

PR #29 merge commit:

```text
b2c0b2c7a91290670d908292b3db5719d6bd6ddb
```

PR #27 final source head / merge commit:

```text
head   0b740a5026d46beb88d18092185ce9b7bd5700ce
merge  5bbb607ae408d5d9d25812825200304054a7aced
```

PR #27 final validation:

```text
CI #402                         GREEN
  Android unit tests + APK      GREEN
  ASP contract tests            GREEN
  Real Clingo cross-validation  GREEN
R2 #358                         GREEN
final Codex review              CLEAN / no major issues
review threads                  6 / 6 RESOLVED
```

Because this handoff itself is committed after `5bbb607a...`, a new session must query live `main` before creating a branch. Treat `5bbb607a...` as the latest validated **source baseline**, not a forever-current HEAD.

## 2. What has just completed

### PR #29 — Impaired Information Semantics

The truthful-vs-false family decision for impaired information is now centralized.

Current authority:

```text
healthy
    → truthful by default

Drunk / Poisoned
    → strongly prefer legal false information
    → explicit truthful exceptions remain legal and explainable

balance / style
    → may rank legal candidates
    → must not own truthful-vs-false family choice
```

Do not reopen this as a simple “change the percentage” task unless a new correctness bug is found.

### PR #27 — Global Observation Ownership

New Clocktower games now use Global observation ownership through `ClocktowerGameSession`.

Important completed boundaries:

- Host creates unbound `EpistemicObservationDraft` values;
- session owns Global identity/sequence allocation;
- restored `LEGACY_LOCAL` games retain compatibility behavior;
- exact duplicate Global commits are adapter-idempotent;
- Global revision supersession invalidates stale A4 work;
- Death/Execution preflight occurs before caller state/event publication;
- Virgin + Spy registration path preflights before registration event mutation;
- private information redisplay/correction uses statement-versioned record identity.

Do not create another Global cursor or move identity allocation back into UI/Host.

## 3. Immediate objective

Create a **new focused tests-first PR** for:

# Storyteller Information Decision Foundation

This is the semantic/authority foundation for later structured manual Storyteller selection.

The next PR should **not** start by building manual UI.

The objective is to make automatic recommendation and manual selection peer inputs to one decision/validation pipeline.

Target model:

```text
Actual / registered game state
        ↓
role-specific legal information builder
        ↓
impairment policy
        ↓
InformationDecisionContext
   ├── recommended candidate(s)
   └── manual legal candidate(s)
        ↓
Storyteller confirmation
        ↓
shared semantic/rules validation
        ↓
confirmed result
        ↓
EpistemicObservationDraft
        ↓
ClocktowerGameSession
```

Core principle:

> Recommendation is advice, not authority. Storyteller confirmation is authority.

## 4. Preferred Foundation shape

Names are illustrative, not mandatory. Prefer a small pure semantic layer such as:

```text
InformationDecisionContext
InformationDecisionSource
InformationDecisionValidationResult
InformationDecisionWarning
```

Initial decision provenance should remain intentionally small:

```text
MANUAL
RECOMMENDATION_ACCEPTED
```

The Foundation should own or clearly expose:

- legal candidate set;
- recommended candidate(s);
- candidate semantic identity;
- current game/input revision or equivalent freshness token;
- shared validation result;
- hard-block reason when illegal;
- soft warning(s) when legal but discouraged;
- conversion from confirmed legal choice to the same `EpistemicObservationDraft` shape used by production Global ownership.

Avoid adding broad abstractions that are not required by the first tests.

## 5. Required RED contracts first

Before production implementation, add executable tests for at least:

1. **Equivalent authority path**  
   `RECOMMENDATION_ACCEPTED` and `MANUAL` selecting the same legal result produce equivalent confirmed semantic output / observation draft.

2. **Healthy false hard block**  
   A healthy information role cannot manually choose a false result unless real role/registration semantics make that result legal.

3. **Impaired manual legal space**  
   Drunk/Poisoned subjects may manually choose from their legal unreliable result space.

4. **Role-format legality**  
   Wrong target count, wrong result shape, impossible categorical/numeric value, or invalid structured pair must hard block.

5. **Soft warning is not hard block**  
   A legal-but-discouraged manual result may proceed with a warning.

6. **No semantic bypass**  
   Manual and recommendation paths both consume the same impairment / registration / role-format validation boundary.

7. **Stale decision rejection**  
   A recommendation or manual context created for an old revision/context cannot be confirmed after the relevant game/input revision changes.

8. **Decision provenance**  
   The confirmed result distinguishes `MANUAL` from `RECOMMENDATION_ACCEPTED` without creating divergent semantic pipelines.

9. **Same observation authority**  
   Both paths produce the same unbound `EpistemicObservationDraft` model; neither assigns Global timeline identity.

10. **Legacy direct recommendation regression**  
    Add a production-wiring/structural contract preventing a recommendation from directly becoming durable observation state outside the shared decision seam.

Capture genuine RED before writing production implementation.

## 6. Manual selection boundary

Manual does **not** mean unrestricted free text.

Future structured examples:

```text
Empath          → 0 / 1 / 2
Fortune Teller  → legal yes/no presentation
Undertaker      → legal role identity
Investigator    → legal Minion role + two-player pairing
```

The Foundation PR may model these legal spaces where needed for tests, but should not build the full production UI yet.

### Hard block examples

- violates official role format;
- wrong target count/type;
- healthy role receives an illegal false result;
- result cannot be represented as a valid proposition / observation draft;
- stale decision context.

### Soft warning examples

- Drunk/Poisoned subject is given truthful information while strong legal false candidates exist;
- legal result differs substantially from the algorithm recommendation;
- choice may expose impairment;
- choice is legal but highly disruptive to balance/style goals.

Storyteller may confirm through soft warnings, but not through hard blocks.

## 7. Explicit non-goals for the next PR

Do NOT expand the Foundation PR into:

- complete manual Storyteller UI;
- history UI redesign;
- Historical Action + Observation Capture;
- Spy/Recluse registration rewrite;
- Investigator small-player balance tuning;
- broad evil-side win-rate tuning;
- A3 historical multi-night expansion;
- B4 expansion;
- ZDD production promotion;
- ML / personalized learning;
- advanced new-script recommendation semantics;
- arbitrary free-text information input.

If a RED test reveals a true official-rules correctness bug in a touched path, classify it separately before broadening scope.

## 8. Architecture boundaries that must survive

### Registration before impairment

```text
actual world
    ↓
registration projection
    ↓
truthful result / legal information space
    ↓
impairment policy
    ↓
storyteller decision
```

Spy/Recluse registration is not a Drunk/Poison misinformation probability feature.

### Player knowledge safety

Do not inject storyteller-only truth into player-knowledge-safe cores merely to make manual/recommendation UI easier.

### Session authority

`ClocktowerGameSession` continues to own Global timeline identity. `InformationDecisionContext`, UI, recommendation modules, and manual selectors must not assign Global sequence numbers.

### Recommendation status

Recommendation is a candidate/advice signal. It must no longer be treated as the final durable fact authority once this Foundation is installed.

## 9. Validation gate

For the final implementation head require:

```text
new focused decision tests                    GREEN
existing impaired-information policy tests    GREEN
existing recommendation tests                 GREEN
Global observation/session tests              GREEN
full Android unit tests                       GREEN
assembleDebug                                  GREEN
R2 main-thread boundary                       GREEN
ASP contract tests                            GREEN
Real Clingo cross-validation                  GREEN
exact diff audit                              CLEAN / EXPECTED FILES ONLY
final Codex review                            CLEAN
all review threads                            RESOLVED
```

The repository is Public; GitHub Actions must execute real checkout/compiler/test steps.

## 10. Branch / merge discipline

- Read `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` first.
- Query live `main` before branch creation.
- Start a new short-lived focused branch from latest `main`.
- RED first.
- Keep the first implementation minimal and semantic.
- Whole-file replacement is acceptable for large files when sourced from the target branch live head with blob-SHA guard; always exact-diff audit afterward.
- Do not create temporary Actions writer infrastructure merely because a file is large.
- Use writer/blob-graft only for the documented fallback cases.
- Do not merge without the normal review/CI gate.

## 11. Stop condition for the next development session

The next session should stop after:

```text
focused Storyteller Information Decision Foundation PR exists
RED provenance is captured
minimal shared decision/validation seam is implemented
recommendation path is routed through the seam at the intended boundary
manual semantic path exists in tests/API without requiring full UI
focused + full CI are green
exact diff is audited
final review is clean
PR is ready for merge/review
```

Do **not** automatically continue into Structured Manual UI or Historical Action + Observation Capture in the same slice unless explicitly authorized after this Foundation is reviewed/merged.

## 12. What follows after the Foundation

Expected order:

```text
Storyteller Information Decision Foundation       NEXT
        ↓
Structured Manual Storyteller Information UI
        ↓
Historical Action + Observation Capture
        ↓
A3 historical multi-night exact baseline
        ↓
authoritative physical Grimoire ledger / Spy VerifiedExact
        ↓
B4 historical expansion
        ↓
revision-driven recommendation/history unification
        ↓
reconsider ZDD production promotion
```

Multi-script support levels remain:

```text
LEVEL 1  Flow supported
LEVEL 2  Manual legal information supported
LEVEL 3  Automatic recommendation supported
LEVEL 4  Advanced balance-aware recommendation supported
```

The Storyteller Decision work is the key foundation for making Level 2 practical across future scripts.