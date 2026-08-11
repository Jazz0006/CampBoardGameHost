package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.AbilityState
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
import org.json.JSONArray
import org.json.JSONObject

/** Canonical, locale-independent JSON used by fixtures, replay and oracle adapters. */
object EpistemicSemanticJson {
    fun encode(value: FormalGameState): String = canonical(formalGameState(value))
    fun encode(value: InformationProposition): String = canonical(proposition(value))
    fun encode(value: EpistemicObservation): String = canonical(observation(value))
    fun encode(value: StorytellerDecisionPoint): String = canonical(decisionPoint(value))
    fun encode(value: LegalChoiceSet): String = canonical(legalChoiceSet(value))
    fun encode(value: PlayerKnowledgeSnapshot): String = canonical(playerKnowledge(value))

    fun decodeFormalGameState(json: String): FormalGameState = formalGameState(JSONObject(json))
    fun decodeInformationProposition(json: String): InformationProposition = proposition(JSONObject(json))
    fun decodeEpistemicObservation(json: String): EpistemicObservation = observation(JSONObject(json))
    fun decodeStorytellerDecisionPoint(json: String): StorytellerDecisionPoint = decisionPoint(JSONObject(json))
    fun decodeLegalChoiceSet(json: String): LegalChoiceSet = legalChoiceSet(JSONObject(json))
    fun decodePlayerKnowledgeSnapshot(json: String): PlayerKnowledgeSnapshot = playerKnowledge(JSONObject(json))

    private fun formalGameState(value: FormalGameState): Map<String, Any?> = mapOf(
        "gameId" to value.gameId,
        "gameStateRevision" to value.gameStateRevision,
        "phase" to value.phase.name,
        "players" to value.players.map(::formalPlayer),
        "publicPropositions" to value.publicPropositions.map(::proposition),
        "round" to value.round,
        "rulesetRef" to ruleset(value.rulesetRef),
        "schemaVersion" to value.schemaVersion,
        "snapshotId" to value.snapshotId,
        "storytellerOnlyPropositions" to value.storytellerOnlyPropositions.map(::proposition),
    )

    private fun formalPlayer(value: FormalPlayerState): Map<String, Any?> = mapOf(
        "actualAlignment" to value.actualAlignment.name,
        "actualRole" to value.actualRole.value,
        "actualType" to value.actualType.name,
        "alive" to value.alive,
        "poisoned" to value.poisoned,
        "seat" to value.seat,
        "shownRole" to value.shownRole?.value,
    )

    private fun ruleset(value: RulesetRef): Map<String, Any?> = mapOf(
        "coverage" to value.coverage.name,
        "rulesetVersion" to value.rulesetVersion,
        "scriptContentHash" to value.scriptContentHash,
        "scriptId" to value.scriptId.value,
        "sourceRevision" to value.sourceRevision,
    )

    private fun proposition(value: InformationProposition): Map<String, Any?> = when (value) {
        is InformationProposition.RoleAt -> mapOf(
            "kind" to "role-at",
            "role" to value.role.value,
            "seat" to value.seat,
        )
        is InformationProposition.AlignmentAt -> mapOf(
            "alignment" to value.alignment.name,
            "kind" to "alignment-at",
            "seat" to value.seat,
        )
        is InformationProposition.CharacterTypeAt -> mapOf(
            "characterType" to value.characterType.name,
            "kind" to "character-type-at",
            "seat" to value.seat,
        )
        is InformationProposition.AliveAt -> mapOf(
            "alive" to value.alive,
            "kind" to "alive-at",
            "seat" to value.seat,
        )
        is InformationProposition.AbilityStateAt -> mapOf(
            "abilityRole" to value.abilityRole.value,
            "abilityState" to value.abilityState.name,
            "kind" to "ability-state-at",
            "seat" to value.seat,
        )
        is InformationProposition.RoleInPlay -> mapOf(
            "inPlay" to value.inPlay,
            "kind" to "role-in-play",
            "role" to value.role.value,
        )
        is InformationProposition.SetupProfile -> mapOf(
            "demons" to value.demons,
            "kind" to "setup-profile",
            "minions" to value.minions,
            "outsiders" to value.outsiders,
            "townsfolk" to value.townsfolk,
        )
        is InformationProposition.AnyOf -> mapOf(
            "alternatives" to value.alternatives.map(::proposition),
            "kind" to "any-of",
        )
        is InformationProposition.AllOf -> mapOf(
            "kind" to "all-of",
            "propositions" to value.propositions.map(::proposition),
        )
        is InformationProposition.Not -> mapOf(
            "kind" to "not",
            "proposition" to proposition(value.proposition),
        )
        is InformationProposition.NumericResult -> mapOf(
            "kind" to "numeric-result",
            "metric" to value.metric.name,
            "sourceSeat" to value.sourceSeat,
            "subjectSeats" to value.subjectSeats,
            "value" to value.value,
        )
    }

