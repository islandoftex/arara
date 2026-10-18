// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import org.islandoftex.arara.core.configuration.ExecutionOptions
import org.islandoftex.arara.core.session.LinearExecutor
import org.islandoftex.arara.core.session.Session
import org.islandoftex.arara.core.session.HaltRequest
import org.islandoftex.arara.mvel.utils.MvelState
import java.nio.file.Files
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CLIRegressionTest {
    private fun fixture(test: (File) -> Unit) {
        val root = Files.createTempDirectory("arara-cli-regression").toFile()
        val options = LinearExecutor.executionOptions
        val hooks = LinearExecutor.hooks
        val ui = Session.userInterfaceOptions
        val logging = Session.loggingOptions
        val preambles = MvelState.preambles.toMap()
        val defaultPreamble = MvelState.defaultPreamble
        val prepend = MvelState.prependPreambleIfDirectivesGiven
        try {
            Session.clear()
            LinearExecutor.executionOptions = ExecutionOptions()
            test(root)
        } finally {
            LinearExecutor.executionOptions = options
            LinearExecutor.hooks = hooks
            Session.userInterfaceOptions = ui
            Session.loggingOptions = logging
            MvelState.preambles.clear()
            MvelState.preambles.putAll(preambles)
            MvelState.defaultPreamble = defaultPreamble
            MvelState.prependPreambleIfDirectivesGiven = prepend
            Session.clear()
            HaltRequest.clear()
            root.deleteRecursively()
        }
    }

    @Test
    fun bareHaltReachesTheCliWithItsExactStatusAndStopsFollowingDirectives() = fixture { root ->
        File(root, "arararc.yaml").writeText("!config\npaths: ['.']\n")
        File(root, "stop.yaml").writeText("""
            !config
            identifier: stop
            name: Stop
            commands:
              - command: "@{ halt(7); }"
            arguments: []
        """.trimIndent())
        File(root, "run.tex").writeText("% arara: stop\n% arara: missingrule\n")
        val status = assertFailsWith<ProgramResult> { CLI().parse(listOf("-d", root.path, "run.tex")) }
        assertEquals(7, status.statusCode)
    }
}
