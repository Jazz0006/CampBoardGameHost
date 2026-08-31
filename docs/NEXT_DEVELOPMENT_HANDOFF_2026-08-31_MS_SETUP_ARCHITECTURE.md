# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Updated: 2026-09-01 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6C ACCEPTED — S6D-0 COMPLETE — S6D-1 RED NEXT — S7 BLOCKED**

## 1. Live / accepted checkpoints

Live `main` at S6D-0 audit:
`eed51bade5163790316a31e8295e2e841df90357`

Branch:
`codex/ms-setup-generic-architecture`

PR:
`#61 — DRAFT / OPEN / UNMERGED`

Accepted S6C code/test checkpoint:
`38a04c1353c883c3bda4b4a506085c3c1d2766bd`

Focused GREEN tree:
`70e7f41d1e30e5e701c02ceb95660572a99d27d4`

Acceptance:

```text
CI #1289: FAST / gate SUCCESS
R2 #1206: SUCCESS

CI #1290:
  :app:testFull + :app:assembleDebug SUCCESS
  ASP contract SUCCESS
  Real Clingo SUCCESS
  CI gate SUCCESS
R2 #1207 SUCCESS
```

Do not treat later docs-only S6D planning/audit commits as a replacement accepted code checkpoint.

## 2. Why S6D is required

S6C is complete and must not be reopened. S6D corrects the interpretation of its coverage.

The intended information architecture is:

```text
committed perceived role
-> role ability semantics / legal truth space
-> RELIABLE / POISONED / DRUNK
-> generic impairment policy
-> deterministic recommendation
-> observation/history
```

Current first-night production is a hybrid:

```text
perceived-role-aware actor resolution
-> role-local / legacy candidate construction
-> legacyInformationCandidates
-> unified first-night pool
-> generic reliability selection
-> structured display/observation
```

A3 exact Possible Worlds is already the correctness baseline, A4/ZDD remains shadow/prototype, and A3/A4 world-set quality does not generally own production recommendation. Do not introduce `before -> observation -> after` scoring in S6D/S7.

## 3. S6D-0 — COMPLETE

Dedicated audit:
`docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md`

The audit covered:

```text
Washerwoman
Librarian
Investigator
Chef
Empath
Fortune Teller
```

### 3.1 Global finding

First-night actor/waking identity is already perceived-role aware. `AbilityFunctioningSemantics` maps actual Drunk to committed shownRole and independently reports DRUNK/POISONED/FUNCTIONING.

Therefore the general defect is **not** that a Drunk shown as an information role fails to wake. The defect is that downstream candidate/truth ownership is not uniformly based on the same role-semantic space before reliability is applied.

### 3.2 Pair family findings

- Washerwoman remains Host-local and is not covered by the S6C generic pair ability generator.
- Librarian/Investigator have typed healthy candidate support, but current first-night natural precompute still selects actual Librarian/Investigator holders only.
- `actual Drunk + shownRole=Librarian/Investigator` can be found by UI actor resolution but does not enter that same typed natural precompute source.
- unreliable pair truth currently checks raw actual identity in the displayed pair, rather than consistently reusing registration-aware healthy truth.
- healthy Investigator correctly supports Recluse registered-state truth from S6C; unreliable Investigator can therefore classify the same role-legal clue differently.
- Washerwoman/Librarian have the analogous risk around Spy registration.
- legacy `recommendedDrunkInvestigatorOption` remains in UI compatibility projection and must not become new semantic authority.

### 3.3 Numeric family findings — highest immediate risk

Chef/Empath directly use different truth definitions based on reliability:

```text
healthy Chef    -> registration-aware chefValue
impaired Chef   -> raw chefActualIdentityValue

healthy Empath  -> registration-aware empathValue
impaired Empath -> raw empathActualIdentityValue
```

This violates the frozen layering. Reliability may change truth/false-family choice but must not change the role's semantic truth itself.

The generic numeric Foundation/UI is not the bug: it accepts a caller-supplied `trueValue`. Existing tests prove candidate projection/selection around that value, not that the caller computed it from the correct shared role semantics.

### 3.4 Fortune Teller

Keep Fortune Teller query-dependent. Existing tests protect first-night vs later-night Demon authority, not Drunk/Poisoned semantic equivalence. Preserve red-herring, Recluse-registration and selected-two-player semantics while completing a shared boolean/query semantic seam later in S6D.

## 4. S6D stage plan

```text
S6D-0  six-role coverage/ownership audit                         COMPLETE
S6D-1  high-value semantic behavior REDs                         NEXT
S6D-2  generic perceived-ability semantic request/result seam
S6D-3  pair family completion: WW / Librarian / Investigator
S6D-4  numeric family completion: Chef / Empath
S6D-5  query-dependent boolean completion: Fortune Teller
S6D-6  FirstNightInformationMigration shadow/parity integration
S6D-7  full acceptance + checkpoint
```

Do not mechanically create one test per role if two stable abstraction-level tests prove the risk.

## 5. S6D-1 — NEXT

Use behavior-first REDs only.

### RED A — registration-aware pair truth is reliability-invariant

Preferred proof uses Investigator + Recluse because healthy registered truth is already defined and accepted by S6C. Add Washerwoman + Spy only if needed to prove missing pair-family coverage.

Required behavior:

