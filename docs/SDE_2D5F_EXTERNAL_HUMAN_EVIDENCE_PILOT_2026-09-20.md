# SDE-2D5F — External Human Evidence Pilot

> Date: 2026-09-20 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> Status: **CALIBRATION-ONLY EXTERNAL VALIDITY EVIDENCE — NOT D5F LABELS / NOT HOLDOUT**

## 1. Purpose

This note records the first external-human Storyteller evidence pilot and the BEGINNER-policy hypotheses it exposed.

External human choices are **positive-unlabeled observational evidence**:

- a human-selected candidate is evidence that an experienced Storyteller considered that choice usable in one real ecology;
- an unselected legal candidate is **not** a negative label;
- this corpus must not replace the controlled D5F human review;
- it must not expose or tune against the sealed holdout;
- it must not directly freeze thresholds or cut production policy over.

The intended sequence is:

~~~text
D5F controlled human review
    +
external-human pilot evidence
    ↓
interpretable multi-axis beginner-policy hypotheses
    ↓
D5F-C candidate gates/orderings
~~~

## 2. Pilot 1 — ClockTracker 14-player Trouble Brewing

Public source game:

`ffb40a93-3d7b-42c4-bba8-bc9c363dcd30`

Observed Night-1 Storyteller choices reconstructed by the test-only pilot:

- Demon bluffs: Chef / Investigator / Saint;
- Red Herring: seat 10 Monk;
- Washerwoman: seats 12 / 2 as Chef, requiring Spy registration;
- poisoned Librarian: seats 1 / 9 as Saint;
- Drunk shown Empath: 0;
- Fortune Teller: seats 7 / 6 -> NO.

Executable pilot:

- `app/src/test/java/com/codex/campboardgamehost/clocktower/review/Sde2D5ExternalHumanPilot.kt`;
- dedicated workflow run `35446943045` — SUCCESS;
- ordinary CI run `35446945108` — SUCCESS;
- R2 run `35446945073` — SUCCESS.

The pilot is test/review-only. Production recommendation selection, the eight-item D5F human-label manifest and sealed holdout remain unchanged.

## 3. First external-human findings

### 3.1 Expressiveness passed

The observed human choices all remained representable:

- 35 legal Demon-bluff triplets existed;
- Chef / Investigator / Saint was legal;
- Red Herring seat 10 was legal;
- Drunk shown-Empath domain was 0 / 1 / 2;
- observed Drunk 0 was legal and semantically false;
- the complete observed public Night-1 ecology retained at least one legal strategic witness for every recipient.

This is an external validity check on candidate-domain and topology-first expressiveness, not a quality label.

### 3.2 Legacy setup heuristic does not reproduce the human choice

The current production compatibility heuristic kept the observed plan in `RECOMMENDED`, but many plans ranked above it.

Across GENTLE / BALANCED / AGGRESSIVE, the top legacy plan used:

- Red Herring seat 14;
- Butler / Chef / Investigator bluffs.

The observed plan used:

- Red Herring seat 10;
- Chef / Investigator / Saint bluffs.

Therefore old `bluffDifficulty / demon-bluff-ease` remains useful as one practical-bluffability signal but is not an adequate sole owner of beginner bluff quality.

### 3.3 Drunk misinformation suggests a bounded-pressure objective

The healthy Empath value in the reconstructed seating was 1.

Observed human Drunk information was 0.

Across ordinary good-recipient perspectives, the recurring pattern was approximately:

~~~text
healthy value 1       -> weakest strategic reduction
human false value 0   -> intermediate strategic reduction
alternate false 2     -> stronger strategic reduction
~~~

The observed human choice is therefore consistent with a BEGINNER policy that permits useful misinformation without maximizing strategic collapse.

Do not interpret one case as a threshold. Treat it as evidence for a Drunk-marginal **acceptable band**, not a monotone "more false / more collapsing is better" objective.

### 3.4 Demon-bluff shared/union is not monotone quality

For many recipient perspectives the observed Chef / Investigator / Saint triplet had:

~~~text
0 legal triplets with lower shared/union
12 equal
23 higher
~~~

The human Storyteller therefore selected a legal triplet near the low end of the current shared/union axis rather than maximizing common strategic support.

