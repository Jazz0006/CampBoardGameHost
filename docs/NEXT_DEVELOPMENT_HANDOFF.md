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
- impaired information should preserve a believable cross-interaction perceived world through a persistent role-agnostic narrative state; it must not degrade into independent nightly lies;
- whole-bundle interaction, role-function exposure, bluff usability and confirmation chains matter in addition to strategic topology;
- no opaque global score;
- do not implement policy as named-role, exact-seat, or known-fixture special cases unless a real rules distinction requires it.

## 5. Shared impaired-narrative invariant

Treat examples involving a Drunk or poisoned information role as examples of one generic requirement, not separate implementation tasks.

Future production design must provide a shared persistent narrative state over:

~~~text
perceived role
+ committed observation history
+ current visible history
+ believable counterworld intent
+ current legal outcome domain
~~~

The shared owner chooses a coherent legal continuation. Named role modules only supply role-specific legality/semantics.

Before accepting an implementation, audit for suspicious branches keyed directly to a named role, a known fixture, exact seats, or one previously discussed scenario. Such branches are acceptable only when they encode a genuine rules distinction, not policy convenience.

Tests must prove the generic abstraction and fanout across multiple information shapes. Passing one named-role example is not evidence that the general contract is implemented.

## 6. Evidence hierarchy

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

The current external catalog contains **five tracked GOLD candidates but no admitted verified GOLD case yet**. Three detailed Ben Burns games still require primary-video Night-1 verification; the official Ben/Evin recordings require fuller state extraction. Multiple games from Ben share one Storyteller independence key and do not count as independent experts.

## 7. Interpretation rules

- reconstruct the legal alternatives that existed at the exact lifecycle stage;
- chosen A does not imply all unchosen alternatives are bad;
- explicit rationale/rejection is strong evidence;
- repeated comparable choices strengthen a preference;
- cross-source consistency strengthens a preference;
- one silent observed choice is weak evidence;
- final winner is not a quality label.

## 8. NEXT — execute in this order

### Step 1 — audit/delete obsolete clean calibration code — COMPLETE

Deleted the two representative-clean-corpus Kotlin files. They had no external task/report/resource wiring and protected no unique durable contract. Pair legality, numeric semantics, registration witnesses, and setup/deal behavior remain covered at their owning typed tests.

Do not restore the seven clean scenarios for calibration.

### Step 2 — GOLD source discovery — SEEDED / CONTINUE SELECTIVELY

The catalog now tracks:

- Ben Burns — `A Stud In Scarlet` — `FULLY_RECONSTRUCTABLE` from the current detailed index, pending primary-video verification;
- Ben Burns — `Human Remains Of The Day` — `FULLY_RECONSTRUCTABLE`, pending primary verification;
- Ben Burns — `Live and Imp-Person` — `FULLY_RECONSTRUCTABLE`, pending primary verification;
- Ben Burns — official `Trouble Brewing - A Fond Farewell` — `PARTIALLY_RECONSTRUCTABLE`, primary Night-1 chapter available;
- Evin — early TPI Trouble Brewing playthrough — `PARTIALLY_RECONSTRUCTABLE`, valuable independent-Storyteller target.

TPI independently verifies Ben's long-running involvement and trusted/expert status. Evin is also independently qualified as experienced/trusted through his TPI co-founder role, integral early involvement, founding of the Newcastle Clocktower group, and the official TPI-hosted early Trouble Brewing game. His remaining GOLD blocker is state extraction, not expertise.

Do not manufacture GOLD by treating the detailed secondary episode index as final verification.

### Step 3 — primary verification + exact case reconstruction — NEXT

Start with the first three Ben cases. Verify the material setup commitments and Night-1 decisions against the primary recordings, then materialize the cases into canonical project state.

High-value first cases:

- `A Stud In Scarlet`: Drunk shown Empath, Drunk Empath 0, FT YES via Recluse-as-Demon;
- `Human Remains Of The Day`: known Poisoner target plus poisoned Washerwoman misinformation;
- `Live and Imp-Person`: Librarian/Chef/FT bundle with interaction-scoped Recluse registration.

A smaller exact corpus is better than a larger ambiguous one.

### Step 4 — model observed choice + legal counterfactuals

Implement an **evidence reconstruction/projection harness**, not a second rules engine:

~~~text
committed prefix at decision time
    ↓
production legality owner
    ↓
complete legal alternative set
    ↓
observed expert choice matched to one legal candidate/witness
    ↓
existing proposition materialization
    ↓
exact evaluator + topology evaluator
    ↓
whole-bundle diagnostics
~~~

Keep earlier committed choices and already-made player-controlled choices fixed. Do not freeze later uncommitted Storyteller choices to their eventual observed values; that would leak hindsight into the counterfactual.

Do not encode `unchosen = bad`.

Bounded implementation note: `FirstNightNumericLegalDomain` exposes player-visible Chef/Empath values but not the exact registration witness. Do not solve this by duplicating numeric rules. Reuse `TroubleBrewingTopologyObservationWitnessEvaluator` / registration-witness semantics to project each legal numeric value to its complete witness alternatives, and preserve both a value-level grouped view and a value+witness evidence candidate view.

### Step 5 — extract repeated policy constraints

Candidate dimensions include thematic registration prior, role-function exposure avoidance, confirmation-chain avoidance, healthy-information floor, impaired-information narrative consistency, bluff narrative support, and Red Herring placement.

Only promote a rule when evidence is repeated or explicitly reasoned.

### Step 6 — SILVER generalization

After GOLD patterns exist, test them against structured ClockTracker cases.

### Step 7 — bounded human adjudication

Ask the project owner only about ambiguous reconstruction, conflicting expert evidence, or BEGINNER-specific adaptation.

Do not return to large synthetic labeling sessions.

## 9. Explicit non-goals

Do not derive D5F-C thresholds, open sealed holdout, cut production selection over, rewrite core legality/topology architecture, remove compatibility policy still used in production, begin SDE-3, or merge PR #150.

## 10. Current checkpoint and next success condition

Completed in this checkpoint:

1. obsolete clean-corpus code/tests deleted after confirming no unique durable coverage;
2. GOLD qualification rubric added to the synthesis/catalog;
3. five candidate expert sources recorded;
4. reconstruction feasibility assessed;
5. observed-choice + legal-counterfactual integration contract defined.

Next success condition:

1. primary-video verification completed for the three detailed Ben candidates;
2. at least the first verified case materialized into canonical state without duplicating legality;
3. its observed decision matched to the complete production legal candidate/witness set;
4. exact + strategic whole-bundle diagnostics generated for observed choice and legal counterfactuals;
5. no policy conclusion promoted merely because an alternative was unchosen.

Code-smell checkpoint: current pair/numeric/Fortune-Teller Drunk adapters converge on a shared exact consequence evaluator and are acceptable semantic adapters. Do not let the role-named Fortune Teller adapter become a role-specific cross-night policy; persistent impaired narrative selection must remain shared and semantic-shape based.

Do not manufacture GOLD evidence if public material is insufficient; report the gap and use the strongest available evidence tier.
