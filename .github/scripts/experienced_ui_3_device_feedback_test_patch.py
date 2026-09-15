from pathlib import Path

ROOT = Path('.')


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding='utf-8')


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding='utf-8')


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected exactly one anchor, found {count}')
    return text.replace(old, new, 1)


# Pair truth-marker contract: direct truth plus typed Spy/Recluse registration truth.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerPairInformationSquareTablePresentationTest.kt'
text = read(path)
old = '''    @Test
    fun `truth marker is driven only by selected candidate actual-role match`() {
        assertEquals(
            "✓",
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = "Chef",
                actualRoleId = "Chef",
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = "Chef",
                actualRoleId = "Empath",
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = false,
                selectedRoleId = "Chef",
                actualRoleId = "Chef",
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = null,
                actualRoleId = "Chef",
            ),
        )
    }
'''
new = '''    @Test
    fun `truth marker follows direct role truth for the selected candidate`() {
        val chefOption = option("Chef", 1, 4)
        assertEquals(
            "✓",
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = "Chef",
                actualRoleId = "Chef",
                selectedOption = chefOption,
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = "Chef",
                actualRoleId = "Empath",
                selectedOption = chefOption,
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = false,
                selectedRoleId = "Chef",
                actualRoleId = "Chef",
                selectedOption = chefOption,
            ),
        )
        assertNull(
            clocktowerPairInformationTruthMarker(
                isSelectedCandidate = true,
                selectedRoleId = null,
                actualRoleId = "Chef",
                selectedOption = chefOption,
            ),
        )
    }

    @Test
    fun `truth marker includes typed Spy and Recluse registration hits`() {
        val spyOption = option("Washerwoman", 2, 5).copy(
            spyRegistersGood = true,
            spyRegisteredRoleEnName = "Washerwoman",
        )
        val recluseOption = option("Poisoner", 3, 6).copy(
            recluseRegistersEvil = true,
            recluseRegisteredRoleEnName = "Poisoner",
        )

        assertEquals(
            "✓",
            clocktowerPairInformationTruthMarker(true, "Washerwoman", "Spy", spyOption),
        )
        assertEquals(
            "✓",
            clocktowerPairInformationTruthMarker(true, "Poisoner", "Recluse", recluseOption),
        )
        assertNull(clocktowerPairInformationTruthMarker(true, "Chef", "Spy", spyOption))
        assertNull(clocktowerPairInformationTruthMarker(true, "Chef", "Recluse", recluseOption))
    }
'''
text = replace_once(text, old, new, 'pair truth-marker test block')
write(path, text)


# Chef presentation: Spy/Recluse get a distinct registration palette; witness marker remains separate.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerChefSquareTablePresentationTest.kt'
text = read(path)
anchor = '''            actualEvilSeats = setOf(2, 3),
            recluseSeat = 4,
'''
if text.count(anchor) != 3:
    raise SystemExit(f'chef first-test visual anchors: expected 3, found {text.count(anchor)}')
text = text.replace(
    anchor,
    '''            actualEvilSeats = setOf(2, 3),
            spySeat = null,
            recluseSeat = 4,
''',
)
text = replace_once(
    text,
    '''            actualEvilSeats = clocktowerChefActualEvilSeats(players),
            recluseSeat = clocktowerChefRecluseSeat(players),
''',
    '''            actualEvilSeats = clocktowerChefActualEvilSeats(players),
            spySeat = null,
            recluseSeat = clocktowerChefRecluseSeat(players),
''',
    'chef recluse witness visual args',
)
text = replace_once(
    text,
    '        assertEquals(ClocktowerSquareTableSeatState.Neutral, visualRecluse.state)\n',
    '        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, visualRecluse.state)\n',
    'chef recluse static palette assertion',
)
text = replace_once(
    text,
    '''        assertEquals(ClocktowerSquareTableSeatState.SelectedHighlighted, recluseVisual.state)
        assertEquals("隐", recluseVisual.badge)
''',
    '''        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, recluseVisual.state)
        assertEquals("隐", recluseVisual.badge)
        assertEquals("★", recluseVisual.marker)
''',
    'chef counted recluse assertion',
)
text = replace_once(
    text,
    '''        assertEquals(setOf(2), clocktowerChefEffectiveEvilSeats(players, option))
        assertEquals(emptySet<Int>(), clocktowerChefEffectivePairSeats(players, option, value = 0))
        assertEquals(setOf(2, 3), clocktowerChefActualEvilSeats(players))
''',
    '''        assertEquals(setOf(2), clocktowerChefEffectiveEvilSeats(players, option))
        assertEquals(emptySet<Int>(), clocktowerChefEffectivePairSeats(players, option, value = 0))
        assertEquals(setOf(2, 3), clocktowerChefActualEvilSeats(players))

        val spyVisual = clocktowerChefSeatVisual(
            seatNumber = 3,
            actorSeat = 1,
            actualEvilSeats = setOf(2, 3),
            spySeat = 3,
            recluseSeat = null,
            effectivePairSeats = emptySet(),
            language = "zh",
        )
        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, spyVisual.state)
        assertEquals("间", spyVisual.badge)
        assertEquals(null, spyVisual.marker)
''',
    'chef spy palette assertions',
)
write(path, text)


