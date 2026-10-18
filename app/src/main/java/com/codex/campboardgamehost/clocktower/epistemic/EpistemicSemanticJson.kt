package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import org.json.JSONArray
import org.json.JSONObject

/** Canonical, locale-independent schema-v2 JSON used by fixtures, replay and oracle adapters. */
object EpistemicSemanticJson {
    fun encode(value: FormalGameState): String = canonical(formalGameState(value))
    fun encode(value: InformationProposition): String = canonical(proposition(value))
    fun encode(value: EpistemicObservation): String = canonical(observation(value))
    fun encode(value: RecordedEpistemicObservation): String = canonical(recordedObservation(value))
    fun encode(value: PlayerKnowledgeSnapshot): String = canonical(playerKnowledge(value))

    fun decodeFormalGameState(json: String): FormalGameState = formalGameState(checkedRoot(json))
    fun decodeInformationProposition(json: String): InformationProposition = proposition(JSONObject(json))
    fun decodeEpistemicObservation(json: String): EpistemicObservation = observation(checkedRoot(json))
    fun decodeRecordedEpistemicObservation(json: String): RecordedEpistemicObservation = recordedObservation(checkedRoot(json))
    fun decodePlayerKnowledgeSnapshot(json: String): PlayerKnowledgeSnapshot = playerKnowledge(checkedRoot(json))

    private fun formalGameState(value: FormalGameState): Map<String, Any?> = mapOf(
        "gameId" to value.gameId, "gameStateRevision" to value.gameStateRevision, "phase" to value.phase.name,
        "players" to value.players.map(::formalPlayer), "publicPropositions" to value.publicPropositions.map(::proposition),
        "round" to value.round, "rulesetRef" to ruleset(value.rulesetRef), "schemaVersion" to value.schemaVersion,
        "snapshotId" to value.snapshotId, "storytellerOnlyPropositions" to value.storytellerOnlyPropositions.map(::proposition),
        "timeline" to value.timeline.map(::actionFact),
    ) + formalActionTimelineBindingFields(value.actionTimelineBinding)

    private fun formalPlayer(value: FormalPlayerState): Map<String, Any?> = mapOf(
        "actualAlignment" to value.actualAlignment.name, "actualRole" to value.actualRole.value,
        "actualType" to value.actualType.name, "alive" to value.alive, "poisoned" to value.poisoned,
        "seat" to value.seat, "shownRole" to value.shownRole?.value,
    )

