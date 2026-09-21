// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli

import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.parse
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs the actual CLI entry point to verify behavior that only manifests
 * through it. [ProjectConfigurationTest] pins the fix for project
 * specifications ignoring configuration files: the `project-config`
 * fixture is only solvable when the configuration of the project is
 * loaded, as its rule is reachable exclusively through the rule paths
 * the configuration provides.
 */
class ProjectConfigurationTest {
    private fun runCLI(vararg args: String): Pair<Int, String> {
        val sysout = System.out
        val output = ByteArrayOutputStream()
        return try {
            System.setOut(PrintStream(output))
            val status = try {
                CLI().parse(args.toList())
                0
            } catch (result: ProgramResult) {
                result.statusCode
            }
            status to output.toByteArray().toString(Charsets.UTF_8)
        } finally {
            System.setOut(sysout)
            output.close()
        }
    }

    @Test
    fun `loads the configuration file for project specification runs`() {
        val (status, output) = runCLI(
            "--working-directory",
            "src/test/resources/executiontests/project-config",
            "project.lua"
        )
        assertEquals(
            0, status,
            "the project run exits successfully"
        )
        // the rule is only reachable through the rule paths provided by
        // the configuration file, and the echoed marker only shows up
        // because the configuration enables verbose output
        assertTrue(
            output.contains("ProjectConfigWorks"),
            "the rule was found through the configured rule paths and " +
                "executed with the configured verbose output"
        )
    }
}
