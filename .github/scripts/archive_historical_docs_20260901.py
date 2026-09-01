from pathlib import Path
import os

ROOT = Path('.')
DOCS = ROOT / 'docs'

MOVES = {
    'CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md': 'archive/superseded-workflows/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md',
    'LARGE_FILE_WORKFLOW_ADOPTION_CHECKPOINT_2026-08-31.md': 'archive/checkpoints/LARGE_FILE_WORKFLOW_ADOPTION_CHECKPOINT_2026-08-31.md',
    'MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md',
    'MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md',
    'MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md',
    'MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md',
    'MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md',
    'MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md',
    'MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md',
    'MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md',
    'MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md',
    'MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S6C_GENERIC_IMPAIRED_INFORMATION_REPLAN_2026-08-31.md',
    'MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_S6C_GENERIC_INFORMATION_SEMANTICS_CHECKPOINT_2026-08-31.md',
    'MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md': 'archive/completed-campaigns/ms-setup/MS_S6D_FIRST_NIGHT_PERCEIVED_ABILITY_AUDIT_2026-09-01.md',
    'MS_S7_S8_PR61_CLOSEOUT_CHECKPOINT_2026-09-01.md': 'archive/completed-campaigns/ms-setup/MS_S7_S8_PR61_CLOSEOUT_CHECKPOINT_2026-09-01.md',
    'MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md': 'archive/completed-campaigns/ms-setup/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md',
    'NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md': 'archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S6D_CLOSEOUT.md',
    'NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md': 'archive/handoffs/NEXT_DEVELOPMENT_HANDOFF_2026-09-01_MS_S7_PR_CLOSEOUT.md',
    'R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md': 'archive/superseded-designs/R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md',
    'SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md': 'archive/completed-campaigns/same-night/SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md',
    'SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md': 'archive/completed-campaigns/same-night/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md',
    'TBSP_6H_B_PRODUCTION_WIRING_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/tbsp/TBSP_6H_B_PRODUCTION_WIRING_CHECKPOINT_2026-08-31.md',
    'TBSP_6J_CLEANUP_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/tbsp/TBSP_6J_CLEANUP_CHECKPOINT_2026-08-31.md',
    'TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md': 'archive/completed-campaigns/tbsp/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md',
    'TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md': 'archive/completed-campaigns/tbsp/TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md',
    'TBSP_PR57_TEST_AUDIT_2026-08-30.md': 'archive/completed-campaigns/tbsp/TBSP_PR57_TEST_AUDIT_2026-08-30.md',
    'TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md': 'archive/completed-campaigns/tbsp/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md',
    'TBSP_ROTATION_WEIGHT_CONTRACT_V1.md': 'archive/completed-campaigns/tbsp/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md',
    'design_plan_audit_2026-08-21.md': 'archive/checkpoints/design_plan_audit_2026-08-21.md',
    'github_connector_large_file_editing_playbook.md': 'archive/superseded-workflows/github_connector_large_file_editing_playbook.md',
    'phase_a_exit_review_2026-08-20.md': 'archive/checkpoints/phase_a_exit_review_2026-08-20.md',
    'r5_5_stage_close_known_limitations_2026-08-21.md': 'archive/completed-campaigns/r5-5/r5_5_stage_close_known_limitations_2026-08-21.md',
    'storyteller_a4_5_observation_cache_rebuild_spec.md': 'archive/deferred/storyteller_a4_5_observation_cache_rebuild_spec.md',
    'storyteller_a4_zdd_prototype.md': 'archive/deferred/storyteller_a4_zdd_prototype.md',
    'storyteller_revision_driven_dynamic_decision_engine_plan.md': 'archive/superseded-designs/storyteller_revision_driven_dynamic_decision_engine_plan.md',
}

EXPECTED_ROOT_DOCS = {
    'AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md',
    'CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md',
    'CURRENT_DEVELOPMENT_ROADMAP.md',
    'CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md',
    'DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md',
    'EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md',
    'LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md',
    'NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md',
    'README.md',
    'SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md',
    'SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md',
    'SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md',
    'TESTING_STRATEGY.md',
    'asp_oracle_cross_validation.md',
    'epistemic_reference_matrix.md',
    'external_solver_evaluation.md',
    'r6_p1_2_knowledge_timeline_semantics_2026-08-21.md',
    'unified_semantic_model.md',
    '多剧本多板子与动态游戏流程架构设计_v1.md',
}