    private fun actionFact(value: ActionFact): Map<String, Any?> = when (value) {
        is ActionFact.Poison -> mapOf("actionId" to value.actionId, "kind" to "poison", "sequence" to value.sequence, "targetSeat" to value.targetSeat)
        is ActionFact.Protect -> mapOf("actionId" to value.actionId, "kind" to "protect", "sequence" to value.sequence, "targetSeat" to value.targetSeat)
        is ActionFact.Attack -> mapOf("actionId" to value.actionId, "kind" to "attack", "sequence" to value.sequence, "targetSeat" to value.targetSeat)
        is ActionFact.Execution -> mapOf("actionId" to value.actionId, "kind" to "execution", "sequence" to value.sequence, "targetSeat" to value.targetSeat)
        is ActionFact.Death -> mapOf("actionId" to value.actionId, "kind" to "death", "sequence" to value.sequence, "targetSeat" to value.targetSeat)
        is ActionFact.NoNightDeaths -> mapOf("actionId" to value.actionId, "kind" to "no-night-deaths", "sequence" to value.sequence)
        is ActionFact.NoExecution -> mapOf("actionId" to value.actionId, "kind" to "no-execution", "sequence" to value.sequence)
        is ActionFact.SlayerShot -> mapOf(
            "actionId" to value.actionId, "kind" to "slayer-shot", "sequence" to value.sequence,
            "claimantSeat" to value.claimantSeat, "targetSeat" to value.targetSeat,
            "abilityConsumed" to value.abilityConsumed, "hit" to value.hit,
        ) + (value.targetRegistersAsDemon?.let { mapOf("targetRegistersAsDemon" to it) } ?: emptyMap())
        is ActionFact.Nomination -> mapOf(
            "actionId" to value.actionId, "kind" to "nomination", "sequence" to value.sequence,
            "nominatorSeat" to value.nominatorSeat, "nomineeSeat" to value.nomineeSeat,
            "firstVirginNomination" to value.firstVirginNomination,
        ) + (value.nominatorRegistersAsTownsfolk?.let { mapOf("nominatorRegistersAsTownsfolk" to it) } ?: emptyMap())
        is ActionFact.Vote -> mapOf(
            "actionId" to value.actionId, "kind" to "vote", "sequence" to value.sequence,
            "nominatorSeat" to value.nominatorSeat, "nomineeSeat" to value.nomineeSeat,
            "voterSeats" to value.voterSeats, "ghostVoterSeats" to value.ghostVoterSeats,
        )
        is ActionFact.KlutzLearnedDeath -> mapOf(
            "actionId" to value.actionId, "kind" to "klutz-learned-death", "sequence" to value.sequence,
            "klutzSeat" to value.klutzSeat, "deathActionId" to value.deathActionId,
            "functioningWhenLearned" to value.functioningWhenLearned,
        )
        is ActionFact.KlutzChoice -> mapOf(
            "actionId" to value.actionId, "kind" to "klutz-choice", "sequence" to value.sequence,
            "klutzSeat" to value.klutzSeat, "chosenSeat" to value.chosenSeat,
            "learnedActionId" to value.learnedActionId,
        )
        is ActionFact.RoleChange -> mapOf("actionId" to value.actionId, "alignment" to value.alignment.name, "kind" to "role-change", "role" to value.role.value, "sequence" to value.sequence, "targetSeat" to value.targetSeat, "type" to value.type.name)
        is ActionFact.ButlerMasterChoice -> mapOf(
            "actionId" to value.actionId, "kind" to "butler-master",
            "sequence" to value.sequence, "butlerSeat" to value.butlerSeat, "masterSeat" to value.masterSeat,
        )
        is ActionFact.RedHerringChoice -> mapOf(
            "actionId" to value.actionId, "kind" to "red-herring-choice",
            "sequence" to value.sequence, "selectedSeat" to value.selectedSeat,
        )
        is ActionFact.MayorRedirectChoice -> mapOf(
            "actionId" to value.actionId, "kind" to "mayor-redirect",
            "sequence" to value.sequence, "mayorSeat" to value.mayorSeat, "targetSeat" to value.targetSeat,
        )
        is ActionFact.DemonBluffChoice -> mapOf(
            "actionId" to value.actionId, "kind" to "demon-bluff-choice",
            "sequence" to value.sequence, "roleIds" to value.roles.map { it.value },
        )
        is ActionFact.PhaseAdvance -> mapOf("actionId" to value.actionId, "kind" to "phase-advance", "phase" to value.phase.name, "round" to value.round, "sequence" to value.sequence)
    }

    private fun formalActionTimelineBindingFields(value: FormalActionTimelineBinding): Map<String, Any?> = when (value) {
        FormalActionTimelineBinding.Legacy -> emptyMap()
        is FormalActionTimelineBinding.Global -> mapOf(
            "actionTimelineBinding" to mapOf(
                "kind" to "global",
                "entries" to value.timeline.entries.map { entry ->
                    mapOf("actionId" to entry.fact.actionId, "point" to timelinePoint(entry.point))
                },
            ),
        )
    }

    private fun ruleset(value: RulesetRef): Map<String, Any?> = mapOf(
        "coverage" to value.coverage.name, "rulesetVersion" to value.rulesetVersion,
        "scriptContentHash" to value.scriptContentHash, "scriptId" to value.scriptId.value,
        "sourceRevision" to value.sourceRevision,
    )

