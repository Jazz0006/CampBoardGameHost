# MS-S6B Shown Identity Commitment Checkpoint — 2026-08-31

Status: **COMPLETE / ACCEPTED**

Branch: `codex/ms-setup-generic-architecture`

Draft PR: `#61`

Accepted code/test checkpoint:

`d4cf3969aabcea7433b96b5b320171fbc821853e`

Test-first RED checkpoint:

`afc970cc9006ced2de24a99bcaa8d789a1d7a11a`

## Validation

```text
RED:
afc970cc9006ced2de24a99bcaa8d789a1d7a11a
CI #1256 / run 33365203015   EXPECTED RED
:app:testFast reached compileDebugUnitTestKotlin and failed on missing S6B commitment types
R2 #1173 / run 33365203021   SUCCESS

GREEN:
d4cf3969aabcea7433b96b5b320171fbc821853e
CI #1257 / run 33365333667   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1174 / run 33365333672   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Exact S6B diff from the pre-S6B docs carrier `7b68df45c44bfec8afdc545e637e5465c1dc08e0` contains exactly:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitment.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitterTest.kt
```

No existing production source was modified.

## Ownership / API audit result

The S6B audit confirmed:

- stable `setupSeed` already exists upstream in `SetupCandidateRequest` and downstream in `CommittedClocktowerSetup`; it does not belong in `SetupCandidate`;
- `SetupCandidate` remains the selected actual-role-only pre-seat composition;
- S6A already owns legality and returns canonical `SetupShownIdentityPolicy` option pools;
- S6B therefore needs only a small pre-seat commitment value rather than seat assignments, `PlayerState`, persistence data or recommendation decisions;
- `CommittedClocktowerSetup` remains a later materialized exact setup fact and is not constructed in S6B;
- the legacy TB `tb-drunk-v1` hash namespace is tied to dataset/preset selection semantics and is not reused as generic commitment authority.

## Accepted generic contract

S6B introduces:

```text
ShownIdentityCommitment
├─ actualRole
└─ shownRole

SetupShownIdentityCommitment
├─ canonical overrides: 0..N ShownIdentityCommitment
└─ shownRoleFor(actualRole)

SetupShownIdentityCommitter
└─ commit(
     selected SetupCandidate,
     resolved SetupShownIdentityPolicy,
     setupSeed,
   )
   -> SetupShownIdentityCommitment
```

Only roles whose shown identity differs from their actual role are stored as override facts.

For any role without an override:

```text
shownRoleFor(actualRole) == actualRole
```

Therefore an explicit S6A no-override policy produces an empty commitment while preserving ordinary actual=shown identity semantics.

S6B remains pre-seat. It does not create `CommittedSetupSeat` or `CommittedClocktowerSetup`.

## Deterministic selection

Each S6A override is selected independently from its canonical legal option pool.

The generic commitment namespace is:

```text
setup-shown-identity-v1
```

The stable hash material includes:

```text
script
candidate source kind
providerId
candidateId when present
canonical selected actual-role composition
override actualRole
canonical legal shown-role options
setupSeed
```

Fields are length-prefixed before hashing so delimiter-like text cannot ambiguously collapse adjacent fields.

Selection uses existing `MurmurHash3.low64Utf8` plus unsigned remainder into the canonical legal option list.

Consequences:

- identical candidate + policy + seed gives identical commitment;
- caller input ordering cannot affect the result;
- different setup seeds can explore different legal shown identities when multiple options exist;
- a single legal option commits directly;
- no unseeded `.random()` / `.shuffled()` participates;
- no shown-role history/cooldown participates in the first generic implementation.

## Fail-closed behavior

S6B does not invent or expand legality.

Before selecting an option, it requires:

- every override actual role to exist in the selected candidate;
- an override cannot show the actual role as itself;
- no legal shown option may already be an actual in-play role.

S6A constructors already guarantee non-empty unique option pools and canonical ordering. S6B consumes those accepted options and fails on inconsistent policy/candidate combinations rather than silently falling back.

## Typed evidence

`SetupShownIdentityCommitterTest` covers:

- same candidate/policy/setupSeed -> same legal commitment;
- canonical result independent of candidate and option input order;
- multiple setup seeds explore more than one legal option where choices exist;
- single-option deterministic commitment;
- explicit no-override policy preserving actual identities;
- multiple override facts canonicalized and independently deterministic;
- candidate and policy inputs remain unmodified;
- override actual role absent from candidate fails closed;
- shown option already in actual composition fails closed;
- shown option equal to actual override role fails closed;
- setupSeed materially participates in selection.

The test is pure Kotlin typed evidence; no source-string architecture assertion was introduced.

## Protected architecture

The causal order remains:

```text
Composition
-> Identity
-> Information
```

S6B does not change:

- `SetupCandidate`;
- `SetupCandidateRequest`;
- S5 candidate diversity/scoring/selection;
- S6A option legality/resolution;
- TB preset validator/model/legacy selector/scorer/deal flow;
- NGJ production flow;
- seat assignment or deal shuffle;
- `CommittedClocktowerSetup` materialization;
- `PlayerState.shownRole` production wiring;
- recommendation or `StorytellerDecision.DrunkShownRole`;
- persistence/recovery;
- App/Host.

Shown identity still cannot feed back into actual-role composition selection.

## Legacy semantics note

Current legacy TB shown-role selection used a TB-specific namespace containing dataset ID, player count, preset ID and game seed.

S6B deliberately does **not** preserve that implementation as generic authority. S7 may compare controlled TB cutover behavior where useful, but the generic identity commitment is now owned by `setup-shown-identity-v1` after composition and legality resolution.

This preserves the S4.5 architecture correction: shown identity is downstream of actual composition.

## Next slice

**MS-S6C — recommendation ownership inversion.**

Before implementation, audit:

- current `StorytellerDecision.DrunkShownRole` producers/consumers;
- all places where recommendation currently chooses or replaces Drunk shown identity;
- `PlayerState.shownRole` availability at recommendation boundaries;
- existing first-night information families that can consume an already-committed perceived role;
- which legacy tests/types can be narrowed or retired only after stable typed replacement evidence exists.

S6C must make recommendation consume committed shown identity and generate information only. It must not perform TB/NGJ production cutovers, seat/deal integration, persistence changes or App/Host expansion unless an explicit later slice owns those changes.

Do not start S6C automatically from this checkpoint.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.
