// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl

import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.templates.standard.SimpleScriptTemplate

object Executor {
    /**
     * The DSL's main task is to provide an easy project configuration format.
     * All projects obtained from build configuration files are collected here.
     */
    internal val projects = mutableListOf<DSLProject>()
    internal val rules = mutableListOf<DSLRule>()

    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting execution")

        val scriptSource = """
            project("name") {
              workingDirectory(".")
              file("file to add", 2)
              file(File("another file"), 1)
              dependsOn("another project")
            }

            project("quack") {
                file("quack")
            }

            rule("id",
                 label="Long label",
                 description="Optional description",
                 authors=listOf("Quack")) {
              argument("identifier") {
                required = true
                type = Int::class // this should be something for advanced user, but we should allow it
                // we also need a way for a choices value etc.
              }
              // command("pdflatex", arguments["identifier"], arguments["quack"])
            }
        """.trimIndent().toScriptSource()

        BasicJvmScriptingHost().evalWithTemplate<SimpleScriptTemplate>(
                scriptSource,
                {
                    updateClasspath(classpathFromClass<Executor>())
                    defaultImports.put(listOf(
                            "java.io.*",
                            "org.islandoftex.arara.dsl.DSLDeclaration.project",
                            "org.islandoftex.arara.dsl.DSLDeclaration.rule"
                    ))
                }
        ).valueOrThrow()

        println(projects)
    }
}
