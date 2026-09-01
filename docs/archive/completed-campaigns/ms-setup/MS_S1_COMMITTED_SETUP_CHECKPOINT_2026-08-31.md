# MS-S1 CommittedClocktowerSetup Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Branch: `codex/ms-setup-generic-architecture`  
> Status: **ACCEPTED — MS-S1 COMPLETE**

## 1. Scope

MS-S1 introduces only the persistence-independent exact committed setup domain fact and generic provenance metadata.

Production:

`app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt`

Typed evidence:

`app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt`

No App, Host, TB adapter, NGJ adapter, persistence codec, restore wiring, candidate generation, diversity selection or shown-identity policy cutover is part of this slice.

## 2. Accepted contract

```text
CommittedClocktowerSetup
├─ script: ScriptId
├─ setupSeed: Long
├─ assignments: exact ordered seats 1..N
│  ├─ seat
│  ├─ actualRole: RoleId
│  └─ shownRole: RoleId
└─ provenance
   ├─ sourceKind: TEMPLATE | GENERATED
   ├─ providerId
   └─ candidateId?
```

The setup snapshots the caller-provided assignment list. `playerCount` is derived from the exact assignment count.

Persistence schema/versioning is deliberately absent from the domain model and belongs to MS-S1R.

## 3. Accepted invariants

- non-empty exact assignments;
- canonical ordered seat identity `1..N`;
- explicit actual and shown roles;
- non-blank provider identity;
- non-blank candidate identity when present;
- structural equality/hash identity for equivalent committed facts;
- caller-owned mutable assignment lists cannot mutate the committed setup after construction;
- provenance is metadata, not reconstruction authority;
- no Android/session/UI/persistence dependency;
- no TB-only mandatory metadata.

## 4. Test-first / implementation provenance

```text
test-first checkpoint:
a91edcc015586f3802f86ee93d517f2091aaa4f2

production checkpoint:
cb57ad2afdd6166da94f8d1fce454c900b8fe08b

validated MS-S1 code/test head:
f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b
```

The first test commit referenced the not-yet-created committed-setup types, providing the intended compile RED. Ordinary feature-branch pushes do not run CI, so no artificial temporary RED workflow was created.

The final test refinement added a durable structural-identity assertion and supplied a real `app/*` PR delta so the repository's incremental CI router exercised the owning Android FAST path on the exact accepted code/test head.

## 5. Validation

Exact validated head:

`f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b`

GitHub evidence:

```text
CI #1187 / run 33351536770                SUCCESS
Android FAST unit tests                   SUCCESS
Real Clingo cross-validation              SUCCESS
CI aggregate gate                         SUCCESS
ASP contract tests                        SKIPPED as not selected
R2 #1104 / run 33351536807                SUCCESS
```

Full Android/T4 was intentionally not selected for this small domain-contract slice; the repository's risk-based routing selected FAST Android + Real Clingo, which matches the current testing strategy.

## 6. Exact scope audit

Relative to the accepted planning baseline `3948cd7feb6643a636e1583d74be7e6c68266144`, the validated MS-S1 head changes only:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt
docs/archive/completed-campaigns/ms-setup/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md
```

No persistence/App/Host/TB/NGJ production wiring changed.

## 7. Next slice

MS-S1 is complete. The next slice is **MS-S1R — exact setup persistence authority migration**.

MS-S1R must:

1. re-audit the exact TB active-game setup persistence/restore call chain;
2. introduce a small generic codec/checkpoint representation for exact `CommittedClocktowerSetup`;
3. persist and restore the exact committed setup directly;
4. ensure restore does not invoke template loading, selector, shown-role chooser, recommendation or randomization;
5. preserve supported compatibility deliberately;
6. retire only TB setup-reconstruction plumbing fully superseded by the new authority;
7. stop before general unfinished-night/draft recovery cleanup.

Broad exact-resume simplification remains separate future `REC-R1` work.
