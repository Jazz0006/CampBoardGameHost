# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-07 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Live `main` at campaign start:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
Merge pull request #110 — Audit Poisoner execution dusk crash path
```

Always re-query live GitHub state before implementation, validation or merge.

Active working branch:

```text
codex/persistence-simplification
```

This branch was created directly from the live `main` checkpoint above. The previous docs-only D6 branch
`codex/d6-ownership-plan` and draft PR #111 are **superseded historical planning evidence**. PR #111 was
closed without merge. Do not implement the old D6 save/restore plan.

## 2. Current priority — Persistence Simplification / Recent Emergency Recovery

### 2.1 Product decision

The app does **not** need a general-purpose long-lived Save Game system.

The supported product need is narrow:

- a game is actively being hosted on one phone;
- the user may receive a call, switch apps, lock the phone, suffer process reclamation, crash, or accidentally close the app;
- reopening shortly afterwards should recover enough durable game state to continue safely;
- there is no requirement to save today and continue tomorrow;
- there is no promise that an active game can survive an app upgrade or an incompatible recovery-schema change;
- there is no requirement to reconstruct the exact pre-crash UI/runtime state.

The governing rule is:

> **Restore the game, not the App.**

The active-game persistence feature is therefore redefined as **Recent Emergency Recovery**.

Initial recovery horizon: **12 hours**. Expired or compatibility-mismatched active recovery may be discarded
rather than migrated. This horizon is a recovery safety policy, not a long-term archive retention rule.

### 2.2 Why this work precedes D6 decomposition

The persistence requirement audit found that the current active snapshot mixes:

- durable game facts;
- current flow position;
- unconfirmed draft input;
- transient UI state;
- recommendation/cache state;
- setup provenance and compatibility metadata;
- archive/history data;
- duplicated Clocktower checkpoint fields.

The restore path correspondingly reconstructs a large portion of App/Compose state. At the same time, some
Host-local `remember` state is not persisted at all, so the current system already does not provide true
full-fidelity runtime restoration.

Therefore this is a **requirements and feature simplification campaign**, not a file-size/decomposition slice.
Optimizing the existing full snapshot first would preserve unnecessary architecture. Persistence must be
reduced before App/Host ownership is re-audited.

## 3. Persistence Requirement Reduction Audit — accepted classification

Every active-recovery field must be classified into one of the following categories before it is retained.

| Category | Meaning | Recovery policy |
|---|---|---|
| `DURABLE_GAME_FACT` | Losing it changes the game that has already happened | Persist |
| `RECOVERY_CONTINUATION` | Needed to safely resume an unfinished mandatory flow | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Can be deterministically rebuilt from durable facts/current rules | Recompute |
| `TRANSIENT_UI` | Unconfirmed selection, navigation, presentation, loading/cache state | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Long-lived review/history/cross-game accounting, not active recovery | Separate owner |

### 3.1 Must remain durable

At minimum, preserve the already-committed facts needed to avoid changing or replaying the game:

- dealt player/card state including actual/shown identity and death/elimination state;
- game kind, Clocktower script, stable game id and seed where required for deterministic semantics;
- current round/phase where it changes game semantics;
- confirmed mechanical facts that are still active or awaiting later resolution;
- once-per-game / consumed ability facts such as Virgin, Slayer and Artist usage;
- confirmed nomination/vote consequences that affect the rest of the day, including ghost-vote authority and current highest vote;
- mandatory pending continuations such as unresolved Demon succession or Klutz resolution;
- durable records/events while production logic still consumes them;
- semantic action timeline and epistemic observations;
- semantic-history mode and global timeline cursor;
- revision fields that currently participate in durable session/history semantics until a separate proof shows they are reconstructible;
- the minimal setup-rotation bookkeeping required to record a completed game.

### 3.2 Default to recompute or discard

The new recovery design should remove active persistence of state whose loss merely requires re-entering an
unfinished interaction or rebuilding presentation:

- generic raw `screen` restoration;
- Clocktower day `dayMode` where a safe Day Overview can be entered instead;
- current nominator/nominee/current vote count before vote confirmation;
- selected execution before end-day confirmation;
- Slayer claimant/target UI selection before resolution;
- Artist claimant/truthful/shown-answer UI selection before confirmation;
- Clocktower attack/poison/Monk/Mayor/successor **draft** targets when the corresponding fact has not been confirmed;
- recommendation loading state, locks, temporary candidate lists and other cache/UI state;
- provisional Drunk-information recommendation cache;
- setup/count flags that can be derived from the committed dealt cards;
- event counter when it can be safely reconstructed from durable events;
- preview-only data that can be projected from a typed recovery object.

### 3.3 Important exceptions and cautions

Do not apply “drafts are disposable” mechanically across all games.

The current Werewolf night flow retains several selections until a single Dawn confirmation. Re-waking roles
after a crash may itself change the social game. Until Werewolf gains explicit per-step commit boundaries, its
already-completed night-role inputs may remain `RECOVERY_CONTINUATION` even though they look like draft fields.

Clocktower information also uses a publication boundary:

```text
recommendation / prepared choice
!=
published information
```

Recommendation state may be recomputed, but information already shown to a player is durable history and must
not silently change after recovery.

`recommendedDemonBluffRoleNames` currently behaves closer to an applied/committed bluff triple than a disposable
cache; retain it until publication ownership proves a better durable source.

## 4. Archive and active recovery are separate products

PS1 now enforces this separation in production: new archive writes project live game state into a narrow `GameArchiveRecord` and encode it with `GameArchiveJsonCodec`; they no longer consume `activeGameSnapshotJson()`. Legacy `{\"snapshot\": ...}` archive entries remain readable through an archive-only compatibility fallback, while active Recovery save/restore remains on its existing path.

Define two independent concepts:

```text
RecoverySnapshot
    short-lived current-game crash/process-death recovery

GameArchiveRecord
    long-lived completed/restarted-game review and cross-game history
```

Archive compatibility may remain broader than active recovery compatibility. Old archive records should remain
readable where practical, but that requirement must not force the active recovery format to carry long-term
migration baggage.

A strict active-game restore parser must never become the archive-review parser.

## 5. Active campaign sequence

### PS0 — Freeze the recovery product contract

Status: **complete at planning level**.

Frozen requirements:

- one recent active recovery only;
- 12-hour initial horizon;
- no user-facing long-term Save Game contract;
- no cross-version active-recovery compatibility promise;
- exact UI/runtime reconstruction is not required;
- confirmed game facts and already-published information must survive;
- unconfirmed transient UI may be discarded;
- recovery must enter a safe, non-duplicating continuation point.

Implementation details such as the exact compatibility token remain owned by PS2; do not rebuild the old content
identity framework merely under a new name.

### PS1 — Separate Archive from Recovery

Status: **complete on draft PR #112**.

Goal: remove the product/data-model assumption that the active recovery snapshot is also the archive payload.

Required results:

- introduce a typed `GameArchiveRecord` or equivalent narrow archive projection;
- archive write no longer consumes the active recovery JSON object;
- preserve existing user-visible archive/review content;
- preserve old archive-read compatibility where currently supported;
- do not route archive review through strict recent-recovery validation;
- add behavior tests around archive projection/legacy reading where needed.

Do not yet change lifecycle persistence timing.

PS1 validation completed with the focused `GameArchiveJsonCodecTest`, `:app:testFast`, `git diff --check`, and an exact App wiring diff audit. The App wiring change is limited to archive decode/store/restart anchors; active Recovery snapshot generation, restore semantics and lifecycle save triggers were not changed.

### PS2 — Introduce minimal typed `RecoverySnapshot`

Status: **next slice; not started**.

Goal: replace “serialize App runtime” with a typed recovery model containing common envelope + game-specific
payloads.

Required shape:

```text
RecoveryEnvelope
├── recoveryFormatVersion
├── compatibility token
├── savedAtMillis
└── game
    ├── UndercoverRecovery
    ├── WerewolfRecovery
    └── ClocktowerRecovery
```

`ClocktowerRecovery` should be grouped by real concepts such as identity, position, players, durable mechanics,
history and bookkeeping. Do not create a flat 50–70-field God DTO.

The codec must be deterministic: capture time/compatibility inputs before encoding. Encoding owns wire format;
it must not derive game identity or inspect Compose/Android state.

At this stage, prefer changing the write model while keeping existing save trigger semantics stable.

### PS3 — Simplify restore around safe re-entry

Goal:

```text
Recovery JSON
-> parse + validate complete RecoverySnapshot
-> rebuild durable game state
-> choose safe continuation UI
```

Do not incrementally mutate live Compose state during parsing.

Recovery UI policy examples:

- Clocktower Day -> Day Overview unless a mandatory continuation requires a narrower mode;
- Clocktower Night -> safe current/next step reconstructed from durable facts; unconfirmed selection is re-entered;
- Klutz / unresolved Demon succession -> restore the mandatory continuation;
- already-confirmed vote/mechanical facts must not be replayed;
- Werewolf retains the minimum night continuation needed to avoid repeating already-performed role interactions.

Landing/Setup preview must consume typed recovery metadata rather than independently re-parsing raw JSON keys.

### PS4 — Retire superseded active-save infrastructure

After the new write/read path is proven, delete obsolete active-save responsibilities rather than leaving dual
schemas indefinitely.

Candidates include:

- old v3-only active-save compatibility plumbing that exists only to support long-lived full restore;
- separate raw saved-game preview parser;
- duplicated Clocktower checkpoint key writes;
- persistence of Clocktower draft fields and other transient UI;
- committed setup provenance that has no post-start gameplay consumer;
- unreachable legacy active-save branches/shims;
- tests that only protect removed schema/implementation details.

Retain real archive compatibility and durable semantic/history tests.

### PS5 — Simplify persistence triggers

Only after the new recovery model/restore behavior is stable, audit the current blanket synchronous write path.

Target direction:

```text
durable game transaction committed
-> recovery becomes dirty / checkpointed

ON_PAUSE / ON_STOP
-> last-chance durable write
```

Do not change save contents and save timing in the same early checkpoint unless a failing behavior forces it.
A4 durability ordering must remain protected where observation publication currently depends on successful
persistence.

## 6. Validation strategy

This campaign changes a durability contract, so behavior tests are justified even when structural source tests
are not.

Required coverage should include representative Undercover, Werewolf and Clocktower recovery states and the
highest-risk boundaries:

- confirmed fact survives restart;
- unconfirmed Clocktower draft is intentionally discarded/re-entered;
- published information/history is unchanged;
- ghost-vote/highest-vote state survives while unconfirmed current nomination does not;
- unresolved mandatory continuation survives;
- Werewolf night continuation does not force already-performed social interactions to repeat;
- expired/incompatible recovery fails closed without partially mutating live state;
- archive review remains readable after archive/recovery separation;
- no duplicate semantic action/observation occurs because of recovery.

Use frozen representative recovery fixtures where wire compatibility itself is the contract. Do not prove a
codec by encoding and immediately decoding the same object only. Avoid source-string tests for normal behavior.

At logical checkpoints use the project risk-based test policy from `AGENTS.md` / `TESTING_STRATEGY.md`.
Persistence/restore/transaction boundaries merit focused behavior coverage and a full validation gate before
merge. Real-device recovery testing is required before this campaign is considered complete.

## 7. Explicit non-goals

The current campaign does **not** include:

- D6 App/Host large-file decomposition;
- a global ViewModel migration;
- DataStore migration merely for modernization;
- long-lived save slots or manual Save/Load UX;
- cross-version active-game migration framework;
- recommendation-quality redesign;
- A4/ZDD production cutover;
- unrelated Host UI redesign.

File-size reduction is a welcome consequence, not an acceptance metric.

## 8. D6 status — deferred and must be re-audited

Night Step decomposition D1–D5 is complete and integrated through PR #106. The subsequent crash/initialization/
Poisoner corrections #107, #108 and #110 are also integrated into current `main`.

The previous D6 plan assumed preservation and decomposition of the current full active-save system. That premise
is now invalid. Draft PR #111 was closed without merge.

After Persistence Simplification is completed and merged:

1. re-fetch live `main`;
2. re-measure App/Host ownership and change-context hotspots;
3. ignore the old D6 sequence except as historical evidence;
4. write a fresh D6 ownership/decomposition audit against the reduced codebase;
5. then execute the separately approved decomposition campaign.

Do not mix large-file extraction into PS1–PS5 simply because persistence code moves naturally. Move code only
when required to establish the new recovery/archive responsibility boundary.

## 9. Other pending roadmap items

### UI-R5 — real-device stabilization

Still open. Prior CI built debug APKs, but successful workflow execution is not physical-device acceptance.
Persistence Simplification itself will require targeted process-death/restart/device testing. The broader UI-R5
matrix remains after the fresh D6 campaign unless a release-blocking UI defect is encountered earlier.

### EPI-MQ — information quality / Productive Uncertainty

Remain deferred until the current structural/recovery work is stable. The accepted future direction is to reject
obviously unreasonable unreliable clues and optimize useful uncertainty/evil-side playability without returning
committed shown-identity authority to recommendation.

### Beginner Mode

Deferred until the cognitive-consistency/information-quality foundation is ready. Product direction remains:
minimal user choices, automatic clues, unreliable drunk/poison information policy, and automatic Spy/Recluse
false registration policy suitable for inexperienced Storytellers.

### UX-R6

Legacy recommendation-provider replacement remains after EPI-MQ as currently planned.

### A4 / ZDD

Remain non-production. Do not treat this persistence campaign as authorization for rollout.

## 10. Current references

Active campaign documents:

- `docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md`

Long-lived engineering authority:

- root `AGENTS.md`
- `docs/TESTING_STRATEGY.md`
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`

Prior Night Step decomposition/reference evidence remains historical support, not the active execution route:

- `docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`
- PR #106 and its merged commits/checks
- closed PR #111 for the superseded D6 plan

## 11. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution and architecture/test rules;
3. this roadmap controls current project state and priority;
4. the active Persistence Simplification handoff controls the approved narrow campaign plan;
5. specialized design docs control their own semantic/product domain where non-conflicting;
6. archive documents, old Git branches and historical PR records are evidence only.