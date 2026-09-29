// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue
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
        assertTrue(
            relative.toString().endsWith("pdflatex.lua"),
            "relative rule paths resolve under the working directory"
        )
        val absolute = RuleFiles.construct(
            MPPPath(workingDirectory.toString()), "pdflatex", RuleFormat.LUA,
            MPPPath(workingDirectory.toString())
        )
        assertTrue(
            absolute.toString().endsWith("pdflatex.lua"),
            "absolute rule paths resolve on their own"
        )
    }
}
