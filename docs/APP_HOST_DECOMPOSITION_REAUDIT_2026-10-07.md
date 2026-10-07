# App / Host Decomposition Re-audit — 2026-10-07

> Repository: `Jazz0006/CampBoardGameHost`
>
> Baseline: live `main` at `e7b1691710bef73a2969a002667e2330c2529169` after RES-3 merge.
>
> Status: **CURRENT DECOMPOSITION AUDIT / RES-5 INPUT**
>
> This document supersedes `APP_HOST_DECOMPOSITION_PLAN_AUDIT_2026-09-28.md` as the current decomposition plan. The older audit remains historical evidence for constraints that are explicitly carried forward here.

## 1. Why this re-audit is needed

The 2026-09-28 audit was performed before the RES engine/recommendation separation and before the physical RES-3 purge.

RES-3 materially changed the Host ownership surface:

- legacy `RecommendationStyle`, `RecommendationPlan`, `RecommendationProfile`, `WeightedStableSelector`, `GameBalanceEvaluator` and related local score/rank infrastructure are gone;
- the old setup recommendation service/evaluator and dynamic Mayor/Demon/pair recommenders are gone;
- current recommendation/provider integration is a neutral optional seam, not Host-owned ranking authority;
- complete Manual legal domains and rule-deterministic outcomes remain engine-owned.

Therefore the old H2 proposal around `dynamicStorytellerState()`, `DynamicGameState` and `GameBalanceEvaluator` is obsolete and must not be implemented.

The purpose of this audit is not to make files small for its own sake. The success criterion is narrower ownership, fewer duplicated seams, and a smaller set of files that must be understood or edited for one responsibility.

## 2. Current size and concentration

Fresh post-RES-3 measurements from live `main`:

| Surface | Current lines | Approx. source characters | Prior 2026-09-28 lines | Observation |
| --- | ---: | ---: | ---: | --- |
| `CampBoardGameHostApp.kt` | 3,760 | ~210,588 | 3,613 | Still large; growth is mainly app/session orchestration rather than recommendation logic. |
| `clocktower/ui/ClocktowerHostScreen.kt` | 3,267 | ~187,462 | 4,226 | RES-3 already removed almost 1,000 lines, confirming that obsolete recommendation authority was a major Host coupling source. |
| `ClocktowerJudgeScreen` parameter surface | ~86 parameters | — | 89 previously | Still a major coupling signal despite RES-3. |

The two files now have different problems:

- **App root:** state lifetime, session publication, durability/recovery, setup/start flows, and large transaction-application callbacks are still concentrated in one Compose root.
- **Host:** recommendation ranking has largely disappeared, but information preparation, registration interaction state, night-step assembly, and phase routing remain concentrated in one large function.

## 3. Fresh ownership map

### 3.1 App root

Current major blocks:

| Approx. lines | Responsibility | Current verdict |
| --- | --- | --- |
| 184–344 | preferences + active recovery raw storage + game archive preferences | Good bounded storage extractions exist; keep them separate. |
| 359–437 | Clocktower presentation role catalog / labels | Still an optional bounded extraction. |
| 439–1494 | Compose state lifetime, session publication, A4 prewarm/rebuild, timeline/observation recording, recovery capture/application | Keep central lifetime authority in App; only extract already-cohesive storage or pure preparation. |
| 1495–2037 | player management, setup/start flows, archive/restart, role mutation helpers | Mixed; setup/session entry remains App orchestration. |
| 2238–3561 | `ClocktowerJudgeScreen(...)` wiring | Largest single App hotspot. The callback bodies, not the call syntax, are the real debt. |
| 2973–3106 | `onConfirmDay` | High-risk transaction application: session mutation + public observation + outcome + navigation. |
| 3107–3561 | `onConfirmNight` | Highest-risk block: dawn planning/materialization, death/role/poison/session/history/durability/navigation convergence. |

Important conclusion: the App file cannot be safely reduced by wrapping all Judge inputs/callbacks in one `AppContext`, `HostActions`, manager, or giant state object. That would hide rather than reduce coupling.

### 3.2 Host

Current major blocks:

