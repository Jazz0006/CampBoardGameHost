> **历史实施记录（2026-10-20 标记）**：本文后续段落中的旧 Pending/Previous/Dawn/展示即提交和 MiniMCP 起点均已撤销或过时；不得据此执行。当前唯一接续见 [NEXT_DEVELOPMENT_HANDOFF](../../NEXT_DEVELOPMENT_HANDOFF.md) 与 [CURRENT_DEVELOPMENT_ROADMAP](../../CURRENT_DEVELOPMENT_ROADMAP.md)。D2 已在分批实施，完整 nextStep/恢复/P5 未完成。

# Minimal Host Rebuild — P0 current-tree audit and first P1 handoff (2026-10-19; historical evidence)

> **2026-10-20 USER A–D SUPERSESSION:** [MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md](../../MINIMAL_HOST_FLOW_AUDIT_AND_DECISIONS_2026-10-20.md) is the latest authoritative human-reviewed process audit. A delete night Start/Ready page and `nightStarted` control; B inline end-day single confirmation on existing Day table, no EndConfirm page; C no standalone KlutzLearnedDeath/learned-death event, derive actual Klutz choice from confirmed death/identity (retain the player's real selection); D private information actually shown becomes one Session observation, dismiss closes the step and auto-advances (no extra Next). Sections below headed older Pending/Back/Dawn or earlier phase summaries are **historical records**, not a valid implementation specification. After replacements are verified, physically remove all obsolete production producers/consumers/models/screens/tests/CI guards in the same migration batch. Existing AI/Host facts, two built-in scripts, security, game continuity and both inter-game diversity paths must continue to work. Changes are documentation, not completed A–D code migration.

> **Evidence limit:** Initial current-source audit enumerated 226 Kotlin production files and probed key execution chains; no claim of complete semantic all-file review, crash-recovery implementation, TB/NGJ all-role proof, or A–D implementation acceptance. Current branch/HEAD/CI must be re-queried before the next code change.

> **Execution record / P1 remains incomplete.** This document supplements, never overrides, `MINIMAL_HOST_ARCHITECTURE_REBUILD_2026-10-19.md`. Original inventory baseline `c91a83ff` is historical. Current #335 code checkpoint `f91184fd24ed394b4880330bccc56bc18796eacf` implements Session-staged Imp attack committed at the next step's explicit start, plus private NIGHT deaths and atomic DAWN/DAY public announcements. [Full Android CI](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39254476810) / [R2](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39254476803) PASSED. #330–#333 merged into `main`, #334 and #335 still Draft. This is only a vertical slice: generic pendingOperation, immediate deterministic night death effects/conditional wake, old control retirement and crash recovery are **not implemented**. See NEXT_DEVELOPMENT_HANDOFF for current execution order.

## 2026-10-20 follow-up — one pending writer now handles four player actions

The current #335 code HEAD is `c29a319b7e4d29423fb4c5534060924ab2cc6f8b`. It has replaced the Imp-only `pendingNightAttack` with a single Session-owned `pendingOperation` covering Poisoner, Monk, Butler and Imp, including deterministic timing and poison flags only when the subsequent *real* step begins. The UI derives already-accepted action kinds from Session history and prevents back-editing, and the old Butler immediate writer and `clearConfirmedNightAttack/Protection` were physically removed. `be91f77` and `8f7058e` passed full Android CI/R2; latest `c29a319b` [full Android CI](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39327637102) / [R2](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/39327637052) both passed. This does not yet address night-end manual death entry, immediate death-triggered wakes, resumable durable checkpoints, or the full removal of death-preview adapters. Keep #335 Draft.

## 2026-10-20 Current status (supersedes old Pending checkpoints above)

The former `pendingNightAttack` and general `pendingOperation` paths have been physically retired in #335. All live night screens proceed forward only; actions commit at the current step's Next, with no intermediate Begin Current Step button. Normal Imp attack mechanically commits any forced private Death immediately, and Mayor bounce has now been migrated to the same Session action+Death atomic boundary in `19c7c9a`; unresolved Mayor rulings block dawn in `c178e8f`. Older inventory/parity notes below are historic, **not** current production contracts. Imp self-kill succession, legacy night-end free result UI retirement, and practical interruption recovery are still open.

## 2026-10-20 Night-end double-path retired (latest checkpoint)

The Mayor-bounced-Imp succession code at `b040fb66` writes the redirected Imp death and forced Scarlet Woman RoleChange in one Session transaction. At `712d46e6` the production night-result picker, late death writer, successor selection and poison carry override were deleted rather than kept as alternate authorities. At `ae0e3447`, the last dynamic-night flow callback and FIRST_NIGHT-to-DAY completion were aligned with the one-way flow. `71f54a7e` retires the last obsolete cleanup test. The actual Dawn `PhaseAdvance` publishes already confirmed private Death observations; poison persists through Dawn/Day and expires at outgoing Day. Exact latest full CI/R2 must be checked before declaring verification. All old inventory/read-path notes below are historical; priority next is obsolete attack-preview adapters, wake/identity/Mayor test coverage, practical crash recovery and complete TB 5–15 / NGJ 5–6 games.

## 2026-10-20 Night→Day without Dawn (latest)

The user explicitly cancelled a distinct Dawn/public-death knowledge system. A confirmed Death is effective immediately for all subsequent abilities. `0e63d9c` removed the Dawn phase enums, UI screen, and synthetic Night→Dawn `PUBLIC AliveAt` duplication, displaying the confirmed Night Death list directly on the existing Day table. It passed full Android CI #39355476807. R2 initially failed because the prior structural guard *required* that obsolete Dawn screen; `64c57df` reverses the R2 guard, retires the second Imp role writer and renames historical phase-based helper naming while preserving stable action IDs; full Android CI #39356416753 and R2 #39356416743 both passed. Official script metadata's `dawn` token remains solely a termination marker; do not reintroduce a step or separate UI. Keep NGJ Klutz player action, actual private information observations and AI recommendation chain. Legacy daytime public-death observation duplication, obsolete night attack prediction adapters, crash recovery and whole-role coverage are still open.

## Inventory and evidence limits

The current tracked tree contains 227 production Kotlin files and 161 unit-test Kotlin files, with no tracked `androidTest` Kotlin source. An ownership/retirement row for each production Kotlin path is in [MINIMAL_HOST_P0_FILE_MAPPING_2026-10-19.tsv](../../MINIMAL_HOST_P0_FILE_MAPPING_2026-10-19.tsv). Each row states evidence depth; a filename/package classification **is not** proof of full-file semantic review. Unverified paths remain REVIEW_REQUIRED or *_REVIEW and must not be deleted by name alone. Tests, XML/assets, workflow guardrails, and non-Kotlin production resources also need semantic P0 sign-off before claiming complete whole-tree audit.

Current read-proven critical chain:
`built-in script/catalog → ClocktowerProductionOtherNightFlow → ClocktowerFlowPlanner/ClocktowerHostInteractionProjector → ClocktowerNightStepMaterializerRegistry → ClocktowerHostScreen`.
`App confirmed target/Monk/Poisoner → ClocktowerNightCheckpoint → ClocktowerDemonAttackProductionAdapter → ClocktowerNightHostProjection → conditional Ravenkeeper/Sage steps → ClocktowerNightResultConfirmation → ClocktowerGameSession.commitConfirmedNightDeaths`.
Current late `commitConfirmedNightDeaths` mechanically applies deaths only at night-end, and creates PUBLIC death observations in NIGHT. These are not accepted final semantics. Session is the existing live writer; it lacks one durable pending + stable resumable step ID / atomic on-disk restore. The two oversize production Kotlin files are `CampBoardGameHostApp.kt` (~156KB) and `ClocktowerHostScreen.kt` (~94KB).

### Executable replacement / retirement ledger

| Priority | Currently live producer or consumer | New accepted owner | Required cutover proof | Then retire |
|---|---|---|---|---|
| P1a | App/HostScreen DemonKill draft→immediate Attack fact; `ClocktowerNightCheckpoint` | one Session-owned pending Attack, Next stage, ready step, next-real-start accept; canonical AI revision | attack 3→6 produces only final Attack 6; no Attack after stage; cannot ordinary undo after start | independent UI attack draft and its adapters; do **not** retire whole night projection yet |
| P1b | Session in-memory commits only, UI cursor in Compose | one Session state containing pending, stable step, ordered facts and atomic durable transaction | crash immediately after stage/accept; replay and stale versions; UI has no second authoritative cursor | redundant UI checkpoint/restore policies and obsolete tests |
| P2a | `ClocktowerDemonAttackProductionAdapter.kt` + `ClocktowerNightHostProjection.kt` death preview | narrow Host action-time poison/Monk/Imp semantics → accepted private death event+GameState | poison before Monk, after Monk, Mayor genuine redirect, Soldier, self-kill/Scarlet Woman, Ravenkeeper/Sage trigger, dead-actor skip | obsolete attack/dawn death projection and exclusive tests, not working capability |
| P2b | `ClocktowerNightResultConfirmation.kt`, App `onConfirmNight` arbitrarily selects dead/new Imp/keepPoison | Session event-driven forced death/poison expiry/succession; Dawn publishes only already accepted deaths | no PUBLIC observation before Dawn; Dawn no free-form mechanics; private new Imp handoff | night-end free-form result screen and callbacks/tests |
| P2c | `ClocktowerFlowPlanner.kt`, `ClocktowerHostInteractionProjector.kt`, `ClocktowerProductionFirstNightFlow.kt`, `ClocktowerProductionOtherNightFlow.kt`, `ClocktowerResolvedFlowFacts.kt` | `nextStep(Session, built-in Script)` with stable step identity / ordered triggers | all actual TB/NGJ role wakes/skips in 5/6/7/9/10/12/13/15 seats; no repeat after cursor reorder | entire old parallel planner/projector/registry control chain and exclusive tests |
| P3 | Day nomination / voting / execution / character special UI control | Session event+forced outcome engine | Virgin, Slayer, Saint, Butler vote, Mayor, Klutz, ghost vote, endgame | UI-owned adjudication and old test-only candidate rules |
| P4 | AI, setup, presentation mixed in App | existing opening and later AI via read-only Session adapter, accepted results via same Host boundary | Manual/Assisted/Automatic parity; strategy memory; two across-game anti-repeat tests; stale pending fingerprint | redundant AI/Host adapter and mutable UI truth only when fully replaced |

No complete night old chain is proven safe for immediate independent deletion. Existing production callers are **migration work**, not a reason to keep superseded design forever. Do not revive generic Rules, ratings, world enumeration, or shadow state.

## Built-in character contracts

Legend: **H** = Host forced rule once facts are known; **P** = player supplies target/action; **S** = AI or human Storyteller makes an actually permissible discretionary choice; **private/public** indicates real delivery/publication boundary. Actual shown information, including errors, must be recorded as shown; displayed wrong information does not prove a registration choice. Duration for first-night information is one reveal, nightly abilities resolve in the relevant order; one-use day abilities persist throughout the game.

| Script / character | Trigger; required input | Determinate Host result vs genuine discretion | Duration / actual visibility |
|---|---|---|---|
| TB Washerwoman | first night; no player choice | S chooses valid pair, role; H checks forced unique results after registration facts | once, private |
| TB Librarian | first night | H zero-Outsiders when forced; otherwise S pair/role incl. genuine registration choice | once, private |
| TB Investigator | first night | S permitted Minion pair/role; do not expose actual roles beyond clue | once, private |
| TB Chef | first night | H adjacent evil pairs when registration unambiguous; S at registration/impairment | once, private |
| TB Empath | each living night | H living-neighbor evil count at action time; S permitted registration/impairment | nightly, private |
| TB Fortune Teller | each living night; P two targets | H Boolean demon/red herring if forced; S registration/impairment | nightly, private; red herring persistent |
| TB Undertaker | later night following execution | H wake and executed character when execution occurred; S permitted misinformation | nightly condition, private |
| TB Monk | later night; P chooses another player | H protects the chosen player from Demon **that night** when functioning; does not persist into the following day or night | nightly protection private |
| TB Ravenkeeper | night death trigger; P picks target | H exact trigger and character when forced; S permitted misinformation/registration | once at qualifying death, private |
| TB Virgin | first nomination by a Townsfolk | H consumes ability, initiates execution if functioning; registration may need S | one use, public |
| TB Slayer | daytime, publicly P target | H kill Demon if functioning; S registration exceptions | one use, public |
| TB Soldier | Demon attack | H avoids death if functioning | passive, private until Dawn |
| TB Mayor | Demon attack / 3 alive no execution | H victory when criteria; S may redirect Demon kill under permitted conditions | passive; victory public/redirect private |
| TB Butler | each night P master | H next-day voting condition while master votes | night decision private, day voting public |
| TB Drunk | setup H actual Drunk, shown Townsfolk | H no depicted ability; S false information as permitted | persistent, shown private |
| TB Recluse | relevant registration | S permitted evil/Minion/Demon registration only at check | contextual, private unless result shown |
| TB Saint | executed | H immediate evil victory if criteria | single execution, public |
| TB Poisoner | each night P target | H target poisoned this night and following day when ability functions; time ordered | private temporal status |
| TB Spy | every night | H offers existing Storyteller table; S permitted good registration | nightly private phone, no mirror |
| TB Scarlet Woman | Demon death with >=5 players alive | H fixed succession when qualifying (evaluate alive at relevant time) | passive, new Demon private |
| TB Baron | setup | H outsider count modifier (respect setup capacity) | setup only, private roster |
| TB Imp | later night P target | H death only after actual-time protection/poison/Mayor; H self-kill successor if determined; S genuinely discretionary | attack private, death internal immediate/Dawn public |
| NGJ Clockmaker | first night | H distance between Demon and closest Minion (registration caveats) | once, private |
| NGJ Investigator | first night | S permitted pair/Minion choice, H fixed info only if forced | once, private |
| NGJ Empath | each living night | H living-neighbor count; S impairment/registration | nightly private |
| NGJ Chambermaid | each living night; P chooses two living others | H count of who woke due to own ability, using actual wake events | nightly private |
| NGJ Artist | daytime once; P poses yes/no question | H unique answer if clear/healthy; S question interpretation and permitted false information | one use, private |
| NGJ Sage | killed by Demon | H death trigger; **S chooses two names including Demon, Sage does not choose** | once on qualifying death, private |
| NGJ Drunk | setup | H actual vs shown distinct; S permitted misinformation | persistent private |
| NGJ Klutz | learns death; P publicly chooses living player | H evil-result victory | on learned death, public |
| NGJ Baron | setup | H script's outsider modification constrained at 6-player special capacity | setup |
| NGJ Scarlet Woman | qualifying Demon death | H successor where uniquely determined | passive private |
| NGJ Imp | later night P target | H death and transfer if forced | attack private/death Dawn-public |

## Cross-role/size acceptance examples

- Poisoner → Monk → Imp: action-order poisoning and effective Monk protection; dying Monk after protection cannot erase an already functioning earlier protection retroactively.
- Imp kills a not-yet-acting ordinary role: skip that wake; kills Ravenkeeper or Sage: death-trigger information still occurs. Night internals cannot disclose death to players until Dawn.
- Mayor redirect or genuine registration/false-information permission stops at S-choice boundary, never silently applies a random result.
- Scarlet Woman eligibility at Demon death is based on that instant's player count; new identity is privately delivered once; no repeated steps on reordered wake list.
- Chef adjacency, Empath living-neighbor, FT red herring, Spy/Recluse, Butler master voting, Virgin nomination, Slayer, Saint, Klutz, Chambermaid actual woke status.
- Full trajectories 5/6/7/9/10/12/13/15 and setup-only counts all 5–15; NGJ five and six, especially 6-seat outsider cap. Both setup-diversity and returning-player-role-rotation paths retained.

## P1 current limits and gates

P0 map is a **file inventory with provisional dispositions**, not blanket permission to delete 227 files. A bounded isolated coding pass explored the production Imp pending-operation cutover and tests, but its presence in a separate worktree **does not mean it is in PR #335 or has CI**. A valid P1 code commit needs: true Next stage with no event, one amendable pending, commit at following actual start, safe persistent restore, AI invalidation, privacy and idempotence tests, removal of superseded live paths, full Android CI/R2. Keep the PR Draft; never force push or merge #335/#334, nor pay for model tests without approval.
