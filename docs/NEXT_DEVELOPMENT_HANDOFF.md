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

The current external catalog contains **five tracked GOLD candidates but no admitted verified GOLD case yet**. Three detailed Ben Burns games still require primary-video Night-1 verification; `A Fond Farewell` now has primary-verified material state but is blocked on production Traveller legality; the Evin recording still requires state extraction. Multiple games from Ben share one Storyteller independence key and do not count as independent experts.

`A Fond Farewell` has now moved beyond metadata-only discovery. The official primary video was manually checked for material Night-1 state on 2026-09-22, including full radial player↔role mapping, Traveller alignments, Drunk shown Chef, Red Herring, Demon bluffs, Poisoner target, Washerwoman clue, Fortune Teller targets, and two explicit Ben rationales. The canonical record is `docs/SDE_2D5F_A_FOND_FAREWELL_PRIMARY_RECONSTRUCTION_2026-09-22.md`. The case is still not admitted GOLD because its five Travellers are not represented by the current production rules/domain model, so production-owned legal counterfactual recovery is unavailable. The official 2019 Evin recording remains the next independent-Storyteller primary extraction target.

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
- Ben Burns — official `Trouble Brewing - A Fond Farewell` — `PRIMARY_STATE_VERIFIED_TRAVELLER_EXECUTION_BLOCKED`; primary Night-1 state and two choice-specific rationales recovered;
- Evin — official 2019 TPI **8-player** Trouble Brewing playthrough — `PARTIALLY_RECONSTRUCTABLE`, valuable independent-Storyteller target.

TPI independently verifies Ben's long-running involvement and trusted/expert status. Evin is also independently qualified as experienced/trusted through his TPI co-founder role, integral early involvement, founding of the Newcastle Clocktower group, and the official TPI-hosted early Trouble Brewing game. His remaining GOLD blocker is state extraction, not expertise.

Do not manufacture GOLD by treating the detailed secondary episode index as final verification.

The source catalog now stores `storyteller_independence_key` explicitly. Ben's four candidates all use `st-ben-burns`; Evin uses `st-evin`. Blank means independence has not been established and must not be counted as a distinct expert.

### Step 3 — primary verification + executable case reconstruction — IN PROGRESS

Executable canonical reconstructions now exist for the first three Ben candidates. All three detailed Ben candidates now have executable committed-prefix consequence reports through the dedicated `:app:sde2D5FExpertObservedCalibration` T3 task/workflow. The workflow publishes A Stud, Live and Imp-Person, and Human Remains reports together as one artifact.

The latest accepted B4 executable checkpoint, `26b9afe72e72705f53dc225a79fd5be6e3b6195c`, passed dedicated expert-observed calibration **run #31**. The evidence harness now routes impaired numeric decisions through the shared numeric projector and summarizes complete pair legal domains through a shared typed prevalence projection. This adds no score, ranking, BAD/ACCEPTABLE labeling, or GOLD promotion.

All three executable secondary-reconstructed Ben cases remain `PRIMARY_VERIFICATION_PENDING`. Continue verifying their material setup commitments and Night-1 decisions against the primary recordings; do not promote them to GOLD from the secondary reconstruction alone.

`A Fond Farewell` is now the inverse state: primary verification is complete, but executable projection is deliberately blocked by missing Traveller semantics. The detailed production-boundary audit is `docs/SDE_2D5F_TRAVELLER_MODEL_BOUNDARY_AUDIT_2026-09-22.md`. It confirms that this is not a one-file enum gap: base setup quotas, player-count knowledge, strategic topology and setup witness feasibility all assume every formal seat is a base Townsfolk/Outsider/Minion/Demon participant. Traveller implementation is therefore **deferred from B4**. Do not fake execution by dropping Travellers, compressing the circle, or implementing evidence-local Chef arithmetic.

High-value cases:

- `A Stud In Scarlet`: Drunk shown Empath, Drunk Empath 0, FT YES via Recluse-as-Demon;
- `Human Remains Of The Day`: known Poisoner target plus poisoned Washerwoman misinformation;
- `Live and Imp-Person`: Librarian/Chef/FT bundle with interaction-scoped Recluse registration.

A smaller verified corpus is better than a larger ambiguous one.

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
topology-first real-case consequence
    ↓
whole-bundle diagnostics

Exact registration/mechanical semantics remain authoritative for legality and witness recovery. Exhaustive possible-world consequence is an optional deep audit only; bounded D4 differential tests own broad topology/exhaustive parity.
~~~

Keep earlier committed choices and already-made player-controlled choices fixed. Do not freeze later uncommitted Storyteller choices to their eventual observed values; that would leak hindsight into the counterfactual.

Do not encode `unchosen = bad`.