Frozen correction:

**Do not derive a monotone "higher shared/union is better" BEGINNER policy.**

Shared/union may remain useful as a **coherence / fragility floor**, but once adequate coherence exists, additional shared support may have little or negative value if it sacrifices distinct bluff routes.

## 4. BEGINNER-policy hypothesis exposed by the pilot

The current target is not "make Evil maximally strong".

It is:

> reduce execution burden for a novice Demon while preventing Night-1 information from collapsing the strategic game too early.

Demon-bluff quality should therefore remain multi-axis.

### 4.1 Practical bluffability / execution difficulty

Each bluff should be usable by a novice Demon without requiring:

- complex fabricated night histories;
- high memory burden;
- highly precise mechanical claims;
- advanced social coordination before the player understands the table.

Legacy `bluffDifficulty` may contribute evidence here, but cannot own the final decision.

### 4.2 Real-information anchoring

Prefer bluff routes that can reuse or plausibly attach to real table information.

Examples include:

- a bluff that naturally explains an existing public information thread;
- a bluff that can borrow a mechanically plausible target/result structure;
- a bluff that lets Evil tell a story using facts players already know rather than inventing an isolated narrative.

This is especially valuable for novice Demons.

### 4.3 Narrative-route diversity

The three Demon bluffs should provide meaningfully different play routes rather than three variants of the same claim burden.

Potential diagnostics include role-behaviour class, information cadence and claim-maintenance burden.

The goal is graceful fallback:

~~~text
route A becomes implausible
    -> route B or C remains independently playable
~~~

### 4.4 Strategic-world coverage diversity

Different bluffs supporting different strategic-world regions is not automatically fragility.

After a minimum coherence floor, distinct coverage may be an advantage because the Demon can select the route that fits the evolving public world.

Therefore distinguish:

- catastrophic incompatibility;
- adequate common support;
- useful complementary coverage.

Do not collapse these into one shared/union score.

### 4.5 Night-1 strategic-pressure cap

BEGINNER Night-1 information should be meaningful but should not maximize good-team world reduction.

Desired shape:

- preserve broad Demon cover;
- preserve multiple Evil seat topologies;
- avoid excessive forced-good seats / hard-clear chains;
- retain enough role-information utility to generate discussion and later verification.

This is a pressure **band / gate**, not a target to maximize or minimize.

## 5. Additional axes to investigate in the next real cases

The next 2–3 public Trouble Brewing cases should explicitly test whether human Storytellers appear to react to:

1. **evil-topology coupling** — whether Drunk/poisoned information is chosen in response to Evil seating adjacency or other dangerous true patterns;
2. **confirmation-chain suppression** — whether information is selected to avoid two healthy facts mutually hard-confirming players;
3. **bluff-to-live-information anchoring** — whether Demon bluffs align with likely/actual information claims already present in the setup;
4. **claim burden / cadence** — one-shot first-night bluff versus bluff requiring repeated nightly fabricated information;
5. **route diversity** — information role / protective role / passive or ability-demonstration role mix;
6. **coverage complementarity** — different bluffs preserving different strategic regions after a coherence floor;
7. **misinformation pressure band** — false Drunk/Poisoned outputs that are neither trivial nor maximally collapsing;
8. **Red-Herring interaction value** — whether placement amplifies a useful ambiguity without creating an immediate hard trap;
9. **new-player recoverability** — whether a bluff route remains playable after a novice makes an imperfect Day-1 claim.

These are hypotheses to collect evidence for, not yet policy gates.

## 6. D5F lifecycle impact

Insert a small **D5F-B2 External Human Evidence Pilot** before D5F-C gate derivation.

D5F-B2 may:

- add test/review-only real-game fixtures;
- calculate existing D5 diagnostics;
- add descriptive diagnostics for the hypotheses above;
- record observed human choices;
- compare observed choices with the legal candidate domain.

D5F-B2 must not:

- infer negative labels from unselected choices;
- modify the eight-item human-label manifest automatically;
- derive/freeze thresholds;
- inspect the sealed holdout;
- change visible production recommendation selection.

D5F-C may begin only after controlled human review is complete and the external-human pilot findings have been reviewed for whether additional interpretable axes are required.
