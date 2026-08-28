# GCR-3 Source-String Retirement Audit

> Date: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Parent acceptance checkpoint: `474103ed13caaf34a329ca5e80e2f0ba64963b86`

## 1. Entry condition

GCR-1 and GCR-2 blocker acceptance is complete at `474103ed13caaf34a329ca5e80e2f0ba64963b86`.

Full acceptance evidence:

```text
CI #963 / run 33175600756 SUCCESS
- Android :app:testFull + :app:assembleDebug SUCCESS
- ASP contract tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS

R2 run 33175600749 SUCCESS
```

GCR-3 is test-quality cleanup. It does not change accepted gameplay semantics merely to retire source inspection.

## 2. Classification rule

```text
A. typed replacement already proves behavior -> retire source-string test
B. behavior matters but production boundary is not callable -> introduce a seam only when independently justified
C. architecture/ownership-only invariant -> keep coarse source guard, remove gameplay/detail/order assertions
D. obsolete implementation-shape assertion -> delete
```

## 3. Final classification and status

| Test / source portion | Class | Final status |
|---|---|---|
| `ClocktowerDawnDurableMaterializationProductionWiringTest` | C | **Slimmed.** Retains only App callback -> `NightDawnDurableMaterializationPlanner` + durable-state ownership. GCR-3C seam extraction deferred because typed planner/session tests already prove semantics. |
| `ClocktowerGlobalObservationProductionWiringTest` | B/C | **Slimmed.** Retains history-mode ownership, App -> canonical session commit authority, and Host draft-vs-durable boundary. |
| `InformationDecisionProductionAuthorityWiringTest` | B/C | **Slimmed.** Retains only Host publication through canonical confirmation authority. |
| `ClocktowerDemonSuccessionProductionWiringTest` | C | **Slimmed.** Retains App -> canonical succession resolver/planner ownership. |
| `ClocktowerHistoricalActionProductionWiringTest` | B/C | **Slimmed.** Retains one durable action timeline, canonical session authority, persistence ownership, and typed confirmed action drafts. |
| `ClocktowerNightRestoreProductionOwnershipTest` | C | **Slimmed.** Retains Host -> `NightTransactionReconstructor` using restored `nightCheckpoint`. |
| `ClocktowerSameNightEffectiveStateProductionWiringTest` | C | **Slimmed.** Retains only Host effective-ability-subject and poison-lifecycle ownership. |
| `ClocktowerMayorDemonExclusionWiringTest` | C | **Slimmed.** Retains rules-owned legal-target consumption. |
| `ClocktowerProductionOtherNightWiringTest` | C | **Slimmed.** Retains planner/materializer ownership and absence of the legacy parallel ordering path. |
| source-wiring portion of `ClocktowerChambermaidSelectionAuthorityTest` | C | **Slimmed.** Production inspection is one Host -> `resolveChambermaidSelection` ownership assertion. |
| `ClocktowerNightTransactionArchitectureGuardTest` | C | **Consolidated.** Four coarse contracts remain: typed checkpoint owner, pure-vs-durable boundary, canonical Dawn planner owner, and Host checkpoint-backed death consumer. |
| `ClocktowerCurrentDemonProductionWiringTest` | C temporary | **Slimmed.** One Host -> `resolveCurrentDemonHostContext` ownership assertion remains until a callable Host presentation seam exists for an independent reason. |
| `ClocktowerPoisonedSpyFailSafePolicyWiringTest` | C temporary | **Slimmed.** Two policy-boundary assertions remain: First Night and Other Night poisoned Spy branches publish no `GrimoireState`. No fake-Grimoire subsystem introduced. |

## 4. Completed test-only cleanup checkpoints

```text
3ffc353d  docs: add GCR-3 retirement audit
c42b88b3  Chambermaid source guard slimming
27e894ae  Other Night source guard slimming
a04f9e9a  Demon succession source guard slimming
321ea100  Mayor source guard slimming
25e0d6db  InformationDecision source guard slimming
326ee38f  Global observation source guard slimming
eda18877  Historical action source guard slimming
84e808de  Night transaction architecture guard consolidation
46ed471d  Same-night effective-state guard slimming
910f2c32  Current-Demon guard slimming
e866b8ae  Night-restore ownership guard slimming
b54d3014  Poisoned-Spy fail-safe guard slimming
732f6548  Dawn durable materialization guard slimming
```

No production file changed in GCR-3.

Intermediate validation:

```text
fe8816bd
- CI #973 SUCCESS
- R2 #900 SUCCESS
```

The final test-only head must receive its own FAST/R2 acceptance before GCR-3 is marked accepted.

## 5. GCR-3C decision

Do **not** extract a Dawn production seam solely to remove the remaining coarse source guard.

Typed planner/session tests already own exactly-once, stable-ID and retry semantics. The remaining source test protects only the non-callable App callback ownership boundary. A callable `ProductionDawnMaterializer` may be introduced later only if production architecture independently benefits from it.

## 6. Stop conditions

- No gameplay semantics change during source-test cleanup.
- No broad App/Host refactor solely to reduce source-string count.
- Do not merge or mark PR #54 ready.
- Accept GCR-3 only after the final test-only head passes FAST and R2.
