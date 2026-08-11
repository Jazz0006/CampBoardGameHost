# Trouble Brewing epistemic reference matrix

> Project: CampBoardGameHost  
> Milestone: Phase A / PR A0 + A2.1 executable corpus  
> Version: 1.1  
> Date: 2026-08-11

## 1. Scope and notation

This matrix defines the first frozen golden-scenario catalog for the player-perspective world engine. A2.1 has converted all 33 original entries into machine-readable official contracts and expanded the corpus to 48 scenarios; A3/B4 will execute the world/timeline implementation against them.

Oracle abbreviations:

- **OFFICIAL** — official role text, almanac or published ruling; authority, not executable oracle.
- **ASP** — `pnkfelix/botc-asp@616e61b720cc853af031f2623fd6bde33b869865`.
- **ENUM** — future CampBoardGameHost `EnumeratedWorldSet` baseline.
- **ZDD** — `pnkfelix/botc-zdd-@0bbe6fa07afe84ab506e772315d0f7edc305939d`.
- **ORZ** — `olarozenfeld/botc@fc919f19356f78aa9fd22f036f5fe63257d7fde8` where its log semantics cover the case.

Expected result terminology:

- **SAT** — at least one mechanically possible world is consistent with the perspective and observation.
- **UNSAT** — no mechanically possible world is consistent.
- **CHOICE** — multiple official-legal storyteller outcomes must remain separate possible branches.
- **STATE** — deterministic transition expected in the actual-world rules layer.

These scenarios do not assign likelihoods. Player claims and storyteller-only secrets are excluded unless the scenario explicitly places them inside the selected perspective.

## 2. Golden scenario catalog

