# RES-5 Physical Module / Dependency Convergence Acceptance — 2026-10-07

> Repository: `Jazz0006/CampBoardGameHost`
>
> Branch: `res-5-physical-module-dependency-convergence`
>
> PR: #242 — `RES-5: converge provider boundary and Host seams`
>
> Status: **COMPLETE / ACCEPTED**

## 1. Accepted implementation shape

RES-5 converges the post-RES-4 codebase without reintroducing recommendation authority into the Game Engine or Host.

Completed bounded slices:

- **RES-5A — legacy StorytellerPolicy compatibility purge**
  - checkpoint: `4d0c841d10b5c407306264ca641e0ecd0fb63e26`
  - physically removed the old `StorytellerPolicy*` request/context/provider compatibility family and adapter;
  - `app/src` now has zero `StorytellerPolicy` references;
  - the neutral `StorytellerProviderContractV1`, request factory and Host-owned context builder remain.

- **RES-5B / DEC-H0 — NightCheckpoint seam convergence**
  - checkpoint: `03601061a7f68bc897a6a4e44ab306eef9e05a87`
  - removed 13 duplicated App -> `ClocktowerJudgeScreen` night/revision scalar parameters;
  - the Host now derives those exact values from `ClocktowerNightCheckpoint`;
  - the screen boundary is 72 parameters, down from the post-RES-3 audit baseline of about 85 after exact duplicate removal;
  - canonical state and transaction semantics are unchanged.

- **RES-5C / DEC-A1+A2 — bounded preference/archive storage extraction**
  - checkpoint: `9c5e2e79992350396ab3d4f6c324fd9684299455`
  - added `AppPreferencesStore` for language / Storyteller experience / common-player preferences;
  - added `GameArchivePreferencesStore` for game archive preference I/O;
  - active-game Recovery remains App-owned and was not mixed into either store.

- **RES-5D / DEC-H1 — neutral information preparation extraction**
  - checkpoint: `67ac6f10228dbfa745ae1d8bf16a0770df8164ab`
  - added stateless/read-only `ClocktowerNeutralInformationPreparation`;
  - moved numeric, yes/no, role-reveal and pair presentation/legal preparation out of the large Host function;
  - preserved rules-owned legal domains, `ClocktowerPairManualAuthority`, `ClocktowerInformationStepBuilder`, structured adapters and candidate identity;
  - no ranking, named policy, publication/confirmation, registration mutation or canonical commit authority moved into the preparation object.

- **RES-5E — dormant automatic-policy seam purge**
  - checkpoint: `d6382ddbbfaa280307ef2361d007011c4aa68f20`
  - removed production-dead `automaticSelectionOptions` and `automaticPolicyRecommendation` seams;
  - multi-choice automatic information now fails closed to Manual until a future neutral provider response is explicitly materialized and rebound;
  - `StorytellerPolicy`, `automaticPolicyRecommendation` and `automaticSelectionOptions` are all zero-reference in `app/src`.
  - ordinary checkpoint validation: CI #3831 GREEN / R2 #3508 GREEN.

## 2. Surviving provider dependency graph

The intended neutral graph is preserved:

```text
engine/session pending decision + immutable TB snapshot
        |
        v
StorytellerProviderRequestFactoryV1
        |
        +--> StorytellerProviderGameContextBuilderV1
        |      (Host-owned player context + prior committed decisions)
        |
        v
StorytellerProviderContractV1
        |
        v
future optional provider/materializer
```

The engine/session and neutral contract surfaces do not import recommendation implementation packages. Provider conversation memory is not an authority.

## 3. Post-extraction convergence verdict

No further code movement is justified inside RES-5.

- `legacyInformationCandidates` remains a real migration/parity source with active structured/presentation consumers and is not executable recommendation authority.
- `misinformationPressure` remains semantic projection data used by first-night truth/misinformation preparation; it is not the deleted weighted recommendation scorer.
- `recommendedDisplayOptions` and related presentation data still have active UI/materializer consumers; broad renaming/removal would be a separate migration, not required for physical authority convergence.
- DEC-A3 presentation-catalog extraction remains optional reading-scope maintenance and does not block the boundary.
- App transaction application callbacks and whole-night-step assembly remain explicitly deferred pending dedicated ownership audits; moving them now would mostly relocate dependencies rather than reduce them.

RES-5 therefore stops at the smallest meaningful ownership convergence point instead of targeting arbitrary file-size thresholds.

Final post-RES-5 measurements:

- `CampBoardGameHostApp.kt`: 3,588 lines / 202,319 source characters (post-RES-3 audit baseline: 3,760 / ~210,588);
- `ClocktowerHostScreen.kt`: 2,954 lines / 173,295 source characters (post-RES-3 audit baseline: 3,267 / ~187,462);
- `ClocktowerJudgeScreen`: 72 parameters after removing 13 exact NightCheckpoint duplicates (post-RES-3 audit baseline: ~85).

## 4. Final acceptance

Accepted T4 checkpoint:

- exact executable/docs checkpoint: `cdf5dba23eaad23f4fea3899ab54df97553a55cd` with `[full-ci]`;
- CI #3832 — GREEN;
- Android full JVM tests + debug assemble — GREEN;
- ASP contract tests — GREEN;
- Real Clingo cross-validation — GREEN;
- R2 main-thread boundary #3509 — GREEN;
- exact-head PR audit — mergeable/clean, 0 failed/pending checks and 0 unresolved review threads.

RES-5 is therefore **COMPLETE / ACCEPTED**. The next active development lane is the resumed GSP context-retrieval / materializer / benchmark route. DEC-A3 remains optional independent maintenance; App transaction-application and broader Host assembly decomposition require fresh dedicated audits rather than continuation of RES-5.
