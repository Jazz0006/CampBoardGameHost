package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.TroubleBrewingGameSnapshotJsonCodec
import com.codex.campboardgamehost.clocktower.domain.*
import com.codex.campboardgamehost.clocktower.epistemic.*
import com.codex.campboardgamehost.clocktower.session.*
import java.security.MessageDigest
import org.json.JSONArray
import org.json.JSONObject

/**
 * Current-format, optional and strictly typed causal-decision sidecar.
 * Missing key is LEGACY_UNRECORDED, not a verified empty complete history.
 * Never use display text, candidate registration witnesses, or current revision as old timing.
 */
internal object ClocktowerCausalJournalPersistence {
    const val ROOT_KEY = "clocktowerCausalDecisionJournalV1"
    private const val VERSION = 1

    fun encode(archive: StorytellerCausalJournalArchiveV1): JSONObject = JSONObject().apply {
        put("version", VERSION)
        put("gameId", archive.gameId)
        put("records", JSONArray().apply {
            archive.records.forEach { record ->
                put(when (record) {
                    is StorytellerCausalJournalRecordV1.Captured -> JSONObject().apply {
                        put("kind", "capture")
                        val frozen = record.frozen
                        put("identity", JSONObject().apply {
                            put("gameId", frozen.identity.gameId)
                            put("scriptId", frozen.identity.scriptId)
                            put("decisionTypeId", frozen.identity.decisionTypeId)
                            put("decisionId", frozen.identity.decisionId)
                        })
                        put("gameRevision", frozen.revision.gameStateRevision)
                        put("playerRevision", frozen.revision.playerInputRevision)
                        put("exclusiveGlobalSequence", frozen.exclusiveGlobalSequence)
                        put("gameSnapshot", TroubleBrewingGameSnapshotJsonCodec.encode(
                            TroubleBrewingGameSnapshotV1(
                                gameId = frozen.snapshotIdentity.gameId,
                                gameSeed = frozen.snapshotIdentity.gameSeed,
                                position = frozen.snapshotIdentity.position,
                                grimoireSeats = frozen.snapshotIdentity.seats,
                                setupState = frozen.snapshotIdentity.setupState,
                            ),
                        ))
                        put("players", JSONArray().apply {
                            frozen.players.forEach { player -> put(JSONObject().apply {
                                put("seat", player.seat)
                                put("experienceLevel", player.experienceLevel.name)
                                put("claimedRoleIds", JSONArray(player.claimedRoleIds.map { it.value }))
                                put("pressureLevel", player.pressureLevel?.name ?: JSONObject.NULL)
                            }) }
                        })
                        put("legalCandidateIds", JSONArray(frozen.legalCandidateIds))
                        put("prefixDigest", fingerprint(frozen.historyPrefix))
                    }
                    is StorytellerCausalJournalRecordV1.Committed -> JSONObject().apply {
                        put("kind", "commit")
                        put("decisionId", record.decisionId)
                        val decision = record.value
                        require(decision.registrations.isEmpty() ||
                            decision.selectedOutcome.decisionType in setOf(
                                StorytellerProviderDecisionContextV1.REGISTRATION_RESOLUTION,
                                StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION,
                            )) {
                            "Typed registration facts require a verified registration-resolution commit."
                        }
                        put("eventId", decision.eventId)
                        put("gameRevision", decision.gameStateRevision)
                        put("playerRevision", decision.playerInputRevision)
                        put("selectedCandidateId", decision.selectedCandidateId)
                        put("outcomeType", decision.selectedOutcome.decisionType)
                        put("outcomeFields", JSONObject(decision.selectedOutcome.canonicalFields))
                        put("abilityState", decision.abilityState.name)
                        put("truthRelation", decision.truthRelation.name)
                        put("registrations", JSONArray().apply {
                            decision.registrations.forEach { fact -> put(JSONObject().apply {
                                put("interactionId", fact.interactionId)
                                put("subjectSeat", fact.subjectSeat)
                                put("registeredRole", fact.registeredRole?.value ?: JSONObject.NULL)
                                put("registeredType", fact.registeredType?.name ?: JSONObject.NULL)
                                put("registeredAlignment", fact.registeredAlignment?.name ?: JSONObject.NULL)
                                put("registrationQuestion", fact.registrationQuestion.name)
                                put("reason", fact.reason.name)
                            }) }
                        })
                    }
                    is StorytellerCausalJournalRecordV1.Corrected -> JSONObject().apply {
                        put("kind", "correction")
                        put("correctionId", record.correctionId)
                        put("replacedEventId", record.replacedEventId)
                        put("replacementEventId", record.replacementEventId)
                    }
                })
            }
        })
    }