ROOT_README = '''# CampBoardGameHost 文档入口

> Updated: 2026-09-01 Australia/Sydney
> Purpose: keep the active `docs/` root small, authoritative, and safe for new development sessions.

## 1. Start here

For a new development session, read in this order:

1. root `AGENTS.md`;
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — single current project-status / execution-sequence authority;
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md) — the one ACTIVE handoff;
4. the domain-specific authority needed for the task;
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) and live GitHub state before implementation/merge.

Do not infer current work from archived handoffs, old checkpoint SHAs, or completed campaign documents.

## 2. Current campaign

Current Draft PR: **#63 — UX-R2 pair decision foundation**.

Current sequence:

```text
UX-R2A  pair semantic scenario contracts
UX-R2B  pair adoption of shared InformationDecision authority
UX-R2C  pair production vertical slice (next PR)
UX-R2D  manual-authority audit across major clue families
UX-R3/R4 remove global mode only after manual authority is complete
EPI-MQ  Productive Uncertainty / cognitive-consistency campaign
UX-R6   provider cutover behind the stable UI/decision contract
```

The global Manual front-door must not be removed until every currently supported major information family has an independent correct manual path.

## 3. Current product / algorithm authorities

- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md) — clue recommendation/manual product boundary;
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md) — Productive Uncertainty campaign;
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — Possible Worlds / epistemic foundation;
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md), [`unified_semantic_model.md`](unified_semantic_model.md), and [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md) — detailed supporting semantic references;
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md) and [`external_solver_evaluation.md`](external_solver_evaluation.md) — oracle/solver references.

## 4. Long-lived engineering authorities

- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md);
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md);
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md).

Same-night work is complete as a campaign. Keep the compact long-lived references in root:

- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md);
- [`DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`](DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md).

Detailed completed-campaign architecture is archived.

## 5. Other retained foundational architecture

- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md).

A document can remain in root because it is a durable reference even when its date is old. Age alone is not an archival criterion.

## 6. Archive policy

Historical material is under [`archive/`](archive/README.md).

Archive when a document is primarily:

- a completed campaign checkpoint/audit;
- a closed handoff;
- superseded workflow/process guidance;
- superseded implementation design;
- paused unfinished work that is explicitly deferred.

Do not delete useful history merely because it is old. Archive preserves evidence while preventing it from competing with current authorities.

A file under `archive/` is never current execution authority unless `CURRENT_DEVELOPMENT_ROADMAP.md` explicitly reactivates it.
'''

ARCHIVE_README = '''# CampBoardGameHost Historical Documentation Archive

> Historical evidence only. `PASS`, `COMPLETE`, `READY`, and `NEXT` inside this directory describe the state at the time the document was written; they are not current project status.

For current work, start with:

1. `../../AGENTS.md`;
2. [`../CURRENT_DEVELOPMENT_ROADMAP.md`](../CURRENT_DEVELOPMENT_ROADMAP.md);
3. the one active handoff named by the roadmap.

## Archive layout

```text
archive/
  handoffs/                 closed historical handoffs
  checkpoints/              one-time audits / acceptance checkpoints
  completed-campaigns/
    ms-setup/               completed multi-script setup campaign evidence
    tbsp/                   completed Trouble Brewing setup-preset campaign evidence/contracts
    same-night/             detailed completed same-night architecture/transaction documents
    r5-5/                   completed R5.5 closeout material
  superseded-workflows/     replaced development/editing workflows
  superseded-designs/       implementation plans replaced by newer authorities
  deferred/                 unfinished work that is paused, not cancelled
```

Older archive material that predates this structure may remain directly under `archive/`; it is still historical evidence.

## Rules

- Archive is not a deletion mechanism: useful design history, regression evidence, and old decisions remain available.
- Do not resume a deferred file directly. First re-query live `main`, read the current roadmap, and explicitly reactivate the work.
- If an archived document conflicts with a current authority, the current authority wins.
- Old PR numbers, SHAs, test counts, branch names, or status labels are historical snapshots only.
- When a current campaign closes, prefer moving its micro-checkpoints/handoffs here in one docs-only batch rather than letting active `docs/` accumulate stale `NEXT` instructions.
'''


