# GSP — Result-First Spy/Recluse Registration Ambiguity & Canonical History Contract

> Date: 2026-10-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **ACCEPTED DESIGN; GENERIC RESULT–WITNESS UI FIX ACCEPTED PR #254; DURABLE TYPED RULING CAPTURE/RECOVERY PENDING**  
> Scope: cross-cutting GSP-R1B/R1C history and registration/reveal contract. This document is a design authority; executable generic result/witness behavior was corrected in [PR #254](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md).  
> Context: [GSP-R1A capture audit](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md); [current roadmap](CURRENT_DEVELOPMENT_ROADMAP.md); [handoff](NEXT_DEVELOPMENT_HANDOFF.md).

## 1. Product decision: one observed result need not have one registration explanation

**The player's observed information is a canonical fact; legal registration witnesses are possible explanations; only a witness *explicitly adjudicated by the Storyteller* may be recorded as a canonical registration ruling.**

Trouble Brewing example: a functioning Empath's two living neighbours are a Spy and a Recluse. The Storyteller shows **1**. At least two legal witness interpretations can support that result:

| Witness | Spy registers as | Recluse registers as | Empath receives |
| --- | --- | --- | --- |
| A | Evil (actual alignment) | Good (actual alignment) | 1 |
| B | Good (Spy ability) | Evil (Recluse ability) | 1 |

The Storyteller may intentionally confirm the **result 1** without determining whether A or B was the operative explanation. **Do not infer that choosing 1 commits either witness.** The existence of multiple legal witnesses is not itself missing, erroneous or unreliable history.

Registration is **per relevant ability interaction**. A concrete ruling on this Empath interaction does not bind how Spy or Recluse registers for a later Chef, Fortune Teller, Investigator, Undertaker or other interaction. Conversely, it must be possible to record a specific adjudication when one really was made.

This principle applies to any result-first legal information choice with multiple witnesses (including Chef numbers, Fortune Teller yes/no and role-reveal information), not only Empath.

## 2. Three distinct canonical/derived layers

1. **Committed observation (canonical):** the actual player-facing outcome, e.g. `Empath` received typed `NumericResult(LIVING_EVIL_NEIGHBOURS, value=1)`, with source/recipient, game, phase/round, chronology, identity and reliability. It remains recorded even when no registration witness was chosen.
2. **Legal witness set (derived/hypothetical):** zero, one or multiple registration assignments that make a result legal for a *functioning* ability given the at-decision state. Keep these alternatives available for legality, explanation, future-world analysis and provider reasoning; do **not** commit a member merely because it appears first in enumeration.
3. **Explicit ruling (optional canonical fact):** a concrete `RegistrationFact`/interaction-local witness **only if Storyteller explicitly selected/confirmed it**. A missing explicit choice for a known ambiguous result is a first-class `UNRESOLVED_NOT_REQUIRED` state, not an unknown result and not an implied deterministic first witness.

Model distinctions that must survive provider projection and Recovery:

- `EXPLICIT_RULING`: Storyteller did choose a concrete witness; capture its identity, scope and provenance.
- `UNRESOLVED_NOT_REQUIRED`: Storyteller confirmed the displayed result but did not select a witness, and legality was established by one or more alternatives.
- `UNAVAILABLE_OR_UNRECORDED`: historical capture does not establish whether an explicit ruling occurred; do not retrospectively assert unresolved-by-choice.
- `NOT_APPLICABLE`: no special registration applies, or the result is legally unconstrained by witness because the ability was Drunk/Poisoned/otherwise malfunctioning. Preserve actual impairment/reliability semantics separately; never invent a Spy/Recluse witness merely to explain an unreliable number.

The exact type/enum names above are **proposed contract semantics, not already implemented APIs**. Avoid encoding the absence of an explicit ruling as `false` for either Spy or Recluse, or as an automatically selected witness.

A minimal illustrative projection (not a finalized serialization schema):

```json
{
  "observation": {
    "sourceRole": "Empath",
    "metric": "LIVING_EVIL_NEIGHBOURS",
    "shownValue": 1
  },
  "registrationResolution": "UNRESOLVED_NOT_REQUIRED",
  "selectedWitness": null,
  "legalWitnessCount": 2
}
```

If witnesses are derived later, validate using the **frozen at-interaction game/ability state**; do not recompute from a mutated current roster/poison/death state and present the result as historical fact. A count alone is insufficient for exact replay provenance.

## 3. Actual code observation / why this needs fixing

**Historical root-cause evidence** on `main@3d3843a82a3e1cabc92499e1f4d0d9b14c30b1ac` (pre-fix; see [accepted code fix](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md) for current production semantics):

- `ClocktowerRegistrationResultDomain.kt`: `clocktowerAlignmentRegistrationWitnesses` enumerates Spy/Recluse variants; `distinctClocktowerFinalInformationResults` deduplicates by visible result and **retains the first witness**; its comment explicitly says the retained first witness can be committed by the registration callback.
- `ClocktowerRegistrationResultPresentation.kt`: a numeric visible option contains `spyRegistersGood` and `recluseRegistersEvil` from a selected enumeration witness, even when other witnesses produce the same number.
- `ClocktowerRegistrationInteractionState.kt`: `applyRecommendedWitness` writes that witness into UI-local Spy/Recluse booleans; those are subsequently read by `ClocktowerHostScreen.kt` `recordSpyRegistration` / `recordRecluseRegistration`.
- `ClocktowerNightStepUi.kt` applies the selected option before publication, and `ClocktowerHostScreen.kt` records registrations when advancing/confirming steps. Current registration record events are localized `RoleAction` UI events, **not** an accepted durable typed canonical registration log (per R1A).
- `ClocktowerEmpathSquareTableUi.kt` / `ClocktowerChefSquareTableUi.kt` highlight a particular contributing registration witness in the diagram. An arbitrarily retained witness can visually imply certainty that was never chosen.
- `TroubleBrewingTopologyObservationWitnessEvaluator.kt` already reasons over **sets of legal registration witnesses**, so uncertainty does not require a rule-engine rewrite.

**Resolved in generic production result/display flow by PR #254:** “result-first” deduplication now keeps all derived candidate witnesses separately and does not apply/record the first one. Do not reconstruct a committed historical ruling from legacy localized `ClocktowerEvent` text; a canonical durable typed registration producer is still pending.

## 4. Required behavior and ownership

### 4.1 Game Engine / rules domain

- Produce/check the complete legal set of result/witness combinations under the **correct interaction-local state** and ability reliability.
- The legality predicate for a displayed result is `exists(witness): legal(result, witness, state)`; result selection alone is not witness selection.
- Distinguish a uniquely compelled ruling from a choice that never had to be materialized. Do not extrapolate registration across unrelated ability interactions.
- Never change actual Spy/Recluse alignment, identity or underlying game truth because of how they register to one ability.

### 4.2 Host / interaction confirmation

- Storyteller selects/commits the **player-facing result first**.
- If multiple witnesses support it, leave the witness unresolved by default; optionally allow **explicit** witness selection in an advanced UI.
- Do not invoke witness-application or registration-recording side effects from dedup choice unless an explicit witness really was confirmed.
- Confirmation must be idempotent; no late automatic first-witness assignment on night-step advance, replay or Recovery restore.
- Role-specific cases that genuinely demand a concrete registration choice must be identified by the rules owner and allowed to request one explicitly; this example does not prove that all registration choices may always be omitted.

### 4.3 Canonical history / Recovery / provider (R1B and follow-on)

- R1B immutable neutral provider prefix **must not promote UI-local registration state, candidate witness, or the first dedup entry to a historical ruling**. Report registration producer coverage as partial/unknown unless new typed commit evidence is present.
- When the registration producer is implemented, write typed separate `InformationObserved` and optional `ExplicitRegistrationRuling` records, linked by stable game/interaction identity and global chronological cutoff, with explicit completeness/provenance.
- Recovery must preserve displayed-result fact and any actual explicit ruling without collapsing `UNRESOLVED_NOT_REQUIRED` into `UNAVAILABLE_OR_UNRECORDED` and without generating a fictional selected witness on restore.
- The Provider can see the actual result and ambiguity/known witnesses as context and may explain alternatives, but **cannot assume a specific Spy/Recluse registration was committed or commit one itself**.
- Do not reconstruct a historical legal witness set from the later mutable state; respect R1A's exact captured exclusive history cutoff and frozen context requirements.

### 4.4 Player-facing / Storyteller UI

- Show the result once (e.g. **Empath: 1**) without forcing an explanatory neighbour.
- Optionally indicate “two legal registration explanations; no specific ruling recorded” to the Storyteller, not to the player.
- Advanced optional controls may allow “keep unresolved” (default) or “specify A/B”, but only show alternatives that the rules owner has proved legal for that interaction.
- If unresolved, Chef/Empath graphical markings must not put an exclusive contribution star on one specific Spy/Recluse seat. Display uncertainty or highlight the candidate region neutrally.
- A Drunk/Poisoned arbitrary result must not be cosmetically labeled “truthful due to registration” just because an unrelated witness happens to reproduce it.

## 5. Implementation sequence and acceptance

**Accepted executable checkpoint:** PR #254 implements the shared result-witness separation, explicit-only manual registration recording, contradiction rejection and conservative Chef/Empath visuals. **Still pending:** typed explicit ruling producer, semantic Recovery, optional full advanced adjudication UI. See [acceptance](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md).

- **GSP-R1B (immediate):** honor the negative invariant in neutral history prefix: observations are typed facts, potential witnesses are not canonical rulings; report registration coverage/unknown honestly. R1B's bounded **read-only** materializer is not a license to refactor UI recording or add speculative registration truth.
- **Generic UI result–witness correction:** COMPLETE / ACCEPTED via PR #254, including the previous implicit first-witness effect and ambiguous square hints. **Follow-on dedicated typed registration producer / Recovery / optional explicit-ruling annotation** remains pending; do not confuse existing localized RoleAction UI history with canonical adjudication.
- **GSP-R1C:** end-to-end fresh/recovered prefix equivalence and historical replay semantics, including case where two distinct witnesses give the same result. Do not claim complete registration capture until producer/Recovery tests prove it.

Mandatory regression cases for executable changes:

1. Functioning Empath with Spy/Recluse neighbours: A and B both yield **1**; selecting 1 stores **only result**, with no selected witness and no invented seat highlight.
2. Same setup with explicit A selected: records precisely A, no implied registration for later ability interactions; a subsequent distinct interaction may choose B.
3. Different witnesses giving the same Chef numeric or Fortune Teller boolean result dedupe the **visible option**, but preserve the unresolved alternatives internally.
4. Drunk/Poisoned Empath receiving an arbitrary numeric result: records what was shown with impairment semantics, **without fabricated registration witness**.
5. Fresh save/restore and repeated confirmation: result, explicit/unresolved status and interaction identity remain stable; no automatic first-witness assignment.
6. Provider projection with **old historical data**: absence of a typed ruling is `UNAVAILABLE_OR_UNRECORDED` (unless positively known unresolved), not an explicit “none” and not a guessed first witness.
7. Historical replay from frozen prefix does not use later registration judgments, poison state or correction to rewrite earlier choice interpretation.

For implementation, consult the code paths in section 3, the [R1A coverage audit](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md), root `AGENTS.md`, and `docs/TESTING_STRATEGY.md`. Use focused behavioral RED/GREEN where a new stable invariant is introduced, then `:app:testFast` and CI/R2 at the appropriate acceptance checkpoint.

## 6. Non-goals / authority boundary

- Do **not** require every displayed information result to have a uniquely persisted causal witness.
- Do **not** make registration a permanent Spy/Recluse property or propagate one interaction ruling across the whole game.
- Do **not** change game rules, force a witness through a UI shortcut, infer semantic truth from localized History text, or train/evaluate LLMs against fabricated explanation labels.
- Do **not** add network/LLM production authority before GSP-3A benchmark or weaken manual/offline operation.
