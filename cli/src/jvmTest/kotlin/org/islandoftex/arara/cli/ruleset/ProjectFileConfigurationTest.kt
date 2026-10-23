// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.ruleset

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.core.files.FileType
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.rules.Directives

class ProjectFileConfigurationTest {
    @Test
    fun headerExtractionUsesTheArgumentInsteadOfTheGlobalExecutorOptions() {
        val source = Files.createTempFile("arara-header", ".tex").toFile()
        val hooks = Directives.hooks
        try {
            DirectiveUtils.initializeDirectiveCore()
            source.writeText("Body text\n% arara: marker\n")
            val file = ProjectFile(MPPPath(source.path), FileType("tex", "^\\s*%\\s+"))
            assertEquals(0, file.fetchDirectives(true).size)
            assertEquals(1, file.fetchDirectives(false).size)
        } finally {
            Directives.hooks = hooks
            source.delete()
        }
    }
}
