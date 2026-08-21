# R6 Drunk / Poison Ability Correctness Hotfix Handoff

**Date:** 2026-08-22  
**Repository:** `Jazz0006/CampBoardGameHost`  
**Target:** tests-first correctness hotfix for Blood on the Clocktower Drunk / Poisoned ability semantics

---

## 1. Current repository state

This handoff was originally prepared from the post-PR-#23 `main` baseline. Immediately before this integration-policy update:

- `main`: `aab271be7dde9a659e6041abee59c50a558e61c9`
  - contains this handoff document as a docs-only commit;
- PR #24: **Draft / open**, `codex/r6-semantic-history-foundation`
  - base: `main`
  - head: `cdd3d7d300379c4e4a31ee000453a168188d1537`
  - contains production semantic-history / persistence work and does modify `CampBoardGameHostApp.kt`, but its App changes are concentrated around semantic-history state, save, restore, and reset;
- PR #27: **Draft / open**, stacked on #24, `codex/r6-global-observation-cutover`
  - base: `codex/r6-semantic-history-foundation`
  - head: `c4324c1e1e2e568c68171177d36c4cf664322895`
  - current changed files are limited to global observation/session contracts, tests, and handoff documentation; production `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt` wiring is still intentionally not implemented.

Always query the live `main`, PR #24, and PR #27 heads before implementation because these values may advance after this document update.

PR #24 and #27 are ongoing R6 semantic-history / global-observation work. This correctness hotfix must **not** be implemented inside either stacked PR.

Create a new hotfix branch from the then-current `main`.

Recommended branch name:

`codex/clocktower-drunk-poison-correctness-hotfix`

This hotfix has **merge priority over unfinished expansionary R6 work** because it fixes confirmed production gameplay correctness defects found in real-game field testing.

---

## 2. Why this hotfix exists

Real-game field testing found a hard rules defect:

> A player whose **actual role is Drunk** but whose **shown role is Monk** can choose a protection target, and the current production resolution may treat that target as genuinely protected.

That is incorrect.

The broader audit found that this is not only a Monk-specific issue. The project already models `actualRole` separately from `shownRole`, but production behavior is not yet consistently separated into:

1. **perceived / interaction role** — what the player believes they are and what interaction the Host must simulate; and
2. **ability functioning state** — whether the underlying ability can actually affect game state or produce reliable information.

The fix should establish and enforce that semantic distinction rather than patching one role at a time.

---

## 3. Authoritative gameplay semantics

For a **Drunk** player:

- the player believes they are their `shownRole`;
- the Host should generally run the interaction for that shown role when appropriate;
- the player may wake, choose targets, make decisions, or receive information as though the shown role were real;
- however the player's actual character ability does **not function**;
- information produced for the Drunk may therefore be unreliable / arbitrary according to storyteller policy;
- mechanical effects must not actually affect the game.

For a **Poisoned** real character:

- the character still behaves as though their ability is functioning;
- wake/choice/interaction normally still occurs;
- the ability is not functioning while poisoned;
- mechanical effects do not apply;
- information may be unreliable / false according to storyteller policy.

The implementation must preserve official rules semantics even if a different behavior would appear to improve balance.

---

## 4. Existing architecture that should be preserved

Current state already has the right foundational distinction:

- `PlayerCard.clocktowerRole` = actual role;
- `PlayerCard.clocktowerShownRole` = perceived/shown role;
- domain `PlayerState` carries `actualRole` and `shownRole` separately;
- production night flow intentionally includes a Drunk's `shownRole` in waking-role projection;
- information flow already has explicit reliability concepts such as `InformationReliability.DRUNK` and `InformationReliability.POISONED`.

Relevant current production helpers include:

- `actualClocktowerRoleCards(...)`
- `roleActor(enName)`
- `actorIsPoisoned(...)`
- `actorIsUnreliable(enName, actor)`
- `infoStep(...)`

`roleActor(enName)` currently intentionally treats:

`actualRole == Drunk && shownRole == enName`

as an actor for the simulated interaction. That is desirable for interaction fidelity and should not be removed.

The bug is that interaction eligibility and ability effect are not consistently separated later in resolution.

---

## 5. Confirmed defects

### 5.1 Monk — confirmed hard bug

Current production behavior allows a Drunk shown as Monk to wake and select a target. That interaction is correct.

The problem is downstream resolution: protection is effectively inferred from the committed Monk target rather than from a functioning actual Monk ability.

Current affected concept:

`clocktowerConfirmedMonkProtectedTarget`

The current resolution path has logic equivalent to:

```kotlin
val protectedByMonk = clocktowerConfirmedMonkProtectedTarget == deathName
```

