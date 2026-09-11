# NEXT DEVELOPMENT HANDOFF — UX-MODE-1 Device Regressions + Remaining Convergence

> Date: 2026-09-11 Australia/Sydney  
> Status: **CURRENT — device regressions block UX-MODE-1 acceptance**  
> Program: Storyteller Experience Modes  
> Active branch: `codex/ux-mode-1`  
> Draft PR: `#120 UX-MODE-1 Beginner / Experienced Storyteller mode`  
> PR base: `main` at branch creation `34b5bd84aae5e7216d1d1460d572b0d60b5da02b`  
> PR head when this handoff was written: `7108f707f14d78ca99a5303a84959ba84c15dc92`  
> Supersedes as active execution handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_BEGINNER_EXPERIENCED.md`

## 0. Start here in the next conversation

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main`, `codex/ux-mode-1`, PR #120 head/state/checks;
6. only the source/tests needed for the first blocker below.

Do **not** merge PR #120 without explicit user authorization.

The immediate goal is no longer generic UX-MODE planning. It is:

```text
stabilize two real-device regressions
-> finish remaining UX-MODE semantic cleanup
-> broad acceptance validation
-> only then resume EPI-MQ
```

## 1. UX-MODE-1 product contract remains unchanged

There is one user-facing switch:

```text
熟练模式
允许说书人手动调整系统推荐的线索
```

Fresh/default state is Beginner / Experienced OFF.

Internal typed state:

```kotlin
enum class StorytellerExperienceMode {
    BEGINNER,
    EXPERIENCED,
}
```

Critical invariant:

> Beginner and Experienced use one legal-candidate / rules / recommendation pipeline. Mode changes only interaction authority and presentation.

Temporary recommendation policy until EPI-MQ:

- both modes use current internal `RecommendationStyle.AGGRESSIVE`;
- Beginner auto-uses Top-1 Storyteller-owned recommendations;
- Experienced exposes recommended alternatives/manual legal intervention;
- player-owned choices remain manual;
- no ad-hoc Compose/UI RNG;
- same semantic decision must remain stable across recomposition/navigation/restore.

Temporary automatic Storyteller policies remain:

```text
Spy/Recluse special registration:
90% legal special / false registration
10% actual registration

Mayor:
90% redirect to eligible living Townsfolk when one exists
10% Mayor dies
otherwise Mayor dies

non-forced Demon succession:
Baron 4
Scarlet Woman 3
Spy 2
Poisoner 1
```

Rules legality remains upstream-authoritative.

## 2. Completed implementation before this handoff

### UX-MODE-1A — product/ownership audit

Complete.

Final product behavior, authority split, deterministic-random requirements, and temporary policy are established.

### UX-MODE-1B — typed mode + persistence/settings wiring

Complete.

Implemented:

- `StorytellerExperienceMode`;
- fresh / unknown migration defaults to `BEGINNER`;
- `StorytellerRecommendationUxPolicy.fromExperienceMode`;
- both modes use temporary internal `AGGRESSIVE` style;
- Beginner automatic execution / Experienced manual alternatives;
- compact `中 / EN` settings controls;
- Experienced-mode switch;
- app-root persistence/wiring moved away from the old global automation setting.

Relevant product checkpoint:

`a75b54e771907b5bc54f36e6a51d522b2f1f2087`

cleanup:

`d1afdf526368e672d13488ae7405fe4107dd10cb`

### UX-MODE-1C — temporary automatic policy

Core policy complete.

Typed selector coverage established for:

- Spy/Recluse 90/10 registration;
- Mayor 90/10 redirect/death;
- Demon successor 4:3:2:1;
- fixed-point probability mass;
- deterministic semantic decision identity.

RED:

`ed6ed125cae7a0991a8e694784ce64c95be7d1ef`

GREEN:

`e9feafc6a0e0e1d5a771c5c53e3d93b6fb88c03d`

Integration checkpoints:

- Spy/Recluse: `208ce67719d5938d2b7a2578fb5169b4177cb962`
- Mayor + Demon succession: `47e8ddd5c1fe4efaad7d704e1ce9b0b0ac50a4ba`

Stable night automatic decisions use semantic keys and must not use mutable revision-derived IDs for RNG.

### UX-MODE-1D — setup recommendation UI cleanup

Complete for the setup recommendation card itself.

Removed user-facing Gentle/Balanced/Aggressive selection language and obsolete setup style controls.

RED:

`9b41bbc963f46b3fdbf0c621e4273c3dc46b568f`

product GREEN:

`55c5b0160de504963fc2516aed0ac37f61c6c8c2`

cleanup:

`388334621095a685ecf2137ecb04d56ccaa2af03`

### Red Herring auto-advance ownership bug

During the UX-MODE audit a duplicate owner was discovered:

- `ClocktowerHostScreen.kt` already auto-advanced from durable `redHerring` state;
- `ClocktowerNightStepUi.kt` had acquired a second `LaunchedEffect` owner.

This could double-advance and skip a step.

Tests were changed first to require HostScreen as the sole owner, then the duplicate NightStep effect was removed.

Product fix:

`ad91a3b73f8598ba6219524a3bcfee76694d9c6a`

Current branch cleanup head after one-shot removal:

`7108f707f14d78ca99a5303a84959ba84c15dc92`

## 3. Device blocker A — first-night Demon/Minion presentation falls back to legacy text Card

### Observed behavior

On real device, first-night evil-team flow is not fully using the square-table presentation.

The user specifically observed that around Minion/Demon wake and Demon bluff presentation the UI can fall back to the older text/Card layout instead of the unified square-table night surface.

The user later restored/tested `main` and reported that the problem **appears to remain there as well**. Therefore do not spend more time trying to prove this was introduced by PR #120 before fixing the current product behavior.

Historical provenance is no longer the priority.

### Current code shape already known

First-night Minion/Demon information is represented as an evil-information step rather than an ordinary ability action. It can carry:

```text
displayKind = EvilInfo
roleEnName = null
action = None / no role-specific night action
```

Current square-table routing is strongest for explicit `ClocktowerNightAction` or explicit role-specific presentation surfaces. Evil-team introduction/bluff is therefore a special presentation case and currently can reach the generic legacy Card path.

### Required target behavior

Do not invent a separate redesign.

Reuse the already-established UI-R5/UI-NAV square-table shell:

- full-screen square table;
- current acting/waking player visually identifiable where meaningful;
- wake instruction in the central content;
- unified global Previous / Host Tools / Next affordances;
- Demon bluff information displayed within that same presentation family;
- no return to the old generic text Card for normal first-night evil-team hosting.

### Testing requirement

Before production fix, establish a durable regression contract for first-night `EvilInfo` presentation routing.

Prefer a typed/presentation-owner seam over a brittle giant source-string test. If no typed routing seam exists, establish the smallest stable helper rather than putting more special-case logic into the huge Host source.

Do not broaden this into another whole UI-R5 redesign.

## 4. Device blocker B — crash after Demon bluff / next step

### Real-device evidence

Debug bundle supplied by the user:

`storyteller-debug-1789125312141.zip`

Bundle metadata:

```text
appVersionName = 0.1.4-fieldtest
appVersionCode = 5
buildType = debug
Android SDK = 36
Android release = 16
manufacturer = Xiaomi
model = 2511FPC34G
```

The ZIP does **not** include a Git commit SHA. Do not claim the exact binary provenance is main or PR #120 solely from this bundle.

The user reports that after restoring to `main` the problem appears to remain, but binary/commit provenance of this specific ZIP is not independently proven.

### Exact exception

`crash.txt` contains:

```text
java.lang.IllegalArgumentException:
Selection audit requires the complete candidate pool.
```

Relevant stack:

```text
SelectionAuditRecord.<init>(SelectionDistributionTelemetry.kt:48)
ClocktowerRegistrationUiKt$SpyRegistrationPanel$1$1.invokeSuspend(ClocktowerRegistrationUi.kt:71)
```

