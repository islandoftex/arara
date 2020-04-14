// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.nio.file.Path
import kotlin.reflect.KClass
import org.islandoftex.arara.api.session.Command

class DSLRule(
    val id: String,
    val label: String = "",
    val description: String = "",
    val authors: List<String> = listOf()
) {
    private val arguments = mutableListOf<DSLRuleArgument>()
    private val executables = mutableListOf<Command>()

    fun command(
        command: String,
        vararg parameters: String,
        workingDirectory: Path?
    ): Command {
        TODO("command implementation")
    }

    fun execute(executable: () -> Int) {
        TODO("command implementation")
    }

    fun argument(identifier: String, configure: DSLRuleArgument.() -> Unit) =
            DSLRuleArgument(identifier).apply(configure).also {
                arguments.add(it)
            }
}

class DSLRuleArgument(val identifier: String) {
    var type: KClass<*> = String::class
    var required: Boolean = false
}
