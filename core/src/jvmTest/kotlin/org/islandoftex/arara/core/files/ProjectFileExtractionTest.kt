// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.api.rules.DirectiveFetchingHooks
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.LinearExecutor
import org.junit.jupiter.api.io.TempDir

class ProjectFileExtractionTest {
    @TempDir
    lateinit var directory: Path

    private val previousHooks = Directives.hooks
    private val previousOptions = LinearExecutor.executionOptions

    @AfterTest
    fun restore() {
        Directives.hooks = previousHooks
        LinearExecutor.executionOptions = previousOptions
    }

    @Test
    fun headerScopeUsesTheArgumentRatherThanGlobalExecutionOptions() {
        val path = directory.toMPPPath().resolve("main.tex").apply {
            writeText("% arara: header\n\\documentclass{article}\n% arara: body\n")
        }
        Directives.hooks = DirectiveFetchingHooks(buildDirectiveRaw = { id, _, condition, lines ->
            object : Directive {
                override val identifier = id
                override val parameters = emptyMap<String, Any>()
                override val conditional = condition
                override val lineNumbers = lines
                override fun execute() = 0
            }
        })
        val file = ProjectFile(path, FileType("tex", "^%.*"))
        listOf(false, true).forEach { globalHeaderScope ->
            LinearExecutor.executionOptions = ExecutionOptions(parseOnlyHeader = globalHeaderScope)
            assertEquals(listOf("header"), file.fetchDirectives(true).map { it.identifier })
            assertEquals(listOf("header", "body"), file.fetchDirectives(false).map { it.identifier })
            assertEquals(listOf(listOf(1), listOf(3)), file.fetchDirectives(false).map { it.lineNumbers })
        }
    }
}
