# GSP Post-RES-5 Re-entry Audit — 2026-10-08

> Repository: `Jazz0006/CampBoardGameHost`
>
> Audit baseline: live `main@050f9ac72be5510a85f35ec75c89672bf935a6bf`
>
> Status: **CURRENT GSP RE-ENTRY AUDIT / ROUTE RESET AFTER RES-5**
>
> Scope: determine which pre-RES GSP foundations still survive, which recorded gaps are already closed or obsolete, and the smallest safe route from the neutral provider contract to a meaningful whole-game benchmark.

## 1. Executive conclusion

RES-5 successfully removed the two obsolete recommendation generations and left a usable neutral provider shell. GSP should resume, but **must not resume the old pre-RES `GSP-2B3 -> 2C -> 2D` sequence verbatim**.

The most important remaining gap is now:

> **The provider request does not yet carry the canonical current-game longitudinal history that already exists in the Host.**

Current `StorytellerProviderGameContextV1` contains per-seat player context and effective prior Storyteller decisions, but not the canonical `ActionFactTimeline` or `EpistemicObservationLog`. A general Storyteller reasoner therefore cannot yet reconstruct the whole game narrative, information continuity, poisoning/registration timing, prior shown information, deaths/role changes, or other longitudinal interactions from the neutral request alone.

The next executable target should be **GSP-R1 — neutral current-game longitudinal context materialization**.

## 2. Surviving foundations that should be reused

### 2.1 Neutral provider contract — KEEP

The RES-2/5 survivor is structurally sound:

- `StorytellerProviderRequestV1`;
- `StorytellerProviderResponseV1`;
- `StorytellerProviderResponseValidatorV1`;
- `StorytellerProviderRequestFactoryV1`;
- `StorytellerProviderGameContextBuilderV1`.

The contract already protects:

- canonical decision identity;
- source `gameStateRevision + playerInputRevision`;
- complete Host-generated legal candidate IDs;
- decision-specific typed payloads;
- provider deferral / missing-context reporting;
- stale-response rejection;
- unknown-candidate rejection;
- no provider commit authority.

The provider contract remains vendor/model/network neutral.

### 2.2 Current-game player-context ownership — KEEP

`ClocktowerSessionState` owns sparse `storytellerPlayerContextBySeat` overrides:

- `experienceLevel`: BEGINNER / NORMAL / EXPERT;
- `claimedRoleIds`;
- Storyteller-declared `pressureLevel`.

`ClocktowerGameSession.updateStorytellerPlayerContext(...)` increments only `playerInputRevision`, which is exactly the correct freshness behavior for recommendation input that is not mechanical Game State.

### 2.3 Recovery persistence — ALREADY COMPLETE

The old GSP route is stale when it says GSP-2B still lacks Recovery persistence.

Current main already persists and restores the session player-context overrides through:

- `ClocktowerRecoveryHistory.storytellerPlayerContextBySeat`;
- `RecoverySnapshotJsonCodec`;
- `RecoverySnapshotStrictDecoder`;
- App restore/session reconstruction;
- dedicated Recovery codec/session tests.

Do **not** create another GSP Recovery layer or replay the old `gsp-2b2/gsp-2b3-recovery-player-context` branches.

### 2.4 Canonical longitudinal history — KEEP AT CURRENT OWNERS

The Host/session already owns the facts needed for whole-game reasoning:

- `DecisionHistoryArchive` — committed Storyteller decisions;
- `ActionFactTimeline` — committed mechanical actions with global timeline identity;
- `EpistemicObservationLog` — information actually shown or publicly established;
- `ClocktowerSemanticHistoryMode` / global sequence authority.

These are durable canonical inputs. GSP must project them; it must not create a second mutable narrative history.

## 3. Gaps and obsolete surfaces

### 3.1 Provider request is not yet production-connected

Fresh main-source reachability shows that `StorytellerProviderRequestFactoryV1.fromDrunkAssignment/fromPairInformation/fromMayorRedirect` and `StorytellerProviderResponseValidatorV1` currently have no production consumer outside their definitions. Their value is contract/test foundation.

This is acceptable before benchmark materialization. GSP re-entry should remain offline/local until GSP-3A/B demonstrate recommendation quality.

