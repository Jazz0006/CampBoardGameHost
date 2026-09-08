# Persistence Requirement Reduction Audit

> Date: 2026-09-07 Australia/Sydney  
> Campaign: Persistence Simplification / Recent Emergency Recovery  
> Baseline main: `ac71cbe392fb542727dc0c2d69ac82c5fdc0435e`

## 1. Executive conclusion

The existing active-game persistence system is substantially broader than the actual product requirement.

The app needs **short-horizon emergency recovery for the current game**, not a general-purpose long-lived Save
Game feature. The current implementation serializes a large fraction of App/runtime state, restores many Compose
states directly, carries strict content/schema identity checks, duplicates Clocktower checkpoint fields, and
reuses the same active snapshot as the archive payload.

This complexity should be removed before any further D6 App/Host large-file decomposition.

Governing design principle:

> **Restore the game, not the App.**

## 2. Product requirement actually needed

Supported scenario:

- one phone is actively hosting a game;
- the user may receive a phone call, switch apps, lock the phone, suffer Android process reclamation, crash, or
  accidentally close the app;
- reopening shortly afterwards should recover the committed game and allow safe continuation.

Not required:

- manual save slots;
- long-term save retention;
- “save today, continue tomorrow”;
- restoring an active game across incompatible app versions;
- reproducing the exact UI/navigation/cache state present immediately before failure.

Initial recovery horizon accepted for implementation planning: **12 hours**.

## 3. Current architecture findings

### 3.1 Active snapshot mixes unrelated state categories

The current `activeGameSnapshotJson()` includes common/game setup fields, runtime cards, records, game outcome,
Clocktower phase/revisions/history, numerous night/day selections, recommendation-derived state, and a serialized
`ClocktowerNightCheckpoint` whose persisted values overlap top-level fields already written by the App.

Consequences:

- one new runtime field often raises save/restore/schema/fallback questions even when the field is only UI state;
- persistence ownership is coupled to App-root state ownership;
- restore has to know too much about Compose/runtime layout;
- tests protect an unnecessarily large wire contract.

### 3.2 Restore mutates live state while decoding

The current restore flow validates some top-level data first, then assigns player/cards/records/events and many
individual state fields directly. Some checkpoint-related fields are assigned from raw JSON and later overwritten
from the reconstructed checkpoint.

A failure after mutation has started can leave partially modified in-memory state even when the persisted save is
then cleared. A future recovery parser should build and validate a complete typed recovery object before applying
anything to live state.

### 3.3 Full-fidelity restore is not actually provided

Despite the large active snapshot, Host-local runtime state such as Spy/Recluse registration selections,
recorded-registration guards, `recordedNightSteps`, recommendation/UI loading state and similar `remember` values
are not all persisted.

Therefore the current system pays much of the architectural cost of exact runtime restoration without actually
providing that contract. This supports explicitly adopting safe game-state re-entry instead.

### 3.4 Active recovery and archive are incorrectly coupled

`archiveCurrentGameForRestart()` currently archives `activeGameSnapshotJson()`.

Archive review needs long-lived historical facts. Emergency recovery needs short-lived continuation facts. Their
compatibility and retention requirements are different. The current shared format forces both concerns into one
large schema.

### 3.5 Saved-game preview creates a second raw-schema reader

The current saved-game preview separately interprets active-save JSON keys in order to render resume metadata.
Once recovery is typed, preview should be a projection from the parsed recovery object rather than another JSON
reader with its own compatibility assumptions.

## 4. Field classification policy

Use these five categories for every retained/deleted field.

### DURABLE_GAME_FACT

Definition: losing the value changes something that has already happened in the game or allows a committed action
to be repeated differently.

Examples:

- actual/shown role and death/elimination state;
- confirmed Poisoner target while mechanically active;
- confirmed Monk protection;
- confirmed Demon attack/death fact awaiting Dawn resolution;
- one-shot ability usage;
- ghost-vote authority and confirmed highest vote;
- published player information;
- semantic action/observation history.

Policy: persist.

### RECOVERY_CONTINUATION