```text
same game
+ same perceived ability
+ same displayed clue
-> same semantic truth under role/registration rules
```

Changing RELIABLE -> POISONED/DRUNK may change impairment selection/provenance, but must not relabel a role-semantic truth as false.

Also prove an actual Drunk with committed shownRole reaches that perceived role's semantic space without mutating shown identity.

### RED B — numeric role truth is reliability-invariant

Prefer Empath with a Spy/Recluse registration ruling such that the role-legal value differs from raw actual identity count.

Prove:

```text
healthy Empath
poisoned Empath
actual Drunk shown Empath
```

share the same semantic truthful numeric value for the same role/game/registration context. Reliability changes recommendation family behavior only.

Current production is expected to fail because impaired Empath/Chef uses raw actual-identity reference values.

### Optional RED C

Add Washerwoman perceived-role coverage only if A/B do not naturally establish that missing pair family. Do not duplicate the entire six-role matrix as tests.

Fortune Teller belongs to S6D-5 unless a concrete earlier regression forces it forward.

## 6. GREEN direction after RED

Preferred architecture:

```text
perceived ability request
-> role semantic evaluator
-> healthy legal/truth space
-> reliability
-> generic impairment selection
-> typed AbilityObservation
-> first-night migration/UI projection
```

For pair roles, generalize/reuse the S6C healthy semantic-space seam rather than adding Drunk-specific branches.

For Chef/Empath, introduce the minimum semantic value/candidate seam so registration-aware truth is computed once and supplied to all reliability states.

For Fortune Teller, retain its chosen-target query shape.

Do not:

- modify committed shown identity;
- create DrunkEmpath/PoisonedEmpath/etc. strategy classes;
- change accepted 90/10 policy;
- introduce A3/A4 epistemic quality;
- broadly rewrite registration;
- start S7/S8/REC-R1 or App/Host decomposition.

## 7. Existing tests to preserve

- `NaturalPairInformationCandidateGeneratorTest` — healthy Librarian/Investigator truth and Investigator Recluse/Spy registration.
- `FirstNightInformationMigrationTest` — six-family shadow parity, mismatch fail-safe, invalidation and selected commit.
- `StructuredNumberInformationUiModelTest` — typed Foundation-to-number-UI projection and stale validation.
- `StructuredEmpathInformationAdapterTest` — healthy/poisoned typed Empath UI/Foundation behavior around a supplied true value.
- `MalfunctionNumberPolicyTest` — generic numeric candidate/policy behavior.
- `ClocktowerFortuneTellerPhaseAuthorityTest` — first-night base-Demon vs other-night effective-state authority.

Do not mistake these tests for proof of the missing real-game semantic input contract.

## 8. S6D acceptance condition

S6D needs its own checkpoint before S7 can start. Acceptance requires:

1. all six first-night B2 families have explicit semantic ownership;
2. Drunk shown X reaches X semantics without actualRole==X shortcuts;
3. Healthy/Poisoned/Drunk share role truth/legal-space semantics;
4. registration is resolved before impairment;
5. migration/parity remains fail-safe;
6. shown identity remains immutable;
7. focused CI/R2 and required full acceptance pass.

## 9. S7 boundary

S7 remains blocked until S6D acceptance.

Afterward S7 owns the controlled TB production cutover through:

```text
480 templates
-> S5 composition
-> S6A policy
-> S6B identity commit
-> CommittedClocktowerSetup
-> S6D semantic information
-> UI projection
```

S7 may retain `legacyInformationCandidates` as a compatibility/UI projection while authority migrates. It must prove no dual semantic authority before retiring an old path.

S7 does not own A3/A4 epistemic quality.

## 10. After S7

Resume the v2.2 algorithm-consistency route as a separate campaign/PR:

**ALG-B2R — First-night Epistemic Gate**

```text
semantic candidate
-> recipient PlayerWorldSet BEFORE
-> observation
-> PlayerWorldSet AFTER
-> epistemic metrics / quality gates
-> ranking
```

Do not silently stack ALG-B2R onto PR #61.

## 11. Protected invariants

```text
Drunk actual identity remains Drunk.
Committed shown identity cannot be rerolled/replaced by recommendation.
S5 cannot consume shown identity.
S6A owns identity legality only.
S6B owns commitment only.
S6C/S6D own information semantics only.
Healthy/Poisoned/Drunk share semantic truth before reliability policy.
Registration stays below impairment policy.
Restore/start/background work cannot reroll committed setup.
```

Also preserve Dawn/Dusk convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe behavior, living-Demon UI authority and current NGJ legality.

## 12. Validation / governance

Follow `AGENTS.md`, `docs/TESTING_STRATEGY.md` and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based tests-first. Do not add source-string, exact helper/call-chain, class-existence or exact internal constant tests when a behavior seam exists.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.

## 13. Resume guard

1. read `AGENTS.md`;
2. read roadmap and this handoff;
3. read `docs/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md`;
4. preserve accepted S6C code checkpoint `38a04c1353c883c3bda4b4a506085c3c1d2766bd` until S6D acceptance;
5. re-query live main/branch/PR/checks;
6. begin **S6D-1 RED**, not S7;
7. start with registration-sensitive pair and numeric behavior contracts;
8. do not introduce A3/A4 epistemic ranking;
9. keep PR #61 Draft/unmerged.
