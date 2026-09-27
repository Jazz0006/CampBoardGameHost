# Current-Only Recent Recovery Minimal-State Audit

> Date: 2026-09-28 Australia/Sydney  
> Branch: `codex/current-only-recovery-cleanup`  
> Base reviewed before branch creation: `main@5d2982a7492a5092a898436a0386b822ee7343f5`  
> Status: **BROAD READ-ONLY AUDIT COMPLETE / FOCUSED FIELD DISPOSITION AUDIT NEXT / NO PRODUCTION EDITS YET**

## 1. Product decision

Recent Emergency Recovery is not a normal Save Game feature.

The supported product requirement is:

~~~text
current app/runtime contract
+ recent emergency interruption / Android process loss
+ <= 4 hour freshness
-> restore the latest safe current-game continuation
~~~

The product does **not** need:

- deliberate long-term unfinished-game saves;
- next-day continuation;
- cross-version active-game migration;
- reconstruction of obsolete persistence schemas;
- exact restoration of arbitrary Compose/UI draft state;
- compatibility code solely to preserve data written by older development builds.

The governing rule remains:

> **Restore the game, not the App.**

Because this application has no external installed user population whose unfinished saves need migration, old active-save compatibility has no product value. Current-format identity/version markers may remain as fail-closed validation, but readable/writable active Recovery should have one current contract and no migration chain.

## 2. Why this cleanup is being reopened

The 2026-09-07/08 Persistence Simplification campaign correctly reduced the original broad save/restore architecture to typed short-horizon Recovery. Subsequent SDE, semantic-history, setup-history and integration work added new persisted fields and some bounded compatibility paths.

A fresh 2026-09-28 audit found architecture drift in two related dimensions:

1. **current-only compatibility drift** — old schema readers, migration/fallback branches and legacy-shape tolerance exist again;
2. **minimal-state drift** — some Recovery fields/wire keys remain from the earlier exact-progress reconstruction model or duplicate state that current authority can derive.

This cleanup is intentionally separate from the current Storyteller policy/evidence mainline. Trouble Brewing real-game evidence collection and E3/E4 qualification continue independently. This branch must not introduce C5/V2 policy behavior.

## 3. Audit findings — current-only compatibility

### 3.1 Explicit old-schema compatibility still in production

The broad audit found three clear old-schema read/migration paths:

1. **DecisionTrace**
   - `DecisionTrace.LEGACY_SCHEMA_VERSION = 1`;
   - `DecisionTraceArchiveJsonCodec` accepts schema v1 and v2;
   - schema-v1 truth/credibility payloads migrate into the current in-memory shape;
   - focused tests explicitly protect v1 migration.

2. **Trouble Brewing setup completion persistence**
   - schema v1 and current schema v2 are both accepted;
   - v1 reconstructs missing `playerStartingIdentities` as empty;
   - focused tests explicitly protect v1 readability.

3. **Trouble Brewing setup rotation history**
   - store version v1 and current v2 are both accepted;
   - v1 reconstructs missing starting identities as empty;
   - focused tests explicitly protect v1 readability.

These conflict with the current-only product contract unless an independent non-Recovery consumer can prove a current need.

### 3.2 Implicit legacy-shape tolerance

Additional compatibility exists without an explicit legacy version constant:

- missing epistemic `timelineBinding` becomes `ObservationTimelineBinding.LegacyLocal`;
- missing Grimoire `truthBinding` becomes `GrimoireTruthBinding.LEGACY_DISPLAY_ONLY`;
- missing formal `actionTimelineBinding` becomes `FormalActionTimelineBinding.Legacy`;
- missing persisted Clocktower action timeline currently reconstructs an empty timeline;
- old NightCheckpoint Map decoding contains confirmed-from-draft fallback semantics.

These paths require focused reachability classification. Current production gameplay starts Clocktower sessions in `GLOBAL_V1`; historical/runtime semantic modes must not be deleted merely because they contain “legacy” names. Only persistence compatibility that exists to read obsolete stored shapes is in scope.

## 4. Audit findings — stale exact-progress Recovery surface

### 4.1 Werewolf Recovery is a dead persistence surface

The Werewolf runtime has been removed from production:

- active Recovery capture throws for `GameKind.Werewolf`;
- strict Recovery decode rejects Werewolf;
- the restore planner rejects `WerewolfRecovery`.

Nevertheless the code still contains:

- `WerewolfRecovery`;
- full Werewolf Recovery JSON encoding;
- restore branches;
- tests that construct/encode Werewolf Recovery.

This is a high-confidence dead Recovery surface. Historical Archive/review model support is a separate concern and must not be removed by association.

### 4.2 Recovery still writes obsolete draft keys as explicit null

