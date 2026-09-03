from pathlib import Path
import re

path = Path('app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt')
text = path.read_text()

pattern = re.compile(
    r'    if \(phase == ClocktowerPhase\.Day && dayMode == ClocktowerDayMode\.Klutz\) \{\n.*?\n        return\n    \}\n',
    re.S,
)
matches = list(pattern.finditer(text))
if len(matches) < 2:
    raise SystemExit(f'Expected active + legacy Klutz blocks, found {len(matches)}')

replacement = '''    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Klutz) {
        val klutzChoiceCard = cards.firstOrNull { it.name == klutzChoiceName }
        val klutzRegistrationKey = klutzChoiceCard
            ?.takeIf { it.name == spyCard?.name }
            ?.let { registrationKey("Klutz", it.name) }
        val klutzSpyRecommendations = if (klutzRegistrationKey != null && spyCard != null) {
            registrationRecommendationOptions(
                key = klutzRegistrationKey,
                roleEnName = "Klutz",
                teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                detail = ClocktowerRegistrationDetail.Role,
                subject = spyCard,
                isSpy = true,
                outcomeMisinformationPressure = 5,
                specialRegistrationBalanceImpact = -1,
            )
        } else {
            emptyList()
        }
        val automaticKlutzSpyRegistration = WeightedStableSelector.selectStyle(
            klutzSpyRecommendations,
            automaticStorytellerStyle,
            ClocktowerRegistrationRecommendationOption::style,
        )
        val klutzTableState = clocktowerKlutzTableState(
            seats = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
            ).seats,
            klutzName = pendingKlutzName,
            alivePlayerNames = publicAliveCards.mapTo(mutableSetOf()) { it.name },
            choiceName = klutzChoiceName,
        )
        ClocktowerKlutzTableScreen(
            round = round,
            tableState = klutzTableState,
            actionsEnabled = gameOutcome == null,
            onSeatClick = { seatId ->
                val playerName = klutzTableState.playerNameForSeat(seatId)
                onSelectKlutzChoice(if (klutzChoiceName == playerName) null else playerName)
            },
            onConfirm = {
                if (
                    automaticStorytellerInfo &&
                    spyCanRegister("Klutz") &&
                    klutzRegistrationKey != null &&
                    automaticKlutzSpyRegistration != null
                ) {
                    spyRegistrationGood[klutzRegistrationKey] =
                        automaticKlutzSpyRegistration.usesSpecialRegistration
                    if (automaticKlutzSpyRegistration.usesSpecialRegistration) {
                        automaticKlutzSpyRegistration.registeredRoleEnName?.let {
                            spyRegistrationRole[klutzRegistrationKey] = it
                        }
                    }
                }
                recordSpyRegistration(
                    klutzRegistrationKey,
                    listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                    "Klutz",
                )
                onConfirmKlutzChoice(spyRegistersGood(klutzRegistrationKey, "Klutz"))
            },
            specialContent = {
                if (klutzRegistrationKey != null && spyCard != null) {
                    SpyRegistrationPanel(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        automaticStorytellerStyle = automaticStorytellerStyle,
                        cards = cards,
                        spy = spyCard,
                        teams = listOf(ClocktowerTeam.Townsfolk, ClocktowerTeam.Outsider),
                        registersGood = spyRegistersGood(klutzRegistrationKey, "Klutz"),
                        registeredRoleEnName = spyRegistrationRole[klutzRegistrationKey],
                        recommendations = klutzSpyRecommendations,
                        enabled = spyCanRegister("Klutz"),
                        onRegistersGoodChange = { good ->
                            spyRegistrationGood[klutzRegistrationKey] = good
                            if (good && spyRegistrationRole[klutzRegistrationKey] == null) {
                                spyRegistrationRole[klutzRegistrationKey] = "Washerwoman"
                            }
                        },
                        onRoleChange = { spyRegistrationRole[klutzRegistrationKey] = it },
                    )
                }
            },
        )
        return
    }
'''

text = text[:matches[0].start()] + replacement + text[matches[0].end():]
path.write_text(text)