    private fun proposition(value: InformationProposition): Map<String, Any?> = when (value) {
        is InformationProposition.RoleAt -> mapOf("kind" to "role-at", "role" to value.role.value, "seat" to value.seat)
        is InformationProposition.ShownRoleAt -> mapOf("kind" to "shown-role-at", "role" to value.role.value, "seat" to value.seat)
        is InformationProposition.AlignmentAt -> mapOf("alignment" to value.alignment.name, "kind" to "alignment-at", "seat" to value.seat)
        is InformationProposition.CharacterTypeAt -> mapOf("characterType" to value.characterType.name, "kind" to "character-type-at", "seat" to value.seat)
        is InformationProposition.AliveAt -> mapOf("alive" to value.alive, "kind" to "alive-at", "seat" to value.seat)
        is InformationProposition.AbilityStateAt -> mapOf("abilityRole" to value.abilityRole.value, "abilityState" to value.abilityState.name, "kind" to "ability-state-at", "seat" to value.seat)
        is InformationProposition.RoleInPlay -> mapOf("inPlay" to value.inPlay, "kind" to "role-in-play", "role" to value.role.value)
        is InformationProposition.PlayerCount -> mapOf("kind" to "player-count", "value" to value.value)
        is InformationProposition.SetupProfile -> mapOf("demons" to value.demons, "kind" to "setup-profile", "minions" to value.minions, "outsiders" to value.outsiders, "townsfolk" to value.townsfolk)
        is InformationProposition.AnyOf -> mapOf("alternatives" to value.alternatives.map(::proposition), "kind" to "any-of")
        is InformationProposition.AllOf -> mapOf("kind" to "all-of", "propositions" to value.propositions.map(::proposition))
        is InformationProposition.Not -> mapOf("kind" to "not", "proposition" to proposition(value.proposition))
        is InformationProposition.NumericResult -> mapOf("kind" to "numeric-result", "metric" to value.metric.name, "sourceSeat" to value.sourceSeat, "subjectSeats" to value.subjectSeats, "value" to value.value)
        is InformationProposition.BooleanResult -> mapOf("kind" to "boolean-result", "metric" to value.metric.name, "sourceSeat" to value.sourceSeat, "subjectSeats" to value.subjectSeats, "value" to value.value)
        is InformationProposition.GrimoireState -> mapOf(
            "kind" to "grimoire-state",
            "seats" to value.seats.sortedBy { it.seat }.map { seat ->
                mapOf(
                    "alive" to seat.alive,
                    "displayedRole" to seat.displayedRole.value,
                    "ruleReminderTokens" to seat.reminderTokens.sorted().map(::grimoireReminderToken),
                    "seat" to seat.seat,
                )
            },
        ) + grimoireTruthBindingFields(value.truthBinding)
    }

    private fun grimoireTruthBindingFields(value: GrimoireTruthBinding): Map<String, Any?> = when (value) {
        GrimoireTruthBinding.LEGACY_DISPLAY_ONLY -> emptyMap()
        GrimoireTruthBinding.VERIFIED_EXACT -> mapOf("truthBinding" to "verified-exact")
    }

    private fun grimoireReminderToken(value: GrimoireReminderTokenRef): Map<String, Any?> = mapOf(
        "label" to value.label,
        "occurrence" to value.occurrence,
        "scope" to value.scope.name,
        "sourceRole" to value.sourceRole.value,
    )

    private fun observation(value: EpistemicObservation): Map<String, Any?> = mapOf(
        "observationId" to value.observationId, "phase" to value.phase.name,
        "proposition" to proposition(value.proposition), "recipientSeats" to value.recipientSeats.sorted(),
        "reliability" to value.reliability.name, "round" to value.round, "schemaVersion" to value.schemaVersion,
        "sequence" to value.sequence, "snapshotId" to value.snapshotId,
        "sourceAbility" to value.sourceAbility?.value, "sourceSeat" to value.sourceSeat,
        "visibility" to value.visibility.name,
    ) + timelineBindingFields(value.timelineBinding)

    private fun recordedObservation(value: RecordedEpistemicObservation): Map<String, Any?> = mapOf(
        "phase" to value.phase.name, "proposition" to proposition(value.proposition),
        "recipientSeats" to value.recipientSeats.sorted(), "recordId" to value.recordId,
        "reliability" to value.reliability.name, "round" to value.round, "schemaVersion" to value.schemaVersion,
        "sequence" to value.sequence, "sourceAbility" to value.sourceAbility?.value,
        "sourceSeat" to value.sourceSeat, "visibility" to value.visibility.name,
    ) + timelineBindingFields(value.timelineBinding)

    private fun timelineBindingFields(value: ObservationTimelineBinding): Map<String, Any?> = when (value) {
        ObservationTimelineBinding.LegacyLocal -> emptyMap()
        is ObservationTimelineBinding.Global -> mapOf(
            "timelineBinding" to mapOf(
                "kind" to "global",
                "point" to timelinePoint(value.point),
            ),
        )
    }

