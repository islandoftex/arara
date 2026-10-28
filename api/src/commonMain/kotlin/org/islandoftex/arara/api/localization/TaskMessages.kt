// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for task messages. */
public interface TaskMessages {
    /** Summary warning for legacy YAML rules scheduled for removal in version 9. */
    public val warningDeprecatedYaml: String

    /**
     * The error shown when the requested preamble is not defined in the
     * configuration.
     */
    public fun errorParserInvalidPreamble(name: String): String

    /**
     * The message shown when a boolean entry is met in dry-run mode.
     */
    public fun infoInterpreterDryRunModeBooleanMode(value: Boolean): String

    /**
     * The message shown instead of running a command in dry-run mode.
     */
    public fun infoInterpreterDryRunModeSystemCommand(command: String): String

    /**
     * The label shown for a single rule author.
     */
    public val infoLabelAuthor: String

    /**
     * The label shown for multiple rule authors.
     */
    public val infoLabelAuthors: String

    /** The label shown beside a system command in styled task details. */
    public val infoLabelSystemCommand: String

    /**
     * The label shown for the conditional of an entry in dry-run mode.
     */
    public val infoLabelConditional: String

    /**
     * The label shown when a rule has no authors.
     */
    public val infoLabelNoAuthors: String

    /** The warning dialog label. */
    public val infoLabelOnWarning: String

    /** The question dialog label. */
    public val infoLabelOnQuestion: String

    /** The information dialog label. */
    public val infoLabelOnInfo: String

    /** The numbered selection prompt; the range is appended literally. */
    public val infoLabelDialogPrompt: String

    /**
     * The label shown for the details of an entry.
     */
    public val infoLabelOnDetails: String

    /**
     * The label shown when an entry raises an error.
     */
    public val infoLabelOnError: String

    /**
     * The label shown when an entry fails.
     */
    public val infoLabelOnFailure: String

    /**
     * The label shown when an entry succeeds.
     */
    public val infoLabelOnSuccess: String

    /**
     * The label shown for a rule command without a name.
     */
    public val infoLabelUnnamedTask: String

    /**
     * The license note printed in the terminal.
     */
    public val infoParserNotes: String
}
