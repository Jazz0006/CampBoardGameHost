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

## 11. Drunk information

The Drunk's shown identity is committed at setup and is not reselected by the later SDE.

For a Drunk information role, do not model night-one output as an isolated false number/pair.

Instead maintain a persistent **shadow world / narrative intent**:

~~~text
Drunk shown role
    + selected believable false-world interpretation
        ↓
night 1 output
        ↓
later deaths / seating changes / observations
        ↓
next output consistent with the same misleading world when feasible
~~~

The goal is not "always false."

Truthful information is legal and may be preferable when a false result would immediately reveal the Drunk or create an incoherent trajectory.

For multi-night roles, temporal coherence is a first-class policy concern.

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

> **Enumerate every legal first-night information outcome, preserve role/lifecycle ownership, prefer Spy-as-Good and Recluse-as-Evil registration by default, avoid directly exposing Recluse to the Librarian or Spy to the Investigator when healthy alternatives exist, and select information as a whole-table bundle whose goal is a playable middle band rather than a maximum/minimum metric. Drunk misinformation must form a believable cross-night story rather than merely be false.**


## 19. Active documentation authority

For current execution, read:

1. `CURRENT_DEVELOPMENT_ROADMAP.md`;
2. `NEXT_DEVELOPMENT_HANDOFF.md`;
3. this synthesis;
4. `SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`.

The 2026-09-20 D5 policy-correction, extreme-fixture-correction and representative-clean-corpus design documents were removed from active docs after their valid conclusions were folded into this synthesis. Git history is sufficient for historical traceability.

Do not revive those deleted routes as parallel calibration authorities.
