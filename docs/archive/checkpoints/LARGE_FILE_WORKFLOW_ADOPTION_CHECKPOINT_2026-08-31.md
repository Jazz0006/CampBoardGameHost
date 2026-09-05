# Large-file workflow adoption checkpoint — 2026-08-31

The repository now treats `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` as the normative detailed SOP for large/truncated-file edits.

Writer priority is now:

```text
small/medium safe file
-> GitHub connector direct edit

large/truncated file with stable unique anchors
-> GitHub Actions one-shot workflow + separate Python patch script

one-shot path cannot be made safe / genuinely requires local worktree
-> Codex/Luna
```

Normative policy commit:

```text
e30a0de4e28eed7ca0e6d3a19b578bbec87dc892
`docs: prioritize one-shot large-file patches`
```

Temporary policy workflow/script cleanup:

```text
35c2b3fb4c00a006f4327740c842f0d28cec5580
`chore(ci): remove workflow-policy one-shot files`
```

Self-validation workflow:

```text
run 33345822992 — SUCCESS
```

The run successfully verified exact branch/remote HEAD and blobs, applied the separate Python patch, passed exact diff/semantic audits, committed the policy update, and removed its own temporary workflow/script.

The detailed SOP records the previously observed TBSP-6L startup failure in which non-trivial Python with triple-quoted multiline strings was embedded directly inside YAML, breaking YAML block indentation and producing a failed workflow with `jobs=[]`. The normative fix is to keep non-trivial multiline patch logic in a separate `.github/scripts/<slice>_patch.py` file.

It also records LF/UTF-8 preservation, exact anchor `count == 1`, no absolute-line-number mutation, bot-push/cleanup CI behavior, stale-run remote-head protection, changed-file allowlists, and Luna fallback criteria.
