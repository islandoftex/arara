// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.CommandEvaluator
import org.islandoftex.arara.core.rules.InputHandling
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.mvel.interpreter.MvelCommandEvaluator
import org.islandoftex.arara.mvel.rules.SerialRuleCommand

internal class CommandExecution(private val executionOptions: ExecutionOptions) {
    private val logger = KotlinLogging.logger(Interpreter::class.java.name)

    // Read only after failure: booleans set 1; commands set their nonzero exit status (or 1).
    private var lastFailureCode = 1
    private var failureOutput = ""

    /**
     * "Run" a boolean return value
     * @param value The boolean.
     * @param conditional The conditional to print in dry-run mode.
     * @param authors The authors of the rule.
     * @return Returns [value]
     */
    private fun runBoolean(
        value: Boolean,
        conditional: DirectiveConditional,
        authors: List<String>
    ): Boolean = value.also {
        if (!value) lastFailureCode = 1
        logger.info {
            "Boolean value: $it"
        }

        if (executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.printWrapped(
                LanguageController.messages
                    .infoInterpreterDryRunModeBooleanMode(it)
            )
            DisplayUtils.printConditional(conditional)
        }
    }

    /**
     * Run a command
     *
     * @param command The command to run.
     * @param conditional The conditional applied to the run (only for printing).
     * @param authors The rule authors (only for printing).
     * @param exitCheck Determines whether the process exit code indicates success.
     * @param ruleIdentifier Rule context for wrappers invoking other tools.
     * @return Success of the execution.
     * @throws AraraException Execution failed.
     */
    @Throws(AraraException::class)
    private fun runCommand(
        command: Command,
        conditional: DirectiveConditional,
        authors: List<String>,
        exitCheck: (Int) -> Boolean,
        ruleIdentifier: String
    ): Boolean {
        logger.info {
            "System command: $command"
        }
        return if (executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            DisplayUtils.printAuthors(authors)
            DisplayUtils.renderer.printDryRunCommand(command.toString())
            DisplayUtils.printConditional(conditional)
            true
        } else {
            DisplayUtils.renderer.printTaskDetails(command.toString(), conditional, authors)
            CommandRunner.runResult(command, ruleIdentifier).let { result ->
                exitCheck(result.exitCode).also { success ->
                    failureOutput = if (success) "" else result.output
                    if (!success) lastFailureCode = result.exitCode.takeUnless { it == 0 } ?: 1
                }
            }
        }
    }

    /**
     * Converts the command evaluation result to a flat list.
     * @param result The result
     * @return A flat list.
     */
    private fun resultToList(result: Any?) = if (result == null && HaltRequest.exitCode != null) {
        emptyList()
    } else if (result is List<*>) {
        InputHandling.flatten(result)
    } else {
        listOf(result)
    }

    /**
     * Execute a command.
     * @param command The command to evaluate.
     * @param conditional Under which condition to execute.
     * @param rule The rule (only passed for output purposes).
     * @param parameters The parameters for evaluation
     * @throws AraraException Running the command failed.
     */
    @Throws(AraraException::class)
    fun executeCommand(
        command: RuleCommand,
        conditional: DirectiveConditional,
        rule: Rule,
        parameters: Map<String, Any>
    ): ExecutionStatus {
        val evaluator = commandEvaluator(command)
        return resultToList(evaluator.evaluate(parameters)).filter { it.toString().isNotBlank() }
            .also {
                HaltRequest.exitCode?.let { code ->
                    throw HaltExpectedException(
                        LanguageController.messages.errorInterpreterUserRequestedHalt,
                        ExecutionStatus.FinishedWithCode(code)
                    )
                }
            }
            .fold(ExecutionStatus.Processing() as ExecutionStatus) { previous, current ->
                val taskName = current.commandDisplayName(command.name)
                DisplayUtils.printEntry(rule.displayName!!, taskName)

                failureOutput = ""
                val success = executeValue(current, conditional, rule, evaluator)

                DisplayUtils.printEntryResult(success)
                if (!success) DisplayUtils.renderer.printCommandFailure(failureOutput)
                when {
                    executionOptions.haltOnErrors && !success ->
                        throw HaltExpectedException(
                            LanguageController
                                .messages.errorInterpreterCommandUnsuccessfulExit(taskName),
                            ExecutionStatus.FinishedWithCode(lastFailureCode)
                        )
                    success -> ExecutionStatus.Processing()
                    else -> ExecutionStatus.FinishedWithCode(lastFailureCode)
                }.let { if (it.exitCode != 0) it else previous }
            }
    }

    private fun executeValue(
        value: Any?, conditional: DirectiveConditional, rule: Rule, evaluator: CommandEvaluator
    ): Boolean = when (value) {
        is Boolean -> runBoolean(
            value, conditional,
            rule.authors
        )
        is Command -> runCommand(
            value, conditional,
            rule.authors, evaluator::isSuccessful, rule.identifier
        )
        else ->
            throw AraraExceptionWithHeader(
                LanguageController
                    .messages.errorInterpreterWrongReturnType
            )
    }


    private fun commandEvaluator(command: RuleCommand): CommandEvaluator = when (command) {
        is CommandEvaluator -> command
        is SerialRuleCommand -> MvelCommandEvaluator(command)
        else -> throw AraraExceptionWithHeader(
            LanguageController.messages.errorInterpreterWrongReturnType
        )
    }

}

private fun Any?.commandDisplayName(fallback: String?): String =
    (this as? Command)?.displayName?.takeIf { it.isNotBlank() }
        ?: fallback ?: LanguageController.messages.infoLabelUnnamedTask
