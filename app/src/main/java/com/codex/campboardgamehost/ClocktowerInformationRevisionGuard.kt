package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.session.StorytellerDecisionRevision

/** A confirmed information result belongs to the same canonical Session revision as the preview. */
internal fun clocktowerInformationPublicationAllowed(
    sourceRevision: StorytellerDecisionRevision?,
    currentRevision: StorytellerDecisionRevision,
): Boolean = sourceRevision == null || sourceRevision == currentRevision
