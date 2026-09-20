# NEXT DEVELOPMENT HANDOFF — SDE-2D5F-B4 expert-observed first-night policy calibration

> Updated: 2026-09-21 Australia/Sydney  
> This is the **only active handoff**.

## 1. Read first

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`
5. `docs/SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`
6. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`
7. this handoff

Do not reload deleted 2026-09-20 D5 correction/design files from Git history unless a concrete historical question requires them.

## 2. Live branch

Branch: `sde-2d5-calibration-policy-evidence`

PR #150 remains **draft**. Do not merge unless the user explicitly says **“授权合并”**.

Always query live branch/PR/checks before editing.

## 3. Why the route changed

Two calibration approaches were rejected as policy authorities:

1. the earlier extreme 7-player fixture was already nearly solved by fixed information;
2. the replacement seven clean scenarios deliberately excluded Drunk and froze registration-dependent context.

A second correction is methodological: one-person labels are not strong enough to define expert Storyteller policy.

The active route is **expert-observation-first**.

## 4. Current policy summary

- legal candidate enumeration remains complete and rules-owned;
- Spy/Recluse registration remains interaction-scoped;
- BEGINNER thematic prior: Spy normally hides as Good; Recluse normally registers as Evil;
- whole-bundle health may materially override that prior;
- avoid Librarian exposing Recluse and strongly avoid Investigator exposing Spy when healthy alternatives exist;
- Chef/Empath are fixed only when registration cannot change their legal healthy value;
- Drunk information should form a believable multi-night narrative, not merely be false;
- whole-bundle interaction, role-function exposure, bluff usability and confirmation chains matter in addition to strategic topology;
- no opaque global score.

## 5. Evidence hierarchy

~~~text
GOLD
    verified experienced/trusted Storyteller real games
    reconstructable setup + Night 1 decision state
    explicit rationale preferred

SILVER
    high-fidelity structured real-game logs
    expertise not independently established

QUALITATIVE
    tutorials / postmortems / repeated experienced-community discussion

DIAGNOSTIC_ONLY
    synthetic / extreme / counterfactual fixtures
~~~

The current external catalog contains **no verified GOLD case**.

## 6. Interpretation rules

- reconstruct the legal alternatives that existed at the exact lifecycle stage;
- chosen A does not imply all unchosen alternatives are bad;
- explicit rationale/rejection is strong evidence;
- repeated comparable choices strengthen a preference;
- cross-source consistency strengthens a preference;
- one silent observed choice is weak evidence;
- final winner is not a quality label.

## 7. NEXT — execute in this order

### Step 1 — audit/delete obsolete clean calibration code

Inspect:

- `app/src/test/java/com/codex/campboardgamehost/clocktower/review/Sde2D5FRepresentativeHealthyInformationCorpus.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/review/Sde2D5FRepresentativeHealthyInformationCorpusTest.kt`
- any task/report wiring that exists only for that corpus.

Delete them if they protect no unique durable legality/generator contract.

Do not delete canonical `NaturalPairInformationCandidateGenerator`, `FirstNightNumericInformationSemantics`, `TroubleBrewingRegistrationDomain`, or topology/exact evaluators.

### Step 2 — GOLD source discovery

Use current web research to find expert/trusted Trouble Brewing Storyteller games. Prioritize official/TPI-affiliated material and clearly experienced Storytellers. Record evidence for expertise; do not infer it from production quality alone.

### Step 3 — choose reconstructable GOLD cases

Prefer cases where Night 1 can recover roles/seats, Drunk shown role, Demon bluffs, Red Herring, Poisoner target, Spy/Recluse registrations, first-night information, and Storyteller rationale.

A smaller exact corpus is better than a larger ambiguous one.

### Step 4 — model observed choice + legal counterfactuals

~~~text
committed state at decision time
    ↓
production legality owner
    ↓
complete legal alternative set
    ↓
observed expert choice marked separately
    ↓
whole-bundle / topology diagnostics
~~~

Do not encode `unchosen = bad`.

### Step 5 — extract repeated policy constraints

Candidate dimensions include thematic registration prior, role-function exposure avoidance, confirmation-chain avoidance, healthy-information floor, impaired-information narrative consistency, bluff narrative support, and Red Herring placement.

Only promote a rule when evidence is repeated or explicitly reasoned.

### Step 6 — SILVER generalization

After GOLD patterns exist, test them against structured ClockTracker cases.

### Step 7 — bounded human adjudication

Ask the project owner only about ambiguous reconstruction, conflicting expert evidence, or BEGINNER-specific adaptation.

Do not return to large synthetic labeling sessions.

## 8. Explicit non-goals

Do not derive D5F-C thresholds, open sealed holdout, cut production selection over, rewrite core legality/topology architecture, remove compatibility policy still used in production, begin SDE-3, or merge PR #150.

## 9. Success condition for the next conversation

A successful next conversation should finish with:

1. obsolete clean-corpus code/tests either deleted or explicitly justified by unique durable coverage;
2. a verified source-quality rubric applied consistently;
3. at least a small set of candidate GOLD Trouble Brewing games identified;
4. exact reconstruction feasibility assessed for those cases;
5. the next implementation slice defined around observed expert choice + legal counterfactuals.

Do not manufacture GOLD evidence if public material is insufficient; report the gap and use the strongest available evidence tier.