### 3.2 Current provider game context is too narrow

`StorytellerProviderGameContextV1` currently carries only:

- `players`;
- `priorDecisions`.

It does not carry:

- committed action facts;
- recorded epistemic observations;
- globally ordered narrative prefix;
- public/private information recipients;
- poison/protection/attack/execution/death/role-change chronology beyond what happens to be recoverable from the current snapshot;
- the exact earlier information statements needed for Drunk/Poison continuity.

This is now the primary architectural blocker for meaningful whole-game recommendation.

### 3.3 Pre-RES SDE historical prefix is useful as a pattern, not a dependency

`SdeHistoricalPrefixRef.Global` already demonstrates the correct high-level principle:

- identify canonical action/observation records;
- retain global chronology;
- reject future/post-decision facts;
- do not copy mutable history ownership.

However it lives under recommendation/SDE and must **not** become a dependency of `StorytellerProviderContractV1`.

GSP-R1 should introduce a neutral equivalent or neutral materialized history projection at the Host/provider boundary.

### 3.4 Storyteller edit surface is still missing from main

There is no production call to `ClocktowerGameSession.updateStorytellerPlayerContext(...)` in current main.

The old remote branch `gsp-2b3b-player-context-edit-surface` contains a useful historical prototype:

- host-only role-list editing;
- player experience selector;
- current-game claimed roles;
- current-game declared pressure;
- a separate persisted per-name experience profile.

But that branch diverged before RES-3/4/5 and modifies large App/UI files against obsolete ownership. It is **reference material only** and must not be cherry-picked or merged wholesale.

### 3.5 Cross-game recommendation history is not implemented

The existing `CrossGameHistory / HistoricalClueSignature` surface is not a usable GSP source:

- `recordCompletedGameSignature(...)` has no production caller;
- no live producer constructs `HistoricalClueSignature`;
- no durable persistence path was found;
- its fields are narrow legacy clue-shape data rather than a generic provider episode contract.

Treat this surface as dead/legacy until a separate cleanup or migration audit proves otherwise.

### 3.6 Generic game archive is not sufficient as provider cross-game context

`ArchivedGameReview` / `GameArchiveJsonCodec` persist:

- players/cards;
- eliminations;
- generic events;
- outcome.

They do **not** persist the complete neutral provider decision episodes, decision history, epistemic observation log, per-player Storyteller context, or semantic recommendation/diversity outcomes needed for reconstructable cross-game reasoning.

The archive is valuable human review history, but GSP must not silently treat it as a complete recommendation-history database.

### 3.7 Prompt/response materializer does not exist yet

No current production/main source provides:

- deterministic JSON/wire encoding for `StorytellerProviderRequestV1`;
- benchmark prompt materialization;
- parseable provider response materialization;
- local prompt-response fixture harness.

This remains correctly downstream of context completeness.

## 4. Context layers after the audit

The Host-owned target remains four layers, but their implementation state is now clearer.

| Layer | Current status | Owner / next action |
| --- | --- | --- |
| Current canonical state + legal domain | COMPLETE foundation | Engine/session + neutral request factory |
| Current-game longitudinal/narrative history | **PARTIAL** | canonical history exists; GSP-R1 must project it into neutral provider context |
| Player context | session + Recovery COMPLETE; edit/profile UX missing | GSP-R2 |
| Cross-game player/recommendation/diversity history | **MISSING** | GSP-R3, from a new generic Host-owned durable contract; do not revive `HistoricalClueSignature` as policy |
| Prompt/response materialization | **MISSING** | GSP-R4 |
| Remote provider/API | NOT AUTHORIZED YET | only after GSP-3A/B |

## 5. Revised GSP re-entry route

The old pre-RES 2B3/2C/2D numbering is historical. Use the following post-RES sequence.

### GSP-R0 — post-RES-5 re-entry audit

Status: **COMPLETE when this audit is merged.**

Acceptance:

- live main and neutral contract re-audited;
- already-complete Recovery work removed from the blocker list;
- stale/dead cross-game surfaces classified;
- old pre-RES branches explicitly reference-only;
- next executable slice selected without reintroducing policy/recommendation ownership.

