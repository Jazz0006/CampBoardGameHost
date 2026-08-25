# CampBoardGameHost — Information Decision Correctness Bug Handoff

> Date: 2026-08-25 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Current stable code baseline at diagnosis: `5367603d2d7150e7ba88f19d061eb04f8da20aeb` (PR #51, App-root through S9.1)
> Priority: **CURRENT / correctness before further structural decomposition**
> Recommended implementation branch: `codex/clocktower-information-decision-correctness-hotfix`
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
> Specialized design authority: `docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`

## 1. Confirmed defect

The Clocktower automatic-information path has an authority gap between **recommendation selection / display preparation** and **durable epistemic observation commit**.

The intended architecture already says:

```text
validated legal information candidates
-> InformationDecisionContext
-> MANUAL or RECOMMENDATION_ACCEPTED confirmation
-> ConfirmedInformationDecision
-> EpistemicObservationDraft
-> display / session durable observation commit
```

The important correctness rule is that an automatic recommendation is **not** itself a game fact. It must be accepted/confirmed against the same immutable decision context and current revision before any observation draft becomes durable.

Current production wiring does not strongly preserve that proof at the final commit boundary. In `ClocktowerHostScreen.kt`, `recordReliablePrivateInformation(displayStep)` currently treats a non-null `displayStep.informationDecisionDraft` as sufficient authority and immediately calls `onRecordEpistemicObservation(...)`.

Conceptually the current weak seam is:

```text
ClocktowerNightStepUi.informationDecisionDraft
-> recordReliablePrivateInformation(...)
-> onRecordEpistemicObservation(draft)
```

A bare draft no longer proves, at durable-commit time, that:

- it came from `InformationDecisionContext.confirm(...)`;
- it was confirmed as `RECOMMENDATION_ACCEPTED` or `MANUAL`;
- the candidate still belongs to the legal candidate snapshot that produced it;
- the candidate was actually in `recommendedCandidateIds` when source is recommendation acceptance;
- the decision context revision still matches current game/player-input revision;
- the draft belongs to the same immutable semantic decision snapshot rather than a recomputed/recomposed successor context.

This is a correctness bug because durable observation history is later consumed by GLOBAL epistemic/history replay. Once a wrong/stale information observation is committed, downstream reasoning can be internally consistent but based on a fact the Storyteller never validly confirmed.

## 2. Existing architecture already defines the correct behavior

`InformationDecisionFoundation.kt` already provides the semantic authority:

```text
InformationDecisionSource
  MANUAL
  RECOMMENDATION_ACCEPTED
```

Do **not** add an `AUTO` source merely to patch the bug. Automatic mode should still semantically accept a recommendation through `RECOMMENDATION_ACCEPTED`.

`InformationDecisionContext.validate(...)` already hard-blocks:

```text
STALE_CONTEXT
ILLEGAL_CANDIDATE
NOT_RECOMMENDED
```

`InformationDecisionContext.confirm(...)` is the boundary that returns a `ConfirmedInformationDecision` and its `EpistemicObservationDraft` only when validation is allowed.

`StructuredNumberInformationUiModel` already follows this model correctly: manual choice delegates to `chooseManually(...)`, recommendation acceptance delegates to `acceptRecommendation(...)`, and both call the same context confirmation authority.

`FirstNightInformationMigration` also documents the same lifecycle principle: generated candidates are not game facts and display is the commit boundary for the selected observation.

The hotfix should therefore **close the production wiring gap**, not invent a second decision system.

## 3. Primary failure class to protect

The highest-risk sequence is:

```text
context A built at revision R
-> automatic recommendation candidate C selected from A
-> game state or player input changes / Compose rebuilds context B
-> old C or old draft survives in a display/wiring object
-> display/record path commits it without a fresh confirmation against current revision/context
```

Candidate IDs alone are not enough authority. A candidate ID is meaningful only inside the immutable decision snapshot that generated it.

The correction must make stale-snapshot acceptance impossible even when:

- the same textual candidate ID exists in both old and new contexts;
- automatic mode recomposes/reselects;
- recommendation metadata changes;
- registration/impairment inputs change;
- game-state revision or player-input revision changes between recommendation generation and display.

The implementation does not have to use a specific new type, but durable wiring must retain enough immutable provenance to establish the confirmation's context/revision identity.

## 4. Required correctness invariants

### 4.1 One decision authority

Both sources remain peers:

```text
MANUAL
RECOMMENDATION_ACCEPTED
        ↓
InformationDecisionContext.confirm(...)
```

No direct automatic-selection-to-durable-draft bypass is allowed.

### 4.2 Confirmation happens against current revision

At semantic confirmation time the current:

```text
gameStateRevision
playerInputRevision
```

must match the revision captured by the decision context.

`STALE_CONTEXT` must produce no confirmed decision and no durable observation.

### 4.3 Recommendation membership is checked

An automatic/recommendation-accepted candidate must still be present in the context's `recommendedCandidateIds`.

`NOT_RECOMMENDED` must produce no durable observation.

### 4.4 Legal candidate membership is checked

A candidate not in the context's validated legal candidate set must never be committed, even if a UI object can syntactically represent the result.

### 4.5 Candidate snapshot identity is immutable

Do not reconstruct semantic authority from mutable UI state after selection. The selected candidate must remain tied to the immutable context/snapshot/revision that produced the confirmation.

### 4.6 Durable observation remains post-confirmation only

`EpistemicObservationDraft` is an output of a valid confirmation. Recommendation generation, ranking, preview and candidate display are not durable facts.

### 4.7 Existing manual semantics remain unchanged

The hotfix must not weaken the existing structured manual path or remove its warnings. A legal manual alternative may still differ from recommendation and carry the existing soft warning.

## 5. Recommended boundary correction

Preferred direction:

```text
InformationDecisionContext
-> confirm(candidateId, source, currentRevision)
-> provenance-bearing confirmed decision
-> display
-> durable observation commit
```

Avoid passing a naked `EpistemicObservationDraft` through the information-decision production path when that loses the proof of which context/revision confirmed it.

One reasonable implementation shape is to let the display step carry a `ConfirmedInformationDecision` or another narrow immutable confirmation envelope rather than only `informationDecisionDraft`. The exact type should be chosen after RED tests establish the required contract.

Do not put new legality/revision rules into `ClocktowerHostScreen.kt`. Host should wire the established authority; it should not become a second semantic validator.

## 6. Tests-first RED plan

Start with tests only. Do not edit production in the first step.

### RED A — stale automatic recommendation cannot become a durable draft

Build context A at revision, for example:

```text
gameStateRevision = 7
playerInputRevision = 11
recommended candidate = C
```

Then attempt recommendation acceptance with current revision `7/12` or `8/11`.

Require:

```text
validation = STALE_CONTEXT
confirmed = null
no observation is publishable
```

This pure semantic behavior already exists; the production-wiring RED must prove the automatic path cannot bypass it.

### RED B — old confirmation cannot authorize a successor context

Create two immutable decision contexts where the same textual candidate ID is present but the revision/semantic snapshot differs.

Prove that confirmation/draft from context A cannot be durably committed as though it belonged to context B/current revision.

This is the key snapshot-identity characterization.

### RED C — automatic source must still pass recommendation membership

For a legal candidate that is not in `recommendedCandidateIds`, require:

```text
source = RECOMMENDATION_ACCEPTED
-> NOT_RECOMMENDED
-> no durable observation
```

Do not introduce a separate automatic bypass source.

### RED D — production authority wiring

Add a narrow production-wiring/ownership test proving that the automatic information path cannot call the durable observation callback solely because a bare `informationDecisionDraft` is present.

The real invariant should be:

```text
automatic recommendation
-> InformationDecisionContext confirmation
-> current-revision validation
-> confirmed decision
-> durable observation
```

Avoid a brittle assertion that freezes an incidental function location if the same invariant can be tested through a better owner boundary.

### RED E — preserve manual/recommendation parity

Keep the existing contract:

```text
same legal candidate + same context
MANUAL confirmation draft == RECOMMENDATION_ACCEPTED confirmation draft
```

The fix must not fork two semantic implementations.

### RED F — first-night candidate lifecycle

Preserve the existing rule that generated/recommended first-night candidates are not facts. Only the selected, validly confirmed/displayed observation becomes durable.

## 7. Likely affected files

Expected production area is narrow and correctness-focused:

```text
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/session/InformationDecisionFoundation.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/session/StructuredNumberInformationUiModel.kt
```

Not every file above must change. Prefer the smallest authority-correct diff.

Corresponding tests may include:

```text
InformationDecisionFoundationTest
StructuredNumberInformationUiModel tests
new automatic information decision production-wiring characterization
ClocktowerGlobalObservationProductionWiringTest where relevant
first-night information lifecycle/migration tests where relevant
```

## 8. Forbidden scope expansion

Do not combine this hotfix with:

- S9.2 persistence extraction;
- App-root byte-reduction work;
- generic `ClocktowerHostScreen.kt` decomposition;
- A4 cache/prewarm/rebuild work;
- A3/B4 historical-exact expansion;
- new misinformation probability tuning;
- unrelated registration redesign;
- new scripts;
- history UI changes;
- JSON/schema/persistence migration changes.

Do not change official-rule semantics that are not necessary for this authority bug.

## 9. Validation ladder

Follow `docs/TESTING_STRATEGY.md`.

Expected sequence:

```text
T0 RED:
  new automatic information-decision authority tests
  + InformationDecisionFoundationTest

GREEN / T0:
  same focused set

T1:
  :app:testFast

T2:
  affected Clocktower information / recommendation / observation wiring tests
  + :app:assembleDebug where required

T3:
  run only if the implemented change crosses a semantic trigger defined by TESTING_STRATEGY

PR T4:
  applicable full Android gate
```

If the change affects GLOBAL observation publication ordering, include the relevant `ClocktowerGlobalObservationProductionWiringTest` coverage before merge.

## 10. Exact-diff acceptance criteria

Accept only if all are true:

- automatic information decisions cannot bypass `InformationDecisionContext` confirmation;
- stale context cannot create/display a durable observation through the automatic path;
- recommendation membership and legal-candidate membership remain enforced;
- candidate/confirmation provenance remains bound to an immutable decision snapshot;
- manual path semantics remain unchanged;
- no new `InformationDecisionSource.AUTO` is introduced;
- no unrelated Host transaction ordering changes;
- no persistence/schema changes;
- no size-driven decomposition is mixed in;
- focused tests, T1 and affected T2 are GREEN;
- exact diff contains only the approved correctness boundary and tests.

## 11. Hard STOP conditions

Stop and re-audit before widening scope if the fix appears to require:

- rewriting the recommendation engine;
- changing GLOBAL timeline identity/sequence authority;
- moving large Host transaction blocks;
- changing persistence representation;
- touching A4/A3/B4 authority;
- introducing a second information-legality implementation outside `InformationDecisionContext`;
- accepting mutable UI state as substitute for immutable decision provenance.

## 12. Resume protocol

Before implementation in a new conversation:

1. read root `AGENTS.md`;
2. read `docs/README.md`;
3. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. read this handoff;
5. read `docs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`;
6. read `docs/TESTING_STRATEGY.md`;
7. query live `main` and record the actual current SHA;
8. create a dedicated hotfix branch from live `main`;
9. re-audit the current information-decision call chain before writing RED;
10. implement tests-first; do not touch S9.2 until this bug is merged.
