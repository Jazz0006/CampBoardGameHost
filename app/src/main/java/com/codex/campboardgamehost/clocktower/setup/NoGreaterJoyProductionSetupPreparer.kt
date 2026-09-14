package com.codex.campboardgamehost.clocktower.setup

import com.codex.campboardgamehost.clocktower.catalog.ValidatedClocktowerRuleset
import com.codex.campboardgamehost.clocktower.domain.CommittedClocktowerSetup
import com.codex.campboardgamehost.clocktower.domain.CommittedSetupSeat
import com.codex.campboardgamehost.clocktower.domain.MurmurHash3
import com.codex.campboardgamehost.clocktower.domain.RoleId
import com.codex.campboardgamehost.clocktower.domain.ScriptId

/** Single pure preparation transaction for a new No Greater Joy production game. */
internal object NoGreaterJoyProductionSetupPreparer {
    fun prepare(
        ruleset: ValidatedClocktowerRuleset,
        playerCount: Int,
        gameSeed: Long,
    ): CommittedClocktowerSetup {
        require(ruleset.script.id == NO_GREATER_JOY_SCRIPT) {
            "No Greater Joy production preparation requires the No Greater Joy ruleset."
        }
        require(playerCount in SUPPORTED_PLAYER_COUNTS) {
            "No Greater Joy production setup supports five or six players."
        }

        val provider = requireNotNull(
            ClocktowerSetupProviderRegistry(
                providers = listOf(
                    ClocktowerSetupProvider(
                        script = NO_GREATER_JOY_SCRIPT,
                        providerId = GENERATED_PROVIDER_ID,
                        candidateSource = GeneratedSetupCandidateSource(
                            providerId = GENERATED_PROVIDER_ID,
                            ruleset = ruleset,
                        ),
                    ),
                ),
            ).find(NO_GREATER_JOY_SCRIPT),
        ) {
            "No Greater Joy generated setup provider is not registered."
        }
        val candidate = provider.candidates(
            SetupCandidateRequest(
                script = NO_GREATER_JOY_SCRIPT,
                playerCount = playerCount,
                setupSeed = gameSeed,
            ),
        ).single()
        val shownIdentityPolicy = SetupShownIdentityPolicyResolver().resolve(candidate, ruleset)
        val shownIdentityCommitment = SetupShownIdentityCommitter().commit(
            candidate = candidate,
            policy = shownIdentityPolicy,
            setupSeed = gameSeed,
        )
        val seatedActualRoles = candidate.actualRoles.sortedWith(
            Comparator { left, right ->
                val rankComparison = java.lang.Long.compareUnsigned(
                    seatRank(left, gameSeed),
                    seatRank(right, gameSeed),
                )
                if (rankComparison != 0) rankComparison else left.value.compareTo(right.value)
            },
        )

        return CommittedClocktowerSetup(
            script = candidate.script,
            setupSeed = gameSeed,
            assignments = seatedActualRoles.mapIndexed { index, actualRole ->
                CommittedSetupSeat(
                    seat = index + 1,
                    actualRole = actualRole,
                    shownRole = shownIdentityCommitment.shownRoleFor(actualRole),
                )
            },
            provenance = candidate.provenance,
        )
    }

    private fun seatRank(role: RoleId, gameSeed: Long): Long = MurmurHash3.low64Utf8(
        "$SEAT_NAMESPACE|$gameSeed|${role.value}",
    )

    private val NO_GREATER_JOY_SCRIPT = ScriptId("no_greater_joy")
    private val SUPPORTED_PLAYER_COUNTS = 5..6
    private const val GENERATED_PROVIDER_ID = "generated-seeded-v1"
    private const val SEAT_NAMESPACE = "no-greater-joy-seat-v1"
}
