# App and Host decomposition plan audit

> Date: 2026-09-28 Australia/Sydney
> Audited baseline: `main@325a47f3597b61788091ef91d9d41674f7e25d91`, after PRs #166 and #167.
> Status: **HISTORICAL / SUPERSEDED BY `APP_HOST_DECOMPOSITION_REAUDIT_2026-10-07.md`**. Keep this document only for historical constraints and comparison; do not execute its H2 dynamic-recommendation projection route after RES-3.
> Scope: `CampBoardGameHostApp.kt` and `clocktower/ui/ClocktowerHostScreen.kt`, their immediate owners, existing plans and relevant test boundaries.

## 1. Recommendation

Continue with a small number of responsibility-based extractions, not a campaign to bring both files below a line-count threshold. **Do not restart the old S9 or D6 plans verbatim.** Several proposed extractions already happened, and later audits explicitly rejected others.

The safest next App slice is preferences storage. Archive storage is a separate, slightly riskier slice. Neither requires changing game-state ownership. For Host, first-night evil-information presentation is a bounded candidate; recommendation-state projection deserves a focused contract audit before implementation. Neither warrants a general Host controller or a replacement recommendation algorithm.

The largest remaining App blocks are transaction application and recovery application. They are expensive to understand, but wrapping them in an object with dozens of callbacks would preserve that cost and introduce another layer. Keep the previous NO-GO decision for broad transaction extraction unless a new product requirement supplies a genuinely narrower contract.

This is compatible with the current evidence-collection pause: policy V1, candidate ordering, rules and replay evidence remain unchanged. Structural work must not quietly become policy tuning.

## 2. Evidence and limits

This audit used the merged main checkout, declaration/effect inventories, targeted reads of state ownership, storage, recovery, transaction callbacks, recommendation preparation and materialization, plus production reference searches and relevant test/architecture-guard inspection. It is not a line-by-line correctness proof of all game rules, a complete dead-code audit, or a runtime performance measurement.

Measurements count physical lines and UTF-8 bytes in the checkout, including imports, comments and blank lines. Line references below refer only to the audited commit; implementations should locate symbols rather than patch numeric positions.

| Surface | Lines | Bytes | Observation |
| --- | ---: | ---: | --- |
| `CampBoardGameHostApp.kt` | 3,613 | 204,576 | Storage adapters, presentation catalog, state/lifecycle, recovery, setup and transaction wiring remain together. |
| `clocktower/ui/ClocktowerHostScreen.kt` | 4,226 | 247,838 | One large `ClocktowerJudgeScreen` still combines preparation, effects, materialization and phase routing. |
| `ClocktowerJudgeScreen` signature | 89 parameters | — | 38 parameters start with `on`; five expose `MutableState`; three optional computation providers remain. |

These are maintainability signals, not proof of poor runtime performance. No speedup or memory reduction is claimed from moving code. A smaller signature alone is not success if the same fields are hidden inside a large context object.

## 3. Audit of the existing plans

