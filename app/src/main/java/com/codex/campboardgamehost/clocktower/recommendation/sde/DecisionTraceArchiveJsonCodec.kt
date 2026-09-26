package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import java.math.BigInteger
import org.json.JSONArray
import org.json.JSONObject

/**
 * Strict current-format persistence codec for the immutable DecisionTrace replay archive.
 *
 * Persisted traces remain diagnostic snapshots. Canonical historical facts are referenced through
 * Global history-prefix identities and remain owned by ActionFactTimeline / EpistemicObservationLog.
 */
internal object DecisionTraceArchiveJsonCodec {
    const val CURRENT_FORMAT_VERSION: Int = 1

    fun encode(archive: DecisionTraceArchive): String =
        JSONObject().apply {
            put("formatVersion", CURRENT_FORMAT_VERSION)
            put("traces", JSONArray().apply {
                archive.traces.forEach { put(encodeTrace(it)) }
            })
        }.toString()

    fun decode(raw: String): DecisionTraceArchive {
        require(raw.isNotBlank()) { "DecisionTrace archive payload cannot be blank." }
        return try {
            decodeRoot(JSONObject(raw))
        } catch (error: IllegalArgumentException) {
            throw error
        } catch (error: Exception) {
            throw IllegalArgumentException("Malformed DecisionTrace archive payload.", error)
        }
    }

    private fun decodeRoot(root: JSONObject): DecisionTraceArchive {
        root.requireExactKeys("formatVersion", "traces")
        val version = root.requiredInt("formatVersion")
        require(version == CURRENT_FORMAT_VERSION) {
            "Unsupported DecisionTrace archive format version " + version + "."
        }
        return DecisionTraceArchive(
            root.requiredArray("traces").mapObjects("traces", ::decodeTrace),
        )
    }

    private fun encodeTrace(trace: DecisionTrace): JSONObject = JSONObject().apply {
        put("schemaVersion", trace.schemaVersion)
        put("evidenceCheckpoint", trace.evidenceCheckpoint.value)
        put("decisionId", trace.decisionId)
        put("lifecycleStage", encodeLifecycle(trace.lifecycleStage))
        put("sourceRevision", encodeRevision(trace.sourceRevision))
        put("historyPrefix", encodeGlobalPrefix(trace.historyPrefixRef))
        put("legalCandidateIds", strings(trace.legalCandidateIds))
        put("featureEvaluation", encodeFeatureEvaluation(trace.featureEvaluation))
        put("policySnapshot", encodePolicySnapshot(trace.policySnapshot))
        put("policySelection", trace.policySelection?.let(::encodePolicySelection) ?: JSONObject.NULL)
        put("actualChoice", encodeActualChoice(trace.actualChoice))
    }

    private fun decodeTrace(json: JSONObject): DecisionTrace {
        json.requireExactKeys(
            "schemaVersion",
            "evidenceCheckpoint",
            "decisionId",
            "lifecycleStage",
            "sourceRevision",
            "historyPrefix",
            "legalCandidateIds",
            "featureEvaluation",
            "policySnapshot",
            "policySelection",
            "actualChoice",
        )
        val schemaVersion = json.requiredInt("schemaVersion")
        require(schemaVersion == DecisionTrace.CURRENT_SCHEMA_VERSION) {
            "Unsupported DecisionTrace schema version " + schemaVersion + "."
        }
        return DecisionTrace(
            schemaVersion = schemaVersion,
            evidenceCheckpoint = EvidenceCheckpointId(json.requiredNonBlankString("evidenceCheckpoint")),
            decisionId = json.requiredNonBlankString("decisionId"),
            lifecycleStage = decodeLifecycle(json.requiredObject("lifecycleStage")),
            sourceRevision = decodeRevision(json.requiredObject("sourceRevision")),
            historyPrefixRef = decodeGlobalPrefix(json.requiredObject("historyPrefix")),
            legalCandidateIds = json.requiredStringList("legalCandidateIds"),
            featureEvaluation = decodeFeatureEvaluation(json.requiredObject("featureEvaluation")),
            policySnapshot = decodePolicySnapshot(json.requiredObject("policySnapshot")),
            policySelection = json.requiredNullableObject("policySelection")?.let(::decodePolicySelection),
            actualChoice = decodeActualChoice(json.requiredObject("actualChoice")),
        )
    }

    private fun encodeLifecycle(stage: SdeDecisionLifecycleStage): JSONObject =
        when (stage) {
            SdeDecisionLifecycleStage.SetupPrecommit -> JSONObject().apply {
                put("kind", "setupPrecommit")
            }
            is SdeDecisionLifecycleStage.Interaction -> JSONObject().apply {
                put("kind", "interaction")
                put("phase", stage.phase.name)
                put("round", stage.round)
                put("sequence", stage.sequence)
            }
        }

    private fun decodeLifecycle(json: JSONObject): SdeDecisionLifecycleStage =
        when (json.requiredNonBlankString("kind")) {
            "setupPrecommit" -> {
                json.requireExactKeys("kind")
                SdeDecisionLifecycleStage.SetupPrecommit
            }
            "interaction" -> {
                json.requireExactKeys("kind", "phase", "round", "sequence")
                SdeDecisionLifecycleStage.Interaction(
                    phase = json.requiredEnum("phase"),
                    round = json.requiredInt("round"),
                    sequence = json.requiredInt("sequence"),
                )
            }
            else -> throw IllegalArgumentException("Unsupported DecisionTrace lifecycle kind.")
        }

    private fun encodeRevision(revision: InformationDecisionRevision): JSONObject =
        JSONObject().apply {
            put("gameStateRevision", revision.gameStateRevision)
            put("playerInputRevision", revision.playerInputRevision)
        }

    private fun decodeRevision(json: JSONObject): InformationDecisionRevision {
        json.requireExactKeys("gameStateRevision", "playerInputRevision")
        return InformationDecisionRevision(
            gameStateRevision = json.requiredLong("gameStateRevision"),
            playerInputRevision = json.requiredLong("playerInputRevision"),
        )
    }

    private fun encodeGlobalPrefix(prefix: SdeHistoricalPrefixRef): JSONObject {
        val global = prefix as? SdeHistoricalPrefixRef.Global
            ?: throw IllegalArgumentException(
                "Only traces bound to a canonical global history prefix can be persisted.",
            )
        return JSONObject().apply {
            put("kind", "global")
            put("gameId", global.gameId)
            put("actionRefs", JSONArray().apply {
                global.actionRefs.forEach { ref ->
                    put(JSONObject().apply {
                        put("actionId", ref.actionId)
                        put("globalSequence", ref.globalSequence)
                    })
                }
            })
            put("observationRefs", JSONArray().apply {
                global.observationRefs.forEach { ref ->
                    put(JSONObject().apply {
                        put("recordId", ref.recordId)
                        put("globalSequence", ref.globalSequence)
                    })
                }
            })
        }
    }

