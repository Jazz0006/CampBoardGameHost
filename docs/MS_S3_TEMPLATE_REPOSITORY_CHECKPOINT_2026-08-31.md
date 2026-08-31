# MS-S3 Template Repository Checkpoint — 2026-08-31

Status: **COMPLETE / ACCEPTED**

Branch: `codex/ms-setup-generic-architecture`

Draft PR: `#61`

Accepted code/test checkpoint:

`6b15822e75680fb8e718f5db24358e1a935b5523`

Test-first checkpoint:

`07bcd40b4542eff28d7a033b638ae980744f858f`

## Validation

```text
CI #1230 / run 33357908514   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1147 / run 33357908443   SUCCESS
Full Android                 SKIPPED by risk router
ASP / Real Clingo            SKIPPED by risk router
```

Exact MS-S3 diff from the prior docs checkpoint contains only:

- `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepository.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepositoryTest.kt`

No App/Host/Trouble Brewing/No Greater Joy production wiring changed.

## Accepted contract

MS-S3 introduces a pure Kotlin optional template repository adapted to the MS-S2 `SetupCandidateSource` boundary.

```text
TemplateBucketKey
├─ script: ScriptId
└─ playerCount: positive Int

TemplateRepository
├─ immutable bucket snapshot
├─ find(script, playerCount) -> List<SetupCandidate>
└─ SetupCandidateSource.candidates(request)
```

The repository does not use `setupSeed`; template availability is keyed only by exact script + player count. A missing bucket returns an empty list and is a normal result for later generated-candidate fallback.

## Accepted invariants

- exact script + player-count lookup only;
- absent script/player-count returns an empty list;
- caller-owned map/list mutation cannot mutate repository contents;
- stored candidate script must match its bucket script;
- stored candidate player count must match its bucket player count;
- repository candidates must have `SetupSourceKind.TEMPLATE`;
- template candidates must have a durable non-null `candidateId`;
- duplicate `(providerId, candidateId)` identities within a bucket are rejected;
- returned candidate order is deterministic and canonicalized by provider ID, candidate ID, then canonical role composition;
- the repository directly implements `SetupCandidateSource` so later providers can use it without a second adapter.

## Ownership boundary

The generic repository deliberately does **not** own:

- Android `Context` or asset loading;
- JSON parsing;
- TB dataset schema/version/status;
- TB role/category/composition validation;
- TB runtime selection/scoring policy;
- diversity history or weighting;
- generated setup algorithms;
- seat assignment;
- Drunk/shown-identity commitment;
- persistence/recovery.

The existing `TroubleBrewingSetupPresetValidator` remains the owner of TB-specific dataset correctness, including role/team validation, composition rules, Baron constraints and Drunk shown-role option validation. MS-S3 does not weaken or replace those checks.

## Next slice

**MS-S4 — deterministic seeded legal `GeneratedSetupCandidateSource`.**

MS-S4 should generate legal pre-seat actual-role candidates through the MS-S2 boundary using the request seed. It must not absorb MS-S5 diversity scoring/selection, MS-S6 shown-identity commitment, or MS-S8 NGJ production cutover.

Keep PR #61 Draft. Do not merge or mark Ready without explicit user authorization.
