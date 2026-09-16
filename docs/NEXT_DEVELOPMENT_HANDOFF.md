# NEXT DEVELOPMENT HANDOFF — FN-BUNDLE-1 Shown-Role Semantics

> Updated: 2026-09-16 Australia/Sydney  
> Status: **CURRENT / canonical active handoff**  
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

After the merge, documentation-only commits advance `main`; therefore always query live `main` instead of assuming `55da0b8` is the current branch tip. Treat `55da0b8` as the merged FN-BUNDLE-0 **code baseline**, not as a permanently current SHA.

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

## 3. Current active PR — #140

PR #140:

`FN-BUNDLE-1: materialize first-night information for exact evaluation`

Branch:

`fn-bundle-1-evaluator-correctness`

The branch was rebuilt cleanly on the merged FN-BUNDLE-0 code baseline after #139 squash merge.

At the time of that rebuild:

```text
base code: 55da0b8366eae6daa01df9bef668c56b6eecfaec
head:      edcdbccdf6c8e2110afcae5d04219d347a2f2da7
files:     3 FN-BUNDLE-1 files only
```

Subsequent `main` movement is documentation-only. Before continuing, query and sync/rebase as appropriate so #140 is based on current main without reintroducing already-merged FN-BUNDLE-0 changes.

## 4. What #140 already contains

### Production adapter

`FirstNightInformationPropositionMaterializer.kt`

Current responsibilities:

- convert already-legal Washerwoman/Librarian/Investigator pair information to epistemic propositions;
- convert Librarian zero-Outsider information;
- convert Chef numeric information;
- convert Empath numeric information using existing living-neighbour structure;
- remain a thin adapter, not a legality/truth engine.

### Current tests

`FirstNightInformationPropositionMaterializerTest.kt`

currently proves representative:

- Washerwoman pair satisfied through Spy registration;
- Investigator pair satisfied through Recluse registration;
- Librarian zero-Outsider actual-target semantics;
- Chef/Empath numeric proposition structure;
- representative actual Drunk shown as Chef receiving false Chef information:
  - matches under `MECHANICALLY_CREDIBLE`;
  - does not match under `FUNCTIONING_ONLY`.

`FirstNightBundleExperimentExactFixtureTest.kt`

currently proves a compact healthy public clue bundle has exact AFTER equal to the same-baseline conjunction of its projected clue observations.

These are useful, but they do **not** finish FN-BUNDLE-1.

## 5. The important semantic gap

The `BEGINNER_PUBLIC_GOOD_INFO` experiment means players publicly reveal both:

1. the role identity they believe/show themselves to be; and
2. their first-night clue information.

Current `FirstNightPublicGoodInfoProjection.project()` only copies the clue observation to PUBLIC visibility.

That under-models confirmation chains.

Example:

```text
Washerwoman publicly claims Washerwoman
+ Washerwoman clue supports Empath identity
+ Empath publicly claims Empath
+ Empath publicly shares 0
```

Without public role claims, the exact evaluator does not receive the full public information structure the experiment intends to model.

## 6. Immediate task — complete `ShownRoleAt` fanout

Before adding a new proposition type, audit the complete fanout of `InformationProposition`.

Map at least:

- proposition declaration;
- every exhaustive `when` over propositions;
- `TroubleBrewingWorldObservationEvaluator`;
- exact/enumerated filtering paths;
- canonical/stable-ID or serialization/codec paths if any;
- ASP/Clingo/symbolic/ZDD representations if they consume proposition syntax;
- tests/snapshots/fingerprints dependent on proposition variants.

Do not add `ShownRoleAt` until this map is understood.

Then introduce:

```text
InformationProposition.ShownRoleAt(seat, role)
```

Required exact semantics:

```text
world.shownRolesBySeat[seat] == role
```

Important boundaries:

- this is a claim about the role the player is shown/believes they are;
- it is not `RoleAt(seat, role)`;
- it does not use Spy/Recluse registration;
- it is not loosened merely because the player's ability is malfunctioning;
- it must not reveal actual hidden identity.

For an actual Drunk shown Chef:

```text
ShownRoleAt(seat, Chef)   // valid public role claim
RoleAt(seat, Drunk)       // must NOT be emitted by PUBLIC_GOOD_INFO
```