| ID | Area | Frozen setup / action / perspective | Query or observation | Expected result | Primary validators |
|---|---|---|---|---|---|
| TB-SETUP-01 | Base setup | 7 players, no setup-modifying ability | role-count profile | exactly 5 Townsfolk, 0 Outsiders, 1 Minion, 1 Demon | OFFICIAL, ASP, ENUM, ZDD |
| TB-SETUP-02 | Baron | 8 players with Baron in play | role-count profile | base 5/1/1/1 becomes 3 Townsfolk, 3 Outsiders, 1 Minion, 1 Demon | OFFICIAL, ASP, ENUM, ZDD |
| TB-SETUP-03 | Drunk | Drunk is in play and receives a not-in-play Townsfolk token | perspective may assign perceived role as actual | actual-role world is SAT; world treating Drunk as that actual Townsfolk is UNSAT in storyteller truth, but may remain in that player's perspective | OFFICIAL, ASP, ENUM, ORZ |
| TB-SETUP-04 | Evil knowledge | 6-player Teensyville game | Minion/Demon mutual knowledge | evil players do not learn each other; worlds requiring that private knowledge are excluded from their perspective facts | OFFICIAL, ASP, ENUM |
| TB-SETUP-05 | Evil knowledge | 7-player standard game | Minion/Demon mutual knowledge | Minion learns Demon; Demon learns Minion and three not-in-play good bluffs | OFFICIAL, ASP, ENUM |
| TB-WW-01 | Washerwoman | healthy Washerwoman; exactly one displayed pair member is the shown Townsfolk in actual state | pair observation | SAT | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-WW-02 | Washerwoman | healthy Washerwoman; neither displayed pair member can be the shown Townsfolk under any legal registration | pair observation | UNSAT | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-LIB-01 | Librarian | healthy Librarian; an Outsider is in play | two seats + shown Outsider | SAT only when at least one shown seat can be that Outsider | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-LIB-02 | Librarian | healthy Librarian; no Outsiders are in play | zero-Outsider observation | SAT | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-LIB-03 | Librarian/Drunk | recipient is actually Drunk and perceives Librarian in a no-Outsider setup | arbitrary nonzero Librarian pair | SAT in the recipient's malfunction-allowed perspective; not proof that an Outsider exists | OFFICIAL, ASP, ENUM |
| TB-INV-01 | Investigator | healthy Investigator; a Minion is in play | two seats + actual Minion role | SAT when at least one seat can be that Minion | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-INV-02 | Investigator/Spy | healthy Investigator; Spy is the only actual Minion | pair showing Spy | SAT; natural true candidate must exist | OFFICIAL, ASP, ENUM, ZDD |
| TB-INV-03 | Investigator/Spy boundary | healthy Investigator; Spy is the only actual Minion | pair showing a different Minion by treating Spy as that Minion | UNSAT; Spy may register as good/Townsfolk/Outsider, not as a different Minion | OFFICIAL, ASP, ENUM, ZDD |
| TB-SPY-01 | Washerwoman/Spy registration | healthy Washerwoman; Spy is in one displayed seat | pair showing a specific Townsfolk with Spy registering as that good character | CHOICE and SAT only when the local registration fact is bound to the observation | OFFICIAL, ASP, ENUM, ZDD |
| TB-CHEF-01 | Chef | three adjacent evil seats around the circle | Chef number | count adjacent evil pairs, not evil players; the three-seat run contributes 2 | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-CHEF-02 | Chef/Recluse | Recluse adjacent to evil; healthy Chef | count with Recluse as evil vs good | CHOICE; both legal registration branches remain distinct complete outcomes | OFFICIAL, ASP, ENUM, ZDD |
| TB-EMPATH-01 | Empath | healthy Empath with two living neighbours, one evil | number 1 | SAT; 0 or 2 is UNSAT absent registration | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-EMPATH-02 | Empath/death | one immediate neighbour is dead; next living player is evil | next-night number | dead player is skipped; living-neighbour result is used | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-FT-01 | Fortune Teller | healthy FT checks two seats containing actual Demon | yes/no observation | YES is SAT; NO is UNSAT absent malfunction | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-FT-02 | Fortune Teller/red herring | healthy FT checks red herring and a non-Demon | yes/no observation | YES is SAT because red herring registers as Demon to this ability | OFFICIAL, ASP, ENUM, ZDD |
| TB-FT-03 | Fortune Teller/Recluse | healthy FT checks Recluse, not Demon or red herring | result with/without Demon registration | CHOICE: YES requires bound Recluse-as-Demon registration; NO remains legal without it | OFFICIAL, ASP, ENUM, ZDD |
| TB-MAL-01 | Poisoned information | Poisoner selects Empath for current night/day interval | arbitrary number | both truthful and false mechanically legal observations remain SAT in malfunction-allowed perspective | OFFICIAL, ASP, ENUM, ZDD |
| TB-MAL-02 | Poison duration | Poisoner changes target on next night | old and new target state | old target becomes healthy; new target is poisoned for the new interval | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-IMP-01 | Soldier interaction | poisoned Soldier is selected by Imp | night death | Soldier dies; malfunctioning ability does not protect | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-IMP-02 | Monk protection | healthy Monk protects another player; Imp selects that player | night death | protected target survives | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-IMP-03 | Imp starpass | Imp selects self; at least one living Minion | role transition | Imp dies and one living Minion becomes Imp | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-SW-01 | Scarlet Woman | Demon dies with at least five players alive and healthy Scarlet Woman alive | role transition | Scarlet Woman becomes Demon; game does not end from that Demon death | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-UT-01 | Undertaker | a player was executed and died yesterday; Undertaker healthy tonight | learned role | actual executed character is SAT; incompatible role is UNSAT absent malfunction/registration | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-RK-01 | Ravenkeeper | Ravenkeeper dies at night and selects a player | learned role | selected player's role is learned; incompatible result requires malfunction/registration branch | OFFICIAL, ASP, ENUM, ZDD |
| TB-SLAYER-01 | Slayer/Recluse | healthy Slayer shoots Recluse | death outcome | CHOICE: survives without Demon registration; may die when Recluse registers as Demon | OFFICIAL, ASP, ENUM, ZDD |
| TB-VIRGIN-01 | Virgin | healthy Virgin first nominated by healthy Townsfolk | execution transition | nominator is immediately executed and day ends | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-SAINT-01 | Saint | healthy Saint is executed and dies | game end | good team loses | OFFICIAL, ASP, ENUM, ZDD, ORZ |
| TB-MAYOR-01 | Mayor | healthy Mayor selected by Imp at night; valid alternative death target | death branch | CHOICE: Mayor may die or another player may die; no-death branch requires a separate legal cause | OFFICIAL, ASP, ENUM, ZDD, ORZ |

Catalog size: **33 scenarios**.

## 3. Required assertions per scenario

Every executable fixture derived from the table must assert more than a single Boolean when applicable:

```text
actual-world legality
recipient knowledge boundary
beforeWorlds is SAT
afterWorlds expected SAT / UNSAT
legal output set or deterministic transition
registration facts bound to the output
malfunction state kept separate from registration
stable scenario serialization and ID
```

