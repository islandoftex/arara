// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.interpreter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraExceptionWithHeader
import org.islandoftex.arara.api.configuration.ExecutionMode
import org.islandoftex.arara.api.configuration.ExecutionOptions
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveConditional
import org.islandoftex.arara.api.rules.DirectiveConditionalType
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleEngine
import org.islandoftex.arara.api.session.ExecutionStatus
import org.islandoftex.arara.cli.ruleset.DirectiveImpl
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.cli.ruleset.RuleFiles
import org.islandoftex.arara.cli.utils.DisplayUtils
import org.islandoftex.arara.cli.utils.RunLog
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.lua.rules.LuaConditionalEvaluator
import org.islandoftex.arara.mvel.rules.DirectiveConditionalEvaluator

/**
 * Interprets the list of directives.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
class Interpreter(
    private val executionOptions: ExecutionOptions,
    private val currentFile: ProjectFile,
    private val workingDirectory: MPPPath,
    private val engine: RuleEngine = RuleEngine.YAML
) {
    private val logger = KotlinLogging.logger { }
    private val commands = CommandExecution(executionOptions)

    /**
     * Executes each directive, throwing an exception if something bad has
     * happened.
     *
     * @throws AraraException Something wrong happened, to be caught in the
     * higher levels.
     */
    @Throws(AraraException::class)
    fun execute(directive: Directive): ExecutionStatus {
        logger.info {
            "Interpreting rule \"${directive.identifier}\""
        }

        val file = RuleFiles.find(directive, executionOptions, workingDirectory, engine)
        val resolvedEngine = if (file.fileName.endsWith(".lua")) RuleEngine.LUA else RuleEngine.YAML
        RunLog.rule(directive.identifier, file.toString(), resolvedEngine.name.lowercase())
        if (resolvedEngine == RuleEngine.YAML && executionOptions.warnDeprecatedYaml) {
            RunLog.deprecatedYaml(directive.identifier, file.toString())
            DisplayUtils.deprecatedYamlUsed()
        }

        // parse the rule identified by the directive (may throw an exception)
        return try {
            val rule = RuleFiles.parseRule(file, directive.identifier)
            val parameters = directive.parameters.let { values ->
                if ("options" in values && executionOptions.executionMode != ExecutionMode.SAFE_RUN)
                    values.plus("options" to DirectiveUtils.interpolateOptions(
                        values.getValue("options"), resolvedEngine
                    ))
                else values
            }
            val resolvedDirective = DirectiveImpl(
                directive.identifier, parameters, directive.conditional, directive.lineNumbers, resolvedEngine
            )
            Interpreter(executionOptions, currentFile, workingDirectory, resolvedEngine)
                .executeRule(rule, resolvedDirective, file)
        } finally {
            HaltRequest.exitCode?.let {
                RunLog.haltRequested(directive.identifier, it, resolvedEngine.name.lowercase())
            }
        }
    }

    internal fun executeRule(rule: Rule, directive: Directive, file: MPPPath): ExecutionStatus {
        val parameters = RuleArguments.parseArguments(rule, directive)
        val evaluator = if (engine == RuleEngine.LUA) LuaConditionalEvaluator(executionOptions)
        else DirectiveConditionalEvaluator(executionOptions)
        val available =
            if (runPriorEvaluation(directive.conditional))
                evaluator.evaluate(directive.conditional)
            else true

        return when {
            !available -> {
                RunLog.skipped(directive.identifier, directive.conditional, engine.name.lowercase())
                DisplayUtils.printSkippedDirective(directive.identifier)
                ExecutionStatus.Processing()
            }
            HaltRequest.exitCode != null ->
                // a halt was requested by a rule
                ExecutionStatus.FinishedWithCode(HaltRequest.exitCode!!)
            else -> {
                try {
                    executeCommands(rule, directive.conditional, parameters, evaluator::evaluate)
                } catch (e: AraraExceptionWithHeader) {
                    // rethrow arara exceptions that are bound to have a header by
                    // prepending the header and removing the outer exception with
                    // header where possible
                    throw AraraException(
                        LanguageController.messages.errorRuleIdentifierAndPath(
                            directive.identifier, file.parent.toString()
                        ) + " " +
                            e.message,
                        e.exception ?: e
                    )
                }
            }
        }
    }

    private fun executeCommands(
        rule: Rule,
        conditional: DirectiveConditional,
        parameters: Map<String, Any>,
        repeat: (DirectiveConditional) -> Boolean
    ): ExecutionStatus {
        var status: ExecutionStatus = ExecutionStatus.Processing()
        try {
            do {
                for (command in rule.commands) {
                    status = retainFailure(status, commands.executeCommand(command, conditional, rule, parameters))
                }
            } while (repeat(conditional))
        } catch (exception: HaltExpectedException) {
            if (exception.status.exitCode != 0) status = exception.status
        }
        return status
    }

    private fun retainFailure(previous: ExecutionStatus, current: ExecutionStatus): ExecutionStatus =
        if (current.exitCode != 0) current else previous

    /**
     * Checks if the current conditional has a prior evaluation.
     *
     * @param conditional The current conditional object.
     * @return A boolean value indicating if the current conditional has a prior
     * evaluation.
     */
    private fun runPriorEvaluation(conditional: DirectiveConditional): Boolean {
        return if (LinearExecutor.executionOptions.executionMode == ExecutionMode.DRY_RUN) {
            false
        } else {
            when (conditional.type) {
                DirectiveConditionalType.IF,
                DirectiveConditionalType.WHILE,
                DirectiveConditionalType.UNLESS -> true
                else -> false
            }
        }
    }
}
