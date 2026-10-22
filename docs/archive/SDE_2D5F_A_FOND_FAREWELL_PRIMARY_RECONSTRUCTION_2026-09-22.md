# SDE-2D5F A Fond Farewell Primary Reconstruction — 2026-09-22

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 (**must remain draft**)  
> Case: `goldcand-ben-04`  
> Primary source: https://www.youtube.com/watch?v=M5VY5GnXAxw  
> Storyteller: Ben Burns / independence key `st-ben-burns`

## 1. Status

The material first-night state for `A Fond Farewell` has now been manually checked against the official primary video.

This upgrades the source-state verification from “chapter metadata only” to **primary-video verified reconstruction material**.

It does **not** yet admit the case as GOLD calibration evidence, because the live game contains five Trouble Brewing Travellers and the current production/evidence model does not expose Traveller roles/alignment as a canonical legality input. The B4 architecture requires production owners to own legality; the evidence harness must not invent a parallel Traveller-specific rules model merely to make this fixture executable.

Current case status:

```text
primary material state                         VERIFIED
expert/trusted Storyteller                     VERIFIED
observed Night-1 choices                       RECOVERED
choice-specific rationale                      RECOVERED for Red Herring + Drunk-Chef
full table / role mapping                      RECOVERED
Traveller alignment                            RECOVERED
production-executable legal counterfactuals    BLOCKED_BY_TRAVELLER_DOMAIN
GOLD admission                                 NOT YET
```

## 2. Provenance discipline

The timestamps below were manually transcribed from the official video. A full-grimoire screenshot from the same primary recording was used to recover player↔role placement.

The video itself displays a circular table without numeric seat labels. The seat numbers below are therefore **analysis-derived identifiers only**:

- seat 1 is assigned to InspirationFollows;
- numbering increases clockwise;
- the numbers are not claimed to be source-authored seat numbers.

Player↔role mapping must follow each player's radial position toward the role token, not nearest-label distance. This matters particularly in the lower-left sector: **Malakai is the Washerwoman and Rachel is the Gunslinger**.

## 3. Primary-verified table state

| Derived seat | Player | Actual / shown character | Alignment / status |
|---:|---|---|---|
| 1 | InspirationFollows | Scarlet Woman | Evil Minion |
| 2 | Buster | Slayer | Good Townsfolk |
| 3 | Kyra | Saint | Good Outsider |
| 4 | Ellen | Poisoner | Evil Minion |
| 5 | Lyra | Undertaker | Good Townsfolk; Red Herring |
| 6 | Viva La Sam | Fortune Teller | Good Townsfolk |
| 7 | Ekin | Imp | Evil Demon |
| 8 | patters | Soldier | Good Townsfolk |
| 9 | Barrow | Ravenkeeper | Good Townsfolk; poisoned on Night 1 |
| 10 | Iris | Baron | Evil Minion |
| 11 | Maggot | Mayor | Good Townsfolk |
| 12 | Gecko | Drunk, shown Chef | Good Outsider; believes Chef |
| 13 | Ffin | Recluse | Good Outsider |
| 14 | Chiz | Bureaucrat | Evil Traveller |
| 15 | Rachel | Gunslinger | Good Traveller |
| 16 | Malakai | Washerwoman | Good Townsfolk |
| 17 | Reznora | Scapegoat | Evil Traveller |
| 18 | Malashaan | Butler | Good Outsider |
| 19 | Milk | Beggar | Good Traveller |
| 20 | sincerity | Thief | Good Traveller |

The 15 non-Traveller characters are therefore consistent with a Baron-modified 15-player Trouble Brewing setup: 7 Townsfolk, 4 Outsiders, 3 Minions, 1 Demon.

## 4. Primary timeline

| Video time | Primary observation | Evidence interpretation |
|---|---|---|
| 03:02 | Bureaucrat chosen Evil | Chiz / Bureaucrat = Evil Traveller |
| 03:11 | Scapegoat chosen Evil | Reznora / Scapegoat = Evil Traveller; all other Travellers are Good |
| 03:43 | Drunk shown Chef | Gecko = actual Drunk, perceived Chef |
| 03:55 | Washerwoman information selected: Fortune Teller between the actual Fortune Teller and Mayor | Later delivered to Malakai; candidates are Viva La Sam (actual FT) and Maggot (Mayor) |
| 04:17 | Undertaker marked as Red Herring | Lyra / Undertaker = Red Herring |
| 04:17 | Ben rationale: “FT often choose their neighbors as well” | Choice-specific Red Herring rationale tied to anticipated player target behavior |
| 04:47 | Demon bluffs: Empath, Monk, Virgin | Setup-persistent bluff set |
| 06:26 | Poisoner targets Barrow / Ravenkeeper | Barrow is poisoned for Night 1 |
| 06:41 | Washerwoman Malakai is woken and receives the 03:55 information | Observed healthy Washerwoman clue |
| 07:16 | Drunk-shown-Chef Gecko is given 4 | Impaired numeric output |
| 07:16 | Ben rationale: “a somewhat believable number” | Choice-specific impaired-information rationale |
| 08:05 | Fortune Teller selects Malashaan + Malakai | Butler + Washerwoman; neither is Demon or Red Herring |

