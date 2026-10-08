# GSP-R1 / App–Host Post-Split Re-audit — 2026-10-08

> Role: CURRENT RE-AUDIT / GSP-R1 IMPLEMENTATION PREFLIGHT
>
> Verified live `main`: `009efe2a29718f2cb83918ca83dea53a6fadbd13` (local and origin synchronized, clean).
>
> Previous authority: `GSP_POST_RES5_REENTRY_AUDIT_2026-10-08.md` at `c2bbc70`.
>
> Change reviewed: first and second App/Host extraction waves, merged PRs #244, #245, #246. This audit supersedes the *source-state assessment* of the previous GSP-R0 audit while retaining its general architecture goals.

## 1. Verdict

**GSP-R1 remains the correct next executable recommendation task.** None of the three App/Host splits has materialized the canonical current-game action/observation history into `StorytellerProviderGameContextV1`. The split changed presentation/Host organization, not provider data completeness or canonical history ownership.

Do **not** re-run old GSP-2B3/2C/2D, restore `StorytellerPolicy*`, add local ranking or merge UI display-history strings into a provider contract.

The new seams reduce Host closure size and clarify presentation ownership. They do not yet establish complete physical separation of UI and domain metadata, nor can a green test suite alone prove all fresh/restored UI lifecycles behave identically.

## 2. Exact change and validation

Live main advanced three commits since GSP-R0:

| PR | Merge/main commit | Changes | Verified PR acceptance |
| --- | --- | --- | --- |
| #244 | `e351375f0b45a31ea932fd6409d417696ca25c24` | H-R registration result presentation; H-M recurring Empath/FT materializers; A-C role presentation catalog; A-S non-self-kill demon succession rule reuse | CI #3837, R2 #3512; Android full, ASP, Clingo, boundary GREEN |
| #245 | `5dd5b374bc5fffb37b0558de04853188411640d1` | H-P localized information event-history payload | CI #3839, R2 #3513; all applicable gates GREEN |
| #246 | `009efe2a29718f2cb83918ca83dea53a6fadbd13` | H-I `ClocktowerRegistrationInteractionState` (UI-local Spy/Recluse keys, witnesses, recording markers) | CI #3841, R2 #3514; all applicable gates GREEN |

Actual latest source lengths (physical lines):

- `CampBoardGameHostApp.kt`: 3,528, down 59 from the pre-split 3,587;
- `ClocktowerHostScreen.kt`: 2,740, down 213 from 2,953;
- `ClocktowerNightStepUi.kt`: still 1,169, not a new megafile;
- new files: `ClocktowerRegistrationResultPresentation.kt` 108; `ClocktowerRecurringInformationStepMaterializers.kt` 99; `ClocktowerPresentationRoleCatalog.kt` 81; `ClocktowerInformationHistoryPayload.kt` 57; `ClocktowerRegistrationInteractionState.kt` 78.

These are structural size observations, not gameplay correctness measurements. The new files add ~423 lines rather than reducing total project lines by the headline 272 lines; ownership/convergence is the actual benefit.

## 3. Updated boundary map and retention decisions

| New seam | What it owns | What it must never own |
| --- | --- | --- |
| `ClocktowerRegistrationResultPresentation` | already-legal numeric, FT yes/no and role-reveal display options; de-duplication preserving witness semantics | rules legal domain, choice policy, canonical registration commits |
| `ClocktowerRecurringInformationStepMaterializers` | Empath / Fortune Teller shared first/other-night presentation entries; lazy candidate callbacks | phase/flow ordering, history, recommendation policy |
| `ClocktowerPresentationRoleCatalog` | role labels, script display-membership and lookup previously in App | authoritative script ruleset or new mutable game state |
| `ClocktowerInformationHistoryPayload` | localized `ClocktowerEvent` text/associated names for already-authorized reveal | canonical semantic observation, provider chronology or recommendation episode |
| `ClocktowerRegistrationInteractionState` | Compose-local Spy/Recluse selection and idempotence maps | durable registration truth, game/session state, cross-game memory |
| App `promoteScarletWomanIfNeeded` | pre-death-count adapter and authorized successor application | independent duplicate threshold rule; `DemonSuccessionSemantics` now provides that decision |

The production caller audit confirms Empath/FT entries are used by **both** first- and other-night registries; H-R is called from Host's three result-first presentation paths; H-P only feeds `onRecordEvent` under `performClocktowerPlayerRevealHandoff`; H-I is instantiated as an unkeyed `remember` at the original Host state-owner location. Game rules/transaction application remain in the existing owners.

The relevant negative evidence remains:

- `StorytellerProviderGameContextV1` still contains only `players` and `priorDecisions`;
- `StorytellerProviderGameContextBuilderV1` still reads only player overrides plus `DecisionHistoryArchive`;
- no production call site currently consumes the request builder as a recommendation provider;
- `ClocktowerGameSession.updateStorytellerPlayerContext` still has no production editing call site;
- `recordCompletedGameSignature` still has no production call site.

The new H-P human review event is **not** a new canonical chronological owner. It uses localized strings and seat-number extraction, so importing it into the provider context would lose typed epistemic semantics and can manufacture false historical certainty.

## 4. Important architecture findings

### 4.1 GSP-R1 still lacks typed complete chronology

`ClocktowerSessionState` already owns:

- `ActionFactTimeline`: mechanics including poison/protect/attack/death/role change with a global timeline;
- `EpistemicObservationLog`: typed information actually revealed/established;
- `DecisionHistoryArchive`: `APPLIED` effective decisions after correction filtering, selected via `DecisionHistoryRepository.project()`.