Current Clocktower Recovery intentionally discards unconfirmed night/UI drafts, but the wire format still writes and tests old keys as null:

- `clocktowerDemonAttackDraftTarget`;
- `clocktowerPoisonTarget`;
- `clocktowerMonkProtectedTarget`;
- `clocktowerMayorRedirectTarget`;
- `clocktowerDemonSuccessorTarget`;
- `clocktowerKlutzChoiceName`.

The strict decoder then requires the draft keys to exist and remain null.

This protects an obsolete wire shape rather than current behavior. Current-only Recovery should omit non-contract draft fields entirely.

### 4.3 ClocktowerNightCheckpoint domain model is live; its old persistence shell is not

`ClocktowerNightCheckpoint` is still a live same-night transaction/domain model and must remain.

However its persistence-facing methods:

- `persistedValues(): Map<String, Any?>`;
- `fromPersistedValues(...)`;

still encode/decode the old flat persistence representation and carry old fallback semantics such as promoting draft values into confirmed values when confirmed keys are absent.

Production same-night reconstruction uses typed `NightTransactionRestoreComposition.compose(checkpoint = ...)`. The Map-based `restore(...)` / `fromPersistedValues(...)` path has no current production caller outside the Recovery encoder shell and tests.

Target direction:

~~~text
keep ClocktowerNightCheckpoint as live transaction state
remove/retire obsolete Map persistence ownership
encode current Recovery fields directly
never synthesize confirmed facts from obsolete draft fields
~~~

## 5. Audit findings — duplicate / derivable Recovery fields

The following are strong candidates for the focused field audit.

### 5.1 Undercover setup duplicates

`UndercoverRecovery` persists `undercoverCount` and `includeBlank`, while validation already requires both values to equal facts derivable from recovered cards.

Candidate direction:

~~~text
cards = authority
undercoverCount = count(cards.role == Undercover)
includeBlank = cards contains Blank
~~~

`lastWordsMode` requires a focused consumer audit: if it affects only future setup/quick-restart configuration rather than continuation of the current Undercover game, it should leave Recovery as well.

### 5.2 Clocktower semantic-history mode

Fresh production Clocktower sessions are explicitly created as `GLOBAL_V1`. Current-only Recovery does not promise restoring pre-cutover sessions.

Therefore persisted `semanticHistoryMode` is likely a redundant historical compatibility discriminator rather than current Recovery state. The focused audit must verify all current restore/replay consumers before removal.

### 5.3 Timeline cursor

Recovery persists:

- action timeline;
- epistemic observations;
- `nextTimelineGlobalSequence`.

Current production global commits advance the cursor from committed globally bound actions/observations. The only direct `allocateTimelinePoint(...)` caller found in the broad audit is test code.

This makes `nextTimelineGlobalSequence` a strong derivation candidate:

~~~text
max(committed global action/observation sequence) + 1
~~~

The focused audit must prove empty-history, action-only, observation-only, interleaved, retry/idempotency and overflow behavior before changing ownership.

### 5.4 Stable currentDealIndex

`currentDealIndex` is semantically required for `PassPhone` / `RevealCard` identity handoff continuation. Its value during `RecoveryEntryPoint.Stable` appears non-authoritative and should be classified separately rather than treated as universally durable state.

## 6. Recovery fields that remain durable unless a focused audit proves otherwise

Short-horizon does **not** mean process memory survives. Android process death destroys RAM, so already committed game facts and non-repeatable social interactions remain durable.

Current default KEEP surface includes:

- dealt/current cards and identity/death state;
- round and semantic phase where gameplay depends on them;
- Clocktower game ID and seed;
- confirmed attack / Poisoner / Monk / Mayor facts still mechanically active;
- Demon succession facts and mandatory continuation;
- Red Herring;
- applied Demon bluffs where presentation/history still consumes them;
- Butler master while mechanically relevant;
- Virgin / Slayer / Artist usage and any committed claim history still consumed by rules/UI;
- last executed player where later rules consume it;
- pending Klutz obligation and return semantics;
- ghost-vote authority and confirmed highest vote;
- committed action timeline and epistemic observations;
- revisions still consumed by current canonical/session or SDE replay contracts;
- night-start / safe night-step continuation where replaying an already-performed social interaction could leak information;
- `PassPhone` / `RevealCard` identity-delivery continuation.

Fields leave this list only with producer/consumer/authority evidence.

## 7. Non-gameplay data currently transported through Recovery

Two surfaces need ownership review rather than immediate deletion.

### 7.1 Trouble Brewing setup rotation bookkeeping

`troubleBrewingSetupRotationRecord` is not required to continue the current game. It exists so a completed game can later update cross-game setup diversity history.

