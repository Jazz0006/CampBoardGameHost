# SDE-2D5F First-Night Information Policy Synthesis — 2026-09-21

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 (**must remain draft**)  
> Status: **design authority for the current first-night BEGINNER policy synthesis; production cutover is not authorized**

## 1. Purpose

This document records the current best model for how the app should construct and present Trouble Brewing first-night information after roles, the Drunk's shown identity, and seating are committed.

The target is not to maximize uncertainty, maximize Evil survival, maximize misinformation, or minimize mechanical world reduction.

The target is a **healthy first-night information ecology for beginner/ordinary players**:

- Good receives enough reliable structure to reason and discuss;
- Evil retains enough plausible seating/topology and bluff routes to play;
- individually legal clues do not accidentally form an overwhelming confirmation chain;
- Spy, Recluse and Drunk are used in ways that express their intended role function;
- misinformation remains believable and, for multi-night impaired roles, temporally coherent;
- legality, lifecycle ownership and player-controlled decisions remain strict boundaries.

This policy is intentionally distinct from the legacy GENTLE/BALANCED/AGGRESSIVE recommendation profiles.

## 2. Core architecture

The first-night system should be modeled as:

~~~text
committed setup
    roles + seats + Drunk shown identity
        ↓
legal outcome domains
        ↓
precommitted setup/night structure
    Demon bluffs + Fortune Teller red herring
        ↓
whole-bundle candidate construction
    healthy pair clues
    Chef / Empath registration branches
    Drunk misinformation where applicable
    Spy / Recluse interaction-scoped registrations
        ↓
whole-table semantic + strategic evaluation
        ↓
select a healthy bundle
        ↓
Poisoner player choice
        ↓
delta re-evaluation of still-unshown / still-controllable information
        ↓
commit observations as the night proceeds
~~~

The exact implementation may stage the search for performance, but the semantic model is a **joint information bundle**, not independent per-role optimization.

## 3. Legality and policy must remain separate

Rules owners enumerate what is legal.

Policy owners decide which legal result is desirable for the current table.

Do not encode a preferred Storyteller result by deleting legal candidates from the rules domain.

For each information event define a:

`legalOutcomeDomain`

A clue is genuinely **forced** only when the legal domain has one player-visible outcome after all applicable registration choices are accounted for.

This replaces the loose assumption that Chef / Empath are always fixed numeric information. They are fixed only when Spy/Recluse registration does not create multiple legal values.

## 4. Spy / Recluse registration policy

### 4.1 Interaction-scoped legality remains authoritative

Spy/Recluse registration is per interaction. It must never mutate canonical identity and does not need to be consistent across unrelated information events.

Existing registration legality remains:

- Spy may register as Good and as a Townsfolk/Outsider;
- Recluse may register as Evil and as a Minion/Demon;
- poisoning disables their special registration ability;
- a selected registration witness belongs to one interaction.

### 4.2 BEGINNER thematic default

The default policy should follow role fantasy:

~~~text
Spy
    default -> use the special Good/Townsfolk/Outsider registration
    intent  -> hide the Spy and support believable Good worlds

Recluse
    default -> use the special Evil/Minion/Demon registration
    intent  -> create plausible suspicion / absorb Evil detection

actual registration
    -> fallback when the thematic special registration materially harms bundle health
~~~

This is a **strong prior, not a hard rule**.

Do not override the thematic default for a tiny metric improvement. Override only when the default causes a material gameplay problem, for example:

- moves the bundle from an acceptable middle band into excessive confirmation / Evil collapse;
- creates a strong cross-confirmation chain;
- makes an already-exposed Evil location substantially more certain;
- destroys multiple otherwise usable Evil narratives.

A useful policy statement is:

> **Registration follows role fantasy by default; whole-bundle health may override it only for a material gameplay benefit.**

## 5. Role-function exposure should normally be avoided

Two legal first-night outcomes are especially undesirable when alternatives exist:

### 5.1 Librarian -> Recluse

A Librarian clue that directly identifies the Recluse pair can explain away later registration anomalies too quickly and reduces the Recluse's intended ambiguity.

Policy:

`AVOID_IF_HEALTHY_ALTERNATIVE_EXISTS`

This must not become illegal.

Forced example:

~~~text
healthy Librarian
actual Outsiders = { Recluse }
no usable Spy registration alternative