    fun decode(
        value: JSONObject,
        expectedGameId: String,
        actions: ActionFactTimeline,
        observations: List<RecordedEpistemicObservation>,
        recoveredCursor: Long,
    ): StorytellerCausalJournalArchiveV1 {
        require(value.getInt("version") == VERSION)
        require(value.getString("gameId") == expectedGameId)
        require(recoveredCursor >= 0L)
        val actionsWithPoints = actions.entries.map { StorytellerProviderHistoryEntryV1.Action(it.fact, it.point) }
        val observationsWithPoints = observations.map { record ->
            val point = (record.timelineBinding as? ObservationTimelineBinding.Global)?.point
                ?: error("Causal Recovery requires globally timed observations.")
            StorytellerProviderHistoryEntryV1.Observation(
                record.recordId, point, record.sourceSeat, record.sourceAbility,
                record.visibility, record.recipientSeats, record.reliability, record.proposition,
            )
        }
        val full = (actionsWithPoints + observationsWithPoints).sortedBy { it.point.globalSequence }
        require(full.zipWithNext().all { (a, b) -> a.point.globalSequence < b.point.globalSequence })
        require(full.all { it.point.globalSequence < recoveredCursor })
        val records = value.getJSONArray("records")
        val decoded = (0 until records.length()).map { index ->
            val record = records.getJSONObject(index)
            when (record.getString("kind")) {
                "capture" -> {
                    val identity = record.getJSONObject("identity")
                    val source = StorytellerProviderDecisionIdentityV1(
                        identity.getString("gameId"), identity.getString("scriptId"),
                        identity.getString("decisionTypeId"), identity.getString("decisionId"),
                    )
                    require(source.gameId == expectedGameId)
                    val revision = StorytellerProviderRevisionV1(
                        record.longExact("gameRevision"), record.longExact("playerRevision"),
                    )
                    val cutoff = record.longExact("exclusiveGlobalSequence")
                    require(cutoff in 0L..recoveredCursor)
                    val snapshot = TroubleBrewingGameSnapshotJsonCodec.decode(record.getString("gameSnapshot"))
                    require(snapshot.gameId == expectedGameId)
                    require(snapshot.script.value == source.scriptId)
                    val prefix = StorytellerProviderHistoryPrefixV1.capturedLive(
                        expectedGameId, revision, cutoff,
                        full.filter { it.point.globalSequence < cutoff },
                    )
                    require(record.getString("prefixDigest") == fingerprint(prefix)) {
                        "Frozen history checksum differs from the recovered canonical chronology."
                    }
                    val players = record.getJSONArray("players").let { array ->
                        (0 until array.length()).map { i ->
                            val player = array.getJSONObject(i)
                            val roleIds = player.getJSONArray("claimedRoleIds")
                            StorytellerProviderPlayerContextV1(
                                player.getInt("seat"),
                                PlayerExperienceLevelV1.valueOf(player.getString("experienceLevel")),
                                (0 until roleIds.length()).map { RoleId(roleIds.getString(it)) },
                                if (player.isNull("pressureLevel")) null else
                                    StorytellerDeclaredPressureLevelV1.valueOf(player.getString("pressureLevel")),
                            )
                        }
                    }
                    require(players.map { it.seat } == snapshot.grimoireSeats.map { it.seat })
                    val ids = record.getJSONArray("legalCandidateIds").let { array ->
                        (0 until array.length()).map { array.getString(it) }
                    }
                    StorytellerCausalJournalRecordV1.Captured(FrozenStorytellerDecisionPrefixV1(
                        identity = source,
                        revision = revision,
                        snapshotIdentity = FrozenStorytellerSnapshotIdentityV1(
                            snapshot.gameId, snapshot.gameSeed, snapshot.position,
                            snapshot.setupState, snapshot.grimoireSeats,
                        ),
                        historyPrefix = prefix,
                        players = players,
                        legalCandidateIds = ids,
                    ))
                }
                "commit" -> {
                    val fields = record.getJSONObject("outcomeFields")
                    val ordered = fields.keys().asSequence().toList().sorted()
                    val map = ordered.associateWith { fields.getString(it) }
                    StorytellerCausalJournalRecordV1.Committed(
                        record.getString("decisionId"),
                        StorytellerProviderPriorDecisionV1(
                            record.getString("eventId"),
                            record.longExact("gameRevision"),
                            record.longExact("playerRevision"),
                            record.getString("selectedCandidateId"),
                            DecisionOutcomeSnapshot(record.getString("outcomeType"), map),
                            AbilityState.valueOf(record.getString("abilityState")),
                            TruthRelation.valueOf(record.getString("truthRelation")),
                            registrations = if (record.has("registrations")) {
                                record.getJSONArray("registrations").let { array ->
                                    (0 until array.length()).map { i ->
                                        val fact = array.getJSONObject(i)
                                        RegistrationFact(
                                            interactionId = fact.getString("interactionId"),
                                            subjectSeat = fact.getInt("subjectSeat"),
                                            registeredRole = if (fact.isNull("registeredRole")) null
                                                else RoleId(fact.getString("registeredRole")),
                                            registeredType = if (fact.isNull("registeredType")) null
                                                else CharacterType.valueOf(fact.getString("registeredType")),
                                            registeredAlignment = if (fact.isNull("registeredAlignment")) null
                                                else Alignment.valueOf(fact.getString("registeredAlignment")),
                                            registrationQuestion = RegistrationQuestion.valueOf(
                                                fact.getString("registrationQuestion"),
                                            ),
                                            reason = RegistrationReason.valueOf(fact.getString("reason")),
                                        )
                                    }
                                }
                            } else emptyList(),
                        ),
                    )
                }
                "correction" -> StorytellerCausalJournalRecordV1.Corrected(
                    record.getString("correctionId"), record.getString("replacedEventId"),
                    record.getString("replacementEventId"),
                )
                else -> error("Unsupported causal journal record at index $index.")
            }
        }
        // No implied older captures when absent; no silent filtering of invalid records.
        return StorytellerCausalJournalArchiveV1(expectedGameId, decoded)
    }

    private fun JSONObject.longExact(name: String): Long {
        val raw = get(name)
        require(raw is Long || raw is Int || raw is Short || raw is Byte) {
            "Causal journal $name must be a JSON integer."
        }
        return (raw as Number).toLong()
    }

    /** Stable content fingerprint checks that Recovery did not silently alter any old observation. */
    private fun fingerprint(prefix: StorytellerProviderHistoryPrefixV1): String {
        val payload = buildString {
            append(prefix.gameId).append('|').append(prefix.sourceRevision).append('|')
            append(prefix.exclusiveGlobalSequence).append('\n')
            prefix.entries.forEach { entry ->
                append(entry.point).append('|').append(entry.kind).append('|').append(entry.entryId)
                when (entry) {
                    is StorytellerProviderHistoryEntryV1.Action -> append('|').append(entry.fact)
                    is StorytellerProviderHistoryEntryV1.Observation -> {
                        append('|').append(entry.sourceSeat).append('|').append(entry.sourceAbility)
                        append('|').append(entry.visibility).append('|')
                        append(entry.recipientSeats.sorted()).append('|').append(entry.reliability)
                        append('|').append(EpistemicSemanticJson.encode(entry.proposition))
                    }
                }
                append('\n')
            }
        }
        return MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
