package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.Alignment
import com.codex.campboardgamehost.clocktower.domain.CharacterType
import com.codex.campboardgamehost.clocktower.domain.RegistrationFact
import com.codex.campboardgamehost.clocktower.domain.RegistrationQuestion
import com.codex.campboardgamehost.clocktower.domain.RegistrationReason
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.RuleCoverage
import com.codex.campboardgamehost.clocktower.domain.RulesetRef
import com.codex.campboardgamehost.clocktower.domain.ScriptId
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import java.math.BigInteger
import org.json.JSONArray
import org.json.JSONObject

/** Canonical, locale-independent schema-v2 JSON used by fixtures, replay and oracle adapters. */
object EpistemicSemanticJson {
    fun encode(value: FormalGameState): String = canonical(formalGameState(value))
    fun encode(value: InformationProposition): String = canonical(proposition(value))
    fun encode(value: EpistemicObservation): String = canonical(observation(value))
    fun encode(value: RecordedEpistemicObservation): String = canonical(recordedObservation(value))
    fun encode(value: StorytellerDecisionPoint): String = canonical(decisionPoint(value))
    fun encode(value: LegalChoiceSet): String = canonical(legalChoiceSet(value))
    fun encode(value: PlayerKnowledgeSnapshot): String = canonical(playerKnowledge(value))
    fun encode(value: RegistrationQuery): String = canonical(registrationQuery(value))
    fun encode(value: RegistrationProfile): String = canonical(registrationProfile(value))
    fun encode(value: WorldCardinality): String = canonical(worldCardinality(value))
    fun encode(value: PlayerWorldSetIdentity): String = canonical(playerWorldSetIdentity(value))

    fun decodeFormalGameState(json: String): FormalGameState = formalGameState(checkedRoot(json))
    fun decodeInformationProposition(json: String): InformationProposition = proposition(JSONObject(json))
    fun decodeEpistemicObservation(json: String): EpistemicObservation = observation(checkedRoot(json))
    fun decodeRecordedEpistemicObservation(json: String): RecordedEpistemicObservation = recordedObservation(checkedRoot(json))
    fun decodeStorytellerDecisionPoint(json: String): StorytellerDecisionPoint = decisionPoint(checkedRoot(json))
    fun decodeLegalChoiceSet(json: String): LegalChoiceSet = legalChoiceSet(checkedRoot(json))
    fun decodePlayerKnowledgeSnapshot(json: String): PlayerKnowledgeSnapshot = playerKnowledge(checkedRoot(json))
    fun decodeRegistrationQuery(json: String): RegistrationQuery = registrationQuery(checkedRoot(json))
    fun decodeRegistrationProfile(json: String): RegistrationProfile = registrationProfile(checkedRoot(json))
    fun decodeWorldCardinality(json: String): WorldCardinality = worldCardinality(checkedRoot(json))
    fun decodePlayerWorldSetIdentity(json: String): PlayerWorldSetIdentity = playerWorldSetIdentity(checkedRoot(json))

