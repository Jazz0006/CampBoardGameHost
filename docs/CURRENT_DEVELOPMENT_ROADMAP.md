# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-28  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Latest verified code checkpoint before this docs correction: `7fa38d47d682cfb324052d8b56c46563ffc0b815`  
> Current priority: **SNE-7 REOPENED — SNE-7.9 corrective campaign ACTIVE; SNE-7.9C2C Dawn production consumer cut-over NEXT**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state / SNE-7      REOPENED / 7.9 CORRECTIVE SLICES ACTIVE
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

Do not merge or mark PR #54 ready. Do not resume App-root decomposition, A3 setup-snapshot work, A4/B4 authority promotion, recommendation tuning, or generic custom-script Demon-death succession until SNE-7.9 is closed or explicitly paused.

## 2. Why SNE-7 was reopened

The earlier `SNE-7.1–7.8 IMPLEMENTATION COMPLETE / FINAL VALIDATION` status was too optimistic. Final acceptance audit found real production correctness and authority defects beyond the typed seams already covered by tests.

Blocking findings were:

1. Chambermaid stored selections could become stale after upstream same-night state changed.
2. Mayor redirect confirmation did not invalidate when upstream confirmed Poison / Monk / Demon attack facts changed; the Monk-protected-Mayor scenario could produce a real wrong death.
3. Host, observation preflight, and Dawn did not consume one canonical night-death authority.
4. Production Demon succession did not yet fully consume the validated succession resolution path.
5. `NightTransactionReconstructor` successor legality was not strong enough for production restore activation.
6. Real restore did not consume the reconstructor.
7. Integration smoke coverage stopped before App-owned durable Dawn effects.

Therefore the correct campaign state is:

> **SNE-7 REOPENED — blocking correctness findings discovered during final acceptance audit.**

## 3. SNE-7.9 corrective route

The authoritative order is:

```text
SNE-7.9A  Mayor redirect dependency invalidation
SNE-7.9B  Chambermaid stale-target revalidation
SNE-7.9C  canonical night-death resolution
SNE-7.9D  Demon succession legality / Dawn
SNE-7.9E  real restore + durable Dawn integration
```

Do not skip ahead to D/E while C still has multiple production death authorities.

## 4. Current SNE-7.9 live status

### SNE-7.9A — Mayor redirect dependency invalidation

**COMPLETE / GREEN**

`NightCheckpointReducer` now treats confirmed Poison, Monk protection, and Demon attack as upstream dependencies of both Mayor redirect and Demon successor confirmations:

```text
upstream confirmed value CHANGED
→ confirmedMayorRedirectTarget = null
→ confirmedDemonSuccessorTarget = null
→ editable Mayor/successor drafts preserved

idempotent reconfirm, confirmed value UNCHANGED
→ dependent confirmations preserved
```

Accepted production checkpoint:

```text
12d84cc9ed8076df9833d2fa268bc523283211b2
  fix: invalidate stale Mayor redirect on upstream reconfirm
```

Dependency direction remains one-way. Mayor confirmation itself does not clear successor confirmation unless a separate proven dependency requires it.

### SNE-7.9B — Chambermaid stale-target revalidation

**COMPLETE / GREEN**

Production now uses a typed revalidation seam:

```text
stored first/second selections
+ current eligible names at Chambermaid cursor
→ revalidateTwoPlayerSelection
→ resolveChambermaidSelection
→ revalidated targets + wokeCount
```

Host result, player-visible display, recorded targets, and current UI selection consume the revalidated result.

Accepted production checkpoint:

```text
f9300f72b4ef63a521a87e9d2a087c7ae9db2f03
  fix: revalidate Chambermaid selection authority
```

### SNE-7.9C — canonical night-death resolution

**IN PROGRESS**

Accepted sub-slices:

```text
C1
58fe6cd4e927128c3cb208dab9d12c8423ca5188
  NightDawnResolutionPlanner accepts canonical DemonNightAttackOutcome

C2A
dee41713e25b2387b77419a74ea256082fe2a44a  RED
e0b25a8d822bd348e33b6e7a9378be89bd564da9  GREEN
  resolveTroubleBrewingDemonNightAttackOutcome production adapter

C2B
b2830c5846c14320e37371d706f678db7b10e996  RED
7fa38d47d682cfb324052d8b56c46563ffc0b815  GREEN
  TroubleBrewingDawnDeathFacts canonical direct-attack facts
```

`TroubleBrewingDawnDeathFacts` currently owns:

```text
attackOutcome
originalDeathSeat
mayorSeat
demonSafeSeats
```

All seats use stable original table ordering; never derive seat identity by re-indexing a filtered alive collection.

