// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.core.files

import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.AraraIOException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.api.files.MPPPath
import org.islandoftex.arara.api.files.ProjectFile
import org.islandoftex.arara.api.rules.Directive
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.rules.Directives

/**
 * Implements the project file model: a file reference paired with its
 * type and a priority deciding the execution order within a project.
 */
open class ProjectFile(
    /**
     * The path of the file within the project.
     */
    override val path: MPPPath,
    /**
     * The type the file was detected as.
     */
    override val fileType: FileType,
    /**
     * The priority deciding the execution order within the project;
     * files with the highest priority run first, and equals run in
     * the sorted order of their resolved paths - the map form of
     * the specification has no declaration order to keep.
     */
    override val priority: Int = DEFAULT_PRIORITY
) : ProjectFile {
    /**
     * Holds the default priority of a project file.
     */
    companion object {
        /**
         * This value represents the default priority of a project file. It
         * should be used whenever it is irrelevant which priority a file has
         * (within a project).
         */
        const val DEFAULT_PRIORITY = 0
    }

    /**
     * Two project files are equal if they agree on path, file type and
     * priority.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is org.islandoftex.arara.core.files.ProjectFile) return false

        if (path != other.path) return false
        if (fileType != other.fileType) return false
        if (priority != other.priority) return false

        return true
    }

    /**
     * The hash code is consistent with [equals].
     */
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + priority
        return result
    }

    /**
     * Extracts the directives of the file, honouring the header-only
     * flag and the comment pattern of the file type.
     */
    override fun fetchDirectives(parseOnlyHeader: Boolean): List<Directive> =
        try {
            Directives.extractDirectives(
                path.readLines(),
                parseOnlyHeader,
                fileType
            )
        } catch (ioexception: AraraIOException) {
            throw AraraException(
                LanguageController.messages.errorExtractorIoError,
                ioexception
            )
        }

    /**
     * A human-readable representation of the project file, for logs and
     * error reports.
     */
    override fun toString(): String {
        return "ProjectFile(path=$path, fileType=$fileType, priority=$priority)"
    }
}