Definition: not necessarily a completed game fact, but needed to resume a mandatory unfinished interaction without
forcing already-performed social/game actions to be repeated.

Examples:

- current round/phase;
- a safe Clocktower night step cursor;
- unresolved Demon succession;
- pending Klutz choice;
- current Werewolf night-role inputs where the role interaction has already happened but Dawn has not yet been
  committed.

Policy: persist narrowly and justify each field.

### DERIVED_RECOMPUTABLE

Definition: deterministically rebuildable from durable state/current rules.

Examples/candidates:

- player-name list if cards are authoritative;
- setup counts and include-role flags after deal;
- event counter if recoverable from durable event sequence;
- ruleset basis/ref derivable from current script/current compatible build;
- `showResults` where outcome presence is authoritative;
- preview labels.

Policy: recompute rather than persist unless a concrete semantic dependency proves otherwise.

### TRANSIENT_UI

Definition: unfinished interaction/presentation state whose loss only requires re-entry.

Examples:

- raw screen/navigation location;
- Clocktower day mode in ordinary day flow;
- nominator/nominee/current vote count before vote confirmation;
- selected execution before final confirmation;
- Slayer claimant/target before action resolution;
- Artist claimant/truthful/shown answer before confirmation;
- unconfirmed Clocktower target drafts;
- open dialogs, current tabs, loading flags, animation state;
- recommendation cache/locks/candidate-list UI state.

Policy: do not persist.

### ARCHIVE_OR_BOOKKEEPING

Definition: long-lived review/cross-game accounting concern rather than current recovery.

Examples:

- completed-game archive projection;
- setup-rotation completion metadata;
- future cross-game recommendation history.

Policy: separate owner/format from RecoverySnapshot.

## 5. Clocktower-specific findings

### 5.1 Draft/confirmed distinction should define persistence

`ClocktowerNightCheckpoint` currently contains both confirmed and draft targets for attack, Poisoner, Monk, Mayor
redirect and Demon succession.

For emergency recovery, default policy is:

```text
confirmed -> durable when still semantically relevant
unconfirmed draft -> discard and re-enter
```

This aligns recovery with the transaction boundaries already introduced by the night checkpoint reducer/host
transactions.

Do not save a draft merely because it currently lives next to a confirmed field.

### 5.2 Not all day state is disposable

Ordinary nomination UI selection is transient. A committed vote, however, updates durable day facts including
highest vote and ghost-vote consumption. These confirmed consequences must survive recovery even if recovery
returns to Day Overview.

### 5.3 Mandatory continuations must survive

Klutz and unresolved Demon succession cannot be flattened to generic Day/Night Overview because they represent
mandatory game continuations. Recovery must re-enter those flows precisely enough to avoid losing or replaying a
rule consequence.

### 5.4 Recommendation state and published information are different

Recommendation candidates/cache may be regenerated. Once information has been shown to a player, that publication
is a durable fact and is already represented through event/epistemic/history paths.

The recovery design must use publication/history as authority rather than persisting recommendation-process state.

### 5.5 Demon bluffs need conservative treatment initially

`recommendedDemonBluffRoleNames` currently behaves as an applied bluff selection consumed by first-night
presentation, not merely an ephemeral recommendation cache. Keep it in the first reduced recovery model until a
better committed/publication owner is proven.

### 5.6 Events remain operational data today

`clocktowerEvents` are not archive-only in current production code. Some recommendation logic reads prior events,
for example to avoid/reason about prior unreliable information. Do not remove event persistence during the first
reduction pass.

### 5.7 Revisions/global cursor remain initially durable

`playerInputRevision` is involved in session-owned epistemic-observation transitions, while
`nextTimelineGlobalSequence` is a global semantic-history identity cursor. `gameStateRevision` also participates in
current snapshot/freshness behavior.

Retain these initially. Reconstructibility can be audited separately after the reduced recovery contract is stable.

## 6. Werewolf-specific caution

Clocktower has increasingly explicit selection/confirmation transaction boundaries. Werewolf currently retains
night selections until a single Dawn confirmation.

