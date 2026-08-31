# MS-S4 Generated Setup Candidate Source — Accepted Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S4 COMPLETE / ACCEPTED — STOP BEFORE MS-S5**

## 1. Live state re-confirmed before MS-S4

```text
main:
eed51bade5163790316a31e8295e2e841df90357

branch / Draft PR #61 head before MS-S4:
e33aa8fcad84fd0d5f99e7ee3843527a1296c520

PR #61:
OPEN / DRAFT
base = main
```

`e33aa8f...` was a docs-only carrier. The previously accepted MS-S3 production code/test checkpoint remained:

`6b15822e75680fb8e718f5db24358e1a935b5523`

## 2. MS-S4 accepted code/test checkpoint

```text
RED typed contract commit:
26aa5946d265be7a97d7fb9f82d4a79094516b0b

RED contract expansion for all supported distributions:
758f27b0fd6ba28e35eb136652f1b3e5345687fc

MS-S4 production GREEN code/test checkpoint:
6de0e8c99c89a091615c513255adbdb773b3cc69
```

The test commits preceded the production source. At the initial RED commit, `GeneratedSetupCandidateSource` did not exist; the typed contract therefore described an unimplemented source boundary before production code was added.

## 3. Validation at accepted production checkpoint

```text
CI #1236 / run 33359464789
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router

R2 #1153 / run 33359464788   SUCCESS
```

Focused typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSourceTest.kt`

The connector-only implementation environment did not run a second redundant local Gradle invocation; the focused contract was executed by the risk-routed Android `:app:testFast` job above. This is consistent with the project workflow rule that CI is the independent checkpoint authority.

## 4. Legacy NGJ audit result

The legacy `generateClocktowerAssignments(...)` path currently mixes three responsibilities:

```text
actual-role composition generation
+ shuffled final role order / seat-facing assignment order
+ Drunk shown-role selection
```

MS-S4 extracted only the first responsibility conceptually; the legacy function itself was not modified or cut over.

Legacy composition evidence audited:

- `clocktowerDistribution(playerCount)` supplies the current 5–15 base Townsfolk/Outsider/Minion/Demon distribution;
- Minions are selected before Baron adjustment;
- if Baron is selected, legacy code applies `+2 Outsiders`, capped by the number of Outsiders available in the script;
- Townsfolk count is reduced by the actual Outsider delta after that cap;
- legacy role selection uses unseeded `.shuffled()` paths;
- legacy Drunk shown identity additionally uses random selection and remains outside MS-S4.

Existing `NoGreaterJoySetupRegressionTest` was left unchanged and continues to preserve the established NGJ role pool plus 5/6-player base distributions/startability.

## 5. Accepted production boundary

New production file:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt`

Accepted input/output:

```text
SetupCandidateRequest(script, playerCount, setupSeed)
+ injected ValidatedClocktowerRuleset
+ stable providerId
-> one deterministic legal pre-seat SetupCandidate
```

The source reuses the accepted MS-S2 contracts:

```text
SetupCandidate
SetupCandidateRequest
SetupCandidateSource
ClocktowerSetupProvider
```

It does not implement template-vs-generated orchestration and does not change MS-S3 `TemplateRepository` semantics. An empty template bucket remains a normal repository result; policy/orchestration remains future ownership.

## 6. Deterministic seed contract

MS-S4 does not call Kotlin's unseeded `.random()` or `.shuffled()`.

Each selectable role receives a deterministic rank derived from:

```text
setupSeed
+ script id
+ playerCount
+ team namespace
+ role id
```

The implementation uses fixed FNV-style text accumulation followed by a fixed 64-bit mixing function. Role ID is a deterministic tie-breaker.

Consequences:

- identical request + ruleset produces identical candidate output;
- seed participates directly in the ordering;
- different seeds can produce different legal compositions when the script has choice space;
- no process-global random state is consumed.

## 7. Distribution and setup-modifier legality