But the neutral provider context cannot read the first two. An independent prompt or stateless model invocation would miss the exact Drunk/Poison information narrative.

### 4.2 Three history identity regimes cannot be silently collapsed

1. `GLOBAL_V1` action facts and observations have authoritative `TimelinePoint.globalSequence`; use only committed **pre-decision** entries.
2. `LEGACY_LOCAL` recovered observations lack global cross-action chronology; do not invent global sequence or claim full coverage. Expose explicit availability/coverage, or defer historical materialization.
3. `DecisionHistoryArchive` prior decisions have revision identity and an applied/correction projection, **not** the same global sequence. Keep this a separate typed decision-history view unless an exact causal bridge is proved.

R1 must bind a decision cutoff to the exact host/session snapshot and reject future records. A revision-only comparison is not sufficient proof of action/observation pre-decision chronology for replay benchmarks. Never infer unspecified order from UI event text.

### 4.3 History *capture coverage* must be audited before claiming complete game context

R1 should map the production paths that commit action facts, epistemic observations, role registrations and actual player-information revelations. The surviving Host `recordReliablePrivateInformation` has a role-specific legacy proposition fallback; some human-viewable actions and public claims are not necessarily typed semantic observations. If coverage is incomplete, say which facts are unavailable rather than claiming the model received a complete game narrative.

`ClocktowerInformationHistoryPayload` is a display/archive text projector only. It is not a substitute for `EpistemicObservationLog`, nor should localized/regex-parsed text be converted into supposedly canonical provider facts.

### 4.4 App/domain role catalog remains directionally coupled

`clocktower/domain/RoleCatalogAdapter.kt` still imports `ClocktowerRole`, `ClocktowerScript`, `ClocktowerTeam` and `clocktowerRolesForScript` from the App-root presentation world. Moving the list into `ClocktowerPresentationRoleCatalog.kt` improved App readability but did **not** eliminate this dependency inversion. This is a legitimate future narrow domain-native catalog / script-metadata authority audit, especially before expanding multi-script support. It is **independent** of GSP-R1 and should not turn R1 into another App/Host decomposition campaign.

### 4.5 UI interaction lifetime is still a test boundary

`ClocktowerRegistrationInteractionState` intentionally preserves the original six unkeyed `remember` maps, phase/round-derived keys and write-before-record order. Existing typed state tests and full CI protect many invariants, but a targeted fresh-game / restore / re-entry interaction test is still useful if this state owner changes again. Do not merge it with the durable canonical session to eliminate apparent duplication.

### 4.6 PR #245 review documentation debt

PR #245 merged with one unresolved P2 inline review thread concerning stale campaign wording: the document reported H-P completed while still describing H-P/H-I as pending candidates. This is a **documentation-status inconsistency**, not a reported runtime defect. PR #246 then completed H-I as well. Update the active audit/status accordingly; avoid re-planning already-completed extraction.

## 5. Revised executable order — retain GSP-R1; tighten its preflight

**GSP-R1A — canonical capture and prefix-coverage audit (NEXT).**

- Enumerate Host/session producers for `ActionFactTimeline` and `EpistemicObservationLog`, including FirstNight/OtherNight/Day, Drunk/Poison, Spy/Recluse registration, private/public reveal, role change and fresh/restored modes.
- Distinguish facts actually persisted from UI-only text and derived display projection.
- Select a neutral cutoff/coverage/unknown contract; identify historical `GLOBAL_V1` and `LEGACY_LOCAL` behavior and already available tests.
- Define how `DecisionHistoryArchive` remains a separate applied-decision view, never a fabricated globally sequenced event.

**GSP-R1B — neutral immutable action/observation materializer.**

- Extend `StorytellerProviderGameContextV1` with bounded neutral typed prefix/coverage, without importing `clocktower.recommendation.*`, UI `ClocktowerEvent`, or SDE-specific prefix types.
- Build from Host-owned canonical session values, validate exact game, revision and prefix; no second mutable owner.
- Preserve legal candidate domain, provider no-commit authority and Manual fallback.
- Test fresh/recovery equivalence, predecision cutoff, chronological order, unknown/incomplete coverage, poisoning/information continuity, defensive collection copies and same-decision repeatability.

**GSP-R1C — provider context acceptance and offline fixtures.**

- Freeze representative Drunk/Poison and mid-game multi-decision contexts, comparing typed exported facts with the actual canonical committed prefix.
- Confirm replay/provenance without dependence on LLM conversation memory.
- Use risk-based Android T0/T1/T2 and full T4 `[full-ci]` / GitHub CI/R2 before merge.

After R1: R2 player-context edit/profile -> R3 cross-game/diversity -> R4 prompt/response materializer -> GSP-3A manual blind benchmark. Do not start API integration merely because R1 data exists.

## 6. Work intentionally NOT authorized in this audit

- No production changes, test-code changes, new policy, recommender, selector or network API.
- No universal mutable `HostContext`, `HostActions` or giant Night/Day transaction executor.
- No assumption that localized `ClocktowerEvent` history is equivalent to typed canonical epistemic observations.
- No automatic global ordering for legacy Recovery histories.
- No unreviewed changes to RoleCatalogAdapter/script identity.
- No claim of new executable regression results: #244–246 full CI/R2 acceptance is historical evidence, this is docs-only.

## 7. Handoff

Current source authority: live `main` and this re-audit. Old GSP-R0 remains valid for its RES-5-era facts, but is superseded where code-shape/fan-out assumptions differ.

Next implementation must begin at **GSP-R1A**, not R2, another App/Host decomposition slice, or API materialization.