=> Librarian must show Recluse in a legal pair
~~~

### 5.2 Investigator -> Spy

Directly pointing the Investigator at the actual Spy exposes the Minion whose role function is concealment and information-assisted bluffing.

Policy:

`STRONGLY_AVOID_IF_HEALTHY_ALTERNATIVE_EXISTS`

This also must not become illegal.

Forced example:

~~~text
healthy Investigator
actual Minions = { Spy }
no Recluse available as a legal Minion-registration anchor

=> Investigator must point to Spy in a legal pair
~~~

If a Recluse is present, using a legal Recluse-as-Minion registration is normally thematically preferable to exposing the real Spy, subject to whole-bundle health.

This motivates a general diagnostic:

`roleFunctionExposureCost`

rather than ad-hoc illegality.

## 6. Pair-information domains

### Washerwoman

Legal healthy anchors include:

- actual Townsfolk;
- Spy registering as a legal Townsfolk.

The Drunk is an Outsider and is not an actual Townsfolk anchor merely because the Drunk is shown a Townsfolk identity.

### Librarian

Legal healthy information includes:

- actual Outsiders;
- Spy registering as a legal Outsider;
- `0 Outsiders` when the actual Outsider count is zero and the selected registration path permits the natural result.

A zero-Outsider result is strategically strong because it constrains Baron / Outsider narratives and must be evaluated as part of the whole bundle.

### Investigator

Legal healthy anchors include:

- actual Minions, including an actual Spy;
- Recluse registering as a legal Minion.

Recluse may register as a Minion not actually in play when the rules allow that registration question.

## 7. Chef / Empath numeric information

Chef and Empath should not be classified globally as `RULE_DETERMINED` merely from actual identity.

For each event:

1. enumerate all healthy legal numeric values induced by actual identity plus legal Spy/Recluse registrations;
2. if one value remains, it is forced;
3. if several values remain, the registration branch is a Storyteller-controlled bundle variable;
4. choose the thematic registration by default;
5. allow a material whole-bundle-health override.

The registration decision remains interaction-scoped.

## 8. Whole-bundle evaluation

Do not choose Washerwoman, Librarian, Investigator, Chef, Empath, Drunk information, Demon bluffs and registration rulings as independent local maxima.

The important failure mode is:

~~~text
several individually reasonable clues
    -> cross-confirm one another
    -> collapse Demon / Minion topology
    -> Evil has little meaningful play
~~~

The evaluation surface should keep distinct, interpretable axes such as:

- Demon-cover retention;
- Evil strategic-topology retention;
- Evil-cover retention;
- forced-good fraction;
- cross-confirmation / confirmation-chain structure;
- role-information utility;
- Outsider / Baron certainty;
- Demon-bluff usability and resilience;
- narrative-route diversity;
- impaired-information believability;
- future correction / flexibility;
- role-function exposure;
- beginner complexity cost.

Do not collapse these into one opaque weighted score.

The strategic topology quotient remains more meaningful than raw exact-role-world cardinality. Mechanical-world count remains descriptive evidence, not the primary policy objective.

B4 expert-observed evidence now adds an important qualification: under `MECHANICALLY_CREDIBLE` uncertainty, Drunk/Poisoner explanations can preserve every evil-seat topology across many legal first-night outputs. A topology-neutral result therefore means only that the evil-seat quotient does not distinguish those candidates. It must not be interpreted as policy equivalence. Registration semantics, role-function exposure, confirmation chains, bluff interaction, semantic truth, narrative consistency and future flexibility remain separate first-class dimensions.

## 9. Demon bluffs

Demon bluffs are a joint output, not three independent role scores.

For BEGINNER play, the policy should prefer a usable mix of narrative routes rather than merely maximizing support count or selecting the three easiest static roles.

Useful dimensions include:

- each bluff has enough individual support to be playable;
- the triplet has more than one narrative route;
- the set is not excessively redundant;
- execution/claim burden is suitable for the target player level;
- the bluffs do not collide badly with likely Good information;
- the bluffs remain useful after the selected first-night information bundle.

The Drunk's shown identity should normally not also be used as a beginner Demon bluff because it creates an avoidable direct-claim collision.

Demon-bluff legality remains setup-owned; SDE policy should consume legal triplets rather than duplicate legality.

