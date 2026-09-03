from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
text = HOST.read_text()

old_record = '''    val executionThreshold = (publicAliveCards.size + 1) / 2
    fun recordCurrentVote(): String? {
        if (currentVoteCount >= executionThreshold) {
            when {
                currentVoteCount > highestVoteCount -> {
                    highestVoteName = nomineeName
                    highestVoteCount = currentVoteCount
                }
                currentVoteCount == highestVoteCount -> {
                    highestVoteName = null
                }
            }
        }
        return highestVoteName?.takeIf { highestVoteCount >= executionThreshold }
    }
    val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
'''
new_record = '''    val executionThreshold = (publicAliveCards.size + 1) / 2
    val scriptRoleNames = clocktowerRolesForScript(script).map { it.enName }.toSet()
'''

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

for label, old in (("recordCurrentVote", old_record), ("vote onConfirm", old_confirm)):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"Expected exactly one {label} anchor, found {count}")

text = text.replace(old_record, new_record)
text = text.replace(old_confirm, new_confirm)
HOST.write_text(text)
