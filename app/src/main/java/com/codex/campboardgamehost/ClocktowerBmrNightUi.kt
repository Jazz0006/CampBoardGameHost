package com.codex.campboardgamehost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val step = night.interactions.firstOrNull { !state.nightInteractionConfirmed(it) }
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
        val exorcised = actor?.actualType == CharacterType.DEMON && state.bmrExorcised(actor.seat) && step?.kind == ClocktowerHostInteractionKind.ROLE_PHASE_ACTION
        val grandchildSetup = id.startsWith("bmr:grandchild:")
        val info = step != null && (step.roleId == null || listOf("bmr:info:", "bmr:lunatic-info:", "bmr:lunatic-identity:", "bmr:lunatic-targets:", "bmr:exorcist-notice:", "bmr:goon-notice:").any { id.startsWith(it) } || role == "Chambermaid")
        val regurgitate = id.startsWith("bmr:regurgitate:")
        val pukkaSettlement = id.startsWith("bmr:pukka-settlement:")
        val passive = id.startsWith("bmr:gossip:") || id.startsWith("bmr:moonchild:")
        val oldPoison = if(pukkaSettlement) state.pukkaPoisonsDue().single { it.fact.actionId == id.removePrefix("bmr:pukka-settlement:") }.fact as ActionFact.PukkaChoice else null
        val moonchildChoice = if(id.startsWith("bmr:moonchild:")) state.actionTimeline.entries.lastOrNull { it.point.phase == StorytellerPhase.DAY && it.point.round == state.round - 1 && it.fact is ActionFact.MoonchildChoice }?.fact as? ActionFact.MoonchildChoice else null
        val fixedTarget = oldPoison?.targetSeat ?: moonchildChoice?.targetSeat
        val selection = step?.let { state.bmrNightSelection(it, targets, use, charge, chosenRole) }
        val optional = selection?.optionalUse == true
        val maximum = selection?.maximumTargets ?: 0
        val selectable = selection?.selectableSeats.orEmpty()
        val prop = if(step != null && selection != null) state.bmrInformationPreview(step, selection, targets, bluffs, number, shownSeat, shownRole) else null
        val attackRole = role in setOf("Shabaloth", "Po", "Zombuul", "Assassin", "Godfather") && !fake && !exorcised
        val dyingSeats = selection?.deathSeats.orEmpty()
        fun deathResult(seat: Int): Boolean? = if(seat in selection?.forcedNoDeathSeats.orEmpty()) false else deaths[seat]
        val selectedReady = when {
            step == null || exorcised || info -> true
            regurgitate -> declinedRevival || targets.size == 1
            optional && use == null -> false
            role == "Po" && selection?.chargeAllowed == true && charge -> true
            else -> selection?.targetsValid(targets) == true
        }
        val needsDrunk = selection?.drunkSeats?.isNotEmpty() == true
        val gm = if(attackRole && role !in setOf("Assassin", "Godfather") || pukkaSettlement)
            dyingSeats.firstOrNull { deathResult(it) == true }?.let(state::bmrGrandmotherAtRisk)?.let(state.gameState::playerAt) else null
        val ready = if(info) preview != null else selectedReady && dyingSeats.all { deathResult(it) != null } &&
            (!needsDrunk || drunkSeat != null) && (selection?.roleRequired != true || chosenRole != null) && (gm == null || grandmotherDies != null)
        fun nextAction(): BmrHostAction? = when {
            step == null -> BmrHostAction.Dawn
            info -> preview?.action
            exorcised -> BmrHostAction.Exorcised(id)
            grandchildSetup -> BmrHostAction.Grandchild(id, targets.single())
            regurgitate -> BmrHostAction.Regurgitate(id, targets.singleOrNull())
            pukkaSettlement -> BmrHostAction.PukkaSettlement(requireNotNull(oldPoison).actionId, requireNotNull(deathResult(oldPoison.targetSeat)), grandmotherDies)
            passive -> BmrHostAction.Passive(id, fixedTarget ?: targets.singleOrNull(), dyingSeats.singleOrNull()?.let { requireNotNull(deathResult(it)) } ?: false)
            role == "Pukka" && !fake -> BmrHostAction.PukkaChoice(targets.single())
            else -> BmrHostAction.Choice(BmrNightChoice(id, targets, if(optional && use == false) null else chosenRole,
                if(optional) use == true else true, drunkSeat, dyingSeats.map { requireNotNull(deathResult(it)) }, grandmotherDies))
        }
        if(showing && preview != null) {
            val text = bmrPlayerInformationText(requireNotNull(preview).proposition, state, language)
            ClocktowerPlayerDisplayCardLocalized(ClocktowerNightStepUi(t("信息", "Information"), null, true, "", "", text, "",
                displayKind = ClocktowerDisplayKind.Plain, displayPrimary = text), emptyList()) { showing = false }
        } else ClocktowerDarkTheme {
            ClocktowerHostFullScreenScaffold(previousEnabled = false, onPrevious = {}, onHostTools = onHostTools,
                previousLabel = "", hostToolsLabel = t("主持工具", "Host tools"), nextLabel = if(step == null) t("天亮", "Dawn") else t("下一步", "Next"),
                nextEnabled = ready, onBack = {}, onNext = { if(ready) nextAction()?.let(::accept) }) {
                HostTableShell(state.gameState.toHostSeatPresentations { roleName(it) }, Modifier.fillMaxSize(),
                    interaction = HostTableInteractionState(HostTableInteractionMode.OrderedSelection, selectable.mapTo(mutableSetOf()) { ClocktowerSeatId(it) }, targets.map { ClocktowerSeatId(it) }),
                    onSeatClick = { clicked ->
                        val n = clicked.number
                        if(n in selectable) { targets = if(n in targets) targets - n else if(targets.size < maximum) targets + n else listOf(n); charge = false; declinedRevival = false; deaths = emptyMap(); drunkSeat = null }
                    }) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if(step == null) t("本夜结算完成", "Night resolved") else t("${actor?.seat ?: ""}号 ${actor?.name.orEmpty()} · ", "#${actor?.seat ?: ""} ${actor?.name.orEmpty()} · ") + (step.roleId?.let(::roleName) ?: t("阵营信息", "Team information")), style = MaterialTheme.typography.titleMedium)
                        if(grandchildSetup) Text(t("不唤醒祖母；先选定真实孙辈。", "Do not wake the Grandmother; choose the actual grandchild first."))
                        if(info) Text(if(id.endsWith("minion_info")) t("唤醒所有爪牙，展示信息。", "Wake all Minions and show the information.") else t("唤醒 ${actor?.seat} 号，展示信息。", "Wake #${actor?.seat} and show the information."))
                        else if(passive || regurgitate || pukkaSettlement) Text(t("说书人结算，不唤醒玩家。", "Storyteller resolution; do not wake a player."))
                        else if(!grandchildSetup && !exorcised && step != null) Text(if((selection?.attackNumber ?: 1) > 1) t("继续 ${actor?.seat} 号的选择，不再次唤醒。", "Continue #${actor?.seat}'s choices without another wake.") else t("唤醒 ${actor?.seat} 号，由其选择。", "Wake #${actor?.seat} for their choice."))
                        selection?.attackTotal?.let { total -> Text(t("第 ${selection.attackNumber}/$total 次选择", "Choice ${selection.attackNumber}/$total")) }
                        if(fake) Text(t("实际为疯子；按展示的恶魔行动。", "Lunatic: use the shown Demon actions."))
                        if(exorcised) Text(t("驱魔生效，本夜不唤醒。旧毒与返流仍单独结算。", "Exorcised: no ability wake. Settle existing poison/regurgitation separately."))
                        if(step != null && maximum > 0) Text(t("按顺序选择 $maximum 人：", "Choose $maximum in order: ") + targets.joinToString(" → ") { seat(it) })
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
                        if(gm != null) BmrYesNo(t("祖母 ${seat(gm.seat)} 死亡？", "Grandmother ${seat(gm.seat)} dies?"), grandmotherDies, { grandmotherDies = it }, language)
                        if(info && id.startsWith("bmr:info:Grandmother:") && actor?.impaired == true) {
                            BmrSeatPicker(t("展示玩家", "Shown player"), shownSeat ?: actor?.seat?.let(state::bmrGrandchild), state.gameState.players.map { it.seat }, state, language) { shownSeat = it }
                            BmrRolePicker(t("展示角色", "Shown character"), shownRole, badMoonRisingRoles.filter { RoleId(it.enName) in selection?.displayRoles.orEmpty() }, language) { shownRole = it }
                        }
                        if(info && (id.startsWith("bmr:lunatic-info:") || role == "Godfather" && actor?.impaired == true)) {
                            val candidates = badMoonRisingRoles.filter { RoleId(it.enName) in selection?.displayRoles.orEmpty() }
                            BmrRolePicker(t("选择展示角色", "Choose shown characters"), null, candidates, language) { r -> bluffs = if(r in bluffs) bluffs - r else if(bluffs.size < (selection?.displayRolesMaximum ?: 0)) bluffs + r else bluffs }
                            bluffs.forEach { r -> TextButton(onClick = { bluffs = bluffs - r }) { Text("× " + roleName(r)) } }
                        }
                        if(info && role == "Chambermaid" && targets.size == 2) {
                            selection?.numericValues.orEmpty().forEach { n -> TextButton(onClick = { number = n }) { Text((if(number == n) "✓ " else "") + "$n") } }
                        }
                        if(info) Button(enabled = prop != null, onClick = {
                            prop?.let { p ->
                                val action = if(step?.roleId == null) BmrHostAction.EvilInformation(id) else BmrHostAction.Information(id, state.bmrInformationDraft(requireNotNull(step), p))
                                preview = BmrPlayerPreview(p, action); showing = true
                            }
                        }) { Text(t("展示信息", "Show information")) }
                        if(preview != null) Text(t("已展示；下一步确认最后一次展示。", "Shown; Next confirms the last display."))
                        if(error) Text(t("结果与当前状态不一致，请核对后重试。", "Check the selected result against the current state."), color = MaterialTheme.colorScheme.error)
                        ClocktowerBmrStatusControls(state, ::accept)
                    }
                }
            }
        }
    }
}
