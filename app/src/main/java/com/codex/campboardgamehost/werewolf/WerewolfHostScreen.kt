package com.codex.campboardgamehost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Behavior-preserving R2 extraction for Werewolf host UI. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WerewolfSettingsScreen(
    playerCount: Int,
    werewolfCount: Int,
    includeSeer: Boolean,
    includeWitch: Boolean,
    includeHunter: Boolean,
    lastWordsMode: LastWordsMode,
    onWerewolfCountChange: (Int) -> Unit,
    onIncludeSeerChange: (Boolean) -> Unit,
    onIncludeWitchChange: (Boolean) -> Unit,
    onIncludeHunterChange: (Boolean) -> Unit,
    onLastWordsModeChange: (LastWordsMode) -> Unit,
    onApplyTemplate: (WerewolfTemplate) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val specialCount = listOf(includeSeer, includeWitch, includeHunter).count { it }
    val villagerCount = playerCount - werewolfCount - specialCount
    val roleTotal = werewolfCount + specialCount + villagerCount.coerceAtLeast(0)
    val canStart = playerCount >= MIN_WEREWOLF_PLAYERS && villagerCount >= 0
    val recommendedTemplates = werewolfTemplates.filter { it.playerCount == playerCount }
    val otherTemplates = werewolfTemplates.filter { it.playerCount != playerCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            GameSettingsHeader(
                title = stringResource(R.string.game_werewolf),
                subtitle = stringResource(R.string.game_settings_subtitle, playerCount),
                onBack = onBack,
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.werewolf_template_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.werewolf_template_hint), color = Color(0xFF6F7B74))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendedTemplates.forEach { template ->
                            Button(
                                onClick = { onApplyTemplate(template) },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(templateLabel(template))
                            }
                        }
                        otherTemplates.forEach { template ->
                            OutlinedButton(
                                onClick = { onApplyTemplate(template) },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(templateLabel(template))
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(stringResource(R.string.werewolf_role_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.werewolf_role_summary, roleTotal, playerCount, villagerCount.coerceAtLeast(0)), color = Color(0xFF6F7B74))
                    StepperRow(
                        label = stringResource(R.string.role_werewolf),
                        value = werewolfCount,
                        range = 1..(playerCount - specialCount).coerceAtLeast(1),
                        onChange = onWerewolfCountChange,
                    )
                    RoleToggleRow(
                        roleName = stringResource(R.string.role_seer),
                        description = stringResource(R.string.role_seer_desc),
                        checked = includeSeer,
                        onCheckedChange = onIncludeSeerChange,
                    )
                    RoleToggleRow(
                        roleName = stringResource(R.string.role_witch),
                        description = stringResource(R.string.role_witch_desc),
                        checked = includeWitch,
                        onCheckedChange = onIncludeWitchChange,
                    )
                    RoleToggleRow(
                        roleName = stringResource(R.string.role_hunter),
                        description = stringResource(R.string.role_hunter_desc),
                        checked = includeHunter,
                        onCheckedChange = onIncludeHunterChange,
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(stringResource(R.string.role_villager), fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.villager_auto_fill_hint), color = Color(0xFF6F7B74))
                        }
                        Text(villagerCount.coerceAtLeast(0).toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(stringResource(R.string.last_words_settings), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.last_words_settings_hint), color = Color(0xFF6F7B74))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LastWordsMode.entries.forEach { mode ->
                            if (mode == lastWordsMode) {
                                Button(
                                    onClick = { onLastWordsModeChange(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(stringResource(mode.labelResId()))
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onLastWordsModeChange(mode) },
                                    shape = RoundedCornerShape(8.dp),
                                ) {
                                    Text(stringResource(mode.labelResId()))
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = onStart,
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(if (canStart) stringResource(R.string.start_dealing) else stringResource(R.string.werewolf_roles_invalid))
            }
        }
    }
}

@Composable
private fun templateLabel(template: WerewolfTemplate): String {
    val specials = buildList {
        if (template.includeSeer) add(stringResource(R.string.role_seer_short))
        if (template.includeWitch) add(stringResource(R.string.role_witch_short))
        if (template.includeHunter) add(stringResource(R.string.role_hunter_short))
    }.joinToString("")
        .ifBlank { stringResource(R.string.no_special_roles_short) }
    return stringResource(R.string.werewolf_template_label_format, template.playerCount, template.werewolfCount, specials)
}

@Composable
private fun RoleToggleRow(
    roleName: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Column(modifier = Modifier.weight(1f)) {
            Text(roleName, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color(0xFF6F7B74), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WerewolfJudgeScreen(
    cards: List<PlayerCard>,
    records: List<EliminationRecord>,
    nightNumber: Int,
    stepIndex: Int,
    pendingNightDeath: String?,
    seerCheckTarget: String?,
    witchSaveUsed: Boolean,
    witchPoisonUsed: Boolean,
    witchSavedTonight: Boolean,
    witchPoisonTarget: String?,
    hunterShotTarget: String?,
    selectedDayExile: String?,
    gameOutcome: GameOutcome?,
    lastWordsPromptNames: List<String>,
    onStepIndexChange: (Int) -> Unit,
    onSelectNightDeath: (String?) -> Unit,
    onSelectSeerCheck: (String?) -> Unit,
    onToggleWitchSave: (Boolean) -> Unit,
    onSelectWitchPoison: (String?) -> Unit,
    onSelectHunterShot: (String?) -> Unit,
    onConfirmDawn: (List<Pair<String, String>>) -> Unit,
    onSelectDayExile: (String?) -> Unit,
    onConfirmDayExile: () -> Unit,
    onDismissLastWordsPrompt: () -> Unit,
    onShowResults: () -> Unit,
) {
    val steps = buildList {
        add(WerewolfJudgeStep.Wolves)
        if (cards.any { it.role == Role.Seer }) add(WerewolfJudgeStep.Seer)
        if (cards.any { it.role == Role.Witch }) add(WerewolfJudgeStep.Witch)
        if (cards.any { it.role == Role.Hunter }) add(WerewolfJudgeStep.Hunter)
        add(WerewolfJudgeStep.Dawn)
        add(WerewolfJudgeStep.DayVote)
    }
    val currentIndex = stepIndex.coerceIn(0, steps.lastIndex)
    val currentStep = steps[currentIndex]
    val aliveCards = cards.filter { it.eliminatedRound == null }
    val wolfAttackDeath = pendingNightDeath?.takeUnless { witchSavedTonight }
    val baseNightDeathEvents = buildList {
        if (wolfAttackDeath != null) add(wolfAttackDeath to stringResource(R.string.werewolf_record_night_death))
        if (witchPoisonTarget != null) add(witchPoisonTarget to stringResource(R.string.werewolf_record_witch_poison))
    }.distinctBy { it.first }
    val baseNightDeathNames = baseNightDeathEvents.map { it.first }
    val hunterDiesTonight = baseNightDeathNames.any { name -> cards.firstOrNull { it.name == name }?.role == Role.Hunter }
    val selectedDayExileCard = cards.firstOrNull { it.name == selectedDayExile }
    val hunterCanShootAfterDayExile = selectedDayExileCard?.role == Role.Hunter
    val hunterShotEvent = hunterShotTarget
        ?.takeIf { hunterDiesTonight || hunterCanShootAfterDayExile }
        ?.let { it to stringResource(R.string.werewolf_record_hunter_shot) }
    val nightDeathEvents = (baseNightDeathEvents + listOfNotNull(hunterShotEvent)).distinctBy { it.first }
    val nightDeathNames = nightDeathEvents.map { it.first }

    fun roleCards(role: Role): List<PlayerCard> = cards.filter { it.role == role }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.werewolf_judge_assistant), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        gameOutcome?.title ?: stringResource(R.string.werewolf_night_format, nightNumber),
                        color = Color(0xFF5C6A63),
                    )
                }
            }
        }

        if (lastWordsPromptNames.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4DC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(stringResource(R.string.last_words_prompt_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.last_words_prompt_names, lastWordsPromptNames.joinToString(stringResource(R.string.name_separator))))
                        Text(stringResource(R.string.last_words_prompt_hint), color = Color(0xFF6F7B74))
                        Button(
                            onClick = onDismissLastWordsPrompt,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.got_it))
                        }
                    }
                }
            }
        }

        if (gameOutcome != null) {
            item {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2EA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(gameOutcome.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(gameOutcome.summary)
                        Text(gameOutcome.reason, color = Color(0xFF5C6A63))
                    }
                }
            }
        }

        item {
            HostProgressCard(
                title = stringResource(R.string.host_current_step),
                subtitle = stringResource(currentStep.titleResId()),
                progress = stringResource(R.string.host_step_progress_format, currentIndex + 1, steps.size),
            )
        }

        item {
            HostScriptCard(
                title = stringResource(currentStep.titleResId()),
                script = stringResource(currentStep.scriptResId()),
                action = stringResource(currentStep.actionResId()),
            ) {
                    when (currentStep) {
                        WerewolfJudgeStep.Wolves -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_werewolf), players = roleCards(Role.Werewolf))
                            HostActionSection(
                                title = stringResource(R.string.werewolf_choose_night_death),
                                helper = stringResource(R.string.host_choose_one_player_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = pendingNightDeath,
                                    onSelect = { onSelectNightDeath(if (pendingNightDeath == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                        }

                        WerewolfJudgeStep.Seer -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_seer), players = roleCards(Role.Seer))
                            HostActionSection(
                                title = stringResource(R.string.werewolf_choose_seer_check),
                                helper = stringResource(R.string.host_choose_one_player_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = seerCheckTarget,
                                    onSelect = { onSelectSeerCheck(if (seerCheckTarget == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                            seerCheckTarget?.let { targetName ->
                                val target = cards.firstOrNull { it.name == targetName }
                                val result = if (target?.role == Role.Werewolf) {
                                    stringResource(R.string.seer_result_werewolf)
                                } else {
                                    stringResource(R.string.seer_result_good)
                                }
                                Text(stringResource(R.string.seer_result_format, targetName, result), color = Color(0xFF2F5D50), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        WerewolfJudgeStep.Witch -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_witch), players = roleCards(Role.Witch))
                            HostInstructionBlock(
                                label = stringResource(R.string.host_current_result_label),
                                text = pendingNightDeath?.let { stringResource(R.string.werewolf_pending_death_format, it) }
                                    ?: stringResource(R.string.werewolf_no_pending_death),
                                backgroundColor = Color(0xFFFFFCF6),
                                textColor = Color(0xFF5C6A63),
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = witchSavedTonight,
                                    onCheckedChange = onToggleWitchSave,
                                    enabled = !witchSaveUsed && pendingNightDeath != null && gameOutcome == null,
                                )
                                Text(
                                    if (witchSaveUsed) stringResource(R.string.witch_save_used) else stringResource(R.string.witch_use_save),
                                    color = if (witchSaveUsed) Color(0xFF9A4B36) else Color(0xFF1F2925),
                                )
                            }
                            HostActionSection(
                                title = if (witchPoisonUsed) stringResource(R.string.witch_poison_used) else stringResource(R.string.witch_choose_poison),
                                helper = stringResource(R.string.host_optional_action_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards.filter { it.name != pendingNightDeath || !witchSavedTonight },
                                    selectedName = witchPoisonTarget,
                                    onSelect = { onSelectWitchPoison(if (witchPoisonTarget == it) null else it) },
                                    enabled = !witchPoisonUsed && gameOutcome == null,
                                )
                            }
                        }

                        WerewolfJudgeStep.Hunter -> {
                            WerewolfRoleLine(roleName = stringResource(R.string.role_hunter), players = roleCards(Role.Hunter))
                            Text(stringResource(R.string.hunter_status_hint), color = Color(0xFF6F7B74))
                        }

                        WerewolfJudgeStep.Dawn -> {
                            if (nightDeathEvents.isEmpty()) {
                                HostInstructionBlock(
                                    label = stringResource(R.string.host_current_result_label),
                                    text = stringResource(R.string.werewolf_no_final_death),
                                    backgroundColor = Color(0xFFFFFCF6),
                                    textColor = Color(0xFF5C6A63),
                                )
                            } else {
                                HostActionSection(title = stringResource(R.string.werewolf_final_deaths)) {
                                    nightDeathEvents.forEach { (name, note) ->
                                        Text(stringResource(R.string.werewolf_death_event_format, name, note), color = Color(0xFF6F7B74))
                                    }
                                }
                            }
                            if (hunterDiesTonight) {
                                HostActionSection(
                                    title = stringResource(R.string.hunter_choose_shot),
                                    helper = stringResource(R.string.host_optional_action_hint),
                                ) {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name !in nightDeathNames },
                                        selectedName = hunterShotTarget,
                                        onSelect = { onSelectHunterShot(if (hunterShotTarget == it) null else it) },
                                        enabled = gameOutcome == null,
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    onConfirmDawn(nightDeathEvents)
                                    onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex))
                                },
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(stringResource(R.string.werewolf_confirm_dawn))
                            }
                        }

                        WerewolfJudgeStep.DayVote -> {
                            HostActionSection(
                                title = stringResource(R.string.werewolf_choose_day_exile),
                                helper = stringResource(R.string.host_skip_allowed_hint),
                            ) {
                                SelectablePlayerChips(
                                    cards = aliveCards,
                                    selectedName = selectedDayExile,
                                    onSelect = { onSelectDayExile(if (selectedDayExile == it) null else it) },
                                    enabled = gameOutcome == null,
                                )
                            }
                            if (hunterCanShootAfterDayExile) {
                                HostActionSection(
                                    title = stringResource(R.string.hunter_choose_shot),
                                    helper = stringResource(R.string.host_optional_action_hint),
                                ) {
                                    SelectablePlayerChips(
                                        cards = aliveCards.filter { it.name != selectedDayExile },
                                        selectedName = hunterShotTarget,
                                        onSelect = { onSelectHunterShot(if (hunterShotTarget == it) null else it) },
                                        enabled = gameOutcome == null,
                                    )
                                }
                            }
                            Button(
                                onClick = onConfirmDayExile,
                                enabled = gameOutcome == null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(if (selectedDayExile == null) stringResource(R.string.werewolf_no_exile_next_night) else stringResource(R.string.werewolf_confirm_day_exile))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onStepIndexChange((currentIndex - 1).coerceAtLeast(0)) },
                            enabled = currentIndex > 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.previous_step))
                        }
                        Button(
                            onClick = { onStepIndexChange((currentIndex + 1).coerceAtMost(steps.lastIndex)) },
                            enabled = currentIndex < steps.lastIndex,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.next_step))
                        }
                    }
            }
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.player_status), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        items(cards) { card ->
            WerewolfPlayerStatusRow(card)
        }

        item {
            HorizontalDivider()
            Text(stringResource(R.string.elimination_records), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (records.isEmpty()) {
                Text(stringResource(R.string.no_eliminations), color = Color(0xFF6F7B74))
            }
        }

        items(records) { record ->
            Text(record.displayText(), modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            Button(
                onClick = onShowResults,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Text(if (gameOutcome == null) stringResource(R.string.end_and_reveal) else stringResource(R.string.view_results))
            }
        }
    }
}
