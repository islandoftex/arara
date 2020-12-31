// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.arara.mvel.files

import kotlinx.serialization.Serializable
import org.islandoftex.arara.api.AraraException
import org.islandoftex.arara.api.files.FileType
import org.islandoftex.arara.core.configuration.ConfigurationUtils
import org.islandoftex.arara.core.localization.LanguageController
import org.islandoftex.arara.core.session.LinearExecutor

/**
 * Implements the file type model.
 *
 * @author Island of TeX
 * @version 5.0
 * @since 4.0
 */
@Serializable
class FileType : FileType {
    override var extension: String = FileType.INVALID_EXTENSION
        private set

    override var pattern: String = FileType.INVALID_PATTERN
        @Throws(AraraException::class)
        get() {
            if (field == FileType.INVALID_PATTERN) {
                field = ConfigurationUtils.defaultFileTypePatterns[extension]
                        ?: throw AraraException(
                            LanguageController.messages
                                .ERROR_FILETYPE_UNKNOWN_EXTENSION.format(
                                        extension,
                                        LinearExecutor.executionOptions.fileTypes
                                                .joinToString(" | ", "[ ", " ]")
                                )
                        )
            }
            return field
        }
        private set

    /**
     * Provides a textual representation of the current file type object.
     * @return A string containing a textual representation of the current file
     * type object.
     */
    override fun toString(): String {
        return ".$extension"
    }

    /**
     * Implements the file type equals method, checking if one file type is
     * equal to another. Note that only the file extension is considered.
     * @param other The object to be analyzed.
     * @return A boolean value indicating if those two objects are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileType
        if (extension != other.extension) return false
        return true
    }

    /**
     * Implements the file type hash code. Note that only the file extension is
     * considered.
     * @return An integer representing the file type hash code.
     */
    override fun hashCode(): Int {
        return extension.hashCode()
    }
}
