from pathlib import Path
from textwrap import dedent


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise AssertionError(f"{label}: expected exactly one match, got {count}")
    return text.replace(old, new, 1)


def replace_region(text: str, start_marker: str, end_marker: str, replacement: str, label: str) -> str:
    if text.count(start_marker) != 1:
        raise AssertionError(f"{label}: start marker count={text.count(start_marker)}")
    start = text.index(start_marker)
    end = text.index(end_marker, start)
    return text[:start] + replacement.rstrip() + "\n" + text[end:]


deal_path = Path("app/src/main/java/com/codex/campboardgamehost/AppDealScreens.kt")
deal = deal_path.read_text()

deal = replace_once(
    deal,
    dedent(
        """\
        internal fun PassPhoneScreen(
            playerName: String,
            gameKind: GameKind,
            current: Int,
            total: Int,
            onReveal: () -> Unit,
        ) {
        """
    ),
    dedent(
        """\
        internal fun PassPhoneScreen(
            playerName: String,
            playerNames: List<String>,
            gameKind: GameKind,
            current: Int,
            total: Int,
            onReveal: () -> Unit,
            onPrevious: () -> Unit,
            onHostTools: () -> Unit,
            onNext: () -> Unit,
        ) {
        """
    ),
    "PassPhoneScreen signature",
)

deal = replace_once(
    deal,
    dedent(
        """\
            ClocktowerDealHandoffScreen(
                playerName = playerName,
                current = current,
                total = total,
                onReveal = onReveal,
            )
        """
    ),
    dedent(
        """\
            ClocktowerDealHandoffScreen(
                playerName = playerName,
                playerNames = playerNames,
                current = current,
                total = total,
                onReveal = onReveal,
                onPrevious = onPrevious,
                onHostTools = onHostTools,
                onNext = onNext,
            )
        """
    ),
    "Clocktower controller call",
)

deal = replace_once(
    deal,
    dedent(
        """\
            ClocktowerPlayerRoleRevealScreen(
                card = card,
                current = current,
                total = total,
                onHide = onHide,
            )
        """
    ),
    dedent(
        """\
            ClocktowerPlayerRoleRevealScreen(
                card = card,
                onHide = onHide,
            )
        """
    ),
    "Clocktower player reveal call",
)

controller = dedent(
    """\
    @Composable
    private fun ClocktowerDealHandoffScreen(
        playerName: String,
        playerNames: List<String>,
        current: Int,
        total: Int,
        onReveal: () -> Unit,
        onPrevious: () -> Unit,
        onHostTools: () -> Unit,
        onNext: () -> Unit,
    ) {
        val language = LocalContext.current.resources.configuration.locales[0].language
        fun text(zh: String, en: String): String = if (language == "en") en else zh
        require(total == playerNames.size) { "Identity controller total must match the physical seat count" }
        require(current in 1..total) { "Identity controller current seat must be in range" }
        require(playerNames[current - 1] == playerName) { "Identity controller target must match the current seat" }
        val seats = playerNames.mapIndexed { index, name ->
            HostSeatPresentation(
                seatId = ClocktowerSeatId(index + 1),
                playerName = name,
                isAlive = true,
            )
        }
        val currentSeatId = ClocktowerSeatId(current)

        ClocktowerDarkTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text("身份展示", "IDENTITY REVEAL"),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            "$current / $total",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                HostTableShell(
                    seats = seats,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    interaction = HostTableInteractionState(
                        mode = HostTableInteractionMode.Sequential,
                        currentSeatId = currentSeatId,
                    ),
                ) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 68.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "$current / $total",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                            )
                            Text(
                                text(
                                    "给 ${current} 号 $playerName 展示身份",
                                    "Show the role to seat $current · $playerName",
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Button(
                                onClick = onReveal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Text(text("展示身份", "Show identity"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                    HostBottomActionBar(
                        previousLabel = text("上一步", "Previous"),
                        hostToolsLabel = text("主持工具", "Host Tools"),
                        nextLabel = text("下一步", "Next"),
                        onPrevious = onPrevious,
                        onHostTools = onHostTools,
                        onNext = onNext,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        previousEnabled = current > 1,
                    )
                }
            }
        }
    }
    """
)