    /** Omits formal snapshot and caller-supplied IDs so storyteller secrets cannot perturb a player cache key. */
    fun encodeKnowledgeIdentityPayload(
        rulesetRef: RulesetRef,
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
    ): String = canonical(
        mapOf(
            "hypothesis" to hypothesis.name,
            "perceivedRole" to knowledge.perceivedRole.value,
            "privateObservations" to knowledge.privateObservations.map { identityObservation(it, knowledge.recipientSeat) },
            "publicObservations" to knowledge.publicObservations.map { identityObservation(it, knowledge.recipientSeat) },
            "recipientSeat" to knowledge.recipientSeat,
            "rulesetRef" to ruleset(rulesetRef),
            "schemaVersion" to EPISTEMIC_SCHEMA_VERSION,
            "setupKnowledge" to knowledge.setupKnowledge.map(::proposition),
        ),
    )

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
        is ActionFact.RoleChange -> mapOf("actionId" to value.actionId, "alignment" to value.alignment.name, "kind" to "role-change", "role" to value.role.value, "sequence" to value.sequence, "targetSeat" to value.targetSeat, "type" to value.type.name)
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
        )
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

    private fun identityObservation(value: EpistemicObservation, perspectiveSeat: Int): Map<String, Any?> = mapOf(
        "phase" to value.phase.name, "proposition" to proposition(value.proposition),
        "recipientSeats" to if (value.visibility == ObservationVisibility.PRIVATE) listOf(perspectiveSeat) else emptyList<Int>(),
        "reliability" to value.reliability.name, "round" to value.round,
        "sourceAbility" to value.sourceAbility?.value, "sourceSeat" to value.sourceSeat,
        "visibility" to value.visibility.name,
    )

    private fun decisionPoint(value: StorytellerDecisionPoint): Map<String, Any?> = mapOf(
        "candidateFamilyId" to value.candidateFamilyId?.value, "decisionPointId" to value.decisionPointId,
        "decisionTypeId" to value.decisionTypeId, "phase" to value.phase.name,
        "queryPropositions" to value.queryPropositions.map(::proposition), "recipientSeats" to value.recipientSeats.sorted(),
        "round" to value.round, "schemaVersion" to value.schemaVersion, "sequence" to value.sequence,
        "snapshotId" to value.snapshotId, "sourceAbility" to value.sourceAbility.value, "sourceSeat" to value.sourceSeat,
    )

    private fun legalChoiceSet(value: LegalChoiceSet): Map<String, Any?> = mapOf(
        "choiceSetId" to value.choiceSetId,
        "choices" to value.choices.map { mapOf("choiceId" to it.choiceId, "interactionId" to it.interactionId, "observation" to observation(it.observation), "registrations" to it.registrations.map(::registration)) },
        "decisionPointId" to value.decisionPointId, "rulesetRef" to ruleset(value.rulesetRef), "schemaVersion" to value.schemaVersion,
    )

    private fun registration(value: RegistrationFact): Map<String, Any?> = mapOf(
        "interactionId" to value.interactionId, "reason" to value.reason.name,
        "registeredAlignment" to value.registeredAlignment?.name, "registeredRole" to value.registeredRole?.value,
        "registeredType" to value.registeredType?.name, "registrationQuestion" to value.registrationQuestion.name,
        "subjectSeat" to value.subjectSeat,
    )

    private fun playerKnowledge(value: PlayerKnowledgeSnapshot): Map<String, Any?> = mapOf(
        "formalSnapshotId" to value.formalSnapshotId, "knowledgeSnapshotId" to value.knowledgeSnapshotId,
        "perceivedRole" to value.perceivedRole.value, "privateObservations" to value.privateObservations.map(::observation),
        "publicObservations" to value.publicObservations.map(::observation), "recipientSeat" to value.recipientSeat,
        "schemaVersion" to value.schemaVersion, "setupKnowledge" to value.setupKnowledge.map(::proposition),
    )

    private fun registrationQuery(value: RegistrationQuery): Map<String, Any?> = mapOf(
        "detectingAbility" to value.detectingAbility.value, "interactionId" to value.interactionId,
        "queriedAlignment" to value.queriedAlignment?.name, "queriedRole" to value.queriedRole?.value,
        "queriedType" to value.queriedType?.name, "question" to value.question.name,
        "schemaVersion" to EPISTEMIC_SCHEMA_VERSION, "subjectSeat" to value.subjectSeat,
        "timelinePoint" to timelinePoint(value.timelinePoint),
    )

    private fun timelinePoint(value: TimelinePoint): Map<String, Any?> = mapOf(
        "globalSequence" to value.globalSequence, "phase" to value.phase.name,
        "round" to value.round, "sequence" to value.sequence,
    )

    private fun registrationProfile(value: RegistrationProfile): Map<String, Any?> = mapOf(
        "alignment" to value.alignment?.name, "basis" to value.basis.name,
        "characterType" to value.characterType?.name, "role" to value.role?.value,
        "schemaVersion" to EPISTEMIC_SCHEMA_VERSION,
    )

    private fun worldCardinality(value: WorldCardinality): Map<String, Any?> = when (value) {
        is WorldCardinality.Exact -> mapOf("kind" to "exact", "schemaVersion" to EPISTEMIC_SCHEMA_VERSION, "value" to value.value.toString())
        is WorldCardinality.AtLeast -> mapOf("kind" to "at-least", "lowerBound" to value.lowerBound.toString(), "schemaVersion" to EPISTEMIC_SCHEMA_VERSION)
    }

    private fun playerWorldSetIdentity(value: PlayerWorldSetIdentity): Map<String, Any?> = mapOf(
        "hypothesis" to value.hypothesis.name, "recipientSeat" to value.recipientSeat,
        "schemaVersion" to value.schemaVersion, "value" to value.value,
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
        "role-change" -> ActionFact.RoleChange(json.getString("actionId"), json.getLong("sequence"), json.getInt("targetSeat"), RoleId(json.getString("role")), Alignment.valueOf(json.getString("alignment")), CharacterType.valueOf(json.getString("type")))
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
        "grimoire-state" -> InformationProposition.GrimoireState(json.getJSONArray("seats").objects().map(::grimoireSeatView))
        else -> error("Unknown InformationProposition kind: ${json.getString("kind")}")
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

    private fun decisionPoint(json: JSONObject): StorytellerDecisionPoint = StorytellerDecisionPoint(
        decisionPointId = json.getString("decisionPointId"), snapshotId = json.getString("snapshotId"),
        phase = StorytellerPhase.valueOf(json.getString("phase")), round = json.getInt("round"),
        sequence = json.getInt("sequence"), sourceSeat = json.nullableInt("sourceSeat"),
        sourceAbility = RoleId(json.getString("sourceAbility")), decisionTypeId = json.getString("decisionTypeId"),
        recipientSeats = json.getJSONArray("recipientSeats").ints().toSet(),
        queryPropositions = json.getJSONArray("queryPropositions").objects().map(::proposition),
        candidateFamilyId = json.nullableString("candidateFamilyId")?.let(::CandidateFamilyId),
        schemaVersion = json.getInt("schemaVersion"),
    )

    private fun legalChoiceSet(json: JSONObject): LegalChoiceSet = LegalChoiceSet(
        choiceSetId = json.getString("choiceSetId"), decisionPointId = json.getString("decisionPointId"),
        rulesetRef = ruleset(json.getJSONObject("rulesetRef")),
        choices = json.getJSONArray("choices").objects().map {
            LegalEpistemicChoice(it.getString("choiceId"), it.getString("interactionId"), observation(it.getJSONObject("observation")), it.getJSONArray("registrations").objects().map(::registration))
        }, schemaVersion = json.getInt("schemaVersion"),
    )

    private fun registration(json: JSONObject): RegistrationFact = RegistrationFact(
        interactionId = json.getString("interactionId"), subjectSeat = json.getInt("subjectSeat"),
        registeredRole = json.nullableString("registeredRole")?.let(::RoleId),
        registeredType = json.nullableString("registeredType")?.let { CharacterType.valueOf(it) },
        registeredAlignment = json.nullableString("registeredAlignment")?.let { Alignment.valueOf(it) },
        registrationQuestion = RegistrationQuestion.valueOf(json.getString("registrationQuestion")),
        reason = RegistrationReason.valueOf(json.getString("reason")),
    )

    private fun playerKnowledge(json: JSONObject): PlayerKnowledgeSnapshot = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = json.getString("knowledgeSnapshotId"), formalSnapshotId = json.getString("formalSnapshotId"),
        recipientSeat = json.getInt("recipientSeat"), perceivedRole = RoleId(json.getString("perceivedRole")),
        publicObservations = json.getJSONArray("publicObservations").objects().map(::observation),
        privateObservations = json.getJSONArray("privateObservations").objects().map(::observation),
        setupKnowledge = json.getJSONArray("setupKnowledge").objects().map(::proposition), schemaVersion = json.getInt("schemaVersion"),
    )

    private fun registrationQuery(json: JSONObject): RegistrationQuery = RegistrationQuery(
        subjectSeat = json.getInt("subjectSeat"), interactionId = json.getString("interactionId"),
        timelinePoint = timelinePoint(json.getJSONObject("timelinePoint")), detectingAbility = RoleId(json.getString("detectingAbility")),
        question = RegistrationQuestion.valueOf(json.getString("question")), queriedRole = json.nullableString("queriedRole")?.let(::RoleId),
        queriedType = json.nullableString("queriedType")?.let { CharacterType.valueOf(it) },
        queriedAlignment = json.nullableString("queriedAlignment")?.let { Alignment.valueOf(it) },
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
    private fun registrationProfile(json: JSONObject) = RegistrationProfile(
        json.nullableString("role")?.let(::RoleId), json.nullableString("characterType")?.let { CharacterType.valueOf(it) },
        json.nullableString("alignment")?.let { Alignment.valueOf(it) }, RegistrationBasis.valueOf(json.getString("basis")),
    )
    private fun worldCardinality(json: JSONObject): WorldCardinality = when (json.getString("kind")) {
        "exact" -> WorldCardinality.Exact(BigInteger(json.getString("value")))
        "at-least" -> WorldCardinality.AtLeast(BigInteger(json.getString("lowerBound")))
        else -> error("Unknown WorldCardinality kind: ${json.getString("kind")}")
    }
    private fun playerWorldSetIdentity(json: JSONObject) = PlayerWorldSetIdentity(
        json.getString("value"), json.getInt("recipientSeat"), EpistemicHypothesis.valueOf(json.getString("hypothesis")), json.getInt("schemaVersion"),
    )

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
    private fun JSONObject.nullableInt(key: String): Int? = if (!has(key) || isNull(key)) null else getInt(key)
}