## 7. PUBLIC_GOOD_INFO projection change

After `ShownRoleAt` exists, `FirstNightPublicGoodInfoProjection` must project both:

```text
public shown-role claim
+
public clue observation
```

for each PUBLIC_GOOD_INFO sharing source.

Requirements:

- derive the claim from the player's shown/perceived ability represented by the observation, not hidden actual identity;
- create at most one identical shown-role claim per `(sourceSeat, shownRole)` within one projected bundle;
- deterministic ordering;
- deterministic stable IDs;
- no durable mutation;
- clue observations remain separate observations;
- latent Red Herring/demon-bluff entries remain non-shared in this profile.

If multiple first-night entries from the same sharing player occur later, deduplication must prevent repeated identical identity claims.

## 8. Required new fixtures

### Healthy confirmation-chain fixture

Add a fixture that proves role claims materially participate in the conjunction, not merely clue contents.

A representative pattern should include identity confirmation, for example:

```text
Washerwoman claims Washerwoman
Washerwoman supports Empath among two seats
Empath claims Empath
Empath shares 0
```

The fixture should prove the projection actually contains the expected shown-role claims plus clue observations and that exact AFTER is computed from the whole conjunction.

### Drunk shown-role fixture

Use an actual Drunk shown as a Townsfolk role such as Chef.

Prove:

- PUBLIC_GOOD_INFO emits `ShownRoleAt(drunkSeat, Chef)`;
- that claim matches a world whose `shownRolesBySeat[drunkSeat] == Chef`;
- no public identity proposition exposes actual `Drunk`;
- the false Chef clue can still be handled under the existing malfunction hypothesis rules independently of the shown-role claim.

This is distinct from the existing Drunk false-information fixture.

## 9. CI state

Important: after #140 was rebuilt/retargeted onto merged main, its current head had **no workflow run yet**.

Do not report #140 as CI-green based on old stacked-branch runs or #139 CI.

After the `ShownRoleAt` work is complete:

1. run/observe the normal PR CI on the actual current head;
2. verify Android FAST/full tasks required by the workflow;
3. because proposition semantics change, follow `docs/TESTING_STRATEGY.md` escalation for exact/symbolic/ASP/Clingo validation where applicable;
4. inspect failures rather than weakening tests/contracts;
5. only call FN-BUNDLE-1 complete when current-head required checks are green.

Do not merge #140 without explicit user authorization.

## 10. FN-BUNDLE-1 completion condition

FN-BUNDLE-1 is complete only when all are true:

```text
[ ] InformationProposition fanout audited
[ ] ShownRoleAt implemented at the correct semantic owner
[ ] exact evaluator matches shownRolesBySeat
[ ] no registration/malfunction leakage into shown-role identity semantics
[ ] PUBLIC_GOOD_INFO adds deterministic deduplicated shown-role claims
[ ] healthy confirmation-chain fixture passes
[ ] Drunk shown-role fixture passes
[ ] existing materializer/registration/numeric fixtures still pass
[ ] current #140 head has required CI green
```

Until then, do not begin FN-BUNDLE-2 whole-7-player harness as the main implementation task.

## 11. Next stage after #140

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

## 12. Product/architecture rules that remain stable

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

## 13. First concrete action in the new conversation

After reading the required documents and querying live source:

1. inspect live `main` and PR #140;
2. sync #140 with documentation-only main movement without reintroducing FN-BUNDLE-0 diff;
3. perform full `InformationProposition` fanout audit;
4. implement `ShownRoleAt` at the common epistemic owner;
5. extend PUBLIC_GOOD_INFO with deterministic deduplicated shown-role claims;
6. add healthy confirmation-chain and Drunk shown-role fixtures;
7. run required current-head CI;
8. stop for review/merge authorization before merging #140.

## 14. Stable rule

> **FN-BUNDLE-0 is finished and merged. The next conversation should not repeat candidate-space or setup-pair audits. Resume directly at FN-BUNDLE-1: make PUBLIC_GOOD_INFO include public shown-role identity claims as well as clue contents, prove healthy confirmation chains and Drunk shown-role semantics, then obtain current-head CI before considering FN-BUNDLE-1 complete.**