## 10. Fortune Teller red herring

The red herring is a setup/precommit choice, not a response to the Fortune Teller's selected pair.

It should be evaluated as part of the global first-night ecology.

Useful considerations include:

- expected survival / time to matter;
- suspicion potential;
- interaction with Demon-bluff narratives;
- whether the seat is easily hard-confirmed;
- whether it duplicates misinformation already supplied by Recluse;
- whether it creates excessive or insufficient pressure.

Do not waste an independent misinformation route without a table-level reason.

## 11. Persistent impaired narrative state

The Drunk's shown identity is committed at setup and is not reselected by the later SDE.

Do not model an impaired information role as a sequence of independent per-interaction false outputs.

The policy needs a **persistent impaired narrative state**: a role-agnostic representation of the world that the affected player is being led to believe, together with the already-committed observations that constrain future misinformation.

Conceptually:

~~~text
perceived role
+ committed information history
+ current public / player-visible history
+ selected believable counterworld assumptions
        ↓
persistent impaired narrative state
        ↓
current legal output domain
        ↓
choose the next output that best preserves
the same believable perceived world when feasible
~~~

For the Drunk, this persistent state is commonly a shadow world: a coherent alternative interpretation of seats, alignments, roles, registrations or other facts that explains the information already shown to that player.

The same abstraction must work across **information shapes**, not named roles:

- recurring numeric information;
- recurring boolean / target-check information;
- role-identification information triggered by later events;
- pair / categorical information;
- future scripts with other repeated or history-dependent information surfaces.

Named roles such as Empath, Fortune Teller, Undertaker or Ravenkeeper are examples only. They must not define separate policy algorithms.

The goal is not "always false." A truthful output is legal and may be the most believable continuation when a forced lie would contradict the established narrative, reveal impairment, or create an implausible discontinuity.

Temporal coherence is therefore a first-class policy concern:

- previously committed information constrains later choices;
- deaths, seat-neighbour changes, executions, claims, registrations and other newly visible facts may legitimately change the next result;
- absence of a meaningful world change should not cause arbitrary information oscillation;
- when several legal continuations exist, prefer one that preserves a simple coherent perceived world;
- if the previous narrative becomes impossible, transition deliberately to the least disruptive new explanation rather than randomizing independently.

This state is not permission to fabricate illegal outputs. Rules still own the current legal surface domain; the persistent narrative owner ranks only among legal Storyteller-controllable outcomes.

### 11.1 Generalization / anti-special-case invariant

Implementation must solve this at the shared semantic/history layer, not through scenario-specific or role-specific patches.

Forbidden target shapes include:

~~~text
if role == Empath and drunk ...
if role == FortuneTeller and previousAnswer == ...
if fixture == knownCalibrationCase ...
if exact seating pattern == ...
~~~

unless the condition expresses an actual rules distinction owned by that role.

A role module may define:

> What outputs are legal for this ability?

It must not separately own:

> How should misinformation remain coherent across time?

That second question belongs to the shared impaired-narrative policy/history owner.

Every implementation slice must fan out across the supported information-shape families and prove the generic contract with typed tests. A test for one named role is insufficient if the production abstraction claims to support a broader family.

## 12. Poisoner lifecycle

The Poisoner target belongs to the Evil player, not the SDE.

The first-night system may compute a baseline plan before the Poisoner acts, but after the target is selected it should re-evaluate only decisions that are:

- still unshown;
- still Storyteller/SDE-controlled;
- legally affected by the new poisoning state.

Do not retroactively change committed Demon bluffs, red herring, Drunk shown identity, or already-shown information.

## 13. Preferred correction order

When the default first-night bundle is too strong against Evil, prefer the least disruptive correction that preserves role meaning:

~~~text
ordinary legal anchor / decoy choice
    ↓
thematic Spy/Recluse registration
    ↓
material override of the default registration when necessary
    ↓
Drunk / poisoned misinformation where the role is already impaired
~~~

Do not make a healthy player impaired after setup simply because the healthy truth is inconvenient.

Likewise, when the bundle is too weak for Good, prefer clearer healthy information rather than maximizing misinformation.

## 14. Search model

A practical implementation does not need to brute-force every complete future game.

Recommended staged search:

1. commit setup identity/seating inputs;
2. enumerate legal first-night outcome domains;
3. identify truly forced observations;
4. enumerate / select legal Demon-bluff and red-herring precommit candidates;
5. enumerate whole-bundle combinations over still-controllable first-night outputs and registration branches;
6. project exact/topology diagnostics plus semantic table features;
7. reject clearly pathological bundles;
8. prefer thematic registration and low-complexity options inside the acceptable middle band;
9. use deterministic weighted/tie selection among similarly acceptable bundles;
10. after Poisoner choice, run a bounded delta search over still-controllable information;
11. persist Drunk narrative intent / relevant history for later-night coherence.

For performance, candidate pruning is allowed only after legality is preserved and the pruning reason is policy-visible.

## 15. Engineering invariants to preserve

The following existing architecture remains desirable:

- `TroubleBrewingRegistrationDomain` owns registration legality;
- registration witnesses remain interaction-scoped;
- exact/topology epistemic evaluators own consequence mechanics;
- `StrategicWorldKey(demonSeat, minionSeats)` remains the strategic quotient;
- `NormalizedStrategicDiagnosticsProjector` remains descriptive, not a policy scorer;
- persistent impaired narrative coherence belongs to a shared semantic/history policy owner, not named-role branches;
- Demon-bluff joint-output evaluation remains separate from legality;
- Drunk shown identity remains setup-persistent;
- Poisoner target remains Evil-player-controlled;
- no opaque global scalar;
- no second rules/world solver.

## 16. Calibration implications

Do **not** maintain an active calibration stratum whose defining property is that Spy, Recluse or Drunk have been deliberately excluded.

Such clean scenarios can prove narrow legality or projection contracts, but typed unit/integration tests already own those invariants. They are not representative enough to justify ongoing calibration maintenance or human-review effort.

The active calibration target is the real Storyteller problem:

- committed setup and seating;
- Spy / Recluse registration choices where legal;
- Drunk shown identity and misinformation where present;
- Chef / Empath registration-derived branches;
- Washerwoman / Librarian / Investigator choices;
- Demon bluffs;
- red herring;
- Poisoner-selected context when already known;
- cross-confirmation between all relevant channels.

### Evidence hierarchy

Policy calibration should be **expert-observation-first**, not single-reviewer-label-first.

Use the following hierarchy:

1. **GOLD — expert observed decisions**
   - real Trouble Brewing games run by demonstrably experienced / trusted Storytellers;
   - preferably official/TPI-affiliated productions, established expert channels, or Storytellers with substantial public history;
   - enough grimoire / Night 1 detail to reconstruct the committed setup and the actual Storyteller choices;
   - explicit Storyteller rationale is especially valuable.

2. **SILVER — high-fidelity real game records**
   - structured ClockTracker or equivalent game logs with seating, roles, Demon bluffs, red herring, poisoning and Night 1 information;
   - Storyteller expertise may be unknown;
   - use to test whether GOLD-derived patterns generalize, not to define them alone.

3. **QUALITATIVE — community postmortems / guidance**
   - real-game reports, experienced community discussion and Storyteller tutorials;
   - useful for discovering policy dimensions, failure modes and rationale;
   - do not convert directly into numeric gates without stronger evidence.

4. **DIAGNOSTIC_ONLY — synthetic fixtures**
   - extreme/counterfactual/test fixtures;
   - useful for mechanics, regression and metric sensitivity;
   - never the primary calibration truth.

### What an observed expert choice means

An expert choosing candidate A does **not** prove that every unchosen legal candidate was bad.

For each reconstructable game, record:

- the exact legal candidate set known at that lifecycle stage;
- the observed chosen output;
- whether explicit rationale exists;
- whether an alternative was explicitly considered/rejected;
- whole-bundle state before and after the choice;
- player-experience context when known.

Strong preference evidence comes from:

- explicit expert rationale;
- repeated choices across comparable games;
- observed rejection of an alternative;
- consistent cross-source patterns.

A single chosen action without rationale is weaker evidence.

Do not use the eventual winner as the label for whether the Storyteller choice was good.

### Human review role

Human review remains useful, but it is not the primary source of truth.

The project owner's review should mainly:

- verify that the reconstructed table meaning is correct;
- detect cases where the evidence extractor misunderstood the Storyteller's intent;
- adjudicate conflicting expert evidence;
- decide product-specific BEGINNER adaptations when expert practice targets a different audience.

