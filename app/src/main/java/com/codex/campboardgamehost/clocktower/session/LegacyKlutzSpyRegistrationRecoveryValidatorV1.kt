package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.*

/**
 * Strict historical *read-only* validation for legacy impossible TB+Klutz+Spy
 * snapshots created by the 1C3A/1C3B synthetic fixture. No production path
 * can create these records. Preserve the old exact decoder rather than silently
 * changing Recovery semantics for any already persisted format.
 */
internal data class KlutzDeathTriggerProvenanceV1(
    val deathActionId: String,
    val deathSequence: Long,
    val lastPoisonActionId: String? = null,
    val lastPoisonSequence: Long? = null,
    val deathTriggerRevision: Long? = null,
) {
    val isVerifiedDeathSnapshot: Boolean get() = deathTriggerRevision != null
}

/** Pure proof shared by live writer and strict historical Recovery validation. */
internal object KlutzDeathTriggerProvenanceResolverV1 {
    fun verified(
        actions: List<ActionFact>,
        klutzSeat: Int,
    ): KlutzDeathTriggerProvenanceV1? {
        val ordered = actions.sortedBy { it.sequence }
        val death = ordered.lastOrNull {
            when (it) {
                is ActionFact.Death -> it.targetSeat == klutzSeat
                is ActionFact.Execution -> it.targetSeat == klutzSeat
                else -> false
            }
        } ?: return null
        // New canonical death facts have their actual predeath condition stamped by
        // ClocktowerGameSession. Never override a known poisoned-at-death value using
        // an older poison target that may be stale or incomplete.
        val predeath = when (death) {
            is ActionFact.Death -> death.klutzDeathTrigger
            is ActionFact.Execution -> death.klutzDeathTrigger
            else -> null
        }
        if (predeath != null) {
            if (!predeath.functioningAtDeath || !predeath.wasAlive ||
                predeath.actualRole != RoleId("Klutz")) return null
            return KlutzDeathTriggerProvenanceV1(
                deathActionId = death.actionId,
                deathSequence = death.sequence,
                deathTriggerRevision = predeath.sourceGameStateRevision,
            )
        }
        // Historical 1C3A format with no predeath snapshot: retain its narrower
        // positively proven prior-poison contract; absence remains UNKNOWN.
        val poison = ordered.lastOrNull {
            it is ActionFact.Poison && it.sequence < death.sequence
        } as? ActionFact.Poison ?: return null
        // An absent/corrupted poison prefix is UNKNOWN, not a proof of sobriety.
        if (poison.targetSeat == klutzSeat) return null
        return KlutzDeathTriggerProvenanceV1(
            death.actionId, death.sequence, poison.actionId, poison.sequence,
        )
    }
}

internal object LegacyKlutzSpyRegistrationRecoveryValidatorV1 {
    fun validateCommitted(
        frozen: FrozenStorytellerDecisionPrefixV1,
        event: StorytellerProviderPriorDecisionV1,
    ) {
        val type = StorytellerProviderDecisionContextV1.DAY_ABILITY_REGISTRATION
        require(frozen.identity.decisionTypeId == type && event.selectedOutcome.decisionType == type)
        val f = event.selectedOutcome.canonicalFields
        val priorKeys = setOf(
            "interactionId", "abilityRole", "actorSeat", "subjectSeat", "question",
            "status", "registeredAlignment", "evilWins",
            "deathActionId", "deathSequence", "lastPoisonActionId", "lastPoisonSequence",
        )
        require(f.keys == priorKeys || f.keys == priorKeys +
            setOf("deathEvidenceKind", "deathTriggerRevision"))
        require(f.getValue("interactionId").isNotBlank())
        require(f.getValue("abilityRole") == "Klutz")
        require(f.getValue("question") == RegistrationQuestion.ALIGNMENT.name)
        require((frozen.snapshotIdentity.position.phase as? SnapshotField.Known<StorytellerPhase>)?.value ==
            StorytellerPhase.DAY)
        val klutzSeat = f.getValue("actorSeat").toInt()
        val spySeat = f.getValue("subjectSeat").toInt()
        require(klutzSeat > 0 && spySeat > 0 && klutzSeat != spySeat)
        val klutz = frozen.snapshotIdentity.seats.single { it.seat == klutzSeat }
        val spy = frozen.snapshotIdentity.seats.single { it.seat == spySeat }
        require((klutz.actualRoleId as? SnapshotField.Known<String>)?.value == "Klutz")
        require((klutz.alive as? SnapshotField.Known<Boolean>)?.value == false)
        require((spy.actualRoleId as? SnapshotField.Known<String>)?.value == "Spy")
        require((spy.alive as? SnapshotField.Known<Boolean>)?.value == true)
        require((spy.poisoned as? SnapshotField.Known<Boolean>)?.value == false)
        val actions = frozen.historyPrefix.entries
            .filterIsInstance<StorytellerProviderHistoryEntryV1.Action>()
            .map { it.fact }
        val death = requireNotNull(KlutzDeathTriggerProvenanceResolverV1.verified(actions, klutzSeat)) {
            "Recovered Klutz ruling has no actual death-time sobriety evidence."
        }
        require(f.getValue("deathActionId") == death.deathActionId)
        require(f.getValue("deathSequence").toLong() == death.deathSequence)
        if ("deathEvidenceKind" in f) {
            require(f.getValue("deathEvidenceKind") ==
                if (death.isVerifiedDeathSnapshot) "SESSION_TRIGGER" else "LEGACY_POISON")
            require(f.getValue("deathTriggerRevision") == (death.deathTriggerRevision?.toString() ?: ""))
            require(f.getValue("lastPoisonActionId") == (death.lastPoisonActionId ?: ""))
            require(f.getValue("lastPoisonSequence") == (death.lastPoisonSequence?.toString() ?: ""))
        } else {
            // Validated 1C3A decision archives retain their original exact schema.
            require(!death.isVerifiedDeathSnapshot)
            require(f.getValue("lastPoisonActionId") == death.lastPoisonActionId)
            require(f.getValue("lastPoisonSequence").toLong() == death.lastPoisonSequence)
        }
        val status = RegistrationResolutionStatusV1.valueOf(f.getValue("status"))
        require(status == RegistrationResolutionStatusV1.EXPLICIT_ACTUAL ||
            status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL)
        val special = status == RegistrationResolutionStatusV1.EXPLICIT_SPECIAL
        require(f.getValue("registeredAlignment") == if (special) "GOOD" else "")
        require(f.getValue("evilWins") == (!special).toString())
        require(event.selectedCandidateId == if (special) "special:alignment:GOOD" else "actual")
        require(event.selectedCandidateId in frozen.legalCandidateIds)
        require(event.abilityState == AbilityState.FUNCTIONING)
        if (special) {
            require(event.truthRelation == TruthRelation.TRUE_TO_REGISTERED_STATE)
            val fact = event.registrations.single()
            require(fact.interactionId == f.getValue("interactionId"))
            require(fact.subjectSeat == spySeat && fact.reason == RegistrationReason.SPY_ABILITY)
            require(fact.registrationQuestion == RegistrationQuestion.ALIGNMENT)
            require(fact.registeredAlignment == Alignment.GOOD)
            require(fact.registeredType == null && fact.registeredRole == null)
        } else {
            require(event.truthRelation == TruthRelation.NOT_APPLICABLE)
            require(event.registrations.isEmpty())
        }
    }
}