# Empath presentation: evidence is limited to authoritative living-neighbour scope.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerEmpathSquareTablePresentationTest.kt'
text = read(path)
anchor = '''            actualEvilSeats = setOf(2, 3),
            recluseSeat = 5,
'''
if text.count(anchor) != 3:
    raise SystemExit(f'empath first-test visual anchors: expected 3, found {text.count(anchor)}')
text = text.replace(
    anchor,
    '''            actualEvilSeats = setOf(2, 3),
            spySeat = null,
            recluseSeat = 5,
''',
)
text = replace_once(
    text,
    '        assertEquals(ClocktowerSquareTableSeatState.Neutral, recluse.state)\n',
    '        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, recluse.state)\n',
    'empath recluse palette assertion',
)
text = replace_once(
    text,
    '''        assertTrue(actor.isCurrentActor)
        assertFalse(evil.isCurrentActor)
''',
    '''        assertTrue(actor.isCurrentActor)
        assertFalse(evil.isCurrentActor)

        val unrelatedEvil = clocktowerEmpathSeatVisual(
            seatNumber = 3,
            actorSeat = 1,
            scopeSeats = setOf(2, 5),
            actualEvilSeats = setOf(2, 3),
            spySeat = null,
            recluseSeat = 5,
            contributingSeats = emptySet(),
            language = "zh",
        )
        assertEquals(ClocktowerSquareTableSeatState.Neutral, unrelatedEvil.state)
        assertEquals(null, unrelatedEvil.badge)
''',
    'empath unrelated evil assertions',
)
text = replace_once(
    text,
    '''        assertEquals(emptySet<Int>(), clocktowerEmpathContributingSeats(players, option, value = 0))
        assertEquals(setOf(2), clocktowerEmpathActualEvilSeats(players))
''',
    '''        assertEquals(emptySet<Int>(), clocktowerEmpathContributingSeats(players, option, value = 0))
        assertEquals(setOf(2), clocktowerEmpathActualEvilSeats(players))

        val spyVisual = clocktowerEmpathSeatVisual(
            seatNumber = 2,
            actorSeat = 1,
            scopeSeats = setOf(2, 3),
            actualEvilSeats = setOf(2),
            spySeat = 2,
            recluseSeat = null,
            contributingSeats = emptySet(),
            language = "zh",
        )
        assertEquals(ClocktowerSquareTableSeatState.RegistrationHint, spyVisual.state)
        assertEquals("邻·间", spyVisual.badge)
        assertEquals(null, spyVisual.marker)
''',
    'empath spy palette assertions',
)
write(path, text)


# Undertaker player-facing reveal copy uses the existing RoleReveal template.
path = 'app/src/test/java/com/codex/campboardgamehost/ClocktowerUndertakerSquareTablePresentationTest.kt'
text = read(path)
anchor = '''    @Test
    fun `executed context and current actor are independent visual dimensions`() {
'''
new_test = '''    @Test
    fun `player reveal leads with executed player and shows role as primary`() {
        val choice = ClocktowerUndertakerResultChoice(
            key = "chef",
            executedSeat = 4,
            roleId = RoleId("Chef"),
            displayLabel = "厨师",
            sourceKind = ClocktowerUndertakerResultSourceKind.Direct,
        )
        val cards = List(7) { index ->
            PlayerCard(
                name = "P${index + 1}",
                role = Role.Civilian,
                word = "",
            )
        }
        val display = clocktowerUndertakerPlayerDisplayStep(undertakerStep(), choice, cards, "zh")

        assertEquals("昨天被处决的 4号 P4，身份是", display.displayTitle)
        assertEquals("厨师", display.displayPrimary)
        assertNull(display.displaySecondary)
        assertNull(display.displayFooter)
    }

''' + anchor
text = replace_once(text, anchor, new_test, 'undertaker reveal copy test')
write(path, text)

print('Experienced UI 3 presentation tests patched successfully')
