# Development Lessons — Same-Night Effective State Campaign

> Date: 2026-08-27  
> Purpose: preserve concrete engineering lessons from the SNE-6B2 campaign so later work does not repeat the same failures.

## 1. Rules legality must be upstream of recommendations

The Demon-successor work exposed an invalid dependency direction:

```text
recommendations
→ names shown by UI
→ treated as legal targets
```

The correct direction is:

```text
rules semantics
→ legal target identities
→ manual UI = all legal targets
→ recommender = ranking/subset inside the legal set
```

A recommender must never become a hidden rules engine. The same rule applies to Mayor redirect, registration, misinformation, and future automatic target selection.

## 2. Stable identity must not be rebuilt from filtered views

A seat is a stable table identity. Never derive it by re-indexing `publicAliveCards` or another filtered list.

Correct pattern:

```kotlin
cards.indexOf(card) + 1
```

General rule:

```text
stable identity != current filtered-view position
```

This applies to seats, replay identity, checkpoints, registration subjects, decision candidates, and night interaction IDs.

## 3. Pure semantics may lead production, but production wiring must not

A pure rules function may safely support a future case before production uses it. Production must only feed cases that the current slice has explicitly validated.

Do not connect a broader input merely because the pure model already accepts it. This prevented non-self Demon death from being accidentally introduced while only Imp self-kill succession had been proved.

## 4. Draft selection and confirmed mechanical fact are different state

```text
editable draft
!=
confirmed mechanical fact
```

A UI selection becomes mechanical authority only at the explicit confirmation boundary.

Consequences:

- Previous must not erase already-confirmed mechanics.
- Editing a draft without confirming must not change effective state.
- Reconfirmation may replace a confirmed fact only through the explicit transaction.
- Legacy save data containing draft-only state must not invent a historical confirmation during restore.

## 5. Same-night role changes belong in projection, not early public mutation

Same-night state now follows:

```text
persisted/public base role
+ confirmed RoleChanged facts
+ canonical interaction cursor
→ effective current role
```

Do not mutate `PlayerCard` early merely to make later same-night logic see a new role.

This preserves both dimensions needed by rules such as Fortune Teller:

- old dead Demon remains a Demon for truth evaluation;
- new successor is also a Demon for later same-night consumers.

Public/persisted materialization happens at the defined Dawn transaction boundary.

## 6. Persistent effects follow source-ability lifetime

A stored confirmed target is not the same thing as a currently effective effect.

Example:

```text
Poisoner confirms poison target
→ Poisoner later becomes Demon
→ stored confirmed target may remain
→ effective poison immediately ends because the source no longer owns Poisoner ability
```

Do not clear raw historical facts merely to model current effectiveness.

## 7. Dawn materialization must use exact confirmed facts

Imp succession must not fall back from:

```text
draft target
or
first living Minion
```

Dawn materialization consumes the exact confirmed successor only. If succession is mechanically required but no confirmed target exists, the game must remain unresolved rather than inventing a target.

## 8. Game-outcome evaluation must respect unresolved mechanical transactions

SNE-6B2.6 exposed a hidden coupling: once public role mutation is correctly delayed, `evaluateGameOutcome(...)` can incorrectly see the old Demon dead before the confirmed successor has been materialized.

Therefore unresolved/pending Demon succession blocks premature game-outcome evaluation and Dawn transition.

This is a general transaction lesson: **derived terminal state must not be evaluated in the middle of an unresolved mandatory mechanical transition.**

## 9. Product simplification must be explicit, not disguised as official rules

For the current Trouble Brewing automatic-host implementation, the product intentionally prohibits Mayor redirect from targeting the current Demon.

This is a product/house-rule restriction, not official Blood on the Clocktower semantics. It is used to avoid introducing generic non-self Demon-death succession before that cross-script behavior is implemented.

Generic non-self Demon death remains deferred for arbitrary custom/dynamic scripts. Future examples include night deaths or role-change flows caused by characters such as Assassin, Godfather, Gossip, Pit-Hag, Fang Gu, and future roles.

## 10. Source-wiring tests must lock semantics, not Kotlin formatting

Several false REDs came from source-string tests that depended on:

- the first textual occurrence of a common token;
- a function call being on one line;
- exact whitespace or line wrapping.

Correct practice:

```text
unique block anchor
+ contains semantic token A
+ contains semantic token B
+ absence of forbidden legacy token C
```

Do not change correct production code or insert meaningless comments to satisfy a brittle source-string assertion.

When one test contains several instances of the same brittle assumption, repair all of them in one test-only correction instead of discovering them one assertion at a time.

## 11. Expected RED failure must be described as success for the RED phase

Luna once stopped after reporting a JUnit failure even though that failure was the intended RED.

Future instructions must say explicitly:

```text
If the focused test fails at the expected contract/assertion,
that is RED PASS; continue to the production patch.
```

## 12. ChatGPT and Luna responsibilities must remain asymmetric

ChatGPT owns:

- architecture and rule semantics;
- live GitHub state;
- RED design;
- small/medium connector edits;
- exact patch specification for large files;
- remote diff/parent/scope audit;
- checkpoint and CI acceptance.

Luna owns only the requested file-level implementation and local validation for files/tasks that require a real worktree.

Do not ask Luna to repeat architecture audit or remote review that ChatGPT can perform independently.

## 13. Luna instructions should be short and deterministic

Normal Luna task shape:

```text
HEAD
→ exact file edits
→ exact focused test
→ checkpoint test only if required
→ git diff --check
→ commit + push
```

Avoid long architecture explanations and implementation-choice language. The architectural decision should already have been made before Luna receives the task.

## 14. Test cadence must match risk, not commit count

Micro-slice:

```text
RED T0
→ GREEN T0
→ diff-check
→ push
→ remote diff audit
```

Related micro-slices can continue without waiting for old-head CI. At the logical checkpoint:

```text
T1 / affected T2-T3 as triggered
→ latest-head GitHub CI/R2
→ stage ACCEPT
```

Do not automatically run `testFast` or full CI after every small edit. Conversely, persistence/transaction/shared-projector/build changes may deserve earlier escalation.

## 15. Do not duplicate already-valid local test evidence

When Luna ran an exact focused test with `--rerun-tasks` and reported `BUILD SUCCESSFUL`, ChatGPT should not run the identical test again simply to obtain duplicate evidence.

ChatGPT's independent verification should instead be:

- remote parent chain;
- exact canonical diff;
- changed-file scope;
- stage-level CI when it is actually the gate.