    private fun playerKnowledge(value: PlayerKnowledgeSnapshot): Map<String, Any?> = mapOf(
        "formalSnapshotId" to value.formalSnapshotId, "knowledgeSnapshotId" to value.knowledgeSnapshotId,
        "perceivedRole" to value.perceivedRole.value, "privateObservations" to value.privateObservations.map(::observation),
        "publicObservations" to value.publicObservations.map(::observation), "recipientSeat" to value.recipientSeat,
        "schemaVersion" to value.schemaVersion, "setupKnowledge" to value.setupKnowledge.map(::proposition),
    )

    private fun timelinePoint(value: TimelinePoint): Map<String, Any?> = mapOf(
        "globalSequence" to value.globalSequence, "phase" to value.phase.name,
        "round" to value.round, "sequence" to value.sequence,
    )

    private fun formalGameState(json: JSONObject): FormalGameState {
        val timeline = json.optJSONArray("timeline")?.objects()?.map(::actionFact).orEmpty()
        return FormalGameState(
            snapshotId = json.getString("snapshotId"), gameId = json.getString("gameId"),
            gameStateRevision = json.getLong("gameStateRevision"), rulesetRef = ruleset(json.getJSONObject("rulesetRef")),
            phase = StorytellerPhase.valueOf(json.getString("phase")), round = json.getInt("round"),
            players = json.getJSONArray("players").objects().map(::formalPlayer),
            publicPropositions = json.getJSONArray("publicPropositions").objects().map(::proposition),
            storytellerOnlyPropositions = json.getJSONArray("storytellerOnlyPropositions").objects().map(::proposition),
            timeline = timeline,
            schemaVersion = json.getInt("schemaVersion"),
            actionTimelineBinding = formalActionTimelineBinding(json, timeline),
        )
    }

    private fun formalPlayer(json: JSONObject): FormalPlayerState = FormalPlayerState(
        seat = json.getInt("seat"), actualRole = RoleId(json.getString("actualRole")),
        actualAlignment = Alignment.valueOf(json.getString("actualAlignment")),
        actualType = CharacterType.valueOf(json.getString("actualType")),
        shownRole = json.nullableString("shownRole")?.let(::RoleId), alive = json.getBoolean("alive"),
        poisoned = json.getBoolean("poisoned"),
    )

    private fun actionFact(json: JSONObject): ActionFact = when (json.getString("kind")) {
        "poison" -> ActionFact.Poison(json.getString("actionId"), json.getLong("sequence"), json.nullableInt("targetSeat"))
        "protect" -> ActionFact.Protect(json.getString("actionId"), json.getLong("sequence"), json.getInt("targetSeat"))
        "attack" -> ActionFact.Attack(json.getString("actionId"), json.getLong("sequence"), json.getInt("targetSeat"))
        "execution" -> ActionFact.Execution(json.getString("actionId"), json.getLong("sequence"), json.getInt("targetSeat"))
        "death" -> ActionFact.Death(json.getString("actionId"), json.getLong("sequence"), json.getInt("targetSeat"))
        "no-night-deaths" -> ActionFact.NoNightDeaths(json.getString("actionId"), json.getLong("sequence"))
        "no-execution" -> ActionFact.NoExecution(json.getString("actionId"), json.getLong("sequence"))
        "slayer-shot" -> ActionFact.SlayerShot(
            json.getString("actionId"), json.getLong("sequence"),
            json.getInt("claimantSeat"), json.getInt("targetSeat"),
            json.getBoolean("abilityConsumed"), json.getBoolean("hit"),
            json.nullableBoolean("targetRegistersAsDemon"),
        )
        "nomination" -> ActionFact.Nomination(
            json.getString("actionId"), json.getLong("sequence"),
            json.getInt("nominatorSeat"), json.getInt("nomineeSeat"),
            json.getBoolean("firstVirginNomination"),
            json.nullableBoolean("nominatorRegistersAsTownsfolk"),
        )
        "vote" -> ActionFact.Vote(
            json.getString("actionId"), json.getLong("sequence"),
            json.getInt("nominatorSeat"), json.getInt("nomineeSeat"),
            json.getJSONArray("voterSeats").let { a -> (0 until a.length()).map(a::getInt) },
            json.getJSONArray("ghostVoterSeats").let { a -> (0 until a.length()).map(a::getInt) },
        )
        "klutz-learned-death" -> ActionFact.KlutzLearnedDeath(
            json.getString("actionId"), json.getLong("sequence"),
            json.getInt("klutzSeat"), json.getString("deathActionId"),
            json.getBoolean("functioningWhenLearned"),
        )
        "klutz-choice" -> ActionFact.KlutzChoice(
            json.getString("actionId"), json.getLong("sequence"),
            json.getInt("klutzSeat"), json.getInt("chosenSeat"),
            json.getString("learnedActionId"),
        )
        "role-change" -> ActionFact.RoleChange(json.getString("actionId"), json.getLong("sequence"), json.getInt("targetSeat"), RoleId(json.getString("role")), Alignment.valueOf(json.getString("alignment")), CharacterType.valueOf(json.getString("type")))
        "red-herring-choice" -> ActionFact.RedHerringChoice(
            json.getString("actionId"), json.getLong("sequence"),
            if (json.isNull("selectedSeat")) null else json.getInt("selectedSeat"),
        )
        "demon-bluff-choice" -> ActionFact.DemonBluffChoice(
            json.getString("actionId"), json.getLong("sequence"),
            json.getJSONArray("roleIds").let { arr ->
                (0 until arr.length()).map { i -> RoleId(arr.getString(i)) }
            },
        )
        "phase-advance" -> ActionFact.PhaseAdvance(json.getString("actionId"), json.getLong("sequence"), StorytellerPhase.valueOf(json.getString("phase")), json.getInt("round"))
        else -> error("Unknown B4 action fact kind: ${json.getString("kind")}")
    }