| Prior document / decision | Current verdict |
| --- | --- |
| [Deferred App-root S9](archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md): extract snapshot codec / validated restore parser | **Superseded as an implementation recipe.** `RecoverySnapshotJsonCodec`, `RecoverySnapshotStrictDecoder`, `RecoveryRestorePlanner`, `RecoveryApplicationCoordinator` and `RecoveryAppEnvironment` already cover those responsibilities. Do not add parallel ActiveGame parser/codec owners. |
| [D6 global audit](archive/checkpoints/d6/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md): preparation, materializers, effects, transaction and recovery waves | Useful question inventory, not the final approved queue. Its initial sizes, dead-code list and planned second wave are historical. Read the later closeout before implementing its suggestions. |
| [D6 closeout](archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md) and [R3 viability audit](archive/checkpoints/d6/R3_TRANSACTION_APPLICATION_VIABILITY_AUDIT_2026-09-09.md) | **Still applicable:** no generic transaction applier, no second session authority. Current App callbacks still apply heterogeneous session, presentation, durability and continuation work. |
| [Numeric materializer audit](archive/checkpoints/d6/D6_2T_NUMERIC_MATERIALIZER_FAMILY_AUDIT_2026-09-09.md) | **Still applicable:** no new Clockmaker/Chef/Empath family wrapper merely because all display numbers. Existing builder and numeric option preparation already own the common parts. Chambermaid and Sage have concrete specialized materializers; reuse their principle, not a one-file-per-role rule. |
| [Setup-effect audit](archive/checkpoints/d6/D6_2X_SETUP_EFFECT_OWNER_NECESSITY_AUDIT_2026-09-09.md) | **Still applicable:** recommendation loading, auto-application, first-night precompute/start gating, poison invalidation and A4 have different lifetimes. Do not aggregate them into one effect controller. |
| [Current-only Recovery audit](CURRENT_ONLY_RECOVERY_MINIMAL_STATE_AUDIT_2026-09-28.md) | R0–R6 are complete. R7 bookkeeping, replay transport and persistence-trigger ownership remain separate optional work, not unfinished decoder cleanup. |
| [Current roadmap](CURRENT_DEVELOPMENT_ROADMAP.md) / [handoff](NEXT_DEVELOPMENT_HANDOFF.md) | Preserve the evidence lane and frozen policy. This document proposes a bounded maintenance lane; it does not reopen D6 wholesale or authorize C5/V2. |

Documentation drift: root AGENTS.md still describes the older S7.1/S7.2 pause, whereas the current roadmap and D6 closeout record later D6 completion. Treat the older paragraph as a prohibition on automatic resumption, not as evidence that later extractions never happened. This audit records the discrepancy; it does not rewrite project authority. Any approved implementation handoff should link this fresh audit and state its exact slice.

The old S9 requirement to manufacture new RED tests before structural work and its byte-saving threshold are not appropriate gates today. Apply the current risk-based testing policy: existing coverage first, new tests for uncovered durable contracts, no abstractions introduced solely to satisfy a test ritual.

## 4. Current ownership map

### App root

| Region / anchor | Responsibility | Decision |
| --- | --- | --- |
| Lines 167–244, preferences keys and `load/saveLanguageMode`, experience mode, common players | Android preferences and normalization/default behavior | Good small extraction candidate; preserve keys and write behavior. |
| Lines 259–326, recovery raw storage, preview, archive I/O | Three adjacent but different responsibilities | Separate archive storage from emergency recovery. Do not introduce one generic persistence manager. |
| Lines 341–417, role lists, script selection and labels | Presentation role catalog and script presentation helpers | Optional later move; preserve the distinction from the validated ruleset catalog. |
| Lines 421–749, remembered state, session publication, precompute/A4 effects | Composition lifetime and session bridges | Keep in root initially. Do not centralize all mutable state. |
| Lines 759–1070, checkpoint/history/observation/shadow operations | Existing typed owners plus App-specific publication, diagnostics and durability ordering | Preserve current ordering. Small read-only preparation may later be isolated, not the entire block. |
| `activeGameRecoverySnapshot` at 1117; `applyValidatedRecoveryPlan` at 1247; lifecycle at 1420 | Capture, application and persistence scheduling | Current codecs/planner already exist. Application is not a missing parser. R7 is a separate high-risk decision. |
| `startUndercoverGame`, `startTroubleBrewingGame`, `startClocktowerGame` | Deal/setup/session entry | Preserve dedicated prewarm/reveal coordinators and setup authority. |
| Judge call at 2098; `onConfirmNewDemon` 2403, `onConfirmDay` 2831, `onConfirmNight` 2965 | Cross-owner transaction application | Keep R3 NO-GO. Do not extract all callbacks as one object. |

`ClocktowerGameSession` remains canonical for its mechanical state, revisions and semantic chronology. App also owns UI drafts, presentation mirrors and route/continuation state. Those are not all equivalent to canonical session fields. A refactor must classify each value before moving it, rather than copying every remembered value into a new session model.

