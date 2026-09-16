# NEXT DEVELOPMENT HANDOFF — FN-BUNDLE-1 Shown-Role Semantics

> Updated: 2026-09-16 Australia/Sydney  
> Status: **CURRENT / FN-BUNDLE-1 T4 acceptance checkpoint**  
> Current route decision: `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`  
> Prior `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`: **superseded as execution authority**

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/EPI_MQ_FIRST_NIGHT_BUNDLE_ROUTE_2026-09-16.md`;
6. query live `main`, PR #140 and current checks before editing.

Do not use dated handoffs in `docs/archive/` as execution authority.

## 1. Current repository state

FN-BUNDLE-0 is complete.

PR #139 was squash-merged into `main` with code baseline commit:

`55da0b8366eae6daa01df9bef668c56b6eecfaec`

That merge includes both:

- the FN-BUNDLE-0 candidate-space/live-seam audit; and
- retirement of the obsolete setup-owned pair-information route.

After the merge, documentation-only commits advanced `main` to `dbfeec7417f42efe6357f8055b84d8a42eff064b` at the start of this FN-BUNDLE-1 continuation. Treat `55da0b8` as the merged FN-BUNDLE-0 **code baseline**, not as a permanently current SHA, and always query live `main` before editing.

## 2. Pair-information ownership cleanup is complete

The historical duplicated route:

```text
SetupCandidateGenerator.generatePairInformationCandidates()
→ SetupRecommendationModule.naturalPairCandidates()
→ coordinator/precompute forwarding
```

has been retired as a rules/semantic owner.

Canonical ownership is now:

```text
NaturalPairInformationCandidateGenerator
        ↓