    private fun decodeGlobalPrefix(json: JSONObject): SdeHistoricalPrefixRef.Global {
        json.requireExactKeys("kind", "gameId", "actionRefs", "observationRefs")
        require(json.requiredNonBlankString("kind") == "global") {
            "Persisted DecisionTrace requires a canonical global history prefix."
        }
        return SdeHistoricalPrefixRef.Global(
            gameId = json.requiredNonBlankString("gameId"),
            actionRefs = json.requiredArray("actionRefs").mapObjects("actionRefs") { ref ->
                ref.requireExactKeys("actionId", "globalSequence")
                SdeHistoricalActionRef(
                    actionId = ref.requiredNonBlankString("actionId"),
                    globalSequence = ref.requiredLong("globalSequence"),
                )
            },
            observationRefs = json.requiredArray("observationRefs").mapObjects("observationRefs") { ref ->
                ref.requireExactKeys("recordId", "globalSequence")
                SdeHistoricalObservationRef(
                    recordId = ref.requiredNonBlankString("recordId"),
                    globalSequence = ref.requiredLong("globalSequence"),
                )
            },
        )
    }

    private fun encodeFeatureEvaluation(evaluation: DecisionFeatureEvaluation): JSONObject =
        when (evaluation) {
            is DecisionFeatureEvaluation.Ready -> JSONObject().apply {
                put("kind", "ready")
                put("candidates", JSONArray().apply {
                    evaluation.candidates.forEach { candidate ->
                        put(JSONObject().apply {
                            put("candidateId", candidate.candidateId)
                            put("features", encodeFeatures(candidate.features))
                        })
                    }
                })
            }
            is DecisionFeatureEvaluation.Deferred -> JSONObject().apply {
                put("kind", "deferred")
                put("candidateIds", strings(evaluation.candidateIds))
                put(
                    "missingCapabilities",
                    strings(evaluation.missingCapabilities.map { it.name }.sorted()),
                )
            }
        }

    private fun decodeFeatureEvaluation(json: JSONObject): DecisionFeatureEvaluation =
        when (json.requiredNonBlankString("kind")) {
            "ready" -> {
                json.requireExactKeys("kind", "candidates")
                DecisionFeatureEvaluation.Ready(
                    candidates = json.requiredArray("candidates").mapObjects("candidates") { candidate ->
                        candidate.requireExactKeys("candidateId", "features")
                        CandidateDecisionFeatures(
                            candidateId = candidate.requiredNonBlankString("candidateId"),
                            features = decodeFeatures(candidate.requiredObject("features")),
                        )
                    },
                )
            }
            "deferred" -> {
                json.requireExactKeys("kind", "candidateIds", "missingCapabilities")
                DecisionFeatureEvaluation.Deferred(
                    candidateIds = json.requiredStringList("candidateIds"),
                    missingCapabilities =
                        json.requiredEnumSet<EpistemicEvaluationCapability>("missingCapabilities"),
                )
            }
            else -> throw IllegalArgumentException("Unsupported DecisionFeatureEvaluation kind.")
        }

    private fun encodeFeatures(features: DecisionFeatures): JSONObject = JSONObject().apply {
        put("strategic", encodeProjection(features.strategic, ::encodeStrategicFeatures))
        put(
            "confirmationChainImpact",
            encodeProjection(features.confirmationChainImpact, ::encodeConfirmationFeatures),
        )
        put(
            "healthyInformationUtility",
            encodeProjection(features.healthyInformationUtility, ::encodeHealthyFeatures),
        )
        put("truthCredibility", encodeProjection(features.truthCredibility, ::encodeTruthCredibility))
        put(
            "roleFunctionExposure",
            encodeProjection(features.roleFunctionExposure, ::encodeRoleFunctionExposure),
        )
        put("semanticTruth", encodeProjection(features.semanticTruth) { it.name })
        put(
            "impairedNarrative",
            encodeProjection(features.impairedNarrative, ::encodeImpairedNarrative),
        )
        put("bluffNarrative", encodeProjection(features.bluffNarrative, ::encodeBluffNarrative))
        put("relationships", encodeProjection(features.relationships, ::encodeRelationships))
        put(
            "futureFlexibility",
            encodeProjection(features.futureFlexibility, ::encodeFutureFlexibility),
        )
    }

    private fun decodeFeatures(json: JSONObject): DecisionFeatures {
        json.requireExactKeys(
            "strategic",
            "confirmationChainImpact",
            "healthyInformationUtility",
            "truthCredibility",
            "roleFunctionExposure",
            "semanticTruth",
            "impairedNarrative",
            "bluffNarrative",
            "relationships",
            "futureFlexibility",
        )
        return DecisionFeatures(
            strategic =
                decodeObjectProjection(json.requiredObject("strategic"), ::decodeStrategicFeatures),
            confirmationChainImpact =
                decodeObjectProjection(
                    json.requiredObject("confirmationChainImpact"),
                    ::decodeConfirmationFeatures,
                ),
            healthyInformationUtility =
                decodeObjectProjection(
                    json.requiredObject("healthyInformationUtility"),
                    ::decodeHealthyFeatures,
                ),
            truthCredibility =
                decodeObjectProjection(
                    json.requiredObject("truthCredibility"),
                    ::decodeTruthCredibility,
                ),
            roleFunctionExposure =
                decodeObjectProjection(
                    json.requiredObject("roleFunctionExposure"),
                    ::decodeRoleFunctionExposure,
                ),
            semanticTruth =
                decodeStringProjection<SemanticTruth>(json.requiredObject("semanticTruth")),
            impairedNarrative =
                decodeObjectProjection(
                    json.requiredObject("impairedNarrative"),
                    ::decodeImpairedNarrative,
                ),
            bluffNarrative =
                decodeObjectProjection(
                    json.requiredObject("bluffNarrative"),
                    ::decodeBluffNarrative,
                ),
            relationships =
                decodeObjectProjection(
                    json.requiredObject("relationships"),
                    ::decodeRelationships,
                ),
            futureFlexibility =
                decodeObjectProjection(
                    json.requiredObject("futureFlexibility"),
                    ::decodeFutureFlexibility,
                ),
        )
    }

