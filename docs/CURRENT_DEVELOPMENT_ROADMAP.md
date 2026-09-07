# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-07 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Persistence Simplification started from live `main`:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
Merge pull request #110 — Audit Poisoner execution dusk crash path
```

Active branch / PR:

```text
branch: codex/persistence-simplification
PR:     #112 — Persistence Simplification: recent emergency recovery
state:  open / draft / unmerged
```

At the PS4 planning audit, branch head before the new PS4 documentation commits was:

```text
8ab643bec947d818b12b29ad43cee3b02ccf1727
docs: trigger final PS3 checkpoint gates [full-ci]
```

The PS4 handoff/progress documentation commits advance the branch head beyond that SHA. Always re-query live GitHub state before implementation, validation or merge.

The previous D6 branch `codex/d6-ownership-plan` and closed draft PR #111 are historical evidence only. Do not implement the old D6 sequence.

## 2. Current priority — PS4 Persistence Cleanup

Persistence Simplification remains the active campaign. PS3 typed Recovery is complete; the next approved work is:

> **PS4 — Retire superseded active-save infrastructure**

Immediate next slice:

> **PS4.3 — typed Recovery wire cleanup / format bump**

PS4.1 and PS4.2 are complete. Do not begin PS5, Werewolf module deletion or D6 decomposition as part of PS4.3.

## 3. Frozen product contract — Recent Emergency Recovery

The app does **not** need a general-purpose long-lived Save Game system.

Supported need:

- one phone is actively hosting the game;
- ordinary background/foreground movement continues from in-memory state while the Android process survives;
- disk Recovery exists only for process loss, crash, accidental close or equivalent interruption;
- Recovery is short-horizon emergency continuity, not normal Save/Load UX;
- stale Recovery window is **4 hours** from the last successful persisted snapshot;
- there is no next-day continuation promise;
- there is no cross-version active-game migration framework;
- exact pre-crash App/Compose/UI restoration is not required.

Governing rule:

> **Restore the game, not the App.**

Recovery classification remains:

| Category | Recovery policy |
|---|---|
| `DURABLE_GAME_FACT` | Persist |
| `RECOVERY_CONTINUATION` | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Recompute |
| `TRANSIENT_UI` | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Separate owner or retain only proven current-game bookkeeping |

Clocktower already-published information/history remains durable. Unconfirmed UI selections/drafts are disposable unless a game rule requires a mandatory continuation.

For Klutz, preserve the pending Klutz owner/return-to-Dawn requirement, but not an unconfirmed choice draft. Recovery re-enters the mandatory interaction safely.

## 4. Archive and Recovery are separate products

```text
RecoverySnapshot
    short-lived current-game process-loss recovery

GameArchiveRecord
    long-lived completed/restarted-game review and cross-game history
```

PS1 separated archive writes from the active snapshot. Archive compatibility may remain broader than active Recovery compatibility, and a strict current Recovery decoder must not become the archive-review parser.

Retain real archive-read compatibility where currently supported. PS4 cleanup must not delete it merely because old active-save infrastructure is being retired.

## 5. Persistence Simplification campaign status

```text
PS0  product/recovery contract freeze               COMPLETE
PS1  Archive / active Recovery separation           COMPLETE
PS2  minimal typed RecoverySnapshot + writer        COMPLETE
PS3  typed safe Preview/Restore + atomic apply       COMPLETE
PS4  retire superseded active-save infrastructure   IN PROGRESS (PS4.1–PS4.2 COMPLETE)
PS5  simplify persistence triggers                   NOT STARTED
```

### PS1 — Archive / Recovery separation

Complete on draft PR #112.

Result:

- new archive writes use typed `GameArchiveRecord` / `GameArchiveJsonCodec`;
- archive writes no longer consume `activeGameSnapshotJson()`;
- legacy `{ "snapshot": ... }` archive entries remain readable through archive-only compatibility;
- archive review no longer depends on strict active-Recovery compatibility.

Checkpoint:

- `docs/PS1_ARCHIVE_RECOVERY_SEPARATION_CHECKPOINT_2026-09-07.md`

### PS2 — Typed Recovery writer

Complete on draft PR #112.

Result:

- production active saves project live state into typed `RecoverySnapshot`;
- `persistActiveGameStateIfNeeded()` writes `RecoverySnapshotJsonCodec` output;
- arbitrary navigation/UI state is not persisted;
- confirmed Clocktower facts, mandatory continuations and durable semantic/history state remain durable;
- normal draft targets/day UI are omitted;
- lifecycle save timing remains unchanged.

Authoritative production checkpoint:

```text
abeb056d9f8b2fbe99b62da7f3dcaa5c169b522a
```

Checkpoint:

- `docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`

### PS3 — Typed safe Recovery read/Preview/Restore

Complete on draft PR #112.

Current architecture:

```text
live game state
-> typed RecoverySnapshot
-> RecoverySnapshotJsonCodec
-> recent active recovery