Numeric witness projection is now **implemented** in the B4 evidence bridge. `FirstNightNumericLegalDomain` remains the player-visible value owner, while `Sde2D5FExpertObservedNumericEvidenceProjector` reuses existing exact observation/registration semantics to recover witness alternatives. The same projector now covers reliable and impaired numeric evidence; do not reintroduce a Drunk-specific numeric evidence path or push evidence-only witness identity into the production legal candidate.

### Step 5 — extract repeated policy constraints

Candidate dimensions include thematic registration prior, role-function exposure avoidance, confirmation-chain avoidance, healthy-information floor, impaired-information narrative consistency, bluff narrative support, Red Herring placement, and player-choice likelihood / target ecology. `A Fond Farewell` supplies choice-specific Ben rationale for both believable impaired information and Red Herring placement around likely Fortune Teller target behavior; keep these as dimensions, not weights.

Use the current evidence-maturity ledger rather than inventing weights:

- rules-confirmed foundations: interaction-scoped registration; impaired information may legally be true or false;
- repeated qualitative candidates: narrative anchoring, truth danger, temporal consistency / impairment detectability;
- expert-candidate-only or under-evidenced: role-function exposure severity, Red Herring ordering, healthy-information floor.

Catalog tag counts are curation signals, not independent statistical observations. Only promote a policy rule when evidence is repeated or explicitly reasoned, and never from candidate rarity alone.

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
4. generic pair/numeric/boolean evidence projectors reuse production legality and witness owners; reliable and impaired numeric evidence now share the same numeric projector;
5. shared canonical-world / claim / consequence projection avoids fixture-specific rules;
6. `A Stud In Scarlet` committed-prefix topology report is green and artifacted;
7. `Live and Imp-Person` committed-prefix consequence report is green and artifacted;
8. `Human Remains Of The Day` committed-prefix consequence report is green and artifacted, preserving all 273 legal poisoned-Washerwoman candidates while summarizing them by strategic signature;
9. dedicated `:app:sde2D5FExpertObservedCalibration` separates current B4 feedback from the historical multi-hour D5 calibration workload;
10. `A Fond Farewell` material Night-1 state is primary-verified and preserved in a dedicated reconstruction document; its five-Traveller dependency is explicitly blocked from fixture-local execution.

Measured boundary: a 9-player exhaustive real-case possible-world sample remained multi-minute even when reduced to one stage. Real expert-case consequence therefore stays topology-first. D4 bounded differential tests own broad topology/exhaustive correctness; exact real-case evaluation remains an exceptional deep-audit hook.

Substantive result so far:

- A Stud's currently reconstructed Chef, Drunk-Empath, and Fortune Teller alternatives all leave the evil-seat strategic quotient at 56 -> 56. This is a valid neutral result, not a failed harness; the case's current value lies primarily in registration and impaired-information evidence.
  The descriptive feature report now additionally records Drunk-Empath `0=FALSE` (observed), `1=TRUE`, `2=FALSE`, with no special registration required; semantic truth and registration are separate axes.
- Live is the stronger interaction-scoped registration case: the same Recluse is naturally unregistered for observed Chef=1 while the observed Fortune Teller YES uses a Recluse-as-Demon witness.
- Human Remains exposes the limit of topology-only policy evidence: all 273 legal poisoned-Washerwoman candidates collapse to one strategic-after signature at 42 -> 42. The later fixed-target Fortune Teller result is forced and also remains 42 -> 42. This case therefore needs non-topology dimensions such as narrative anchoring, truth danger, role-function exposure and confirmation structure.
  The new descriptive prevalence view shows 24/273 truthful versus 249/273 false candidates, 42 Demon-bluff-role candidates, 143 touching actual Evil, 78 touching the Demon, 78 touching the Minion, and only 13 all-Evil pairs. The observed false Empath clue to the actual Minion+Demon shares its complete descriptive signature with 1 other candidate. Do not turn this narrowness into a preference score.

Next success condition:

1. continue primary-video verification for the three detailed secondary-reconstructed Ben candidates; prioritize material Night-1 timestamps/source confirmation rather than adding more Ben volume;
2. make the independent Evin 8-player playthrough the next fresh primary extraction target;
3. keep `A Fond Farewell` as primary-verified but execution-blocked; Traveller support has been audited and explicitly deferred from B4, and any later implementation must be a separate canonical-domain capability;
4. extract repeated policy dimensions only where source rationale and legal counterfactuals support them; do not derive D5F-C numeric gates yet;
5. keep 0 admitted GOLD until a case satisfies both primary-state verification and production-owned complete legal-counterfactual recovery;
6. do not promote a policy rule merely because an alternative was unchosen.

Code-smell checkpoint: named role adapters are acceptable only at the legality/proposition semantic boundary. Cross-case evidence projection and later persistent impaired narrative policy must remain shared and information-shape/history based.

Do not manufacture GOLD evidence if public material is insufficient; report the gap and use the strongest available evidence tier.