    private fun <T> encodeProjection(
        projection: FeatureProjection<T>,
        encodeValue: (T) -> Any,
    ): JSONObject =
        when (projection) {
            is FeatureProjection.Projected -> JSONObject().apply {
                put("kind", "projected")
                put("value", encodeValue(projection.value))
            }
            is FeatureProjection.Unavailable -> JSONObject().apply {
                put("kind", "unavailable")
                put("reason", projection.reason.name)
            }
        }

    private fun <T> decodeObjectProjection(
        json: JSONObject,
        decodeValue: (JSONObject) -> T,
    ): FeatureProjection<T> =
        when (json.requiredNonBlankString("kind")) {
            "projected" -> {
                json.requireExactKeys("kind", "value")
                FeatureProjection.Projected(decodeValue(json.requiredObject("value")))
            }
            "unavailable" -> {
                json.requireExactKeys("kind", "reason")
                FeatureProjection.Unavailable(json.requiredEnum("reason"))
            }
            else -> throw IllegalArgumentException("Unsupported feature projection kind.")
        }

    private inline fun <reified T : Enum<T>> decodeStringProjection(
        json: JSONObject,
    ): FeatureProjection<T> =
        when (json.requiredNonBlankString("kind")) {
            "projected" -> {
                json.requireExactKeys("kind", "value")
                FeatureProjection.Projected(json.requiredEnum("value"))
            }
            "unavailable" -> {
                json.requireExactKeys("kind", "reason")
                FeatureProjection.Unavailable(json.requiredEnum("reason"))
            }
            else -> throw IllegalArgumentException("Unsupported feature projection kind.")
        }

    private fun encodeStrategicFeatures(value: StrategicDecisionFeatures): JSONObject =
        JSONObject().apply {
            put("demonCoverRetention", encodeRatio(value.demonCoverRetention))
            put("evilTopologyRetention", encodeRatio(value.evilTopologyRetention))
            put("evilCoverRetention", encodeRatio(value.evilCoverRetention))
            put("forcedGoodFraction", encodeRatio(value.forcedGoodFraction))
            put("forcedEvilFraction", encodeRatio(value.forcedEvilFraction))
            put("forcedGoodSeats", ints(value.forcedGoodSeats.sorted()))
            put("forcedEvilSeats", ints(value.forcedEvilSeats.sorted()))
        }

    private fun decodeStrategicFeatures(json: JSONObject): StrategicDecisionFeatures {
        json.requireExactKeys(
            "demonCoverRetention",
            "evilTopologyRetention",
            "evilCoverRetention",
            "forcedGoodFraction",
            "forcedEvilFraction",
            "forcedGoodSeats",
            "forcedEvilSeats",
        )
        return StrategicDecisionFeatures(
            demonCoverRetention = decodeRatio(json.requiredObject("demonCoverRetention")),
            evilTopologyRetention = decodeRatio(json.requiredObject("evilTopologyRetention")),
            evilCoverRetention = decodeRatio(json.requiredObject("evilCoverRetention")),
            forcedGoodFraction = decodeDefinedRatio(json.requiredObject("forcedGoodFraction")),
            forcedEvilFraction = decodeDefinedRatio(json.requiredObject("forcedEvilFraction")),
            forcedGoodSeats =
                json.requiredIntList("forcedGoodSeats").toSetStrict("forcedGoodSeats"),
            forcedEvilSeats =
                json.requiredIntList("forcedEvilSeats").toSetStrict("forcedEvilSeats"),
        )
    }

    private fun encodeRatio(value: StrategicRatio): JSONObject =
        when (value) {
            is StrategicRatio.Defined -> JSONObject().apply {
                put("kind", "defined")
                put("numerator", value.numerator)
                put("denominator", value.denominator)
            }
            StrategicRatio.Undefined -> JSONObject().apply {
                put("kind", "undefined")
            }
        }

    private fun decodeRatio(json: JSONObject): StrategicRatio =
        when (json.requiredNonBlankString("kind")) {
            "defined" -> decodeDefinedRatio(json)
            "undefined" -> {
                json.requireExactKeys("kind")
                StrategicRatio.Undefined
            }
            else -> throw IllegalArgumentException("Unsupported strategic ratio kind.")
        }

    private fun decodeDefinedRatio(json: JSONObject): StrategicRatio.Defined {
        json.requireExactKeys("kind", "numerator", "denominator")
        require(json.requiredNonBlankString("kind") == "defined") {
            "Expected defined strategic ratio."
        }
        return StrategicRatio.Defined(
            numerator = json.requiredInt("numerator"),
            denominator = json.requiredInt("denominator"),
        )
    }

    private fun encodeConfirmationFeatures(value: ConfirmationChainFeatures): JSONObject =
        JSONObject().apply {
            put("candidateRemovedExactWorldCount", value.candidateRemovedExactWorldCount.toString())
            put(
                "candidateRemovedStrategicWorldKeys",
                strategicWorldKeys(value.candidateRemovedStrategicWorldKeys),
            )
            put("candidateRemovedDemonSeats", ints(value.candidateRemovedDemonSeats.sorted()))
            put("candidateChannel", encodeConfirmationChannel(value.candidateChannel))
            put("historicalObservationImpacts", JSONArray().apply {
                value.historicalObservationImpacts.forEach { put(encodeHistoricalImpact(it)) }
            })
        }

    private fun decodeConfirmationFeatures(json: JSONObject): ConfirmationChainFeatures {
        json.requireExactKeys(
            "candidateRemovedExactWorldCount",
            "candidateRemovedStrategicWorldKeys",
            "candidateRemovedDemonSeats",
            "candidateChannel",
            "historicalObservationImpacts",
        )
        return ConfirmationChainFeatures(
            candidateRemovedExactWorldCount =
                json.requiredBigInteger("candidateRemovedExactWorldCount"),
            candidateRemovedStrategicWorldKeys =
                json.requiredStrategicWorldKeySet("candidateRemovedStrategicWorldKeys"),
            candidateRemovedDemonSeats =
                json.requiredIntList("candidateRemovedDemonSeats")
                    .toSetStrict("candidateRemovedDemonSeats"),
            candidateChannel = decodeConfirmationChannel(json.requiredObject("candidateChannel")),
            historicalObservationImpacts =
                json.requiredArray("historicalObservationImpacts")
                    .mapObjects("historicalObservationImpacts", ::decodeHistoricalImpact),
        )
    }