| Approx. lines | Responsibility | Current verdict |
| --- | --- | --- |
| 81–168 | `ClocktowerJudgeScreen` 86-parameter boundary | Needs convergence, but only through existing typed owners or genuinely cohesive new seams. |
| 169–357 | first-night publication migration + Spy/Recluse registration interaction-local state | Keep interaction lifetime semantics; possible later focused registration-state extraction only after a separate audit. |
| 358–558 | `ClocktowerNightHostProjection` consumption + effective-night calculations + event recording | Projection boundary is already useful; do not invent a replacement broad context. |
| 559–722 | screen-local day/special state + first-night precompute | Keep Compose lifetime local unless a typed owner already exists. |
| 723–1288 | neutral number/yes-no/role/pair information preparation plus currently named Librarian/Investigator policy hooks | Strong post-RES-4 extraction candidate. |
| 1289–1528 | result-first registration option projection + first-night evil-information inputs | Mixed; the evil-information presentation module already exists, so the old H1 work is largely complete. |
| 1529–2356 | first/other-night step materializer assembly | Large but already delegates to typed materializers/registry. Do not move wholesale into one giant builder. |
| 2357–2875 | Dawn/Day routing | Rendering already delegates to dedicated screens; moving only the glue would mostly relocate coupling. |
| 2876–3267 | first-night/night ready and active routing | Existing `ClocktowerNightScreen` owns substantial rendering; remaining glue should be reduced only through typed state convergence. |

## 4. Critical new seam finding — ClocktowerNightCheckpoint duplication

`ClocktowerNightCheckpoint` already owns the same-night typed transaction values:

- game/player revisions;
- night started / step index;
- confirmed + draft Demon attack;
- confirmed + draft Poisoner target;
- confirmed + draft Monk target;
- confirmed + draft Mayor redirect;
- pending new Demon identity;
- Demon successor draft + confirmed target;
- next timeline sequence.

However `ClocktowerJudgeScreen` still separately receives many of the same values:

- `gameStateRevision`, `playerInputRevision`;
- `pendingNightDeath`, `demonAttackDraftTarget`;
- `poisonTarget`, `poisonDraftTarget`;
- `monkProtectedTarget`, `monkProtectedDraftTarget`;
- `mayorRedirectTarget`, `mayorRedirectDraftTarget`;
- `pendingNewDemonName`, `pendingNightNewDemonIdentityName`;
- `demonSuccessorTarget`.

The App constructs `currentClocktowerNightCheckpoint()` directly from those same fields.

This is a real duplicate boundary, not merely a long parameter list. A bounded post-RES-4 / RES-5 slice should make Host read those immutable values from `ClocktowerNightCheckpoint` and remove the duplicate scalar parameters.

Do **not** force `phase` or unrelated day state into the checkpoint merely to shrink the signature. Do not make the checkpoint a general Host context.

## 5. Reclassification of the 2026-09-28 plan

| Old item | Fresh verdict | Reason |
| --- | --- | --- |
| A1 — App preferences storage | **STILL VALID / LOW RISK** | Language mode, Storyteller experience mode and common-player preferences remain private Context helpers in App with narrow consumers. |
| A2 — archive storage | **STILL VALID / LOW RISK** | `loadGameHistory/archiveGame` remain App preference I/O around existing `GameArchiveJsonCodec`; separate from Recovery. |
| A3 — presentation catalog | **STILL VALID / OPTIONAL** | Role presentation data still lives in App and has broad read-only fan-out. Useful isolation, modest size benefit. |
| H1 — first-night evil-information presentation | **MOSTLY COMPLETE / DO NOT REOPEN AS A CAMPAIGN** | `ClocktowerFirstNightEvilInformationPresentation.kt` already exists and Host consumes `clocktowerFirstNightEvilInformationSteps`. Remaining bluff legality/manual choice belongs outside the renderer. |
| H2 — dynamic recommendation-state projection | **OBSOLETE / DELETE FROM PLAN** | RES-3 removed `dynamicStorytellerState`, `DynamicGameState` recommendation ownership and `GameBalanceEvaluator`. |
| Deferred pair preparation | **REOPEN AFTER RES-4** | RES-3 removed local style/rank/weight machinery. RES-4 will remove named Librarian V2 / Investigator INV1-A execution hooks, leaving a much cleaner neutral preparation surface. |
| Recovery R7 | **STILL DEFERRED / SEPARATE DESIGN** | Durability and re-entry ordering remain high risk and unrelated to file-size cleanup. |
| Generic App transaction applier | **STILL NO-GO** | `onConfirmDay/onConfirmNight` apply heterogeneous session, presentation, durability and continuation effects. One generic executor would become a second authority. |

