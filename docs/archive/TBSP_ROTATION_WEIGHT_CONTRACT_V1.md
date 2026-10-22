# TBSP Rotation Weight Contract v1

> Campaign: TBSP — Trouble Brewing Setup Preset Integration  
> Status: **NORMATIVE / APPROVED**  
> Approved by product owner: 2026-08-30 Australia/Sydney  
> Applies to: TBSP-2 history-aware preset selection  
> Parent handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md`

## 1. Purpose

This document freezes the numeric and deterministic selection contract that the parent TBSP handoff intentionally left OPEN. Implementation may now proceed with TBSP-2 history-aware selector GREEN work, but must preserve the hard/soft boundary defined here.

## 2. Hard eligibility rules remain authoritative

The following are hard filters, not weights:

```text
exact previous real non-Demon composition -> reject
last-game overlap threshold               -> reject above threshold
```

`exact repeat` is evaluated by real in-play non-Demon role composition, not by preset ID.

Fallback may relax only the last-game overlap threshold. It must never re-enable an exact-repeat composition.

## 3. Five-game history weights

Use the dataset-defined history weights in recency order:

```text
most recent      1.00
age 1            0.65
age 2            0.40
age 3            0.20
age 4            0.10
```

For each candidate:

```text
weightedOverlap =
    Σ(overlap_i × historyWeight_i)
    / Σ(historyWeight_i for available history entries)

baseNoveltyWeight = max(0.20, 1.0 - weightedOverlap)
```

Only available history entries participate in the denominator.

## 4. Approved soft penalties

Soft penalties are multiplicative and never make an otherwise eligible candidate illegal.

```text
same immediately-previous Minion set     × 0.70
primary style seen >= 2 of previous 5    × 0.88
same consecutive Drunk shown role        × 0.40
```

`primary style` means `preset.styleTags.firstOrNull()` after preserving the preset's declared tag order.

A style penalty applies when the candidate's non-null primary style appears in at least two of the previous five recorded games.

The Drunk penalty applies only when the candidate contains Drunk, a shown role has been selected for the candidate, the immediately previous game also recorded a Drunk shown role, and those shown-role IDs are equal.

## 5. Final candidate weight

```text
rawWeight =
    baseNoveltyWeight
    × all applicable soft-penalty multipliers

finalWeight = max(0.05, rawWeight)
```

The `0.05` floor guarantees that soft penalties remain soft.

## 6. Fallback contract

Start with the player-count-specific last-game maximum-overlap threshold from the dataset.

If no candidates remain after hard filtering:

```text
threshold += 0.05
re-evaluate
repeat until the first non-empty eligible candidate set is found
```

Selection must occur from the first non-empty relaxation level. Exact-repeat rejection remains active at every level.

## 7. Deterministic weighted selection

Before weighting or drawing, canonicalize eligible candidates by ascending `preset.id`.

The weighted draw must be deterministic for:

```text
same dataset
+ same player count
+ same recent rotation history
+ same game seed
= same selected preset and Drunk shown role
```

Implementation must not depend on input collection iteration order.

Use an independent selector seed namespace derived from the game seed. Avoid nondeterminism from floating-point iteration order: convert final candidate weights to a fixed-point integer representation before cumulative weighted drawing.

Existing deterministic namespaces remain independent:

```text
tb-preset-v1
tb-drunk-v1
```

A later seat materialization slice will use its own namespace.

## 8. Non-goals

This contract does not authorize:

- App/runtime wiring;
- persistence;
- seat assignment;
- A3 immutable setup snapshot work;
- changing Trouble Brewing legality rules;
- changing No Greater Joy setup behavior.
