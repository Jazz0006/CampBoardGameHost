# Next Development Handoff — UX-R2 Pair Decision Foundation

> Date: 2026-09-01 Australia/Sydney  
> Status: **ACTIVE CURRENT HANDOFF**  
> Branch: `codex/clue-ux-r2-manual-pair-selector`  
> Draft PR: #63 — `UX-R2: establish structured manual pair selection`  
> Do not merge or mark ready without explicit user authorization.

## 1. Read first

Before continuing implementation, read:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. `docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md` when choosing validation scope.

The Productive Uncertainty plan is relevant for later provider work but is not part of the current #63 implementation slice.

## 2. Live-state checkpoint

Last verified `main` before this docs-convergence sequence:

```text
6111ffe3863713895d2b21ab086cf31abcca4a4e
```

Last fully validated executable/code checkpoint on PR #63:

```text
b014345afe2b003bad2a2ee23cb2cf809a2a4eb2
```

Validation:

```text
CI #1373                      SUCCESS
R2 main-thread boundary #1288 SUCCESS
```

Later commits in the current sequence are documentation-only unless a new executable edit is explicitly introduced.

Documentation-authority convergence is complete: root `AGENTS.md`, roadmap, clue-UX decision, Productive Uncertainty plan, algorithm v2.2 authority metadata, and this active handoff now use one current authority hierarchy and one implementation route.

The 2026-09-01 archive convergence batch is also complete: 35 completed/superseded/deferred documents were moved out of the active `docs/` root into typed `archive/` subdirectories; the active/foundational root now contains 19 Markdown files. The archive move changed no executable source. Its one-shot exact-tree/move/diff audit succeeded and the temporary workflow/script self-cleaned.

Always re-query live GitHub state before implementation or merge.

## 3. What #63 already contains

### Pair structured manual projection

`PairInformationManualSelection`:

- consumes `PairInformationLegalCandidate` only;
- preserves every supplied legal candidate exactly once;
- groups role choices by shown role;
- exposes unordered seat-pair choices;
- normalizes reversed Storyteller seat selection;
- resolves to the exact original legal candidate;
- preserves exact interaction-scoped registration facts;
- exposes zero-result only when upstream legal domain supplies it.

### Night-step transport

`ClocktowerNightStepUi` and `ClocktowerInformationStepBuilder` can carry a nullable precomputed pair manual-selection model.

The Builder does not own:

- `GameState`;
- role definitions;
- legal-domain generation;
- truth classification;
- recommendation ranking;
- automation policy.

This is intentional.

## 4. Important architecture correction before production UI

Do not continue directly into the large Host UI from the current transport seam.

The project already has a stronger shared information-decision lifecycle:

```text
InformationDecisionContext
InformationDecisionSnapshot
ConfirmedInformationDecision
InformationDecisionSource.MANUAL
InformationDecisionSource.RECOMMENDATION_ACCEPTED
```

and `StructuredNumberInformationUiModel` already demonstrates the desired pattern:

```text
validated legal candidates
-> structured choices
-> choose candidateId
-> shared context.confirm(...)
-> exact confirmation + snapshot
```

Pair selection should adopt this Foundation rather than develop a parallel confirmation mechanism.

## 5. Current implementation target — UX-R2A

Add durable typed pair semantic scenario contracts.

### Washerwoman

Lock:

1. functioning/no Spy registration path -> only truthful Townsfolk clues;
2. functioning + Spy -> legal Spy-as-Townsfolk registration truths are selectable;
3. Drunk -> complete current-script Townsfolk × legal unordered pair display space;
4. Poisoned -> same complete legal display space;
5. no reliability state permits a zero-character Washerwoman result.

### Librarian

Lock:

1. functioning with zero actual Outsiders and no Spy -> exactly `No Outsiders`;
2. functioning with zero actual Outsiders + Spy -> `No Outsiders` plus legal Spy-as-Outsider registered truths may coexist;
3. functioning with actual Outsider(s) -> only truthful legal clues;
4. Drunk/Poisoned -> current-script Outsider × legal unordered pairs + `No Outsiders`.

### Investigator