MS-S4 carries the current Clocktower base distribution for player counts 5–15 in a private pure setup helper.

Generation order is:

```text
base distribution
-> deterministic Minion selection
-> detect selected Baron by stable external id `baron`
-> apply Baron +2 Outsiders once
-> cap to available script Outsiders
-> reduce Townsfolk by the actual Outsider delta
-> deterministically select Townsfolk / Outsiders / Demon
-> validate exact requested player count
-> canonical SetupCandidate actual-role multiset
```

Selected setup-modifier roles other than the currently supported Baron semantic fail explicitly rather than being silently ignored.

This preserves the current NGJ small-game legality intent:

```text
5 players + Baron + two available Outsiders:
1 Townsfolk + 2 Outsiders + 1 Minion + 1 Demon

6 players + Baron + two available Outsiders:
2 Townsfolk + 2 Outsiders + 1 Minion + 1 Demon
```

The typed test also uses a four-Outsider ruleset at six players to prove Baron changes `1 -> 3 Outsiders`, rather than applying its `+2` modifier repeatedly.

## 8. Typed evidence accepted

`GeneratedSetupCandidateSourceTest` proves:

1. same request/seed produces structurally equal candidates;
2. script, derived player count and generated provenance are correct;
3. provider identity is stable and compatible with `ClocktowerSetupProvider`;
4. generated role IDs all belong to the injected validated ruleset;
5. all current supported player counts 5–15 match the base Clocktower distribution when no setup modifier is selected;
6. multiple seeds explore more than one legal composition where choices exist;
7. Baron applies one `+2 Outsider` adjustment;
8. Baron adjustment caps to available Outsiders for NGJ-shaped 5/6-player rulesets.

MS-S2's accepted `SetupCandidate` contract remains the typed authority that candidates are canonical **pre-seat actual-role multisets** with no seat or shown-role fields.

## 9. Dependency / authority audit

The new source imports only:

- Clocktower catalog metadata types;
- `SetupCandidateSource` / request/candidate setup contracts;
- generic setup provenance types.

It does **not** read or depend on:

- Android `Context`;
- UI/Compose state;
- persistence or recovery state;
- diversity/history state;
- `TemplateRepository` fallback policy;
- seat assignment;
- shown identities / Drunk disguise;
- App or Host production wiring.

`BuiltInClocktowerRulesetCatalog.fromContext(...)` remains an outer production catalog concern. MS-S4 receives an already validated `ValidatedClocktowerRuleset` directly, so the generator itself stays pure Kotlin and Android-independent.

## 10. Exact diff audit

Compared with the pre-MS-S4 docs carrier `e33aa8fcad84fd0d5f99e7ee3843527a1296c520`, the accepted production checkpoint `6de0e8c99c89a091615c513255adbdb773b3cc69` is ahead by three commits and changes exactly two files:

```text
ADDED  app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt
ADDED  app/src/test/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSourceTest.kt
```

No existing source/test file was modified. In particular there are no changes to:

- `CampBoardGameHostApp.kt` / NGJ production flow;
- TB preset production flow or 480-preset dataset;
- `ClocktowerHostScreen.kt`;
- persistence/recovery;
- TemplateRepository;
- Mayor / Imp / Monk / Attack-Protect / A3 / A4 / ZDD surfaces.

## 11. Explicitly not implemented in MS-S4

Still future:

```text
MS-S5  diversity/history scoring and selector
MS-S6  generic shown-identity commitment / Drunk shown role
MS-S7  TB production adaptation
MS-S8  NGJ production cutover
MS-S9  future no-template script acceptance
```

Also untouched: REC-R1 and all unrelated rules-engine campaigns.

## 12. Resume point

MS-S4 is accepted and development stops here for this slice.

Next campaign slice is **MS-S5**, but it must not begin automatically from this checkpoint.

Before future work, re-query live `main`, branch, Draft PR #61 and current checks. Keep PR #61 Draft; do not merge, mark Ready, rebase or force-push without explicit authorization.