    private fun encodeHistoricalImpact(
        value: HistoricalObservationConfirmationImpact,
    ): JSONObject =
        JSONObject().apply {
            put("provenance", JSONObject().apply {
                put("observationRef", JSONObject().apply {
                    put("recordId", value.provenance.observationRef.recordId)
                    put("globalSequence", value.provenance.observationRef.globalSequence)
                })
                put("sourceSeat", value.provenance.sourceSeat ?: JSONObject.NULL)
                put("sourceAbility", value.provenance.sourceAbility?.value ?: JSONObject.NULL)
                put("channel", encodeConfirmationChannel(value.provenance.channel))
            })
            put("relation", value.relation.name)
            put("restoration", JSONObject().apply {
                put(
                    "restoredExactWorldCount",
                    value.restoration.restoredExactWorldCount.toString(),
                )
                put(
                    "restoredStrategicWorldKeys",
                    strategicWorldKeys(value.restoration.restoredStrategicWorldKeys),
                )
                put("restoredDemonSeats", ints(value.restoration.restoredDemonSeats.sorted()))
            })
            put("authenticatesDistinctSource", value.authenticatesDistinctSource)
            put(
                "wasIndependentlyConstrainingBefore",
                value.wasIndependentlyConstrainingBefore,
            )
        }

    private fun decodeHistoricalImpact(
        json: JSONObject,
    ): HistoricalObservationConfirmationImpact {
        json.requireExactKeys(
            "provenance",
            "relation",
            "restoration",
            "authenticatesDistinctSource",
            "wasIndependentlyConstrainingBefore",
        )
        val provenance = json.requiredObject("provenance")
        provenance.requireExactKeys("observationRef", "sourceSeat", "sourceAbility", "channel")
        val observationRef = provenance.requiredObject("observationRef")
        observationRef.requireExactKeys("recordId", "globalSequence")
        val restoration = json.requiredObject("restoration")
        restoration.requireExactKeys(
            "restoredExactWorldCount",
            "restoredStrategicWorldKeys",
            "restoredDemonSeats",
        )
        return HistoricalObservationConfirmationImpact(
            provenance = ConfirmationObservationProvenance(
                observationRef = SdeHistoricalObservationRef(
                    recordId = observationRef.requiredNonBlankString("recordId"),
                    globalSequence = observationRef.requiredLong("globalSequence"),
                ),
                sourceSeat = provenance.requiredNullableInt("sourceSeat"),
                sourceAbility =
                    provenance.requiredNullableString("sourceAbility")?.let(::RoleId),
                channel = decodeConfirmationChannel(provenance.requiredObject("channel")),
            ),
            relation = json.requiredEnum("relation"),
            restoration = ConfirmationAmbiguityRestoration(
                restoredExactWorldCount =
                    restoration.requiredBigInteger("restoredExactWorldCount"),
                restoredStrategicWorldKeys =
                    restoration.requiredStrategicWorldKeySet("restoredStrategicWorldKeys"),
                restoredDemonSeats =
                    restoration.requiredIntList("restoredDemonSeats")
                        .toSetStrict("restoredDemonSeats"),
            ),
            authenticatesDistinctSource =
                json.requiredBoolean("authenticatesDistinctSource"),
            wasIndependentlyConstrainingBefore =
                json.requiredBoolean("wasIndependentlyConstrainingBefore"),
        )
    }

    private fun encodeConfirmationChannel(value: ConfirmationChannelRef): JSONObject =
        when (value) {
            is ConfirmationChannelRef.Source -> JSONObject().apply {
                put("kind", "source")
                put("sourceSeat", value.sourceSeat ?: JSONObject.NULL)
                put("sourceAbility", value.sourceAbility?.value ?: JSONObject.NULL)
            }
            is ConfirmationChannelRef.Interaction -> JSONObject().apply {
                put("kind", "interaction")
                put("interactionId", value.interactionId)
            }
            is ConfirmationChannelRef.Observation -> JSONObject().apply {
                put("kind", "observation")
                put("observationRecordId", value.observationRecordId)
            }
        }

    private fun decodeConfirmationChannel(json: JSONObject): ConfirmationChannelRef =
        when (json.requiredNonBlankString("kind")) {
            "source" -> {
                json.requireExactKeys("kind", "sourceSeat", "sourceAbility")
                ConfirmationChannelRef.Source(
                    sourceSeat = json.requiredNullableInt("sourceSeat"),
                    sourceAbility =
                        json.requiredNullableString("sourceAbility")?.let(::RoleId),
                )
            }
            "interaction" -> {
                json.requireExactKeys("kind", "interactionId")
                ConfirmationChannelRef.Interaction(
                    json.requiredNonBlankString("interactionId"),
                )
            }
            "observation" -> {
                json.requireExactKeys("kind", "observationRecordId")
                ConfirmationChannelRef.Observation(
                    json.requiredNonBlankString("observationRecordId"),
                )
            }
            else -> throw IllegalArgumentException("Unsupported confirmation channel kind.")
        }

    private fun encodeHealthyFeatures(value: HealthyInformationUtilityFeatures): JSONObject =
        JSONObject().apply {
            put("usableBefore", healthyRoutes(value.usableHealthyRouteRefsBefore))
            put("independentBefore", healthyRoutes(value.independentHealthyRouteRefsBefore))
            put("usableAfter", healthyRoutes(value.usableHealthyRouteRefsAfter))
            put("independentAfter", healthyRoutes(value.independentHealthyRouteRefsAfter))
            put("newlyRedundant", healthyRoutes(value.newlyRedundantHealthyRouteRefs))
            put("contradicted", healthyRoutes(value.contradictedHealthyRouteRefs))
            put(
                "currentCandidate",
                value.currentCandidateHealthyRouteRef?.let(::encodeHealthyRoute)
                    ?: JSONObject.NULL,
            )
        }

    private fun decodeHealthyFeatures(json: JSONObject): HealthyInformationUtilityFeatures {
        json.requireExactKeys(
            "usableBefore",
            "independentBefore",
            "usableAfter",
            "independentAfter",
            "newlyRedundant",
            "contradicted",
            "currentCandidate",
        )
        val current =
            json.requiredNullableObject("currentCandidate")?.let(::decodeHealthyRoute)
        require(current == null || current is HealthyInformationRouteRef.CurrentCandidate) {
            "Healthy current candidate route must use current-candidate identity."
        }
        return HealthyInformationUtilityFeatures(
            usableHealthyRouteRefsBefore =
                json.requiredHealthyHistoricalRouteSet("usableBefore"),
            independentHealthyRouteRefsBefore =
                json.requiredHealthyHistoricalRouteSet("independentBefore"),
            usableHealthyRouteRefsAfter =
                json.requiredHealthyRouteSet("usableAfter"),
            independentHealthyRouteRefsAfter =
                json.requiredHealthyRouteSet("independentAfter"),
            newlyRedundantHealthyRouteRefs =
                json.requiredHealthyHistoricalRouteSet("newlyRedundant"),
            contradictedHealthyRouteRefs =
                json.requiredHealthyHistoricalRouteSet("contradicted"),
            currentCandidateHealthyRouteRef =
                current as? HealthyInformationRouteRef.CurrentCandidate,
        )
    }

