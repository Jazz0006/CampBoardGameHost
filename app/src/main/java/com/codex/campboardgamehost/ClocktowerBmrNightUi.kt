package com.codex.campboardgamehost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.unit.dp
import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.flow.ClocktowerHostInteractionKind
import com.codex.campboardgamehost.clocktower.session.*

private data class BmrPlayerPreview(val proposition: InformationProposition, val action: BmrHostAction)

/** BMR controls on the existing table and private display; all rules/confirmation live in Session. */
@Composable
internal fun ClocktowerBmrNightControls(state: ClocktowerSessionState, ruleset: ValidatedClocktowerRuleset,
    onHostTools: () -> Unit, onAction: (BmrHostAction) -> Unit) {
    val language = LocalContext.current.resources.configuration.locales[0].language
    fun t(zh: String, en: String) = if(language == "en") en else zh
    fun roleName(role: RoleId) = clocktowerRoleLabel(role, language)
    fun seat(n: Int) = "$n · ${state.gameState.playerAt(n)?.name.orEmpty()}"
    val night = ClocktowerNightFacts(state, ruleset)
    val step = state.nextBmrNightInteraction(ruleset)
    key(state.gameId, state.phase, state.round, state.gameStateRevision, state.playerInputRevision, step?.id) {
        var targets by remember { mutableStateOf(emptyList<Int>()) }
        var chosenRole by remember { mutableStateOf<RoleId?>(null) }
        var bluffs by remember { mutableStateOf(emptyList<RoleId>()) }
        var use by remember { mutableStateOf<Boolean?>(null) }
        var charge by remember { mutableStateOf(false) }
        var declinedRevival by remember { mutableStateOf(false) }
        var drunkSeat by remember { mutableStateOf<Int?>(null) }
        var number by remember { mutableStateOf<Int?>(null) }
        var shownSeat by remember { mutableStateOf<Int?>(null) }
        var shownRole by remember { mutableStateOf<RoleId?>(null) }
        var deaths by remember { mutableStateOf(emptyMap<Int, Boolean>()) }
        var grandmotherDies by remember { mutableStateOf<Boolean?>(null) }
        var preview by remember { mutableStateOf<BmrPlayerPreview?>(null) }
        var showing by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf(false) }
        fun accept(action: BmrHostAction) { runCatching { onAction(action) }.onFailure { error = true } }
        val role = step?.roleId?.value
        val id = step?.id?.value.orEmpty()
        val actor = step?.actorSeat?.let(state.gameState::playerAt)
        val fake = actor?.actualRole?.value == "Lunatic"
        val lunaticInfo = id.startsWith("bmr:lunatic-info:")
        val lunaticNotice = id.startsWith("bmr:lunatic-identity:") || id.startsWith("bmr:lunatic-targets:")
        val minionInfo = step?.kind == ClocktowerHostInteractionKind.EVIL_INFORMATION && id.endsWith("minion_info")
        val demonOpeningInfo = step?.kind == ClocktowerHostInteractionKind.EVIL_INFORMATION && id.endsWith("demon_info")
        val demonInformation = demonOpeningInfo || lunaticNotice
        val godfatherInfo = id.startsWith("bmr:info:Godfather:")
        val godfatherAction = role == "Godfather" && step?.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION
        val chambermaid = role == "Chambermaid"
        val sailor = role == "Sailor"
        val courtier = role == "Courtier"
        val effectiveUse = if (courtier) chosenRole != null else use
        val selection = step?.let { state.bmrNightSelection(it, targets, effectiveUse, charge, chosenRole) }
        val exorcised = selection?.exorcised == true
        val grandchildSetup = id.startsWith("bmr:grandchild:")
        val info = selection?.information == true
        val regurgitate = id.startsWith("bmr:regurgitate:")
        val pukkaChoiceId = selection?.pukkaChoiceId
        val pukkaSettlement = pukkaChoiceId != null
        val passive = id.startsWith("bmr:gossip:") || id.startsWith("bmr:moonchild:")
        val optional = selection?.optionalUse == true
        val maximum = selection?.maximumTargets ?: 0
        val selectable = selection?.selectableSeats.orEmpty()
        val prop = if(step != null && selection != null) state.bmrInformationPreview(step, selection, targets, bluffs, number, shownSeat, shownRole) else null
        val dyingSeats = selection?.deathSeats.orEmpty()
        fun deathResult(seat: Int): Boolean? = if(seat in selection?.forcedNoDeathSeats.orEmpty()) false else deaths[seat]
        val selectedReady = when {
            step == null || exorcised || info -> true
            regurgitate -> declinedRevival || targets.size == 1
            optional && effectiveUse == null -> false
            role == "Po" && selection?.chargeAllowed == true && charge -> true
            else -> selection?.targetsValid(targets) == true
        }
        val needsDrunk = selection?.drunkSeats?.isNotEmpty() == true
        val resolvedDrunkSeat = if (sailor) selection?.drunkSeats?.singleOrNull() ?: drunkSeat else drunkSeat
        val gm = dyingSeats.firstOrNull { deathResult(it) == true }?.let { selection?.grandmothers?.get(it) }
        val gmResult = gm?.let { if(it.forcedNoDeath) false else grandmotherDies }
        val ready = if(info) preview != null else selectedReady && dyingSeats.all { deathResult(it) != null } &&
            (!needsDrunk || resolvedDrunkSeat != null) && (selection?.roleRequired != true || chosenRole != null) && (gm == null || gmResult != null)
        fun nextAction(): BmrHostAction? = when {
            step == null -> BmrHostAction.Dawn
            info -> preview?.action
            exorcised -> BmrHostAction.Exorcised(id)
            grandchildSetup -> BmrHostAction.Grandchild(id, targets.single())
            regurgitate -> BmrHostAction.Regurgitate(id, targets.singleOrNull())
            pukkaChoiceId != null -> BmrHostAction.PukkaSettlement(pukkaChoiceId, requireNotNull(deathResult(dyingSeats.single())), gmResult)
            passive -> BmrHostAction.Passive(id, selection?.fixedTarget ?: targets.singleOrNull(), dyingSeats.singleOrNull()?.let { requireNotNull(deathResult(it)) } ?: false)
            role == "Pukka" && !fake -> BmrHostAction.PukkaChoice(targets.single())
            else -> BmrHostAction.Choice(BmrNightChoice(id, targets, if(optional && effectiveUse == false) null else chosenRole,
                if(optional) effectiveUse == true else true, resolvedDrunkSeat, dyingSeats.map { requireNotNull(deathResult(it)) }, gmResult))
        }
        if(showing && preview != null) {
            val shown = requireNotNull(preview).proposition
            val displayStep = if (minionInfo) {
                val demon = (shown as InformationProposition.AllOf).propositions
                    .filterIsInstance<InformationProposition.CharacterTypeAt>()
                    .single { it.characterType == CharacterType.DEMON }
                val name = state.gameState.playerAt(demon.seat)?.name.orEmpty()
                val label = "#${demon.seat} $name"
                ClocktowerNightStepUi(
                    title = stringResource(R.string.clocktower_first_night_minion_title),
                    actor = null, isRealAction = true, reason = "", storytellerAction = "",
                    tellPlayer = stringResource(R.string.clocktower_first_night_minion_info_format, label),
                    explanation = "", displayKind = ClocktowerDisplayKind.EvilInfo,
                    displayTitle = stringResource(R.string.clocktower_first_night_minion_title),
                    displayPrimary = "${stringResource(R.string.clocktower_evil_display_demon)}\n$label",
                )
            } else if (lunaticInfo || demonOpeningInfo) {
                val parts = (shown as InformationProposition.AllOf).propositions
                val minions = parts.filterIsInstance<InformationProposition.CharacterTypeAt>()
                val roles = parts.filterIsInstance<InformationProposition.RoleInPlay>()
                val minionLabels = minions.joinToString("\n") { "#${it.seat} ${state.gameState.playerAt(it.seat)?.name.orEmpty()}" }
                    .ifEmpty { stringResource(R.string.clocktower_first_night_demon_no_minions) }
                ClocktowerNightStepUi(
                    title = stringResource(R.string.clocktower_first_night_demon_title),
                    actor = null, isRealAction = true, reason = "", storytellerAction = "",
                    tellPlayer = bmrPlayerInformationText(shown, state, language), explanation = "",
                    displayKind = ClocktowerDisplayKind.EvilInfo,
                    displayTitle = stringResource(R.string.clocktower_first_night_demon_title),
                    displayPrimary = "${stringResource(R.string.clocktower_evil_display_minions)}\n$minionLabels",
                    displaySecondary = "${stringResource(R.string.clocktower_evil_display_bluffs)}\n${roles.joinToString("\n") { roleName(it.role) }}",
                )
            } else if (chambermaid) {
                val result = shown as InformationProposition.NumericResult
                val queried = result.subjectSeats.joinToString(" + ") { n -> "#$n ${state.gameState.playerAt(n)?.name.orEmpty()}" }
                ClocktowerNightStepUi(
                    title = t("侍女信息", "Chambermaid information"),
                    actor = null, isRealAction = true, reason = "", storytellerAction = "",
                    tellPlayer = result.value.toString(), explanation = "",
                    displayKind = ClocktowerDisplayKind.Number,
                    displayTitle = t("侍女信息", "Chambermaid information"),
                    displayPrimary = result.value.toString(),
                    displaySecondary = t("查询玩家：\n$queried", "Queried players:\n$queried"),
                )
            } else if (godfatherInfo) {
                val outsiders = (shown as InformationProposition.CharactersInPlay).roles
                    .joinToString("\n") { roleName(it) }.ifEmpty { t("无外来者", "No Outsiders") }
                ClocktowerNightStepUi(
                    title = t("教父信息", "Godfather information"),
                    actor = null, isRealAction = true, reason = "", storytellerAction = "",
                    tellPlayer = bmrPlayerInformationText(shown, state, language), explanation = "",
                    displayKind = ClocktowerDisplayKind.EvilInfo,
                    displayTitle = t("教父信息", "Godfather information"),
                    displayPrimary = "${t("本局在场的外来者", "Outsiders in play")}\n$outsiders",
                )
            } else if (id.startsWith("bmr:lunatic-identity:")) {
                val identities = (shown as InformationProposition.AllOf).propositions
                    .filterIsInstance<InformationProposition.RoleAt>()
                val demon = identities.single { it.seat == actor?.seat }
                val lunatic = identities.single { it.role == RoleId("Lunatic") }
                val label = "#${lunatic.seat} ${state.gameState.playerAt(lunatic.seat)?.name.orEmpty()}"
                ClocktowerNightStepUi(
                    title = stringResource(R.string.clocktower_first_night_demon_title),
                    actor = null, isRealAction = true, reason = "", storytellerAction = "",
                    tellPlayer = bmrPlayerInformationText(shown, state, language), explanation = "",
                    displayKind = ClocktowerDisplayKind.EvilInfo,
                    displayTitle = stringResource(R.string.clocktower_first_night_demon_title),
                    displayPrimary = "${t("你是", "You are")}\n${roleName(demon.role)}",
                    displaySecondary = "${t("疯子", "Lunatic")}\n$label",
                )
            } else if (id.startsWith("bmr:lunatic-targets:")) {
                val choice = shown as InformationProposition.PlayerTargets
                val label = "#${choice.actorSeat} ${state.gameState.playerAt(choice.actorSeat)?.name.orEmpty()}"
                val chosen = choice.targetSeats.mapIndexed { index, n ->
                    val name = state.gameState.playerAt(n)?.name.orEmpty()
                    if (choice.targetSeats.size > 1) "${index + 1}. #$n $name" else "#$n $name"
                }.joinToString("\n").ifEmpty { t("本夜未选择目标", "No targets chosen tonight") }
                ClocktowerNightStepUi(
                    title = stringResource(R.string.clocktower_first_night_demon_title),
                    actor = null, isRealAction = true, reason = "", storytellerAction = "",
                    tellPlayer = bmrPlayerInformationText(shown, state, language), explanation = "",
                    displayKind = ClocktowerDisplayKind.EvilInfo,
                    displayTitle = stringResource(R.string.clocktower_first_night_demon_title),
                    displayPrimary = "${t("疯子", "Lunatic")}\n$label",
                    displaySecondary = "${t("疯子本夜选择的目标", "Lunatic's chosen targets tonight")}\n$chosen",
                )
            } else {
                val text = bmrPlayerInformationText(shown, state, language)
                ClocktowerNightStepUi(t("信息", "Information"), null, true, "", "", text, "",
                    displayKind = ClocktowerDisplayKind.Plain, displayPrimary = text)
            }
            ClocktowerPlayerDisplayCardLocalized(displayStep, emptyList()) { showing = false }
        } else if (minionInfo || demonInformation || godfatherInfo || courtier) ClocktowerDarkTheme {
            ClocktowerEvilInfoSquareTableDialog(
                seats = state.gameState.toHostSeatPresentations { roleName(it) },
                presentation = ClocktowerEvilInfoSquareTablePresentation(
                    actorSeat = actor?.seat,
                    wakeInstruction = when {
                        minionInfo -> t("唤醒 爪牙", "Wake Minions")
                        godfatherInfo -> t("唤醒 教父", "Wake Godfather")
                        courtier -> t("唤醒 侍臣", "Wake Courtier")
                        else -> t("唤醒 恶魔", "Wake Demon")
                    },
                    title = "", primary = null, secondary = null, footer = null,
                    showPlayerDisplayAction = !courtier && prop != null,
                    showHostDetails = false,
                    highlightedSeats = if (minionInfo) night.minionSeats.toSet() else emptySet(),
                    groupLines = if (minionInfo) night.minionSeats.map { n ->
                        val name = state.gameState.playerAt(n)?.name.orEmpty()
                        t("${n}号 $name", "P$n $name")
                    } else if (godfatherInfo || courtier) listOf(
                        t("${actor?.seat}号 ${actor?.name.orEmpty()}", "P${actor?.seat} ${actor?.name.orEmpty()}"),
                    ) else listOf(
                        t("${actor?.seat}号 ${actor?.name.orEmpty()}", "P${actor?.seat} ${actor?.name.orEmpty()}"),
                        actor?.actualRole?.let(::roleName).orEmpty(),
                    ),
                    hostInstruction = when {
                        courtier -> t("询问其本夜是否使用能力；若使用，让其选择一个角色。", "Ask whether they use their ability tonight; if so, have them choose a character.")
                        godfatherInfo && actor?.impaired == true -> t("选择本次向其展示的外来者角色；不选表示无外来者。选好后展示给玩家。", "Choose the Outsider characters to show; an empty selection means no Outsiders. Then show the information to the player.")
                        godfatherInfo -> t("向其展示本局在场的外来者角色。", "Show the Outsider characters in play.")
                        demonOpeningInfo -> t("向其展示爪牙和伪装角色。", "Show their Minions and bluff characters.")
                        id.startsWith("bmr:lunatic-identity:") -> t("告知其真实恶魔身份，以及疯子是谁。", "Show their real Demon identity and who the Lunatic is.")
                        id.startsWith("bmr:lunatic-targets:") -> t("向其展示疯子本夜选择的目标。", "Show the Lunatic's chosen targets tonight.")
                        else -> null
                    },
                ),
                language = language,
                onHostTools = onHostTools,
                onNext = { if (ready) nextAction()?.let(::accept) },
                onShowPlayerDisplay = {
                    prop?.let { p ->
                        val action = if (step?.roleId == null) BmrHostAction.EvilInformation(id)
                            else BmrHostAction.Information(id, state.bmrInformationDraft(requireNotNull(step), p))
                        preview = BmrPlayerPreview(p, action)
                        showing = true
                    }
                },
                nextEnabled = ready,
                hostControls = {
                    if (courtier) {
                        val roles = badMoonRisingRoles.filter { RoleId(it.enName) in selection?.allowedRoles.orEmpty() }
                        BmrRolePicker(t("选择角色", "Choose a character"), chosenRole, roles, language) { chosenRole = it }
                        if (chosenRole == null) Text(t("本夜不使用能力", "Do not use the ability tonight"))
                        else {
                            Text(t("本夜使用能力：${chosenRole?.let(::roleName)}", "Use the ability tonight: ${chosenRole?.let(::roleName)}"))
                            TextButton(onClick = { chosenRole = null }) { Text(t("清除选择", "Clear selection")) }
                        }
                    }
                    if (godfatherInfo && actor?.impaired == true) {
                        val candidates = badMoonRisingRoles.filter { RoleId(it.enName) in selection?.displayRoles.orEmpty() }
                        BmrRolePicker(t("选择展示的外来者", "Choose shown Outsiders"), null, candidates, language) { r ->
                            bluffs = if (r in bluffs) bluffs - r else if (bluffs.size < (selection?.displayRolesMaximum ?: 0)) bluffs + r else bluffs
                        }
                        if (bluffs.isEmpty()) Text(t("展示：无外来者", "Showing: no Outsiders"))
                        bluffs.forEach { r -> TextButton(onClick = { bluffs = bluffs - r }) { Text("× " + roleName(r)) } }
                    }
                },
            )
            if (error) AlertDialog(
                onDismissRequest = { error = false },
                text = { Text(t("结果与当前状态不一致，请核对后重试。", "Check the selected result against the current state.")) },
                confirmButton = { TextButton(onClick = { error = false }) { Text(t("返回", "Back")) } },
            )
        } else if (sailor) ClocktowerDarkTheme {
            ClocktowerNightActionSquareTableDialog(
                seats = state.gameState.toHostSeatPresentations { roleName(it) },
                enabled = true, language = language,
                seatPresentation = { n -> clocktowerSingleTargetSeatPresentation(n, actor?.seat, targets.singleOrNull(), selectable) },
                onSeatSelected = { n ->
                    if (n in selectable) {
                        targets = if (targets.singleOrNull() == n) emptyList() else listOf(n)
                        drunkSeat = null
                    }
                },
                onHostTools = onHostTools,
                onNext = { if (ready) nextAction()?.let(::accept) },
                nextEnabled = ready,
            ) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                ) {
                    ClocktowerNightActionWakeInstruction(t(
                        "唤醒 水手\n${actor?.seat}号 ${actor?.name.orEmpty()}\n让其选择一名存活玩家，可以选择自己。",
                        "Wake Sailor\nP${actor?.seat} ${actor?.name.orEmpty()}\nHave them choose one living player, including themself.",
                    ))
                    targets.singleOrNull()?.let { target ->
                        Text(t("已选择：${seat(target)}", "Selected: ${seat(target)}"))
                        val drunkOptions = selection?.drunkSeats.orEmpty()
                        when {
                            drunkOptions.isEmpty() -> Text(t("主持人提示：本次能力无法生效；仅记录玩家选择，不新增醉酒效果。", "Storyteller: this ability cannot take effect; record the player's choice without adding drunkenness."))
                            drunkOptions.size == 1 -> Text(t("水手选择了自己，将醉酒至黄昏。", "The Sailor chose themself and will be drunk until dusk."))
                            else -> {
                                Text(t("示意水手闭眼，再决定谁醉酒。", "Put the Sailor to sleep, then decide who is drunk."))
                                Text(t("谁醉酒至黄昏？", "Who is drunk until dusk?"))
                                Column(Modifier.fillMaxWidth().selectableGroup()) {
                                    listOfNotNull(actor?.seat, target).distinct().filter { it in drunkOptions }.forEach { n ->
                                        val player = requireNotNull(state.gameState.playerAt(n))
                                        val subject = if (n == actor?.seat) t("水手本人", "Sailor") else t("所选玩家", "Chosen player")
                                        Row(
                                            Modifier.fillMaxWidth().selectable(
                                                selected = drunkSeat == n,
                                                role = SemanticsRole.RadioButton,
                                                onClick = { drunkSeat = n },
                                            ).padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RadioButton(selected = drunkSeat == n, onClick = null)
                                            Text("$subject: #$n ${player.name} · ${roleName(player.actualRole)}", modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (error) Text(t("结果与当前状态不一致，请核对后重试。", "Check the selected result against the current state."), color = MaterialTheme.colorScheme.error)
                }
            }
        } else if (chambermaid) ClocktowerDarkTheme {
            ClocktowerNightActionSquareTableDialog(
                seats = state.gameState.toHostSeatPresentations { roleName(it) },
                enabled = true,
                language = language,
                seatPresentation = { n -> clocktowerTwoTargetSeatPresentation(n, actor?.seat, targets, selectable) },
                onSeatSelected = { n ->
                    if (n in selectable) {
                        targets = if (n in targets) targets - n else if (targets.size < maximum) targets + n else listOf(n)
                        number = null
                    }
                },
                onHostTools = onHostTools,
                onNext = { if (ready) nextAction()?.let(::accept) },
                nextEnabled = ready,
            ) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                ) {
                    ClocktowerNightActionWakeInstruction(t(
                        "唤醒 侍女\n${actor?.seat}号 ${actor?.name.orEmpty()}\n让其选择除自己以外的两名存活玩家。",
                        "Wake Chambermaid\nP${actor?.seat} ${actor?.name.orEmpty()}\nHave them choose two other living players.",
                    ))
                    Text(t("查询玩家（已选 ${targets.size}/2）", "Queried players (${targets.size}/2)"))
                    targets.forEach { n -> Text(t("${n}号 ${state.gameState.playerAt(n)?.name.orEmpty()}", "P$n ${state.gameState.playerAt(n)?.name.orEmpty()}")) }
                    if (targets.size == 2) {
                        val values = selection?.numericValues.orEmpty()
                        if (values.size > 1) {
                            Text(t("选择向其展示的数字", "Choose the number to show"))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                values.sorted().forEach { n -> OutlinedButton(onClick = { number = n }) { Text((if (number == n) "✓ " else "") + n) } }
                            }
                        } else values.singleOrNull()?.let { n -> Text(t("展示数字：$n", "Number to show: $n")) }
                    }
                    OutlinedButton(enabled = prop != null, onClick = {
                        prop?.let { p ->
                            preview = BmrPlayerPreview(p, BmrHostAction.Information(id, state.bmrInformationDraft(requireNotNull(step), p)))
                            showing = true
                        }
                    }) { Text(t("展示给玩家", "Show to player")) }
                    if (preview != null) Text(t("已展示；下一步确认最后一次展示。", "Shown; Next confirms the last display."))
                    if (error) Text(t("结果与当前状态不一致，请核对后重试。", "Check the selected result against the current state."), color = MaterialTheme.colorScheme.error)
                }
            }
        } else ClocktowerDarkTheme {
            ClocktowerHostFullScreenScaffold(previousEnabled = false, onPrevious = {}, onHostTools = onHostTools,
                previousLabel = "", hostToolsLabel = t("主持工具", "Host tools"), nextLabel = if(step == null) t("天亮", "Dawn") else t("下一步", "Next"),
                nextEnabled = ready, onBack = {}, onNext = { if(ready) nextAction()?.let(::accept) }) {
                HostTableShell(state.gameState.toHostSeatPresentations { roleName(it) }, Modifier.fillMaxSize(),
                    wakingSeatIds = if (fake || godfatherAction) setOfNotNull(actor?.seat?.let(::ClocktowerSeatId)) else emptySet(),
                    interaction = HostTableInteractionState(HostTableInteractionMode.OrderedSelection, selectable.mapTo(mutableSetOf()) { ClocktowerSeatId(it) }, targets.map { ClocktowerSeatId(it) }),
                    onSeatClick = { clicked ->
                        val n = clicked.number
                        if(n in selectable) { targets = if(n in targets) targets - n else if(targets.size < maximum) targets + n else listOf(n); charge = false; declinedRevival = false; deaths = emptyMap(); drunkSeat = null }
                    }) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (godfatherAction) {
                            ClocktowerNightActionWakeInstruction(t("唤醒 教父\n${actor?.seat}号 ${actor?.name.orEmpty()}\n让其选择今晚要杀死的一名玩家，并记录实际结算结果。", "Wake Godfather\nP${actor?.seat} ${actor?.name.orEmpty()}\nHave them choose one player to kill tonight, then record the actual outcome."))
                        } else if (fake) {
                            Text(if (lunaticInfo) t("唤醒 疯子", "Wake Lunatic") else t("唤醒 ${actor?.seat}号 · ${actor?.shownRole?.let(::roleName).orEmpty()}", "Wake P${actor?.seat} · ${actor?.shownRole?.let(::roleName).orEmpty()}"), style = MaterialTheme.typography.titleMedium)
                            Text(t("${actor?.seat}号 ${actor?.name.orEmpty()}", "P${actor?.seat} ${actor?.name.orEmpty()}"))
                            if (lunaticInfo) Text(t("其认为自己是：${actor?.shownRole?.let(::roleName).orEmpty()}", "Believes they are: ${actor?.shownRole?.let(::roleName).orEmpty()}"))
                        } else Text(if(step == null) t("本夜结算完成", "Night resolved") else t("${actor?.seat ?: ""}号 ${actor?.name.orEmpty()} · ", "#${actor?.seat ?: ""} ${actor?.name.orEmpty()} · ") + (step.roleId?.let(::roleName) ?: t("阵营信息", "Team information")), style = MaterialTheme.typography.titleMedium)
                        if(grandchildSetup) Text(t("不唤醒祖母；先选定真实孙辈。", "Do not wake the Grandmother; choose the actual grandchild first."))
                        if(lunaticInfo) Text(t("在方桌上选择向其展示的 $maximum 名“爪牙”，并选择 3 个伪装角色。配置完成后，唤醒该玩家并展示信息。", "Choose $maximum players to show as Minions and 3 bluff characters. Once ready, wake this player and show the information."))
                        else if(info) Text(t("唤醒 ${actor?.seat} 号，展示信息。", "Wake #${actor?.seat} and show the information."))
                        else if(passive || regurgitate || pukkaSettlement) Text(t("说书人结算，不唤醒玩家。", "Storyteller resolution; do not wake a player."))
                        else if(!grandchildSetup && !exorcised && !godfatherAction && step != null) Text(if((selection?.attackNumber ?: 1) > 1) t("继续 ${actor?.seat} 号的选择，不再次唤醒。", "Continue #${actor?.seat}'s choices without another wake.") else t("唤醒 ${actor?.seat} 号，由其选择。", "Wake #${actor?.seat} for their choice."))
                        selection?.attackTotal?.let { total -> Text(t("第 ${selection.attackNumber}/$total 次选择", "Choice ${selection.attackNumber}/$total")) }
                        if(fake && !lunaticInfo) Text(t("实际为疯子；记录其选择，不产生真实恶魔效果。", "Actually the Lunatic: record their choices without real Demon effects."))
                        if(exorcised) Text(t("驱魔生效，本夜不唤醒。旧毒与返流仍单独结算。", "Exorcised: no ability wake. Settle existing poison/regurgitation separately."))
                        if(lunaticInfo) Text(t("向其展示的爪牙（已选 ${targets.size}/$maximum）：", "Shown Minions (${targets.size}/$maximum): ") + targets.joinToString(" · ") { seat(it) })
                        else if(step != null && maximum > 0) Text((if(fake) t("让其选择今晚的目标：", "Have them choose tonight's targets: ") else t("按顺序选择 $maximum 人：", "Choose $maximum in order: ")) + targets.joinToString(" → ") { seat(it) })
                        if(optional) BmrYesNo(t("本夜使用能力？", "Use the ability tonight?"), use, { use = it; targets = emptyList(); chosenRole = null }, language)
                        if(selection?.chargeAllowed == true) TextButton(onClick = { charge = true; targets = emptyList() }) { Text(t("不攻击，蓄力", "Choose nobody; charge")) }
                        if(regurgitate) TextButton(onClick = { declinedRevival = true; targets = emptyList() }) { Text(t("本夜不返流", "No regurgitation")) }
                        if(selection?.roleRequired == true) BmrRolePicker(t("选择角色", "Choose a character"), chosenRole, badMoonRisingRoles, language) { chosenRole = it }
                        if(needsDrunk) {
                            Text(t("实际醉酒对象", "Player actually drunk"))
                            selection?.drunkSeats.orEmpty().forEach { n ->
                                TextButton(onClick = { drunkSeat = n }) { Text((if(drunkSeat == n) "✓ " else "") + seat(n)) }
                            }
                        }
                        dyingSeats.forEach { n -> BmrYesNo(t("${seat(n)} 死亡？", "${seat(n)} dies?"), deathResult(n), { deaths = deaths + (n to it) }, language) }
                        if(gm != null) BmrYesNo(t("祖母 ${seat(gm.seat)} 死亡？", "Grandmother ${seat(gm.seat)} dies?"), gmResult, { grandmotherDies = it }, language)
                        if(info && id.startsWith("bmr:info:Grandmother:") && actor?.impaired == true) {
                            BmrSeatPicker(t("展示玩家", "Shown player"), shownSeat ?: actor?.seat?.let(state::bmrGrandchild), state.gameState.players.map { it.seat }, state, language) { shownSeat = it }
                            BmrRolePicker(t("展示角色", "Shown character"), shownRole, badMoonRisingRoles.filter { RoleId(it.enName) in selection?.displayRoles.orEmpty() }, language) { shownRole = it }
                        }
                        if(lunaticInfo) {
                            val candidates = badMoonRisingRoles.filter { RoleId(it.enName) in selection?.displayRoles.orEmpty() }
                            BmrRolePicker(t("向其提供的伪装角色（已选 ${bluffs.size}/3）", "Shown bluffs (${bluffs.size}/3)"), null, candidates, language) { r -> bluffs = if(r in bluffs) bluffs - r else if(bluffs.size < (selection?.displayRolesMaximum ?: 0)) bluffs + r else bluffs }
                            bluffs.forEach { r -> TextButton(onClick = { bluffs = bluffs - r }) { Text("× " + roleName(r)) } }
                        }
                        if(info) Button(enabled = prop != null, onClick = {
                            prop?.let { p ->
                                val action = if(step?.roleId == null) BmrHostAction.EvilInformation(id) else BmrHostAction.Information(id, state.bmrInformationDraft(requireNotNull(step), p))
                                preview = BmrPlayerPreview(p, action); showing = true
                            }
                        }) { Text(if(lunaticInfo) t("展示给玩家", "Show to player") else t("展示信息", "Show information")) }
                        if(preview != null) Text(t("已展示；下一步确认最后一次展示。", "Shown; Next confirms the last display."))
                        if(error) Text(t("结果与当前状态不一致，请核对后重试。", "Check the selected result against the current state."), color = MaterialTheme.colorScheme.error)
                        ClocktowerBmrStatusControls(state, ::accept)
                    }
                }
            }
        }
    }
}