Do not derive D5F-C gates solely from one person's intuitive labels.

## 17. Items not yet frozen

The following still require calibration rather than arbitrary constants:

- how large a whole-bundle improvement is required to override thematic Spy/Recluse registration;
- exact severity difference between `Librarian -> Recluse` and `Investigator -> Spy`;
- healthy middle-band gates for 5–6, 7–9, 10–12 and 13–15 players;
- exact beginner complexity penalties;
- bluff narrative-route preferences;
- red-herring preference ordering;
- how much future flexibility should influence night-one selection.

Do not freeze these from intuition alone.

## 18. Frozen summary

The current policy direction is:

> **Enumerate every legal first-night information outcome, preserve role/lifecycle ownership, prefer Spy-as-Good and Recluse-as-Evil registration by default, avoid directly exposing Recluse to the Librarian or Spy to the Investigator when healthy alternatives exist, and select information as a whole-table bundle whose goal is a playable middle band rather than a maximum/minimum metric. Impaired misinformation must preserve a believable cross-interaction perceived world through a shared role-agnostic narrative state rather than independent per-role lies.**


## 19. Active documentation authority

For current execution, read:

1. `CURRENT_DEVELOPMENT_ROADMAP.md`;
2. `NEXT_DEVELOPMENT_HANDOFF.md`;
3. this synthesis;
4. `SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`.

The 2026-09-20 D5 policy-correction, extreme-fixture-correction and representative-clean-corpus design documents were removed from active docs after their valid conclusions were folded into this synthesis. Git history is sufficient for historical traceability.

Do not revive those deleted routes as parallel calibration authorities.

## 20. D5F-B4 expert-source qualification and observed-choice integration

### 20.1 GOLD qualification rubric

A real game is admitted to the GOLD corpus only when all required gates below are satisfied:

- **expertise is independently verified**: the Storyteller has documented substantial Storyteller/TPI/design/community practice; source fame or production quality alone is insufficient;
- **the game is authentic**: a real Trouble Brewing game, not a synthetic teaching fixture or counterfactual;
- **the material committed state is reconstructable** at the exact decision point: seats/roles plus every setup/night commitment that can change the legal outcome or consequence of the decision;
- **the observed choice is recoverable** from the source;
- **the complete legal alternative set is recoverable from production legality owners** rather than hand-authored from the evidence record.

Record rationale separately:

~~~text
EXPLICIT_SPECIFIC
    exact decision rationale / explicit rejected alternative

EXPLICIT_GENERAL
    Storyteller states a general balancing principle relevant to the case

REPEATED_PATTERN
    same Storyteller makes comparable choices across multiple reconstructable games

OBSERVED_ONLY
    choice is visible but no rationale is recovered
~~~

Rationale increases evidence strength but is not required for GOLD if the other gates are satisfied.

Every case also carries an **independence key** based on Storyteller identity. Multiple games run by Ben Burns, for example, are repeated evidence from one expert, not multiple independent experts.

Do not promote a case using a detailed secondary reconstruction until the material Night-1 state has been checked against the primary recording. A case may therefore remain a **GOLD candidate** even when Storyteller expertise is already verified.

### 20.2 First candidate set

The first high-value candidates are:

1. **A Stud In Scarlet** — Ben Burns — fully reconstructable from the current episode index, pending primary Night-1 timestamp verification. High-value decisions include Drunk-Empath misinformation and Fortune Teller YES through Recluse-as-Demon registration.
2. **Human Remains Of The Day** — Ben Burns — fully reconstructable from the current episode index, pending primary verification. High-value decision: poisoned Washerwoman misinformation with known Poisoner target; general early-game balancing rationale is also recorded, but is not choice-specific.
3. **Live and Imp-Person** — Ben Burns — fully reconstructable from the current episode index, pending primary verification. High-value because Chef and Fortune Teller can use different interaction-scoped Recluse registration branches in the same Night 1.
4. **Trouble Brewing - A Fond Farewell** — Ben Burns on the official Blood on the Clocktower channel — primary video and explicit Night-1 chapter are available, but the complete decision state still needs extraction.
5. **early TPI Trouble Brewing playthrough (4sfa8_kNxsQ)** — Evin — official TPI-endorsed primary recording and an important independent-Storyteller target. Evin's expert/trusted status is independently supported by his TPI co-founder role, integral early involvement, and founding of the Newcastle Clocktower group; the remaining blocker is complete Night-1 state extraction before GOLD admission.

