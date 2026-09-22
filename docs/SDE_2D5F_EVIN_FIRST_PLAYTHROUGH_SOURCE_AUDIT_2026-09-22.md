# SDE-2D5F Evin First Playthrough Source Audit — 2026-09-22

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 (**must remain draft**)  
> Case: `goldcand-evin-01`  
> Primary recording: https://www.youtube.com/watch?v=4sfa8_kNxsQ

## 1. Decision

Keep the 2019 Evin Trouble Brewing recording as the next independent-Storyteller primary extraction target.

The source identity and Storyteller provenance are strong enough for a GOLD candidate, but the material Night-1 decision state has **not** yet been extracted from the primary recording. Do not infer that state from generic first-game setup guidance, unrelated early TPI videos, or search snippets.

Current status:

```text
official TPI provenance                 VERIFIED
Evin as Storyteller                     VERIFIED
Trouble Brewing                         VERIFIED
8-player game                           VERIFIED
experienced/regular early players       VERIFIED AT SOURCE-DESCRIPTION LEVEL
full seating / actual roles             NOT EXTRACTED
setup commitments                       NOT EXTRACTED
Night-1 outputs                         NOT EXTRACTED
choice-specific rationale               NOT EXTRACTED
production legal-counterfactual replay  BLOCKED ON STATE EXTRACTION
GOLD admission                          NOT YET
```

## 2. Source triangulation

Three public source surfaces identify the same recording:

1. TPI's current Trouble Brewing page presents it as Evin running Trouble Brewing for some of BotC's earliest playtesters and fans:
   - https://bloodontheclocktower.com/pages/where-to-buy-trouble-brewing
2. TPI's Kickstarter FAQ states that the first full play-through was released on 21 April 2019 and links:
   - https://youtu.be/4sfa8_kNxsQ
   - https://www.kickstarter.com/projects/pandemoniuminstitute/blood-on-the-clocktower/faqs
3. The Kickstarter campaign update is titled **"The First Full Play-Through of Blood on the Clocktower - Trouble Brewing"** and describes the game as eight regular Sydney players playing Trouble Brewing:
   - https://www.kickstarter.com/projects/pandemoniuminstitute/blood-on-the-clocktower/posts/2486129

These facts strengthen source authenticity and player-experience context. They do **not** recover the hidden Grimoire/Night-1 state.

## 3. Indexed-web extraction result

A targeted indexed-web search on 2026-09-22 did not expose a reliable transcript, timestamped Night-1 log, complete role table, or setup-state reconstruction for this exact YouTube ID.

Therefore the next state-extraction step must be direct inspection of the primary recording rather than another round of generic web inference.

This negative result is worth recording so future B4 work does not repeatedly re-run the same metadata-only search and mistake source provenance for decision-state verification.

## 4. Two explicit anti-inference guards

### 4.1 Do not substitute the rulebook's recommended 8-player setup

Current Trouble Brewing setup guidance contains a recommended first-game eight-player bag:

```text
Chef
Empath
Fortune Teller
Undertaker
Virgin
Drunk shown Investigator
Scarlet Woman
Imp
```

That is generic setup guidance. It is **not evidence** that the Evin video used that exact bag.

Do not copy these roles into `goldcand-evin-01` unless the primary recording verifies them.

### 4.2 Do not reuse the lineup from the older "An Introduction to Blood on the Clocktower" video

An older TPI blog post describes a different filmed game with an 11-seat lineup including an Evil Bone Collector Traveller. That material belongs to a different recording and cannot be used to reconstruct `4sfa8_kNxsQ`.

The presence of early-TPI player names or similar filming context is not enough to merge the records.

## 5. Required direct-primary extraction

When the recording is inspected, recover only source-visible or safely rule-derived facts, with timestamps:

1. clockwise player order;
2. actual role at each seat;
3. Drunk actual identity and shown Townsfolk identity, if present;
4. Demon bluff triplet;
5. Fortune Teller Red Herring;
6. Poisoner target, if present;
7. all first-night Storyteller-controlled information outputs;
8. all player-selected first-night targets needed to interpret those outputs;
9. any Spy/Recluse registration ruling needed to make an observed output legal;
10. any explicit Storyteller rationale tied to a specific decision.

Keep the same provenance distinction used for `A Fond Farewell`:

```text
primary-observed fact
vs
rule-derived consequence
vs
analysis-derived seat identifier
```

## 6. GOLD boundary

Do not promote this case to admitted GOLD until both are true:

1. the material committed Night-1 state is verified from the primary recording; and
2. the complete legal counterfactual domain can be recovered through existing production legality owners.

Once the primary state is recovered, this case is especially valuable because it provides a second Storyteller independence key (`st-evin`) rather than adding more volume to `st-ben-burns`.

No policy weight, preference ordering, or BAD/ACCEPTABLE label follows from source authenticity alone.
