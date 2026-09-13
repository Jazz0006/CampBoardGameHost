from pathlib import Path

TARGET = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt")
HELPER = Path("app/src/main/java/com/codex/campboardgamehost/ClocktowerNightPresentationRole.kt")

raw = TARGET.read_bytes()
if b"\r\n" in raw or b"\r" in raw:
    raise SystemExit("Unexpected line ending; refusing implicit large-file normalization")
text = raw.decode("utf-8")

# All current role-name reads in this Compose function are presentation-local:
# first-night UI projection/family identity, dedicated square-table ownership,
# and the visible ability label. Domain/rules state remains on the original step.
role_read_count = text.count("step.roleEnName")
if role_read_count != 9:
    raise SystemExit(f"Expected exactly 9 presentation-local step.roleEnName reads, found {role_read_count}")
text = text.replace("step.roleEnName", "presentationRoleEnName")

old_anchor = """    val language = LocalContext.current.resources.configuration.locales[0].language
    fun optionId(option: ClocktowerDisplayOption): String = clocktowerInformationCandidateId(option)
"""
new_anchor = """    val language = LocalContext.current.resources.configuration.locales[0].language
    val presentationRoleEnName = clocktowerNightPresentationRoleEnName(
        stepRoleEnName = step.roleEnName,
        actor = step.actor,
    )
    fun optionId(option: ClocktowerDisplayOption): String = clocktowerInformationCandidateId(option)
"""
anchor_count = text.count(old_anchor)
if anchor_count != 1:
    raise SystemExit(f"Expected exactly one presentation-role insertion anchor, found {anchor_count}")
text = text.replace(old_anchor, new_anchor, 1)

if text.count("step.roleEnName") != 1:
    raise SystemExit("Only the presentation-role resolver may read step.roleEnName after patch")
required_tokens = [
    'roleEnName = presentationRoleEnName.orEmpty()',
    'familyId = presentationRoleEnName ?: "first-night-information"',
    'presentationRoleEnName in setOf("Washerwoman", "Librarian", "Investigator")',
    'presentationRoleEnName == "Chef"',
    'presentationRoleEnName == "Empath"',
    'presentationRoleEnName == "Undertaker"',
    'abilityLabel = presentationRoleEnName',
]
for token in required_tokens:
    if token not in text:
        raise SystemExit(f"Missing required post-patch semantic token: {token}")

if HELPER.exists():
    raise SystemExit(f"Helper already exists unexpectedly: {HELPER}")
helper_text = """package com.codex.campboardgamehost

/**
 * Resolves the role whose night information surface the Storyteller should present.
 *
 * A Drunk remains Drunk for rules/state ownership, but their night presentation follows the
 * Townsfolk character they believe they are. Other roles keep the step's authoritative role key.
 */
internal fun clocktowerNightPresentationRoleEnName(
    stepRoleEnName: String?,
    actor: PlayerCard?,
): String? {
    val actualRoleEnName = actor?.clocktowerRole?.enName
    val shownRoleEnName = actor?.clocktowerShownRole?.enName
    return if (actualRoleEnName == "Drunk" && !shownRoleEnName.isNullOrBlank()) {
        shownRoleEnName
    } else {
        stepRoleEnName
    }
}
"""

TARGET.write_text(text, encoding="utf-8", newline="\n")
HELPER.write_text(helper_text, encoding="utf-8", newline="\n")
