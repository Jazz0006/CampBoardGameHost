# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S1 COMPLETE / ACCEPTED — MS-S1R NEXT**

## 1. Live baseline and accepted checkpoints

Campaign start baseline:

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

PR #57:
MERGED / CLOSED
```

Current campaign branch:

`codex/ms-setup-generic-architecture`

Current campaign PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

Accepted MS-S1 code/test checkpoint:

`f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b`

Accepted validation:

```text
CI #1187 / run 33351536770                SUCCESS
Android FAST unit tests                   SUCCESS
Real Clingo cross-validation              SUCCESS
CI aggregate gate                         SUCCESS
R2 #1104 / run 33351536807                SUCCESS
```

Later docs-only commits are carriers and do not replace the validated MS-S1 code/test head.

Always re-query live GitHub state before a production write.

## 2. Product goal

Build one script-neutral Clocktower setup pipeline:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup policy/provider
-> query optional template candidates
-> templates exist: validated template candidates
-> no templates: legal generated candidates
-> common deterministic diversity selector
-> commit shown-identity decisions
-> CommittedClocktowerSetup
```

The setup engine ends at `CommittedClocktowerSetup`.

Persistence/recovery is an outer consumer. Setup generation must not depend on Android storage, active-session restore, Host UI state, or unfinished-game recovery.

The App root must not gain a new `if (script == ...)` setup branch when a future script is added.

## 3. Ownership audit — MS-S0 COMPLETE

Current production ownership remains approximately:

```text
App start wiring
├─ Trouble Brewing
│  ├─ frozen 480-preset dataset + validator
│  ├─ TB preset selector
│  ├─ TB rotation history/scorer
│  ├─ selector-owned Drunk shown-role choice
│  ├─ TB deterministic deal materializer
│  └─ committed PlayerCards + TB-specific provenance
└─ No Greater Joy / no-template
   ├─ script role definitions + player-count distribution
   ├─ legacy broad/random composition path
   ├─ legacy Drunk shown-role handling
   └─ committed PlayerCards

prepared cards + seed
-> ClocktowerJudgeScreen / Host orchestration
-> session/domain transactions
```

`ClocktowerJudgeScreen` consumes prepared `cards`, `script`, `gameSeed`, `ClocktowerNightCheckpoint` and callbacks. It does not own initial setup generation/materialization. New setup policy/randomization must not move into Host.

NGJ's current generation uses unseeded shuffle/random behavior before the game seed is established. MS-S4/MS-S8 will introduce deterministic seeded generation while preserving legality and user-visible semantics; exact legacy random-sequence parity is not required.

## 4. Recovery scope decision — MS-S0.5 COMPLETE

Detailed audit:

`docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md`

The product no longer promises arbitrary exact continuation of an unfinished game after exit/restart.

Supported recovery goal:

```text
best-effort crash / Android process-death recovery
-> latest supported stable committed domain checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart at the next safe domain/action boundary
```

Explicit non-goals:

- “play half today and continue tomorrow” as a product contract;
- exact restoration to arbitrary in-progress UI state;
- durable draft persistence solely to recreate a partially completed interaction;
- indefinite cross-version support for every unfinished-save shape.

Committed-domain retry/idempotency/convergence remains protected. Completed-game setup/diversity history remains separately durable.

## 5. Recovery work split

Use replacement-first migration:

```text
introduce replacement authority
-> cut consumers over
-> prove parity / call-site ownership
-> retire superseded legacy path
```

### MS-S1R — setup persistence authority migration

MS-S1R is directly coupled to setup ownership and is the next slice.

Target:

```text
OLD
TB provenance / source metadata
-> reload current TB template data
-> reconstruct setup selection

NEW
exact CommittedClocktowerSetup
-> persist as authoritative setup fact
-> restore exact CommittedClocktowerSetup

provenance
-> source/audit metadata only
```

MS-S1R may retire TB setup-reconstruction plumbing only after exact generic setup persistence and direct restore are proven.

MS-S1R must not broaden into general unfinished-night recovery cleanup.

### REC-R1 — separate future campaign

REC-R1, outside MS-SETUP, will later re-evaluate exact-resume behavior in `ClocktowerNightCheckpoint`, `nightStepIndex`, attack/poison/Monk/Mayor/succession draft persistence, `NightTransactionRestoreComposition`, and tests that exist only for exact mid-interaction continuation.

REC-R1 must retain anything required for runtime confirmed-vs-draft separation or committed transaction correctness.

## 6. Campaign sequence

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                       COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance   COMPLETE / ACCEPTED
MS-S1R  exact setup persistence authority migration + TB setup-restore retirement NEXT
MS-S2   generic SetupCandidate + source contract + setup policy/provider registry
MS-S3   optional TemplateRepository keyed by script + player count
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S7   adapt TB 480-preset pipeline; preserve TB behavior/parity
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   acceptance: future no-template script needs no App-root branch; templates are provider/data registration only

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement multiple slices at once merely because they share the campaign.

## 7. MS-S1 accepted result

Authoritative checkpoint:

`docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md`

Accepted production owner:

`app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt`

Accepted typed test:

`app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt`

Accepted contract:

```text
CommittedClocktowerSetup
├─ script: ScriptId
├─ setupSeed: Long
├─ assignments: ordered List<CommittedSetupSeat>
│  ├─ seat: Int
│  ├─ actualRole: RoleId
│  └─ shownRole: RoleId
└─ provenance: SetupProvenance
   ├─ sourceKind: TEMPLATE | GENERATED
   ├─ providerId: String
   └─ candidateId: String?
```

Accepted invariants include canonical ordered seats `1..N`, explicit actual/shown roles, nonblank provenance identity, assignment-list snapshotting, structural equality/hash identity, and no Android/session/persistence dependency.

Persistence schema/version is deliberately outside this domain model and belongs to MS-S1R. `playerCount` is derived from assignments.

## 8. MS-S1R immediate objective

Migrate only **setup persistence authority**.

Before implementation, audit these exact current responsibilities and call sites:

1. TB active-game provenance codec/model;
2. active-game save serialization fields for committed TB setup;
3. restore decode path and any 480-preset dataset lookup;
4. `committedTroubleBrewingSetupSelection` lifecycle in App;
5. completion-history use of original committed selection;
6. existing typed persistence/restore tests proving selector/preparer is not called on restore;
7. older-save compatibility behavior.

Then design the smallest generic persistence boundary around the already accepted `CommittedClocktowerSetup`.

## 9. MS-S1R required contract

Target durable payload must be sufficient to restore exact setup facts without consulting candidate sources:

```text
schema/version at persistence boundary
script identity
setup seed
ordered exact seats
  actualRole
  shownRole
provenance
  sourceKind
  providerId
  candidateId?
```

Restore authority:

```text
persisted exact committed setup
-> decode/validate
-> CommittedClocktowerSetup
```

Forbidden restore behavior:

- load template repository to infer roles;
- invoke preset selector/preparer;
- choose Drunk/shown identity again;
- invoke recommendation;
- invoke random generation;
- silently repair an invalid persisted exact setup by selecting another one.

## 10. MS-S1R evidence strategy

Because this slice changes persistence/restore authority, use typed behavior evidence rather than source-string ceremony.

Minimum durable evidence should prove:

1. exact codec/checkpoint round-trip preserves script, seed, seat order, actual roles, shown roles and provenance;
2. direct restore returns the same `CommittedClocktowerSetup` facts;
3. selector/preparer/template source is not required to restore the exact setup;
4. invalid/corrupt payload fails explicitly rather than rerolling/reselecting;
5. deliberately supported legacy TB save compatibility remains correct until its retirement trigger;
6. completion-history logic still records the original committed setup/selection, not a reconstruction.

If production cutover touches the large App root, follow the required large-file one-shot workflow rather than unsafe whole-file replacement.

Persistence/schema changes may justify earlier T1/T2/T3 escalation under `docs/TESTING_STRATEGY.md`.

## 11. MS-S1R retirement boundary

Delete/simplify old TB setup restore code only when all of the following are true:

- exact generic committed setup persistence exists;
- restore uses it as authority;
- call-site audit proves the old reconstruction path is no longer required for supported saves;
- affected typed tests are replaced/narrowed according to the repository test-retirement policy;
- completion/diversity history still has the original committed setup identity it needs.

Do not delete general `ClocktowerNightCheckpoint`, `NightTransactionRestoreComposition`, Dawn/Dusk recovery authorities, or unrelated draft fields in MS-S1R.

## 12. Protected predecessor invariants

Preserve throughout MS-SETUP:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random TB setup.
Identity reveal does not synchronously block on recommendation/First Night computation.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup.
```

Also preserve committed-domain correctness unrelated to arbitrary resume:

- Dawn poison exactly-once / retry convergence;
- Dusk/next-night poison expiry exactly-once / retry convergence;
- Fortune Teller current/effective-state authority;
- poisoned Spy fail-safe semantics;
- current living-Demon UI authority;
- NGJ setup legality/current behavior until explicit migration parity proof.

## 13. Workflow

Follow:

- root `AGENTS.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` for large/truncated production files.

Use risk-based evidence, not RED ceremony.

For large files:

```text
safe small/medium file
-> GitHub connector direct edit

large/truncated file + stable unique anchors
-> GitHub Actions one-shot workflow + separate Python patch script

one-shot cannot be made safe / complete local worktree genuinely required
-> Codex/Luna
```

## 14. Immediate next action — MS-S1R audit first

Before writing MS-S1R production code:

1. re-confirm live `main`, branch head, Draft PR #61 and checks;
2. inspect existing TB provenance codec/models/tests;
3. inspect App active-save serialization and restore decode anchors without editing the large file yet;
4. map supported legacy-save compatibility requirements;
5. freeze the smallest exact generic codec/checkpoint and typed RED/evidence plan;
6. only then implement the small codec/test seam;
7. cut over App persistence using the safe large-file path if needed;
8. retire superseded TB reconstruction only after replacement proof.

## 15. Explicit non-goals for MS-S1R

Do not broaden into:

- general unfinished-game recovery cleanup;
- Mayor redirect redesign;
- Imp succession redesign;
- Monk/Attack-Protect replay;
- A3/A4/ZDD;
- Host/App decomposition for its own sake;
- regeneration/reformatting of the frozen TB preset dataset;
- MS-S2 candidate-source design before MS-S1R is accepted;
- PR Ready/merge changes.

Keep PR #61 Draft. Do not merge, mark Ready, force-push or rebase without explicit user authorization.