    private fun observation(value: EpistemicObservation): Map<String, Any?> = mapOf(
        "observationId" to value.observationId,
        "phase" to value.phase.name,
        "proposition" to proposition(value.proposition),
        "recipientSeats" to value.recipientSeats.sorted(),
        "reliability" to value.reliability.name,
        "round" to value.round,
        "schemaVersion" to value.schemaVersion,
        "sequence" to value.sequence,
        "snapshotId" to value.snapshotId,
        "sourceAbility" to value.sourceAbility?.value,
        "sourceSeat" to value.sourceSeat,
        "visibility" to value.visibility.name,
    )

    private fun decisionPoint(value: StorytellerDecisionPoint): Map<String, Any?> = mapOf(
        "decisionPointId" to value.decisionPointId,
        "decisionTypeId" to value.decisionTypeId,
        "phase" to value.phase.name,
        "queryPropositions" to value.queryPropositions.map(::proposition),
        "recipientSeats" to value.recipientSeats.sorted(),
        "round" to value.round,
        "schemaVersion" to value.schemaVersion,
        "sequence" to value.sequence,
        "snapshotId" to value.snapshotId,
        "sourceAbility" to value.sourceAbility.value,
        "sourceSeat" to value.sourceSeat,
    )

    private fun legalChoiceSet(value: LegalChoiceSet): Map<String, Any?> = mapOf(
        "choiceSetId" to value.choiceSetId,
        "choices" to value.choices.map { choice ->
            mapOf(
                "choiceId" to choice.choiceId,
                "observation" to observation(choice.observation),
                "registrations" to choice.registrations.map(::registration),
            )
        },
        "decisionPointId" to value.decisionPointId,
        "rulesetRef" to ruleset(value.rulesetRef),
        "schemaVersion" to value.schemaVersion,
    )

    private fun registration(value: RegistrationFact): Map<String, Any?> = mapOf(
        "interactionId" to value.interactionId,
        "reason" to value.reason.name,
        "registeredAlignment" to value.registeredAlignment?.name,
        "registeredRole" to value.registeredRole?.value,
        "registeredType" to value.registeredType?.name,
        "registrationQuestion" to value.registrationQuestion.name,
        "subjectSeat" to value.subjectSeat,
    )

    private fun playerKnowledge(value: PlayerKnowledgeSnapshot): Map<String, Any?> = mapOf(
        "formalSnapshotId" to value.formalSnapshotId,
        "knowledgeSnapshotId" to value.knowledgeSnapshotId,
        "perceivedRole" to value.perceivedRole.value,
        "privateObservations" to value.privateObservations.map(::observation),
        "publicObservations" to value.publicObservations.map(::observation),
        "recipientSeat" to value.recipientSeat,
        "schemaVersion" to value.schemaVersion,
        "setupKnowledge" to value.setupKnowledge.map(::proposition),
    )