### Host

| Region / anchor | Responsibility | Decision |
| --- | --- | --- |
| Lines 198–504 | Local information lifecycle, interaction registration and effective-night queries | Preserve publication gates and interaction-local registration semantics. |
| Effects at 638, 670, 720, 745 | Precompute demand/start and setup recommendation loading/application | Keep independent keys, cancellation and mount locations. |
| `recommendedNumberOptions` 883 through pair/manual preparation at 1663 | Candidate adapters, presentation and policy inputs | Mixed responsibility; use narrow contracts, not one “information context.” |
| `dynamicStorytellerState` 1019 | Converts cards/history/usage/protection into dynamic recommendation state | Promising read-only projection boundary; first freeze exact input/output behavior. |
| `informationDecisionPublicationAllowed` 1792 and `recordReliablePrivateInformation` 1798 | Revision-aware publication and observation routing | Keep authoritative confirmation and publication ordering outside renderers. |
| Lines 1902–2003 plus first-night minion/demon registry entries from 2213 | First-night evil-information content | Bounded presentation candidate after factoring facts from localized rendering. |
| `nightSteps` at 2202 and registry closures | Canonical-flow materialization | Keep registry identity/order ownership. Do not move the closure list into a huge builder receiving all Host state. |
| Night advance at 3965–4027 and active screen wiring | Confirmation, refreshed-flow navigation and auto advance | Preserve dynamic step expansion and exactly-once confirmation. |

## 5. Proposed sequence and acceptance gates

Names below are proposed responsibilities, not instructions to create every listed file. Each slice needs a final symbol allowlist and producer/consumer map before editing.

### A1 — App preferences storage: recommended first

Architecture pre-flight:

- Current owner: App's private Context extensions and preferences constants.
- Proposed responsibility: `AppPreferencesStore` under `persistence`, owning language, experience mode and common-player storage only.
- Authoritative state owners: preferences for persisted settings; existing App Compose state for the current UI values.
- Narrow input/output seam: typed load/save operations for `LanguageMode`, `StorytellerExperienceMode` and `List<String>`; no game session, navigation or mutable-state parameters.
- Decision: extract storage; keep localized Context creation and UI state lifetime in App.
- Reason: settings changes should not require reading game transactions; this removes an independent Android I/O responsibility.

Preserve absent versus stored-empty common-player semantics, normalization, fallback, preference names and asynchronous `apply()` writes. Reuse `resolveInitialCommonPlayers`; do not duplicate its policy. Do not move recovery writes into this store because those use synchronous durability semantics.

Evidence: existing `CommonPlayersDefaultPolicyTest`, focused storage behavior characterization where not already covered, compilation and exact diff. A broad source-shape test or new generic key-value framework is unnecessary. Stop if the proposed store starts receiving game state or route callbacks.

### A2 — Archive storage: separate from A1

Extract the I/O responsibility around `loadGameHistory` / `archiveGame` into an archive-specific storage adapter. Continue using `GameArchiveJsonCodec`; return the existing archive review models. Keep restart orchestration, setup-completion bookkeeping and navigation in App.

Preserve newest-first order, capacity 20, current timestamp/id behavior, empty-card handling, malformed-entry behavior, and synchronous `commit()`. Current code rereads history after writing and does not propagate the commit result as an error: do not quietly “improve” that behavior in a move. A desired error-handling change needs its own contract.

Keep role lookup as a narrow dependency rather than exposing the whole presentation catalog. Evidence: `GameArchiveJsonCodecTest`, `GameArchiveLegacyRejectionTest`, plus focused adapter tests for ordering/capacity/write behavior. Codec tests alone do not prove storage semantics.

### H1 — First-night evil-information presentation: conditional GO

Architecture pre-flight:

- Current owner: Host's minion/demon content preparation and corresponding registry entries.
- Proposed responsibility: a first-night evil-information presentation module, not a rules or setup-recommendation service.
- Authoritative state owners: current card/session projection, existing bluff-selection/presentation functions, and canonical night flow.
- Narrow input/output seam: stable seat/role presentation facts and existing `DemonBluffPresentationResolution` into prepared minion/demon content; materialization consumes that content under existing registry identities.
- Decision: extract only if content preparation and its consumers move together with a small cohesive boundary.
- Reason: first-night wording/reveal presentation can then change without opening pair recommendations, day actions or recovery.

Keep the existing seven-player eligibility behavior, absent actor placeholders, seat labels, bilingual text and Pending/Invalid/Ready bluff handling. Do not recompute bluff legality or choose new bluffs in the renderer. Preserve any deliberate distinction between display metadata, `tellPlayer` and permission to reveal; a presentation refactor must not infer that one implies the other.

Evidence: `ClocktowerDemonBluffPresentationTest`, `ClocktowerProductionFirstNightFlowTest`, `ClocktowerNightStepMaterializerRegistryTest`, plus typed presentation tests for 5/6/7-player boundaries, missing actors and all bluff states. These existing tests do not by themselves prove every new presentation branch; add only the missing contract coverage. Check fresh/restored and beginner/experienced paths.

Stop if the proposed API needs setup providers, mutable registration maps, telemetry writers, session mutation or a large bundle of preformatted strings passed from Host. That would relocate the rendering block without reducing the preparation burden.

### H2 — Dynamic recommendation-state projection: contract audit before GO

`dynamicStorytellerState()` currently builds `DynamicGameState` from cards/script/seed/poison, phase/round, spent abilities, protected target, information-history pressure and registration history. It is called for the current dynamic state and again by registration, Mayor and Demon-successor option paths (Host lines 1071, 1105, 1170 and 1203).

A pure UI-to-recommendation projector can be a useful ownership boundary. It should return the existing `DynamicGameState`, own no mutable state, and call the existing balance evaluator. All production callers must inherit the same projection. Inputs should be cohesive facts, not the whole Host or a bag of arbitrary getters.

However, current history derivation reads player names and localized event titles (`registration` / `登记`; misinformation streak separately checks `misleading` / `误导`). This is semantic debt. Moving it must first preserve the exact behavior; replacing it with typed event facts would be a separate behavior/data-contract change, not “cleanup.” Keep candidate weights, metadata, seeds, ordering and style adjustment unchanged.

Before implementation, characterize equality of the complete output across representative phases, spent abilities, protection/poison, empty/history-bearing inputs and both title languages. `GameBalanceEvaluatorTest` validates the lower evaluator, not this entire Host adapter. Preserve existing eager/lazy invocation timing; do not add caching in the extraction slice.

Expected benefit: future history-to-recommendation adapter changes become locally testable. Expected limitation: the Judge parameter count may not fall. Stop if the new module absorbs ranking, publication, registration mutation or both setup and runtime lifecycles.

### A3 — Presentation catalog: optional, modest return

The role lists and `clocktowerRolesForScript` are consumed by Host, first-night request preparation, role localization, setup recommendation UI, the domain role adapter and recovery. Their fan-out is broader than a simple private helper move.

A presentation-catalog module can remove unrelated role text from App. Preserve names, membership, ordering and lookup behavior exactly, including No Greater Joy. Keep validated ruleset authority in `BuiltInClocktowerRulesetCatalog`; do not merge the two sources by inventing fallback rules or regeneration behavior. Move cohesive data plus access functions, keeping implementation lists private and retaining only the API consumers actually need.

This is a modest isolation benefit, not a prerequisite for H1/H2 or a reason to reopen S9. Verify both scripts, localization, archive lookup and recovery role resolution. Do not widen every private helper merely to make the move compile.

### Deferred — pair preparation and Recovery R7

The pair-information region is large (recommended pair options at 1233, unreliable options at 1457, legal manual options at 1638), but it combines natural candidates, interaction registration, pressure metadata, ranking and display propositions. Existing `PairInformationLegalDomain`, `ClocktowerPairManualAuthority` and the information builder already own important contracts. A single new pair controller would risk becoming a second legality owner.

