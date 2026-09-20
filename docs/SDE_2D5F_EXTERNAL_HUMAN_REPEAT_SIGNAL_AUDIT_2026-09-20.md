# SDE-2D5F — External Human Repeat-Signal Audit

> Date: 2026-09-20 Australia/Sydney
> Repository: Jazz0006/CampBoardGameHost
> Branch: sde-2d5-calibration-policy-evidence
> Status: CALIBRATION-ONLY EXTERNAL EVIDENCE — NO GATE FREEZE / NO HOLDOUT

## 1. Purpose

This audit classifies which BEGINNER-policy parameters now recur across independent real Trouble Brewing records and which remain plausible but weakly evidenced.

Reusable source catalog:

docs/SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv

Evidence hierarchy:

~~~text
highest:
    executable / high-fidelity real game record
    explicit Storyteller decision rationale

middle:
    detailed trajectory / postmortem
    repeated-group Storyteller observation

secondary:
    community advice / preference
~~~

A human-selected choice is positive observational evidence only. Unselected alternatives are not negative labels.

## 2. Confidence vocabulary

~~~text
STRONG_REPEAT
    independently appears in several real games or explicit ST rationales
    and has a coherent mechanical interpretation

REPEATED_EMERGING
    appears in at least two independent records
    but the exact policy shape or boundary remains unclear

REPEATED_SECONDARY
    repeated, but much of the support is advice/postmortem
    rather than exact Storyteller decision evidence

TENTATIVE
    plausible and worth measuring
    but still too dependent on one exact case or theory
~~~

## 3. STRONG_REPEAT — healthy-truth danger / Evil-topology coupling

Independent records repeatedly show Storytellers using Drunk or Poisoned information to suppress a healthy truth that would expose the actual Evil seating unusually hard.

Representative catalog records:

- ct-02: explicit choice between Drunk Chef and Drunk Investigator after seeing Evil clustered;
- rd-01: planned Drunk Investigator changed to Drunk Empath because Empath landed next to two Evil players;
- rd-02: Drunk Chef shown 0 while Demon and Minion were adjacent;
- rd-09: Poisoned Empath shown 0 while adjacent to the Poisoner;
- rd-14: postmortem where healthy Chef/Empath information rapidly collapsed Evil.

Conclusion:

TRUTH_DANGER / EVIL_TOPOLOGY_COUPLING is no longer a singleton hypothesis.

Future impaired-information evaluation should separate:

~~~text
healthyTruthDanger
    how strongly the healthy truth exposes the actual Evil topology

candidateImpairedPressure
    what the candidate impaired output does to the public strategic world
~~~

Two false outputs with similar marginal retention are not equivalent if one specifically prevents an accidental near-hard-solve of the real Evil topology.

## 4. STRONG_REPEAT — cross-channel narrative coherence

Real Storytellers repeatedly choose malfunctioning or registration information that reinforces a plausible live story rather than generating an isolated arbitrary falsehood.

Representative records:

- ct-01: poisoned Librarian Saint information coexists with Saint as a Demon bluff; Spy registration supports the Chef information channel;
- rd-04: poisoned Ravenkeeper is shown Mayor, matching the Poisoner bluff;
- rd-05: Red Herring plus Drunk Undertaker jointly creates a coherent false Scarlet-Woman world;
- rd-11: Drunk Ravenkeeper checks the Imp and is shown the Imp Soldier bluff;
- rd-12: Poisoned Ravenkeeper checking the Imp is shown Recluse, matching the live claim/world;
- rd-13: executed Spy registers as Washerwoman when shown to the Undertaker, supporting the Spy claim.

Conclusion:

REAL_INFORMATION_ANCHORING should be widened to CROSS_CHANNEL_NARRATIVE_COHERENCE.

For impaired information, evaluate whether a candidate:

- supports an existing Evil bluff or plausible counterworld;
- meshes with already-public healthy information;
- creates a coherent alternate explanation across multiple channels;
- or creates an isolated contradiction whose main explanation is simply that someone malfunctioned.

