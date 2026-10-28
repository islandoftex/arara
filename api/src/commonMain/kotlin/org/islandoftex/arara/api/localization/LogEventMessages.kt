// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.api.localization

/** Typed messages for log event messages. */
public interface LogEventMessages {
    /**
     * The log marker that starts the captured output buffer of a
     * command.
     */
    public val logInfoBeginBuffer: String

    /**
     * The log line recording the value of a boolean entry.
     */
    public fun logInfoBooleanMode(value: Boolean): String

    /**
     * The log heading of the block listing all directives.
     */
    public val logInfoDirectivesBlock: String

    /**
     * The log marker that ends the captured output buffer of a command.
     */
    public val logInfoEndBuffer: String

    /**
     * The log line recording that a rule is about to be interpreted.
     */
    public fun logInfoInterpretRule(rule: String): String

    /**
     * The log line recording that a task of a rule is about to be
     * interpreted.
     */
    public fun logInfoInterpretTask(task: String, rule: String): String

    /**
     * The log line recording a potential directive pattern found during
     * extraction.
     */
    public fun logInfoPotentialPatternFound(line: String, pattern: String): String

    /**
     * The log line recording the directory a rule was resolved from.
     */
    public fun logInfoRuleLocation(path: String): String

    /**
     * The log line recording the system command about to run.
     */
    public fun logInfoSystemCommand(command: String): String

    /**
     * The log heading of the result of a task.
     */
    public val logInfoTaskResult: String

    /**
     * The log line recording that all directives passed validation.
     */
    public val logInfoValidatedDirectives: String

    /**
     * The log line greeting the user with the current version.
     */
    public fun logInfoWelcomeMessage(version: String): String
}
