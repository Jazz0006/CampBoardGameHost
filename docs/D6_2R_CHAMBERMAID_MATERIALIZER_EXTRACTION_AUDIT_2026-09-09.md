# D6.2r — Chambermaid materializer extraction audit

> Date: 2026-09-09 Australia/Sydney
> Status: HISTORICAL AUDIT COMPLETE; D6.2s IMPLEMENTED / VALIDATED.
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Audited head: `48eb0d10fcb157a62a5f1613a9906333ab5e684f`.
> Latest validated production checkpoint: `a5f1654fe9adf56ce7fc3cbc07cd039972715a55`.

## Decision

Extract the stable Chambermaid step assembly, but keep recommendation invocation and its lifecycle dependencies in Host.

The first-night and other-night Chambermaid registry entries are behaviorally identical. Their step shape can be owned by a narrow materializer consuming prepared immutable content, proposition and display-option provider. The same registry `Entry` can then be inserted into both phase registries because the registry derives the phase-specific interaction ID when it is constructed.

Do not move `recommendedNumberOptions` or package its captured context. It owns recommendation-coordinator invocation, prior-history lookup, localized style labels and pressure presentation shared by several numeric roles; moving it with Chambermaid would create the broad role context rejected in D6.2p.

## Actual dependency inventory

| Current closure dependency | Classification | D6.2s ownership |
|---|---|---|
| `informationStepBuilder` | existing narrow step assembly owner | explicit materializer collaborator |
| localized role/explanation/footer/instruction strings | immutable Chambermaid UI content | one typed content value prepared in Host |
| `chambermaidResult` | immutable resolved truth text | explicit materializer input |
| `chambermaidPresentation` | D6.2q immutable seat/proposition owner | explicit materializer input |
| truthful display proposition | derived from current actor seat + prepared presentation | prepare in Host and pass as value |
| unreliable display options | lazy recommendation/telemetry boundary | pass one role-specific provider; implementation remains in Host |

No Compose state, session, Recovery writer, registration map, navigation callback, publication callback or general night context is required by the extracted assembler.

## Why one callback is acceptable

The callback is not a disguised action bag. It represents one cohesive output already expected by `ClocktowerInformationStepBuilder`: unreliable numeric display options for the current Chambermaid actor. Host remains the only owner of recommendation invocation and all of its shared context. The materializer neither invokes arbitrary Host actions nor learns how options are ranked.

Eagerly precomputing the options is not allowed because the builder deliberately calls `displayOptions` only for an unreliable actor. Eager evaluation could change recommendation audit/lifecycle behavior.

## D6.2s implementation contract

Allowlist:

- new `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializer.kt`;
- `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt`;
- new `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidStepMaterializerTest.kt`.

Implementation:

1. Add an immutable localized Chambermaid step-content value and a narrow materializer function/extension around `ClocktowerInformationStepBuilder.build`.
2. Inputs are limited to the builder, content, result, prepared `ClocktowerChambermaidSelectionPresentation`, truthful display proposition and one lazy `(PlayerCard) -> List<ClocktowerDisplayOption>` provider.
3. Preserve role names, `ClocktowerNightAction.Chambermaid`, number display semantics, secondary seat text, footer, instruction and all builder defaults.
4. Construct one Chambermaid registry `Entry` in Host and reuse that exact entry in both first-night and other-night entry lists.
5. Host continues to create the truthful proposition from the current actor seat and continues to invoke `recommendedNumberOptions` with max 2, pressure cost 1 and the existing proposition factory.
6. Keep canonical registry positions unchanged: Chambermaid remains between Empath and Fortune Teller in both phases.

Typed tests must verify reliable step fields/proposition/secondary text, unreliable lazy option forwarding, missing-actor behavior and that a reliable actor does not invoke the provider. Keep the existing registry/order tests; add only a minimal source ownership assertion if no typed test can prove that one entry is reused in both lists.

## Expected result

- remove the two duplicated 30–35-line Chambermaid assembly blocks from Host;
- replace them with one narrow entry construction plus two list references;
- reduce Host by roughly 35–55 net lines while making step-shape behavior callable in typed tests;
- preserve one recommendation lifecycle owner and avoid any new broad context object.

This is a role-specific pilot because Chambermaid has unique ordered two-player presentation semantics. It is not precedent for creating one tiny file per role. After D6.2s, reassess the remaining numeric roles as a family and stop if they require nullable registration fields or role-specific callback bags.

## Validation and stop conditions

Use exact three-file diff review, `git diff --check`, production/test Kotlin compile, FAST and R2. Confirm exactly two Chambermaid list references and unchanged neighboring materializer identities. The D6.2l FULL gate remains sufficient unless workflow, rules, registry order or lifecycle behavior changes.

Stop if implementation requires changing `ClocktowerInformationStepBuilder`, moving `recommendedNumberOptions`, eagerly invoking recommendations, adding Compose/session/Recovery dependencies, or passing more than the one display-option provider.

## Follow-up

D6.2s completed at production/test checkpoint `a692cc722f1e597e747154bf05a2689fee9bed4c`, with the directly affected Empath source assertion corrected at final validated head `b2263cd08bc2ce223598698324bf2b22243c91f2`. Final CI 34299329715 and R2 34299329713 passed. See `docs/D6_2S_CHAMBERMAID_MATERIALIZER_EXTRACTION_PROGRESS_2026-09-09.md`.
