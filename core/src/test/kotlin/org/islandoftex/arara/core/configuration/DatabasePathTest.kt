// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.configuration

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.toMPPPath
import org.islandoftex.arara.core.files.Project

class DatabasePathTest {
    @Test
    fun configuredPathsAreResolvedPerProjectAndAbsolutePathsRemainAbsolute() {
        val root = Files.createTempDirectory("arara database paths ").toMPPPath()
        try {
            val first = Project("First", root.resolve("first"), emptySet())
            val second = Project("Second", root.resolve("second"), emptySet())
            val relative = LocalConfiguration(dbname = "cache/../cache/hashes.yaml")
            assertEquals(first.workingDirectory.resolve("cache/hashes.yaml"),
                relative.toExecutionOptions(first).databaseName)
            assertEquals(second.workingDirectory.resolve("cache/hashes.yaml"),
                relative.toExecutionOptions(second).databaseName)
            val absolute = root.resolve("shared/hashes.yaml")
            assertEquals(absolute, LocalConfiguration(dbname = absolute.toString())
                .toExecutionOptions(first).databaseName)
            val base = ExecutionOptions(databaseName = MPPPath("inherited.yaml"))
            assertEquals(base.databaseName, LocalConfiguration().toExecutionOptions(first, base).databaseName)
            assertFailsWith<AraraException> { LocalConfiguration(dbname = " ").toExecutionOptions(first) }
            assertFailsWith<AraraException> { LocalConfiguration(dbname = "bad\u0000path").toExecutionOptions(first) }
        } finally {
            root.removeDirectory()
        }
    }
}