The first three cases are not three independent expert confirmations because they share the same principal Storyteller.

## 21. Observed expert choice + legal counterfactual contract

Do not build a second evidence-specific rules engine.

The intended evidence harness should reconstruct one real game into the same canonical state types used by production and then ask existing legality owners for the decision domain.

A candidate evidence model should be structurally similar to:

~~~text
ExpertObservedFirstNightCase
    case/source metadata
    storyteller independence key
    reconstructed setup + persistent commitments
    ordered observed decision records

ObservedExpertDecision
    lifecycle point / source revision
    decision semantics
    observed candidate identity
    selected registration witness when observable
    rationale grade/source
~~~

For each decision:

~~~text
committed prefix at decision time
    ↓
existing production legality owner
    ↓
complete legal candidate set
    ↓
match observed expert output to exactly one legal candidate/witness
    ↓
materialize every legal counterfactual through existing proposition adapters
    ↓
topology-first real-case consequence
    ↓
whole-bundle diagnostics

Exact registration/mechanical semantics remain authoritative for legality and witness recovery. Exhaustive possible-world consequence is not the normal real-case path: bounded D4 differential tests already own broad topology/exhaustive parity, while the evidence bridge retains an optional exact-oracle hook for exceptional deep audits.
~~~

Counterfactual reconstruction must not leak hindsight. Earlier committed choices are fixed. Player-controlled choices already made are fixed. Later uncommitted Storyteller decisions must remain variables rather than being silently frozen to what the expert eventually chose.

Use two diagnostic views when useful:

- **committed-prefix consequence**: immediate consequence of replacing only the current decision;
- **continuation-feasible whole-bundle consequence**: whether healthy later legal continuations remain after that replacement.

The observed expert candidate is a distinguished member of the legal set, **not a positive label**. Unchosen candidates receive diagnostics, not BAD labels.

### Existing owner reuse

The current code already has most of the required seams:

- pair legality: `NaturalPairInformationCandidateGenerator` / `PairInformationLegalDomain`;
- numeric legality: `FirstNightNumericInformationSemantics` / `FirstNightNumericLegalDomain`;
- Fortune Teller legality/semantics: `FortuneTellerInformationSemantics`;
- registration legality/witnesses: `TroubleBrewingRegistrationDomain` plus exact registration-witness binding;
- proposition materialization: `TroubleBrewingFirstNightInformationPropositionMaterializer`;
- exact consequence: `StorytellerDecisionEngine.evaluateExactConsequences` / `ExactHistoricalHypotheticalObservationBundleEvaluator`;
- strategic consequence: `TroubleBrewingTopologyHypotheticalBundleEvaluator`;
- committed/unshown lifecycle boundary: session first-night information lifecycle and revisions.

The next implementation slice should therefore be an **evidence reconstruction/projection harness**, not new production selection code.

### Current executable B4 consequence boundary

The first executable expert-observed consequence slice now confirms an important scale boundary.

A real 9-player case remained multi-minute when exhaustive possible-world consequence was enabled even for only one sampled decision stage. The dedicated topology-first T3 workflow now covers A Stud In Scarlet, Live and Imp-Person, and Human Remains Of The Day—including Human Remains' complete 273-candidate poisoned Washerwoman domain—in about 3.5 minutes end-to-end including build/setup/report publication.

Therefore:

- real expert cases use production legality owners and exact registration-witness semantics to recover the legal decision surface;
- committed-prefix consequence for routine B4 analysis is topology-first;
- exhaustive real-case possible-world scans are optional deep audits, not a per-case requirement;
- bounded D4 differential tests remain the owner of broad topology-versus-exhaustive evaluator correctness;
- do not reintroduce all-recipient/all-stage exact enumeration merely to obtain a mechanical-world count.

The first `A Stud In Scarlet` report is also intentionally neutral on the current strategic quotient: every reconstructed Chef, Drunk-Empath and fixed-target Fortune Teller alternative preserves 56 evil-seat strategic topologies for every evaluated good recipient. This does not invalidate the case or the evaluator. It demonstrates that registration choice, impaired-information narrative, role-function exposure and other policy dimensions can carry expert-choice information even when the strategic quotient is unchanged.