    private fun encodeHealthyRoute(value: HealthyInformationRouteRef): JSONObject =
        when (value) {
            is HealthyInformationRouteRef.HistoricalObservation -> JSONObject().apply {
                put("kind", "historical")
                put("recordId", value.recordId)
                put("recipientSeat", value.recipientSeat)
            }
            is HealthyInformationRouteRef.CurrentCandidate -> JSONObject().apply {
                put("kind", "current")
                put("candidateId", value.candidateId)
                put("recipientSeat", value.recipientSeat)
            }
        }

    private fun decodeHealthyRoute(json: JSONObject): HealthyInformationRouteRef =
        when (json.requiredNonBlankString("kind")) {
            "historical" -> {
                json.requireExactKeys("kind", "recordId", "recipientSeat")
                HealthyInformationRouteRef.HistoricalObservation(
                    recordId = json.requiredNonBlankString("recordId"),
                    recipientSeat = json.requiredInt("recipientSeat"),
                )
            }
            "current" -> {
                json.requireExactKeys("kind", "candidateId", "recipientSeat")
                HealthyInformationRouteRef.CurrentCandidate(
                    candidateId = json.requiredNonBlankString("candidateId"),
                    recipientSeat = json.requiredInt("recipientSeat"),
                )
            }
            else -> throw IllegalArgumentException(
                "Unsupported healthy-information route kind.",
            )
        }

    private fun encodeTruthCredibility(value: TruthCredibilityFeatures): JSONObject {
        require(!value.hasTypedMaterial) {
            "DecisionTrace schema v1 cannot persist typed truth/credibility material."
        }
        return JSONObject().apply {
            put("truthDangerReasonCodes", strings(value.truthDangerReasonCodes.sorted()))
            put(
                "credibilityDisruptionReasonCodes",
                strings(value.credibilityDisruptionReasonCodes.sorted()),
            )
        }
    }

    private fun decodeTruthCredibility(json: JSONObject): TruthCredibilityFeatures {
        json.requireExactKeys(
            "truthDangerReasonCodes",
            "credibilityDisruptionReasonCodes",
        )
        return TruthCredibilityFeatures(
            truthDangerReasonCodes =
                json.requiredStringSet("truthDangerReasonCodes"),
            credibilityDisruptionReasonCodes =
                json.requiredStringSet("credibilityDisruptionReasonCodes"),
        )
    }

    private fun encodeRoleFunctionExposure(
        value: RoleFunctionExposureFeatures,
    ): JSONObject =
        JSONObject().apply {
            put("direct", exposureTargets(value.directlyExposedTargets))
            put("already", exposureTargets(value.alreadyExposedTargets))
            put("new", exposureTargets(value.newlyExposedTargets))
            put(
                "confirmationAmplified",
                exposureTargets(value.confirmationAmplifiedTargets),
            )
            put("forced", exposureTargets(value.forcedExposureTargets))
            put("avoidable", exposureTargets(value.avoidableExposureTargets))
        }

    private fun decodeRoleFunctionExposure(
        json: JSONObject,
    ): RoleFunctionExposureFeatures {
        json.requireExactKeys(
            "direct",
            "already",
            "new",
            "confirmationAmplified",
            "forced",
            "avoidable",
        )
        return RoleFunctionExposureFeatures(
            directlyExposedTargets = json.requiredExposureTargetSet("direct"),
            alreadyExposedTargets = json.requiredExposureTargetSet("already"),
            newlyExposedTargets = json.requiredExposureTargetSet("new"),
            confirmationAmplifiedTargets =
                json.requiredExposureTargetSet("confirmationAmplified"),
            forcedExposureTargets = json.requiredExposureTargetSet("forced"),
            avoidableExposureTargets = json.requiredExposureTargetSet("avoidable"),
        )
    }

    private fun encodeExposureTarget(
        value: RoleFunctionExposureTargetRef,
    ): JSONObject =
        JSONObject().apply {
            put("seat", value.seat)
            put("role", value.role.value)
            put("recipientSeat", value.recipientSeat)
            put("capability", value.capability.name)
        }

    private fun decodeExposureTarget(
        json: JSONObject,
    ): RoleFunctionExposureTargetRef {
        json.requireExactKeys("seat", "role", "recipientSeat", "capability")
        return RoleFunctionExposureTargetRef(
            seat = json.requiredInt("seat"),
            role = RoleId(json.requiredNonBlankString("role")),
            recipientSeat = json.requiredInt("recipientSeat"),
            capability = json.requiredEnum("capability"),
        )
    }

    private fun encodeImpairedNarrative(
        value: ImpairedNarrativeFeatures,
    ): JSONObject =
        JSONObject().apply {
            put("currentAbilityState", value.currentAbilityState.name)
            put("impairmentLifetime", value.impairmentLifetime.name)
            put(
                "priorImpairedObservationIds",
                strings(value.priorImpairedObservationIds.sorted()),
            )
            put(
                "contradictoryPriorObservationIds",
                strings(value.contradictoryPriorObservationIds.sorted()),
            )
            put("relation", value.relation.name)
            put("transitionNecessity", value.transitionNecessity.name)
            put("detectabilitySignal", value.detectabilitySignal.name)
        }

    private fun decodeImpairedNarrative(
        json: JSONObject,
    ): ImpairedNarrativeFeatures {
        json.requireExactKeys(
            "currentAbilityState",
            "impairmentLifetime",
            "priorImpairedObservationIds",
            "contradictoryPriorObservationIds",
            "relation",
            "transitionNecessity",
            "detectabilitySignal",
        )
        return ImpairedNarrativeFeatures(
            currentAbilityState = json.requiredEnum<AbilityState>("currentAbilityState"),
            impairmentLifetime = json.requiredEnum("impairmentLifetime"),
            priorImpairedObservationIds =
                json.requiredStringSet("priorImpairedObservationIds"),
            contradictoryPriorObservationIds =
                json.requiredStringSet("contradictoryPriorObservationIds"),
            relation = json.requiredEnum("relation"),
            transitionNecessity = json.requiredEnum("transitionNecessity"),
            detectabilitySignal = json.requiredEnum("detectabilitySignal"),
        )
    }

