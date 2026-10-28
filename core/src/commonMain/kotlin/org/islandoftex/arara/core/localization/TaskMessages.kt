// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for task messages. */
interface TaskMessages {
    /** Summary warning for legacy YAML rules scheduled for removal in version 9. */
    val warningDeprecatedYaml: String

    /**
     * The error shown when the requested preamble is not defined in the
     * configuration.
     */
    fun errorParserInvalidPreamble(name: String): String

    /**
     * The message shown when a boolean entry is met in dry-run mode.
     */
    fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String

    /**
     * The message shown instead of running a command in dry-run mode.
     */
    fun infoInterpreterDryRunModeSystemCommand(command: String): String

    /**
     * The label shown for a single rule author.
     */
    val infoLabelAuthor: String

    /**
     * The label shown for multiple rule authors.
     */
    val infoLabelAuthors: String

    /** The label shown beside a system command in styled task details. */
    val infoLabelSystemCommand: String

    /**
     * The label shown for the conditional of an entry in dry-run mode.
     */
    val infoLabelConditional: String

    /**
     * The label shown when a rule has no authors.
     */
    val infoLabelNoAuthors: String

    /** The warning dialog label. */
    val infoLabelOnWarning: String

    /** The question dialog label. */
    val infoLabelOnQuestion: String

    /** The information dialog label. */
    val infoLabelOnInfo: String

    /** The numbered selection prompt; the range is appended literally. */
    val infoLabelDialogPrompt: String

    /**
     * The label shown for the details of an entry.
     */
    val infoLabelOnDetails: String

    /**
     * The label shown when an entry raises an error.
     */
    val infoLabelOnError: String

    /**
     * The label shown when an entry fails.
     */
    val infoLabelOnFailure: String

    /**
     * The label shown when an entry succeeds.
     */
    val infoLabelOnSuccess: String

    /**
     * The label shown for a rule command without a name.
     */
    val infoLabelUnnamedTask: String

    /**
     * The license note printed in the terminal.
     */
    val infoParserNotes: String
}
