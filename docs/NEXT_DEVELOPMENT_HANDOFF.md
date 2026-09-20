# NEXT DEVELOPMENT HANDOFF — SDE-2D5F v2 human review

> Updated: 2026-09-20 Australia/Sydney  
> This is the **only active handoff**.

## 1. Read first

Use this order:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`
5. `docs/SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`
6. this handoff

Only read archived D5F precursor audits if historical reasoning is needed.

## 2. Live branch

Branch:

`sde-2d5-calibration-policy-evidence`

PR #150 remains **draft**.

**Do not merge unless the user explicitly says “授权合并”.**

Always query live branch / PR / checks before editing.

## 3. Current state

~~~text
D5A–D5E                         COMPLETE
D5F-A                           COMPLETE
D5F-B manifest infrastructure  COMPLETE
D5F-B3 policy-model correction COMPLETE
D5F-B v2 human review          NEXT
D5F-C                           BLOCKED
sealed holdout                  CLOSED
SDE-3                           BLOCKED
~~~

Accepted B3 functional head:

`74d8bb4cc07c5425bcb61fa821bc2287620f379f`

Focused B3 calibration:

`35494283217 — SUCCESS`

## 4. Canonical manifest

Use only:

`app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`

Version:

`d5f-b-calibration-v2`

It contains 11 records and all are currently `UNREVIEWED`.

Do **not** continue labeling:

`app/src/test/resources/review/sde-2d5f-human-label-manifest-v1-obsolete.tsv`

Do not transfer old/provisional judgments.

## 5. v2 record set

~~~text
d5f:bluff:13af34bc8befea2f:r1
d5f:bluff:334926f04bd2c687:r1
d5f:bluff:fe702b4aac3ca49a:r1
d5f:confirmation:sig-3a97786fbe44b310
d5f:confirmation:sig-c8f4a0424526b659
d5f:drunk:baron-6-drunk-empath-seat-2
d5f:role-info:sig-00002562710d4654:marginal-1
d5f:role-info:sig-44f30b2b21cd7682:marginal-0
d5f:role-info:sig-796b6039ad4bb4aa:marginal-0
d5f:role-info:sig-89a6d878c5516337:marginal-1
d5f:role-info:sig-aab5a4dfa48cfce0:marginal-2
~~~

## 6. Review labels

Allowed labels:

~~~text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
~~~

Use explicit reasons. Relevant B3 vocabulary includes:

- `EXCESSIVE_CONFIRMATION_CHAIN`
- `INSUFFICIENT_HEALTHY_INFORMATION`
- `IMPAIRED_CLUE_TOO_REVEALING`
- `IMPAIRED_CLUE_COHERENT`
- `BLUFF_EXECUTION_BURDEN`
- `BLUFF_NARRATIVE_REDUNDANCY`
- `BLUFF_COHERENCE_FRAGILE`
- `BLUFF_ROUTES_USABLE`
- `CROSS_CHANNEL_NARRATIVE_COHERENCE`
- `OTHER_EXPLICIT_REVIEW_REASON`

Do not invent a score to replace the human judgment.

## 7. How to interpret each evidence family

### Drunk

Compare one same-setup surface:

- truthful;
- mild false;
- stronger false when present.

Each candidate retains HealthyCore / FullBundle / DrunkMarginal / normalized diagnostics.

`counterfactualHealthyTruthDanger` is contextual evidence only.

Truth/false relation is not itself a quality ordering.

### Demon bluff

Consider jointly:

- per-role support;
- individual-support floor;
- shared / union support;
- pairwise coverage;
- distinct strategic patterns;
- execution burden;
- claim burden / cadence;
- narrative route class/diversity;
- external-human observed triplet.

Do not treat higher/lower shared-to-union as automatically better.

### Confirmation chain

Distinguish:

- one strong healthy clue, which can be normal;
- several individually reasonable clues that jointly collapse Demon cover / topology.

Use full-bundle + leave-one-out restoration evidence. Do not invent a global confirmation score.

### Role information

Respect owner/control metadata.

Current real records include:

- Chef / Empath → rule-determined, diagnostic-only;
- Washerwoman → Storyteller-controlled.

General architecture also allows Storyteller-controlled Librarian / Investigator outputs where legal.

## 8. Frozen lifecycle ownership

- Drunk shown role: persistent after setup commit.
- Drunk unshown clue: SDE-planned until shown.
- Demon bluff triplet: SDE planning output before reveal; persistent input after reveal.
- Chef / Empath healthy clue: mechanics-determined.
- Washerwoman / Librarian / Investigator legal output: Storyteller-controlled.
- Fortune Teller pair: player-controlled.
- Red Herring: setup-controlled before persistence.
- Poisoner target: Evil-player-owned.

Optimize only current-stage SDE-owned controllable variables.

## 9. Compatibility code intentionally retained

Do not delete yet:

- approximate 90/10 impaired-information compatibility bridge;
- legacy `MalfunctionPolicy`.

Do not restore old Drunk shown-role recommendation/scoring abstractions.

## 10. NEXT action

Begin the v2 human review **one record at a time**.

For each record:

1. inspect the rendered evidence;
2. get the user's explicit judgment;
3. record the label and reason;
4. preserve uncertainty when the evidence does not justify a strong label.

After all 11 are settled:

- validate manifest completeness;
- only then plan D5F-C.

Do not open sealed holdout evidence, freeze thresholds, cut production policy, or begin SDE-3.