For the 08:05 Fortune Teller choice, **NO** is rule-derived from the primary-verified fixed targets and reconstructed state. It was not separately transcribed as a spoken result in the manual extraction, so the corpus should preserve the distinction between primary-observed target choice and rule-derived answer.

## 5. Derived facts — not additional primary observations

### 5.1 Actual Chef truth

Using the recovered clockwise seating and actual alignments, the six Evil players are:

- InspirationFollows / Scarlet Woman;
- Ellen / Poisoner;
- Ekin / Imp;
- Iris / Baron;
- Chiz / Bureaucrat Traveller;
- Reznora / Scapegoat Traveller.

None of those six Evil players are adjacent to another Evil player in the recovered 20-seat circle.

Therefore the **actual functioning Chef value is 0**.

Gecko is the Drunk shown Chef and received **4**, so the observed value is false in the actual world. This is useful expert evidence because Ben explicitly describes the selected false value as “a somewhat believable number”, rather than describing the goal as “make it false”.

Do not yet convert this into a numeric preference weight.

### 5.2 Washerwoman

Malakai's observed Washerwoman clue points to:

- Viva La Sam = actual Fortune Teller;
- Maggot = Mayor.

So the observed clue is a normal truthful Washerwoman construction with one actual matching Townsfolk.

### 5.3 Fortune Teller

The fixed targets are Malashaan / Butler and Malakai / Washerwoman.

Neither target is:

- the Imp;
- the Red Herring (Lyra / Undertaker);
- a Recluse capable of interaction-local Demon registration.

The reconstructed functioning result is therefore NO.

## 6. Why this case matters to the current policy model

This case provides two unusually strong **choice-specific expert rationales**.

### Red Herring: anticipated player behavior

Ben's explanation that Fortune Tellers often choose their neighbors supports a policy dimension that is not reducible to static role strength or seat identity:

> Red Herring placement may consider the probability of a player actually selecting the seat.

This is evidence for a generic **player-choice-likelihood / target-ecology** dimension. It is not yet evidence for a fixed neighbor bonus or weight.

### Drunk information: believable counterworld

Ben's “a somewhat believable number” comment directly supports the current shared impaired-narrative direction:

> impaired information should inhabit a believable perceived world, rather than merely invert truth.

This is consistent with the existing role-agnostic persistent impaired-narrative architecture. It does not establish that 4 is globally preferred, nor that false information is always preferred.

## 7. Architecture finding: Travellers are now a real calibration dependency

This case also exposes a concrete model boundary that cannot be ignored.

The Night-1 Chef meaning depends on:

- the full circular seating, including Travellers;
- the public Traveller alignments;
- adjacency across Traveller/base-player boundaries.

The current repository has no existing Traveller domain surface found by the B4 audit (`Bureaucrat` / `TRAVELLER` are not represented in the current production rules model searched for this slice).

Therefore **do not** make this case executable by:

- deleting Travellers from the table;
- compressing the 20-seat circle into 15 base seats;
- hard-coding Chef=4 as a fixture special case;
- adding an evidence-only alternate Chef arithmetic implementation.

Any of those would corrupt either adjacency, legality, or ownership.

The correct future path is:

1. decide whether Traveller support belongs in the current product scope;
2. if yes, add canonical Traveller character/alignment/seating semantics in the production rules/domain owner;
3. then project this already-primary-verified case through the ordinary B4 legal-domain and consequence machinery;
4. only after production-owned legal counterfactual recovery may the case satisfy the executable-GOLD gate.

## 8. Current policy-use boundary

Allowed now:

- retain the primary-verified expert rationale;
- strengthen evidence that believable impaired narratives and player-choice likelihood deserve independent policy dimensions;
- use the case as a Traveller-support requirements fixture;
- preserve the exact observed state for later executable reconstruction.

Not allowed now:

- rank Chef 4 against legal alternatives using a hand-built Traveller model;
- infer that Undertaker is the best Red Herring;
- freeze a neighbor bonus;
- freeze an impaired-information truth/falsehood weight;
- admit this case as executable GOLD before production-owned Traveller legality exists.
