# MS-S1R — Exact Setup Persistence Authority Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **COMPLETE / ACCEPTED**

## Accepted checkpoint

Final accepted branch/code-test head:

`2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e`

Key product cutover commit:

`e5e0fd5051aa8b4347894e0e1cfc745fae846e97` — `feat(ms-s1r): restore TB completion summary directly`

Validation:

```text
CI #1220 / run 33356512627                 SUCCESS
Android full unit tests + debug APK         SUCCESS
Real Clingo cross-validation                SUCCESS
ASP contract tests                          SUCCESS
CI aggregate gate                           SUCCESS
R2 #1137 / run 33356512640                  SUCCESS
```

Before final legacy retirement, the replacement contracts and `:app:testFast` also passed in the retirement runner after deleting the superseded legacy provenance codec/test. The runner stopped only on a staged-vs-unstaged diff-audit command error; the validated deletions were then applied directly through the GitHub connector and the final branch head passed the normal CI/R2 gates above.

## Accepted architecture

Setup recovery authority is now:

```text
active save
├─ exact CommittedClocktowerSetup
│  ├─ script
│  ├─ setup seed
│  ├─ ordered seats
│  │  ├─ actual role
│  │  └─ shown role
│  └─ provenance metadata
└─ Trouble Brewing compact completion/diversity record
   ├─ dataset/provider identity
   ├─ preset/candidate identity
   └─ rotation-scoring summary fields
```

Restore is direct:

```text
persisted exact committed setup
-> decode + validate
-> CommittedClocktowerSetup
```

Trouble Brewing completion/diversity history is restored from its compact committed record rather than by reconstructing `TroubleBrewingSetupPresetSelection` from the current 480-preset dataset.

## Retired authority

The following setup-recovery authority is no longer used:

```text
TB provenance metadata
-> load current trouble_brewing_setup_presets_v2_final.json
-> find preset by dataset/schema/player count/preset id
-> reconstruct TroubleBrewingSetupPresetSelection
```

Superseded implementation/tests retired in MS-S1R include:

- `TroubleBrewingSetupProvenancePersistence.kt`;
- `TroubleBrewingSetupProvenancePersistenceTest.kt`;
- the old App-root provenance wiring test whose contract required selection/provenance reconstruction.

A call-site audit proved the legacy provenance codec had no surviving production consumer before retirement. The surviving production wiring guard now explicitly forbids reintroducing that restore path.

## Protected behavior retained

MS-S1R preserves:

- exact actual and shown identities after restore;
- stable seat ordering;
- setup seed identity;
- setup provenance as audit/source metadata, not reconstruction authority;
- Trouble Brewing completion/diversity history based on the originally committed setup selection summary;
- true-completion gating before rotation history writes;
- archive/active-save clearing only after completion-history persistence;
- no selector/preparer/recommendation/random rerun during restore;
- no template dataset lookup during current Trouble Brewing restore;
- fail-closed validation when exact setup and compact completion metadata disagree.

Current supported Trouble Brewing saves require the new exact committed setup and compact completion/diversity record. The product does not promise indefinite compatibility with every older unfinished-game save format.

## Deliberate non-goals

MS-S1R did not change general unfinished-night recovery semantics. In particular it did not broaden into:

- `ClocktowerNightCheckpoint` draft cleanup;
- exact `nightStepIndex` simplification;
- Attack/Poison/Monk/Mayor/succession draft recovery;
- `NightTransactionRestoreComposition` redesign;
- Dawn/Dusk retry/idempotency authority;
- Host/App decomposition for its own sake.

Those remain outside this slice; broad unfinished-game simplification belongs to future REC-R1.

## Next slice

MS-S2 is next:

```text
generic SetupCandidate
+ candidate-source contract
+ script setup policy/provider registry
```

MS-S2 should remain persistence-independent and should not yet implement optional template repositories, generated setup algorithms, common diversity scoring, or TB/NGJ production cutover beyond the smallest contracts needed to establish generic ownership.