Revisit only with a complete matrix of reliable/unreliable, manual/automatic, all three pair abilities, Spy/Recluse, no-target results and precompute ready/fallback paths. Start with pure projection after legal candidates are established, not a new candidate-generation policy. The current audit does not approve that extraction.

Recovery R7 likewise needs a separate design. Preserve `prepare -> reject or crossSessionBoundary -> apply`, then safe route re-entry. Preserve `persist success -> durability release -> A4 rebuild`. Do not reduce write frequency, split replay durability or move transient drafts into persistence to shrink App. Snapshot capture already returns a typed `RecoverySnapshot`; another large capture DTO would add little value.

## 6. What must remain unchanged

- Session mechanical/revision/chronology authority and PlayerCard projection ordering.
- Draft versus confirmed selections; confirmation-specific revision increments.
- Public-alive preflight before mutation; Dawn retry/idempotency and succession/Klutz/Ravenkeeper continuations.
- Interaction-relative effective-night state, stable seat identity and canonical registry order.
- Information revision checks, registration application, confirmation, telemetry, observation/history publication and reveal order.
- Compose `remember` keys, mount lifetime, cancellation/stale-result protection and lazy candidate evaluation.
- Current-only Recovery compatibility/age behavior; archive remains a separate product surface.
- SDE V1, legal domains, candidate IDs/order, replay capture and diagnostic durability.

Do not introduce `AppContext`, `HostState`, `HostActions`, generic `Manager`/`Utils`, or a new ViewModel merely to hide the existing dependency list. No wholesale private-to-internal expansion and no feature behavior changes bundled with file moves.

## 7. Validation and stopping criteria

For each approved slice:

1. Record owner, typed seam, complete producers/consumers, state lifetime and forbidden scope before production edits.
2. Identify existing tests; add characterization only where a durable contract is uncovered. No manufactured RED for mechanical movement.
3. Review the complete diff and repeat reference searches. Separate deleted code from moved code and genuinely new behavior.
4. Run focused tests/compile evidence, T1 at a logical checkpoint, and applicable T2/T3. Use full Android tests/build and required CI/R2 before merge under `TESTING_STRATEGY.md`.
5. Check `.github/workflows/r2-write-probe.yml` for ownership guards. Keep the coarse prohibition against reclaiming extracted responsibilities; do not preserve obsolete declarations for assertions.
6. If moving recovery capture, assess `BuiltInClocktowerRulesetCatalogTest`'s source assertion that validation remains spelled inside App. Replace/narrow it only when the same shared-catalog contract is proved at the new owner; it is not permission to duplicate validation.

Existing transaction evidence to preserve for any later high-risk work includes `NightCheckpointHostTransactionTest`, `NightTransactionHostIntegrationSmokeTest`, `NightDawnRestoreRetryConvergenceAcceptanceTest` and `ClocktowerDynamicNightAdvanceTest`. Recovery evidence includes planner, application coordinator, write-gate, lifecycle and No Greater Joy tests. Pure lower-level GREEN tests alone cannot prove arbitrary Compose relocation is safe.

Measure success by the files and owners needed to change one responsibility: a preference change should become storage + owning tests; first-night presentation should become its presentation module + existing flow contract; a projection change should become projector + evaluator contracts. Compare this expected reading scope after each slice. If all the same Host/App details are still needed plus a new wrapper, stop.

Do not promise either root will become small. A1/A2/A3 remove modest independent responsibilities; H1/H2 are bounded opportunities with explicit gates. The deep orchestration may reasonably remain large. Re-audit after the first useful App and Host slices rather than committing to a long sequence of increasingly speculative abstractions.

## 8. Audit delivery

Only this document was added. Production, tests, resources, workflows and the current roadmap were not changed. No Android tests were run for this documentation-only audit, and no new runtime/performance claims are made. Implementation remains a separate user-selected step.
