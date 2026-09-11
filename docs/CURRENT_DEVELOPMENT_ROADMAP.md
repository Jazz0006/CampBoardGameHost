# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-11 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / merged
R3 transaction-application viability audit        COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / merged via PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / merged via PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / merged via PR #119
EPI-MQ-0 baseline / ownership re-audit            COMPLETE — old EPI-MQ-1 = MODIFY

UX-MODE-1 Beginner / Experienced Storyteller Mode CURRENT — implementation substantially advanced; device regressions block acceptance
EPI-MQ / Productive Uncertainty                   QUEUED immediately after UX-MODE-1
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign documents are historical evidence, not default execution authority.

## 2. Immediate priority — UX-MODE-1 stabilization

Active implementation branch:

`codex/ux-mode-1`

Draft PR:

`#120 UX-MODE-1 Beginner / Experienced Storyteller mode`

Current active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_DEVICE_REGRESSIONS.md`

The previous UX-MODE design handoff remains a historical/product-design record:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_BEGINNER_EXPERIENCED.md`

The finalized product design still uses one user-facing switch:

```text
熟练模式
允许说书人手动调整系统推荐的线索
```

Fresh installs default to Beginner / Experienced OFF.

Critical invariant:

> **Beginner and Experienced share one rules / legal-candidate / recommendation pipeline. Experience mode changes interaction authority and presentation only.**

Temporary recommendation policy until EPI-MQ:

- both modes consume the same internal `RecommendationStyle.AGGRESSIVE` provider;
- Beginner auto-uses Top-1 for Storyteller-owned strategic choices;
- Experienced exposes recommended/manual legal alternatives;
- player-owned choices remain manual;
- probabilistic automatic rulings use stable semantic decision identity and must not re-roll on recomposition/navigation/restore.

Temporary automatic policies remain:

```text
Spy/Recluse registration:
90% legal special / false registration
10% actual registration

Mayor:
90% redirect to eligible living Townsfolk when one exists
10% Mayor dies
otherwise Mayor dies

non-forced Demon succession:
Baron 4 > Scarlet Woman 3 > Spy 2 > Poisoner 1
```

Rules legality remains authoritative upstream.

## 3. UX-MODE-1 progress already completed

### UX-MODE-1A — product/ownership audit

Complete.

Product semantics, Beginner/Experienced authority split, deterministic-random requirements and temporary aggressive policy are finalized.

### UX-MODE-1B — typed mode / persistence / settings

Complete.

Implemented:

- `StorytellerExperienceMode`;
- unknown/fresh preference defaults to `BEGINNER`;
- typed `StorytellerRecommendationUxPolicy`;
- Beginner automatic execution / Experienced manual alternatives;
- both modes use internal AGGRESSIVE recommendation style;
- compact `中 / EN` language controls;
- Experienced-mode switch;
- app-root persistence/wiring moved away from legacy global automation settings.

Relevant product checkpoint:

`a75b54e771907b5bc54f36e6a51d522b2f1f2087`

cleanup:

`d1afdf526368e672d13488ae7405fe4107dd10cb`

### UX-MODE-1C — temporary automatic Storyteller policy

Core policy complete.

Typed selector contracts cover:

- Spy/Recluse 90/10 registration;
- Mayor 90/10 redirect/death;
- Demon successor 4:3:2:1;
- fixed-point probability mass;
- deterministic semantic decision keys.

RED:

`ed6ed125cae7a0991a8e694784ce64c95be7d1ef`

GREEN:

`e9feafc6a0e0e1d5a771c5c53e3d93b6fb88c03d`

Integration checkpoints:

- Spy/Recluse: `208ce67719d5938d2b7a2578fb5169b4177cb962`
- Mayor + Demon succession: `47e8ddd5c1fe4efaad7d704e1ce9b0b0ac50a4ba`

### UX-MODE-1D — setup recommendation UI cleanup

Complete for the setup recommendation card.

Removed user-facing Gentle/Balanced/Aggressive selection controls/copy from that surface.

RED:

`9b41bbc963f46b3fdbf0c621e4273c3dc46b568f`

product GREEN:

`55c5b0160de504963fc2516aed0ac37f61c6c8c2`

cleanup:

`388334621095a685ecf2137ecb04d56ccaa2af03`

### Red Herring duplicate auto-advance owner

A regression was found during UX-MODE audit: HostScreen and NightStep both attempted to auto-advance Red Herring, risking double navigation / skipped steps.

Tests were changed first to enforce one owner, then NightStep's duplicate effect was removed.

Product fix:

`ad91a3b73f8598ba6219524a3bcfee76694d9c6a`

## 4. Current acceptance blockers from real-device testing

### Blocker A — first-night Minion/Demon/bluff falls back to legacy text Card

Real-device testing shows a remaining UI island in the first-night evil-team flow: Minion/Demon wake and Demon bluff presentation can fall back to the old generic text/Card surface instead of the unified square-table night surface.

The user subsequently restored/tested `main` and reported that the problem appears to remain there as well. Historical blame is no longer the priority; fix the present product behavior.

Current structural risk:

```text
first-night EvilInfo
roleEnName = null
no explicit role-specific NightAction
-> may miss square-table routing
-> generic legacy Card
```

Target:

- existing full-screen square-table shell;
- current actor/wake cue where applicable;
- central wake/bluff instruction;
- existing Previous / Host Tools / Next navigation;
- no normal first-night evil-team fallback to legacy Card.

Create a durable routing/presentation regression contract before production fix. Prefer a typed presentation seam over brittle giant-source tests.

