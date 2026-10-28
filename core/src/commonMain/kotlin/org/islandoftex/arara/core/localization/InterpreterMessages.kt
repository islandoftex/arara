// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.localization

/** Typed messages for interpreter messages. */
interface InterpreterMessages {
    /**
     * The error shown when a conditional expression cannot be compiled.
     */
    val errorEvaluateCompilationFailed: String

    /**
     * The error shown when a conditional does not evaluate to a boolean
     * value.
     */
    val errorEvaluateNotBooleanValue: String

    /**
     * The error shown when directives cannot be extracted due to an IO
     * problem.
     */
    val errorExtractorIoError: String

    /**
     * The error shown when an entry in the directive options cannot be
     * interpolated.
     */
    val errorExtractorInterpolationFailure: String

    /**
     * The error shown when a required rule argument is missing from the
     * directive.
     */
    fun errorInterpreterArgumentIsRequired(argument: String): String

    /**
     * The error shown when a rule command cannot be evaluated.
     */
    val errorInterpreterCommandRuntimeError: String

    /**
     * The error shown when a command exits with a code its exit check
     * rejects.
     */
    fun errorInterpreterCommandUnsuccessfulExit(command: String): String

    /**
     * The message shown when a rule or a directive asks arara to halt.
     */
    val errorInterpreterUserRequestedHalt: String

    /**
     * The error shown when the exit status expression of a rule command
     * cannot be evaluated.
     */
    val errorInterpreterExitRuntimeError: String

    /**
     * The error shown when the flag expression of a rule argument cannot
     * be evaluated.
     */
    val errorInterpreterFlagRuntimeException: String

    /**
     * The error shown when a rule cannot be found in the configured
     * paths.
     */
    fun errorInterpreterRuleNotFound(identifier: String, expected: String, paths: String): String

    /**
     * The error shown when a directive carries keys the rule does not
     * declare.
     */
    fun errorInterpreterUnknownKeys(keys: String): String

    /**
     * The error shown when the exit expression of a rule command does
     * not return a boolean value.
     */
    val errorInterpreterWrongExitClosureReturn: String

    /**
     * The error shown when a rule command evaluates to an unsupported
     * return type.
     */
    val errorInterpreterWrongReturnType: String

    /**
     * The error shown when list replication has more or fewer
     * parameters than expected.
     */
    val errorReplicateListMissingFormatArgumentsException: String
}