raw active recovery
-> current format / exact compatibility token / <=4h validity
-> strict complete decode
-> game-specific semantic validation
-> current Clocktower runtime/ruleset resolution
-> ValidatedRecoveryPlan
       ↙             ↘
typed preview       atomic App apply
```

PS3 results:

- strict decoder and 4-hour validity policy;
- malformed/incompatible/future/expired Recovery fails closed before App mutation;
- Preview and Restore share `prepareCurrentRecoveryPlan(raw)`;
- Stable preview no longer depends on generic `screen`;
- production restore uses `RecoveryApplicationCoordinator` atomic apply;
- old raw restore and `ClocktowerNightCheckpoint.fromPersistedValues()` fallback are no longer production restore authority;
- pending Klutz derives safe Day/Klutz continuation;
- ordinary Clocktower unconfirmed UI/draft state is not reconstructed;
- durable semantic action/history/epistemic state remains restored.

Supported typed-Recovery product surface is:

```text
Undercover
Blood on the Clocktower
```

Werewolf Recovery is intentionally outside this product surface because the current product direction is to remove the entire Werewolf module separately. Do not expand or repair Werewolf Recovery during PS4.

PS3 authoritative production checkpoint:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

Final PS3 audit/cleanup lineage includes:

```text
28ad2f7cc734be80cd8aecce718e074f38c38082
8ab643bec947d818b12b29ad43cee3b02ccf1727
```

PS3.4 final gates passed focused typed Recovery tests, `:app:testFast`, `:app:assembleDebug`, exact production-path audit, full requested final CI/R2 checkpoint and temporary one-shot cleanup.

Checkpoint:

- `docs/PS3_TYPED_SAFE_RESTORE_CHECKPOINT_2026-09-07.md`

## 6. PS4 — Retire superseded active-save infrastructure

Status: **in progress — PS4.1 and PS4.2 complete; PS4.3 next**.

Authoritative route:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS4_CLEANUP.md`

Current progress:

- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`

### 6.1 PS4 entry audit findings

#### `activeGameSnapshotJson()`

PS3.4 proved current Recovery write/Preview/Restore no longer calls it. It is the safest first cleanup target.

#### `LegacyRestoreCompatibility`

Most of the shell is obsolete transitional debt, but it cannot be deleted as the first action because it still carries one genuine durable bookkeeping field:

```text
troubleBrewingSetupRotationRecord
```

Current typed Clocktower restore uses that record, and completed-game Trouble Brewing setup-rotation bookkeeping still needs it.

Required order:

```text
move rotation record into typed ClocktowerRecovery
-> prove Recovery + rotation bookkeeping
-> remove remaining LegacyRestoreCompatibility
```

Do not delete setup-rotation bookkeeping.

#### Recovery token ownership

`RecoveryCompatibilityToken` still depends on `ActiveGamePersistenceCoordinator.CURRENT_VERSION`.

Recovery must gain its own small format/current-contract compatibility authority before old ActiveGame identity/coordinator plumbing is retired.

#### Legacy-shaped wire payload

Typed Recovery still serializes transitional active-save metadata including old state version/identity/setup/ruleset fields. The product has only a 4-hour current-format Recovery contract, so PS4 should deliberately retire this migration baggage rather than maintain dual schemas indefinitely.

A Recovery format bump (expected direction v1 -> v2) is preferred to keeping obsolete active-save compatibility fields. Previous format should fail closed; do not build a migration framework.

#### Current ruleset basis is retained

`ClocktowerRulesetPersistenceBasis` itself is **not** automatically cleanup debt. Current typed Clocktower runtime resolution still uses a role-set basis to derive/validate the current Trouble Brewing ruleset identity.

Retain the current basis/`refFor()` semantics unless a later fresh audit proves they are unnecessary. Remove only genuinely unreachable legacy restore shims.

### 6.2 Approved PS4 route

#### PS4.1 — Proven-dead active snapshot removal

Status: **COMPLETE**. Production checkpoint: `721c7394115cdc839afb189f88eb9234cd5ea204`.

Delete:

- `activeGameSnapshotJson()`;
- only helpers/constants/imports proven exclusive to it.

Do not change Recovery schema/token/save timing in this slice.

Evidence:

- fresh repository-wide reference audit;
- existing owning persistence/Recovery tests;
- compile / focused validation;
- `:app:testFast` at the logical checkpoint;
- `git diff --check`;
- exact changed-file/reference audit.

This is behavior-preserving dead-code cleanup. Do **not** manufacture a RED test merely because source is deleted.

#### PS4.2 — Recovery owns compatibility identity

Status: **COMPLETE**. RED checkpoint: `64c9866fccffa501ae1e1e3889c478b33764231e`; production GREEN checkpoint: `9fa0e3f86832cd847fb71126e4de54524e0326d2`.

Recovery compatibility is now derived from `RecoverySnapshot.CURRENT_FORMAT_VERSION`, producing `recovery-v1:<GameKind>` for format v1. The token no longer depends on `ActiveGamePersistenceCoordinator.CURRENT_VERSION`. No migration support was added, and the 4-hour/fail-closed policy is unchanged.

Cut dependency on `ActiveGamePersistenceCoordinator.CURRENT_VERSION` and give Recovery its own current-format/current-contract token authority.

Protect existing typed behavior:

- correct token accepted;
- wrong token rejected;
- wrong format rejected;
- future timestamp rejected;
- exactly 4 hours accepted;
- older than 4 hours expired.

#### PS4.3 — Typed Recovery wire cleanup / format bump

Order:

1. move `TroubleBrewingSetupRotationRecord` into typed Clocktower Recovery ownership;
2. update writer/strict decoder/App apply;
3. deliberately bump current Recovery format;
4. remove obsolete active-save metadata from Recovery wire;
5. remove `LegacyRestoreCompatibility` after no real durable field remains.

Likely retired Recovery metadata after proof:

```text
legacy active-state version
PersistedActiveGameIdentity
CommittedClocktowerSetup
persisted clocktowerRulesetRoleIds
persisted clocktowerRulesetRef
```

Current Recovery must remain strict/all-or-nothing.

#### PS4.4 — Retire ActiveGame identity/coordinator infrastructure

After PS4.2/PS4.3, perform a new reference audit. Delete only proven unreachable types/functions, potentially including old ActiveGame coordinator/identity/envelope/json-codec and legacy restore paths.

Tests protecting only removed implementation/schema details should be retired under the `AGENTS.md` test-retirement policy after confirming no unique product contract is lost.

#### PS4.5 — Setup / ruleset / test hygiene

Audit remaining transitional persistence code.

Potential cleanup:

- committed setup persistence with no post-start production consumer;
- legacy ruleset restore shims with no caller;
- duplicate obsolete Clocktower checkpoint mappings/writes;
- tests tied only to removed active-save schema shape.

Must retain:

- Trouble Brewing setup-rotation bookkeeping/history;
- real archive compatibility;
- durable semantic/history persistence;
- action timeline / epistemic observations;
- current ruleset resolution behavior.

#### PS4.6 — Final architecture checkpoint

Target absence:

```text
NO activeGameSnapshotJson
NO LegacyRestoreCompatibility
NO obsolete ActiveGame restore identity/coordinator ownership in Recovery
NO old committed-setup authority in active Recovery
NO persisted legacy ruleset-ref/role-id authority in Recovery wire
NO independent raw Preview parser
NO independent raw Restore parser
```

Target retained architecture:

```text
one typed Recovery writer
one strict current-format Recovery decoder
one Recovery preparation/validation planner
Preview ----\
            -> prepareCurrentRecoveryPlan(raw)
Restore ----/
one atomic RecoveryApplicationCoordinator apply boundary
separate Archive compatibility
setup-rotation bookkeeping
durable semantic/history persistence
```

Final PS4 checkpoint should run focused Recovery + Archive behavior tests, `:app:testFast`, `:app:testFull`, `:app:assembleDebug`, applicable ASP/Clingo, R2, exact diff/reference audits and remote-head race lock.

## 7. PS5 — Simplify persistence triggers

Status: **not started; do not enter from PS4 cleanup automatically**.

Only after Recovery contents/ownership are fully simplified should current blanket synchronous write timing be re-audited.

Target direction remains:

```text
durable game transaction committed
-> recovery dirty/checkpointed

ON_PAUSE / ON_STOP
-> last-chance durable write
```

Do not change save contents and save timing in the same cleanup checkpoint. A4 durability ordering must remain protected where observation publication currently depends on successful persistence.

## 8. Validation strategy

Persistence is a durability boundary, so high-value behavior/integration tests are justified. Do not use source-string RED ceremony where typed behavior already exists.

Current supported Recovery acceptance matrix must include representative **Undercover and Clocktower** cases:

- valid Stable Recovery without generic `screen` survives Preview/restart;
- confirmed fact survives restart;
- unconfirmed Clocktower drafts are intentionally discarded/re-entered;
- already-published information/history remains unchanged;
- ghost-vote/highest-vote durable state survives while unconfirmed nomination UI does not;
- pending Klutz derives mandatory continuation;
- unresolved Demon continuation survives;
- `gameOutcome` derives results presentation;
- expired (>4h), future-dated, wrong-format and incompatible Recovery fail closed before App mutation;
- malformed cards/records/events/history fail all-or-nothing;
- archive review remains readable after Recovery cleanup;
- Recovery does not duplicate semantic actions/observations;
- Trouble Brewing setup-rotation bookkeeping survives the PS4 legacy-shell removal.

Werewolf recovery behavior is **not** a required PS4 acceptance target. Whole-module removal is a separate future task.

Use frozen representative wire fixtures when wire compatibility/current-format failure itself is the contract. Do not prove codecs only by encoding and immediately decoding the same object.

At logical checkpoints follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`. Persistence/schema/transaction boundaries justify broader gates. Real-device process-loss/restart testing is still required before the overall Persistence Simplification campaign is considered release-ready.

