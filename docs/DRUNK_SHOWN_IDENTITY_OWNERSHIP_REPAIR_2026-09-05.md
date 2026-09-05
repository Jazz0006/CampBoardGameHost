# Drunk Shown-Identity Ownership Repair — 2026-09-05

> Status: **ACTIVE HOTFIX / OWNERSHIP CLEANUP**  
> Branch: `codex/drunk-shown-identity-ownership-cleanup`  
> Base checkpoint: `9cd72cba22737d1d803f8a30d25c2a5c25570211`  
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
- jointly evaluate that remaining information with Red Herring, Demon Bluffs and other mutable Storyteller choices where applicable.

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

The current Host setup-recommendation path still reconstructs the already committed Drunk shown identity as a `StorytellerDecision.DrunkShownRole` lock and preserves/reinserts it during reevaluation and clear-lock operations.

The recommendation service correctly rejects such a lock as `shown-identity-is-committed-setup-fact`. The result is a false lock conflict, and downstream manual Demon Bluff display may become pending/empty because no valid setup recommendation survives.

This is an incomplete authority migration, not a Demon Bluff algorithm defect.

## 3. Test strategy

Follow root `AGENTS.md`: behavior-first, typed seams, no new source-string wiring tests merely to force RED.

### 3.1 Existing coverage to preserve

`SetupShownIdentityCommitterTest` already protects durable setup behavior:

- same candidate/policy/setup seed -> same committed shown identity;
- selected shown identity is always legal under the setup policy;
- different seeds explore more than one legal option where multiple options exist;
- commitment is immutable with respect to input candidate/policy;
- illegal options fail closed.

Do not duplicate these assertions.

`SetupRecommendationShownIdentityOwnershipTest` already protects the downstream ownership contract:

- committed shown identity is consumed by recommendation;
- recommendation output does not contain `DrunkShownRole`;
- shown identity supplied as a recommendation lock is rejected;
- impaired information can be generated as observations from the committed shown role.

Strengthen this test only where a durable ownership gap remains.

### 3.2 New regression proof

The missing regression is the Host/recommendation-lock boundary:

```text
Given a legal committed setup with Drunk.shownRole
When recommendation lock state is initialized / replaced / cleared
Then shownRole is not synthesized into mutable locks
And clear returns an empty mutable lock set
```

Prefer a small typed lock-state owner if needed because the bug is caused by recommendation-lock semantics living inline in Compose state. Do not add a `ClocktowerHostScreen.kt` source-text assertion.

### 3.3 Legacy-test retirement

Audit old setup migration coverage for:

- `SetupCandidateGenerator.generateDrunkCandidates()`;
- `SetupClueOutcome.DrunkShownRole`;
- `StorytellerDecision.DrunkInvestigatorInfo` as a legacy setup recommendation path.

If a path has no production caller after the ownership cutover, remove the obsolete production path and its tests rather than preserving it only for test compatibility.

## 4. Implementation slices

### Slice A — visible regression and lock ownership

1. Add the smallest durable typed regression for mutable setup recommendation lock state.
2. Remove Host synthesis/preservation of committed shown identity from recommendation locks.
3. Initial mutable locks are empty unless the user has explicitly locked a mutable recommendation decision.
4. Reevaluation replaces mutable locks with exactly the supplied mutable lock set.
5. Clear-lock action clears mutable locks to empty.
6. Verify the false `InvalidLocks` path disappears for a legal Drunk setup.

### Slice B — history / recommendation ownership cleanup

Audit and remove downstream ownership remnants where they are no longer meaningful:

- Drunk shown-role history signature dimension;
- history cooldown/penalty based on repeated shown identity;
- recommendation tie-break/scoring branches that still treat `DrunkShownRole` as selectable;
- `drunkSuitability` metadata if it no longer serves any live setup/template responsibility.

Important: template history/de-duplication remains unchanged. Only independent shown-identity history influence is removed.

### Slice C — legacy Drunk recommendation path retirement

Audit production references for:

- `generateDrunkCandidates()`;
- `SetupClueOutcome.DrunkShownRole`;
- `StorytellerDecision.DrunkShownRole`;
- `StorytellerDecision.DrunkInvestigatorInfo`.

Rules:

- zero production callers -> retire production path + obsolete test;
- test-only caller -> normally retire;
- live production caller -> migrate only if it conflicts with the committed-shown-role observation pipeline, then remove legacy branch;
- do not delete a type blindly if a real compatibility boundary still uses it.

## 5. Validation cadence

```text
new typed regression
-> meaningful RED on current bug when executable
-> Slice A GREEN
-> focused setup shown-identity + recommendation ownership tests
-> Slice B cleanup + focused history/recommendation tests
-> Slice C legacy retirement + affected setup/recommendation tests
-> :app:testFast at logical checkpoint
-> assembleDebug
-> git diff --check / exact changed-file audit
-> GitHub CI checkpoint
```

Do not run the full suite after every cleanup micro-edit.

## 6. Acceptance criteria

The repair is complete only when all are true:

1. A selected template fully determines the legal Drunk shown-identity pool.
2. One shown identity is committed as part of setup and survives same-setup recovery/recomposition.
3. Recommendation reads, but cannot select/replace/lock, that shown identity.
4. Host/UI cannot synthesize committed shown identity into recommendation locks.
5. Clearing recommendation locks produces no hidden reinserted identity lock.
6. A legal setup containing Drunk can still produce setup recommendations and downstream Demon Bluff data under the existing bluff algorithm.
7. Independent Drunk shown-role history cooldown no longer affects recommendation scoring.
8. Fake information continues to be generated from the fixed shown identity and remains jointly evaluated with other mutable setup recommendation choices.
9. No Demon Bluff recommendation-quality redesign is included in this repair.

## 7. Explicitly deferred

After this ownership repair is stable, separately design the Demon Bluff recommendation algorithm. That future work may improve bluff strategic quality, role synergy, interactions, difficulty and history behavior, but must consume the already fixed committed setup established here.