The crash occurs on the Android main thread through a Compose `LaunchedEffect` coroutine.

### Confirmed PR #120 ownership mismatch

On `codex/ux-mode-1`, `SpyRegistrationPanel` now obtains its automatic ruling from the new temporary legal registration policy:

```text
complete legal registration roles
-> clocktowerTemporaryRegistrationSelection(...)
-> automaticRuling
```

That path can produce a valid `automaticRuling` independently of the legacy `recommendations` list.

However the audit preview still constructs its candidate pool from:

```kotlin
candidates = recommendations.map { ... }
```

`SelectionAuditRecord` deliberately enforces:

```kotlin
require(candidates.isNotEmpty()) {
    "Selection audit requires the complete candidate pool."
}
```

Therefore the new selector owner and old telemetry owner can disagree:

```text
automaticRuling != null
legacy recommendations == empty
-> SelectionAuditRecord(candidates = empty)
-> IllegalArgumentException
-> process crash
```

### Same-class risk in Recluse

`RecluseRegistrationPanel` has the same architecture shape:

- new temporary automatic ruling source;
- old `recommendations.map { ... }` audit candidate source.

Treat Spy + Recluse as one bug family.

### Correct fix direction

Do **not** fix this by merely writing:

```kotlin
if (recommendations.isNotEmpty()) { ... }
```

or by swallowing the exception.

That would hide the ownership inconsistency and make telemetry silently incomplete.

Required architecture rule:

> Automatic selection and selection-audit preview must describe the same complete legal candidate domain (or the audit contract must explicitly model why no auditable candidate domain exists). Telemetry must not use an obsolete partial/empty source after selector ownership moved.

The smallest safe fix should establish/reuse a typed candidate projection from the same legal registration domain used by the automatic selector, then feed that complete domain into audit preview/commit semantics.

Preserve privacy/aggregate constraints of `SelectionDistributionTelemetry`.

### Tests-first requirement

Create RED coverage for at least:

1. Beginner automatic Spy registration remains safe when legacy `recommendations` is empty;
2. the audit preview receives a non-empty complete candidate-domain representation from the authoritative selector domain;
3. committed family matches the selected automatic ruling;
4. same contract for Recluse;
5. no illegal special registration is invented;
6. deterministic selection identity remains unchanged.

Prefer testing a pure helper/domain adapter rather than trying to execute Compose itself unless a suitable Compose test already exists.

## 5. Remaining UX-MODE semantic debt after the two blockers

The following are known and should remain queued behind the crash + evil-info presentation fixes.

### 5.1 Hidden BALANCED setup default

`ClocktowerHostScreen.kt` still initializes/resets setup recommendation selection with `RecommendationStyle.BALANCED` in places.

Final product contract requires both modes to consume the same temporary internal `AGGRESSIVE` provider.

Target:

```kotlin
selectedRecommendationStyle = automaticStorytellerStyle
```

not a hidden BALANCED default.

### 5.2 Demon bluff fallback still defaults to BALANCED

`ClocktowerDemonBluffPresentation.kt` still has a manual/default BALANCED fallback and test coverage that explicitly expects Balanced behavior.

Experienced mode must use the same current internal recommendation source as Beginner. Fix tests-first as a separate semantic slice.

### 5.3 Beginner first-night ready screen still needs zero-decision simplification

Automatic setup recommendation application is owned outside the recommendation card by `ClocktowerHostScreen.kt`.

Therefore Beginner can hide the recommendation card **after** Ready + automatic recommendation actually applied.

Do not hide merely on `RecommendationUiState.Ready`; avoid the small race before the auto-apply effect commits.

Failure/loading/invalid states must remain visible.

Experienced mode keeps the recommendation/manual surface.

### 5.4 Remaining public legacy style terminology

`ClocktowerNightStepUi.kt` and some presentation reason strings still contain public language such as:

- automatic mode;
- balanced option;
- default style;
- different pressure;
- selected/current style.