## 9. Explicit non-goals

The current PS4 campaign does **not** include:

- PS5 persistence-trigger timing redesign;
- deleting the entire Werewolf module;
- D6 App/Host large-file decomposition;
- global ViewModel migration;
- DataStore migration merely for modernization;
- long-lived save slots/manual Save/Load UX;
- next-day continuation;
- cross-version active-game migration framework;
- general session database;
- recommendation-quality redesign;
- A4/ZDD production cutover;
- unrelated Host UI redesign.

File-size reduction is a welcome consequence, not an acceptance metric.

## 10. D6 status — deferred and must be re-audited

Night Step decomposition D1–D5 is complete and integrated through PR #106. Subsequent crash/initialization/Poisoner corrections #107/#108/#110 are integrated in the campaign base.

The old D6 plan assumed preservation/decomposition of the former full active-save architecture. That premise is invalid. PR #111 was closed without merge.

After Persistence Simplification is completed and merged:

1. re-fetch live `main`;
2. re-measure App/Host ownership and change-context hotspots;
3. treat old D6 docs/PR #111 as historical evidence only;
4. write a fresh D6 ownership/decomposition audit against the reduced codebase;
5. execute only the newly approved route.

Do not mix large-file extraction into PS4/PS5 simply because persistence cleanup reduces App code.

## 11. Other pending roadmap items

### Werewolf module removal

Current product direction is to remove the whole Werewolf module because the app's mandatory human-judge model is a structural disadvantage relative to familiar no-judge WeChat mini-program workflows.

Do this as a **separate campaign after Persistence Simplification**, not inside PS4 cleanup. Re-audit shared navigation/models/assets/tests before deletion.

### UI-R5 — real-device stabilization

Still open. Process-loss/restart acceptance is required for Persistence Simplification. Broader UI-R5 remains pending unless a release-blocking UI defect appears earlier.

### EPI-MQ — information quality / Productive Uncertainty

Deferred until structural/recovery work is stable. Future direction remains to reject obviously unreasonable unreliable clues and improve evil-side playability/useful uncertainty without returning committed shown-identity authority to recommendation.

### Beginner Mode

Deferred until cognitive-consistency/information-quality work is ready. Accepted product direction remains minimal user choices, automatic clues, unreliable drunk/poison information policy, and automatic Spy/Recluse false registration suitable for inexperienced Storytellers.

### UX-R6

Legacy recommendation-provider replacement remains after EPI-MQ as currently planned.

### A4 / ZDD

Remain non-production. Persistence Simplification does not authorize rollout.

## 12. Current references

Current execution authority:

- root `AGENTS.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — this file;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS4_CLEANUP.md` — **current next-chat implementation authority**;
- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md` — current PS4 audited progress/entry checkpoint.

Persistence campaign history/evidence:

- `docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md`;
- `docs/PS1_ARCHIVE_RECOVERY_SEPARATION_CHECKPOINT_2026-09-07.md`;
- `docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md` — historical PS3 route;
- `docs/PS3_TYPED_SAFE_RESTORE_CHECKPOINT_2026-09-07.md` — completed PS3 evidence.

Long-lived engineering authority:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Prior D1–D5/D6 planning remains historical support only:

- `docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`;
- PR #106 merged evidence;
- closed PR #111 superseded D6 plan.

## 13. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution, architecture and test rules;
3. this roadmap controls current project state, product boundary and priority;
4. `NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS4_CLEANUP.md` controls the approved PS4 implementation route;
5. `PS4_CLEANUP_PROGRESS_2026-09-07.md` records the current PS4 checkpoint/next slice;
6. umbrella Persistence Simplification docs control historical campaign intent where non-conflicting;
7. specialized design docs control their own semantic/product domain where non-conflicting;
8. archive documents, old branches and historical PR records are evidence only.

## 14. Next conversation start point

The next conversation should:

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS4_CLEANUP.md`;
4. read `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`;
5. re-query live `main`, PR #112, branch head and checks;
6. distinguish the PS3 production checkpoint from later docs-only PS4 planning commits;
7. begin **PS4.1 only** with a fresh reference audit of `activeGameSnapshotJson()` and its exclusively-dead dependencies;
8. do not merge PR #112 without explicit user authorization.