Direction: classify as bookkeeping. Prefer an independent minimal owner if it can be separated without weakening completion accounting.

### 7.2 SDE historical replay export

`sdeHistoricalReplayInputJson` is a recent IF-D feature, not legacy code. It is explicitly read-only diagnostic/replay transport and not canonical game truth.

Direction: keep behavior intact during initial Recovery cleanup. Later decide whether diagnostic replay durability should continue to piggyback on Recovery or move to a separate diagnostic owner. Do not delete it merely to make Recovery smaller.

## 8. Persistence-trigger architecture needs a later re-audit

Current trigger topology remains:

~~~text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
~~~

PS5 retained `SideEffect` partly because the generic persistence path covered Undercover, Werewolf and Clocktower and there was no universal durable revision.

The Werewolf runtime has since been removed, so that historical rationale is partially stale. `RecoveryWriteGate` suppresses duplicate physical writes, but ordinary attempts still construct and encode a potentially large Recovery payload.

Do **not** remove `SideEffect` in the first cleanup slice. It still participates in foreground retry and A4 observation durability ordering. Trigger ownership/performance is a separate follow-up after the persisted state contract is minimized.

## 9. Focused audit method

Before production edits, build a field-by-field disposition matrix for every current Recovery field and wire key.

For each field answer:

1. production producer;
2. production consumer after restore;
3. authoritative owner;
4. whether losing it changes a committed game fact;
5. whether losing it forces a non-repeatable player interaction to repeat;
6. whether it can be deterministically derived from other current authorities;
7. whether it is bookkeeping/diagnostic rather than gameplay Recovery;
8. whether any test protects only an obsolete persistence shape.

Allowed dispositions:

- **KEEP** — durable current-game fact or mandatory continuation;
- **DERIVE** — remove persisted duplicate and reconstruct from authority;
- **MOVE OUT OF RECOVERY** — real data, wrong owner;
- **DELETE** — dead/obsolete compatibility or draft persistence surface;
- **SEPARATE FOLLOW-UP** — valuable question that should not broaden the current slice.

## 10. Planned implementation route

Implementation must stay small and independently reviewable.

~~~text
R0  document product contract + broad audit                         COMPLETE
R1  focused current Recovery field/wire disposition matrix          NEXT
R2  delete high-confidence dead Recovery surface
    - WerewolfRecovery persistence-only surface
    - permanently-null obsolete draft wire keys
    - dead Klutz draft Recovery field
R3  retire old NightCheckpoint persistence shell/fallback
    - keep typed live NightCheckpoint domain behavior
    - direct current Recovery mapping
R4  remove explicit current-only compatibility drift
    - TB setup completion v1 read path
    - TB rotation history v1 read path
    - DecisionTrace schema-v1 migration
R5  tighten implicit old-shape fallback where current writers prove strict fields
    - epistemic/timeline/grimoire compatibility seams
R6  derive redundant Recovery state only where R1 proves authority
    - Undercover duplicates
    - semantic-history mode
    - timeline cursor
    - stable-only irrelevant continuation values
R7  ownership follow-up
    - setup-rotation bookkeeping
    - SDE replay transport
    - SideEffect / lifecycle persistence trigger architecture
~~~

R2–R6 may be split further if fan-out or validation risk is larger than expected. R7 is explicitly not required to complete the initial cleanup.

## 11. Validation strategy

This campaign is architecture/ownership cleanup with some intentional current-format contract changes.

Use risk-based evidence:

- establish existing focused Recovery GREEN baseline before the first production slice;
- for deleted legacy compatibility, replace “old v1 remains readable” tests with current-only fail-closed contract tests only where a durable boundary benefits from one;
- for derived fields, use typed behavior tests proving reconstruction is exact;
- preserve strict malformed-current-payload rejection;
- preserve process-loss continuation behavior for committed facts and mandatory social continuations;
- re-run producer/consumer search after every shared DTO/codec change;
- use GitHub CI/R2 as normal Android acceptance;
- do not manufacture RED for purely mechanical deletion when existing coverage + compile/static/diff evidence is sufficient.

## 12. Scope guard

This branch must not:

- change Storyteller recommendation ranking/policy;
- create `BEGINNER_CONSERVATIVE_V2`;
- modify evidence qualification thresholds;
- reinterpret current game rules;
- remove Archive/history support merely because active Recovery no longer needs it;
- remove live Clocktower night transaction semantics;
- remove SDE diagnostic durability without a separate ownership decision;
- merge without explicit user authorization.

The active SDE/product mainline remains real-game evidence collection and E3/E4 qualification. This cleanup branch is a bounded maintenance lane chosen specifically because production policy code is paused while evidence is being collected.