Remove/neutralize these only after behavior is stable. Internal algorithm enums may remain until legacy cleanup; do not mechanically delete internal `RecommendationStyle` use.

### 5.5 Legacy mode cleanup

Later UX-MODE-1F cleanup still needs to remove obsolete `StorytellerAutomationMode` / `fromLegacyMode` ownership once all live references are gone.

Do not mix this cleanup into the crash fix.

## 6. Recommended next execution order

### Step 1 — re-confirm live refs

At the beginning of the next conversation:

```text
query live main
query codex/ux-mode-1
query PR #120 head/state/checks
```

Do not assume `7108f707...` is still the head if newer commits exist.

### Step 2 — fix Spy/Recluse audit crash first

Reason: hard process crash on real device.

Sequence:

```text
read current registration selector + telemetry owners
-> establish pure typed RED for empty legacy recommendations + valid automatic domain
-> implement one authoritative audit candidate projection
-> focused GREEN
-> exact diff/ownership audit
-> T1 :app:testFast
```

Do not broaden into unrelated recommendation redesign.

### Step 3 — fix first-night evil-team square-table routing

Sequence:

```text
identify current EvilInfo presentation routing owner
-> RED that normal first-night Minion/Demon evil-info cannot use generic legacy Card
-> route through existing square-table shell
-> preserve bluff content and Show/Next semantics
-> focused GREEN
-> device-oriented regression validation
```

Do not spend more time on historical blame unless needed to recover an existing implementation.

### Step 4 — finish unified temporary recommendation defaults

Separate small slices:

1. setup selected style uses `automaticStorytellerStyle` rather than hidden BALANCED;
2. Demon bluff Experienced/manual fallback uses the unified current provider rather than BALANCED.

### Step 5 — finish Beginner first-night Ready simplification

Hide the setup recommendation card only after automatic recommendation is actually applied; keep failure/progress surfaces.

### Step 6 — terminology / legacy cleanup

Neutralize remaining user-visible old style language, then remove obsolete legacy mode ownership where safe.

### Step 7 — acceptance validation

At minimum:

- relevant T0 focused suites during each slice;
- `:app:testFast` T1 after meaningful production checkpoints;
- affected T2/T4 according to `docs/TESTING_STRATEGY.md`;
- PR CI + R2 merge gates;
- real-device pass through setup -> Minion -> Demon/bluffs -> following first-night roles;
- verify no crash and no legacy Card island in normal first-night evil-team flow.

PR remains draft until acceptance is complete.

## 7. Scope fences

During these fixes do not:

- merge PR #120 without explicit user authorization;
- resume EPI-MQ implementation early;
- activate A4/ZDD;
- change player-owned choices into automatic choices;
- introduce a second candidate/recommendation pipeline for Beginner;
- suppress crash exceptions without repairing candidate/audit ownership;
- redesign all night UI beyond the concrete EvilInfo regression;
- reopen completed D6 solely because a file is large;
- perform unsafe whole-file replacement of protected/large source in a constrained connector environment.

Follow `AGENTS.md` architecture pre-flight and large-file SOP where applicable.

## 8. New-conversation starter prompt

Recommended prompt for the next conversation:

```text
请继续 Jazz0006/CampBoardGameHost 的 UX-MODE-1。

先读取并以以下文件为权威：
- 根目录 AGENTS.md
- docs/TESTING_STRATEGY.md
- docs/CURRENT_DEVELOPMENT_ROADMAP.md
- docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_DEVICE_REGRESSIONS.md

先重新确认 live main、codex/ux-mode-1、draft PR #120 的 head/state/checks。

当前最高优先级是附件真机崩溃已经定位到的 Spy/Recluse registration audit ownership bug：SelectionAuditRecord 收到空 candidate pool。先 tests-first 修这个 hard crash，不要用简单空列表 guard 压掉异常；自动 selector 与 audit 必须使用同一个完整合法候选域。

完成后再处理首夜 Minion/Demon/bluff 回落到 legacy 文字 Card 的问题，把 EvilInfo 恢复到现有 square-table shell。不要 merge PR。
```
