# Clocktower Night Step UI Decomposition Audit

> Status: **PRE-DECOMPOSITION ARCHITECTURE REFERENCE**  
> Date: 2026-09-05 Australia/Sydney  
> Scope: `ClocktowerNightStepUi.kt` and the surrounding Night Step UI cluster  
> Execution state: **Do not mechanically execute the slice order below without a fresh live-state audit.**

## 1. Purpose

This document records the architecture reconnaissance performed before the next Night Step UI decomposition campaign. The goal is not to split a large file by line count. The goal is to reduce the **change context radius** by assigning state, domain preparation, interaction logic, side effects and rendering to stable owners.

The original audit was performed before the latest square-table / inline-wake UI work fully settled. Since then the UI stack has continued to evolve. Therefore the ownership findings and candidate seams remain useful, but exact file boundaries and slice order must be revalidated against live `main` before implementation.

## 2. Core diagnosis

`ClocktowerNightStepCardLocalized(...)` had grown into a broad composition point with a very large parameter/callback surface and at least six mixed responsibility classes:

1. screen/external state wiring;
2. recommendation and selection orchestration;
3. structured information preparation;
4. UI-local interaction state;
5. semantic mapping / player-display projection / telemetry side effects;
6. action rendering and interaction UI.

The architectural problem is therefore **mixed ownership**, not merely file size.

Do not replace the current shape with a `NightStepContext`, `NightStepState`, `Args`, `Environment`, or similar God parameter object containing the same broad dependency surface.

## 3. Target ownership map

| Current responsibility | Preferred owner direction |
|---|---|
| Night-step inputs and external wiring | thin screen/composition/state-holder layer |
| first-night candidate projection | presentation/preparation owner |
| AUTO / ASSISTED recommendation projection | recommendation presentation layer |
| selection audit creation / commit | explicit selection/commit boundary |
| Empath / Chef / Fortune Teller structured information preparation | structured-information presenter |
| Fortune Teller transient UI state | Fortune interaction local owner |
| Pair Manual transient selection state | Pair Manual UI/local state owner |
| Poisoner / Monk / Butler / Demon Kill / similar target interaction rendering | narrow target-interaction components after contracts are stable |
| Mayor redirect / Demon successor Storyteller ruling interaction | ruling interaction owner |
| repeated player-display `step.copy(...)` construction | pure player-display projection owner |
| first-night benchmark coroutine/logging | diagnostics owner |
| card shell / navigation / note / common layout | rendering shell |

The end state should make `ClocktowerNightStepUi.kt` primarily a composition/wiring layer rather than a place where new gameplay-adjacent UI policy naturally accumulates.

## 4. Existing seams that should be preserved

### 4.1 Structured numeric information

`StructuredNumericInformationAdapter.kt` already provides a strong lower seam through `prepareNumericInformationUiModel(...)`. It owns numeric legality/result preparation, recommendation identity, typed numeric propositions, observation drafts and revision context.

Compose should consume prepared typed presentation data rather than reconstructing those semantics.

### 4.2 Structured boolean information

`StructuredBooleanInformationAdapter.kt` similarly owns the legal Yes/No result domain, reliability-aware options, recommendation identity, typed boolean propositions and decision context.

This is a good model for moving semantic preparation out of rendering code.

### 4.3 Pair Manual authority

`ClocktowerPairManualAuthority.kt` establishes `PairInformationLegalDomain` as the selectable-outcome authority. Presentation options are templates; they do not create new legal results. Selection must resolve back through the legal domain so localized UI text never becomes semantic authority.

This boundary must remain authoritative during the Manual UI redesign and decomposition.

### 4.4 Pair recommendation presentation precedent

`ClocktowerPairRecommendationPresentationUi.kt` consumes an already-ranked presentation model rather than generating candidates, ranking options or owning Manual legality. New extracted UI should follow this pattern: **prepared typed model in, user intent/result out**.

### 4.5 Host selection semantics

`ClocktowerHostSelectionSemantics.kt` already contains useful pure two-player selection/revalidation semantics. Do not duplicate these rules inside Compose components.

