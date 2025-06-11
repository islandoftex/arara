// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl

import org.islandoftex.arara.dsl.language.DSLInstance
import org.islandoftex.arara.dsl.scripting.AraraScriptCompilationConfiguration
import org.islandoftex.arara.dsl.scripting.AraraScriptEvaluationConfiguration
import java.io.File
import java.net.URLDecoder
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

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

        val path =
            URLDecoder.decode(
                Executor::class.java.protectionDomain
                    .codeSource.location.path,
                "UTF-8",
            )
        val scriptSource =
            Paths
                .get(File(path).toURI())
                .parent
                .toAbsolutePath()
                .resolve(
                    "../../../../src/jvmTest/resources/org/islandoftex/arara/dsl/samples/project.kts",
                ).readText()
                .trimIndent()
                .toScriptSource()

        BasicJvmScriptingHost()
            .eval(
                scriptSource,
                AraraScriptCompilationConfiguration(),
                AraraScriptEvaluationConfiguration(),
            ).valueOrThrow()

        println(DSLInstance.projects.size)
        println(DSLInstance.projects)
        println(DSLInstance.rules.size)
        println(DSLInstance.rules)
    }
}
