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
 * A source file and its execution metadata.
 * Equality includes path, file type and priority; file ownership is validated separately.
 */
open class ProjectFile(
    /** Source path used for directive extraction. */
    override val path: MPPPath,
    /** Filename extension and directive comment pattern. */
    override val fileType: FileType,
    /** Execution priority within the project; higher values run first. */
    override val priority: Int = DEFAULT_PRIORITY
) : ProjectFile {
    /** Default execution metadata for project files. */
    companion object {
        /**
         * This value represents the default priority of a project file. It
         * should be used whenever it is irrelevant which priority a file has
         * (within a project).
         */
        const val DEFAULT_PRIORITY = 0
    }

    /** Compare path, file type and priority. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is org.islandoftex.arara.core.files.ProjectFile) return false

        if (path != other.path) return false
        if (fileType != other.fileType) return false
        if (priority != other.priority) return false

        return true
    }

    /** Hash the same metadata used by [equals]. */
    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + priority
        return result
    }

    /**
     * Read directives from [path], restricting extraction to the header when requested.
     * Read failures are wrapped in [AraraException] with the original cause.
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

    /** Describe the path, file type and priority for diagnostics. */
    override fun toString(): String {
        return "ProjectFile(path=$path, fileType=$fileType, priority=$priority)"
    }
}
