// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.Project
import org.islandoftex.arara.core.session.Environment

/**
 * Verifies the configuration application: the documented placeholders
 * in the rule paths - the user's home directory and name, and the
 * working directory of the project - expand during the application,
 * script-computed configurations skip that expansion, relative paths
 * resolve against the project, and the database name keeps only its
 * base name.
 */
class LocalConfigurationTest {
    @Test
    fun `expands the path placeholders and resolves relative paths`() {
        val userHome = Environment.getSystemPropertyOrNull("user.home") ?: ""
        val userName = Environment.getSystemPropertyOrNull("user.name") ?: ""
        val directory = Files.createTempDirectory("arara-config-expansion")
        val config = directory.resolve("arararc.yaml")
        Files.writeString(
            config,
            """
            !config
            paths:
              - '@{user.home}/marker-rules'
              - '@{user.name}-rules'
              - '@{application.workingDirectory}/nested-rules'
              - relative-rules
            dbname: 'deep/arara-db.yaml'
            """.trimIndent()
        )
        val workingDirectory = MPPPath(directory.toString())
        val options = LocalConfiguration.load(MPPPath(config.toString()))
            .toExecutionOptions(
                Project("test", workingDirectory, setOf()),
                ExecutionOptions()
            )
        val paths = options.rulePaths
        assertTrue(
            MPPPath("$userHome/marker-rules").normalize() in paths,
            "the user home placeholder expands: $paths"
        )
        assertTrue(
            (workingDirectory / "$userName-rules").normalize() in paths,
            "the user name placeholder expands into a relative path: $paths"
        )
        assertTrue(
            (workingDirectory / "nested-rules").normalize() in paths,
            "the working directory placeholder expands: $paths"
        )
        assertTrue(
            (workingDirectory / "relative-rules").normalize() in paths,
            "relative paths resolve against the project: $paths"
        )
        assertEquals(
            MPPPath("arara-db.yaml"),
            options.databaseName,
            "the database name keeps only its base name"
        )
    }

    @Test
    fun `keeps paths verbatim without token expansion`() {
        val directory = Files.createTempDirectory("arara-config-verbatim")
        val workingDirectory = MPPPath(directory.toString())
        // a script-based configuration computes its own paths, so the
        // model keeps whatever the script produced without treating it
        // as engine token syntax
        val configuration = LocalConfiguration(
            paths = listOf("@{user.home}/marker-rules", "relative-rules"),
            expandPathTokens = false
        )
        val paths = configuration.toExecutionOptions(
            Project("test", workingDirectory, setOf()),
            ExecutionOptions()
        ).rulePaths
        assertTrue(
            (workingDirectory / "@{user.home}/marker-rules").normalize()
                in paths,
            "the script-computed path is kept verbatim: $paths"
        )
        assertTrue(
            (workingDirectory / "relative-rules").normalize() in paths,
            "relative paths still resolve against the project: $paths"
        )
    }
}
