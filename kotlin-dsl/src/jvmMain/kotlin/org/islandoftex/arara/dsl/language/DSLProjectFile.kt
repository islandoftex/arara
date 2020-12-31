// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.dsl.language

import java.nio.file.Path
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.files.ProjectFile
import org.islandoftex.arara.core.files.UNKNOWN_TYPE

/**
 * A project file model to capture DSL methods within.
 */
class DSLProjectFile(private val path: Path) {
    /**
     * The list of directives this file has. If the user supplies a non-null
     * value, there will be no fetching of directives from the file. The
     * directives are evaluated as if they appeared in the source file.
     */
    var directives: List<String>? = null

    /**
     * The file's priority within a project.
     */
    var priority: Int = ProjectFile.DEFAULT_PRIORITY

    /**
     * Turn this DSL object into arara's core object.
     *
     * @return A [ProjectFile] resembling the user's configuration.
     */
    internal fun toProjectFile(): org.islandoftex.arara.api.files.ProjectFile {
        // TODO: use file types from execution
        val fileType = ConfigurationUtils.defaultFileTypes
                .find { it.extension == path.toString().substringAfterLast('.') }
                ?: FileType.UNKNOWN_TYPE
        return org.islandoftex.arara.dsl.files.ProjectFile(path, fileType, priority)
                .apply { directives = this@DSLProjectFile.directives }
    }
}
