from pathlib import Path

path = Path('app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerVirginPreflightProductionWiringTest.kt')
text = path.read_text()
old = '''        val nominationScreenFlow = hostSource
            .substringAfter("ClocktowerNominationScreen(")
            .substringBefore("onCancel =")
        val nominationPreflightIndex = nominationScreenFlow.indexOf("onPreflightVirginExecution(")
        val nominationRegistrationIndex = nominationScreenFlow.indexOf("recordSpyRegistration(")
'''
new = '''        val pendingNominationFlow = hostSource
            .substringAfter("ClocktowerPendingNominationTableScreen(")
            .substringBefore("onCancel =")
        val nominationPreflightIndex = pendingNominationFlow.indexOf("onPreflightVirginExecution(")
        val nominationRegistrationIndex = pendingNominationFlow.indexOf("recordSpyRegistration(")
'''
if text.count(old) != 1:
    raise SystemExit(f'expected one Virgin wiring anchor, found {text.count(old)}')
path.write_text(text.replace(old, new, 1))