    private fun formalActionTimelineBinding(
        json: JSONObject,
        timeline: List<ActionFact>,
    ): FormalActionTimelineBinding {
        if (!json.has("actionTimelineBinding")) return FormalActionTimelineBinding.Legacy
        require(!json.isNull("actionTimelineBinding")) {
            "actionTimelineBinding cannot be null when present."
        }
        val binding = json.getJSONObject("actionTimelineBinding")
        return when (binding.getString("kind")) {
            "global" -> {
                val persistedEntries = binding.getJSONArray("entries").objects()
                val actionIds = persistedEntries.map { it.getString("actionId") }
                require(actionIds.distinct().size == actionIds.size) {
                    "Global formal action timeline binding cannot contain duplicate action IDs."
                }
                val factsById = timeline.associateBy(ActionFact::actionId)
                require(actionIds.toSet() == factsById.keys) {
                    "Global formal action timeline binding must reference every persisted action exactly once."
                }
                FormalActionTimelineBinding.Global(
                    ActionFactTimeline(
                        persistedEntries.map { entry ->
                            TimelineBoundActionFact(
                                fact = factsById.getValue(entry.getString("actionId")),
                                point = timelinePoint(entry.getJSONObject("point")),
                            )
                        },
                    ),
                )
            }
            else -> throw IllegalArgumentException(
                "Unknown formal action timeline binding kind: ${binding.getString("kind")}",
            )
        }
    }

    private fun ruleset(json: JSONObject): RulesetRef = RulesetRef(
        scriptId = ScriptId(json.getString("scriptId")), scriptContentHash = json.getString("scriptContentHash"),
        rulesetVersion = json.getString("rulesetVersion"), sourceRevision = json.getString("sourceRevision"),
        coverage = RuleCoverage.valueOf(json.getString("coverage")),
    )

