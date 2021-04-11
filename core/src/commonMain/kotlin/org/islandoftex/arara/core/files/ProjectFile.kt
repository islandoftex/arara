// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.Directives
import org.islandoftex.arara.core.session.LinearExecutor

@Serializable
open class ProjectFile(
    @Contextual
    override val path: MPPPath,
    override val fileType: FileType,
    override val priority: Int = DEFAULT_PRIORITY
) : ProjectFile {
    companion object {
        /**
         * This value represents the default priority of a project file. It
         * should be used whenever it is irrelevant which priority a file has
         * (within a project).
         */
        const val DEFAULT_PRIORITY = 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is org.islandoftex.arara.core.files.ProjectFile) return false

        if (path != other.path) return false
        if (fileType != other.fileType) return false
        if (priority != other.priority) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + priority
        return result
    }

    override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> =
        try {
            Directives.extractDirectives(
                    path.readLines(),
                    LinearExecutor.executionOptions.parseOnlyHeader,
                    fileType
            )
        } catch (ioexception: AraraIOException) {
            throw AraraException(
                    LanguageController.messages.ERROR_EXTRACTOR_IO_ERROR,
                    ioexception
            )
        }

    override fun toString(): String {
        return "ProjectFile(path=$path, fileType=$fileType, priority=$priority)"
    }
}
