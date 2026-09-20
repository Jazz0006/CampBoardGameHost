package com.codex.campboardgamehost.clocktower.review

internal data class Sde2D5FHumanLabelEntry(
    val reviewId: String,
    val label: FirstNightBeginnerCorpusLabel,
    val reasons: Set<Sde2D5FReviewReason>,
    val note: String = "",
) {
    init {
        require(reviewId.isNotBlank())
        require(
            label != FirstNightBeginnerCorpusLabel.UNREVIEWED || reasons.isEmpty(),
        ) {
            "UNREVIEWED manifest entries cannot carry inferred review reasons."
        }
    }
}

internal data class Sde2D5FHumanLabelManifest(
    val version: String,
    val entries: List<Sde2D5FHumanLabelEntry>,
) {
    init {
        require(version.isNotBlank())
    }
}

internal data class Sde2D5FHumanLabelManifestValidation(
    val unknownReviewIds: Set<String>,
    val referenceReviewIds: Set<String>,
    val duplicateReviewIds: Set<String>,
    val entriesMissingReasons: Set<String>,
    val unreviewedRequiredReviewIds: Set<String>,
    val missingRequiredPolicyVariables: Set<Sde2D5FPolicyVariable>,
) {
    val isValid: Boolean
        get() =
            unknownReviewIds.isEmpty() &&
                referenceReviewIds.isEmpty() &&
                duplicateReviewIds.isEmpty() &&
                entriesMissingReasons.isEmpty()

    val isCompleteForGateDerivation: Boolean
        get() =
            isValid &&
                unreviewedRequiredReviewIds.isEmpty() &&
                missingRequiredPolicyVariables.isEmpty()
}

internal object Sde2D5FHumanLabelManifestBuilder {
    fun unreviewedTemplate(
        material: Sde2D5FCalibrationReviewMaterial,
        version: String,
    ): Sde2D5FHumanLabelManifest {
        require(version.isNotBlank())
        return Sde2D5FHumanLabelManifest(
            version = version,
            entries = material.records
                .filter { it.reviewability == Sde2D5FReviewability.REVIEWABLE }
                .map { record ->
                    Sde2D5FHumanLabelEntry(
                        reviewId = record.reviewId,
                        label = FirstNightBeginnerCorpusLabel.UNREVIEWED,
                        reasons = emptySet(),
                    )
                }
                .sortedBy(Sde2D5FHumanLabelEntry::reviewId),
        )
    }
}

internal object Sde2D5FHumanLabelManifestValidator {
    fun validate(
        material: Sde2D5FCalibrationReviewMaterial,
        manifest: Sde2D5FHumanLabelManifest,
    ): Sde2D5FHumanLabelManifestValidation {
        val recordsById = material.records.associateBy(Sde2D5FReviewRecord::reviewId)
        val referenceIds = material.records
            .filter { it.reviewability != Sde2D5FReviewability.REVIEWABLE }
            .mapTo(linkedSetOf(), Sde2D5FReviewRecord::reviewId)
        val requiredIds = material.records
            .filter { it.reviewability == Sde2D5FReviewability.REVIEWABLE }
            .mapTo(linkedSetOf(), Sde2D5FReviewRecord::reviewId)

        val entriesById = manifest.entries.groupBy(Sde2D5FHumanLabelEntry::reviewId)

        val unknown = entriesById.keys
            .filterNot(recordsById::containsKey)
            .toCollection(linkedSetOf())
        val references = entriesById.keys
            .filter(referenceIds::contains)
            .toCollection(linkedSetOf())
        val duplicates = entriesById
            .filterValues { it.size > 1 }
            .keys
            .toCollection(linkedSetOf())
        val missingReasons = manifest.entries
            .filter {
                it.label != FirstNightBeginnerCorpusLabel.UNREVIEWED &&
                    it.reasons.isEmpty()
            }
            .mapTo(linkedSetOf(), Sde2D5FHumanLabelEntry::reviewId)
        val unreviewedRequired = requiredIds
            .filter { reviewId ->
                val entries = entriesById[reviewId].orEmpty()
                entries.isEmpty() ||
                    entries.any { it.label == FirstNightBeginnerCorpusLabel.UNREVIEWED }
            }
            .toCollection(linkedSetOf())

        val storytellerOwnedPolicyVariables = material.records
            .filter {
                it.controlSurface.decisionOwner == Sde2D5FDecisionOwner.STORYTELLER_SDE
            }
            .flatMapTo(linkedSetOf()) { it.controlSurface.controllableVariables }
        val reviewablePolicyVariables = material.records
            .filter {
                it.reviewability == Sde2D5FReviewability.REVIEWABLE &&
                    it.controlSurface.decisionOwner == Sde2D5FDecisionOwner.STORYTELLER_SDE
            }
            .flatMapTo(linkedSetOf()) { it.controlSurface.controllableVariables }
        val missingRequiredPolicyVariables =
            (storytellerOwnedPolicyVariables - reviewablePolicyVariables)
                .toCollection(linkedSetOf())

        return Sde2D5FHumanLabelManifestValidation(
            unknownReviewIds = unknown,
            referenceReviewIds = references,
            duplicateReviewIds = duplicates,
            entriesMissingReasons = missingReasons,
            unreviewedRequiredReviewIds = unreviewedRequired,
            missingRequiredPolicyVariables = missingRequiredPolicyVariables,
        )
    }
}

