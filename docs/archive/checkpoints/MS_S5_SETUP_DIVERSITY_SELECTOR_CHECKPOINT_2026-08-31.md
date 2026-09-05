# MS-S5 — Generic Actual-Composition Diversity Selector Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **COMPLETE / ACCEPTED — MS-S6A NEXT**

## 1. Live state and predecessor boundary

Before MS-S5 production work:

```text
campaign baseline main:
eed51bade5163790316a31e8295e2e841df90357

Draft PR #61:
OPEN / DRAFT / NOT MERGED
base = main

pre-S5 docs carrier head:
165728aad3a4fece28ecb9380a8a50e0a9b2e7e8

accepted predecessor production checkpoint:
MS-S4 = 6de0e8c99c89a091615c513255adbdb773b3cc69
```

MS-S4.5 had already frozen the corrected causal architecture:

```text
Composition
-> Identity
-> Information
```

Therefore MS-S5 was deliberately restricted to actual-role composition diversity and could not consume shown-identity metadata/history.

## 2. Accepted MS-S5 commits

Typed RED contract:

`d0145d2347490aa4b7b1f037188e1204cfac3832` — `test(ms-s5): define setup diversity selector contract`

Production GREEN commits:

```text
ff78693767c47bf9d19bd44848eb52d98bfde126
feat(ms-s5): add setup diversity history contract

86c3ce651025de9ccbe1094b161becc171514e69
feat(ms-s5): add deterministic setup diversity selector
```

Accepted MS-S5 code/test checkpoint:

`86c3ce651025de9ccbe1094b161becc171514e69`

Later documentation commits are carrier commits only and do not replace this validated code/test checkpoint.

## 3. Genuine RED evidence

At RED head `d0145d2347490aa4b7b1f037188e1204cfac3832`:

```text
R2 #1160 / run 33362653634   SUCCESS
CI #1243 / run 33362653627   FAILED as expected
```

The Android FAST job failed during `:app:compileDebugUnitTestKotlin` because the new stable S5 contracts did not yet exist. The log explicitly reported unresolved references for:

- `SetupDiversityRecord`;
- `SetupDiversityHistory`;
- `SetupDiversityScorer`;
- `SetupDiversitySelector`;
- `SetupDiversityPolicy`;
- `SetupExactRepeatPolicy`.

This was the intended RED: the typed behavior contract preceded production implementation, and no unrelated predecessor test failure was involved.

## 4. GREEN validation

At accepted production checkpoint `86c3ce651025de9ccbe1094b161becc171514e69`:

```text
CI #1245 / run 33362804682   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1162 / run 33362804691   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

This slice is isolated pure-Kotlin setup-domain work with no production-flow cutover. The risk-routed FAST suite plus the new typed contract and R2 gate are the accepted checkpoint evidence; no redundant local Gradle run was claimed or required.

## 5. Exact MS-S5 diff

Compared with the pre-S5 docs carrier `165728aad3a4fece28ecb9380a8a50e0a9b2e7e8`, accepted S5 checkpoint `86c3ce651025de9ccbe1094b161becc171514e69` adds exactly three files:

```text
ADDED app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversityHistory.kt
ADDED app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelector.kt
ADDED app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelectorTest.kt
```

No existing production source was modified.

In particular MS-S5 did not edit:

- `TroubleBrewingSetupPresetSelector.kt`;
- `TroubleBrewingSetupPresetRotationScorer.kt`;
- TB preset data/validator/deal planner;
- NGJ legacy setup flow;
- `CampBoardGameHostApp.kt`;
- `ClocktowerHostScreen.kt`;
- persistence/recovery;
- recommendation code;
- shown-identity code.

## 6. Generic history contract

New type:

```text
SetupDiversityRecord
├─ script: ScriptId
├─ canonical actualRoles: List<RoleId>
└─ derived playerCount

SetupDiversityHistory
└─ recentSetups: newest-first immutable snapshot
```

The history intentionally contains only actual-role composition facts required by generic setup diversity.

It does not contain:

- shown identity;
- `drunkAsOptions`;
- selected Drunk shown role;
- recommendation decisions;
- clue information;
- TB dataset/style metadata;
- seat assignment;
- persistence schema.

History scoring filters records by the candidate's exact `script + playerCount` before applying age weights. Cross-script or cross-count history cannot influence the candidate score.

## 7. Invariant-role exclusion

A key generic design result of MS-S5 is that the scorer does not need script-specific character-type knowledge merely to reproduce composition novelty correctly.

For each candidate pool, the scorer computes the role multiset intersection that is invariant across **every** candidate. Those invariant occurrences are removed before overlap is measured.

Conceptually:

```text
candidate pool:
A = Imp + role1 + role2 + role3
B = Imp + role4 + role5 + role6

invariant multiset:
Imp

