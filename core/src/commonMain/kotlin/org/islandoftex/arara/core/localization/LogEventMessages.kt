// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for log event messages. */
interface LogEventMessages {
    /**
     * The log marker that starts the captured output buffer of a
     * command.
     */
    val logInfoBeginBuffer: String

    /**
     * The log line recording the value of a boolean entry.
     */
    fun logInfoBooleanMode(value: Boolean): String

    /**
     * The log heading of the block listing all directives.
     */
    val logInfoDirectivesBlock: String

    /**
     * The log marker that ends the captured output buffer of a command.
     */
    val logInfoEndBuffer: String

    /**
     * The log line recording that a rule is about to be interpreted.
     */
    fun logInfoInterpretRule(rule: String): String

    /**
     * The log line recording that a task of a rule is about to be
     * interpreted.
     */
    fun logInfoInterpretTask(task: String, rule: String): String

    /**
     * The log line recording a potential directive pattern found during
     * extraction.
     */
    fun logInfoPotentialPatternFound(line: String, pattern: String): String

    /**
     * The log line recording the directory a rule was resolved from.
     */
    fun logInfoRuleLocation(path: String): String

    /**
     * The log line recording the system command about to run.
     */
    fun logInfoSystemCommand(command: String): String

    /**
     * The log heading of the result of a task.
     */
    val logInfoTaskResult: String

    /**
     * The log line recording that all directives passed validation.
     */
    val logInfoValidatedDirectives: String

    /**
     * The log line greeting the user with the current version.
     */
    fun logInfoWelcomeMessage(version: String): String
}
