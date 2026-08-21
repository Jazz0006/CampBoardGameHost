# R6 / P1.2 Knowledge Timeline Semantics — 2026-08-21

## Baseline

- R6.3 observation timeline migration contract merged as `9575a0b9fa5a8f391e9a513fe58e5cd2e23c7486`.
- Durable observations now distinguish `LegacyLocal` from `Global(TimelinePoint)` and Global logs canonicalize by `globalSequence`.
- Production Host is still intentionally not wired to the allocator/global observation path.

## Audit finding

`A4PlayerKnowledgeFactory` was re-sorting bound observations by:

```text
round -> local sequence -> observationId
```

That could discard the chronology established by a Global observation log before the observations entered a `PlayerKnowledgeSnapshot`.

The exact A3 `EnumeratedWorldSet` and A4 ZDD shadow also currently replay knowledge observations in that legacy order. However their current observation evaluator is time-insensitive: it does not read observation `phase`, `round`, local `sequence`, `timelineBinding`, or `globalSequence` when deciding whether a world matches. Current filtering therefore remains a conjunction over the same static/current-world predicates, so replay order does not presently change the surviving exact world set.

## R6.4 contract

This slice establishes a shared knowledge-input chronology rule:

- a direct observation collection must be entirely LegacyLocal or entirely Global;
- mixed migration modes fail closed;
- Global collections require unique `globalSequence` values;
- LegacyLocal collections retain the compatibility order `round -> sequence -> observationId`;
- Global collections canonicalize by `globalSequence -> observationId`;
- `A4PlayerKnowledgeFactory` validates/canonicalizes the whole collection before splitting public/private observations.

This ensures a `PlayerKnowledgeSnapshot` produced by the factory no longer silently reverts a Global history to legacy round/local ordering.

## PlayerWorldSetIdentity decision for the current evaluator

`globalSequence` does **not** enter `PlayerWorldSetIdentity` in this slice.

That is deliberate rather than an omission. The current world evaluator does not use timeline position in its filtering semantics. Adding `globalSequence` to the cache/world-set identity now would create a misleading contract in which the identity appears time-sensitive while the world construction is not.

The distinction is:

```text
knowledgeSnapshotId
  may change when the durable Global timeline identity changes

PlayerWorldSetIdentity
  remains unchanged when only globalSequence changes
  while the evaluator remains time-insensitive
```

Existing identity inputs such as phase/round remain unchanged in this slice; R6.4 does not broaden or shrink their historical contract.

## Required follow-up before time-aware multi-night world reasoning

Before any world-builder starts interpreting an observation against historical state rather than the current/static world, all world-set replay consumers must use the shared Global chronology rather than their local `round -> sequence` sort. Known consumers include:

- `EnumeratedWorldSet.fromWorlds(...)`;
- `ZddPlayerWorldSet.enumerateDirectMeasured(...)`.

At that point the identity contract must be revisited tests-first. If changing `globalSequence` can change the actual world constraints, the semantic timeline position must also participate in the world-set identity/cache key. Do not add it earlier merely as defensive cache invalidation.

## Deliberate exclusions

This slice does not:

- connect production Host/Compose to the timeline allocator;
- change observation persistence schema again;
- migrate `ActionFact` timeline identity;
- make A3/ZDD world evaluation historically time-aware;
- add `globalSequence` to `PlayerWorldSetIdentity`;
- address P1.3 actual-truth vs knowledge-safe world-builder input;
- address P1.1 Spy reminder-token truth boundaries.

P1.2 therefore remains open for the remaining action/world-builder timeline work; this document records the knowledge-layer semantics that later work must preserve.
