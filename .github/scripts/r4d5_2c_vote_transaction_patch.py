from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
text = HOST.read_text()

old_confirm = '''            onConfirm = { voteRecord, confirmedGhostVoteAuthority ->
                onGhostVoteAuthorityChange(confirmedGhostVoteAuthority)
                currentVoteCount = voteRecord.voteCount
                recordVoteEvent(voteRecord)
                recordCurrentVote()
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
'''
new_confirm = '''            onConfirm = { voteState ->
                val voteTransaction = commitClocktowerVoteTransaction(
                    voteState = voteState,
                    nomineeName = requireNotNull(nomineeName) { "Confirmed vote requires nominee" },
                    executionThreshold = executionThreshold,
                    highestVoteName = highestVoteName,
                    highestVoteCount = highestVoteCount,
                )
                onGhostVoteAuthorityChange(voteTransaction.ghostVoteAuthority)
                highestVoteName = voteTransaction.highestVoteName
                highestVoteCount = voteTransaction.highestVoteCount
                recordVoteEvent(voteTransaction.voteRecord)
                nominatorName = null
                nomineeName = null
                currentVoteCount = 0
                dayMode = ClocktowerDayMode.Overview
            },
'''

count = text.count(old_confirm)
if count != 1:
    raise SystemExit(f"Expected exactly one persistent vote onConfirm anchor, found {count}")

text = text.replace(old_confirm, new_confirm)
HOST.write_text(text)
