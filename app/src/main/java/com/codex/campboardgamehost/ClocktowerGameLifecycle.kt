package com.codex.campboardgamehost

import android.content.Context
import androidx.compose.runtime.*
import com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.rules.RulesetJsonLoader
import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.setup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import java.util.UUID
import java.io.IOException

/** Coordinates entry/exit of one game; Session alone owns facts and the App alone navigates. */
internal class ClocktowerGameLifecycle(
    private val context: Context,
    private val catalog: BuiltInClocktowerRulesetCatalog,
    private val preferences: AppPreferencesState,
    private val aiSettings: StorytellerAiSettings,
    private val conversation: ClocktowerAiConversation,
    private val newSeed: () -> Long,
    private val onSaveError: () -> Unit,
    private val store: ClocktowerActiveGameStore = ClocktowerActiveGameStore.fromContext(context),
) {
    enum class StartResult { STARTED, NOT_READY, FAILED }
    enum class RestoreResult { NONE, DEAL, HOST, FAILED }
    sealed interface RestartResult {
        data class Ready(val script: ClocktowerScript, val players: List<String>) : RestartResult
        data object NotReady : RestartResult
        data object Failed : RestartResult
    }
    var sessionUi by mutableStateOf<ClocktowerSessionUiState?>(null)
        private set
    var operationMode by mutableStateOf(aiSettings.preferredMode)
        private set
    var rulesetRef by mutableStateOf<RulesetRef?>(null)
        private set
    private var rotation: TroubleBrewingSetupRotationRecord? = null
    private var active = true

    fun dispose() { active = false; sessionUi?.deactivate() }
    fun readyForNewGame(script: ClocktowerScript) = storytellerNewGameConfigurationReady(
        storytellerModeForNewGame(aiSettings.preferredMode, script), aiSettings.directEnabled,
        aiSettings.directLoaded, aiSettings.directModel, aiSettings.directKey,
        aiSettings.gatewayLoaded, aiSettings.gatewayEndpoint, aiSettings.gatewayToken)

    fun beginNewGame(script: ClocktowerScript): Boolean {
        if (!active || !readyForNewGame(script)) return false
        operationMode = storytellerModeForNewGame(aiSettings.preferredMode, script)
        return true
    }
    fun takeOverManually() { operationMode = StorytellerOperationMode.MANUAL; persistMetadata() }

    fun prepareTroubleBrewing(players: List<String>): TroubleBrewingPreparedSetup {
        val dataset = context.assets.open("setup/trouble_brewing_setup_presets_v2_final.json")
            .bufferedReader().use { TroubleBrewingSetupPresetJson.parse(it.readText()) }
        val history = TroubleBrewingSetupRotationHistoryStore.fromContext(context)
        return TroubleBrewingProductionSetupPreparer.prepare(dataset,
            catalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry, players.toList(), newSeed(),
            history.historyFor(dataset.datasetId, dataset.schemaVersion, players.size),
            history.recentPlayerStartingIdentityHistoryFor(dataset.datasetId, dataset.schemaVersion))
    }

    fun commitTroubleBrewingSetup(gameId: String, prepared: TroubleBrewingPreparedSetup,
        candidate: TroubleBrewingDrunkCandidate?): StartResult {
        val registry = catalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry
        val committed = TroubleBrewingSetupCommitter.commit(prepared.intermediateSetup, candidate, registry)
        val basis = ClocktowerRulesetPersistenceBasis(committed.gameState.players.map { it.actualRole }.toSet())
        val knowledge = context.assets.open("rules/trouble_brewing.json").bufferedReader().use {
            RulesetJsonLoader.parse(it.readText())
        }
        val ref = TroubleBrewingRulesetPersistence.refFor(knowledge, basis)
        val nextRotation = TroubleBrewingSetupRotationRecordFactory.fromCommittedSetup(prepared, committed, registry)
        return installNew(gameId, committed.gameState, ref, nextRotation)
    }

    fun startNoGreaterJoy(players: List<String>): StartResult {
        val seed = newSeed()
        val prepared = NoGreaterJoyProductionSetupPreparer.prepare(
            catalog.ruleset(ClocktowerScript.NoGreaterJoy), players.size, seed)
        // Use the same card builder as restored/TB Session display; no parallel assignment renderer.
        val roles = clocktowerRolesForScript(ClocktowerScript.NoGreaterJoy).associateBy { RoleId(it.enName) }
        val cards = prepared.assignments.map { assignment ->
            clocktowerPlayerCard(players[assignment.seat - 1], requireNotNull(roles[assignment.actualRole]),
                requireNotNull(roles[assignment.shownRole]), "en")
        }
        return installNew(UUID.randomUUID().toString(), cards.toClocktowerGameState(
            ClocktowerScript.NoGreaterJoy, seed, null), null, null)
    }

    private fun metadata(ref: RulesetRef? = rulesetRef, record: TroubleBrewingSetupRotationRecord? = rotation) =
        ClocktowerActiveGameMetadata.encode(operationMode, ref, record, conversation.openingPlanRecommendation,
            conversation.strategicPolicyCursor, conversation.canonicalConversationResponseId, conversation.canonicalConversationModel)

    private fun installNew(gameId: String, initial: GameState, ref: RulesetRef?, record: TroubleBrewingSetupRotationRecord?): StartResult {
        if (!active) return StartResult.NOT_READY
        val session = ClocktowerGameSession.createProduction(gameId, initial.seed, initial, ClocktowerSemanticHistoryMode.GLOBAL_V1,
            initialPlayerContextBySeat = initial.players.mapNotNull { player ->
                preferences.experience(player.name).takeUnless { it == PlayerExperienceLevelV1.BEGINNER }
                    ?.let { player.seat to StorytellerPlayerContextInputV1(experienceLevel = it) }
            }.toMap())
        return try {
            session.persistWith { confirmed -> store.save(confirmed, metadata(ref, record)) }
            sessionUi?.deactivate()
            rulesetRef = ref; rotation = record
            sessionUi = ClocktowerSessionUiState(session, onSaveError)
            StartResult.STARTED
        } catch (_: IOException) { onSaveError(); StartResult.FAILED }
    }

    fun persistMetadata() {
        sessionUi?.commit { session -> session.persistWith { store.save(it, metadata()) } }
    }

    suspend fun restore(): RestoreResult {
        if (!active || sessionUi != null) return RestoreResult.NONE
        return try {
            val saved = withContext(Dispatchers.IO) { store.load() } ?: return RestoreResult.NONE
            if (!active || sessionUi != null) return RestoreResult.NONE
            val restored = ClocktowerActiveGameMetadata.restore(saved, catalog, conversation)
            rulesetRef = restored.ref; rotation = restored.rotation; operationMode = restored.mode
            restored.session.persistWith { store.save(it, metadata()) }
            sessionUi = ClocktowerSessionUiState(restored.session, onSaveError)
            if (restored.needsDeal) RestoreResult.DEAL else RestoreResult.HOST
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { RestoreResult.FAILED }
    }

    fun finish(): Boolean {
        val session = sessionUi?.session ?: return false
        if (session.state.outcome != null && rotation != null &&
            !TroubleBrewingSetupRotationHistoryStore.fromContext(context).recordCompletedGame(session.state.gameId, requireNotNull(rotation))) return false
        return try {
            store.clear()
            sessionUi?.deactivate(); sessionUi = null; rotation = null; rulesetRef = null
            true
        } catch (_: IOException) { onSaveError(); false }
    }
    fun prepareRestart(): RestartResult {
        val state = sessionUi?.snapshot ?: return RestartResult.Failed
        val script = ClocktowerScript.entries.single { it.toRecommendationScriptId() == state.gameState.script }
        if (!readyForNewGame(script)) return RestartResult.NotReady
        val players = state.gameState.players.sortedBy { it.seat }.map { it.name }
        return if (finish()) RestartResult.Ready(script, players) else RestartResult.Failed
    }
}
