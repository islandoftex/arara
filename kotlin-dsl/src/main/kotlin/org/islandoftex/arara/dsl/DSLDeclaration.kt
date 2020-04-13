// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl

/**
 * An object holding all public members that are available as top-level
 * objects in DSL files.
 */
object DSLDeclaration {
    fun project(name: String, configure: DSLProject.() -> Unit): DSLProject =
            DSLProject(name).apply(configure).also {
                Executor.projects.add(it)
            }

    fun rule(
        id: String,
        label: String = "",
        description: String = "",
        authors: List<String> = listOf(),
        configure: DSLRule.() -> Unit
    ): DSLRule =
            DSLRule(id).apply(configure).also {
                Executor.rules.add(it)
            }
}