reveal = dedent(
    """\
    @Composable
    private fun ClocktowerPlayerRoleRevealScreen(
        card: PlayerCard,
        onHide: () -> Unit,
    ) {
        val context = LocalContext.current
        val language = context.resources.configuration.locales[0].language
        fun text(zh: String, en: String): String = if (language == "en") en else zh
        val shownRole = card.clocktowerShownRole
        val roleName = shownRole?.nameFor(language) ?: card.roleLabel ?: stringResource(card.role.labelResId())
        val team = shownRole?.team
        val teamName = team?.label(context)
        val description = shownRole?.descriptionFor(language) ?: card.word
        val accentColor = when (team) {
            ClocktowerTeam.Townsfolk -> Color(0xFF8FB6D6)
            ClocktowerTeam.Outsider -> Color(0xFF9AAEC0)
            ClocktowerTeam.Minion -> Color(0xFFD09A6A)
            ClocktowerTeam.Demon -> Color(0xFFD96B70)
            null -> Color(0xFFC5A56A)
        }

        ClocktowerDarkTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text("仅供你查看", "FOR YOUR EYES ONLY"),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                        )
                        Text(card.name, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.48f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            teamName?.let {
                                Surface(
                                    color = accentColor.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(50),
                                ) {
                                    Text(
                                        text = it,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        color = accentColor,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                            }
                            Text(
                                roleName,
                                color = accentColor,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                            HorizontalDivider(color = accentColor.copy(alpha = 0.28f))
                            Text(
                                text("你的能力", "YOUR ABILITY"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                description,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text(
                            "记住角色和能力。隐藏页面后把手机交回说书人。",
                            "Remember your character and ability. Hide this screen, then return the phone to the Storyteller.",
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
                    Button(
                        onClick = onHide,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = text("隐藏身份，交回说书人", "Hide role and return to Storyteller"),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
    """
)

deal = replace_region(
    deal,
    "@Composable\nprivate fun ClocktowerDealHandoffScreen(",
    "@Composable\nprivate fun FullScreenColumn(",
    controller + reveal,
    "Clocktower deal/reveal region",
)
deal_path.write_text(deal)

root_path = Path("app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt")
root = root_path.read_text()
route_start = root.index("                    Screen.PassPhone -> PassPhoneScreen(")
route_end = root.index("                    Screen.ClocktowerJudge -> ClocktowerJudgeScreen(", route_start)
legacy_routes = root[route_start:route_end]
if legacy_routes.count("currentDealIndex += 1") != 1 or "GameKind.Clocktower -> Screen.ClocktowerJudge" not in legacy_routes:
    raise AssertionError("Unexpected legacy identity route shape")
new_routes = dedent(
    """\
                        Screen.PassPhone -> PassPhoneScreen(
                            playerName = cards[currentDealIndex].name,
                            playerNames = cards.map { it.name },
                            gameKind = currentGameKind,
                            current = currentDealIndex + 1,
                            total = cards.size,
                            onReveal = { screen = Screen.RevealCard },
                            onPrevious = {
                                if (currentGameKind == GameKind.Clocktower && currentDealIndex > 0) {
                                    currentDealIndex -= 1
                                }
                            },
                            onHostTools = {
                                if (currentGameKind == GameKind.Clocktower) {
                                    hostToolTab = HostToolTab.Roles
                                    showHostTools = true
                                }
                            },
                            onNext = {
                                if (currentGameKind == GameKind.Clocktower) {
                                    if (currentDealIndex == cards.lastIndex) {
                                        screen = Screen.ClocktowerJudge
                                    } else {
                                        currentDealIndex += 1
                                    }
                                }
                            },
                        )

                        Screen.RevealCard -> RevealCardScreen(
                            card = cards[currentDealIndex],
                            gameKind = currentGameKind,
                            current = currentDealIndex + 1,
                            total = cards.size,
                            onHide = {
                                when (currentGameKind) {
                                    GameKind.Werewolf -> error("Werewolf runtime has been removed.")
                                    GameKind.Clocktower -> screen = Screen.PassPhone
                                    GameKind.Undercover -> {
                                        if (currentDealIndex == cards.lastIndex) {
                                            screen = Screen.Game
                                        } else {
                                            currentDealIndex += 1
                                            screen = Screen.PassPhone
                                        }
                                    }
                                }
                            },
                        )

    """
)
# The surrounding when branch is indented by 20 spaces. dedent leaves 20 here intentionally.
root = root[:route_start] + new_routes + root[route_end:]
root = replace_once(
    root,
    dedent(
        """\
                                onHostTools = {
                                    hostToolTab = HostToolTab.Roles
                                    showHostTools = true
                                },
                                onSelectNightDeath = { selected ->
        """
    ),
    dedent(
        """\
                                onHostTools = {
                                    hostToolTab = HostToolTab.Roles
                                    showHostTools = true
                                },
                                onPreviousFromFirstNightReady = {
                                    currentDealIndex = cards.lastIndex
                                    screen = Screen.PassPhone
                                },
                                onSelectNightDeath = { selected ->
        """
    ),
    "root first-night previous callback",
)
root_path.write_text(root)

