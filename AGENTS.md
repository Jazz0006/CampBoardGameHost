# CampBoardGameHost AI Development Instructions

## Current authority (2026-10-18)

**Read first:** docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md, docs/CURRENT_DEVELOPMENT_ROADMAP.md, docs/NEXT_DEVELOPMENT_HANDOFF.md and docs/TESTING_STRATEGY.md.

**Owner correction 2026-10-18 (supersedes earlier AI retirement plan):** The cleanup target is excessive **Host-owned gameplay-rule legality checking**, NOT the already developed AI storyteller recommendation product. Restore any AI recommendation path accidentally disabled by B3 before resuming Host Rules cleanup. Never delete working AI functionality merely because it uses an old candidate adapter; isolate such adapters from Host fact-commit gates, and replace only when an equivalent working AI path is verified.

The user approved one three-module architecture with the following ownership:

- Host Core: the sole authoritative game state and ordered structured key-event log, with necessary manual game operations, atomic commits, deduplication, stale-response rejection, confidentiality and basic safe continuation. Android, independent WebHost and EvidenceLab must eventually share its data semantics. Host must not calculate clue correctness or reject gameplay facts by rule legality.
- Rules: a separately rebuilt **read-only** legal-information-query service over Host state. **Delete existing Host legality gates, Host-generated rule verdicts, witness inference and excessive duplicate rule calculators.** Rules cannot commit facts or block user-confirmed actual results. Temporary AI-only candidate production may remain until replaced without disabling AI.
- AI Clues: **preserve** live opening and later-night recommendations, prompts, real API communication, response parsing, joint planning, strategic memory, preselection, and AI_ASSISTED/AI_AUTOMATIC behavior. Clean genuine dead experiments or duplicated inference only with verified non-impact on these paths. AI results remain recommendations, never authoritative Host facts before confirmation. No unapproved paid API calls.

**Corrected phases:** R0 repair B3 AI pair recommendation and validate; P1 delete Host-side Rules legality enforcement without disabling AI; P2 selectively retire proven-unused experiments/duplicates (never wholesale AI); P3 converge one Host state and one event log; P4 responsibility-led UI reduction and **every source file <=50,000 bytes**; P5 full audit/cross-project contract and build a new independent read-only Rules service as required while continuing to evolve, not recreate, AI.

Phase-one manual information entry must accept any **structurally representable** confirmed value, independent of gameplay-rule legality and old Host candidate menus. Convenience menus for pair, Chef and Empath belong only to the later re-created Rules module. Do not reintroduce their old algorithms during reduction.

## Every-file review and deletion policy

**Full-tree-first is the required primary workflow (owner reiterated 2026-10-18).** Read the untruncated real Git tree, classify **all** source modules horizontally by Host fact/security, active AI, obsolete Host Rules, dead experimentation, and undecided product scope. Record conclusions in the existing full-source audit ledger; do not traverse one legacy call path and mistake that for a full repository audit. Verify consumers of each deletion candidate before actually deleting it. For each module decide:
1. necessary Host state/event/operation, transport/security or platform adaptation: retain minimal responsibility;
2. Host-owned gameplay validity engine, proven-unused experiment, dead feature or duplicate state/history: delete the implementation and its exclusive tests; preserve AI recommendation producers/consumers and strategic logic;
3. mixed files: first disconnect retired consumers, delete obsolete responsibilities, then keep only narrow real ownership;
4. uncertainty: prepare a short user-review item with evidence, deletion risk and recommendation. Favor clean deletion followed by eventual greenfield replacement over long-lived compatibility wrappers.

Do not treat obsolete tests as proof that Host legality checks must remain. **Do treat production AI recommendation consumers as features to protect**, not collateral deletions; preserve their functionality while replacing Host-only rule barriers. Never fabricate facts from legal outcomes, e.g. a shown result does not imply an unchosen Spy/Recluse registration.

No independent Recovery, parallel historical archive, second textual replay, duplicate writable GameState, generalized Context/Manager facade, artificial one-function-per-file extraction, mechanical fix of source-string tests, or reintroduction of old legality validators. UI transient state is not canonical game truth. The final <=50,000-byte size is a **hard requirement**, not a guideline; prioritize code removal, then cohesive responsibility extraction. A large intact Host file cannot be grandfathered under older growth policies.

## Operational workflow and tools