without first proving that the acting Monk is a functioning real Monk.

Expected behavior:

- real healthy Monk -> wakes, selects, protection functions;
- real poisoned Monk -> wakes, selects, protection does **not** function;
- Drunk shown as Monk -> wakes, selects, protection does **not** function.

Important: do **not** fix this by suppressing the fake Monk interaction. The fake Monk still needs to believe the ability was used normally.

---

### 5.2 Ravenkeeper — confirmed sibling defect

Current night-death trigger is based on actual role, effectively:

```kotlin
nightDeathWillOccur && card.clocktowerRole?.enName == "Ravenkeeper"
```

That correctly triggers a real Ravenkeeper but fails to simulate the shown role for a Drunk who believes they are the Ravenkeeper.

Expected behavior:

- real healthy Ravenkeeper dies at night -> wakes, chooses a player, receives functioning/reliable information;
- real poisoned Ravenkeeper dies at night -> still wakes and chooses, but information is unreliable;
- Drunk shown as Ravenkeeper dies at night -> should receive the simulated Ravenkeeper death interaction and unreliable information.

This is the opposite side of the Monk problem: Monk currently simulates the interaction but incorrectly grants the mechanical effect; Ravenkeeper currently fails to simulate the conditional interaction at all for the Drunk shown role.

---

### 5.3 Sage — confirmed same trigger-class problem in scripts containing Sage

Current Sage trigger is similarly based on actual role, effectively:

```kotlin
nightDeathWillOccur && card.clocktowerRole?.enName == "Sage"
```

Expected behavior:

- real healthy Sage killed by Demon -> trigger and reliable Sage information;
- real poisoned Sage killed by Demon -> trigger still occurs, information unreliable;
- Drunk shown as Sage, when the corresponding apparent trigger condition occurs -> simulate the Sage interaction and provide unreliable information.

The hotfix should treat this as part of the same semantic family rather than a separate one-off patch.

---

## 6. Roles already audited as apparently correct

These code-inspection results are useful evidence, but **must not be treated as sufficient proof**. Add regression tests for the mechanically important cases.

### Information-oriented roles

Current production paths appear to correctly treat Drunk shown roles as interaction actors and mark the resulting information unreliable:

- Washerwoman
- Librarian
- Investigator
- Chef
- Empath
- Fortune Teller
- Undertaker
- Clockmaker
- Chambermaid
- Artist

In particular, the generic information path already has logic equivalent to:

```kotlin
val actorIsDrunkShownRole =
    actor?.clocktowerRole?.enName == "Drunk" &&
    actor.clocktowerShownRole?.enName == enName

val informationReliability = when {
    actorIsPoisoned(actor) -> InformationReliability.POISONED
    actorIsDrunkShownRole -> InformationReliability.DRUNK
    else -> InformationReliability.RELIABLE
}
```

This general pattern should be preserved.

### Mechanical / passive roles

Current inspection suggests the following are already keyed to actual role / actual functioning rather than shown identity:

- Soldier
- Mayor
- Virgin
- Slayer

Examples of desirable current behavior:

- Drunk shown as Soldier can still die to the Demon;
- Drunk shown as Mayor does not gain the Mayor three-alive win condition;
- Drunk shown as Virgin does not execute the nominator;
- Drunk shown as Slayer may publicly claim/use the action, but the shot cannot gain real Slayer mechanical effect.

Again, add regression coverage rather than relying only on inspection.

---

## 7. Architectural requirement for the fix

Do **not** solve this with scattered checks such as:

```kotlin
if (card.clocktowerRole?.enName == "Drunk") { ... }
```

inside every character implementation.

The desired semantic model is:

### A. Perceived / interaction role

Determines questions such as:

- Should this player be represented as this character?
- Should the Host wake them?
- Should they choose a target?
- Should a conditional shown-role interaction be simulated?
- What does the player believe happened?

Conceptually:

```text
perceivedRole = shownRole when actual role is Drunk
otherwise perceivedRole = actualRole
```

### B. Ability functioning state

Determines whether the character ability can actually alter canonical game state or produce constrained truthful information.

Use one coherent concept, for example:

```text
FUNCTIONING
DRUNK
POISONED
```

Naming may differ if an equivalent existing abstraction should be reused.

Conceptually:

```text
FUNCTIONING -> mechanical effects allowed; reliable information rules apply
DRUNK      -> simulate perceived role; no real mechanical effect; info unreliable
POISONED   -> simulate real role; no real mechanical effect; info unreliable
```

Avoid creating a second independent source of role truth. `actualRole`, `shownRole`, poison state, and canonical game state must remain authoritative.

---

## 8. Tests-first implementation requirements

