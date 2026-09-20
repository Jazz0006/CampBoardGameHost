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
D5F-B v2 human review          IN PROGRESS — 8/11 COMPLETE
D5F-C                           BLOCKED
sealed holdout                  CLOSED
SDE-3                           BLOCKED
~~~

Accepted B3 functional head:

`74d8bb4cc07c5425bcb61fa821bc2287620f379f`

Documentation/archive consolidation baseline:

`77fe159d06f3e4c57144f8621cf6d50e46742b21`

Always query live branch because documentation-only commits may follow this baseline.

Focused B3 calibration:

`35494283217 — SUCCESS`

## 4. Canonical manifest

Use only:

`app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`

Version:

`d5f-b-calibration-v2`

It contains 11 records. Human review is now **8/11 complete**; exactly 3 records remain `UNREVIEWED`.

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

## 10. Human review progress

Completed labels:

~~~text
d5f:bluff:13af34bc8befea2f:r1                 BAD_TOO_WEAK
d5f:bluff:334926f04bd2c687:r1                 ACCEPTABLE
d5f:bluff:fe702b4aac3ca49a:r1                 ACCEPTABLE
d5f:confirmation:sig-3a97786fbe44b310         BAD_TOO_STRONG
d5f:confirmation:sig-c8f4a0424526b659         BAD_TOO_STRONG
d5f:drunk:baron-6-drunk-empath-seat-2         BAD_TOO_STRONG
d5f:role-info:sig-00002562710d4654:marginal-1 ACCEPTABLE
d5f:role-info:sig-44f30b2b21cd7682:marginal-0 BAD_TOO_STRONG
~~~

Remaining records, in order:

~~~text
d5f:role-info:sig-796b6039ad4bb4aa:marginal-0
d5f:role-info:sig-89a6d878c5516337:marginal-1
d5f:role-info:sig-aab5a4dfa48cfce0:marginal-2
~~~

Important human-review findings that must survive into D5F-C:

- **Player count matters for impaired information.** In the reviewed 6-player Drunk-as-Empath case, truthful `0` was judged too revealing even though normalized strategic topology did not shrink. Small games may need a stronger bias toward coherent misinformation than larger games.
- **Multi-night impaired roles require temporal coherence.** A Drunk Empath should not be optimized as independent nightly numbers. Human Storyteller reasoning plans a misleading trajectory across nights: e.g. preserve a coherent `0` story until neighbor composition changes, then use later values to maintain the false world.
- **Topology-neutral does not mean harmless.** A Washerwoman clue can leave the strategic topology count unchanged yet still be too strong because cross-channel evidence quickly confirms the real good role, making the Demon decoy practically useless.
- **Confirmation-chain judgment is contextual.** Healthy Chef=1 was acceptable alone; the same information can become too strong when reinforced by Empath / pair-information channels.

## 11. Review-surface repair during human review

Human review exposed that role-information records exported only source role/seat plus numerical diagnostics, without the actual clue proposition. That was not sufficient for responsible human labeling.

Fixes:

- `b42862d866d7458e1989e963b42f54bd0d203e8f` — role-information calibration evidence now carries its canonical observation key; renderer exposes it; tests cover the payload.
- `58bdaa2d16b63c1d4378b9156b0b838c8165bce0` — the SDE-2D5 calibration workflow now prints/uploads the D5F review + manifest artifacts instead of discarding them.
- `4343669679d9232c08e211a2b5a6638506085068` — fixed an obsolete renderer fixture assertion exposed by FULL CI.

These are review/evidence changes only; they do not change production recommendation policy.

## 12. NEXT action

Continue the v2 human review **one record at a time**, starting with:

`d5f:role-info:sig-796b6039ad4bb4aa:marginal-0`

Before asking for the label, show the user the actual human-readable clue proposition from the repaired review evidence, not only strategic/raw counts.

For each remaining record:

1. inspect the rendered evidence including the actual clue;
2. explain what the clue means at the table and whether it is SDE-controlled or rule-determined;
3. get the user's explicit judgment;
4. record the label and explicit reason;
5. preserve uncertainty when the evidence does not justify a strong label.

After all 11 are settled:

- validate manifest completeness;
- explicitly reconcile the new human findings above with D5F-C gate/band design;
- only then plan D5F-C.

Do not open sealed holdout evidence, freeze thresholds, cut production policy, or begin SDE-3.
