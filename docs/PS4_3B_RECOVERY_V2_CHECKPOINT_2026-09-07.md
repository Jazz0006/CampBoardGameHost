# PS4.3b Recovery v2 — Final Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.3b COMPLETE; STOP BEFORE PS4.4**

## Product result

Recovery is now deliberately current-format v2:

```text
RecoverySnapshot.CURRENT_FORMAT_VERSION = 2
recovery-v2:Undercover
recovery-v2:Clocktower
```

Previous v1 Recovery is rejected as `UnsupportedFormat`; no migration path exists or is promised.

`LegacyRestoreCompatibility` is deleted. Current Recovery wire no longer emits or reads:

```text
version
gameContentIdentity
committedClocktowerSetup
clocktowerRulesetRoleIds
clocktowerRulesetRef
```

Trouble Brewing setup-rotation bookkeeping remains typed and durable through `ClocktowerRecovery.troubleBrewingSetupRotationRecord`.

`activeGamePersistenceCoordinator.identityForSave(...)` remains exactly once as a temporary save-time validation bridge. Its ownership/removal is a PS4.4 question, not part of PS4.3b.

Archive compatibility, 4-hour Recovery validity, atomic apply, semantic history, action timeline and epistemic observation durability are unchanged.

## Tests-first evidence

RED contract checkpoint:

```text
e4cde20f8449eed49e36ef6e0162f696f3901096
test: define Recovery v2 schema contract
```

RED evidence:

```text
CI run 34110974863
:app:testFast
1191 tests completed, 3 failed
```

All three failures were the new v2 contract assertions only: v2 authority, v1 rejection, and absence of obsolete wire metadata.

GREEN production checkpoint:

```text
58d687cc8c8082c040cf07eabcf5c1cfbd4dda15
refactor: cut Recovery over to v2 schema
```

Exact product diff:

```text
12 app source/test files
10 insertions
188 deletions
```

PS4.3b product one-shot run:

```text
34111655699
```

Product-critical stages passed:

```text
exact lineage/reference audit     PASS
schema/App patch                  PASS
post-patch wire audit             PASS
:app:compileDebugKotlin            PASS
:app:testFast                      PASS
git diff --check                  PASS
remote-head race check            PASS
product commit/push               PASS
```

The one-shot's final self-removal command hit a local modified-tool guard after the product push. Temporary PS4.3b tooling was subsequently removed through GitHub Contents API, ending at:

```text
75d112d9ff068cc4950e7c1449e1e2930eeba4da
```

No PS4.3b product-patch tooling remains in the branch tree.

## Documentation sync

Progress authority:

```text
19266c7075364e6a09187cf1a7552918939fc219
docs: record PS4.3b Recovery v2 checkpoint [full-ci]
```

Roadmap sync:

```text
d801d92cc7fd61c33a43b38d53a6e0fe54678ae1
docs: advance roadmap to PS4.4
```

Roadmap tooling cleanup:

```text
8a57586cce6798e840c0f132d753e66ca26d100c
```

`docs/CURRENT_DEVELOPMENT_ROADMAP.md` and `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md` now both identify PS4.3 as complete and PS4.4 as next/not started.

## Stop boundary

Do not begin PS4.4 without a fresh repository-wide reference/behavior audit of the ActiveGame identity/coordinator chain.

Do not begin PS5, Werewolf deletion or D6, and do not merge PR #112 without explicit user authorization.