    private fun proposition(json: JSONObject): InformationProposition = when (json.getString("kind")) {
        "role-at" -> InformationProposition.RoleAt(json.getInt("seat"), RoleId(json.getString("role")))
        "shown-role-at" -> InformationProposition.ShownRoleAt(json.getInt("seat"), RoleId(json.getString("role")))
        "alignment-at" -> InformationProposition.AlignmentAt(json.getInt("seat"), Alignment.valueOf(json.getString("alignment")))
        "character-type-at" -> InformationProposition.CharacterTypeAt(json.getInt("seat"), CharacterType.valueOf(json.getString("characterType")))
        "alive-at" -> InformationProposition.AliveAt(json.getInt("seat"), json.getBoolean("alive"))
        "ability-state-at" -> InformationProposition.AbilityStateAt(json.getInt("seat"), RoleId(json.getString("abilityRole")), AbilityState.valueOf(json.getString("abilityState")))
        "role-in-play" -> InformationProposition.RoleInPlay(RoleId(json.getString("role")), json.getBoolean("inPlay"))
        "player-count" -> InformationProposition.PlayerCount(json.getInt("value"))
        "setup-profile" -> InformationProposition.SetupProfile(json.getInt("townsfolk"), json.getInt("outsiders"), json.getInt("minions"), json.getInt("demons"))
        "any-of" -> InformationProposition.AnyOf(json.getJSONArray("alternatives").objects().map(::proposition))
        "all-of" -> InformationProposition.AllOf(json.getJSONArray("propositions").objects().map(::proposition))
        "not" -> InformationProposition.Not(proposition(json.getJSONObject("proposition")))
        "numeric-result" -> InformationProposition.NumericResult(NumericMetric.valueOf(json.getString("metric")), json.getInt("sourceSeat"), json.getJSONArray("subjectSeats").ints(), json.getInt("value"))
        "boolean-result" -> InformationProposition.BooleanResult(BooleanMetric.valueOf(json.getString("metric")), json.getInt("sourceSeat"), json.getJSONArray("subjectSeats").ints(), json.getBoolean("value"))
        "grimoire-state" -> InformationProposition.GrimoireState(
            seats = json.getJSONArray("seats").objects().map(::grimoireSeatView),
            truthBinding = grimoireTruthBinding(json),
        )
        else -> error("Unknown InformationProposition kind: ${json.getString("kind")}")
    }

    private fun grimoireTruthBinding(json: JSONObject): GrimoireTruthBinding {
        if (!json.has("truthBinding")) return GrimoireTruthBinding.LEGACY_DISPLAY_ONLY
        require(!json.isNull("truthBinding")) {
            "Grimoire truthBinding cannot be null when present."
        }
        return when (val binding = json.getString("truthBinding")) {
            "verified-exact" -> GrimoireTruthBinding.VERIFIED_EXACT
            else -> throw IllegalArgumentException("Unknown Grimoire truthBinding: $binding")
        }
    }

    private fun grimoireSeatView(json: JSONObject): GrimoireSeatView {
        if (json.has("reminderTokens")) {
            val legacy = json.getJSONArray("reminderTokens")
            require(legacy.length() == 0) {
                "Legacy schema-v2 Grimoire reminderTokens require explicit migration; raw token IDs cannot be inferred as rule-backed reminder-token identity."
            }
        }
        val tokens = if (json.has("ruleReminderTokens")) {
            json.getJSONArray("ruleReminderTokens").objects().map(::grimoireReminderToken)
        } else {
            emptyList()
        }
        return GrimoireSeatView(
            seat = json.getInt("seat"),
            displayedRole = RoleId(json.getString("displayedRole")),
            alive = json.getBoolean("alive"),
            reminderTokens = tokens,
        )
    }

    private fun grimoireReminderToken(json: JSONObject): GrimoireReminderTokenRef = GrimoireReminderTokenRef(
        sourceRole = RoleId(json.getString("sourceRole")),
        scope = try {
            GrimoireReminderTokenScope.valueOf(json.getString("scope"))
        } catch (error: IllegalArgumentException) {
            throw IllegalArgumentException("Unknown Grimoire reminder-token scope: ${json.getString("scope")}", error)
        },
        label = json.getString("label"),
        occurrence = json.getInt("occurrence"),
    )

    private fun observation(json: JSONObject): EpistemicObservation = EpistemicObservation(
        observationId = json.getString("observationId"), snapshotId = json.getString("snapshotId"),
        phase = StorytellerPhase.valueOf(json.getString("phase")), round = json.getInt("round"),
        sequence = json.getInt("sequence"), sourceSeat = json.nullableInt("sourceSeat"),
        sourceAbility = json.nullableString("sourceAbility")?.let(::RoleId),
        visibility = ObservationVisibility.valueOf(json.getString("visibility")),
        recipientSeats = json.getJSONArray("recipientSeats").ints().toSet(),
        reliability = ObservationReliability.valueOf(json.getString("reliability")),
        proposition = proposition(json.getJSONObject("proposition")), schemaVersion = json.getInt("schemaVersion"),
        timelineBinding = observationTimelineBinding(json),
    )