Add failing tests before production behavior changes.

At minimum cover the following contracts.

### 8.1 Monk

1. healthy real Monk selects a target -> target survives Demon attack because of Monk protection;
2. poisoned real Monk selects a target -> target is **not** protected by Monk;
3. Drunk shown as Monk selects a target -> target is **not** protected by Monk;
4. fake/poisoned Monk interaction still occurs so the player does not learn that their ability is malfunctioning;
5. persistence/restore must not accidentally convert a fake or poisoned Monk's committed selection into real protection.

### 8.2 Ravenkeeper

1. healthy real Ravenkeeper dies at night -> conditional interaction occurs;
2. poisoned real Ravenkeeper dies at night -> conditional interaction still occurs, information reliability is poisoned/unreliable;
3. Drunk shown as Ravenkeeper dies at night -> simulated conditional interaction occurs, information reliability is drunk/unreliable;
4. a player who is neither actual nor perceived Ravenkeeper must not receive the interaction.

### 8.3 Sage

1. healthy real Sage with legal Demon-death trigger -> interaction occurs with reliable information;
2. poisoned real Sage -> interaction occurs but information is unreliable;
3. Drunk shown as Sage -> corresponding apparent trigger interaction is simulated and information is unreliable.

### 8.4 Soldier

Regression tests:

1. healthy real Soldier survives Demon attack;
2. poisoned real Soldier can die to Demon attack;
3. Drunk shown as Soldier can die to Demon attack.

### 8.5 Slayer

Regression tests:

1. healthy unused real Slayer can kill a valid Demon target;
2. poisoned real Slayer cannot produce the kill effect;
3. Drunk shown as Slayer cannot produce the kill effect;
4. fake/poisoned usage still behaves convincingly from the player's point of view.

### 8.6 Virgin

Regression tests:

1. healthy real Virgin first nominated by qualifying Townsfolk -> ability works;
2. poisoned real Virgin -> ability does not execute nominator;
3. Drunk shown as Virgin -> ability does not execute nominator.

### 8.7 Mayor

Regression tests:

1. Drunk shown as Mayor receives neither real death-redirection effect nor Mayor three-alive win condition;
2. poisoned real Mayor does not receive a functioning ability while poisoned;
3. healthy real Mayor behavior remains unchanged.

### 8.8 Information roles

Add at least one representative generic test proving that the shared information interaction path preserves:

- Drunk shown-role interaction eligibility;
- `DRUNK` reliability;
- Poisoned real-role interaction eligibility;
- `POISONED` reliability;
- healthy real-role reliability.

Prefer generic/shared contract tests over duplicating nearly identical tests for every information character where possible.

---

## 9. Recommended implementation direction

First look for the smallest reusable semantic seam that can distinguish:

```text
interaction actor
vs
functioning ability owner
```

Good outcomes would include one shared helper/value object that can answer questions like:

```text
perceivedRole(player)
abilityReliability(player, role)
abilityFunctions(player, role)
shouldSimulateInteraction(player, role, trigger)
```

The exact API is not prescribed. Reuse an existing semantic abstraction if one already fits cleanly.

The important constraint is that downstream mechanical resolution must no longer infer real ability effect merely from the existence of UI/action state such as a selected target.

For example, a committed Monk target should represent:

> "the apparent Monk selected this player"

not automatically:

> "this player is canonically protected by a functioning Monk ability."

That second fact must be derived only after checking actual role and functioning state.

---

## 10. Scope constraints

This hotfix is intentionally narrow.

### In scope

- Drunk shown-role interaction correctness;
- Poisoned ability-functioning correctness for the same affected families;
- Monk mechanical protection resolution;
- Ravenkeeper conditional shown-role interaction;
- Sage conditional shown-role interaction;
- shared semantic helper/refactor needed to prevent recurrence;
- regression tests for already-audited mechanical roles;
- persistence/restore correctness where affected by the new semantic distinction;
- discovery and correction of directly related sibling defects.

### Explicitly out of scope

Do not mix in:

- misinformation probability/style tuning;
- `GameBalanceEvaluator` changes;
- Investigator small-game balance tuning;
- evil-side global win-rate tuning;
- history UI redesign;
- action timeline capture;
- PR #24 semantic-history foundation changes;
- PR #27 global-observation cutover changes;
- recommendation-entry-point migration;
- unrelated production Host redesign;
- script-wide gameplay balance changes.

If an unrelated defect is discovered, document it separately rather than expanding this hotfix.

---

## 11. Integration with unfinished stacked R6 work

This section is authoritative for branch/merge ordering.

### 11.1 Development branch

Develop this correctness repair from current `main` on a separate branch:

```text
main
  └─ codex/clocktower-drunk-poison-correctness-hotfix
```

Do **not** base the hotfix on PR #24 or PR #27.

Do **not** duplicate the same repair independently inside those R6 branches.

### 11.2 Merge priority

After tests and exact diff audit pass, this hotfix should be reviewed and merged to `main` **before expansionary production work continues on the stacked R6 branches**.

Target sequence:

```text
current main
   │
   ├── Drunk/Poison correctness hotfix
   │       │
   │       └── merge to main
   │
   ▼
updated main
   │
   └── integrate into PR #24 branch
             │
             ▼
        validate PR #24
             │
             └── integrate updated #24 into PR #27 branch
                         │
                         ▼
                    validate PR #27
                         │
                         └── resume #27 production wiring / later R6 work
```

### 11.3 Updating PR #24 after the hotfix merges

PR #24 already contains a long tests-first history and modifies `CampBoardGameHostApp.kt`. Prefer a low-risk integration of the updated `main` into the PR #24 branch rather than rewriting history merely to obtain a linear graph.

A merge of updated `main` into `codex/r6-semantic-history-foundation` is acceptable and is generally preferred if rebasing the long stack would create unnecessary risk.

Resolve any conflict semantically:

- preserve the hotfix's gameplay correctness behavior;
- preserve #24's semantic-history/persistence behavior;
- do not let one side silently revert the other;
- rerun the relevant local tests after integration.

The current #24 App patch is primarily in semantic-history state, save, restore, and reset, so direct semantic overlap with the Drunk/Poison resolution work is expected to be limited even though the same large file may be touched.

### 11.4 Updating PR #27 after #24 is current

PR #27 is intentionally stacked on #24. After #24 contains the merged hotfix baseline, integrate the updated #24 branch into `codex/r6-global-observation-cutover`.

Do not independently merge old `main` into #27 in a way that bypasses its #24 dependency.

Then rerun the relevant local tests and continue #27 from that updated stacked base.

### 11.5 What Work must not do

Work/Codex must not:

- merge #24/#27 early merely to unblock this correctness fix;
- implement the hotfix directly on #24 or #27;
- reimplement the same hotfix a second time after it has merged to `main`;
- continue #27 production App/Host wiring before the hotfix branch boundary and integration plan are understood;
- discard #24/#27 changes because they are not yet merged;
- force a history rewrite if a simple merge safely preserves both lines of work.

The goal is to preserve all unfinished R6 work while allowing the confirmed gameplay defect to reach `main` independently and first.

---

## 12. Validation requirements

Before considering the hotfix complete:

1. demonstrate tests-first red evidence for the missing contracts;
2. implement the smallest coherent production fix;
3. run the relevant Android/unit test suite locally;
4. run broader project tests that are available locally;
5. perform an exact diff audit;
6. verify no unrelated recommendation/history/R6 code was changed;
7. summarize every affected role and the final semantic behavior;
8. summarize any sibling defect discovered during the sweep;
9. stop before merge unless explicitly instructed to merge.

GitHub Actions may currently be quota-blocked; do not treat pre-runner `steps=null` failures as valid red/green evidence. Local executable test evidence remains required.

After the hotfix is explicitly approved and merged, branch-integration validation for #24 and #27 is a separate follow-up step; do not silently perform those merges as part of the hotfix implementation unless explicitly instructed.

---

## 13. Suggested Work/Codex startup instruction

Use this as the first implementation prompt:

> Read `docs/R6_DRUNK_POISON_CORRECTNESS_HOTFIX_HANDOFF_2026-08-22.md`. Audit the current `Jazz0006/CampBoardGameHost` live `main`, PR #24, and PR #27 heads before changing code. The unfinished R6 work must be preserved: #24 is the semantic-history foundation and #27 is stacked on #24. Create a separate correctness-hotfix branch from current `main`; do not implement this repair inside #24/#27. Strictly tests-first, implement the Drunk/Poisoned perceived-role vs ability-functioning contract described in the handoff. Prefer one coherent semantic seam over scattered role-specific Drunk checks. Do not modify misinformation tuning, history UI, balance tuning, or unrelated R6 work. Run local tests and perform an exact diff audit. Stop before merge unless explicitly instructed. After the hotfix is later approved and merged to `main`, the required integration order is updated `main` -> #24 branch -> #27 branch -> resume later R6 production wiring.

---

## 14. Completion definition

The hotfix is complete only when the production system consistently satisfies this invariant:

> **Shown/perceived role controls the player's simulated experience; actual role plus current impairment state controls whether an ability truly functions.**

A Drunk or Poisoned player must not gain a real mechanical ability merely because the Host UI correctly simulated that character's interaction.