This is not a mandate to always protect an Evil claim.

## 5. STRONG_REPEAT — temporal consistency / impairment detectability

Several independent real discussions and game reports show that the same numeric output has different value depending on prior nights.

Representative records:

- rd-06: after healthy Empath 0, a sudden poisoned 1 can advertise Poisoner; continuing 0 may preserve more uncertainty;
- rd-10: a poisoned Fortune Teller changes from an earlier NO to a YES, with the prior check/history materially affecting plausibility;
- rd-17: independent discussion explicitly frames poisoning as creating believable false worlds and notes that coincidentally true information can be preferable to conspicuously false information;
- rd-04: the Storyteller retrospectively distinguishes a harsh first-night false Empath answer from what they would choose for inexperienced players.

Conclusion:

~~~text
droisoned => prefer false information
~~~

is rejected as a policy.

Future impaired-information selection needs at least:

~~~text
temporalConsistency
changePointDetectability
priorClaimCompatibility
~~~

A true candidate while impaired remains first-class when it produces the more believable public world.

## 6. STRONG_REPEAT — minimum healthy-information floor / strategic-pressure band

External evidence now supports both sides of the beginner objective.

- ct-01 is a real 14-player Night-1 ecology that preserved broad Evil topology while still providing several meaningful information channels.
- rd-07 reports an ecology with almost no healthy information as too opaque.
- rd-08 independently reports a beginner-heavy table complaining that there was too little information and nothing to work with.

Conclusion:

Night-1 strategic pressure is a band, not a minimization target.

A BEGINNER policy must reject both:

- excessive strategic collapse / near-hard-clears;
- excessive opacity / insufficient healthy information.

This reinforces the existing separation between strategic pressure and role-information utility.

## 7. REPEATED_EMERGING — Red-Herring trajectory value

At least two independent real games show Red Herring creating downstream epistemic effects rather than being a one-shot static setup value:

- ct-04: Fortune Teller repeatedly hits the Red Herring and repeatedly receives YES;
- rd-05: an immediate Red-Herring YES combines with Drunk Undertaker information to sustain a false Demon/Scarlet-Woman world.

Conclusion:

RED_HERRING_TRAJECTORY is repeated, but the objective remains unclear.

Do not maximize expected Red-Herring hits. Future diagnostics should distinguish:

- effectively inert placement;
- useful ambiguity;
- repeated interaction;
- placement so loud that the Fortune Teller can simply identify the Red Herring.

## 8. REPEATED_EMERGING — future correction capacity

The role selected as Drunk can determine how much future balance/control remains available.

- rd-03: explicit ST rationale for choosing Drunk Undertaker so future information could compensate if the Poisoner chose poorly;
- rd-04, rd-11 and rd-12: later impaired Ravenkeeper information is used to shape a live world after table state develops.

Two separate concepts must not be conflated:

~~~text
roleLevelControlReserve
    choosing an ongoing / later-trigger role as Drunk

decisionLevelCorrection
    choosing a later false/true result after the live game develops
~~~

Keep FUTURE_CORRECTION_CAPACITY as an emerging cross-night SDE axis.

## 9. REPEATED_SECONDARY — practical bluffability / claim burden

Real-game and community evidence repeatedly says that bluff roles differ sharply in execution burden.

- ct-03: human Saint / Monk / Investigator triplet has three different maintenance patterns;
- rd-15: beginner advice explicitly separates low-fabrication and information-bearing bluff options;
- rd-16: an Undertaker bluff without Spy went badly while easier bluffs were reserved for new Minions; the same discussion reports correct Chef numbers being used successfully as bluffs.

PRACTICAL_BLUFFABILITY and CLAIM_BURDEN / CADENCE should remain distinct diagnostics.

Important correction:

minimum mechanical burden is not automatically the best beginner bluff.

A passive identity may be easy to maintain but give a novice Evil player little useful material for information exchange. A separate beginner-agency / participation axis remains TENTATIVE until more direct real-game evidence exists.

## 10. REPEATED_EMERGING — narrative-route diversity