### GSP-R1 — neutral current-game longitudinal context

**NEXT EXECUTABLE.**

Goal: materialize the canonical committed prefix needed for whole-game reasoning into a typed neutral provider context.

Required properties:

1. read only from Host/session canonical `DecisionHistoryArchive`, `ActionFactTimeline`, and `EpistemicObservationLog`;
2. preserve global chronology;
3. support a live-current prefix and an explicit historical/frozen prefix for benchmark reconstruction;
4. reject post-decision/future records instead of guessing chronology;
5. carry factual/mechanical/epistemic semantics, not recommendation scores or policy labels;
6. introduce no second mutable history store;
7. import no `clocktower.recommendation.*` implementation into the neutral provider/domain/session owners;
8. keep provider requests independently reconstructable without LLM conversation memory.

Recommended shape:

- add neutral provider-history types under domain/contract ownership;
- use stable action/observation identity + global sequence as prefix authority;
- materialize bounded action/observation payloads for the request;
- extend `StorytellerProviderGameContextBuilderV1` rather than creating a generic `HostContext` manager;
- add exact tests for chronology, prefix exclusion, Recovery/fresh-state equivalence, and no future leakage.

Do not simply move `SdeHistoricalPrefixRef` into the provider contract. Extract the idea, not the recommendation-owned type.

### GSP-R2 — Storyteller player-context edit + durable experience profile

After R1.

Split if useful:

- **R2A:** explicit host-only editing of experience / claims / declared pressure through the existing session mutation;
- **R2B:** persist only stable cross-game player experience and hydrate it into a new game.

Rules:

- claims and declared pressure stay current-game only and already travel through Recovery;
- only experience is a durable player-profile candidate;
- player-profile identity must be audited against the product's current player-name identity before persistence;
- do not cherry-pick the old `gsp-2b3b-player-context-edit-surface` branch wholesale.

### GSP-R3 — generic cross-game recommendation/diversity context

After R1/R2 establish stable current-game/player identities.

Define a Host-owned, bounded, explainable cross-game contract for:

- recent player role/treatment history;
- prior selected Storyteller outcomes;
- semantic repetition/similarity metadata;
- soft diversity context.

Requirements:

- no legality effect;
- no hard repeat prohibition;
- no old weighted selector;
- no `HistoricalClueSignature` narrow-field policy resurrection;
- later game outcomes remain evaluation metadata unless they were known at the historical decision point.

The durable storage owner must be selected explicitly; do not silently overload the human-review game archive.

### GSP-R4 — prompt/response materializer + local validator

Only after R1–R3 define the complete benchmark input contract.

Build deterministic, vendor-neutral local materialization:

- structured request serialization;
- human/LLM-readable benchmark prompt;
- parseable structured response;
- existing local candidate/freshness validator;
- Manual fallback semantics.

No network/API dependency.

### GSP-3A — manual blind benchmark

Then run frozen benchmark cases in clean independent sessions/models.

At least one case must exercise:

- multiple interacting first-night decisions;
- longitudinal information history;
- player-level context;
- at least one cross-game/diversity enrichment;
- genuinely different strong alternatives.

Only after GSP-3A/B demonstrates stable value may GSP-3C add an automated remote adapter.

## 6. Explicit non-goals

This re-entry does not authorize:

- restoring Q04/V1/V2/Librarian/Investigator executable selectors;
- restoring `StorytellerPolicy*`;
- hand-authored weighted ranking;
- recommendation-owned mutable memory;
- provider conversation memory as correctness authority;
- API/network integration;
- model training;
- broad App/Host decomposition;
- a universal mutable `HostContext` object.

## 7. Immediate handoff

Start from fresh live `main`.

Next task:

> **GSP-R1 — neutral current-game longitudinal context**

Before editing, map the full fan-out of:

- `StorytellerProviderGameContextV1`;
- `StorytellerProviderGameContextBuilderV1`;
- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- existing SDE historical-prefix producers only as semantic reference.

Implement the smallest neutral typed prefix/materialization seam, prove prefix safety and no recommendation imports, then reassess whether R1 needs a second slice before moving to R2.