Lock:

1. functioning -> actual Minion truth plus legal Recluse-as-Minion registration truth;
2. Drunk/Poisoned -> current-script Minion × legal unordered pairs;
3. never expose `No Minions`.

Use typed/domain tests. Do not create UI/source-string tests for these semantics.

## 6. Current implementation target — UX-R2B

Audit `InformationDecisionContext<T>` and its current generic dependence on `DynamicInformationOutcome`.

Goal:

```text
PairInformationLegalDomain
-> shared InformationDecisionContext-style authority
-> PairInformationManualSelectionModel
-> structured selection resolves candidateId
-> context.confirm(candidateId, MANUAL, currentRevision)
-> ConfirmedInformationDecision
```

Requirements:

- do not duplicate `PairInformationOutcome` as an artificial second pair type merely to satisfy an existing generic bound;
- narrowly generalize the Foundation if needed;
- preserve existing Number behavior and `StructuredNumberInformationUiModel` contracts;
- preserve candidate IDs and registration facts;
- manual confirmation outside the current legal domain must be impossible;
- stale revision must be rejected;
- recommendation acceptance must remain limited to current recommended candidate IDs.

## 7. Scope boundary for #63

Allowed:

- pair scenario tests;
- narrow generic/Foundation changes required for pair adoption;
- pair structured manual model adaptation;
- small presentation/session model changes directly required by the Foundation;
- documentation synchronization;
- focused/FAST/CI validation at the appropriate checkpoint.

Do not add:

- full production pair picker in `ClocktowerHostScreen.kt`;
- removal of global Automatic/Manual selector;
- Top-1/alternative presentation redesign;
- new legacy ranking heuristics;
- Productive Uncertainty ranking;
- PlayerWorldSet production recommender integration;
- A4/ZDD production rollout;
- broad future-script support;
- Host/App decomposition;
- unrelated persistence/recovery work.

## 8. Why production UI is a separate slice

`ClocktowerHostScreen.kt` is large and protected orchestration code.

The next PR after #63 should be a vertical production slice that only wires an already-stable decision model:

```text
existing complete game context owner
-> PairInformationLegalDomain
-> shared decision context
-> structured pair presentation model
-> Host/night-step transport
-> dedicated manual picker
-> exact confirm
-> durable visible observation
```

Legality must not be recomputed in Compose or Builder.

## 9. Later route after #63

```text
UX-R2C
pair production vertical slice

UX-R2D
manual-authority audit across Number / Yes-No / role-category-reveal families

UX-R3/R4
only then remove global mode and establish provider-neutral primary + 0–2 alternatives + manual shell

UX-R5
thin family-specific presentation polish

EPI-MQ
scenario corpus -> PlayerWorldSet BEFORE/AFTER -> gates -> persistence/breakability/interaction -> Productive Uncertainty

UX-R6
replace legacy provider behind the unchanged UI/decision contract
```

Critical rule: do not delete the global Manual front-door path until every currently supported major information family has an independent correct manual authority path.

## 10. Validation strategy

Follow risk-based evidence in `AGENTS.md` / `TESTING_STRATEGY.md`.

For UX-R2A stable semantic contracts, meaningful focused RED/GREEN is appropriate where current tests do not already protect the behavior.

For generic Foundation widening, existing Number tests count as pre-existing evidence. Add new tests only for stable pair adoption/confirmation behavior not already protected.

At the logical #63 checkpoint:

- focused pair/domain/Foundation tests GREEN;
- existing structured Number contracts GREEN;
- `:app:testFast` / GitHub CI as required by current cadence;
- R2 boundary GREEN;
- `git diff --check` / exact changed-file and semantic diff audit;
- PR body synchronized with final scope.

## 11. Stop condition

Stop #63 when:

1. pair semantic scenarios are explicitly protected;
2. pair manual/recommendation decisions can share the existing decision confirmation lifecycle;
3. existing Number behavior is preserved;
4. no full Host/UI production cutover has been added;
5. current docs and PR body match the implemented scope;
6. checkpoint validation is green.

Then leave PR #63 Draft and request explicit merge authorization.