novelty comparison:
compare only the varying role occurrences
```

This has two important properties:

1. fixed roles such as TB's Imp do not artificially inflate repetition scores;
2. generic MS-S5 does not need to know which role is a Demon, Minion, Townsfolk or Outsider.

The implementation is multiset-aware, so repeated role IDs remain well-defined if a future ruleset permits them.

## 8. Fixed-point deterministic scoring

MS-S5 uses integer fixed-point arithmetic:

```text
FIXED_POINT_SCALE = 1_000_000
```

`SetupDiversityScore` contains:

```text
weightedOverlapFixedPoint
noveltyWeightFixedPoint
```

Recent matching setup records are weighted by `SetupDiversityPolicy.historyWeights` after script/player-count filtering. Novelty is:

```text
max(minimumNoveltyWeight,
    FIXED_POINT_SCALE - weightedOverlap)
```

The accepted default history decay corresponds to the established TB age shape without importing TB models:

```text
100, 65, 40, 20, 10
```

The generic scorer does not use floating-point selection weights.

## 9. Exact-repeat and last-game overlap policy

`SetupDiversityPolicy` owns generic composition-selection policy only.

Accepted exact-repeat modes:

```text
ALLOW
REJECT_WHEN_ALTERNATIVE
REJECT
```

`REJECT_WHEN_ALTERNATIVE` is the generic default: reject the immediately previous exact composition when another composition exists, but allow the sole available composition instead of manufacturing a fallback candidate.

`REJECT` fails explicitly if strict exact-repeat removal leaves no candidate.

The optional last-game overlap filter uses fixed-point thresholds and an explicit fallback increment. The accepted default fallback step is:

```text
50_000 = 0.05
```

Selection evaluates the initial threshold, then increases it by the fallback step until the **first non-empty eligibility level** is reached. It does not skip ahead to a broader pool when a narrower legal level already exists.

The generic default max-overlap threshold is `1_000_000`, so script-specific thresholds are supplied later by an adapter/policy owner rather than hard-coded into the generic core.

## 10. Deterministic weighted selection

After eligibility filtering:

1. candidates are canonicalized by a stable key containing source kind, provider ID, optional candidate ID and canonical actual-role composition;
2. duplicate stable candidate identities in one selection pool are rejected;
3. each eligible candidate receives its generic novelty weight;
4. the draw seed is derived with existing `MurmurHash3.low64Utf8` from:

```text
setup-diversity-v1
+ script
+ playerCount
+ selectionSeed
```

5. unsigned modular selection chooses one weighted candidate.

Consequences:

- identical candidates/history/seed produce the same result;
- caller candidate ordering cannot change the result;
- different seeds can explore multiple equally eligible candidates;
- a single candidate is returned directly;
- generated candidates do not require a template `candidateId` merely to pass through S5.

## 11. Typed contract coverage

`SetupDiversitySelectorTest` proves:

1. history snapshots/canonicalizes actual-role compositions;
2. invariant roles common to every candidate are excluded from overlap;
3. cross-script and cross-player-count history is ignored;
4. exact-repeat rejection selects an alternative when one exists;
5. strict exact-repeat rejection fails closed when nothing remains;
6. last-game overlap threshold uses the first non-empty eligibility level;
7. selection is deterministic and input-order independent;
8. multiple seeds explore more than one equally novel candidate;
9. one generated candidate with no template candidate ID selects successfully;
10. mixed-script candidate pools fail explicitly.

These are typed domain contracts, not source-string implementation-shape assertions.

## 12. What MS-S5 deliberately did not genericize

The legacy TB scorer also contains script-specific soft penalties for:

- same Minion set;
- repeated primary style tag;
- repeated Drunk shown role.

MS-S5 does **not** copy these into generic history/scoring.

Ownership after S4.5/S5 is:

```text
generic S5:
actual-role composition overlap
+ exact-repeat policy
+ last-game overlap eligibility/fallback
+ generic age-weighted novelty
+ deterministic weighted selection

TB S7 adapter:
TB-specific Minion-set diversity
+ TB-specific style diversity

retired semantic:
Drunk shown-role repetition must NOT alter actual-composition weight
```

This avoids forcing TB metadata fields onto future scripts and preserves the S4.5 rule that shown identity cannot feed back into composition selection.

## 13. Protected boundary after MS-S5

The setup pipeline now has implemented generic owners for:

```text
candidate legality/source      MS-S2/S3/S4
actual-composition selection   MS-S5
```

Still future:

```text
shown-identity options/policy  MS-S6A
shown-identity commitment      MS-S6B
recommendation inversion       MS-S6C
TB production cutover          MS-S7
NGJ production cutover         MS-S8
future-script acceptance       MS-S9
```

MS-S5 must remain unable to consume shown identity even after later stages exist.

## 14. Next slice — MS-S6A

MS-S6A is next, but must start only after a fresh live-state audit.

Its exact objective is a pure generic shown-identity policy/options boundary:

```text
selected SetupCandidate
+ validated ruleset
+ candidate provenance
-> legal shown-identity options/policy
```

Key constraints for S6A:

- do not change `SetupCandidate` to contain shown roles;
- template candidates must be able to resolve template-specific metadata through durable provenance identity;
- generated candidates derive legal options from the script/ruleset and selected actual composition;
- do not select/commit one option yet — that belongs to S6B;
- do not change recommendation yet — that belongs to S6C;
- do not cut TB or NGJ production over yet — S7/S8;
- keep PR #61 Draft and unmerged.
