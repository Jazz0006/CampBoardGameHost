# EXPERIENCED NIGHT FLOW — S3 Target Confirmation Eligibility Audit

> Date: 2026-09-14 Australia/Sydney  
> Status: **S3 COMPLETE — shared eligibility implemented and remote validation green**
> Program: `EXPERIENCED-NIGHT-FLOW-1`  
> Branch: `codex/experienced-night-flow-correctness`  
> PR: `#123` — draft / do not merge yet  
> Parent handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-14_EXPERIENCED_NIGHT_FLOW_CORRECTNESS.md`

## 1. Frozen S3 invariant

For a required single-target interaction, confirmation is enabled exactly when:

```text
interaction is enabled
+ selection exists
+ selection belongs to the already-authoritative supplied candidate domain
-> confirmation enabled
```

Presentation consumes the supplied legal domain. It must not infer role rules, life state, team,
registration, protection, or any other gameplay legality.

An explicitly modeled alternate legal outcome may supplement the table candidate domain. In S3 the
only such outcome is `Mayor dies`, represented by the Mayor seat on the Mayor-redirect ruling.

## 2. Defect and ownership finding

`ClocktowerSingleTargetSelection` already carries the three inputs needed by the invariant:
`selectedSeat`, `selectableSeats`, and `enabled`. The common square-table dialog also supports a
`nextEnabled` input. However, the current ordinary, Beginner-compact, and Ravenkeeper single-target
render paths accept the dialog default of `true`. Manual Mayor/successor rulings use only
`!automatic`. Consequently a null or restored stale selection can expose an enabled Next action.

The square-table seat layer is not another legality owner: it attaches a click handler only when
the table is selectable and the individual seat is interaction-enabled. The missing contract is
therefore confirmation eligibility, not seat-click filtering.

The stable S3 seam is one pure derived helper beside `ClocktowerSingleTargetSelection`. Every
must-inherit surface passes its result to the existing `nextEnabled` parameter. The generic dialog
default remains unchanged because read-only and non-target night surfaces intentionally use it.

## 3. Producer and state matrix

| Action | Production source | Supplied candidate domain | Selection authority | Restore behavior | Mode behavior |
|---|---|---|---|---|---|
| Red Herring | First Night Fortune Teller setup | `clocktowerRedHerringCandidates(publicAliveCards)` | recovery mechanics | restored mechanics value may be absent/stale; eligibility must fail closed | Beginner may auto-select/advance; Experienced manual |
| Poison | First/Other Night Poisoner action | alive public cards | checkpoint draft/confirmed target | draft is restored and revalidated by membership | player choice in both modes |
| Butler Master | First/Other Night Butler action | every seat except Butler | recovery mechanics | restored value is revalidated by membership | player choice in both modes |
| Monk Protect | Other Night Monk action | `clocktowerMonkTargetCards(cards, actor)` | checkpoint draft/confirmed target | draft is restored and revalidated by membership | player choice in both modes |
| Demon Kill | Other Night living Demon action | alive public cards; self allowed | checkpoint draft/confirmed target | draft is restored and revalidated by membership | player choice in both modes |
| Ravenkeeper | Dynamic death-triggered action | `clocktowerRavenkeeperTargetCards(cards)` | transient target state | restored flow begins with no transient selection | player choice in both modes; specialized result surface |
| Mayor Redirect | `MAYOR_REDIRECT_ELIGIBLE` ruling | redirect candidates; Mayor seat is explicit alternate outcome | checkpoint draft/confirmed target | draft is restored and revalidated against either legal outcome | Beginner automatic/read-only; Experienced manual |
| Demon Successor | forced/choice successor ruling | exact resolver-produced successor seats | checkpoint draft/confirmed target | draft is restored and revalidated by membership | Beginner automatic/read-only; Experienced manual |

Candidate production remains authoritative. S3 does not change any row's domain or persistence
owner.

## 4. Must-inherit fan-out

The shared eligibility contract must feed:

1. `ClocktowerSingleTargetAbilitySection` for Red Herring, Poison, Butler Master, Monk Protect and
   Demon Kill;
2. `ClocktowerBeginnerSingleTargetAbilityDialog` for the same interaction family when compact
   guidance is selected;
3. `ClocktowerRavenkeeperSquareTableDialog`, including its Beginner reveal variant;
4. `ClocktowerNightRulingSection` for manual Mayor Redirect and Demon Successor.

Automatic Mayor/successor surfaces remain concrete but read-only while their existing effect
settles, so their Next action remains disabled regardless of any carried selection.

## 5. Intentional exemptions

| Surface | Reason |
|---|---|
| Fortune Teller and Chambermaid two-target flows | They have distinct multi-target/result eligibility and are not single-target consumers. |
| Generic information and read-only reveal surfaces | They do not require a target selection before navigation. |
| Outer `ClocktowerNightActiveScreen` bottom action bar | S2 full-screen plans own the rendered navigation for these steps; this legacy shell is not the production confirmation owner. |
| Candidate producers and confirmation reducers | They remain gameplay/legal-domain and durable-transition owners; S3 only derives UI eligibility. |

## 6. Typed evidence plan

The smallest durable RED is a direct test of the pure contract:

- null selection is disabled;
- a member of the supplied domain is enabled;
- a non-member/stale selection is disabled;
- a disabled interaction stays disabled even with a legal selection;
- an explicitly supplied alternate legal seat enables Mayor confirmation.

The production wiring then consumes the same helper in every must-inherit surface. Static call-site
audit is required in addition to focused unit tests because local Compose UI instrumentation is not
part of the fast JVM suite.

## 7. Deferred adjacent finding

Poison and Monk seat-toggle handlers compare a tap with the confirmed target rather than the current
draft target. That can affect deselection after editing a previously confirmed choice. It does not
change the S3 confirmation invariant or candidate domain, so it is deferred to the S4 edit/reconfirm
regression matrix rather than widening this change.

## 8. Implementation and validation result

Remote checkpoints:

```text
49b1c3e5 docs: audit S3 target confirmation eligibility
f7c6d4d6 test: define S3 target eligibility RED
c9faee03 fix: require legal single target before advance [full-ci]
```

The implementation added one pure `clocktowerSingleTargetConfirmationEnabled` contract and exposed
derived presentation properties for ordinary abilities and rulings. Every must-inherit surface now
passes that value to the existing navigation scaffold. The generic square-table default was left
unchanged for intentional non-target exemptions. The impossible/defensive Red Herring empty-domain
copy now directs the Host to go back and inspect player state instead of contradicting the disabled
Next action.

Validation evidence:

```text
local git diff --check and XML parse                              PASS
must-inherit call-site and nextEnabled scan                       PASS
local focused Gradle execution                                    BLOCKED before compilation
reason                                                            uncached Gradle 9.5 distribution; services.gradle.org unreachable
R2 main-thread boundary run 34807533943                            PASS
CI run 34807533986 / full Android unit tests + debug APK           PASS
3da6ac62...c9faee03 exact remote compare                          PASS
remote commit count                                                3 ahead / 0 behind
changed-file scope                                                 exact expected 8 paths
```

S3 is accepted. Continue with S4's Experienced/Beginner regression matrix, including the deferred
Poison/Monk edit-and-reconfirm behavior and required real-device acceptance. PR #123 remains Draft.