For `CHOICE` scenarios, each branch receives its own stable candidate ID. A test must fail if implementation first generates the displayed information and later rolls registration independently.

## 4. Perspective rules used by the matrix

1. A player knows their received token and private observations, not the storyteller's actual-role assignment.
2. The Drunk generally believes the shown Townsfolk token; the truth layer still records actual Drunk.
3. Poisoning is not automatically revealed to its target.
4. Red herring identity is storyteller-only and is applied only to Fortune Teller semantics.
5. Spy and Recluse registration is local to the queried interaction; it is not a permanent global role rewrite.
6. Public deaths, executions and nominations enter later snapshots at their actual timeline position.
7. Evil-only setup knowledge is included only for the evil player's perspective and only when player-count rules grant it.
8. Storyteller-only bluffs, actual roles and decision seeds must never leak into a good player's `PlayerWorldSet`.

## 5. Coverage map and implementation order

| Slice | Scenario IDs | First executable milestone |
|---|---|---|
| Setup and knowledge boundary | `TB-SETUP-01`–`05` | A2.1 contract; A3 implementation |
| First-night pair information | `TB-WW-01`–`02`, `TB-LIB-01`–`03`, `TB-INV-01`–`03`, `TB-SPY-01` | A2 then A3 |
| First-night numeric information | `TB-CHEF-01`–`02`, `TB-EMPATH-01` | A2 then A3 |
| FT and local registration | `TB-FT-01`–`03` | A2 then A3 |
| Malfunction boundary | `TB-MAL-01`–`02` | A2.1 contract; full timeline in B4 |
| Night/day transitions | `TB-EMPATH-02`, `TB-IMP-01`–`03`, `TB-SW-01`, `TB-UT-01`, `TB-RK-01`, `TB-VIRGIN-01`, `TB-SAINT-01`, `TB-MAYOR-01` | A2.1 contract; B4 implementation |
| High-impact registration | `TB-SLAYER-01` | A2.1 contract; B4 implementation |

## 6. Known limits at A0

- The catalog is a semantic specification, not yet an executable fixture set.
- Exact seat assignments and serialized inputs will be added with the A1 unified semantic model to avoid freezing an ad-hoc test schema now.
- External implementations may encode role text differently. Agreement does not override official rules; disagreement is recorded and investigated.
- This first catalog focuses on the interactions most relevant to player cognition. Butler voting legality, nomination-count minutiae, dead-vote consumption and all game-end permutations remain deterministic-rule coverage but are not required for the initial epistemic engine gate.
- Mechanical world counts are not player probabilities. A0 defines no probability or weighting assertions.

## 7. A0 exit checklist

- [x] More than 20 Trouble Brewing golden scenarios defined.
- [x] Setup, first-night information, malfunction, registration and multi-night transitions represented.
- [x] Each scenario has an official rationale category and planned formal validators.
- [x] Perspective boundary stated explicitly.
- [x] No external implementation is labelled an official source.
- [x] Exact versus future approximate computation boundary preserved.

## 8. A2.1 expansion and execution status

A2.1 adds 15 coverage-driven variants beyond the original 33:

```text
TB-FT-04 Spy red-herring prohibition
TB-FT-05 Recluse red-herring eligibility
TB-MAL-03 poison-target knowledge boundary
TB-MAL-04 Drunk malfunction/registration separation
TB-IMP-04 poisoned Imp self-kill without starpass
TB-SW-02 Scarlet Woman threshold negative branch
TB-UT-02 execution without death
TB-RK-02 poisoned Ravenkeeper output
TB-SLAYER-02 Slayer hits actual Demon
TB-VIRGIN-02 non-Townsfolk nomination
TB-MAYOR-02 illegal no-death redirect branch
TB-KNOW-01 Spy grimoire recipient/time
TB-KNOW-02 Spy grimoire non-leakage
TB-KNOW-03 red-herring non-leakage
TB-KNOW-04 general storyteller-secret non-leakage
```

All 48 are schema-v2 official contracts. Twenty are also executable against the frozen ASP adapter; 28 are explicitly `ORACLE_NOT_APPLICABLE` until a faithful timeline or recipient-projection adapter exists. The release baseline has 18 agreements, one documented Drunk coverage gap, one documented Spy red-herring Oracle variance, zero unexplained mismatches, and zero `NOT_RUN`.
