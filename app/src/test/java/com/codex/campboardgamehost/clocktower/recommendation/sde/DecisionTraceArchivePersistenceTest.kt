package com.codex.campboardgamehost.clocktower.recommendation.sde

import com.codex.campboardgamehost.clocktower.domain.AbilityState
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.SemanticTruth
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.EpistemicEvaluationCapability
import com.codex.campboardgamehost.clocktower.epistemic.StrategicWorldKey
import com.codex.campboardgamehost.clocktower.session.InformationDecisionRevision
import com.codex.campboardgamehost.clocktower.session.InformationDecisionSource
import java.math.BigInteger
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionTraceArchivePersistenceTest {
    @Test
    fun `codec deterministically round trips ready and deferred traces`() {
        val archive = DecisionTraceArchive()
            .append(readyTrace())
            .append(deferredTrace())

        val encoded = DecisionTraceArchiveJsonCodec.encode(archive)
        val decoded = DecisionTraceArchiveJsonCodec.decode(encoded)

        assertEquals(archive, decoded)
        assertEquals(encoded, DecisionTraceArchiveJsonCodec.encode(decoded))
        assertEquals(encoded, DecisionTraceArchiveJsonCodec.encode(archive))
    }

    @Test
    fun `codec fails closed on malformed or incompatible payloads`() {
        val encoded = DecisionTraceArchiveJsonCodec.encode(
            DecisionTraceArchive().append(readyTrace()),
        )

        val missingTraces = JSONObject(encoded).apply {
            remove("traces")
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(missingTraces)
        }

        val wrongType = JSONObject(encoded).apply {
            put("traces", "not-an-array")
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(wrongType)
        }

        val unknownKind = JSONObject(encoded).apply {
            getJSONArray("traces")
                .getJSONObject(0)
                .getJSONObject("lifecycleStage")
                .put("kind", "future-kind")
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(unknownKind)
        }

        val incompatibleArchive = JSONObject(encoded).apply {
            put("formatVersion", DecisionTraceArchiveJsonCodec.CURRENT_FORMAT_VERSION + 1)
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(incompatibleArchive)
        }

        val incompatibleTrace = JSONObject(encoded).apply {
            getJSONArray("traces")
                .getJSONObject(0)
                .put("schemaVersion", DecisionTrace.CURRENT_SCHEMA_VERSION + 1)
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(incompatibleTrace)
        }
    }

    @Test
    fun `current trace schema deterministically round trips typed truth credibility material`() {
        val trace = readyTrace()
        val ready = trace.featureEvaluation as DecisionFeatureEvaluation.Ready
        val source = ConfirmationChannelRef.Source(
            sourceSeat = 1,
            sourceAbility = RoleId("Chef"),
        )
        val unresolved = ConfirmationChannelRef.Source(
            sourceSeat = 3,
            sourceAbility = RoleId("Washerwoman"),
        )
        val strategicWorld = StrategicWorldKey(
            demonSeat = 5,
            minionSeats = listOf(6),
        )
        val disruption = CredibilityDisruptionImpact(
            committedInputRef = CommittedDecisionInputRef(
                inputId = "red-herring-seat-1",
                ownerId = "SetupCandidateGenerator",
                kind = SdeCommittedDecisionInputKind.RED_HERRING,
            ),
            affectedSource = source,
            mechanism = CredibilityDisruptionMechanism.RED_HERRING_FALSE_POSITIVE,
        )
        val typed = ready.candidates.mapIndexed { index, candidate ->
            if (index != 0) {
                candidate
            } else {
                candidate.copy(
                    features = candidate.features.copy(
                        truthCredibility = FeatureProjection.Projected(
                            TruthCredibilityFeatures(
                                truthDangerSources = setOf(
                                    TruthDangerSourceImpact(
                                        source = source,
                                        exactWorldReduction = BigInteger.ONE,
                                        strategicWorldKeysRemoved = setOf(strategicWorld),
                                        demonSeatsRemoved = setOf(5),
                                    ),
                                ),
                                credibilityDisruptions = setOf(disruption),
                                unresolvedSourceRefs = setOf(unresolved),
                                truthDangerReasonCodes = setOf("legacy-truth"),
                                credibilityDisruptionReasonCodes = setOf("legacy-credibility"),
                            ),
                        ),
                    ),
                )
            }
        }
        val typedTrace = trace.copy(
            featureEvaluation = DecisionFeatureEvaluation.Ready(typed),
        )

        val archive = DecisionTraceArchive().append(typedTrace)
        val encoded = DecisionTraceArchiveJsonCodec.encode(archive)
        val decoded = DecisionTraceArchiveJsonCodec.decode(encoded)

        assertEquals(DecisionTrace.CURRENT_SCHEMA_VERSION, typedTrace.schemaVersion)
        assertEquals(archive, decoded)
        assertEquals(encoded, DecisionTraceArchiveJsonCodec.encode(decoded))
    }

    @Test
    fun `schema v1 legacy truth credibility payload migrates to current trace schema without inventing typed material`() {
        val current = JSONObject(
            DecisionTraceArchiveJsonCodec.encode(
                DecisionTraceArchive().append(readyTrace()),
            ),
        )
        val trace = current.getJSONArray("traces").getJSONObject(0)
        trace.put("schemaVersion", DecisionTrace.LEGACY_SCHEMA_VERSION)
        val candidates = trace
            .getJSONObject("featureEvaluation")
            .getJSONArray("candidates")
        for (index in 0 until candidates.length()) {
            val truth = candidates
                .getJSONObject(index)
                .getJSONObject("features")
                .getJSONObject("truthCredibility")
            if (truth.getString("kind") == "projected") {
                val currentValue = truth.getJSONObject("value")
                truth.put(
                    "value",
                    JSONObject().apply {
                        put(
                            "truthDangerReasonCodes",
                            currentValue.getJSONArray("truthDangerReasonCodes"),
                        )
                        put(
                            "credibilityDisruptionReasonCodes",
                            currentValue.getJSONArray("credibilityDisruptionReasonCodes"),
                        )
                    },
                )
            }
        }

        val decoded = DecisionTraceArchiveJsonCodec.decode(current.toString())
        val migrated = decoded.traces.single()
        assertEquals(DecisionTrace.CURRENT_SCHEMA_VERSION, migrated.schemaVersion)
        val migratedFeatures =
            (migrated.featureEvaluation as DecisionFeatureEvaluation.Ready)
                .candidates
                .first()
                .features
                .truthCredibility as FeatureProjection.Projected
        assertFalse(migratedFeatures.value.hasTypedMaterial)
        assertEquals(setOf("truth-danger"), migratedFeatures.value.truthDangerReasonCodes)
        assertEquals(
            setOf("credibility-disruption"),
            migratedFeatures.value.credibilityDisruptionReasonCodes,
        )

        val reencoded = JSONObject(DecisionTraceArchiveJsonCodec.encode(decoded))
        assertEquals(
            DecisionTrace.CURRENT_SCHEMA_VERSION,
            reencoded.getJSONArray("traces").getJSONObject(0).getInt("schemaVersion"),
        )
    }

    @Test
    fun `codec rejects non canonical prefixes and duplicate archive keys`() {
        val encoded = DecisionTraceArchiveJsonCodec.encode(
            DecisionTraceArchive().append(readyTrace()),
        )

        val nonCanonicalPrefix = JSONObject(encoded).apply {
            getJSONArray("traces")
                .getJSONObject(0)
                .getJSONObject("historyPrefix")
                .put("kind", "notCaptured")
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(nonCanonicalPrefix)
        }

        val duplicateIdentical = JSONObject(encoded).apply {
            val traces = getJSONArray("traces")
            traces.put(JSONObject(traces.getJSONObject(0).toString()))
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(duplicateIdentical)
        }

        val duplicateConflict = JSONObject(encoded).apply {
            val traces = getJSONArray("traces")
            traces.put(
                JSONObject(traces.getJSONObject(0).toString()).apply {
                    put("evidenceCheckpoint", "different-checkpoint")
                },
            )
        }.toString()
        assertThrows(IllegalArgumentException::class.java) {
            DecisionTraceArchiveJsonCodec.decode(duplicateConflict)
        }
    }

    @Test
    fun `store persists immutable archive idempotently and fails closed on conflicts`() {
        var stored: String? = null
        var writes = 0
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { raw ->
                writes += 1
                stored = raw
                true
            },
        )
        val trace = readyTrace()

        assertEquals(DecisionTraceArchive(), store.load())
        assertTrue(store.append(trace))
        assertEquals(1, writes)
        assertEquals(trace, store.load().find(trace.archiveKey))

        assertTrue(store.append(trace))
        assertEquals(1, writes)

        val conflict = trace.copy(
            evidenceCheckpoint = EvidenceCheckpointId("different-checkpoint"),
        )
        assertThrows(IllegalArgumentException::class.java) {
            store.append(conflict)
        }
        assertEquals(1, writes)
        assertEquals(trace, store.load().find(trace.archiveKey))
    }

    @Test
    fun `store never silently repairs malformed persisted data`() {
        val store = DecisionTraceArchiveStore(
            readRaw = { """{"formatVersion":1,"traces":"broken"}""" },
            writeRaw = { true },
        )

        assertThrows(IllegalArgumentException::class.java) {
            store.load()
        }
    }

    @Test
    fun `store reports failed durable write without claiming success`() {
        var stored: String? = null
        val store = DecisionTraceArchiveStore(
            readRaw = { stored },
            writeRaw = { false },
        )

        assertEquals(false, store.append(readyTrace()))
        assertEquals(null, stored)
    }

    private fun readyTrace(): DecisionTrace {
        val candidateIds = listOf("candidate-a", "candidate-b")
        val historicalRoute = HealthyInformationRouteRef.HistoricalObservation(
            recordId = "obs-1",
            recipientSeat = 2,
        )
        val currentRoute = HealthyInformationRouteRef.CurrentCandidate(
            candidateId = "candidate-a",
            recipientSeat = 2,
        )
        val strategicWorld = StrategicWorldKey(
            demonSeat = 5,
            minionSeats = listOf(6),
        )
        val confirmationChannel = ConfirmationChannelRef.Source(
            sourceSeat = 2,
            sourceAbility = RoleId("empath"),
        )
        val exposureTarget = RoleFunctionExposureTargetRef(
            seat = 7,
            role = RoleId("recluse"),
            recipientSeat = 2,
            capability = RoleFunctionExposureCapability.REGISTRATION_AMBIGUITY,
        )
        val features = DecisionFeatures(
            strategic = FeatureProjection.Projected(
                StrategicDecisionFeatures(
                    demonCoverRetention = StrategicRatio.Defined(2, 3),
                    evilTopologyRetention = StrategicRatio.Defined(3, 4),
                    evilCoverRetention = StrategicRatio.Defined(4, 5),
                    forcedGoodFraction = StrategicRatio.Defined(1, 8),
                    forcedEvilFraction = StrategicRatio.Defined(1, 8),
                    forcedGoodSeats = setOf(1),
                    forcedEvilSeats = setOf(5),
                ),
            ),
            confirmationChainImpact = FeatureProjection.Projected(
                ConfirmationChainFeatures(
                    candidateRemovedExactWorldCount = BigInteger.valueOf(2L),
                    candidateRemovedStrategicWorldKeys = setOf(strategicWorld),
                    candidateRemovedDemonSeats = setOf(5),
                    candidateChannel = confirmationChannel,
                    historicalObservationImpacts = listOf(
                        HistoricalObservationConfirmationImpact(
                            provenance = ConfirmationObservationProvenance(
                                observationRef = SdeHistoricalObservationRef(
                                    recordId = "obs-1",
                                    globalSequence = 3L,
                                ),
                                sourceSeat = 3,
                                sourceAbility = RoleId("washerwoman"),
                                channel = ConfirmationChannelRef.Source(
                                    sourceSeat = 3,
                                    sourceAbility = RoleId("washerwoman"),
                                ),
                            ),
                            relation = ConfirmationObservationRelation.SUPPORTS_EXISTING_OBSERVATION,
                            restoration = ConfirmationAmbiguityRestoration(
                                restoredExactWorldCount = BigInteger.ONE,
                                restoredStrategicWorldKeys = setOf(strategicWorld),
                                restoredDemonSeats = setOf(5),
                            ),
                            authenticatesDistinctSource = true,
                            wasIndependentlyConstrainingBefore = true,
                        ),
                    ),
                ),
            ),
            healthyInformationUtility = FeatureProjection.Projected(
                HealthyInformationUtilityFeatures(
                    usableHealthyRouteRefsBefore = setOf(historicalRoute),
                    independentHealthyRouteRefsBefore = setOf(historicalRoute),
                    usableHealthyRouteRefsAfter = setOf(historicalRoute, currentRoute),
                    independentHealthyRouteRefsAfter = setOf(currentRoute),
                    newlyRedundantHealthyRouteRefs = emptySet(),
                    contradictedHealthyRouteRefs = emptySet(),
                    currentCandidateHealthyRouteRef = currentRoute,
                ),
            ),
            truthCredibility = FeatureProjection.Projected(
                TruthCredibilityFeatures(
                    truthDangerReasonCodes = setOf("truth-danger"),
                    credibilityDisruptionReasonCodes = setOf("credibility-disruption"),
                ),
            ),
            roleFunctionExposure = FeatureProjection.Projected(
                RoleFunctionExposureFeatures(
                    directlyExposedTargets = setOf(exposureTarget),
                    alreadyExposedTargets = emptySet(),
                    newlyExposedTargets = setOf(exposureTarget),
                    confirmationAmplifiedTargets = emptySet(),
                    forcedExposureTargets = emptySet(),
                    avoidableExposureTargets = setOf(exposureTarget),
                ),
            ),
            semanticTruth = FeatureProjection.Projected(SemanticTruth.TRUE),
            impairedNarrative = FeatureProjection.Projected(
                ImpairedNarrativeFeatures(
                    currentAbilityState = AbilityState.MALFUNCTIONING_DRUNK,
                    impairmentLifetime = ImpairmentLifetime.PERSISTENT_SETUP_BOUND,
                    priorImpairedObservationIds = setOf("obs-1"),
                    contradictoryPriorObservationIds = emptySet(),
                    relation = ImpairedNarrativeRelation.COMPATIBLE_WITH_PRIOR_IMPAIRED_NARRATIVE,
                    transitionNecessity = NarrativeTransitionNecessity.NONE,
                    detectabilitySignal = ImpairedNarrativeDetectabilitySignal.NONE,
                ),
            ),
            bluffNarrative = FeatureProjection.Projected(
                BluffNarrativeFeatures(
                    claimBurdenReasonCodes = setOf("claim-burden"),
                    narrativeRouteIds = setOf("route-1"),
                ),
            ),
            relationships = FeatureProjection.Projected(
                DecisionRelationshipFeatures(
                    collisionCandidateIds = setOf("candidate-b"),
                    supportCandidateIds = emptySet(),
                ),
            ),
            futureFlexibility = FeatureProjection.Projected(
                FutureFlexibilityFeatures(
                    retainedRouteIds = setOf("future-1"),
                    lostRouteIds = setOf("future-2"),
                    reasonCodes = setOf("future-flex"),
                ),
            ),
        )
        val evaluations = listOf(
            PolicyEvaluation(
                candidateId = "candidate-a",
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                disposition = PolicyDisposition.SURVIVOR,
                softPreferenceReasons = setOf(PolicyReasonCode("soft-a")),
                equivalenceState = PolicyEquivalenceState.Tied(candidateIds.toSet()),
            ),
            PolicyEvaluation(
                candidateId = "candidate-b",
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                disposition = PolicyDisposition.SURVIVOR,
                softPreferenceReasons = setOf(PolicyReasonCode("soft-b")),
                equivalenceState = PolicyEquivalenceState.Tied(candidateIds.toSet()),
            ),
        )

        return DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
            decisionId = "numeric|Empath|game-1|FirstNight|1|0|2|LIVING_EVIL_NEIGHBOURS",
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 0,
            ),
            sourceRevision = InformationDecisionRevision(
                gameStateRevision = 3L,
                playerInputRevision = 5L,
            ),
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = "game-1",
                actionRefs = listOf(
                    SdeHistoricalActionRef(
                        actionId = "action-1",
                        globalSequence = 1L,
                    ),
                ),
                observationRefs = listOf(
                    SdeHistoricalObservationRef(
                        recordId = "obs-1",
                        globalSequence = 3L,
                    ),
                ),
            ),
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Ready(
                candidates = listOf(
                    CandidateDecisionFeatures("candidate-a", features),
                    CandidateDecisionFeatures(
                        "candidate-b",
                        features.copy(
                            healthyInformationUtility =
                                FeatureProjection.Unavailable(FeatureUnavailableReason.NOT_APPLICABLE),
                            semanticTruth = FeatureProjection.Projected(SemanticTruth.FALSE),
                        ),
                    ),
                ),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Ready(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                evaluations = evaluations,
                limitations = setOf(
                    PolicyLimitationCode("preference-evidence-not-authorized"),
                ),
            ),
            policySelection = PolicySelection(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateId = "candidate-a",
                method = PolicySelectionMethod.SEEDED_HASH_V1,
            ),
            actualChoice = DecisionTraceActualChoice.Committed(
                candidateId = "candidate-a",
                source = InformationDecisionSource.RECOMMENDATION_ACCEPTED,
                manualOverride = false,
            ),
        )
    }

    private fun deferredTrace(): DecisionTrace {
        val candidateIds = listOf("candidate-c", "candidate-d")
        return DecisionTrace(
            evidenceCheckpoint = EvidenceCheckpointId("sde-3b-merged-2026-09-24"),
            decisionId = "pair|Investigator|game-1|FirstNight|1|1|MINION_PAIR",
            lifecycleStage = SdeDecisionLifecycleStage.Interaction(
                phase = StorytellerPhase.FIRST_NIGHT,
                round = 1,
                sequence = 1,
            ),
            sourceRevision = InformationDecisionRevision(
                gameStateRevision = 4L,
                playerInputRevision = 5L,
            ),
            historyPrefixRef = SdeHistoricalPrefixRef.Global(
                gameId = "game-1",
                actionRefs = emptyList(),
                observationRefs = emptyList(),
            ),
            legalCandidateIds = candidateIds,
            featureEvaluation = DecisionFeatureEvaluation.Deferred(
                candidateIds = candidateIds,
                missingCapabilities = setOf(
                    EpistemicEvaluationCapability.EXACT_HISTORICAL_REPLAY,
                    EpistemicEvaluationCapability.EXACT_HYPOTHETICAL_OBSERVATION,
                ),
            ),
            policySnapshot = DecisionTracePolicySnapshot.Deferred(
                policyVersion = PolicyVersions.BEGINNER_CONSERVATIVE_V1,
                candidateIds = candidateIds,
                reasons = setOf(PolicyDeferralCode("test-deferred")),
            ),
            policySelection = null,
        )
    }
}