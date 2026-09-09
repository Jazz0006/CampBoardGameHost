# D6.2n — Live information preparation ownership audit

> Date: 2026-09-09 Australia/Sydney
> Status: HISTORICAL READ-ONLY AUDIT COMPLETE; D6.2o implemented and validated at `ad16ccc694e687ab10e62677cf0369fa26e9c2fd`.
> Follow-up: `D6_2O_NUMERIC_OPTION_PREPARATION_PROGRESS_2026-09-09.md`.
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Audited head: `c283b373adc4e33933c9a2f36f54aa491f3c6043`.
> Latest validated production checkpoint: `4c16f4c09ae7df693a3b16d6910ab026373e05cd`.
> main: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.

## Decision

Start R1 with the numeric information option adapter, not by moving the Chef, Empath or Chambermaid materializer closures wholesale.

The six numeric recommendation call sites share one coherent responsibility: recover the previous displayed number, construct an `UnreliableNumberContext`, ask the existing recommendation owner for ranked values, and project those values into `ClocktowerDisplayOption`. The history parser and result projection are pure and have stable typed inputs. They can leave Host without moving Compose state, rules authority, registration decisions, telemetry or information publication.

The role materializer closures are not yet safe extraction units. Chef and Empath include Spy/Recluse registration alternatives; Chambermaid captures current two-player selection; all of them also capture localized text, seats and current actor lookup. Moving these closures now would require a broad context or many arbitrary callbacks. D6.2o first removes their dependency on Host-local numeric presentation implementation. A later slice can then introduce a cohesive numeric step input per role family.

## Post-cleanup measurements

| Surface | Current size / count |
|---|---:|
| `ClocktowerHostScreen.kt` | 4545 lines / 268260 bytes |
| `ClocktowerNightStepUi.kt` | 868 lines / 45697 bytes |
| `CampBoardGameHostApp.kt` | 4142 lines / 233013 bytes |
| Judge parameters / callbacks / providers / MutableState | 87 / 34 / 3 / 5 |
| NightStep parameters / callbacks | 48 / 13 |

R0 removed 963 production Kotlin lines across D6.2k–m and aligned R2 with the active UI. The remaining Host size is live composition and preparation; deletion alone is no longer the main route.

## Current numeric information topology

`recommendedNumberOptions` has six call sites:

| Phase | Role | Additional semantics retained at caller |
|---|---|---|
| First night | Clockmaker | fixed truth and maximum distance |
| First night | Chef | typed proposition plus Spy/Recluse registration alternatives |
| First night | Empath | typed neighbor seats plus Spy/Recluse registration alternatives |
| First night | Chambermaid | selected pair identity and typed proposition |
| Other night | Empath | cursor-relative alive neighbors and prior shown value |
| Other night | Chambermaid | current selected pair and typed proposition |

Two Empath step preparations also read the previous shown number directly for `ClocktowerNightStepUi.previousShownNumber`. They must call the same extracted history parser used by recommendation context preparation.

The current recommendation call is not an information commit. `ClocktowerRecommendationCoordinator.recommendNumber` delegates to the stateless night recommendation module and returns `List<UnreliableNumberRecommendation>`. Durable selection still flows later through structured information preparation, explicit confirmation and publication. D6.2o keeps the coordinator invocation in Host so this slice cannot accidentally move lifecycle authority.

## Ownership split

| Responsibility | Owner after D6.2o |
|---|---|
| true values, neighbor seats, registration alternatives | existing rules/Host preparation |
| recommendation ranking | `ClocktowerRecommendationCoordinator` and recommendation module |
| previous unreliable number parsing | new numeric option preparation module |
| recommendation-to-display projection | new numeric option preparation module |
| role step assembly and materializer order | Host + `ClocktowerNightStepMaterializerRegistry` |
| AUTO/ASSISTED/MANUAL decision lifecycle | existing structured preparation and NightStep UI |
| telemetry, observation draft, publication/history | existing Host/NightStep confirmation path |

The new module owns no mutable state, coroutine, Compose API, session, roster, registration map or persistence service.

## D6.2o exact implementation contract

Production allowlist:

- add `app/src/main/java/com/codex/campboardgamehost/ClocktowerNumericInformationOptionPreparation.kt`;
- edit `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt`.

Test allowlist:

- add `app/src/test/java/com/codex/campboardgamehost/ClocktowerNumericInformationOptionPreparationTest.kt`.

Implementation:

1. Add a pure history function accepting events, title and actor name. Preserve reverse search, `UnreliableInformation` filtering, actor membership, localized title prefix matching, Chinese/English separator parsing, first digit extraction and null fallback.
2. Add a pure projector accepting ranked `UnreliableNumberRecommendation` values and presentation inputs. Preserve labels, high-pressure suffix, `Number` display kind, proposition mapping, style, truthful flag, clamped misinformation pressure, balanced default, score rule IDs and warning IDs.
3. Keep `UnreliableNumberContext` construction and `recommendationCoordinator.recommendNumber` invocation in Host. Replace the Host-local mapping body with the pure projector.
4. Replace both direct Empath history reads with the same extracted history function.
5. Do not change any of the six materializer call-site arguments, `ClocktowerInformationStepBuilder`, structured decision adapters, registration alternatives, recommendation ordering, telemetry or publication.
6. Remove only imports directly orphaned by the move.

The production diff must be behavior-preserving. Add typed tests because the extracted pure functions become a stable adapter contract: cover localized history formats and filtering; cover every projected field and pressure clamping. Do not add source-string tests.

## Validation

- exact three-file allowlist and semantic diff review;
- `git diff --check`;
- focused new adapter test through the available remote Android gate;
- ordinary Android FAST + compile and R2;
- full CI is not required unless implementation crosses the frozen boundary or modifies workflow/semantics.

The immediately preceding D6.2l checkpoint passed full Android JVM tests, debug APK, ASP and Real Clingo. D6.2m passed FAST and R2. No local Android GREEN is expected in this workspace because Android SDK/Gradle bootstrap remains unavailable.

## Stop conditions

Stop and re-audit if the implementation needs to:

- pass cards, registration maps or the whole Judge/Host context into the new module;
- invoke `selectInformation`, record telemetry, create observation drafts or publish information;
- alter candidate values/order, stable IDs, recommendation styles or history matching;
- change any materializer entry order, step lifetime, remember key or callback;
- widen unrelated visibility or introduce a general `Context`, `State`, `Actions`, `Utils` or `Manager` owner.

## Expected outcome and next slice

D6.2o is intentionally a foundation slice. Host should lose roughly 25–45 net lines after retaining short orchestration calls; repository production lines may stay similar because the responsibility moves into a tested file. The meaningful result is that numeric display projection can be understood and changed without loading the full Host.

After D6.2o, re-evaluate a numeric role-step input for Clockmaker, Chef, Empath and Chambermaid. Keep registration-aware Chef/Empath preparation at the caller until it has a narrow typed result. A materializer extraction is accepted only if it consumes cohesive prepared values rather than the current broad Host scope.