    private fun encodeBluffNarrative(value: BluffNarrativeFeatures): JSONObject =
        JSONObject().apply {
            put(
                "claimBurdenReasonCodes",
                strings(value.claimBurdenReasonCodes.sorted()),
            )
            put("narrativeRouteIds", strings(value.narrativeRouteIds.sorted()))
        }

    private fun decodeBluffNarrative(json: JSONObject): BluffNarrativeFeatures {
        json.requireExactKeys("claimBurdenReasonCodes", "narrativeRouteIds")
        return BluffNarrativeFeatures(
            claimBurdenReasonCodes =
                json.requiredStringSet("claimBurdenReasonCodes"),
            narrativeRouteIds = json.requiredStringSet("narrativeRouteIds"),
        )
    }

    private fun encodeRelationships(value: DecisionRelationshipFeatures): JSONObject =
        JSONObject().apply {
            put(
                "collisionCandidateIds",
                strings(value.collisionCandidateIds.sorted()),
            )
            put("supportCandidateIds", strings(value.supportCandidateIds.sorted()))
        }

    private fun decodeRelationships(
        json: JSONObject,
    ): DecisionRelationshipFeatures {
        json.requireExactKeys("collisionCandidateIds", "supportCandidateIds")
        return DecisionRelationshipFeatures(
            collisionCandidateIds =
                json.requiredStringSet("collisionCandidateIds"),
            supportCandidateIds = json.requiredStringSet("supportCandidateIds"),
        )
    }

    private fun encodeFutureFlexibility(
        value: FutureFlexibilityFeatures,
    ): JSONObject =
        JSONObject().apply {
            put("retainedRouteIds", strings(value.retainedRouteIds.sorted()))
            put("lostRouteIds", strings(value.lostRouteIds.sorted()))
            put("reasonCodes", strings(value.reasonCodes.sorted()))
        }

    private fun decodeFutureFlexibility(
        json: JSONObject,
    ): FutureFlexibilityFeatures {
        json.requireExactKeys("retainedRouteIds", "lostRouteIds", "reasonCodes")
        return FutureFlexibilityFeatures(
            retainedRouteIds = json.requiredStringSet("retainedRouteIds"),
            lostRouteIds = json.requiredStringSet("lostRouteIds"),
            reasonCodes = json.requiredStringSet("reasonCodes"),
        )
    }

    private fun encodePolicySnapshot(
        snapshot: DecisionTracePolicySnapshot,
    ): JSONObject =
        when (snapshot) {
            is DecisionTracePolicySnapshot.Ready -> JSONObject().apply {
                put("kind", "ready")
                put("policyVersion", snapshot.policyVersion.value)
                put("evaluations", JSONArray().apply {
                    snapshot.evaluations.forEach { put(encodePolicyEvaluation(it)) }
                })
                put(
                    "limitations",
                    strings(snapshot.limitations.map { it.value }.sorted()),
                )
            }
            is DecisionTracePolicySnapshot.Deferred -> JSONObject().apply {
                put("kind", "deferred")
                put("policyVersion", snapshot.policyVersion.value)
                put("candidateIds", strings(snapshot.candidateIds))
                put("reasons", strings(snapshot.reasons.map { it.value }.sorted()))
            }
        }

    private fun decodePolicySnapshot(
        json: JSONObject,
    ): DecisionTracePolicySnapshot =
        when (json.requiredNonBlankString("kind")) {
            "ready" -> {
                json.requireExactKeys(
                    "kind",
                    "policyVersion",
                    "evaluations",
                    "limitations",
                )
                DecisionTracePolicySnapshot.Ready(
                    policyVersion =
                        PolicyVersion(json.requiredNonBlankString("policyVersion")),
                    evaluations =
                        json.requiredArray("evaluations")
                            .mapObjects("evaluations", ::decodePolicyEvaluation),
                    limitations =
                        json.requiredStringSet("limitations")
                            .mapTo(linkedSetOf()) { PolicyLimitationCode(it) },
                )
            }
            "deferred" -> {
                json.requireExactKeys(
                    "kind",
                    "policyVersion",
                    "candidateIds",
                    "reasons",
                )
                DecisionTracePolicySnapshot.Deferred(
                    policyVersion =
                        PolicyVersion(json.requiredNonBlankString("policyVersion")),
                    candidateIds = json.requiredStringList("candidateIds"),
                    reasons =
                        json.requiredStringSet("reasons")
                            .mapTo(linkedSetOf()) { PolicyDeferralCode(it) },
                )
            }
            else -> throw IllegalArgumentException(
                "Unsupported DecisionTrace policy snapshot kind.",
            )
        }

    private fun encodePolicyEvaluation(value: PolicyEvaluation): JSONObject =
        JSONObject().apply {
            put("candidateId", value.candidateId)
            put("policyVersion", value.policyVersion.value)
            put("disposition", value.disposition.name)
            put(
                "rejectionReasons",
                strings(value.rejectionReasons.map { it.value }.sorted()),
            )
            put(
                "softPreferenceReasons",
                strings(value.softPreferenceReasons.map { it.value }.sorted()),
            )
            put("equivalenceState", encodeEquivalenceState(value.equivalenceState))
        }

    private fun decodePolicyEvaluation(json: JSONObject): PolicyEvaluation {
        json.requireExactKeys(
            "candidateId",
            "policyVersion",
            "disposition",
            "rejectionReasons",
            "softPreferenceReasons",
            "equivalenceState",
        )
        return PolicyEvaluation(
            candidateId = json.requiredNonBlankString("candidateId"),
            policyVersion =
                PolicyVersion(json.requiredNonBlankString("policyVersion")),
            disposition = json.requiredEnum("disposition"),
            rejectionReasons =
                json.requiredStringSet("rejectionReasons")
                    .mapTo(linkedSetOf()) { PolicyReasonCode(it) },
            softPreferenceReasons =
                json.requiredStringSet("softPreferenceReasons")
                    .mapTo(linkedSetOf()) { PolicyReasonCode(it) },
            equivalenceState =
                decodeEquivalenceState(json.requiredObject("equivalenceState")),
        )
    }

    private fun encodeEquivalenceState(
        value: PolicyEquivalenceState,
    ): JSONObject =
        when (value) {
            PolicyEquivalenceState.NotEvaluated -> JSONObject().apply {
                put("kind", "notEvaluated")
            }
            PolicyEquivalenceState.Unique -> JSONObject().apply {
                put("kind", "unique")
            }
            is PolicyEquivalenceState.Tied -> JSONObject().apply {
                put("kind", "tied")
                put("candidateIds", strings(value.candidateIds.sorted()))
            }
        }