## 6. Approved bounded sequence

This audit does **not** reopen D6 wholesale. It approves the following bounded route.

### DEC-A1 — App preferences storage

**Risk:** low.

Extract only:

- language mode load/save;
- Storyteller experience mode load/save;
- common-player load/save and existing absent-vs-empty behavior.

Suggested owner: a small preference-specific store under `persistence`.

Do not include active-game recovery or archive storage.

Primary evidence:

- `CommonPlayersDefaultPolicyTest`;
- focused storage characterization only where Android preference behavior is not already covered;
- existing settings flow compile/integration evidence.

Expected benefit: small App reduction, strong ownership isolation.

### DEC-A2 — Game archive preferences storage

**Risk:** low.

Extract only:

- `loadGameHistory`;
- `archiveGame`;
- archive preference key/capacity I/O.

Continue using `GameArchiveJsonCodec`. Pass role lookup as a narrow dependency; do not absorb presentation catalog or Recovery.

Primary evidence:

- `GameArchiveJsonCodecTest`;
- `GameArchiveLegacyRejectionTest`;
- focused newest-first / capacity / malformed-entry storage behavior if uncovered.

### DEC-H0 — NightCheckpoint seam convergence

**Risk:** low-to-medium.

Make `ClocktowerJudgeScreen` consume same-night immutable draft/confirmed/revision values from the already-existing `ClocktowerNightCheckpoint`.

Remove duplicate scalar parameters only when exact equivalence is proven.

Do not:

- add unrelated day/UI fields to the checkpoint;
- move mutable Compose ownership into the checkpoint;
- change transaction semantics;
- convert `phaseName` into a cross-layer UI enum merely for convenience.

Primary evidence:

- `ClocktowerNightCheckpoint` / reducer / host transaction tests;
- night target legality/presentation tests;
- Host integration compile and exact producer/consumer re-search.

Expected result: a materially smaller and less contradictory App↔Host seam, not merely fewer lines.

### DEC-H1 — Neutral information-preparation extraction

**Timing:** after RES-4 removes named deterministic selectors.

**Risk:** medium.

Target the large Host preparation region that currently builds neutral numeric, yes/no, role-reveal and pair presentation/legal options.

The extraction must:

- consume existing rules-owned/legal-domain outputs;
- preserve `PairInformationLegalDomain`, `ClocktowerPairManualAuthority`, `ClocktowerInformationStepBuilder`, structured information adapters and current candidate identity;
- remain stateless/read-only;
- own no recommendation ranking or named special policy;
- not absorb publication/confirmation or registration mutation.

A good result is a small number of typed preparation functions/models whose inputs are actual semantic facts. A bad result is one `InformationContext` carrying most of `ClocktowerJudgeScreen`.

Primary evidence:

- `InformationDecisionFoundationTest`;
- `PairInformationLegalDomainTest`;
- `ClocktowerPairManualAuthorityTest`;
- `ClocktowerPairManualSelectionModelTest`;
- `ClocktowerFirstNightInformationRequestTest`;
- structured numeric/boolean preparation tests;
- first-night production-flow/materializer tests.

Expected reduction: several hundred Host lines are plausible after RES-4, but ownership reduction is the acceptance criterion.

### DEC-A3 — Clocktower presentation catalog

**Risk:** low-to-medium; optional after A1/A2/H0/H1.

Move presentation-only role lists, script presentation membership and localization helpers out of App.

Do not merge this with `BuiltInClocktowerRulesetCatalog`; validated ruleset authority and presentation catalog remain distinct.