PairInformationLegalDomain / canonical consumers
```

The precompute transport may remain for compatibility/performance purposes, but pair candidates are sourced from the canonical generator. Do not recreate setup-owned pair semantics.

**Do not repeat FN-BUNDLE-0 candidate-space or setup-pair ownership audits.**

## 3. Current active PR — #140

PR #140:

`FN-BUNDLE-1: materialize first-night information for exact evaluation`

Branch:

`fn-bundle-1-evaluator-correctness`

The branch was synchronized with live `main` before the ShownRoleAt work continued.

The executable implementation immediately before this documentation-only T4 checkpoint was:

```text
head: 87e3489aab0812e6475357f41632c0ea80a3a293
main: dbfeec7417f42efe6357f8055b84d8a42eff064b
```

This handoff update is intentionally a **documentation-only `[full-ci]` checkpoint commit**. It exists to force the repository's T4 acceptance route without changing executable behavior.

Do not merge #140 without explicit user authorization.

## 4. FN-BUNDLE-1 implementation now present in #140

### First-night information materializer

`FirstNightInformationPropositionMaterializer.kt` remains a thin adapter and now has the intended FN-BUNDLE-1 coverage:

- Washerwoman/Librarian/Investigator pair information converts already-legal pair values into epistemic propositions;
- Librarian zero-Outsider information converts to absence claims over script Outsiders;
- Chef and Empath numeric information converts without reimplementing truth calculation;
- Empath subject seats reuse the existing living-neighbour rule owner;
- Spy/Recluse registration semantics remain owned by the epistemic/rules evaluation path;
- malfunction semantics remain owned by the epistemic evaluator.

### Shown-role proposition

The shared epistemic contract now includes:

```text
InformationProposition.ShownRoleAt(seat, role)
```

Its exact semantics are deliberately distinct from actual identity:

```text
world.shownRolesBySeat[seat] == role
```

Boundaries now enforced:

- `ShownRoleAt` is not `RoleAt`;
- it does not use Spy/Recluse registration;
- it is not loosened merely because the player's ability is malfunctioning;
- it does not expose hidden actual identity;
- an actual Drunk shown Chef can satisfy `ShownRoleAt(seat, Chef)` without emitting `RoleAt(seat, Drunk)`.

### InformationProposition fanout audit

The ShownRoleAt change was audited through the shared proposition fanout before completion. The affected paths include:

- proposition declaration and seat-reference validation;
- every compiler-exposed exhaustive `when` consumer;
- exact/enumerated world observation evaluation;
- knowledge-construction boundary validation;
- recovery semantic-history validation;
- canonical semantic JSON encode/decode;
- existing shown-role exact-world representation and ZDD/exact fallback path;
- workflow-selected exact/oracle validation.

The initial CI compile exposed two missed exhaustive consumers (`KnowledgeConstructionInput` and `RecoveryRestorePlanner`); both were fixed rather than hidden behind catch-all branches.

### Exact world shown-role state

Trouble Brewing exact enumeration now records shown-role identity explicitly for ordinary non-Drunk players. A Drunk's shown Townsfolk identity is recorded only where the recipient-visible knowledge actually provides it. This keeps `ShownRoleAt` as a direct world-state query rather than silently falling back to actual identity.

### PUBLIC_GOOD_INFO projection

`FirstNightPublicGoodInfoProjection` now projects, for each sharing source:

```text
public shown-role claim
+
public clue observation
```

with these constraints:

- shown identity is derived from the observation's represented shown/perceived ability, not hidden actual identity;
- duplicate `(sourceSeat, shownRole)` claims are emitted at most once per projected bundle;
- ordering and observation IDs are deterministic;
- identity claims are `NOT_ABILITY_INFORMATION` and have no `sourceAbility` malfunction coupling;
- clue observations remain separate observations;
- latent Red Herring/demon-bluff entries remain outside this public-sharing profile.

## 5. Required fixtures now present

### Healthy confirmation chain

The exact fixture now covers a representative public conjunction containing:

```text
Washerwoman claims Washerwoman
+ Washerwoman clue supports Empath among two seats
+ Empath claims Empath
+ Empath shares 0
```

It proves:

- the projection contains both shown-role claims and clue observations;
- exact AFTER is the conjunction over the complete projected bundle;
- shown-role claims materially tighten the surviving world set compared with clue-only filtering.

### Drunk shown-role semantics

A separate fixture covers an actual Drunk shown Chef and proves:

- PUBLIC_GOOD_INFO emits `ShownRoleAt(drunkSeat, Chef)`;
- the claim matches a world whose `shownRolesBySeat[drunkSeat] == Chef`;
- no public identity proposition exposes actual `Drunk`;
- shown-role identity still matches under `FUNCTIONING_ONLY` because it is not ability information;
- the false Chef clue remains independently accepted only by the existing malfunction-aware hypothesis path;
- an incorrect shown-role claim is rejected.

The fixture was deliberately kept local to the exact semantic contract instead of constructing an unnecessary complete six-player exact baseline, after CI demonstrated that the broader construction added memory cost without stronger evidence.

### Canonical JSON

A dedicated semantic contract test proves ShownRoleAt canonical encode/decode round-trip and structural JSON fields without depending on brittle textual object-key ordering.

## 6. Validation history before this T4 checkpoint

Useful intermediate CI evidence:

- R2 main-thread boundary: green;
- Android compile fanout failures were found and fixed;
- subsequent FAST run reached 1364 tests and exposed only the two new-test issues described above;
- those test issues were corrected without weakening production contracts;
- Real Clingo frozen-oracle cross-validation was green on the executable ShownRoleAt implementation before the final test-only fixes;
- normal PR CI on `87e3489` is green and FAST passes.

However, under `docs/TESTING_STRATEGY.md`, ordinary synchronize-event FAST CI is **not** sufficient for this logical acceptance checkpoint.

## 7. T4 acceptance checkpoint — current task

This documentation-only commit intentionally uses `[full-ci]` in its commit message.

The workflow must therefore select the full checkpoint:

```text
android=true
android_full=true
asp=true
oracle=true
```

Required acceptance evidence on the **current PR head**:

- Android `:app:testFull` passes;
- `:app:assembleDebug` passes in the same full Android job;
- ASP golden corpus validation passes;
- ASP harness Python tests pass;
- Real Clingo cross-validation passes;
- aggregate CI gate passes;
- R2 main-thread boundary passes.

Do not substitute a previous FAST-only green run for this T4 checkpoint.

## 8. FN-BUNDLE-1 completion condition

Implementation requirements are now satisfied:

```text
[x] InformationProposition fanout audited
[x] ShownRoleAt implemented at the correct semantic owner
[x] exact evaluator matches shownRolesBySeat directly
[x] no registration/malfunction leakage into shown-role identity semantics
[x] PUBLIC_GOOD_INFO adds deterministic deduplicated shown-role claims
[x] healthy confirmation-chain fixture implemented and FAST-green
[x] Drunk shown-role fixture implemented and FAST-green
[x] existing materializer/registration/numeric fixtures remain FAST-green
```

Final acceptance requirement:

```text
[ ] current #140 `[full-ci]` head has required T4 CI green
```

**When that live current-head T4 run is green, FN-BUNDLE-1 is complete and #140 is ready for explicit merge authorization.**

Until then, do not begin FN-BUNDLE-2 whole-7-player harness as the main implementation task.

## 9. Next stage after #140

FN-BUNDLE-2 builds the complete healthy 7-player first-night harness.

Use the FN-BUNDLE-0 evidence:

```text
raw representative product: 110,000
represented PUBLIC_GOOD_INFO factor product: 100
```

Strategy:

1. compose complete legal bundle IDs;
2. materialize public projection including shown-role claims;
3. canonicalize projected observation signatures;
4. group complete bundle provenance/multiplicity by identical signature;
5. exact-evaluate each distinct signature once;
6. use deterministic bounded sampling only if the quotient remains too large and measured cost justifies it.

Do not retreat to independent per-role scoring.

## 10. Product/architecture rules that remain stable

Target product behavior remains:

```text
legal complete bundles
→ exact/capability-aware diagnostics
→ BEGINNER Badness rejection
→ uniform random among acceptable survivors
```

Ownership remains:

```text
rules          -> legality / registration semantics
session        -> actual state / timeline / commit
epistemic      -> exact recipient-visible consequence semantics
recommendation -> bundle composition / Badness / survivor selection
UI             -> presentation / confirmation
```

General-purpose LLM recommendation remains deferred.

Do not introduce final Badness thresholds before the human-reviewed corpus exists.

## 11. Stable rule

> **FN-BUNDLE-0 is finished and merged; do not repeat its candidate-space or setup-pair ownership audits. FN-BUNDLE-1 code is implemented through ShownRoleAt/public identity projection and its required fixtures. Treat the current `[full-ci]` run as the final T4 acceptance gate; if it is green, stop for explicit #140 merge authorization before beginning FN-BUNDLE-2.**
