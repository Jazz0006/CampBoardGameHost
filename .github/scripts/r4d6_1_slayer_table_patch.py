from pathlib import Path

HOST = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
text = HOST.read_text()

start_marker = "    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Slayer) {\n"
end_marker = "\n    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Artist) {\n"
start = text.find(start_marker)
if start < 0:
    raise SystemExit("Active Slayer block start not found")
end = text.find(end_marker, start)
if end < 0:
    raise SystemExit("Active Slayer block end not found")
old_block = text[start:end]
if old_block.count("SelectablePlayerChips(") != 2:
    raise SystemExit("Expected exactly two legacy Slayer chip selectors in active block")

new_block = '''    if (phase == ClocktowerPhase.Day && dayMode == ClocktowerDayMode.Slayer) {
        val slayerTargetCard = cards.firstOrNull { it.name == slayerTargetName }
        val slayerRecluseRecommendations = slayerTargetCard
            ?.takeIf { it.clocktowerRole?.enName == "Recluse" }
            ?.let { recluse ->
                registrationRecommendationOptions(
                    key = registrationKey("SlayerRecluse", recluse.name),
                    roleEnName = "Slayer",
                    teams = listOf(ClocktowerTeam.Demon),
                    detail = ClocktowerRegistrationDetail.Role,
                    subject = recluse,
                    isSpy = false,
                    outcomeMisinformationPressure = 4,
                    specialRegistrationBalanceImpact = 1,
                )
            }
            .orEmpty()
        val automaticSlayerRecluseRegistration = WeightedStableSelector.selectStyle(
            slayerRecluseRecommendations,
            automaticStorytellerStyle,
            ClocktowerRegistrationRecommendationOption::style,
        )
        val slayerTableState = clocktowerSlayerTableState(
            seats = clocktowerDayOverviewTableState(
                cards.toClocktowerGameState(
                    script = script,
                    seed = gameSeed,
                    poisonedPlayerName = poisonTarget,
                ),
            ).seats,
            claimantCandidateNames = slayerClaimantCandidates.mapTo(mutableSetOf()) { it.name },
            alivePlayerNames = publicAliveCards.mapTo(mutableSetOf()) { it.name },
            claimantName = slayerClaimantName,
            targetName = slayerTargetName,
        )
        ClocktowerSlayerTableScreen(
            round = round,
            tableState = slayerTableState,
            actionsEnabled = gameOutcome == null,
            onSeatClick = { seatId ->
                val selectedName = slayerTableState.playerNameForSeat(seatId)
                if (slayerClaimantName == null) {
                    slayerClaimantName = selectedName
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = false
                } else {
                    slayerTargetName = if (slayerTargetName == selectedName) null else selectedName
                    slayerRecluseRegistersDemon = false
                }
            },
            onResetClaimant = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
            },
            onResolve = {
                val claimantName = slayerClaimantName
                val targetName = slayerTargetName
                if (claimantName != null && targetName != null) {
                    val targetIsHealthyRecluse =
                        slayerTargetCard?.clocktowerRole?.enName == "Recluse" &&
                            poisonTarget != targetName
                    val recluseRegistersDemon = if (
                        automaticStorytellerInfo &&
                        targetIsHealthyRecluse &&
                        automaticSlayerRecluseRegistration != null
                    ) {
                        automaticSlayerRecluseRegistration.usesSpecialRegistration
                    } else {
                        slayerRecluseRegistersDemon
                    }
                    onSlayerShot(claimantName, targetName, recluseRegistersDemon)
                    slayerClaimantName = null
                    slayerTargetName = null
                    slayerRecluseRegistersDemon = false
                    dayMode = ClocktowerDayMode.Overview
                }
            },
            onBack = {
                slayerClaimantName = null
                slayerTargetName = null
                slayerRecluseRegistersDemon = false
                dayMode = ClocktowerDayMode.Overview
            },
            specialContent = {
                if (slayerTargetCard?.clocktowerRole?.enName == "Recluse") {
                    val slayerRecluse = slayerTargetCard
                    RecluseRegistrationPanel(
                        automaticStorytellerInfo = automaticStorytellerInfo,
                        automaticStorytellerStyle = automaticStorytellerStyle,
                        cards = cards,
                        recluse = slayerRecluse,
                        teams = listOf(ClocktowerTeam.Demon),
                        registersEvil = slayerRecluseRegistersDemon,
                        registeredRoleEnName = if (slayerRecluseRegistersDemon) "Imp" else null,
                        recommendations = slayerRecluseRecommendations,
                        enabled = poisonTarget != slayerTargetName,
                        onRegistersEvilChange = { slayerRecluseRegistersDemon = it },
                        onRoleChange = {},
                    )
                }
            },
        )
        return
    }
'''

updated = text[:start] + new_block + text[end:]
active_start = updated.find(start_marker)
active_end = updated.find(end_marker, active_start)
active_block = updated[active_start:active_end]
if "ClocktowerSlayerTableScreen(" not in active_block:
    raise SystemExit("Slayer table screen was not installed")
if "SelectablePlayerChips(" in active_block:
    raise SystemExit("Legacy Slayer chip selector remains in active block")
HOST.write_text(updated)