    private fun formalGameState(json: JSONObject): FormalGameState = FormalGameState(
        snapshotId = json.getString("snapshotId"),
        gameId = json.getString("gameId"),
        gameStateRevision = json.getLong("gameStateRevision"),
        rulesetRef = ruleset(json.getJSONObject("rulesetRef")),
        phase = StorytellerPhase.valueOf(json.getString("phase")),
        round = json.getInt("round"),
        players = json.getJSONArray("players").objects().map(::formalPlayer),
        publicPropositions = json.getJSONArray("publicPropositions").objects().map(::proposition),
        storytellerOnlyPropositions = json.getJSONArray("storytellerOnlyPropositions").objects().map(::proposition),
        schemaVersion = json.getInt("schemaVersion"),
    )

    private fun formalPlayer(json: JSONObject): FormalPlayerState = FormalPlayerState(
        seat = json.getInt("seat"),
        actualRole = RoleId(json.getString("actualRole")),
        actualAlignment = Alignment.valueOf(json.getString("actualAlignment")),
        actualType = CharacterType.valueOf(json.getString("actualType")),
        shownRole = json.nullableString("shownRole")?.let(::RoleId),
        alive = json.getBoolean("alive"),
        poisoned = json.getBoolean("poisoned"),
    )

    private fun ruleset(json: JSONObject): RulesetRef = RulesetRef(
        scriptId = ScriptId(json.getString("scriptId")),
        scriptContentHash = json.getString("scriptContentHash"),
        rulesetVersion = json.getString("rulesetVersion"),
        sourceRevision = json.getString("sourceRevision"),
        coverage = RuleCoverage.valueOf(json.getString("coverage")),
    )

    private fun proposition(json: JSONObject): InformationProposition = when (json.getString("kind")) {
        "role-at" -> InformationProposition.RoleAt(json.getInt("seat"), RoleId(json.getString("role")))
        "alignment-at" -> InformationProposition.AlignmentAt(
            json.getInt("seat"),
            Alignment.valueOf(json.getString("alignment")),
        )
        "character-type-at" -> InformationProposition.CharacterTypeAt(
            json.getInt("seat"),
            CharacterType.valueOf(json.getString("characterType")),
        )
        "alive-at" -> InformationProposition.AliveAt(json.getInt("seat"), json.getBoolean("alive"))
        "ability-state-at" -> InformationProposition.AbilityStateAt(
            seat = json.getInt("seat"),
            abilityRole = RoleId(json.getString("abilityRole")),
            abilityState = AbilityState.valueOf(json.getString("abilityState")),
        )
        "role-in-play" -> InformationProposition.RoleInPlay(
            RoleId(json.getString("role")),
            json.getBoolean("inPlay"),
        )
        "setup-profile" -> InformationProposition.SetupProfile(
            townsfolk = json.getInt("townsfolk"),
            outsiders = json.getInt("outsiders"),
            minions = json.getInt("minions"),
            demons = json.getInt("demons"),
        )
        "any-of" -> InformationProposition.AnyOf(
            json.getJSONArray("alternatives").objects().map(::proposition),
        )
        "all-of" -> InformationProposition.AllOf(
            json.getJSONArray("propositions").objects().map(::proposition),
        )
        "not" -> InformationProposition.Not(proposition(json.getJSONObject("proposition")))
        "numeric-result" -> InformationProposition.NumericResult(
            metric = NumericMetric.valueOf(json.getString("metric")),
            sourceSeat = json.getInt("sourceSeat"),
            subjectSeats = json.getJSONArray("subjectSeats").ints(),
            value = json.getInt("value"),
        )
        else -> error("Unknown InformationProposition kind: ${json.getString("kind")}")
    }

    private fun observation(json: JSONObject): EpistemicObservation = EpistemicObservation(
        observationId = json.getString("observationId"),
        snapshotId = json.getString("snapshotId"),
        phase = StorytellerPhase.valueOf(json.getString("phase")),
        round = json.getInt("round"),
        sequence = json.getInt("sequence"),
        sourceSeat = json.nullableInt("sourceSeat"),
        sourceAbility = json.nullableString("sourceAbility")?.let(::RoleId),
        visibility = ObservationVisibility.valueOf(json.getString("visibility")),
        recipientSeats = json.getJSONArray("recipientSeats").ints().toSet(),
        reliability = ObservationReliability.valueOf(json.getString("reliability")),
        proposition = proposition(json.getJSONObject("proposition")),
        schemaVersion = json.getInt("schemaVersion"),
    )

