// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl

import java.io.File
import java.net.URLDecoder
import java.nio.file.Paths
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.updateClasspath
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.templates.standard.SimpleScriptTemplate
import org.islandoftex.arara.api.files.Project
import org.islandoftex.arara.dsl.language.DSLInstance

/**
 * The executor object for projects specified in arara's Kotlin DSL.
 */
object Executor {
    /**
     * Currently, this library is an application with main function (just for
     * testing purposes).
     */
    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting execution")

        val path = URLDecoder.decode(Executor::class.java.protectionDomain
                .codeSource.location.path, "UTF-8")
        val scriptSource = Paths.get(File(path).toURI()).parent.toAbsolutePath()
                .toFile()
                .resolve("../../../src/test/resources/org/islandoftex/arara/dsl/samples/rule.kts")
                .readText().trimIndent().toScriptSource()

        BasicJvmScriptingHost().evalWithTemplate<SimpleScriptTemplate>(
                scriptSource,
                {
                    updateClasspath(classpathFromClass<Executor>())
                    updateClasspath(classpathFromClass<Project>())
                    defaultImports.put(listOf(
                            "java.io.*",
                            DSLInstance::class.qualifiedName + ".project",
                            DSLInstance::class.qualifiedName + ".rule"
                    ))
                }
        ).valueOrThrow()

        println(DSLInstance.projects)
        println(DSLInstance.rules)
    }
}