    private fun decodeEquivalenceState(
        json: JSONObject,
    ): PolicyEquivalenceState =
        when (json.requiredNonBlankString("kind")) {
            "notEvaluated" -> {
                json.requireExactKeys("kind")
                PolicyEquivalenceState.NotEvaluated
            }
            "unique" -> {
                json.requireExactKeys("kind")
                PolicyEquivalenceState.Unique
            }
            "tied" -> {
                json.requireExactKeys("kind", "candidateIds")
                PolicyEquivalenceState.Tied(
                    json.requiredStringSet("candidateIds"),
                )
            }
            else -> throw IllegalArgumentException(
                "Unsupported policy equivalence kind.",
            )
        }

    private fun encodePolicySelection(value: PolicySelection): JSONObject =
        JSONObject().apply {
            put("policyVersion", value.policyVersion.value)
            put("candidateId", value.candidateId)
            put("method", value.method.name)
        }

    private fun decodePolicySelection(json: JSONObject): PolicySelection {
        json.requireExactKeys("policyVersion", "candidateId", "method")
        return PolicySelection(
            policyVersion =
                PolicyVersion(json.requiredNonBlankString("policyVersion")),
            candidateId = json.requiredNonBlankString("candidateId"),
            method = json.requiredEnum("method"),
        )
    }

    private fun encodeActualChoice(
        value: DecisionTraceActualChoice,
    ): JSONObject =
        when (value) {
            DecisionTraceActualChoice.Pending -> JSONObject().apply {
                put("kind", "pending")
            }
            is DecisionTraceActualChoice.Committed -> JSONObject().apply {
                put("kind", "committed")
                put("candidateId", value.candidateId)
                put("source", value.source.name)
                put("manualOverride", value.manualOverride)
                put(
                    "overrideReason",
                    value.overrideReason?.let(::encodeOverrideReason)
                        ?: JSONObject.NULL,
                )
            }
        }

    private fun decodeActualChoice(
        json: JSONObject,
    ): DecisionTraceActualChoice =
        when (json.requiredNonBlankString("kind")) {
            "pending" -> {
                json.requireExactKeys("kind")
                DecisionTraceActualChoice.Pending
            }
            "committed" -> {
                json.requireExactKeys(
                    "kind",
                    "candidateId",
                    "source",
                    "manualOverride",
                    "overrideReason",
                )
                DecisionTraceActualChoice.Committed(
                    candidateId = json.requiredNonBlankString("candidateId"),
                    source =
                        json.requiredEnum<InformationDecisionSource>("source"),
                    manualOverride = json.requiredBoolean("manualOverride"),
                    overrideReason =
                        json.requiredNullableObject("overrideReason")
                            ?.let(::decodeOverrideReason),
                )
            }
            else -> throw IllegalArgumentException(
                "Unsupported DecisionTrace actual-choice kind.",
            )
        }

    private fun encodeOverrideReason(
        value: DecisionTraceOverrideReason,
    ): JSONObject =
        JSONObject().apply {
            put("code", value.code ?: JSONObject.NULL)
            put("text", value.text ?: JSONObject.NULL)
        }

    private fun decodeOverrideReason(
        json: JSONObject,
    ): DecisionTraceOverrideReason {
        json.requireExactKeys("code", "text")
        return DecisionTraceOverrideReason(
            code = json.requiredNullableString("code"),
            text = json.requiredNullableString("text"),
        )
    }

    private fun strategicWorldKeys(
        values: Set<StrategicWorldKey>,
    ): JSONArray =
        JSONArray().apply {
            values.sortedWith(
                compareBy<StrategicWorldKey>(
                    { it.demonSeat },
                    { it.minionSeats.joinToString(",") },
                ),
            ).forEach { value ->
                put(JSONObject().apply {
                    put("demonSeat", value.demonSeat)
                    put("minionSeats", ints(value.minionSeats))
                })
            }
        }

    private fun JSONObject.requiredStrategicWorldKeySet(
        key: String,
    ): Set<StrategicWorldKey> =
        requiredArray(key)
            .mapObjects(key) { json ->
                json.requireExactKeys("demonSeat", "minionSeats")
                StrategicWorldKey(
                    demonSeat = json.requiredInt("demonSeat"),
                    minionSeats = json.requiredIntList("minionSeats"),
                )
            }
            .toSetStrict(key)

    private fun healthyRoutes(
        values: Set<out HealthyInformationRouteRef>,
    ): JSONArray =
        JSONArray().apply {
            values.sortedBy(::healthyRouteSortKey)
                .forEach { put(encodeHealthyRoute(it)) }
        }

    private fun healthyRouteSortKey(
        value: HealthyInformationRouteRef,
    ): String =
        when (value) {
            is HealthyInformationRouteRef.HistoricalObservation ->
                listOf(
                    "0",
                    value.recordId,
                    value.recipientSeat.toString(),
                ).joinToString("|")
            is HealthyInformationRouteRef.CurrentCandidate ->
                listOf(
                    "1",
                    value.candidateId,
                    value.recipientSeat.toString(),
                ).joinToString("|")
        }

    private fun JSONObject.requiredHealthyRouteSet(
        key: String,
    ): Set<HealthyInformationRouteRef> =
        requiredArray(key)
            .mapObjects(key, ::decodeHealthyRoute)
            .toSetStrict(key)

    private fun JSONObject.requiredHealthyHistoricalRouteSet(
        key: String,
    ): Set<HealthyInformationRouteRef.HistoricalObservation> =
        requiredArray(key)
            .mapObjects(key) { json ->
                val route = decodeHealthyRoute(json)
                require(route is HealthyInformationRouteRef.HistoricalObservation) {
                    key + " may only contain historical healthy-information routes."
                }
                route
            }
            .toSetStrict(key)

    private fun exposureTargets(
        values: Set<RoleFunctionExposureTargetRef>,
    ): JSONArray =
        JSONArray().apply {
            values.sortedWith(
                compareBy<RoleFunctionExposureTargetRef>(
                    { it.seat },
                    { it.role.value },
                    { it.recipientSeat },
                    { it.capability.name },
                ),
            ).forEach { put(encodeExposureTarget(it)) }
        }

    private fun JSONObject.requiredExposureTargetSet(
        key: String,
    ): Set<RoleFunctionExposureTargetRef> =
        requiredArray(key)
            .mapObjects(key, ::decodeExposureTarget)
            .toSetStrict(key)