internal object Sde2D5FHumanLabelManifestCodec {
    fun render(
        manifest: Sde2D5FHumanLabelManifest,
    ): String = buildString {
        appendLine("version=${escape(manifest.version)}")
        manifest.entries
            .sortedBy(Sde2D5FHumanLabelEntry::reviewId)
            .forEach { entry ->
                append(escape(entry.reviewId))
                append('\t')
                append(entry.label.name)
                append('\t')
                append(
                    entry.reasons
                        .sortedBy(Sde2D5FReviewReason::name)
                        .joinToString(",") { it.name },
                )
                append('\t')
                appendLine(escape(entry.note))
            }
    }

    fun parse(
        raw: String,
    ): Sde2D5FHumanLabelManifest {
        val lines = raw.lineSequence()
            .filter(String::isNotBlank)
            .toList()
        require(lines.isNotEmpty()) { "D5F label manifest is empty." }

        val versionLine = lines.first()
        require(versionLine.startsWith("version=")) {
            "D5F label manifest must start with version=<value>."
        }
        val version = unescape(versionLine.removePrefix("version="))
        require(version.isNotBlank())

        val entries = lines.drop(1).mapIndexed { index, line ->
            val fields = splitTabs(line)
            require(fields.size == 4) {
                "Malformed D5F label manifest line ${index + 2}: expected 4 tab-separated fields."
            }
            val reviewId = unescape(fields[0])
            val label = enumValueOrError<FirstNightBeginnerCorpusLabel>(
                fields[1],
                "label",
                index + 2,
            )
            val reasons = if (fields[2].isBlank()) {
                emptySet()
            } else {
                fields[2].split(',')
                    .mapTo(linkedSetOf()) { rawReason ->
                        enumValueOrError<Sde2D5FReviewReason>(
                            rawReason,
                            "review reason",
                            index + 2,
                        )
                    }
            }
            Sde2D5FHumanLabelEntry(
                reviewId = reviewId,
                label = label,
                reasons = reasons,
                note = unescape(fields[3]),
            )
        }

        val duplicates = entries.groupingBy(Sde2D5FHumanLabelEntry::reviewId)
            .eachCount()
            .filterValues { it > 1 }
            .keys
        require(duplicates.isEmpty()) {
            "Duplicate D5F label manifest review IDs: ${duplicates.sorted().joinToString()}."
        }

        return Sde2D5FHumanLabelManifest(
            version = version,
            entries = entries.sortedBy(Sde2D5FHumanLabelEntry::reviewId),
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrError(
        raw: String,
        fieldName: String,
        lineNumber: Int,
    ): T = try {
        enumValueOf<T>(raw)
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException(
            "Unknown D5F $fieldName '$raw' on manifest line $lineNumber.",
        )
    }

    private fun splitTabs(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var escaped = false
        line.forEach { ch ->
            when {
                escaped -> {
                    current.append('\\')
                    current.append(ch)
                    escaped = false
                }
                ch == '\\' -> escaped = true
                ch == '\t' -> {
                    fields += current.toString()
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        if (escaped) current.append('\\')
        fields += current.toString()
        return fields
    }

    private fun escape(value: String): String = buildString {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(ch)
            }
        }
    }

    private fun unescape(value: String): String = buildString {
        var index = 0
        while (index < value.length) {
            val ch = value[index]
            if (ch != '\\') {
                append(ch)
                index += 1
                continue
            }
            require(index + 1 < value.length) {
                "Trailing escape in D5F label manifest."
            }
            when (val escaped = value[index + 1]) {
                '\\' -> append('\\')
                't' -> append('\t')
                'n' -> append('\n')
                'r' -> append('\r')
                else -> throw IllegalArgumentException(
                    "Unsupported D5F label manifest escape: \\$escaped",
                )
            }
            index += 2
        }
    }
}
