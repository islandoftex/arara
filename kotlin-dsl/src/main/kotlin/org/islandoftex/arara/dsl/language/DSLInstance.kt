// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import org.islandoftex.arara.api.files.Project

/**
 * An object holding all public members that are available as top-level
 * objects in DSL files.
 */
object DSLInstance {
    /**
     * The DSL's main task is to provide an easy project configuration format.
     * All projects obtained from build configuration files are collected here.
     */
    internal val projects = mutableListOf<Project>()

    /**
     * Rules are specified in the DSL as well. This saves all rules.
     */
    internal val rules = mutableListOf<DSLRule>()

    fun project(name: String, configure: DSLProject.() -> Unit): Project =
            DSLProject(name).apply(configure).toProject().also {
                projects.add(it)
            }

    fun rule(
        id: String,
        label: String = "",
        description: String = "",
        authors: List<String> = listOf(),
        configure: DSLRule.() -> Unit
    ): DSLRule = DSLRule(id).apply(configure).also {
        rules.add(it)
    }
}
