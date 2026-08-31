# MS-S1 CommittedClocktowerSetup Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Branch: `codex/ms-setup-generic-architecture`  
> Status: **IMPLEMENTED — VALIDATION PENDING**

## Scope

MS-S1 introduces only the persistence-independent exact committed setup domain fact and generic provenance metadata.

Production:

`app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt`

Typed evidence:

`app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt`

## Contract

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

Persistence schema/versioning is deliberately absent and belongs to MS-S1R.

## Invariants

- non-empty exact assignments;
- canonical seat order `1..N`;
- explicit actual and shown roles;
- non-blank provider identity;
- non-blank candidate identity when present;
- provenance is metadata, not reconstruction authority;
- no Android/session/UI/persistence dependency;
- no TB-only mandatory metadata.

## Scope exclusions

No App, Host, TB adapter, NGJ adapter, persistence codec, restore wiring, candidate generation, diversity selection or shown-identity policy cutover is part of MS-S1.

## Validation status

The test-first commit was created before the production type. No branch CI auto-runs on ordinary feature-branch push; normal CI will be obtained through the campaign Draft PR.

Do not mark MS-S1 accepted until the focused/current-head CI evidence is green and the remote diff remains within the allowed scope.