- The user's explicit latest instructions override older plans. Current authority is this file and the dated Host/Rules/AI plan, then the current roadmap/handoff and testing strategy; Git and source are factual implementation evidence. Historical/archive documents are not new task instructions.
- Inspect live GitHub main, base/PR and actual HEAD, CI/R2 status, Mini MCP branch/HEAD/worktree before changes. Remote GitHub is the canonical state. Current campaign uses stacked Draft PR #330–#334, with #334 on codex/minimal-host-full-scan-20261018. Its current HEAD must be rechecked every session, not copied from memory.
- GitHub Connector is the normal repository read/edit/branch/PR/CI tool. Use Mini MCP repository alias clocktower and Codex CLI only when full large-file local context or reliable local changes are necessary. Inspect exact current branch, expected HEAD and clean status before branch/materialization, then audit the committed remote diff and CI independently through GitHub. Do not assume Mini MCP local branch already equals the latest remote.
- Define the targeted module, all relevant production consumers and affected canonical facts before deletion. Prefer sizeable complete deletion slices over many shallow adapters, but don't combine unrelated changes when that obscures validation. Do not let a mechanical subagent redesign semantics without Chat-level/user authority.
- When removal exposes dependencies, decide whether each dependency belongs to the sole Host fact/operation/safety surface. If not, delete it too. If yes, keep only a narrow typed input/confirmed result and existing Session commit.
- Retain a single structured chronological fact log and exact observed information with proper recipient scope. One committed event describes what happened; never record two events distinguished only by whether AI or human suggested the action. Protect stable seat identity, event chronology, permission/privacy and stale asynchronous response boundaries.

## Evidence-first validation (not mandatory TDD)

Default to evidence-first, risk-based proof. No ritual RED before every source edit and no test created solely for an implementation shape or trivial intermediate change. Keep high-value tests for one-writer Session, event sequence/identity, atomicity, duplicate prevention, receiver confidentiality, stale AI request isolation, basic setup/action/manual game flow and minimum continuity.

Tests of **retired Host rule-gating correctness** may be removed or narrowed. Keep meaningful tests of AI opening/pair/later-night provider contracts, strategy continuity, response acceptance, preselection, model transport and security; delete only genuinely orphaned experimental tests. Update obsolete architecture guards and CI routing; do not resurrect intentionally removed features to satisfy yesterday's test. The surviving repo source of test task execution is the current Gradle and GitHub workflow definitions, not historical test counts.

- Local/focused Android validation: ./gradlew :app:testFast
- Full checkpoint: ./gradlew :app:testFull :app:assembleDebug, or actual GitHub CI [full-ci] checkpoint as provided by .github/workflows/ci.yml.
- Relevant ASP/Clingo tests remain meaningful only for active contracts; retire obsolete oracle gates when their protected engine is deliberately removed.
- Always check actual CI/R2 for the **exact code HEAD**, distinguishing docs-only success from Android/real compiler success. If failed, inspect job logs and repair code or retire obsolete contract tests. Never claim unrun device or paid API validation.

Every batch must report: deleted production files and exclusive tests, removed producer/consumer chain, actual net code shrinkage, remaining oversized source files, Host state/event risks, CI/R2 IDs/verdict, and unknown owner decisions. No vague “all rules removed” claim unless actual production references are audited.

## PR, security and governance

Keep the current stacked cleanup branches as Draft until the user authorizes merge/retarget. A successful CI is not permission to merge incomplete/cross-scope work. No force push, destructive branch cleanup or unrelated working-tree staging without explicit user approval. Paid OpenAI API experimentation needs explicit budget authorization. Never expose credentials or private game-role data in public logs or player screens.

N3150 Android builder is not the default; GitHub CI/R2 is current independent Android acceptance. Android Studio/ADB real-device acceptance is separately planned after major deletions. Mini MCP developer memory is optional and advisory; versioned authority and verified source/PR state override it.

## Current maintained documents

- docs/HOST_RULES_AI_REBUILD_AND_SOURCE_PURGE_2026-10-18.md — corrected architecture: Host Rules reduction while preserving existing AI.
- docs/CURRENT_DEVELOPMENT_ROADMAP.md — sole live priority/status, no stacked old NEXTs.
- docs/NEXT_DEVELOPMENT_HANDOFF.md — sole live next-batch action and exact tested baseline.
- docs/MINIMAL_HOST_PRODUCT_CONTRACT_2026-10-17.md — current minimum Host fact contract, revised 2026-10-18.
- docs/MINIMAL_HOST_ALL_SOURCE_FILE_INDEX_2026-10-18.tsv — historical base inventory, not a finished semantic audit.
- docs/TESTING_STRATEGY.md — retained testing/CI acceptance rules.

Older architecture, recovery, rules/domain and AI-prompts notes remain historical reference only where they do not conflict with the current authority.