host_path = Path("app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt")
host = host_path.read_text()
host = replace_once(
    host,
    dedent(
        """\
            onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit,
            onHostTools: () -> Unit,
            onMovePreviousNightStep: () -> Unit,
        """
    ),
    dedent(
        """\
            onRecordEpistemicObservation: (EpistemicObservationDraft) -> Unit,
            onHostTools: () -> Unit,
            onPreviousFromFirstNightReady: () -> Unit,
            onMovePreviousNightStep: () -> Unit,
        """
    ),
    "ClocktowerJudgeScreen signature",
)
host = replace_once(
    host,
    dedent(
        """\
                title = text("说书人开局准备", "STORYTELLER SETUP"),
                subtitle = text("首夜裁定推荐", "First-night recommendations"),
                description = text(
                    "这是说书人私密页面。确认推荐与裁定后，直接进入首夜流程。",
                    "This is a private Storyteller screen. Review the plan, then begin the first night.",
                ),
                buttonLabel = text("确认裁定，开始首夜", "Confirm plan and begin first night"),
                onHostTools = onHostTools,
                onStartNight = {
        """
    ),
    dedent(
        """\
                title = text("身份展示完成", "IDENTITY DISPLAY COMPLETE"),
                subtitle = text("准备进入首夜", "Prepare for the first night"),
                description = text(
                    "这是说书人私密页面。可返回最后一位玩家重新展示身份，或确认首夜裁定后开始夜晚。",
                    "This is a private Storyteller screen. You may return to the final player to show the role again, or confirm the first-night rulings and begin the night.",
                ),
                buttonLabel = text("确认裁定，开始首夜", "Confirm plan and begin first night"),
                onHostTools = onHostTools,
                onPrevious = onPreviousFromFirstNightReady,
                onStartNight = {
        """
    ),
    "first-night identity-complete boundary",
)
host_path.write_text(host)

recommendation_path = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerStorytellerRecommendationUi.kt")
recommendation = recommendation_path.read_text()
recommendation = replace_once(
    recommendation,
    dedent(
        """\
            buttonLabel: String,
            onHostTools: () -> Unit,
            onStartNight: () -> Unit,
            content: @Composable () -> Unit,
        """
    ),
    dedent(
        """\
            buttonLabel: String,
            onHostTools: () -> Unit,
            onPrevious: (() -> Unit)? = null,
            onStartNight: () -> Unit,
            content: @Composable () -> Unit,
        """
    ),
    "recommendation previous parameter",
)
recommendation = replace_once(
    recommendation,
    dedent(
        """\
                        onPrevious = {},
                        onHostTools = onHostTools,
                        onNext = onStartNight,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        previousEnabled = false,
        """
    ),
    dedent(
        """\
                        onPrevious = onPrevious ?: {},
                        onHostTools = onHostTools,
                        onNext = onStartNight,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        previousEnabled = onPrevious != null,
        """
    ),
    "recommendation bottom previous",
)
recommendation_path.write_text(recommendation)

# Presentation/privacy contract audit before any test or commit.
deal = deal_path.read_text()
controller_start = deal.index("private fun ClocktowerDealHandoffScreen(")
controller_end = deal.index("@Composable\nprivate fun ClocktowerPlayerRoleRevealScreen(", controller_start)
controller_body = deal[controller_start:controller_end]
for forbidden in ("PlayerCard", "clocktowerRole", "clocktowerShownRole", "actualRole", "shownRole", "ClocktowerTeam"):
    assert forbidden not in controller_body, f"privacy leak in controller: {forbidden}"
for required in ("playerNames: List<String>", "HostTableShell(", "currentSeatId = currentSeatId", "HostBottomActionBar("):
    assert required in controller_body, f"missing controller contract: {required}"

reveal_start = deal.index("private fun ClocktowerPlayerRoleRevealScreen(")
reveal_end = deal.index("@Composable\nprivate fun FullScreenColumn(", reveal_start)
reveal_body = deal[reveal_start:reveal_end]
for forbidden in ("HostTableShell(", "HostBottomActionBar(", "onHostTools", "pass to next player", "交给下一位玩家"):
    assert forbidden not in reveal_body, f"player reveal navigation/privacy leak: {forbidden}"
assert "Hide role and return to Storyteller" in reveal_body
assert '"$current / $total"' not in reveal_body

root = root_path.read_text()
route_start = root.index("Screen.PassPhone -> PassPhoneScreen(")
route_end = root.index("Screen.ClocktowerJudge -> ClocktowerJudgeScreen(", route_start)
routes = root[route_start:route_end]
assert "playerNames = cards.map { it.name }" in routes
assert "GameKind.Clocktower -> screen = Screen.PassPhone" in routes
assert "GameKind.Clocktower -> Screen.ClocktowerJudge" not in routes
assert routes.count("currentDealIndex += 1") == 2
assert "onPreviousFromFirstNightReady" in root

host = host_path.read_text()
first_start = host.index("if (phase == ClocktowerPhase.FirstNight && !nightStarted)")
first_end = host.index("if (phase == ClocktowerPhase.Night && !nightStarted)", first_start)
first_boundary = host[first_start:first_end]
assert "onPrevious = onPreviousFromFirstNightReady" in first_boundary
assert "身份展示完成" in first_boundary and "准备进入首夜" in first_boundary
later_end = host.index("if ((phase == ClocktowerPhase.FirstNight || phase == ClocktowerPhase.Night) && nightStarted)", first_end)
later_boundary = host[first_end:later_end]
assert "onPrevious =" not in later_boundary

print("UI-NAV-1E patch + privacy contract audit PASS")