## 5. Candidate seam A — Pair Manual presentation and local state

This was the strongest first seam in the pre-latest-UI audit.

The Night Step UI locally interpreted pair propositions such as `AnyOf`, `AllOf`, `RoleAt` and `RoleInPlay`, grouped them for display, then also owned transient UI state such as:

- whether Manual pair selection was open;
- selected Manual role;
- selected first seat.

That duplicates semantic interpretation despite the existing Pair Manual authority.

Preferred direction:

```text
PairInformationLegalDomain
-> ClocktowerPairManualAuthority
-> PairManualPresentationModel
-> PairManualSelectionSection / screen
-> selected typed option / intent
-> authority-backed commit
```

The UI layer should not need to understand proposition grammar in order to render role/seat choices.

The presentation model should preserve stable typed option identity and expose only UI-ready role/seat groupings. It must not become a second legality generator.

This seam is especially compatible with the product direction toward a dedicated full-screen Manual selection surface.

## 6. Candidate seam B — Player display projection

The Night Step UI repeatedly constructed player-facing display state through similar `step.copy(...)` blocks, setting fields such as display kind/title/primary/secondary/footer/proposition/truthfulness.

That transformation answers a semantic presentation question: **what exactly will the player see after this Storyteller decision?** It should not be scattered through Compose event handlers.

Preferred direction:

```text
selected semantic result / ClocktowerDisplayOption
-> pure ClocktowerPlayerDisplayProjection
-> sanitized player-display step/model
-> rendering / reveal
```

Benefits:

- one mapping authority;
- easier typed behavior tests;
- less duplicated `step.copy(...)` construction;
- lower risk that recommendation, Manual, numeric and boolean paths create subtly different reveal state.

## 7. Candidate seam C — Structured information presentation preparation

Although numeric/boolean adapters already exist, the Night Step UI still performed preparation such as:

- locating actor seat;
- deriving subject seats;
- obtaining true values from legacy candidates / propositions;
- selecting recommended values;
- branching on role/action before invoking the adapters.

Preferred direction:

```text
NightStepStructuredInformationInput
-> prepareNightStepStructuredInformation(...)
-> StructuredInformationPresentation
-> Compose renderer
```

The renderer should ideally only switch on prepared presentation shapes such as numeric / boolean / none. Role semantics and recommendation preparation belong upstream.

## 8. Candidate seam D — Action interaction rendering

Do **not** start decomposition by moving the entire `when(step.action)` block into another giant composable. That only relocates the broad parameter list.

After narrower state and presentation contracts exist, group interactions by stable interaction shape rather than by making one file for every role.

Candidate families:

- `SingleTargetInteraction` — Poisoner / Monk / Demon Kill / Butler / Ravenkeeper where semantics genuinely align;
- `TwoTargetInteraction` — Fortune Teller / Chambermaid where shared selection lifecycle is real;
- `StorytellerRulingInteraction` — Mayor redirect / Demon successor where Storyteller adjudication is the key interaction shape.

Red Herring should join a shared family only if its lifecycle and authority are actually equivalent.

Avoid premature generalization: shared visual similarity alone is insufficient.

## 9. State ownership / hoisting rule

Do not hoist UI-local transient state into Host/App/session merely to make extraction possible.

Examples of UI-local state from the earlier audit included:

- Pair Manual open/role/first-seat transient state;
- Fortune Teller temporary display-option/limit UI state.

These should move with the smallest cohesive interaction owner.

By contrast, semantic selections that represent current game/session interaction state, such as selected Fortune Teller seats when they must survive the interaction lifecycle, remain with the higher authoritative owner.

The decision is based on **lifetime and authority**, not on a preference for centralized state.

## 10. Diagnostics seam

First-night recommendation/benchmark execution and logging were also present in the Night Step UI via coroutine/effect/logging state. This is clearly not core rendering ownership.

A future diagnostics owner can absorb that work, but it is lower priority than Pair Manual, display projection and structured-information seams because it has less impact on normal feature change context.

## 11. Test/evidence implications

The decomposition should follow the repository's risk-based test-first policy:

```text
classify change
-> identify real owner and durable contract
-> identify existing owning tests
-> establish baseline
-> add durable characterization only for a real uncovered risk
-> refactor / extract
-> rerun focused evidence
-> retire superseded source-shape assertions
```

Existing valuable typed evidence includes coverage for:

- Pair Manual legal-domain projection and exact registration facts;
- structured Fortune Teller legal/truth results and exact pair proposition identity;
- structured Empath legality/recommendation/history/confirmation behavior;
- two-player selection action semantics;
- Host decomposition/helper behavior;
- production information-decision authority wiring.

Known test debt: parts of `StructuredEmpathInformationAdapterTest` have historically inspected `ClocktowerNightStepUi.kt` source shape / local ordering. When a durable typed seam supersedes those assertions, retire or narrow them rather than preserving obsolete local variable names or call ordering.

A pure file extraction or ownership-preserving move does not require a manufactured RED. A genuinely new stable seam should receive a durable typed contract test when that contract is not already protected.

## 12. Pre-latest-UI candidate slice order

The earlier audit proposed:

1. **S1 Pair Manual presentation + local state owner** — low/medium risk, very high ownership value;
2. **S2 Player display projection** — low/medium risk, high value;
3. **S3 Structured information preparation** — medium risk, very high value;
4. **S4 narrowed action interaction renderer** — medium risk, very high value;
5. **S5 recommendation/audit/benchmark wiring cleanup** — medium/high risk, high value.

This sequence is **not locked**. Recent UI work added/changed square-table components, actor cues and supporting presentation files. Re-run the cluster audit before selecting S1.

## 13. Current product/UI decisions that affect decomposition

The architecture must accommodate the current UI direction rather than preserve an older Night presentation shape:

- square/rectangular table surfaces are preferred where they improve phone-screen information density;
- Manual information selection is moving toward a dedicated full-screen workflow;
- recommendation rationale text is not valuable until the consistency/quality algorithm can provide meaningful reasons;
- Fortune Teller-style interaction needs a clean distinction between selecting targets, deterministic result display, and Storyteller-discretion result choice;
- the latest UI-N1 product decision uses an **inline actor/wake cue on the same persistent table**, not a separate WAKE acknowledgement state.

The decomposition must preserve these product choices while keeping gameplay legality upstream.

## 14. Fresh-audit scope before implementation

Before any decomposition implementation, audit live `main` as one **Night Step UI cluster**, including at minimum:

```text
ClocktowerNightStepUi.kt
+ current square-table / HostTableShell presentation owners
+ PR #99/#100-era HostSeatPresentation and actor-cue components
+ Pair recommendation / Pair Manual files
+ StructuredNumericInformationAdapter.kt
+ StructuredBooleanInformationAdapter.kt
+ target-selection / selection-semantics owners
+ relevant typed tests
```

For each candidate extraction record:

- authoritative state owner;
- domain/rules owner;
- side-effect owner;
- rendering owner;
- smallest typed input/result contract;
- affected existing tests;
- source-string tests that become obsolete;
- dependency direction before/after;
- expected change-context-radius reduction.

## 15. Success criteria

A decomposition slice is successful only if it improves ownership, not merely file size.

Expected properties:

- `ClocktowerNightStepUi.kt` loses a coherent responsibility;
- the new owner has a narrow typed contract;
- no God context is introduced;
- no new component depends on the whole Host/screen merely to render a subsection;
- domain legality remains upstream of UI;
- UI-local state remains at the lowest correct owner;
- visibility is not widened merely for extraction;
- typed tests protect the durable seam;
- obsolete source-shape tests are retired when superseded;
- common feature changes require understanding fewer unrelated files/owners.

## 16. Non-goals

This audit does not authorize:

- gameplay/rules semantic changes;
- recommendation algorithm redesign;
- EPI-MQ / Productive Uncertainty implementation;
- broad App/Host decomposition unrelated to the Night Step ownership problem;
- one-file-per-role fragmentation;
- mechanical pursuit of a file-size threshold at the expense of cohesion.
