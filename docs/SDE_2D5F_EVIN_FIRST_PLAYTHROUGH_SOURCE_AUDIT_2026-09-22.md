# SDE-2D5F Evin First Playthrough Source Audit — 2026-09-22

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 (**must remain draft**)  
> Case: `goldcand-evin-01`  
> Primary recording: https://www.youtube.com/watch?v=4sfa8_kNxsQ

## 1. Decision

The 2019 Evin Trouble Brewing recording has now moved from source-discovery status to a **primary-verified, executable material Night-1 decision slice**.

Current status:

```text
official TPI provenance                 VERIFIED
Evin as Storyteller                     VERIFIED
Trouble Brewing                         VERIFIED
8-player game                           VERIFIED
experienced/regular early players       VERIFIED AT SOURCE-DESCRIPTION LEVEL
full seating / actual roles             PRIMARY VERIFIED
Drunk shown identity                    PRIMARY VERIFIED
Red Herring                             PRIMARY VERIFIED
Washerwoman output                      PRIMARY VERIFIED
Chef output                             PRIMARY VERIFIED
Fortune Teller targets/result           PRIMARY VERIFIED
Demon bluff triplet                     PRIMARY VERIFIED
choice-specific rationale               PRIMARY VERIFIED FOR RED HERRING
production legal-counterfactual replay  EXECUTABLE FOR MATERIAL SLICE
GOLD decision-slice admission           YES
```

Canonical primary reconstruction:

`docs/SDE_2D5F_EVIN_FIRST_PLAYTHROUGH_PRIMARY_RECONSTRUCTION_2026-09-22.md`

## 2. Source triangulation

Three public source surfaces identify the same recording:

1. TPI's current Trouble Brewing page presents it as Evin running Trouble Brewing for some of BotC's earliest playtesters and fans:
   - https://bloodontheclocktower.com/pages/where-to-buy-trouble-brewing
2. TPI's Kickstarter FAQ states that the first full play-through was released on 21 April 2019 and links:
   - https://youtu.be/4sfa8_kNxsQ
   - https://www.kickstarter.com/projects/pandemoniuminstitute/blood-on-the-clocktower/faqs
3. The Kickstarter campaign update is titled **"The First Full Play-Through of Blood on the Clocktower - Trouble Brewing"** and describes the game as eight regular Sydney players playing Trouble Brewing:
   - https://www.kickstarter.com/projects/pandemoniuminstitute/blood-on-the-clocktower/posts/2486129

These facts establish source authenticity and player-context provenance independently of the manual primary-video reconstruction.

## 3. Primary extraction result

The direct primary review recovered:

- Doug — Chef;
- Claire — Fortune Teller;
- Sarah-Regina — Washerwoman;
- Lewis — Scarlet Woman;
- Julian — Imp;
- Marianna — Drunk shown Monk;
- Filip — Undertaker;
- Michael — Virgin;
- 06:15 Washerwoman Sarah-Regina learns Julian or Filip is the Undertaker;
- 06:27 Chef Doug receives 1;
- 06:49 Fortune Teller Claire selects Doug + Sarah-Regina;
- 06:55 Claire receives YES because Doug is the Red Herring;
- postgame screenshot: Demon bluffs = Recluse / Slayer / Soldier;
- postgame review: Evin explains that Doug/Chef was selected as Red Herring because Chef's information was especially damaging to Evil, so Red-Herring contamination could make the table doubt Doug and the Chef information.

The production evidence harness now reconstructs the same state and asks existing legality owners for the legal alternatives rather than encoding hand-authored outcome rules.

## 4. Demon-bluff and rationale update

A later primary postgame screenshot resolves the bluff triplet directly:

```text
Demon bluffs = Recluse / Slayer / Soldier
```

This supersedes the earlier unknown-bluff boundary. The prior comment-level Recluse hint is retained only as corroborating secondary context.

The postgame explanation also recovers a choice-specific rationale for Red Herring: Doug's Chef information materially hurt Evil, and making Doug the Red Herring could make other players distrust Doug and his information after a Fortune Teller hit.

This is a high-value primary rationale for truth danger / credibility disruption. It is not permission to hard-code Chef, seat 1, or a role-strength ordering into policy.

## 5. Rule-derived consequences

Production semantics confirm:

- the observed Washerwoman clue is truthful because Filip is the Undertaker;
- the observed clue needs no Spy/Recluse registration witness;
- Chef=1 is forced by the single adjacent Evil pair Lewis/Scarlet Woman + Julian/Imp;
- after Doug is committed as Red Herring and Claire chooses Doug + Sarah-Regina, Fortune Teller YES is forced.

Therefore Chef=1 and the FT YES are mechanical observations, not free Storyteller output choices at those lifecycle stages.

The Washerwoman pair is a genuine Storyteller-controlled choice. Red Herring placement is also Storyteller-controlled, while the later FT hit is trajectory evidence and must not be leaked backward as hindsight during Red Herring counterfactual comparison.

## 6. GOLD decision-slice boundary

GOLD admission is scoped to the reconstructed **material decision slice**.

A hidden field that is unavailable in the source does not block a decision slice when:

1. production semantics establish that the field cannot change that decision's legal alternative set or consequence semantics; and
2. the unavailable field is explicitly retained as unknown rather than inferred.

That rule is stricter than silently completing the game state from comments or generic setup advice.

For the Evin slice:

- expertise is independently verified;
- the game is authentic;
- all state material to the admitted decisions is primary-verified;
- the observed choices/results are recoverable;
- complete legal alternatives for those decisions are production-owned and executable.

Therefore `goldcand-evin-01` is admitted as GOLD for this material Night-1 slice and supplies the second Storyteller independence key, `st-evin`.

## 7. Historical anti-inference guards retained

Do not substitute the rulebook's recommended eight-player bag or reuse an unrelated early TPI lineup. Those earlier warnings remain valid even though the actual primary setup is now known.

Do not convert the absence of choice-specific rationale into an inferred preference ordering. Observed choice is evidence; unchosen legal alternatives are not automatically bad.
