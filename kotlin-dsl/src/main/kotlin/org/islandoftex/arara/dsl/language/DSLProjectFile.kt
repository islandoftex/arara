// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.nio.file.Path
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.files.UNKNOWN_TYPE

class DSLProjectFile(private val path: Path) {
    var directives: List<String>? = null
    var priority: Int = ProjectFile.DEFAULT_PRIORITY

    fun toProjectFile(): org.islandoftex.arara.api.files.ProjectFile {
        // TODO: use file types from execution
        val fileType = ConfigurationUtils.defaultFileTypes
                .find { it.extension == path.toString().substringAfterLast('.') }
                ?: FileType.UNKNOWN_TYPE
        return org.islandoftex.arara.dsl.files.ProjectFile(path, fileType, priority)
                .apply { directives = this@DSLProjectFile.directives }
    }
}
