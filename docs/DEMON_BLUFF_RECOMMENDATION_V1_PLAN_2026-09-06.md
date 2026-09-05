# Demon Bluff Recommendation V1 — Setup-Time Recommendation Plan

> Status: **PLANNED — AFTER NIGHT STEP DECOMPOSITION / UI-R5, BEFORE EPI-MQ**  
> Date: 2026-09-06 Australia/Sydney  
> Scope: setup-time Demon bluff recommendation quality only

## 1. Purpose

Current Demon bluff recommendation quality is not acceptable as a long-term product algorithm. V1 should be extracted as an independent setup-time recommendation subsystem rather than remaining an incidental part of generic setup recommendation logic.

The core optimization unit is **the three-role bluff package**, not three independently high-ranked roles.

This phase is intentionally scheduled after the current large-file / Night Step ownership decomposition reaches a clean checkpoint, and before the cognitive-consistency / EPI-MQ campaign.

## 2. Architectural position

Target dependency direction:

```text
committed setup
+ legal bluff domain
+ evil-team composition
+ committed Drunk shown identity / setup facts
        ↓
Demon Bluff Recommendation V1
        ↓
ranked legal bluff triples
        ↓
existing setup / Host presentation flow
```

Later:

```text
Demon Bluff Recommendation V1
        + EPI-MQ / cognitive-consistency capabilities
        ↓
Demon Bluff Recommendation V2
```

V1 must not depend on historical observation replay, public claim tracking, full hypothetical-world reasoning, or later-night state.

## 3. Permanent ownership rule

Separate these concerns:

```text
legal bluff domain
!=
bluff quality ranking
```

Rules/semantic authority decides which good characters are legally valid bluffs. Recommendation may rank only within that complete legal domain.

Recommendation must not silently redefine legality merely because a role is strategically poor, overlaps a shown identity, is hard for a beginner to maintain, or has weak synergy.

## 4. V1 optimization target

Primary target:

> Maximize the practical usefulness of the three-role bluff package for an inexperienced evil team while preserving legality, setup coherence, diversity, fallback value, and reasonable Storyteller neutrality.

This is not equivalent to maximizing theoretical deception power for an expert player.

The default product bias should favor bluffs that a new Demon can actually execute and maintain.

## 5. Role-level BluffProfile

V1 should represent recommendation-relevant role properties through an explicit profile or equivalent typed scoring input rather than scattered role-name conditionals.

Candidate dimensions include:

- maintenance cost;
- verification risk;
- claim flexibility;
- fabrication difficulty;
- setup plausibility;
- social survival value;
- misinformation / narrative power;
- collapse cost if the bluff fails;
- evil-role synergy;
- Storyteller supportability;
- beginner complexity.

Exact weights are an implementation/calibration decision for the V1 phase, not frozen by this planning document.

## 6. Setup-aware scoring

A role's bluff value is setup-dependent.

V1 may consider committed setup facts such as:

- actual evil-team composition;
- Outsider structure;
- Drunk actual/shown identity;
- roles actually in play;
- other setup facts already lawfully available to the Storyteller recommendation subsystem.

Examples of useful setup-aware effects:

- Spy can increase the practical value of bluffs that require hidden role knowledge;
- Baron changes Outsider-count plausibility;
- a bluff that collides with a committed Drunk shown identity can remain legal while receiving a strong quality penalty;
- multiple bluffs sharing the same obvious weakness can be penalized at the triple level.

## 7. Triple-level enumeration and scoring

V1 should enumerate legal three-role combinations directly. Trouble Brewing's domain is small enough that exhaustive triple evaluation is preferable to greedy top-three selection.

Conceptual scoring shape:

```text
TripleScore =
      sum(IndividualBluffUtility)
    + RoleDiversity
    + NarrativeCoverage
    + TeamShareability
    + ContingencyValue
    + EvilRoleSynergy
    + SetupCoherence
    - VerificationRiskConcentration
    - FabricationBurdenConcentration
    - ClaimCollisionRisk
    - OutsiderCountTension
    - StorytellerOverfitPenalty
```

The exact formula and weights remain open until implementation.

## 8. Important triple-level properties

### 8.1 Diversity

Do not require a rigid passive/info/active template, but reward packages whose three roles do not all fail for the same reason.

### 8.2 Team shareability

Treat the three bluffs as resources for the evil team, not solely as three identities for the Demon personally.

### 8.3 Contingency value

A good package should remain usable if one bluff becomes unattractive or unavailable as a practical claim.

A useful conceptual metric is the worst remaining pair after any one bluff is discarded.

### 8.4 Beginner executability

A theoretically powerful bluff that requires sustained fabricated information, perfect recall, or expert rules knowledge should receive a substantial beginner-mode penalty.

### 8.5 Anti-overfitting

Do not make the recommendation feel like the Storyteller is using omniscient hidden information to engineer a perfect counter to one specific good player. Prefer broadly sustainable narrative value over brittle one-player targeting.

## 9. V1 non-goals

Do not include in V1:

- public claim history;
- day-by-day bluff adaptation;
- historical observation replay;
- full hypothetical-world enumeration;
- EPI-MQ productive-uncertainty scoring;
- dynamic replacement of already-issued bluffs;
- automatic player coaching text for how to fake each role;
- broad setup recommendation redesign unrelated to Demon bluffs;
- gameplay-rule changes.

## 10. V2 handoff boundary

After EPI-MQ / cognitive-consistency is mature, V2 may add:

- projected false-world sustainability;
- information-network conflict risk;
- cross-role verification pressure;
- expected world-convergence delay;
- richer Storyteller-supportability estimates;
- calibration against real play data.

V2 must consume cognitive-consistency capability rather than move cognitive-consistency authority inside the bluff recommender.

## 11. Recommended implementation sequence

```text
B1 audit current bluff ownership and legal-domain authority
-> B2 extract typed bluff recommendation boundary
-> B3 define role-level BluffProfile / setup-aware features
-> B4 exhaustive legal triple enumeration
-> B5 triple scoring + deterministic tie handling / bounded rotation
-> B6 integrate with existing setup recommendation output
-> B7 typed regression + quality characterization
-> B8 real-game calibration checkpoint
```

Do not start B1 until the active Night Step decomposition campaign and UI-R5 checkpoint are complete unless the roadmap is explicitly reprioritized.

## 12. Success criteria

V1 is complete when:

- legality and quality ranking are separate owners;
- recommendation evaluates whole bluff triples rather than greedily taking three role scores;
- setup-aware and evil-team-aware quality factors are explicit;
- beginner executability materially affects ranking;
- one bluff failure does not routinely collapse the whole recommended package;
- recommendation output remains compatible with MANUAL / Host presentation flows;
- no EPI-MQ dependency was pulled forward;
- durable typed tests protect the new recommendation seam;
- current legal bluff semantics remain unchanged.
