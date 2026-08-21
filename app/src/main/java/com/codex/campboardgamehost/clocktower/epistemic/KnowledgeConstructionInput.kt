package com.codex.campboardgamehost.clocktower.epistemic

/**
 * Knowledge-safe projection used while constructing recipient-scoped player knowledge.
 *
 * [worldInput] carries only opaque snapshot binding, ruleset identity and structural seats. The
 * only additional formal-state facts admitted here are propositions already classified as public.
 * Actual roles/alignment/type, poison state, shown roles and storyteller-only propositions are not
 * representable through this input unless a caller explicitly misclassifies them as public facts.
 */
data class KnowledgeConstructionInput(
    val worldInput: KnowledgeSafeWorldInput,
    val publicPropositions: List<InformationProposition> = emptyList(),
) {
    init {
        val seats = worldInput.playerSeats.toSet()
        require(publicPropositions.all { seats.containsAll(it.knowledgeBoundaryReferencedSeats()) }) {
            "Every public proposition seat must exist in the knowledge-safe structural input."
        }
    }

    val formalSnapshotId: String get() = worldInput.formalSnapshotId
    val playerSeats: List<Int> get() = worldInput.playerSeats
    val playerCount: Int get() = worldInput.playerCount
}

/** One-way projection: only explicitly public formal propositions cross the knowledge boundary. */
fun FormalGameState.toKnowledgeConstructionInput(): KnowledgeConstructionInput = KnowledgeConstructionInput(
    worldInput = toKnowledgeSafeWorldInput(),
    publicPropositions = publicPropositions,
)

/** Mirrors FormalGameState's proposition-seat validation at the safe direct-input boundary. */
private fun InformationProposition.knowledgeBoundaryReferencedSeats(): Set<Int> = when (this) {
    is InformationProposition.RoleAt -> setOf(seat)
    is InformationProposition.AlignmentAt -> setOf(seat)
    is InformationProposition.CharacterTypeAt -> setOf(seat)
    is InformationProposition.AliveAt -> setOf(seat)
    is InformationProposition.AbilityStateAt -> setOf(seat)
    is InformationProposition.RoleInPlay -> emptySet()
    is InformationProposition.PlayerCount -> emptySet()
    is InformationProposition.SetupProfile -> emptySet()
    is InformationProposition.AnyOf -> alternatives.flatMap { it.knowledgeBoundaryReferencedSeats() }.toSet()
    is InformationProposition.AllOf -> propositions.flatMap { it.knowledgeBoundaryReferencedSeats() }.toSet()
    is InformationProposition.Not -> proposition.knowledgeBoundaryReferencedSeats()
    is InformationProposition.NumericResult -> setOf(sourceSeat) + subjectSeats
    is InformationProposition.BooleanResult -> setOf(sourceSeat) + subjectSeats
    is InformationProposition.GrimoireState -> seats.mapTo(linkedSetOf()) { it.seat }
}
