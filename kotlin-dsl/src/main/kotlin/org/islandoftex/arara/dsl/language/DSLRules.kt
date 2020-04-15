// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.nio.file.Path
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.rules.Rule
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.rules.RuleCommand
import org.islandoftex.arara.api.session.Command

/**
 * A rule model class to capture DSL methods within.
 */
class DSLRule(
    private val id: String,
    private val label: String? = null,
    private val description: String = "",
    private val authors: List<String> = listOf()
) {
    private val commands = mutableListOf<RuleCommand>()

    /**
     * The currently collected arguments.
     */
    @Deprecated("This should not be used to fetch the arguments and " +
            "will be removed as soon as we found a way to remove the " +
            "inline/reified classification of argument.")
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
        vararg parameters: String,
        workingDirectory: Path? = null
    ): Command {
        TODO("command implementation")
    }

    /**
     * Execute an arbitrary block of code.
     *
     * @param executable The scope for the execution. Must return an exit code.
     * @return A [Command] object executing this piece of code.
     */
    fun execute(executable: DSLExecutable.() -> Int): RuleCommand {
        TODO("command implementation")
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
        configure: DSLRuleArgument.() -> Unit
    ): RuleArgument<*> {
        if (identifier in arguments)
            throw AraraException("Two rule arguments can't have the same " +
                    "identifier.")
        return DSLRuleArgument(identifier).apply(configure)
                .toRuleArgument<T>().also { ruleArguments.add(it) }
    }

    /**
     * Create a rule argument with .
     *
     * @param identifier The argument's identifier (for use in directives).
     * @param configure A configuration scope.
     * @return A [RuleArgument] with the given specifics.
     */
    @JvmName("nullableStringArgument")
    fun argument(identifier: String, configure: DSLRuleArgument.() -> Unit) =
            argument<String?>(identifier, configure)

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [Rule] resembling the user's configuration.
     */
    internal fun toRule(): Rule = org.islandoftex.arara.dsl.rules.Rule(
            identifier = id,
            displayName = label?.let { "$label ($description)" } ?: description,
            authors = authors,
            commands = commands,
            arguments = ruleArguments
    )
}

/**
 * A rule argument model to capture DSL methods within.
 */
class DSLRuleArgument(val identifier: String) {
    /**
     * Whether this argument is required in a directive.
     */
    var required: Boolean = false

    /**
     * The default value of the argument. If null and `T` is non-null you have
     * to specify a sef
     */
    var defaultValue: Any? = null

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [RuleArgument] resembling the user's configuration.
     */
    inline fun <reified T> toRuleArgument(): RuleArgument<T> {
        val default = if (defaultValue is T)
            defaultValue as T
        else
            throw AraraException("The default value has to match the given " +
                    "rule type.")
        return org.islandoftex.arara.dsl.rules.RuleArgument(
                identifier, isRequired = required, defaultValue = default
        )
    }
}

/**
 * An executable block model to capture DSL methods within.
 */
class DSLExecutable {
    private var exitValue = 0

    /**
     * Specify the exit value of the command.
     *
     * @param value The exit value.
     * @return The exit value.
     */
    fun exit(value: Int): Int = value.also { exitValue = value }
}
