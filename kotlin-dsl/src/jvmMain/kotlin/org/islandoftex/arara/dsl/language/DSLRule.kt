// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.api.session.Command
import org.islandoftex.arara.core.session.Environment
import org.islandoftex.arara.core.session.LinearExecutor
import java.nio.file.Path

/**
 * A rule model class to capture DSL methods within.
 */
class DSLRule(
    private val id: String,
    private val label: String? = null,
    private val description: String = "",
    private val authors: List<String> = listOf(),
) {
    private val commands = mutableListOf<RuleCommand>()

    /**
     * The currently collected arguments.
     */
    @Deprecated(
        "This should not be used to fetch the arguments and " +
            "will be removed as soon as we found a way to remove the " +
            "inline/reified classification of argument.",
    )
    val ruleArguments = mutableListOf<RuleArgument<*>>()

    /**
     * Rule authors may access the values of their arguments from here.
     * Alternatively, rule arguments created by `argument` may be saved in
     * variables.
     */
    // TODO: insert values here
    val arguments: Map<String, String>
        get() = ruleArguments.map { it.identifier to "" }.toMap()

    /**
     * Create a command within the rule.
     *
     * @param command The command's executable call.
     * @param parameters Other command line parameters.
     * @param workingDirectory The command's working directory. It will be
     *   resolved to the current project's working directory if null.
     * @return A [Command] for the specified configuration.
     */
    fun command(
        command: String,
        vararg parameters: String?,
        workingDirectory: Path? = null,
    ): Command = org.islandoftex.arara.core.session.Command(
        listOf(command)
            .plus(parameters.filterNotNull().filterNot { it.isBlank() }),
        workingDirectory?.toMPPPath(),
    ).also {
        commands.add(
            org.islandoftex.arara.dsl.rules.RuleCommand(command) {
                Environment.executeSystemCommand(
                    it,
                    !LinearExecutor.executionOptions.verbose,
                    LinearExecutor.executionOptions.timeoutValue,
                ).first
            },
        )
    }

    /**
     * Execute an arbitrary block of code.
     *
     * @param executable The scope for the execution. Must return an exit code.
     * @return A [Command] object executing this piece of code.
     */
    fun execute(
        name: String? = null,
        executable: DSLExecutable.() -> Unit,
    ): RuleCommand {
        val dslExecutable = DSLExecutable()
        return org.islandoftex.arara.dsl.rules.RuleCommand(name) {
            dslExecutable.executable()
            dslExecutable.exitValue
        }.also { commands.add(it) }
    }

    /**
     * Create a generic rule argument.
     *
     * @param identifier The argument's identifier (for use in directives).
     * @param configure A configuration scope.
     * @return A [RuleArgument] with the given specifics.
     */
    inline fun <reified T> argument(
        identifier: String,
        configure: DSLRuleArgument<T>.() -> Unit,
    ): RuleArgument<T> {
        if (identifier in arguments) {
            throw AraraException(
                "Two rule arguments can't have the same " +
                    "identifier.",
            )
        }
        return DSLRuleArgument<T>(identifier).apply(configure)
            .toRuleArgument().also { ruleArguments.add(it) }
    }

    /**
     * Create a rule argument with .
     *
     * @param identifier The argument's identifier (for use in directives).
     * @param configure A configuration scope.
     * @return A [RuleArgument] with the given specifics.
     */
    @JvmName("nullableStringArgument")
    fun argument(identifier: String, configure: DSLRuleArgument<String?>.() -> Unit) =
        argument<String?>(identifier, configure)

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [Rule] resembling the user's configuration.
     */
    internal fun toRule(): Rule = org.islandoftex.arara.dsl.rules.Rule(
        identifier = id,
        displayName = label ?: description,
        authors = authors,
        commands = commands,
        arguments = ruleArguments,
    )
}