Two independent ClockTracker Storyteller choices show varied bluff types:

- ct-01: Chef / Investigator / Saint;
- ct-03: Saint / Monk / Investigator.

Secondary community evidence in rd-15 explicitly recommends offering choices from different behavioural groups.

This is sufficient to keep NARRATIVE_ROUTE_DIVERSITY as an emerging axis, but not to define a numeric target.

## 11. REPEATED_EMERGING — confirmation-chain / catastrophic-interaction suppression

Individual legal clues can become dangerous through interaction.

- rd-14: healthy Chef plus Empath / Recluse context rapidly narrows Evil;
- ct-02 and rd-01: Storytellers react to actual seating because otherwise one healthy channel becomes disproportionately decisive;
- rd-05: conversely, two misleading channels can reinforce the same false world.

This supports a bundle-level CONFIRMATION_CHAIN diagnostic. More executable cases are still needed to separate useful confirmation from unhealthy near-hard-clear.

## 12. TENTATIVE — shared-support coherence floor

Pilot 1 strongly rejects monotone higher-is-better shared/union.

External evidence does not yet establish the lower bound:

how little shared strategic support becomes genuinely too fragmented for a novice Demon?

Current state:

~~~text
monotone shared/union maximization
    REJECTED

minimum coherence floor
    PLAUSIBLE

threshold / exact ordering
    TENTATIVE
~~~

## 13. TENTATIVE — strategic coverage complementarity as an independent reward

Pilot 1 and varied human bluff triplets make complementary world coverage plausible.

Current external records do not prove that Storytellers selected those triplets because of complementary strategic coverage.

Keep the metric visible for diagnosis, but do not independently reward it yet.

## 14. Consequence for future Drunk / Poisoned information selection

Candidate generation remains rules-owned and complete.

Selection should compare each legal impaired-information candidate along separate interpretable axes:

~~~text
1. healthyTruthDanger
   How dangerous would the healthy truth be to the actual Evil topology?

2. impairedStrategicPressure
   How much does this candidate collapse strategic worlds?

3. healthyInformationFloorImpact
   Does the ecology retain enough meaningful reliable information for Good?

4. crossChannelNarrativeCoherence
   Does the result fit a plausible live world / bluff / registration story?

5. temporalConsistency
   Does it fit the recipient prior information history?

6. impairmentDetectability
   Would this answer itself loudly reveal malfunction?

7. futureCorrectionCapacity
   Does this choice preserve or exhaust useful future ambiguity/control?

8. beginnerRecoverability
   Can a novice player reasonably revisit and reinterpret the clue later?
~~~

Critical non-rule:

False information is not intrinsically preferred over true information for a Drunk or Poisoned player.

The preferred candidate is the mechanically legal candidate that produces an appropriate beginner-table ecology.

Do not combine these axes into an opaque global weighted scalar during D5F-B2.

## 15. Current confidence snapshot

~~~text
STRONG_REPEAT
    TRUTH_DANGER / EVIL_TOPOLOGY_COUPLING
    CROSS_CHANNEL_NARRATIVE_COHERENCE
    TEMPORAL_CONSISTENCY / IMPAIRMENT_DETECTABILITY
    MINIMUM_HEALTHY_INFORMATION_FLOOR / N1_PRESSURE_BAND

REPEATED_EMERGING
    RED_HERRING_TRAJECTORY
    FUTURE_CORRECTION_CAPACITY
    NARRATIVE_ROUTE_DIVERSITY
    CONFIRMATION_CHAIN / CATASTROPHIC_INTERACTION_SUPPRESSION

REPEATED_SECONDARY
    PRACTICAL_BLUFFABILITY
    CLAIM_BURDEN / CADENCE

TENTATIVE
    SHARED_SUPPORT_COHERENCE_FLOOR threshold
    STRATEGIC_COVERAGE_COMPLEMENTARITY as independent reward
    BEGINNER_AGENCY / PARTICIPATION as separate metric
~~~

This confidence classification is descriptive external evidence only. It does not freeze D5F gates and must not be copied into the eight-item human-label manifest.
