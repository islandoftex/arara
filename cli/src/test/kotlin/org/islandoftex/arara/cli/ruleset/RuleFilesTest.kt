// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.files.MPPPath

/**
 * Verifies rule file resolution: paths resolve in both the absolute and
 * the relative shape.
 */
class RuleFilesTest {
    @Test
    fun `constructs rule paths in both shapes`() {
        val workingDirectory = Files.createTempDirectory("arara-rule-paths")
        val relative = RuleFiles.construct(
            MPPPath("rules"), "pdflatex", RuleFormat.LUA,
            MPPPath(workingDirectory.toString())
        )
        // the relative base resolves under the working directory -
        // the end alone would not tell a resolved path from a
        // dangling one
        assertEquals(
            MPPPath(workingDirectory.toString()).resolve("rules")
                .resolve("pdflatex.lua").toString(),
            relative.toString(),
            "relative rule paths resolve under the working directory"
        )
        val absolute = RuleFiles.construct(
            MPPPath(workingDirectory.toString()), "pdflatex", RuleFormat.LUA,
            MPPPath(workingDirectory.toString())
        )
        assertEquals(
            MPPPath(workingDirectory.toString()).resolve("pdflatex.lua")
                .toString(),
            absolute.toString(),
            "absolute rule paths resolve on their own"
        )
    }
}
