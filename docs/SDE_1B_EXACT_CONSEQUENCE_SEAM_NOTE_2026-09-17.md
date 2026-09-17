# SDE-1B/1C Exact-Consequence Seam Note

> Date: 2026-09-17 Australia/Sydney  
> Branch: `sde-1-orchestration-seam`  
> Status: **IMPLEMENTED ON BRANCH / VALIDATION PENDING**

## Scope

This first executable SDE slice is deliberately evaluation-only.

Implemented flow:

```text
already-legal / already-materialized hypothetical candidate
→ StorytellerDecisionRequest + StorytellerDecisionContext
→ StorytellerDecisionEngine.evaluate(...)
→ ExactHistoricalHypotheticalObservationBundleEvaluator
→ CandidateConsequence
```

The seam does not own or change:

- legal candidate generation;
- proposition semantics;
- exact world solving;
- production candidate selection;
- UI routing;
- `InformationDecisionContext` confirmation;
- session semantic-history commit;
- flow ordering;
- Drunk / Spy-Recluse / Poisoner uncertainty.

## Focused contract

`StorytellerDecisionEngineTest` uses a healthy first-night Empath structured numeric observation and checks that SDE returns the same exact BEFORE/AFTER cardinality and structural diagnostics as direct invocation of the existing exact evaluator.

The test also checks:

- deterministic repeated evaluation;
- candidate identity preservation;
- no mutation of action timeline or observation log;
- exact capability deferral is surfaced unchanged, with no heuristic fallback.

## Validation

No local Gradle environment is available in this connector-only execution path. Validation should run through the branch PR CI before any merge decision.
