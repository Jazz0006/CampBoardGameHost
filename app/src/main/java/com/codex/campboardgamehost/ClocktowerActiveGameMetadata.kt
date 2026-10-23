package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.*
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.setup.*
import org.json.JSONArray
import org.json.JSONObject

/** Advisory AI state and immutable setup identity travel with the one active Session save. */
internal object ClocktowerActiveGameMetadata {
    data class Restored(val session: ClocktowerGameSession, val mode: StorytellerOperationMode,
        val ref: RulesetRef?, val rotation: TroubleBrewingSetupRotationRecord?, val needsDeal: Boolean)

    fun restore(saved: ClocktowerActiveGameStore.Saved,
        catalog: com.codex.campboardgamehost.clocktower.catalog.BuiltInClocktowerRulesetCatalog,
        conversation: ClocktowerAiConversation): Restored {
        val state = saved.state
        val metadata = saved.metadata
        val session = ClocktowerGameSession.restore(state)
        val opening = opening(metadata)
        val cursor = cursor(metadata)
        require(opening == null || opening.selection.gameId == state.gameId)
        require(cursor == null || (cursor.gameId == state.gameId && cursor.exclusiveGlobalSequence <= state.nextTimelineGlobalSequence))
        val restored = Restored(session, StorytellerOperationMode.valueOf(metadata.getString("mode")),
            ruleset(metadata), rotation(metadata), state.phase == StorytellerPhase.FIRST_NIGHT &&
                state.actionTimeline.entries.isEmpty() && state.epistemicObservationLog.records.isEmpty())
        val bluffNames = opening?.selection?.demonBluffs.orEmpty().map { external ->
            requireNotNull(catalog.ruleset(ClocktowerScript.TroubleBrewing).characterRegistry.findByExternalId(external)).id.value
        }
        conversation.openingPlanRecommendation = opening
        conversation.openingRecommendedInformation = opening?.selection?.information.orEmpty()
        conversation.openingRecommendedRedHerringSeat = opening?.selection?.redHerringSeat
        conversation.openingRecommendedBluffRoleNames = bluffNames
        conversation.strategicPolicyCursor = cursor
        conversation.canonicalConversationResponseId = metadata.optString("responseId").takeIf { it.isNotBlank() }
        conversation.canonicalConversationModel = metadata.optString("responseModel").takeIf { it.isNotBlank() }
        conversation.strategicLastReviewedHost = StorytellerStrategicChangeCheckpointV2.capture(session,
            StorytellerStrategicChangeCheckpointV2.Position(state.phase, state.round))
        return restored
    }