Forcing a Werewolf role to wake again after a process death can reveal information or change player behavior.
Therefore fields such as selected night death, Seer target, Witch save/poison choices may remain
`RECOVERY_CONTINUATION` in the first reduced model even though they are not yet committed to the final Dawn state.

A later Werewolf transaction redesign could reduce this further, but it is not required for the initial
Persistence Simplification campaign.

## 7. Setup persistence findings

`committedClocktowerSetup` is created for setup provenance/consistency and is persisted/restored, but after game
start it is not currently a normal Host/rules gameplay input. The dealt cards already carry actual/shown identities.

Therefore the full committed setup object is a strong candidate to leave emergency recovery.

`committedTroubleBrewingSetupRotationRecord` is different: it is later used when recording a completed game into
cross-game setup-rotation history. Preserve the minimal bookkeeping data needed for that purpose.

## 8. Compatibility simplification

The current active-save v3 path has strict content identity and compatibility validation. That makes sense for a
long-lived save format but is unnecessary for short-horizon emergency recovery when the product explicitly makes
no cross-version continuation promise.

New active recovery should use a small compatibility boundary:

- recovery-format version;
- current compatible build/rules token;
- timestamp/TTL.

If incompatible or expired, fail closed and discard recovery rather than migrating an old active game.

Do not remove archive compatibility merely because active recovery becomes strict/current-only.

## 9. Proposed target model

```text
RecoveryEnvelope
├── recoveryFormatVersion
├── compatibilityToken
├── savedAtMillis
└── game
    ├── UndercoverRecovery
    ├── WerewolfRecovery
    └── ClocktowerRecovery
```

Conceptual Clocktower payload:

```text
ClocktowerRecovery
├── identity
│   ├── script
│   ├── gameId
│   └── gameSeed
├── position
│   ├── phase
│   ├── round
│   ├── nightStarted
│   └── safe night continuation
├── players
│   └── dealt/current cards
├── mechanics
│   ├── confirmed active night facts
│   ├── one-shot/consumed ability facts
│   ├── confirmed day vote facts
│   └── mandatory pending continuations
├── history
│   ├── records/events
│   ├── action timeline
│   ├── epistemic observations
│   └── semantic cursors/revisions
└── bookkeeping
    └── minimal setup-rotation completion record
```

This must not become a flat God DTO. The JSON wire format can be compact/stable while the in-memory typed model is
grouped by real responsibilities.

## 10. Persistence timing finding

Current persistence includes lifecycle saves and a Compose `SideEffect` path, with synchronous SharedPreferences
commit semantics. This may cause full snapshot work much more often than a true recovery checkpoint requires.

However trigger simplification should come last. First shrink and prove the snapshot/restore contract while
preserving current durability timing, especially A4 observation durability ordering. Then separately move toward
transaction-driven dirty/checkpoint writes plus lifecycle last-chance persistence.

## 11. Recommended independent campaign

This work is deliberately separate from D6 decomposition:

```text
PS0 product-contract freeze
-> PS1 archive/recovery separation
-> PS2 minimal typed RecoverySnapshot
-> PS3 safe typed restore
-> PS4 retire old active-save infrastructure
-> PS5 persistence-trigger simplification
-> merge
-> fresh D6 App/Host ownership audit
```

File-size reduction is not the acceptance metric. The goal is to delete unnecessary product/architecture
responsibility so later decomposition operates on the system the app actually needs.

## 12. Validation priorities

Highest-risk behavioral proofs:

- confirmed game facts survive process-death recovery;
- unconfirmed Clocktower drafts are intentionally discarded and safely re-entered;
- published information/history survives exactly;
- confirmed vote/ghost-vote consequences survive while unconfirmed nomination UI does not;
- mandatory Klutz/Demon succession continuation survives;
- Werewolf recovery does not require already-performed role interactions to be repeated;
- incompatible/expired recovery never partially mutates live state;
- archive review remains valid after active/archive separation;
- recovery cannot duplicate already-recorded semantic action/observation facts.

Use behavior tests and frozen representative fixtures where the wire contract matters. Do not create permanent
source-string tests merely to prove code moved or fields disappeared.