def rel_from_docs_file(current_path: Path, target_path: Path) -> str:
    return Path(os.path.relpath(target_path, current_path.parent)).as_posix()


def rewrite_references(path: Path, original_root_semantics: bool) -> None:
    text = path.read_text(encoding='utf-8')
    original = text
    for source_name, dest_rel in MOVES.items():
        dest = DOCS / dest_rel
        # Repository-qualified paths are unambiguous from anywhere.
        text = text.replace(f'docs/{source_name}', f'docs/{dest_rel}')
        if original_root_semantics:
            rel = rel_from_docs_file(path, dest)
            # Root-doc markdown links to an old root file.
            text = text.replace(f']({source_name})', f']({rel})')
            text = text.replace(f'`{source_name}`', f'`{rel}`')
    if text != original:
        path.write_text(text, encoding='utf-8')


# Fail closed before moving anything.
for source_name, dest_rel in MOVES.items():
    source = DOCS / source_name
    dest = DOCS / dest_rel
    if not source.is_file():
        raise SystemExit(f'missing source before archive move: {source}')
    if dest.exists():
        raise SystemExit(f'archive destination already exists: {dest}')

# Move only the explicit allowlist.
for source_name, dest_rel in MOVES.items():
    source = DOCS / source_name
    dest = DOCS / dest_rel
    dest.parent.mkdir(parents=True, exist_ok=True)
    os.rename(source, dest)

# Replace stale docs entry points with current authority-oriented indexes.
(DOCS / 'README.md').write_text(ROOT_README, encoding='utf-8')
(DOCS / 'archive' / 'README.md').write_text(ARCHIVE_README, encoding='utf-8')

# Fix references from current root docs and root AGENTS. Historical archived files remain
# historical snapshots; only repository-qualified paths are rewritten inside them below.
for path in [ROOT / 'AGENTS.md', *sorted(DOCS.glob('*.md'))]:
    rewrite_references(path, original_root_semantics=True)

# Within the files moved in this batch, repair explicit docs/<old-path> references but do not
# rewrite their historical prose/phase language.
for dest_rel in MOVES.values():
    rewrite_references(DOCS / dest_rel, original_root_semantics=False)

# Specific AGENTS subordinate-workflow reference must point at the archived location.
agents = (ROOT / 'AGENTS.md').read_text(encoding='utf-8')
stale = 'docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md'
if stale in agents:
    raise SystemExit('AGENTS still contains stale local-patch workflow path')

# Structural audit: the active docs root must now be exactly the curated set.
actual_root_docs = {p.name for p in DOCS.glob('*.md')}
if actual_root_docs != EXPECTED_ROOT_DOCS:
    missing = sorted(EXPECTED_ROOT_DOCS - actual_root_docs)
    extra = sorted(actual_root_docs - EXPECTED_ROOT_DOCS)
    raise SystemExit(f'root docs mismatch; missing={missing}; extra={extra}')

# Every source must be gone and every destination must exist.
for source_name, dest_rel in MOVES.items():
    if (DOCS / source_name).exists():
        raise SystemExit(f'source still exists after move: {source_name}')
    if not (DOCS / dest_rel).is_file():
        raise SystemExit(f'destination missing after move: {dest_rel}')

# Root docs/AGENTS must not keep root-relative markdown links to archived basenames.
for path in [ROOT / 'AGENTS.md', *sorted(DOCS.glob('*.md'))]:
    text = path.read_text(encoding='utf-8')
    for source_name in MOVES:
        if f']({source_name})' in text or f'docs/{source_name}' in text:
            raise SystemExit(f'stale archived-doc reference in {path}: {source_name}')

# The one active handoff must remain at docs root.
active_handoff = DOCS / 'NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md'
if not active_handoff.is_file():
    raise SystemExit('active UX-R2 handoff was not preserved at docs root')

print(f'Archived {len(MOVES)} historical/deferred documents.')
print(f'Active/foundational docs root now contains {len(EXPECTED_ROOT_DOCS)} Markdown files.')
