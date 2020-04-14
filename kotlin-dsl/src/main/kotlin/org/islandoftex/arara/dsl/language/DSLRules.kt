// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.nio.file.Path
import kotlin.reflect.KClass
import org.islandoftex.arara.api.rules.RuleArgument
import org.islandoftex.arara.api.session.Command

/**
 * A rule model class to capture DSL methods within.
 */
class DSLRule(
    val id: String,
    val label: String = "",
    val description: String = "",
    val authors: List<String> = listOf()
) {
    private val arguments = mutableListOf<RuleArgument<*>>()
    private val executables = mutableListOf<Command>()

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
        workingDirectory: Path?
    ): Command {
        TODO("command implementation")
    }

    /**
     * Execute an arbitrary block of code.
     *
     * @param executable The scope for the execution. Must return an exit code.
     * @return A [Command] object executing this piece of code.
     */
    fun execute(executable: DSLExecutable.() -> Int): Command {
        TODO("command implementation")
    }

    /**
     * Create a rule argument.
     *
     * @param identifier The argument's identifier (for use in directives).
     * @param configure A configuration scope.
     * @return A [RuleArgument] with the given specifics.
     */
    fun argument(identifier: String, configure: DSLRuleArgument.() -> Unit) =
            DSLRuleArgument(identifier).apply(configure)
                    .toRuleArgument().also { arguments.add(it) }
}

/**
 * A rule argument model to capture DSL methods within.
 */
class DSLRuleArgument(val identifier: String) {
    /**
     * The type of the argument. By default a string because that is the input
     * in directives.
     */
    var type: KClass<*> = String::class

    /**
     * Whether this argument is required in a directive.
     */
    var required: Boolean = false

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [RuleArgument] resembling the user's configuration.
     */
    internal fun toRuleArgument(): RuleArgument<*> {
        TODO("rule argument implementation")
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
