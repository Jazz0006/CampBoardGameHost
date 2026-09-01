# MS-S6C Generic Information Semantics Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6C COMPLETE / ACCEPTED**

## 1. Accepted checkpoint

Accepted code/test checkpoint:

`38a04c1353c883c3bda4b4a506085c3c1d2766bd`

This is an empty `[full-ci]` acceptance commit over the exact code tree from:

`70e7f41d1e30e5e701c02ceb95660572a99d27d4`

Exact compare `70e7f41... -> 38a04c1...` contains **zero changed files**. The accepted code tree is therefore exactly the focused-GREEN tree.

Live `main` at acceptance audit:

`eed51bade5163790316a31e8295e2e841df90357`

PR #61 remained Draft / open / unmerged at acceptance.

## 2. Acceptance evidence

Focused final GREEN:

```text
commit: 70e7f41d1e30e5e701c02ceb95660572a99d27d4
CI #1289 / run 33393595657
Android FAST: SUCCESS
CI gate: SUCCESS
R2 #1206 / run 33393595784: SUCCESS
```

Full acceptance checkpoint:

```text
commit: 38a04c1353c883c3bda4b4a506085c3c1d2766bd
CI #1290 / run 33393872108
Android :app:testFull + :app:assembleDebug: SUCCESS
ASP contract tests: SUCCESS
Real Clingo cross-validation: SUCCESS
CI gate: SUCCESS
R2 #1207 / run 33393872097: SUCCESS
```

S6C stabilization also passed a full validation checkpoint before the final registration slice:

```text
commit: ac01d256ba833e78a7af911583fde9aec631e0c5
CI #1285 / run 33391778684: SUCCESS
Android full + assembleDebug: SUCCESS
ASP contract: SUCCESS
Real Clingo: SUCCESS
CI gate: SUCCESS
R2 #1202: SUCCESS
```

## 3. S6C ownership result

The frozen causal order remains:

```text
Composition
-> Identity
-> Information
```

S6C now enforces the intended information ownership boundary:

- committed setup/shown identity is consumed as input and is not selected or rerolled by recommendation;
- recommendation owns information only;
- role-specific code owns legal ability/display semantics;
- reliability-family choice is generic across supported impaired information paths;
- Drunk and Poisoned use the shared impairment semantics where they share an information domain;
- the first-version impaired-family product bias is explicit at approximately 90% false / 10% truthful rather than the previous 97/3 default;
- `GENTLE / BALANCED / AGGRESSIVE` remain primarily within-family severity/ranking controls rather than independent truth-probability systems.

## 4. Legacy Drunk-Investigator ownership retirement

Active new recommendation generation no longer requires recommendation-owned Drunk identity/information decisions.

Important distinction:

- legacy `StorytellerDecision.DrunkInvestigatorInfo` and related schema may remain for compatibility/history decode;
- it is no longer a required active recommendation decision;
- `PlanLegalityValidator` accepts zero legacy Drunk-Investigator payloads on the active path and only permits a single compatibility value when the committed shown identity is Investigator;
- `TroubleBrewingSetupRecommendationLock` no longer converts committed setup identity into recommendation decisions;
- setup recommendation tests/simulation now observe committed identity and generic information output instead of asserting retired ownership.

This is migration, not destructive compatibility cleanup.

## 5. Generic information representation

S6C introduced/reused a generic pair-information seam instead of maintaining a setup-only Drunk-Investigator misinformation engine.

Key behavior:

```text
committed perceived ability
-> role-specific legal display / healthy truth space
-> RELIABLE / DRUNK / POISONED reliability
-> generic family policy
-> deterministic generic selection
-> AbilityObservation
```

Directly affected history/effect paths were updated to consume generic information semantics where required. Stale/provisional information behavior remains protected by behavior tests rather than source-shape assertions.

## 6. Investigator registration correctness — S6C-8

S6C-8 audited healthy Investigator truth semantics specifically for Recluse / Spy interactions.

The durable findings are:

1. An actual Minion such as Spy remains ordinary `TRUE_TO_ACTUAL_STATE` truth and does not require a synthetic registration record.
2. Recluse may supply Investigator truth via `TRUE_TO_REGISTERED_STATE` with an explicit `RegistrationFact` using:
   - `registeredType = MINION`
   - `registeredAlignment = EVIL`
   - `RegistrationQuestion.SPECIFIC_MINION`
   - `RegistrationReason.RECLUSE_ABILITY`
3. Recluse registration truth is not limited to the Minion actually in play. The healthy production recommender uses the current script role definitions, so Recluse may legally register as an out-of-play Minion on that script.
4. When a displayed clue is already true because it contains the actual Minion, actual-state truth is preferred and no unnecessary special registration is written.
5. When truth genuinely depends on Recluse registration, the selected `AbilityObservation` carries the corresponding `RegistrationDecision` so history/replay can distinguish registered-state truth from actual-state truth.

No broad registration subsystem rewrite was performed.

## 7. S6C-8 RED / GREEN evidence

First registration RED:

```text
96590e69aee1e7225a7640dba4c92cc867a5de7d
CI #1286 / run 33392562068
1008 FAST tests, exactly 1 failure:
healthy investigator may use recluse special registration as minion truth
```

First minimum GREEN:

```text
bce62910547c4825bc4ec2619e887b52931866b0
CI #1287 / run 33392833221
Android FAST: SUCCESS
CI gate: SUCCESS
```

Follow-up durable RED for the discovered partial gap:

```text
0caea78599b22f83da0869c8ed3054167d456844
CI #1288 / run 33393143470
1009 FAST tests, exactly 1 failure:
healthy investigator can truthfully use out of play minion through recluse registration
```

Final GREEN:

```text
70e7f41d1e30e5e701c02ceb95660572a99d27d4
CI #1289 / run 33393595657
Android FAST: SUCCESS
CI gate: SUCCESS
R2 #1206: SUCCESS
```

The final S6C-8 production diff from its second RED changed only:

```text
NaturalPairInformationCandidateGenerator.kt
PairInformationAbilityRecommender.kt
```

## 8. Whole-slice diff boundary

Compared with accepted S6B `d4cf3969aabcea7433b96b5b320171fbc821853e`, S6C touched the information/recommendation/history/compatibility surfaces required by the migration plus their tests and S6C planning docs.

It did **not** cut over TB setup production (S7), NGJ/no-template production (S8), redesign persistence/recovery, alter S5 actual-composition ownership, change S6A shown-role legality or S6B commitment namespace, or rewrite the complete registration subsystem.

## 9. Protected invariants at acceptance

Preserved:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe.
```

Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, living-Demon UI authority and current NGJ legality also remain outside this slice.

## 10. Next boundary

**STOP after this checkpoint documentation.**

The next implementation slice is **MS-S7 — Trouble Brewing controlled semantic cutover**.

S7 owns integration of the validated TB template flow through the accepted architecture:

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> S6C accepted information semantics
```

S7 also owns TB-specific Minion/style diversity adaptation and retirement of the legacy `selectedDrunkShownRole -> preset finalWeight` coupling.

Do not start S8, broad recovery work, App/Host decomposition, or merge/Ready PR #61 as part of this checkpoint.
