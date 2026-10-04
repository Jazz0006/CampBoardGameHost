# LRE-P INV1-A Investigator Policy Cutover — 2026-10-04

> Status: **COMPLETE / ACCEPTED**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> PR: **#222 — LRE-P: add INV1-A Investigator policy**
>
> Executable head: `89fb4a518260fb838f69a6d6b389869ab5f865f6`
>
> Validation: **CI #3759 GREEN / R2 #3453 GREEN**
>
> Squash merge: `420eef4e21769e3adf8928f187009f2afc1e5fa2`

## 1. Evidence authority

Source semantics come from ClocktowerEvidenceLab:

`docs/EL_LRE_INV1_INVESTIGATOR_PAIR_CONSTRUCTION_VERIFIED_HANDOFF_2026-10-04.md`

The implemented slice uses only **INV1-A**, whose VERIFIED relation is an explicit bounded preference:

```text
when Empath topology already places unusually strong pressure on the Demon,
prefer an Investigator pair construction that preserves a plausible alternative world
while still including the real Minion
```

INV1-B and INV1-C are not folded into this policy.

## 2. Production policy identity

`FUNCTIONING_INVESTIGATOR_INV1_V1`

Owner:

`FunctioningInvestigatorInv1ProductionSelector`

## 3. Exact admitted scope

Automatic authority exists only when all of the following hold:

- Trouble Brewing;
- first night, round 1;
- Investigator is functioning / reliable;
- exactly one actual Minion is in play;
- exactly one actual Empath is in play;
- the canonical snapshot confirms that Empath is not poisoned;
- the Empath is seated between the Demon and an actual Townsfolk;
- a truthful legal Investigator candidate exists showing the sole real Minion role between:
  - the Empath's Townsfolk neighbour; and
  - the sole real Minion.

The selected pair contains no special-registration witness.

## 4. Fail-closed boundary

Outside the exact predicate above, the selector returns unavailable.

Therefore:

```text
policy predicate matches
-> versioned INV1-A recommendation may auto-select

policy predicate does not match
-> no legacy heuristic fallback
-> LRE-1 authority seam leaves multi-choice decision Manual
```

This includes:

- poisoned Empath;
- poisoned / Drunk Investigator;
- Empath not adjacent to the Demon;
- more than one actual Minion;
- missing or ambiguous required context;
- any legal Investigator construction that depends on broader INV1-B / INV1-C semantics.

## 5. Tests-first acceptance

The first pushed checkpoint intentionally contained only the contract test and failed CI because the selector/policy identity did not yet exist. That established the expected RED state.

The implementation then added:

- the versioned policy identity;
- the narrow production selector;
- UI production wiring through `automaticPolicyRecommendation`;
- deterministic selection inside the evidence-backed candidate subset;
- fail-closed tests for out-of-scope topology, unreliable Investigator, multiple Minions, and poisoned Empath.

The final executable head passed the full Android test gate and R2 boundary gate.

## 6. What this does not mean

This cutover does **not** establish:

- a total Investigator pair ranking;
- a general preference for the Empath's neighbour;
- automatic use of Recluse registration;
- ordering between INV1-B and INV1-C;
- a numeric setup-strength threshold;
- any legacy heuristic fallback when INV1-A is unavailable.

## 7. Next LRE-P choice

EvidenceLab Priority-1 bounded verification is complete for:

- Washerwoman;
- Investigator;
- Demon bluffs;
- Red Herring.

The next Host family should be chosen by implementation readiness and existing canonical state / legal-domain support, not by continuing broad evidence collection first.

The strongest immediate candidates are Demon bluffs and Red Herring because both already have verified evidence and existing Host setup/commit infrastructure. Washerwoman is also evidence-ready but requires its own exact Host predicate before any additional automatic cutover.