    private fun strings(values: Iterable<String>): JSONArray =
        JSONArray().apply { values.forEach(::put) }

    private fun ints(values: Iterable<Int>): JSONArray =
        JSONArray().apply { values.forEach(::put) }

    private fun JSONObject.requireExactKeys(vararg expected: String) {
        val actual = keys().asSequence().toSet()
        val expectedSet = expected.toSet()
        require(actual == expectedSet) {
            "Persisted object keys mismatch. Expected " +
                expectedSet +
                " but found " +
                actual +
                "."
        }
    }

    private fun JSONObject.requiredObject(key: String): JSONObject {
        require(has(key) && !isNull(key)) { "Missing required object '" + key + "'." }
        return opt(key) as? JSONObject
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' must be an object.",
            )
    }

    private fun JSONObject.requiredNullableObject(
        key: String,
    ): JSONObject? {
        require(has(key)) { "Missing required nullable object '" + key + "'." }
        if (isNull(key)) return null
        return opt(key) as? JSONObject
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' must be an object or null.",
            )
    }

    private fun JSONObject.requiredArray(key: String): JSONArray {
        require(has(key) && !isNull(key)) { "Missing required array '" + key + "'." }
        return opt(key) as? JSONArray
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' must be an array.",
            )
    }

    private fun JSONObject.requiredNonBlankString(key: String): String {
        val value = requiredString(key)
        require(value.isNotBlank()) { "Persisted '" + key + "' cannot be blank." }
        return value
    }

    private fun JSONObject.requiredString(key: String): String {
        require(has(key) && !isNull(key)) { "Missing required string '" + key + "'." }
        return opt(key) as? String
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' must be a string.",
            )
    }

    private fun JSONObject.requiredNullableString(
        key: String,
    ): String? {
        require(has(key)) { "Missing required nullable string '" + key + "'." }
        if (isNull(key)) return null
        return (opt(key) as? String)
            ?.also {
                require(it.isNotBlank()) {
                    "Persisted '" + key + "' cannot be blank."
                }
            }
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' must be a string or null.",
            )
    }

    private fun JSONObject.requiredBoolean(key: String): Boolean {
        require(has(key) && !isNull(key)) { "Missing required boolean '" + key + "'." }
        return opt(key) as? Boolean
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' must be a boolean.",
            )
    }

    private fun JSONObject.requiredInt(key: String): Int {
        val value = requiredLong(key)
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "Persisted '" + key + "' is outside Int range."
        }
        return value.toInt()
    }

    private fun JSONObject.requiredNullableInt(
        key: String,
    ): Int? {
        require(has(key)) { "Missing required nullable integer '" + key + "'." }
        if (isNull(key)) return null
        val raw = opt(key)
        require(raw is Byte || raw is Short || raw is Int || raw is Long) {
            "Persisted '" + key + "' must be an integer or null."
        }
        val value = (raw as Number).toLong()
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            "Persisted '" + key + "' is outside Int range."
        }
        return value.toInt()
    }

    private fun JSONObject.requiredLong(key: String): Long {
        require(has(key) && !isNull(key)) { "Missing required integer '" + key + "'." }
        val raw = opt(key)
        require(raw is Byte || raw is Short || raw is Int || raw is Long) {
            "Persisted '" + key + "' must be an integer."
        }
        return (raw as Number).toLong()
    }

    private fun JSONObject.requiredBigInteger(key: String): BigInteger {
        val value = requiredNonBlankString(key)
        return runCatching { BigInteger(value) }
            .getOrElse {
                throw IllegalArgumentException(
                    "Persisted '" + key + "' must be an integer string.",
                    it,
                )
            }
    }

    private inline fun <reified T : Enum<T>> JSONObject.requiredEnum(
        key: String,
    ): T {
        val value = requiredNonBlankString(key)
        return enumValues<T>().firstOrNull { it.name == value }
            ?: throw IllegalArgumentException(
                "Persisted '" + key + "' has an unknown enum value '" + value + "'.",
            )
    }

    private fun JSONObject.requiredStringList(key: String): List<String> =
        requiredArray(key)
            .mapValues(key) { index, value ->
                (value as? String)
                    ?.also {
                        require(it.isNotBlank()) {
                            "Persisted '" + key + "'[" + index + "] cannot be blank."
                        }
                    }
                    ?: throw IllegalArgumentException(
                        "Persisted '" + key + "'[" + index + "] must be a string.",
                    )
            }
            .also { values ->
                require(values.distinct().size == values.size) {
                    "Persisted '" + key + "' entries must be unique."
                }
            }

    private fun JSONObject.requiredStringSet(key: String): Set<String> =
        requiredStringList(key).toCollection(linkedSetOf())

    private fun JSONObject.requiredIntList(key: String): List<Int> =
        requiredArray(key).mapValues(key) { index, value ->
            require(value is Byte || value is Short || value is Int || value is Long) {
                "Persisted '" + key + "'[" + index + "] must be an integer."
            }
            val longValue = (value as Number).toLong()
            require(longValue in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                "Persisted '" + key + "'[" + index + "] is outside Int range."
            }
            longValue.toInt()
        }

    private inline fun <reified T : Enum<T>> JSONObject.requiredEnumSet(
        key: String,
    ): Set<T> {
        val rawValues = requiredStringList(key)
        return rawValues.mapTo(linkedSetOf()) { raw ->
            enumValues<T>().firstOrNull { it.name == raw }
                ?: throw IllegalArgumentException(
                    "Persisted '" + key + "' has an unknown enum value '" + raw + "'.",
                )
        }.also { values ->
            require(values.size == rawValues.size) {
                "Persisted '" + key + "' entries must be unique."
            }
        }
    }

    private inline fun <T> JSONArray.mapObjects(
        label: String,
        transform: (JSONObject) -> T,
    ): List<T> =
        buildList {
            for (index in 0 until length()) {
                val value = opt(index) as? JSONObject
                    ?: throw IllegalArgumentException(
                        "Persisted '" + label + "'[" + index + "] must be an object.",
                    )
                add(transform(value))
            }
        }

    private inline fun <T> JSONArray.mapValues(
        label: String,
        transform: (Int, Any?) -> T,
    ): List<T> =
        buildList {
            for (index in 0 until length()) {
                add(transform(index, opt(index)))
            }
        }

    private fun <T> List<T>.toSetStrict(label: String): Set<T> {
        require(distinct().size == size) {
            "Persisted '" + label + "' entries must be unique."
        }
        return toCollection(linkedSetOf())
    }
}