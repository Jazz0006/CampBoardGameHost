package com.codex.campboardgamehost.clocktower.epistemic

/**
 * Exact immutable evil-seat topology domain for Trouble Brewing setup.
 *
 * This is deliberately smaller than mechanical-world enumeration: it chooses only the setup Demon
 * seat and setup Minion seats. Role identities, good-role allocation, shown roles, impairment,
 * Red Herring state and registration remain later witness-feasibility dimensions.
 */
internal object TroubleBrewingStrategicTopologyDomain {
    fun enumerate(
        playerCount: Int,
        profile: InformationProposition.SetupProfile,
    ): List<StrategicWorldKey> {
        require(profile == TroubleBrewingSetupProfiles.standard(playerCount) ||
            profile == TroubleBrewingSetupProfiles.withBaron(playerCount)
        ) {
            "Strategic topology enumeration requires a legal Trouble Brewing setup profile."
        }
        require(profile.demons == 1) {
            "Trouble Brewing strategic topology requires exactly one Demon."
        }

        val seats = (1..playerCount).toList()
        return buildList {
            for (demonSeat in seats) {
                val remainingSeats = seats.filterNot { it == demonSeat }
                combinations(remainingSeats, profile.minions).forEach { minionSeats ->
                    add(
                        StrategicWorldKey(
                            demonSeat = demonSeat,
                            minionSeats = minionSeats,
                        ),
                    )
                }
            }
        }
    }

    private fun combinations(
        values: List<Int>,
        count: Int,
    ): Sequence<List<Int>> = sequence {
        require(count >= 0)
        if (count == 0) {
            yield(emptyList())
            return@sequence
        }
        if (count > values.size) return@sequence

        val selected = ArrayList<Int>(count)
        suspend fun SequenceScope<List<Int>>.walk(start: Int) {
            if (selected.size == count) {
                yield(selected.toList())
                return
            }

            val remainingNeeded = count - selected.size
            val lastStart = values.size - remainingNeeded
            for (index in start..lastStart) {
                selected += values[index]
                walk(index + 1)
                selected.removeAt(selected.lastIndex)
            }
        }

        walk(0)
    }
}