However C is **not complete**. The real App Dawn consumer still rebuilds `originalDeathSeat` / Mayor applicability and later rechecks Monk/Soldier itself instead of consuming the canonical facts. Host also still calculates `resolvedNightDeathName / nightDeathWillOccur` independently. Observation preflight remains another consumer to cut over later.

### SNE-7.9C2C — NEXT

**Dawn production consumer cut-over**

Narrow goal:

```text
confirmed attack / poison / Monk facts
→ resolveTroubleBrewingDawnDeathFacts(...)
→ NightDawnDeathResolutionInput(
     attackOutcome,
     originalDeathSeat,
     mayorSeat,
     demonSafeSeats,
     ...
   )
→ NightDawnResolutionPlanner
→ DawnDeathIntent
```

C2C must remove Dawn's duplicate direct-attack / Mayor-applicability / Monk-Soldier safety authority from the actual Dawn death decision. Keep event sequencing, Ravenkeeper/Klutz consequences, durable history, phase transition, and unrelated succession behavior unchanged unless the cut-over mechanically requires a minimal projection adjustment.

C2C must **not** simultaneously migrate Host or observation preflight. Those remain later SNE-7.9C slices after C2C is accepted.

### SNE-7.9D — Demon succession legality / Dawn

**NOT STARTED**

Must address the earlier #4/#5 findings only after canonical death consumers are converging. Production must consume validated `DemonSuccessionResolution / planDemonSuccession()` semantics, and restore/reconstruction must not accept a successor merely because it exists/alive/is a Minion.

Do not resurrect the previously deferred generic non-self Demon-death / custom-script succession work unless explicitly authorized.

### SNE-7.9E — real restore + durable Dawn integration

**NOT STARTED**

Must connect real restore to `NightTransactionReconstructor` and add integration coverage spanning the real durable boundary, including as applicable:

```text
persist / restore
→ reconstructed same-night mechanical state
→ canonical death / succession resolution
→ Dawn materialization
→ ActionFact / observation durability
→ phase change
```

The existing lifecycle smoke is not sufficient because it deliberately stops before App-owned durable effects.

## 5. Protected architecture contracts

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable canonical interaction plan
+ checkpoint.nightStepIndex
→ derived current interaction / cursor
→ ClocktowerEffectiveNightState
→ actor eligibility
→ ability functioning
→ persistent-effect lifetime
→ target legality
→ information truth
→ triggers
→ current role
```

Hard contracts:

- mechanical death and public death announcement remain distinct;
- never write `eliminatedRound` early merely to make later-night logic work;
- stable seat/interaction identity never comes from re-indexing filtered views;
- draft UI state is never mechanical authority;
- changed reconfirmation is the dependent-invalidation boundary;
- invalidating a confirmed dependent does not erase its editable draft;
- same-night `RoleChanged` is projected before Dawn materialization;
- persistent effects follow source ability lifetime;
- rules determine legality, recommendation ranks legal choices, UI displays legal choices;
- pure semantics may support future cases, but production wiring only activates validated slices;
- no second durable night state owner and no replay of transient `NightResolutionEvent` as event sourcing.

## 6. Testing / writer policy

Preferred proof order:

```text
typed pure/domain behavior
→ typed reducer/planner/session behavior
→ typed adapter/integration behavior
→ minimal coarse source ownership guard only where the App boundary is not JVM-callable
```

Cadence:

```text
micro-slice
  → exact T0 RED
  → minimal GREEN
  → focused --rerun-tasks
  → git diff --check
  → push
  → Chat remote parent/diff/scope audit

logical checkpoint
  → :app:testFast + triggered T2/T3
  → latest-head GitHub CI/R2
```

Do not wait for old-head CI between already-focused-GREEN micro-slices. Do not rerun an identical focused command merely to duplicate evidence already produced with `--rerun-tasks`.

`CampBoardGameHostApp.kt` remains a large file. Use a complete-file/worktree-safe edit path; never perform a truncated whole-file replacement.

## 7. Current exact next-start instruction

```text
1. confirm live branch still descends from code checkpoint 7fa38d47;
2. establish SNE-7.9C2C RED around Dawn production ownership/canonical facts consumption;
3. prove the RED fails for the intended missing Dawn cut-over;
4. minimally cut the real Dawn transaction over to resolveTroubleBrewingDawnDeathFacts;
5. run the focused C2C GREEN set with --rerun-tasks and git diff --check;
6. perform remote parent/diff/scope audit;
7. only then decide the next SNE-7.9C consumer slice (observation preflight vs Host);
8. do not start 7.9D or 7.9E yet;
9. keep PR #54 draft/unmerged.
```

## 8. Development authority / startup order

Read in this order:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md`;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`;
10. re-query live `main`, PR #54 head/state and checks before editing.

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