This is a useful reading-scope reduction but should not block higher-value Host convergence.

## 7. Explicitly deferred high-risk work

### App day/night transaction application

The large App callbacks remain the biggest source-size hotspot:

- `onConfirmNewDemon` ~169 lines;
- `onSlayerShot` ~110 lines;
- `onConfirmDay` ~134 lines;
- `onConfirmNight` ~455 lines.

They already compose typed owners such as:

- `NightCheckpointHostTransaction`;
- `NightDawnResolutionPlanner`;
- `NightDawnDurableMaterializationPlanner`;
- `NightDawnPoisonRecoveryAuthority`;
- `ClocktowerGameSession` mutation boundaries.

That is evidence that planning has already been decomposed more than application.

**Current decision: do not move these callbacks merely to reduce `CampBoardGameHostApp.kt`.**

A future App transaction-application audit may approve narrower executors only if it can prove:

- no second mechanical/session authority;
- exact durability ordering;
- exact event/observation timeline identity;
- exact A4 invalidation/rebuild timing;
- fresh/restored convergence;
- navigation/continuation ordering.

Until then, App remains the composition/application owner.

### Whole night-step assembly

The 1529–2356 Host block is large, but it already orchestrates many typed materializers. Moving the complete registry construction into one giant builder would only move the dependency list.

Re-audit after DEC-H0/H1. Approve only smaller cohesive role-family or phase assembly boundaries whose inputs stay narrow.

### Day routing glue

Dedicated day screens already exist. Moving the remaining `when(dayMode)` glue to another file without reducing state/callback dependencies is not meaningful decomposition.

## 8. Relationship to RES-4 and RES-5

Recommended sequence:

```text
RES-3 COMPLETE / ACCEPTED
-> current decomposition re-audit COMPLETE
-> RES-4 named deterministic special-policy physical purge
-> RES-5 physical module/dependency convergence
   -> DEC-H0 NightCheckpoint seam convergence
   -> DEC-H1 neutral information-preparation extraction
   -> DEC-A1/A2/A3 either before or during the same bounded maintenance window
-> re-audit App transaction application + remaining Host materialization
-> resume GSP context materializer/retrieval/benchmark
```

A1/A2 are independent of RES-4 and may be implemented earlier if desired, but Host extraction should wait until RES-4 removes the remaining named special-policy hooks so they are not moved into a new module and then immediately deleted.

The decomposition work should therefore be treated as an input to **RES-5 physical convergence**, not as a new broad campaign inserted ahead of RES-4.

## 9. Acceptance rules for every extraction

For each slice:

1. Freeze the current owner, producers, consumers, state lifetime and forbidden responsibility before editing.
2. Prefer existing typed seams over new bundle objects.
3. Do not create `AppContext`, `HostState`, `HostActions`, generic manager/utils or a second session authority.
4. Preserve Manual legal domains, deterministic rules, session revision/timeline authority and recovery ordering.
5. Use existing tests for behavior-preserving moves; add characterization only for a real uncovered contract.
6. Re-run full producer/consumer search after the move.
7. Inspect the exact diff and distinguish moved code from deleted code and genuinely new behavior.
8. Use T1 at logical checkpoints and T4 `[full-ci]` for acceptance of shared orchestration/boundary changes.

## 10. Bottom line

A renewed decomposition effort is now justified, but it should be **narrower and more architectural than the old plan**.

The highest-value immediate architectural findings are:

1. the old recommendation-projector H2 route is obsolete;
2. `ClocktowerNightCheckpoint` already provides a real seam that can remove duplicated App↔Host state parameters;
3. RES-4 will make neutral information preparation substantially cleaner and finally suitable for extraction;
4. App storage/catalog responsibilities remain easy bounded wins;
5. the very large day/night transaction callbacks should not be mechanically moved until a dedicated application-ordering audit proves a safe owner.

The first-wave goal is not “make both files <100K.” It is to make the next change to preferences, archive storage, same-night checkpoint state, or information preparation require opening fewer owners. Re-measure both root files after A1/A2/H0/H1 before approving deeper surgery.
