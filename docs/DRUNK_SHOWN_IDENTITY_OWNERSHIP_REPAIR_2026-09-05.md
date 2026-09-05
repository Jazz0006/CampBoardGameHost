# Drunk Shown-Identity Ownership Repair — 2026-09-05

> Status: **COMPLETE — FINAL CHECKPOINT PASSED**  
> Branch: `codex/drunk-shown-identity-ownership-cleanup`  
> Base checkpoint: `9cd72cba22737d1d803f8a30d25c2a5c25570211`  
> Validated code checkpoint: `a7c24fb5e7a0909bc77b84b9dcab5cf8b6e459b7`  
> Post-validation workflow cleanup head: `18d122fd1c3cab7461010ebb1b913ad1b2616198`  
> Scope: Trouble Brewing setup identity commitment, setup recommendation ownership, Host lock-state wiring, related history/legacy cleanup.  
> Explicit non-goal: Demon Bluff recommendation-quality redesign.

## 1. Product contract

The setup/recommendation authority boundary is now fixed as follows:

```text
setup template history / diversity selection
-> select one setup template
-> commit actual roles
-> if the selected template contains Drunk:
     choose one shown identity from that template's drunkAsOptions
     using setup-seed-backed deterministic randomness
-> committed setup is complete
   = actual role composition + Drunk shown identity
-> recommendation reads the committed GameState
-> recommendation generates remaining Storyteller information / decisions
```

### 1.1 Template and shown-identity semantics

- Template history/de-duplication applies to **template selection**.
- After a template is selected, Drunk shown identity is chosen from that template's allowed `drunkAsOptions`.
- Drunk shown identity itself has **no independent history/cooldown/de-duplication rule**.
- Repeating the same shown identity in consecutive games is legal.
- The setup seed may be used for deterministic randomness so reload/recovery of the same setup preserves the same committed shown identity.

### 1.2 Ownership invariants

`Setup` owns:

- selected template;
- actual role composition;
- template `drunkAsOptions`;
- one committed Drunk `shownRole` when applicable.

`GameState` owns the committed setup facts consumed by later systems.

`Recommendation` must:

- read `PlayerState.shownRole`;
- generate fake information appropriate to that already committed shown identity;
- use the complete committed GameState as the semantic/legal basis for later recommendation work.

`Recommendation` must **never**:

- select Drunk shown identity;
- replace Drunk shown identity;
- emit `StorytellerDecision.DrunkShownRole` as a production recommendation;
- accept or synthesize `DrunkShownRole` as a mutable recommendation lock;
- penalize a current recommendation because the committed shown identity repeats prior-game history.

`UI / Host` must:

- display the committed shown identity;
- pass only genuinely mutable recommendation decisions as recommendation locks;
- never convert `GameState.shownRole` back into a recommendation lock.

## 2. Confirmed regression

The pre-repair Host setup-recommendation path reconstructed the already committed Drunk shown identity as a `StorytellerDecision.DrunkShownRole` lock and preserved/reinserted it during reevaluation and clear-lock operations.

The recommendation service correctly rejected such a lock as `shown-identity-is-committed-setup-fact`. The result was a false lock conflict, and downstream manual Demon Bluff display could become pending/empty because no valid setup recommendation survived.

This was an incomplete authority migration, not a Demon Bluff algorithm defect.

## 3. Test strategy

Follow root `AGENTS.md`: behavior-first, typed seams, no new source-string wiring tests merely to force RED.

### 3.1 Existing coverage preserved

`SetupShownIdentityCommitterTest` protects durable setup behavior:

- same candidate/policy/setup seed -> same committed shown identity;
- selected shown identity is always legal under the setup policy;
- different seeds explore more than one legal option where multiple options exist;
- commitment is immutable with respect to input candidate/policy;
- illegal options fail closed.

`SetupRecommendationShownIdentityOwnershipTest` protects the downstream ownership contract:

- committed shown identity is consumed by recommendation;
- recommendation output does not contain `DrunkShownRole`;
- shown identity supplied as a recommendation lock is rejected;
- impaired information can be generated as observations from the committed shown role.

### 3.2 Added lock-boundary contract

A typed `SetupRecommendationLockPolicy` now owns mutable setup-recommendation lock semantics:

```text
initial locks = empty
replace = supplied mutable decisions, excluding committed shown identity
clear = empty
```

This gives durable typed coverage without a `ClocktowerHostScreen.kt` source-text test.

### 3.3 Added history regression

A behavior test now proves that two otherwise identical impaired-information observations with different committed perceived abilities produce the same recommendation-history signature.

The test was established against the pre-fix behavior and produced the intended RED before history production code changed.

## 4. Implementation status

### Slice A — visible regression and lock ownership — COMPLETE

Implemented:

1. Added `SetupRecommendationLockPolicy`.
2. Host recommendation locks now initialize empty.
3. Host reevaluation replaces locks through the mutable-lock policy.
4. Host clear-lock action clears to empty.
5. Removed `committedIdentityDecisions` and `preservingCommittedIdentity()` from Host lock ownership.
6. The large Host edit was applied through the repository-standard fail-closed one-shot workflow with exact branch/blob/anchor locks.
7. Focused ownership tests passed before and after the Host patch.

### Slice B — history / recommendation ownership cleanup — COMPLETE

Implemented:

1. Newly generated `HistoricalClueSignature` no longer records Drunk committed shown identity.
2. `HistoricalClueSignature.canonical()` excludes the legacy `drunkShownRole` field, so it cannot alter `CrossGameHistory.digest()` or recommendation selection seed.
3. `HistoryCooldown` no longer applies any Drunk shown-role repetition penalty.
4. The legacy `drunkShownRole` data field is temporarily retained as an inert compatibility field only; new signatures leave it null and neither digest nor cooldown consumes it.
5. Template history/de-duplication remains unchanged and still acts upstream during setup-template selection.

Evidence:

- meaningful history RED established on the pre-fix checkpoint;
- focused `HistoryCooldownTest`, `SetupRecommendationLockPolicyTest`, and `SetupRecommendationShownIdentityOwnershipTest` GREEN after the fix.

A temporary audit command incorrectly included Markdown files in `git diff --check`; Markdown hard-break trailing spaces caused that audit step to fail after the tests had already passed. The final checkpoint correctly limited diff-check to code/test files.

### Slice C — legacy Drunk recommendation path retirement — COMPLETE WITH EXPLICIT COMPATIBILITY BOUNDARY

Retired:

- `SetupCandidateGenerator.generateDrunkCandidates()`;
- all tests whose only purpose was to keep that historical generator alive;
- the private option/pair helpers used only by that historical generator.

Intentionally retained for now:

- `StorytellerDecision.DrunkShownRole` as an explicit legacy/invalid-input type so service/validator boundaries can reject it deterministically;
- `StorytellerDecision.DrunkInvestigatorInfo` compatibility lock behavior, because current legality and constrained-recommendation paths still exercise it;
- associated legacy evaluator branches that remain required by that compatibility path.

Do not delete these retained types merely to eliminate old names; retire them only when the compatibility lock path itself is deliberately replaced.

### 4.4 Final checkpoint — PASS

The final validation workflow passed all required gates on code checkpoint `a7c24fb5e7a0909bc77b84b9dcab5cf8b6e459b7`:

- focused ownership/migration suite, including `SetupCandidateGeneratorTest`;
- `:app:testFast`;
- `:app:assembleDebug`;
- code/test-only `git diff --check`;
- semantic ownership audit confirming no Host committed-identity lock synthesis remains;
- history audit confirming shown identity no longer participates in recommendation history digest/cooldown;
- audit confirming `generateDrunkCandidates` has no remaining production/test references;
- remote branch-head reconfirmation.

The temporary final-checkpoint workflow then deleted itself successfully, producing cleanup head `18d122fd1c3cab7461010ebb1b913ad1b2616198`.

## 5. Additional audit finding — information-quality parity is a separate follow-up

The ownership migration is now conceptually clear, but the audit found a distinct recommendation-quality issue that should not be hidden inside this hotfix:

```text
committed Drunk shownRole
-> PairInformationAbilityRecommender
-> one generic impaired AbilityObservation
```

The active generic path correctly reads the committed shown identity and complete `GameState`, but the selected impaired observation is currently produced inside `SetupEvaluator` rather than enumerated as part of the aggregate `CandidatePlan` Cartesian product.

Consequently, the old Investigator-specific path's explicit cross-choice scoring — for example Red Herring overlap and several pair-specific interaction scores — is not automatically equivalent to the new generic observation path.

This is **not** a shown-identity ownership defect and does not justify restoring recommendation authority over `shownRole`. It is a recommendation-quality migration/parity question.

Before claiming full equivalence with the historical joint optimizer, a follow-up should decide how generic fixed-identity information candidates participate in aggregate scoring with Red Herring, Demon Bluffs and future mutable setup choices. This can be discussed immediately before, or together with, the planned Demon Bluff recommendation-algorithm redesign.

## 6. Final validation cadence

Completed logical checkpoint:

```text
focused setup/history/ownership tests
-> :app:testFast
-> :app:assembleDebug
-> code/test-only git diff --check
-> semantic ownership audit
-> verify temporary one-shot/proof files are absent
-> PASS
```

A normal PR/CI gate is still required before merge if this branch is opened as a PR.

## 7. Acceptance criteria — COMPLETE

All ownership-hotfix acceptance criteria are satisfied:

1. A selected template fully determines the legal Drunk shown-identity pool.
2. One shown identity is committed as part of setup and survives same-setup recovery/recomposition.
3. Recommendation reads, but cannot select/replace/lock, that shown identity.
4. Host/UI cannot synthesize committed shown identity into recommendation locks.
5. Clearing recommendation locks produces no hidden reinserted identity lock.
6. A legal setup containing Drunk can still produce setup recommendations and downstream Demon Bluff data under the existing bluff algorithm.
7. Independent Drunk shown-role history/cooldown no longer affects recommendation weighting or stable-selection history digest.
8. Fake information continues to be generated from the fixed shown identity and committed GameState.
9. The separate cross-choice information-quality parity gap is documented rather than incorrectly represented as solved by this ownership hotfix.
10. No Demon Bluff recommendation-quality redesign is included in this repair.

## 8. Explicitly deferred

After this ownership repair:

1. decide the generic impaired-information / aggregate-plan interaction-quality model described in Section 5;
2. separately design the Demon Bluff recommendation algorithm.

Both future tasks must consume the already fixed committed setup established here and must not move Drunk shown-identity authority back into recommendation.
