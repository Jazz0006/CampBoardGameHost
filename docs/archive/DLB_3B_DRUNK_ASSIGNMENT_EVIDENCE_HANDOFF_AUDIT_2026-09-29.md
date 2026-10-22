# DLB-3B Drunk Assignment Evidence Handoff Audit — 2026-09-29

> Status: **C1D ACCEPTED / DLB-3B CONTRACT DESIGN READY**
>
> Host baseline: `main@e3c051a7ff7ea6538d9e7135315dc074c60324f5`
>
> Evidence source: `Jazz0006/ClocktowerEvidenceLab` C1C/C1D

## 1. Why this audit exists

The DLB-3A readiness audit correctly rejected repurposing fixed-Drunk misinformation evaluators as a Drunk-seat selector.

One factual premise in that audit is now stale: the targeted EvidenceLab package is no longer short of its intended replay quota.

EvidenceLab has completed C1C with:

- 3 / 3 `PREFIX_RECONSTRUCTABLE` Drunk-assignment cases;
- 2 explicit historical assignment-rationale cases;
- targeted acquisition stopped;
- C1D cross-project replay handoff active.

Therefore DLB-3B is no longer blocked on obtaining a third replay case.

That does **not** authorize a production preference or a V1 policy change.

## 2. Evidence package now available

### Replay case 1 — A Stud In Scarlet

- complete shown-role layout before Drunk assignment;
- shown Empath selected as Drunk;
- assignment rationale UNKNOWN.

### Replay case 2 — A Fond Farewell

- complete role layout before assignment;
- shown Chef selected as Drunk;
- explicit rationale: support a deliberately extreme Chef misinformation narrative;
- Red Herring and Demon bluffs occur later and are excluded from the prefix.

### Replay case 3 — G10 Game 2 / The Megavoid

Historical shown-role layout, clockwise from the selected Empath:

1. Empath;
2. Imp;
3. Undertaker;
4. Librarian;
5. Spy;
6. Monk;
7. Mayor;
8. Virgin;
9. Butler.

The full layout is evidenced as fixed before assignment.

Observed choice:

- seat 1 / shown Empath -> actual Drunk.

Explicit assignment rationale:

- the Empath is adjacent to the Demon.

Later Librarian information, Demon bluffs and night information are excluded from the assignment prefix.

## 3. C1D downstream replay contract

EvidenceLab exports:

- historical pre-assignment shown-role layout;
- observed Storyteller choice;
- source-backed rationale where available;
- provenance;
- explicit alternatives only when source-observed.

EvidenceLab does **not** export legal Drunk alternatives.

Host must independently derive legality through:

`TroubleBrewingDrunkCandidateDomain`.

Host PR #173 adds an evidence-derived regression using G10 Game 2.

Expected Host-derived legal domain:

- seat 1 Empath;
- seat 3 Undertaker;
- seat 4 Librarian;
- seat 6 Monk;
- seat 7 Mayor;
- seat 8 Virgin.

The observed historical seat 1 Empath must map into that domain.

The replay must then continue through the existing DLB-3A shadow surface with:

- complete rules-derived legal candidate IDs;
- candidate-specific DLB-2 hypothetical setup;
- ecology audit;
- normal SetupPrecommit SDE envelope;
- normal DecisionTrace / replay input;
- frozen `BEGINNER_CONSERVATIVE_V1` still Deferred;
- no policy selection;
- no canonical Drunk commit.

## 4. What the evidence now supports

The evidence is sufficient to define a **score-free setup-level consequence contract**.

It supports at least two distinct consequence families.

### Healthy-information suppression / topology consequence

G10 demonstrates a Storyteller selecting the shown Empath because its seat is adjacent to the Demon.

The production feature must be expressed generically from the hypothetical setup / role-function ecology.

Do not hard-code:

- Empath;
- Demon;
- seat 1;
- this exact nine-player layout.

### Impaired-information narrative opportunity

A Fond Farewell demonstrates a Storyteller selecting the shown Chef partly to create a useful extreme-number misinformation route.

The production contract must preserve room for future misinformation opportunity without choosing one arbitrary later output as if it were already committed.

Do not repurpose the existing fixed-Drunk whole-bundle output evaluators as the assignment projector.

## 5. What remains unsupported

The current evidence does **not** justify:

- numeric weights between consequence families;
- a scalar Drunk-seat score;
- universal preference for information roles;
- universal preference for Empath-next-to-Evil;
- a hard reject rule derived from either rationale case;
- automatic Beginner Drunk selection;
- modification of frozen `BEGINNER_CONSERVATIVE_V1`;
- seeded survivor selection across evidence-equivalent candidates;
- hindsight use of Red Herring, Poisoner target, Demon bluffs, later information, public claims or outcomes.

## 6. Revised DLB-3B gate

The former gate:

```text
blocked because targeted assignment replay package < 3 cases
```

is superseded.

The current gate is:

```text
3-case evidence package available
    ↓
C1D executable downstream replay
    ↓
define score-free candidate consequence envelope
    ↓
project only evidence-backed descriptive consequences
    ↓
shadow replay / compare against historical choices
    ↓
separate versioned policy-semantics decision later
```

The C1D regression is accepted. DLB-3B may now begin score-free consequence-contract design.

## 7. Recommended DLB-3B contract boundary

Keep DLB-3B candidate-aligned and non-scalar.

A candidate consequence envelope should remain able to represent independently:

- healthy-information function suppressed or altered by making this seat Drunk;
- setup/topology interactions relevant to that healthy function;
- later impaired-information opportunity that may exist for the shown role;
- whether a consequence is exact/projected, descriptive-only, or unavailable;
- provenance/diagnostic reason for unavailable dimensions.

The first implementation should not collapse these into a weighted score.

The existing DLB-3A ecology census remains diagnostic input. It may feed a dedicated evidence-backed projector, but must not be copied wholesale into frozen `DecisionFeatures` without an explicit contract.

## 8. Acceptance meaning

A successful C1D G10 regression proves:

- EvidenceLab historical prefix maps cleanly into Host's current intermediate setup;
- Host can independently derive legal candidates;
- the observed historical choice is representable as one Host candidate;
- DLB-3A can replay the domain without hindsight leakage;
- current policy still refuses to manufacture an unsupported selection.

It does **not** prove that the Host would or should choose Empath.

That distinction remains mandatory for DLB-3B.


## 9. Validation checkpoint

The first exact-head CI classification pass skipped Android tests because the change set was test/docs-only. That pass was correctly rejected as insufficient acceptance evidence.

Accepted executable checkpoint:

`87240bb1e3ba6cfe61461905741659d3ba5426ae`

Exact-head acceptance:

- CI #3532 / run `36525125415`: GREEN;
- Android full unit tests + debug APK assemble: GREEN;
- ASP contract tests: GREEN;
- Real Clingo cross-validation: GREEN;
- aggregate CI gate: GREEN;
- R2 #3273 / run `36525125412`: GREEN.

The evidence-derived G10 replay regression therefore executed successfully on the accepted Host tree.

C1D is ACCEPTED from the downstream Host side.

This acceptance proves cross-project replay compatibility only. It does not convert the observed Empath choice into a recommendation label or authorize a frozen-V1 policy delta.