`Live and Imp-Person` provides a different kind of evidence. In the reconstructed Night 1, the observed Chef=1 uses the natural registration witness while the later observed Fortune Teller YES uses Recluse-as-Demon. The same Recluse is therefore not modeled by a persistent/global registration state; registration remains interaction-scoped. Its current Librarian, Chef and Fortune Teller counterfactuals are also topology-neutral at 252 -> 252 for every evaluated good recipient. That strengthens the interpretation that this case is about registration semantics, role-function exposure and confirmation structure rather than strategic-topology compression.


`Human Remains Of The Day` demonstrates a separate limitation of the strategic quotient. The poisoned Washerwoman has 273 production-legal pair-information candidates in the reconstructed Night 1, yet all 273 collapse to one strategic-after signature and preserve 42 evil-seat strategic topologies for every evaluated good recipient. The later Fortune Teller result is forced for the fixed targets/Red Herring and also remains 42 -> 42. This is not grounds to discard the case: it shows that impaired-information policy cannot be learned from topology compression alone. Narrative anchoring, accidental-truth danger, role-function exposure, confirmation chains, and persistent perceived-world coherence must remain explicit independent dimensions.

### Numeric registration-witness projection gap

One bounded integration gap is now explicit:

- `FirstNightNumericLegalDomain` correctly enumerates player-visible Chef/Empath values;
- `FirstNightNumericInformationSemantics` correctly accounts for Spy/Recluse registration when determining the healthy truth-value set;
- but the numeric legal-domain candidate currently carries the **value**, not the exact interaction-local registration witness that produced that value.

The witness information already exists downstream. `TroubleBrewingTopologyObservationWitnessEvaluator` enumerates complete numeric registration-witness branches and `RegistrationWitnessSemanticMatcher` can bind a selected witness. Tests already prove natural, Spy and Recluse numeric witness alternatives.

Therefore the evidence harness should add a thin **value-to-witness projection** over existing owners. It must not duplicate Chef/Empath arithmetic or registration legality. For evidence analysis, candidate identity may need to be:

~~~text
player-visible numeric value
+ interaction-local registration witness
~~~

while retaining a grouped view by player-visible value. This allows expert cases such as `A Stud In Scarlet` and `Live and Imp-Person` to distinguish different Recluse registration choices without changing canonical identity or inventing a second numeric rules engine.

## 22. D5F-B4A cleanup result

The obsolete seven-scenario clean calibration corpus protected no unique durable contract and has been retired:

- `Sde2D5FRepresentativeHealthyInformationCorpus.kt` — deleted;
- `Sde2D5FRepresentativeHealthyInformationCorpusTest.kt` — deleted;
- its Markdown report had no independent task/workflow/resource owner and required no separate deletion.

Durable behavior remains covered at the owning layers, including pair-information legality, numeric registration-aware truth domains, exact registration witnesses, and setup/deal semantics.

This cleanup is especially important because the retired corpus deliberately excluded Drunk and froze multi-valued Chef/Empath registration context to one branch; those assumptions are no longer valid calibration policy.

## 23. Anti-special-case code audit

No production fixture ID such as `TB2_7_...` is part of the active policy path.

Current first-night Drunk consequence adapters are split by **information shape**:

- pair information;
- numeric information;
- Fortune Teller target-check/boolean semantics.

They all converge on the shared `TroubleBrewingFirstNightDrunkWholeBundleExactEvaluator`. This is acceptable because the adapters own legal-domain/proposition differences rather than separate policy scoring.

`TroubleBrewingFirstNightInformationPropositionMaterializer` also contains named roles, but those branches encode different proposition semantics and therefore belong at the rules/semantic adapter boundary.

One future-risk area remains: the role-named Fortune Teller adapter must not become a template for role-specific cross-night coherence policy. When persistent impaired narrative state is implemented, continuation policy must move through shared semantic/history abstractions so equivalent boolean/target-check information in future scripts inherits the same behavior automatically.

Legacy `ImpairedInformationPolicy`, `RegistrationPolicy`, and the approximate impaired truthful/false-family bridge remain compatibility code by explicit roadmap decision. They are not evidence that the new D5F policy should be implemented through those legacy scalar heuristics.