    private fun recordedObservation(json: JSONObject): RecordedEpistemicObservation = RecordedEpistemicObservation(
        recordId = json.getString("recordId"), phase = StorytellerPhase.valueOf(json.getString("phase")),
        round = json.getInt("round"), sequence = json.getInt("sequence"),
        sourceSeat = json.nullableInt("sourceSeat"), sourceAbility = json.nullableString("sourceAbility")?.let(::RoleId),
        visibility = ObservationVisibility.valueOf(json.getString("visibility")),
        recipientSeats = json.getJSONArray("recipientSeats").ints().toSet(),
        reliability = ObservationReliability.valueOf(json.getString("reliability")),
        proposition = proposition(json.getJSONObject("proposition")), schemaVersion = json.getInt("schemaVersion"),
        timelineBinding = observationTimelineBinding(json),
    )

    private fun observationTimelineBinding(json: JSONObject): ObservationTimelineBinding {
        if (!json.has("timelineBinding")) return ObservationTimelineBinding.LegacyLocal
        require(!json.isNull("timelineBinding")) {
            "timelineBinding cannot be null when present."
        }
        val binding = json.getJSONObject("timelineBinding")
        return when (binding.getString("kind")) {
            "global" -> ObservationTimelineBinding.Global(timelinePoint(binding.getJSONObject("point")))
            else -> throw IllegalArgumentException("Unknown observation timeline binding kind: ${binding.getString("kind")}")
        }
    }

    private fun playerKnowledge(json: JSONObject): PlayerKnowledgeSnapshot = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = json.getString("knowledgeSnapshotId"), formalSnapshotId = json.getString("formalSnapshotId"),
        recipientSeat = json.getInt("recipientSeat"), perceivedRole = RoleId(json.getString("perceivedRole")),
        publicObservations = json.getJSONArray("publicObservations").objects().map(::observation),
        privateObservations = json.getJSONArray("privateObservations").objects().map(::observation),
        setupKnowledge = json.getJSONArray("setupKnowledge").objects().map(::proposition), schemaVersion = json.getInt("schemaVersion"),
    )

    private fun timelinePoint(json: JSONObject): TimelinePoint {
        require(json.has("globalSequence") && !json.isNull("globalSequence")) {
            "Legacy schema-v2 TimelinePoint without globalSequence requires explicit migration; " +
                "globalSequence cannot be inferred from local sequence."
        }
        return TimelinePoint(
            phase = StorytellerPhase.valueOf(json.getString("phase")),
            round = json.getInt("round"),
            sequence = json.getInt("sequence"),
            globalSequence = json.getLong("globalSequence"),
        )
    }
    private fun checkedRoot(json: String): JSONObject = JSONObject(json).also {
        requireSchemaVersion(it.getInt("schemaVersion"))
    }

    private fun canonical(value: Any?): String = when (value) {
        null -> "null"
        is String -> JSONObject.quote(value)
        is Boolean, is Int, is Long -> value.toString()
        is Map<*, *> -> value.entries.map { (key, item) -> requireNotNull(key) as String to item }.sortedBy { it.first }
            .joinToString(prefix = "{", postfix = "}") { (key, item) -> "${JSONObject.quote(key)}:${canonical(item)}" }
        is Iterable<*> -> value.joinToString(prefix = "[", postfix = "]") { canonical(it) }
        else -> error("Unsupported canonical JSON value: ${value::class.java.name}")
    }

    private fun JSONArray.objects(): List<JSONObject> = List(length(), ::getJSONObject)
    private fun JSONArray.ints(): List<Int> = List(length(), ::getInt)
    private fun JSONArray.strings(): List<String> = List(length(), ::getString)
    private fun JSONObject.nullableString(key: String): String? = if (!has(key) || isNull(key)) null else getString(key)
    private fun JSONObject.nullableBoolean(key: String): Boolean? = if (!has(key) || isNull(key)) null else getBoolean(key)
    private fun JSONObject.nullableInt(key: String): Int? = if (!has(key) || isNull(key)) null else getInt(key)
}
