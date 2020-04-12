// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.cli.model

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import org.islandoftex.arara.Arara
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.cli.configuration.AraraSpec
import org.islandoftex.arara.cli.ruleset.DirectiveUtils
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.localization.LanguageController

class ProjectFile(
    path: Path,
    fileType: FileType,
    priority: Int = DEFAULT_PRIORITY
) : ProjectFile(path, fileType, priority) {
    override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> {
        try {
            return DirectiveUtils.extractDirectives(
                    Files.readAllLines(path),
                    Arara.config[AraraSpec.executionOptions].parseOnlyHeader,
                    Arara.config[AraraSpec.Execution.reference].fileType
            )
        } catch (ioexception: IOException) {
            throw AraraException(
                    LanguageController.messages.ERROR_EXTRACTOR_IO_ERROR,
                    ioexception
            )
        }
    }
}