### Blocker B — hard crash after Demon bluff / Next

User-supplied debug bundle:

`storyteller-debug-1789125312141.zip`

Exact exception:

```text
java.lang.IllegalArgumentException:
Selection audit requires the complete candidate pool.
```

Stack:

```text
SelectionAuditRecord.<init>(SelectionDistributionTelemetry.kt:48)
ClocktowerRegistrationUiKt$SpyRegistrationPanel$1$1.invokeSuspend(ClocktowerRegistrationUi.kt:71)
```

The crash is on Android main thread via Compose `LaunchedEffect`.

The bundle records app `0.1.4-fieldtest`, versionCode 5, debug, Android 16 / SDK 36, Xiaomi `2511FPC34G`, but **does not contain a Git SHA**. Do not assert the exact binary came from main or PR #120 solely from the ZIP.

Confirmed architecture bug on `codex/ux-mode-1`:

```text
new automatic selector source:
legal registration domain
-> clocktowerTemporaryRegistrationSelection(...)
-> automaticRuling

old audit source:
recommendations.map { ... }
```

The new automatic selector can produce a valid ruling even when the legacy `recommendations` list is empty, while `SelectionAuditRecord` rejects an empty candidate pool.

`RecluseRegistrationPanel` has the same ownership shape and should be treated as the same bug family.

Correct fix rule:

> **Automatic selection and audit preview must describe the same complete legal candidate domain. Do not merely guard `recommendations.isNotEmpty()` or swallow the exception.**

Required tests-first coverage:

- automatic Spy path is safe with empty legacy recommendations;
- automatic Recluse path is safe with empty legacy recommendations;
- audit receives a non-empty complete candidate representation from the authoritative selector domain;
- committed family matches selected automatic ruling;
- legality and deterministic selection identity remain intact.

This hard crash is the highest-priority next production fix.

## 5. Remaining UX-MODE semantic debt after the blockers

Keep these separate from the crash fix.

### Hidden BALANCED setup default

`ClocktowerHostScreen.kt` still initializes/resets selected setup recommendation style to `RecommendationStyle.BALANCED` in places.

Both modes should use the current policy style (`automaticStorytellerStyle`, temporarily AGGRESSIVE).

### Demon bluff fallback still defaults to BALANCED

`ClocktowerDemonBluffPresentation.kt` still contains a manual/default BALANCED fallback and corresponding tests.

Experienced mode must use the same current provider as Beginner, not a separate hidden Balanced recommendation source.

### Beginner first-night Ready simplification

Automatic setup recommendation application is owned outside the setup recommendation card.

Beginner may hide the recommendation card only after:

```text
recommendation state == Ready
AND
automatic recommendation has actually been applied
```

Do not hide it merely on Ready; loading/error/invalid states remain visible.

Experienced keeps the recommendation/manual surface.

### Remaining public legacy style terminology

Some NightStep/presentation copy still mentions concepts such as automatic mode, balanced option, current style or different pressure.

Neutralize these after behavior stabilizes. Do not mechanically delete internal `RecommendationStyle` enum usage.

### Legacy automation ownership cleanup

Later UX-MODE-1F should remove obsolete `StorytellerAutomationMode` / `fromLegacyMode` ownership once live references are gone.

Do not mix that cleanup into the crash fix.

## 6. Required next execution order

```text
1. Re-query live main, codex/ux-mode-1, PR #120 head/state/checks.

2. Fix Spy/Recluse audit hard crash tests-first.
   - establish smallest pure typed RED
   - make audit consume authoritative complete registration candidate domain
   - focused GREEN
   - exact diff/ownership audit
   - :app:testFast

3. Fix first-night EvilInfo square-table routing tests-first.
   - no normal Minion/Demon/bluff legacy Card island
   - reuse existing square-table shell/navigation

4. Replace hidden BALANCED setup/bluff defaults with current unified policy style.

5. Finish Beginner Ready zero-decision presentation.

6. Neutralize remaining old style terminology and perform legacy mode cleanup.

7. Run acceptance validation / PR CI / R2 / real-device first-night flow.

8. Only after UX-MODE-1 acceptance resume EPI-MQ.
```

PR #120 remains draft and must not be merged without explicit user authorization.

## 7. EPI-MQ direction after UX-MODE-1

EPI-MQ-0 is complete as an architecture/ownership audit.

Authoritative design record:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Resume sequence after UX-MODE acceptance:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility / contradiction / impairment-exposure gates
EPI-MQ-3+   productive-uncertainty metrics and ranking
```

Do not activate A4/ZDD or replace the recommendation provider as part of the initial evaluator extraction.

## 8. Architecture / scope continuity

For current UX-MODE stabilization:

- preserve one rules/legal/recommendation pipeline;
- keep player-owned choices manual;
- keep probability/scoring out of Compose;
- preserve deterministic semantic selection identity;
- repair selector/audit ownership instead of hiding telemetry failures;
- reuse existing square-table UI primitives rather than starting another UI redesign;
- follow `AGENTS.md` architecture pre-flight before substantial protected/core edits;
- follow `docs/TESTING_STRATEGY.md` T0–T4 escalation;
- do not reopen D6 solely because a source file is large;
- do not resume EPI-MQ before UX-MODE acceptance.

## 9. Default reading order for the next development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_DEVICE_REGRESSIONS.md`;
5. live GitHub `main`, `codex/ux-mode-1`, PR #120;
6. current registration selector/audit source and tests.

The older UX-MODE design handoff is background evidence only; do not use it instead of the current device-regression handoff.

## 10. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
