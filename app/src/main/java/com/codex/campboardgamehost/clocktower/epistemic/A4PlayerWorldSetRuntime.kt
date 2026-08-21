package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.RoleDefinition
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * Snapshot-facing A3/A4 entry point. The construction core consumes only [KnowledgeSafeWorldInput]
 * plus declared player knowledge; complete storyteller truth is projected away before enumeration.
 */
class A4PlayerWorldSetRuntime(
    private val policy: A4WorldEngineRuntimePolicy = A4WorldEngineRuntimePolicy(),
    private val telemetry: A4WorldEngineTelemetryRecorder = A4WorldEngineTelemetryRecorder(),
) {
    fun build(
        snapshot: GameSnapshot,
        phase: StorytellerPhase,
        round: Int,
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
        publicPropositions: List<InformationProposition> = emptyList(),
        storytellerOnlyPropositions: List<InformationProposition> = emptyList(),
    ): A4PlayerWorldSetBuild = build(
        FormalGameState.from(snapshot, phase, round, publicPropositions, storytellerOnlyPropositions),
        knowledge,
        hypothesis,
        roleDefinitions,
    )

    /** Compatibility adapter: formal storyteller truth crosses no further than this projection. */
    fun build(
        formal: FormalGameState,
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
    ): A4PlayerWorldSetBuild = build(
        formal.toKnowledgeSafeWorldInput(),
        knowledge,
        hypothesis,
        roleDefinitions,
    )

    /** Actual world-construction core. This API has no access to formal secret player fields. */
    fun build(
        input: KnowledgeSafeWorldInput,
        knowledge: PlayerKnowledgeSnapshot,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
    ): A4PlayerWorldSetBuild {
        require(knowledge.formalSnapshotId == input.formalSnapshotId) {
            "Player knowledge must be bound to the current formal snapshot."
        }
        require(knowledge.recipientSeat in input.playerSeats) {
            "Player knowledge recipient must exist in the knowledge-safe structural input."
        }
        val structuralKnowledge = knowledge.copy(
            setupKnowledge = (knowledge.setupKnowledge + InformationProposition.PlayerCount(input.playerCount)).distinct(),
        )
        fun enumerated(): Timed<EnumeratedWorldSet> = timed {
            TroubleBrewingWorldEnumerator.enumerate(input.rulesetRef, structuralKnowledge, hypothesis, roleDefinitions)
        }.also { measured ->
            record(A4WorldEngineOperation.BUILD, A4WorldSetRepresentation.ENUMERATED, measured.value, measured.elapsedMillis)
        }
        fun directZdd(): Timed<ZddPlayerWorldSet> = timed {
            ZddPlayerWorldSet.enumerateDirect(input.rulesetRef, structuralKnowledge, hypothesis, roleDefinitions)
        }.also { measured ->
            record(A4WorldEngineOperation.BUILD, A4WorldSetRepresentation.ZDD, measured.value, measured.elapsedMillis)
        }

        val baseline = if (policy.preferredRepresentation() == A4WorldSetRepresentation.ENUMERATED) enumerated() else null
        val shadow = if (policy.shadowsZdd()) {
            val measured = directZdd()
            require(measured.value.cardinality() == baseline!!.value.cardinality()) {
                "ZDD shadow cardinality disagrees with baseline."
            }
            measured.value
        } else null
        val selected: PlayerWorldSet = when (policy.preferredRepresentation()) {
            A4WorldSetRepresentation.ENUMERATED -> baseline!!.value
            A4WorldSetRepresentation.ZDD -> directZdd().value
        }
        return A4PlayerWorldSetBuild(selected, shadow, telemetry.snapshot())
    }

    /** Builds one isolated possible-world set per recipient from the same replay log. */
    fun buildAll(
        snapshot: GameSnapshot,
        phase: StorytellerPhase,
        round: Int,
        perceivedRolesBySeat: Map<Int, com.codex.campboardgamehost.clocktower.domain.RoleId>,
        observations: Collection<EpistemicObservation>,
        hypothesis: EpistemicHypothesis,
        roleDefinitions: Collection<RoleDefinition>,
        setupKnowledge: Collection<InformationProposition> = emptyList(),
    ): Map<Int, A4PlayerWorldSetBuild> {
        val formal = FormalGameState.from(snapshot, phase, round)
        return A4PlayerKnowledgeFactory.createAll(formal, perceivedRolesBySeat, observations, setupKnowledge)
            .associateBy(PlayerKnowledgeSnapshot::recipientSeat) { knowledge ->
                build(formal.toKnowledgeSafeWorldInput(), knowledge, hypothesis, roleDefinitions)
            }
    }

    private fun record(
        operation: A4WorldEngineOperation,
        representation: A4WorldSetRepresentation,
        worldSet: PlayerWorldSet,
        elapsedMillis: Long,
    ) = telemetry.record(A4WorldEngineTelemetry(
        operation = operation,
        representation = representation,
        filterStrategy = (worldSet as? ZddPlayerWorldSet)?.lastFilterStrategy,
        elapsedMillis = elapsedMillis,
        cardinality = worldSet.cardinality(),
        recommendation = policy.recommendation(elapsedMillis),
    ))

    private inline fun <T> timed(block: () -> T): Timed<T> {
        val start = System.nanoTime()
        val value = block()
        return Timed(value, (System.nanoTime() - start) / NANOS_PER_MILLI)
    }

    private data class Timed<T>(val value: T, val elapsedMillis: Long)
    private companion object { const val NANOS_PER_MILLI = 1_000_000L }
}

data class A4PlayerWorldSetBuild(
    val selected: PlayerWorldSet,
    /** Present only in shadow rollout; never used to make storyteller or UI decisions. */
    val zddShadow: ZddPlayerWorldSet?,
    val telemetry: List<A4WorldEngineTelemetry>,
)