    private fun decisionPoint(json: JSONObject): StorytellerDecisionPoint = StorytellerDecisionPoint(
        decisionPointId = json.getString("decisionPointId"),
        snapshotId = json.getString("snapshotId"),
        phase = StorytellerPhase.valueOf(json.getString("phase")),
        round = json.getInt("round"),
        sequence = json.getInt("sequence"),
        sourceSeat = json.nullableInt("sourceSeat"),
        sourceAbility = RoleId(json.getString("sourceAbility")),
        decisionTypeId = json.getString("decisionTypeId"),
        recipientSeats = json.getJSONArray("recipientSeats").ints().toSet(),
        queryPropositions = json.getJSONArray("queryPropositions").objects().map(::proposition),
        schemaVersion = json.getInt("schemaVersion"),
    )

    private fun legalChoiceSet(json: JSONObject): LegalChoiceSet = LegalChoiceSet(
        choiceSetId = json.getString("choiceSetId"),
        decisionPointId = json.getString("decisionPointId"),
        rulesetRef = ruleset(json.getJSONObject("rulesetRef")),
        choices = json.getJSONArray("choices").objects().map { choice ->
            LegalEpistemicChoice(
                choiceId = choice.getString("choiceId"),
                observation = observation(choice.getJSONObject("observation")),
                registrations = choice.getJSONArray("registrations").objects().map(::registration),
            )
        },
        schemaVersion = json.getInt("schemaVersion"),
    )

    private fun registration(json: JSONObject): RegistrationFact = RegistrationFact(
        interactionId = json.getString("interactionId"),
        subjectSeat = json.getInt("subjectSeat"),
        registeredRole = json.nullableString("registeredRole")?.let(::RoleId),
        registeredType = json.nullableString("registeredType")?.let { CharacterType.valueOf(it) },
        registeredAlignment = json.nullableString("registeredAlignment")?.let { Alignment.valueOf(it) },
        registrationQuestion = RegistrationQuestion.valueOf(json.getString("registrationQuestion")),
        reason = RegistrationReason.valueOf(json.getString("reason")),
    )

    private fun playerKnowledge(json: JSONObject): PlayerKnowledgeSnapshot = PlayerKnowledgeSnapshot(
        knowledgeSnapshotId = json.getString("knowledgeSnapshotId"),
        formalSnapshotId = json.getString("formalSnapshotId"),
        recipientSeat = json.getInt("recipientSeat"),
        perceivedRole = RoleId(json.getString("perceivedRole")),
        publicObservations = json.getJSONArray("publicObservations").objects().map(::observation),
        privateObservations = json.getJSONArray("privateObservations").objects().map(::observation),
        setupKnowledge = json.getJSONArray("setupKnowledge").objects().map(::proposition),
        schemaVersion = json.getInt("schemaVersion"),
    )

    private fun canonical(value: Any?): String = when (value) {
        null -> "null"
        is String -> JSONObject.quote(value)
        is Boolean, is Int, is Long -> value.toString()
        is Map<*, *> -> value.entries
            .map { (key, item) -> requireNotNull(key) as String to item }
            .sortedBy { it.first }
            .joinToString(prefix = "{", postfix = "}") { (key, item) ->
                "${JSONObject.quote(key)}:${canonical(item)}"
            }
        is Iterable<*> -> value.joinToString(prefix = "[", postfix = "]") { canonical(it) }
        else -> error("Unsupported canonical JSON value: ${value::class.java.name}")
    }

    private fun JSONArray.objects(): List<JSONObject> = List(length(), ::getJSONObject)
    private fun JSONArray.ints(): List<Int> = List(length(), ::getInt)

    private fun JSONObject.nullableString(key: String): String? =
        if (isNull(key)) null else getString(key)

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key)) null else getInt(key)
}
