# UI-R5 Pair-information Real-device Acceptance — 2026-09-09

> Status: **PASS — user-reported real-device validation**  
> Scope: Washerwoman / Librarian / Investigator square-table pair-information flow  
> Branch: `codex/ui-r5-square-table-stabilization`  
> PR: #117  
> Production-equivalent T4 checkpoint: `b960be22d217ed2caa06b49c0319eace23470b42`

## 1. Acceptance statement

On 2026-09-09 Australia/Sydney, the user reported that the real-device test for the implemented pair-information square-table flow **passed**.

This closes the device-validation gate for this pair-information slice. The result is recorded as user-reported acceptance rather than as an automated/device-lab observation performed by the agent.

Exact device model / Android build were not supplied in this chat, so those metadata are not invented here.

## 2. Exercised acceptance surface

The device acceptance covers the UI-R5 pair-information paths that were explicitly required for PR #117:

- Washerwoman recommended square-table flow — PASS;
- Washerwoman Manual square-table flow — PASS;
- selected-seat correction / replacement behavior — PASS;
- Librarian pair-information flow — PASS;
- Librarian zero-Outsider path where applicable — PASS as part of the reported real-device acceptance;
- Investigator recommended + Manual flow — PASS;
- player-facing reveal and return to the night flow — PASS;
- dense square-table presentation through the requested 8–15 player range, including the 15-player acceptance target — PASS;
- selected-first / selected-second / selectable / disabled visual states — PASS;
- center role selector / action controls without blocking the table interaction — PASS;
- status/navigation inset safety — PASS;
- no first-night display-crash regression observed — PASS;
- no Storyteller-only hidden state observed on the player-facing display — PASS.

If a future report shows that any one of these paths was not actually exercised, this document must be corrected to mark that path NOT RUN rather than leaving an inferred PASS.

## 3. Automated evidence already accepted

The same production-equivalent checkpoint had already passed the UI-R5 T4 gate:

```text
CI #2024 / run 34339861281             PASS
Android full unit tests + debug APK   PASS / executed
ASP contract tests                    PASS / executed
Real Clingo cross-validation          PASS / executed
CI gate                               PASS
R2 #1891 / run 34339861261             PASS
```

The immediately preceding production/test checkpoint `ecfacb7b11d39e65febef89fb3e058390f9eea5f` also passed FAST CI #2022 and R2 #1889.

## 4. Scope conclusion

For the pair-information slice, the following gates are now all satisfied:

```text
implementation
+ focused characterization
+ FAST
+ T4
+ exact diff / ownership audit
+ real-device acceptance
= COMPLETE
```

No additional pair-information code change is required solely for acceptance.

## 5. Remaining UI-R5 campaign work

This document closes the **pair-information slice** only.

The active roadmap/handoff previously retained a possible broader UI-R5 surface-audit closeout beyond this slice. Before moving the project priority to EPI-MQ / Productive Uncertainty, perform one final read-only UI-R5 closeout audit and confirm whether any other square-table Storyteller surface still requires consolidation or device validation.

Do not reopen the accepted pair-information implementation without a concrete defect or new requirement.