    fun encode(mode: StorytellerOperationMode, ref: RulesetRef?, rotation: TroubleBrewingSetupRotationRecord?,
        opening: HostOpeningSemanticAcceptanceV2?, cursor: StorytellerStrategicContinuationV2.MemoryCursor?,
        responseId: String?, responseModel: String?): JSONObject = JSONObject().apply {
        put("mode", mode.name)
        ref?.let { put("ruleset", JSONObject().put("script", it.scriptId.value).put("hash", it.scriptContentHash)
            .put("version", it.rulesetVersion).put("source", it.sourceRevision).put("coverage", it.coverage.name)) }
        rotation?.let { v -> put("rotation", JSONObject().apply {
            put("dataset", v.datasetId); put("schema", v.schemaVersion); put("preset", v.presetId); put("count", v.playerCount)
            put("roles", JSONArray(v.realNonDemonRoleIds.sorted())); put("minions", JSONArray(v.minionRoleIds.sorted())); put("style", v.primaryStyleTag)
            put("identities", JSONArray(v.playerStartingIdentities.map { JSONObject().put("player", it.playerKey)
                .put("actual", it.actualRoleId).put("shown", it.shownRoleId).put("category", it.actualRoleCategory.name) }))
        }) }
        opening?.let { v -> put("opening", JSONObject().apply {
            val s = v.selection
            put("gameId", s.gameId); put("revision", s.gameStateRevision); put("input", s.playerInputRevision); put("roster", s.rosterFingerprint)
            put("drunk", s.drunkSeat); put("redHerring", s.redHerringSeat); s.demonBluffs?.let { put("bluffs", JSONArray(it)) }
            put("information", JSONArray(s.information.values.map(::semantic)))
            put("conditional", JSONArray(v.conditional.map { JSONObject().put("id", it.decisionId)
                .put("proposed", it.proposed?.let(::semantic)).put("waiting", JSONArray(it.waitingForEvents)) }))
            v.strategyMemory?.let { put("memory", memory(it)) }
        }) }
        cursor?.let { put("cursor", JSONObject().put("gameId", it.gameId).put("revision", it.gameStateRevision)
            .put("input", it.playerInputRevision).put("global", it.exclusiveGlobalSequence).put("version", it.version).put("memory", memory(it.memory))) }
        put("responseId", responseId); put("responseModel", responseModel)
    }
    fun ruleset(j: JSONObject): RulesetRef? = j.optJSONObject("ruleset")?.let {
        RulesetRef(ScriptId(it.getString("script")), it.getString("hash"), it.getString("version"), it.getString("source"), RuleCoverage.valueOf(it.getString("coverage"))) }
    fun rotation(j: JSONObject): TroubleBrewingSetupRotationRecord? = j.optJSONObject("rotation")?.let { v ->
        TroubleBrewingSetupRotationRecord(v.getString("dataset"), v.getInt("schema"), v.getString("preset"), v.getInt("count"),
            v.getJSONArray("roles").strings().toSet(), v.getJSONArray("minions").strings().toSet(), v.nullString("style"),
            v.getJSONArray("identities").objects().map { TroubleBrewingPlayerStartingIdentity(it.getString("player"), it.getString("actual"),
                it.getString("shown"), TroubleBrewingStartingRoleCategory.valueOf(it.getString("category"))) }).also(TroubleBrewingSetupRotationRecordFactory::validate)
    }
    fun opening(j: JSONObject): HostOpeningSemanticAcceptanceV2? = j.optJSONObject("opening")?.let { v ->
        HostOpeningSemanticAcceptanceV2(OpeningSelectionV2(v.getString("gameId"), v.getLong("revision"), v.getLong("input"),
            v.getString("roster"), v.nullInt("drunk"), v.nullInt("redHerring"), v.optJSONArray("bluffs")?.strings(),
            v.getJSONArray("information").objects().map(::semantic).associateBy { it.sourceSeat }),
            v.getJSONArray("conditional").objects().map { HostOpeningConditionalIntentV2(it.getString("id"), it.optJSONObject("proposed")?.let(::semantic),
                it.getJSONArray("waiting").strings()) }, v.optJSONObject("memory")?.let(::memory)) }
    fun cursor(j: JSONObject): StorytellerStrategicContinuationV2.MemoryCursor? = j.optJSONObject("cursor")?.let { v ->
        StorytellerStrategicContinuationV2.MemoryCursor(v.getString("gameId"), v.getLong("revision"), v.getLong("input"),
            v.getLong("global"), v.getInt("version"), memory(v.getJSONObject("memory"))) }
    private fun semantic(v: HostOpeningSemanticValueV2) = JSONObject().put("seat", v.sourceSeat).put("ability", v.abilityRoleId)
        .put("effective", v.effectiveAbilityRoleId).put("basis", v.basis).put("mode", v.subjectMode).put("subjects", JSONArray(v.subjectSeats))
        .put("kind", v.observationKind).put("number", v.number).put("role", v.roleId).put("boolean", v.boolean)
    private fun semantic(j: JSONObject) = HostOpeningSemanticValueV2(j.getInt("seat"), j.getString("ability"), j.nullString("effective"),
        j.getString("basis"), j.getString("mode"), j.getJSONArray("subjects").ints(), j.getString("kind"), j.nullInt("number"), j.nullString("role"),
        if(j.isNull("boolean")) null else j.getBoolean("boolean"))
    private fun memory(v: HostOpeningStrategyMemoryV2) = JSONObject().put("tensions", JSONArray(v.tensions)).put("uncertainties", JSONArray(v.uncertainties))
        .put("intentions", JSONArray(v.intentions.map { JSONObject().put("trigger", it.trigger).put("approach", it.approach).put("risk", it.risk) }))
    private fun memory(j: JSONObject) = HostOpeningStrategyMemoryV2(j.getJSONArray("tensions").strings(), j.getJSONArray("intentions").objects().map {
        HostOpeningStrategicIntentionV2(it.getString("trigger"), it.getString("approach"), it.getString("risk")) }, j.getJSONArray("uncertainties").strings())
    private fun JSONObject.nullString(key: String) = if(isNull(key)) null else getString(key)
    private fun JSONObject.nullInt(key: String) = if(isNull(key)) null else getInt(key)
    private fun JSONArray.objects() = (0 until length()).map(::getJSONObject)
    private fun JSONArray.strings() = (0 until length()).map(::getString)
    private fun JSONArray.ints() = (0 until length()).map(::getInt)
}
